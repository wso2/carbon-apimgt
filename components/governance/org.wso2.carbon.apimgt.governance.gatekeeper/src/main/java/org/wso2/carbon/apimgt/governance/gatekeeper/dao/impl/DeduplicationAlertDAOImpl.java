/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.DeduplicationAlertDAO;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationAlert;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationDecision;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DeduplicationAlertDAO for database operations.
 */
public class DeduplicationAlertDAOImpl implements DeduplicationAlertDAO {

    private static final Log log = LogFactory.getLog(DeduplicationAlertDAOImpl.class);

    private final ObjectMapper objectMapper;

    // SQL Constants - Using simplified schema without optional columns
    private static final String INSERT_ALERT = 
            "INSERT INTO AM_DEDUP_ALERT (ALERT_ID, NEW_API_UUID, NEW_API_NAME, NEW_API_VERSION, " +
            "HIGHEST_SIMILARITY, SEVERITY, STATUS, ORGANIZATION, " +
            "MESSAGE, RECOMMENDATION, SIMILAR_APIS_JSON, CREATED_AT) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String GET_ALERT = 
            "SELECT * FROM AM_DEDUP_ALERT WHERE ALERT_ID = ? AND ORGANIZATION = ?";

    private static final String GET_PENDING_ALERTS = 
            "SELECT * FROM AM_DEDUP_ALERT WHERE ORGANIZATION = ? AND STATUS = 'PENDING' " +
            "ORDER BY CREATED_AT DESC";

    private static final String GET_ALERTS_FOR_API = 
            "SELECT * FROM AM_DEDUP_ALERT WHERE NEW_API_UUID = ? AND ORGANIZATION = ? " +
            "ORDER BY CREATED_AT DESC";

    private static final String GET_ALERTS_PAGINATED = 
            "SELECT * FROM AM_DEDUP_ALERT WHERE ORGANIZATION = ? ORDER BY CREATED_AT DESC LIMIT ? OFFSET ?";

    private static final String GET_ALERTS_PAGINATED_WITH_STATUS = 
            "SELECT * FROM AM_DEDUP_ALERT WHERE ORGANIZATION = ? AND STATUS = ? " +
            "ORDER BY CREATED_AT DESC LIMIT ? OFFSET ?";

    private static final String UPDATE_ALERT_STATUS = 
            "UPDATE AM_DEDUP_ALERT SET STATUS = ?, RESOLVED_AT = ?, RESOLVED_BY = ? " +
            "WHERE ALERT_ID = ? AND ORGANIZATION = ?";

    private static final String DELETE_ALERT = 
            "DELETE FROM AM_DEDUP_ALERT WHERE ALERT_ID = ? AND ORGANIZATION = ?";

    private static final String DELETE_ALERTS_FOR_API = 
            "DELETE FROM AM_DEDUP_ALERT WHERE NEW_API_UUID = ? AND ORGANIZATION = ?";

    private static final String COUNT_PENDING_ALERTS = 
            "SELECT COUNT(*) FROM AM_DEDUP_ALERT WHERE ORGANIZATION = ? AND STATUS = 'PENDING'";

    private static final String INSERT_DECISION = 
            "INSERT INTO AM_DEDUP_DECISION (DECISION_ID, ALERT_ID, NEW_API_UUID, EXISTING_API_UUID, " +
            "SIMILARITY_SCORE, ACTION, JUSTIFICATION, DECIDED_BY, DECIDED_AT, ORGANIZATION, STATUS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String GET_DECISION = 
            "SELECT * FROM AM_DEDUP_DECISION WHERE DECISION_ID = ? AND ORGANIZATION = ?";

    private static final String GET_DECISIONS_FOR_ALERT = 
            "SELECT * FROM AM_DEDUP_DECISION WHERE ALERT_ID = ? AND ORGANIZATION = ?";

