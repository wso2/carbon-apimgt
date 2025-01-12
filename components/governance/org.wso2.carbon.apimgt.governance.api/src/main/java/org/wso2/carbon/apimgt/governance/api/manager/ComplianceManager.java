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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * This interface represents the Compliance Manager, which is responsible for managing compliance related operations
 */
public interface ComplianceManager {

    /**
     * Handle Policy Change Event
     *
     * @param policyId     Policy ID
     * @param organization Organization
     */
    void handlePolicyChangeEvent(String policyId, String organization) throws GovernanceException;


    /**
     * Handle Ruleset Change Event
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     */
    void handleRulesetChangeEvent(String rulesetId, String organization) throws GovernanceException;


    /**
     * Handle API Compliance Evaluation Request Async
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param govPolicies  List of governance policies to be evaluated
     * @param organization Organization
     * @throws GovernanceException If an error occurs while handling the API compliance evaluation
     */
    void handleAPIComplianceEvaluationAsync(String artifactId, String artifactType,
                                            List<String> govPolicies, String organization)
            throws GovernanceException;


}
