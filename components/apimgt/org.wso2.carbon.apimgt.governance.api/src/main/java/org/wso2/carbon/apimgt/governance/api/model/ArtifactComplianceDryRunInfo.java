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

package org.wso2.carbon.apimgt.governance.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the compliance dry run information of an artifact
 */
public class ArtifactComplianceDryRunInfo {

    private final Map<APIMGovernancePolicy, Map<RulesetInfo, List<RuleViolation>>> violations = new HashMap<>();

    public Map<APIMGovernancePolicy, Map<RulesetInfo, List<RuleViolation>>> getViolations() {
        return Collections.unmodifiableMap(violations);
    }

    public void addRuleViolationsForRuleset(APIMGovernancePolicy policy, RulesetInfo ruleset,
                                            List<RuleViolation> ruleViolations) {
        violations.computeIfAbsent(policy, k -> new HashMap<>()).put(ruleset, ruleViolations);
    }
}
