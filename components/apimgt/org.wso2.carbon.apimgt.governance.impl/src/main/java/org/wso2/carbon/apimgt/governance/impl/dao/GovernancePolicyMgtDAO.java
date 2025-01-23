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
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;

import java.util.List;
import java.util.Map;

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


    /**
     * Get the list of rulesets for a given policy
     *
     * @param policyId Policy ID
     * @return List of rulesets
     * @throws GovernanceException If an error occurs while getting the rulesets
     */
    List<Ruleset> getRulesetsByPolicyId(String policyId) throws GovernanceException;

    /**
     * Get the list of policies by label
     *
     * @param label        label
     * @param organization organization
     * @return Map of policy IDs, policy names
     * @throws GovernanceException If an error occurs while getting the policies
     */
    Map<String, String> getPoliciesByLabel(String label, String organization) throws GovernanceException;

    /**
     * Get PolicyIds by label
     *
     * @param label        Label
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of Policy IDs
     */
    List<String> getPoliciesByLabelAndState(String label, GovernableState state, String organization)
            throws GovernanceException;


    /**
     * Get Policies without labels
     *
     * @param organization Organization
     * @return Map of Policy IDs, Policy Names
     */
    Map<String, String> getPoliciesWithoutLabels(String organization)
            throws GovernanceException;

    /**
     * Get Policies without labels by state
     *
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of Policy IDs
     */
    List<String> getPoliciesWithoutLabelsByState(GovernableState state, String organization)
            throws GovernanceException;

    /**
     * Get the actions of a policy
     *
     * @param policyId Policy ID
     * @return List of Governance Actions
     * @throws GovernanceException If an error occurs while getting the actions
     */
    List<GovernanceAction> getActionsByPolicyId(String policyId) throws GovernanceException;

    /**
     * Get the labels of a policy
     *
     * @param policyId Policy ID
     * @return List of Labels
     * @throws GovernanceException If an error occurs while getting the labels
     */
    List<String> getLabelsByPolicyId(String policyId) throws GovernanceException;
}


