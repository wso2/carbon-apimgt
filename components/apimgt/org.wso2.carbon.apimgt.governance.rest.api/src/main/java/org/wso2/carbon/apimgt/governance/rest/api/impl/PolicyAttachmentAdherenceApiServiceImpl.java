package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachment;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.PolicyAttachmentAdherenceSate;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyAttachmentManager;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyAttachmentAdherenceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactCompForPolicyAttachmentDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactCompSummaryForPolicyAttachmentDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceStatusDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

/**
 * Implementation of the Policy Attachment Adherence API
 */
public class PolicyAttachmentAdherenceApiServiceImpl implements PolicyAttachmentAdherenceApiService {

    /**
     * Get the policy attachment adherence summary
     *
     * @param messageContext The message context
     * @return The policy adherence summary
     * @throws APIMGovernanceException If an error occurs while getting the policy adherence summary
     */
    public Response getPolicyAttachmentAdherenceSummary(MessageContext messageContext) throws APIMGovernanceException {


        PolicyAttachmentManager policyAttachmentManager = new PolicyAttachmentManager();
        ComplianceManager complianceManager = new ComplianceManager();

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        List<String> policyIds = policyAttachmentManager.getGovernancePolicyAttachments(organization)
                .getGovernancePolicyAttachmentList().stream().map(APIMGovernancePolicyAttachment::getId)
                .collect(Collectors.toList());

        Map<PolicyAttachmentAdherenceSate, List<String>> adherenceMap =
                complianceManager.getAdherenceStateofEvaluatedPolicies(organization);

        int followedCount = adherenceMap.get(PolicyAttachmentAdherenceSate.FOLLOWED).size();
        int violatedCount = adherenceMap.get(PolicyAttachmentAdherenceSate.VIOLATED).size();

        PolicyAttachmentAdherenceSummaryDTO summaryDTO =
                new PolicyAttachmentAdherenceSummaryDTO();
        summaryDTO.setTotal(policyIds.size());
        summaryDTO.setViolated(violatedCount);
        summaryDTO.setFollowed(followedCount);
        summaryDTO.setUnApplied(policyIds.size() - followedCount - violatedCount);

        return Response.ok().entity(summaryDTO).build();
    }

    /**
     * Get the policy attachment adherence for a given policy attachment ID
     *
     * @param policyAttachmentId The policy Attachment ID
     * @param messageContext     The message context
     * @return The policy attachment adherence for the given policy ID
     * @throws APIMGovernanceException If an error occurs while getting the policy attachment adherence for the given
     *                                 policy attachment id
     */
    public Response getPolicyAttachmentAdherenceByPolicyAttachmentId(String policyAttachmentId,
                                                                     MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        APIMGovernancePolicyAttachment policyAttachment = new PolicyAttachmentManager()
                .getGovernancePolicyAttachmentByID(policyAttachmentId, organization);

        Map<ArtifactComplianceState, List<ArtifactInfo>> evaluatedArtifacts =
                new ComplianceManager().getArtifactsComplianceForPolicy(policyAttachmentId, organization,
                        true);

        PolicyAttachmentAdherenceDetailsDTO adherenceDetailsDTO = new PolicyAttachmentAdherenceDetailsDTO();
        adherenceDetailsDTO.setName(policyAttachment.getName());
        adherenceDetailsDTO.setId(policyAttachment.getId());
        List<ArtifactCompForPolicyAttachmentDTO> artifactComplianceForPolicyDTOList = new ArrayList<>();

        // Set the status of the policy attachment adherence
        boolean isCompliantArtifactsExist = evaluatedArtifacts.containsKey(ArtifactComplianceState.COMPLIANT) &&
                !evaluatedArtifacts.get(ArtifactComplianceState.COMPLIANT).isEmpty();

        boolean isNonCompliantArtifactsExist = evaluatedArtifacts.containsKey(ArtifactComplianceState.NON_COMPLIANT) &&
                !evaluatedArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).isEmpty();

        if (!isNonCompliantArtifactsExist && !isCompliantArtifactsExist) {
            adherenceDetailsDTO.setStatus(PolicyAttachmentAdherenceDetailsDTO.StatusEnum.UNAPPLIED);
        } else if (isNonCompliantArtifactsExist) {
            adherenceDetailsDTO.setStatus(PolicyAttachmentAdherenceDetailsDTO.StatusEnum.VIOLATED);
        } else {
            adherenceDetailsDTO.setStatus(PolicyAttachmentAdherenceDetailsDTO.StatusEnum.FOLLOWED);
        }

        // Set the compliance status of the artifacts attached to the policy attachment
        for (Map.Entry<ArtifactComplianceState, List<ArtifactInfo>> entry : evaluatedArtifacts.entrySet()) {
            ArtifactComplianceState complianceState = entry.getKey();
            List<ArtifactInfo> artifactInfoList = entry.getValue();

            for (ArtifactInfo artifactInfo : artifactInfoList) {
                ArtifactCompForPolicyAttachmentDTO complianceDTO = new ArtifactCompForPolicyAttachmentDTO();
                complianceDTO.setId(artifactInfo.getArtifactRefId());

                ArtifactType artifactType = artifactInfo.getArtifactType();
                ArtifactInfoDTO infoDTO = new ArtifactInfoDTO();
                infoDTO.setName(artifactInfo.getName());
                infoDTO.setVersion(artifactInfo.getVersion());
                infoDTO.setType(ArtifactInfoDTO.TypeEnum.valueOf(String.valueOf(artifactType)));

                complianceDTO.setInfo(infoDTO);

                complianceDTO.setStatus(ArtifactCompForPolicyAttachmentDTO
                        .StatusEnum.valueOf(String.valueOf(complianceState)));

                artifactComplianceForPolicyDTOList.add(complianceDTO);
            }
        }

