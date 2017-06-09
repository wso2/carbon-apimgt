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

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.util.List;

/**
 * Provides access to Policy data layer
 */
public interface PolicyDAO {

    /**
     * Gets a Policy by Name and Level
     *
     * @param policyLevel Policy level to which this policy belongs to
     * @param policyName Policy Name
     * @return {@link Policy} Gets a Policy by given name and level
     * @throws APIMgtDAOException If failed to get the Policy
     */
    Policy getPolicy(String policyLevel, String policyName) throws APIMgtDAOException;

    /**
     * Gets a Policy by uuid and Level
     *
     * @param policyLevel Policy level to which this policy belongs to
     * @param uuid policy id
     * @return {@link Policy} Gets a Policy by given name and level
     * @throws APIMgtDAOException If failed to get the Policy
     */
    Policy getPolicyByUuid(String policyLevel, String uuid) throws APIMgtDAOException;

    /**
     * Gets all the Policies belongs to a level
     *
     * @param policyLevel Policy level
     * @return {@link List} List Policies belongs to the provided level
     * @throws APIMgtDAOException If failed to get Policies
     */
    List<Policy> getPolicies(String policyLevel) throws APIMgtDAOException;

    /**
     * Adds a Policy belongs to a level
     *
     * @param policyLevel Policy Level to which this Policy belongs to
     * @param policy Policy to add
     * @throws APIMgtDAOException If failed to add a Policy
     */
    void addPolicy(String policyLevel, Policy policy) throws APIMgtDAOException;

    /**
     * Updates a Policy belongs to a level
     *
     * @param policyLevel Policy Level to which this Policy belongs to
     * @param policy Policy to add
     * @throws APIMgtDAOException If failed to update the specified policy
     */
    void updatePolicy(String policyLevel, Policy policy) throws APIMgtDAOException;

    /**
     * Deletes a Policy by Name and Level
     *
     * @param policyName Policy Name to delete
     * @param policyLevel Policy Level to which the policy belongs to
     * @throws APIMgtDAOException If failed to delete a policy.
     */
    void deletePolicy(String policyName, String policyLevel) throws APIMgtDAOException;

    /**
     * Deletes a Policy by Name and Level
     *
     * @param uuid Policy Name to delete
     * @param policyLevel Policy Level to which the policy belongs to
     * @throws APIMgtDAOException If failed to delete a policy.
     */
    void deletePolicyByUuid(String uuid, String policyLevel) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of a throttling policy given its policy level and policy name
     *
     * @param policyLevel level of the throttling policy
     * @param policyName  name of the throttling policy
     * @return last updated time
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    String getLastUpdatedTimeOfThrottlingPolicy(String policyLevel, String policyName) throws APIMgtDAOException;
}
