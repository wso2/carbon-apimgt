/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.impl.SubscriptionServiceImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIMonetizationUsageDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for Store subscription related operations
 */
public class SubscriptionsApiServiceImpl implements SubscriptionsApiService {

    private static final Log log = LogFactory.getLog(SubscriptionsApiServiceImpl.class);

    /**
     * Get all subscriptions that are of user or shared subscriptions of the user's group.
     * <p/>
     * If apiId is specified this will return the subscribed applications of that api
     * If application id is specified this will return the api subscriptions of that application
     *
     * @param apiId         api identifier
     * @param applicationId application identifier
     * @param offset        starting index of the subscription list
     * @param limit         max num of subscriptions returned
     * @param ifNoneMatch   If-None-Match header value
     * @return matched subscriptions as a list of SubscriptionDTOs
     */
    @Override
    public Response subscriptionsGet(String apiId, String applicationId, String groupId,
            String xWSO2Tenant, Integer offset, Integer limit, String ifNoneMatch,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        // currently groupId is taken from the user so that groupId coming as a query parameter is not honored.
        // As a improvement, we can check admin privileges of the user and honor groupId.
        groupId = RestApiUtil.getLoggedInUserGroupId();
        SubscriptionListDTO subscriptionListDTO = SubscriptionServiceImpl.getSubscriptions(apiId,
                applicationId, groupId, offset, limit, organization);
        return Response.ok().entity(subscriptionListDTO).build();
    }

    /**
     * Creates a new subscriptions with the details specified in the body parameter
     *
     * @param body new subscription details
     * @return newly added subscription as a SubscriptionDTO if successful
     */
    @Override
    public Response subscriptionsPost(SubscriptionDTO body, String xWSO2Tenant, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String userOrganization = RestApiUtil.getValidatedSubjectOrganization(messageContext);

        SubscriptionDTO addedSubscriptionDTO = SubscriptionServiceImpl.addSubscriptions(body, organization,
                userOrganization);

        if (addedSubscriptionDTO != null) {
            try {
                URI uri = new URI(RestApiConstants.RESOURCE_PATH_SUBSCRIPTIONS + "/"
                        + addedSubscriptionDTO.getSubscriptionId());
                return Response.created(uri).entity(addedSubscriptionDTO).build();
            } catch (URISyntaxException e) {
                if (RestApiUtil.isDueToResourceNotFound(e)) {
                    //this happens when the specified API identifier does not exist
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, body.getApiId(), e, log);
                } else {
                    //unhandled exception
                    RestApiUtil.handleInternalServerError(
                            "Error while adding the subscription API:" + body.getApiId() + ", application:" +
                                    body.getApplicationId() + ", tier:" + body.getThrottlingPolicy(), e, log);
                }
            }
        }
        return null;
    }

    /**
     * Update already created subscriptions with the details specified in the body parameter
     *
     * @param body new subscription details
     * @return newly added subscription as a SubscriptionDTO if successful
     */
    @Override
    public Response subscriptionsSubscriptionIdPut(String subscriptionId, SubscriptionDTO body, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        SubscriptionDTO addedSubscriptionDTO = SubscriptionServiceImpl.updateSubscriptions(subscriptionId,
                body, organization);

        if (addedSubscriptionDTO != null) {
            try {
                return Response.ok(
                        new URI(RestApiConstants.RESOURCE_PATH_SUBSCRIPTIONS + "/" +
                                addedSubscriptionDTO.getSubscriptionId())).entity(addedSubscriptionDTO).build();
            } catch (URISyntaxException e) {
                if (RestApiUtil.isDueToResourceNotFound(e)) {
                    //this happens when the specified API identifier does not exist
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, body.getApiId(), e, log);
                } else {
                    //unhandled exception
                    RestApiUtil.handleInternalServerError(
                            "Error while adding the subscription API:" + body.getApiId() + ", application:" +
                                    body.getApplicationId() + ", tier:" + body.getThrottlingPolicy(), e, log);
                }
            }
        }
        return null;
    }

    /**
     * Create multiple new subscriptions with the list of subscription details specified in the body parameter.
     *
     * @param body list of new subscription details
     * @return list of newly added subscription as a SubscriptionDTO if successful
     */


    @Override
    public Response subscriptionsMultiplePost(List<SubscriptionDTO> body, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        List<SubscriptionDTO> subscriptions = SubscriptionServiceImpl.addMultipleSubscriptions(body, organization);
        return Response.ok().entity(subscriptions).build();
    }

    /**
     * Gets a subscription by identifier
     *
     * @param subscriptionId subscription identifier
     * @param ifNoneMatch    If-None-Match header value
     * @return matched subscription as a SubscriptionDTO
     */
    @Override
    public Response subscriptionsSubscriptionIdGet(String subscriptionId, String ifNoneMatch,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        SubscriptionDTO subscriptionDTO = SubscriptionServiceImpl.getSubscription(subscriptionId,
                organization);
        return Response.ok().entity(subscriptionDTO).build();
    }

    @Override
    public Response subscriptionsSubscriptionIdUsageGet(String subscriptionId, MessageContext messageContext)
            throws APIManagementException {

        APIMonetizationUsageDTO apiMonetizationUsageDTO = SubscriptionServiceImpl
                .getSubscriptionsUsage(subscriptionId);
        return Response.ok().entity(apiMonetizationUsageDTO).build();
    }

    /**
     * Deletes the subscription matched to subscription id
     *
     * @param subscriptionId subscription identifier
     * @param ifMatch        If-Match header value
     * @return 200 response if successfully deleted the subscription
     */
    @Override
    public Response subscriptionsSubscriptionIdDelete(String subscriptionId, String ifMatch,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        SubscribedAPI subscribedAPI = SubscriptionServiceImpl
                .deleteSubscriptionsBySubscriptionId(subscriptionId, organization);
        if (subscribedAPI != null &&
                APIConstants.SubscriptionStatus.DELETE_PENDING.equals(subscribedAPI.getSubStatus())) {
            if (subscribedAPI.getSubscriptionId() == -1) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            return Response.status(Response.Status.CREATED).build();
        }
        return Response.ok().build();
    }

    /**
     * Get additional Info details of subscriptions attached with given API
     *
     * @param apiId         apiId
     * @param offset        starting index of the subscription list
     * @param limit         max num of subscriptions returned
     * @param ifNoneMatch   If-None-Match header value
     * @param messageContext message context
     * @return Response with additional Info of the GraphQL API
     */
    @Override
    public Response getAdditionalInfoOfAPISubscriptions(String apiId, String groupId, String xWSO2Tenant,
            Integer offset, Integer limit, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        AdditionalSubscriptionInfoListDTO additionalSubscriptionInfoListDTO = SubscriptionServiceImpl
                .getAdditionalInfoOfAPISubscriptions(apiId, groupId, offset, limit, organization);
        return Response.ok().entity(additionalSubscriptionInfoListDTO).build();
    }
}
