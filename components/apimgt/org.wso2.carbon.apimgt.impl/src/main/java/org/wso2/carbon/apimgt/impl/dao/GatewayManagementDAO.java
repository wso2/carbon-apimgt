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
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Gateway Instance and Deployment operations.
 * Handles all data access for AM_GW_INSTANCES and AM_GW_REVISION_DEPLOYMENT tables.
 */
public class GatewayManagementDAO {

    private static final Log log = LogFactory.getLog(GatewayManagementDAO.class);
    private static final GatewayManagementDAO INSTANCE = new GatewayManagementDAO();

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

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    SQLConstants.GatewayManagementSQLConstants.DELETE_OLD_GATEWAYS_SQL)) {
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
     * Checks if a gateway instance exists in the database for the given gateway ID and organization.
     *
     * @param gatewayId    the gateway identifier to check
     * @param organization the organization identifier to check
     * @return true if the gateway exists, false otherwise
     * @throws APIManagementException if database operation fails
     */
    public boolean gatewayExists(String gatewayId, String organization) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(
                SQLConstants.GatewayManagementSQLConstants.SELECT_GATEWAY_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, organization);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error checking gateway existence", e);
        }
    }

    /**
     * Inserts a new gateway instance record into the database.
     *
     * @param gatewayId    the unique gateway identifier
     * @param organization the organization identifier
     * @param envLabels    the environment label to associate with this gateway
     * @param lastUpdated  timestamp of the last update
     * @param gwProperties serialized gateway properties as byte array
     * @throws APIManagementException if database operation fails
     */
    public void insertGatewayInstance(String gatewayId, String organization, List<String> envLabels,
                                      Timestamp lastUpdated, byte[] gwProperties) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps1 = connection.prepareStatement(
                    SQLConstants.GatewayManagementSQLConstants.INSERT_GATEWAY_INSTANCE_SQL);
                    PreparedStatement ps2 = connection.prepareStatement(
                            SQLConstants.GatewayManagementSQLConstants.INSERT_GATEWAY_ENV_MAPPING_SQL)) {

                ps1.setString(1, gatewayId);
                ps1.setString(2, organization);
                ps1.setTimestamp(3, lastUpdated);
                ps1.setBinaryStream(4, new ByteArrayInputStream(gwProperties));
                ps1.executeUpdate();

                if (envLabels != null && !envLabels.isEmpty()) {
                    for (String envLabel : envLabels) {
                        ps2.setString(1, envLabel);
                        ps2.setString(2, gatewayId);
                        ps2.setString(3, organization);
                        ps2.executeUpdate();
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error inserting gateway instance", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
    }

    /**
     * Checks if a deployment record exists for the specified gateway and API.
     *
     * @param gatewayId the gateway identifier
     * @param apiId     the API identifier
     * @return true if the deployment record exists, false otherwise
     * @throws APIManagementException if database operation fails
     */
    public boolean deploymentExists(String gatewayId, String apiId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(
                SQLConstants.GatewayManagementSQLConstants.SELECT_DEPLOYMENT_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, apiId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error checking deployment existence", e);
        }
    }

    /**
     * Inserts a new deployment record into the database.
     *
     * @param gatewayId    the gateway identifier
     * @param apiId        the API identifier
     * @param organization the organization identifier
     * @param status       the deployment status (e.g., SUCCESS, FAILURE)
     * @param action       the deployment action (e.g., DEPLOY, UNDEPLOY)
     * @param revisionUuid the API revision UUID
     * @throws APIManagementException if database operation fails
     */
    public void insertDeployment(String gatewayId, String apiId, String organization, String status, String action,
                                 String revisionUuid, long lastUpdated) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(
                    SQLConstants.GatewayManagementSQLConstants.INSERT_DEPLOYMENT_SQL)) {
                ps.setString(1, apiId);
                ps.setString(2, organization);
                ps.setString(3, status);
                ps.setString(4, action);
                ps.setString(5, revisionUuid);
                ps.setLong(6, lastUpdated);
                ps.setString(7, gatewayId);
                ps.setString(8, organization);
                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error inserting deployment", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
    }

    /**
     * Updates an existing deployment record in the database.
     *
     * @param gatewayId    the gateway identifier
     * @param apiId        the API identifier
     * @param organization the organization identifier
     * @param status       the updated deployment status (e.g., SUCCESS, FAILURE)
     * @param action       the updated deployment action (e.g., DEPLOY, UNDEPLOY)
     * @param revisionUuid the updated API revision UUID
     * @throws APIManagementException if database operation fails
     */
    public void updateDeployment(String gatewayId, String apiId, String organization, String status, String action,
                                 String revisionUuid, long lastUpdated) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(
                    SQLConstants.GatewayManagementSQLConstants.UPDATE_DEPLOYMENT_SQL)) {
                ps.setString(1, status);
                ps.setString(2, action);
                ps.setString(3, revisionUuid);
                ps.setLong(4, lastUpdated);
                ps.setString(5, organization);
                ps.setString(6, gatewayId);
                ps.setString(7, organization);
                ps.setString(8, apiId);
                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error updating deployment", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
    }

    /**
     * Updates the heartbeat timestamp and status for a gateway instance.
     * This method is called when a gateway sends a heartbeat to indicate it's still active.
     *
     * @param gatewayId    the gateway identifier
     * @param organization the organization identifier
     * @param lastUpdated  the timestamp of the heartbeat
     * @throws APIManagementException if database operation fails
     */
    public void updateGatewayHeartbeat(String gatewayId, String organization, Timestamp lastUpdated)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(
                    SQLConstants.GatewayManagementSQLConstants.UPDATE_GATEWAY_HEARTBEAT_SQL)) {
                ps.setTimestamp(1, lastUpdated);
                ps.setString(2, gatewayId);
                ps.setString(3, organization);
                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error updating gateway heartbeat", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
    }

    /**
     * Updates an existing gateway instance with new information.
     * This method updates environment labels, properties, and last updated timestamp.
     *
     * @param gatewayId    the gateway identifier
     * @param organization the organization identifier
     * @param envLabels    the environment labels to associate with this gateway
     * @param lastUpdated  timestamp of the last update
     * @param gwProperties serialized gateway properties as byte array
     * @throws APIManagementException if database operation fails
     */
    public void updateGatewayInstance(String gatewayId, String organization, List<String> envLabels,
                                      Timestamp lastUpdated, byte[] gwProperties) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps1 = connection.prepareStatement(
                    SQLConstants.GatewayManagementSQLConstants.UPDATE_GATEWAY_INSTANCE_SQL);
                    PreparedStatement ps2 = connection.prepareStatement(
                            SQLConstants.GatewayManagementSQLConstants.DELETE_GATEWAY_ENV_MAPPING_SQL);
                    PreparedStatement ps3 = connection.prepareStatement(
                            SQLConstants.GatewayManagementSQLConstants.INSERT_GATEWAY_ENV_MAPPING_SQL)) {

                // Update gateway instance
                ps1.setTimestamp(1, lastUpdated);
                ps1.setBinaryStream(2, new ByteArrayInputStream(gwProperties));
                ps1.setString(3, gatewayId);
                ps1.setString(4, organization);
                ps1.executeUpdate();

                // Delete existing environment mappings
                ps2.setString(1, gatewayId);
                ps2.setString(2, organization);
                ps2.executeUpdate();

                // Insert new environment mappings
                if (envLabels != null && !envLabels.isEmpty()) {
                    for (String envLabel : envLabels) {
                        ps3.setString(1, envLabel);
                        ps3.setString(2, gatewayId);
                        ps3.setString(3, organization);
                        ps3.executeUpdate();
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error updating gateway instance", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error getting database connection", e);
        }
    }

    /**
     * Get gateway instances by environment label and organization with dynamically calculated status (ACTIVE/EXPIRED).
     *
     * @param envLabel     Environment label to filter
     * @param organization Organization to filter (entries with this organization or 'WSO2-ALL-TENANTS' will be returned)
     * @return List of GatewayInstanceInfo with dynamically calculated status
     * @throws APIManagementException if DB error
     */
    public List<GatewayInstanceInfo> getGatewayInstancesByEnvironment(String envLabel, String organization)
            throws APIManagementException {
        List<GatewayInstanceInfo> result = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(
                SQLConstants.GatewayManagementSQLConstants.SELECT_GATEWAYS_BY_ENV_SQL)) {
            ps.setString(1, envLabel);
            ps.setString(2, organization);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GatewayInstanceInfo info = new GatewayInstanceInfo();
                    info.gatewayId = rs.getString(APIConstants.GatewayNotification.DB_COLUMN_GATEWAY_UUID);
                    info.lastUpdated = rs.getTimestamp(APIConstants.GatewayNotification.DB_COLUMN_LAST_UPDATED);
                    result.add(info);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error retrieving gateway instances by environment", e);
        }

        return result;
    }

    /**
     * Checks if the existing entry for the given gateway ID and organization has a timestamp previous to the given timestamp.
     *
     * @param gatewayId        the gateway identifier to check
     * @param organization     the organization identifier to check
     * @param compareTimestamp the timestamp to compare against
     * @return true if the existing entry has an older timestamp, false if newer or gateway doesn't exist
     * @throws APIManagementException if database operation fails
     */
    public boolean isGatewayTimestampInorder(String gatewayId, String organization, Timestamp compareTimestamp)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(
                SQLConstants.GatewayManagementSQLConstants.SELECT_GATEWAY_TIMESTAMP_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, organization);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp existingTimestamp = rs.getTimestamp(
                            APIConstants.GatewayNotification.DB_COLUMN_LAST_UPDATED);
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
     * @param gatewayId        the gateway identifier to check
     * @param apiId            the API identifier to check
     * @param compareTimestamp the timestamp to compare against
     * @return true if the existing deployment has an older timestamp, false if newer or deployment doesn't exist
     * @throws APIManagementException if database operation fails
     */
    public boolean isDeploymentTimestampInorder(String gatewayId, String apiId, long compareTimestamp)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(
                SQLConstants.GatewayManagementSQLConstants.SELECT_DEPLOYMENT_TIMESTAMP_SQL)) {
            ps.setString(1, gatewayId);
            ps.setString(2, apiId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long existingTimestamp = rs.getLong(
                            APIConstants.GatewayNotification.DB_COLUMN_LAST_UPDATED);
                    return existingTimestamp < compareTimestamp;
                }
                return false; // Deployment doesn't exist
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error checking deployment timestamp", e);
        }
    }

    /**
     * Checks if an API exists in the database.
     *
     * @param apiUuid the API UUID to check
     * @return true if the API exists, false otherwise
     * @throws APIManagementException if database operation fails
     */
    public boolean apiExists(String apiUuid) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(
                SQLConstants.GatewayManagementSQLConstants.SELECT_API_SQL)) {
            ps.setString(1, apiUuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error checking API existence", e);
        }
    }

    /**
     * Calculates gateway deployment statistics for a specific API revision and environment.
     * This includes deployed count, failed count, latest success time, and live gateway count.
     *
     * @param apiRevisionDeployment the APIRevisionDeployment object to populate with stats
     * @param revisionUuid          the UUID of the API revision
     * @param environmentName       the name of the environment
     * @param apiUuid               the UUID of the API
     * @throws APIManagementException if database operation fails
     */
    public void setGatewayDeploymentStats(APIRevisionDeployment apiRevisionDeployment, String revisionUuid,
                                          String environmentName, String apiUuid) throws APIManagementException {
        GatewayNotificationConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
        long currentTime = System.currentTimeMillis();
        long expireTimeThreshold = currentTime - (config.getGatewayCleanupConfiguration().getExpireTimeSeconds() * 1000L);
        Timestamp expireTimestamp = new Timestamp(expireTimeThreshold);

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.GATEWAY_DEPLOYMENT_STATS_QUERY)) {
                ps.setString(1, revisionUuid);
                ps.setString(2, environmentName);
                ps.setTimestamp(3, expireTimestamp);

                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        int deployedCount = resultSet.getInt(APIConstants.GatewayNotification.DEPLOYED_COUNT);
                        int failedCount = resultSet.getInt(APIConstants.GatewayNotification.FAILED_COUNT);
                        long latestSuccessTime = resultSet.getLong(APIConstants.GatewayNotification.LATEST_SUCCESS_TIME);

                        apiRevisionDeployment.setDeployedGatewayCount(deployedCount);
                        apiRevisionDeployment.setFailedGatewayCount(failedCount);

                        if (latestSuccessTime > 0) {
                            Timestamp successTimestamp = new Timestamp(latestSuccessTime);
                            apiRevisionDeployment.setSuccessDeployedTime(successTimestamp.toString());
                        }
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.GATEWAY_LIVE_COUNT_WITH_API_ORGANIZATION_QUERY)) {
                ps.setTimestamp(1, expireTimestamp);
                ps.setString(2, environmentName);
                ps.setString(3, apiUuid);

                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        int liveCount = resultSet.getInt(APIConstants.GatewayNotification.LIVE_COUNT);
                        apiRevisionDeployment.setLiveGatewayCount(liveCount);
                    }
                }
            }

        } catch (SQLException e) {
            log.error("Error while calculating gateway deployment statistics for revision: " + revisionUuid
                              + " and environment: " + environmentName, e);
        }
    }

    public static class GatewayInstanceInfo {
        public String gatewayId;
        public Timestamp lastUpdated;
    }
}
