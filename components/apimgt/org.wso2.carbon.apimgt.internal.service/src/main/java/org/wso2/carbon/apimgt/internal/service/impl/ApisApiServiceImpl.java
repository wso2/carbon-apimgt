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
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.DeployedAPIRevision;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.subscription.API;
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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayArtifactDAO;
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
                log.debug("Platform gateway deployment notify: invalid or expired api-key for apiId=" + apiId);
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
                    log.debug("Platform gateway deployment notify: API not found or organization mismatch apiId="
                            + apiId + ", gatewayId=" + gateway.id);
                }
                return Response.status(Response.Status.NOT_FOUND).entity("API not found for gateway organization").build();
            }
        } catch (APIManagementException e) {
            log.error("Error validating API for platform gateway deployment notification: apiId=" + apiId
                    + ", gatewayId=" + gateway.id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
        }
        try {
            Response persistError =
                    persistPlatformGatewayDeploymentNotification(apiId, gateway, deploymentId, requestBody);
            if (persistError != null) {
                return persistError;
            }
        } catch (APIManagementException e) {
            log.error("Failed to persist platform gateway deployment status for apiId=" + apiId + ", gatewayId="
                    + gateway.id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to persist deployment status")
                    .build();
        }
        DeploymentAcknowledgmentResponseDTO response = new DeploymentAcknowledgmentResponseDTO();
        response.setStatus(DeploymentAcknowledgmentResponseDTO.StatusEnum.RECEIVED);
        return Response.ok().entity(response).build();
    }

    /**
     * Records platform gateway deployment outcome in AM_GW_REVISION_DEPLOYMENT (same data path as
     * {@link org.wso2.carbon.apimgt.internal.service.impl.NotifyApiDeploymentStatusApiServiceImpl}) so Publisher
     * deployment stats (liveGatewayCount / deployedGatewayCount) update for Platform gateways.
     */
    private Response persistPlatformGatewayDeploymentNotification(String apiId, PlatformGatewayDAO.PlatformGateway gateway,
            String deploymentIdQueryParam, Map<String, Object> requestBody) throws APIManagementException {

        String correlationKey = resolveRevisionUuidForPlatformNotify(deploymentIdQueryParam, requestBody);

        if (StringUtils.isBlank(correlationKey)) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway deployment notify: missing revision/deployment id for apiId=" + apiId
                        + ", gatewayId=" + gateway.id);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    "deploymentId query parameter or deploymentId/revisionId in JSON body is required to correlate "
                            + "gateway deployment with an API revision").build();
        }

        RevisionUuidResolveOutcome resolved =
                resolvePlatformNotifyCorrelationToRevisionUuid(apiId, gateway, correlationKey.trim());
        if (resolved.failureResponse != null) {
            return resolved.failureResponse;
        }
        String revisionUuid = resolved.revisionUuid;

        GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
        String organization = gateway.organizationId;
        if (!dao.gatewayExists(gateway.id, organization)) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway deployment notify: gateway not registered gatewayUuid=" + gateway.id
                        + ", organization=" + organization);
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Gateway instance not registered").build();
        }
        if (!dao.apiExists(apiId)) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway deployment notify: api not in AM_API for apiId=" + apiId);
            }
            return Response.status(Response.Status.NOT_FOUND).entity("API not found").build();
        }

        long timeStamp = resolveNotifyTimestampMillis(requestBody);
        String[] statusAndAction = mapPlatformPayloadToNotifyStatusAndAction(requestBody);
        String notifyStatus = statusAndAction[0];
        String action = statusAndAction[1];

        if (dao.deploymentExists(gateway.id, apiId)) {
            if (dao.isDeploymentTimestampInorder(gateway.id, apiId, timeStamp)) {
                dao.updateDeployment(gateway.id, apiId, organization, notifyStatus, action, revisionUuid, timeStamp);
            }
        } else {
            dao.insertDeployment(gateway.id, apiId, organization, notifyStatus, action, revisionUuid, timeStamp);
        }
        return null;
    }

    /**
     * Maps a gateway-supplied correlation token (revision UUID, opaque deployment id, or legacy body {@code revisionId})
     * to the AM_REVISION row UUID for {@code apiId}. Matches the resolution order used for WebSocket
     * {@code deployment.ack} in {@link org.wso2.carbon.apimgt.internal.service.websocket.GatewayConnectEndpoint}.
     */
    private RevisionUuidResolveOutcome resolvePlatformNotifyCorrelationToRevisionUuid(String apiId,
            PlatformGatewayDAO.PlatformGateway gateway, String correlationKey) throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        APIRevision revisionByUuid = apiMgtDAO.getRevisionByRevisionUUID(correlationKey);
        if (revisionByUuid != null) {
            if (!apiId.equals(revisionByUuid.getApiUUID())) {
                if (log.isDebugEnabled()) {
                    log.debug("Platform gateway deployment notify: revision UUID belongs to another API; apiId="
                            + apiId + ", revisionApiId=" + revisionByUuid.getApiUUID() + ", gatewayId=" + gateway.id);
                }
                return RevisionUuidResolveOutcome.fail(Response.status(Response.Status.BAD_REQUEST).entity(
                        "revisionUuid does not refer to a revision of the requested API").build());
            }
            return RevisionUuidResolveOutcome.ok(revisionByUuid.getRevisionUUID());
        }

        String fromArtifactCache = PlatformGatewayArtifactDAO.getInstance()
                .getArtifactRevisionIdByGatewayEnvAndDeploymentId(gateway.id, correlationKey);
        if (StringUtils.isBlank(fromArtifactCache)) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway deployment notify: could not resolve correlation key to a revision UUID; "
                        + "apiId=" + apiId + ", gatewayId=" + gateway.id);
            }
            return RevisionUuidResolveOutcome.fail(Response.status(Response.Status.NOT_FOUND).entity(
                    "Unknown deployment or revision correlation for this API and gateway").build());
        }

        APIRevision revisionFromDeployment = apiMgtDAO.getRevisionByRevisionUUID(fromArtifactCache);
        if (revisionFromDeployment == null) {
            log.warn("Platform gateway deployment notify: artifact cache returned revision UUID not present in AM_REVISION; "
                    + "apiId=" + apiId + ", gatewayId=" + gateway.id);
            return RevisionUuidResolveOutcome.fail(Response.status(Response.Status.NOT_FOUND).entity(
                    "Resolved revision is not available for this API").build());
        }
        if (!apiId.equals(revisionFromDeployment.getApiUUID())) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway deployment notify: deployment id maps to a revision of another API; "
                        + "apiId=" + apiId + ", revisionApiId=" + revisionFromDeployment.getApiUUID()
                        + ", gatewayId=" + gateway.id);
            }
            return RevisionUuidResolveOutcome.fail(Response.status(Response.Status.BAD_REQUEST).entity(
                    "deployment correlation does not map to a revision of the requested API").build());
        }
        return RevisionUuidResolveOutcome.ok(fromArtifactCache);
    }

    private static final class RevisionUuidResolveOutcome {
        private final String revisionUuid;
        private final Response failureResponse;

        private RevisionUuidResolveOutcome(String revisionUuid, Response failureResponse) {
            this.revisionUuid = revisionUuid;
            this.failureResponse = failureResponse;
        }

        static RevisionUuidResolveOutcome ok(String revisionUuid) {
            return new RevisionUuidResolveOutcome(revisionUuid, null);
        }

        static RevisionUuidResolveOutcome fail(Response failureResponse) {
            return new RevisionUuidResolveOutcome(null, failureResponse);
        }
    }

    /**
     * Extracts a correlation token from the notify request. Precedence: JSON {@code revisionUuid}, then query
     * {@code deploymentId}, then JSON {@code deploymentId} / {@code revisionId}. The value may be an opaque platform
     * deployment id; callers must translate it via {@link #resolvePlatformNotifyCorrelationToRevisionUuid}.
     */
    private static String resolveRevisionUuidForPlatformNotify(String deploymentIdQueryParam,
            Map<String, Object> requestBody) {
        if (requestBody != null) {
            String fromBodyUuid = StringUtils.trimToEmpty(stringValueFromBody(requestBody, "revisionUuid"));
            if (StringUtils.isNotBlank(fromBodyUuid)) {
                return fromBodyUuid;
            }
        }
        String revisionUuid = StringUtils.trimToEmpty(deploymentIdQueryParam);
        if (StringUtils.isNotBlank(revisionUuid)) {
            return revisionUuid;
        }
        if (requestBody == null) {
            return null;
        }
        revisionUuid = StringUtils.trimToEmpty(stringValueFromBody(requestBody, "deploymentId"));
        if (StringUtils.isNotBlank(revisionUuid)) {
            return revisionUuid;
        }
        return StringUtils.trimToEmpty(stringValueFromBody(requestBody, "revisionId"));
    }

    private static String stringValueFromBody(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static long resolveNotifyTimestampMillis(Map<String, Object> requestBody) {
        long fallback = System.currentTimeMillis();
        if (requestBody == null) {
            return fallback;
        }
        Long fromDeployed = extractEpochMillis(requestBody.get("deployedAt"));
        if (fromDeployed != null) {
            return fromDeployed;
        }
        Long fromUpdated = extractEpochMillis(requestBody.get("updatedAt"));
        if (fromUpdated != null) {
            return fromUpdated;
        }
        return fallback;
    }

    private static Long extractEpochMillis(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Maps API Platform gateway JSON ({@code status} / {@code desiredState}) to AM_GW_REVISION_DEPLOYMENT
     * STATUS and ACTION (aligned with internal notify-api deployment DTOs).
     * <p>
     * Outcome ({@code FAILURE} vs success) is inferred from {@code status} only. {@code DEPLOY} vs {@code UNDEPLOY}
     * follows {@code desiredState} when set, otherwise falls back to {@code status}.
     */
    private static String[] mapPlatformPayloadToNotifyStatusAndAction(Map<String, Object> requestBody) {
        String status = "";
        String desiredState = "";
        if (requestBody != null) {
            status = StringUtils.defaultString(stringValueFromBody(requestBody, "status"));
            desiredState = StringUtils.defaultString(stringValueFromBody(requestBody, "desiredState"));
        }
        String actionSource = StringUtils.isNotBlank(desiredState) ? desiredState : status;
        String actionSourceTrimmed = actionSource.trim();
        String actionSourceLower = actionSourceTrimmed.toLowerCase();
        String statusLower = status.toLowerCase();

        if (statusLower.contains("fail") || statusLower.contains("error")) {
            String failureAction = APIConstants.AuditLogConstants.DEPLOY;
            if (APIConstants.AuditLogConstants.UNDEPLOYED.equalsIgnoreCase(actionSourceTrimmed)
                    || "undeployed".equals(actionSourceLower)) {
                failureAction = APIConstants.AuditLogConstants.UNDEPLOY;
            }
            return new String[] { APIConstants.GatewayNotification.DEPLOYMENT_STATUS_FAILURE, failureAction };
        }
        if (APIConstants.AuditLogConstants.UNDEPLOYED.equalsIgnoreCase(actionSourceTrimmed)
                || "undeployed".equals(actionSourceLower)) {
            return new String[] { APIConstants.GatewayNotification.DEPLOYMENT_STATUS_SUCCESS,
                    APIConstants.AuditLogConstants.UNDEPLOY };
        }
        return new String[] { APIConstants.GatewayNotification.DEPLOYMENT_STATUS_SUCCESS,
                APIConstants.AuditLogConstants.DEPLOY };
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
        if (StringUtils.isBlank(gateway.id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String yaml = artifactService.getStoredArtifact(apiId, gateway.id);
        if (yaml == null) {
            if (log.isDebugEnabled()) {
                log.debug("No platform gateway artifact for API " + apiId + " on gateway " + gateway.id);
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
