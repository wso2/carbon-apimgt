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

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationRequest;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationStatus;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the DAO class related assessing compliance
 */
public interface ComplianceMgtDAO {

    /**
     * Add an artifact compliance evaluation request
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param organization Organization
     * @throws GovernanceException If an error occurs while adding the artifact
     *                             compliance evaluation request event
     */
    void addComplianceEvaluationRequest(String artifactId, ArtifactType artifactType,
                                        String policyId,
                                        String organization) throws GovernanceException;

    /**
     * Get pending evaluation requests
     *
     * @return List of pending evaluation requests
     * @throws GovernanceException If an error occurs while getting the pending evaluation requests
     */
    List<ComplianceEvaluationRequest> getPendingComplianceEvaluationRequests() throws GovernanceException;

    /**
     * Get the processing compliance evaluation request by artifact ID and policy ID, if any
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @return ComplianceEvaluationRequest
     * @throws GovernanceException If an error occurs while getting the processing compliance evaluation request
     */
    ComplianceEvaluationRequest getProcessingComplianceEvaluationRequest(String artifactId, String policyId)
            throws GovernanceException;

    /**
     * Update the evaluation status of a request
     *
     * @param requestId Evaluation request ID
     * @param status    Evaluation status
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    void updateComplianceEvaluationStatus(String requestId, ComplianceEvaluationStatus status) throws
            GovernanceException;

    /**
     * Update the evaluation status of all processing requests to pending
     *
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    void updateProcessingRequestToPending() throws GovernanceException;

    /**
     * Delete an evaluation request
     *
     * @param requestId Evaluation request ID
     * @throws GovernanceException If an error occurs while deleting the evaluation request
     */
    void deleteComplianceEvaluationRequest(String requestId) throws GovernanceException;

    /**
     * Delete an evaluation request by artifact ID
     *
     * @param artifactId Artifact ID
     * @throws GovernanceException If an error occurs while deleting the evaluation request
     */
    void deleteComplianceEvaluationRequestByArtifactId(String artifactId) throws GovernanceException;

    /**
     * Add a compliance evaluation result to DB
     *
     * @param result         Evaluation result
     * @param ruleViolations List of rule violations from policy evaluation
     * @throws GovernanceException If an error occurs while adding the compliance evaluation result
     */
    void addComplianceEvaluationResult(ComplianceEvaluationResult result, List<RuleViolation> ruleViolations)
            throws GovernanceException;

    /**
     * Get the artifact info
     *
     * @param artifactId Artifact ID
     * @return ArtifactInfo
     * @throws GovernanceException If an error occurs while getting the artifact info
     */
    ArtifactInfo getArtifactInfo(String artifactId) throws GovernanceException;

    /**
     * Get the rule violations
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return List of rule violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    List<RuleViolation> getRuleViolations(String artifactId, String policyId, String rulesetId)
            throws GovernanceException;

    /**
     * Get the rule violations
     *
     * @param artifactId Artifact ID
     * @param rulesetId  Ruleset ID
     * @return List of rule violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    List<RuleViolation> getRuleViolations(String artifactId, String rulesetId)
            throws GovernanceException;

    /**
     * Get the rule violations by artifact ID
     *
     * @param artifactId Artifact ID
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    List<RuleViolation> getRuleViolationsByArtifactId(String artifactId) throws GovernanceException;


    /**
     * Get the compliance evaluation result
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return ComplianceEvaluationResult
     * @throws GovernanceException If an error occurs while getting the compliance evaluation result
     */
    ComplianceEvaluationResult getComplianceEvaluationResult(String artifactId, String policyId,
                                                             String rulesetId) throws GovernanceException;

    /**
     * Get compliance evaluation results by artifact ID
     *
     * @param artifactId Artifact ID
     * @return List of ComplianceEvaluationResult
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    List<ComplianceEvaluationResult> getComplianceEvaluationResultsByArtifactId(String artifactId)
            throws GovernanceException;

    /**
     * Get compliance evaluation results by artifact ID and ruleset ID
     *
     * @param artifactId Artifact ID
     * @param rulesetId  Ruleset ID
     * @return List of ComplianceEvaluationResult
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    List<ComplianceEvaluationResult> getComplianceEvaluationResultsByArtifactIdAndRulesetId(String artifactId,
                                                                                            String rulesetId)
            throws GovernanceException;

    /**
     * Get compliance evaluation results by artifact ID and policy ID
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @return List of ComplianceEvaluationResult
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    List<ComplianceEvaluationResult> getComplianceEvaluationResultsByArtifactAndPolicyId(
            String artifactId, String policyId) throws GovernanceException;

    /**
     * Get list of all compliance evaluated artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of all compliance evaluated artifacts
     * @throws GovernanceException If an error occurs while getting the list of all compliance evaluated artifacts
     */
    List<String> getAllComplianceEvaluatedArtifacts(ArtifactType artifactType,
                                                    String organization) throws GovernanceException;

    /**
     * Get list of non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of non-compliant artifacts
     * @throws GovernanceException If an error occurs while getting the list of non-compliant artifacts
     */
    List<String> getNonCompliantArtifacts(ArtifactType artifactType,
                                          String organization) throws GovernanceException;

    /**
     * Get list of all compliance evaluated policies
     *
     * @param organization Organization
     * @return List of all compliance evaluated policies
     * @throws GovernanceException If an error occurs while getting the list of all compliance evaluated policies
     */
    List<String> getAllComplianceEvaluatedPolicies(String organization) throws GovernanceException;

    /**
     * Get list of violated policies
     *
     * @param organization Organization
     * @return List of violated policies
     * @throws GovernanceException If an error occurs while getting the list of violated policies
     */
    List<String> getViolatedPolicies(String organization) throws GovernanceException;

    /**
     * Get compliance evaluation results for a given policy as a map of artifact type to list of
     * compliance evaluation results
     *
     * @param policyId Policy ID
     * @return Map of compliance evaluation results
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    Map<ArtifactType, List<ComplianceEvaluationResult>> getEvaluationResultsForPolicy(String policyId)
            throws GovernanceException;

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactId Artifact ID
     * @throws GovernanceException If an error occurs while deleting the governance data
     */
    void deleteArtifact(String artifactId) throws GovernanceException;

}
