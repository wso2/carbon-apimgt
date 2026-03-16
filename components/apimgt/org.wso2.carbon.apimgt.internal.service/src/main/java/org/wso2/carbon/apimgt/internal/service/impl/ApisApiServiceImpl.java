/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.DeployedAPIRevision;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.subscription.API;
import org.wso2.carbon.apimgt.api.model.subscription.Subscription;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.internal.service.ApisApiService;
import org.wso2.carbon.apimgt.internal.service.dto.APIListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.DeployedAPIRevisionDTO;
import org.wso2.carbon.apimgt.internal.service.dto.DeployedEnvInfoDTO;
import org.wso2.carbon.apimgt.internal.service.dto.DeploymentAcknowledgmentResponseDTO;
import org.wso2.carbon.apimgt.internal.service.dto.UnDeployedAPIRevisionDTO;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.PlatformGatewayArtifactService;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisGet(String xWSO2Tenant, String apiId, String context, String version, String gatewayLabel,
            Boolean expand, String accept, MessageContext messageContext) throws APIManagementException {
        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        String organization = RestApiUtil.getOrganization(messageContext);
        organization = SubscriptionValidationDataUtil.validateTenantDomain(organization, messageContext);
        APIListDTO apiListDTO;
        if (StringUtils.isNotEmpty(gatewayLabel)) {
            if (StringUtils.isNotEmpty(apiId)) {
                API api = subscriptionValidationDAO.getApiByUUID(apiId, gatewayLabel, organization, expand);
                apiListDTO = SubscriptionValidationDataUtil.fromAPIToAPIListDTO(api);
            } else if (StringUtils.isNotEmpty(context) && StringUtils.isNotEmpty(version)) {
                if (!context.startsWith("/t/" + organization.toLowerCase())) {
                    apiListDTO = new APIListDTO();
                }
                API api = subscriptionValidationDAO
                        .getAPIByContextAndVersion(context, version, gatewayLabel, expand);
                apiListDTO = SubscriptionValidationDataUtil.fromAPIToAPIListDTO(api);
            } else {
                if (APIConstants.ORG_ALL_QUERY_PARAM.equals(organization)) {
                    // Retrieve API Detail according to Gateway label.
                    apiListDTO = SubscriptionValidationDataUtil.fromAPIListToAPIListDTO(
                            subscriptionValidationDAO.getAllApisByLabel(gatewayLabel, expand));
                } else {
                    // Retrieve API Detail according to Gateway label.
                    apiListDTO = SubscriptionValidationDataUtil.fromAPIListToAPIListDTO(
                            subscriptionValidationDAO.getAllApis(organization, gatewayLabel, expand));
                }
            }
        } else {
            apiListDTO = SubscriptionValidationDataUtil.fromAPIListToAPIListDTO(
                    subscriptionValidationDAO.getAllApis(organization, expand));
        }
        if (APIConstants.APPLICATION_GZIP.equals(accept)) {
            try {
                File zippedResponse = GZIPUtils.constructZippedResponse(apiListDTO);
                return Response.ok().entity(zippedResponse)
                        .header("Content-Disposition", "attachment").
                                header("Content-Encoding", "gzip").build();
            } catch (APIManagementException e) {
                RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
            }
        } else {
            return Response.ok().entity(apiListDTO).build();
        }
        return null;
    }

    @Override
    public Response apisApiIdGet(String apiId, String accept,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getOrganization(messageContext);
        organization = SubscriptionValidationDataUtil.validateTenantDomain(organization, messageContext);

        if (accept != null && accept.toLowerCase().contains("application/zip")) {
            return getApiAsPlatformGatewayZip(apiId, organization, messageContext);
        }
        return apisGet(organization, apiId, null, null, null, true, accept != null ? accept : "application/json",
                messageContext);
    }

    @Override
    public Response apisApiIdGatewayDeploymentsPost(String apiId, String apiKey, Map<String, Object> requestBody,
            String deploymentId, MessageContext messageContext) throws APIManagementException {
        if (StringUtils.isEmpty(apiKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Missing api-key header").build();
        }
        PlatformGatewayDAO.PlatformGateway gateway;
        try {
            gateway = PlatformGatewayTokenUtil.verifyToken(apiKey);
        } catch (Exception e) {
            log.error("Platform gateway token verification failed with unexpected error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
        }
        if (gateway == null) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway token verification failed: invalid or expired api-key");
            }
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid api-key").build();
        }
        // Validate that the acknowledged API exists and belongs to the same organization as the gateway.
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIInfo apiInfo = apiProvider.getAPIInfoByUUID(apiId);
            if (apiInfo == null || gateway.organizationId == null
                    || !gateway.organizationId.equals(apiInfo.getOrganization())) {
                if (log.isDebugEnabled()) {
                    log.debug("Platform gateway deployment notification rejected: API not found or organization "
                            + "mismatch for apiId=" + apiId + ", gatewayId=" + gateway.id);
                }
                return Response.status(Response.Status.NOT_FOUND).entity("API not found for gateway organization").build();
            }
        } catch (APIManagementException e) {
            log.error("Error validating API for platform gateway deployment notification: apiId=" + apiId
                    + ", gatewayId=" + gateway.id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
        }
        if (log.isDebugEnabled()) {
            log.debug("Platform gateway deployment notification received: apiId=" + apiId + ", gatewayId=" + gateway.id
                    + ", deploymentId=" + deploymentId);
        }
        DeploymentAcknowledgmentResponseDTO response = new DeploymentAcknowledgmentResponseDTO();
        response.setStatus(DeploymentAcknowledgmentResponseDTO.StatusEnum.RECEIVED);
        return Response.ok().entity(response).build();
    }

    @Override
    public Response apisApiIdSubscriptionsGet(String apiId, String apiKey, MessageContext messageContext)
            throws APIManagementException {
        if (StringUtils.isEmpty(apiKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Missing api-key header").build();
        }
        PlatformGatewayDAO.PlatformGateway gateway;
        try {
            gateway = PlatformGatewayTokenUtil.verifyToken(apiKey);
        } catch (Exception e) {
            log.error("Platform gateway token verification failed for subscriptions request", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
        }
        if (gateway == null) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway token verification failed: invalid or expired api-key");
            }
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid api-key").build();
        }
        String organization = gateway.organizationId;
        if (StringUtils.isEmpty(organization)) {
            organization = RestApiUtil.getOrganization(messageContext);
            organization = SubscriptionValidationDataUtil.validateTenantDomain(organization, messageContext);
        }
        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        List<Subscription> allSubs;
        try {
            allSubs = subscriptionValidationDAO.getAllSubscriptionsByOrganization(organization);
        } catch (APIManagementException e) {
            log.error("Error loading subscriptions for organization: " + organization, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Subscription sub : allSubs) {
            if (apiId.equals(sub.getApiUUID())) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", sub.getSubscriptionUUID());
                item.put("apiId", sub.getApiUUID());
                item.put("applicationId", sub.getApplicationUUID());
                item.put("subscriptionToken", "");
                item.put("subscriptionPlanId", sub.getPolicyId());
                item.put("gatewayId", "");
                item.put("status", sub.getSubscriptionState() != null ? sub.getSubscriptionState() : "ACTIVE");
                item.put("createdAt", "1970-01-01T00:00:00Z");
                item.put("updatedAt", "1970-01-01T00:00:00Z");
                result.add(item);
            }
        }
        return Response.ok().entity(result).build();
    }

    private Response getApiAsPlatformGatewayZip(String apiId, String organization, MessageContext messageContext)
            throws APIManagementException {
        // Platform gateway zip requires api-key: resolve gateway → revision → return stored artifact (or 404).
        String apiKey = getApiKeyFromMessageContext(messageContext);
        if (StringUtils.isBlank(apiKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Missing api-key header").build();
        }
        PlatformGatewayDAO.PlatformGateway gateway;
        try {
            gateway = PlatformGatewayTokenUtil.verifyToken(apiKey);
        } catch (Exception e) {
            log.error("Platform gateway token verification failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
        }
        if (gateway == null) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway token verification failed: invalid or expired api-key");
            }
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid api-key").build();
        }
        if (gateway.organizationId == null || !gateway.organizationId.equals(organization)) {
            if (log.isDebugEnabled()) {
                log.debug("Organization mismatch for platform gateway zip: apiId=" + apiId + ", gatewayId=" + gateway.id);
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        PlatformGatewayArtifactService artifactService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayArtifactService();
        if (artifactService == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String gatewayName = gateway.name;
        if (StringUtils.isBlank(gatewayName)) {
            PlatformGatewayService platformGatewayService =
                    ServiceReferenceHolder.getInstance().getPlatformGatewayService();
            if (platformGatewayService != null) {
                try {
                    org.wso2.carbon.apimgt.api.model.PlatformGateway gw =
                            platformGatewayService.getGatewayById(gateway.id);
                    if (gw != null && StringUtils.isNotBlank(gw.getName())) {
                        gatewayName = gw.getName();
                    }
                } catch (APIManagementException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not resolve gateway name for " + gateway.id + ": " + e.getMessage());
                    }
                }
            }
        }
        if (StringUtils.isBlank(gatewayName)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String revisionUuid = artifactService.getRevisionUuidByApiAndGatewayName(apiId, gatewayName);
        if (StringUtils.isBlank(revisionUuid)) {
            if (log.isDebugEnabled()) {
                log.debug("No revision deployed for API " + apiId + " on gateway " + gatewayName);
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String yaml = artifactService.getStoredRevisionArtifact(apiId, revisionUuid);
        if (yaml == null) {
            if (log.isDebugEnabled()) {
                log.debug("No stored artifact for API " + apiId + " revision " + revisionUuid);
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        byte[] zipBytes = buildZipWithYaml(yaml);
        return Response.ok(zipBytes)
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"api.zip\"")
                .build();
    }

    private static String getApiKeyFromMessageContext(MessageContext messageContext) {
        if (messageContext == null) {
            return null;
        }
        HttpServletRequest request = (HttpServletRequest) messageContext.getHttpServletRequest();
        if (request == null) {
            return null;
        }
        String header = request.getHeader("api-key");
        if (StringUtils.isBlank(header)) {
            return null;
        }
        return header;
    }

    private static byte[] buildZipWithYaml(String yamlContent) throws APIManagementException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("api.yaml");
            zos.putNextEntry(entry);
            zos.write(yamlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new APIManagementException("Failed to build zip for API Platform gateway", e);
        }
    }

    public Response deployedAPIRevision(List<DeployedAPIRevisionDTO> deployedAPIRevisionDTOList,
                                        MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        List<String> revisionUUIDs = new ArrayList<>();
        for (DeployedAPIRevisionDTO deployedAPIRevisionDTO : deployedAPIRevisionDTOList) {
            String organizationFromQueryParam = RestApiUtil.getOrganization(messageContext);
            // get revision uuid
            String revisionUUID = apiProvider.getAPIRevisionUUID(Integer.toString(deployedAPIRevisionDTO.getRevisionId()),
                    deployedAPIRevisionDTO.getApiId());
            if (StringUtils.isNotEmpty(organizationFromQueryParam) &&
                    !organizationFromQueryParam.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM)) {
                revisionUUID = apiProvider.getAPIRevisionUUIDByOrganization(Integer.toString(deployedAPIRevisionDTO.getRevisionId()),
                        deployedAPIRevisionDTO.getApiId(), organizationFromQueryParam);
            }
            if (revisionUUID == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(null).build();
            }
            if (!revisionUUIDs.contains(revisionUUID)) {
                revisionUUIDs.add(revisionUUID);
                String organization = apiProvider.getAPIInfoByUUID(deployedAPIRevisionDTO.getApiId()).getOrganization();
                Map<String, Environment> environments = APIUtil.getEnvironments(organization);
                List<DeployedAPIRevision> deployedAPIRevisions = new ArrayList<>();
                for (DeployedEnvInfoDTO deployedEnvInfoDTO : deployedAPIRevisionDTO.getEnvInfo()) {
                    DeployedAPIRevision deployedAPIRevision = new DeployedAPIRevision();
                    deployedAPIRevision.setRevisionUUID(revisionUUID);
                    String environment = deployedEnvInfoDTO.getName();
                    if (environments.get(environment) == null) {
                        RestApiUtil.handleBadRequest("Gateway environment not found: " + environment, log);
                    }
                    deployedAPIRevision.setDeployment(environment);
                    deployedAPIRevision.setVhost(deployedEnvInfoDTO.getVhost());
                    if (StringUtils.isEmpty(deployedEnvInfoDTO.getVhost())) {
                        RestApiUtil.handleBadRequest(
                                "Required field 'vhost' not found in deployment", log
                        );
                    }
                    deployedAPIRevisions.add(deployedAPIRevision);
                }
                apiProvider.addDeployedAPIRevision(deployedAPIRevisionDTO.getApiId(), revisionUUID, deployedAPIRevisions);
            }
        }

        return Response.ok().build();
    }

    @Override
    public Response unDeployedAPIRevision(UnDeployedAPIRevisionDTO unDeployedAPIRevisionDTO, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        apiProvider.removeUnDeployedAPIRevision(unDeployedAPIRevisionDTO.getApiUUID(), unDeployedAPIRevisionDTO.getRevisionUUID(),
                unDeployedAPIRevisionDTO.getEnvironment());
        return Response.ok().build();
    }
}
