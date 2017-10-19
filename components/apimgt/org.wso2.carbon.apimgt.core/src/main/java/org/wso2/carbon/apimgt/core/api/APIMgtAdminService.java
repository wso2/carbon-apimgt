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
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.workflow.Workflow;

import java.util.List;
import java.util.Set;

/**
 * This interface used to have API core services
 */
public interface APIMgtAdminService {

    /**
     * Gets all Policies by level
     *
     * @param policyLevel policy level
     * @return list of {@link Policy} instance
     * @throws APIManagementException If failed to get the Policies
     */
    List<Policy> getPoliciesByLevel(APIMgtAdminService.PolicyLevel policyLevel) throws APIManagementException;

    /**
     * Gets all Policies by level and name
     *
     * @param policyLevel policy level
     * @param policyName Policy Name
     * @return {@link Policy} instance
     * @throws APIManagementException If failed to get the Policies
     */
    Policy getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIManagementException;

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
     * Adds new {@link APIPolicy} to the system
     *
     * @param policy      Policy object to be added.
     * @return created policy uuid
     * @throws APIManagementException If failed to add the policy.
     */
    String addApiPolicy(APIPolicy policy) throws APIManagementException;

    /**
     * Adds new {@link ApplicationPolicy} to the system
     *
     * @param policy      Policy object to be added.
     * @return created policy uuid
     * @throws APIManagementException If failed to add the policy.
     */
    String addApplicationPolicy(ApplicationPolicy policy) throws APIManagementException;

    /**
     *  Add a block condition
     *
     * @param blockConditions BlockConditions Object to be added
     * @return UUID of the new Block Condition
     * @throws APIManagementException
     */
    String addBlockCondition(BlockConditions blockConditions) throws APIManagementException;

    /**
     * Adds new {@link SubscriptionPolicy} to the system
     *
     * @param policy      Policy object to be added.
     * @return created policy uuid
     * @throws APIManagementException If failed to add the policy.
     */
    String addSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException;

    /**
     * Updates a {@link APIPolicy} instance
     *
     * @param policy      Policy object to be updated.
     * @throws APIManagementException If failed to update the policy.
     */
    void updateApiPolicy(APIPolicy policy) throws APIManagementException;

    /**
     * Updates a {@link SubscriptionPolicy} instance
     *
     * @param policy      Policy object to be updated.
     * @throws APIManagementException If failed to update the policy.
     */
    void updateSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException;

    /**
     * Updates a {@link ApplicationPolicy} instance
     *
     * @param policy      Policy object to be updated.
     * @throws APIManagementException If failed to update the policy.
     */
    void updateApplicationPolicy(ApplicationPolicy policy) throws APIManagementException;

    /**
     * Updates a block condition given its UUID
     *
     * @param uuid uuid of the block condition
     * @param state state of condition
     * @return state change success or not
     * @throws APIManagementException
     */
    boolean updateBlockConditionStateByUUID(String uuid, Boolean state) throws APIManagementException;

    /**
     * Delete existing @{@link Policy} in the system
     *
     * @param policyName    Policy Name to be deleted.
     * @param policyLevel Policy Level to which above Policy belongs to
     * @throws APIManagementException   If failed to delete the policy.
     */
    void deletePolicy(String policyName, APIMgtAdminService.PolicyLevel policyLevel) throws APIManagementException;

    /**
     * Delete existing @{@link BlockConditions} in the system
     *
     * @param uuid uuid of the block condition to be deleted
     * @return true if successfully deleted
     * @throws APIManagementException If failed to delete the block condition
     */
    boolean deleteBlockConditionByUuid(String uuid) throws APIManagementException;

    /**
     * Delete an existing Plicy in the system using the policy id
     *
     * @param uuid    Policy uuid to be deleted.
     * @param policyLevel Policy Level to which above Policy belongs to
     * @throws APIManagementException   If failed to delete the policy.
     */
    void deletePolicyByUuid(String uuid, APIMgtAdminService.PolicyLevel policyLevel) throws APIManagementException;

    /**
     * Gets a {@link APIPolicy} by policy name
     *
     * @param policyName  Name of the policy
     * @return Policy object.
     * @throws APIManagementException If failed to get policy.
     */
    APIPolicy getApiPolicy(String policyName) throws APIManagementException;

    /**
     * Returns a {@link SubscriptionPolicy} by policy name
     *
     * @param policyName  Name of the policy                                                                        DAO
     * @return Policy object.
     * @throws APIManagementException If failed to get policy.
     */
    SubscriptionPolicy getSubscriptionPolicy(String policyName) throws APIManagementException;

    /**
     * Returns a {@link ApplicationPolicy} by policy name
     *
     * @param policyName  Name of the policy
     * @return Policy object.
     * @throws APIManagementException If failed to get policy.
     */
    ApplicationPolicy getApplicationPolicy(String policyName) throws APIManagementException;

    /**
     * Returns a {@link APIPolicy} by policy id
     *
     * @param uuid  id of the policy
     * @return Policy object.
     * @throws APIManagementException If failed to get policy.
     */
    APIPolicy getApiPolicyByUuid(String uuid) throws APIManagementException;

    /**
     * Returns a {@link ApplicationPolicy} by policy id
     *
     * @param uuid  id of the policy
     * @return Policy object.
     * @throws APIManagementException If failed to get policy.
     */
    ApplicationPolicy getApplicationPolicyByUuid(String uuid) throws APIManagementException;

    /**
     * Get a {@link SubscriptionPolicy} by policy id
     *
     * @param uuid  id of the policy
     * @return Policy object.
     * @throws APIManagementException If failed to get policy.
     */
    SubscriptionPolicy getSubscriptionPolicyByUuid(String uuid) throws APIManagementException;

