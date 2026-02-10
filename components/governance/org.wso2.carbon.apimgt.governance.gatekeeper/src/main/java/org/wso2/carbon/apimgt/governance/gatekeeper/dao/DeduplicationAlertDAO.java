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

package org.wso2.carbon.apimgt.governance.gatekeeper.dao;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationAlert;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationDecision;

import java.util.List;

/**
 * DAO interface for managing Deduplication Alerts and Decisions.
 */
public interface DeduplicationAlertDAO {

    // ==================== Alert Operations ====================

    /**
     * Creates a new deduplication alert.
     *
     * @param alert The alert to create
     * @throws APIMGovernanceException If creation fails
     */
    void createAlert(DeduplicationAlert alert) throws APIMGovernanceException;

    /**
     * Gets an alert by ID.
     *
     * @param alertId      The alert ID
     * @param organization The organization
     * @return The alert or null if not found
     * @throws APIMGovernanceException If retrieval fails
     */
    DeduplicationAlert getAlert(String alertId, String organization) throws APIMGovernanceException;

    /**
     * Gets all pending alerts for an organization.
     *
     * @param organization The organization
     * @return List of pending alerts
     * @throws APIMGovernanceException If retrieval fails
     */
    List<DeduplicationAlert> getPendingAlerts(String organization) throws APIMGovernanceException;

    /**
     * Gets alerts for a specific API.
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @return List of alerts for the API
     * @throws APIMGovernanceException If retrieval fails
     */
    List<DeduplicationAlert> getAlertsForApi(String apiUuid, String organization) throws APIMGovernanceException;

    /**
     * Gets all alerts for an organization with pagination.
     *
     * @param organization The organization
     * @param offset       Offset for pagination
     * @param limit        Limit for pagination
     * @param status       Optional status filter (null for all)
     * @return List of alerts
     * @throws APIMGovernanceException If retrieval fails
     */
    List<DeduplicationAlert> getAlerts(String organization, int offset, int limit, 
                                        DeduplicationAlert.AlertStatus status) throws APIMGovernanceException;

    /**
     * Updates an alert status.
     *
     * @param alertId      The alert ID
     * @param status       The new status
     * @param resolvedBy   Who resolved the alert (optional)
     * @param organization The organization
     * @throws APIMGovernanceException If update fails
     */
    void updateAlertStatus(String alertId, DeduplicationAlert.AlertStatus status, 
                           String resolvedBy, String organization) throws APIMGovernanceException;

    /**
     * Deletes an alert and its associated decisions.
     *
     * @param alertId      The alert ID
     * @param organization The organization
     * @throws APIMGovernanceException If deletion fails
     */
    void deleteAlert(String alertId, String organization) throws APIMGovernanceException;

    /**
     * Deletes all alerts for an API (when API is deleted).
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @throws APIMGovernanceException If deletion fails
     */
    void deleteAlertsForApi(String apiUuid, String organization) throws APIMGovernanceException;

    /**
     * Gets the count of pending alerts for an organization.
     *
     * @param organization The organization
     * @return Count of pending alerts
     * @throws APIMGovernanceException If count fails
     */
    int getPendingAlertCount(String organization) throws APIMGovernanceException;

    // ==================== Decision Operations ====================

    /**
     * Creates a new decision for an alert.
     *
     * @param decision The decision to create
     * @throws APIMGovernanceException If creation fails
     */
    void createDecision(DeduplicationDecision decision) throws APIMGovernanceException;

    /**
     * Gets a decision by ID.
     *
     * @param decisionId   The decision ID
     * @param organization The organization
     * @return The decision or null if not found
     * @throws APIMGovernanceException If retrieval fails
     */
    DeduplicationDecision getDecision(String decisionId, String organization) throws APIMGovernanceException;

    /**
     * Gets all decisions for an alert.
     *
     * @param alertId      The alert ID
     * @param organization The organization
     * @return List of decisions
     * @throws APIMGovernanceException If retrieval fails
     */
    List<DeduplicationDecision> getDecisionsForAlert(String alertId, String organization) 
            throws APIMGovernanceException;

    /**
     * Gets decision history for an API.
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @return List of decisions involving this API
     * @throws APIMGovernanceException If retrieval fails
     */
    List<DeduplicationDecision> getDecisionHistoryForApi(String apiUuid, String organization) 
            throws APIMGovernanceException;

    /**
     * Updates a decision status.
     *
     * @param decisionId   The decision ID
     * @param status       The new status
     * @param organization The organization
     * @throws APIMGovernanceException If update fails
     */
    void updateDecisionStatus(String decisionId, DeduplicationDecision.DecisionStatus status, 
                              String organization) throws APIMGovernanceException;
}
