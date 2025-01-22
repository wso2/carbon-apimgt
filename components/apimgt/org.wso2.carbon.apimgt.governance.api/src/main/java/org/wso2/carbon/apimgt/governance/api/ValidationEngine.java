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

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;

import java.util.List;

/**
 * This class represents a Validation Engine. This can be extended to implement a specific validation engine like
 * spectral
 */
public interface ValidationEngine {

    /**
     * Check if a ruleset is valid
     *
     * @param ruleset Ruleset
     * @return True if the ruleset is valid, False otherwise
     * @throws GovernanceException If an error occurs while validating the ruleset
     */
    boolean isRulesetValid(Ruleset ruleset) throws GovernanceException;

    /**
     * Extract rules from a ruleset
     *
     * @param ruleset Ruleset
     * @return List of rules
     * @throws GovernanceException If an error occurs while extracting rules
     */
    List<Rule> extractRulesFromRuleset(Ruleset ruleset) throws GovernanceException;

    /**
     * Validate a target against a ruleset
     *
     * @param target  Target to be validated
     * @param ruleset Ruleset
     * @return List of rule violations
     * @throws GovernanceException If an error occurs while validating the target
     */
    List<RuleViolation> validate(String target, Ruleset ruleset) throws GovernanceException;
}
