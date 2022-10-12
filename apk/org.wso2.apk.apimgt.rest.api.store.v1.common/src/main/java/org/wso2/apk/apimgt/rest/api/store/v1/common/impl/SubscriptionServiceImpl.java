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

package org.wso2.apk.apimgt.rest.api.store.v1.common.impl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.apimgt.api.APIConsumer;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.MonetizationException;
import org.wso2.apk.apimgt.api.SubscriptionAlreadyExistingException;
import org.wso2.apk.apimgt.api.WorkflowResponse;
import org.wso2.apk.apimgt.api.WorkflowStatus;
import org.wso2.apk.apimgt.api.model.ApiTypeWrapper;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.Monetization;
import org.wso2.apk.apimgt.api.model.SubscribedAPI;
import org.wso2.apk.apimgt.api.model.Subscriber;
import org.wso2.apk.apimgt.api.model.SubscriptionResponse;
import org.wso2.apk.apimgt.impl.workflow.HttpWorkflowResponse;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.APIMonetizationUsageDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoListDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.common.mappings.AdditionalSubscriptionInfoMappingUtil;
import org.wso2.apk.apimgt.rest.api.store.v1.common.mappings.SubscriptionMappingUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the service implementation class for Store subscription related operations
 */
public class SubscriptionServiceImpl {

    private SubscriptionServiceImpl() {
    }

    /**
     * Get all subscriptions that are of user or shared subscriptions of the user's group.
     * <p/>
     * If apiId is specified this will return the subscribed applications of that api If application id is specified
     * this will return the api subscriptions of that application
     *
     * @param apiId         api identifier
     * @param applicationId application identifier
     * @param offset        starting index of the subscription list
     * @param limit         max num of subscriptions returned
     * @return matched subscriptions as a list of SubscriptionDTOs
     */
    public static SubscriptionListDTO getSubscriptions(String apiId, String applicationId, String groupId,
            Integer offset, Integer limit, String organization) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        Subscriber subscriber = new Subscriber(username);
        Set<SubscribedAPI> subscriptions;
        List<SubscribedAPI> subscribedAPIList = new ArrayList<>();

        //pre-processing
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        SubscriptionListDTO subscriptionListDTO;
        if (!StringUtils.isEmpty(apiId)) {
            // todo : FIX properly, need to done properly with backend side pagination.
            // todo : getSubscribedIdentifiers() method should NOT be used. Appears to be too slow.

            // This will fail with an authorization failed exception if user does not have permission to
            // access the API
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);

            if (apiTypeWrapper.isAPIProduct()) {
                subscriptions = apiConsumer.getSubscribedIdentifiers(subscriber, apiTypeWrapper.getApiProduct().getId(),
                        groupId, organization);
            } else {
                subscriptions = apiConsumer.getSubscribedIdentifiers(subscriber, apiTypeWrapper.getApi().getId(),
                        groupId, organization);
            }

            //sort by application name
            subscribedAPIList.addAll(subscriptions);

            subscribedAPIList.sort(Comparator.comparing(o -> o.getApplication().getName()));

            subscriptionListDTO = SubscriptionMappingUtil.fromSubscriptionListToDTO(subscribedAPIList, limit, offset,
                    organization);

            SubscriptionMappingUtil.setPaginationParams(subscriptionListDTO, apiId, "", limit, offset,
                    subscribedAPIList.size());

