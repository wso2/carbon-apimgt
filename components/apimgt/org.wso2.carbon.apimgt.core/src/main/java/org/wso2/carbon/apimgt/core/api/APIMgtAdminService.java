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

import org.wso2.carbon.apimgt.core.exception.APIConfigRetrievalException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;

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
     * @throws APIManagementException If failed to get list of subscriptions.
     */
    List<SubscriptionValidationData> getAPISubscriptions(int limit) throws APIManagementException;

    /**
     * Return all API subscriptions of a given API
     *
     * @param apiContext Context of API
     * @param apiVersion Version of API
     * @return all subscriptions
     * @throws APIManagementException If failed to get list of subscriptions.
     */
    List<SubscriptionValidationData> getAPISubscriptionsOfApi(String apiContext, String apiVersion)
            throws APIManagementException;

    /**
     * Load api info from db
     *
     * @return Subscription Validation Information
     * @throws APIManagementException If failed to get lAPI summary data
     */
    public List<APISummary> getAPIInfo() throws APIManagementException;

    /**
     * Adds new @{@link Policy} to the system
     *
     * @param policyLevel Tier level of the policy.
     * @param policy      Policy object to be added.
     * @throws APIManagementException If failed to add the policy.
     */
    void addPolicy(String policyLevel, Policy policy) throws APIManagementException;

    /**
     * Updates existing @{@link Policy} to the system
     *
     * @param policy Policy object to be updated.
     * @throws APIManagementException If failed to update the policy.
     */
    void updatePolicy(Policy policy) throws APIManagementException;

    /**
     * Delete existing @{@link Policy} in the system
     *
     * @param policyName    Policy Name to be deleted.
     * @param policyLevel Policy Level to which above Policy belongs to
     * @throws APIManagementException   If failed to delete the policy.
     */
    void deletePolicy(String policyName, String policyLevel) throws APIManagementException;


    /**
     * Delete existing @{@link Policy} in the system
     *
     * @param uuid    Policy uuid to be deleted.
     * @param policyLevel Policy Level to which above Policy belongs to
     * @throws APIManagementException   If failed to delete the policy.
     */
    void deletePolicyByUuid(String uuid, String policyLevel) throws APIManagementException;

    /**
     * Get a @{@link Policy} by policy name
     *
     * @param policyLevel Tier level of the policy.
     * @param policyName  Name of the policy
     * @return Policy object.
     * @throws APIManagementException If failed to get policy.
     */
    Policy getPolicy(String policyLevel, String policyName) throws APIManagementException;

    /**
     * Get a @{@link Policy} by policy uuid
     *
     * @param uuid Policy uuid
     * @param policyLevel Tier level of the policy.
     * @return Policy object.
     * @throws APIManagementException If failed to get policy.
     */
    Policy getPolicyByUuid(String uuid, String policyLevel) throws APIManagementException;

    /**
     * Get a List of policies of a particular level
     *
     * @param policyLevel Tier level of the policy.
     * @return List of Policy objects of the given level.
     * @throws APIManagementException If failed to get policies.
     */
    List<Policy> getAllPoliciesByLevel(String policyLevel) throws APIManagementException;

    /**
     * Delete existing label in the system by labelId
     *
     * @param labelId Id of the label to be deleted.
     * @throws APIManagementException If failed to delete the label.
     */
    void deleteLabel(String labelId) throws APIManagementException;

    /**
     * Register gateway labels in the system
     *
     * @param labels List of labels
     * @param overwriteLabels Flag to overwrite gateway labels
     * @throws APIManagementException If failed to register labels.
     */
    void registerGatewayLabels(List<Label> labels, String overwriteLabels) throws APIManagementException;

    /**
     * Retrieve API's gateway configuration
     *
     * @param apiId     UUID of the API
     * @return          The API gateway configuration
     * @throws APIConfigRetrievalException   If failed to retrive API gateway config
     */
    String getAPIGatewayServiceConfig(String apiId) throws APIConfigRetrievalException;

    /**
     * Retrieve Resources for API
     * @param apiContext Context of API
     * @param apiVersion Version of API
     * @return list of API Resources
     * @throws APIManagementException if failed to retrieve resources
     */
    List<UriTemplate> getAllResourcesForApi(String apiContext, String apiVersion) throws APIManagementException;

    /**
     * Get a list of APIs with given gateway labels and status
     *@param gatewayLabels A list of gateway labels
     * @param status Lifecycle status
     * @throws APIManagementException If failed to get API list
     */
    List<API> getAPIsByStatus(List<String> gatewayLabels, String status) throws APIManagementException;

    /**
     * Get a list of APIs with given gateway labels
     *
     * @throws APIManagementException If failed to get API list
     */
    List<API> getAPIsByGatewayLabel(List<String> gatewayLabels) throws APIManagementException;
    List<APIPolicy> getAllAdvancePolicies() throws APIManagementException;

    /**
     * Get a List of Advance policies.
     *
     * @return List of Policy objects of the given level.
     * @throws APIManagementException If failed to get policies.
     */
    List<ApplicationPolicy> getAllApplicationPolicies() throws APIManagementException;

    /**
     * Get a List of Advance policies.
     *
     * @return List of Policy objects of the given level.
     * @throws APIManagementException If failed to get policies.
     */
    List<SubscriptionPolicy> getAllSubscriptionPolicies() throws APIManagementException;
}
