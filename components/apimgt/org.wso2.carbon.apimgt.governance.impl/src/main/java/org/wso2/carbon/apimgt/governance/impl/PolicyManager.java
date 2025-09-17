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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.util.AuditLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the Governance Policy Manager
 */
public class PolicyManager {

    private static final Log log = LogFactory.getLog(PolicyManager.class);
    private final GovernancePolicyMgtDAO policyMgtDAO;

    public PolicyManager() {
        policyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
    }

    /**
     * Create a new Governance Policy
     *
     * @param organization     Organization
     * @param governancePolicy Governance Policy
     * @return APIMGovernancePolicy Created object
     * @throws APIMGovernanceException If an error occurs while creating the policy
     */

    public APIMGovernancePolicy createGovernancePolicy(String organization, APIMGovernancePolicy
            governancePolicy) throws APIMGovernanceException {

        if (log.isDebugEnabled()) {
            log.debug("Creating governance policy: " + governancePolicy.getName() + 
                     " in organization: " + organization);
        }

        if (policyMgtDAO.getGovernancePolicyByName(governancePolicy.getName(), organization) != null) {
            log.warn("Policy with name " + governancePolicy.getName() + 
                    " already exists in organization: " + organization);
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_ALREADY_EXISTS,
                    governancePolicy.getName(), organization);
        }

        governancePolicy.setId(APIMGovernanceUtil.generateUUID());
        checkForInvalidActions(governancePolicy);
        addMissingNotifyActions(governancePolicy);

        APIMGovernancePolicy newPolicy = policyMgtDAO.createGovernancePolicy(governancePolicy, organization);
        log.info("Successfully created governance policy: " + newPolicy.getName() + 
                " with " + newPolicy.getActions().size() + " actions");
        AuditLogger.log("Governance Policy",
                "New governance policy %s with ID %s created by user %s in organization %s",
                newPolicy.getName(), newPolicy.getId(), newPolicy.getCreatedBy(), organization);
        return newPolicy;
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */

    public APIMGovernancePolicy updateGovernancePolicy(String policyId, APIMGovernancePolicy governancePolicy,
                                                       String organization)
            throws APIMGovernanceException {

        if (policyMgtDAO.getGovernancePolicyByID(policyId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }

        String newName = governancePolicy.getName();
        APIMGovernancePolicy policyWithNewName = policyMgtDAO.getGovernancePolicyByName(newName, organization);
        if (policyWithNewName != null && !policyWithNewName.getId().equals(policyId)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_ALREADY_EXISTS, newName, organization);
        }

        checkForInvalidActions(governancePolicy);
        addMissingNotifyActions(governancePolicy);

        APIMGovernancePolicy updatedPolicy = policyMgtDAO.updateGovernancePolicy(policyId, governancePolicy,
                organization);
        AuditLogger.log("Governance Policy", "Governance policy %s with ID %s updated by user %s in organization %s",
                updatedPolicy.getName(), updatedPolicy.getId(), updatedPolicy.getUpdatedBy(), organization);
        return updatedPolicy;
    }

