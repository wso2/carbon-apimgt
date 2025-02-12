
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
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachment;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachmentList;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyAttachmentManager;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyAttachmentsApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyAttachmentDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyAttachmentListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.PolicyAttachmentMappingUtil;
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
public class PolicyAttachmentsApiServiceImpl implements PolicyAttachmentsApiService {

    /**
     * Create a new Governance Policy Attachment
     *
     * @param policyAttachmentDTO Governance Policy  Attachment with policy Ids
     * @param messageContext      Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while creating the policy attachment
     */
    public Response createGovernancePolicyAttachment(APIMGovernancePolicyAttachmentDTO policyAttachmentDTO,
                                                     MessageContext messageContext) throws APIMGovernanceException {

        APIMGovernancePolicyAttachmentDTO createdPolicyAttachmentDTO;
        URI createdPolicyAttachmentURI;

        try {
            PolicyAttachmentManager policyAttachmentManager = new PolicyAttachmentManager();
            APIMGovernancePolicyAttachment governancePolicyAttachment =
                    PolicyAttachmentMappingUtil.fromDTOtoGovernancePolicyAttachment(policyAttachmentDTO);

            String username = APIMGovernanceAPIUtil.getLoggedInUsername();
            String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

            governancePolicyAttachment.setCreatedBy(username);
            governancePolicyAttachment = policyAttachmentManager.createGovernancePolicyAttachment(organization,
                    governancePolicyAttachment);

            // Access policy attachment compliance in the background
            new ComplianceManager().handlePolicyAttachmentChangeEvent(governancePolicyAttachment.getId(), organization);

            createdPolicyAttachmentDTO = PolicyAttachmentMappingUtil.
                    fromGovernancePolicyAttachmentToGovernancePolicyAttachmentDTO(governancePolicyAttachment);
            createdPolicyAttachmentURI = new URI(
                    APIMGovernanceAPIConstants.POLICY_ATTACHMENT_PATH + "/" + createdPolicyAttachmentDTO.getId());

        } catch (URISyntaxException e) {
            String error = String.format("Error while creating URI for new Governance Policy Attachment %s",
                    policyAttachmentDTO.getName());
            throw new APIMGovernanceException(error, e, APIMGovExceptionCodes.INTERNAL_SERVER_ERROR);
        }
        return Response.created(createdPolicyAttachmentURI).entity(createdPolicyAttachmentDTO).build();
    }

    /**
     * Update a Governance Policy Attachment
     *
     * @param policyAttachmentId  Policy Attachment ID
     * @param policyAttachmentDTO Governance Policy Attachment with policy Ids
     * @param messageContext      Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while updating the policy attachment
     */
    public Response updateGovernancePolicyAttachmentById(String policyAttachmentId, APIMGovernancePolicyAttachmentDTO
            policyAttachmentDTO, MessageContext messageContext) throws APIMGovernanceException {
        PolicyAttachmentManager policyAttachmentManager = new PolicyAttachmentManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();

        APIMGovernancePolicyAttachment governancePolicyAttachment =
                PolicyAttachmentMappingUtil.
                        fromDTOtoGovernancePolicyAttachment(policyAttachmentDTO);

        governancePolicyAttachment.setUpdatedBy(username);
        APIMGovernancePolicyAttachment updatedPolicy = policyAttachmentManager.updateGovernancePolicyAttachment
                (policyAttachmentId, governancePolicyAttachment, organization);

        APIMGovernancePolicyAttachmentDTO updatedPolicyDTO = PolicyAttachmentMappingUtil.
                fromGovernancePolicyAttachmentToGovernancePolicyAttachmentDTO(updatedPolicy);

        // Re-access policy attachment compliance in the background
        new ComplianceManager().handlePolicyAttachmentChangeEvent(policyAttachmentId, organization);

        return Response.status(Response.Status.OK).entity(updatedPolicyDTO).build();
    }

