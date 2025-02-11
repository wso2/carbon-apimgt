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
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.Policy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyContent;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.PolicyList;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the Governance Ruleset DAO
 */
public interface PolicyMgtDAO {

    /**
     * Create a new Governance Ruleset
     *
     * @param policy      Ruleset object
     * @param rules        List of rules
     * @param organization Organization
     * @return RulesetInfo Created object
     * @throws APIMGovernanceException If an error occurs while creating the ruleset
     */
    PolicyInfo createPolicy(Policy policy, List<Rule> rules, String organization) throws APIMGovernanceException;

    /**
     * Update a Governance Ruleset
     *
     * @param policyId Ruleset ID
     * @param policy   Ruleset object
     * @param rules     List of rules
     * @return RulesetInfo Updated object
     * @throws APIMGovernanceException If an error occurs while updating the ruleset
     */
    PolicyInfo updatePolicy(String policyId, Policy policy, List<Rule> rules, String organization)
            throws APIMGovernanceException;

    /**
     * Delete a Governance Ruleset
     *
     * @param policyId    Ruleset ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the ruleset
     */
    void deletePolicy(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Get all the Governance Rulesets of the organization
     *
     * @param organization Organization
     * @return RulesetList object
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    PolicyList getPolicies(String organization) throws APIMGovernanceException;

    /**
     * Get a Governance Ruleset by name
     *
     * @param name         Ruleset name
     * @param organization Organization
     * @return RulesetInfo object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset
     */
    PolicyInfo getPolicyByName(String name, String organization) throws APIMGovernanceException;

    /**
     * Get a Governance Ruleset by ID
     *
     * @param policyId    Ruleset ID
     * @param organization Organization
     * @return RulesetInfo object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset
     */
    PolicyInfo getPolicyById(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Search for Governance Rulesets based on the search criteria
     *
     * @param searchCriteria Search attributes
     * @param organization   Organization
     * @return List of RulesetInfo objects
     * @throws APIMGovernanceException If an error occurs while searching for policies
     */
    PolicyList searchPolicies(Map<String, String> searchCriteria, String organization)
            throws APIMGovernanceException;

    /**
     * Get the content of a Governance Ruleset
     *
     * @param policyId    Ruleset ID
     * @param organization Organization
     * @return Content of the ruleset
     * @throws APIMGovernanceException If an error occurs while getting the ruleset content
     */
    PolicyContent getPolicyContent(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Get the associated policy attachments for a policy
     *
     * @param policyId    Ruleset ID
     * @param organization Organization
     * @return List of associated policies
     */
    List<String> getAssociatedPolicyAttachmentForPolicy(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Get the rules of a Ruleset
     *
     * @param policyId    Ruleset ID
     * @param organization Organization
     * @return List of rules
     */
    List<Rule> getPolicyByPolicyId(String policyId, String organization) throws APIMGovernanceException;
}
