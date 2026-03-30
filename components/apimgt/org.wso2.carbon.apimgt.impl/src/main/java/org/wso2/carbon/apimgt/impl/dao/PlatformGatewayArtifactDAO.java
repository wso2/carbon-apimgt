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
import org.wso2.carbon.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for platform gateway deployed artifact storage using a dedicated platform cache table.
 * Resolution (apiId, gateway name/UUID) → REVISION_UUID uses AM_DEPLOYMENT_REVISION_MAPPING and
 * AM_REVISION / AM_GATEWAY_ENVIRONMENT (main DB).
 * Artifact read/write uses AM_GW_PLATFORM_API_ARTIFACTS (artifact synchronizer DB).
 */
public class PlatformGatewayArtifactDAO {

    private static final Log log = LogFactory.getLog(PlatformGatewayArtifactDAO.class);
    private static final PlatformGatewayArtifactDAO INSTANCE = new PlatformGatewayArtifactDAO();

    private PlatformGatewayArtifactDAO() {
    }

    public static PlatformGatewayArtifactDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Resolve REVISION_UUID for (apiId, gateway name) from AM_DEPLOYMENT_REVISION_MAPPING join AM_REVISION.
     *
     * @param apiId       API UUID
     * @param gatewayName environment/gateway name (NAME in AM_DEPLOYMENT_REVISION_MAPPING)
     * @return REVISION_UUID or null if no deployment found
     */
    public String getRevisionUuidByApiAndGatewayName(String apiId, String gatewayName)
            throws APIManagementException {
        if (apiId == null || gatewayName == null) {
            log.warn("Cannot resolve revision UUID - apiId or gatewayName is null");
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Resolving revision UUID for API: " + apiId + ", gateway: " + gatewayName);
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_REVISION_UUID_BY_API_AND_GATEWAY_NAME)) {
            ps.setString(1, apiId.trim());
            ps.setString(2, gatewayName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("REVISION_UUID") : null;
            }
        } catch (SQLException e) {
            log.error("Error resolving revision for API " + apiId + " and gateway " + gatewayName, e);
            throw new APIManagementException("Error resolving revision for platform gateway", e);
        }
    }

    /**
     * Resolve REVISION_UUID for (apiId, gateway environment UUID).
     */
    public String getRevisionUuidByApiAndGatewayEnvUuid(String apiId, String gatewayEnvUuid)
            throws APIManagementException {
        if (apiId == null || gatewayEnvUuid == null) {
            return null;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_REVISION_UUID_BY_API_AND_GATEWAY_ENV_UUID)) {
            ps.setString(1, apiId.trim());
            ps.setString(2, gatewayEnvUuid.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("REVISION_UUID") : null;
            }
        } catch (SQLException e) {
            log.error("Error resolving revision for API " + apiId + " and gateway environment " + gatewayEnvUuid, e);
            throw new APIManagementException("Error resolving revision for platform gateway", e);
        }
    }

    /**
     * Get stored deployed artifact (YAML) from the dedicated platform cache table.
     *
     * @param apiId API ID (UUID)
     * @param gatewayEnvUuid gateway environment UUID
     * @return YAML content or null if not found
     */
    public String getArtifact(String apiId, String gatewayEnvUuid) throws APIManagementException {
        if (apiId == null || gatewayEnvUuid == null) {
            return null;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_ARTIFACT_BY_API_AND_GATEWAY_SQL)) {
            ps.setString(1, apiId.trim());
            ps.setString(2, gatewayEnvUuid.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                byte[] bytes = rs.getBytes("ARTIFACT");
                return bytes != null ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8) : null;
            }
        } catch (SQLException e) {
            log.error("Error getting platform deployed artifact for API " + apiId + " on gateway "
                    + gatewayEnvUuid, e);
            throw new APIManagementException("Error getting platform deployed artifact", e);
        }
    }

    /**
     * Save or replace platform deployed artifact in the dedicated platform cache table.
     * INSERT or UPDATE one row (API_ID, GATEWAY_ENV_UUID, DEPLOYMENT_ID, REVISION_ID, ARTIFACT).
     *
     * @param apiId API ID (UUID)
     * @param revisionId REVISION_UUID used to build the artifact
     * @param gatewayEnvUuid gateway environment UUID
     * @param deploymentId gateway deployment ID
     * @param yamlContent platform api.yaml content (stored as bytes)
     */
    public void saveArtifact(String apiId, String revisionId, String gatewayEnvUuid, String deploymentId,
                             String yamlContent)
            throws APIManagementException {
        String validationError = "API ID, revision ID, gateway environment UUID and deployment ID are required";
        if (apiId == null || revisionId == null || gatewayEnvUuid == null || deploymentId == null) {
            log.error("Cannot save platform artifact - " + validationError);
            throw new APIManagementException(validationError);
        }
        apiId = apiId.trim();
        revisionId = revisionId.trim();
        gatewayEnvUuid = gatewayEnvUuid.trim();
        deploymentId = deploymentId.trim();
        if (apiId.isEmpty() || revisionId.isEmpty() || gatewayEnvUuid.isEmpty() || deploymentId.isEmpty()) {
            log.error("Cannot save platform artifact - " + validationError);
            throw new APIManagementException(validationError);
        }
        if (yamlContent == null) {
            log.error("Cannot save platform artifact - YAML content is required for API: " + apiId);
            throw new APIManagementException("YAML content is required");
        }
        log.info("Saving platform artifact for API: " + apiId + ", gateway: " + gatewayEnvUuid
                + ", deployment: " + deploymentId);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        byte[] artifactBytes = yamlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    SQLConstants.PlatformGatewayArtifactSQLConstants.UPDATE_ARTIFACT_BY_API_AND_GATEWAY_SQL)) {
                ps.setBytes(1, artifactBytes);
                ps.setTimestamp(2, now);
                ps.setString(3, revisionId);
                ps.setString(4, deploymentId);
                ps.setString(5, apiId);
                ps.setString(6, gatewayEnvUuid);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement insertPs = connection.prepareStatement(
                            SQLConstants.PlatformGatewayArtifactSQLConstants.INSERT_ARTIFACT_SQL)) {
                        insertPs.setBytes(1, artifactBytes);
                        insertPs.setTimestamp(2, now);
                        insertPs.setString(3, apiId);
                        insertPs.setString(4, revisionId);
                        insertPs.setString(5, gatewayEnvUuid);
                        insertPs.setString(6, deploymentId);
                        insertPs.executeUpdate();
                    } catch (SQLIntegrityConstraintViolationException e) {
                        try (PreparedStatement retryPs = connection.prepareStatement(
                                SQLConstants.PlatformGatewayArtifactSQLConstants.UPDATE_ARTIFACT_BY_API_AND_GATEWAY_SQL)) {
                            retryPs.setBytes(1, artifactBytes);
                            retryPs.setTimestamp(2, now);
                            retryPs.setString(3, revisionId);
                            retryPs.setString(4, deploymentId);
                            retryPs.setString(5, apiId);
                            retryPs.setString(6, gatewayEnvUuid);
                            retryPs.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error saving platform deployed artifact for API " + apiId + " on gateway "
                    + gatewayEnvUuid + " deployment " + deploymentId, e);
            throw new APIManagementException("Error saving platform deployed artifact", e);
        }
    }

    /**
     * Delete deployed artifact rows for (apiId, revisionId). Used when a revision is deleted.
     */
    public void deleteRevisionArtifact(String apiId, String revisionId) throws APIManagementException {
        if (apiId == null || revisionId == null) {
            return;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.DELETE_ARTIFACTS_BY_API_AND_REVISION_SQL)) {
            ps.setString(1, apiId.trim());
            ps.setString(2, revisionId.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting platform revision artifact for API " + apiId + " revision " + revisionId, e);
            throw new APIManagementException("Error deleting platform revision artifact", e);
        }
    }

    /**
     * Delete deployed artifact for (apiId, gatewayEnvUuid). Used on undeploy.
     */
    public void deleteArtifactForGateway(String apiId, String gatewayEnvUuid) throws APIManagementException {
        if (apiId == null || gatewayEnvUuid == null) {
            return;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.DELETE_ARTIFACT_BY_API_AND_GATEWAY_SQL)) {
            ps.setString(1, apiId.trim());
            ps.setString(2, gatewayEnvUuid.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting platform deployed artifact for API " + apiId + " and gateway "
                    + gatewayEnvUuid, e);
            throw new APIManagementException("Error deleting platform deployed artifact", e);
        }
    }

    /**
     * Delete all artifact rows for an API from the dedicated platform cache table (e.g. on API delete).
     */
    public void deleteAllRevisionArtifactsForApi(String apiId) throws APIManagementException {
        if (apiId == null) {
            return;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.DELETE_REVISION_ARTIFACTS_BY_API_SQL)) {
            ps.setString(1, apiId.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting all revision artifacts for API " + apiId, e);
            throw new APIManagementException("Error deleting revision artifacts for API", e);
        }
    }

    /**
     * List all deployments (apiUuid, deploymentId, deployedTime) for a gateway environment UUID.
     * Optional since: if non-null, only rows with DEPLOYED_TIME >= since are returned.
     */
    public List<DeploymentRow> listDeploymentsByGatewayEnvUuid(String gatewayEnvUuid, Timestamp since)
            throws APIManagementException {
        if (gatewayEnvUuid == null) {
            return new ArrayList<>();
        }
        String sql = since != null
                ? SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_DEPLOYMENTS_BY_GATEWAY_UUID_SINCE
                : SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_DEPLOYMENTS_BY_GATEWAY_UUID;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gatewayEnvUuid.trim());
            if (since != null) {
                ps.setTimestamp(2, since);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<DeploymentRow> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new DeploymentRow(
                            rs.getString("API_ID"),
                            rs.getString("DEPLOYMENT_ID"),
                            rs.getTimestamp("TIME_STAMP")));
                }
                return rows;
            }
        } catch (SQLException e) {
            log.error("Error listing deployments for gateway environment " + gatewayEnvUuid, e);
            throw new APIManagementException("Error listing deployments for platform gateway", e);
        }
    }

    /**
     * Check whether the given deployment is stored for the given gateway environment.
     * Used to authorize batch artifact requests: only return artifacts for deployments on this gateway.
     *
     * @param gatewayEnvUuid gateway environment UUID
     * @param deploymentId deployment ID
     * @return true if a row exists for (gatewayEnvUuid, deploymentId), false otherwise
     */
    public boolean isDeploymentOnGateway(String gatewayEnvUuid, String deploymentId) throws APIManagementException {
        if (gatewayEnvUuid == null || deploymentId == null) {
            return false;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_DEPLOYMENT_ON_GATEWAY_EXISTS)) {
            ps.setString(1, gatewayEnvUuid.trim());
            ps.setString(2, deploymentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Error checking deployment on gateway " + gatewayEnvUuid + " deployment " + deploymentId, e);
            throw new APIManagementException("Error checking deployment authorization", e);
        }
    }

    /**
     * Resolve deployment ID to API_UUID on a gateway environment (for batch deployment lookup).
     */
    public String getApiUuidByDeploymentId(String gatewayEnvUuid, String deploymentId) throws APIManagementException {
        if (gatewayEnvUuid == null || deploymentId == null) {
            return null;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_API_UUID_BY_DEPLOYMENT_AND_GATEWAY_SQL)) {
            ps.setString(1, gatewayEnvUuid.trim());
            ps.setString(2, deploymentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("API_ID") : null;
            }
        } catch (SQLException e) {
            log.error("Error resolving API_ID for deployment " + deploymentId + " on gateway " + gatewayEnvUuid, e);
            throw new APIManagementException("Error resolving API for deployment", e);
        }
    }

    /** One row from listDeploymentsByGatewayEnvUuid. */
    public static class DeploymentRow {
        private final String apiUuid;
        private final String deploymentId;
        private final Timestamp deployedTime;

        public DeploymentRow(String apiUuid, String deploymentId, Timestamp deployedTime) {
            this.apiUuid = apiUuid;
            this.deploymentId = deploymentId;
            this.deployedTime = deployedTime;
        }

        public String getApiUuid() {
            return apiUuid;
        }

        public String getDeploymentId() {
            return deploymentId;
        }

        public Timestamp getDeployedTime() {
            return deployedTime;
        }
    }
}