    /**
     * Delete a Governance Policy Attachment
     *
     * @param policyAttachmentId       Policy Attachment ID
     * @param messageContext Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while deleting the policy attachment
     */
    public Response deleteGovernancePolicyAttachment(String policyAttachmentId, MessageContext messageContext)
            throws APIMGovernanceException {
        PolicyAttachmentManager policyAttachmentManager = new PolicyAttachmentManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        policyAttachmentManager.deletePolicyAttachment(policyAttachmentId, organization);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Get Governance Policy Attachment by ID
     *
     * @param policyAttachmentId       Policy Attachment ID
     * @param messageContext Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while retrieving the policy attachment
     */
    public Response getGovernancePolicyAttachmentById(String policyAttachmentId, MessageContext messageContext)
            throws APIMGovernanceException {
        PolicyAttachmentManager policyAttachmentManager = new PolicyAttachmentManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        APIMGovernancePolicyAttachment policyAttachment = policyAttachmentManager
                .getGovernancePolicyAttachmentByID(policyAttachmentId, organization);
        APIMGovernancePolicyAttachmentDTO policyAttachmentDTO
                = PolicyAttachmentMappingUtil
                .fromGovernancePolicyAttachmentToGovernancePolicyAttachmentDTO(policyAttachment);
        return Response.status(Response.Status.OK).entity(policyAttachmentDTO).build();
    }

    /**
     * Get all Governance Policy Attachments
     *
     * @param limit          Limit for Pagination
     * @param offset         Offset for Pagination
     * @param query          Query for filtering
     * @param messageContext Message Context
     * @return Response
     * @throws APIMGovernanceException If an error occurs while retrieving the policy attachments
     */
    public Response getGovernancePolicyAttachments(Integer limit, Integer offset, String query,
                                                   MessageContext messageContext)
            throws APIMGovernanceException {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query != null ? query : "";

        PolicyAttachmentManager policyAttachmentManager = new PolicyAttachmentManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        APIMGovernancePolicyAttachmentList attachmentList;
        if (!query.isEmpty()) {
            attachmentList = policyAttachmentManager.searchGovernancePolicyAttachments(query, organization);
        } else {
            attachmentList = policyAttachmentManager.getGovernancePolicyAttachments(organization);
        }

        APIMGovernancePolicyAttachmentListDTO policyListDTO = getPaginatedPolicyAttachmentList(attachmentList, limit,
                offset, query);

        return Response.status(Response.Status.OK).entity(policyListDTO).build();
    }

    /**
     * Get a paginated list of Governance Policy Attachments
     *
     * @param attachmentList List of Governance Policy Attachments
     * @param limit      Limit for Pagination
     * @param offset     Offset for Pagination
     * @param query      Query for filtering
     * @return Paginated Governance Policy Attachment List
     */
    private APIMGovernancePolicyAttachmentListDTO getPaginatedPolicyAttachmentList(
            APIMGovernancePolicyAttachmentList attachmentList, int limit, int offset, String query) {

        int attachmentCount = attachmentList.getCount();
        List<APIMGovernancePolicyAttachmentDTO> policies = new ArrayList<>();
        APIMGovernancePolicyAttachmentListDTO paginatedPolicyListDTO = new APIMGovernancePolicyAttachmentListDTO();
        paginatedPolicyListDTO.setCount(Math.min(attachmentCount, limit));

        // If the provided offset value exceeds the offset, reset the offset to default.
        if (offset > attachmentCount) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        // Select only the set of policy attachments which matches the given limit and offset values.
        int start = offset;
        int end = Math.min(attachmentCount, start + limit);
        for (int i = start; i < end; i++) {
            APIMGovernancePolicyAttachment attachment = attachmentList.getGovernancePolicyAttachmentList().get(i);
            APIMGovernancePolicyAttachmentDTO policyAttachmentDTO = PolicyAttachmentMappingUtil
                    .fromGovernancePolicyAttachmentToGovernancePolicyAttachmentDTO(attachment);
            policies.add(policyAttachmentDTO);
        }
        paginatedPolicyListDTO.setList(policies);

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(attachmentCount);
        paginatedPolicyListDTO.setPagination(paginationDTO);

        // Set previous and next URLs for pagination
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, attachmentCount);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = APIMGovernanceAPIUtil.getPaginatedURLWithQuery
                    (APIMGovernanceAPIConstants.POLICY_ATTACHMENT_GET_URL,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = APIMGovernanceAPIUtil.getPaginatedURLWithQuery
                    (APIMGovernanceAPIConstants.POLICY_ATTACHMENT_GET_URL,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        paginationDTO.setPrevious(paginatedPrevious);
        paginationDTO.setNext(paginatedNext);

        return paginatedPolicyListDTO;
    }
}
