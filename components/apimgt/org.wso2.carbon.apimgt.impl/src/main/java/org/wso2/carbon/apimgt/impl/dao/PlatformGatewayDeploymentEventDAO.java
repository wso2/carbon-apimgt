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

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DAO for AM_GW_PLATFORM_DEPLOYMENT_EVENT: persist deploy/undeploy/delete events per gateway
 * for multi-CP WebSocket sync; pending events are sent when a gateway connects (push-on-connect).
 */
public class PlatformGatewayDeploymentEventDAO {

    private static final Log log = LogFactory.getLog(PlatformGatewayDeploymentEventDAO.class);
    private static final PlatformGatewayDeploymentEventDAO INSTANCE = new PlatformGatewayDeploymentEventDAO();

    private PlatformGatewayDeploymentEventDAO() {
    }

    public static PlatformGatewayDeploymentEventDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Insert one deployment event row (ID = new UUID, CREATED_AT = now, DELIVERED_AT = NULL).
     */
    public void insertEvent(String gatewayId, String apiId, String revisionUuid, String eventType, String payload)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Inserting deployment event for gateway: " + gatewayId + ", API: " + apiId + ", event type: "
                    + eventType);
        }
        if (gatewayId == null || apiId == null || eventType == null || payload == null) {
            throw new APIManagementException("gatewayId, apiId, eventType and payload are required");
        }
        String id = UUID.randomUUID().toString();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayDeploymentEventSQLConstants.INSERT_EVENT)) {
            ps.setString(1, id);
            ps.setString(2, gatewayId.trim());
            ps.setString(3, apiId.trim());
            ps.setString(4, revisionUuid != null ? revisionUuid.trim() : null);
            ps.setString(5, eventType.trim());
            ps.setBytes(6, payload.getBytes(StandardCharsets.UTF_8));
            ps.setTimestamp(7, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error inserting platform gateway deployment event for gateway " + gatewayId + " api " + apiId, e);
            throw new APIManagementException("Error inserting deployment event", e);
        }
    }

    /** Claim lease duration: claims older than this are considered expired and can be re-claimed (ms). */
    private static final long CLAIM_LEASE_DURATION_MS = 60_000L;

    /**
     * Get pending events for a gateway (DELIVERED_AT IS NULL and not actively claimed, or claim expired) ordered by CREATED_AT.
     * Returns list of (id, payload).
     */
    public List<DeploymentEventRecord> getPendingEventsForGateway(String gatewayId) throws APIManagementException {
        if (gatewayId == null || gatewayId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<DeploymentEventRecord> list = new ArrayList<>();
        java.sql.Timestamp leaseExpiry = new Timestamp(System.currentTimeMillis() - CLAIM_LEASE_DURATION_MS);
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayDeploymentEventSQLConstants.SELECT_PENDING_FOR_GATEWAY)) {
            ps.setString(1, gatewayId.trim());
            ps.setTimestamp(2, leaseExpiry);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("ID");
                    byte[] payloadBytes = rs.getBytes("PAYLOAD");
                    String payload = payloadBytes != null ? new String(payloadBytes, StandardCharsets.UTF_8) : null;
                    if (id != null && payload != null) {
                        list.add(new DeploymentEventRecord(id, payload));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error getting pending deployment events for gateway " + gatewayId, e);
            throw new APIManagementException("Error getting pending deployment events", e);
        }
        return list;
    }

    /**
     * Mark the given event IDs as delivered (SET DELIVERED_AT = now).
     */
    public void markDelivered(List<String> eventIds) throws APIManagementException {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String placeholders = String.join(",", Collections.nCopies(eventIds.size(), "?"));
        String sql = "UPDATE AM_GW_PLATFORM_DEPLOYMENT_EVENT SET DELIVERED_AT = ? WHERE ID IN (" + placeholders + ")";
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, now);
            for (int i = 0; i < eventIds.size(); i++) {
                ps.setString(i + 2, eventIds.get(i));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error marking deployment events as delivered: " + e.getMessage(), e);
            throw new APIManagementException("Error marking deployment events as delivered", e);
        }
    }

    /**
     * Claim pending events for the gateway (set CLAIMED_AT/CLAIMED_BY) so another node cannot process them.
     * Caller must send over WebSocket and then call {@link #markDelivered(List)} for successfully sent IDs.
     * Does not set DELIVERED_AT; delivery is acknowledged only after markDelivered.
     *
     * @return list of (id, payload) for the events that were claimed
     */
    public List<DeploymentEventRecord> getAndMarkDeliveredPendingEventsForGateway(String gatewayId)
            throws APIManagementException {
        if (gatewayId == null || gatewayId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (log.isDebugEnabled()) {
            log.debug("Claiming pending events for gateway: " + gatewayId);
        }
        String claimId = UUID.randomUUID().toString();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp leaseExpiry = new Timestamp(System.currentTimeMillis() - CLAIM_LEASE_DURATION_MS);
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayDeploymentEventSQLConstants.UPDATE_CLAIM_PENDING_FOR_GATEWAY)) {
                    ps.setTimestamp(1, now);
                    ps.setString(2, claimId);
                    ps.setString(3, gatewayId.trim());
                    ps.setTimestamp(4, leaseExpiry);
                    ps.executeUpdate();
                }
                List<DeploymentEventRecord> list = new ArrayList<>();
                try (PreparedStatement ps = connection.prepareStatement(
                        SQLConstants.PlatformGatewayDeploymentEventSQLConstants.SELECT_CLAIMED_BY_BATCH)) {
                    ps.setString(1, claimId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String id = rs.getString("ID");
                            byte[] payloadBytes = rs.getBytes("PAYLOAD");
                            String payload = payloadBytes != null
                                    ? new String(payloadBytes, StandardCharsets.UTF_8) : null;
                            if (id != null && payload != null) {
                                list.add(new DeploymentEventRecord(id, payload));
                            }
                        }
                    }
                }
                connection.commit();
                return list;
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.warn("Rollback failed: " + ex.getMessage());
                }
                log.error("Error claiming pending events for gateway " + gatewayId, e);
                throw new APIManagementException("Error getting pending deployment events", e);
            }
        } catch (SQLException e) {
            log.error("Error claiming pending events for gateway " + gatewayId, e);
            throw new APIManagementException("Error getting pending deployment events", e);
        }
    }

    /**
     * Delete delivered events older than the given timestamp to prevent unbounded table growth.
     *
     * @param before delete rows where DELIVERED_AT IS NOT NULL AND DELIVERED_AT &lt; before
     * @return number of rows deleted
     */
    public int deleteDeliveredEventsOlderThan(Timestamp before) throws APIManagementException {
        if (before == null) {
            return 0;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.PlatformGatewayDeploymentEventSQLConstants.DELETE_DELIVERED_EVENTS_OLDER_THAN)) {
            ps.setTimestamp(1, before);
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting old delivered deployment events: " + e.getMessage(), e);
            throw new APIManagementException("Error deleting old delivered events", e);
        }
    }

    /**
     * Record returned from getPendingEventsForGateway (id and payload for send + mark delivered).
     */
    public static class DeploymentEventRecord {
        private final String id;
        private final String payload;

        public DeploymentEventRecord(String id, String payload) {
            this.id = id;
            this.payload = payload;
        }

        public String getId() {
            return id;
        }

        public String getPayload() {
            return payload;
        }
    }
}
