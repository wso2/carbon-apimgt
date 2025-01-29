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

package org.wso2.carbon.apimgt.governance.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyAdherenceSate;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.Severity;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the Compliance Manager, which is responsible for managing compliance related operations
 */
public class ComplianceManagerImpl implements ComplianceManager {
    private static final Log log = LogFactory.getLog(ComplianceManagerImpl.class);

    private final ComplianceMgtDAO complianceMgtDAO;
    private final GovernancePolicyMgtDAO policyMgtDAO;

    private final RulesetMgtDAO rulesetMgtDAO;

    public ComplianceManagerImpl() {
        complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
        policyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
        rulesetMgtDAO = RulesetMgtDAOImpl.getInstance();
    }


    /**
     * Handle Policy Change Event
     *
     * @param policyId     Policy ID
     * @param organization Organization
     */
    @Override
    public void handlePolicyChangeEvent(String policyId, String organization) throws GovernanceException {

        // Get the policy and its labels and associated governable states

        GovernancePolicy policy = policyMgtDAO.getGovernancePolicyByID(policyId);

        List<String> labels = policy.getLabels();
        List<GovernableState> governableStates = policy.getGovernableStates();

        // Get artifacts that should be governed by the policy
        List<ArtifactInfo> artifacts = new ArrayList<>();

        if (labels != null && !labels.isEmpty()) {
            artifacts.addAll(getArtifactsByLabelsAndGovernableStates(labels, governableStates));
        } else {
            // If labels are not defined, the policy is an organization level policy
            artifacts.addAll(getArtifactsByGovernableStates(governableStates, organization));
        }

        for (ArtifactInfo artifact : artifacts) {
            String artifactId = artifact.getArtifactId();
            ArtifactType artifactType = artifact.getArtifactType();
            complianceMgtDAO.addComplianceEvaluationRequest(artifactId, artifactType,
                    policyId, organization);
        }
    }

    /**
     * Get Artifacts by Governable States
     *
     * @param governableStates List of governable states
     * @param organization     Organization
     * @return List of unique artifact information
     */
    private List<ArtifactInfo> getArtifactsByGovernableStates(List<GovernableState> governableStates,
                                                              String organization) throws GovernanceException {
        Map<ArtifactType, List<String>> artifactsMap = GovernanceUtil.getAllArtifactsMap(organization);
        return filterAndCollectArtifacts(artifactsMap, governableStates);
    }

    /**
     * Get Artifacts by Labels and Governable State
     *
     * @param labels           List of labels
     * @param governableStates List of governable states
     * @return List of unique artifact information
     */
    private List<ArtifactInfo> getArtifactsByLabelsAndGovernableStates(List<String> labels,
                                                                       List<GovernableState> governableStates)
            throws GovernanceException {
        List<ArtifactInfo> artifactInfoList = new ArrayList<>();
        Set<String> artifactIds = new HashSet<>(); // Track unique artifact IDs

        // Get Artifacts for each label and merge results
        for (String label : labels) {
            Map<ArtifactType, List<String>> artifactsMap = GovernanceUtil.getArtifactsForLabel(label);

            // Collect artifacts by filtering based on governable states
            List<ArtifactInfo> filteredArtifacts = filterAndCollectArtifacts(artifactsMap, governableStates);

            // Add only unique artifacts based on artifactId
            for (ArtifactInfo artifactInfo : filteredArtifacts) {
                if (artifactIds.add(artifactInfo.getArtifactId())) {
                    artifactInfoList.add(artifactInfo);
                }
            }
        }

        return artifactInfoList;
    }

    /**
     * Filter and collect artifacts based on governable states
     *
     * @param artifactsMap     Map of artifact type to list of artifact IDs
     * @param governableStates List of governable states
     * @return List of unique artifact information
     */
    private List<ArtifactInfo> filterAndCollectArtifacts(Map<ArtifactType, List<String>> artifactsMap,
                                                         List<GovernableState> governableStates)
            throws GovernanceException {
        List<ArtifactInfo> artifactInfoList = new ArrayList<>();

        for (ArtifactType artifactType : artifactsMap.keySet()) {
            List<String> artifactIds = artifactsMap.get(artifactType);

            if (ArtifactType.isArtifactAPI(artifactType)) {
                for (String artifactId : artifactIds) {
                    String apiStatus = APIMUtil.getAPIStatus(artifactId);
                    boolean isDeployed = APIMUtil.isAPIDeployed(artifactId);
                    boolean isAPIGovernable = APIMUtil.isAPIGovernable(apiStatus, isDeployed, governableStates);
                    // If the API should be governed by the policy
                    if (isAPIGovernable) {
                        ArtifactInfo artifactInfo = new ArtifactInfo();
                        artifactInfo.setArtifactId(artifactId);
                        artifactInfo.setArtifactType(artifactType);
                        artifactInfoList.add(artifactInfo);
                    }
                }
            }
        }

        return artifactInfoList;
    }

