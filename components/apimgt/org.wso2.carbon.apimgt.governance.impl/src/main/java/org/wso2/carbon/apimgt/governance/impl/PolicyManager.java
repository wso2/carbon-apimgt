/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl;

import org.wso2.carbon.apimgt.governance.impl.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.impl.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.impl.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.impl.model.Ruleset;

import java.util.List;

/**
 * This interface represents the Governance Policy Manager
 */
public interface PolicyManager {
    /**
     * Create a new Governance Policy
     *
     * @param organization     Organization
     * @param governancePolicy Governance Policy
     * @return GovernancePolicyInfo Created object
     * @throws GovernanceException If an error occurs while creating the policy
     */
    GovernancePolicy createGovernancePolicy(String organization, GovernancePolicy
            governancePolicy) throws GovernanceException;

    /**
     * Get Governance Policy by Name
     *
     * @param organization Organization
     * @param policyID     Policy ID
     * @return GovernancePolicyInfo
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    GovernancePolicy getGovernancePolicyByID(String organization, String policyID) throws GovernanceException;

    /**
     * Get Governance Policies
     *
     * @param organization Organization
     * @return GovernancePolicyList
     * @throws GovernanceException If an error occurs while retrieving the policies
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
     * @param policyId                           Policy ID
     * @param organization                       Organization
     * @param governancePolicy Governance Policy Info
     * @return GovernancePolicyInfo Updated object
     * @throws GovernanceException If an error occurs while updating the policy
     */
    GovernancePolicy updateGovernancePolicy(String policyId, String organization,
                                            GovernancePolicy
                                                    governancePolicy)
            throws GovernanceException;


    /**
     * Get the list of rulesets for a given policy
     *
     * @param policyId Policy ID
     * @return List of rulesets
     * @throws GovernanceException If an error occurs while getting the rulesets
     */
    List<Ruleset> getRulesetsByPolicyId(String policyId) throws GovernanceException;

    /**
     * Get the list of policies by label and state
     *
     * @param label        label
     * @param state        governable state for policy
     * @param organization organization
     * @return List of policy IDs
     * @throws GovernanceException If an error occurs while getting the policies
     */
    List<String> getPoliciesByLabelAndState(String label, String state, String organization) throws GovernanceException;

    /**
     * Get the list of organization wide policies by state
     *
     * @param state        governable state for policy
     * @param organization organization
     * @return List of policy IDs
     * @throws GovernanceException If an error occurs while getting the policies
     */
    List<String> getOrganizationWidePoliciesByState(String state, String organization) throws GovernanceException;

    /**
     * This method checks whether a blocking action is present for a given governable state of a policy
     *
     * @param policyId Policy ID
     * @param state    Governable State
     * @return
     * @throws GovernanceException
     */
    boolean isBlockingActionPresentForState(String policyId, String state) throws GovernanceException;

}