        adherenceDetailsDTO.setEvaluatedArtifacts(artifactComplianceForPolicyDTOList);
        return Response.ok().entity(adherenceDetailsDTO).build();

    }

    /**
     * Get the policy attachment adherence for all policy attachments
     *
     * @param limit          The limit
     * @param offset         The offset
     * @param messageContext The message context
     * @return The policy attachment adherence for all policy attachments
     * @throws APIMGovernanceException If an error occurs while getting the policy attachment adherence for
     * all policy attachments
     */
    public Response getPolicyAttachmentAdherenceForAllPolicyAttachments(Integer limit, Integer offset,
                                                                        MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        // Get the list of policy attachments
        List<APIMGovernancePolicyAttachment> allPolicies = new PolicyAttachmentManager()
                .getGovernancePolicyAttachments(organization)
                .getGovernancePolicyAttachmentList();

        // If the offset is greater than the total number of policy attachments, set the offset to the default value
        if (offset > allPolicies.size()) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        List<APIMGovernancePolicyAttachment> attachments = allPolicies.subList(offset, Math.min(offset + limit,
                allPolicies.size()));

        List<PolicyAttachmentAdherenceStatusDTO> adherenceStatusDTOS = new ArrayList<>();

        for (APIMGovernancePolicyAttachment attachment : attachments) {
            Map<ArtifactComplianceState, List<ArtifactInfo>> evaluatedArtifactsByPolicy =
                    new ComplianceManager().getArtifactsComplianceForPolicy(attachment.getId(), organization,
                            false);

            int compliantCount = evaluatedArtifactsByPolicy.get(ArtifactComplianceState.COMPLIANT).size();
            int nonCompliantCount = evaluatedArtifactsByPolicy.get(ArtifactComplianceState.NON_COMPLIANT).size();

            PolicyAttachmentAdherenceStatusDTO policyAttachmentAdherenceStatusDTO =
                    getPolicyAttachmentAdherenceStatusDTO(attachment, compliantCount, nonCompliantCount);

            adherenceStatusDTOS.add(policyAttachmentAdherenceStatusDTO);
        }

        PolicyAttachmentAdherenceListDTO listDTO = new PolicyAttachmentAdherenceListDTO();
        listDTO.setCount(attachments.size());
        listDTO.setList(adherenceStatusDTOS);

        // Set pagination details for the artifact compliance list
        setPaginationDetailsForPolicyAttachmentAdherence(listDTO, limit, offset, allPolicies.size());

        return Response.ok().entity(listDTO).build();
    }

    /**
     * Get the policy attachment adherence status
     *
     * @param policyAttachment                APIMGovernancePolicyAttachment object
     * @param compliantArtifactCount    total number of compliant artifacts
     * @param nonCompliantArtifactCount total number of non-compliant artifacts
     * @return PolicyAdherenceStatusDTO object
     */
    private PolicyAttachmentAdherenceStatusDTO getPolicyAttachmentAdherenceStatusDTO(
            APIMGovernancePolicyAttachment policyAttachment,
            int compliantArtifactCount,
            int nonCompliantArtifactCount) {

        ArtifactCompSummaryForPolicyAttachmentDTO summaryDTO = new ArtifactCompSummaryForPolicyAttachmentDTO();
        summaryDTO.setCompliant(compliantArtifactCount);
        summaryDTO.setNonCompliant(nonCompliantArtifactCount);

        PolicyAttachmentAdherenceStatusDTO statusDTO = new PolicyAttachmentAdherenceStatusDTO();
        statusDTO.setId(policyAttachment.getId());
        statusDTO.setName(policyAttachment.getName());
        statusDTO.setArtifactComplianceSummary(summaryDTO);
        if (compliantArtifactCount == 0 && nonCompliantArtifactCount == 0) {
            statusDTO.setStatus(PolicyAttachmentAdherenceStatusDTO.StatusEnum.UNAPPLIED);
        } else {
            statusDTO.setStatus(nonCompliantArtifactCount > 0 ? PolicyAttachmentAdherenceStatusDTO.StatusEnum.VIOLATED :
                    PolicyAttachmentAdherenceStatusDTO.StatusEnum.FOLLOWED);
        }

        return statusDTO;
    }

    /**
     * Set pagination details for the policy attachment adherence list
     *
     * @param listDTO PolicyAttachmentAdherenceListDTO object
     * @param limit   max number of objects returned
     * @param offset  starting index
     * @param size    total number of objects
     */
    private void setPaginationDetailsForPolicyAttachmentAdherence(PolicyAttachmentAdherenceListDTO listDTO, int limit,
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
                    APIMGovernanceAPIConstants.POLICY_ATTACHMENT_ADHERENCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = APIMGovernanceAPIUtil.getPaginatedURL(APIMGovernanceAPIConstants
                            .POLICY_ATTACHMENT_ADHERENCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }
        paginationDTO.setNext(paginatedNext);
        paginationDTO.setPrevious(paginatedPrevious);

        listDTO.setPagination(paginationDTO);
    }
}
