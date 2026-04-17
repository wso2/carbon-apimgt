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

/**
 * Model class representing a user decision on API deduplication conflict.
 * This captures what action the user takes when a duplicate API is detected.
 */
public class DeduplicationDecision implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Enum defining possible actions for handling duplicate APIs.
     */
    public enum Action {
        /**
         * Keep both APIs - override the duplicate warning.
         * User explicitly acknowledges the similarity and wants both APIs to exist.
         */
        OVERRIDE("OVERRIDE", "Keep both APIs as separate entities"),

        /**
         * Decline/reject the new API.
         * User decides not to create the duplicate API.
         */
        DECLINE("DECLINE", "Reject the new API"),

        /**
         * Create the new API as a version of the existing similar API.
         * This triggers a workflow to version the existing API.
         */
        CREATE_VERSION("CREATE_VERSION", "Create as a new version of existing API"),

        /**
         * Merge the new API definition into the existing API.
         * Combines endpoints/operations from both definitions.
         */
        MERGE("MERGE", "Merge with existing API"),

        /**
         * Defer the decision - keep the API in draft state pending review.
         */
        DEFER("DEFER", "Defer decision for later review");

        private final String code;
        private final String description;

        Action(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Action fromCode(String code) {
            for (Action action : values()) {
                if (action.code.equalsIgnoreCase(code)) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Unknown action code: " + code);
        }
    }

    @JsonProperty("decisionId")
    private String decisionId;

    @JsonProperty("newApiUuid")
    private String newApiUuid;

    @JsonProperty("newApiName")
    private String newApiName;

    @JsonProperty("newApiVersion")
    private String newApiVersion;

    @JsonProperty("existingApiUuid")
    private String existingApiUuid;

    @JsonProperty("existingApiName")
    private String existingApiName;

    @JsonProperty("existingApiVersion")
    private String existingApiVersion;

    @JsonProperty("similarityScore")
    private double similarityScore;

    @JsonProperty("action")
    private Action action;

    @JsonProperty("justification")
    private String justification;

    @JsonProperty("decidedBy")
    private String decidedBy;

    @JsonProperty("decidedAt")
    private LocalDateTime decidedAt;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("status")
    private DecisionStatus status;

    /**
     * Enum for decision status.
     */
    public enum DecisionStatus {
        PENDING,
        APPLIED,
        CANCELLED,
        EXPIRED
    }

    /**
     * Default constructor.
     */
    public DeduplicationDecision() {
        this.status = DecisionStatus.PENDING;
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * Builder class for creating DeduplicationDecision instances.
     */
    public static class Builder {
        private final DeduplicationDecision decision;

        /**
         * Creates a new Builder instance.
         */
        public Builder() {
            this.decision = new DeduplicationDecision();
        }

        /**
         * Sets the decision ID.
         *
         * @param decisionId The decision ID
         * @return This builder instance
         */
        public Builder decisionId(String decisionId) {
            decision.decisionId = decisionId;
            return this;
        }

        public Builder newApiUuid(String newApiUuid) {
            decision.newApiUuid = newApiUuid;
            return this;
        }

        public Builder newApiName(String newApiName) {
            decision.newApiName = newApiName;
            return this;
        }

        public Builder newApiVersion(String newApiVersion) {
            decision.newApiVersion = newApiVersion;
            return this;
        }

        public Builder existingApiUuid(String existingApiUuid) {
            decision.existingApiUuid = existingApiUuid;
            return this;
        }

        public Builder existingApiName(String existingApiName) {
            decision.existingApiName = existingApiName;
            return this;
        }

        public Builder existingApiVersion(String existingApiVersion) {
            decision.existingApiVersion = existingApiVersion;
            return this;
        }

        public Builder similarityScore(double similarityScore) {
            decision.similarityScore = similarityScore;
            return this;
        }

        public Builder action(Action action) {
            decision.action = action;
            return this;
        }

        public Builder justification(String justification) {
            decision.justification = justification;
            return this;
        }

        public Builder decidedBy(String decidedBy) {
            decision.decidedBy = decidedBy;
            return this;
        }

        public Builder organization(String organization) {
            decision.organization = organization;
            return this;
        }

        public Builder status(DecisionStatus status) {
            decision.status = status;
            return this;
        }

        public DeduplicationDecision build() {
            return decision;
        }
    }

    // Getters and Setters
    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
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

    public String getExistingApiUuid() {
        return existingApiUuid;
    }

    public void setExistingApiUuid(String existingApiUuid) {
        this.existingApiUuid = existingApiUuid;
    }

    public String getExistingApiName() {
        return existingApiName;
    }

    public void setExistingApiName(String existingApiName) {
        this.existingApiName = existingApiName;
    }

    public String getExistingApiVersion() {
        return existingApiVersion;
    }

    public void setExistingApiVersion(String existingApiVersion) {
        this.existingApiVersion = existingApiVersion;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(String decidedBy) {
        this.decidedBy = decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(DecisionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DeduplicationDecision{" +
                "decisionId='" + decisionId + '\'' +
                ", newApiUuid='" + newApiUuid + '\'' +
                ", existingApiUuid='" + existingApiUuid + '\'' +
                ", similarityScore=" + similarityScore +
                ", action=" + action +
                ", status=" + status +
                '}';
    }
}
