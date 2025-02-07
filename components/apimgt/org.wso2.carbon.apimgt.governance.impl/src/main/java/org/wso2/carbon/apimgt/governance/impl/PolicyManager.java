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

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Governance Policy Manager
 */
public class PolicyManager {

    private final GovernancePolicyMgtDAO policyMgtDAO;

    public PolicyManager() {
        policyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
    }

    /**
     * Create a new Governance Policy
     *
     * @param organization     Organization
     * @param governancePolicy Governance Policy
     * @return GovernancePolicy Created object
     * @throws GovernanceException If an error occurs while creating the policy
     */

    public GovernancePolicy createGovernancePolicy(String organization, GovernancePolicy
            governancePolicy) throws GovernanceException {

        governancePolicy.setId(APIMGovernanceUtil.generateUUID());
        checkForInvalidActions(governancePolicy);
        addMissingNotifyActions(governancePolicy);

        return policyMgtDAO.createGovernancePolicy(governancePolicy, organization);
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId         Policy ID
     * @param governancePolicy Governance Policy
     * @return GovernancePolicy Updated object
     * @throws GovernanceException If an error occurs while updating the policy
     */

    public GovernancePolicy updateGovernancePolicy(String policyId, GovernancePolicy governancePolicy)
            throws GovernanceException {

        checkForInvalidActions(governancePolicy);
        addMissingNotifyActions(governancePolicy);

        return policyMgtDAO.updateGovernancePolicy(policyId, governancePolicy);
    }

    /**
     * This checks whether any invalid action such as,
     * - Actions assigned to invalid governable states
     * - BLOCK actions are present for API_CREATE and API_UPDATE states
     *
     * @param policy Governance Policy
     * @throws GovernanceException If an error occurs while checking the actions
     */
    private void checkForInvalidActions(GovernancePolicy policy)
            throws GovernanceException {

        List<GovernableState> governableStates = policy.getGovernableStates();
        List<GovernanceAction> actions = policy.getActions();
        for (GovernanceAction action : actions) {
            if (!governableStates.contains(action.getGovernableState())) {
                throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_ASSIGNING_ACTION_TO_POLICY,
                        "Invalid governable state found in the policy. Please update the policy");
            }
            if (GovernanceActionType.BLOCK.equals(action.getType()) &&
                    (GovernableState.API_CREATE.equals(action.getGovernableState()) ||
                            GovernableState.API_UPDATE.equals(action.getGovernableState()))) {
                throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_ASSIGNING_ACTION_TO_POLICY,
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
    private void addMissingNotifyActions(GovernancePolicy policy) {

        List<GovernableState> governableStates = policy.getGovernableStates();
        List<GovernanceAction> actions = policy.getActions();
        for (GovernableState state : governableStates) {
            for (RuleSeverity severity : RuleSeverity.values()) {
                boolean isActionPresent = false;
                for (GovernanceAction action : actions) {
                    if (state.equals(action.getGovernableState()) &&
                            severity.equals(action.getRuleSeverity())) {
                        isActionPresent = true;
                        break;
                    }
                }
                if (!isActionPresent) {
                    GovernanceAction notifyAction = new GovernanceAction();
                    notifyAction.setType(GovernanceActionType.NOTIFY);
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
     * @param policyId Policy ID
     * @throws GovernanceException If an error occurs while deleting the policy
     */

    public void deletePolicy(String policyId) throws GovernanceException {
        policyMgtDAO.deletePolicy(policyId);
    }

    /**
     * Get Governance Policy by Name
     *
     * @param policyID Policy ID
     * @return GovernancePolicy
     * @throws GovernanceException If an error occurs while retrieving the policy
     */

    public GovernancePolicy getGovernancePolicyByID(String policyID)
            throws GovernanceException {
        GovernancePolicy policyInfo = policyMgtDAO.getGovernancePolicyByID(policyID);
        if (policyInfo == null) {
            throw new GovernanceException(GovernanceExceptionCodes.POLICY_NOT_FOUND, policyID);
        }
        return policyInfo;
    }

    /**
     * Get Governance Policies
     *
     * @param organization Organization
     * @return GovernancePolicyList
     * @throws GovernanceException If an error occurs while retrieving the policies
     */

    public GovernancePolicyList getGovernancePolicies(String organization) throws GovernanceException {
        return policyMgtDAO.getGovernancePolicies(organization);
    }

    /**
     * Get the list of rulesets for a given policy
     *
     * @param policyId Policy ID
     * @return List of rulesets
     * @throws GovernanceException If an error occurs while getting the rulesets
     */

    public List<RulesetInfo> getRulesetsByPolicyId(String policyId) throws GovernanceException {
        return policyMgtDAO.getRulesetsByPolicyId(policyId);
    }

    /**
     * Get the list of policies by label
     *
     * @param label        label
     * @param organization organization
     * @return Map of Policy IDs, Policy Names
     * @throws GovernanceException If an error occurs while getting the policies
     */

    public Map<String, String> getPoliciesByLabel(String label, String organization)
            throws GovernanceException {
        return policyMgtDAO.getPoliciesByLabel(label, organization);
    }

    /**
     * Get the list of organization wide policies
     *
     * @param organization organization
     * @return Map of Policy IDs, Policy Names
     * @throws GovernanceException If an error occurs while getting the policies
     */

    public Map<String, String> getOrganizationWidePolicies(String organization) throws GovernanceException {
        return policyMgtDAO.getGlobalPolicies(organization);
    }

    /**
     * Get the list of policies by label and state
     *
     * @param label        Label
     * @param state        Governable State for the policy
     * @param organization Organization
     * @return List of policy IDs
     * @throws GovernanceException If an error occurs while getting the policies
     */

    public List<String> getPoliciesByLabelAndState(String label, GovernableState state, String organization)
            throws GovernanceException {
        return policyMgtDAO.getPoliciesByLabelAndState(label, state, organization);
    }

    /**
     * Get the list of organization wide policies by state
     *
     * @param state        Governable State for the policy
     * @param organization organization
     * @return List of policy IDs
     * @throws GovernanceException If an error occurs while getting the policies
     */

    public List<String> getOrganizationWidePoliciesByState(GovernableState state, String organization)
            throws GovernanceException {
        return policyMgtDAO.getGlobalPoliciesWithState(state, organization);
    }

    /**
     * This method checks whether a blocking action is present for a given governable state of a policy
     *
     * @param policyId Policy ID
     * @param state    Governable State
     * @return true if a blocking action is present, false otherwise
     * @throws GovernanceException If an error occurs while checking the presence of blocking action
     */

    public boolean isBlockingActionPresentForState(String policyId, GovernableState state)
            throws GovernanceException {
        boolean isBlockingActionPresent = false;
        List<GovernanceAction> actions = policyMgtDAO.getActionsByPolicyId(policyId);
        for (GovernanceAction action : actions) {
            if (GovernanceActionType.BLOCK
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
     * @return GovernancePolicyList
     * @throws GovernanceException If an error occurs while searching for policies
     */

    public GovernancePolicyList searchGovernancePolicies(String query, String organization)
            throws GovernanceException {
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
        String[] criteria = query.split(" ");

        for (String criterion : criteria) {
            String[] parts = criterion.split(":");

            if (parts.length == 2) {
                String searchPrefix = parts[0];
                String searchValue = parts[1];

                // Add valid prefixes to criteriaMap
                if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicySearchAttributes.STATE)) {
                    criteriaMap.put(APIMGovernanceConstants.PolicySearchAttributes.STATE, searchValue);
                } else if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicySearchAttributes.NAME)) {
                    criteriaMap.put(APIMGovernanceConstants.PolicySearchAttributes.NAME, searchValue);
                }
            }
        }

        return criteriaMap;
    }

    /**
     * Delete the label policy mappings
     *
     * @param label        Label ID
     * @param organization Organization
     * @throws GovernanceException If an error occurs while deleting the mappings
     */
    public void deleteLabelPolicyMappings(String label, String organization) throws GovernanceException {
        policyMgtDAO.deleteLabelPolicyMappings(label, organization);
    }
}
