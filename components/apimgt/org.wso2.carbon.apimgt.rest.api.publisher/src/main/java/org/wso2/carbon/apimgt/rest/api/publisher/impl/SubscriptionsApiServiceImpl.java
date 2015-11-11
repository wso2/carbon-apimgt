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

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.SubscriptionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;
import javax.ws.rs.core.Response;


/** This is the service implementation class for Publisher subscriptions related operations
 * 
 */
public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {

    /** Retieves all subscriptions or retrieves subscriptions for a given API Id
     * 
     * @param apiId API identifier
     * @param limit max number of objects returns
     * @param offset starting index
     * @param accept accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted subscriptions
     */
    @Override
    public Response subscriptionsGet(String apiId, Integer limit, Integer offset, String accept,
            String ifNoneMatch) {

        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            SubscriptionListDTO subscriptionListDTO;
            if (apiId != null) {
                //todo: support groupId
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
                List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(apiIdentifier);
                subscriptionListDTO = SubscriptionMappingUtil.fromSubscriptionListToDTO(apiUsages, limit, offset);
                SubscriptionMappingUtil.setPaginationParams(subscriptionListDTO, apiId, "", limit, offset,
                        apiUsages.size());
            } else {
                //todo: support groupId
                UserApplicationAPIUsage[] allApiUsage = apiProvider.getAllAPIUsageByProvider(username);
                subscriptionListDTO = SubscriptionMappingUtil.fromUserApplicationAPIUsageArrayToDTO(allApiUsage, limit,
                        offset);
                SubscriptionMappingUtil.setPaginationParams(subscriptionListDTO, "", "", limit, offset,
                        allApiUsage.length);
            }
            return Response.ok().entity(subscriptionListDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response subscriptionsBlockSubscriptionPost(String subscriptionId, String blockState, String ifMatch,
            String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        APIProvider apiProvider;
        try {
            apiProvider = RestApiUtil.getProvider(username);
            SubscribedAPI subscribedAPI = new SubscribedAPI(subscriptionId);
            subscribedAPI.setSubStatus(blockState);
            apiProvider.updateSubscription(subscribedAPI);

            SubscribedAPI updatedSubscribedAPI = apiProvider.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(updatedSubscribedAPI);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response subscriptionsUnblockSubscriptionPost(String subscriptionId, String ifMatch,
            String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        APIProvider apiProvider;
        try {
            apiProvider = RestApiUtil.getProvider(username);
            SubscribedAPI subscribedAPI = new SubscribedAPI(subscriptionId);
            subscribedAPI.setSubStatus(APIConstants.SubscriptionStatus.UNBLOCKED);
            apiProvider.updateSubscription(subscribedAPI);

            SubscribedAPI updatedSubscribedAPI = apiProvider.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(updatedSubscribedAPI);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response subscriptionsSubscriptionIdGet(String subscriptionId, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        APIProvider apiProvider;
        try {
            apiProvider = RestApiUtil.getProvider(username);
            SubscribedAPI subscribedAPI = apiProvider.getSubscriptionByUUID(subscriptionId);
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

}
