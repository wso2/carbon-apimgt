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

package org.wso2.carbon.apimgt.governance.gatekeeper.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.DeduplicationAlertDAO;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.impl.DeduplicationAlertDAOImpl;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationAlert;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationDecision;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing deduplication alerts and user decisions.
 * This service provides the decision feedback loop functionality, allowing
 * users to take action on detected duplicate APIs.
 */
public class DeduplicationAlertService {

    private static final Log log = LogFactory.getLog(DeduplicationAlertService.class);

    private static volatile DeduplicationAlertService instance;

    private final DeduplicationAlertDAO alertDAO;
    private final GatekeeperService gatekeeperService;

    /**
     * Private constructor for singleton.
     */
    private DeduplicationAlertService() {
        this.alertDAO = DeduplicationAlertDAOImpl.getInstance();
        this.gatekeeperService = GatekeeperService.getInstance();
    }

    /**
     * Gets the singleton instance.
     *
     * @return DeduplicationAlertService instance
     */
    public static DeduplicationAlertService getInstance() {
        if (instance == null) {
            synchronized (DeduplicationAlertService.class) {
                if (instance == null) {
                    instance = new DeduplicationAlertService();
                }
            }
        }
        return instance;
    }

    /**
     * Creates an alert from a duplicate check result.
     *
     * @param checkResult  The duplicate check result
     * @param apiName      The name of the new API
     * @param apiVersion   The version of the new API
     * @param apiContext   The context of the new API
     * @param createdBy    Who created the API
     * @param organization The organization
     * @return The created alert
     * @throws APIMGovernanceException If alert creation fails
     */
    public DeduplicationAlert createAlertFromCheckResult(DuplicateCheckResult checkResult,
                                                          String apiName,
                                                          String apiVersion,
                                                          String apiContext,
                                                          String createdBy,
                                                          String organization) throws APIMGovernanceException {

        if (!checkResult.hasDuplicates()) {
            log.debug("No duplicates found, skipping alert creation for API: " + checkResult.getApiId());
            return null;
        }

        // Create the alert
        DeduplicationAlert alert = new DeduplicationAlert();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setNewApiUuid(checkResult.getApiId());
        alert.setNewApiName(apiName);
        alert.setNewApiVersion(apiVersion);
        alert.setOrganization(organization);
        alert.setStatus(DeduplicationAlert.AlertStatus.PENDING);

        // Add similar API evidence
        double highestSimilarity = 0.0;
        for (DuplicateCheckResult.SimilarAPI similar : checkResult.getSimilarAPIs()) {
            DeduplicationAlert.SimilarApiEvidence evidence = new DeduplicationAlert.SimilarApiEvidence();
            evidence.setApiUuid(similar.getApiId());
            evidence.setSimilarityScore(similar.getSimilarityScore());
            
            // Create breakdown from similarity score
            DeduplicationAlert.SimilarityBreakdown breakdown = 
                    new DeduplicationAlert.SimilarityBreakdown(similar.getSimilarityScore());
            evidence.setSimilarityBreakdown(breakdown);
            
            alert.addSimilarApi(evidence);
            
            if (similar.getSimilarityScore() > highestSimilarity) {
                highestSimilarity = similar.getSimilarityScore();
            }
        }

        alert.setHighestSimilarity(highestSimilarity);
        alert.setSeverity(DeduplicationAlert.Severity.fromSimilarityScore(highestSimilarity));

        // Generate message and recommendation
        alert.setMessage(String.format(
                "Potential duplicate detected: API '%s' has %.1f%% similarity with %d existing API(s).",
                apiName, highestSimilarity * 100, checkResult.getSimilarAPIs().size()));
        alert.generateRecommendation();
        alert.setDefaultAvailableActions();

        // Persist the alert
        alertDAO.createAlert(alert);

        log.info("Created deduplication alert: " + alert.getAlertId() + " for API: " + apiName);

        return alert;
    }

    /**
     * Gets all pending alerts for an organization.
     *
     * @param organization The organization
     * @return List of pending alerts
     * @throws APIMGovernanceException If retrieval fails
     */
    public List<DeduplicationAlert> getPendingAlerts(String organization) throws APIMGovernanceException {
        return alertDAO.getPendingAlerts(organization);
    }

