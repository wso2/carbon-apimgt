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

package org.wso2.carbon.apimgt.impl.service;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.api.model.CreatePlatformGatewayResult;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of platform gateway service. Delegates persistence to {@link PlatformGatewayDAO}.
 */
public class PlatformGatewayServiceImpl implements PlatformGatewayService {

    private static final PlatformGatewayServiceImpl INSTANCE = new PlatformGatewayServiceImpl();

    public static PlatformGatewayServiceImpl getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayServiceImpl() {
    }

    @Override
    public CreatePlatformGatewayResult createGateway(String organizationId, String name, String displayName,
                                                     String description, String vhost, boolean isCritical,
                                                     String functionalityType, String propertiesJson)
            throws APIManagementException {
        PlatformGatewayDAO dao = PlatformGatewayDAO.getInstance();
        if (dao.getGatewayByNameAndOrganization(name, organizationId) != null) {
            throw new APIManagementException(
                    String.format("A platform gateway with name '%s' already exists in the organization", name),
                    org.wso2.carbon.apimgt.api.ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS);
        }

        String gatewayId = UUID.randomUUID().toString();
        String tokenId = PlatformGatewayTokenUtil.generateTokenId();
        String plainToken = PlatformGatewayTokenUtil.generateToken();
        String tokenHash;
        try {
            tokenHash = PlatformGatewayTokenUtil.hashToken(plainToken);
        } catch (NoSuchAlgorithmException e) {
            throw new APIManagementException("Error hashing gateway token", e);
        }

        Timestamp now = Timestamp.from(Instant.now());
        PlatformGatewayDAO.PlatformGateway gateway = new PlatformGatewayDAO.PlatformGateway(
                gatewayId,
                organizationId,
                name,
                displayName,
                description,
                vhost,
                isCritical,
                functionalityType,
                propertiesJson,
                false,
                now,
                now
        );

        Connection connection = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            dao.createGateway(connection, gateway);
            dao.createToken(connection, tokenId, gatewayId, tokenHash, now);
            connection.commit();
        } catch (APIManagementException e) {
            rollbackQuietly(connection);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw new APIManagementException("Error creating platform gateway", e);
        } finally {
            closeQuietly(connection);
        }

        String registrationToken = tokenId + PlatformGatewayTokenUtil.COMBINED_TOKEN_SEPARATOR + plainToken;
        return new CreatePlatformGatewayResult(toApiModel(gateway), registrationToken);
    }

    @Override
    public PlatformGateway getGatewayByNameAndOrganization(String name, String organizationId)
            throws APIManagementException {
        PlatformGatewayDAO.PlatformGateway g = PlatformGatewayDAO.getInstance()
                .getGatewayByNameAndOrganization(name, organizationId);
        return g != null ? toApiModel(g) : null;
    }

    @Override
    public List<PlatformGateway> listGatewaysByOrganization(String organizationId) throws APIManagementException {
        List<PlatformGatewayDAO.PlatformGateway> list = PlatformGatewayDAO.getInstance()
                .listGatewaysByOrganization(organizationId);
        return list.stream().map(this::toApiModel).collect(Collectors.toList());
    }

    @Override
    public PlatformGateway getGatewayById(String id) throws APIManagementException {
        PlatformGatewayDAO.PlatformGateway g = PlatformGatewayDAO.getInstance().getGatewayById(id);
        return g != null ? toApiModel(g) : null;
    }

    private static PlatformGateway toApiModel(PlatformGatewayDAO.PlatformGateway g) {
        PlatformGateway api = new PlatformGateway();
        api.setId(g.id);
        api.setOrganizationId(g.organizationId);
        api.setName(g.name);
        api.setDisplayName(g.displayName);
        api.setDescription(g.description);
        api.setVhost(g.vhost);
        api.setCritical(g.isCritical);
        api.setFunctionalityType(g.functionalityType);
        api.setProperties(g.properties);
        api.setActive(g.isActive);
        api.setCreatedAt(g.createdAt != null ? new java.util.Date(g.createdAt.getTime()) : null);
        api.setUpdatedAt(g.updatedAt != null ? new java.util.Date(g.updatedAt.getTime()) : null);
        return api;
    }

    private static void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
