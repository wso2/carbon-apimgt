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
 * DAO for platform gateway tokens (AM_GATEWAY_TOKEN) and instance registration (AM_GW_INSTANCES).
 * Platform gateway metadata is stored in AM_GATEWAY_ENVIRONMENT (GATEWAY_TYPE='Platform').
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
        public final String properties;
        public final boolean isActive;
        public final Timestamp createdAt;
        public final Timestamp updatedAt;

        public PlatformGateway(String id, String organizationId, String name, String displayName,
                               String description, String vhost, String properties, boolean isActive,
                               Timestamp createdAt, Timestamp updatedAt) {
            this.id = id;
            this.organizationId = organizationId;
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.vhost = vhost;
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
        public final String gatewayName;

        public TokenWithGateway(String tokenHash, String gatewayId, String organizationId, String gatewayName) {
            this.tokenHash = tokenHash;
            this.gatewayId = gatewayId;
            this.organizationId = organizationId;
            this.gatewayName = gatewayName != null ? gatewayName : "";
        }
    }

    /**
     * Insert a gateway registration token (deterministic hash only, no salt).
     */
    public void createToken(Connection connection, String tokenId, String gatewayId, String tokenHash,
                            Timestamp createdAt) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Creating platform gateway token for gateway: " + gatewayId);
        }
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
     * Creates the token and registers the gateway in AM_GW_INSTANCES in a single transaction.
     * Caller must have already created the environment (AM_GATEWAY_ENVIRONMENT) with UUID = gateway.id.
     */
    public void createGatewayWithTokenAndGatewayInstance(PlatformGateway gateway, String tokenId, String tokenHash,
                                                         List<String> envLabels)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                createToken(connection, tokenId, gateway.id, tokenHash, gateway.createdAt);
                GatewayManagementDAO.getInstance().insertGatewayInstance(connection, gateway.id, gateway.organizationId,
                        envLabels, gateway.createdAt, new byte[0]);
                connection.commit();
                if (log.isInfoEnabled()) {
                    log.info("Successfully created platform gateway with token for gateway: " + gateway.name);
                }
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
     * UUIDs of platform gateways (AM_GATEWAY_ENVIRONMENT with GATEWAY_TYPE='Platform') that have a row in AM_GW_INSTANCES.
     */
    public List<String> getPlatformGatewayUuidsWithInstance(String organizationId) throws APIManagementException {
        List<String> uuids = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewaySQLConstants.SELECT_PLATFORM_GATEWAY_UUIDS_WITH_INSTANCE_SQL)) {
            ps.setString(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    uuids.add(rs.getString("UUID"));
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error listing platform gateway UUIDs with instance", e);
        }
        return uuids;
    }

    /**
     * Build a minimal PlatformGateway from a token row (for WebSocket session; has id and organizationId only).
     */
    public static PlatformGateway fromTokenWithGateway(TokenWithGateway tokenRow) {
        if (tokenRow == null) {
            return null;
        }
        String name = tokenRow.gatewayName != null ? tokenRow.gatewayName : "";
        return new PlatformGateway(tokenRow.gatewayId, tokenRow.organizationId, name, name, null, "",
                null, false, null, null);
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
                rs.getString("ORGANIZATION_ID"),
                rs.getString("GATEWAY_NAME")
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
                // 2. AM_DEPLOYMENT_REVISION_MAPPING for this gateway's env only (scoped by gateway UUID + org to avoid cross-org deletes)
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayDeletionSQLConstants.DELETE_AM_DEPLOYMENT_REVISION_MAPPING_BY_ENV_NAME_SQL)) {
                    ps.setString(1, gatewayId);
                    ps.setString(2, organizationId);
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
                // 6. AM_GATEWAY_TOKEN (before env delete so FK to AM_GATEWAY_ENVIRONMENT is satisfied)
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewaySQLConstants.DELETE_PLATFORM_GATEWAY_TOKENS_SQL)) {
                    ps.setString(1, gatewayId);
                    ps.executeUpdate();
                }
                // 7. AM_GW_VHOST and AM_GATEWAY_ENVIRONMENT
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
