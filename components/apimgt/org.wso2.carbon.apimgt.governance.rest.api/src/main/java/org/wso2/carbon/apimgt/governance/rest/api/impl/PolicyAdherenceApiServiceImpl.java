package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.PolicyAdherenceSate;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyAdherenceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceForPolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryForPolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceStatusDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

/**
 * Implementation of the Policy Adherence API
 */
public class PolicyAdherenceApiServiceImpl implements PolicyAdherenceApiService {

    /**
     * Get the policy adherence summary
     *
     * @param messageContext The message context
     * @return The policy adherence summary
     * @throws APIMGovernanceException If an error occurs while getting the policy adherence summary
     */
    public Response getPolicyAdherenceSummary(MessageContext messageContext) throws APIMGovernanceException {


        PolicyManager policyManager = new PolicyManager();
        ComplianceManager complianceManager = new ComplianceManager();

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        List<String> policyIds = policyManager.getGovernancePolicies(organization)
                .getGovernancePolicyList().stream().map(APIMGovernancePolicy::getId).collect(Collectors.toList());

        Map<PolicyAdherenceSate, List<String>> adherenceMap =
                complianceManager.getAdherenceStateofEvaluatedPolicies(organization);

        int followedCount = adherenceMap.get(PolicyAdherenceSate.FOLLOWED).size();
        int violatedCount = adherenceMap.get(PolicyAdherenceSate.VIOLATED).size();

        PolicyAdherenceSummaryDTO policyAdherenceSummaryDTO = new PolicyAdherenceSummaryDTO();
        policyAdherenceSummaryDTO.setTotal(policyIds.size());
        policyAdherenceSummaryDTO.setViolated(violatedCount);
        policyAdherenceSummaryDTO.setFollowed(followedCount);
        policyAdherenceSummaryDTO.setUnApplied(policyIds.size() - followedCount - violatedCount);

        return Response.ok().entity(policyAdherenceSummaryDTO).build();
    }

    /**
     * Get the policy adherence for a given policy ID
     *
     * @param policyId       The policy ID
     * @param messageContext The message context
     * @return The policy adherence for the given policy ID
     * @throws APIMGovernanceException If an error occurs while getting the policy adherence for the given policy ID
     */
    public Response getPolicyAdherenceByPolicyId(String policyId, MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        APIMGovernancePolicy policy = new PolicyManager().getGovernancePolicyByID(policyId, organization);

        Map<ArtifactComplianceState, List<ArtifactInfo>> evaluatedArtifacts =
                new ComplianceManager().getArtifactsComplianceForPolicy(policyId, organization,
                        true);

        PolicyAdherenceDetailsDTO policyAdherenceDetailsDTO = new PolicyAdherenceDetailsDTO();
        policyAdherenceDetailsDTO.setName(policy.getName());
        policyAdherenceDetailsDTO.setId(policy.getId());
        List<ArtifactComplianceForPolicyDTO> artifactComplianceForPolicyDTOList = new ArrayList<>();

        // Set the status of the policy adherence
        boolean isCompliantArtifactsExist = evaluatedArtifacts.containsKey(ArtifactComplianceState.COMPLIANT) &&
                !evaluatedArtifacts.get(ArtifactComplianceState.COMPLIANT).isEmpty();

        boolean isNonCompliantArtifactsExist = evaluatedArtifacts.containsKey(ArtifactComplianceState.NON_COMPLIANT) &&
                !evaluatedArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).isEmpty();

        if (!isNonCompliantArtifactsExist && !isCompliantArtifactsExist) {
            policyAdherenceDetailsDTO.setStatus(PolicyAdherenceDetailsDTO.StatusEnum.UNAPPLIED);
        } else if (isNonCompliantArtifactsExist) {
            policyAdherenceDetailsDTO.setStatus(PolicyAdherenceDetailsDTO.StatusEnum.VIOLATED);
        } else {
            policyAdherenceDetailsDTO.setStatus(PolicyAdherenceDetailsDTO.StatusEnum.FOLLOWED);
        }

        // Set the compliance status of the artifacts attached to the policy
        for (Map.Entry<ArtifactComplianceState, List<ArtifactInfo>> entry : evaluatedArtifacts.entrySet()) {
            ArtifactComplianceState complianceState = entry.getKey();
            List<ArtifactInfo> artifactInfoList = entry.getValue();

            for (ArtifactInfo artifactInfo : artifactInfoList) {
                ArtifactComplianceForPolicyDTO artifactComplianceForPolicyDTO = new ArtifactComplianceForPolicyDTO();
                artifactComplianceForPolicyDTO.setId(artifactInfo.getArtifactRefId());

                ArtifactType artifactType = artifactInfo.getArtifactType();
                ArtifactInfoDTO infoDTO = new ArtifactInfoDTO();
                infoDTO.setName(artifactInfo.getName());
                infoDTO.setVersion(artifactInfo.getVersion());
                infoDTO.setType(ArtifactInfoDTO.TypeEnum.valueOf(String.valueOf(artifactType)));

                artifactComplianceForPolicyDTO.setInfo(infoDTO);

                artifactComplianceForPolicyDTO.setStatus(ArtifactComplianceForPolicyDTO
                        .StatusEnum.valueOf(String.valueOf(complianceState)));

                artifactComplianceForPolicyDTOList.add(artifactComplianceForPolicyDTO);
            }
        }

