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
    private static final String SELECT_GATEWAY_TIMESTAMP_SQL =
            "SELECT LAST_UPDATED FROM AM_GW_INSTANCES WHERE GATEWAY_ID = ?";
    private static final String SELECT_DEPLOYMENT_TIMESTAMP_SQL =
            "SELECT LAST_UPDATED FROM AM_GW_REVISION_DEPLOYMENT WHERE GATEWAY_ID = ? AND API_ID = ?";

    private GatewayManagementDAO() {
    }
    
    /**
     * Returns the singleton instance of GatewayManagementDAO.
     *
     * @return the singleton GatewayManagementDAO instance
     */
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
     * Checks if a gateway instance exists in the database.
     *
     * @param gatewayId the gateway identifier to check
     * @return true if the gateway exists, false otherwise
     * @throws APIManagementException if database operation fails
     */
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

    /**
     * Inserts a new gateway instance record into the database.
     *
     * @param gatewayId the unique gateway identifier
     * @param envLabels comma-separated list of environment labels
     * @param lastUpdated timestamp of the last update
     * @param gwProperties serialized gateway properties as byte array
     * @throws APIManagementException if database operation fails
     */
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

    /**
     * Checks if a deployment record exists for the specified gateway and API.
     *
     * @param gatewayId the gateway identifier
     * @param apiId the API identifier
     * @return true if the deployment record exists, false otherwise
     * @throws APIManagementException if database operation fails
     */
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

    /**
     * Inserts a new deployment record into the database.
     *
     * @param gatewayId the gateway identifier
     * @param apiId the API identifier
     * @param status the deployment status (e.g., "SUCCESS", "FAILURE")
     * @param action the deployment action (e.g., "DEPLOY", "UNDEPLOY")
     * @param revisionId the API revision identifier
     * @throws APIManagementException if database operation fails
     */
    public void insertDeployment(String gatewayId, String apiId, String status, Timestamp lastUpdated, String action,
                                 String revisionId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_DEPLOYMENT_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, apiId);
            ps.setString(3, status);
            ps.setString(4, action);
            ps.setString(5, revisionId);
            ps.setTimestamp(6, lastUpdated);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error inserting deployment", e);
        }
    }

    /**
     * Updates an existing deployment record in the database.
     *
     * @param gatewayId the gateway identifier
     * @param apiId the API identifier
     * @param status the updated deployment status (e.g., "SUCCESS", "FAILURE")
     * @param action the updated deployment action (e.g., "DEPLOY", "UNDEPLOY")
     * @param revisionId the updated API revision identifier
     * @throws APIManagementException if database operation fails
     */
    public void updateDeployment(String gatewayId, String apiId, String status, Timestamp lastUpdated,String action,
                                 String revisionId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_DEPLOYMENT_SQL)) {
            ps.setString(1, status);
            ps.setString(2, action);
            ps.setString(3, revisionId);
            ps.setTimestamp(4, lastUpdated);
            ps.setString(5, gatewayId);
            ps.setString(6, apiId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error updating deployment", e);
        }
    }

    /**
     * Updates the heartbeat timestamp and status for a gateway instance.
     * This method is called when a gateway sends a heartbeat to indicate it's still active.
     *
     * @param gatewayId the gateway identifier
     * @param lastUpdated the timestamp of the heartbeat
     * @throws APIManagementException if database operation fails
     */
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

    /**
     * Updates an existing gateway instance with new information.
     * This method updates environment labels, properties, and last updated timestamp.
     *
     * @param gatewayId the gateway identifier
     * @param envLabels comma-separated list of environment labels
     * @param lastUpdated timestamp of the last update
     * @param gwProperties serialized gateway properties as byte array
     * @throws APIManagementException if database operation fails
     */
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

    /**
     * Checks if the existing entry for the given gateway ID has a timestamp previous to the given timestamp.
     *
     * @param gatewayId the gateway identifier to check
     * @param compareTimestamp the timestamp to compare against
     * @return true if the existing entry has an older timestamp, false if newer or gateway doesn't exist
     * @throws APIManagementException if database operation fails
     */
    public boolean isGatewayTimestampInorder(String gatewayId, Timestamp compareTimestamp) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_GATEWAY_TIMESTAMP_SQL)) {
            ps.setString(1, gatewayId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp existingTimestamp = rs.getTimestamp("LAST_UPDATED");
                    return existingTimestamp != null && existingTimestamp.before(compareTimestamp);
                }
                return false; // Gateway doesn't exist
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error checking gateway timestamp", e);
        }
    }

    /**
     * Checks if the existing deployment for the given gateway ID and API ID has a timestamp previous to the given timestamp.
     *
     * @param gatewayId the gateway identifier to check
     * @param apiId the API identifier to check
     * @param compareTimestamp the timestamp to compare against
     * @return true if the existing deployment has an older timestamp, false if newer or deployment doesn't exist
     * @throws APIManagementException if database operation fails
     */
    public boolean isDeploymentTimestampInorder(String gatewayId, String apiId, Timestamp compareTimestamp) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_DEPLOYMENT_TIMESTAMP_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, apiId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp existingTimestamp = rs.getTimestamp("LAST_UPDATED");
                    return existingTimestamp != null && existingTimestamp.before(compareTimestamp);
                }
                return false; // Deployment doesn't exist
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error checking deployment timestamp", e);
        }
    }

    public static class GatewayInstanceInfo {
        public String gatewayId;
        public java.sql.Timestamp lastUpdated;
        public String status;
    }
}
