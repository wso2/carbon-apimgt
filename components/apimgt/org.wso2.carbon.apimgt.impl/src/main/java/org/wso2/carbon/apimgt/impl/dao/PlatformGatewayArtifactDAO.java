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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for platform gateway revision-scoped artifact storage using AM_GW_API_ARTIFACTS.
 * Resolution (apiId, gateway name) → REVISION_UUID uses AM_DEPLOYMENT_REVISION_MAPPING and AM_REVISION (main DB).
 * Artifact read/write uses AM_GW_API_ARTIFACTS (artifact synchronizer DB).
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
            return null;
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
     * Get stored revision artifact (YAML) from AM_GW_API_ARTIFACTS.
     *
     * @param apiId         API ID (UUID)
     * @param revisionId    REVISION_UUID
     * @return YAML content or null if not found
     */
    public String getRevisionArtifact(String apiId, String revisionId) throws APIManagementException {
        if (apiId == null || revisionId == null) {
            return null;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_REVISION_ARTIFACT_SQL)) {
            ps.setString(1, apiId.trim());
            ps.setString(2, revisionId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                byte[] bytes = rs.getBytes("ARTIFACT");
                return bytes != null ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8) : null;
            }
        } catch (SQLException e) {
            log.error("Error getting platform revision artifact for API " + apiId + " revision " + revisionId, e);
            throw new APIManagementException("Error getting platform revision artifact", e);
        }
    }

    /**
     * Save or replace platform revision artifact in AM_GW_API_ARTIFACTS.
     * INSERT or UPDATE one row (API_ID, REVISION_ID, ARTIFACT). Uses artifact synchronizer connection.
     *
     * @param apiId         API ID (UUID)
     * @param revisionId    REVISION_UUID
     * @param yamlContent   platform api.yaml content (stored as bytes)
     */
    public void saveRevisionArtifact(String apiId, String revisionId, String yamlContent)
            throws APIManagementException {
        if (apiId == null || revisionId == null) {
            throw new APIManagementException("API ID and revision ID are required");
        }
        if (yamlContent == null) {
            throw new APIManagementException("YAML content is required");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        byte[] artifactBytes = yamlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQLConstants.UPDATE_API_ARTIFACT)) {
                ps.setBytes(1, artifactBytes);
                ps.setTimestamp(2, now);
                ps.setString(3, apiId.trim());
                ps.setString(4, revisionId.trim());
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement insertPs = connection.prepareStatement(SQLConstants.ADD_GW_API_ARTIFACT)) {
                        insertPs.setBytes(1, artifactBytes);
                        insertPs.setTimestamp(2, now);
                        insertPs.setString(3, apiId.trim());
                        insertPs.setString(4, revisionId.trim());
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error saving platform revision artifact for API " + apiId + " revision " + revisionId, e);
            throw new APIManagementException("Error saving platform revision artifact", e);
        }
    }

    /**
     * Delete revision artifact for (apiId, revisionId). Optional, e.g. when revision is undeployed from all.
     */
    public void deleteRevisionArtifact(String apiId, String revisionId) throws APIManagementException {
        if (apiId == null || revisionId == null) {
            return;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.DELETE_FROM_AM_GW_API_ARTIFACTS_WHERE_API_ID_AND_REVISION_ID)) {
            ps.setString(1, apiId.trim());
            ps.setString(2, revisionId.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting platform revision artifact for API " + apiId + " revision " + revisionId, e);
            throw new APIManagementException("Error deleting platform revision artifact", e);
        }
    }

    /**
     * Delete all artifact rows for an API from AM_GW_API_ARTIFACTS (e.g. on API delete).
     */
    public void deleteAllRevisionArtifactsForApi(String apiId) throws APIManagementException {
        if (apiId == null) {
            return;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.DELETE_FROM_AM_GW_API_ARTIFACTS_BY_API_ID)) {
            ps.setString(1, apiId.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting all revision artifacts for API " + apiId, e);
            throw new APIManagementException("Error deleting revision artifacts for API", e);
        }
    }

    /**
     * List all deployments (apiUuid, revisionUuid, deployedTime) for a gateway by name.
     * Optional since: if non-null, only rows with DEPLOYED_TIME >= since are returned.
     */
    public List<DeploymentRow> listDeploymentsByGatewayName(String gatewayName, Timestamp since)
            throws APIManagementException {
        if (gatewayName == null) {
            return new ArrayList<>();
        }
        String sql = since != null
                ? SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_DEPLOYMENTS_BY_GATEWAY_NAME_SINCE
                : SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_DEPLOYMENTS_BY_GATEWAY_NAME;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gatewayName.trim());
            if (since != null) {
                ps.setTimestamp(2, since);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<DeploymentRow> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new DeploymentRow(
                            rs.getString("API_UUID"),
                            rs.getString("REVISION_UUID"),
                            rs.getTimestamp("DEPLOYED_TIME")));
                }
                return rows;
            }
        } catch (SQLException e) {
            log.error("Error listing deployments for gateway " + gatewayName, e);
            throw new APIManagementException("Error listing deployments for platform gateway", e);
        }
    }

    /**
     * Resolve REVISION_UUID to API_UUID (for batch deployment lookup).
     */
    public String getApiUuidByRevisionUuid(String revisionUuid) throws APIManagementException {
        if (revisionUuid == null) {
            return null;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_API_UUID_BY_REVISION_UUID)) {
            ps.setString(1, revisionUuid.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("API_UUID") : null;
            }
        } catch (SQLException e) {
            log.error("Error resolving API_UUID for revision " + revisionUuid, e);
            throw new APIManagementException("Error resolving API for revision", e);
        }
    }

    /** One row from listDeploymentsByGatewayName. */
    public static class DeploymentRow {
        private final String apiUuid;
        private final String revisionUuid;
        private final Timestamp deployedTime;

        public DeploymentRow(String apiUuid, String revisionUuid, Timestamp deployedTime) {
            this.apiUuid = apiUuid;
            this.revisionUuid = revisionUuid;
            this.deployedTime = deployedTime;
        }

        public String getApiUuid() {
            return apiUuid;
        }

        public String getRevisionUuid() {
            return revisionUuid;
        }

        public Timestamp getDeployedTime() {
            return deployedTime;
        }
    }
}
