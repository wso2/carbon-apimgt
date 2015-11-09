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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.SubscriptionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {
    @Override
    public Response subscriptionsGet(String apiId, String applicationId, String groupId, String accept,
            String ifNoneMatch) {
        //todo: validation: only one of {application id,api id} should present
        String username = RestApiUtil.getLoggedInUsername();
        Subscriber subscriber = new Subscriber(username);
        Set<SubscribedAPI> subscriptions = new HashSet<>();
        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            if (!StringUtils.isEmpty(apiId)) {

                /* API api; //todo how can we get this? we cant get this using current username as the provider, may need the admin user of the current tenant
                if (RestApiUtil.isUUID(apiId)) {
                    api = apiProvider.getAPIbyUUID(apiId);
                } else {
                    APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiId(apiId);
                    api = apiProvider.getAPI(apiIdentifier);
                }*/

                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiId(apiId);
                subscriptions = apiConsumer.getSubscribedIdentifiers(subscriber, apiIdentifier, groupId);

            } else if (!StringUtils.isEmpty(applicationId)) {
                Application application = apiConsumer.getApplicationByUUID(applicationId);
                subscriptions =
                        apiConsumer.getSubscribedAPIs(subscriber, application.getName(), application.getGroupId());
            }

            List<SubscriptionDTO> subscriptionDTOs = new ArrayList<>();
            for (SubscribedAPI subscription : subscriptions) {
                SubscriptionDTO subscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(subscription);
                subscriptionDTOs.add(subscriptionDTO);
            }
            return Response.ok().entity(subscriptionDTOs).build();

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response subscriptionsBlockSubscriptionPost(String subscriptionId, String ifMatch,
            String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public Response subscriptionsUnblockSubscriptionPost(String subscriptionId, String ifMatch,
            String ifUnmodifiedSince) {
        return null;
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
    
    /*
    @Override
    public Response subscriptionsSubscriptionIdPut(String subscriptionId, SubscriptionDTO body, String accept,
            String ifNoneMatch, String ifModifiedSince) {

        String username = RestApiUtil.getLoggedInUsername();
        APIProvider apiProvider = null;
        APIConsumer apiConsumer = null;
        try {
            apiProvider = RestApiUtil.getProvider(username);
            SubscribedAPI subscribedAPI = SubscriptionMappingUtil.fromDTOToSubscription(body);
            apiProvider.updateSubscription(subscribedAPI);

            //retrieve the updated Subscription
            apiConsumer = RestApiUtil.getConsumer(username);
            SubscribedAPI updatedSubscribedAPI = apiConsumer.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(updatedSubscribedAPI);
            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }*/
}
