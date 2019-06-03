/*
*  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
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
     * @throws APIManagementException if the action failed
     */
    boolean createBillingPlan(SubscriptionPolicy subPolicy) throws APIManagementException;

    /**
     * Update billing plan of a policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws APIManagementException if the action failed
     */
    boolean updateBillingPlan(SubscriptionPolicy subPolicy) throws APIManagementException;

    /**
     * Delete a billing plan of a policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws APIManagementException if the action failed
     */
    boolean deleteBillingPlan(SubscriptionPolicy subPolicy) throws APIManagementException;

    /**
     * Enable monetization for a API
     *
     * @param tenantDomain           tenant domain
     * @param api                    API
     * @param monetizationProperties monetization properties map
     * @return true if successful, false otherwise
     * @throws APIManagementException if the action failed
     */
    boolean enableMonetization(String tenantDomain, API api,
                               Map<String, String> monetizationProperties) throws APIManagementException;

    /**
     * Disable monetization for a API
     *
     * @param tenantDomain           tenant domain
     * @param api                    API
     * @param monetizationProperties monetization properties map
     * @return true if successful, false otherwise
     * @throws APIManagementException if the action failed
     */
    boolean disableMonetization(String tenantDomain, API api,
                                Map<String, String> monetizationProperties) throws APIManagementException;

    /**
     * Get mapping of tiers and billing engine plans
     *
     * @param api API
     * @return tier to billing plan mapping
     * @throws APIManagementException if failed to get tier to billing plan mapping
     */
    Map<String, String> getMonetizedPoliciesToPlanMapping(API api) throws APIManagementException;

    /**
     * Get current usage for a subscription
     *
     * @param subscriptionUUID subscription UUID
     * @param apiProvider      API provider
     * @return current usage for a subscription
     * @throws APIManagementException if failed to get current usage for a subscription
     */
    Map<String, String> getCurrentUsage(String subscriptionUUID, APIProvider apiProvider) throws APIManagementException;

}
