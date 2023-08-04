package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.GatewayPolicyDeployment;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.GatewayPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.GatewayPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.OperationPolicyMappingUtil;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingsDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.APIConstants.UN_AUTHORIZED_ERROR_MESSAGE;

public class GatewayPoliciesApiServiceImpl implements GatewayPoliciesApiService {
    private static final Log log = LogFactory.getLog(GatewayPoliciesApiServiceImpl.class);

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
            apiProvider.applyGatewayGlobalPolicies(gatewayPolicyList, mappingDescription, displayName, tenantDomain);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to apply policies", e, log);
            } else {
                String errorMessage = "Error while applying gateway policy";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    public Response engageGlobalPolicy(String gatewayPolicyMappingId, List<GatewayPolicyDeploymentDTO> gatewayPolicyDeploymentDTOList,
            MessageContext messageContext) {

        if (gatewayPolicyDeploymentDTOList == null) {
            RestApiUtil.handleBadRequest("Gateway policy deployment list is empty", log);
        }
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            Map<Boolean, List<GatewayPolicyDeployment>> gatewayPolicyDeploymentMap =
                    GatewayPolicyMappingUtil.fromDTOToGatewayPolicyDeploymentMap(gatewayPolicyMappingId, gatewayPolicyDeploymentDTOList);
            apiProvider.engageGatewayGlobalPolicies(gatewayPolicyDeploymentMap, tenantDomain, gatewayPolicyMappingId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to apply policies", e, log);
            } else {
                String errorMessage = "Error while deploying gateway policy";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    public Response deleteGatewayPolicyByPolicyId(String gatewayPolicyMappingId, MessageContext messageContext) {
        if (StringUtils.isBlank(gatewayPolicyMappingId)) {
            RestApiUtil.handleBadRequest("Gateway policy mapping ID is empty", log);
        }
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            List<OperationPolicyData> operationPolicyDataList = apiProvider.getGatewayPolicyDataListByPolicyId(
                    gatewayPolicyMappingId, false);
            if (!operationPolicyDataList.isEmpty()) {
                apiProvider.deleteGatewayPolicyMappingByPolicyMappingId(gatewayPolicyMappingId, tenantDomain);
                return Response.ok().build();
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "Gateway policy mapping not found for the given Mapping ID : " + gatewayPolicyMappingId, log);
            }
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to delete policy mapping", e, log);
            } else {
                String errorMessage = "Error while deleting the gateway policy mapping";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override public Response getAllGatewayPolicies(Integer limit, Integer offset, MessageContext messageContext) {
        return null;
    }

    @Override public Response getGatewayPolicyMappingContentByPolicyMappingId(String gatewayPolicyMappingId,
            MessageContext messageContext) {
        return null;
    }

    @Override public Response updateGatewayPoliciesToFlows(String gatewayPolicyMappingId,
            GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO, MessageContext messageContext) {
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
            apiProvider.updateGatewayGlobalPolicies(gatewayPolicyList, mappingDescription, displayName, tenantDomain,
                    gatewayPolicyMappingId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to apply policies", e, log);
            } else {
                String errorMessage = "Error while applying gateway policy";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    private boolean isAuthorizationFailure(Exception e) {
        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains(UN_AUTHORIZED_ERROR_MESSAGE);
    }
}
