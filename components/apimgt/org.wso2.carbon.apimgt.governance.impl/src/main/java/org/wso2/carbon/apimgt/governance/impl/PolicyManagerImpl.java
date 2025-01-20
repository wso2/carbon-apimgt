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

import org.wso2.carbon.apimgt.governance.api.PolicyManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.util.List;

/**
 * This class represents the Governance Policy Manager Implementation
 */
public class PolicyManagerImpl implements PolicyManager {

    private GovernancePolicyMgtDAO policyMgtDAO;

    public PolicyManagerImpl() {
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
    @Override
    public GovernancePolicy createGovernancePolicy(String organization, GovernancePolicy
            governancePolicy) throws GovernanceException {
        governancePolicy.setId(GovernanceUtil.generateUUID());
        return policyMgtDAO.createGovernancePolicy(organization, governancePolicy);
    }

    /**
     * Get Governance Policy by Name
     *
     * @param organization Organization
     * @param policyID     Policy ID
     * @return GovernancePolicy
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    @Override
    public GovernancePolicy getGovernancePolicyByID(String organization, String policyID) throws GovernanceException {
        GovernancePolicy policyInfo = policyMgtDAO.getGovernancePolicyByID(organization, policyID);
        if (policyInfo == null) {
            throw new GovernanceException(GovernanceExceptionCodes.POLICY_NOT_FOUND, policyID, organization);
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
    @Override
    public GovernancePolicyList getGovernancePolicies(String organization) throws GovernanceException {
        return policyMgtDAO.getGovernancePolicies(organization);
    }

    /**
     * Delete a Governance Policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @throws GovernanceException If an error occurs while deleting the policy
     */
    @Override
    public void deletePolicy(String policyId, String organization) throws GovernanceException {
        policyMgtDAO.deletePolicy(policyId, organization);
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId                           Policy ID
     * @param organization                       Organization
     * @param governancePolicy Governance Policy
     * @return GovernancePolicy Updated object
     * @throws GovernanceException If an error occurs while updating the policy
     */
    @Override
    public GovernancePolicy updateGovernancePolicy(String policyId, String organization,
                                                   GovernancePolicy governancePolicy)
            throws GovernanceException {
        return policyMgtDAO.updateGovernancePolicy(policyId, organization, governancePolicy);
    }

    /**
     * Get the list of rulesets for a given policy
     *
     * @param policyId Policy ID
     * @return List of rulesets
     * @throws GovernanceException If an error occurs while getting the rulesets
     */
    @Override
    public List<Ruleset> getRulesetsByPolicyId(String policyId) throws GovernanceException {
        return policyMgtDAO.getRulesetsByPolicyId(policyId);
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
    @Override
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
    @Override
    public List<String> getOrganizationWidePoliciesByState(GovernableState state, String organization)
            throws GovernanceException {
        return policyMgtDAO.getPoliciesWithoutLabelsByState(state, organization);
    }

    /**
     * This method checks whether a blocking action is present for a given governable state of a policy
     *
     * @param policyId Policy ID
     * @param state    Governable State
     * @return true if a blocking action is present, false otherwise
     * @throws GovernanceException If an error occurs while checking the presence of blocking action
     */
    @Override
    public boolean isBlockingActionPresentForState(String policyId, GovernableState state) throws GovernanceException {
        boolean isBlockingActionPresent = false;
        List<GovernanceAction> actions = policyMgtDAO.getActionsByPolicyId(policyId);
        for (GovernanceAction action : actions) {
            if (GovernanceActionType.BLOCK
                    .equals(action.getType()) &&
                    String.valueOf(action.getGovernableState()).equals(state)) {
                isBlockingActionPresent = true;
                break;
            }
        }
        return isBlockingActionPresent;
    }


}
