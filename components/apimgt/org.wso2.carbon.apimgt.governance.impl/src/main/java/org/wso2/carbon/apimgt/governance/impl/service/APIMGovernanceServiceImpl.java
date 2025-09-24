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

package org.wso2.carbon.apimgt.governance.impl.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceDryRunInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.util.AuditLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the API Governance Service Implementation which is responsible for managing governance related
 * operations on API Manager side.
 */
@Component(
        name = "org.wso2.carbon.apimgt.governance.service",
        service = APIMGovernanceService.class,
        immediate = true
)
public class APIMGovernanceServiceImpl implements APIMGovernanceService {

    private static final Log log = LogFactory.getLog(APIMGovernanceServiceImpl.class);
    private final ComplianceManager complianceManager;
    private final PolicyManager policyManager;

    public APIMGovernanceServiceImpl() {

        complianceManager = new ComplianceManager();
        policyManager = new PolicyManager();
    }

    /**
     * Check if there are any policies with blocking actions for the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact type (ArtifactType.API)
     * @param state         State to be governed
     * @param organization  Organization
     * @return True if there are policies with blocking actions, False otherwise
     * @throws APIMGovernanceException If an error occurs while checking for policies with blocking actions
     */
    @Override
    public boolean isPoliciesWithBlockingActionExist(String artifactRefId,
                                                     ArtifactType artifactType, APIMGovernableState state,
                                                     String organization)
            throws APIMGovernanceException {

        if (log.isDebugEnabled()) {
            log.debug("Checking for policies with blocking actions for artifact: " + artifactRefId + 
                     " in state: " + state);
        }
        List<String> applicablePolicyIds = APIMGovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactRefId,
                artifactType, state, organization);
        boolean hasBlockingActions = APIMGovernanceUtil.isBlockingActionsPresent(applicablePolicyIds, state, 
                organization);
        if (log.isDebugEnabled()) {
            log.debug("Found " + applicablePolicyIds.size() + " applicable policies, blocking actions present: " + 
                     hasBlockingActions);
        }
        return hasBlockingActions;
    }

    /**
     * Evaluate compliance of the artifact asynchronously
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact type ArtifactType.API
     *                      , DO NOT USE ArtifactType.API
     * @param state         State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public void evaluateComplianceAsync(String artifactRefId, ArtifactType artifactType,
                                        APIMGovernableState state, String organization) throws
            APIMGovernanceException {

        if (log.isDebugEnabled()) {
            log.debug("Starting async compliance evaluation for artifact: " + artifactRefId + " in state: " + state);
        }

        // Check whether the artifact is governable and if not return
        boolean isArtifactGovernable = APIMGovernanceUtil.isArtifactGovernable(artifactRefId, artifactType);
        if (!isArtifactGovernable) {
            String message = String.format("Skipping async compliance evaluation for artifact %s " +
                    "in organization %s due to incompatible artifact type", artifactRefId, organization);
            log.debug(message);
            AuditLogger.log("Skip Async Eval", message);
            return;
        }

        List<APIMGovernableState> dependentAPIMGovernableStates =
                APIMGovernableState.getDependentGovernableStates(state);

        log.info("Evaluating compliance for artifact: " + artifactRefId + " across " + 
                dependentAPIMGovernableStates.size() + " dependent states");

        for (APIMGovernableState dependentState : dependentAPIMGovernableStates) {
            List<String> applicablePolicyIds = APIMGovernanceUtil
                    .getApplicablePoliciesForArtifactWithState(artifactRefId,
                            artifactType, dependentState, organization);
            if (log.isDebugEnabled()) {
                log.debug("Found " + applicablePolicyIds.size() + " applicable policies for state: " + dependentState);
            }
            complianceManager.handleComplianceEvalAsync
                    (artifactRefId, artifactType, applicablePolicyIds, organization);
        }
    }

    /**
     * Evaluate compliance of the artifact synchronously
     *
     * @param artifactRefId          Artifact Reference ID (ID of the artifact on APIM side)
     * @param revisionId             Revision ID
     * @param artifactType           Artifact type ArtifactType.API
     * @param state                  State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param artifactProjectContent This is a map of RuleType and String which contains the content of the artifact
     *                               project. This is used to evaluate the compliance of the artifact.
     *                               API_METADATA --> api.yaml content
     *                               API_DEFINITION --> api definition content
     *                               API_DOCUMENTATION --> api documentation content.
     *                               <p>
     *                               If not provided the details will be taken from DB
     * @param organization           Organization
     * @return ArtifactComplianceInfo object
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public ArtifactComplianceInfo evaluateComplianceSync(String artifactRefId, String revisionId,
                                                         ArtifactType artifactType, APIMGovernableState state,
                                                         Map<RuleType, String> artifactProjectContent,
                                                         String organization) throws APIMGovernanceException {

        // Check whether the artifact is governable and if not return
        boolean isArtifactGovernable = APIMGovernanceUtil.isArtifactGovernable(artifactRefId, artifactType);
        if (!isArtifactGovernable) {
            String message = String.format("Skipping sync compliance evaluation for artifact %s " +
                    "in organization %s due to incompatible artifact type", artifactRefId, organization);
            log.debug(message);
            AuditLogger.log("Skip Sync Eval", message);
            ArtifactComplianceInfo artifactComplianceInfo = new ArtifactComplianceInfo();
            artifactComplianceInfo.setBlockingNecessary(false);
            return artifactComplianceInfo;
        }

        List<String> applicablePolicyIds = APIMGovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactRefId,
                artifactType, state, organization);

        ArtifactComplianceInfo artifactComplianceInfo = complianceManager.handleComplianceEvalSync
                (artifactRefId, revisionId, artifactType, applicablePolicyIds,
                        artifactProjectContent, state, organization);

        // Though compliance is evaluated sync , we need to evaluate the compliance for all dependent states async to
        // update results in the database. Hence, calling the async method here and this won't take time as it is async
        evaluateComplianceAsync(artifactRefId, artifactType, state, organization);
        return artifactComplianceInfo;
    }


    /**
     * This method can be called to evaluate the compliance of the artifact without persisting the compliance data (A
     * dry run) using the provided artifact content file path and the artifact type.
     *
     * @param artifactType Artifact type (ArtifactType.API, etc)
     *                     project
     * @param organization Organization
     * @return ArtifactComplianceDryRunInfo object
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public ArtifactComplianceDryRunInfo evaluateComplianceDryRunSync(ArtifactType artifactType,
                                                                     byte[] zipArchive, String organization)
            throws APIMGovernanceException {

        ExtendedArtifactType extendedArtifactType = APIMGovernanceUtil
                .getExtendedArtifactTypeFromProject(zipArchive, artifactType);

        if (extendedArtifactType == null) {
            String message = String.format(
                    "Skipping sync compliance evaluation for given artifact in organization %s " +
                            "due to incompatible artifact type", organization);
            log.debug(message);
            AuditLogger.log("Skip Dry Run Eval", message);
            return null;
        }

        Map<String, String> policies = policyManager.getOrganizationWidePolicies(organization);

        List<String> applicablePolicyIds = new ArrayList<>(policies.keySet());

        Map<RuleType, String> contentMap = APIMGovernanceUtil
                .extractArtifactProjectContent(zipArchive, artifactType);

        return complianceManager.handleComplianceEvalDryRun(extendedArtifactType, applicablePolicyIds,
                contentMap, organization);


    }

    /**
     * Handle artifact label attach
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact type ArtifactType.API
     * @param labels        List of label IDs
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while attaching the label
     */
    @Override
    public void evaluateComplianceOnLabelAttach(String artifactRefId, ArtifactType artifactType,
                                                List<String> labels,
                                                String organization) throws APIMGovernanceException {

        // Check whether the artifact is governable and if not return
        boolean isArtifactGovernable = APIMGovernanceUtil.isArtifactGovernable(artifactRefId, artifactType);
        if (!isArtifactGovernable) {
            return;
        }


        // Clear previous compliance data for the artifact
        complianceManager.deleteArtifact(artifactRefId, artifactType, organization);

        Set<String> allCandidatePolicies = new HashSet<>();

        // Get policies for labels
        for (String label : labels) {
            allCandidatePolicies.addAll(new ArrayList<>(policyManager
                    .getPoliciesByLabel(label, organization).keySet()));
        }

        // Need to add organization wide policies as well
        allCandidatePolicies.addAll(policyManager.getOrganizationWidePolicies(organization).keySet());

        // Filter which policies should be evaluated at current state
        List<String> applicablePolicyIds = new ArrayList<>();
        for (String policyId : allCandidatePolicies) {
            APIMGovernancePolicy policy = policyManager.getGovernancePolicyByID(policyId, organization);
            isArtifactGovernable = APIMGovernanceUtil.isArtifactGovernable(
                    artifactRefId, artifactType, policy.getGovernableStates());
            if (isArtifactGovernable) {
                applicablePolicyIds.add(policyId);
            }
        }


        complianceManager.handleComplianceEvalAsync(artifactRefId, artifactType, applicablePolicyIds, organization);
    }

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact type ArtifactType.API
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while clearing the compliance information
     */
    @Override
    public void clearArtifactComplianceInfo(String artifactRefId, ArtifactType artifactType, String organization)
            throws APIMGovernanceException {

        complianceManager.deleteArtifact(artifactRefId, artifactType, organization);
    }

    /**
     * Get applicable rulesets for the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of artifact on APIM side)
     * @param artifactType  Artifact type (ArtifactType.API)
     * @param ruleType      Rule type (RuleType.API_DEFINITION)
     * @param ruleCategory  Rule category (RuleCategory.SPECTRAL)
     * @param organization  Organization
     * @return List of Rulesets
     * @throws APIMGovernanceException If an error occurs while getting the applicable rulesets
     */
    @Override
    public List<Ruleset> getApplicableRulesetsForArtifact(String artifactRefId, ArtifactType artifactType,
                                                          RuleType ruleType, RuleCategory ruleCategory,
                                                          String organization) throws APIMGovernanceException {

        ExtendedArtifactType extendedArtifactType = APIMGovernanceUtil
                .getExtendedArtifactTypeForArtifact(artifactRefId, artifactType);

        if (artifactRefId == null || extendedArtifactType == null) {
            return new ArrayList<>();
        }
        List<String> policies = new ArrayList<>(policyManager.getOrganizationWidePolicies(organization).keySet());

        // Retrieve labels and filter policies by labels
        List<String> labels = APIMGovernanceUtil.getLabelsForArtifact(artifactRefId, artifactType);
        for (String label : labels) {
            policies.addAll(policyManager.getPoliciesByLabel(label, organization).keySet());
        }

        // Process each policy to retrieve applicable rulesets
        Set<String> rulesetIds = new HashSet<>();
        List<Ruleset> rulesets = new ArrayList<>();

        for (String policyId : policies) {
            APIMGovernancePolicy policy = policyManager.getGovernancePolicyByID(policyId, organization);

            boolean isGovernable = APIMGovernanceUtil.isArtifactGovernable(artifactRefId, artifactType,
                    policy.getGovernableStates());

            if (isGovernable) {
                List<Ruleset> policyRulesets = policyManager.getRulesetsWithContentByPolicyId(policyId, organization);
                for (Ruleset ruleset : policyRulesets) {
                    if (!rulesetIds.contains(ruleset.getId()) && ruleset.getRuleType().equals(ruleType) &&
                            ruleset.getRuleCategory().equals(ruleCategory) &&
                            ruleset.getArtifactType().equals(extendedArtifactType)) {
                        rulesetIds.add(ruleset.getId());
                        rulesets.add(ruleset);
                    }
                }
            }
        }

        return rulesets;
    }

    /**
     * Get applicable rulesets by extended artifact type
     *
     * @param extendedArtifactType Extended artifact type (ExtendedArtifactType.REST_API)
     * @param ruleType             Rule type (RuleType.API_DEFINITION)
     * @param ruleCategory         Rule category (RuleCategory.SPECTRAL)
     * @param organization         Organization
     * @return List of Rulesets
     * @throws APIMGovernanceException If an error occurs while getting the applicable rulesets
     */
    @Override
    public List<Ruleset> getApplicableRulesetsByExtendedArtifactType(ExtendedArtifactType extendedArtifactType,
                                                                     RuleType ruleType, RuleCategory ruleCategory,
                                                                     String organization) throws
            APIMGovernanceException {

        if (extendedArtifactType == null) {
            return new ArrayList<>();
        }

        List<String> policies = new ArrayList<>(policyManager.getOrganizationWidePolicies(organization).keySet());

        // Process each policy to retrieve applicable rulesets
        Set<String> rulesetIds = new HashSet<>();
        List<Ruleset> rulesets = new ArrayList<>();
        for (String policyId : policies) {
            APIMGovernancePolicy policy = policyManager.getGovernancePolicyByID(policyId, organization);

            // If the policy can govern API creation or update
            if (policy.getGovernableStates().contains(APIMGovernableState.API_CREATE) ||
                    policy.getGovernableStates().contains(APIMGovernableState.API_UPDATE)) {

                List<Ruleset> policyRulesets = policyManager.getRulesetsWithContentByPolicyId(policyId, organization);
                for (Ruleset ruleset : policyRulesets) {
                    if (!rulesetIds.contains(ruleset.getId()) && ruleset.getRuleType().equals(ruleType) &&
                            ruleset.getRuleCategory().equals(ruleCategory) &&
                            ruleset.getArtifactType().equals(extendedArtifactType)) {
                        rulesetIds.add(ruleset.getId());
                        rulesets.add(ruleset);
                    }
                }
            }
        }

        return rulesets;
    }

}
