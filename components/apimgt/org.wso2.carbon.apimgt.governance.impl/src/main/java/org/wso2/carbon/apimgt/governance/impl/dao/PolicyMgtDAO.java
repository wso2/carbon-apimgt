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
import org.wso2.carbon.apimgt.governance.api.model.Policy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyContent;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.PolicyList;
import org.wso2.carbon.apimgt.governance.api.model.Rule;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the Governance Policy DAO
 */
public interface PolicyMgtDAO {

    /**
     * Create a new Governance Policy
     *
     * @param policy       Policy object
     * @param rules        List of rules
     * @param organization Organization
     * @return PolicyInfo Created object
     * @throws APIMGovernanceException If an error occurs while creating the policy
     */
    PolicyInfo createPolicy(Policy policy, List<Rule> rules, String organization) throws APIMGovernanceException;

    /**
     * Update a Governance Policy
     *
     * @param policyId Policy ID
     * @param policy   Policy object
     * @param rules     List of rules
     * @return PolicyInfo Updated object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    PolicyInfo updatePolicy(String policyId, Policy policy, List<Rule> rules, String organization)
            throws APIMGovernanceException;

    /**
     * Delete a Governance Policy
     *
     * @param policyId    Policy ID
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */
    void deletePolicy(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Get all the Governance Policies of the organization
     *
     * @param organization Organization
     * @return PolicyList object
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    PolicyList getPolicies(String organization) throws APIMGovernanceException;

    /**
     * Get a Governance Policy by name
     *
     * @param name         Policy name
     * @param organization Organization
     * @return PolicyInfo object
     * @throws APIMGovernanceException If an error occurs while getting the policy
     */
    PolicyInfo getPolicyByName(String name, String organization) throws APIMGovernanceException;

    /**
     * Get a Governance Policy by ID
     *
     * @param policyId    Policy ID
     * @param organization Organization
     * @return PolicyInfo object
     * @throws APIMGovernanceException If an error occurs while getting the policy
     */
    PolicyInfo getPolicyById(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Search for Governance Policies based on the search criteria
     *
     * @param searchCriteria Search attributes
     * @param organization   Organization
     * @return PolicyList object
     * @throws APIMGovernanceException If an error occurs while searching for policies
     */
    PolicyList searchPolicies(Map<String, String> searchCriteria, String organization)
            throws APIMGovernanceException;

    /**
     * Get the content of a Governance Policy
     *
     * @param policyId    Policy ID
     * @param organization Organization
     * @return Content of the policy
     * @throws APIMGovernanceException If an error occurs while getting the policy content
     */
    PolicyContent getPolicyContent(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Get the associated policy attachments for a policy
     *
     * @param policyId    Policy ID
     * @param organization Organization
     * @return List of associated policies
     */
    List<String> getAssociatedPolicyAttachmentForPolicy(String policyId, String organization) throws APIMGovernanceException;

    /**
     * Get the rules of a Policy
     *
     * @param policyId    Policy ID
     * @param organization Organization
     * @return List of rules
     */
    List<Rule> getPolicyByPolicyId(String policyId, String organization) throws APIMGovernanceException;
}
