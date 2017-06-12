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

package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;

import java.util.List;
import java.util.Set;

/**
 * Provides access to Policy data layer
 */
public interface PolicyDAO {

    /**
     * Checks whether the Throttling Policy exists for the gen policy level and name
     *
     * @param policyLevel policy level {@link APIMgtAdminService.PolicyLevel}
     * @param policyName  polcy name
     * @return true if the policy exists, else false
     * @throws APIMgtDAOException if any error occurs while checking the Policy existence
     */
    boolean policyExists(APIMgtAdminService.PolicyLevel policyLevel, String policyName) throws APIMgtDAOException;

    /**
     * Gets all Policies by level
     *
     * @param policyLevel policy level
     * @return list of {@link Policy} instance
     * @throws APIMgtDAOException If failed to get the Policies
     */
    List<Policy> getPoliciesByLevel(APIMgtAdminService.PolicyLevel policyLevel) throws APIMgtDAOException;

    /**
     * Gets all Policies by level and name
     *
     * @param policyLevel policy level
     * @param policyName  Policy Name
     * @return {@link Policy} instance
     * @throws APIMgtDAOException If failed to get the Policies
     */
    Policy getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIMgtDAOException;

    /**
     * Gets all Application Policies
     *
     * @return list of {@link ApplicationPolicy} instance
     * @throws APIMgtDAOException If failed to get the Policy
     */
    List<ApplicationPolicy> getApplicationPolicies() throws APIMgtDAOException;

    /**
     * Gets an Application Policy by name
     *
     * @param policyName Policy Name
     * @return {@link ApplicationPolicy} instance
     * @throws APIMgtDAOException If failed to get the Policy
     */
    ApplicationPolicy getApplicationPolicy(String policyName) throws APIMgtDAOException;

    /**
     * Gets a Policy by uuid
     *
     * @param uuid policy id
     * @return {@link ApplicationPolicy} instance
     * @throws APIMgtDAOException If failed to get the Policy
     */
    ApplicationPolicy getApplicationPolicyByUuid(String uuid) throws APIMgtDAOException;

    /**
     * Gets all Subscription Policies
     *
     * @return list of {@link SubscriptionPolicy} instance
     * @throws APIMgtDAOException If failed to get the Policy
     */
    List<SubscriptionPolicy> getSubscriptionPolicies() throws APIMgtDAOException;

    /**
     * Gets a Subscription Policy by name
     *
     * @param policyName Policy Name
     * @return {@link SubscriptionPolicy} Gets a Policy by given name
     * @throws APIMgtDAOException If failed to get the Policy
     */
    SubscriptionPolicy getSubscriptionPolicy(String policyName) throws APIMgtDAOException;

    /**
     * Gets a Subscription Policy by uuid and Level
     *
     * @param uuid policy id
     * @return {@link SubscriptionPolicy} Gets a Policy by given name
     * @throws APIMgtDAOException If failed to get the Policy
     */
    SubscriptionPolicy getSubscriptionPolicyByUuid(String uuid) throws APIMgtDAOException;

    /**
     * Gets all API Policies
     *
     * @return list of {@link APIPolicy} instance
     * @throws APIMgtDAOException If failed to get the Policy
     */
    List<APIPolicy> getApiPolicies() throws APIMgtDAOException;

    /**
     * Gets an Advanced Policy by name
     *
     * @param policyName Policy Name
     * @return {@link APIPolicy} Gets a Policy by given name and level
     * @throws APIMgtDAOException If failed to get the Policy
     */
    APIPolicy getApiPolicy(String policyName) throws APIMgtDAOException;

    /**
     * Gets an Advanced Policy by uuid
     *
     * @param uuid policy id
     * @return {@link APIPolicy} Gets a Policy by given name and level
     * @throws APIMgtDAOException If failed to get the Policy
     */
    APIPolicy getApiPolicyByUuid(String uuid) throws APIMgtDAOException;

    /**
     * Adds an Application policy
     *
     * @param policy Policy to add
     * @throws APIMgtDAOException If failed to add a Policy
     */
    void addApplicationPolicy(ApplicationPolicy policy) throws APIMgtDAOException;

    /**
     * Adds an API Policy
     *
     * @param policy Policy to add
     * @throws APIMgtDAOException If failed to add a Policy
     */
    void addApiPolicy(APIPolicy policy) throws APIMgtDAOException;

    /**
     * Adds a Subscription Policy
     *
     * @param policy Policy to add
     * @throws APIMgtDAOException If failed to add a Policy
     */
    void addSubscriptionPolicy(SubscriptionPolicy policy) throws APIMgtDAOException;

    /**
     * Updates an existing Application Policy
     *
     * @param policy Policy to update
     * @throws APIMgtDAOException If failed to update the specified policy
     */
    void updateApplicationPolicy(ApplicationPolicy policy) throws APIMgtDAOException;

