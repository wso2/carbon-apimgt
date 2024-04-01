/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayPolicyData;
import org.wso2.carbon.apimgt.api.model.GatewayPolicyDeployment;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.GatewayPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.GatewayPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.OperationPolicyMappingUtil;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingsDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for managing gateway policies.
 */
public class GatewayPoliciesApiServiceImpl implements GatewayPoliciesApiService {
    private static final Log log = LogFactory.getLog(GatewayPoliciesApiServiceImpl.class);

    /**
     * Add gateway policy mapping.
     *
     * @param gatewayPolicyMappingsDTO gateway policy mapping DTO
     * @param messageContext           message context
     * @return Response object containing gateway policy mapping DTO
     */
    @Override
    public Response addGatewayPoliciesToFlows(GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO,
            MessageContext messageContext) {

        if (gatewayPolicyMappingsDTO == null) {
            RestApiUtil.handleBadRequest("Gateway policy mapping list is empty", log);
        }
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            List<OperationPolicy> gatewayPolicyList = OperationPolicyMappingUtil.fromDTOToAPIOperationPoliciesList(
                    gatewayPolicyMappingsDTO.getPolicyMapping());
            String mappingDescription = gatewayPolicyMappingsDTO.getDescription();
            String displayName = gatewayPolicyMappingsDTO.getDisplayName();
            String policyMappingUUID = apiProvider.applyGatewayGlobalPolicies(gatewayPolicyList, mappingDescription,
                    displayName, tenantDomain);
            GatewayPolicyData gatewayPolicyData = new GatewayPolicyData();
            gatewayPolicyData.setPolicyMappingId(policyMappingUUID);
            gatewayPolicyData.setPolicyMappingName(displayName);
            gatewayPolicyData.setPolicyMappingDescription(mappingDescription);
            GatewayPolicyMappingInfoDTO gatewayPolicyMappingInfoDTO =
                    GatewayPolicyMappingUtil.fromGatewayPolicyDataToInfoDTO(gatewayPolicyData);
            URI createdGatewayPolicyMappingUri = new URI(RestApiConstants.REST_API_PUBLISHER_VERSION
                    + "/" + RestApiConstants.RESOURCE_PATH_GATEWAY_POLICIES + "/" + policyMappingUUID);
            return Response.created(createdGatewayPolicyMappingUri).entity(gatewayPolicyMappingInfoDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                String errorMessage = "One or more policy IDs in the policy mapping are invalid. " + e.getMessage();
                RestApiUtil.handleResourceNotFoundError(errorMessage, e, log);
            } else {
                String errorMessage = "Error while adding a gateway policy mapping. " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Deploy gateway policy to gateway environments.
     *
     * @param gatewayPolicyMappingId         gateway policy mapping id
     * @param gatewayPolicyDeploymentDTOList gateway policy deployment DTO list
     * @param messageContext                 message context
     * @return Response object containing gateway policy deployment DTO list
     */
    @Override
    public Response engageGlobalPolicy(String gatewayPolicyMappingId,
            List<GatewayPolicyDeploymentDTO> gatewayPolicyDeploymentDTOList, MessageContext messageContext) {

        if (StringUtils.isBlank(gatewayPolicyMappingId)) {
            RestApiUtil.handleBadRequest("Gateway policy mapping ID is empty", log);
        } else if (gatewayPolicyDeploymentDTOList.isEmpty()) {
            RestApiUtil.handleBadRequest("Gateway policy deployment list is empty", log);
        }
        String organization = RestApiCommonUtil.getLoggedInUserTenantDomain();
        validateGatewayLabels(gatewayPolicyDeploymentDTOList, organization);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            // checks whether the gateway policy mapping exists in the particular gateway
            Iterator<GatewayPolicyDeploymentDTO> iterator = gatewayPolicyDeploymentDTOList.iterator();
            while (iterator.hasNext()) {
                GatewayPolicyDeploymentDTO gatewayPolicyDeploymentDTO = iterator.next();
                String gwName = gatewayPolicyDeploymentDTO.getGatewayLabel();
                boolean isDeployment = gatewayPolicyDeploymentDTO.isGatewayDeployment();
                if (isDeployment && apiProvider.hasExistingDeployments(organization, gwName)) {
                    iterator.remove(); // Safely remove the element using the iterator
                }
            }
            List<OperationPolicyData> operationPolicyDataList = apiProvider.getGatewayPolicyDataListByPolicyId(
                    gatewayPolicyMappingId, false);
            if (!operationPolicyDataList.isEmpty()) {
                Map<Boolean, List<GatewayPolicyDeployment>> gatewayPolicyDeploymentMap = GatewayPolicyMappingUtil.fromDTOToGatewayPolicyDeploymentMap(
                        gatewayPolicyMappingId, gatewayPolicyDeploymentDTOList);
                apiProvider.engageGatewayGlobalPolicies(gatewayPolicyDeploymentMap, organization, gatewayPolicyMappingId);
                for (GatewayPolicyDeploymentDTO gatewayPolicyDeploymentDTO : gatewayPolicyDeploymentDTOList) {
                    gatewayPolicyDeploymentDTO.setMappingUUID(gatewayPolicyMappingId);
                }
                GenericEntity<List<GatewayPolicyDeploymentDTO>> entity = new GenericEntity<List<GatewayPolicyDeploymentDTO>>(
                        gatewayPolicyDeploymentDTOList) {
                };
                return Response.ok(entity).build();
            } else {
                RestApiUtil.handleResourceNotFoundError("Policy mapping not found for the given ID : "
                        + gatewayPolicyMappingId, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while deploying gateway policy mapping. " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    private void validateGatewayLabels(List<GatewayPolicyDeploymentDTO> gatewayPolicyDeploymentDTOList,
            String organization) {
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            List<Environment> allEnvs = apiAdmin.getAllEnvironments(organization);
            if (allEnvs.isEmpty()) {
                RestApiUtil.handleBadRequest("No environments found", log);
            }
            boolean allLabelsValid = true;
            List<String> invalidLabels = new ArrayList<>();

            for (GatewayPolicyDeploymentDTO dto : gatewayPolicyDeploymentDTOList) {
                String labelToCheck = dto.getGatewayLabel();
                boolean labelFound = false;
                for (Environment env : allEnvs) {
                    if (env.getName().equals(labelToCheck)) {
                        labelFound = true;
                        break;
                    }
                }
                if (!labelFound) {
                    allLabelsValid = false;
                    invalidLabels.add(labelToCheck);
                }
            }
            if (!allLabelsValid) {
                String invalidLabelsJoined = String.join(", ", invalidLabels);
                RestApiUtil.handleResourceNotFoundError("Invalid gateway labels: " + invalidLabelsJoined, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while validating gateway labels. ", e, log);
        }
    }

    /**
     * Delete gateway policy mapping.
     *
     * @param gatewayPolicyMappingId gateway policy mapping id
     * @param messageContext         message context
     */
    @Override
    public Response deleteGatewayPolicyByPolicyId(String gatewayPolicyMappingId, MessageContext messageContext) {

        if (StringUtils.isBlank(gatewayPolicyMappingId)) {
            RestApiUtil.handleBadRequest("Gateway policy mapping ID is empty", log);
        }
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            if (apiProvider.isPolicyMetadataExists(gatewayPolicyMappingId)) {
                if (apiProvider.isPolicyMappingDeploymentExists(gatewayPolicyMappingId, tenantDomain)) {
                    String errorMessage = "Gateway policy mapping ID: " + gatewayPolicyMappingId
                            + " has active deployments.";
                    RestApiUtil.handlePreconditionFailedError(errorMessage, log);
                }
                apiProvider.deleteGatewayPolicyMappingByPolicyMappingId(gatewayPolicyMappingId, tenantDomain);
                return Response.ok().build();
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "Gateway policy mapping not found for the given Mapping ID : " +
                                gatewayPolicyMappingId, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting the gateway policy mapping. " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieve all the gateway policy mappings.
     *
     * @param offset         starting index
     * @param limit          max number of objects returned
     * @param messageContext message context
     * @return Response object containing gateway policy mapping DTO list
     */
    @Override
    public Response getAllGatewayPolicies(Integer limit, Integer offset, String query,
            MessageContext messageContext) {

        GatewayPolicyMappingDataListDTO gatewayPolicyMappingDataListDTO = null;
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            // If gateway label is provided, retrieve the gateway policy mappings for the given label
            if (query != null) {
                String gatewayLabel = GatewayPolicyMappingUtil.getQueryParams(query);
                if (gatewayLabel != null) {
                    GatewayPolicyData gatewayPolicyData = apiProvider.getLightweightGatewayPolicyMappings(
                            tenantDomain, gatewayLabel);
                    gatewayPolicyData.setGatewayLabels(Collections.singleton(gatewayLabel));
                    gatewayPolicyMappingDataListDTO = GatewayPolicyMappingUtil.fromGatewayPolicyDataListToDTO(
                            Collections.singletonList(gatewayPolicyData), offset, limit);
                } else {
                    RestApiUtil.handleBadRequest("Query parameter is not supported for this request", log);
                }
            } else {
                offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
                List<GatewayPolicyData> gatewayPolicyData = apiProvider.getAllLightweightGatewayPolicyMappings(
                        tenantDomain);
                // Set limit to the query param value or the count of all policy mappings
                limit = limit != null ? limit : gatewayPolicyData.size();
                gatewayPolicyMappingDataListDTO = GatewayPolicyMappingUtil.fromGatewayPolicyDataListToDTO(
                        gatewayPolicyData, offset, limit);
            }
            return Response.ok().entity(gatewayPolicyMappingDataListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the gateway policy mappings. ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieve gateway policy mapping content by policy mapping id.
     *
     * @param gatewayPolicyMappingId gateway policy mapping id
     * @param messageContext         message context
     * @return Response object containing gateway policy mapping DTO
     */
    @Override
    public Response getGatewayPolicyMappingContentByPolicyMappingId(String gatewayPolicyMappingId,
            MessageContext messageContext) {

        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            GatewayPolicyData gatewayPolicyData = apiProvider.getGatewayPolicyMappingDataByPolicyMappingId(
                    gatewayPolicyMappingId, tenantDomain);
            GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO = GatewayPolicyMappingUtil.fromGatewayPolicyDataToDTO(
                    gatewayPolicyData);
            return Response.ok().entity(gatewayPolicyMappingsDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the gateway policy mapping. ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Update gateway policy mapping.
     *
     * @param gatewayPolicyMappingId   gateway policy mapping id
     * @param gatewayPolicyMappingsDTO updated gateway policy mapping DTO
     * @param messageContext           message context
     * @return Response object containing processed gateway policy mapping DTO
     */
    @Override
    public Response updateGatewayPoliciesToFlows(String gatewayPolicyMappingId,
            GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO, MessageContext messageContext) {

        if (StringUtils.isBlank(gatewayPolicyMappingId)) {
            RestApiUtil.handleBadRequest("Gateway policy mapping ID is empty", log);
        } else if (gatewayPolicyMappingsDTO == null) {
            RestApiUtil.handleBadRequest("Gateway policy mapping list is empty", log);
        }

        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            List<OperationPolicyData> operationPolicyDataList = apiProvider.getGatewayPolicyDataListByPolicyId(
                    gatewayPolicyMappingId, false);
            if (!operationPolicyDataList.isEmpty()) {
                List<OperationPolicy> gatewayPolicyList = OperationPolicyMappingUtil.fromDTOToAPIOperationPoliciesList(
                        gatewayPolicyMappingsDTO.getPolicyMapping());
                String mappingDescription = gatewayPolicyMappingsDTO.getDescription();
                String displayName = gatewayPolicyMappingsDTO.getDisplayName();
                String mappingID = apiProvider.updateGatewayGlobalPolicies(gatewayPolicyList, mappingDescription,
                        displayName, tenantDomain, gatewayPolicyMappingId);
                gatewayPolicyMappingsDTO.id(mappingID);
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "Gateway policy mapping not found for the given Mapping ID : " +
                                gatewayPolicyMappingId, log);
            }
            return Response.ok(gatewayPolicyMappingsDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while applying gateway policy. ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
