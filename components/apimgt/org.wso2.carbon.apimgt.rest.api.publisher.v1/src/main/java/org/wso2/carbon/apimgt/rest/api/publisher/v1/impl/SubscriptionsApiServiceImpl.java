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
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.SubscriptionAlreadyExistingException;
import org.wso2.carbon.apimgt.api.SubscriptionBlockedException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;

import org.wso2.carbon.apimgt.impl.workflow.HttpWorkflowResponse;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationUsageDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriberInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.SubscriptionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
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
    public Response blockSubscription(String subscriptionId, String blockState, String ifMatch,
                                                       MessageContext messageContext) {
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            // validates the subscriptionId if it exists
            SubscribedAPI currentSubscription = apiProvider.getSubscriptionByUUID(subscriptionId);

            if (currentSubscription == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
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
    public Response getSubscriptions(String apiId, Integer limit, Integer offset, String ifNoneMatch, String query,
            MessageContext messageContext) {
        // pre-processing
        // setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            SubscriptionListDTO subscriptionListDTO;
            List<SubscribedAPI> apiUsages;

            if (apiId != null) {
                String apiUuid = apiId;
                APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
                if (apiRevision != null && apiRevision.getApiUUID() != null) {
                    apiUuid = apiRevision.getApiUUID();
                }
                apiUsages = apiProvider.getAPIUsageByAPIId(apiUuid, organization);
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
    public Response getSubscriptionUsage(String subscriptionId, MessageContext messageContext) {

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
    public Response unBlockSubscription(String subscriptionId, String ifMatch,
            MessageContext messageContext) {
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);

            // validates the subscriptionId if it exists
            SubscribedAPI currentSubscription = apiProvider.getSubscriptionByUUID(subscriptionId);
            if (currentSubscription == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
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
    public Response getSubscriberInfoBySubscriptionId(String subscriptionId, MessageContext messageContext)
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

    /**
     * Update already created subscriptions with the details specified in the body parameter
     *
     * @param body new subscription details
     * @return newly added subscription as a SubscriptionDTO if successful
     */
//    @Override
    public Response subscriptionsSubscriptionIdPut(String subscriptionId, SubscriptionDTO body, String xWSO2Tenant, String apiId, String businessPlan,
                                                   MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
//        APIConsumer apiConsumer;
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        SubscribedAPI currentSubscription = apiProvider.getSubscriptionByUUID(subscriptionId);
//            Application application = apiConsumer.getApplicationByUUID(applicationId);
        Application application = currentSubscription.getApplication();
        String applicationId = application.getUUID();

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
//            apiConsumer = RestApiCommonUtil.getConsumer(username);

//            String applicationId = body.getApplicationId();
            String currentThrottlingPolicy = body.getThrottlingPolicy();
//            String requestedThrottlingPolicy = body.getRequestedThrottlingPolicy();

            SubscribedAPI subscribedAPI = apiProvider.getSubscriptionByUUID(subscriptionId);
            //Check whether the subscription status is not empty and also not blocked
//            if (body.getStatus() != null && subscribedAPI != null) {
//                if ("BLOCKED".equals(body.getStatus().value()) || "ON_HOLD".equals(body.getStatus().value())
//                        || "REJECTED".equals(body.getStatus().value()) || "BLOCKED".equals(subscribedAPI.getSubStatus())
//                        || "ON_HOLD".equals(subscribedAPI.getSubStatus())
//                        || "REJECTED".equals(subscribedAPI.getSubStatus())) {
//                    RestApiUtil.handleBadRequest(
//                            "Cannot update subscriptions with provided or existing status", log);
//                    return null;
//                }
//            } else {
//                RestApiUtil.handleBadRequest(
//                        "Request must contain status of the subscription", log);
//                return null;
//            }

            if (subscribedAPI.getSubCreatedStatus() != null && subscribedAPI != null) {
                if ("BLOCKED".equals(subscribedAPI.getSubCreatedStatus()) || "ON_HOLD".equals(subscribedAPI.getSubCreatedStatus())
                        || "REJECTED".equals(subscribedAPI.getSubCreatedStatus()) || "BLOCKED".equals(subscribedAPI.getSubStatus())
                        || "ON_HOLD".equals(subscribedAPI.getSubStatus())
                        || "REJECTED".equals(subscribedAPI.getSubStatus())) {
                    RestApiUtil.handleBadRequest(
                            "Cannot update subscriptions with provided or existing status", log);
                    return null;
                }
            } else {
                RestApiUtil.handleBadRequest(
                        "Request must contain status of the subscription", log);
                return null;
            }

            //check whether user is permitted to access the API. If the API does not exist,
            // this will throw a APIMgtResourceNotFoundException
            if (apiId != null) {
                if (!RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(apiId, organization)) {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, log);
                }
            } else {
                RestApiUtil.handleBadRequest(
                        "Request must contain either apiIdentifier or apiProductIdentifier and the relevant type", log);
                return null;
            }


            if (application == null) {
                //required application not found
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                return null;
            }

            if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                //application access failure occurred
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }

            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);


            apiTypeWrapper.setTier(body.getThrottlingPolicy());

            SubscriptionResponse subscriptionResponse = apiProvider
                    .updateSubscription(apiTypeWrapper, username, application, subscriptionId, businessPlan);
            SubscribedAPI addedSubscribedAPI = apiProvider
                    .getSubscriptionByUUID(subscriptionResponse.getSubscriptionUUID());
            SubscriptionDTO addedSubscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(addedSubscribedAPI);
            WorkflowResponse workflowResponse = subscriptionResponse.getWorkflowResponse();
            if (workflowResponse instanceof HttpWorkflowResponse) {
                String payload = workflowResponse.getJSONPayload();
//                addedSubscriptionDTO.setRedirectionParams(payload);
            }

            return Response.ok(new URI(RestApiConstants.RESOURCE_PATH_SUBSCRIPTIONS + "/" +
                    addedSubscribedAPI.getUUID())).entity(addedSubscriptionDTO).build();

        } catch (APIMgtAuthorizationFailedException e) {
            //this occurs when the api:application:tier mapping is not allowed. The reason for the message is taken from
            // the message of the exception e
            RestApiUtil.handleAuthorizationFailure(e.getMessage(), e, log);
        } catch (SubscriptionAlreadyExistingException e) {
            RestApiUtil.handleResourceAlreadyExistsError(
                    "Specified subscription already exists for API " + apiId + ", for application "
                            + applicationId, e, log);
        } catch (SubscriptionBlockedException e) {
            RestApiUtil.handleOperationBlockedError("Subscription blocked. " + e.getMessage()
                    + ". Please contact the API publisher.", e, log);
        } catch (APIManagementException | URISyntaxException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                //this happens when the specified API identifier does not exist
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                //unhandled exception
                RestApiUtil.handleInternalServerError(
                        "Error while adding the subscription API:" + apiId + ", application:" + applicationId + ", tier:" + body.getThrottlingPolicy(), e, log);
            }
        }
        return null;
    }

    @Override
    public Response changeSubscriptionBusinessPlan(String subscriptionId, String businessPlan, String ifMatch,
                                                   MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        SubscribedAPI currentSubscription = apiProvider.getSubscriptionByUUID(subscriptionId);
        Application application = currentSubscription.getApplication();
        String apiId = currentSubscription.getAPIUUId();
        String applicationId = application.getUUID();

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            // Check whether the subscription status is not empty and also not blocked
            if (currentSubscription != null && currentSubscription.getSubCreatedStatus() != null) {
                if ("BLOCKED".equals(currentSubscription.getSubCreatedStatus()) || "ON_HOLD".equals(currentSubscription.getSubCreatedStatus()) ||
                        "REJECTED".equals(currentSubscription.getSubCreatedStatus()) || "BLOCKED".equals(currentSubscription.getSubStatus()) ||
                        "ON_HOLD".equals(currentSubscription.getSubStatus()) || "REJECTED".equals(currentSubscription.getSubStatus())) {
                    RestApiUtil.handleBadRequest("Cannot update subscriptions with provided or existing status", log);
                    return null;
                }
            } else {
                RestApiUtil.handleBadRequest("Request must contain status of the subscription", log);
                return null;
            }

            if (application == null) {
                // Required application not found
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                return null;
            }

            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
            apiTypeWrapper.setTier(businessPlan);

            SubscriptionResponse subscriptionResponse = apiProvider.updateSubscription(apiTypeWrapper, username, application, subscriptionId, businessPlan);
            SubscribedAPI updatedSubscription = apiProvider.getSubscriptionByUUID(subscriptionResponse.getSubscriptionUUID());
            SubscriptionDTO addedSubscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(updatedSubscription);

            return Response.ok(new URI(RestApiConstants.RESOURCE_PATH_SUBSCRIPTIONS + "/" + updatedSubscription.getUUID()))
                    .entity(addedSubscriptionDTO)
                    .build();

        } catch (APIMgtAuthorizationFailedException e) {
            // This occurs when the api:application:tier mapping is not allowed. The reason for the message is taken from the message of the exception e
            RestApiUtil.handleAuthorizationFailure(e.getMessage(), e, log);
        } catch (SubscriptionAlreadyExistingException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Specified subscription already exists for API " + apiId + ", for application " + applicationId, e, log);
        } catch (APIManagementException | URISyntaxException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                // This happens when the specified API identifier does not exist
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                // Unhandled exception
                RestApiUtil.handleInternalServerError("Error while adding the subscription API:" + apiId + ", application:" + applicationId + ", tier:" + businessPlan, e, log);
            }
        }
        return null;
    }
}
