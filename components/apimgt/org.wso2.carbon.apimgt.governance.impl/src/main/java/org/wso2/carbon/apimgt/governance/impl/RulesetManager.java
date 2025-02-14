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

import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the Ruleset Manager.
 */
public class RulesetManager {

    private RulesetMgtDAO rulesetMgtDAO;

    public RulesetManager() {
        rulesetMgtDAO = RulesetMgtDAOImpl.getInstance();
    }

    /**
     * Create a new Governance Ruleset
     *
     * @param ruleset      Ruleset object
     * @param organization Organization
     * @return Ruleset Created object
     */

    public RulesetInfo createNewRuleset(Ruleset ruleset, String organization) throws APIMGovernanceException {

        if (rulesetMgtDAO.getRulesetByName(ruleset.getName(), organization) != null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_ALREADY_EXIST, ruleset.getName(),
                    organization);
        }
        ruleset.setId(APIMGovernanceUtil.generateUUID());

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance().
                getValidationEngineService().getValidationEngine();

        validationEngine.validateRulesetContent(ruleset);
        List<Rule> rules = validationEngine.extractRulesFromRuleset(ruleset);

        if (rules.isEmpty()) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT,
                    ruleset.getName());
        }

        return rulesetMgtDAO.createRuleset(ruleset, rules, organization);
    }

    /**
     * Delete a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the ruleset
     */

    public void deleteRuleset(String rulesetId, String organization) throws APIMGovernanceException {
        RulesetInfo ruleset = rulesetMgtDAO.getRulesetById(rulesetId, organization);
        if (ruleset == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        } else if (isRulesetAssociatedWithPolicies(rulesetId, organization)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_RULESET_ASSOCIATED_WITH_POLICIES,
                    ruleset.getId());
        }
        rulesetMgtDAO.deleteRuleset(rulesetId, organization);
    }

    /**
     * Check if a ruleset is associated with any policies
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return boolean True if the ruleset is associated with policies
     */
    private boolean isRulesetAssociatedWithPolicies(String rulesetId, String organization)
            throws APIMGovernanceException {
        List<String> policyIds = rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId, organization);
        return !policyIds.isEmpty();
    }

    /**
     * Update a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param ruleset      Ruleset object
     * @param organization Organization
     * @return Ruleset Updated object
     * @throws APIMGovernanceException If an error occurs while updating the ruleset
     */

    public RulesetInfo updateRuleset(String rulesetId, Ruleset ruleset, String organization)
            throws APIMGovernanceException {

        RulesetInfo existingRuleset = rulesetMgtDAO.getRulesetById(rulesetId, organization);
        if (existingRuleset == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }

        String newName = ruleset.getName();
        RulesetInfo existingRulesetByName = rulesetMgtDAO.getRulesetByName(newName, organization);
        if (existingRulesetByName != null && !existingRulesetByName.getId().equals(rulesetId)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_ALREADY_EXIST, newName, organization);
        }

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance().
                getValidationEngineService().getValidationEngine();

        validationEngine.validateRulesetContent(ruleset);
        List<Rule> rules = validationEngine.extractRulesFromRuleset(ruleset);

        if (rules.isEmpty()) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT,
                    ruleset.getName());
        }

        return rulesetMgtDAO.updateRuleset(rulesetId, ruleset, rules, organization);
    }

    /**
     * Get all the Governance Rulesets
     *
     * @param organization Organization
     * @return RulesetList object
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */

    public RulesetList getRulesets(String organization) throws APIMGovernanceException {
        return rulesetMgtDAO.getRulesets(organization);
    }

    /**
     * Get a Governance Ruleset by ID
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return RulesetInfo object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset
     */

    public RulesetInfo getRulesetById(String rulesetId, String organization) throws APIMGovernanceException {
        RulesetInfo ruleset = rulesetMgtDAO.getRulesetById(rulesetId, organization);
        if (ruleset == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }
        return ruleset;
    }

    /**
     * Get a Governance Ruleset by name
     *
     * @param name         Name of the ruleset
     * @param organization Organization
     * @return RulesetInfo object
     * @throws APIMGovernanceException If an error occurs while getting the ruleset
     */
    public RulesetInfo getRulesetByName(String name, String organization) throws APIMGovernanceException {
        return rulesetMgtDAO.getRulesetByName(name, organization);
    }

    /**
     * Get the content of a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return Content of the ruleset
     * @throws APIMGovernanceException If an error occurs while getting the ruleset content
     */

    public RulesetContent getRulesetContent(String rulesetId, String organization) throws APIMGovernanceException {
        RulesetContent content = rulesetMgtDAO.getRulesetContent(rulesetId, organization);
        if (content == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }
        return content;

    }

    /**
     * Get the policies using the Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of policies using the ruleset
     * @throws APIMGovernanceException If an error occurs while getting the ruleset usage
     */

    public List<String> getRulesetUsage(String rulesetId, String organization) throws APIMGovernanceException {
        RulesetInfo ruleset = rulesetMgtDAO.getRulesetById(rulesetId, organization);
        if (ruleset == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }
        return rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId, organization);
    }

    /**
     * Get the rules using the Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of rules using the ruleset
     * @throws APIMGovernanceException If an error occurs while getting the ruleset usage
     */

    public List<Rule> getRulesByRulesetId(String rulesetId, String organization) throws APIMGovernanceException {
        if (rulesetMgtDAO.getRulesetById(rulesetId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }
        return rulesetMgtDAO.getRulesByRulesetId(rulesetId, organization);
    }

    /**
     * Search for Governance Rulesets
     *
     * @param query        Search query
     * @param organization Organization
     * @return List of RulesetInfo objects
     * @throws APIMGovernanceException If an error occurs while searching for rulesets
     */

    public RulesetList searchRulesets(String query, String organization) throws APIMGovernanceException {
        Map<String, String> searchCriteria = getRulesetSearchCriteria(query);
        return rulesetMgtDAO.searchRulesets(searchCriteria, organization);

    }

    /**
     * Get the search criteria for the ruleset search from a query such as
     * `query=name:{name} ruleType:{type} artifactType:{type}`
     *
     * @param query Search query
     * @return Map of search criteria
     */
    private Map<String, String> getRulesetSearchCriteria(String query) {
        Map<String, String> criteriaMap = new HashMap<>();
        String[] criteria = query.split(" ");

        for (String criterion : criteria) {
            String[] parts = criterion.split(":");

            if (parts.length == 2) {
                String searchPrefix = parts[0];
                String searchValue = parts[1];

                // Add valid prefixes to criteriaMap
                if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.RulesetSearchAttributes.ARTIFACT_TYPE)) {
                    criteriaMap.put(APIMGovernanceConstants.RulesetSearchAttributes.ARTIFACT_TYPE, searchValue);
                } else if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.RulesetSearchAttributes.RULE_TYPE)) {
                    criteriaMap.put(APIMGovernanceConstants.RulesetSearchAttributes.RULE_TYPE, searchValue);
                } else if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.RulesetSearchAttributes.NAME)) {
                    criteriaMap.put(APIMGovernanceConstants.RulesetSearchAttributes.NAME, searchValue);
                }
            }
        }

        return criteriaMap;
    }

}