        policyAdherenceDetailsDTO.setEvaluatedArtifacts(artifactComplianceForPolicyDTOList);
        return Response.ok().entity(policyAdherenceDetailsDTO).build();

    }

    /**
     * Get the policy adherence for all policies
     *
     * @param limit          The limit
     * @param offset         The offset
     * @param messageContext The message context
     * @return The policy adherence for all policies
     * @throws APIMGovernanceException If an error occurs while getting the policy adherence for all policies
     */
    public Response getPolicyAdherenceForAllPolicies(Integer limit, Integer offset, MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        // Get the list of policies
        List<APIMGovernancePolicy> allPolicies = new PolicyManager().getGovernancePolicies(organization)
                .getGovernancePolicyList();

        // If the offset is greater than the total number of policies, set the offset to the default value
        if (offset > allPolicies.size()) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        List<APIMGovernancePolicy> policies = allPolicies.subList(offset, Math.min(offset + limit, allPolicies.size()));

        List<PolicyAdherenceStatusDTO> policyAdherenceStatusDTOs = new ArrayList<>();

        for (APIMGovernancePolicy policy : policies) {
            Map<ArtifactComplianceState, List<ArtifactInfo>> evaluatedArtifactsByPolicy =
                    new ComplianceManager().getArtifactsComplianceForPolicy(policy.getId(), organization,
                            false);

            int compliantCount = evaluatedArtifactsByPolicy.get(ArtifactComplianceState.COMPLIANT).size();
            int nonCompliantCount = evaluatedArtifactsByPolicy.get(ArtifactComplianceState.NON_COMPLIANT).size();

            PolicyAdherenceStatusDTO policyAdherenceStatusDTO = getPolicyAdherenceStatusDTO(
                    policy, compliantCount, nonCompliantCount);

            policyAdherenceStatusDTOs.add(policyAdherenceStatusDTO);
        }

        PolicyAdherenceListDTO listDTO = new PolicyAdherenceListDTO();
        listDTO.setCount(policies.size());
        listDTO.setList(policyAdherenceStatusDTOs);

        // Set pagination details for the artifact compliance list
        setPaginationDetailsForPolicyAdherence(listDTO, limit, offset, allPolicies.size());

        return Response.ok().entity(listDTO).build();
    }

    /**
     * Get the policy adherence status
     *
     * @param policy                    APIMGovernancePolicy object
     * @param compliantArtifactCount    total number of compliant artifacts
     * @param nonCompliantArtifactCount total number of non-compliant artifacts
     * @return PolicyAdherenceStatusDTO object
     */
    private PolicyAdherenceStatusDTO getPolicyAdherenceStatusDTO(APIMGovernancePolicy policy,
                                                                 int compliantArtifactCount,
                                                                 int nonCompliantArtifactCount) {

        ArtifactComplianceSummaryForPolicyDTO summaryDTO = new ArtifactComplianceSummaryForPolicyDTO();
        summaryDTO.setCompliant(compliantArtifactCount);
        summaryDTO.setNonCompliant(nonCompliantArtifactCount);

        PolicyAdherenceStatusDTO statusDTO = new PolicyAdherenceStatusDTO();
        statusDTO.setId(policy.getId());
        statusDTO.setName(policy.getName());
        statusDTO.setArtifactComplianceSummary(summaryDTO);
        if (compliantArtifactCount == 0 && nonCompliantArtifactCount == 0) {
            statusDTO.setStatus(PolicyAdherenceStatusDTO.StatusEnum.UNAPPLIED);
        } else {
            statusDTO.setStatus(nonCompliantArtifactCount > 0 ? PolicyAdherenceStatusDTO.StatusEnum.VIOLATED :
                    PolicyAdherenceStatusDTO.StatusEnum.FOLLOWED);
        }

        return statusDTO;
    }

    /**
     * Set pagination details for the policy adherence list
     *
     * @param listDTO PolicyAdherenceListDTO object
     * @param limit   max number of objects returned
     * @param offset  starting index
     * @param size    total number of objects
     */
    private void setPaginationDetailsForPolicyAdherence(PolicyAdherenceListDTO listDTO, int limit,
                                                        int offset, int size) {

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(size);

        // Set previous and next URLs for pagination
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = APIMGovernanceAPIUtil.getPaginatedURL(
                    APIMGovernanceAPIConstants.POLICY_ADHERENCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = APIMGovernanceAPIUtil.getPaginatedURL(APIMGovernanceAPIConstants.POLICY_ADHERENCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }
        paginationDTO.setNext(paginatedNext);
        paginationDTO.setPrevious(paginatedPrevious);

        listDTO.setPagination(paginationDTO);
    }
}
