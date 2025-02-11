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
import org.wso2.carbon.apimgt.governance.api.model.Policy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyContent;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.PolicyList;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.impl.dao.PolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.PolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the Policy Manager.
 */
public class PolicyManager {

    private PolicyMgtDAO policyMgtDAO;

    public PolicyManager() {
        policyMgtDAO = PolicyMgtDAOImpl.getInstance();
    }

    /**
     * Create a new Governance Policy
     *
     * @param policy       Policy object
     * @param organization Organization
     * @return Policy Created object
     */

    public PolicyInfo createNewPolicy(Policy policy, String organization) throws APIMGovernanceException {

        if (policyMgtDAO.getPolicyByName(policy.getName(), organization) != null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_ALREADY_EXIST, policy.getName(),
                    organization);
        }
        policy.setId(APIMGovernanceUtil.generateUUID());

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance().
                getValidationEngineService().getValidationEngine();

        validationEngine.validatePolicyContent(policy);
        List<Rule> rules = validationEngine.extractRulesFromPolicy(policy);

        if (rules.isEmpty()) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_POLICY_CONTENT,
                    policy.getName());
        }

        return policyMgtDAO.createPolicy(policy, rules, organization);
    }

    /**
     * Delete a Governance Policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */

    public void deletePolicy(String policyId, String organization) throws APIMGovernanceException {
        PolicyInfo policy = policyMgtDAO.getPolicyById(policyId, organization);
        if (policy == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        } else if (isPolicyLinkedWithAttachments(policyId, organization)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_POLICY_ASSOCIATED_WITH_POLICY_ATTACHMENTS,
                    policy.getId());
        }
        policyMgtDAO.deletePolicy(policyId, organization);
    }

    /**
     * Check if a policy is associated with any policies
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return boolean True if the policy is associated with policies
     */
    private boolean isPolicyLinkedWithAttachments(String policyId, String organization)
            throws APIMGovernanceException {
        List<String> policyIds = policyMgtDAO.getAssociatedPolicyAttachmentForPolicy(policyId, organization);
        return !policyIds.isEmpty();
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId     Policy ID
     * @param policy       Policy object
     * @param organization Organization
     * @return PolicyInfo Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */

    public PolicyInfo updatePolicy(String policyId, Policy policy, String organization)
            throws APIMGovernanceException {

        PolicyInfo existingPolicy = policyMgtDAO.getPolicyById(policyId, organization);
        if (existingPolicy == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }

        String newName = policy.getName();
        PolicyInfo existingPolicyByName = policyMgtDAO.getPolicyByName(newName, organization);
        if (existingPolicyByName != null && !existingPolicyByName.getId().equals(policyId)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_ALREADY_EXIST, newName, organization);
        }

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance().
                getValidationEngineService().getValidationEngine();

        validationEngine.validatePolicyContent(policy);
        List<Rule> rules = validationEngine.extractRulesFromPolicy(policy);

        if (rules.isEmpty()) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_POLICY_CONTENT,
                    policy.getName());
        }

        return policyMgtDAO.updatePolicy(policyId, policy, rules, organization);
    }

    /**
     * Get all the Governance Policies
     *
     * @param organization Organization
     * @return PolicyList object
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */

    public PolicyList getPolicies(String organization) throws APIMGovernanceException {
        return policyMgtDAO.getPolicies(organization);
    }

    /**
     * Get a Governance Policy by ID
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return PolicyInfo object
     * @throws APIMGovernanceException If an error occurs while getting the policy
     */

    public PolicyInfo getPolicyById(String policyId, String organization) throws APIMGovernanceException {
        PolicyInfo policy = policyMgtDAO.getPolicyById(policyId, organization);
        if (policy == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }
        return policy;
    }

    /**
     * Get the content of a Governance Policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return Content of the policy
     * @throws APIMGovernanceException If an error occurs while getting the policy content
     */

    public PolicyContent getPolicyContent(String policyId, String organization) throws APIMGovernanceException {
        PolicyContent content = policyMgtDAO.getPolicyContent(policyId, organization);
        if (content == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }
        return content;

    }

    /**
     * Get the policies using the Governance Policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return List of policies using the policy
     * @throws APIMGovernanceException If an error occurs while getting the policy usage
     */

    public List<String> getPolicyUsage(String policyId, String organization) throws APIMGovernanceException {
        PolicyInfo policy = policyMgtDAO.getPolicyById(policyId, organization);
        if (policy == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }
        return policyMgtDAO.getAssociatedPolicyAttachmentForPolicy(policyId, organization);
    }

    /**
     * Get the rules using the Governance Policy
     *
     * @param policyId     Policy ID
     * @param organization Organization
     * @return List of rules using the policy
     * @throws APIMGovernanceException If an error occurs while getting the policy usage
     */

    public List<Rule> getRulesByPolicyId(String policyId, String organization) throws APIMGovernanceException {
        if (policyMgtDAO.getPolicyById(policyId, organization) == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, policyId);
        }
        return policyMgtDAO.getPolicyByPolicyId(policyId, organization);
    }

    /**
     * Search for Governance Policies
     *
     * @param query        Search query
     * @param organization Organization
     * @return PolicyList object
     * @throws APIMGovernanceException If an error occurs while searching for policies
     */

    public PolicyList searchPolicies(String query, String organization) throws APIMGovernanceException {
        Map<String, String> searchCriteria = getPolicySearchCriteria(query);
        return policyMgtDAO.searchPolicies(searchCriteria, organization);

    }

    /**
     * Get the search criteria for the policy search from a query such as
     * `query=name:{name} ruleType:{type} artifactType:{type}`
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
                if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicySearchAttributes.ARTIFACT_TYPE)) {
                    criteriaMap.put(APIMGovernanceConstants.PolicySearchAttributes.ARTIFACT_TYPE, searchValue);
                } else if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicySearchAttributes.POLICY_TYPE)) {
                    criteriaMap.put(APIMGovernanceConstants.PolicySearchAttributes.POLICY_TYPE, searchValue);
                } else if (searchPrefix.equalsIgnoreCase(APIMGovernanceConstants.PolicySearchAttributes.NAME)) {
                    criteriaMap.put(APIMGovernanceConstants.PolicySearchAttributes.NAME, searchValue);
                }
            }
        }

        return criteriaMap;
    }

}
