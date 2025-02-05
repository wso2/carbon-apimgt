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
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param policyIds     List of Policy IDs
     * @param organization  Organization
     * @throws GovernanceException If an error occurs while adding the artifact
     *                             compliance evaluation request event
     */
    void addComplianceEvalRequest(String artifactRefId, ArtifactType artifactType,
                                  List<String> policyIds, String organization)
            throws GovernanceException;

    /**
     * Get pending evaluation requests
     *
     * @return List of pending evaluation requests
     * @throws GovernanceException If an error occurs while getting the pending evaluation requests
     */
    List<ComplianceEvaluationRequest> getPendingComplianceEvalRequests() throws GovernanceException;

    /**
     * Add an artifact compliance evaluation request event
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Request ID
     * @throws GovernanceException If an error occurs while adding the artifact compliance evaluation
     *                             request
     */
    String getPendingEvalRequest(String artifactRefId, ArtifactType artifactType, String organization)
            throws GovernanceException;


    /**
     * Update the evaluation status of a pending request to processing
     *
     * @param requestId Request ID
     * @return True if the request is updated successfully
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    boolean updatePendingRequestToProcessing(String requestId) throws GovernanceException;

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
    void deleteComplianceEvalRequest(String requestId) throws GovernanceException;

    /**
     * Delete evaluation requests for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @throws GovernanceException If an error occurs while deleting the evaluation request
     */
    void deleteComplianceEvalReqsForArtifact(String artifactRefId, ArtifactType artifactType,
                                             String organization) throws GovernanceException;


    /**
     * Add compliance evaluation results for an artifact and policy
     *
     * @param artifactRefId        Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType         Artifact Type
     * @param policyId             Policy ID
     * @param rulesetViolationsMap Map of Rulesets to Rule Violations
     * @param organization         Organization
     * @throws GovernanceException If an error occurs while adding the compliance evaluation results
     */
    void addComplianceEvalResults(String artifactRefId, ArtifactType artifactType, String policyId,
                                  Map<String, List<RuleViolation>> rulesetViolationsMap, String organization)
            throws GovernanceException;

    /**
     * Get the rule violations
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return List of rule violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    List<RuleViolation> getRuleViolations(String artifactRefId, ArtifactType artifactType, String rulesetId,
                                          String organization)
            throws GovernanceException;

    /**
     * Get the rule violations for an artifact
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    List<RuleViolation> getRuleViolationsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                     String organization) throws GovernanceException;


    /**
     * Get policy evaluations for an artifact
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of evaluated policies
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    List<String> getEvaluatedPoliciesForArtifact(String artifactRefId, ArtifactType artifactType,
                                                 String organization) throws GovernanceException;

    /**
     * Get ruleset runs for an artifact
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of evaluated rulesets
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    List<String> getEvaluatedRulesetsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                 String organization)
            throws GovernanceException;

    /**
     * Check if a ruleset is evaluated for an artifact
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     * @return True if the ruleset is evaluated for the artifact
     * @throws GovernanceException If an error occurs while getting the compliance evaluation results
     */
    boolean isRulesetEvaluatedForArtifact(String artifactRefId,
                                          ArtifactType artifactType, String rulesetId, String organization)
            throws GovernanceException;

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
     * Get list of all violated rulesets
     *
     * @param organization Organization
     * @return List of all violated rulesets
     * @throws GovernanceException If an error occurs while getting the list of all violated rulesets
     */
    List<String> getViolatedRulesets(String organization) throws GovernanceException;

    /**
     * Get list of all violated rulesets for an artifact
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of all violated rulesets for an artifact
     * @throws GovernanceException If an error occurs while getting the list of all
     *                             violated rulesets for an artifact
     */
    List<String> getViolatedRulesetsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                String organization)
            throws GovernanceException;

    /**
     * Get list of all evaluated artifacts for a policy
     *
     * @param policyId Policy ID
     * @return List of all evaluated artifacts for a policy
     * @throws GovernanceException If an error occurs while getting the list of all
     *                             evaluated artifacts for a policy
     */
    List<ArtifactInfo> getEvaluatedArtifactsForPolicy(String policyId)
            throws GovernanceException;

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @param organization Organization
     * @throws GovernanceException If an error occurs while deleting the governance data
     */
    void deleteArtifact(String artifactRefId, ArtifactType artifactType,
                        String organization) throws GovernanceException;

}