            return subscriptionListDTO;
        } else if (!StringUtils.isEmpty(applicationId)) {
            Application application = apiConsumer.getApplicationByUUID(applicationId);

            if (application == null) {
                throw new APIManagementException("Request application is with id " + applicationId + " not found",
                        ExceptionCodes.APPLICATION_NOT_FOUND);
            }

            if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                throw new APIManagementException(
                        "User " + username + " does not have permission to access application with Id : " + applicationId,
                        ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR, RestApiConstants.RESOURCE_APPLICATION,
                                applicationId));
            }

            subscriptions = apiConsumer.getPaginatedSubscribedAPIsByApplication(application, offset, limit,
                    organization);
            subscribedAPIList.addAll(subscriptions);

            subscriptionListDTO = SubscriptionMappingUtil.fromSubscriptionListToDTO(subscribedAPIList, limit, offset,
                    organization);
            return subscriptionListDTO;

        } else {
            //neither apiId nor applicationId is given
            String errorMessage = "Either applicationId or apiId should be available";
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
        }
    }

    /**
     * Creates a new subscriptions with the details specified in the body parameter
     *
     * @param body new subscription details
     * @return newly added subscription as a SubscriptionDTO if successful
     */
    public static SubscriptionDTO addSubscriptions(SubscriptionDTO body, String organization, String userOrganization)
            throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer;

        try {
            apiConsumer = RestApiCommonUtil.getConsumer(username, userOrganization);
            String applicationId = body.getApplicationId();

            //check whether user is permitted to access the API. If the API does not exist,
            // this will throw a APIMgtResourceNotFoundException
            if (body.getApiId() != null) {
                if (!RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(body.getApiId(), organization)) {
                    throw new APIManagementException(
                            "User " + username + " does not have permission to access the API : " + body.getApiId(),
                            ExceptionCodes.NO_READ_PERMISSIONS);
                }
            } else {
                String errorMessage = "Request must contain either apiIdentifier or apiProductIdentifier " + "and the relevant type";
                throw new APIManagementException(
                        ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
            }

            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application == null) {
                //required application not found
                throw new APIManagementException("Request application is with id " + applicationId + " not found",
                        ExceptionCodes.APPLICATION_NOT_FOUND);
            }

            // If application creation workflow status is pending or rejected, throw a Bad request exception
            if (application.getStatus().equals(WorkflowStatus.REJECTED.toString()) || application.getStatus()
                    .equals(WorkflowStatus.CREATED.toString())) {
                String errorMessage = "Workflow status is not Approved";
                throw new APIManagementException(
                        ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
            }

            if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                //application access failure occurred
                throw new APIManagementException(
                        "User " + username + " does not have permission to access application with Id : " + applicationId,
                        ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR, RestApiConstants.RESOURCE_APPLICATION,
                                applicationId));
            }

            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(body.getApiId(), organization);
            apiTypeWrapper.setTier(body.getThrottlingPolicy());
            SubscriptionResponse subscriptionResponse = apiConsumer.addSubscription(apiTypeWrapper, username,
                    application);
            SubscribedAPI addedSubscribedAPI = apiConsumer.getSubscriptionByUUID(
                    subscriptionResponse.getSubscriptionUUID());

            SubscriptionDTO addedSubscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(addedSubscribedAPI,
                    apiTypeWrapper, organization);
            WorkflowResponse workflowResponse = subscriptionResponse.getWorkflowResponse();
            if (workflowResponse instanceof HttpWorkflowResponse) {
                String payload = workflowResponse.getJSONPayload();
                addedSubscriptionDTO.setRedirectionParams(payload);
            }
            return addedSubscriptionDTO;

        } catch (APIMgtAuthorizationFailedException e) {
            //this occurs when the api:application:tier mapping is not allowed. The reason for the message is taken from
            // the message of the exception e
            throw new APIManagementException(e.getMessage(), ExceptionCodes.FORBIDDEN_ERROR);
        } catch (SubscriptionAlreadyExistingException e) {
            throw new APIManagementException(
                    RestApiConstants.STATUS_CONFLICT_MESSAGE_SUBSCRIPTION_ALREADY_EXISTS + " " + body.getApiId() + ", for application " + body.getApplicationId(),
                    e, ExceptionCodes.SUBSCRIPTION_ALREADY_EXISTS);
        }
    }

    /**
     * Update already created subscriptions with the details specified in the body parameter
     *
     * @param body new subscription details
     * @return newly added subscription as a SubscriptionDTO if successful
     */
    public static SubscriptionDTO updateSubscriptions(String subscriptionId, SubscriptionDTO body, String organization)
            throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer;

        try {
            apiConsumer = RestApiCommonUtil.getConsumer(username);
            String applicationId = body.getApplicationId();
            String currentThrottlingPolicy = body.getThrottlingPolicy();
            String requestedThrottlingPolicy = body.getRequestedThrottlingPolicy();

            SubscribedAPI subscribedAPI = apiConsumer.getSubscriptionByUUID(subscriptionId);
            //Check whether the subscription status is not empty and also not blocked
            if (body.getStatus() != null && subscribedAPI != null) {
                if ("BLOCKED".equals(body.getStatus().value()) || "ON_HOLD".equals(
                        body.getStatus().value()) || "REJECTED".equals(body.getStatus().value()) || "BLOCKED".equals(
                        subscribedAPI.getSubStatus()) || "ON_HOLD".equals(
                        subscribedAPI.getSubStatus()) || "REJECTED".equals(subscribedAPI.getSubStatus())) {
                    String errorMessage = "Cannot update subscriptions with provided or existing status";
                    throw new APIManagementException(
                            ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
                }
            } else {
                String errorMessage = "Request must contain status of the subscription";
                throw new APIManagementException(
                        ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
            }

            //check whether user is permitted to access the API. If the API does not exist,
            // this will throw a APIMgtResourceNotFoundException
            if (body.getApiId() != null) {
                if (!RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(body.getApiId(), organization)) {
                    throw new APIManagementException(
                            "User " + username + " does not have permission to access API with Id : " + body.getApiId(),
                            ExceptionCodes.NO_READ_PERMISSIONS);
                }
            } else {
                String errorMessage = "Request must contain either apiIdentifier or apiProductIdentifier and the " + "relevant type";
                throw new APIManagementException(
                        ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
            }

            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application == null) {
                //required application not found
                throw new APIManagementException("Request application is with id " + applicationId + " not found",
                        ExceptionCodes.APPLICATION_NOT_FOUND);
            }

            if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                //application access failure occurred
                throw new APIManagementException(
                        "User " + username + " does not have permission to access application with Id : " + applicationId,
                        ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR, RestApiConstants.RESOURCE_APPLICATION,
                                applicationId));
            }

            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(body.getApiId(), organization);

            apiTypeWrapper.setTier(body.getThrottlingPolicy());

            SubscriptionResponse subscriptionResponse = apiConsumer.updateSubscription(apiTypeWrapper, username,
                    application, subscriptionId, currentThrottlingPolicy, requestedThrottlingPolicy);
            SubscribedAPI addedSubscribedAPI = apiConsumer.getSubscriptionByUUID(
                    subscriptionResponse.getSubscriptionUUID());
            SubscriptionDTO addedSubscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(addedSubscribedAPI,
                    organization);
            WorkflowResponse workflowResponse = subscriptionResponse.getWorkflowResponse();
            if (workflowResponse instanceof HttpWorkflowResponse) {
                String payload = workflowResponse.getJSONPayload();
                addedSubscriptionDTO.setRedirectionParams(payload);
            }

            return addedSubscriptionDTO;

        } catch (APIMgtAuthorizationFailedException e) {
            //this occurs when the api:application:tier mapping is not allowed. The reason for the message is taken from
            // the message of the exception e
            throw new APIManagementException(e.getMessage(), ExceptionCodes.FORBIDDEN_ERROR);
        } catch (SubscriptionAlreadyExistingException e) {
            throw new APIManagementException(
                    RestApiConstants.STATUS_CONFLICT_MESSAGE_SUBSCRIPTION_ALREADY_EXISTS + " " + body.getApiId() + ", for application " + body.getApplicationId(),
                    e, ExceptionCodes.SUBSCRIPTION_ALREADY_EXISTS);
        }
    }

    /**
     * Create multiple new subscriptions with the list of subscription details specified in the body parameter.
     *
     * @param body list of new subscription details
     * @return list of newly added subscription as a SubscriptionDTO if successful
     */
    public static List<SubscriptionDTO> addMultipleSubscriptions(List<SubscriptionDTO> body, String organization)
            throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        List<SubscriptionDTO> subscriptions = new ArrayList<>();
        for (SubscriptionDTO subscriptionDTO : body) {
            try {
                APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
                String applicationId = subscriptionDTO.getApplicationId();

                //check whether user is permitted to access the API. If the API does not exist,
                // this will throw a APIMgtResourceNotFoundException
                if (!RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(subscriptionDTO.getApiId(), organization)) {
                    throw new APIManagementException(
                            "User " + username + " does not have permission to access API : " + subscriptionDTO.getApiId(),
                            ExceptionCodes.NO_READ_PERMISSIONS);
                }

                Application application = apiConsumer.getApplicationByUUID(applicationId);
                if (application == null) {
                    //required application not found
                    throw new APIManagementException("Request application is with id " + applicationId + " not found",
                            ExceptionCodes.APPLICATION_NOT_FOUND);
                }

                if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    //application access failure occurred
                    throw new APIManagementException(
                            "User " + username + " does not have permission to access application with Id : " + applicationId,
                            ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                                    RestApiConstants.RESOURCE_APPLICATION, applicationId));
                }

                ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(subscriptionDTO.getApiId(),
                        organization);

                apiTypeWrapper.setTier(subscriptionDTO.getThrottlingPolicy());
                SubscriptionResponse subscriptionResponse = apiConsumer.addSubscription(apiTypeWrapper, username,
                        application);
                SubscribedAPI addedSubscribedAPI = apiConsumer.getSubscriptionByUUID(
                        subscriptionResponse.getSubscriptionUUID());
                SubscriptionDTO addedSubscriptionDTO = SubscriptionMappingUtil.fromSubscriptionToDTO(addedSubscribedAPI,
                        organization);
                subscriptions.add(addedSubscriptionDTO);

            } catch (APIMgtAuthorizationFailedException e) {
                //this occurs when the api:application:tier mapping is not allowed. The reason for the message is
                // taken from the message of the exception e
                throw new APIManagementException(e.getMessage(), ExceptionCodes.FORBIDDEN_ERROR);
            } catch (SubscriptionAlreadyExistingException e) {
                throw new APIManagementException(
                        RestApiConstants.STATUS_CONFLICT_MESSAGE_SUBSCRIPTION_ALREADY_EXISTS + " " + subscriptionDTO.getApiId() + ", for application " + subscriptionDTO.getApplicationId(),
                        e, ExceptionCodes.SUBSCRIPTION_ALREADY_EXISTS);
            }
        }
        return subscriptions;
    }

    /**
     * Gets a subscription by identifier
     *
     * @param subscriptionId subscription identifier
     * @return matched subscription as a SubscriptionDTO
     */
    public static SubscriptionDTO getSubscription(String subscriptionId, String organization)
            throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer;
        try {
            apiConsumer = RestApiCommonUtil.getConsumer(username);
            SubscribedAPI subscribedAPI = validateAndGetSubscription(username, subscriptionId, apiConsumer);
            return SubscriptionMappingUtil.fromSubscriptionToDTO(subscribedAPI, organization);
        } catch (APIManagementException e) {
            throw new APIManagementException("Failed to get subscribed API information of " + subscriptionId,
                    ExceptionCodes.from(ExceptionCodes.SUBSCRIPTION_RETRIEVE_EXCEPTION,
                            "Subscribed API information of " + subscriptionId));
        }
    }

    public static APIMonetizationUsageDTO getSubscriptionsUsage(String subscriptionId) throws APIManagementException {

        if (StringUtils.isBlank(subscriptionId)) {
            String errorMessage = "Subscription ID cannot be empty or null when getting monetization usage.";
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
        }
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            Monetization monetizationImplementation = apiConsumer.getMonetizationImplClass();
            Map<String, String> billingEngineUsageData = monetizationImplementation.getCurrentUsageForSubscription(
                    subscriptionId, RestApiCommonUtil.getLoggedInUserProvider());
            if (MapUtils.isEmpty(billingEngineUsageData)) {
                String errorMessage = "Billing engine usage data was not found for subscription ID : " + subscriptionId;
                throw new APIManagementException(
                        ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
            }
            APIMonetizationUsageDTO apiMonetizationUsageDTO = new APIMonetizationUsageDTO();
            apiMonetizationUsageDTO.setProperties(billingEngineUsageData);
            return apiMonetizationUsageDTO;
        } catch (APIManagementException e) {
            String errorMessage = "Failed to retrieve billing engine usage data for subscription ID : " + subscriptionId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        } catch (MonetizationException e) {
            String errorMessage = "Failed to get current usage for subscription ID : " + subscriptionId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }
    }

    /**
     * Deletes the subscription matched to subscription id
     *
     * @param subscriptionId subscription identifier
     * @return 200 response if successfully deleted the subscription
     */
    public static SubscribedAPI deleteSubscriptionsBySubscriptionId(String subscriptionId, String organization)
            throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer;
        try {
            apiConsumer = RestApiCommonUtil.getConsumer(username);
            SubscribedAPI subscribedAPI = validateAndGetSubscription(username, subscriptionId, apiConsumer);
            apiConsumer.removeSubscription(subscribedAPI, organization);
            return subscribedAPI;
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting subscription with id " + subscriptionId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }
    }

    private static SubscribedAPI validateAndGetSubscription(String username, String subscriptionId,
            APIConsumer apiConsumer) throws APIManagementException {
        SubscribedAPI subscribedAPI = apiConsumer.getSubscriptionByUUID(subscriptionId);
        if (subscribedAPI == null) {
            throw new APIManagementException("Request Subscription is with id " + subscriptionId + " not found",
                    ExceptionCodes.SUBSCRIPTION_NOT_FOUND);
        }
        if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(subscribedAPI.getApplication())) {
            throw new APIManagementException(
                    "User " + username + " does not have permission to access application with Id : " + subscribedAPI.getApplication()
                            .getUUID(),
                    ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR, RestApiConstants.RESOURCE_APPLICATION,
                            subscribedAPI.getApplication().getUUID()));
        }
        return subscribedAPI;
    }

    /**
     * Get additional Info details of subscriptions attached with given API
     *
     * @param apiId  apiId
     * @param offset starting index of the subscription list
     * @param limit  max num of subscriptions returned
     * @return Response with additional Info of the GraphQL API
     */
    public static AdditionalSubscriptionInfoListDTO getAdditionalInfoOfAPISubscriptions(String apiId, String groupId,
            Integer offset, Integer limit, String organization) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        Subscriber subscriber = new Subscriber(username);
        Set<SubscribedAPI> subscriptions;
        List<SubscribedAPI> subscribedAPIList = new ArrayList<>();

        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        AdditionalSubscriptionInfoListDTO additionalSubscriptionInfoListDTO = null;

        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);

        if (apiTypeWrapper.isAPIProduct()) {
            subscriptions = apiConsumer.getSubscribedIdentifiers(subscriber, apiTypeWrapper.getApiProduct().getId(),
                    groupId, organization);
        } else {
            subscriptions = apiConsumer.getSubscribedIdentifiers(subscriber, apiTypeWrapper.getApi().getId(), groupId,
                    organization);
        }

        //Sort subscriptions by application name
        subscribedAPIList.addAll(subscriptions);
        subscribedAPIList.sort(Comparator.comparing(o -> o.getApplication().getName()));
        additionalSubscriptionInfoListDTO = AdditionalSubscriptionInfoMappingUtil.fromAdditionalSubscriptionInfoListToDTO(
                subscribedAPIList, limit, offset, organization);
        AdditionalSubscriptionInfoMappingUtil.setPaginationParams(additionalSubscriptionInfoListDTO, apiId, "", limit,
                offset, subscribedAPIList.size());
        return additionalSubscriptionInfoListDTO;

    }
}
