/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;

import java.util.Map;

/**
 * Interface for monetization
 */

public interface Monetization {

    /**
     * Create billing plan for a policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    boolean createBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException;

    /**
     * Update billing plan of a policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    boolean updateBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException;

    /**
     * Delete a billing plan of a policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    boolean deleteBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException;

    /**
     * Enable monetization for a API
     *
     * @param tenantDomain           tenant domain
     * @param api                    API
     * @param monetizationProperties monetization properties map
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    boolean enableMonetization(String tenantDomain, API api,
                               Map<String, String> monetizationProperties) throws MonetizationException;

    boolean enableMonetization(String tenantDomain, APIProduct apiProduct,
                               Map<String, String> monetizationProperties) throws MonetizationException;

    /**
     * Disable monetization for a API
     *
     * @param tenantDomain           tenant domain
     * @param api                    API
     * @param monetizationProperties monetization properties map
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    boolean disableMonetization(String tenantDomain, API api,
                                Map<String, String> monetizationProperties) throws MonetizationException;

    boolean disableMonetization(String tenantDomain, APIProduct apiProduct,
                                Map<String, String> monetizationProperties) throws MonetizationException;
    /**
     * Get mapping of tiers and billing engine plans
     *
     * @param api API
     * @return tier to billing plan mapping
     * @throws MonetizationException if failed to get tier to billing plan mapping
     */
    Map<String, String> getMonetizedPoliciesToPlanMapping(API api) throws MonetizationException;

    /**
     * Get current usage for a subscription
     *
     * @param subscriptionUUID subscription UUID
     * @param apiProvider      API provider
     * @return current usage for a subscription
     * @throws MonetizationException if failed to get current usage for a subscription
     */
    Map<String, String> getCurrentUsageForSubscription(String subscriptionUUID, APIProvider apiProvider) throws MonetizationException;

    /**
     * Get total revenue for a given API from all subscriptions
     *
     * @param api API
     * @param apiProvider API provider
     * @return total revenue data for a given API from all subscriptions
     * @throws MonetizationException if failed to get total revenue data for a given API
     */
    Map<String, String> getTotalRevenue(API api, APIProvider apiProvider) throws MonetizationException;

    /**
     * Publish the usage for a subscription to the billing engine
     *
     * @return true if the job is successfull, and false otherwise
     * @throws MonetizationException if failed to get current usage for a subscription
     */
    boolean publishMonetizationUsageRecords(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws MonetizationException;

}
