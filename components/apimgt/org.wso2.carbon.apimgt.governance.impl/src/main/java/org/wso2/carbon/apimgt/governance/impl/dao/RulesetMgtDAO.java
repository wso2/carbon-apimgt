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
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the Governance Ruleset DAO
 */
public interface RulesetMgtDAO {

    /**
     * Create a new Governance Ruleset
     *
     * @param ruleset      Ruleset object
     * @param rules        List of rules
     * @param organization Organization
     * @return RulesetInfo Created object
     * @throws APIMGovernanceException If an error occurs while creating the ruleset
     */
    RulesetInfo createRuleset(Ruleset ruleset, List<Rule> rules, String organization) throws APIMGovernanceException;

    /**
     * Update a Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @param ruleset   Ruleset object
     * @param rules     List of rules
     * @return RulesetInfo Updated object
     * @throws APIMGovernanceException If an error occurs while updating the ruleset
     */
    RulesetInfo updateRuleset(String rulesetId, Ruleset ruleset, List<Rule> rules, String organization)
            throws APIMGovernanceException;

    /**
     * Delete a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the ruleset
     */
    void deleteRuleset(String rulesetId, String organization) throws APIMGovernanceException;

    /**
     * Get all the Governance Rulesets of the organization
     *
     * @param organization Organization
     * @return RulesetList object
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */
    RulesetList getRulesets(String organization) throws APIMGovernanceException;

    /**
     * Get a Governance Ruleset by name
     *
     * @param name         Ruleset name
     * @param organization Organization
     * @return RulesetInfo object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset
     */
    RulesetInfo getRulesetByName(String name, String organization) throws APIMGovernanceException;

    /**
     * Get a Governance Ruleset by ID
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return RulesetInfo object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset
     */
    RulesetInfo getRulesetById(String rulesetId, String organization) throws APIMGovernanceException;

    /**
     * Search for Governance Rulesets based on the search criteria
     *
     * @param searchCriteria Search attributes
     * @param organization   Organization
     * @return List of RulesetInfo objects
     * @throws APIMGovernanceException If an error occurs while searching for rulesets
     */
    RulesetList searchRulesets(Map<String, String> searchCriteria, String organization)
            throws APIMGovernanceException;

    /**
     * Get the content of a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return Content of the ruleset
     * @throws APIMGovernanceException If an error occurs while getting the ruleset content
     */
    RulesetContent getRulesetContent(String rulesetId, String organization) throws APIMGovernanceException;

    /**
     * Get the associated policies for a Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of associated policies
     */
    List<String> getAssociatedPoliciesForRuleset(String rulesetId, String organization) throws APIMGovernanceException;

    /**
     * Get the rules of a Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of rules
     */
    List<Rule> getRulesByRulesetId(String rulesetId, String organization) throws APIMGovernanceException;
}
