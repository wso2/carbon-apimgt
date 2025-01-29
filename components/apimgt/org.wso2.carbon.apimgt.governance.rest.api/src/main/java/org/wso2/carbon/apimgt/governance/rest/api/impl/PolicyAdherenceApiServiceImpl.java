package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.PolicyManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyAdherenceSate;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.PolicyManagerImpl;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyAdherenceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceStatusDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.ResultsMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the Policy Adherence API
 */
public class PolicyAdherenceApiServiceImpl implements PolicyAdherenceApiService {

    /**
     * Get the policy adherence summary
     *
     * @param messageContext The message context
     * @return The policy adherence summary
     * @throws GovernanceException If an error occurs while getting the policy adherence summary
     */
    public Response getPolicyAdherenceSummary(MessageContext messageContext) throws GovernanceException {


        PolicyManager policyManager = new PolicyManagerImpl();
        ComplianceManager complianceManager = new ComplianceManagerImpl();

        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);
        List<String> policyIds = policyManager.getGovernancePolicies(organization)
                .getGovernancePolicyList().stream().map(GovernancePolicy::getId).collect(Collectors.toList());

        Map<PolicyAdherenceSate, List<String>> adherenceMap =
                complianceManager.getAdherenceStateofEvaluatedPolicies(organization);

        int followedCount = adherenceMap.get(PolicyAdherenceSate.FOLLOWED).size();
        int violatedCount = adherenceMap.get(PolicyAdherenceSate.VIOLATED).size();

        PolicyAdherenceSummaryDTO summaryDTO = ResultsMappingUtil.getPolicyAdherenceSummary(policyIds.size(),
                followedCount, violatedCount);

        return Response.ok().entity(summaryDTO).build();
    }

    /**
     * Get the policy adherence for a given policy ID
     *
     * @param policyId       The policy ID
     * @param messageContext The message context
     * @return The policy adherence for the given policy ID
     * @throws GovernanceException If an error occurs while getting the policy adherence for the given policy ID
     */
    public Response getPolicyAdherenceByPolicyId(String policyId, MessageContext messageContext)
            throws GovernanceException {

        GovernancePolicy policy = new PolicyManagerImpl().getGovernancePolicyByID(policyId);

        Map<ArtifactComplianceState, List<ArtifactInfo>> evaluatedArtifactsByPolicy =
                new ComplianceManagerImpl().getComplianceStateOfEvaluatedArtifactsByPolicy(policyId, true);

        PolicyAdherenceDetailsDTO detailsDTO = ResultsMappingUtil.getPolicyAdherenceDetailsDTO(policy,
                evaluatedArtifactsByPolicy);

        return Response.ok().entity(detailsDTO).build();

    }

    /**
     * Get the policy adherence for all policies
     *
     * @param limit          The limit
     * @param offset         The offset
     * @param messageContext The message context
     * @return The policy adherence for all policies
     * @throws GovernanceException If an error occurs while getting the policy adherence for all policies
     */
    public Response getPolicyAdherenceForAllPolicies(Integer limit, Integer offset, MessageContext messageContext)
            throws GovernanceException {

        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);


        // Get the list of policies
        List<GovernancePolicy> allPolicies = new PolicyManagerImpl().getGovernancePolicies(organization)
                .getGovernancePolicyList();

        List<GovernancePolicy> policies = allPolicies.subList(offset, Math.min(offset + limit, allPolicies.size()));

        List<PolicyAdherenceStatusDTO> policyAdherenceStatusDTOs = new ArrayList<>();

        for (GovernancePolicy policy : policies) {
            Map<ArtifactComplianceState, List<ArtifactInfo>> evaluatedArtifactsByPolicy =
                    new ComplianceManagerImpl().getComplianceStateOfEvaluatedArtifactsByPolicy(policy.getId(),
                            false);

            int compliantCount = evaluatedArtifactsByPolicy.get(ArtifactComplianceState.COMPLIANT).size();
            int nonCompliantCount = evaluatedArtifactsByPolicy.get(ArtifactComplianceState.NON_COMPLIANT).size();

            PolicyAdherenceStatusDTO policyAdherenceStatusDTO = ResultsMappingUtil.getPolicyAdherenceStatusDTO(
                    policy, compliantCount, nonCompliantCount);

            policyAdherenceStatusDTOs.add(policyAdherenceStatusDTO);
        }

        PolicyAdherenceListDTO listDTO = new PolicyAdherenceListDTO();
        listDTO.setCount(policies.size());
        listDTO.setList(policyAdherenceStatusDTOs);

        // Set pagination details for the artifact compliance list
        ResultsMappingUtil.setPaginationDetailsForPolicyAdherence(listDTO, limit, offset, allPolicies.size());

        return Response.ok().entity(listDTO).build();
    }
}
