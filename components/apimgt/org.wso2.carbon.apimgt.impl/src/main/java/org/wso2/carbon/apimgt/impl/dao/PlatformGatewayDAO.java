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

package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for platform gateway registration (AM_PLATFORM_GATEWAY, AM_PLATFORM_GATEWAY_TOKEN).
 */
public class PlatformGatewayDAO {

    private static final PlatformGatewayDAO INSTANCE = new PlatformGatewayDAO();

    private PlatformGatewayDAO() {
    }

    public static PlatformGatewayDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Result of a platform gateway row.
     * properties: JSON string (optional) for custom key-value properties.
     */
    public static class PlatformGateway {
        public final String id;
        public final String organizationId;
        public final String name;
        public final String displayName;
        public final String description;
        public final String vhost;
        public final boolean isCritical;
        public final String functionalityType;
        public final String properties;
        public final boolean isActive;
        public final Timestamp createdAt;
        public final Timestamp updatedAt;

        public PlatformGateway(String id, String organizationId, String name, String displayName,
                               String description, String vhost, boolean isCritical, String functionalityType,
                               String properties, boolean isActive, Timestamp createdAt, Timestamp updatedAt) {
            this.id = id;
            this.organizationId = organizationId;
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.vhost = vhost;
            this.isCritical = isCritical;
            this.functionalityType = functionalityType;
            this.properties = properties;
            this.isActive = isActive;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }

    /**
     * Active token row with gateway info (for verification).
     */
    public static class TokenWithGateway {
        public final String tokenHash;
        public final String salt;
        public final String gatewayId;
        public final String organizationId;

        public TokenWithGateway(String tokenHash, String salt, String gatewayId, String organizationId) {
            this.tokenHash = tokenHash;
            this.salt = salt;
            this.gatewayId = gatewayId;
            this.organizationId = organizationId;
        }
    }

    /**
     * Insert a platform gateway. Caller must open transaction if also inserting token.
     */
    public void createGateway(Connection connection, PlatformGateway gateway) throws APIManagementException {
        try (PreparedStatement ps = connection.prepareStatement(
                SQLConstants.PlatformGatewaySQLConstants.INSERT_GATEWAY_SQL)) {
            ps.setString(1, gateway.id);
            ps.setString(2, gateway.organizationId);
            ps.setString(3, gateway.name);
            ps.setString(4, gateway.displayName);
            ps.setString(5, gateway.description);
            ps.setString(6, gateway.vhost);
            ps.setBoolean(7, gateway.isCritical);
            ps.setString(8, gateway.functionalityType);
            ps.setString(9, gateway.properties);
            ps.setBoolean(10, gateway.isActive);
            ps.setTimestamp(11, gateway.createdAt);
            ps.setTimestamp(12, gateway.updatedAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error inserting platform gateway", e);
        }
    }

    /**
     * Insert a gateway registration token.
     */
    public void createToken(Connection connection, String tokenId, String gatewayId, String tokenHash,
                            String salt, Timestamp createdAt) throws APIManagementException {
        try (PreparedStatement ps = connection.prepareStatement(
                SQLConstants.PlatformGatewaySQLConstants.INSERT_TOKEN_SQL)) {
            ps.setString(1, tokenId);
            ps.setString(2, gatewayId);
            ps.setString(3, tokenHash);
            ps.setString(4, salt);
            ps.setTimestamp(5, createdAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error inserting platform gateway token", e);
        }
    }

    /**
     * Get platform gateway by ID.
     */
    public PlatformGateway getGatewayById(String id) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.SELECT_GATEWAY_BY_ID_SQL)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRowToGateway(rs) : null;
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting platform gateway by id", e);
        }
    }

    /**
     * Get platform gateway by name and organization (for uniqueness check).
     */
    public PlatformGateway getGatewayByNameAndOrganization(String name, String organizationId)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.SELECT_GATEWAY_BY_NAME_AND_ORG_SQL)) {
            ps.setString(1, name);
            ps.setString(2, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getGatewayById(rs.getString("ID"));
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting platform gateway by name and org", e);
        }
    }

    /**
     * List platform gateways by organization.
     */
    public List<PlatformGateway> listGatewaysByOrganization(String organizationId) throws APIManagementException {
        List<PlatformGateway> list = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.SELECT_GATEWAYS_BY_ORG_SQL)) {
            ps.setString(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToGateway(rs));
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error listing platform gateways", e);
        }
        return list;
    }

    /**
     * Update gateway active status (e.g. connected/disconnected for control plane WebSocket).
     */
    public void updateGatewayActiveStatus(String gatewayId, boolean active) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.UPDATE_GATEWAY_ACTIVE_SQL)) {
            ps.setBoolean(1, active);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, gatewayId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error updating platform gateway active status", e);
        }
    }

    /**
     * Load all active tokens with their gateway info (for verification).
     */
    public List<TokenWithGateway> getActiveTokensWithGateway() throws APIManagementException {
        List<TokenWithGateway> list = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.SELECT_ACTIVE_TOKENS_WITH_GATEWAY_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TokenWithGateway(
                        rs.getString("TOKEN_HASH"),
                        rs.getString("SALT"),
                        rs.getString("GATEWAY_UUID"),
                        rs.getString("ORGANIZATION_ID")
                ));
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error loading active platform gateway tokens", e);
        }
        return list;
    }

    private static PlatformGateway mapRowToGateway(ResultSet rs) throws SQLException {
        return new PlatformGateway(
                rs.getString("ID"),
                rs.getString("ORGANIZATION_ID"),
                rs.getString("NAME"),
                rs.getString("DISPLAY_NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("VHOST"),
                rs.getBoolean("IS_CRITICAL"),
                rs.getString("FUNCTIONALITY_TYPE"),
                rs.getString("PROPERTIES"),
                rs.getBoolean("IS_ACTIVE"),
                rs.getTimestamp("CREATED_AT"),
                rs.getTimestamp("UPDATED_AT")
        );
    }
}