    /**
     * Gets an alert by ID.
     *
     * @param alertId      The alert ID
     * @param organization The organization
     * @return The alert or null if not found
     * @throws APIMGovernanceException If retrieval fails
     */
    public DeduplicationAlert getAlert(String alertId, String organization) throws APIMGovernanceException {
        return alertDAO.getAlert(alertId, organization);
    }

    /**
     * Gets alerts for a specific API.
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @return List of alerts
     * @throws APIMGovernanceException If retrieval fails
     */
    public List<DeduplicationAlert> getAlertsForApi(String apiUuid, String organization) 
            throws APIMGovernanceException {
        return alertDAO.getAlertsForApi(apiUuid, organization);
    }

    /**
     * Gets alerts with pagination.
     *
     * @param organization The organization
     * @param offset       Offset
     * @param limit        Limit
     * @param status       Optional status filter
     * @return List of alerts
     * @throws APIMGovernanceException If retrieval fails
     */
    public List<DeduplicationAlert> getAlerts(String organization, int offset, int limit,
                                               DeduplicationAlert.AlertStatus status) throws APIMGovernanceException {
        return alertDAO.getAlerts(organization, offset, limit, status);
    }

    /**
     * Gets the count of pending alerts.
     *
     * @param organization The organization
     * @return Count of pending alerts
     * @throws APIMGovernanceException If count fails
     */
    public int getPendingAlertCount(String organization) throws APIMGovernanceException {
        return alertDAO.getPendingAlertCount(organization);
    }

    /**
     * Applies a user decision to an alert.
     *
     * @param alertId          The alert ID
     * @param action           The action to take
     * @param existingApiUuid  The existing API UUID (for versioning/merge)
     * @param justification    User justification
     * @param decidedBy        Who made the decision
     * @param organization     The organization
     * @return The result of applying the decision
     * @throws APIMGovernanceException If decision application fails
     */
    public DecisionResult applyDecision(String alertId,
                                        DeduplicationDecision.Action action,
                                        String existingApiUuid,
                                        String justification,
                                        String decidedBy,
                                        String organization) throws APIMGovernanceException {

        // Get the alert
        DeduplicationAlert alert = alertDAO.getAlert(alertId, organization);
        if (alert == null) {
            throw new APIMGovernanceException("Alert not found: " + alertId);
        }

        if (alert.getStatus() != DeduplicationAlert.AlertStatus.PENDING) {
            throw new APIMGovernanceException("Alert is not in PENDING status: " + alertId);
        }

        // Create the decision record
        DeduplicationDecision decision = new DeduplicationDecision.Builder()
                .decisionId(UUID.randomUUID().toString())
                .newApiUuid(alert.getNewApiUuid())
                .newApiName(alert.getNewApiName())
                .newApiVersion(alert.getNewApiVersion())
                .existingApiUuid(existingApiUuid)
                .similarityScore(alert.getHighestSimilarity())
                .action(action)
                .justification(justification)
                .decidedBy(decidedBy)
                .organization(organization)
                .status(DeduplicationDecision.DecisionStatus.APPLIED)
                .build();
        decision.setDecidedAt(LocalDateTime.now());

        // Apply the decision based on action
        DecisionResult result = new DecisionResult();
        result.setDecisionId(decision.getDecisionId());
        result.setAction(action);

        try {
            switch (action) {
                case OVERRIDE:
                    // Keep both APIs - just log and update status
                    result.setSuccess(true);
                    result.setMessage("Both APIs will be kept as separate entities.");
                    log.info("Override decision applied - keeping both APIs. Alert: " + alertId);
                    break;

                case DECLINE:
                    // Mark the new API as rejected - it should be deleted by the caller
                    result.setSuccess(true);
                    result.setMessage("The new API creation has been declined. Please delete the API.");
                    result.setRequiresAction(true);
                    result.setRequiredAction("DELETE_NEW_API");
                    result.setTargetApiUuid(alert.getNewApiUuid());
                    log.info("Decline decision applied - new API should be deleted. Alert: " + alertId);
                    break;

                case CREATE_VERSION:
                    // New API should become a version of the existing API
                    result.setSuccess(true);
                    result.setMessage("Create the new API as a version of the existing API: " + existingApiUuid);
                    result.setRequiresAction(true);
                    result.setRequiredAction("CREATE_AS_VERSION");
                    result.setTargetApiUuid(existingApiUuid);
                    log.info("Create-version decision applied. Alert: " + alertId);
                    break;

                case MERGE:
                    // Merge functionality - future enhancement
                    result.setSuccess(true);
                    result.setMessage("Merge functionality is not yet implemented. " +
                            "Please manually merge the APIs.");
                    result.setRequiresAction(true);
                    result.setRequiredAction("MANUAL_MERGE");
                    log.info("Merge decision applied. Alert: " + alertId);
                    break;

                case DEFER:
                    // Keep the alert pending for later review
                    decision.setStatus(DeduplicationDecision.DecisionStatus.PENDING);
                    result.setSuccess(true);
                    result.setMessage("Decision deferred. The alert will remain pending for review.");
                    // Don't update alert status to resolved
                    alertDAO.createDecision(decision);
                    return result;

                default:
                    throw new APIMGovernanceException("Unknown action: " + action);
            }

            // Update alert status
            alertDAO.updateAlertStatus(alertId, DeduplicationAlert.AlertStatus.RESOLVED, decidedBy, organization);

            // Store the decision
            alertDAO.createDecision(decision);

            result.setAlertResolved(true);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Failed to apply decision: " + e.getMessage());
            log.error("Error applying decision for alert: " + alertId, e);
            throw new APIMGovernanceException("Failed to apply decision", e);
        }

        return result;
    }

