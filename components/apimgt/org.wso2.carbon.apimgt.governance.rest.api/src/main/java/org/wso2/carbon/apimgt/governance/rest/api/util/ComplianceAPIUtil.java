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

import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyAttachmentManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceStatusDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceWithPoliciesDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyValidationResultWithoutRulesDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RuleValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RuleValidationResultViolatedPathDTO;
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

    /**
     * Get the artifacts compliance details DTO using the Artifact Reference Id, artifact type, and organization
     *
     * @param artifactRefId Artifact Reference Id
     * @param artifactType  artifact type
     * @param organization  organization
     * @return ArtifactComplianceDetailsDTO
     * @throws APIMGovernanceException if an error occurs while getting the artifact compliance details
     */
    public static ArtifactComplianceDetailsDTO getArtifactComplianceDetailsDTO(String artifactRefId,
                                                                               ArtifactType artifactType,
                                                                               String organization)
            throws APIMGovernanceException {

        // Check if the artifact is available
        if (!APIMGovernanceUtil.isArtifactAvailable(artifactRefId, artifactType)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ARTIFACT_NOT_FOUND, artifactRefId, organization);
        }

        // Initialize the response DTO
        ArtifactComplianceDetailsDTO artifactComplianceDetailsDTO = new ArtifactComplianceDetailsDTO();

        artifactComplianceDetailsDTO.setId(artifactRefId);

        ArtifactInfoDTO infoDTO = new ArtifactInfoDTO();
        infoDTO.setName(APIMGovernanceUtil.getArtifactName(artifactRefId, artifactType));
        infoDTO.setVersion(APIMGovernanceUtil.getArtifactVersion(artifactRefId, artifactType));
        infoDTO.setType(ArtifactInfoDTO.TypeEnum.valueOf(String.valueOf(artifactType)));
        artifactComplianceDetailsDTO.setInfo(infoDTO);

        // Get all policy attachments applicable to the artifact within the organization as a map of policy ID to policy name
        Map<String, String> applicablePolicies = APIMGovernanceUtil
                .getApplicablePoliciesForArtifact(artifactRefId, artifactType, organization);

        if (applicablePolicies.isEmpty()) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return artifactComplianceDetailsDTO;
        }

        // Check if the evaluation is pending
        boolean isEvaluationPending = new ComplianceManager()
                .isEvaluationPendingForArtifact(artifactRefId, artifactType, organization);
        if (isEvaluationPending) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.PENDING);
            return artifactComplianceDetailsDTO;
        }

        // Get all policy attachments evaluated for the artifact
        List<String> evaluatedPolicies = new ComplianceManager().getEvaluatedPolicyAttachmentsForArtifact(artifactRefId,
                artifactType, organization);

        // If the artifact is not evaluated yet, set the compliance status to not applicable/pending and return
        if (evaluatedPolicies.isEmpty()) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return artifactComplianceDetailsDTO;
        }

        List<PolicyAttachmentAdherenceWithPoliciesDTO> policyAttachmentAdherenceDetails = new ArrayList<>();

        // Get policy adherence results for each policy
        for (Map.Entry<String, String> entry : applicablePolicies.entrySet()) {
            String policyId = entry.getKey();
            String policyName = entry.getValue();
            boolean isPolicyEvaluated = evaluatedPolicies.contains(policyId);
            PolicyAttachmentAdherenceWithPoliciesDTO policyAttachmentAdherence = getPolicyAttachmentAdherenceResultsDTO(policyId,
                    policyName, artifactRefId, artifactType, organization, isPolicyEvaluated);
            policyAttachmentAdherenceDetails.add(policyAttachmentAdherence);

            // If the policy is violated, set the artifact compliance status to non-compliant
            if (policyAttachmentAdherence.getStatus() == PolicyAttachmentAdherenceWithPoliciesDTO.StatusEnum.VIOLATED) {
                artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO
                        .StatusEnum.NON_COMPLIANT);
            }

        }

        artifactComplianceDetailsDTO.setGovernedPolicyAttachments(policyAttachmentAdherenceDetails);
        return artifactComplianceDetailsDTO;
    }

    /**
     * Get how policies run against an artifact adhering to a policy
     *
     * @param policyId          policy ID
     * @param policyName        policy name
     * @param artifactRefId     Artifact Reference Id
     * @param artifactType      artifact type
     * @param organization      organization
     * @param isPolicyEvaluated whether the policy has been evaluated
     * @return PolicyAttachmentAdherenceWithPoliciesDTO
     * @throws APIMGovernanceException if an error occurs while getting the policy adherence results
     */
    private static PolicyAttachmentAdherenceWithPoliciesDTO
    getPolicyAttachmentAdherenceResultsDTO(String policyId,
                                           String policyName,
                                           String artifactRefId,
                                           ArtifactType artifactType,
                                           String organization,
                                           boolean isPolicyEvaluated)
            throws APIMGovernanceException {

        PolicyAttachmentManager policyAttachmentManager = new PolicyAttachmentManager();
        ComplianceManager complianceManager = new ComplianceManager();

        PolicyAttachmentAdherenceWithPoliciesDTO policyAttachmentAdherenceWithPoliciesDTO = new PolicyAttachmentAdherenceWithPoliciesDTO();
        policyAttachmentAdherenceWithPoliciesDTO.setId(policyId);
        policyAttachmentAdherenceWithPoliciesDTO.setName(policyName);

        // If the policy has not been evaluated, set the policy adherence status to unapplied
        if (!isPolicyEvaluated) {
            policyAttachmentAdherenceWithPoliciesDTO.setStatus(PolicyAttachmentAdherenceWithPoliciesDTO.StatusEnum.UNAPPLIED);
            return policyAttachmentAdherenceWithPoliciesDTO;
        }

        // Retrieve policies tied to the policy
        List<PolicyInfo> policyRulesets = policyAttachmentManager.getPoliciesByPolicyAttachmentId(policyId, organization);

        // Retrieve the evaluated policies for the policy
        List<String> evaluatedRulesets =
                complianceManager.getEvaluatedPoliciesForArtifactAndPolicyAttachment(artifactRefId, artifactType,
                        policyRulesets, organization);

        // Store the ruleset validation results
        List<PolicyValidationResultWithoutRulesDTO> rulesetValidationResults = new ArrayList<>();

        // Get ruleset validation results for each ruleset
        for (PolicyInfo ruleset : policyRulesets) {
            boolean isRulesetEvaluated = evaluatedRulesets.contains(ruleset.getId());

            PolicyValidationResultWithoutRulesDTO resultDTO = getRulesetValidationResultsDTO(ruleset, artifactRefId,
                    artifactType, organization, isRulesetEvaluated);
            rulesetValidationResults.add(resultDTO);
        }

        // If all policies are passed, set the policy adherence status to passed
        if (rulesetValidationResults.stream().allMatch(
                rulesetValidationResultDTO -> rulesetValidationResultDTO.getStatus() ==
                        PolicyValidationResultWithoutRulesDTO.StatusEnum.PASSED)) {
            policyAttachmentAdherenceWithPoliciesDTO.setStatus(PolicyAttachmentAdherenceWithPoliciesDTO.StatusEnum.FOLLOWED);
        } else {
            policyAttachmentAdherenceWithPoliciesDTO.setStatus(PolicyAttachmentAdherenceWithPoliciesDTO.StatusEnum.VIOLATED);
        }

        policyAttachmentAdherenceWithPoliciesDTO.setPolicyValidationResults(rulesetValidationResults);

        return policyAttachmentAdherenceWithPoliciesDTO;
    }

    /**
     * Get ruleset validation results
     *
     * @param ruleset            ruleset
     * @param artifactRefId      Artifact Reference Id
     * @param artifactType       artifact type
     * @param organization       organization
     * @param isRulesetEvaluated whether the ruleset has been evaluated
     * @return PolicyValidationResultDTO
     * @throws APIMGovernanceException if an error occurs while updating the ruleset validation results
     */
    private static PolicyValidationResultWithoutRulesDTO getRulesetValidationResultsDTO(PolicyInfo ruleset, String
            artifactRefId, ArtifactType artifactType, String organization, boolean isRulesetEvaluated)
            throws APIMGovernanceException {

        ComplianceManager complianceManager = new ComplianceManager();

        PolicyValidationResultWithoutRulesDTO rulesetDTO = new PolicyValidationResultWithoutRulesDTO();
        rulesetDTO.setId(ruleset.getId());
        rulesetDTO.setName(ruleset.getName());

        // Fetch violations for the current ruleset
        List<RuleViolation> ruleViolations = complianceManager.getRuleViolations(artifactRefId, artifactType,
                ruleset.getId(), organization);

        // If the ruleset has not been evaluated, set the ruleset validation status to unapplied
        if (!isRulesetEvaluated) {
            rulesetDTO.setStatus(PolicyValidationResultWithoutRulesDTO.StatusEnum.UNAPPLIED);
            return rulesetDTO;
        }

        rulesetDTO.setRuleType(PolicyValidationResultWithoutRulesDTO
                .RuleTypeEnum.fromValue(ruleset.getPolicyType().name()));

        rulesetDTO.setStatus(ruleViolations.isEmpty() ?
                PolicyValidationResultWithoutRulesDTO.StatusEnum.PASSED :
                PolicyValidationResultWithoutRulesDTO.StatusEnum.FAILED);

        return rulesetDTO;
    }

    /**
     * Get the compliance details of all artifacts
     *
     * @param artifactType artifact type
     * @param organization organization
     * @param limit        limit
     * @param offset       offset
     * @return ArtifactComplianceListDTO
     * @throws APIMGovernanceException if an error occurs while getting the artifact compliance list
     */
    public static ArtifactComplianceListDTO getArtifactComplianceListDTO(ArtifactType artifactType,
                                                                         String organization, int limit,
                                                                         int offset) throws APIMGovernanceException {

        List<ArtifactComplianceStatusDTO> complianceStatusList = new ArrayList<>();
        int totalArtifactCount = 0;

        // Retrieve Artifacts the given organization
        if (ArtifactType.API.equals(artifactType)) {
            List<String> allAPIs = APIMUtil.getAllAPIs(organization);
            if (offset >= allAPIs.size()) {
                offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
            }
            totalArtifactCount = allAPIs.size();
            List<String> paginatedAPIIds = allAPIs.subList(offset, Math.min(offset + limit, allAPIs.size()));
            for (String apiId : paginatedAPIIds) {
                ArtifactComplianceStatusDTO complianceStatus = getArtifactComplianceStatus(apiId,
                        ArtifactType.API, organization);
                complianceStatusList.add(complianceStatus);
            }
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
        infoDTO.setName(APIMGovernanceUtil.getArtifactName(artifactRefId, artifactType));
        infoDTO.setVersion(APIMGovernanceUtil.getArtifactVersion(artifactRefId, artifactType));
        infoDTO.setType(ArtifactInfoDTO.TypeEnum.valueOf(String.valueOf(artifactType)));
        complianceStatus.setInfo(infoDTO);

        // Retrieve applicable policy attachments for the current artifact
        Map<String, String> applicablePolicies = APIMGovernanceUtil
                .getApplicablePoliciesForArtifact(artifactRefId, artifactType, organization);

        // If no policy attachments are applicable, set the compliance status to not applicable and return
        if (applicablePolicies.isEmpty()) {
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.NOT_APPLICABLE);
            return complianceStatus;
        }

        // Get evaluated policy attachments for the current artifact
        List<String> evaluatedPolicies = complianceManager.getEvaluatedPolicyAttachmentsForArtifact(artifactRefId, artifactType,
                organization);

        // If the artifact is not evaluated yet, set the compliance status to not applicable/pending and return
        if (evaluatedPolicies.isEmpty()) {
            boolean isEvaluationPending = new ComplianceManager()
                    .isEvaluationPendingForArtifact(artifactRefId, artifactType, organization);
            if (isEvaluationPending) {
                complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.PENDING);
            }
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.NOT_APPLICABLE);
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

            // Track the IDs of violated policies
            for (RuleViolation ruleViolation : ruleViolations) {
                violatedRulesets.add(ruleViolation.getPolicyId());
            }
        }

        // Identify violated policy attachments
        List<String> violatedPolicies = complianceManager
                .identifyViolatedPolicyAttachments(evaluatedPolicies, new ArrayList<>(violatedRulesets), organization);

        // Set policy adherence summary
        PolicyAttachmentAdherenceSummaryDTO policyAttachmentAdherenceSummaryDTO = new PolicyAttachmentAdherenceSummaryDTO();
        policyAttachmentAdherenceSummaryDTO.setTotal(applicablePolicies.size());
        policyAttachmentAdherenceSummaryDTO.setViolated(violatedPolicies.size());
        policyAttachmentAdherenceSummaryDTO.setFollowed(evaluatedPolicies.size() - violatedPolicies.size());
        policyAttachmentAdherenceSummaryDTO.setUnApplied(applicablePolicies.size() - evaluatedPolicies.size());

        complianceStatus.setPolicyAttachmentAdherenceSummary(policyAttachmentAdherenceSummaryDTO);
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
     * Get the ruleset validation result DTO
     *
     * @param artifactRefId   Artifact Reference Id
     * @param artifactType artifact type
     * @param rulesetId    ruleset ID
     * @param organization organization
     * @return PolicyValidationResultDTO object
     * @throws APIMGovernanceException if an error occurs while getting the ruleset validation result
     */
    public static PolicyValidationResultDTO getPolicyValidationResultDTO(String artifactRefId,
                                                                         ArtifactType artifactType,
                                                                         String rulesetId, String organization)
            throws APIMGovernanceException {

        ComplianceManager complianceManager = new ComplianceManager();
        PolicyManager policyManager = new PolicyManager();

        PolicyInfo policyInfo = policyManager.getPolicyById(rulesetId, organization);

        // If the ruleset is not found, throw an exception
        if (policyInfo == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }

        PolicyValidationResultDTO rulesetValidationResultDTO = new PolicyValidationResultDTO();
        rulesetValidationResultDTO.setId(rulesetId);
        rulesetValidationResultDTO.setName(policyInfo.getName());

        // If the ruleset has not been evaluated, set the ruleset validation status to unapplied
        boolean isRulesetEvaluatedForArtifact = complianceManager
                .isPolicyEvaluatedForArtifact(artifactRefId, artifactType, rulesetId, organization);
        if (!isRulesetEvaluatedForArtifact) {
            rulesetValidationResultDTO.setStatus(PolicyValidationResultDTO.StatusEnum.UNAPPLIED);
            return rulesetValidationResultDTO;
        }

        Set<String> violatedRuleNames = new HashSet<>();
        List<RuleValidationResultDTO> violatedRules = new ArrayList<>();
        List<RuleValidationResultDTO> followedRules = new ArrayList<>();

        // Fetch all rules within the current ruleset
        List<Rule> allRules = policyManager.getRulesByPolicyId(rulesetId, organization);
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
                PolicyValidationResultDTO.StatusEnum.PASSED :
                PolicyValidationResultDTO.StatusEnum.FAILED);

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
     * Get the artifact compliance summary
     *
     * @param artifactType artifact type
     * @param organization organization
     * @return ArtifactComplianceSummaryDTO object
     */
    public static ArtifactComplianceSummaryDTO getArtifactComplianceSummary(ArtifactType artifactType,
                                                                            String organization)
            throws APIMGovernanceException {

        ComplianceManager complianceManager = new ComplianceManager();

        // Get total number of artifacts
        int totalArtifactsCount = APIMGovernanceUtil.getAllArtifacts(artifactType, organization).size();

        // Get total number of APIs that are compliant and non-compliant
        Map<ArtifactComplianceState, List<String>> compliancyMap = complianceManager
                .getComplianceStateOfEvaluatedArtifacts(artifactType, organization);

        // Get pending artifacts
        List<String> pendingArtifacts = complianceManager.getCompliancePendingArtifacts(artifactType, organization);

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
