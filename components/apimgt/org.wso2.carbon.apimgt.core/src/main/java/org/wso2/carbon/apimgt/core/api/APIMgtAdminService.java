/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.util.List;

/**
 * This interface used to have API core services
 */
public interface APIMgtAdminService {

    /**
     * Return all API subscriptions
     *
     * @param limit Subscription Limit
     * @return all subscriptions
     * @throws APIManagementException
     */
    List<SubscriptionValidationData> getAPISubscriptions(int limit) throws APIManagementException;

    /**
     * Return all API subscriptions of a given API
     *
     * @param apiContext Context of API
     * @param apiVersion Version of API
     * @return all subscriptions
     * @throws APIManagementException
     */
    List<SubscriptionValidationData> getAPISubscriptionsOfApi(String apiContext, String apiVersion)
            throws APIManagementException;

    /**
     * Load api info from db
     *
     * @return Subscription Validation Information
     * @throws APIManagementException
     */
    public List<APISummary> getAPIInfo() throws APIManagementException;

    /**
     * Adds new @{@link Policy} to the system
     *
     * @param policy
     * @throws APIManagementException
     */
    void addPolicy(String policyLevel, Policy policy) throws APIManagementException;

    /**
     * Updates existing @{@link Policy} to the system
     *
     * @param policy
     * @throws APIManagementException
     */
    void updatePolicy(Policy policy) throws APIManagementException;

    /**
     * Delete existing @{@link Policy} in the system
     *
     * @param policy
     * @throws APIManagementException
     */
    void deletePolicy(Policy policy) throws APIManagementException;


    /**
     * Get a @{@link Policy} by policy name
     *
     * @param policyName
     * @return
     * @throws APIManagementException
     */
    Policy getPolicy(String policyLevel, String policyName) throws APIManagementException;

    List<Policy> getAllPoliciesByLevel(String policyLevel) throws APIManagementException;
}
