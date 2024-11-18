package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.wso2.carbon.apimgt.governance.api.GovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.manager.PolicyManager;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyInfoWithRulesetIds;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.impl.PolicyManagerImpl;
import org.wso2.carbon.apimgt.governance.rest.api.GovernancePoliciesApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyInfoWithRulesetIdsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.PolicyMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

/**
 * This is the implementation class for the Governance Policies API.
 */
public class GovernancePoliciesApiServiceImpl implements GovernancePoliciesApiService {

    /**
     * Create a new Governance Policy
     *
     * @param governancePolicyInfoWithRulesetIdsDTO Governance Policy Info with Ruleset Ids
     * @param messageContext                        Message Context
     * @return Response
     * @throws GovernanceException If an error occurs while creating the policy
     */
    public Response createGovernancePolicy(GovernancePolicyInfoWithRulesetIdsDTO governancePolicyInfoWithRulesetIdsDTO,
                                           MessageContext messageContext) throws GovernanceException {

        GovernancePolicyInfoDTO createdPolicyInfoDTO;
        URI createdPolicyURI;

        try {
            PolicyManager policyManager = new PolicyManagerImpl();
            GovernancePolicyInfoWithRulesetIds governancePolicyInfoWithRulesetIds =
                    PolicyMappingUtil.fromDTOtoGovernancePolicyInfoWithRulesetIds(governancePolicyInfoWithRulesetIdsDTO);

            String username = GovernanceAPIUtil.getLoggedInUsername();
            String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

            governancePolicyInfoWithRulesetIds.setCreatedBy(username);
            GovernancePolicyInfo governancePolicyInfo = policyManager.createGovernancePolicy(organization,
                    governancePolicyInfoWithRulesetIds);

            // TODO: Assess compliance for APIs
            createdPolicyInfoDTO = PolicyMappingUtil.
                    fromGovernancePolicyInfoToGovernancePolicyInfoDTO(governancePolicyInfo);
            createdPolicyURI = new URI(
                    GovernanceAPIConstants.POLICY_PATH + "/" + createdPolicyInfoDTO.getId());

        } catch (URISyntaxException e) {
            String error = String.format("Error while creating URI for new Governance Policy %s",
                    governancePolicyInfoWithRulesetIdsDTO.getName());
            throw new GovernanceException(error, e, GovernanceExceptionCodes.INTERNAL_SERVER_ERROR);
        }
        return Response.created(createdPolicyURI).entity(createdPolicyInfoDTO).build();
    }

    /**
     * Get Governance Policy by ID
     *
     * @param policyId       Policy ID
     * @param messageContext Message Context
     * @return Response
     * @throws GovernanceException If an error occurs while retrieving the policy
     */
    public Response getGovernancePolicyById(String policyId, MessageContext messageContext) throws GovernanceException {
        PolicyManager policyManager = new PolicyManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        GovernancePolicyInfo policyInfo = policyManager.getGovernancePolicyByID(organization, policyId);
        GovernancePolicyInfoDTO policyInfoDTO = PolicyMappingUtil.fromGovernancePolicyInfoToGovernancePolicyInfoDTO(policyInfo);
        return Response.status(Response.Status.OK).entity(policyInfoDTO).build();
    }

    /**
     * Get Governance Policies
     *
     * @param messageContext Message Context
     * @return Response
     * @throws GovernanceException If an error occurs while retrieving the policies
     */
    public Response getGovernancePolicies(MessageContext messageContext) throws GovernanceException {
        PolicyManager policyManager = new PolicyManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);
        GovernancePolicyList policyList = policyManager.getGovernancePolicies(organization);

        GovernancePolicyListDTO policyListDTO = PolicyMappingUtil.
                fromGovernancePolicyListToGovernancePolicyListDTO(policyList);

        return Response.status(Response.Status.OK).entity(policyListDTO).build();
    }

    /**
     * Delete a Governance Policy
     *
     * @param policyId       Policy ID
     * @param messageContext Message Context
     * @return Response
     * @throws GovernanceException If an error occurs while deleting the policy
     */
    public Response deleteGovernancePolicy(String policyId, MessageContext messageContext) throws GovernanceException {
        PolicyManager policyManager = new PolicyManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);
        policyManager.deletePolicy(policyId, organization);
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId                              Policy ID
     * @param governancePolicyInfoWithRulesetIdsDTO Governance Policy Info with Ruleset Ids
     * @param messageContext                        Message Context
     * @return Response
     * @throws GovernanceException If an error occurs while updating the policy
     */
    public Response updateGovernancePolicyById(String policyId, GovernancePolicyInfoWithRulesetIdsDTO
            governancePolicyInfoWithRulesetIdsDTO, MessageContext messageContext) throws GovernanceException {
        PolicyManager policyManager = new PolicyManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = GovernanceAPIUtil.getLoggedInUsername();

        GovernancePolicyInfoWithRulesetIds governancePolicyInfoWithRulesetIds =
                PolicyMappingUtil.
                        fromDTOtoGovernancePolicyInfoWithRulesetIds(governancePolicyInfoWithRulesetIdsDTO);

        governancePolicyInfoWithRulesetIds.setUpdatedBy(username);
        GovernancePolicyInfo updatedPolicyInfo = policyManager.updateGovernancePolicy
                (policyId, organization, governancePolicyInfoWithRulesetIds);

        GovernancePolicyInfoDTO updatedPolicyInfoDTO = PolicyMappingUtil.
                fromGovernancePolicyInfoToGovernancePolicyInfoDTO(updatedPolicyInfo);

        //TODO: Access Compliance of existing components

        return Response.status(Response.Status.OK).entity(updatedPolicyInfoDTO).build();
    }
}
