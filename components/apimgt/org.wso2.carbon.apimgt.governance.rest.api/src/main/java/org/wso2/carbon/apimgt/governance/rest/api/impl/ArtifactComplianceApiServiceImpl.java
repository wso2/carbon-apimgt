package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
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
import org.wso2.carbon.apimgt.governance.rest.api.ArtifactComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceStatusDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceWithRulesetsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RuleValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultWithoutRulesDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.SeverityBasedRuleViolationCountDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.ResultsMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API implementation class for Artifact Compliance API
 */
public class ArtifactComplianceApiServiceImpl implements ArtifactComplianceApiService {

    /**
     * Get compliance evaluation results for a given artifact
     *
     * @param artifactId     Artifact ID
     * @param messageContext Message Context
     * @return Response
     * @throws GovernanceException if an error occurs while getting the artifact compliance evaluation results
     */
    public Response getArtifactComplianceByArtifactId(String artifactId, MessageContext messageContext)
            throws GovernanceException {

        ComplianceManager complianceManager = new ComplianceManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        // Get artifact type
        ArtifactType artifactType = GovernanceUtil.getArtifactType(artifactId);

        // Initialize the response DTO
        ArtifactComplianceDetailsDTO artifactComplianceDetailsDTO = new ArtifactComplianceDetailsDTO();

        artifactComplianceDetailsDTO.setArtifactId(artifactId);
        artifactComplianceDetailsDTO.setArtifactType(
                ArtifactComplianceDetailsDTO.ArtifactTypeEnum.fromValue(String.valueOf(artifactType)));
        artifactComplianceDetailsDTO.setArtifactName(GovernanceUtil
                .getArtifactName(artifactId, artifactType));

        // Get all policies applicable to the artifact within the organization as a map of policy ID to policy name
        Map<String, String> applicablePolicies = GovernanceUtil
                .getApplicablePoliciesForArtifact(artifactId, artifactType, organization);

        if (applicablePolicies.isEmpty()) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return Response.ok().entity(artifactComplianceDetailsDTO).build();
        }

        // Get all policies evaluated for the artifact
        List<String> evaluatedPolicies = complianceManager.getEvaluatedPoliciesByArtifactId(artifactId);