    /**
     * This checks whether any invalid action such as,
     * - Actions assigned to invalid governable states
     * - BLOCK actions are present for API_CREATE and API_UPDATE states
     *
     * @param policy Governance Policy
     * @throws APIMGovernanceException If an error occurs while checking the actions
     */
    private void checkForInvalidActions(APIMGovernancePolicy policy)
            throws APIMGovernanceException {

        List<APIMGovernableState> apimGovernableStates = policy.getGovernableStates();
        List<APIMGovernanceAction> actions = policy.getActions();
        for (APIMGovernanceAction action : actions) {
            if (!apimGovernableStates.contains(action.getGovernableState())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_ASSIGNING_ACTION_TO_POLICY,
                        "Invalid governable state found in the policy. Please update the policy");
            }
            if (APIMGovernanceActionType.BLOCK.equals(action.getType()) &&
                    (APIMGovernableState.API_CREATE.equals(action.getGovernableState()) ||
                            APIMGovernableState.API_UPDATE.equals(action.getGovernableState()))) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_ASSIGNING_ACTION_TO_POLICY,
                        "Creating policies with blocking actions for API" +
                                " create/update is not allowed. Please update the policy");
            }
        }

    }

    /**
     * This method adds missing notify actions for each governable state
     *
     * @param policy Governance Policy
     */
    private void addMissingNotifyActions(APIMGovernancePolicy policy) {

        List<APIMGovernableState> apimGovernableStates = policy.getGovernableStates();
        List<APIMGovernanceAction> actions = policy.getActions();
        for (APIMGovernableState state : apimGovernableStates) {
            for (RuleSeverity severity : RuleSeverity.values()) {
                boolean isActionPresent = false;
                for (APIMGovernanceAction action : actions) {
                    if (state.equals(action.getGovernableState()) &&
                            severity.equals(action.getRuleSeverity())) {
                        isActionPresent = true;
                        break;
                    }
                }
                if (!isActionPresent) {
                    APIMGovernanceAction notifyAction = new APIMGovernanceAction();
                    notifyAction.setType(APIMGovernanceActionType.NOTIFY);
                    notifyAction.setGovernableState(state);
                    notifyAction.setRuleSeverity(severity);
                    actions.add(notifyAction);
                }

            }
        }
        policy.setActions(actions);
    }

    /**
     * Delete a Governance Policy
     *
     * @param policyId     Policy ID
     * @param username     Username
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */

    public void deletePolicy(String policyId, String username, String organization) throws APIMGovernanceException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting governance policy with ID: " + policyId + 
                     " in organization: " + organization);
        }
        APIMGovernancePolicy policy = policyMgtDAO.getGovernancePolicyByID(policyId, organization);
        if (policy == null) {
            log.warn("Policy not found with ID: " + policyId);
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }

        policyMgtDAO.deletePolicy(policyId, organization);
        log.info("Successfully deleted governance policy: " + policy.getName());
        AuditLogger.log("Governance Policy", "Governance policy %s with ID %s deleted by user %s in organization %s",
                policy.getName(), policy.getId(), username, organization);
    }

    /**
     * Get Governance Policy by Name
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return APIMGovernancePolicy
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */

    public APIMGovernancePolicy getGovernancePolicyByID(String policyId, String organization)
            throws APIMGovernanceException {
        APIMGovernancePolicy policyInfo = policyMgtDAO.getGovernancePolicyByID(policyId, organization);
        if (policyInfo == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }
        return policyInfo;
    }

    /**
     * Get Governance Policies
     *
     * @param organization Organization
     * @return APIMGovernancePolicyList
     * @throws APIMGovernanceException If an error occurs while retrieving the policies
     */

    public APIMGovernancePolicyList getGovernancePolicies(String organization) throws APIMGovernanceException {
        return policyMgtDAO.getGovernancePolicies(organization);
    }

    /**
     * Get the list of rulesets for a given policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return List of rulesets
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */

    public List<RulesetInfo> getRulesetsByPolicyId(String policyId,
                                                   String organization) throws APIMGovernanceException {
        if (policyMgtDAO.getGovernancePolicyByID(policyId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }
        return policyMgtDAO.getRulesetsByPolicyId(policyId, organization);
    }

    /**
     * Get the list of rulesets with content for a given policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return List of rulesets with content
     * @throws APIMGovernanceException If an error occurs while getting the rulesets
     */
    public List<Ruleset> getRulesetsWithContentByPolicyId(String policyId, String organization)
            throws APIMGovernanceException {
        if (policyMgtDAO.getGovernancePolicyByID(policyId, organization) == null) {
            log.warn("Policy not found for ID: " + policyId);
            return new ArrayList<>();
        }
        return policyMgtDAO.getRulesetsWithContentByPolicyId(policyId, organization);
    }

    /**
     * Get the list of policies by label
     *
     * @param label        label
     * @param organization organization
     * @return Map of Policy IDs, Policy Names
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */

    public Map<String, String> getPoliciesByLabel(String label, String organization)
            throws APIMGovernanceException {
        return policyMgtDAO.getPoliciesByLabel(label, organization);
    }

    /**
     * Get the list of organization wide policies
     *
     * @param organization organization
     * @return Map of Policy IDs, Policy Names
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */

    public Map<String, String> getOrganizationWidePolicies(String organization) throws APIMGovernanceException {
        return policyMgtDAO.getGlobalPolicies(organization);
    }

    /**
     * Get the list of policies by label and state
     *
     * @param label        Label
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of policy IDs
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */

    public List<String> getPoliciesByLabelAndState(String label, APIMGovernableState state, String organization)
            throws APIMGovernanceException {
        return policyMgtDAO.getPoliciesByLabelAndState(label, state, organization);
    }

    /**
     * Get the list of organization wide policies by state
     *
     * @param state        Governable State for the policy
     * @param organization organization
     * @return List of policy IDs
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */

    public List<String> getOrganizationWidePoliciesByState(APIMGovernableState state, String organization)
            throws APIMGovernanceException {
        return policyMgtDAO.getGlobalPoliciesWithState(state, organization);
    }

    /**
     * This method checks whether a blocking action is present for a given governable state of a policy
     *
     * @param policyId     Policy ID
     * @param state        Governable State
     * @param organization Organization
     * @return true if a blocking action is present, false otherwise
     * @throws APIMGovernanceException If an error occurs while checking the presence of blocking action
     */

    public boolean isBlockingActionPresentForState(String policyId, APIMGovernableState state, String organization)
            throws APIMGovernanceException {
        if (policyMgtDAO.getGovernancePolicyByID(policyId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }
        boolean isBlockingActionPresent = false;
        List<APIMGovernanceAction> actions = policyMgtDAO.getActionsByPolicyId(policyId);
        for (APIMGovernanceAction action : actions) {
            if (APIMGovernanceActionType.BLOCK
                    .equals(action.getType()) &&
                    action.getGovernableState().equals(state)) {
                isBlockingActionPresent = true;
                break;
            }
        }
        return isBlockingActionPresent;
    }

    /**
     * This method searches for governance policies
     *
     * @param query        query
     * @param organization organization
     * @return APIMGovernancePolicyList
     * @throws APIMGovernanceException If an error occurs while searching for policies
     */

    public APIMGovernancePolicyList searchGovernancePolicies(String query, String organization)
            throws APIMGovernanceException {
        Map<String, String> searchCriteria = getPolicySearchCriteria(query);
        return policyMgtDAO.searchPolicies(searchCriteria, organization);
    }


    /**
     * Get the search criteria for the polict search from a query such as
     * `query=name:{name} state={state}`
     *
     * @param query Search query
     * @return Map of search criteria
     */
    private Map<String, String> getPolicySearchCriteria(String query) {
        Map<String, String> criteriaMap = new HashMap<>();

        // Regex to match key-value pairs, allowing values with spaces
        Pattern pattern = Pattern.compile("(\\w+):([^:]+)(?=\\s+\\w+:|$)");
        Matcher matcher = pattern.matcher(query);

        while (matcher.find()) {
            String searchPrefix = matcher.group(1);
            String searchValue = matcher.group(2);

            // Add valid prefixes to criteriaMap
            if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicySearchAttributes.STATE)) {
                criteriaMap.put(APIMGovernanceConstants.PolicySearchAttributes.STATE, searchValue);
            } else if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicySearchAttributes.NAME)) {
                criteriaMap.put(APIMGovernanceConstants.PolicySearchAttributes.NAME, searchValue);
            }

        }
        return criteriaMap;
    }

    /**
     * Delete the label policy mappings
     *
     * @param label Label ID
     * @throws APIMGovernanceException If an error occurs while deleting the mappings
     */
    public void deleteLabelPolicyMappings(String label) throws APIMGovernanceException {
        policyMgtDAO.deleteLabelPolicyMappings(label);
        AuditLogger.log("Governance Policy", "Label policy mappings deleted for label %s", label);
    }
}
