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

import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachment;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachmentList;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyAttachmentMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Governance Policy Manager
 */
public class PolicyAttachmentManager {

    private final GovernancePolicyAttachmentMgtDAO policyAttachmentMgtDAO;

    public PolicyAttachmentManager() {
        policyAttachmentMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
    }

    /**
     * Create a new Governance Policy
     *
     * @param organization     Organization
     * @param governancePolicyAttachment Governance Policy
     * @return APIMGovernancePolicy Created object
     * @throws APIMGovernanceException If an error occurs while creating the policy
     */

    public APIMGovernancePolicyAttachment createGovernancePolicyAttachment(String organization,
                                                                           APIMGovernancePolicyAttachment
                                                                                   governancePolicyAttachment)
            throws APIMGovernanceException {

        if (policyAttachmentMgtDAO.getGovernancePolicyAttachmentByName(governancePolicyAttachment.getName(),
                organization) != null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_ALREADY_EXISTS,
                    governancePolicyAttachment.getName(), organization);
        }

        governancePolicyAttachment.setId(APIMGovernanceUtil.generateUUID());
        checkForInvalidActions(governancePolicyAttachment);
        addMissingNotifyActions(governancePolicyAttachment);

        return policyAttachmentMgtDAO.createGovernancePolicyAttachment(governancePolicyAttachment, organization);
    }

    /**
     * Update a Governance Policy
     *
     * @param policyAttachmentId         Policy ID
     * @param governancePolicyAttachment Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */

    public APIMGovernancePolicyAttachment updateGovernancePolicyAttachment(String policyAttachmentId,
                                                                           APIMGovernancePolicyAttachment
                                                                                   governancePolicyAttachment,
                                                                           String organization)
            throws APIMGovernanceException {

        if (policyAttachmentMgtDAO.getGovernancePolicyAttachmentByID(policyAttachmentId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyAttachmentId);
        }

        String newName = governancePolicyAttachment.getName();
        APIMGovernancePolicyAttachment policyWithNewName = policyAttachmentMgtDAO
                .getGovernancePolicyAttachmentByName(newName, organization);
        if (policyWithNewName != null && !policyWithNewName.getId().equals(policyAttachmentId)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_ALREADY_EXISTS, newName, organization);
        }

        checkForInvalidActions(governancePolicyAttachment);
        addMissingNotifyActions(governancePolicyAttachment);

        return policyAttachmentMgtDAO.updateGovernancePolicyAttachment(policyAttachmentId, governancePolicyAttachment,
                organization);
    }

    /**
     * This checks whether any invalid action such as,
     * - Actions assigned to invalid governable states
     * - BLOCK actions are present for API_CREATE and API_UPDATE states
     *
     * @param policyAttachment Governance Policy
     * @throws APIMGovernanceException If an error occurs while checking the actions
     */
    private void checkForInvalidActions(APIMGovernancePolicyAttachment policyAttachment)
            throws APIMGovernanceException {

        List<APIMGovernableState> apimGovernableStates = policyAttachment.getGovernableStates();
        List<APIMGovernanceAction> actions = policyAttachment.getActions();
        for (APIMGovernanceAction action : actions) {
            if (!apimGovernableStates.contains(action.getGovernableState())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_ASSIGNING_ACTION_TO_POLICY,
                        "Invalid governable state found in the policyAttachment. Please update the policyAttachment");
            }
            if (APIMGovernanceActionType.BLOCK.equals(action.getType()) &&
                    (APIMGovernableState.API_CREATE.equals(action.getGovernableState()) ||
                            APIMGovernableState.API_UPDATE.equals(action.getGovernableState()))) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_ASSIGNING_ACTION_TO_POLICY,
                        "Creating policies with blocking actions for API" +
                                " create/update is not allowed. Please update the policyAttachment");
            }
        }

    }

    /**
     * This method adds missing notify actions for each governable state
     *
     * @param policyAttachment Governance Policy
     */
    private void addMissingNotifyActions(APIMGovernancePolicyAttachment policyAttachment) {

        List<APIMGovernableState> apimGovernableStates = policyAttachment.getGovernableStates();
        List<APIMGovernanceAction> actions = policyAttachment.getActions();
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
        policyAttachment.setActions(actions);
    }

    /**
     * Delete a Governance Policy
     *
     * @param policyAttachmentId     Policy ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */

    public void deletePolicyAttachment(String policyAttachmentId, String organization) throws APIMGovernanceException {
        if (policyAttachmentMgtDAO.getGovernancePolicyAttachmentByID(policyAttachmentId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyAttachmentId);
        }

        policyAttachmentMgtDAO.deletePolicyAttachments(policyAttachmentId, organization);
    }

    /**
     * Get Governance Policy by Name
     *
     * @param policyAttachmentId     Policy ID
     * @param organization Organization
     * @return APIMGovernancePolicy
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */

    public APIMGovernancePolicyAttachment getGovernancePolicyAttachmentByID(String policyAttachmentId,
                                                                            String organization)
            throws APIMGovernanceException {
        APIMGovernancePolicyAttachment policyAttachmentInfo = policyAttachmentMgtDAO
                .getGovernancePolicyAttachmentByID(policyAttachmentId, organization);
        if (policyAttachmentInfo == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyAttachmentId);
        }
        return policyAttachmentInfo;
    }

    /**
     * Get Governance Policies
     *
     * @param organization Organization
     * @return APIMGovernancePolicyList
     * @throws APIMGovernanceException If an error occurs while retrieving the policies
     */

    public APIMGovernancePolicyAttachmentList getGovernancePolicyAttachments(String organization)
            throws APIMGovernanceException {
        return policyAttachmentMgtDAO.getGovernancePolicyAttachments(organization);
    }

    /**
     * Get the list of policies for a given policy attachment
     *
     * @param policyAttachmentId     Policy ID
     * @param organization Organization
     * @return List of policies
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */

    public List<PolicyInfo> getPoliciesByPolicyAttachmentId(String policyAttachmentId,
                                                            String organization) throws APIMGovernanceException {
        if (policyAttachmentMgtDAO.getGovernancePolicyAttachmentByID(policyAttachmentId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyAttachmentId);
        }
        return policyAttachmentMgtDAO.getPoliciesByPolicyAttachmentId(policyAttachmentId, organization);
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
        return policyAttachmentMgtDAO.getPolicyAttachmentsByLabel(label, organization);
    }

    /**
     * Get the list of organization wide policies
     *
     * @param organization organization
     * @return Map of Policy IDs, Policy Names
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */

    public Map<String, String> getOrganizationWidePolicyAttachments(String organization)
            throws APIMGovernanceException {
        return policyAttachmentMgtDAO.getGlobalPolicyAttachments(organization);
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

    public List<String> getPolicyAttachmentByLabelAndState(String label, APIMGovernableState state, String organization)
            throws APIMGovernanceException {
        return policyAttachmentMgtDAO.getPolicyAttachmentsByLabelAndState(label, state, organization);
    }

    /**
     * Get the list of organization wide policies by state
     *
     * @param state        Governable State for the policy
     * @param organization organization
     * @return List of policy IDs
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */

    public List<String> getOrganizationWidePolicyAttachmentByState(APIMGovernableState state, String organization)
            throws APIMGovernanceException {
        return policyAttachmentMgtDAO.getGlobalPolicyAttachmentsWithState(state, organization);
    }

    /**
     * This method checks whether a blocking action is present for a given governable state of a policy
     *
     * @param policyAttachmentId     Policy ID
     * @param state        Governable State
     * @param organization Organization
     * @return true if a blocking action is present, false otherwise
     * @throws APIMGovernanceException If an error occurs while checking the presence of blocking action
     */

    public boolean isBlockingActionPresentForState(String policyAttachmentId, APIMGovernableState state,
                                                   String organization)
            throws APIMGovernanceException {
        if (policyAttachmentMgtDAO.getGovernancePolicyAttachmentByID(policyAttachmentId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyAttachmentId);
        }
        boolean isBlockingActionPresent = false;
        List<APIMGovernanceAction> actions = policyAttachmentMgtDAO.getActionsByPolicyAttachmentId(policyAttachmentId);
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

    public APIMGovernancePolicyAttachmentList searchGovernancePolicyAttachments(String query, String organization)
            throws APIMGovernanceException {
        Map<String, String> searchCriteria = getPolicyAttachmentSearchCriteria(query);
        return policyAttachmentMgtDAO.searchPolicyAttachments(searchCriteria, organization);
    }


    /**
     * Get the search criteria for the policy search from a query such as
     * `query=name:{name} state={state}`
     *
     * @param query Search query
     * @return Map of search criteria
     */
    private Map<String, String> getPolicyAttachmentSearchCriteria(String query) {
        Map<String, String> criteriaMap = new HashMap<>();
        String[] criteria = query.split(" ");

        for (String criterion : criteria) {
            String[] parts = criterion.split(":");

            if (parts.length == 2) {
                String searchPrefix = parts[0];
                String searchValue = parts[1];

                // Add valid prefixes to criteriaMap
                if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicyAttachmentSearchAttributes.STATE)) {
                    criteriaMap.put(APIMGovernanceConstants.PolicyAttachmentSearchAttributes.STATE, searchValue);
                } else if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicyAttachmentSearchAttributes.NAME)) {
                    criteriaMap.put(APIMGovernanceConstants.PolicyAttachmentSearchAttributes.NAME, searchValue);
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
     * @throws APIMGovernanceException If an error occurs while deleting the mappings
     */
    public void deleteLabelPolicyAttachmentMappings(String label, String organization) throws APIMGovernanceException {
        policyAttachmentMgtDAO.deleteLabelPolicyAttachmentMappings(label, organization);
    }
}