    /**
     * Updates an existing Subscription Policy
     *
     * @param policy Policy update
     * @throws APIMgtDAOException If failed to update the specified policy
     */
    void updateSubscriptionPolicy(SubscriptionPolicy policy) throws APIMgtDAOException;

    /**
     * Updates an existing API Policy
     *
     * @param policy Policy update
     * @throws APIMgtDAOException If failed to update the specified policy
     */
    void updateApiPolicy(APIPolicy policy) throws APIMgtDAOException;

    /**
     * Deletes an existing Policy
     *
     * @param policyLevel Policy Level to which the policy belongs to
     * @param policyName  Policy Name to delete
     * @throws APIMgtDAOException If failed to delete a policy.
     */
    void deletePolicy(APIMgtAdminService.PolicyLevel policyLevel, String policyName) throws APIMgtDAOException;

    /**
     * Deletes an existing Policy by uuid
     *
     * @param policyLevel Policy Name to delete
     * @param uuid        Policy id to delete
     * @throws APIMgtDAOException If failed to delete a policy.
     */
    void deletePolicyByUuid(APIMgtAdminService.PolicyLevel policyLevel, String uuid) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of a throttling policy given its policy level and policy name
     *
     * @param policyLevel level of the throttling policy
     * @param policyName  name of the throttling policy
     * @return last updated time
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    String getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIMgtDAOException;

    /**
     * Retrieve simplified list of Policies
     *
     * @return list of policies
     * @throws APIMgtDAOException if failed to retrieve policies from Data layer
     */
    Set<PolicyValidationData> getAllPolicies() throws APIMgtDAOException;

    /**
     * Gets all Policies by level and UUID
     *
     * @param policyLevel policy level
     * @param policyName  Policy Name
     * @return {@link Policy} instance
     * @throws APIMgtDAOException If failed to get the Policies
     */
    Policy getPolicyByLevelAndUUID(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIMgtDAOException;

    /**
     * Add a block condition.
     *
     * @param blockConditions BlockCondition object to be added
     * @return uuid of the block condition if successfully added
     * @throws APIMgtDAOException if failed to add block condition
     */
    String addBlockConditions(BlockConditions blockConditions) throws APIMgtDAOException;

    /**
     * Get details of a block condition by UUID.
     *
     * @param uuid uuid of the block condition
     * @return Block conditoin represented by the UUID
     * @throws APIMgtDAOException if getting a block condition from uuid failed
     */
    BlockConditions getBlockConditionByUUID(String uuid) throws APIMgtDAOException;

    /**
     * Return all block conditions.
     *
     * @return list of BlockConditions
     * @throws APIMgtDAOException if getting all block conditions failed
     */
    List<BlockConditions> getBlockConditions() throws APIMgtDAOException;

    /**
     * Update the block condition state true (Enabled) /false (Disabled) given the UUID.
     *
     * @param uuid  UUID of the block condition
     * @param state blocking state
     * @return true if the operation was success
     * @throws APIMgtDAOException if updating state of the block condition failed
     */
    boolean updateBlockConditionStateByUUID(String uuid, Boolean state) throws APIMgtDAOException;

    /**
     * Delete the block condition given the id.
     *
     * @param uuid UUID of the block condition
     * @return true if successfully deleted
     * @throws APIMgtDAOException if deletion failed
     */
    boolean deleteBlockConditionByUuid(String uuid) throws APIMgtDAOException;

    /**
     * Adding a custom policy to database.
     *
     * @param customPolicy CustomPolicy object to be added
     * @return uuid of the added policy
     * @throws APIMgtDAOException if policy adding failed
     */
    String addCustomPolicy(CustomPolicy customPolicy) throws APIMgtDAOException;

    /**
     * get all custom policies.
     *
     * @return List of CustomPolicy objects
     * @throws APIMgtDAOException if getting all custom policies failed
     */
    List<CustomPolicy> getCustomPolicies() throws APIMgtDAOException;

    /**
     * get custom policy by uuid.
     *
     * @param uuid uuid of the policy to be retrieved.
     * @return CustomPolicy object
     * @throws APIMgtDAOException if getting custom policy failed
     */
    CustomPolicy getCustomPolicyByUuid(String uuid) throws APIMgtDAOException;

    /**
     * Update custom policy.
     *
     * @param customPolicy CustomPolicy object to be updated
     * @throws APIMgtDAOException if update custom policy failed
     */
    void updateCustomPolicy(CustomPolicy customPolicy) throws APIMgtDAOException;

    /**
     * Delete Custom policy from database.
     *
     * @param uuid uuid of the policy to be deleted
     * @throws APIMgtDAOException if policy deletion failed
     */
    void deleteCustomPolicy(String uuid) throws APIMgtDAOException;

    /**
     * Gets Policy by level and name
     *
     * @param policyLevel policy level
     * @param policyName  Policy Name
     * @return {@link Policy} instance
     * @throws APIMgtDAOException If failed to get the Policies
     */
    Policy getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIMgtDAOException, APIMgtResourceNotFoundException;

}
