/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl.dao;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;

/**
 * This interface represents the DAO layer for Governance Policy Management
 */
public interface GovernancePolicyMgtDAO {

    /**
     * Create a new Governance Policy
     *
     * @param organization     Organization
     * @param governancePolicy Governance Policy
     * @return GovernancePolicy Created object
     */
    GovernancePolicy createGovernancePolicy(String organization,
                                            GovernancePolicy
                                                    governancePolicy) throws GovernanceException;

    /**
     * Get Governance Policy by Name
     *
     * @param organization Organization
     * @param policyName   Policy Name
     * @return GovernancePolicy
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    GovernancePolicy getGovernancePolicyByName(String organization, String policyName) throws GovernanceException;

    /**
     * Get Governance Policy by ID
     *
     * @param organization Organization
     * @param policyID     Policy ID
     * @return GovernancePolicy
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    GovernancePolicy getGovernancePolicyByID(String organization, String policyID) throws GovernanceException;

    /**
     * Get all the Governance Policies
     *
     * @param organization Organization
     * @return GovernancePolicyList object
     * @throws GovernanceException If an error occurs while getting the policies
     */
    GovernancePolicyList getGovernancePolicies(String organization) throws GovernanceException;

    /**
     * Delete a Governance Policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @throws GovernanceException If an error occurs while deleting the policy
     */
    void deletePolicy(String policyId, String organization) throws GovernanceException;

    /**
     * Update a Governance Policy
     *
     * @param policyId         Policy ID
     * @param organization     Organization
     * @param governancePolicy Governance Policy
     * @return GovernancePolicy Updated object
     * @throws GovernanceException If an error occurs while updating the policy
     */
    GovernancePolicy updateGovernancePolicy(String policyId, String organization,
                                            GovernancePolicy
                                                    governancePolicy)
            throws GovernanceException;
}