    /**
     * Get a List of API policies
     *
     * @return List of {@link APIPolicy} instances
     * @throws APIManagementException If failed to get policies.
     */
    List<APIPolicy> getApiPolicies() throws APIManagementException;

    /**
     * Get a List of API policies
     *
     * @return List of {@link ApplicationPolicy} instances
     * @throws APIManagementException If failed to get policies.
     */
    List<ApplicationPolicy> getApplicationPolicies() throws APIManagementException;

    /**
     * Get a List of API policies
     *
     * @return List of {@link SubscriptionPolicy} instances
     * @throws APIManagementException If failed to get policies.
     */
    List<SubscriptionPolicy> getSubscriptionPolicies() throws APIManagementException;

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
     *
     * @param apiContext Context of API
     * @param apiVersion Version of API
     * @return list of API Resources
     * @throws APIManagementException if failed to retrieve resources
     */
    List<UriTemplate> getAllResourcesForApi(String apiContext, String apiVersion) throws APIManagementException;

    /**
     * Get a list of APIs with given gateway labels and status
     *
     * @param gatewayLabels A list of gateway labels
     * @param status        Lifecycle status
     * @return list of API Resources
     * @throws APIManagementException If failed to get API list
     */
    List<API> getAPIsByStatus(List<String> gatewayLabels, String status) throws APIManagementException;

    /**
     * Get a list of APIs with given gateway labels
     *
     * @param gatewayLabels List of labels
     * @return list of API Resources
     * @throws APIManagementException If failed to get API list
     */
    List<API> getAPIsByGatewayLabel(List<String> gatewayLabels) throws APIManagementException;

    /**
     * Retrieve API Gateway registration summary
     *
     * @return RegistrationSummary
     */
    RegistrationSummary getRegistrationSummary();

    /**
     * Get list of Applications
     *
     * @return list of {@link Application}
     * @throws APIManagementException If failed to get Applications
     */
    List<Application> getAllApplications() throws APIManagementException;

    /**
     *
     * Get List of Endpoints
     * @return list of UUID of Global Endpoints
     * @throws APIManagementException If failed to get Endpoints
     */
    List<Endpoint> getAllEndpoints() throws APIManagementException;

    /**
     * Return Gateway Configuration of Endpoint
     *
     * @param endpointId
     * @return gateway Configuration of Endpoint
     * @throws APIManagementException If failed to get Endpoints configuration
     */
    String getEndpointGatewayConfig(String endpointId) throws APIManagementException;

    /**
     * Return simple Policy related details
     *
     * @return policy related information for gateway
     * @throws APIManagementException If failed to get Policies
     */
    Set<PolicyValidationData> getAllPolicies() throws APIManagementException;

    /**
     * Policy Level enum
     */
    enum PolicyLevel {
        api,
        application,
        subscription,
        custom
    }

    /**
     * Get a list of block conditions.
     *
     * @return List of block Conditions
     * @throws APIManagementException
     */
    List<BlockConditions> getBlockConditions() throws APIManagementException;

    /**
     * Retrieves a block condition by its UUID.
     *
     * @param uuid uuid of the block condition
     * @return Retrieve a block Condition
     * @throws APIManagementException
     */
    BlockConditions getBlockConditionByUUID(String uuid) throws APIManagementException;

    /**
     * Adding custom policy.
     *
     * @param customPolicy CustomPolicy object to be added
     * @return uuid of the added custom policy
     * @throws APIManagementException if failed adding custom policy
     */
    String addCustomRule(CustomPolicy customPolicy) throws APIManagementException;

    /**
     * Update custom policy.
     *
     * @param customPolicy CustomPolicy to be updated
     * @throws APIManagementException if failed updating custom policy
     */
    void updateCustomRule(CustomPolicy customPolicy) throws APIManagementException;

    /**
     * Delete custom rule.
     *
     * @param uuid uuid of the custom policy to be deleted
     * @throws APIManagementException if failed delete custom rule
     */
    void deleteCustomRule(String uuid) throws APIManagementException;

    /**
     * Get all custom policies
     *
     * @return list of custom policies available
     * @throws APIManagementException if failed getting all custom policies
     */
    List<CustomPolicy> getCustomRules() throws APIManagementException;

    /**
     * Get custom policy by uuid
     *
     * @param uuid uuid of the custom policy to be retrieved
     * @return CustomPolicy object
     * @throws APIManagementException if failed getting custom policy
     */
    CustomPolicy getCustomRuleByUUID(String uuid) throws APIManagementException;
    
    /**
     * Retrieve workflow for the given workflow reference ID
     * @param workflowRefId External workflow reference Id
     * @return Workflow workflow entry
     * @throws APIManagementException if API Manager core level exception occurred
     */
    Workflow retrieveWorkflow(String workflowRefId) throws APIManagementException;
    
    /**
     * Complete workflow task 
     * @param workflowExecutor executor related to the workflow task
     * @param workflow workflow object
     * @return WorkflowResponse WorkflowResponse of the executor
     * @throws APIManagementException if API Manager core level exception occurred
     */
    WorkflowResponse completeWorkflow(WorkflowExecutor workflowExecutor, Workflow workflow)
            throws APIManagementException;
    
    /**
     * Retrieve uncompleted workflows for the given type
     * @param type type of workflow
     * @return {@code List<Workflow>} list of workflows
     * @throws APIManagementException if API Manager core level exception occurred
     */
    List<Workflow> retrieveUncompletedWorkflowsByType(String type) throws APIManagementException;
    
    /**
     * Retrieve uncompleted workflows 
     * 
     * @return {@code List<Workflow>} list of workflows
     * @throws APIManagementException if API Manager core level exception occurred
     */
    List<Workflow> retrieveUncompletedWorkflows() throws APIManagementException;

}
