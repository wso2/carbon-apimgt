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

package org.wso2.carbon.apimgt.governance.gatekeeper.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a deduplication alert that requires user attention.
 * This is created when the Gatekeeper detects a potential duplicate API.
 * The alert contains evidence about the similarity and available actions.
 */
public class DeduplicationAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Alert status enum.
     */
    public enum AlertStatus {
        PENDING,    // Awaiting user decision
        RESOLVED,   // User has made a decision
        DISMISSED,  // User dismissed without action
        EXPIRED,    // Alert has expired
        AUTO_RESOLVED // System auto-resolved (e.g., API was deleted)
    }

    /**
     * Alert severity based on similarity score.
     */
    public enum Severity {
        CRITICAL("CRITICAL", "Very high similarity (≥95%)", 0.95),
        HIGH("HIGH", "High similarity (≥80%)", 0.80),
        MEDIUM("MEDIUM", "Medium similarity (≥60%)", 0.60),
        LOW("LOW", "Low similarity (≥50%)", 0.50);

        private final String code;
        private final String description;
        private final double threshold;

        Severity(String code, String description, double threshold) {
            this.code = code;
            this.description = description;
            this.threshold = threshold;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public double getThreshold() {
            return threshold;
        }

        public static Severity fromSimilarityScore(double score) {
            if (score >= CRITICAL.threshold) {
                return CRITICAL;
            } else if (score >= HIGH.threshold) {
                return HIGH;
            } else if (score >= MEDIUM.threshold) {
                return MEDIUM;
            } else {
                return LOW;
            }
        }
    }

    @JsonProperty("alertId")
    private String alertId;

    @JsonProperty("newApiUuid")
    private String newApiUuid;

    @JsonProperty("newApiName")
    private String newApiName;

    @JsonProperty("newApiVersion")
    private String newApiVersion;

    @JsonProperty("newApiContext")
    private String newApiContext;

    @JsonProperty("newApiCreatedBy")
    private String newApiCreatedBy;

    @JsonProperty("similarApis")
    private List<SimilarApiEvidence> similarApis;

    @JsonProperty("highestSimilarity")
    private double highestSimilarity;

    @JsonProperty("severity")
    private Severity severity;

    @JsonProperty("status")
    private AlertStatus status;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("resolvedAt")
    private LocalDateTime resolvedAt;

    @JsonProperty("resolvedBy")
    private String resolvedBy;

    @JsonProperty("decision")
    private DeduplicationDecision decision;

    @JsonProperty("availableActions")
    private List<DeduplicationDecision.Action> availableActions;

    @JsonProperty("message")
    private String message;

    @JsonProperty("recommendation")
    private String recommendation;

    /**
     * Inner class representing evidence for a similar API.
     */
    public static class SimilarApiEvidence implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("apiUuid")
        private String apiUuid;

        @JsonProperty("apiName")
        private String apiName;

        @JsonProperty("apiVersion")
        private String apiVersion;

        @JsonProperty("apiContext")
        private String apiContext;

        @JsonProperty("apiStatus")
        private String apiStatus;

        @JsonProperty("createdBy")
        private String createdBy;

        @JsonProperty("similarityScore")
        private double similarityScore;

        @JsonProperty("similarityBreakdown")
        private SimilarityBreakdown similarityBreakdown;

        @JsonProperty("commonEndpoints")
        private List<String> commonEndpoints;

        @JsonProperty("commonSchemas")
        private List<String> commonSchemas;

        public SimilarApiEvidence() {
            this.commonEndpoints = new ArrayList<>();
            this.commonSchemas = new ArrayList<>();
        }

        // Getters and setters
        public String getApiUuid() {
            return apiUuid;
        }

        public void setApiUuid(String apiUuid) {
            this.apiUuid = apiUuid;
        }

        public String getApiName() {
            return apiName;
        }

        public void setApiName(String apiName) {
            this.apiName = apiName;
        }

        public String getApiVersion() {
            return apiVersion;
        }

        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }

        public String getApiContext() {
            return apiContext;
        }

        public void setApiContext(String apiContext) {
            this.apiContext = apiContext;
        }

        public String getApiStatus() {
            return apiStatus;
        }

        public void setApiStatus(String apiStatus) {
            this.apiStatus = apiStatus;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public double getSimilarityScore() {
            return similarityScore;
        }

        public void setSimilarityScore(double similarityScore) {
            this.similarityScore = similarityScore;
        }

        public SimilarityBreakdown getSimilarityBreakdown() {
            return similarityBreakdown;
        }

        public void setSimilarityBreakdown(SimilarityBreakdown similarityBreakdown) {
            this.similarityBreakdown = similarityBreakdown;
        }

        public List<String> getCommonEndpoints() {
            return commonEndpoints;
        }

        public void setCommonEndpoints(List<String> commonEndpoints) {
            this.commonEndpoints = commonEndpoints;
        }

        public List<String> getCommonSchemas() {
            return commonSchemas;
        }

        public void setCommonSchemas(List<String> commonSchemas) {
            this.commonSchemas = commonSchemas;
        }
    }

    /**
     * Inner class representing breakdown of similarity metrics.
     */
    public static class SimilarityBreakdown implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("overallSimilarity")
        private double overallSimilarity;

        @JsonProperty("pathSimilarity")
        private double pathSimilarity;

        @JsonProperty("schemaSimilarity")
        private double schemaSimilarity;

        @JsonProperty("operationSimilarity")
        private double operationSimilarity;

        @JsonProperty("parameterSimilarity")
        private double parameterSimilarity;

        public SimilarityBreakdown() {
        }

        public SimilarityBreakdown(double overallSimilarity) {
            this.overallSimilarity = overallSimilarity;
            // Default equal distribution if breakdown not calculated
            this.pathSimilarity = overallSimilarity;
            this.schemaSimilarity = overallSimilarity;
            this.operationSimilarity = overallSimilarity;
            this.parameterSimilarity = overallSimilarity;
        }

        // Getters and setters
        public double getOverallSimilarity() {
            return overallSimilarity;
        }

        public void setOverallSimilarity(double overallSimilarity) {
            this.overallSimilarity = overallSimilarity;
        }

        public double getPathSimilarity() {
            return pathSimilarity;
        }

        public void setPathSimilarity(double pathSimilarity) {
            this.pathSimilarity = pathSimilarity;
        }

        public double getSchemaSimilarity() {
            return schemaSimilarity;
        }

        public void setSchemaSimilarity(double schemaSimilarity) {
            this.schemaSimilarity = schemaSimilarity;
        }

        public double getOperationSimilarity() {
            return operationSimilarity;
        }

        public void setOperationSimilarity(double operationSimilarity) {
            this.operationSimilarity = operationSimilarity;
        }

        public double getParameterSimilarity() {
            return parameterSimilarity;
        }

        public void setParameterSimilarity(double parameterSimilarity) {
            this.parameterSimilarity = parameterSimilarity;
        }
    }

    /**
     * Default constructor.
     */
    public DeduplicationAlert() {
        this.similarApis = new ArrayList<>();
        this.availableActions = new ArrayList<>();
        this.status = AlertStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Set available actions based on similarity and policy.
     */
    public void setDefaultAvailableActions() {
        this.availableActions = new ArrayList<>();
        
        // OVERRIDE is always available
        this.availableActions.add(DeduplicationDecision.Action.OVERRIDE);
        
        // DECLINE is always available
        this.availableActions.add(DeduplicationDecision.Action.DECLINE);
        
        // CREATE_VERSION available for high similarity
        if (this.highestSimilarity >= 0.70) {
            this.availableActions.add(DeduplicationDecision.Action.CREATE_VERSION);
        }
        
        // DEFER is always available
        this.availableActions.add(DeduplicationDecision.Action.DEFER);
    }

    /**
     * Generate a recommendation based on similarity score.
     */
    public void generateRecommendation() {
        if (highestSimilarity >= 0.95) {
            this.recommendation = "STRONGLY RECOMMENDED: The new API appears to be nearly "
                    + "identical to an existing API. Consider creating a new version instead.";
        } else if (highestSimilarity >= 0.80) {
            this.recommendation = "RECOMMENDED: The new API has high similarity with existing "
                    + "APIs. Review the differences carefully before proceeding.";
        } else if (highestSimilarity >= 0.60) {
            this.recommendation = "ADVISORY: Moderate similarity detected. Ensure the new API "
                    + "serves a distinct purpose.";
        } else {
            this.recommendation = "INFO: Some similarity detected. Review if these APIs have "
                    + "overlapping functionality.";
        }
    }

    // Getters and setters
    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getNewApiUuid() {
        return newApiUuid;
    }

    public void setNewApiUuid(String newApiUuid) {
        this.newApiUuid = newApiUuid;
    }

    public String getNewApiName() {
        return newApiName;
    }

    public void setNewApiName(String newApiName) {
        this.newApiName = newApiName;
    }

    public String getNewApiVersion() {
        return newApiVersion;
    }

    public void setNewApiVersion(String newApiVersion) {
        this.newApiVersion = newApiVersion;
    }

    public String getNewApiContext() {
        return newApiContext;
    }

    public void setNewApiContext(String newApiContext) {
        this.newApiContext = newApiContext;
    }

    public String getNewApiCreatedBy() {
        return newApiCreatedBy;
    }

    public void setNewApiCreatedBy(String newApiCreatedBy) {
        this.newApiCreatedBy = newApiCreatedBy;
    }

    public List<SimilarApiEvidence> getSimilarApis() {
        return similarApis;
    }

    public void setSimilarApis(List<SimilarApiEvidence> similarApis) {
        this.similarApis = similarApis;
    }

    public void addSimilarApi(SimilarApiEvidence evidence) {
        this.similarApis.add(evidence);
        if (evidence.getSimilarityScore() > this.highestSimilarity) {
            this.highestSimilarity = evidence.getSimilarityScore();
            this.severity = Severity.fromSimilarityScore(this.highestSimilarity);
        }
    }

    public double getHighestSimilarity() {
        return highestSimilarity;
    }

    public void setHighestSimilarity(double highestSimilarity) {
        this.highestSimilarity = highestSimilarity;
        this.severity = Severity.fromSimilarityScore(highestSimilarity);
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public DeduplicationDecision getDecision() {
        return decision;
    }

    public void setDecision(DeduplicationDecision decision) {
        this.decision = decision;
    }

    public List<DeduplicationDecision.Action> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<DeduplicationDecision.Action> availableActions) {
        this.availableActions = availableActions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    @Override
    public String toString() {
        return "DeduplicationAlert{" +
                "alertId='" + alertId + '\'' +
                ", newApiUuid='" + newApiUuid + '\'' +
                ", newApiName='" + newApiName + '\'' +
                ", similarApisCount=" + (similarApis != null ? similarApis.size() : 0) +
                ", highestSimilarity=" + highestSimilarity +
                ", severity=" + severity +
                ", status=" + status +
                '}';
    }
}
