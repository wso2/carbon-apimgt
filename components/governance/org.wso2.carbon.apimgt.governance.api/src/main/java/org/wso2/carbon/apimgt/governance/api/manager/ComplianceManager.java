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

package org.wso2.carbon.apimgt.governance.api.manager;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;

import java.util.Map;

/**
 * This interface represents the Compliance Manager, which is responsible for managing compliance related operations
 */
public interface ComplianceManager {

    /**
     * Get the associated rulesets by policy
     *
     * @param organization Organization Name
     * @return Map of associated rulesets
     * @throws GovernanceException If an error occurs while getting the associated rulesets
     */
    Map<String, Map<String, String>> getAssociatedRulesetsByPolicy(String organization)
            throws GovernanceException;

    /**
     * Assess the compliance of an API
     *
     * @param apiId                       API ID
     * @param organization                Organization Name
     * @param policyToRulesetToContentMap Map of policy to ruleset to content
     * @param authHeader                  Auth header
     */
    void assessAPICompliance(String apiId, String organization, Map<String, Map<String, String>>
            policyToRulesetToContentMap, String authHeader);
}
