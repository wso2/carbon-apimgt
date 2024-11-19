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
import org.wso2.carbon.apimgt.governance.impl.dao.RulsetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulsetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.util.List;

/**
 * Implementation of the RulesetManager interface.
 */
public class RulesetManagerImpl implements RulesetManager {

    private RulsetMgtDAO rulesetMgtDAO;

    public RulesetManagerImpl() {
        rulesetMgtDAO = RulsetMgtDAOImpl.getInstance();
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
    public Ruleset createNewRuleset(String organization, Ruleset ruleset) throws GovernanceException {
        ruleset.setId(GovernanceUtil.generateUUID());
        //TODO: Validate ruleset content with spectral service before creation
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
        RulesetInfo result = rulesetMgtDAO.getRulesetById(organization, rulesetId);
        if (result == null) {
            throw new GovernanceException(GovernanceExceptionCodes.RULESET_NOT_FOUND, rulesetId, organization);
        }
        return result;
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
        // Check if default ruleset is attempted to be deleted
        RulesetInfo ruleset = rulesetMgtDAO.getRulesetById(organization, rulesetId);
        if (ruleset == null) {
            throw new GovernanceException(GovernanceExceptionCodes.RULESET_NOT_FOUND, rulesetId, organization);
        } else if (ruleset.isDefault() == 1) {
            // Can not modify default rulesets
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_CANNOT_MODIFY_DEFAULT_RULESET,
                    ruleset.getName(), organization);
        } else if (isRulesetAssociatedWithPolicies(rulesetId)) {
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
    public Ruleset updateRuleset(String organization, String rulesetId, Ruleset ruleset) throws GovernanceException {
        RulesetInfo oldRuleset = rulesetMgtDAO.getRulesetById(organization, rulesetId);
        if (oldRuleset == null) {
            throw new GovernanceException(GovernanceExceptionCodes.RULESET_NOT_FOUND, rulesetId, organization);
        } else if (oldRuleset.isDefault() == 1) {
            // Can not modify default rulesets
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_CANNOT_MODIFY_DEFAULT_RULESET,
                    ruleset.getName(), organization);
        }
        //TODO: Validate ruleset content with spectral service before update
        return rulesetMgtDAO.updateRuleset(organization, rulesetId, ruleset);
    }
}
