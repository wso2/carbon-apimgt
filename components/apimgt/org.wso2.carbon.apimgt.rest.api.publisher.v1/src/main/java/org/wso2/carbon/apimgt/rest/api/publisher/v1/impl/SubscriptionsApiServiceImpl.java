/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;

import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationUsageDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriberInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.SubscriptionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl implements SubscriptionsApiService {

    private static final Log log = LogFactory.getLog(SubscriptionsApiServiceImpl.class);

    /**
     * Blocks a subscription
     *
     * @param subscriptionId Subscription identifier
     * @param blockState block state; either BLOCKED or PROD_ONLY_BLOCKED
     * @param ifMatch If-Match header value
     * @return 200 response and the updated subscription if subscription block is successful
     */
    public Response subscriptionsBlockSubscriptionPost(String subscriptionId, String blockState, String ifMatch,
                                                       MessageContext messageContext) {
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            // validates the subscriptionId if it exists
            SubscribedAPI currentSubscription = apiProvider.getSubscriptionByUUID(subscriptionId);

            if (currentSubscription == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
            }

            Application subscribedApp = currentSubscription.getApplication();
            String applicationTokenType = "OAUTH";
            if (subscribedApp != null) {
                applicationTokenType = subscribedApp.getTokenType();
            }

            //in case of a JWT type application add a subscription blocking condition as well.
            if (APIConstants.APPLICATION_TOKEN_TYPE_JWT.equals(applicationTokenType)) {
                Identifier apiId = currentSubscription.getApiId();
                if (apiId == null) {
                    apiId = currentSubscription.getProductId();
                }

                String apiContext = "";
                String apiVersion = "";
                if (apiId instanceof APIIdentifier) {
                    apiContext = apiProvider.getAPIContext((APIIdentifier) apiId);
                    apiVersion = apiId.getVersion();
                } else if (apiId instanceof APIProductIdentifier) {
                    APIProduct product = apiProvider.getAPIProduct((APIProductIdentifier) apiId);
                    apiContext = product.getContext();
                    //until product versioning is supported, we will be adding default api product version to the
                    // blacklist condition key
                    apiVersion = APIConstants.API_PRODUCT_VERSION;
                }

                String appId = subscribedApp.getOwner() + "-" + subscribedApp.getName();
                String substatus = currentSubscription.getSubStatus();

                String productionBlockConditionKey =
                        apiContext + ":" + apiVersion + ":" + appId + ":" + APIConstants.API_KEY_TYPE_PRODUCTION;
                String sandboxBlockConditionKey =
                        apiContext + ":" + apiVersion + ":" + appId + ":" + APIConstants.API_KEY_TYPE_SANDBOX;

                //delete existing block conditions
                apiProvider.deleteSubscriptionBlockCondition(productionBlockConditionKey);
                apiProvider.deleteSubscriptionBlockCondition(sandboxBlockConditionKey);

                if (APIConstants.SubscriptionStatus.BLOCKED.equals(substatus)) {
                    /*In case all subscriptions blocked, add block conditions for both sandbox and production
                    key types*/
                    apiProvider.addBlockCondition(APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION,
                            productionBlockConditionKey);
                    apiProvider
                            .addBlockCondition(APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION, sandboxBlockConditionKey);
                } else {
                    /*In case production only blocked add a blocking condition only for production type*/
                    apiProvider.addBlockCondition(APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION,
                            productionBlockConditionKey);
                }
            }

            SubscribedAPI subscribedAPI = new SubscribedAPI(subscriptionId);
            subscribedAPI.setSubStatus(blockState);
            apiProvider.updateSubscription(subscribedAPI);

            SubscribedAPI updatedSubscription = apiProvider.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(updatedSubscription);

            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            String msg = "Error while blocking the subscription " + subscriptionId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        }

        return null;
    }

    /**
     * Retrieves all subscriptions or retrieves subscriptions for a given API Id
     *
     * @param apiId API identifier
     * @param limit max number of objects returns
     * @param offset starting index
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted subscriptions
     */
    public Response subscriptionsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch, String query,
            MessageContext messageContext) {
        // pre-processing
        // setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            SubscriptionListDTO subscriptionListDTO;
            List<SubscribedAPI> apiUsages;

            if (apiId != null) {
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
                apiUsages = apiProvider.getAPIUsageByAPIId(apiIdentifier);
            } else {
                UserApplicationAPIUsage[] allApiUsage = apiProvider.getAllAPIUsageByProvider(username);
                apiUsages = SubscriptionMappingUtil.fromUserApplicationAPIUsageArrayToSubscribedAPIList(allApiUsage);
            }

            if (query != null && !query.isEmpty()) {
                SubscriptionListDTO filteredSubscriptionList = SubscriptionMappingUtil
                        .fromSubscriptionListToDTO(apiUsages, query);
                subscriptionListDTO =
                        SubscriptionMappingUtil.getPaginatedSubscriptions(filteredSubscriptionList, limit, offset);
                SubscriptionMappingUtil.setPaginationParams(subscriptionListDTO, apiId, "", limit,
                        offset, filteredSubscriptionList.getCount());
            } else {
                subscriptionListDTO = SubscriptionMappingUtil.fromSubscriptionListToDTO(apiUsages, limit, offset);
                SubscriptionMappingUtil.setPaginationParams(subscriptionListDTO, apiId, "", limit, offset,
                        apiUsages.size());
            }

            return Response.ok().entity(subscriptionListDTO).build();
        } catch (APIManagementException e) {
            // Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String msg = "Error while retrieving subscriptions of API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }

        return null;
    }

    /**
     * Get monetization usage data for a subscription
     *
     * @param subscriptionId subscription Id
     * @param messageContext message context
     * @return monetization usage data for a subscription
     */
    @Override
    public Response subscriptionsSubscriptionIdUsageGet(String subscriptionId, MessageContext messageContext) {

        if (StringUtils.isBlank(subscriptionId)) {
            String errorMessage = "Subscription ID cannot be empty or null when getting monetization usage.";
            RestApiUtil.handleBadRequest(errorMessage, log);
        }
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            Map<String, String> billingEngineUsageData = monetizationImplementation.
                    getCurrentUsageForSubscription(subscriptionId, apiProvider);
            if (MapUtils.isEmpty(billingEngineUsageData)) {
                String errorMessage = "Billing engine usage data was not found for subscription ID : " + subscriptionId;
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            APIMonetizationUsageDTO apiMonetizationUsageDTO = new APIMonetizationUsageDTO();
            apiMonetizationUsageDTO.setProperties(billingEngineUsageData);
            return Response.ok().entity(apiMonetizationUsageDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Failed to retrieve billing engine usage data for subscription ID : " + subscriptionId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (MonetizationException e) {
            String errorMessage = "Failed to get current usage for subscription ID : " + subscriptionId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Unblocks a subscription
     *
     * @param subscriptionId subscription identifier
     * @param ifMatch If-Match header value
     * @return 200 response and the updated subscription if subscription block is successful
     */
    public Response subscriptionsUnblockSubscriptionPost(String subscriptionId, String ifMatch,
            MessageContext messageContext) {
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);

            // validates the subscriptionId if it exists
            SubscribedAPI currentSubscription = apiProvider.getSubscriptionByUUID(subscriptionId);
            if (currentSubscription == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
            }

            Application subscribedApp = currentSubscription.getApplication();
            String applicationTokenType = "OAUTH";
            if (subscribedApp != null) {
                applicationTokenType = subscribedApp.getTokenType();
            }

            //in case of a JWT type application remove the subscription blocking conditions if exist
            if (APIConstants.APPLICATION_TOKEN_TYPE_JWT.equals(applicationTokenType)) {
                Identifier apiId = currentSubscription.getApiId();
                if (apiId == null) {
                    apiId = currentSubscription.getProductId();
                }

                String apiContext = "";
                String apiVersion = "";
                if (apiId instanceof APIIdentifier) {
                    apiContext = apiProvider.getAPIContext((APIIdentifier) apiId);
                    apiVersion = apiId.getVersion();
                } else if (apiId instanceof  APIProductIdentifier) {
                    APIProduct product = apiProvider.getAPIProduct((APIProductIdentifier) apiId);
                    apiContext = product.getContext();
                    apiVersion = APIConstants.API_PRODUCT_VERSION;
                }

                String appId = subscribedApp.getOwner() + "-" + subscribedApp.getName();

                //delete existing block conditions
                String productionBlockConditionKey =
                        apiContext + ":" + apiVersion + ":" + appId + ":" + APIConstants.API_KEY_TYPE_PRODUCTION;
                String sandboxBlockConditionKey =
                        apiContext + ":" + apiVersion + ":" + appId + ":" + APIConstants.API_KEY_TYPE_SANDBOX;
                apiProvider.deleteSubscriptionBlockCondition(productionBlockConditionKey);
                apiProvider.deleteSubscriptionBlockCondition(sandboxBlockConditionKey);
            }

            SubscribedAPI subscribedAPI = new SubscribedAPI(subscriptionId);
            subscribedAPI.setSubStatus(APIConstants.SubscriptionStatus.UNBLOCKED);
            apiProvider.updateSubscription(subscribedAPI);

            SubscribedAPI updatedSubscribedAPI = apiProvider.getSubscriptionByUUID(subscriptionId);
            SubscriptionDTO subscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(updatedSubscribedAPI);

            return Response.ok().entity(subscriptionDTO).build();
        } catch (APIManagementException e) {
            String msg = "Error while unblocking the subscription " + subscriptionId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        }

        return null;
    }

    @Override
    public Response subscriptionsSubscriptionIdSubscriberInfoGet(String subscriptionId, MessageContext messageContext)
            throws APIManagementException {
        if (StringUtils.isBlank(subscriptionId)) {
            String errorMessage = "Subscription ID cannot be empty or null when getting subscriber info.";
            RestApiUtil.handleBadRequest(errorMessage, log);
        }
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        String subscriberName = apiProvider.getSubscriber(subscriptionId);
        Map subscriberClaims = apiProvider.getSubscriberClaims(subscriberName);
        SubscriberInfoDTO subscriberInfoDTO = SubscriptionMappingUtil.fromSubscriberClaimsToDTO(subscriberClaims,
                subscriberName);
        return Response.ok().entity(subscriberInfoDTO).build();
    }
}
