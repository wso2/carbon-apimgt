/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.publisher.v1.common.impl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.MonetizationException;
import org.wso2.apk.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.apk.apimgt.api.model.APIRevision;
import org.wso2.apk.apimgt.api.model.Monetization;
import org.wso2.apk.apimgt.api.model.SubscribedAPI;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.publisher.v1.common.mappings.SubscriptionMappingUtil;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.APIMonetizationUsageDTO;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.SubscriberInfoDTO;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.SubscriptionDTO;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class for operations related to SubscriptionsApiService
 */
public class SubscriptionsApiCommonImpl {

    private SubscriptionsApiCommonImpl() {
        //To hide the default constructor
    }

    public static SubscriptionDTO blockSubscription(String subscriptionId, String blockState)
            throws APIManagementException {

        SubscribedAPI updatedSubscription = changeBlockStatus(subscriptionId, blockState);
        return SubscriptionMappingUtil.fromSubscriptionToDTO(updatedSubscription);
    }

    public static SubscriptionDTO unBlockSubscription(String subscriptionId) throws APIManagementException {

        SubscribedAPI updatedSubscribedAPI = changeBlockStatus(subscriptionId,
                APIConstants.SubscriptionStatus.UNBLOCKED);
        return SubscriptionMappingUtil.fromSubscriptionToDTO(updatedSubscribedAPI);
    }

    /**
     * Updates the blocked statues of a subscription
     *
     * @param subscriptionId Subscription ID
     * @param blockState     Blocked Status
     * @return Updated Subscribed API
     * @throws APIManagementException if an error occurred in status update
     */
    public static SubscribedAPI changeBlockStatus(String subscriptionId, String blockState)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // validates the subscriptionId if it exists
        SubscribedAPI currentSubscription = apiProvider.getSubscriptionByUUID(subscriptionId);

        if (currentSubscription == null) {
            throw new APIMgtResourceNotFoundException("Requested subscription with Id '" + subscriptionId
                    + "' not found",
                    ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_TYPE_AND_ID, "subscription",
                            subscriptionId));
        }

        SubscribedAPI subscribedAPI = new SubscribedAPI(subscriptionId);
        subscribedAPI.setSubStatus(blockState);
        apiProvider.updateSubscription(subscribedAPI);

        return apiProvider.getSubscriptionByUUID(subscriptionId);
    }

    public static SubscriptionListDTO getSubscriptions(String apiId, Integer limit, Integer offset, String query,
                                                       String organization) throws APIManagementException {

        // setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        List<SubscribedAPI> apiUsages = getSubscribedAPIs(apiId, organization);

        SubscriptionListDTO subscriptionListDTO;
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
        return subscriptionListDTO;
    }

    /**
     * Retrieves subscribed APIs for given API ID and Organization
     *
     * @param apiId        API ID
     * @param organization Organization
     * @return List of subscribed APIs
     * @throws APIManagementException If error occurred in retrieving APIs
     */
    private static List<SubscribedAPI> getSubscribedAPIs(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<SubscribedAPI> apiUsages;

        if (apiId != null) {
            String apiUuid = apiId;
            APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
            if (apiRevision != null && apiRevision.getApiUUID() != null) {
                apiUuid = apiRevision.getApiUUID();
            }
            apiUsages = apiProvider.getAPIUsageByAPIId(apiUuid, organization);
        } else {
            String username = RestApiCommonUtil.getLoggedInUsername();
            UserApplicationAPIUsage[] allApiUsage = apiProvider.getAllAPIUsageByProvider(username);
            apiUsages = fromUserApplicationAPIUsageArrayToSubscribedAPIList(allApiUsage);
        }
        return apiUsages;
    }

    /**
     * Converts a UserApplicationAPIUsage[] array to a corresponding SubscriptionListDTO
     *
     * @param allApiUsage array of UserApplicationAPIUsage
     * @return a list of all subscriptions
     */
    public static List<SubscribedAPI> fromUserApplicationAPIUsageArrayToSubscribedAPIList(
            UserApplicationAPIUsage[] allApiUsage) {

        List<SubscribedAPI> subscribedAPIs = new ArrayList<>();

        for (UserApplicationAPIUsage usage : allApiUsage) {
            Collections.addAll(subscribedAPIs, usage.getApiSubscriptions());
        }

        return subscribedAPIs;
    }

    public static APIMonetizationUsageDTO getSubscriptionUsage(String subscriptionId) throws APIManagementException {

        Map<String, String> billingEngineUsageData = getBillingEngineUsageData(subscriptionId);
        APIMonetizationUsageDTO apiMonetizationUsageDTO = new APIMonetizationUsageDTO();
        apiMonetizationUsageDTO.setProperties(billingEngineUsageData);
        return apiMonetizationUsageDTO;
    }

    /**
     * Retrieves billing usage data for given subscription ID
     *
     * @param subscriptionId Subscription ID
     * @return Map of usages
     * @throws APIManagementException If error occurred in retrieving or mapping billing usage data
     */
    private static Map<String, String> getBillingEngineUsageData(String subscriptionId) throws APIManagementException {

        Map<String, String> billingEngineUsageData;
        try {
            if (StringUtils.isBlank(subscriptionId)) {
                String errorMessage = "Subscription ID cannot be empty or null when getting monetization usage.";
                throw new APIManagementException(errorMessage, ExceptionCodes.BAD_REQUEST_SUBSCRIPTION_ID);
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            billingEngineUsageData = monetizationImplementation.
                    getCurrentUsageForSubscription(subscriptionId, apiProvider);
            if (MapUtils.isEmpty(billingEngineUsageData)) {
                String errorMessage = "Billing engine usage data was not found for subscription ID : " + subscriptionId;
                throw new APIManagementException(errorMessage, ExceptionCodes.BAD_REQUEST_SUBSCRIPTION_ID);
            }
        } catch (MonetizationException e) {
            String errorMessage = "Failed to get current usage for subscription ID : " + subscriptionId;
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                    errorMessage));
        }
        return billingEngineUsageData;
    }

    public static SubscriberInfoDTO getSubscriberInfoBySubscriptionId(String subscriptionId)
            throws APIManagementException {

        if (StringUtils.isBlank(subscriptionId)) {
            String errorMessage = "Subscription ID cannot be empty or null when getting subscriber info.";
            throw new APIManagementException(errorMessage, ExceptionCodes.BAD_REQUEST_SUBSCRIPTION_ID);
        }
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        String subscriberName = apiProvider.getSubscriber(subscriptionId);
        Map<String, String> subscriberClaims = apiProvider.getSubscriberClaims(subscriberName);
        return SubscriptionMappingUtil.fromSubscriberClaimsToDTO(subscriberClaims, subscriberName);
    }

}
