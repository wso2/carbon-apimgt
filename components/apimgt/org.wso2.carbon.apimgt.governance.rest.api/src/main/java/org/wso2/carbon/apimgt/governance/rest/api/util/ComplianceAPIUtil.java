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

package org.wso2.carbon.apimgt.governance.rest.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.impl.RulesetManager;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceStatusDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceWithRulesetsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RuleValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RuleValidationResultViolatedPathDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultWithoutRulesDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.SeverityBasedRuleViolationCountDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents the Results Mapping Utility
 */
public class ComplianceAPIUtil {

    private static final Log log = LogFactory.getLog(ComplianceAPIUtil.class);

    /**
     * Get the artifacts compliance details DTO using the Artifact Reference Id, artifact type,
     * username and organization
     *
     * @param artifactRefId Artifact Reference Id
     * @param artifactType  artifact type
     * @param username      username of logged in user
     * @param organization  organization
     * @return ArtifactComplianceDetailsDTO
     * @throws APIMGovernanceException if an error occurs while getting the artifact compliance details
     */
    public static ArtifactComplianceDetailsDTO getArtifactComplianceDetailsDTO(String artifactRefId,
                                                                               ArtifactType artifactType,
                                                                               String username,
                                                                               String organization)
            throws APIMGovernanceException {

        if (log.isDebugEnabled()) {
            log.debug("Getting artifact compliance details for artifact: " + artifactRefId + " of type: " + 
                    artifactType + " in organization: " + organization);
        }

        // Check if the artifact is available
        if (!APIMGovernanceUtil.isArtifactAvailable(artifactRefId, artifactType, organization)) {
            log.error("Artifact not found: " + artifactRefId + " in organization: " + organization);
            throw new APIMGovernanceException(APIMGovExceptionCodes.ARTIFACT_NOT_FOUND, artifactRefId, organization);
        }

        // Check if the artifact is visible to the user
        if (!APIMGovernanceUtil.isArtifactVisibleToUser(artifactRefId, artifactType, username, organization)) {
            log.warn("User " + username + " is not authorized to view artifact: " + artifactRefId);
            throw new APIMGovernanceException(APIMGovExceptionCodes.UNAUTHORIZED_TO_VIEW_ARTIFACT, artifactRefId,
                    organization);
        }

        // Initialize the response DTO
        ArtifactComplianceDetailsDTO artifactComplianceDetailsDTO = new ArtifactComplianceDetailsDTO();

        artifactComplianceDetailsDTO.setId(artifactRefId);

        ArtifactInfoDTO infoDTO = new ArtifactInfoDTO();
        infoDTO.setName(APIMGovernanceUtil.getArtifactName(artifactRefId, artifactType, organization));
        infoDTO.setVersion(APIMGovernanceUtil.getArtifactVersion(artifactRefId, artifactType, organization));
        infoDTO.setType(ArtifactInfoDTO.TypeEnum.valueOf(String.valueOf(artifactType)));
        infoDTO.setOwner(APIMGovernanceUtil.getArtifactOwner(artifactRefId, artifactType, organization));
        artifactComplianceDetailsDTO.setInfo(infoDTO);

        // Get all policies applicable to the artifact within the organization as a map of policy ID to policy name
        Map<String, String> applicablePolicies = APIMGovernanceUtil
                .getApplicablePoliciesForArtifact(artifactRefId, artifactType, organization);

        if (applicablePolicies.isEmpty()) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return artifactComplianceDetailsDTO;
        }

        // Get all policies evaluated for the artifact
        List<String> evaluatedPolicies = new ComplianceManager().getEvaluatedPoliciesForArtifact(artifactRefId,
                artifactType, organization);

        // Get all pending policies for the artifact
        List<String> pendingPoliciesForArtifact = new ComplianceManager()
                .getPendingPoliciesForArtifact(artifactRefId, artifactType, organization);

