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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.generic.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.generic.internal.GenericServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.generic.model.ConflictReport;
import org.wso2.carbon.apimgt.governance.generic.model.DeduplicationResult;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled task that scans DEPRECATED APIs and uses MinHash/LSH structural similarity
 * to suggest potential successor APIs. This is the Phase 1 "Deprecation Guide" feature
 * that does NOT use AI/LLM — it relies entirely on MinHash/LSH deduplication metrics.
 *
 * <p>
 * The scheduler periodically:
 * <ol>
 *   <li>Queries all APIs with DEPRECATED lifecycle state</li>
 *   <li>For each deprecated API, uses {@link GenericService#checkForDuplicates} with a
 *       lower similarity threshold (default 0.6) to find structurally similar APIs</li>
 *   <li>Filters results to only include non-deprecated (Published/Created) APIs as potential successors</li>
 *   <li>Stores successor suggestions in the AM_DEPRECATION_GUIDE table</li>
 * </ol>
 *
 * Configuration is read from default.json via {@link APIMGovernanceConfigDTO}:
 * <ul>
 *   <li>{@code apim.governance.deprecation_guide.enabled} — master toggle (default: false)</li>
 *   <li>{@code apim.governance.deprecation_guide.scan_interval_minutes} — cron interval (default: 60)</li>
 *   <li>{@code apim.governance.deprecation_guide.successor_similarity_threshold} — min similarity (default: 0.6)</li>
 * </ul>
 */
public class DeprecationGuideScheduler {

    private static final Log log = LogFactory.getLog(DeprecationGuideScheduler.class);

    private static ScheduledExecutorService scheduler;
    private static boolean enabled = false;
    private static int scanIntervalMinutes = 60;
    private static double successorSimilarityThreshold = 0.6;

    // SQL constants
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS AM_DEPRECATION_GUIDE (" +
                    "DEPRECATED_API_UUID VARCHAR(36) NOT NULL, " +
                    "SUCCESSOR_API_UUID VARCHAR(36) NOT NULL, " +
                    "SIMILARITY DOUBLE NOT NULL, " +
                    "SUCCESSOR_API_NAME VARCHAR(256), " +
                    "SUCCESSOR_API_VERSION VARCHAR(30), " +
                    "ORGANIZATION VARCHAR(128) NOT NULL, " +
                    "CREATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "UPDATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (DEPRECATED_API_UUID, SUCCESSOR_API_UUID, ORGANIZATION))";

    private static final String UPSERT_SQL =
            "INSERT INTO AM_DEPRECATION_GUIDE " +
                    "(DEPRECATED_API_UUID, SUCCESSOR_API_UUID, SIMILARITY, SUCCESSOR_API_NAME, " +
                    "SUCCESSOR_API_VERSION, ORGANIZATION, CREATED_TIME, UPDATED_TIME) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "SIMILARITY = VALUES(SIMILARITY), " +
                    "SUCCESSOR_API_NAME = VALUES(SUCCESSOR_API_NAME), " +
                    "SUCCESSOR_API_VERSION = VALUES(SUCCESSOR_API_VERSION), " +
                    "UPDATED_TIME = VALUES(UPDATED_TIME)";

    private static final String DELETE_STALE_SQL =
            "DELETE FROM AM_DEPRECATION_GUIDE WHERE DEPRECATED_API_UUID = ? AND ORGANIZATION = ?";

    private static final String SELECT_SUCCESSORS_SQL =
            "SELECT SUCCESSOR_API_UUID, SIMILARITY, SUCCESSOR_API_NAME, SUCCESSOR_API_VERSION " +
                    "FROM AM_DEPRECATION_GUIDE " +
                    "WHERE DEPRECATED_API_UUID = ? AND ORGANIZATION = ? " +
                    "ORDER BY SIMILARITY DESC";

    /**
     * Initialize the Deprecation Guide scheduler.
     * Reads configuration from default.json and starts periodic scanning if enabled.
     */
    public static void initialize() {
        log.info("[DEPRECATION-GUIDE] Initializing Deprecation Guide Scheduler...");

        try {
            APIManagerConfiguration config = GenericServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration();

            if (config == null) {
                log.warn("[DEPRECATION-GUIDE] APIManagerConfiguration not available. " +
                        "Deprecation Guide disabled.");
                return;
            }

            APIMGovernanceConfigDTO govConfig = config.getAPIMGovernanceConfigurationDto();
            if (govConfig == null) {
                log.warn("[DEPRECATION-GUIDE] Governance config DTO not available. " +
                        "Deprecation Guide disabled.");
                return;
            }

            enabled = govConfig.isDeprecationGuideEnabled();
            scanIntervalMinutes = govConfig.getDeprecationGuideScanIntervalMinutes();
            successorSimilarityThreshold = govConfig.getDeprecationGuideSuccessorSimilarityThreshold();

            log.info("[DEPRECATION-GUIDE] Configuration loaded — enabled: " + enabled +
                    ", interval: " + scanIntervalMinutes + " min" +
                    ", threshold: " + String.format("%.2f", successorSimilarityThreshold));

            if (!enabled) {
                log.info("[DEPRECATION-GUIDE] Deprecation Guide is disabled. Skipping scheduler start.");
                return;
            }

            // Ensure the database table exists
            ensureTableExists();

            // Start the scheduler
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "DeprecationGuideScheduler");
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleAtFixedRate(
                    DeprecationGuideScheduler::scanForSuccessors,
                    5, // initial delay — let other services start first
                    scanIntervalMinutes,
                    TimeUnit.MINUTES);

            log.info("[DEPRECATION-GUIDE] Scheduler started. Will scan every " +
                    scanIntervalMinutes + " minutes.");

        } catch (RuntimeException e) {
            log.error("[DEPRECATION-GUIDE] Failed to initialize Deprecation Guide Scheduler", e);
        }
    }

    /**
     * Shutdown the scheduler gracefully.
     */
    public static void shutdown() {
        if (scheduler != null) {
            log.info("[DEPRECATION-GUIDE] Shutting down Deprecation Guide Scheduler...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("[DEPRECATION-GUIDE] Forcing shutdown...");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("[DEPRECATION-GUIDE] Shutdown interrupted", e);
                Thread.currentThread().interrupt();
            }
            log.info("[DEPRECATION-GUIDE] Scheduler shut down.");
        }
    }

    /**
     * Main scan method — runs periodically to find successor suggestions for deprecated APIs.
     */
    private static void scanForSuccessors() {
        if (!enabled) {
            return;
        }

        log.info("[DEPRECATION-GUIDE] Starting deprecation guide scan...");
        long startTime = System.currentTimeMillis();
        int totalDeprecated = 0;
        int totalSuccessorsFound = 0;

        try {
            GenericService genericService = GenericService.getInstance();
            if (!genericService.isInitialized()) {
                log.warn("[DEPRECATION-GUIDE] GenericService not initialized yet. Skipping scan.");
                return;
            }

            List<String> organizations = getOrganizations();

            for (String organization : organizations) {
                try {
                    // Get all APIs for this organization
                    List<ApiResult> allApis = ApiMgtDAO.getInstance().getAllAPIs(organization);
                    String adminUsername = getAdminUsername(organization);
                    APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);

                    if (apiProvider == null) {
                        log.warn("[DEPRECATION-GUIDE] Could not get APIProvider for org: " + organization);
                        continue;
                    }

                    // Find deprecated APIs
                    List<String> deprecatedApiUuids = new ArrayList<>();
                    for (ApiResult apiResult : allApis) {
                        try {
                            API api = apiProvider.getAPIbyUUID(apiResult.getId(), organization);
                            if (api != null && APIStatus.DEPRECATED.getStatus()
                                    .equalsIgnoreCase(api.getStatus())) {
                                deprecatedApiUuids.add(apiResult.getId());
                            }
                        } catch (APIManagementException | RuntimeException e) {
                            log.debug("[DEPRECATION-GUIDE] Could not check status of API "
                                    + apiResult.getId() + ": " + e.getMessage());
                        }
                    }

                    totalDeprecated += deprecatedApiUuids.size();

                    if (deprecatedApiUuids.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("[DEPRECATION-GUIDE] No deprecated APIs in org: " + organization);
                        }
                        continue;
                    }

                    log.info("[DEPRECATION-GUIDE] Found " + deprecatedApiUuids.size() +
                            " deprecated APIs in org: " + organization);

                    // Process each deprecated API
                    for (String deprecatedUuid : deprecatedApiUuids) {
                        int found = processDeprecatedApi(
                                deprecatedUuid, organization, apiProvider, genericService);
                        totalSuccessorsFound += found;
                    }

                } catch (APIManagementException e) {
                    log.error("[DEPRECATION-GUIDE] Error processing org " + organization, e);
                }
            }

        } catch (RuntimeException e) {
            log.error("[DEPRECATION-GUIDE] Scan failed with unexpected error", e);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[DEPRECATION-GUIDE] Scan completed in " + elapsed + "ms. " +
                "Deprecated APIs: " + totalDeprecated +
                ", Successor suggestions found: " + totalSuccessorsFound);
    }

    /**
     * Process a single deprecated API: find similar non-deprecated APIs as successor candidates.
     *
     * @param deprecatedUuid   UUID of the deprecated API
     * @param organization     The organization
     * @param apiProvider      APIProvider instance for this organization
     * @param genericService GenericService instance
     * @return Number of successor suggestions found
     */
    private static int processDeprecatedApi(String deprecatedUuid, String organization,
                                             APIProvider apiProvider,
                                             GenericService genericService) {
        try {
            // Get the API definition for the deprecated API (package-private access)
            String apiDefinition = genericService.getAPIDefinition(deprecatedUuid, organization);

            if (apiDefinition == null || apiDefinition.isEmpty()) {
                log.debug("[DEPRECATION-GUIDE] No API definition for deprecated API " + deprecatedUuid);
                return 0;
            }

            // Use GenericService to find structurally similar APIs with lower threshold
            // skipFirstInRule=true: deprecated API is typically the original/older one,
            // and we want to find newer APIs as successors
            DeduplicationResult result = genericService.checkForDuplicates(
                    apiDefinition, deprecatedUuid, organization, successorSimilarityThreshold, true);

            if (!result.isDuplicate() || result.getConflictReports() == null
                    || result.getConflictReports().isEmpty()) {
                // No similar APIs found — clear any existing stale suggestions
                clearSuggestionsForApi(deprecatedUuid, organization);
                return 0;
            }

            // Filter to only non-deprecated APIs (Published, Created) as potential successors
            List<SuccessorSuggestion> suggestions = new ArrayList<>();

            for (ConflictReport conflict : result.getConflictReports()) {
                String matchedUuid = conflict.getMatchedApiUuid();
                if (matchedUuid == null || matchedUuid.equals(deprecatedUuid)) {
                    continue;
                }

                try {
                    API matchedApi = apiProvider.getAPIbyUUID(matchedUuid, organization);
                    if (matchedApi == null) {
                        continue;
                    }

                    String status = matchedApi.getStatus();
                    // Only PUBLISHED APIs qualify as valid successors to ensure
                    // service continuity for API consumers
                    if (!APIStatus.PUBLISHED.getStatus().equalsIgnoreCase(status)) {
                        log.info(String.format("[DEPRECATION-GUIDE] Skipping %s (status: %s) "
                                        + "— only PUBLISHED APIs qualify as successors",
                                matchedUuid, status));
                        continue;
                    }

                    String apiName = matchedApi.getId() != null ?
                            matchedApi.getId().getApiName() : "Unknown";
                    String apiVersion = matchedApi.getId() != null ?
                            matchedApi.getId().getVersion() : "Unknown";

                    suggestions.add(new SuccessorSuggestion(
                            matchedUuid, conflict.getSimilarityScore(), apiName, apiVersion));

                    log.info(String.format("[DEPRECATION-GUIDE] Successor candidate for %s: " +
                                    "%s v%s (UUID: %s, similarity: %.2f%%)",
                            deprecatedUuid, apiName, apiVersion, matchedUuid,
                            conflict.getSimilarityScore() * 100));

                } catch (APIManagementException | RuntimeException e) {
                    log.debug("[DEPRECATION-GUIDE] Could not resolve matched API " + matchedUuid
                            + ": " + e.getMessage());
                }
            }

            // Persist suggestions (replaces any stale data)
            if (!suggestions.isEmpty()) {
                persistSuggestions(deprecatedUuid, organization, suggestions);
            } else {
                clearSuggestionsForApi(deprecatedUuid, organization);
            }

            return suggestions.size();

        } catch (APIMGovernanceException | RuntimeException e) {
            log.warn("[DEPRECATION-GUIDE] Error processing deprecated API " + deprecatedUuid
                    + ": " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Stack trace:", e);
            }
            return 0;
        }
    }

    /**
     * Persist successor suggestions to the AM_DEPRECATION_GUIDE table.
     */
    private static void persistSuggestions(String deprecatedUuid, String organization,
                                            List<SuccessorSuggestion> suggestions) {
        Connection connection = null;
        PreparedStatement deleteStmt = null;
        PreparedStatement upsertStmt = null;

        try {
            connection = org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            // Clear existing suggestions for this deprecated API
            deleteStmt = connection.prepareStatement(DELETE_STALE_SQL);
            deleteStmt.setString(1, deprecatedUuid);
            deleteStmt.setString(2, organization);
            deleteStmt.executeUpdate();

            // Insert new suggestions
            Timestamp now = new Timestamp(System.currentTimeMillis());
            upsertStmt = connection.prepareStatement(UPSERT_SQL);

            for (SuccessorSuggestion suggestion : suggestions) {
                upsertStmt.setString(1, deprecatedUuid);
                upsertStmt.setString(2, suggestion.apiUuid);
                upsertStmt.setDouble(3, suggestion.similarity);
                upsertStmt.setString(4, suggestion.apiName);
                upsertStmt.setString(5, suggestion.apiVersion);
                upsertStmt.setString(6, organization);
                upsertStmt.setTimestamp(7, now);
                upsertStmt.setTimestamp(8, now);
                upsertStmt.addBatch();
            }

            upsertStmt.executeBatch();
            connection.commit();

            log.info("[DEPRECATION-GUIDE] Persisted " + suggestions.size() +
                    " successor suggestions for deprecated API " + deprecatedUuid);

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("[DEPRECATION-GUIDE] Error rolling back", rollbackEx);
                }
            }
            log.error("[DEPRECATION-GUIDE] Error persisting suggestions for " + deprecatedUuid, e);
        } finally {
            closeQuietly(upsertStmt);
            closeQuietly(deleteStmt);
            closeQuietly(connection);
        }
    }

    /**
     * Clear all successor suggestions for a given deprecated API.
     */
    private static void clearSuggestionsForApi(String deprecatedUuid, String organization) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil.getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(DELETE_STALE_SQL);
            stmt.setString(1, deprecatedUuid);
            stmt.setString(2, organization);
            int deleted = stmt.executeUpdate();
            connection.commit();

            if (deleted > 0) {
                log.debug("[DEPRECATION-GUIDE] Cleared " + deleted +
                        " stale suggestions for " + deprecatedUuid);
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.debug("Error rolling back: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            log.warn("[DEPRECATION-GUIDE] Error clearing suggestions for " + deprecatedUuid, e);
        } finally {
            closeQuietly(stmt);
            closeQuietly(connection);
        }
    }

    /**
     * Get successor suggestions for a deprecated API.
     * This can be called by REST API endpoints or the publisher portal backend.
     *
     * @param deprecatedApiUuid UUID of the deprecated API
     * @param organization      Organization
     * @return List of successor suggestions, ordered by similarity descending
     */
    public static List<SuccessorSuggestion> getSuccessorSuggestions(String deprecatedApiUuid,
                                                                     String organization) {
        List<SuccessorSuggestion> suggestions = new ArrayList<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil.getConnection();
            stmt = connection.prepareStatement(SELECT_SUCCESSORS_SQL);
            stmt.setString(1, deprecatedApiUuid);
            stmt.setString(2, organization);
            rs = stmt.executeQuery();

            while (rs.next()) {
                suggestions.add(new SuccessorSuggestion(
                        rs.getString("SUCCESSOR_API_UUID"),
                        rs.getDouble("SIMILARITY"),
                        rs.getString("SUCCESSOR_API_NAME"),
                        rs.getString("SUCCESSOR_API_VERSION")));
            }
        } catch (SQLException e) {
            log.error("[DEPRECATION-GUIDE] Error querying successor suggestions", e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            closeQuietly(connection);
        }

        return suggestions;
    }

    /**
     * Ensure the AM_DEPRECATION_GUIDE table exists.
     */
    private static void ensureTableExists() {
        Connection connection = null;
        Statement stmt = null;

        try {
            connection = org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil.getConnection();
            connection.setAutoCommit(false);
            stmt = connection.createStatement();
            stmt.execute(CREATE_TABLE_SQL);

            // Create index — ignore if already exists (MySQL error 1061)
            try {
                stmt.execute("CREATE INDEX IDX_DEPRECATION_GUIDE_ORG " +
                        "ON AM_DEPRECATION_GUIDE(ORGANIZATION)");
            } catch (SQLException indexEx) {
                if (!indexEx.getMessage().contains("Duplicate") &&
                        !indexEx.getMessage().contains("already exists")) {
                    log.warn("[DEPRECATION-GUIDE] Could not create index: " + indexEx.getMessage());
                }
            }

            connection.commit();
            log.info("[DEPRECATION-GUIDE] Database table AM_DEPRECATION_GUIDE verified/created.");

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.debug("Error rolling back: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            log.error("[DEPRECATION-GUIDE] Error ensuring table exists", e);
        } finally {
            closeQuietly(stmt);
            closeQuietly(connection);
        }
    }

    /**
     * Get all organizations. Currently returns super tenant only.
     * Can be extended for multi-tenancy support.
     */
    private static List<String> getOrganizations() {
        List<String> organizations = new ArrayList<>();
        organizations.add("carbon.super");
        return organizations;
    }

    /**
     * Get the admin username for an organization.
     */
    private static String getAdminUsername(String organization) {
        if (organization == null || "carbon.super".equals(organization)) {
            return "admin";
        }
        return "admin@" + organization;
    }

    // --- Resource cleanup helpers ---

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.debug("Error closing resource: " + e.getMessage(), e);
            }
        }
    }

    /**
     * DTO for a successor suggestion.
     */
    public static class SuccessorSuggestion {
        private final String apiUuid;
        private final double similarity;
        private final String apiName;
        private final String apiVersion;

        public SuccessorSuggestion(String apiUuid, double similarity,
                                    String apiName, String apiVersion) {
            this.apiUuid = apiUuid;
            this.similarity = similarity;
            this.apiName = apiName;
            this.apiVersion = apiVersion;
        }

        public String getApiUuid() {
            return apiUuid;
        }

        public double getSimilarity() {
            return similarity;
        }

        public String getApiName() {
            return apiName;
        }

        public String getApiVersion() {
            return apiVersion;
        }

        @Override
        public String toString() {
            return String.format("%s v%s (UUID: %s, similarity: %.2f%%)",
                    apiName, apiVersion, apiUuid, similarity * 100);
        }
    }
}