    /**
     * Dismisses an alert without taking action.
     *
     * @param alertId      The alert ID
     * @param dismissedBy  Who dismissed the alert
     * @param organization The organization
     * @throws APIMGovernanceException If dismissal fails
     */
    public void dismissAlert(String alertId, String dismissedBy, String organization) 
            throws APIMGovernanceException {
        alertDAO.updateAlertStatus(alertId, DeduplicationAlert.AlertStatus.DISMISSED, 
                dismissedBy, organization);
        log.info("Alert dismissed: " + alertId + " by " + dismissedBy);
    }

    /**
     * Auto-resolves alerts when an API is deleted.
     *
     * @param apiUuid      The deleted API UUID
     * @param organization The organization
     * @throws APIMGovernanceException If auto-resolve fails
     */
    public void autoResolveAlertsForDeletedApi(String apiUuid, String organization) 
            throws APIMGovernanceException {
        List<DeduplicationAlert> alerts = alertDAO.getAlertsForApi(apiUuid, organization);
        for (DeduplicationAlert alert : alerts) {
            if (alert.getStatus() == DeduplicationAlert.AlertStatus.PENDING) {
                alertDAO.updateAlertStatus(alert.getAlertId(), 
                        DeduplicationAlert.AlertStatus.AUTO_RESOLVED, "SYSTEM", organization);
                log.info("Auto-resolved alert due to API deletion: " + alert.getAlertId());
            }
        }
    }

    /**
     * Gets decision history for an API.
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @return List of decisions
     * @throws APIMGovernanceException If retrieval fails
     */
    public List<DeduplicationDecision> getDecisionHistory(String apiUuid, String organization) 
            throws APIMGovernanceException {
        return alertDAO.getDecisionHistoryForApi(apiUuid, organization);
    }

    /**
     * Result class for decision application.
     */
    public static class DecisionResult {
        private String decisionId;
        private DeduplicationDecision.Action action;
        private boolean success;
        private String message;
        private boolean alertResolved;
        private boolean requiresAction;
        private String requiredAction;
        private String targetApiUuid;

        public String getDecisionId() {
            return decisionId;
        }

        public void setDecisionId(String decisionId) {
            this.decisionId = decisionId;
        }

        public DeduplicationDecision.Action getAction() {
            return action;
        }

        public void setAction(DeduplicationDecision.Action action) {
            this.action = action;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isAlertResolved() {
            return alertResolved;
        }

        public void setAlertResolved(boolean alertResolved) {
            this.alertResolved = alertResolved;
        }

        public boolean isRequiresAction() {
            return requiresAction;
        }

        public void setRequiresAction(boolean requiresAction) {
            this.requiresAction = requiresAction;
        }

        public String getRequiredAction() {
            return requiredAction;
        }

        public void setRequiredAction(String requiredAction) {
            this.requiredAction = requiredAction;
        }

        public String getTargetApiUuid() {
            return targetApiUuid;
        }

        public void setTargetApiUuid(String targetApiUuid) {
            this.targetApiUuid = targetApiUuid;
        }
    }
}