        // If the artifact is not evaluated and no policies are pending, set the compliance status to not applicable
        if (evaluatedPolicies.isEmpty() && pendingPoliciesForArtifact.isEmpty()) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return artifactComplianceDetailsDTO;
        }

        List<PolicyAdherenceWithRulesetsDTO> policyAdherenceDetails = new ArrayList<>();

        // Get policy adherence results for each policy
        for (Map.Entry<String, String> entry : applicablePolicies.entrySet()) {
            String policyId = entry.getKey();
            String policyName = entry.getValue();
            boolean isPolicyEvaluated = evaluatedPolicies.contains(policyId);
            boolean isPolicyPending = pendingPoliciesForArtifact.contains(policyId);
            PolicyAdherenceWithRulesetsDTO policyAdherence = getPolicyAdherenceResultsDTO(policyId,
                    policyName, artifactRefId, artifactType, organization, isPolicyEvaluated, isPolicyPending);
            policyAdherenceDetails.add(policyAdherence);
        }

        /*
         *  Set the overall compliance status for the artifact
         *  If all policies are un-applied, set the status to not applicable.
         *  If any policy is pending, set the status to pending.
         *  If any policy is violated, set the status to non-compliant.
         *  Otherwise, set the status to compliant
         */

        ArtifactComplianceDetailsDTO.StatusEnum status;
        if (policyAdherenceDetails.stream().allMatch(dto -> dto.getStatus()
                == PolicyAdherenceWithRulesetsDTO.StatusEnum.UNAPPLIED)) {
            status = ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE;
        } else if (policyAdherenceDetails.stream().anyMatch(dto -> dto.getStatus()
                == PolicyAdherenceWithRulesetsDTO.StatusEnum.PENDING)) {
            status = ArtifactComplianceDetailsDTO.StatusEnum.PENDING;
        } else if (policyAdherenceDetails.stream().anyMatch(dto -> dto.getStatus()
                == PolicyAdherenceWithRulesetsDTO.StatusEnum.VIOLATED)) {
            status = ArtifactComplianceDetailsDTO.StatusEnum.NON_COMPLIANT;
        } else if (policyAdherenceDetails.stream().anyMatch(dto -> dto.getStatus()
                == PolicyAdherenceWithRulesetsDTO.StatusEnum.VIOLATED)) {
            status = ArtifactComplianceDetailsDTO.StatusEnum.NON_COMPLIANT;
        } else {
            status = ArtifactComplianceDetailsDTO.StatusEnum.COMPLIANT;
        }

        artifactComplianceDetailsDTO.setStatus(status);


        artifactComplianceDetailsDTO.setGovernedPolicies(policyAdherenceDetails);
        return artifactComplianceDetailsDTO;
    }

    /**
     * Get how rulesets run against an artifact adhering to a policy
     *
     * @param policyId          policy ID
     * @param policyName        policy name
     * @param artifactRefId     Artifact Reference Id
     * @param artifactType      artifact type
     * @param organization      organization
     * @param isPolicyEvaluated whether the policy has been evaluated
     * @param isPolicyPending   whether the policy evaluation is pending
     * @return PolicyAdherenceWithRulesetsDTO
     * @throws APIMGovernanceException if an error occurs while getting the policy adherence results
     */
    private static PolicyAdherenceWithRulesetsDTO getPolicyAdherenceResultsDTO(String policyId, String policyName,
                                                                               String artifactRefId,
                                                                               ArtifactType artifactType,
                                                                               String organization,
                                                                               boolean isPolicyEvaluated,
                                                                               boolean isPolicyPending)
            throws APIMGovernanceException {

        PolicyManager policyManager = new PolicyManager();
        ComplianceManager complianceManager = new ComplianceManager();

        PolicyAdherenceWithRulesetsDTO policyAdherenceWithRulesetsDTO = new PolicyAdherenceWithRulesetsDTO();
        policyAdherenceWithRulesetsDTO.setId(policyId);
        policyAdherenceWithRulesetsDTO.setName(policyName);

        // If the policy evaluation is pending, set the policy adherence status to pending
        if (isPolicyPending) {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.PENDING);
            return policyAdherenceWithRulesetsDTO;
        }

        // If the policy has not been evaluated, set the policy adherence status to unapplied
        if (!isPolicyEvaluated) {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.UNAPPLIED);
            return policyAdherenceWithRulesetsDTO;
        }

        // Retrieve rulesets tied to the policy
        List<RulesetInfo> policyRulesets = policyManager.getRulesetsByPolicyId(policyId, organization);

        // Retrieve the evaluated rulesets for the policy
        List<String> evaluatedRulesets =
                complianceManager.getEvaluatedRulesetsForArtifactAndPolicy(artifactRefId, artifactType,
                        policyRulesets, organization);

        // Store the ruleset validation results
        List<RulesetValidationResultWithoutRulesDTO> rulesetValidationResults = new ArrayList<>();

        // Get ruleset validation results for each ruleset
        for (RulesetInfo ruleset : policyRulesets) {
            boolean isRulesetEvaluated = evaluatedRulesets.contains(ruleset.getId());

            RulesetValidationResultWithoutRulesDTO resultDTO = getRulesetValidationResultsDTO(ruleset, artifactRefId,
                    artifactType, organization, isRulesetEvaluated);
            rulesetValidationResults.add(resultDTO);
        }

        /* If one of the rulesets is violated, set the policy adherence status to violated
         * If all rulesets are un applied policy adherence status to unapplied
         *
         * Else policy adherence status to followed
         * passed, set the policy adherence status to followed
         */
        if (rulesetValidationResults.stream().anyMatch(dto -> dto.getStatus()
                == RulesetValidationResultWithoutRulesDTO.StatusEnum.FAILED)) {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.VIOLATED);
        } else if (rulesetValidationResults.stream().allMatch(dto -> dto.getStatus()
                == RulesetValidationResultWithoutRulesDTO.StatusEnum.UNAPPLIED)) {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.UNAPPLIED);
        } else {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.FOLLOWED);
        }

        policyAdherenceWithRulesetsDTO.setRulesetValidationResults(rulesetValidationResults);

        return policyAdherenceWithRulesetsDTO;
    }

    /**
     * Get ruleset validation results
     *
     * @param ruleset            ruleset
     * @param artifactRefId      Artifact Reference Id
     * @param artifactType       artifact type
     * @param organization       organization
     * @param isRulesetEvaluated whether the ruleset has been evaluated
     * @return RulesetValidationResultDTO
     * @throws APIMGovernanceException if an error occurs while updating the ruleset validation results
     */
    private static RulesetValidationResultWithoutRulesDTO getRulesetValidationResultsDTO(RulesetInfo ruleset, String
            artifactRefId, ArtifactType artifactType, String organization, boolean isRulesetEvaluated)
            throws APIMGovernanceException {

        ComplianceManager complianceManager = new ComplianceManager();

        RulesetValidationResultWithoutRulesDTO rulesetDTO = new RulesetValidationResultWithoutRulesDTO();
        rulesetDTO.setId(ruleset.getId());
        rulesetDTO.setName(ruleset.getName());
        rulesetDTO.setRuleType(RulesetValidationResultWithoutRulesDTO
                .RuleTypeEnum.fromValue(ruleset.getRuleType().name()));

        // If the ruleset has not been evaluated, set the ruleset validation status to unapplied
        if (!isRulesetEvaluated) {
            rulesetDTO.setStatus(RulesetValidationResultWithoutRulesDTO.StatusEnum.UNAPPLIED);
            return rulesetDTO;
        }

        // Fetch violations for the current ruleset
        List<RuleViolation> ruleViolations = complianceManager.getRuleViolations(artifactRefId, artifactType,
                ruleset.getId(), organization);


        rulesetDTO.setStatus(ruleViolations.isEmpty() ?
                RulesetValidationResultWithoutRulesDTO.StatusEnum.PASSED :
                RulesetValidationResultWithoutRulesDTO.StatusEnum.FAILED);

        return rulesetDTO;
    }

    /**
     * Get the compliance details of all artifacts visible to the user of the given organization
     *
     * @param artifactType artifact type
     * @param username     username of logged in user
     * @param organization organization
     * @param limit        limit
     * @param offset       offset
     * @return ArtifactComplianceListDTO
     * @throws APIMGovernanceException if an error occurs while getting the artifact compliance list
     */
    public static ArtifactComplianceListDTO getArtifactComplianceListDTO(ArtifactType artifactType, String username,
                                                                         String organization, int limit,
                                                                         int offset) throws APIMGovernanceException {

        List<ArtifactComplianceStatusDTO> complianceStatusList = new ArrayList<>();

        // Retrieve Artifacts the given organization
        List<String> allArtifacts = APIMGovernanceUtil.getAllArtifacts(artifactType, username, organization);
        int totalArtifactCount = allArtifacts.size();

        if (offset >= allArtifacts.size()) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        List<String> paginatedArtifactIds = allArtifacts.subList(offset,
                Math.min(offset + limit, allArtifacts.size()));

        for (String artifactId : paginatedArtifactIds) {
            ArtifactComplianceStatusDTO complianceStatus = getArtifactComplianceStatus(artifactId,
                    artifactType, organization);
            complianceStatusList.add(complianceStatus);
        }

        ArtifactComplianceListDTO complianceListDTO = new ArtifactComplianceListDTO();
        complianceListDTO.setList(complianceStatusList);
        complianceListDTO.setCount(complianceStatusList.size());

        // Set pagination details for the artifact compliance list
        setPaginationDetailsForArtifactCompliance(complianceListDTO, limit, offset,
                totalArtifactCount, String.valueOf(artifactType));

        return complianceListDTO;

    }

    /**
     * Get artifact compliance status for the artifact
     *
     * @param artifactRefId   Artifact Reference Id
     * @param artifactType artifact type
     * @param organization organization
     * @return ArtifactComplianceStatusDTO
     * @throws APIMGovernanceException if an error occurs while getting the artifact compliance status
     */
    private static ArtifactComplianceStatusDTO getArtifactComplianceStatus(String artifactRefId,
                                                                           ArtifactType artifactType,
                                                                           String organization)
            throws APIMGovernanceException {

        ComplianceManager complianceManager = new ComplianceManager();

        // Create a new DTO to store compliance status for the current API
        ArtifactComplianceStatusDTO complianceStatus = new ArtifactComplianceStatusDTO();

        complianceStatus.setId(artifactRefId);

        ArtifactInfoDTO infoDTO = new ArtifactInfoDTO();
        infoDTO.setName(APIMGovernanceUtil.getArtifactName(artifactRefId, artifactType, organization));
        infoDTO.setVersion(APIMGovernanceUtil.getArtifactVersion(artifactRefId, artifactType, organization));
        infoDTO.setType(ArtifactInfoDTO.TypeEnum.valueOf(String.valueOf(artifactType)));
        infoDTO.setOwner(APIMGovernanceUtil.getArtifactOwner(artifactRefId, artifactType, organization));
        complianceStatus.setInfo(infoDTO);

        // Retrieve applicable policies for the current artifact
        Map<String, String> applicablePolicies = APIMGovernanceUtil
                .getApplicablePoliciesForArtifact(artifactRefId, artifactType, organization);

        // If no policies are applicable, set the compliance status to not applicable and return
        if (applicablePolicies.isEmpty()) {
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.NOT_APPLICABLE);
            return complianceStatus;
        }

        // Get evaluated policies for the current artifact
        List<String> evaluatedPolicies = complianceManager.getEvaluatedPoliciesForArtifact(artifactRefId,
                artifactType,
                organization);

        // Get pending policies for the current artifact
        List<String> pendingPoliciesForArtifact = complianceManager
                .getPendingPoliciesForArtifact(artifactRefId, artifactType, organization);

        // If the artifact is not evaluated yet and no policies are pending, set the compliance
        // status to not applicable
        if (evaluatedPolicies.isEmpty() && pendingPoliciesForArtifact.isEmpty()) {
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.NOT_APPLICABLE);
            return complianceStatus;
        } else if (!pendingPoliciesForArtifact.isEmpty()) {
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.PENDING);
            return complianceStatus;
        }

        // Track violated ruleset IDs for the current artifact
        Set<String> violatedRulesets = new HashSet<>();

        // Retrieve rule violations categorized by severity for the current artifact
        Map<RuleSeverity, List<RuleViolation>> ruleViolationsBySeverity = complianceManager
                .getSeverityBasedRuleViolationsForArtifact(artifactRefId, artifactType, organization);

        List<SeverityBasedRuleViolationCountDTO> ruleViolationCounts = new ArrayList<>();

        // Process each severity level and its associated rule violations
        for (Map.Entry<RuleSeverity, List<RuleViolation>> entry : ruleViolationsBySeverity.entrySet()) {
            RuleSeverity severity = entry.getKey();
            List<RuleViolation> ruleViolations = entry.getValue();

            // Create a DTO to store the count of violations for the current severity
            SeverityBasedRuleViolationCountDTO violationCountDTO = new SeverityBasedRuleViolationCountDTO();

            violationCountDTO.setSeverity(SeverityBasedRuleViolationCountDTO
                    .SeverityEnum.fromValue(String.valueOf(severity)));
            violationCountDTO.setViolatedRulesCount(ruleViolations.size());

            ruleViolationCounts.add(violationCountDTO);

            // Track the IDs of violated rulesets
            for (RuleViolation ruleViolation : ruleViolations) {
                violatedRulesets.add(ruleViolation.getRulesetId());
            }
        }

        // Identify violated policies
        List<String> violatedPolicies = complianceManager
                .identifyViolatedPolicies(evaluatedPolicies, new ArrayList<>(violatedRulesets), organization);

        // Set policy adherence summary
        PolicyAdherenceSummaryDTO policyAdherenceSummaryDTO = new PolicyAdherenceSummaryDTO();
        policyAdherenceSummaryDTO.setTotal(applicablePolicies.size());
        policyAdherenceSummaryDTO.setViolated(violatedPolicies.size());
        policyAdherenceSummaryDTO.setFollowed(evaluatedPolicies.size() - violatedPolicies.size());
        policyAdherenceSummaryDTO.setUnApplied(applicablePolicies.size() - evaluatedPolicies.size());

        complianceStatus.setPolicyAdherenceSummary(policyAdherenceSummaryDTO);
        complianceStatus.setSeverityBasedRuleViolationSummary(ruleViolationCounts);
        complianceStatus.setStatus(violatedPolicies.isEmpty() ?
                ArtifactComplianceStatusDTO.StatusEnum.COMPLIANT :
                ArtifactComplianceStatusDTO.StatusEnum.NON_COMPLIANT);

        return complianceStatus;
    }

    /**
     * Set pagination details for the artifact compliance list
     *
     * @param complianceListDTO ArtifactComplianceListDTO object
     * @param limit             max number of objects returned
     * @param offset            starting index
     * @param size              total number of objects
     * @param artifactType      artifact type
     */
    private static void setPaginationDetailsForArtifactCompliance(ArtifactComplianceListDTO complianceListDTO,
                                                                  int limit, int offset,
                                                                  int size, String artifactType) {

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(size);

        // Set previous and next URLs for pagination
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = APIMGovernanceAPIUtil.getArtifactCompliancePageURL(
                    APIMGovernanceAPIConstants.ARTIFACT_COMPLIANCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), artifactType);
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = APIMGovernanceAPIUtil.getArtifactCompliancePageURL(APIMGovernanceAPIConstants
                            .ARTIFACT_COMPLIANCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), artifactType);
        }
        paginationDTO.setNext(paginatedNext);
        paginationDTO.setPrevious(paginatedPrevious);

        complianceListDTO.setPagination(paginationDTO);
    }

    /**
     * Get the ruleset validation result DTO using the Artifact Reference Id, artifact type, ruleset ID,
     * username and organization
     *
     * @param artifactRefId Artifact Reference Id
     * @param artifactType  artifact type
     * @param rulesetId     ruleset ID
     * @param username      username of logged in user
     * @param organization  organization
     * @return RulesetValidationResultDTO object
     * @throws APIMGovernanceException if an error occurs while getting the ruleset validation result
     */
    public static RulesetValidationResultDTO getRulesetValidationResultDTO(String artifactRefId,
                                                                           ArtifactType artifactType,
                                                                           String rulesetId,
                                                                           String username,
                                                                           String organization)
            throws APIMGovernanceException {

        ComplianceManager complianceManager = new ComplianceManager();
        RulesetManager rulesetManager = new RulesetManager();

        RulesetInfo rulesetInfo = rulesetManager.getRulesetById(rulesetId, organization);

        // If the ruleset is not found, throw an exception
        if (rulesetInfo == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }

        // Check if the artifact is visible to the user
        if (!APIMGovernanceUtil.isArtifactVisibleToUser(artifactRefId, artifactType, username, organization)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.UNAUTHORIZED_TO_VIEW_ARTIFACT, artifactRefId,
                    organization);
        }

        RulesetValidationResultDTO rulesetValidationResultDTO = new RulesetValidationResultDTO();
        rulesetValidationResultDTO.setId(rulesetId);
        rulesetValidationResultDTO.setName(rulesetInfo.getName());

        // If the ruleset has not been evaluated, set the ruleset validation status to unapplied
        boolean isRulesetEvaluatedForArtifact = complianceManager
                .isRulesetEvaluatedForArtifact(artifactRefId, artifactType, rulesetId, organization);
        if (!isRulesetEvaluatedForArtifact) {
            rulesetValidationResultDTO.setStatus(RulesetValidationResultDTO.StatusEnum.UNAPPLIED);
            return rulesetValidationResultDTO;
        }

        Set<String> violatedRuleNames = new HashSet<>();
        List<RuleValidationResultDTO> violatedRules = new ArrayList<>();
        List<RuleValidationResultDTO> followedRules = new ArrayList<>();

        // Fetch all rules within the current ruleset
        List<Rule> allRules = rulesetManager.getRulesByRulesetId(rulesetId, organization);
        Map<String, Rule> rulesMap = allRules.stream()
                .collect(Collectors.toMap(Rule::getName, rule -> rule));

        // Fetch violations for the current ruleset
        List<RuleViolation> ruleViolations = complianceManager.getRuleViolations(artifactRefId, artifactType,
                rulesetId, organization);

        // IMPORTANT: NOTE THAT THERE CAN BE MULTIPLE VIOLATIONS WITH SAME CODE BUT DIFFERENT PATH
        for (RuleViolation ruleViolation : ruleViolations) {
            Rule rule = rulesMap.get(ruleViolation.getRuleName());
            violatedRules.add(ComplianceAPIUtil.getRuleValidationResultDTO(rule, ruleViolation));
            violatedRuleNames.add(rule.getName());
        }

        for (Rule rule : allRules) {
            if (!violatedRuleNames.contains(rule.getName())) {
                followedRules.add(ComplianceAPIUtil.getRuleValidationResultDTO(rule, null));
            }
        }

        rulesetValidationResultDTO.setViolatedRules(violatedRules);
        rulesetValidationResultDTO.setFollowedRules(followedRules);
        rulesetValidationResultDTO.setStatus(violatedRules.isEmpty() ?
                RulesetValidationResultDTO.StatusEnum.PASSED :
                RulesetValidationResultDTO.StatusEnum.FAILED);

        return rulesetValidationResultDTO;
    }

    /**
     * Converts a RuleViolations to a RuleValidationResultDTO object
     *
     * @param rule          Rule object
     * @param ruleViolation RuleViolation object
     * @return RuleValidationResultDTO object
     */
    private static RuleValidationResultDTO getRuleValidationResultDTO(Rule rule, RuleViolation ruleViolation) {

        RuleValidationResultDTO ruleValidationResultDTO = new RuleValidationResultDTO();
        ruleValidationResultDTO.setId(rule.getId());
        ruleValidationResultDTO.setName(rule.getName());
        ruleValidationResultDTO.setDescription(rule.getDescription());
        if (ruleViolation != null) {
            ruleValidationResultDTO.setMessage(ruleViolation.getRuleMessage());
            ruleValidationResultDTO.setStatus(RuleValidationResultDTO.StatusEnum.FAILED);
            ruleValidationResultDTO.setSeverity(RuleValidationResultDTO.SeverityEnum.valueOf(
                    String.valueOf(rule.getSeverity())));
            RuleValidationResultViolatedPathDTO violatedPathDTO = new RuleValidationResultViolatedPathDTO();
            violatedPathDTO.setPath(ruleViolation.getViolatedPath());
            ruleValidationResultDTO.setViolatedPath(violatedPathDTO);
        } else {
            ruleValidationResultDTO.setStatus(RuleValidationResultDTO.StatusEnum.PASSED);
        }

        return ruleValidationResultDTO;
    }

    /**
     * Get the artifact compliance summary of all artifacts visible to the user of the given organization
     *
     * @param artifactType artifact type
     * @param username     username of logged-in user
     * @param organization organization
     * @return ArtifactComplianceSummaryDTO object
     */
    public static ArtifactComplianceSummaryDTO getArtifactComplianceSummary(ArtifactType artifactType,
                                                                            String username,
                                                                            String organization)
            throws APIMGovernanceException {

        ComplianceManager complianceManager = new ComplianceManager();

        // Get all artifacts visible to the user
        List<String> allVisibleArtifacts = APIMGovernanceUtil.getAllArtifacts(artifactType, username, organization);
        int totalArtifactsCount = allVisibleArtifacts.size();

        // Get total number of APIs that are compliant and non-compliant
        Map<ArtifactComplianceState, List<String>> compliancyMap = complianceManager
                .getComplianceStateOfEvaluatedArtifacts(artifactType, organization);

        // Get pending artifacts
        List<String> pendingArtifacts = complianceManager.getCompliancePendingArtifacts(artifactType, organization);

        // Filter out the pending artifacts from compliant and non-compliant artifacts and keep only ids of the
        // visible artifacts
        compliancyMap.get(ArtifactComplianceState.COMPLIANT).retainAll(allVisibleArtifacts);
        compliancyMap.get(ArtifactComplianceState.NON_COMPLIANT).retainAll(allVisibleArtifacts);
        pendingArtifacts.retainAll(allVisibleArtifacts);

        // Filter out the pending artifacts from compliant and non-compliant artifacts
        compliancyMap.get(ArtifactComplianceState.COMPLIANT).removeAll(pendingArtifacts);
        compliancyMap.get(ArtifactComplianceState.NON_COMPLIANT).removeAll(pendingArtifacts);

        int pendingArtifactCount = pendingArtifacts.size();
        int compliantArtifactCount = compliancyMap.get(ArtifactComplianceState.COMPLIANT).size();
        int nonCompliantArtifactCount = compliancyMap.get(ArtifactComplianceState.NON_COMPLIANT).size();

        ArtifactComplianceSummaryDTO summaryDTO = new ArtifactComplianceSummaryDTO();
        summaryDTO.setTotal(totalArtifactsCount);
        summaryDTO.setCompliant(compliantArtifactCount);
        summaryDTO.setNonCompliant(nonCompliantArtifactCount);
        summaryDTO.setPending(pendingArtifactCount);
        summaryDTO.setNotApplicable(totalArtifactsCount - (compliantArtifactCount + nonCompliantArtifactCount +
                pendingArtifactCount));
        return summaryDTO;
    }

}
