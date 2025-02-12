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

package org.wso2.carbon.apimgt.governance.impl.dao;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachment;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachmentList;
import org.wso2.carbon.apimgt.governance.api.model.Policy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the DAO layer for Governance Policy Attachment Management
 */
public interface GovernancePolicyAttachmentMgtDAO {

    /**
     * Create a new Governance Policy Attachment
     *
     * @param governancePolicyAttachment Governance Policy Attachment
     * @param organization     Organization
     * @return APIMGovernancePolicyAttachment Created object
     */
    APIMGovernancePolicyAttachment createGovernancePolicyAttachment(
            APIMGovernancePolicyAttachment governancePolicyAttachment,
            String organization) throws APIMGovernanceException;

    /**
     * Get Governance Policy by Name
     *
     * @param  policyAttachmentName  Policy Attachment Name
     * @param organization Organization
     * @return APIMGovernancePolicyAttachment
     * @throws APIMGovernanceException If an error occurs while retrieving the policy attachment
     */
    APIMGovernancePolicyAttachment getGovernancePolicyAttachmentByName(String policyAttachmentName,
                                                                       String organization)
            throws APIMGovernanceException;

    /**
     * Get Governance Policy Attachment by ID
     *
     * @param policyAttachmentId     Policy Attachment ID
     * @param organization Organization
     * @return APIMGovernancePolicyAttachment
     * @throws APIMGovernanceException If an error occurs while retrieving the policy attachment
     */
    APIMGovernancePolicyAttachment getGovernancePolicyAttachmentByID(String policyAttachmentId, String organization)
            throws APIMGovernanceException;

    /**
     * Get all the Governance Policy Attachments
     *
     * @param organization Organization
     * @return APIMGovernancePolicyAttachmentList object
     * @throws APIMGovernanceException If an error occurs while getting the policy attachments
     */
    APIMGovernancePolicyAttachmentList getGovernancePolicyAttachments(String organization)
            throws APIMGovernanceException;

    /**
     * Update a Governance Policy Attachment
     *
     * @param policyAttachmentId         Policy ID
     * @param governancePolicyAttachment Governance Policy
     * @param organization     Organization
     * @return APIMGovernancePolicy Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy attachment
     */
    APIMGovernancePolicyAttachment updateGovernancePolicyAttachment(
            String policyAttachmentId,
            APIMGovernancePolicyAttachment governancePolicyAttachment, String organization)
            throws APIMGovernanceException;

    /**
     * Get the list of policies for a given policy attachment
     *
     * @param policyAttachmentId Policy Attachment ID
     * @return List of policies
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    List<Policy> getPoliciesWithContentByPolicyAttachmentId(String policyAttachmentId, String organization)
            throws APIMGovernanceException;

    /**
     * Get the list of policies for a given policy attachment
     *
     * @param policyAttachmentId Policy Attachment ID
     * @return List of policies
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    List<PolicyInfo> getPoliciesByPolicyAttachmentId(String policyAttachmentId, String organization)
            throws APIMGovernanceException;

    /**
     * Get the list of policy attachments by label
     *
     * @param label        label
     * @param organization organization
     * @return Map of policy Attachment IDs, policy attachment names
     * @throws APIMGovernanceException If an error occurs while getting the policy attachments
     */
    Map<String, String> getPolicyAttachmentsByLabel(String label, String organization) throws APIMGovernanceException;

    /**
     * Get PolicyAttachmentIds by label
     *
     * @param label        Label
     * @param state        Governable State for the policy attachment
     * @param organization Organization
     * @return List of Policy Attachment IDs
     */
    List<String> getPolicyAttachmentsByLabelAndState(String label, APIMGovernableState state, String organization)
            throws APIMGovernanceException;

    /**
     * Get policy attachments without labels
     *
     * @param organization Organization
     * @return Map of Policy Attachment IDs, and Names
     */
    Map<String, String> getGlobalPolicyAttachments(String organization)
            throws APIMGovernanceException;

    /**
     * Get Policy Attachments without labels by state
     *
     * @param state        Governable State for the policy attachment
     * @param organization Organization
     * @return List of Policy Attachment IDs
     */
    List<String> getGlobalPolicyAttachmentsWithState(APIMGovernableState state, String organization)
            throws APIMGovernanceException;

    /**
     * Get the actions of a policy attachment
     *
     * @param policyAttachmentId Policy Attachment ID
     * @return List of Governance Actions
     * @throws APIMGovernanceException If an error occurs while getting the actions
     */
    List<APIMGovernanceAction> getActionsByPolicyAttachmentId(String policyAttachmentId) throws APIMGovernanceException;

    /**
     * Search for Governance Policy Attachments
     *
     * @param searchCriteria Search criteria
     * @param organization   Organization
     * @return APIMGovernancePolicyAttachmentList object
     * @throws APIMGovernanceException If an error occurs while searching for policy attachments
     */
    APIMGovernancePolicyAttachmentList searchPolicyAttachments(Map<String, String> searchCriteria, String organization)
            throws APIMGovernanceException;

    /**
     * Delete a Governance Policy Attachment
     *
     * @param policyAttachmentId     Policy Attachment ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy attachments
     */
    void deletePolicyAttachments(String policyAttachmentId, String organization) throws APIMGovernanceException;

    /**
     * Delete policy attachments label mappings for a given label
     *
     * @param label        label
     * @param organization organization
     * @throws APIMGovernanceException If an error occurs while deleting the mappings
     */
    void deleteLabelPolicyAttachmentMappings(String label, String organization) throws APIMGovernanceException;
}


