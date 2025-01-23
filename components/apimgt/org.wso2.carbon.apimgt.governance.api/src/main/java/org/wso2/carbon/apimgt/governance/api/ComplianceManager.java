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
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Severity;

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
    void handleComplianceEvaluationAsync(String artifactId, ArtifactType artifactType,
                                         List<String> govPolicies, String organization)
            throws GovernanceException;


    /**
     * Get Rule Violations
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    List<RuleViolation> getRuleViolations(String artifactId, String policyId, String rulesetId)
            throws GovernanceException;

    /**
     * Get the rule violations by artifact ID based on severity
     *
     * @param artifactId Artifact ID
     * @return Map of Rule Violations based on severity
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    Map<Severity, List<RuleViolation>> getSeverityBasedRuleViolationsForArtifact(String artifactId)
            throws GovernanceException;

    /**
     * Get Compliance Evaluation Result
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return Compliance Evaluation Result
     * @throws GovernanceException If an error occurs while getting the compliance evaluation result
     */
    ComplianceEvaluationResult getComplianceEvaluationResult(String artifactId, String policyId, String rulesetId)
            throws GovernanceException;

    /**
     * Check whether the artifact evaluation results exist
     *
     * @param artifactId Artifact ID
     * @return True if the artifact evaluation results exist, False otherwise
     * @throws GovernanceException If an error occurs while checking the existence of the artifact evaluation results
     */
    boolean isArtifactEvaluationResultsExist(String artifactId) throws GovernanceException;

    /**
     * Check whether the policy evaluation results exist for a given artifact
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @return True if the policy evaluation results exist, False otherwise
     * @throws GovernanceException If an error occurs while checking the existence of the policy evaluation results
     */
    boolean isPolicyEvaluationResultsExist(String artifactId, String policyId) throws GovernanceException;

    /**
     * Get a map of compliant and non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return Map of compliant and non-compliant artifacts
     * @throws GovernanceException If an error occurs while getting the compliant and non-compliant artifacts
     */
    Map<ArtifactComplianceState, List<String>> getCompliantAndNonCompliantArtifacts(
            ArtifactType artifactType, String organization) throws GovernanceException;
}
