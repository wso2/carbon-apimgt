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
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.gatekeeper.GatekeeperConstants;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.MinHashDAO;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.impl.MinHashDAOImpl;
import org.wso2.carbon.apimgt.governance.gatekeeper.lsh.LSHIndex;
import org.wso2.carbon.apimgt.governance.gatekeeper.minhash.MinHashGenerator;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.APISignature;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.ConflictReport;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationResult;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main service class for API deduplication using MinHash and LSH.
 * Provides methods to check for duplicates, add/remove APIs from the index,
 * and manage signature persistence.
 */
public class GatekeeperService {

    private static final Log log = LogFactory.getLog(GatekeeperService.class);

    private static volatile GatekeeperService instance;

    private final LSHIndex lshIndex;
    private final MinHashGenerator minHashGenerator;
    private final SignatureService signatureService;
    private final MinHashDAO minHashDAO;

    private boolean initialized = false;

    /**
     * Private constructor for singleton.
     */
    private GatekeeperService() {
        this.minHashGenerator = new MinHashGenerator();
        this.lshIndex = new LSHIndex();
        this.signatureService = new SignatureService();
        this.minHashDAO = MinHashDAOImpl.getInstance();
    }

    /**
     * Gets the singleton instance.
     *
     * @return GatekeeperService instance
     */
    public static GatekeeperService getInstance() {
        if (instance == null) {
            synchronized (GatekeeperService.class) {
                if (instance == null) {
                    instance = new GatekeeperService();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the service by hydrating the LSH index from the database.
     * Should be called during server startup.
     *
     * @throws APIMGovernanceException If hydration fails
     */
    public synchronized void initialize() throws APIMGovernanceException {
        if (initialized) {
            log.info("GatekeeperService already initialized");
            return;
        }

        log.info("Initializing GatekeeperService - ensuring database tables exist");
        ensureTablesExist();

        log.info("Initializing GatekeeperService - hydrating LSH index from database");
        hydrateIndex();
        initialized = true;
        log.info("GatekeeperService initialization complete. LSH index contains " + lshIndex.size() + " APIs");
    }

    /**
     * Ensures the required database tables exist.
     * Creates them if they don't exist (H2-compatible CREATE TABLE IF NOT EXISTS).
     */
    private void ensureTablesExist() {
        java.sql.Connection connection = null;
        java.sql.Statement stmt = null;
        try {
            connection = org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil.getConnection();
            connection.setAutoCommit(false);
            stmt = connection.createStatement();

            // Create AM_API_MINHASH table if not exists
            stmt.execute("CREATE TABLE IF NOT EXISTS AM_API_MINHASH (" +
                    "API_UUID VARCHAR(36) NOT NULL, " +
                    "SIGNATURE_BLOB BLOB NOT NULL, " +
                    "ORGANIZATION VARCHAR(128) NOT NULL, " +
                    "CREATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "UPDATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (API_UUID, ORGANIZATION))");

            // MySQL does not support CREATE INDEX IF NOT EXISTS. Use a try-catch to silently
            // handle the case where the index already exists (error code 1061 = Duplicate key name).
            try {
                stmt.execute("CREATE INDEX IDX_AM_API_MINHASH_ORG ON AM_API_MINHASH(ORGANIZATION)");
            } catch (java.sql.SQLException indexEx) {
                // 1061 = ER_DUP_KEYNAME (MySQL), or similar for other DBs — index already exists
                if (!indexEx.getMessage().contains("Duplicate") && !indexEx.getMessage().contains("already exists")) {
                    log.warn("Could not create index IDX_AM_API_MINHASH_ORG: " + indexEx.getMessage());
                }
            }

            connection.commit();
            log.info("Gatekeeper database tables verified/created successfully");

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (java.sql.SQLException rollbackEx) {
                    log.error("Error rolling back table creation", rollbackEx);
                }
            }
            log.error("Error ensuring Gatekeeper tables exist. " +
                    "Deduplication signatures may not persist across restarts.", e);
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (java.sql.SQLException e) { /* ignore */ }
            }
            if (connection != null) {
                try { connection.close(); } catch (java.sql.SQLException e) { /* ignore */ }
            }
        }
    }

    /**
     * Hydrates the LSH index from the database.
     *
     * @throws APIMGovernanceException If database read fails
     */
    private void hydrateIndex() throws APIMGovernanceException {
        List<APISignature> signatures = minHashDAO.getAllSignatures();

        for (APISignature signature : signatures) {
            try {
                int[] signatureArray = MinHashGenerator.bytesToSignature(signature.getSignatureBlob());
                lshIndex.addSignature(signature.getApiUuid(), signatureArray, signature.getOrganization());
            } catch (Exception e) {
                log.warn("Failed to load signature for API: " + signature.getApiUuid(), e);
            }
        }

        log.info("Hydrated LSH index with " + signatures.size() + " signatures");
    }

    /**
     * Checks if an API is a potential duplicate of existing APIs.
     *
     * @param apiDefinition The API definition to check
     * @param apiUuid       The UUID of the API being checked
     * @param organization  The organization
     * @param threshold     Similarity threshold (0.0 to 1.0)
     * @return DeduplicationResult with conflict reports if duplicates found
     * @throws APIMGovernanceException If check fails
     */
    public DeduplicationResult checkForDuplicates(String apiDefinition, String apiUuid,
                                                   String organization, double threshold)
            throws APIMGovernanceException {

        // Validate threshold
        if (threshold < GatekeeperConstants.MIN_SIMILARITY_THRESHOLD
                || threshold > GatekeeperConstants.MAX_SIMILARITY_THRESHOLD) {
            log.info("Invalid threshold " + threshold + ", using default: "
                    + GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD);
            threshold = GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD;
        }

        log.info("Checking for duplicates for API " + apiUuid + " with threshold: "
                + String.format("%.2f", threshold) + " (%.0f%%) in organization: " + organization);

        // Generate signature for the query API
        SignatureService.APISignatureDTO signatureDTO =
                signatureService.generateSignature(apiDefinition, apiUuid, organization);

        int[] querySignature = signatureDTO.getSignatureArray();

        if (log.isDebugEnabled()) {
            log.debug("Generated signature with " + signatureDTO.getFeatureCount() + " features, "
                    + signatureDTO.getShingleCount() + " shingles for API: " + apiUuid);
        }

        // Find similar APIs using LSH
        List<LSHIndex.SimilarityResult> similarApis =
                lshIndex.findSimilar(querySignature, organization, threshold, minHashGenerator);

        // Filter out the API itself if it's already in the index
        similarApis.removeIf(r -> r.getApiUuid().equals(apiUuid));

        // Log individual similarity scores for debugging
        if (!similarApis.isEmpty()) {
            for (LSHIndex.SimilarityResult sr : similarApis) {
                log.info(String.format("Similarity match: API %s -> %s = %.4f (%.2f%%)",
                        apiUuid, sr.getApiUuid(), sr.getSimilarity(), sr.getSimilarity() * 100));
            }
        }

        if (similarApis.isEmpty()) {
            DeduplicationResult result = DeduplicationResult.unique(apiUuid, organization);
            result.setThreshold(threshold);
            return result;
        }

        // Build conflict reports using "First-In" rule:
        // Only report conflicts where the matched API was created BEFORE the query API.
        // The older (original) API remains compliant; only the newer API gets violations.
        List<ConflictReport> conflictReports = new ArrayList<>();
        boolean highConfidence = false;

        // Read high confidence threshold from config instead of using hardcoded value
        double highConfidenceThreshold = 0.95;
        try {
            DeduplicationConfigService.DeduplicationConfig conf =
                    DeduplicationConfigService.getInstance().getConfig(organization);
            highConfidenceThreshold = conf.getHighConfidenceThreshold();
        } catch (Exception e) {
            log.debug("Could not read high confidence threshold from config, using default: 0.95");
        }

        // Resolve the creation time of the query API for First-In comparison
        String queryApiCreatedTime = null;
        try {
            String adminUsername = getAdminUsername(organization);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);
            if (apiProvider != null) {
                API queryApi = apiProvider.getAPIbyUUID(apiUuid, organization);
                if (queryApi != null) {
                    queryApiCreatedTime = queryApi.getCreatedTime();
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve creation time for query API " + apiUuid + ": " + e.getMessage());
        }

        for (LSHIndex.SimilarityResult similar : similarApis) {
            double similarity = similar.getSimilarity();

            // Resolve matched API name, version, and creation time from UUID
            String matchedApiName = null;
            String matchedApiVersion = null;
            String matchedApiCreatedTime = null;
            try {
                String adminUsername = getAdminUsername(organization);
                APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);
                if (apiProvider != null) {
                    API matchedApi = apiProvider.getAPIbyUUID(similar.getApiUuid(), organization);
                    if (matchedApi != null && matchedApi.getId() != null) {
                        matchedApiName = matchedApi.getId().getApiName();
                        matchedApiVersion = matchedApi.getId().getVersion();
                        matchedApiCreatedTime = matchedApi.getCreatedTime();
                    }
                }
            } catch (Exception e) {
                log.debug("Could not resolve API name for UUID " + similar.getApiUuid()
                        + ": " + e.getMessage());
            }

            // First-In rule: Only report this conflict if the matched API is OLDER than the query API.
            // If the query API is older (or same age), skip — the query API is the "original" and
            // should not receive violations. The newer API (matched) will get its own violations
            // when it is evaluated separately.
            if (queryApiCreatedTime != null && matchedApiCreatedTime != null) {
                int timeComparison = queryApiCreatedTime.compareTo(matchedApiCreatedTime);
                if (timeComparison < 0) {
                    // Query API was created BEFORE the matched API → query is the original, skip
                    log.info(String.format("First-In rule: Skipping match %s -> %s (query created at %s is older " +
                                    "than match created at %s)",
                            apiUuid, similar.getApiUuid(), queryApiCreatedTime, matchedApiCreatedTime));
                    continue;
                } else if (timeComparison == 0) {
                    // Same creation time — use UUID lexicographic order as tiebreaker.
                    // The "smaller" UUID is considered the original.
                    if (apiUuid.compareTo(similar.getApiUuid()) < 0) {
                        log.info(String.format("First-In rule: Skipping match %s -> %s (same creation time, " +
                                        "query UUID is lexicographically smaller)",
                                apiUuid, similar.getApiUuid()));
                        continue;
                    }
                }
            }

            if (similarity >= highConfidenceThreshold) {
                highConfidence = true;
            }

            String displayName = matchedApiName != null ? matchedApiName : similar.getApiUuid();

            ConflictReport report = new ConflictReport.Builder()
                    .matchedApiUuid(similar.getApiUuid())
                    .matchedApiName(matchedApiName)
                    .matchedApiVersion(matchedApiVersion)
                    .similarityScore(similarity)
                    .message(String.format("API '%s' has %.1f%% similarity",
                            displayName, similarity * 100))
                    .recommendation(similarity >= highConfidenceThreshold
                            ? "STRONGLY RECOMMENDED: Consider reusing '" + displayName
                              + "' or creating a new version instead"
                            : "Review '" + displayName
                              + "' to ensure this is not a duplicate")
                    .build();

            conflictReports.add(report);
        }

        // If all conflicts were skipped by the First-In rule, the query API is the original
        if (conflictReports.isEmpty()) {
            log.info("First-In rule: All matches skipped for API " + apiUuid
                    + " (this API is the original/oldest). Marking as unique.");
            DeduplicationResult result = DeduplicationResult.unique(apiUuid, organization);
            result.setThreshold(threshold);
            return result;
        }

        DeduplicationResult result = DeduplicationResult.duplicate(
                apiUuid, organization, conflictReports, highConfidence);
        result.setThreshold(threshold);

        return result;
    }

    /**
     * Adds an API to the deduplication index.
     *
     * @param apiDefinition The API definition
     * @param apiUuid       The API UUID
     * @param organization  The organization
     * @param persist       Whether to persist to database
     * @throws APIMGovernanceException If adding fails
     */
    public void addApiToIndex(String apiDefinition, String apiUuid, String organization, boolean persist)
            throws APIMGovernanceException {

        SignatureService.APISignatureDTO signatureDTO =
                signatureService.generateSignature(apiDefinition, apiUuid, organization);

        // Add to in-memory LSH index
        lshIndex.addSignature(apiUuid, signatureDTO.getSignatureArray(), organization);

        // Persist to database if requested
        if (persist) {
            APISignature apiSignature = new APISignature(
                    apiUuid, signatureDTO.getSignatureBlob(), organization);
            minHashDAO.upsertSignature(apiSignature);
        }

        if (log.isDebugEnabled()) {
            log.debug("Added API " + apiUuid + " to deduplication index");
        }
    }

    /**
     * Removes an API from the deduplication index.
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @param persist      Whether to remove from database
     * @throws APIMGovernanceException If removal fails
     */
    public void removeApiFromIndex(String apiUuid, String organization, boolean persist)
            throws APIMGovernanceException {

        // Remove from in-memory LSH index
        lshIndex.removeSignature(apiUuid);

        // Remove from database if requested
        if (persist) {
            minHashDAO.deleteSignature(apiUuid, organization);
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed API " + apiUuid + " from deduplication index");
        }
    }

    /**
     * Updates an API's signature in the index.
     *
     * @param apiDefinition The updated API definition
     * @param apiUuid       The API UUID
     * @param organization  The organization
     * @throws APIMGovernanceException If update fails
     */
    public void updateApiInIndex(String apiDefinition, String apiUuid, String organization)
            throws APIMGovernanceException {

        // Remove old signature
        lshIndex.removeSignature(apiUuid);

        // Generate new signature and add
        SignatureService.APISignatureDTO signatureDTO =
                signatureService.generateSignature(apiDefinition, apiUuid, organization);

        lshIndex.addSignature(apiUuid, signatureDTO.getSignatureArray(), organization);

        // Update in database
        APISignature apiSignature = new APISignature(
                apiUuid, signatureDTO.getSignatureBlob(), organization);
        minHashDAO.upsertSignature(apiSignature);

        if (log.isDebugEnabled()) {
            log.debug("Updated API " + apiUuid + " in deduplication index");
        }
    }

    /**
     * Checks if an API exists in the index.
     *
     * @param apiUuid The API UUID
     * @return True if the API is in the index
     */
    public boolean isApiInIndex(String apiUuid) {
        return lshIndex.containsApi(apiUuid);
    }

    /**
     * Gets the current size of the LSH index.
     *
     * @return Number of APIs in the index
     */
    public int getIndexSize() {
        return lshIndex.size();
    }

    /**
     * Checks if the service is initialized.
     *
     * @return True if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the LSH index (for testing purposes).
     *
     * @return LSHIndex
     */
    public LSHIndex getLshIndex() {
        return lshIndex;
    }

    /**
     * Gets the signature service.
     *
     * @return SignatureService
     */
    public SignatureService getSignatureService() {
        return signatureService;
    }

    /**
     * Indexes an API by generating and storing its MinHash signature.
     * This is a convenience method for the API event listener.
     *
     * @param apiId         The API UUID
     * @param apiDefinition The OpenAPI definition
     * @param organization  The organization
     * @throws APIMGovernanceException If indexing fails
     */
    public void indexAPI(String apiId, String apiDefinition, String organization) 
            throws APIMGovernanceException {
        addApiToIndex(apiDefinition, apiId, organization, true);
    }

    /**
     * Removes an API from the deduplication index.
     * This is a convenience method for the API event listener.
     *
     * @param apiId        The API UUID
     * @param organization The organization
     * @throws APIMGovernanceException If removal fails
     */
    public void removeAPI(String apiId, String organization) throws APIMGovernanceException {
        removeApiFromIndex(apiId, organization, true);
    }

    /**
     * Checks if an API has potential duplicates.
     * This is a convenience method for the API event listener.
     *
     * @param apiId         The API UUID
     * @param apiDefinition The OpenAPI definition
     * @param organization  The organization
     * @return DuplicateCheckResult with information about similar APIs
     * @throws APIMGovernanceException If check fails
     */
    public org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult checkForDuplicates(
            String apiId, String apiDefinition, String organization) throws APIMGovernanceException {
        
        // Get threshold from deduplication ruleset configuration
        DeduplicationConfigService.DeduplicationConfig config = 
                DeduplicationConfigService.getInstance().getConfig(organization);
        
        // Check if deduplication is enabled in config
        if (!config.isEnabled()) {
            log.info("Deduplication is disabled via config for organization: " + organization 
                    + ". Skipping check for API " + apiId);
            org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult skipResult = 
                    new org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult();
            skipResult.setApiId(apiId);
            skipResult.setHasDuplicates(false);
            skipResult.setThreshold(config.getSimilarityThreshold());
            return skipResult;
        }

        double threshold = config.getSimilarityThreshold();
        
        // Always log the threshold being used (INFO level for visibility)
        log.info("Deduplication check for API " + apiId + " using threshold: " 
                + String.format("%.2f", threshold * 100) + "% (org: " + organization 
                + ", mode: " + config.getMode() + ")");
        
        if (log.isDebugEnabled()) {
            log.debug("Full config - enabled: " + config.isEnabled() + ", mode: " + config.getMode()
                    + ", numHashFunctions: " + config.getNumHashFunctions() 
                    + ", numBands: " + config.getNumBands());
        }
        
        DeduplicationResult result = checkForDuplicates(
                apiDefinition, apiId, organization, threshold);
        
        // Convert to DuplicateCheckResult
        org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult checkResult = 
                new org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult();
        
        checkResult.setApiId(apiId);
        checkResult.setHasDuplicates(result.isDuplicate());
        checkResult.setThreshold(threshold);
        
        if (result.isDuplicate() && result.getConflictReports() != null) {
            List<org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult.SimilarAPI> similarApis 
                    = new ArrayList<>();
            
            for (ConflictReport report : result.getConflictReports()) {
                org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult.SimilarAPI similar = 
                        new org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult.SimilarAPI(
                                report.getMatchedApiUuid(),
                                report.getMatchedApiName(),
                                report.getSimilarityScore());
                similarApis.add(similar);
            }
            checkResult.setSimilarAPIs(similarApis);
        }
        
        return checkResult;
    }

    /**
     * Indexes all existing APIs that are not yet in the AM_API_MINHASH table.
     * This method should be called during server startup to ensure all pre-existing
     * APIs are indexed for deduplication checks.
     *
     * @return Number of APIs indexed
     */
    public int indexExistingAPIs() {
        log.info("Starting to index existing APIs for deduplication...");
        
        int indexedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        
        try {
            // Get all existing API signatures from database
            Set<String> existingSignatureApiIds = new HashSet<>();
            List<APISignature> existingSignatures = minHashDAO.getAllSignatures();
            for (APISignature sig : existingSignatures) {
                existingSignatureApiIds.add(sig.getApiUuid());
            }
            log.info("Found " + existingSignatureApiIds.size() + " APIs already indexed in AM_API_MINHASH");

            // Get all organizations
            List<String> organizations = getOrganizations();
            
            for (String organization : organizations) {
                try {
                    // Get all APIs for this organization
                    List<ApiResult> apis = ApiMgtDAO.getInstance().getAllAPIs(organization);
                    log.info("Found " + apis.size() + " APIs in organization: " + organization);
                    
                    for (ApiResult apiResult : apis) {
                        String apiId = apiResult.getId();
                        
                        // Skip if already indexed
                        if (existingSignatureApiIds.contains(apiId)) {
                            skippedCount++;
                            continue;
                        }
                        
                        try {
                            // Get API definition
                            String apiDefinition = getAPIDefinition(apiId, organization);
                            
                            if (apiDefinition != null && !apiDefinition.isEmpty()) {
                                // Index the API
                                addApiToIndex(apiDefinition, apiId, organization, true);
                                indexedCount++;
                                
                                if (log.isDebugEnabled()) {
                                    log.debug("Indexed existing API: " + apiId + " in organization: " + organization);
                                }
                            } else {
                                log.warn("API definition not found for API: " + apiId + ". Skipping indexing.");
                                skippedCount++;
                            }
                        } catch (Exception e) {
                            log.warn("Failed to index API " + apiId + ": " + e.getMessage());
                            errorCount++;
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing organization " + organization + ": " + e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error indexing existing APIs: " + e.getMessage(), e);
        }
        
        log.info("Finished indexing existing APIs. Indexed: " + indexedCount + 
                ", Skipped: " + skippedCount + ", Errors: " + errorCount);
        
        return indexedCount;
    }

    /**
     * Gets all organizations in the system.
     * Currently only returns super tenant, can be extended for multi-tenancy.
     * 
     * @return List of organization names
     */
    private List<String> getOrganizations() {
        List<String> organizations = new ArrayList<>();
        // For now, just index super tenant APIs
        // Can be extended to support multi-tenancy
        organizations.add("carbon.super");
        return organizations;
    }

    /**
     * Gets the API definition for an API using the APIProvider.
     * 
     * @param apiId The API UUID
     * @param organization The organization
     * @return The API definition (OpenAPI spec) or null if not found
     */
    private String getAPIDefinition(String apiId, String organization) {
        try {
            String adminUsername = getAdminUsername(organization);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);
            
            if (apiProvider != null) {
                API api = apiProvider.getAPIbyUUID(apiId, organization);
                if (api != null) {
                    String swagger = apiProvider.getOpenAPIDefinition(api.getUuid(), organization);
                    if (swagger != null && !swagger.isEmpty()) {
                        return swagger;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not get API definition for " + apiId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets the admin username for the given organization.
     * 
     * @param organization The organization/tenant domain
     * @return The admin username
     */
    private String getAdminUsername(String organization) {
        if (organization == null || "carbon.super".equals(organization)) {
            return "admin";
        }
        return "admin@" + organization;
    }
}
