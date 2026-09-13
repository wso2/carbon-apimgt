
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

import org.apache.commons.lang3.StringUtils;
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
import java.util.Collections;
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

            // The portal hides the control while the feature is off, but the REST API can be called directly.
            // Silently dropping a selection the deployment will not store is worse than saying the flag is off.
            rejectSeveritiesWhenFilteringDisabled(policyManager,
                    governancePolicyDTO.getComplianceAffectingSeverities(), false);

            governancePolicy.setCreatedBy(username);
            // The severity selection travels on the policy and is written by the same insert, so there is no
            // second write to fail on its own and nothing to undo if it does.
            governancePolicy = policyManager.createGovernancePolicy(organization,
                    governancePolicy);

            // Queued as soon as the policy is written, and before anything that only shapes the response. The
            // severity selection rides the insert above, so there is no longer a window in which the scheduler
            // could evaluate a policy whose severities have not landed; what there is instead is a read below
            // which can fail, and a failure to describe the policy must not cost it its evaluation.
            new ComplianceManager().handlePolicyChangeEvent(governancePolicy.getId(), organization);

            setComplianceAffectingSeverities(policyManager, governancePolicy, governancePolicy.getId(),
                    organization);

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

        // As in create, a selection sent while the feature is off is refused rather than dropped.
        rejectSeveritiesWhenFilteringDisabled(policyManager,
                governancePolicyDTO.getComplianceAffectingSeverities(), true);

        // The severity selection is part of the policy update, so the whole change is one transaction. Leaving
        // the field out preserves whatever was stored, while sending it blank clears it back to every severity
        // affecting compliance; the manager picks the statement accordingly.
        APIMGovernancePolicy updatedPolicy = policyManager.updateGovernancePolicy
                (policyId, governancePolicy, organization);

        // Re-assess policy compliance in the background, queued before anything that only shapes the response.
        // The update above clears the policy's stored results as part of its transaction, so if the read below
        // were allowed to prevent this the policy would be left with its old verdicts deleted and no
        // re-evaluation queued to replace them.
        new ComplianceManager().handlePolicyChangeEvent(policyId, organization);

        setComplianceAffectingSeverities(policyManager, updatedPolicy, policyId, organization);

        APIMGovernancePolicyDTO updatedPolicyDTO = PolicyMappingUtil.
                fromGovernancePolicyToGovernancePolicyDTO(updatedPolicy);

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
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();

        policyManager.deletePolicy(policyId, username, organization);
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
        setComplianceAffectingSeverities(policyManager, policy, policyId, organization);
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

        APIMGovernancePolicyListDTO policyListDTO = getPaginatedPolicyList(policyList, limit, offset, query,
                policyManager, organization);

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
                                                               String query, PolicyManager policyManager,
                                                               String organization)
            throws APIMGovernanceException {
        int policyCount = policyList.getCount();

        // A listing has to report the same three states as the single policy response, or a client reading the list
        // concludes the feature is unavailable while the detail view of the same policy says otherwise. The values
        // are read in one query rather than one per row.
        // Decided the same way as the single policy response and as the settings resource, so none of the three
        // can disagree about whether the feature is offered at all.
        boolean severityFilteringEnabled = policyManager.isComplianceAffectingSeverityFilteringEnabled();
        Map<String, String> severitiesByPolicy = severityFilteringEnabled
                ? policyManager.getComplianceAffectingSeverities(organization) : Collections.emptyMap();

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
            policyDTO.setComplianceAffectingSeverities(
                    severityFilteringEnabled ? severitiesByPolicy.getOrDefault(policy.getId(), StringUtils.EMPTY)
                            : null);
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

    /**
     * Refuse a request which asks for a severity selection while the feature is switched off
     * <p>
     * This is input validation rather than a check on whether the value can be stored: with the configuration off
     * no statement names the optional column, so such a request would otherwise succeed with the selection
     * quietly discarded. When the configuration is on the value rides the policy write, and a deployment which
     * has not added the column is told so by that write.
     *
     * @param policyManager                 Manager used to read the configuration
     * @param complianceAffectingSeverities Value from the request, null when the field was not sent
     * @param blankClearsTheValue           True on update, where a blank value clears the stored one and so is
     *                                      still a write; false on create, where there is nothing to clear
     * @throws APIMGovernanceException If a selection was asked for while the feature is off
     */
    private void rejectSeveritiesWhenFilteringDisabled(PolicyManager policyManager,
                                                       String complianceAffectingSeverities,
                                                       boolean blankClearsTheValue) throws APIMGovernanceException {

        boolean writeRequested = blankClearsTheValue ? complianceAffectingSeverities != null
                : StringUtils.isNotBlank(complianceAffectingSeverities);
        if (!writeRequested || policyManager.isComplianceAffectingSeverityFilteringEnabled()) {
            return;
        }
        throw new APIMGovernanceException(APIMGovExceptionCodes.PER_POLICY_SEVERITY_FILTERING_UNAVAILABLE,
                "Per policy severity filtering is not enabled on this deployment. Set "
                        + "apim.governance.per_policy_severity_filtering_enabled to true in deployment.toml, add the "
                        + "optional COMPLIANCE_AFFECTING_SEVERITIES column to GOV_POLICY, and restart the server");
    }

    private void setComplianceAffectingSeverities(PolicyManager policyManager, APIMGovernancePolicy policy,
                                                  String policyId, String organization)
            throws APIMGovernanceException {

        // The configuration is the whole answer. Null is documented as "the feature is not offered here, do not
        // show the control", which is exactly what the flag being off means; with it on the policy either has a
        // selection or has not chosen one yet, and the empty string is the second of those.
        if (!policyManager.isComplianceAffectingSeverityFilteringEnabled()) {
            policy.setComplianceAffectingSeverities(null);
            return;
        }
        String severities = policyManager.getComplianceAffectingSeverities(policyId, organization);
        policy.setComplianceAffectingSeverities(severities == null ? StringUtils.EMPTY : severities);
    }
}
