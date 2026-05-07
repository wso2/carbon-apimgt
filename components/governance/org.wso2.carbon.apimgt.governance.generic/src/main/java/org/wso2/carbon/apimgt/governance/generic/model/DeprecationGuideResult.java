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

package org.wso2.carbon.apimgt.governance.generic.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model representing the result of a deprecation guide query.
 * When an API is about to be deprecated, this object carries
 * the structural-successor recommendation (if any) or flags a
 * migration-risk when no successor can be found.
 *
 * <ul>
 *   <li><b>Scenario A — Successor Found:</b>
 *       {@code successorFound == true}, successor fields populated,
 *       RFC 8594 header values pre-computed.</li>
 *   <li><b>Scenario B — Dead-End Risk:</b>
 *       {@code successorFound == false}, {@code migrationRisk == true},
 *       caller must require explicit user acknowledgement.</li>
 * </ul>
 */
public class DeprecationGuideResult implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── query context ────────────────────────────────────────────────
    @JsonProperty("apiUuid")
    private String apiUuid;

    @JsonProperty("apiName")
    private String apiName;

    @JsonProperty("apiVersion")
    private String apiVersion;

    @JsonProperty("organization")
    private String organization;

    // ── successor info (Scenario A) ──────────────────────────────────
    @JsonProperty("successorFound")
    private boolean successorFound;

    @JsonProperty("successorApiUuid")
    private String successorApiUuid;

    @JsonProperty("successorApiName")
    private String successorApiName;

    @JsonProperty("successorApiVersion")
    private String successorApiVersion;

    @JsonProperty("similarityPercentage")
    private double similarityPercentage;

    // ── RFC 8594 pre-computed headers ────────────────────────────────
    @JsonProperty("rfc8594LinkHeader")
    private String rfc8594LinkHeader;

    @JsonProperty("rfc8594SunsetHeader")
    private String rfc8594SunsetHeader;
    // ── successor classification ─────────────────────────────────────────
    /** Lifecycle state of the successor API (e.g. "PUBLISHED"). */
    @JsonProperty("successorStatus")
    private String successorStatus;

    /**
     * Classification of the successor.
     * <ul>
     *   <li>{@code OFFICIAL_VERSION} — same API name, different version (Priority 1).</li>
     *   <li>{@code SEMANTIC_NEIGHBOR} — different API name, structurally similar (Priority 2).</li>
     * </ul>
     */
    @JsonProperty("successorType")
    private String successorType;
    // ── migration risk (Scenario B) ──────────────────────────────────
    @JsonProperty("migrationRisk")
    private boolean migrationRisk;

    @JsonProperty("message")
    private String message;

    // ── multi-version candidates (Task 5) ────────────────────────────
    /**
     * All discovered successor candidates (multiple versions).
     * The first element is the recommended (highest-priority) successor.
     * Users can select a different one from the UI.
     */
    @JsonProperty("allCandidates")
    private List<SuccessorCandidate> allCandidates;

    // ── lifecycle & enforcement ──────────────────────────────────────
    /**
     * The lifecycle action that triggered this guide: "Deprecate" or "Retire".
     */
    @JsonProperty("lifecycleAction")
    private String lifecycleAction;

    /**
     * Enforcement mode from the ruleset: "warn" or "block".
     * When "block", the transition is rejected if no PUBLISHED successor exists.
     */
    @JsonProperty("enforcementMode")
    private String enforcementMode;

    /**
     * Whether a successor was carried over from the DEPRECATED state
     * (relevant when lifecycle action is "Retire").
     */
    @JsonProperty("successorCarriedOver")
    private boolean successorCarriedOver;

    /* ================================================================
     *  Constructors
     * ================================================================ */

    public DeprecationGuideResult() {
        this.allCandidates = new ArrayList<>();
    }

    /* ================================================================
     *  Static factories
     * ================================================================ */

    /** Successor type constant — official new version of the same API. */
    public static final String TYPE_OFFICIAL_VERSION = "OFFICIAL_VERSION";
    /** Successor type constant — structurally similar API with a different name. */
    public static final String TYPE_SEMANTIC_NEIGHBOR = "SEMANTIC_NEIGHBOR";

    /** Enforcement mode: allow transition with warning. */
    public static final String MODE_WARN = "warn";
    /** Enforcement mode: block transition if no successor. */
    public static final String MODE_BLOCK = "block";

    /**
     * Scenario A — Structural successor found.
     *
     * @param successorStatus  Lifecycle state of the successor (e.g. "PUBLISHED")
     * @param successorType    Classification: {@link #TYPE_OFFICIAL_VERSION} or
     *                         {@link #TYPE_SEMANTIC_NEIGHBOR}
     * @param allCandidates    All discovered successor candidates (may be multi-version)
     * @param lifecycleAction  The triggering lifecycle action ("Deprecate" or "Retire")
     * @param enforcementMode  Enforcement mode ("warn" or "block")
     */
    public static DeprecationGuideResult successorFound(
            String apiUuid, String apiName, String apiVersion, String organization,
            String successorUuid, String successorName, String successorVersion,
            double similarityPct,
            String linkHeader, String sunsetHeader,
            String successorStatus, String successorType,
            List<SuccessorCandidate> allCandidates,
            String lifecycleAction, String enforcementMode) {

        DeprecationGuideResult r = new DeprecationGuideResult();
        r.apiUuid = apiUuid;
        r.apiName = apiName;
        r.apiVersion = apiVersion;
        r.organization = organization;
        r.successorFound = true;
        r.successorApiUuid = successorUuid;
        r.successorApiName = successorName;
        r.successorApiVersion = successorVersion;
        r.similarityPercentage = similarityPct;
        r.rfc8594LinkHeader = linkHeader;
        r.rfc8594SunsetHeader = sunsetHeader;
        r.successorStatus = successorStatus;
        r.successorType = successorType;
        r.migrationRisk = false;
        r.allCandidates = allCandidates != null ? allCandidates : new ArrayList<>();
        r.lifecycleAction = lifecycleAction;
        r.enforcementMode = enforcementMode;
        r.message = String.format(
                "Successor found for %s %s → %s %s (%.1f%%, status: %s)",
                apiName, apiVersion, successorName, successorVersion,
                similarityPct, successorStatus);
        return r;
    }

    /**
     * Scenario B — No successor, dead-end migration risk.
     *
     * @param lifecycleAction The triggering lifecycle action ("Deprecate" or "Retire")
     * @param enforcementMode Enforcement mode ("warn" or "block")
     */
    public static DeprecationGuideResult noSuccessor(
            String apiUuid, String apiName, String apiVersion, String organization,
            String lifecycleAction, String enforcementMode) {

        DeprecationGuideResult r = new DeprecationGuideResult();
        r.apiUuid = apiUuid;
        r.apiName = apiName;
        r.apiVersion = apiVersion;
        r.organization = organization;
        r.successorFound = false;
        r.migrationRisk = true;
        r.lifecycleAction = lifecycleAction;
        r.enforcementMode = enforcementMode;
        r.message = "No valid successor detected. "
                + (lifecycleAction != null ? lifecycleAction + "ing" : "Deprecating")
                + " this API creates a MIGRATION RISK — consumers have no "
                + "structurally similar replacement.";
        return r;
    }

    /* ================================================================
     *  Getters / Setters
     * ================================================================ */

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

    public String getOrganization() {
        return organization;
    }
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public boolean isSuccessorFound() {
        return successorFound;
    }
    public void setSuccessorFound(boolean successorFound) {
        this.successorFound = successorFound;
    }

    public String getSuccessorApiUuid() {
        return successorApiUuid;
    }
    public void setSuccessorApiUuid(String successorApiUuid) {
        this.successorApiUuid = successorApiUuid;
    }

    public String getSuccessorApiName() {
        return successorApiName;
    }
    public void setSuccessorApiName(String successorApiName) {
        this.successorApiName = successorApiName;
    }

    public String getSuccessorApiVersion() {
        return successorApiVersion;
    }
    public void setSuccessorApiVersion(String successorApiVersion) {
        this.successorApiVersion = successorApiVersion;
    }

    public double getSimilarityPercentage() {
        return similarityPercentage;
    }
    public void setSimilarityPercentage(double similarityPercentage) {
        this.similarityPercentage = similarityPercentage;
    }

    public String getRfc8594LinkHeader() {
        return rfc8594LinkHeader;
    }
    public void setRfc8594LinkHeader(String rfc8594LinkHeader) {
        this.rfc8594LinkHeader = rfc8594LinkHeader;
    }

    public String getRfc8594SunsetHeader() {
        return rfc8594SunsetHeader;
    }
    public void setRfc8594SunsetHeader(String rfc8594SunsetHeader) {
        this.rfc8594SunsetHeader = rfc8594SunsetHeader;
    }

    public boolean isMigrationRisk() {
        return migrationRisk;
    }
    public void setMigrationRisk(boolean migrationRisk) {
        this.migrationRisk = migrationRisk;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuccessorStatus() {
        return successorStatus;
    }
    public void setSuccessorStatus(String successorStatus) {
        this.successorStatus = successorStatus;
    }

    public String getSuccessorType() {
        return successorType;
    }
    public void setSuccessorType(String successorType) {
        this.successorType = successorType;
    }

    public List<SuccessorCandidate> getAllCandidates() {
        return allCandidates != null ? Collections.unmodifiableList(allCandidates) : null;
    }
    public void setAllCandidates(List<SuccessorCandidate> allCandidates) {
        this.allCandidates = allCandidates != null ? new ArrayList<>(allCandidates) : null;
    }

    public String getLifecycleAction() {
        return lifecycleAction;
    }
    public void setLifecycleAction(String lifecycleAction) {
        this.lifecycleAction = lifecycleAction;
    }

    public String getEnforcementMode() {
        return enforcementMode;
    }
    public void setEnforcementMode(String enforcementMode) {
        this.enforcementMode = enforcementMode;
    }

    public boolean isSuccessorCarriedOver() {
        return successorCarriedOver;
    }
    public void setSuccessorCarriedOver(boolean successorCarriedOver) {
        this.successorCarriedOver = successorCarriedOver;
    }

    @Override
    public String toString() {
        if (successorFound) {
            return String.format(
                    "DeprecationGuide{successor=%s v%s, similarity=%.1f%%, "
                    + "status=%s, type=%s, candidates=%d, action=%s, mode=%s}",
                    successorApiName, successorApiVersion,
                    similarityPercentage, successorStatus,
                    successorType,
                    allCandidates != null ? allCandidates.size() : 0,
                    lifecycleAction, enforcementMode);
        }
        return String.format("DeprecationGuide{migrationRisk=true, noSuccessor, action=%s, mode=%s}",
                lifecycleAction, enforcementMode);
    }

    /* ================================================================
     *  Inner class: SuccessorCandidate
     * ================================================================ */

    /**
     * Represents a single successor candidate discovered via MinHash/LSH.
     * Multiple candidates are returned when the API has several versions
     * or semantic neighbors in PUBLISHED state.
     */
    public static class SuccessorCandidate implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("apiUuid")
        private String apiUuid;

        @JsonProperty("apiName")
        private String apiName;

        @JsonProperty("apiVersion")
        private String apiVersion;

        @JsonProperty("similarityPercentage")
        private double similarityPercentage;

        @JsonProperty("successorType")
        private String successorType;

        @JsonProperty("status")
        private String status;

        @JsonProperty("context")
        private String context;

        public SuccessorCandidate() {}

        public SuccessorCandidate(String apiUuid, String apiName, String apiVersion,
                                   double similarityPercentage, String successorType,
                                   String status, String context) {
            this.apiUuid = apiUuid;
            this.apiName = apiName;
            this.apiVersion = apiVersion;
            this.similarityPercentage = similarityPercentage;
            this.successorType = successorType;
            this.status = status;
            this.context = context;
        }

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

        public double getSimilarityPercentage() {
            return similarityPercentage;
        }
        public void setSimilarityPercentage(double similarityPercentage) {
            this.similarityPercentage = similarityPercentage;
        }

        public String getSuccessorType() {
            return successorType;
        }
        public void setSuccessorType(String successorType) {
            this.successorType = successorType;
        }

        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }

        public String getContext() {
            return context;
        }
        public void setContext(String context) {
            this.context = context;
        }

        @Override
        public String toString() {
            return String.format("%s v%s (%.1f%%, %s, %s)",
                    apiName, apiVersion, similarityPercentage, successorType, status);
        }
    }
}
