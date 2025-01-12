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

package org.wso2.carbon.apimgt.governance.impl;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.manager.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.validator.SpectralValidationEngine;

import java.util.List;

/**
 * Implementation of the RulesetManager interface.
 */
public class RulesetManagerImpl implements RulesetManager {

    private RulesetMgtDAO rulesetMgtDAO;

    public RulesetManagerImpl() {
        rulesetMgtDAO = RulesetMgtDAOImpl.getInstance();
    }

    /**
     * Create a new Governance Ruleset
     *
     * @param organization Organization
     * @param ruleset      Ruleset object
     * @return Ruleset Created object
     * @throws GovernanceException If an error occurs while creating the ruleset
     */
    @Override
    public RulesetInfo createNewRuleset(String organization, Ruleset ruleset) throws GovernanceException {
        ruleset.setId(GovernanceUtil.generateUUID());
        boolean isRulesetContentValid = SpectralValidationEngine.getInstance().isRulesetValid(ruleset);
        if (!isRulesetContentValid) {
            throw new GovernanceException(GovernanceExceptionCodes.INVALID_RULESET_CONTENT,
                    ruleset.getName());
        }
        return rulesetMgtDAO.createRuleset(organization, ruleset);
    }

    /**
     * Get all the Governance Rulesets
     *
     * @param organization Organization
     * @return RulesetList object
     * @throws GovernanceException If an error occurs while getting the rulesets
     */
    @Override
    public RulesetList getRulesets(String organization) throws GovernanceException {
        return rulesetMgtDAO.getRulesets(organization);
    }

    /**
     * Get a Governance Ruleset by ID
     *
     * @param organization Organization
     * @param rulesetId    Ruleset ID
     * @return RulesetInfo object
     * @throws GovernanceException If an error occurs while getting the ruleset
     */
    @Override
    public RulesetInfo getRulesetById(String organization, String rulesetId) throws GovernanceException {
        return rulesetMgtDAO.getRulesetById(organization, rulesetId);
    }

    /**
     * Get the content of a Governance Ruleset
     *
     * @param organization Organization
     * @param rulesetId    Ruleset ID
     * @return String Content of the ruleset
     * @throws GovernanceException If an error occurs while getting the ruleset content
     */
    @Override
    public String getRulesetContent(String organization, String rulesetId) throws GovernanceException {
        String content = rulesetMgtDAO.getRulesetContent(organization, rulesetId);
        if (content == null) {
            throw new GovernanceException(GovernanceExceptionCodes.RULESET_NOT_FOUND, rulesetId, organization);
        }
        return content;

    }

    /**
     * Delete a Governance Ruleset
     *
     * @param organization Organization
     * @param rulesetId    Ruleset ID
     * @return String Ruleset ID of the deleted ruleset
     * @throws GovernanceException If an error occurs while deleting the ruleset
     */
    @Override
    public void deleteRuleset(String organization, String rulesetId) throws GovernanceException {
        RulesetInfo ruleset = rulesetMgtDAO.getRulesetById(organization, rulesetId);
        if (isRulesetAssociatedWithPolicies(rulesetId)) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_RULESET_ASSOCIATED_WITH_POLICIES,
                    ruleset.getId(), organization);
        }
        rulesetMgtDAO.deleteRuleset(organization, rulesetId);
    }

    /**
     * Check if a ruleset is associated with any policies
     *
     * @param rulesetId Ruleset ID
     * @return boolean True if the ruleset is associated with policies
     */
    private boolean isRulesetAssociatedWithPolicies(String rulesetId) throws GovernanceException {
        List<String> policyIds = rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId);
        return !policyIds.isEmpty();
    }

    /**
     * Update a Governance Ruleset
     *
     * @param organization Organization
     * @param rulesetId    Ruleset ID
     * @param ruleset      Ruleset object
     * @return Ruleset Updated object
     * @throws GovernanceException If an error occurs while updating the ruleset
     */
    @Override
    public RulesetInfo updateRuleset(String organization, String rulesetId, Ruleset ruleset) throws GovernanceException {
        boolean isRulesetContentValid = SpectralValidationEngine.getInstance().isRulesetValid(ruleset);
        if (!isRulesetContentValid) {
            throw new GovernanceException(GovernanceExceptionCodes.INVALID_RULESET_CONTENT,
                    ruleset.getName());
        }
        return rulesetMgtDAO.updateRuleset(organization, rulesetId, ruleset);
    }

    /**
     * Get the policies using the Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @return List of policies using the ruleset
     * @throws GovernanceException If an error occurs while getting the ruleset usage
     */
    @Override
    public List<String> getRulesetUsage(String rulesetId) throws GovernanceException {
        return rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId);
    }
}