        // If the artifact is not evaluated yet, set the compliance status to not applicable and return
        if (evaluatedPolicies.isEmpty()) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return Response.ok().entity(artifactComplianceDetailsDTO).build();
        }

        List<PolicyAdherenceWithRulesetsDTO> policyAdherenceDetails = new ArrayList<>();

        // Get policy adherence results for each policy
        for (Map.Entry<String, String> entry : applicablePolicies.entrySet()) {
            String policyId = entry.getKey();
            String policyName = entry.getValue();
            boolean isPolicyEvaluated = evaluatedPolicies.contains(policyId);
            PolicyAdherenceWithRulesetsDTO policyAdherence = getPolicyAdherenceResults(policyId, policyName,
                    artifactId, isPolicyEvaluated);

            // If the policy is violated, set the artifact compliance status to non-compliant
            if (policyAdherence.getStatus() == PolicyAdherenceWithRulesetsDTO.StatusEnum.VIOLATED) {
                artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO
                        .StatusEnum.NON_COMPLAINT);
            }

        }

        artifactComplianceDetailsDTO.setGovernedPolicies(policyAdherenceDetails);

        return Response.ok().entity(artifactComplianceDetailsDTO).build();
    }

    /**
     * Get how rulesets run against an artifact adhering to a policy
     *
     * @param policyId          policy ID
     * @param policyName        policy name
     * @param artifactId        artifact ID
     * @param isPolicyEvaluated whether the policy has been evaluated
     * @return PolicyAdherenceWithRulesetsDTO
     * @throws GovernanceException if an error occurs while getting the policy adherence results
     */
    private PolicyAdherenceWithRulesetsDTO getPolicyAdherenceResults(String policyId,
                                                                     String policyName, String artifactId,
                                                                     boolean isPolicyEvaluated)
            throws GovernanceException {


        PolicyManager policyManager = new PolicyManagerImpl();
        ComplianceManager complianceManager = new ComplianceManagerImpl();

        PolicyAdherenceWithRulesetsDTO policyAdherenceWithRulesetsDTO = new PolicyAdherenceWithRulesetsDTO();
        policyAdherenceWithRulesetsDTO.setPolicyId(policyId);
        policyAdherenceWithRulesetsDTO.setPolicyName(policyName);

        // If the policy has not been evaluated, set the policy adherence status to unapplied
        if (!isPolicyEvaluated) {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.UNAPPLIED);
            return policyAdherenceWithRulesetsDTO;
        }

        // Retrieve rulesets tied to the policy
        List<Ruleset> applicableRulesets = policyManager.getRulesetsByPolicyId(policyId);

        // Retrieve the evaluated rulesets for the policy
        List<String> evaluatedRulesets =
                complianceManager.getEvaluatedRulesetsByArtifactIdAndPolicyId(artifactId, policyId);

        // Store the ruleset validation results
        List<RulesetValidationResultWithoutRulesDTO> rulesetValidationResults = new ArrayList<>();

        // Get ruleset validation results for each ruleset
        for (Ruleset ruleset : applicableRulesets) {
            boolean isRulesetEvaluated = evaluatedRulesets.contains(ruleset.getId());

            RulesetValidationResultWithoutRulesDTO resultDTO = getRulesetValidationResults(ruleset, artifactId,
                    policyId, isRulesetEvaluated);
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
     * @param policyId           policy ID
     * @param isRulesetEvaluated whether the ruleset has been evaluated
     * @return RulesetValidationResultDTO
     * @throws GovernanceException if an error occurs while updating the ruleset validation results
     */
    private RulesetValidationResultWithoutRulesDTO getRulesetValidationResults(Ruleset ruleset,
                                                                               String artifactId, String policyId,
                                                                               boolean isRulesetEvaluated)
            throws GovernanceException {

        ComplianceManager complianceManager = new ComplianceManagerImpl();

        RulesetValidationResultWithoutRulesDTO rulesetDTO = new RulesetValidationResultWithoutRulesDTO();
        rulesetDTO.setId(ruleset.getId());
        rulesetDTO.setName(ruleset.getName());

        // Fetch violations for the current ruleset
        List<RuleViolation> ruleViolations = complianceManager
                .getRuleViolations(artifactId, policyId, ruleset.getId());

        // If the ruleset has not been evaluated, set the ruleset validation status to unapplied
        if (!isRulesetEvaluated) {
            rulesetDTO.setStatus(RulesetValidationResultWithoutRulesDTO.StatusEnum.UNAPPLIED);
            return rulesetDTO;
        }

        rulesetDTO.setStatus(ruleViolations.isEmpty() ?
                RulesetValidationResultWithoutRulesDTO.StatusEnum.PASSED :
                RulesetValidationResultWithoutRulesDTO.StatusEnum.FAILED);

        return rulesetDTO;
    }

    /**
     * Get compliance evaluation results for all artifacts
     *
     * @param limit          limit
     * @param offset         offset
     * @param artifactType   artifact type
     * @param messageContext message context
     * @return Response
     * @throws GovernanceException if an error occurs while getting the artifact compliance evaluation results
     */
    public Response getArtifactComplianceForAllArtifacts(Integer limit, Integer offset,
                                                         String artifactType,
                                                         MessageContext messageContext) throws GovernanceException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        artifactType = artifactType != null ? artifactType : String.valueOf(ArtifactType.API);

        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        List<ArtifactComplianceStatusDTO> complianceStatusList = new ArrayList<>();

        // Check if the artifact type is API and proceed accordingly
        if (ArtifactType.isArtifactAPI(artifactType)) {
            // Retrieve APIs the given organization
            List<String> apiIds = APIMUtil.getPaginatedAPIs(organization, limit, offset);

            for (String apiId : apiIds) {
                ArtifactComplianceStatusDTO complianceStatus = getArtifactComplianceStatus(apiId,
                        ArtifactType.API, organization);
                complianceStatusList.add(complianceStatus);
            }
        }

        ArtifactComplianceListDTO complianceListDTO = new ArtifactComplianceListDTO();
        complianceListDTO.setList(complianceStatusList);
        complianceListDTO.setCount(complianceStatusList.size());

        // Set pagination details for the artifact compliance list
        ResultsMappingUtil.setPaginationDetailsForArtifactCompliance(complianceListDTO, limit, offset,
                complianceStatusList.size(), artifactType);

        return Response.ok().entity(complianceListDTO).build();

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
    private ArtifactComplianceStatusDTO getArtifactComplianceStatus(String artifactId, ArtifactType artifactType,
                                                                    String organization)
            throws GovernanceException {

        ComplianceManager complianceManager = new ComplianceManagerImpl();

        // Create a new DTO to store compliance status for the current API
        ArtifactComplianceStatusDTO complianceStatus = new ArtifactComplianceStatusDTO();

        complianceStatus.setArtifactId(artifactId);
        complianceStatus.setArtifactName(GovernanceUtil.getArtifactName(artifactId, artifactType));
        complianceStatus.setArtifactType(
                ArtifactComplianceStatusDTO.ArtifactTypeEnum.fromValue(
                        String.valueOf(artifactType)));

        // Retrieve applicable policies for the current artifact
        Map<String, String> applicablePolicies = GovernanceUtil
                .getApplicablePoliciesForArtifact(artifactId, artifactType, organization);

        // If no policies are applicable, set the compliance status to not applicable and return
        if (applicablePolicies.isEmpty()) {
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.NOT_APPLICABLE);
            return complianceStatus;
        }

        // Get evaluated policies for the current artifact
        List<String> evaluatedPolicies = complianceManager.getEvaluatedPoliciesByArtifactId(artifactId);

        // If the artifact is not evaluated yet, set the compliance status to not applicable and return
        if (evaluatedPolicies.isEmpty()) {
            complianceStatus.setStatus(ArtifactComplianceStatusDTO.StatusEnum.NOT_APPLICABLE);
            return complianceStatus;
        }

        // Track violated policy IDs for the current artifact
        Set<String> violatedPolicies = new HashSet<>();

        // Retrieve rule violations categorized by severity for the current artifact
        Map<Severity, List<RuleViolation>> ruleViolationsBySeverity = complianceManager
                .getSeverityBasedRuleViolationsForArtifact(artifactId);

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
        
        complianceStatus.setPolicyAdherenceSummary(ResultsMappingUtil.getPolicyAdherenceSummary(
                applicablePolicies.size(), evaluatedPolicies.size(), violatedPolicies.size()));
        complianceStatus.setSeverityBasedRuleViolationSummary(ruleViolationCounts);

        return complianceStatus;
    }

    /**
     * Get organizational artifact compliance summary
     *
     * @param artifactType   artifact type
     * @param messageContext message context
     * @return Response
     * @throws GovernanceException if an error occurs while getting the artifact compliance summary
     */
    public Response getArtifactComplianceSummary(String artifactType,
                                                 MessageContext messageContext) throws GovernanceException {
        ComplianceManager complianceManager = new ComplianceManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        artifactType = artifactType != null ? artifactType : String.valueOf(ArtifactType.API);

        if (ArtifactType.isArtifactAPI(artifactType)) {
            // Get total number of APIs in the organization
            int totalAPIsCount = APIMUtil.getAllAPIs(organization).size();

            // Get total number of APIs that are compliant and non-compliant
            Map<ArtifactComplianceState, List<String>> compliancyMap =
                    complianceManager.getComplianceStateOfEvaluatedArtifacts(
                            ArtifactType.API, organization);

            int compliantAPIsCount = compliancyMap.get(ArtifactComplianceState.COMPLIANT).size();
            int nonCompliantAPIsCount = compliancyMap.get(ArtifactComplianceState.NON_COMPLIANT).size();

            ArtifactComplianceSummaryDTO summaryDTO = ResultsMappingUtil.getArtifactComplianceSummary(
                    totalAPIsCount, compliantAPIsCount, nonCompliantAPIsCount);
            return Response.ok().entity(summaryDTO).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    /**
     * Get ruleset validation results by artifact ID
     *
     * @param artifactId     artifact ID
     * @param rulesetId      ruleset ID
     * @param messageContext message context
     * @return Response
     * @throws GovernanceException if an error occurs while getting the ruleset validation results
     */
    @Override
    public Response getRulesetValidationResultsByArtifactId(String artifactId, String rulesetId,
                                                            MessageContext messageContext) throws GovernanceException {

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
                .isRulesetEvaluatedForArtifact(artifactId, rulesetId);
        if (!isRulesetEvaluatedForArtifact) {
            rulesetValidationResultDTO.setStatus(RulesetValidationResultDTO.StatusEnum.UNAPPLIED);
            return Response.ok().entity(rulesetValidationResultDTO).build();
        }

        Set<String> violatedRuleCodes = new HashSet<>();
        List<RuleValidationResultDTO> violatedRules = new ArrayList<>();
        List<RuleValidationResultDTO> followedRules = new ArrayList<>();

        // Fetch all rules within the current ruleset
        List<Rule> allRules = rulesetManager.getRules(rulesetId);
        Map<String, Rule> rulesMap = allRules.stream()
                .collect(Collectors.toMap(Rule::getCode, rule -> rule));

        // Fetch violations for the current ruleset
        List<RuleViolation> ruleViolations = complianceManager.getRuleViolations(artifactId, rulesetId);

        // IMPORTANT: NOTE THAT THERE CAN BE MULTIPLE VIOLATIONS WITH SAME CODE BUT DIFFERENT PATH
        for (RuleViolation ruleViolation : ruleViolations) {
            Rule rule = rulesMap.get(ruleViolation.getRuleCode());
            violatedRules.add(ResultsMappingUtil.getRuleValidationResultDTO(rule, ruleViolation));
            violatedRuleCodes.add(rule.getCode());
        }

        for (Rule rule : allRules) {
            if (!violatedRuleCodes.contains(rule.getCode())) {
                followedRules.add(ResultsMappingUtil.getRuleValidationResultDTO(rule, null));
            }
        }

        rulesetValidationResultDTO.setViolatedRules(violatedRules);
        rulesetValidationResultDTO.setFollowedRules(followedRules);
        rulesetValidationResultDTO.setStatus(violatedRules.isEmpty() ?
                RulesetValidationResultDTO.StatusEnum.PASSED :
                RulesetValidationResultDTO.StatusEnum.FAILED);

        return Response.ok().entity(rulesetValidationResultDTO).build();
    }
}
