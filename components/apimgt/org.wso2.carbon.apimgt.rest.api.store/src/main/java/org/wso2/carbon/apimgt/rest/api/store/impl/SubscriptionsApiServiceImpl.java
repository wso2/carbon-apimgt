/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.rest.api.store.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.SubscriptionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {

    @Override
    public Response subscriptionsGet(String apiId, String applicationId, String groupId, Integer offset,
            Integer limit, String accept, String ifNoneMatch) {
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
        groupId = RestAPIStoreUtils.getLoggedInUserGroupIds();

        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            SubscriptionListDTO subscriptionListDTO;
            if (!StringUtils.isEmpty(apiId)) {
                // this will fail if user does not have permission to access the API
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
                subscriptions = apiConsumer.getSubscribedIdentifiers(subscriber, apiIdentifier, groupId);
                subscribedAPIList.addAll(subscriptions);
                //todo: sort by application name
                subscriptionListDTO = SubscriptionMappingUtil
                        .fromSubscriptionListToDTO(subscribedAPIList, limit, offset);
                SubscriptionMappingUtil.setPaginationParamsForAPIId(subscriptionListDTO, apiId, groupId, limit, offset,
                        subscriptions.size());
                return Response.ok().entity(subscriptionListDTO).build();
            } else if (!StringUtils.isEmpty(applicationId)) {
                Application application = apiConsumer.getApplicationByUUID(applicationId);
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    subscriptions = apiConsumer
                            .getSubscribedAPIs(subscriber, application.getName(), application.getGroupId());
                    subscribedAPIList.addAll(subscriptions);
                    //todo: sort by api
                    subscriptionListDTO = SubscriptionMappingUtil
                            .fromSubscriptionListToDTO(subscribedAPIList, limit, offset);
                    SubscriptionMappingUtil
                            .setPaginationParamsForApplicationId(subscriptionListDTO, applicationId, limit,
                                    offset, subscriptions.size());
                    return Response.ok().entity(subscriptionListDTO).build();
                } else {
                    throw new ForbiddenException(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT);
                }
            } else {
                //neither apiId nor applicationId is given
                throw new BadRequestException();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response subscriptionsPost(SubscriptionDTO body, String contentType) {
        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIConsumer apiConsumer;
        try {
            //todo: Validation for allowed throttling tiers and Tenant based validation for subscription
            apiConsumer = RestApiUtil.getConsumer(username);
            String apiId = body.getApiId();
            String applicationId = body.getApplicationId();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
            apiIdentifier.setTier(body.getTier());
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            SubscriptionResponse subscriptionResponse =
                    apiConsumer.addSubscription(apiIdentifier, username, application.getId());
            SubscribedAPI addedSubscribedAPI = apiConsumer.getSubscriptionByUUID(
                    subscriptionResponse.getSubscriptionUUID());
            SubscriptionDTO addedSubscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(addedSubscribedAPI);
            return Response
                    .created(new URI(RestApiConstants.RESOURCE_PATH_SUBSCRIPTIONS + "/" + addedSubscribedAPI.getUUID()))
                    .entity(addedSubscriptionDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response subscriptionsSubscriptionIdGet(String subscriptionId, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer = null;
        try {
            apiConsumer = RestApiUtil.getConsumer(username);
            SubscribedAPI subscribedAPI = apiConsumer.getSubscriptionByUUID(subscriptionId);
            if (subscribedAPI != null) {
                SubscriptionDTO subscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(subscribedAPI);
                return Response.ok().entity(subscriptionDTO).build();
            } else {
                throw new NotFoundException();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response subscriptionsSubscriptionIdDelete(String subscriptionId, String ifMatch, String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer = null;
        try {
            apiConsumer = RestApiUtil.getConsumer(username);
            SubscribedAPI subscribedAPI = new SubscribedAPI(subscriptionId);
            apiConsumer.removeSubscription(subscribedAPI);
            return Response.ok().build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
