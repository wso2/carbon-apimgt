/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO class for Gateway Instance and Deployment operations.
 * Handles all data access for AM_GW_INSTANCES and AM_GW_REVISION_DEPLOYMENT tables.
 */
public class GatewayManagementDAO {
    
    private static final Log log = LogFactory.getLog(GatewayManagementDAO.class);
    private static final GatewayManagementDAO INSTANCE = new GatewayManagementDAO();
    
    // SQL queries
    private static final String UPDATE_EXPIRED_GATEWAYS_SQL = 
            "UPDATE AM_GW_INSTANCES SET STATUS = 'EXPIRED' WHERE LAST_UPDATED < ? AND STATUS = 'ACTIVE'";
    private static final String DELETE_OLD_GATEWAYS_SQL = 
            "DELETE FROM AM_GW_INSTANCES WHERE LAST_UPDATED < ?";
    private static final String DELETE_OLD_DEPLOYMENTS_SQL = 
            "DELETE FROM AM_GW_REVISION_DEPLOYMENT WHERE GATEWAY_ID NOT IN (SELECT GATEWAY_ID FROM AM_GW_INSTANCES)";
    private static final String INSERT_GATEWAY_INSTANCE_SQL =
            "INSERT INTO AM_GW_INSTANCES (GATEWAY_ID, ENV_LABELS, LAST_UPDATED, GW_PROPERTIES, STATUS) VALUES (?, ?, "
                    + "?, ?, ?) ";
    private static final String SELECT_GATEWAY_SQL =
            "SELECT 1 FROM AM_GW_INSTANCES WHERE GATEWAY_ID=?";
    private static final String SELECT_DEPLOYMENT_SQL =
            "SELECT 1 FROM AM_GW_REVISION_DEPLOYMENT WHERE GATEWAY_ID = ? AND API_ID = ?";
    private static final String INSERT_DEPLOYMENT_SQL =
            "INSERT INTO AM_GW_REVISION_DEPLOYMENT (GATEWAY_ID, API_ID, STATUS, ACTION, REVISION_ID, LAST_UPDATED) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_DEPLOYMENT_SQL =
            "UPDATE AM_GW_REVISION_DEPLOYMENT SET STATUS = ?, ACTION = ?, REVISION_ID = ?, LAST_UPDATED = ? WHERE GATEWAY_ID = ? AND API_ID = ?";
    private static final String UPDATE_GATEWAY_HEARTBEAT_SQL =
            "UPDATE AM_GW_INSTANCES SET LAST_UPDATED=?, STATUS=? WHERE GATEWAY_ID=?";
    private static final String UPDATE_GATEWAY_INSTANCE_SQL =
            "UPDATE AM_GW_INSTANCES SET ENV_LABELS=?, LAST_UPDATED=?, GW_PROPERTIES=?, STATUS=? WHERE GATEWAY_ID=?";
    private static final String SELECT_GATEWAYS_BY_ENV_SQL =
            "SELECT GATEWAY_ID, LAST_UPDATED, STATUS FROM AM_GW_INSTANCES WHERE " +
                    "(ENV_LABELS = ? OR ENV_LABELS LIKE ? OR ENV_LABELS LIKE ? OR ENV_LABELS LIKE ?) " +
                    "AND (STATUS = 'ACTIVE' OR STATUS = 'EXPIRED')";

    private GatewayManagementDAO() {
        // Private constructor for singleton
    }
    
    public static GatewayManagementDAO getInstance() {
        return INSTANCE;
    }
    
