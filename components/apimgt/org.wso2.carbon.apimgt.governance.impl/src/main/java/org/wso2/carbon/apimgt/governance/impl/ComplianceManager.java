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
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceDryRunInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.PolicyAdherenceSate;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents the Compliance Manager, which is responsible for managing compliance related operations
 */
public class ComplianceManager {
    private static final Log log = LogFactory.getLog(ComplianceManager.class);

    private final ComplianceMgtDAO complianceMgtDAO;
    private final GovernancePolicyMgtDAO policyMgtDAO;

    private final RulesetMgtDAO rulesetMgtDAO;

    public ComplianceManager() {
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
    public void handlePolicyChangeEvent(String policyId, String organization) throws APIMGovernanceException {

        // Get the policy and its labels and associated governable states
        APIMGovernancePolicy policy = policyMgtDAO.getGovernancePolicyByID(policyId, organization);

        List<String> labels = policy.getLabels();
        List<APIMGovernableState> apimGovernableStates = policy.getGovernableStates();

        // Get artifacts that should be governed by the policy
        List<ArtifactInfo> artifacts = new ArrayList<>();

        if (policy.isGlobal()) {
            // If the policy is a global policy, get all artifacts
            artifacts.addAll(getArtifactsByGovernableStates(apimGovernableStates, organization));
        } else if (labels != null && !labels.isEmpty()) {
            artifacts.addAll(getArtifactsByLabelsAndGovernableStates(labels, apimGovernableStates));
        }

        for (ArtifactInfo artifact : artifacts) {
            String artifactRefId = artifact.getArtifactRefId();
            ArtifactType artifactType = artifact.getArtifactType();
            complianceMgtDAO.addComplianceEvalRequest(artifactRefId, artifactType,
                    Collections.singletonList(policyId), organization);
        }
    }

    /**
     * Get Artifacts by Governable States
     *
     * @param apimGovernableStates List of governable states
     * @param organization         Organization
     * @return List of unique artifact information
     */
    private List<ArtifactInfo> getArtifactsByGovernableStates(List<APIMGovernableState> apimGovernableStates,
                                                              String organization) throws APIMGovernanceException {
        Map<ArtifactType, List<String>> artifactsMap = APIMGovernanceUtil.getAllArtifacts(organization);
        return filterAndCollectArtifacts(artifactsMap, apimGovernableStates);
    }

    /**
     * Get Artifacts by Labels and Governable State
     *
     * @param labels               List of labels
     * @param apimGovernableStates List of governable states
     * @return List of unique artifact information
     */
    private List<ArtifactInfo> getArtifactsByLabelsAndGovernableStates(List<String> labels,
                                                                       List<APIMGovernableState> apimGovernableStates)
            throws APIMGovernanceException {
        List<ArtifactInfo> artifactInfoList = new ArrayList<>();
        Set<String> artifactRefIds = new HashSet<>(); // Track unique artifact IDs

        // Get Artifacts for each label and merge results
        for (String label : labels) {
            Map<ArtifactType, List<String>> artifactsMap = APIMGovernanceUtil.getArtifactsForLabel(label);

            // Collect artifacts by filtering based on governable states
            List<ArtifactInfo> filteredArtifacts = filterAndCollectArtifacts(artifactsMap, apimGovernableStates);

            // Add only unique artifacts based on artifactRefId
            for (ArtifactInfo artifactInfo : filteredArtifacts) {
                if (artifactRefIds.add(artifactInfo.getArtifactRefId())) {
                    artifactInfoList.add(artifactInfo);
                }
            }
        }

        return artifactInfoList;
    }

    /**
     * Filter and collect artifacts based on governable states
     *
     * @param artifactsMap         Map of artifact type to list of artifact IDs
     * @param apimGovernableStates List of governable states
     * @return List of unique artifact information
     */
    private List<ArtifactInfo> filterAndCollectArtifacts(Map<ArtifactType, List<String>> artifactsMap,
                                                         List<APIMGovernableState> apimGovernableStates)
            throws APIMGovernanceException {

        List<ArtifactInfo> artifactInfoList = new ArrayList<>();

        for (Map.Entry<ArtifactType, List<String>> entry : artifactsMap.entrySet()) {
            ArtifactType artifactType = entry.getKey();
            List<String> artifactRefIds = artifactsMap.get(artifactType);

            if (ArtifactType.API.equals(artifactType)) {
                for (String artifactRefId : artifactRefIds) {
                    String apiStatus = APIMUtil.getAPIStatus(artifactRefId);
                    boolean isDeployed = APIMUtil.isAPIDeployed(artifactRefId);
                    boolean isAPIGovernable = APIMUtil.isAPIGovernable(apiStatus, isDeployed, apimGovernableStates);
                    // If the API should be governed by the policy
                    if (isAPIGovernable) {
                        ArtifactInfo artifactInfo = new ArtifactInfo();
                        artifactInfo.setArtifactRefId(artifactRefId);
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

    public void handleRulesetChangeEvent(String rulesetId, String organization) throws APIMGovernanceException {
        List<String> policies = rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId, organization);

        for (String policyId : policies) {
            handlePolicyChangeEvent(policyId, organization);
        }
    }

    /**
     * Handle API Compliance Evaluation Request Async
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param govPolicies   List of governance policies to be evaluated
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while handling the API compliance evaluation
     */

    public void handleComplianceEvalAsync(String artifactRefId, ArtifactType artifactType,
                                          List<String> govPolicies,
                                          String organization) throws APIMGovernanceException {

        if (govPolicies != null && !govPolicies.isEmpty()) {
            complianceMgtDAO.addComplianceEvalRequest(artifactRefId, artifactType, govPolicies, organization);
        }

    }

    /**
     * Get Rule Violations
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetId     Ruleset ID
     * @param organization  Organization
     * @return List of Rule Violations
     * @throws APIMGovernanceException If an error occurs while getting the rule violations
     */

    public List<RuleViolation> getRuleViolations(String artifactRefId, ArtifactType artifactType,
                                                 String rulesetId, String organization) throws APIMGovernanceException {
        return complianceMgtDAO.getRuleViolations(artifactRefId, artifactType, rulesetId, organization);
    }

    /**
     * Get the rule violations by artifact ID based on severity
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Map of Rule Violations based on severity
     * @throws APIMGovernanceException If an error occurs while getting the rule violations
     */

    public Map<RuleSeverity, List<RuleViolation>> getSeverityBasedRuleViolationsForArtifact(String artifactRefId,
                                                                                            ArtifactType artifactType,
                                                                                            String organization)
            throws APIMGovernanceException {
        List<RuleViolation> ruleViolations = complianceMgtDAO.getRuleViolationsForArtifact(artifactRefId, artifactType,
                organization);
        Map<RuleSeverity, List<RuleViolation>> severityBasedRuleViolations = new HashMap<>();
        for (RuleViolation ruleViolation : ruleViolations) {
            RuleSeverity severity = ruleViolation.getSeverity();
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
     * Get list of evaluated policies for the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return List of evaluated policy IDs
     * @throws APIMGovernanceException If an error occurs while getting the list of evaluated policies
     */

    public List<String> getEvaluatedPoliciesForArtifact(String artifactRefId, ArtifactType
            artifactType, String organization)
            throws APIMGovernanceException {
        return complianceMgtDAO.getEvaluatedPoliciesForArtifact(artifactRefId, artifactType, organization);
    }

    /**
     * Get list of evaluated rulesets for the artifact and policy
     *
     * @param artifactRefId  Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType   Artifact Type
     * @param policyRulesets List of rulesets for the policy
     * @param organization   Organization
     * @return List of evaluated rulesets IDs
     * @throws APIMGovernanceException If an error occurs while getting the list of evaluated rulesets
     */

    public List<String> getEvaluatedRulesetsForArtifactAndPolicy(String artifactRefId, ArtifactType artifactType,
                                                                 List<RulesetInfo> policyRulesets, String organization)
            throws APIMGovernanceException {
        List<String> evaluatedRulesetsForArtifact =
                complianceMgtDAO.getEvaluatedRulesetsForArtifact(artifactRefId, artifactType, organization);
        List<String> evaluatedRulesetsForPolicy = new ArrayList<>();
        for (RulesetInfo ruleset : policyRulesets) {
            if (evaluatedRulesetsForArtifact.contains(ruleset.getId())) {
                evaluatedRulesetsForPolicy.add(ruleset.getId());
            }
        }
        return evaluatedRulesetsForPolicy;

    }

    /**
     * Get list of compliance pending artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of compliance pending artifacts
     * @throws APIMGovernanceException If an error occurs while getting the compliance pending artifacts
     */
    public List<String> getCompliancePendingArtifacts(ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        return complianceMgtDAO.getCompliancePendingArtifacts(artifactType, organization);
    }

    /**
     * Get a map of compliant and non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return Map of compliant and non-compliant artifacts
     * @throws APIMGovernanceException If an error occurs while getting the compliant and non-compliant artifacts
     */

    public Map<ArtifactComplianceState, List<String>> getComplianceStateOfEvaluatedArtifacts(
            ArtifactType artifactType, String organization) throws APIMGovernanceException {
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
     * @throws APIMGovernanceException If an error occurs while getting the policy adherence
     */

    public Map<PolicyAdherenceSate, List<String>> getAdherenceStateofEvaluatedPolicies(String organization)
            throws APIMGovernanceException {
        List<String> allComplianceEvaluatedPolicies = complianceMgtDAO
                .getAllComplianceEvaluatedPolicies(organization);

        // Get a map of policies to their rulesets
        Map<String, List<String>> policyRulesetsMap = new HashMap<>();
        for (String policyId : allComplianceEvaluatedPolicies) {
            List<String> rulesets = policyMgtDAO.getRulesetsByPolicyId(policyId, organization)
                    .stream().map(RulesetInfo::getId).collect(Collectors.toList());
            policyRulesetsMap.put(policyId, rulesets);
        }

        // Get the list of violated rulesets
        List<String> violatedRulesets = complianceMgtDAO.getViolatedRulesets(organization);

        // Identify violated policies
        List<String> violatedPolicies = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : policyRulesetsMap.entrySet()) {
            String policyId = entry.getKey();
            List<String> rulesets = entry.getValue();
            if (violatedRulesets.stream().anyMatch(rulesets::contains)) {
                violatedPolicies.add(policyId);
            }
        }

        Map<PolicyAdherenceSate, List<String>> policyAdherence = new HashMap<>();
        policyAdherence.put(PolicyAdherenceSate.FOLLOWED, new ArrayList<>());
        policyAdherence.put(PolicyAdherenceSate.VIOLATED, new ArrayList<>());

        for (String policy : allComplianceEvaluatedPolicies) {
            if (violatedPolicies.contains(policy)) {
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
     * @param policyId                      Policy ID
     * @param organization                  Organization
     * @param resolveArtifactNameAndVersion Whether the artifact name,version should be resolved
     * @return Map of artifacts evaluated by policy
     * @throws APIMGovernanceException If an error occurs while getting the artifacts evaluated by policy
     */

    public Map<ArtifactComplianceState, List<ArtifactInfo>> getArtifactsComplianceForPolicy
    (String policyId, String organization, boolean resolveArtifactNameAndVersion) throws APIMGovernanceException {


        Map<ArtifactComplianceState, List<ArtifactInfo>> complianceStateOfEvaluatedArtifacts = new HashMap<>();

        complianceStateOfEvaluatedArtifacts.put(ArtifactComplianceState.COMPLIANT, new ArrayList<>());
        complianceStateOfEvaluatedArtifacts.put(ArtifactComplianceState.NON_COMPLIANT, new ArrayList<>());

        List<ArtifactInfo> evaluatedArtifacts = complianceMgtDAO.getEvaluatedArtifactsForPolicy(policyId, organization);
        List<String> applicableRulesets = policyMgtDAO.getRulesetsByPolicyId(policyId, organization).
                stream().map(RulesetInfo::getId).collect(Collectors.toList());

        for (ArtifactInfo artifactInfo : evaluatedArtifacts) {
            String artifactRefId = artifactInfo.getArtifactRefId();
            ArtifactType artifactType = artifactInfo.getArtifactType();
            if (resolveArtifactNameAndVersion) {
                artifactInfo.setName(APIMGovernanceUtil.getArtifactName(artifactRefId, artifactType));
                artifactInfo.setVersion(APIMGovernanceUtil.getArtifactVersion(artifactRefId, artifactType));
            }

            List<String> violatedRulesets = complianceMgtDAO.getViolatedRulesetsForArtifact
                    (artifactRefId, artifactType, organization);

            if (violatedRulesets.stream().anyMatch(applicableRulesets::contains)) {
                complianceStateOfEvaluatedArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).add(artifactInfo);
            } else {
                complianceStateOfEvaluatedArtifacts.get(ArtifactComplianceState.COMPLIANT).add(artifactInfo);
            }
        }
        return complianceStateOfEvaluatedArtifacts;
    }

    /**
     * Is Ruleset Evaluated for Artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param rulesetId     Ruleset ID
     * @param organization  Organization
     * @return Whether the ruleset is evaluated for the artifact
     * @throws APIMGovernanceException If an error occurs while checking whether
     *                                 the ruleset is evaluated for the artifact
     */

    public boolean isRulesetEvaluatedForArtifact(String artifactRefId, ArtifactType artifactType,
                                                 String rulesetId, String organization)
            throws APIMGovernanceException {
        return complianceMgtDAO.isRulesetEvaluatedForArtifact(artifactRefId, artifactType,
                rulesetId, organization);
    }


    /**
     * Handle API Compliance Evaluation Request Sync
     *
     * @param artifactRefId          Artifact Reference ID (ID of the artifact on APIM side)
     * @param revisionNo             Revision number
     * @param artifactType           Artifact Type
     * @param govPolicies            List of governance policies to be evaluated
     * @param artifactProjectContent Map of artifact content
     * @param state                  State at which artifact should be governed
     * @param organization           Organization
     * @return ArtifactComplianceInfo object
     * @throws APIMGovernanceException If an error occurs while handling the API compliance evaluation
     */

    public ArtifactComplianceInfo handleComplianceEvalSync(String artifactRefId,
                                                           String revisionNo, ArtifactType artifactType,
                                                           List<String> govPolicies,
                                                           Map<RuleType, String> artifactProjectContent,
                                                           APIMGovernableState state, String organization)
            throws APIMGovernanceException {

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance()
                .getValidationEngineService().getValidationEngine();

        ArtifactComplianceInfo artifactComplianceInfo = new ArtifactComplianceInfo();

        ExtendedArtifactType extendedArtifactTypeForArtifact =
                APIMGovernanceUtil.getExtendedArtifactTypeForArtifact
                        (artifactRefId, artifactType); // API --> REST_API, ASYNC_API, etc

        // Check if artifact is SOAP or GRAPHQL
        if (ExtendedArtifactType.SOAP_API.equals(extendedArtifactTypeForArtifact)
                || ExtendedArtifactType.GRAPHQL_API.equals(extendedArtifactTypeForArtifact)) {
            log.warn("Artifact type " + extendedArtifactTypeForArtifact +
                    " not supported for artifact ID: " + artifactRefId + " " +
                    ". Skipping governance evaluation");
            return artifactComplianceInfo;
        }

        if (artifactProjectContent == null || artifactProjectContent.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No content found in the artifact project for artifact ID: " + artifactRefId +
                        ". Loading content from DB.");
            }

            byte[] project = APIMGovernanceUtil.getArtifactProjectWithRevision(artifactRefId, revisionNo, artifactType,
                    organization);

            if (project == null) {
                log.warn("No content found in the artifact project for artifact ID: " + artifactRefId);
                return artifactComplianceInfo;
            }

            // Only extract content if the artifact type requires it.
            artifactProjectContent = APIMGovernanceUtil.extractArtifactProjectContent(project, artifactType);


            if (artifactProjectContent == null || artifactProjectContent.isEmpty()) {
                log.warn("No content found in the artifact project for artifact ID: " + artifactRefId);
                return artifactComplianceInfo;
            }
        }

        for (String policyId : govPolicies) {
            APIMGovernancePolicy policy = policyMgtDAO.getGovernancePolicyByID(policyId, organization);
            List<Ruleset> rulesets = policyMgtDAO.getRulesetsWithContentByPolicyId(policyId, organization);

            // Validate the artifact against each ruleset
            for (Ruleset ruleset : rulesets) {
                ExtendedArtifactType extendedArtifactType = ruleset.getArtifactType();

                // Check if ruleset's artifact type matches with the artifact's type
                if (extendedArtifactType.equals(extendedArtifactTypeForArtifact)) {

                    // Get target file content from artifact project based on ruleType
                    RuleType ruleType = ruleset.getRuleType();
                    String contentToValidate = artifactProjectContent.get(ruleType);

                    if (contentToValidate == null) {
                        log.warn(ruleType + " content not found in artifact project for artifact ID: " +
                                artifactRefId + ". Skipping governance evaluation for ruleset ID: " + ruleset.getId());
                        continue;
                    }

                    // Send target content and ruleset for validation
                    List<RuleViolation> ruleViolations = validationEngine.validate(
                            contentToValidate, ruleset);

                    Map<APIMGovernanceActionType, List<RuleViolation>> blockableAndNonBlockableViolations =
                            filterBlockableAndNonBlockableRuleViolations(artifactRefId,
                                    artifactType, policy, ruleViolations, state, organization);

                    // Add the rule violations to the compliance info
                    artifactComplianceInfo.addBlockingViolations(blockableAndNonBlockableViolations
                            .get(APIMGovernanceActionType.BLOCK));
                    artifactComplianceInfo.addNonBlockingViolations(blockableAndNonBlockableViolations
                            .get(APIMGovernanceActionType.NOTIFY));
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Ruleset artifact type does not match with the artifact's type. Skipping " +
                                "governance evaluation for ruleset ID: " + ruleset.getId());
                    }
                }
            }
        }

        if (!artifactComplianceInfo.getBlockingRuleViolations().isEmpty()) {
            artifactComplianceInfo.setBlockingNecessary(true);
        }
        return artifactComplianceInfo;
    }

    /**
     * Handle API Compliance Evaluation Request Dry Run
     *
     * @param artifactType           Artifact Type (REST_API, ASYNC_API, etc)
     * @param govPolicies            List of governance policies to be evaluated
     * @param artifactProjectContent Map of artifact content
     * @param organization           Organization
     * @return ArtifactComplianceDryRunInfo object
     * @throws APIMGovernanceException If an error occurs while handling the API compliance evaluation
     */

    public ArtifactComplianceDryRunInfo handleComplianceEvalDryRun(ExtendedArtifactType artifactType,
                                                                   List<String> govPolicies, Map<RuleType, String>
                                                                           artifactProjectContent,
                                                                   String organization) throws
            APIMGovernanceException {

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance()
                .getValidationEngineService().getValidationEngine();
        ArtifactComplianceDryRunInfo artifactComplianceDryRunInfo = new ArtifactComplianceDryRunInfo();

        // Check if artifact is SOAP or GRAPHQL
        if (ExtendedArtifactType.SOAP_API.equals(artifactType) ||
                ExtendedArtifactType.GRAPHQL_API.equals(artifactType)) {
            log.error("Artifact type " + artifactType + " not supported. Skipping governance evaluation");
            return null;
        }

        // If artifact content is not provided dry run is not possible
        if (artifactProjectContent == null || artifactProjectContent.isEmpty()) {
            log.error("No content found in the artifact project.");
            return null;
        }

        for (String policyId : govPolicies) {
            APIMGovernancePolicy policy = policyMgtDAO.getGovernancePolicyByID(policyId, organization);
            List<Ruleset> rulesets = policyMgtDAO.getRulesetsWithContentByPolicyId(policyId, organization);

            // Validate the artifact against each ruleset
            for (Ruleset ruleset : rulesets) {
                RulesetInfo rulesetInfo = rulesetMgtDAO.getRulesetById(ruleset.getId(), organization);
                ExtendedArtifactType extendedArtifactType = ruleset.getArtifactType();

                // Check if ruleset's artifact type matches with the artifact's type
                if (extendedArtifactType.equals(artifactType)) {

                    // Get target file content from artifact project based on ruleType
                    RuleType ruleType = ruleset.getRuleType();
                    String contentToValidate = artifactProjectContent.get(ruleType);

                    if (contentToValidate == null) {
                        log.warn(ruleType + " content not found in artifact project . Skipping governance " +
                                "evaluation " +
                                "for ruleset ID: " + ruleset.getId());
                        continue;
                    }

                    // Send target content and ruleset for validation
                    List<RuleViolation> ruleViolations = validationEngine.validate(
                            contentToValidate, ruleset);

                    artifactComplianceDryRunInfo.addRuleViolationsForRuleset(policy, rulesetInfo, ruleViolations);

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Ruleset artifact type does not match with the artifact's type. Skipping " +
                                "governance evaluation for ruleset ID: " + ruleset.getId());
                    }
                }
            }
        }
        return artifactComplianceDryRunInfo;
    }

    /**
     * Filter Blockable and Non-Blockable Rule Violations Based on Policy and Rule Violations
     *
     * @param artifactRefId  Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType   Artifact Type
     * @param policy         Governance Policy
     * @param ruleViolations List of Rule Violations
     * @param state          State at which artifact should be governed
     * @param organization   Organization
     * @return Map of blockable and non-blockable rule violations
     */
    private Map<APIMGovernanceActionType, List<RuleViolation>>
    filterBlockableAndNonBlockableRuleViolations(String artifactRefId, ArtifactType artifactType,
                                                 APIMGovernancePolicy policy,
                                                 List<RuleViolation> ruleViolations, APIMGovernableState state,
                                                 String organization) throws APIMGovernanceException {

        // Identify blockable severities from the policy
        List<RuleSeverity> blockableSeverities = new ArrayList<>();
        for (APIMGovernanceAction governanceAction : policy.getActions()) {

            // If the state matches and action is block the violation is blockable
            if (state.equals(governanceAction.getGovernableState()) &&
                    APIMGovernanceActionType.BLOCK.equals(governanceAction.getType())) {
                blockableSeverities.add(governanceAction.getRuleSeverity());
            }
        }

        Map<APIMGovernanceActionType, List<RuleViolation>> blockableAndNonBlockableViolations = new HashMap<>();

        List<RuleViolation> blockableViolations = new ArrayList<>();
        List<RuleViolation> nonBlockingViolations = new ArrayList<>();

        // Iterate through the rule violations and categorize them as blockable and non-blockable
        // based on blockable severities
        for (RuleViolation ruleViolation : ruleViolations) {
            ruleViolation.setArtifactRefId(artifactRefId);
            ruleViolation.setArtifactType(artifactType);
            ruleViolation.setOrganization(organization);
            ruleViolation.setRuleType(new RulesetManager()
                    .getRulesetById(ruleViolation.getRulesetId(), organization)
                    .getRuleType());
            if (blockableSeverities.contains(ruleViolation.getSeverity())) {
                blockableViolations.add(ruleViolation);
            } else {
                nonBlockingViolations.add(ruleViolation);
            }
        }

        blockableAndNonBlockableViolations.put(APIMGovernanceActionType.BLOCK, blockableViolations);
        blockableAndNonBlockableViolations.put(APIMGovernanceActionType.NOTIFY, nonBlockingViolations);

        return blockableAndNonBlockableViolations;
    }

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while deleting the governance data
     */

    public void deleteArtifact(String artifactRefId, ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        complianceMgtDAO.deleteArtifact(artifactRefId, artifactType, organization);
    }

    /**
     * Get the list of rulesets evaluated for the artifact
     *
     * @param evaluatedPolicies List of evaluated policies
     * @param violatedRulesets  List of violated rulesets
     * @param organization      Organization
     * @return List of violated policies
     */

    public List<String> identifyViolatedPolicies(List<String> evaluatedPolicies, List<String> violatedRulesets,
                                                 String organization) throws APIMGovernanceException {
        Set<String> violatedPolicies = new HashSet<>();
        for (String policy : evaluatedPolicies) {
            List<String> rulesets = policyMgtDAO.getRulesetsWithContentByPolicyId(policy, organization).stream()
                    .map(Ruleset::getId).collect(Collectors.toList());
            if (violatedRulesets.stream().anyMatch(rulesets::contains)) {
                violatedPolicies.add(policy);
            }
        }
        return new ArrayList<>(violatedPolicies);
    }

    /**
     * Check whether the evaluation is pending for the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Whether the evaluation is pending for the artifact
     * @throws APIMGovernanceException If an error occurs while checking whether the evaluation
     *                                 is pending for the artifact
     */

    public boolean isEvaluationPendingForArtifact(String artifactRefId, ArtifactType artifactType,
                                                  String organization) throws APIMGovernanceException {
        String reqId = complianceMgtDAO.getPendingEvalRequest(artifactRefId, artifactType, organization);
        return reqId != null;
    }
}
