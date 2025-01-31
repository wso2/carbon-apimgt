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
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceDryRunInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.PolicyAdherenceSate;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
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
     * Handle API Compliance Evaluation Request Sync
     *
     * @param artifactId             Artifact ID
     * @param revisionNo             Revision number
     * @param artifactType           Artifact Type
     * @param govPolicies            List of governance policies to be evaluated
     * @param state                  State at which artifact should be governed
     * @param organization           Organization
     * @param artifactProjectContent Map of artifact content
     * @return ArtifactComplianceInfo object
     * @throws GovernanceException If an error occurs while handling the API compliance evaluation
     */
    ArtifactComplianceInfo handleComplianceEvaluationSync(String artifactId,
                                                          String revisionNo,
                                                          ArtifactType artifactType,
                                                          List<String> govPolicies,
                                                          Map<RuleType, String> artifactProjectContent,
                                                          GovernableState state,
                                                          String organization)
            throws GovernanceException;

    /**
     * Handle API Compliance Evaluation Request Dry Run
     *
     * @param artifactType           Artifact Type
     * @param govPolicies            List of governance policies to be evaluated
     * @param artifactProjectContent Map of artifact content
     * @param organization           Organization
     * @return ArtifactComplianceDryRunInfo object
     * @throws GovernanceException If an error occurs while handling the API compliance evaluation
     */
    ArtifactComplianceDryRunInfo handleComplianceEvaluationDryRun(ArtifactType artifactType,
                                                                  List<String> govPolicies,
                                                                  Map<RuleType, String> artifactProjectContent,
                                                                  String organization)
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
     * Get Rule Violations
     *
     * @param artifactId Artifact ID
     * @param rulesetId  Ruleset ID
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    List<RuleViolation> getRuleViolations(String artifactId, String rulesetId) throws GovernanceException;

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
     * Get list of evaluated policies by artifact ID
     *
     * @param artifactId Artifact ID
     * @return List of evaluated policies IDs
     * @throws GovernanceException If an error occurs while getting the list of evaluated policies
     */
    List<String> getEvaluatedPoliciesByArtifactId(String artifactId) throws GovernanceException;

    /**
     * Get list of evaluated rulesets by artifact ID and policy ID
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @return List of evaluated rulesets IDs
     * @throws GovernanceException If an error occurs while getting the list of evaluated rulesets
     */
    List<String> getEvaluatedRulesetsByArtifactIdAndPolicyId(String artifactId, String policyId)
            throws GovernanceException;

    /**
     * Get a map of compliant and non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return Map of compliant and non-compliant artifacts
     * @throws GovernanceException If an error occurs while getting the compliant and non-compliant artifacts
     */
    Map<ArtifactComplianceState, List<String>> getComplianceStateOfEvaluatedArtifacts(
            ArtifactType artifactType, String organization) throws GovernanceException;

    /**
     * Get a map of policies followed and violated in the organization
     *
     * @param organization Organization
     * @return Map of policies followed and violated
     * @throws GovernanceException If an error occurs while getting the policy adherence
     */
    Map<PolicyAdherenceSate, List<String>> getAdherenceStateofEvaluatedPolicies(String organization)
            throws GovernanceException;

    /**
     * Get a map of artifacts evaluated by policy
     *
     * @param policyId            Policy ID
     * @param resolveArtifactName Whether the artifact name should be resolved
     * @return Map of artifacts evaluated by policy
     * @throws GovernanceException If an error occurs while getting the artifacts evaluated by policy
     */
    Map<ArtifactComplianceState, List<ArtifactInfo>>
    getComplianceStateOfEvaluatedArtifactsByPolicy(String policyId,
                                                   boolean resolveArtifactName)
            throws GovernanceException;


    /**
     * Is Ruleset Evaluated for Artifact
     *
     * @param artifactId Artifact ID
     * @param rulesetId  Ruleset ID
     * @return Whether the ruleset is evaluated for the artifact
     * @throws GovernanceException If an error occurs while checking whether the ruleset is evaluated for the artifact
     */
    boolean isRulesetEvaluatedForArtifact(String artifactId, String rulesetId) throws GovernanceException;

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactId Artifact ID
     * @throws GovernanceException If an error occurs while deleting the governance data
     */
    void deleteArtifact(String artifactId) throws GovernanceException;
}
