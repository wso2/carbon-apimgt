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

package org.wso2.carbon.apimgt.governance.api;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Policy;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;

import java.util.List;


/**
 * This class represents a Validation Engine. This can be extended to implement a specific validation engine like
 * spectral
 */
public interface ValidationEngine {

    /**
     * Check if a gov policy is valid
     *
     * @param policy Governance Policy
     * @throws APIMGovernanceException If an error occurs while validating the governance policy
     */
    void validatePolicyContent(Policy policy) throws APIMGovernanceException;

    /**
     * Extract rules from a governance policy
     *
     * @param policy Governance Policy
     * @return List of rules
     * @throws APIMGovernanceException If an error occurs while extracting rules
     */
    List<Rule> extractRulesFromPolicy(Policy policy) throws APIMGovernanceException;

    /**
     * Validate a target against a governance policy
     *
     * @param target Target to be validated
     * @param policy Governance Policy
     * @return List of rule violations
     * @throws APIMGovernanceException If an error occurs while validating the target
     */
    List<RuleViolation> validate(String target, Policy policy) throws APIMGovernanceException;
}
