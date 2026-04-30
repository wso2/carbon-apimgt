/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayArtifactService;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayArtifactDAO;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;
import org.wso2.carbon.apimgt.internal.service.DeploymentsApiService;
import org.wso2.carbon.apimgt.internal.service.dto.BatchDeploymentsRequestDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentInfoDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentsResponseDTO;
import org.wso2.carbon.apimgt.internal.service.utils.DeploymentTarGzBuilder;

import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of deployment sync endpoints (GET /deployments, POST /deployments/batch).
 * Requires api-key authentication (platform gateway token).
 */
public class DeploymentsApiServiceImpl implements DeploymentsApiService {

    private static final Log log = LogFactory.getLog(DeploymentsApiServiceImpl.class);

    @Override
    public Response deploymentsGet(String since, MessageContext messageContext) throws APIManagementException {
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
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid api-key").build();
        }
        if (StringUtils.isBlank(gateway.id)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Gateway id not resolved").build();
        }

        Timestamp sinceTs = parseSince(since);
        List<PlatformGatewayArtifactDAO.DeploymentRow> rows =
                PlatformGatewayArtifactDAO.getInstance().listDeploymentsByGatewayEnvUuid(gateway.id, sinceTs);

        GatewayDeploymentsResponseDTO response = new GatewayDeploymentsResponseDTO();
        List<GatewayDeploymentInfoDTO> list = new ArrayList<>();
        for (PlatformGatewayArtifactDAO.DeploymentRow row : rows) {
            GatewayDeploymentInfoDTO info = new GatewayDeploymentInfoDTO();
            info.setDeploymentId(row.getDeploymentId());
            info.setArtifactId(row.getApiUuid());
            info.setKind(GatewayDeploymentInfoDTO.KindEnum.RESTAPI);
            info.setUpdatedAt(row.getDeployedTime() != null
                    ? row.getDeployedTime().toInstant().toString()
                    : null);
            list.add(info);
        }
        response.setDeployments(list);
        return Response.ok(response).build();
    }

    @Override
    public Response deploymentsBatchPost(BatchDeploymentsRequestDTO batchDeploymentsRequest,
            MessageContext messageContext) throws APIManagementException {
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
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid api-key").build();
        }
        if (StringUtils.isBlank(gateway.id)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Gateway id not resolved").build();
        }
        if (batchDeploymentsRequest == null || batchDeploymentsRequest.getDeploymentIds() == null
                || batchDeploymentsRequest.getDeploymentIds().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("deploymentIds required").build();
        }

        PlatformGatewayArtifactService artifactService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayArtifactService();
        if (artifactService == null) {
            log.error("PlatformGatewayArtifactService not available");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Service unavailable").build();
        }

        PlatformGatewayArtifactDAO dao = PlatformGatewayArtifactDAO.getInstance();
        List<DeploymentTarGzBuilder.DeploymentEntry> entries = new ArrayList<>();
        for (String deploymentId : batchDeploymentsRequest.getDeploymentIds()) {
            if (StringUtils.isBlank(deploymentId)) {
                continue;
            }
            if (!dao.isDeploymentOnGateway(gateway.id, deploymentId)) {
                if (log.isWarnEnabled()) {
                    log.warn("Batch request: deployment " + deploymentId + " is not on gateway " + gateway.id + "; skipping");
                }
                continue;
            }
            String apiUuid = dao.getApiUuidByDeploymentId(gateway.id, deploymentId);
            if (apiUuid == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No API found for deployment/revision ID: " + deploymentId);
                }
                continue;
            }
            String yaml = artifactService.getStoredArtifact(apiUuid, gateway.id);
            if (yaml != null) {
                entries.add(new DeploymentTarGzBuilder.DeploymentEntry(deploymentId, apiUuid, yaml));
            }
        }

        byte[] tarBytes;
        try {
            tarBytes = DeploymentTarGzBuilder.buildTar(entries);
        } catch (IOException e) {
            log.error("Failed to build TAR for deployment batch", e);
            throw new APIManagementException("Failed to build deployment archive", e);
        }

        // Payload is uncompressed TAR; GZIPOutInterceptor (if applied) adds Content-Encoding: gzip.
        // Use application/x-tar so type matches uncompressed content; filename hints .tar.gz when gzip is applied.
        return Response.ok(tarBytes)
                .type("application/x-tar")
                .header("Content-Disposition", "attachment; filename=\"deployments.tar.gz\"")
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
        return StringUtils.isBlank(header) ? null : header;
    }

    private static Timestamp parseSince(String since) {
        if (StringUtils.isBlank(since)) {
            return null;
        }
        try {
            return Timestamp.from(Instant.parse(since));
        } catch (DateTimeParseException e) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid since parameter (expected ISO8601): " + since);
            }
            return null;
        }
    }
}