    private static final String GET_DECISION_HISTORY_FOR_API = 
            "SELECT * FROM AM_DEDUP_DECISION WHERE (NEW_API_UUID = ? OR EXISTING_API_UUID = ?) " +
            "AND ORGANIZATION = ? ORDER BY DECIDED_AT DESC";

    private static final String UPDATE_DECISION_STATUS = 
            "UPDATE AM_DEDUP_DECISION SET STATUS = ? WHERE DECISION_ID = ? AND ORGANIZATION = ?";

    private static final class SingletonHelper {
        private static final DeduplicationAlertDAO INSTANCE = new DeduplicationAlertDAOImpl();
    }

    /**
     * Private constructor for singleton.
     */
    private DeduplicationAlertDAOImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Gets the singleton instance.
     *
     * @return DeduplicationAlertDAO instance
     */
    public static DeduplicationAlertDAO getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public void createAlert(DeduplicationAlert alert) throws APIMGovernanceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        boolean initialAutoCommit = true;
        
        try {
            connection = APIMGovernanceDBUtil.getConnection();
            initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            
            stmt = connection.prepareStatement(INSERT_ALERT);
            stmt.setString(1, alert.getAlertId());
            stmt.setString(2, alert.getNewApiUuid());
            stmt.setString(3, alert.getNewApiName());
            stmt.setString(4, alert.getNewApiVersion());
            stmt.setDouble(5, alert.getHighestSimilarity());
            stmt.setString(6, alert.getSeverity() != null ? alert.getSeverity().name() : "MEDIUM");
            stmt.setString(7, alert.getStatus() != null ? alert.getStatus().name() : "PENDING");
            stmt.setString(8, alert.getOrganization());
            stmt.setString(9, alert.getMessage());
            stmt.setString(10, alert.getRecommendation());
            stmt.setString(11, serializeSimilarApis(alert.getSimilarApis()));
            stmt.setTimestamp(12, Timestamp.valueOf(alert.getCreatedAt()));

            stmt.executeUpdate();
            connection.commit();

            if (log.isDebugEnabled()) {
                log.debug("Created deduplication alert: " + alert.getAlertId());
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Error rolling back transaction", rollbackEx);
                }
            }
            String msg = "Error creating deduplication alert: " + alert.getAlertId();
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("Error closing statement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(initialAutoCommit);
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection", e);
                }
            }
        }
    }

    @Override
    public DeduplicationAlert getAlert(String alertId, String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_ALERT)) {

            stmt.setString(1, alertId);
            stmt.setString(2, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAlert(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            String msg = "Error retrieving alert: " + alertId;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public List<DeduplicationAlert> getPendingAlerts(String organization) throws APIMGovernanceException {
        List<DeduplicationAlert> alerts = new ArrayList<>();

        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_PENDING_ALERTS)) {

            stmt.setString(1, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }

        } catch (SQLException e) {
            String msg = "Error retrieving pending alerts for organization: " + organization;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }

        return alerts;
    }

    @Override
    public List<DeduplicationAlert> getAlertsForApi(String apiUuid, String organization) 
            throws APIMGovernanceException {
        List<DeduplicationAlert> alerts = new ArrayList<>();

        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_ALERTS_FOR_API)) {

            stmt.setString(1, apiUuid);
            stmt.setString(2, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }

        } catch (SQLException e) {
            String msg = "Error retrieving alerts for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }

        return alerts;
    }

    @Override
    public List<DeduplicationAlert> getAlerts(String organization, int offset, int limit,
                                               DeduplicationAlert.AlertStatus status) throws APIMGovernanceException {
        List<DeduplicationAlert> alerts = new ArrayList<>();
        String query = status == null ? GET_ALERTS_PAGINATED : GET_ALERTS_PAGINATED_WITH_STATUS;

        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, organization);
            if (status != null) {
                stmt.setString(paramIndex++, status.name());
            }
            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }

        } catch (SQLException e) {
            String msg = "Error retrieving alerts for organization: " + organization;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }

        return alerts;
    }

    @Override
    public void updateAlertStatus(String alertId, DeduplicationAlert.AlertStatus status,
                                   String resolvedBy, String organization) throws APIMGovernanceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        boolean initialAutoCommit = true;
        
        try {
            connection = APIMGovernanceDBUtil.getConnection();
            initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            
            stmt = connection.prepareStatement(UPDATE_ALERT_STATUS);
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, resolvedBy);
            stmt.setString(4, alertId);
            stmt.setString(5, organization);

            stmt.executeUpdate();
            connection.commit();

            if (log.isDebugEnabled()) {
                log.debug("Updated alert status: " + alertId + " -> " + status);
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Error rolling back transaction", rollbackEx);
                }
            }
            String msg = "Error updating alert status: " + alertId;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("Error closing statement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(initialAutoCommit);
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection", e);
                }
            }
        }
    }

    @Override
    public void deleteAlert(String alertId, String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE_ALERT)) {

            stmt.setString(1, alertId);
            stmt.setString(2, organization);

            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Deleted alert: " + alertId);
            }

        } catch (SQLException e) {
            String msg = "Error deleting alert: " + alertId;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public void deleteAlertsForApi(String apiUuid, String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE_ALERTS_FOR_API)) {

            stmt.setString(1, apiUuid);
            stmt.setString(2, organization);

            int deleted = stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Deleted " + deleted + " alerts for API: " + apiUuid);
            }

        } catch (SQLException e) {
            String msg = "Error deleting alerts for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public int getPendingAlertCount(String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_PENDING_ALERTS)) {

            stmt.setString(1, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            String msg = "Error counting pending alerts for organization: " + organization;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public void createDecision(DeduplicationDecision decision) throws APIMGovernanceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        boolean initialAutoCommit = true;
        
        try {
            connection = APIMGovernanceDBUtil.getConnection();
            initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            
            stmt = connection.prepareStatement(INSERT_DECISION);
            stmt.setString(1, decision.getDecisionId());
            stmt.setString(2, decision.getDecisionId()); // Alert ID (assuming 1:1 for now)
            stmt.setString(3, decision.getNewApiUuid());
            stmt.setString(4, decision.getExistingApiUuid());
            stmt.setDouble(5, decision.getSimilarityScore());
            stmt.setString(6, decision.getAction().name());
            stmt.setString(7, decision.getJustification());
            stmt.setString(8, decision.getDecidedBy());
            stmt.setTimestamp(9, Timestamp.valueOf(decision.getDecidedAt()));
            stmt.setString(10, decision.getOrganization());
            stmt.setString(11, decision.getStatus().name());

            stmt.executeUpdate();
            connection.commit();

            if (log.isDebugEnabled()) {
                log.debug("Created decision: " + decision.getDecisionId());
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Error rolling back transaction", rollbackEx);
                }
            }
            String msg = "Error creating decision: " + decision.getDecisionId();
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("Error closing statement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(initialAutoCommit);
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection", e);
                }
            }
        }
    }

    @Override
    public DeduplicationDecision getDecision(String decisionId, String organization) 
            throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_DECISION)) {

            stmt.setString(1, decisionId);
            stmt.setString(2, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDecision(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            String msg = "Error retrieving decision: " + decisionId;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public List<DeduplicationDecision> getDecisionsForAlert(String alertId, String organization) 
            throws APIMGovernanceException {
        List<DeduplicationDecision> decisions = new ArrayList<>();

        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_DECISIONS_FOR_ALERT)) {

            stmt.setString(1, alertId);
            stmt.setString(2, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    decisions.add(mapResultSetToDecision(rs));
                }
            }

        } catch (SQLException e) {
            String msg = "Error retrieving decisions for alert: " + alertId;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }

        return decisions;
    }

    @Override
    public List<DeduplicationDecision> getDecisionHistoryForApi(String apiUuid, String organization) 
            throws APIMGovernanceException {
        List<DeduplicationDecision> decisions = new ArrayList<>();

        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_DECISION_HISTORY_FOR_API)) {

            stmt.setString(1, apiUuid);
            stmt.setString(2, apiUuid);
            stmt.setString(3, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    decisions.add(mapResultSetToDecision(rs));
                }
            }

        } catch (SQLException e) {
            String msg = "Error retrieving decision history for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }

        return decisions;
    }

    @Override
    public void updateDecisionStatus(String decisionId, DeduplicationDecision.DecisionStatus status,
                                      String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_DECISION_STATUS)) {

            stmt.setString(1, status.name());
            stmt.setString(2, decisionId);
            stmt.setString(3, organization);

            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error updating decision status: " + decisionId;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    // ==================== Helper Methods ====================

    private DeduplicationAlert mapResultSetToAlert(ResultSet rs) throws SQLException {
        DeduplicationAlert alert = new DeduplicationAlert();

        alert.setAlertId(rs.getString("ALERT_ID"));
        alert.setNewApiUuid(rs.getString("NEW_API_UUID"));
        alert.setNewApiName(rs.getString("NEW_API_NAME"));
        alert.setNewApiVersion(rs.getString("NEW_API_VERSION"));
        alert.setHighestSimilarity(rs.getDouble("HIGHEST_SIMILARITY"));
        alert.setSeverity(DeduplicationAlert.Severity.valueOf(rs.getString("SEVERITY")));
        alert.setStatus(DeduplicationAlert.AlertStatus.valueOf(rs.getString("STATUS")));
        alert.setOrganization(rs.getString("ORGANIZATION"));
        alert.setMessage(rs.getString("MESSAGE"));
        alert.setRecommendation(rs.getString("RECOMMENDATION"));
        
        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) {
            alert.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp resolvedAt = rs.getTimestamp("RESOLVED_AT");
        if (resolvedAt != null) {
            alert.setResolvedAt(resolvedAt.toLocalDateTime());
        }
        
        alert.setResolvedBy(rs.getString("RESOLVED_BY"));

        // Deserialize similar APIs
        String similarApisJson = rs.getString("SIMILAR_APIS_JSON");
        if (similarApisJson != null && !similarApisJson.isEmpty()) {
            try {
                List<DeduplicationAlert.SimilarApiEvidence> similarApis =
                        objectMapper.readValue(similarApisJson,
                                new TypeReference<List<DeduplicationAlert.SimilarApiEvidence>>() { });
                alert.setSimilarApis(similarApis);
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize similar APIs JSON for alert: " + alert.getAlertId(), e);
            }
        }

        // Set available actions
        alert.setDefaultAvailableActions();

        return alert;
    }

    private DeduplicationDecision mapResultSetToDecision(ResultSet rs) throws SQLException {
        DeduplicationDecision decision = new DeduplicationDecision();

        decision.setDecisionId(rs.getString("DECISION_ID"));
        decision.setNewApiUuid(rs.getString("NEW_API_UUID"));
        decision.setExistingApiUuid(rs.getString("EXISTING_API_UUID"));
        decision.setSimilarityScore(rs.getDouble("SIMILARITY_SCORE"));
        decision.setAction(DeduplicationDecision.Action.valueOf(rs.getString("ACTION")));
        decision.setJustification(rs.getString("JUSTIFICATION"));
        decision.setDecidedBy(rs.getString("DECIDED_BY"));
        
        Timestamp decidedAt = rs.getTimestamp("DECIDED_AT");
        if (decidedAt != null) {
            decision.setDecidedAt(decidedAt.toLocalDateTime());
        }
        
        decision.setOrganization(rs.getString("ORGANIZATION"));
        decision.setStatus(DeduplicationDecision.DecisionStatus.valueOf(rs.getString("STATUS")));

        return decision;
    }

    private String serializeSimilarApis(List<DeduplicationAlert.SimilarApiEvidence> similarApis) 
            throws APIMGovernanceException {
        try {
            return objectMapper.writeValueAsString(similarApis != null ? similarApis : new ArrayList<>());
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException("Failed to serialize similar APIs", e);
        }
    }
}
