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

/**
 * DAO for platform gateway API artifact storage (AM_PLATFORM_GATEWAY_API_ARTIFACT).
 * Stores platform-format api.yaml per API and organization.
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
     * Get stored YAML content for an API and organization.
     *
     * @param apiId         API UUID
     * @param organizationId organization id
     * @return YAML content or null if not found
     */
    public String getArtifact(String apiId, String organizationId) throws APIManagementException {
        if (apiId == null || organizationId == null) {
            return null;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.SELECT_ARTIFACT_SQL)) {
            ps.setString(1, apiId);
            ps.setString(2, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("YAML_CONTENT") : null;
            }
        } catch (SQLException e) {
            log.error("Error getting platform gateway artifact for API " + apiId, e);
            throw new APIManagementException("Error getting platform gateway artifact", e);
        }
    }

    /**
     * Save or replace platform artifact for an API and organization.
     *
     * @param apiId         API UUID
     * @param organizationId organization id
     * @param yamlContent   platform api.yaml content
     */
    public void saveArtifact(String apiId, String organizationId, String yamlContent) throws APIManagementException {
        if (apiId == null || organizationId == null) {
            throw new APIManagementException("API UUID and organization are required");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String existing = getArtifact(apiId, organizationId);
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            if (existing == null) {
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayArtifactSQLConstants.INSERT_ARTIFACT_SQL)) {
                    ps.setString(1, apiId);
                    ps.setString(2, organizationId);
                    ps.setString(3, yamlContent);
                    ps.setTimestamp(4, now);
                    ps.setTimestamp(5, now);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayArtifactSQLConstants.UPDATE_ARTIFACT_SQL)) {
                    ps.setString(1, yamlContent);
                    ps.setTimestamp(2, now);
                    ps.setString(3, apiId);
                    ps.setString(4, organizationId);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            log.error("Error saving platform gateway artifact for API " + apiId, e);
            throw new APIManagementException("Error saving platform gateway artifact", e);
        }
    }

    /**
     * Delete stored artifact for an API and organization.
     */
    public void deleteArtifact(String apiId, String organizationId) throws APIManagementException {
        if (apiId == null || organizationId == null) {
            return;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayArtifactSQLConstants.DELETE_ARTIFACT_SQL)) {
            ps.setString(1, apiId);
            ps.setString(2, organizationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting platform gateway artifact for API " + apiId, e);
            throw new APIManagementException("Error deleting platform gateway artifact", e);
        }
    }
}