    /**
     * Handle Ruleset Change Event
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     */
    @Override
    public void handleRulesetChangeEvent(String rulesetId, String organization) throws GovernanceException {
        List<String> policies = rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId);

        for (String policyId : policies) {
            handlePolicyChangeEvent(policyId, organization);
        }
    }

    /**
     * Handle API Compliance Evaluation Request Async
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param govPolicies  List of governance policies to be evaluated
     * @param organization Organization
     * @throws GovernanceException If an error occurs while handling the API compliance evaluation
     */
    @Override
    public void handleComplianceEvaluationAsync(String artifactId, ArtifactType artifactType,
                                                List<String> govPolicies,
                                                String organization) throws GovernanceException {

        for (String policyId : govPolicies) {
            complianceMgtDAO.addComplianceEvaluationRequest(artifactId, artifactType,
                    policyId, organization);
        }

    }

    /**
     * Get Rule Violations
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolations(String artifactId, String policyId, String rulesetId)
            throws GovernanceException {
        return complianceMgtDAO.getRuleViolations(artifactId, policyId, rulesetId);
    }

    /**
     * Get Rule Violations
     *
     * @param artifactId Artifact ID
     * @param rulesetId  Ruleset ID
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolations(String artifactId, String rulesetId) throws GovernanceException {
        return complianceMgtDAO.getRuleViolations(artifactId, rulesetId);
    }

    /**
     * Get the rule violations by artifact ID based on severity
     *
     * @param artifactId Artifact ID
     * @return Map of Rule Violations based on severity
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public Map<Severity, List<RuleViolation>> getSeverityBasedRuleViolationsForArtifact(String artifactId)
            throws GovernanceException {
        List<RuleViolation> ruleViolations = complianceMgtDAO.getRuleViolationsByArtifactId(artifactId);
        Map<Severity, List<RuleViolation>> severityBasedRuleViolations = new HashMap<>();
        for (RuleViolation ruleViolation : ruleViolations) {
            Severity severity = ruleViolation.getSeverity();
            if (severityBasedRuleViolations.containsKey(severity)) {
                severityBasedRuleViolations.get(severity).add(ruleViolation);
            } else {
                List<RuleViolation> ruleViolationList = new ArrayList<>();
                ruleViolationList.add(ruleViolation);
                severityBasedRuleViolations.put(severity, ruleViolationList);
            }
        }
        return severityBasedRuleViolations;
    }

    /**
     * Get Compliance Evaluation Result
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return Compliance Evaluation Result
     * @throws GovernanceException If an error occurs while getting the compliance evaluation result
     */
    @Override
    public ComplianceEvaluationResult getComplianceEvaluationResult(String artifactId,
                                                                    String policyId, String rulesetId)
            throws GovernanceException {
        return complianceMgtDAO.getComplianceEvaluationResult(artifactId, policyId, rulesetId);
    }

    /**
     * Get list of evaluated policies by artifact ID
     *
     * @param artifactId Artifact ID
     * @return List of evaluated policy IDs
     * @throws GovernanceException If an error occurs while getting the list of evaluated policies
     */
    @Override
    public List<String> getEvaluatedPoliciesByArtifactId(String artifactId) throws GovernanceException {
        List<ComplianceEvaluationResult> complianceEvaluationResults =
                complianceMgtDAO.getComplianceEvaluationResultsByArtifactId(artifactId);
        Set<String> evaluatedPolicies = new HashSet<>();
        for (ComplianceEvaluationResult complianceEvaluationResult : complianceEvaluationResults) {
            evaluatedPolicies.add(complianceEvaluationResult.getPolicyId());
        }
        return new ArrayList<>(evaluatedPolicies);
    }

    /**
     * Get list of evaluated rulesets by artifact ID and policy ID
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @return List of evaluated ruleset IDs
     * @throws GovernanceException If an error occurs while getting the list of evaluated rulesets
     */
    @Override
    public List<String> getEvaluatedRulesetsByArtifactIdAndPolicyId(String artifactId, String policyId)
            throws GovernanceException {
        List<ComplianceEvaluationResult> complianceEvaluationResults =
                complianceMgtDAO.getComplianceEvaluationResultsByArtifactAndPolicyId(artifactId, policyId);
        Set<String> evaluatedRulesets = new HashSet<>();
        for (ComplianceEvaluationResult complianceEvaluationResult : complianceEvaluationResults) {
            evaluatedRulesets.add(complianceEvaluationResult.getRulesetId());
        }
        return new ArrayList<>(evaluatedRulesets);

    }

    /**
     * Get a map of compliant and non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return Map of compliant and non-compliant artifacts
     * @throws GovernanceException If an error occurs while getting the compliant and non-compliant artifacts
     */
    @Override
    public Map<ArtifactComplianceState, List<String>> getComplianceStateOfEvaluatedArtifacts(
            ArtifactType artifactType, String organization) throws GovernanceException {
        List<String> allComplianceEvaluatedArtifacts =
                complianceMgtDAO.getAllComplianceEvaluatedArtifacts(artifactType, organization);
        List<String> nonCompliantArtifacts = complianceMgtDAO.getNonCompliantArtifacts(artifactType, organization);

        Map<ArtifactComplianceState, List<String>> compliantAndNonCompliantArtifacts = new HashMap<>();
        compliantAndNonCompliantArtifacts.put(ArtifactComplianceState.COMPLIANT, new ArrayList<>());
        compliantAndNonCompliantArtifacts.put(ArtifactComplianceState.NON_COMPLIANT, new ArrayList<>());

        for (String artifact : allComplianceEvaluatedArtifacts) {
            if (nonCompliantArtifacts.contains(artifact)) {
                compliantAndNonCompliantArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).add(artifact);
            } else {
                compliantAndNonCompliantArtifacts.get(ArtifactComplianceState.COMPLIANT).add(artifact);
            }
        }

        return compliantAndNonCompliantArtifacts;
    }

    /**
     * Get a map of policies followed and violated in the organization
     *
     * @param organization Organization
     * @return Map of policies followed and violated
     * @throws GovernanceException If an error occurs while getting the policy adherence
     */
    @Override
    public Map<PolicyAdherenceSate, List<String>> getAdherenceStateofEvaluatedPolicies(String organization)
            throws GovernanceException {
        List<String> allComplianceEvaluatedPolicies = complianceMgtDAO.getAllComplianceEvaluatedPolicies(organization);
        List<String> nonCompliantPolicies = complianceMgtDAO.getViolatedPolicies(organization);

        Map<PolicyAdherenceSate, List<String>> policyAdherence = new HashMap<>();
        policyAdherence.put(PolicyAdherenceSate.FOLLOWED, new ArrayList<>());
        policyAdherence.put(PolicyAdherenceSate.VIOLATED, new ArrayList<>());

        for (String policy : allComplianceEvaluatedPolicies) {
            if (nonCompliantPolicies.contains(policy)) {
                policyAdherence.get(PolicyAdherenceSate.VIOLATED).add(policy);
            } else {
                policyAdherence.get(PolicyAdherenceSate.FOLLOWED).add(policy);
            }
        }

        return policyAdherence;
    }

    /**
     * Get a map of artifacts evaluated by policy
     *
     * @param policyId            Policy ID
     * @param resolveArtifactName Whether the artifact name should be resolved
     * @return Map of artifacts evaluated by policy
     * @throws GovernanceException If an error occurs while getting the artifacts evaluated by policy
     */
    @Override
    public Map<ArtifactComplianceState, List<ArtifactInfo>> getComplianceStateOfEvaluatedArtifactsByPolicy
    (String policyId, boolean resolveArtifactName) throws GovernanceException {

        Map<ArtifactType, List<ComplianceEvaluationResult>> complianceEvaluationResults =
                complianceMgtDAO.getEvaluationResultsForPolicy(policyId);

        Map<ArtifactComplianceState, List<ArtifactInfo>> complianceStateOfEvaluatedArtifacts = new HashMap<>();

        complianceStateOfEvaluatedArtifacts.put(ArtifactComplianceState.COMPLIANT, new ArrayList<>());
        complianceStateOfEvaluatedArtifacts.put(ArtifactComplianceState.NON_COMPLIANT, new ArrayList<>());

        for (ArtifactType artifactType : complianceEvaluationResults.keySet()) {
            List<ComplianceEvaluationResult> evaluationResults = complianceEvaluationResults.get(artifactType);
            Set<String> allEvaluatedArtifacts = new HashSet<>();
            Set<String> nonCompliantArtifacts = new HashSet<>();

            for (ComplianceEvaluationResult evaluationResult : evaluationResults) {
                String artifactId = evaluationResult.getArtifactId();
                allEvaluatedArtifacts.add(artifactId);
                if (!evaluationResult.isEvaluationSuccess()) {
                    nonCompliantArtifacts.add(artifactId);
                }
            }

            for (String artifactId : allEvaluatedArtifacts) {
                ArtifactInfo artifactInfo = new ArtifactInfo();
                artifactInfo.setArtifactId(artifactId);
                artifactInfo.setArtifactType(artifactType);
                if (resolveArtifactName) {
                    artifactInfo.setDisplayName(GovernanceUtil.getArtifactName(artifactId, artifactType));
                }
                if (nonCompliantArtifacts.contains(artifactId)) {
                    complianceStateOfEvaluatedArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).add(artifactInfo);
                } else {
                    complianceStateOfEvaluatedArtifacts.get(ArtifactComplianceState.COMPLIANT).add(artifactInfo);
                }
            }
        }

        return complianceStateOfEvaluatedArtifacts;
    }

    /**
     * Is Ruleset Evaluated for Artifact
     *
     * @param artifactId Artifact ID
     * @param rulesetId  Ruleset ID
     * @return Whether the ruleset is evaluated for the artifact
     * @throws GovernanceException If an error occurs while checking whether the ruleset is evaluated for the artifact
     */
    @Override
    public boolean isRulesetEvaluatedForArtifact(String artifactId, String rulesetId) throws GovernanceException {
        List<ComplianceEvaluationResult> complianceEvaluationResults =
                complianceMgtDAO.getComplianceEvaluationResultsByArtifactIdAndRulesetId(artifactId, rulesetId);
        boolean isRulesetEvaluated = false;
        for (ComplianceEvaluationResult complianceEvaluationResult : complianceEvaluationResults) {
            if (rulesetId.equals(complianceEvaluationResult.getRulesetId())) {
                isRulesetEvaluated = true;
                break;
            }
        }
        return isRulesetEvaluated;
    }


    /**
     * Handle API Compliance Evaluation Request Sync
     *
     * @param artifactId             Artifact ID
     * @param artifactType           Artifact Type
     * @param govPolicies            List of governance policies to be evaluated
     * @param artifactProjectContent Map of artifact content
     * @param state                  State at which artifact should be governed
     * @param organization           Organization
     * @return ArtifactComplianceInfo object
     * @throws GovernanceException If an error occurs while handling the API compliance evaluation
     */
    @Override
    public ArtifactComplianceInfo handleComplianceEvaluationSync(String artifactId, ArtifactType artifactType,
                                                                 List<String> govPolicies, Map<RuleType, String>
                                                                             artifactProjectContent, GovernableState state,
                                                                 String organization) throws GovernanceException {

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance()
                .getValidationEngineService().getValidationEngine();
        ArtifactComplianceInfo artifactComplianceInfo = new ArtifactComplianceInfo();

        // Check if artifact is SOAP or GRAPHQL TODO: Support SOAP and GraphQL
        if (ArtifactType.SOAP_API.equals(artifactType) || ArtifactType.GRAPHQL_API.equals(artifactType)) {
            log.warn("Artifact type " + artifactType + " not supported for artifact ID: " + artifactId + " " +
                    ". Skipping governance evaluation");
            return artifactComplianceInfo;
        }

        if (artifactProjectContent == null || artifactProjectContent.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No content found in the artifact project for artifact ID: " + artifactId +
                        ". Loading content from DB.");
            }

            byte[] project = GovernanceUtil.getArtifactProject(artifactId, artifactType, organization);

            if (project == null) {
                log.warn("No content found in the artifact project for artifact ID: " + artifactId);
                return artifactComplianceInfo;
            }

            // Only extract content if the artifact type requires it.
            if (ArtifactType.isArtifactAPI(artifactType)) {
                artifactProjectContent = APIMUtil.extractAPIProjectContent(project, artifactId, artifactType);
            }

            if (artifactProjectContent == null || artifactProjectContent.isEmpty()) {
                log.warn("No content found in the artifact project for artifact ID: " + artifactId);
                return artifactComplianceInfo;
            }
        }

        for (String policyId : govPolicies) {
            GovernancePolicy policy = policyMgtDAO.getGovernancePolicyByID(policyId);
            List<Ruleset> rulesets = policyMgtDAO.getRulesetsByPolicyId(policyId);

            // Validate the artifact against each ruleset
            for (Ruleset ruleset : rulesets) {
                ArtifactType rulesetArtifactType = ruleset.getArtifactType();

                // Check if ruleset's artifact type matches with the artifact's type
                if ((ArtifactType.isArtifactAPI(artifactType) &&
                        ArtifactType.API.equals(rulesetArtifactType)) ||
                        (rulesetArtifactType.equals(artifactType))) {

                    // Get target file content from artifact project based on ruleType
                    RuleType ruleType = ruleset.getRuleType();
                    String contentToValidate = artifactProjectContent.get(ruleType);

                    if (contentToValidate == null) {
                        log.warn(ruleType + " content not found in artifact project for artifact ID: " +
                                artifactId + ". Skipping governance evaluation for ruleset ID: " + ruleset.getId());
                        continue;
                    }

                    // Send target content and ruleset for validation
                    List<RuleViolation> ruleViolations = validationEngine.validate(
                            contentToValidate, ruleset);

                    Map<GovernanceActionType, List<RuleViolation>> blockableAndNonBlockableViolations =
                            filterBlockableAndNonBlockableRuleViolations(artifactId, policy, ruleViolations, state);

                    // Add the rule violations to the compliance info
                    artifactComplianceInfo.addBlockingViolations(blockableAndNonBlockableViolations
                            .get(GovernanceActionType.BLOCK));
                    artifactComplianceInfo.addNonBlockingViolations(blockableAndNonBlockableViolations
                            .get(GovernanceActionType.NOTIFY));

                    // If there are blockable violations, set the boolean flag
                    artifactComplianceInfo.setBlockingNecessary(
                            !blockableAndNonBlockableViolations.get(GovernanceActionType.BLOCK).isEmpty());

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Ruleset artifact type does not match with the artifact's type. Skipping " +
                                "governance evaluation for ruleset ID: " + ruleset.getId());
                    }
                }
            }
        }
        return artifactComplianceInfo;

    }


    /**
     * Filter Blockable and Non-Blockable Rule Violations Based on Policy and Rule Violations
     *
     * @param artifactId     Artifact ID
     * @param policy         Governance Policy
     * @param ruleViolations List of Rule Violations
     * @param state          State at which artifact should be governed
     * @return Map of blockable and non-blockable rule violations
     */
    private Map<GovernanceActionType, List<RuleViolation>>
    filterBlockableAndNonBlockableRuleViolations(String artifactId, GovernancePolicy policy,
                                                 List<RuleViolation> ruleViolations, GovernableState state) {

        // Identify blockable severities from the policy
        List<Severity> blockableSeverities = new ArrayList<>();
        for (GovernanceAction governanceAction : policy.getActions()) {

            // If the state matches and action is block the violation is blockable
            if (state.equals(governanceAction.getGovernableState()) &&
                    GovernanceActionType.BLOCK.equals(governanceAction.getType())) {
                blockableSeverities.add(governanceAction.getRuleSeverity());
            }
        }

        Map<GovernanceActionType, List<RuleViolation>> blockableAndNonBlockableViolations = new HashMap<>();

        List<RuleViolation> blockableViolations = new ArrayList<>();
        List<RuleViolation> nonBlockingViolations = new ArrayList<>();

        // Iterate through the rule violations and categorize them as blockable and non-blockable
        // based on blockableSeverities
        for (RuleViolation ruleViolation : ruleViolations) {

            ruleViolation.setArtifactId(artifactId);
            ruleViolation.setPolicyId(policy.getId());
            if (blockableSeverities.contains(ruleViolation.getSeverity())) {
                blockableViolations.add(ruleViolation);
            } else {
                nonBlockingViolations.add(ruleViolation);
            }
        }

        blockableAndNonBlockableViolations.put(GovernanceActionType.BLOCK, blockableViolations);
        blockableAndNonBlockableViolations.put(GovernanceActionType.NOTIFY, nonBlockingViolations);

        return blockableAndNonBlockableViolations;
    }

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactId Artifact ID
     * @throws GovernanceException If an error occurs while deleting the governance data
     */
    @Override
    public void deleteArtifact(String artifactId) throws GovernanceException {
        complianceMgtDAO.deleteArtifact(artifactId);
    }
}
