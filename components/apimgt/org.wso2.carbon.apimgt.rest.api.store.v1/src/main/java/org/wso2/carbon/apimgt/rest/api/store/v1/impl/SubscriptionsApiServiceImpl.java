/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.SubscriptionAlreadyExistingException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.rest.api.store.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO.TypeEnum;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.SubscriptionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
    public Response subscriptionsGet(String apiId, String applicationId, String apiType, Integer offset, Integer limit,
            String ifNoneMatch, MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        Subscriber subscriber = new Subscriber(username);
        Set<SubscribedAPI> subscriptions;
        List<SubscribedAPI> subscribedAPIList = new ArrayList<>();

        //pre-processing
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        // currently groupId is taken from the user so that groupId coming as a query parameter is not honored.
        // As a improvement, we can check admin privileges of the user and honor groupId.
        String groupId = RestApiUtil.getLoggedInUserGroupId();

        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            SubscriptionListDTO subscriptionListDTO;
            if (!StringUtils.isEmpty(apiId)) {
                // todo : FIX properly, need to done properly with backend side pagination. 
                // todo : getSubscribedIdentifiers() method should NOT be used. Appears to be too slow. 
                
                // This will fail with an authorization failed exception if user does not have permission to access the API
                API api = apiConsumer.getLightweightAPIByUUID(apiId, tenantDomain);
                subscriptions = apiConsumer.getSubscribedIdentifiers(subscriber, api.getId(), groupId);
                //sort by application name
                subscribedAPIList.addAll(subscriptions);
                subscribedAPIList.sort(Comparator.comparing(o -> o.getApplication().getName()));

                subscriptionListDTO = SubscriptionMappingUtil
                        .fromSubscriptionListToDTO(subscribedAPIList, limit, offset);
                
                return Response.ok().entity(subscriptionListDTO).build();
            } else if (!StringUtils.isEmpty(applicationId)) {
                Application application = apiConsumer.getApplicationByUUID(applicationId);

                if (application == null) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                    return null;
                }

                if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }

                subscriptions = apiConsumer
                        .getPaginatedSubscribedAPIs(subscriber, application.getName(), offset, limit, groupId);
                subscribedAPIList.addAll(subscriptions);
                
                subscriptionListDTO = SubscriptionMappingUtil.fromSubscriptionListToDTO(subscribedAPIList, limit,
                        offset);
                return Response.ok().entity(subscriptionListDTO).build();

            } else {
                //neither apiId nor applicationId is given
                RestApiUtil.handleBadRequest("Either applicationId or apiId should be available", log);
                return null;
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while getting subscriptions of the user " + username, e,
                        log);
            }
        }
        return null;
    }

    /**
     * Creates a new subscriptions with the details specified in the body parameter
     *
     * @param body        new subscription details
     * @return newly added subscription as a SubscriptionDTO if successful
     */
    @Override
    public Response subscriptionsPost(SubscriptionDTO body, MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIConsumer apiConsumer;
        Identifier identifier;
        try {
            apiConsumer = RestApiUtil.getConsumer(username);
            String applicationId = body.getApplicationId();
            
            //check whether user is permitted to access the API. If the API does not exist, 
            // this will throw a APIMgtResourceNotFoundException
            if (body.getType() != null && TypeEnum.API == body.getType() && body.getApiId() != null) {
                if (!RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(body.getApiId(), tenantDomain)) {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, body.getApiId(), log);
                }
                identifier = RestAPIStoreUtils.getAPIIdentifierFromUUID(body.getApiId(), tenantDomain);
            } else if (body.getType() != null && TypeEnum.APIPRODUCT == body.getType()
                    && body.getApiProductId() != null) {

                String uuid = body.getApiProductId();
                APIProduct product = apiConsumer.getAPIProductbyUUID(uuid, tenantDomain);
                if(product == null) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, uuid, log);
                    return null;
                }
                if (!RestAPIStoreUtils.isUserAccessAllowedForAPIProduct(product)) {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API_PRODUCT, uuid, log);
                }

                APIProductIdentifier prodIdentifier = new APIProductIdentifier(product.getId().getProviderName(),
                        product.getId().getName(), product.getId().getVersion());
                prodIdentifier.setUUID(uuid);
                prodIdentifier.setProductId(product.getProductId());
                identifier = prodIdentifier;
            } else {
                RestApiUtil.handleBadRequest(
                        "Request must contain either apiIdentifier or apiProductIdentifier and the relevant type", log);
                return null;
            }

            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application == null) {
                //required application not found
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                return null;
            }

            if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                //application access failure occurred
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }

            //Validation for allowed throttling tiers and Tenant based validation for subscription. If failed this will
            //  throw an APIMgtAuthorizationFailedException with the reason as the message
            RestAPIStoreUtils.checkSubscriptionAllowed(identifier, body.getThrottlingPolicy());

            identifier.setTier(body.getThrottlingPolicy());

            SubscriptionResponse subscriptionResponse = apiConsumer
                    .addSubscription(identifier, username, application.getId());
            SubscribedAPI addedSubscribedAPI = apiConsumer
                    .getSubscriptionByUUID(subscriptionResponse.getSubscriptionUUID());
            SubscriptionDTO addedSubscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(addedSubscribedAPI);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_SUBSCRIPTIONS + "/" + addedSubscribedAPI.getUUID())).entity(
                    addedSubscriptionDTO).build();

        } catch (APIMgtAuthorizationFailedException e) {
            //this occurs when the api:application:tier mapping is not allowed. The reason for the message is taken from
            // the message of the exception e
            RestApiUtil.handleAuthorizationFailure(e.getMessage(), e, log);
        } catch (SubscriptionAlreadyExistingException e) {
            String id =  "";
            if(!StringUtils.isEmpty(body.getApiId())){
               id = "API " + body.getApiId(); 
            } else if (!StringUtils.isEmpty(body.getApiProductId())) {
                id = "Product " + body.getApiProductId(); 
            }
            RestApiUtil.handleResourceAlreadyExistsError(
                    "Specified subscription already exists for " + id + " for application "
                            + body.getApplicationId(), e, log);
        } catch (APIManagementException | URISyntaxException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                //this happens when the specified API identifier does not exist
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, body.getApiId(), e, log);
            } else {
                //unhandled exception
                RestApiUtil.handleInternalServerError(
                        "Error while adding the subscription API:" + body.getApiId() + ", application:" + body
                                .getApplicationId() + ", tier:" + body.getThrottlingPolicy(), e, log);
            }
        }
        return null;
    }

    /**
     * Create multiple new subscriptions with the list of subscription details specified in the body parameter.
     *
     * @param body        list of new subscription details
     * @return list of newly added subscription as a SubscriptionDTO if successful
     */
    @Override
    public Response subscriptionsMultiplePost(List<SubscriptionDTO> body, MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        List<SubscriptionDTO> subscriptions = new ArrayList<>();
        for (SubscriptionDTO subscriptionDTO : body) {
            try {
                APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
                String applicationId = subscriptionDTO.getApplicationId();
                APIIdentifier apiIdentifier = APIMappingUtil
                        .getAPIIdentifierFromUUID(subscriptionDTO.getApiId(), tenantDomain);

                //check whether user is permitted to access the API. If the API does not exist,
                // this will throw a APIMgtResourceNotFoundException
                if (!org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils
                        .isUserAccessAllowedForAPI(apiIdentifier)) {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API,
                            subscriptionDTO.getApiId(), log);
                }

                Application application = apiConsumer.getApplicationByUUID(applicationId);
                if (application == null) {
                    //required application not found
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }

                if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    //application access failure occurred
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }

                //Validation for allowed throttling tiers and Tenant based validation for subscription. If failed this
                // will throw an APIMgtAuthorizationFailedException with the reason as the message
                RestAPIStoreUtils.checkSubscriptionAllowed(apiIdentifier, subscriptionDTO.getThrottlingPolicy());

                apiIdentifier.setTier(subscriptionDTO.getThrottlingPolicy());
                SubscriptionResponse subscriptionResponse = apiConsumer
                        .addSubscription(apiIdentifier, username, application.getId());
                SubscribedAPI addedSubscribedAPI = apiConsumer
                        .getSubscriptionByUUID(subscriptionResponse.getSubscriptionUUID());
                SubscriptionDTO addedSubscriptionDTO =
                        SubscriptionMappingUtil.fromSubscriptionToDTO(addedSubscribedAPI);
                subscriptions.add(addedSubscriptionDTO);

            } catch (APIMgtAuthorizationFailedException e) {
                //this occurs when the api:application:tier mapping is not allowed. The reason for the message is
                // taken from the message of the exception e
                RestApiUtil.handleAuthorizationFailure(e.getMessage(), e, log);
            } catch (SubscriptionAlreadyExistingException e) {
                RestApiUtil.handleResourceAlreadyExistsError(
                        "Specified subscription already exists for API " + subscriptionDTO.getApiId() +
                                " for application " + subscriptionDTO.getApplicationId(), e, log);
            } catch (APIManagementException e) {
                if (RestApiUtil.isDueToResourceNotFound(e)) {
                    //this happens when the specified API identifier does not exist
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, subscriptionDTO.getApiId(),
                            e, log);
                } else {
                    //unhandled exception
                    RestApiUtil.handleInternalServerError(
                            "Error while adding the subscription API:" + subscriptionDTO.getApiId() +
                                    ", application:" + subscriptionDTO.getApplicationId() + ", throttling policy:" +
                                    subscriptionDTO.getThrottlingPolicy(), e, log);
                }
            }
        }
        return Response.ok().entity(subscriptions).build();
    }

    /**
     * Gets a subscription by identifier
     *
     * @param subscriptionId  subscription identifier
     * @param ifNoneMatch     If-None-Match header value
     * @return matched subscription as a SubscriptionDTO
     */
    @Override
    public Response subscriptionsSubscriptionIdGet(String subscriptionId, String ifNoneMatch,
            MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer;
        try {
            apiConsumer = RestApiUtil.getConsumer(username);
            SubscribedAPI subscribedAPI = validateAndGetSubscription(subscriptionId, apiConsumer);

            SubscriptionDTO subscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(subscribedAPI);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while getting subscription with id " + subscriptionId, e, log);
        }
        return null;
    }

    /**
     * Deletes the subscription matched to subscription id
     *
     * @param subscriptionId    subscription identifier
     * @param ifMatch           If-Match header value
     * @return 200 response if successfully deleted the subscription
     */
    @Override
    public Response subscriptionsSubscriptionIdDelete(String subscriptionId, String ifMatch,
            MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer;
        try {
            apiConsumer = RestApiUtil.getConsumer(username);
            SubscribedAPI subscribedAPI = validateAndGetSubscription(subscriptionId, apiConsumer);
            
            apiConsumer.removeSubscription(subscribedAPI);
            return Response.ok().build();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while deleting subscription with id " + subscriptionId, e, log);
        }
        return null;
    }

    private SubscribedAPI validateAndGetSubscription(String subscriptionId, APIConsumer apiConsumer)
            throws APIManagementException {
        SubscribedAPI subscribedAPI = apiConsumer.getSubscriptionByUUID(subscriptionId);
        if (subscribedAPI == null) {
            RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
        }
        if (!RestAPIStoreUtils.isUserAccessAllowedForSubscription(subscribedAPI)) {
            RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
        }
        return subscribedAPI;
    }
}
