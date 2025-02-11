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

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.springframework.http.HttpHeaders;
import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.PolicyCategory;
import org.wso2.carbon.apimgt.governance.api.model.PolicyType;
import org.wso2.carbon.apimgt.governance.api.model.Policy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyContent;
import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.PolicyList;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.rest.api.PoliciesApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.PolicyMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * This is the implementation class for the Policy API.
 */
public class PoliciesApiServiceImpl implements PoliciesApiService {

    /**
     * Create a new Governance Policy
     *
     * @param name                      Name
     * @param policyContentInputStream Policy content input stream
     * @param policyContentDetail      Policy content detail
     * @param policyCategory              Policy category
     * @param policyType                  Policy type
     * @param artifactType              Artifact type
     * @param provider                  Provider
     * @param description               Description
     * @param documentationLink         Documentation link
     * @param messageContext            MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while creating the policy
     */
    @Override
    public Response createPolicy(String name, InputStream policyContentInputStream,
                                 Attachment policyContentDetail, String policyType,
                                 String artifactType, String description, String policyCategory,
                                 String documentationLink, String provider,
                                 MessageContext messageContext) throws APIMGovernanceException {
        PolicyInfoDTO createdPolicyDTO;
        URI createdPolicyURI;
        Policy policy = new Policy();
        try {
            policy.setName(name);
            policy.setPolicyCategory(PolicyCategory.fromString(policyCategory));
            policy.setPolicyType(PolicyType.fromString(policyType));
            policy.setArtifactType(ExtendedArtifactType.fromString(artifactType));
            policy.setProvider(provider);
            policy.setDescription(description);
            policy.setDocumentationLink(documentationLink);

            PolicyContent policyContent = new PolicyContent();
            policyContent.setContent(IOUtils.toByteArray(policyContentInputStream));
            policyContent.setFileName(policyContentDetail.getContentDisposition().getFilename());
            policy.setPolicyContent(policyContent);

            String username = APIMGovernanceAPIUtil.getLoggedInUsername();
            String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
            policy.setCreatedBy(username);

            PolicyManager policyManager = new PolicyManager();
            PolicyInfo createdPolicy = policyManager.createNewPolicy(policy, organization);

            createdPolicyDTO = PolicyMappingUtil.fromPolicyInfoToPolicyInfoDTO(createdPolicy);
            createdPolicyURI = new URI(
                    APIMGovernanceAPIConstants.POLICY_PATH + "/" + createdPolicyDTO.getId());
            return Response.created(createdPolicyURI).entity(createdPolicyDTO).build();

        } catch (URISyntaxException e) {
            String error = String.format("Error while creating URI for new Policy %s",
                    name);
            throw new APIMGovernanceException(error, e, APIMGovExceptionCodes.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            throw new APIMGovernanceException("Error while converting policy content stream", e);
        } finally {
            IOUtils.closeQuietly(policyContentInputStream);
        }
    }


    /**
     * Update a Governance Policy
     *
     * @param policyId                 Policy ID
     * @param name                      Name
     * @param policyContentInputStream Policy content input stream
     * @param policyContentDetail      Policy content detail
     * @param policyCategory              Policy category
     * @param policyType                  Policy type
     * @param artifactType              Artifact type
     * @param provider                  Provider
     * @param description               Description
     * @param documentationLink         Documentation link
     * @param messageContext            MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while updating the policy
     */
    @Override
    public Response updatePolicyById(String policyId, String name, InputStream policyContentInputStream,
                                      Attachment policyContentDetail, String policyType, String artifactType,
                                      String description, String policyCategory, String documentationLink,
                                      String provider, MessageContext messageContext)
            throws APIMGovernanceException {

        try {
            Policy policy = new Policy();
            policy.setName(name);
            policy.setPolicyCategory(PolicyCategory.fromString(policyCategory));
            policy.setPolicyType(PolicyType.fromString(policyType));
            policy.setArtifactType(ExtendedArtifactType.fromString(artifactType));
            policy.setProvider(provider);
            policy.setId(policyId);
            policy.setDescription(description);
            policy.setDocumentationLink(documentationLink);

            PolicyContent policyContent = new PolicyContent();
            policyContent.setContent(IOUtils.toByteArray(policyContentInputStream));
            policyContent.setFileName(policyContentDetail.getContentDisposition().getFilename());
            policy.setPolicyContent(policyContent);

            String username = APIMGovernanceAPIUtil.getLoggedInUsername();
            String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
            policy.setUpdatedBy(username);

            PolicyManager policyManager = new PolicyManager();
            PolicyInfo updatedPolicy = policyManager.updatePolicy(policyId, policy, organization);

            // Re-access policy compliance in the background
            new ComplianceManager().handlePolicyChangeEvent(policyId, organization);

            return Response.status(Response.Status.OK).entity(PolicyMappingUtil.
                    fromPolicyInfoToPolicyInfoDTO(updatedPolicy)).build();
        } catch (IOException e) {
            throw new APIMGovernanceException("Error while converting policy content stream", e);
        } finally {
            IOUtils.closeQuietly(policyContentInputStream);
        }

    }

    /**
     * Delete a Governance Policy
     *
     * @param policyId      Policy ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while deleting the policy
     */
    @Override
    public Response deletePolicy(String policyId, MessageContext messageContext) throws APIMGovernanceException {
        PolicyManager policyManager = new PolicyManager();

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        policyManager.deletePolicy(policyId, organization);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Get a Governance Policy by ID
     *
     * @param policyId      Policy ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while getting the policy
     */
    @Override
    public Response getPolicyById(String policyId, MessageContext messageContext) throws APIMGovernanceException {
        PolicyManager policyManager = new PolicyManager();

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        PolicyInfo policy = policyManager.getPolicyById(policyId, organization);
        PolicyInfoDTO policyInfoDTO = PolicyMappingUtil.fromPolicyInfoToPolicyInfoDTO(policy);
        return Response.status(Response.Status.OK).entity(policyInfoDTO).build();
    }

    /**
     * Get the content of a Governance Policy
     *
     * @param policyId      Policy ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while getting the policy content
     */
    @Override
    public Response getPolicyContent(String policyId, MessageContext messageContext) throws APIMGovernanceException {
        PolicyManager policyManager = new PolicyManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        PolicyContent policyContent = policyManager.getPolicyContent(policyId, organization);

        String fileName = policyContent.getFileName() != null ? policyContent.getFileName() : "policy.yaml";
        String contentTypeHeader = "application/x-yaml"; // Default content type

        if (PolicyContent.ContentType.JSON.equals(policyContent.getContentType())) {
            contentTypeHeader = "application/json";
        }
        
        return Response.status(Response.Status.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName)
                .header(HttpHeaders.CONTENT_TYPE, contentTypeHeader)
                .entity(new String(policyContent.getContent(), StandardCharsets.UTF_8)).build();
    }

    /**
     * Get the list of policies using the Policy
     *
     * @param policyId      Policy ID
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while getting the policy usage
     */
    @Override
    public Response getPolicyUsage(String policyId, MessageContext messageContext) throws APIMGovernanceException {
        PolicyManager policyManager = new PolicyManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        List<String> policies = policyManager.getPolicyUsage(policyId, organization);
        return Response.status(Response.Status.OK).entity(policies).build();
    }

    /**
     * Get all the Governance Policys
     *
     * @param limit          Limit
     * @param offset         Offset
     * @param query          Query for filtering
     * @param messageContext MessageContext
     * @return Response object
     * @throws APIMGovernanceException If an error occurs while getting the policies
     */
    public Response getPolicies(Integer limit, Integer offset, String query, MessageContext messageContext)
            throws APIMGovernanceException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query != null ? query : "";

        PolicyManager policyManager = new PolicyManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        PolicyList policyList;
        if (!query.isEmpty()) {
            policyList = policyManager.searchPolicies(query, organization);
        } else {
            policyList = policyManager.getPolicies(organization);
        }
        PolicyListDTO paginatedPolicyList = getPaginatedPolicies(policyList, limit, offset, query);

        return Response.status(Response.Status.OK).entity(paginatedPolicyList).build();
    }

    /**
     * Get the paginated list of Governance Policys
     *
     * @param policyList PolicyList object
     * @param limit       Limit
     * @param offset      Offset
     * @param query       Query for filtering
     * @return PolicyListDTO object
     */
    private PolicyListDTO getPaginatedPolicies(PolicyList policyList, int limit, int offset, String query) {
        int policyCount = policyList.getCount();
        List<PolicyInfoDTO> paginatedPolicys = new ArrayList<>();
        PolicyListDTO paginatedPolicyListDTO = new PolicyListDTO();
        paginatedPolicyListDTO.setCount(Math.min(policyCount, limit));

        // If the provided offset value exceeds the offset, reset the offset to default.
        if (offset > policyCount) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        // Select only the set of policies which matches the given limit and offset values.
        int start = offset;
        int end = Math.min(policyCount, start + limit);
        for (int i = start; i < end; i++) {
            PolicyInfo policyInfo = policyList.getPolicyList().get(i);
            PolicyInfoDTO policyInfoDTO = PolicyMappingUtil.fromPolicyInfoToPolicyInfoDTO(policyInfo);
            paginatedPolicys.add(policyInfoDTO);
        }
        paginatedPolicyListDTO.setList(paginatedPolicys);

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
                    (APIMGovernanceAPIConstants.POLICY_GET_URL,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = APIMGovernanceAPIUtil.getPaginatedURLWithQuery
                    (APIMGovernanceAPIConstants.POLICY_GET_URL,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        paginationDTO.setPrevious(paginatedPrevious);
        paginationDTO.setNext(paginatedNext);

        return paginatedPolicyListDTO;
    }
}
