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

import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.GovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.PolicyManager;
import org.wso2.carbon.apimgt.governance.api.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.Severity;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.PolicyManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.RulesetManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;
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

    /**
     * Get the artifacts compliance details DTO using the artifact ID, artifact type, and organization
     *
     * @param artifactId   artifact ID
     * @param artifactType artifact type
     * @param organization organization
     * @return ArtifactComplianceDetailsDTO
     * @throws GovernanceException if an error occurs while getting the artifact compliance details
     */
    public static ArtifactComplianceDetailsDTO getArtifactComplianceDetailsDTO(String artifactId,
                                                                               ArtifactType artifactType,
                                                                               String organization)
            throws GovernanceException {

        // Check if the artifact is available
        if (!GovernanceUtil.isArtifactAvailable(artifactId, artifactType)) {
            throw new GovernanceException(GovernanceExceptionCodes.ARTIFACT_NOT_FOUND, artifactId, organization);
        }

        // Initialize the response DTO
        ArtifactComplianceDetailsDTO artifactComplianceDetailsDTO = new ArtifactComplianceDetailsDTO();

        artifactComplianceDetailsDTO.setId(artifactId);

        ArtifactInfoDTO infoDTO = new ArtifactInfoDTO();
        infoDTO.setName(GovernanceUtil.getArtifactName(artifactId, artifactType));
        infoDTO.setVersion(GovernanceUtil.getArtifactVersion(artifactId, artifactType));
        infoDTO.setType(ArtifactInfoDTO.TypeEnum.valueOf(String.valueOf(artifactType)));
        artifactComplianceDetailsDTO.setInfo(infoDTO);

        // Get all policies applicable to the artifact within the organization as a map of policy ID to policy name
        Map<String, String> applicablePolicies = GovernanceUtil
                .getApplicablePoliciesForArtifact(artifactId, artifactType, organization);

        if (applicablePolicies.isEmpty()) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return artifactComplianceDetailsDTO;
        }

        // Get all policies evaluated for the artifact
        List<String> evaluatedPolicies = new ComplianceManagerImpl().getEvaluatedPoliciesForArtifact(artifactId,
                artifactType, organization);

        // If the artifact is not evaluated yet, set the compliance status to not applicable and return
        if (evaluatedPolicies.isEmpty()) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return artifactComplianceDetailsDTO;
        }

        List<PolicyAdherenceWithRulesetsDTO> policyAdherenceDetails = new ArrayList<>();

        // Get policy adherence results for each policy
        for (Map.Entry<String, String> entry : applicablePolicies.entrySet()) {
            String policyId = entry.getKey();
            String policyName = entry.getValue();
            boolean isPolicyEvaluated = evaluatedPolicies.contains(policyId);
            PolicyAdherenceWithRulesetsDTO policyAdherence = getPolicyAdherenceResultsDTO(policyId,
                    policyName, artifactId, artifactType, organization, isPolicyEvaluated);
            policyAdherenceDetails.add(policyAdherence);

            // If the policy is violated, set the artifact compliance status to non-compliant
            if (policyAdherence.getStatus() == PolicyAdherenceWithRulesetsDTO.StatusEnum.VIOLATED) {
                artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO
                        .StatusEnum.NON_COMPLIANT);
            }

        }

        artifactComplianceDetailsDTO.setGovernedPolicies(policyAdherenceDetails);
        return artifactComplianceDetailsDTO;
    }

    /**
     * Get how rulesets run against an artifact adhering to a policy
     *
     * @param policyId          policy ID
     * @param policyName        policy name
     * @param artifactId        artifact ID
     * @param artifactType      artifact type
     * @param organization      organization
     * @param isPolicyEvaluated whether the policy has been evaluated
     * @return PolicyAdherenceWithRulesetsDTO
     * @throws GovernanceException if an error occurs while getting the policy adherence results
     */
    private static PolicyAdherenceWithRulesetsDTO getPolicyAdherenceResultsDTO(String policyId, String policyName,
                                                                               String artifactId,
                                                                               ArtifactType artifactType,
                                                                               String organization,
                                                                               boolean isPolicyEvaluated)
            throws GovernanceException {

        PolicyManager policyManager = new PolicyManagerImpl();
        ComplianceManager complianceManager = new ComplianceManagerImpl();

        PolicyAdherenceWithRulesetsDTO policyAdherenceWithRulesetsDTO = new PolicyAdherenceWithRulesetsDTO();
        policyAdherenceWithRulesetsDTO.setId(policyId);
        policyAdherenceWithRulesetsDTO.setName(policyName);

        // If the policy has not been evaluated, set the policy adherence status to unapplied
        if (!isPolicyEvaluated) {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.UNAPPLIED);
            return policyAdherenceWithRulesetsDTO;
        }

        // Retrieve rulesets tied to the policy
        List<Ruleset> applicableRulesets = policyManager.getRulesetsByPolicyId(policyId);

        // Retrieve the evaluated rulesets for the policy
        List<String> evaluatedRulesets =
                complianceManager.getEvaluatedRulesetsForArtifactAndPolicy(artifactId, artifactType,
                        policyId, organization);

        // Store the ruleset validation results
        List<RulesetValidationResultWithoutRulesDTO> rulesetValidationResults = new ArrayList<>();

        // Get ruleset validation results for each ruleset
        for (Ruleset ruleset : applicableRulesets) {
            boolean isRulesetEvaluated = evaluatedRulesets.contains(ruleset.getId());

            RulesetValidationResultWithoutRulesDTO resultDTO = getRulesetValidationResultsDTO(ruleset, artifactId,
                    artifactType, organization, policyId, isRulesetEvaluated);
            rulesetValidationResults.add(resultDTO);
        }

        // If all rulesets are passed, set the policy adherence status to passed
        if (rulesetValidationResults.stream().allMatch(
                rulesetValidationResultDTO -> rulesetValidationResultDTO.getStatus() ==
                        RulesetValidationResultWithoutRulesDTO.StatusEnum.PASSED)) {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.FOLLOWED);
        } else {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.VIOLATED);
        }

        policyAdherenceWithRulesetsDTO.setRulesetValidationResults(rulesetValidationResults);

        return policyAdherenceWithRulesetsDTO;
    }

    /**
     * Get ruleset validation results
     *
     * @param ruleset            ruleset
     * @param artifactId         artifact ID
     * @param artifactType       artifact type
     * @param organization       organization
     * @param policyId           policy ID
     * @param isRulesetEvaluated whether the ruleset has been evaluated
     * @return RulesetValidationResultDTO
     * @throws GovernanceException if an error occurs while updating the ruleset validation results
     */
    private static RulesetValidationResultWithoutRulesDTO getRulesetValidationResultsDTO(Ruleset ruleset, String
            artifactId, ArtifactType artifactType, String organization, String policyId, boolean isRulesetEvaluated)
            throws GovernanceException {

        ComplianceManager complianceManager = new ComplianceManagerImpl();

        RulesetValidationResultWithoutRulesDTO rulesetDTO = new RulesetValidationResultWithoutRulesDTO();
        rulesetDTO.setId(ruleset.getId());
        rulesetDTO.setName(ruleset.getName());

        // Fetch violations for the current ruleset
        List<RuleViolation> ruleViolations = complianceManager.getRuleViolations(artifactId, artifactType, policyId,
                ruleset.getId(), organization);

        // If the ruleset has not been evaluated, set the ruleset validation status to unapplied
        if (!isRulesetEvaluated) {
            rulesetDTO.setStatus(RulesetValidationResultWithoutRulesDTO.StatusEnum.UNAPPLIED);
            return rulesetDTO;
        }

        rulesetDTO.setRuleType(RulesetValidationResultWithoutRulesDTO
                .RuleTypeEnum.fromValue(ruleset.getRuleType().name()));

        rulesetDTO.setStatus(ruleViolations.isEmpty() ?
                RulesetValidationResultWithoutRulesDTO.StatusEnum.PASSED :
                RulesetValidationResultWithoutRulesDTO.StatusEnum.FAILED);

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
     * @throws GovernanceException if an error occurs while getting the artifact compliance list
     */
    public static ArtifactComplianceListDTO getArtifactComplianceListDTO(ArtifactType artifactType,
                                                                         String organization, int limit,
                                                                         int offset) throws GovernanceException {

        List<ArtifactComplianceStatusDTO> complianceStatusList = new ArrayList<>();
        int totalArtifactCount = 0;

        // Retrieve Artifacts the given organization
        if (ArtifactType.API.equals(artifactType)) {
            List<String> allAPIs = APIMUtil.getAllAPIs(organization);
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
     * @param artifactId   artifact ID
     * @param artifactType artifact type
     * @param organization organization
     * @return ArtifactComplianceStatusDTO
     * @throws GovernanceException if an error occurs while getting the artifact compliance status
     */
    private static ArtifactComplianceStatusDTO getArtifactComplianceStatus(String artifactId, ArtifactType artifactType,
                                                                           String organization)
            throws GovernanceException {

        ComplianceManager complianceManager = new ComplianceManagerImpl();

        // Create a new DTO to store compliance status for the current API
        ArtifactComplianceStatusDTO complianceStatus = new ArtifactComplianceStatusDTO();

        complianceStatus.setId(artifactId);

        ArtifactInfoDTO infoDTO = new ArtifactInfoDTO();
        infoDTO.setName(GovernanceUtil.getArtifactName(artifactId, artifactType));
        infoDTO.setVersion(GovernanceUtil.getArtifactVersion(artifactId, artifactType));
        infoDTO.setType(ArtifactInfoDTO.TypeEnum.valueOf(String.valueOf(artifactType)));
        complianceStatus.setInfo(infoDTO);

        // Retrieve applicable policies for the current artifact
        Map<String, String> applicablePolicies = GovernanceUtil
                .getApplicablePoliciesForArtifact(artifactId, artifactType, organization);

        // If no policies are applicable, set the compliance status to not applicable and return
        if (applicablePolicies.isEmpty()) {
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.NOT_APPLICABLE);
            return complianceStatus;
        }

        // Get evaluated policies for the current artifact
        List<String> evaluatedPolicies = complianceManager.getEvaluatedPoliciesForArtifact(artifactId, artifactType,
                organization);

        // If the artifact is not evaluated yet, set the compliance status to not applicable and return
        if (evaluatedPolicies.isEmpty()) {
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.NOT_APPLICABLE);
            return complianceStatus;
        }

        // Track violated policy IDs for the current artifact
        Set<String> violatedPolicies = new HashSet<>();

        // Retrieve rule violations categorized by severity for the current artifact
        Map<Severity, List<RuleViolation>> ruleViolationsBySeverity = complianceManager
                .getSeverityBasedRuleViolationsForArtifact(artifactId, artifactType, organization);

        List<SeverityBasedRuleViolationCountDTO> ruleViolationCounts = new ArrayList<>();

        // Process each severity level and its associated rule violations
        for (Map.Entry<Severity, List<RuleViolation>> entry : ruleViolationsBySeverity.entrySet()) {
            Severity severity = entry.getKey();
            List<RuleViolation> ruleViolations = entry.getValue();

            // Create a DTO to store the count of violations for the current severity
            SeverityBasedRuleViolationCountDTO violationCountDTO = new SeverityBasedRuleViolationCountDTO();

            violationCountDTO.setSeverity(SeverityBasedRuleViolationCountDTO
                    .SeverityEnum.fromValue(String.valueOf(severity)));
            violationCountDTO.setViolatedRulesCount(ruleViolations.size());

            ruleViolationCounts.add(violationCountDTO);

            // Track the IDs of violated policies
            for (RuleViolation ruleViolation : ruleViolations) {
                violatedPolicies.add(ruleViolation.getPolicyId());
            }
        }

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
            paginatedPrevious = GovernanceAPIUtil.getArtifactCompliancePageURL(
                    GovernanceAPIConstants.ARTIFACT_COMPLIANCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), artifactType);
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = GovernanceAPIUtil.getArtifactCompliancePageURL(GovernanceAPIConstants
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
     * @param artifactId   artifact ID
     * @param artifactType artifact type
     * @param rulesetId    ruleset ID
     * @param organization organization
     * @return RulesetValidationResultDTO object
     * @throws GovernanceException if an error occurs while getting the ruleset validation result
     */
    public static RulesetValidationResultDTO getRulesetValidationResultDTO(String artifactId,
                                                                           ArtifactType artifactType,
                                                                           String rulesetId, String organization)
            throws GovernanceException {

        ComplianceManager complianceManager = new ComplianceManagerImpl();
        RulesetManager rulesetManager = new RulesetManagerImpl();

        RulesetInfo rulesetInfo = rulesetManager.getRulesetById(rulesetId);

        // If the ruleset is not found, throw an exception
        if (rulesetInfo == null) {
            throw new GovernanceException(GovernanceExceptionCodes.RULESET_NOT_FOUND, rulesetId);
        }

        RulesetValidationResultDTO rulesetValidationResultDTO = new RulesetValidationResultDTO();
        rulesetValidationResultDTO.setId(rulesetId);
        rulesetValidationResultDTO.setName(rulesetInfo.getName());

        // If the ruleset has not been evaluated, set the ruleset validation status to unapplied
        boolean isRulesetEvaluatedForArtifact = complianceManager
                .isRulesetEvaluatedForArtifact(artifactId, artifactType, rulesetId, organization);
        if (!isRulesetEvaluatedForArtifact) {
            rulesetValidationResultDTO.setStatus(RulesetValidationResultDTO.StatusEnum.UNAPPLIED);
            return rulesetValidationResultDTO;
        }

        Set<String> violatedRuleCodes = new HashSet<>();
        List<RuleValidationResultDTO> violatedRules = new ArrayList<>();
        List<RuleValidationResultDTO> followedRules = new ArrayList<>();

        // Fetch all rules within the current ruleset
        List<Rule> allRules = rulesetManager.getRules(rulesetId);
        Map<String, Rule> rulesMap = allRules.stream()
                .collect(Collectors.toMap(Rule::getName, rule -> rule));

        // Fetch violations for the current ruleset
        List<RuleViolation> ruleViolations = complianceManager.getRuleViolations(artifactId, artifactType,
                rulesetId, organization);

        // IMPORTANT: NOTE THAT THERE CAN BE MULTIPLE VIOLATIONS WITH SAME CODE BUT DIFFERENT PATH
        for (RuleViolation ruleViolation : ruleViolations) {
            Rule rule = rulesMap.get(ruleViolation.getRuleName());
            violatedRules.add(ComplianceAPIUtil.getRuleValidationResultDTO(rule, ruleViolation));
            violatedRuleCodes.add(rule.getName());
        }

        for (Rule rule : allRules) {
            if (!violatedRuleCodes.contains(rule.getName())) {
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
            ruleValidationResultDTO.setMessage(rule.getMessageOnFailure());
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
            throws GovernanceException {

        int totalArtifactsCount = GovernanceUtil.getAllArtifacts(artifactType, organization).size();

        // Get total number of APIs that are compliant and non-compliant
        Map<ArtifactComplianceState, List<String>> compliancyMap =
                new ComplianceManagerImpl().getComplianceStateOfEvaluatedArtifacts(
                        artifactType, organization);

        int compliantArtifactCount = compliancyMap.get(ArtifactComplianceState.COMPLIANT).size();
        int nonCompliantArtifactCount = compliancyMap.get(ArtifactComplianceState.NON_COMPLIANT).size();

        ArtifactComplianceSummaryDTO summaryDTO = new ArtifactComplianceSummaryDTO();
        summaryDTO.setTotal(totalArtifactsCount);
        summaryDTO.setCompliant(compliantArtifactCount);
        summaryDTO.setNonCompliant(nonCompliantArtifactCount);
        summaryDTO.setNotApplicable(totalArtifactsCount - compliantArtifactCount -
                nonCompliantArtifactCount);
        return summaryDTO;
    }

}
