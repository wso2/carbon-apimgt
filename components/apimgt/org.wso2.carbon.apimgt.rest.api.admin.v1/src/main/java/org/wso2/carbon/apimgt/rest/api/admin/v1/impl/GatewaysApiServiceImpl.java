/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CreatePlatformGatewayRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayListDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

/**
 * Implementation of Platform Gateways Admin API (register/list self-hosted gateways with registration token).
 */
public class GatewaysApiServiceImpl implements GatewaysApiService {

    private static final Log log = LogFactory.getLog(GatewaysApiServiceImpl.class);

    @Override
    public Response gatewaysPost(CreatePlatformGatewayRequestDTO body, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        validateCreateBody(body);

        PlatformGatewayDAO dao = PlatformGatewayDAO.getInstance();
        if (dao.getGatewayByNameAndOrganization(body.getName(), organization) != null) {
            throw new APIManagementException(
                    String.format("A platform gateway with name '%s' already exists in the organization", body.getName()),
                    ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS);
        }

        String gatewayId = UUID.randomUUID().toString();
        String plainToken = PlatformGatewayTokenUtil.generateToken();
        byte[] saltBytes = PlatformGatewayTokenUtil.generateSalt();
        String tokenHash;
        try {
            tokenHash = PlatformGatewayTokenUtil.hashToken(plainToken, saltBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new APIManagementException("Error hashing gateway token", e);
        }
        String saltHex = org.apache.commons.codec.binary.Hex.encodeHexString(saltBytes);

        Timestamp now = Timestamp.from(Instant.now());
        PlatformGatewayDAO.PlatformGateway gateway = new PlatformGatewayDAO.PlatformGateway(
                gatewayId,
                organization,
                body.getName(),
                body.getDisplayName(),
                body.getDescription(),
                body.getVhost(),
                body.isIsCritical() != null && body.isIsCritical(),
                body.getFunctionalityType(),
                true,
                now,
                now
        );

        Connection connection = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            dao.createGateway(connection, gateway);
            dao.createToken(connection, UUID.randomUUID().toString(), gatewayId, tokenHash, saltHex, now);
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.warn("Rollback failed", rollbackEx);
                }
            }
            throw new APIManagementException("Error creating platform gateway", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("Error closing connection", e);
                }
            }
        }

        PlatformGatewayDTO dto = toDTO(gateway);
        dto.setRegistrationToken(plainToken);
        try {
            URI location = new URI(RestApiConstants.RESOURCE_PATH_PLATFORM_GATEWAYS + "/" + gatewayId);
            return Response.created(location).entity(dto).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.CREATED).entity(dto).build();
        }
    }

    @Override
    public Response gatewaysGet(MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        PlatformGatewayDAO dao = PlatformGatewayDAO.getInstance();
        List<PlatformGatewayDAO.PlatformGateway> gateways = dao.listGatewaysByOrganization(organization);
        PlatformGatewayListDTO listDTO = new PlatformGatewayListDTO();
        listDTO.setCount(gateways.size());
        listDTO.setList(gateways.stream().map(this::toDTO).collect(Collectors.toList()));
        return Response.ok().entity(listDTO).build();
    }

    private void validateCreateBody(CreatePlatformGatewayRequestDTO body) throws APIManagementException {
        if (body == null) {
            throw RestApiUtil.buildBadRequestException("Request body is required");
        }
        if (StringUtils.isBlank(body.getName())) {
            throw RestApiUtil.buildBadRequestException("name is required");
        }
        if (StringUtils.isBlank(body.getDisplayName())) {
            throw RestApiUtil.buildBadRequestException("displayName is required");
        }
        if (StringUtils.isBlank(body.getVhost())) {
            throw RestApiUtil.buildBadRequestException("vhost is required");
        }
        if (StringUtils.isBlank(body.getFunctionalityType())) {
            throw RestApiUtil.buildBadRequestException("functionalityType is required");
        }
    }

    private PlatformGatewayDTO toDTO(PlatformGatewayDAO.PlatformGateway g) {
        PlatformGatewayDTO dto = new PlatformGatewayDTO();
        dto.setId(g.id);
        dto.setOrganizationId(g.organizationId);
        dto.setName(g.name);
        dto.setDisplayName(g.displayName);
        dto.setDescription(g.description);
        dto.setVhost(g.vhost);
        dto.setIsCritical(g.isCritical);
        dto.setFunctionalityType(g.functionalityType);
        dto.setIsActive(g.isActive);
        dto.setCreatedAt(g.createdAt);
        dto.setUpdatedAt(g.updatedAt);
        return dto;
    }
}
