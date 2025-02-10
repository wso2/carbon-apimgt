
/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyList;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.rest.api.PoliciesApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.PolicyMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * This is the implementation class for the Governance Policies API.
 */
public class PoliciesApiServiceImpl implements PoliciesApiService {

    /**
     * Create a new Governance Policy
     *
     * @param governancePolicyDTO Governance Policy  with Ruleset Ids
     * @param messageContext      Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while creating the policy
     */
    public Response createGovernancePolicy(APIMGovernancePolicyDTO governancePolicyDTO,
                                           MessageContext messageContext) throws APIMGovernanceException {

        APIMGovernancePolicyDTO createdPolicyDTO;
        URI createdPolicyURI;

        try {
            PolicyManager policyManager = new PolicyManager();
            APIMGovernancePolicy governancePolicy =
                    PolicyMappingUtil.fromDTOtoGovernancePolicy(governancePolicyDTO);

            String username = APIMGovernanceAPIUtil.getLoggedInUsername();
            String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

            governancePolicy.setCreatedBy(username);
            governancePolicy = policyManager.createGovernancePolicy(organization,
                    governancePolicy);

            // Access policy compliance in the background
            new ComplianceManager().handlePolicyChangeEvent(governancePolicy.getId(), organization);

            createdPolicyDTO = PolicyMappingUtil.
                    fromGovernancePolicyToGovernancePolicyDTO(governancePolicy);
            createdPolicyURI = new URI(
                    APIMGovernanceAPIConstants.POLICY_PATH + "/" + createdPolicyDTO.getId());

        } catch (URISyntaxException e) {
            String error = String.format("Error while creating URI for new Governance Policy %s",
                    governancePolicyDTO.getName());
            throw new APIMGovernanceException(error, e, APIMGovExceptionCodes.INTERNAL_SERVER_ERROR);
        }
        return Response.created(createdPolicyURI).entity(createdPolicyDTO).build();
    }

    /**
     * Update a Governance Policy
     *
     * @param policyId            Policy ID
     * @param governancePolicyDTO Governance Policy  with Ruleset Ids
     * @param messageContext      Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    public Response updateGovernancePolicyById(String policyId, APIMGovernancePolicyDTO
            governancePolicyDTO, MessageContext messageContext) throws APIMGovernanceException {
        PolicyManager policyManager = new PolicyManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();

        APIMGovernancePolicy governancePolicy =
                PolicyMappingUtil.
                        fromDTOtoGovernancePolicy(governancePolicyDTO);

        governancePolicy.setUpdatedBy(username);
        APIMGovernancePolicy updatedPolicy = policyManager.updateGovernancePolicy
                (policyId, governancePolicy, organization);

        APIMGovernancePolicyDTO updatedPolicyDTO = PolicyMappingUtil.
                fromGovernancePolicyToGovernancePolicyDTO(updatedPolicy);

        // Re-access policy compliance in the background
        new ComplianceManager().handlePolicyChangeEvent(policyId, organization);

        return Response.status(Response.Status.OK).entity(updatedPolicyDTO).build();
    }

    /**
     * Delete a Governance Policy
     *
     * @param policyId       Policy ID
     * @param messageContext Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */
    public Response deleteGovernancePolicy(String policyId, MessageContext messageContext)
            throws APIMGovernanceException {
        PolicyManager policyManager = new PolicyManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        policyManager.deletePolicy(policyId, organization);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Get Governance Policy by ID
     *
     * @param policyId       Policy ID
     * @param messageContext Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while retrieving the policy
     */
    public Response getGovernancePolicyById(String policyId, MessageContext messageContext)
            throws APIMGovernanceException {
        PolicyManager policyManager = new PolicyManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        APIMGovernancePolicy policy = policyManager.getGovernancePolicyByID(policyId, organization);
        APIMGovernancePolicyDTO policyDTO = PolicyMappingUtil.fromGovernancePolicyToGovernancePolicyDTO(policy);
        return Response.status(Response.Status.OK).entity(policyDTO).build();
    }

    /**
     * Get all Governance Policies
     *
     * @param limit          Limit for Pagination
     * @param offset         Offset for Pagination
     * @param query          Query for filtering
     * @param messageContext Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while retrieving the policies
     */
    public Response getGovernancePolicies(Integer limit, Integer offset, String query, MessageContext messageContext)
            throws APIMGovernanceException {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query != null ? query : "";

        PolicyManager policyManager = new PolicyManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        APIMGovernancePolicyList policyList;
        if (!query.isEmpty()) {
            policyList = policyManager.searchGovernancePolicies(query, organization);
        } else {
            policyList = policyManager.getGovernancePolicies(organization);
        }

        APIMGovernancePolicyListDTO policyListDTO = getPaginatedPolicyList(policyList, limit, offset, query);

        return Response.status(Response.Status.OK).entity(policyListDTO).build();
    }

    /**
     * Get a paginated list of Governance Policies
     *
     * @param policyList List of Governance Policies
     * @param limit      Limit for Pagination
     * @param offset     Offset for Pagination
     * @param query      Query for filtering
     * @return Paginated Governance Policy List
     */
    private APIMGovernancePolicyListDTO getPaginatedPolicyList(APIMGovernancePolicyList policyList, int limit,
                                                               int offset,
                                                               String query) {
        int policyCount = policyList.getCount();
        List<APIMGovernancePolicyDTO> policies = new ArrayList<>();
        APIMGovernancePolicyListDTO paginatedPolicyListDTO = new APIMGovernancePolicyListDTO();
        paginatedPolicyListDTO.setCount(Math.min(policyCount, limit));

        // If the provided offset value exceeds the offset, reset the offset to default.
        if (offset > policyCount) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        // Select only the set of policies which matches the given limit and offset values.
        int start = offset;
        int end = Math.min(policyCount, start + limit);
        for (int i = start; i < end; i++) {
            APIMGovernancePolicy policy = policyList.getGovernancePolicyList().get(i);
            APIMGovernancePolicyDTO policyDTO = PolicyMappingUtil.fromGovernancePolicyToGovernancePolicyDTO(policy);
            policies.add(policyDTO);
        }
        paginatedPolicyListDTO.setList(policies);

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(policyCount);
        paginatedPolicyListDTO.setPagination(paginationDTO);

        // Set previous and next URLs for pagination
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, policyCount);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = APIMGovernanceAPIUtil.getPaginatedURLWithQuery
                    (APIMGovernanceAPIConstants.POLICIES_GET_URL,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = APIMGovernanceAPIUtil.getPaginatedURLWithQuery
                    (APIMGovernanceAPIConstants.POLICIES_GET_URL,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        paginationDTO.setPrevious(paginatedPrevious);
        paginationDTO.setNext(paginatedNext);

        return paginatedPolicyListDTO;
    }
}
