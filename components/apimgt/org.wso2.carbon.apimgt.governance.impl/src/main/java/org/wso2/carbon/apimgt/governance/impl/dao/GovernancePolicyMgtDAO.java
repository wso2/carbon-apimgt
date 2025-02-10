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

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the DAO layer for Governance Policy Management
 */
public interface GovernancePolicyMgtDAO {

    /**
     * Create a new Governance Policy
     *
     * @param governancePolicy Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Created object
     */
    APIMGovernancePolicy createGovernancePolicy(APIMGovernancePolicy governancePolicy,
                                                String organization) throws APIMGovernanceException;

    /**
     * Get Governance Policy by Name
     *
     * @param policyName   Policy Name
     * @param organization Organization
     * @return APIMGovernancePolicy
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */
    APIMGovernancePolicy getGovernancePolicyByName(String policyName,
                                                   String organization) throws APIMGovernanceException;

    /**
     * Get Governance Policy by ID
     *
     * @param policyID     Policy ID
     * @param organization Organization
     * @return APIMGovernancePolicy
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */
    APIMGovernancePolicy getGovernancePolicyByID(String policyID, String organization) throws APIMGovernanceException;

    /**
     * Get all the Governance Policies
     *
     * @param organization Organization
     * @return APIMGovernancePolicyList object
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    APIMGovernancePolicyList getGovernancePolicies(String organization) throws APIMGovernanceException;

    /**
     * Update a Governance Policy
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    APIMGovernancePolicy updateGovernancePolicy(String policyId, APIMGovernancePolicy governancePolicy,
                                                String organization) throws APIMGovernanceException;

    /**
     * Get the list of rulesets for a given policy
     *
     * @param policyId Policy ID
     * @return List of rulesets
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */
    List<Ruleset> getRulesetsWithContentByPolicyId(String policyId, String organization)
            throws APIMGovernanceException;

    /**
     * Get the list of rulesets for a given policy
     *
     * @param policyId Policy ID
     * @return List of rulesets
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */
    List<RulesetInfo> getRulesetsByPolicyId(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Get the list of policies by label
     *
     * @param label        label
     * @param organization organization
     * @return Map of policy IDs, policy names
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    Map<String, String> getPoliciesByLabel(String label, String organization) throws APIMGovernanceException;

    /**
     * Get PolicyIds by label
     *
     * @param label        Label
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of Policy IDs
     */
    List<String> getPoliciesByLabelAndState(String label, APIMGovernableState state, String organization)
            throws APIMGovernanceException;

    /**
     * Get Policies without labels
     *
     * @param organization Organization
     * @return Map of Policy IDs, Policy Names
     */
    Map<String, String> getGlobalPolicies(String organization)
            throws APIMGovernanceException;

    /**
     * Get Policies without labels by state
     *
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of Policy IDs
     */
    List<String> getGlobalPoliciesWithState(APIMGovernableState state, String organization)
            throws APIMGovernanceException;

    /**
     * Get the actions of a policy
     *
     * @param policyId Policy ID
     * @return List of Governance Actions
     * @throws APIMGovernanceException If an error occurs while getting the actions
     */
    List<APIMGovernanceAction> getActionsByPolicyId(String policyId) throws APIMGovernanceException;

    /**
     * Search for Governance Policies
     *
     * @param searchCriteria Search criteria
     * @param organization   Organization
     * @return APIMGovernancePolicyList object
     * @throws APIMGovernanceException If an error occurs while searching for policies
     */
    APIMGovernancePolicyList searchPolicies(Map<String, String> searchCriteria, String organization)
            throws APIMGovernanceException;

    /**
     * Delete a Governance Policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */
    void deletePolicy(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Delete policy label mappings for a given label
     *
     * @param label        label
     * @param organization organization
     * @throws APIMGovernanceException If an error occurs while deleting the mappings
     */
    void deleteLabelPolicyMappings(String label, String organization) throws APIMGovernanceException;
}


