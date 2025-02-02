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

import org.wso2.carbon.apimgt.governance.api.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

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
     * @param ruleset      Ruleset object
     * @param organization Organization
     * @return Ruleset Created object
     */
    @Override
    public RulesetInfo createNewRuleset(Ruleset ruleset, String organization) throws GovernanceException {
        ruleset.setId(ruleset.getId() == null ? GovernanceUtil.generateUUID() : ruleset.getId());
        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance().
                getValidationEngineService().getValidationEngine();
        boolean isRulesetContentValid = validationEngine.isRulesetValid(ruleset);
        if (!isRulesetContentValid) {
            throw new GovernanceException(GovernanceExceptionCodes.INVALID_RULESET_CONTENT,
                    ruleset.getName());
        }
        return rulesetMgtDAO.createRuleset(ruleset, organization);
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
     * @param rulesetId Ruleset ID
     * @return RulesetInfo object
     * @throws GovernanceException If an error occurs while getting the ruleset
     */
    @Override
    public RulesetInfo getRulesetById(String rulesetId) throws GovernanceException {
        return rulesetMgtDAO.getRulesetById(rulesetId);
    }

    /**
     * Get the content of a Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @return String Content of the ruleset
     * @throws GovernanceException If an error occurs while getting the ruleset content
     */
    @Override
    public String getRulesetContent(String rulesetId) throws GovernanceException {
        String content = rulesetMgtDAO.getRulesetContent(rulesetId);
        if (content == null) {
            throw new GovernanceException(GovernanceExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }
        return content;

    }

    /**
     * Delete a Governance Ruleset
     *
     * @param rulesetId    Ruleset ID
     * @throws GovernanceException If an error occurs while deleting the ruleset
     */
    @Override
    public void deleteRuleset(String rulesetId) throws GovernanceException {
        RulesetInfo ruleset = rulesetMgtDAO.getRulesetById(rulesetId);
        if (isRulesetAssociatedWithPolicies(rulesetId)) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_RULESET_ASSOCIATED_WITH_POLICIES,
                    ruleset.getId());
        }
        rulesetMgtDAO.deleteRuleset(rulesetId);
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
     * @param rulesetId    Ruleset ID
     * @param ruleset      Ruleset object
     * @return Ruleset Updated object
     * @throws GovernanceException If an error occurs while updating the ruleset
     */
    @Override
    public RulesetInfo updateRuleset(String rulesetId, Ruleset ruleset)
            throws GovernanceException {

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance().
                getValidationEngineService().getValidationEngine();
        boolean isRulesetContentValid = validationEngine.isRulesetValid(ruleset);
        if (!isRulesetContentValid) {
            throw new GovernanceException(GovernanceExceptionCodes.INVALID_RULESET_CONTENT,
                    ruleset.getName());
        }
        return rulesetMgtDAO.updateRuleset(rulesetId, ruleset);
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

    /**
     * Get the rules using the Governance Ruleset
     *
     * @param rulesetId Ruleset ID
     * @return List of rules using the ruleset
     * @throws GovernanceException If an error occurs while getting the ruleset usage
     */
    @Override
    public List<Rule> getRules(String rulesetId) throws GovernanceException {
        return rulesetMgtDAO.getRulesByRulesetId(rulesetId);
    }
}
