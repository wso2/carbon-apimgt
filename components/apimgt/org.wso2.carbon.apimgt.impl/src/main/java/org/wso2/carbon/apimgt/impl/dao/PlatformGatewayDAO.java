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

package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for platform gateway registration (AM_PLATFORM_GATEWAY, AM_GATEWAY_TOKEN).
 */
public class PlatformGatewayDAO {

    private static final Log log = LogFactory.getLog(PlatformGatewayDAO.class);
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
        public final String gatewayId;
        public final String organizationId;

        public TokenWithGateway(String tokenHash, String gatewayId, String organizationId) {
            this.tokenHash = tokenHash;
            this.gatewayId = gatewayId;
            this.organizationId = organizationId;
        }
    }

    /**
     * Insert a platform gateway. Caller must open transaction if also inserting token.
     */
    public void createGateway(Connection connection, PlatformGateway gateway) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Creating platform gateway with name: " + gateway.name + " for organization: "
                    + gateway.organizationId);
        }
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
            if (isUniqueOrDuplicateViolation(e)) {
                String msg = String.format(
                        ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS.getErrorDescription(), gateway.name);
                throw new APIManagementException(msg, ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS);
            }
            log.error("Failed to create platform gateway with name: " + gateway.name + ". Error: " + e.getMessage());
            throw new APIManagementException("Error inserting platform gateway", e);
        }
    }

    private static boolean isUniqueOrDuplicateViolation(SQLException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String msg = t.getMessage();
            if (msg != null && (msg.toUpperCase().contains("UNIQUE") || msg.toUpperCase().contains("DUPLICATE"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Insert a gateway registration token (deterministic hash only, no salt).
     */
    public void createToken(Connection connection, String tokenId, String gatewayId, String tokenHash,
                            Timestamp createdAt) throws APIManagementException {
        try (PreparedStatement ps = connection.prepareStatement(
                SQLConstants.PlatformGatewaySQLConstants.INSERT_TOKEN_SQL)) {
            ps.setString(1, tokenId);
            ps.setString(2, gatewayId);
            ps.setString(3, tokenHash);
            ps.setTimestamp(4, createdAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error inserting platform gateway token", e);
        }
    }

    /**
     * Creates a platform gateway, its token, and registers it in AM_GW_INSTANCES in a single transaction.
     * Keeps connection and transaction boundary inside the DAO layer (service -> impl -> dao).
     */
    public void createGatewayWithTokenAndGatewayInstance(PlatformGateway gateway, String tokenId, String tokenHash,
                                                         List<String> envLabels)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                createGateway(connection, gateway);
                createToken(connection, tokenId, gateway.id, tokenHash, gateway.createdAt);
                GatewayManagementDAO.getInstance().insertGatewayInstance(connection, gateway.id, gateway.organizationId,
                        envLabels, gateway.createdAt, new byte[0]);
                connection.commit();
            } catch (APIManagementException e) {
                connection.rollback();
                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error creating platform gateway", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
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
     * List platform gateways that have a row in AM_GW_INSTANCES (same source as deployment acks and stats).
     * Use this for GET /environments so the list is consistent with deployment feedback.
     * Deduplicates by gateway id so each gateway appears once even if SQL returns multiple rows per env mapping.
     */
    public List<PlatformGateway> listGatewaysByOrganizationWithInstance(String organizationId)
            throws APIManagementException {
        Map<String, PlatformGateway> byId = new LinkedHashMap<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.SELECT_GATEWAYS_BY_ORG_WITH_INSTANCE_SQL)) {
            ps.setString(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlatformGateway gw = mapRowToGateway(rs);
                    if (gw != null && gw.id != null && !byId.containsKey(gw.id)) {
                        byId.put(gw.id, gw);
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error listing platform gateways with instance", e);
        }
        return new ArrayList<>(byId.values());
    }

    /**
     * Update platform gateway metadata (displayName, description, isCritical, properties).
     * Only these columns are updated; name, vhost, functionalityType are not changed.
     * Caller must pass the full values for updatable fields (merge with existing when doing PATCH).
     *
     * @return updated gateway row, or null if no row matched (wrong id or organization)
     */
    public PlatformGateway updateGatewayMetadata(String gatewayId, String organizationId,
                                                  String displayName, String description, boolean isCritical,
                                                  String propertiesJson) throws APIManagementException {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.UPDATE_GATEWAY_METADATA_SQL)) {
            ps.setString(1, displayName);
            ps.setString(2, description);
            ps.setBoolean(3, isCritical);
            ps.setString(4, propertiesJson);
            ps.setTimestamp(5, now);
            ps.setString(6, gatewayId);
            ps.setString(7, organizationId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                return null;
            }
            connection.commit();
            return getGatewayById(gatewayId);
        } catch (SQLException e) {
            throw new APIManagementException("Error updating platform gateway metadata", e);
        }
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
            connection.commit();
        } catch (SQLException e) {
            throw new APIManagementException("Error updating platform gateway active status", e);
        }
    }

    /**
     * Get a single active token by its hash (deterministic SHA-256(plainToken)). Used for direct lookup on verify.
     */
    public TokenWithGateway getActiveTokenByHash(String tokenHash) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.SELECT_ACTIVE_TOKEN_BY_HASH_SQL)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRowToTokenWithGateway(rs) : null;
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting platform gateway token by hash", e);
        }
    }

    /**
     * Get a single active token by its ID (for combined format tokenId.plainToken).
     */
    public TokenWithGateway getActiveTokenById(String tokenId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.SELECT_ACTIVE_TOKEN_BY_ID_SQL)) {
            ps.setString(1, tokenId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRowToTokenWithGateway(rs) : null;
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting platform gateway token by id", e);
        }
    }

    private static TokenWithGateway mapRowToTokenWithGateway(ResultSet rs) throws SQLException {
        return new TokenWithGateway(
                rs.getString("TOKEN_HASH"),
                rs.getString("GATEWAY_UUID"),
                rs.getString("ORGANIZATION_ID")
        );
    }

    /**
     * Map a result set row to PlatformGateway.
     * <p>PROPERTIES is stored as a string type (VARCHAR/TEXT/MEDIUMTEXT/CLOB depending on DB). For moderate
     * sizes (typical for gateway custom JSON), {@link ResultSet#getString(String)} is standard and works for
     * VARCHAR/TEXT/CLOB in modern drivers (Oracle 10g+, MySQL, PostgreSQL, H2). If a driver required CLOB-
     * specific handling or very large values, use {@code getClob()} and stream via {@code getCharacterStream()}.</p>
     */
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

    /**
     * Revoke all active tokens for a gateway (mark them as revoked).
     * Used as part of regenerate token flow.
     */
    public void revokeTokensByGatewayId(Connection connection, String gatewayId, Timestamp revokedAt)
            throws APIManagementException {
        try (PreparedStatement ps = connection.prepareStatement(
                SQLConstants.PlatformGatewaySQLConstants.REVOKE_TOKENS_BY_GATEWAY_ID_SQL)) {
            ps.setTimestamp(1, revokedAt);
            ps.setString(2, gatewayId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error revoking platform gateway tokens", e);
        }
    }

    /**
     * Regenerate a token for a gateway: revoke existing tokens and create a new one in a single transaction.
     *
     * @param gatewayId   the gateway ID
     * @param tokenId     new token ID
     * @param tokenHash   hash of the new plain token
     * @param createdAt   timestamp for the operation
     */
    public void regenerateToken(String gatewayId, String tokenId, String tokenHash, Timestamp createdAt)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                revokeTokensByGatewayId(connection, gatewayId, createdAt);
                createToken(connection, tokenId, gatewayId, tokenHash, createdAt);
                connection.commit();
            } catch (APIManagementException e) {
                connection.rollback();
                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error regenerating platform gateway token", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
    }

    /**
     * Delete a platform gateway and all references (revision deployments, instance mappings, environment,
     * permissions, tokens). Call only after validating that no API revisions are deployed to this gateway.
     *
     * @param gatewayId       platform gateway UUID
     * @param gatewayName     gateway name (used as env name for AM_DEPLOYMENT_REVISION_MAPPING)
     * @param organizationId organization id
     */
    public void deleteGatewayWithReferences(String gatewayId, String gatewayName, String organizationId)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // 1. AM_GW_REVISION_DEPLOYMENT (references AM_GW_INSTANCES.GATEWAY_ID)
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayDeletionSQLConstants.DELETE_AM_GW_REVISION_DEPLOYMENT_BY_GATEWAY_UUID_SQL)) {
                    ps.setString(1, gatewayId);
                    ps.setString(2, organizationId);
                    ps.executeUpdate();
                }
                // 2. AM_DEPLOYMENT_REVISION_MAPPING by env name (= gateway name)
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayDeletionSQLConstants.DELETE_AM_DEPLOYMENT_REVISION_MAPPING_BY_ENV_NAME_SQL)) {
                    ps.setString(1, gatewayName);
                    ps.executeUpdate();
                }
                // 3. AM_GW_INSTANCE_ENV_MAPPING
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayDeletionSQLConstants.DELETE_AM_GW_INSTANCE_ENV_MAPPING_BY_GATEWAY_UUID_SQL)) {
                    ps.setString(1, gatewayId);
                    ps.setString(2, organizationId);
                    ps.executeUpdate();
                }
                // 4. AM_GW_INSTANCES
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayDeletionSQLConstants.DELETE_AM_GW_INSTANCES_BY_UUID_ORG_SQL)) {
                    ps.setString(1, gatewayId);
                    ps.setString(2, organizationId);
                    ps.executeUpdate();
                }
                // 5. AM_GATEWAY_PERMISSIONS (GATEWAY_UUID = gateway id for platform env)
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.DELETE_ALL_GATEWAY_VISIBILITY_PERMISSION_SQL)) {
                    ps.setString(1, gatewayId);
                    ps.executeUpdate();
                }
                // 5b. AM_GW_VHOST and AM_GATEWAY_ENVIRONMENT (platform gateways are added here on create so
                //     GET /environments returns them; must remove so the gateway disappears from the list)
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.GET_ENVIRONMENT_BY_ORGANIZATION_AND_UUID_SQL)) {
                    ps.setString(1, organizationId);
                    ps.setString(2, gatewayId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int envId = rs.getInt("ID");
                            try (PreparedStatement delVhost = connection.prepareStatement(
                                    SQLConstants.DELETE_GATEWAY_VHOSTS_SQL)) {
                                delVhost.setInt(1, envId);
                                delVhost.executeUpdate();
                            }
                            try (PreparedStatement delEnv = connection.prepareStatement(
                                    SQLConstants.DELETE_ENVIRONMENT_SQL)) {
                                delEnv.setString(1, gatewayId);
                                delEnv.executeUpdate();
                            }
                        }
                    }
                }
                // 6. AM_GATEWAY_TOKEN
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewaySQLConstants.DELETE_PLATFORM_GATEWAY_TOKENS_SQL)) {
                    ps.setString(1, gatewayId);
                    ps.executeUpdate();
                }
                // 7. AM_PLATFORM_GATEWAY
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewaySQLConstants.DELETE_PLATFORM_GATEWAY_SQL)) {
                    ps.setString(1, gatewayId);
                    ps.executeUpdate();
                }
                connection.commit();
                if (log.isDebugEnabled()) {
                    log.debug("Deleted platform gateway and all references: id=" + gatewayId);
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Failed to delete platform gateway " + gatewayId + ": " + e.getMessage());
                throw new APIManagementException("Error deleting platform gateway and references", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
    }
}
