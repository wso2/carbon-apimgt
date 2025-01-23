package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.PolicyManager;
import org.wso2.carbon.apimgt.governance.api.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.PolicyManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.RulesetManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;
import org.wso2.carbon.apimgt.governance.rest.api.ArtifactComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceWithRulesetsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RuleValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.ResultsMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;

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

        // Check if artifact has been governed
        boolean isArtifactGoverned = complianceManager.isArtifactEvaluationResultsExist(artifactId);

        // If the artifact is not governed, set the compliance status to not applicable and return
        if (!isArtifactGoverned) {
            artifactComplianceDetailsDTO.setStatus(ArtifactComplianceDetailsDTO.StatusEnum.NOT_APPLICABLE);
            return Response.ok().entity(artifactComplianceDetailsDTO).build();
        }

        // Get policies applicable to the artifact within the organization
        Map<String, String> applicablePolicyIds = GovernanceUtil
                .getApplicablePoliciesForArtifact(artifactId, artifactType, organization);

        List<PolicyAdherenceWithRulesetsDTO> policyAdherenceDetails = new ArrayList<>();

        // Get policy adherence results for each policy
        for (Map.Entry<String, String> entry : applicablePolicyIds.entrySet()) {
            String policyId = entry.getKey();
            String policyName = entry.getValue();
            PolicyAdherenceWithRulesetsDTO policyAdherence = getPolicyAdherenceResults(policyId, policyName,
                    artifactId);

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
     * @param policyId   policy ID
     * @param policyName policy name
     * @param artifactId artifact ID
     * @return PolicyAdherenceWithRulesetsDTO
     * @throws GovernanceException if an error occurs while getting the policy adherence results
     */
    private PolicyAdherenceWithRulesetsDTO getPolicyAdherenceResults(String policyId, String policyName,
                                                                     String artifactId) throws GovernanceException {


        PolicyManager policyManager = new PolicyManagerImpl();
        ComplianceManager complianceManager = new ComplianceManagerImpl();

        PolicyAdherenceWithRulesetsDTO policyAdherenceWithRulesetsDTO = new PolicyAdherenceWithRulesetsDTO();
        policyAdherenceWithRulesetsDTO.setPolicyId(policyId);
        policyAdherenceWithRulesetsDTO.setPolicyName(policyName);

        // Check if policy has been evaluated for the artifact
        boolean isPolicyEvaluated = complianceManager.isPolicyEvaluationResultsExist(artifactId, policyId);

        // If the policy has not been evaluated, set the policy adherence status to unapplied
        if (!isPolicyEvaluated) {
            policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.UNAPPLIED);
            return policyAdherenceWithRulesetsDTO;
        }

        // Retrieve rulesets tied to the policy
        List<Ruleset> rulesets = policyManager.getRulesetsByPolicyId(policyId);

        // Store the ruleset validation results
        List<RulesetValidationResultDTO> rulesetValidationResults = new ArrayList<>();

        // Get ruleset validation results for each ruleset
        for (Ruleset ruleset : rulesets) {
            RulesetValidationResultDTO rulesetValidationResultDTO = getRulesetValidationResults(ruleset, artifactId,
                    policyId);

            // If the ruleset validation fails, set the policy adherence status to violated
            if (rulesetValidationResultDTO.getStatus() == RulesetValidationResultDTO.StatusEnum.FAILED) {
                policyAdherenceWithRulesetsDTO.setStatus(PolicyAdherenceWithRulesetsDTO.StatusEnum.VIOLATED);
            }
            rulesetValidationResults.add(rulesetValidationResultDTO);
        }

        policyAdherenceWithRulesetsDTO.setRulesetValidationResults(rulesetValidationResults);

        return policyAdherenceWithRulesetsDTO;
    }

    /**
     * Get ruleset validation results
     *
     * @param ruleset    ruleset
     * @param artifactId artifact ID
     * @param policyId   policy ID
     * @return RulesetValidationResultDTO
     * @throws GovernanceException if an error occurs while updating the ruleset validation results
     */
    private RulesetValidationResultDTO getRulesetValidationResults(Ruleset ruleset,
                                                                   String artifactId, String policyId)
            throws GovernanceException {

        ComplianceManager complianceManager = new ComplianceManagerImpl();
        RulesetManager rulesetManager = new RulesetManagerImpl();

        String rulesetId = ruleset.getId();

        RulesetValidationResultDTO rulesetValidationResultDTO = new RulesetValidationResultDTO();
        rulesetValidationResultDTO.setId(ruleset.getId());
        rulesetValidationResultDTO.setName(ruleset.getName());


        Set<String> violatedRuleCodes = new HashSet<>();
        List<RuleValidationResultDTO> violatedRules = new ArrayList<>();
        List<RuleValidationResultDTO> followedRules = new ArrayList<>();

        // Fetch all rules within the current ruleset
        List<Rule> allRules = rulesetManager.getRules(rulesetId);
        Map<String, Rule> rulesMap = allRules.stream()
                .collect(Collectors.toMap(Rule::getCode, rule -> rule));

        // Fetch violations for the current ruleset
        List<RuleViolation> ruleViolations = complianceManager.getRuleViolations(
                artifactId, policyId, rulesetId);

        // Check if ruleset has been evaluated
        if (ruleViolations.isEmpty()) {
            ComplianceEvaluationResult result =
                    complianceManager.getComplianceEvaluationResult(artifactId, policyId,
                            rulesetId);
            if (result == null) {
                rulesetValidationResultDTO.setStatus(RulesetValidationResultDTO.StatusEnum.PENDING);
                return rulesetValidationResultDTO;
            }
        }

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

        return rulesetValidationResultDTO;
    }

    public Response getArtifactComplianceForAllArtifacts(Integer limit, Integer offset,
                                                         String artifactType, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
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

        ArtifactComplianceSummaryDTO summaryDTO = new ArtifactComplianceSummaryDTO();
        ComplianceManager complianceManager = new ComplianceManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        if (ArtifactType.isArtifactAPI(artifactType)) {
            // Get total number of APIs in the organization
            int totalAPIsCount = APIMUtil.getAllAPIs(organization).size();

            // Get total number of APIs that are compliant and non-compliant
            Map<ArtifactComplianceState, List<String>> compliancyMap =
                    complianceManager.getCompliantAndNonCompliantArtifacts(
                            ArtifactType.API, organization);

            int compliantAPIsCount = compliancyMap.get(ArtifactComplianceState.COMPLIANT).size();
            int nonCompliantAPIsCount = compliancyMap.get(ArtifactComplianceState.NON_COMPLIANT).size();
            int notApplicableAPIsCount = totalAPIsCount - compliantAPIsCount - nonCompliantAPIsCount;

            summaryDTO.setArtifactType(ArtifactComplianceSummaryDTO.ArtifactTypeEnum.API);
            summaryDTO.setTotalArtifacts(totalAPIsCount);
            summaryDTO.setCompliantArtifacts(compliantAPIsCount);
            summaryDTO.setNonCompliantArtifacts(nonCompliantAPIsCount);
            summaryDTO.setNotApplicableArtifacts(notApplicableAPIsCount);

        }

        return Response.ok().entity(summaryDTO).build();
    }
}
