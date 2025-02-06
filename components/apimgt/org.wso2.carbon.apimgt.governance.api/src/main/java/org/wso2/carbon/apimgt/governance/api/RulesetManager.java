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

package org.wso2.carbon.apimgt.governance.api;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;

import java.util.List;

/**
 * This interface represents the Governance Ruleset Manager
 */
public interface RulesetManager {

    /**
     * Create a new Governance Ruleset
     *
     * @param ruleset      Ruleset object
     * @param organization Organization
     * @return RulesetInfo Created object
     * @throws GovernanceException If an error occurs while creating the ruleset
     */
    RulesetInfo createNewRuleset(Ruleset ruleset, String organization) throws GovernanceException;

    /**
     * Update a Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @param ruleset   Ruleset object
     * @return RulesetInfo Updated object
     * @throws GovernanceException If an error occurs while updating the ruleset
     */
    RulesetInfo updateRuleset(String rulesetId, Ruleset ruleset) throws GovernanceException;

    /**
     * Delete a Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @throws GovernanceException If an error occurs while deleting the ruleset
     */
    void deleteRuleset(String rulesetId) throws GovernanceException;

    /**
     * Get all the Governance Rulesets
     *
     * @param organization Organization
     * @return RulesetList object
     * @throws GovernanceException If an error occurs while getting the rulesets
     */
    RulesetList getRulesets(String organization) throws GovernanceException;


    /**
     * Get a Governance Ruleset by ID
     *
     * @param rulesetId Ruleset ID
     * @return RulesetInfo object
     * @throws GovernanceException If an error occurs while getting the ruleset
     */
    RulesetInfo getRulesetById(String rulesetId) throws GovernanceException;

    /**
     * Get the content of a Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @return String Content of the ruleset
     * @throws GovernanceException If an error occurs while getting the ruleset content
     */
    RulesetContent getRulesetContent(String rulesetId) throws GovernanceException;

    /**
     * Get the policies using the Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @return List of policies using the ruleset
     * @throws GovernanceException If an error occurs while getting the ruleset usage
     */
    List<String> getRulesetUsage(String rulesetId) throws GovernanceException;

    /**
     * Get the rules using the Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @return List of rules using the ruleset
     * @throws GovernanceException If an error occurs while getting the ruleset usage
     */
    List<Rule> getRules(String rulesetId) throws GovernanceException;

    /**
     * Search for Governance Rulesets
     *
     * @param query        Search query
     * @param organization Organization
     * @return List of RulesetInfo objects
     * @throws GovernanceException If an error occurs while searching for rulesets
     */
    RulesetList searchRulesets(String query, String organization) throws GovernanceException;
}