    /**
     * Update gateway status to EXPIRED for gateways that haven't sent heartbeat within the expire time
     * 
     * @param expireTimeThreshold The timestamp threshold for expiration
     * @return Number of gateways updated
     * @throws APIManagementException if database operation fails
     */
    public int updateExpiredGateways(Timestamp expireTimeThreshold) throws APIManagementException {
        int updatedCount = 0;
        
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EXPIRED_GATEWAYS_SQL)) {
                preparedStatement.setTimestamp(1, expireTimeThreshold);
                updatedCount = preparedStatement.executeUpdate();
                connection.commit();
                
                if (log.isDebugEnabled()) {
                    log.debug("Updated " + updatedCount + " gateways to EXPIRED status");
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error updating expired gateways", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
        
        return updatedCount;
    }
    
    /**
     * Delete gateway records that are older than the data retention period
     * 
     * @param retentionThreshold The timestamp threshold for data retention
     * @return Number of gateways deleted
     * @throws APIManagementException if database operation fails
     */
    public int deleteOldGatewayRecords(Timestamp retentionThreshold) throws APIManagementException {
        int deletedCount = 0;
        
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_OLD_GATEWAYS_SQL)) {
                preparedStatement.setTimestamp(1, retentionThreshold);
                deletedCount = preparedStatement.executeUpdate();
                connection.commit();
                
                if (log.isDebugEnabled()) {
                    log.debug("Deleted " + deletedCount + " old gateway records");
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error deleting old gateway records", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
        
        return deletedCount;
    }
    
    /**
     * Clean up orphaned deployment records (deployments for non-existent gateways)
     * 
     * @return Number of deployment records deleted
     * @throws APIManagementException if database operation fails
     */
    public int cleanupOrphanedDeployments() throws APIManagementException {
        int deletedCount = 0;
        
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_OLD_DEPLOYMENTS_SQL)) {
                deletedCount = preparedStatement.executeUpdate();
                connection.commit();
                
                if (log.isDebugEnabled()) {
                    log.debug("Deleted " + deletedCount + " orphaned deployment records");
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error cleaning up orphaned deployments", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
        
        return deletedCount;
    }

    public boolean gatewayExists(String gatewayId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_GATEWAY_SQL)) {
            ps.setString(1, gatewayId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error checking gateway existence", e);
        }
    }

    public void insertGatewayInstance(String gatewayId, String envLabels, Timestamp lastUpdated, byte[] gwProperties) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_GATEWAY_INSTANCE_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, envLabels);
            ps.setTimestamp(3, lastUpdated);
            ps.setBytes(4, gwProperties);
            ps.setString(5, "ACTIVE");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error inserting gateway instance", e);
        }
    }

    public boolean deploymentExists(String gatewayId, String apiId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_DEPLOYMENT_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, apiId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error checking deployment existence", e);
        }
    }

    public void insertDeployment(String gatewayId, String apiId, String status, String action, String revisionId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_DEPLOYMENT_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, apiId);
            ps.setString(3, status);
            ps.setString(4, action);
            ps.setString(5, revisionId);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error inserting deployment", e);
        }
    }

    public void updateDeployment(String gatewayId, String apiId, String status, String action, String revisionId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_DEPLOYMENT_SQL)) {
            ps.setString(1, status);
            ps.setString(2, action);
            ps.setString(3, revisionId);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setString(5, gatewayId);
            ps.setString(6, apiId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error updating deployment", e);
        }
    }

    public void updateGatewayHeartbeat(String gatewayId, Timestamp lastUpdated) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_GATEWAY_HEARTBEAT_SQL)) {
            ps.setTimestamp(1, lastUpdated);
            ps.setString(2, "ACTIVE");
            ps.setString(3, gatewayId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error updating gateway heartbeat", e);
        }
    }

    public void updateGatewayInstance(String gatewayId, String envLabels, Timestamp lastUpdated, byte[] gwProperties)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_GATEWAY_INSTANCE_SQL)) {
            ps.setString(1, envLabels);
            ps.setTimestamp(2, lastUpdated);
            ps.setBytes(3, gwProperties);
            ps.setString(4, "ACTIVE");
            ps.setString(5, gatewayId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error updating gateway instance", e);
        }
    }

    /**
     * Get gateway instances by environment label and status (ACTIVE/EXPIRED).
     *
     * @param environmentId Environment label to filter
     * @return List of GatewayInstanceInfo for ACTIVE and EXPIRED gateway instances
     * @throws APIManagementException if DB error
     */
    public List<GatewayInstanceInfo> getGatewayInstancesByEnvironment(String environmentId) throws APIManagementException {
        List<GatewayInstanceInfo> result = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_GATEWAYS_BY_ENV_SQL)) {
            ps.setString(1, environmentId);
            ps.setString(2, environmentId + ",%");
            ps.setString(3, "%," + environmentId + ",%");
            ps.setString(4, "%," + environmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GatewayInstanceInfo info = new GatewayInstanceInfo();
                    info.gatewayId = rs.getString("GATEWAY_ID");
                    info.lastUpdated = rs.getTimestamp("LAST_UPDATED");
                    info.status = rs.getString("STATUS");
                    result.add(info);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error fetching gateway instances by environment", e);
        }
        return result;
    }

    public static class GatewayInstanceInfo {
        public String gatewayId;
        public java.sql.Timestamp lastUpdated;
        public String status;
    }
}
