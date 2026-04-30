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

package org.wso2.carbon.apimgt.governance.generic.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.generic.GenericConstants;
import org.wso2.carbon.apimgt.governance.generic.dao.MinHashDAO;
import org.wso2.carbon.apimgt.governance.generic.dao.impl.MinHashDAOImpl;
import org.wso2.carbon.apimgt.governance.generic.lsh.LSHIndex;
import org.wso2.carbon.apimgt.governance.generic.minhash.MinHashGenerator;
import org.wso2.carbon.apimgt.governance.generic.model.APISignature;
import org.wso2.carbon.apimgt.governance.generic.model.ConflictReport;
import org.wso2.carbon.apimgt.governance.generic.model.DeduplicationResult;
import org.wso2.carbon.apimgt.governance.generic.model.DeprecationGuideResult;
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
public class GenericService {

    private static final Log log = LogFactory.getLog(GenericService.class);

    private static final class InstanceHolder {
        private static final GenericService INSTANCE = new GenericService();
    }

    private final LSHIndex lshIndex;
    private final MinHashGenerator minHashGenerator;
    private final SignatureService signatureService;
    private final MinHashDAO minHashDAO;

    private boolean initialized = false;

    /**
     * Private constructor for singleton.
     */
    private GenericService() {
        this.minHashGenerator = new MinHashGenerator();
        this.lshIndex = new LSHIndex();
        this.signatureService = new SignatureService();
        this.minHashDAO = MinHashDAOImpl.getInstance();
    }

    /**
     * Gets the singleton instance.
     *
     * @return GenericService instance
     */
    public static GenericService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Initializes the service by hydrating the LSH index from the database.
     * Should be called during server startup.
     *
     * @throws APIMGovernanceException If hydration fails
     */
    public synchronized void initialize() throws APIMGovernanceException {
        if (initialized) {
            log.debug("GenericService already initialized");
            return;
        }

        log.info("Initializing GenericService");

        hydrateIndex();
        initialized = true;
        log.info("GenericService initialized. LSH index contains " + lshIndex.size() + " APIs");
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
            } catch (RuntimeException e) {
                log.warn("Failed to load signature for API: " + signature.getApiUuid(), e);
            }
        }

        log.debug("Hydrated LSH index with " + signatures.size() + " signatures");
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
        return checkForDuplicates(apiDefinition, apiUuid, organization, threshold, false);
    }

    /**
     * Checks if an API is a potential duplicate of existing APIs.
     *
     * @param apiDefinition   The API definition to check
     * @param apiUuid         The UUID of the API being checked
     * @param organization    The organization
     * @param threshold       Similarity threshold (0.0 to 1.0)
     * @param skipFirstInRule If true, skips the First-In rule (used by the deprecation guide
     *                        to find newer successors for an older API being deprecated)
     * @return DeduplicationResult with conflict reports if duplicates found
     * @throws APIMGovernanceException If check fails
     */
    public DeduplicationResult checkForDuplicates(String apiDefinition, String apiUuid,
                                                   String organization, double threshold,
                                                   boolean skipFirstInRule)
            throws APIMGovernanceException {

        // Validate threshold
        if (threshold < GenericConstants.MIN_SIMILARITY_THRESHOLD
                || threshold > GenericConstants.MAX_SIMILARITY_THRESHOLD) {
            log.debug("Invalid threshold " + threshold + ", using default: "
                    + GenericConstants.DEFAULT_SIMILARITY_THRESHOLD);
            threshold = GenericConstants.DEFAULT_SIMILARITY_THRESHOLD;
        }

        if (log.isDebugEnabled()) {
            log.debug("Checking for duplicates for API " + apiUuid + " with threshold: "
                    + String.format("%.2f", threshold) + " in organization: " + organization);
        }

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
        if (!similarApis.isEmpty() && log.isDebugEnabled()) {
            for (LSHIndex.SimilarityResult sr : similarApis) {
                log.debug(String.format("Similarity match: API %s -> %s = %.4f (%.2f%%)",
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
        } catch (RuntimeException e) {
            log.debug("Could not read high confidence threshold from config, using default: 0.95");
        }

        // Resolve the creation time and name of the query API for First-In comparison
        // and version-family exclusion
        String queryApiCreatedTime = null;
        String queryApiName = null;
        try {
            String adminUsername = getAdminUsername(organization);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);
            if (apiProvider != null) {
                API queryApi = apiProvider.getAPIbyUUID(apiUuid, organization);
                if (queryApi != null) {
                    queryApiCreatedTime = queryApi.getCreatedTime();
                    if (queryApi.getId() != null) {
                        queryApiName = queryApi.getId().getApiName();
                    }
                }
            }
        } catch (APIManagementException | RuntimeException e) {
            log.debug("Could not resolve creation time/name for query API " + apiUuid + ": " + e.getMessage());
        }

        for (LSHIndex.SimilarityResult similar : similarApis) {
            double similarity = similar.getSimilarity();

            // Resolve matched API name, version, and creation time from UUID
            String matchedApiName = null;
            String matchedApiVersion = null;
            String matchedApiCreatedTime = null;
            String matchedApiProvider = null;
            String matchedApiStatus = null;
            try {
                String adminUsername = getAdminUsername(organization);
                APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);
                if (apiProvider != null) {
                    API matchedApi = apiProvider.getAPIbyUUID(similar.getApiUuid(), organization);
                    if (matchedApi != null && matchedApi.getId() != null) {
                        matchedApiName = matchedApi.getId().getApiName();
                        matchedApiVersion = matchedApi.getId().getVersion();
                        matchedApiCreatedTime = matchedApi.getCreatedTime();
                        matchedApiProvider = matchedApi.getId().getProviderName();
                        matchedApiStatus = matchedApi.getStatus();
                    }
                }
            } catch (APIManagementException | RuntimeException e) {
                log.debug("Could not resolve API name for UUID " + similar.getApiUuid()
                        + ": " + e.getMessage());
            }

            // Stale-entry guard: if the matched API no longer exists in AM_API (was deleted),
            // skip it and remove the orphaned MinHash signature from the index and DB.
            if (matchedApiName == null && matchedApiVersion == null) {
                log.info("Skipping stale LSH match for deleted API " + similar.getApiUuid()
                        + ". Removing orphaned MinHash signature.");
                lshIndex.removeSignature(similar.getApiUuid());
                try {
                    minHashDAO.deleteSignature(similar.getApiUuid(), organization);
                } catch (APIMGovernanceException | RuntimeException cleanupEx) {
                    log.debug("Could not clean up stale MinHash for " + similar.getApiUuid()
                            + ": " + cleanupEx.getMessage());
                }
                continue;
            }

            // Lifecycle-status filter: Only APIs in PUBLISHED or DEPLOYED state should be
            // reported as duplicate targets. Draft, testing, deprecated, retired, or blocked
            // APIs are not active production APIs and should not trigger dedup conflicts.
            // NOTE: This filter is SKIPPED when skipFirstInRule=true (deprecation guide mode)
            // because the deprecation guide specifically needs to find successors among all APIs.
            if (!skipFirstInRule
                    && (matchedApiStatus == null
                    || !("PUBLISHED".equalsIgnoreCase(matchedApiStatus)
                    || "DEPLOYED".equalsIgnoreCase(matchedApiStatus)))) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Lifecycle-status filter: Skipping match %s -> %s "
                                    + "(matched API '%s' is %s, not in PUBLISHED/DEPLOYED state)",
                            apiUuid, similar.getApiUuid(), matchedApiName, matchedApiStatus));
                }
                continue;
            }

            // Version-family exclusion: APIs with the SAME name but different versions are
            // intentional API versioning (via "Create New Version"), NOT duplicates.
            // Skip these matches unless we are explicitly looking for successors (deprecation guide).
            if (!skipFirstInRule && queryApiName != null && matchedApiName != null
                    && queryApiName.equals(matchedApiName)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Version-family exclusion: Skipping match %s -> %s "
                                    + "(same API name '%s', different versions)",
                            apiUuid, similar.getApiUuid(), queryApiName));
                }
                continue;
            }

            // First-In rule: Only report this conflict if the matched API is OLDER than the query API.
            // If the query API is older (or same age), skip — the query API is the "original" and
            // should not receive violations. The newer API (matched) will get its own violations
            // when it is evaluated separately.
            //
            // NOTE: This rule is SKIPPED when skipFirstInRule=true (used by the deprecation guide
            // to find newer successors for the older, about-to-be-deprecated API).
            if (!skipFirstInRule && queryApiCreatedTime != null && matchedApiCreatedTime != null) {
                int timeComparison = queryApiCreatedTime.compareTo(matchedApiCreatedTime);
                if (timeComparison < 0) {
                    // Query API was created BEFORE the matched API → query is the original, skip
                    if (log.isDebugEnabled()) {
                        log.debug(String.format(
                                "First-In rule: Skipping match %s -> %s (query created at %s is older " +
                                        "than match created at %s)",
                                apiUuid, similar.getApiUuid(),
                                queryApiCreatedTime, matchedApiCreatedTime));
                    }
                    continue;
                } else if (timeComparison == 0) {
                    // Same creation time — use UUID lexicographic order as tiebreaker.
                    // The "smaller" UUID is considered the original.
                    if (apiUuid.compareTo(similar.getApiUuid()) < 0) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("First-In rule: Skipping match %s -> %s (same creation time, " +
                                            "query UUID is lexicographically smaller)",
                                    apiUuid, similar.getApiUuid()));
                        }
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
                    .matchedApiProvider(matchedApiProvider)
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

        // If all conflicts were skipped by the First-In rule (or other filters),
        // the query API has no relevant matches
        if (conflictReports.isEmpty()) {
            if (!skipFirstInRule) {
                log.debug("First-In rule: All matches skipped for API " + apiUuid
                        + " (this API is the original/oldest). Marking as unique.");
            } else {
                log.debug("[DEPRECATION-GUIDE] All similarity matches filtered out for API "
                        + apiUuid + ". Marking as unique.");
            }
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
    public org.wso2.carbon.apimgt.governance.generic.model.DuplicateCheckResult checkForDuplicates(
            String apiId, String apiDefinition, String organization) throws APIMGovernanceException {
        
        // Get threshold from deduplication ruleset configuration
        DeduplicationConfigService.DeduplicationConfig config = 
                DeduplicationConfigService.getInstance().getConfig(organization);
        
        // Check if deduplication is enabled in config
        if (!config.isEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Deduplication is disabled via config for organization: " + organization 
                        + ". Skipping check for API " + apiId);
            }
            org.wso2.carbon.apimgt.governance.generic.model.DuplicateCheckResult skipResult = 
                    new org.wso2.carbon.apimgt.governance.generic.model.DuplicateCheckResult();
            skipResult.setApiId(apiId);
            skipResult.setHasDuplicates(false);
            skipResult.setThreshold(config.getSimilarityThreshold());
            return skipResult;
        }

        double threshold = config.getSimilarityThreshold();
        
        if (log.isDebugEnabled()) {
            log.debug("Deduplication check for API " + apiId + " using threshold: " 
                    + String.format("%.2f", threshold * 100) + "% (org: " + organization 
                    + ", mode: " + config.getMode() + ")");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Full config - enabled: " + config.isEnabled() + ", mode: " + config.getMode()
                    + ", numHashFunctions: " + config.getNumHashFunctions() 
                    + ", numBands: " + config.getNumBands());
        }
        
        DeduplicationResult result = checkForDuplicates(
                apiDefinition, apiId, organization, threshold);
        
        // Convert to DuplicateCheckResult
        org.wso2.carbon.apimgt.governance.generic.model.DuplicateCheckResult checkResult = 
                new org.wso2.carbon.apimgt.governance.generic.model.DuplicateCheckResult();
        
        checkResult.setApiId(apiId);
        checkResult.setHasDuplicates(result.isDuplicate());
        checkResult.setThreshold(threshold);
        
        if (result.isDuplicate() && result.getConflictReports() != null) {
            List<org.wso2.carbon.apimgt.governance.generic.model.DuplicateCheckResult.SimilarAPI> similarApis 
                    = new ArrayList<>();
            
            for (ConflictReport report : result.getConflictReports()) {
                org.wso2.carbon.apimgt.governance.generic.model.DuplicateCheckResult.SimilarAPI similar = 
                        new org.wso2.carbon.apimgt.governance.generic.model.DuplicateCheckResult.SimilarAPI(
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
        log.debug("Starting to index existing APIs for deduplication...");
        
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
            log.debug("Found " + existingSignatureApiIds.size() + " APIs already indexed in AM_API_MINHASH");

            // Get all organizations
            List<String> organizations = getOrganizations();
            
            for (String organization : organizations) {
                try {
                    // Get all APIs for this organization
                    List<ApiResult> apis = ApiMgtDAO.getInstance().getAllAPIs(organization);
                    log.debug("Found " + apis.size() + " APIs in organization: " + organization);
                    
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
                        } catch (APIMGovernanceException | RuntimeException e) {
                            log.warn("Failed to index API " + apiId + ": " + e.getMessage());
                            errorCount++;
                        }
                    }
                } catch (APIManagementException | RuntimeException e) {
                    log.error("Error processing organization " + organization + ": " + e.getMessage(), e);
                }
            }
            
        } catch (APIMGovernanceException | RuntimeException e) {
            log.error("Error indexing existing APIs: " + e.getMessage(), e);
        }
        
        log.debug("Finished indexing existing APIs. Indexed: " + indexedCount + 
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
    String getAPIDefinition(String apiId, String organization) {
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
        } catch (APIManagementException | RuntimeException e) {
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
    public String getAdminUsername(String organization) {
        if (organization == null || "carbon.super".equals(organization)) {
            return "admin";
        }
        return "admin@" + organization;
    }

    /**
     * Finds a structural successor for an API that is about to be deprecated or retired.
     * Uses MinHash/LSH similarity to find the best non-deprecated, non-retired API
     * that can serve as a replacement.
     *
     * <p><b>Scenario A (successor found):</b> Returns a result with the best successor
     * details, all candidate versions, and pre-computed RFC 8594 Link/Sunset headers.</p>
     * <p><b>Scenario B (no successor):</b> Returns a migration-risk result requiring
     * explicit user acknowledgement before the transition.</p>
     *
     * @param apiUuid         UUID of the API about to be deprecated/retired
     * @param organization    The organization
     * @param lifecycleAction The lifecycle action: "Deprecate" or "Retire"
     * @return DeprecationGuideResult with successor info or migration risk warning
     */
    public DeprecationGuideResult findSuccessorForDeprecation(String apiUuid, String organization,
                                                               String lifecycleAction) {
        log.debug("[DEPRECATION-GUIDE] Finding structural successor for API " + apiUuid
                + " in org: " + organization + " (action: " + lifecycleAction + ")");

        // ── Policy Membership Guardrail ──────────────────────────────────
        // Only execute transition checks if the lifecycle ruleset is explicitly
        // enabled (associated) in at least one governance policy for this organization.
        if (!isLifecycleRulesetInActivePolicy(organization)) {
            log.debug("[DEPRECATION-GUIDE] Lifecycle ruleset not enabled in any governance policy "
                    + "for org: " + organization + ". Skipping transition check for API " + apiUuid);
            return DeprecationGuideResult.noSuccessor(
                    apiUuid, "Unknown", "Unknown", organization,
                    lifecycleAction != null ? lifecycleAction : "Deprecate",
                    DeprecationGuideResult.MODE_WARN);
        }

        String apiName = "Unknown";
        String apiVersion = "Unknown";

        // Read enforcement mode and successor threshold.
        // Priority: 1) DB (GOV_RULESET_CONTENT — updated by Admin UI)
        //           2) YAML file on disk (initial defaults)
        String enforcementMode = DeprecationGuideResult.MODE_WARN;
        double successorThreshold = 0.5; // default
        int sunsetPeriodDays = 90; // default
        boolean configLoadedFromDb = false;

        // ── 1. Try reading from GOV_RULESET_CONTENT (Admin UI persists here) ──
        try {
            String dbContent = getLifecycleRulesetContentFromDb(organization);
            if (dbContent != null && !dbContent.isEmpty()) {
                // Section-aware parsing: only read config from
                // lifecycle_retirement section for combined rulesets
                String currentSection = null;
                for (String line : dbContent.split("\n")) {
                    String trimmed = line.trim();
                    int indent = line.length() - line.stripLeading().length();
                    // Track top-level sections (indent 0)
                    if (indent == 0 && trimmed.endsWith(":")) {
                        currentSection = trimmed.substring(
                                0, trimmed.length() - 1);
                    }
                    // Skip settings under deduplication section
                    if ("deduplication".equals(currentSection)
                            && indent > 0) {
                        continue;
                    }
                    if (trimmed.startsWith("mode:")) {
                        String modeValue = trimmed.substring(5).trim()
                                .replace("\"", "").replace("'", "");
                        if ("block".equalsIgnoreCase(modeValue)) {
                            enforcementMode
                                    = DeprecationGuideResult.MODE_BLOCK;
                        } else if ("warn".equalsIgnoreCase(modeValue)) {
                            enforcementMode
                                    = DeprecationGuideResult.MODE_WARN;
                        }
                        configLoadedFromDb = true;
                    } else if (trimmed.startsWith("similarity_threshold:")) {
                        String thresholdStr = trimmed.substring(
                                "similarity_threshold:".length()).trim();
                        try {
                            double t = Double.parseDouble(thresholdStr);
                            successorThreshold = t;
                        } catch (NumberFormatException nfe) {
                            log.debug("[DEPRECATION-GUIDE] Invalid "
                                    + "threshold in DB: " + thresholdStr);
                        }
                    } else if (trimmed.startsWith(
                            "successor_similarity_threshold:")) {
                        String thresholdStr = trimmed.substring(
                                "successor_similarity_threshold:"
                                        .length()).trim();
                        try {
                            successorThreshold
                                    = Double.parseDouble(thresholdStr);
                        } catch (NumberFormatException nfe) {
                            log.debug("[DEPRECATION-GUIDE] Invalid "
                                    + "successor threshold: "
                                    + thresholdStr);
                        }
                    } else if (trimmed.startsWith("sunset_period_days:")) {
                        String daysStr = trimmed.substring(
                                "sunset_period_days:".length()).trim();
                        try {
                            sunsetPeriodDays = Integer.parseInt(daysStr);
                        } catch (NumberFormatException nfe) {
                            log.debug("[DEPRECATION-GUIDE] Invalid "
                                    + "sunset_period_days: " + daysStr);
                        }
                    }
                }
                if (configLoadedFromDb) {
                    log.debug("[DEPRECATION-GUIDE] Config loaded from "
                            + "DB — mode: " + enforcementMode
                            + ", threshold: " + successorThreshold
                            + ", sunsetDays: " + sunsetPeriodDays);
                }
            }
        } catch (RuntimeException e) {
            log.debug("[DEPRECATION-GUIDE] Could not read config from DB: "
                    + e.getMessage());
        }

        // ── 2. Fallback: YAML file on disk ──
        if (!configLoadedFromDb) {
            try {
                String carbonHome = System.getProperty("carbon.home", "");
                java.io.File rulesetFile = new java.io.File(carbonHome
                        + "/repository/resources/governance/default-rulesets/"
                        + "lifecycle-retirement-ruleset.yaml");
                if (rulesetFile.exists()) {
                    String content = new String(java.nio.file.Files.readAllBytes(
                            rulesetFile.toPath()),
                            java.nio.charset.StandardCharsets.UTF_8);
                    for (String line : content.split("\n")) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith("mode:")) {
                            String modeValue = trimmed.substring(5).trim()
                                    .replace("\"", "").replace("'", "");
                            if ("block".equalsIgnoreCase(modeValue)) {
                                enforcementMode = DeprecationGuideResult.MODE_BLOCK;
                            }
                        } else if (trimmed.startsWith("successor_similarity_threshold:")) {
                            String thresholdStr = trimmed.substring(
                                    "successor_similarity_threshold:".length()).trim();
                            try {
                                successorThreshold = Double.parseDouble(thresholdStr);
                            } catch (NumberFormatException nfe) {
                                log.debug("[DEPRECATION-GUIDE] Invalid threshold in YAML: " + thresholdStr);
                            }
                        } else if (trimmed.startsWith("sunset_period_days:")) {
                            String daysStr = trimmed.substring("sunset_period_days:".length()).trim();
                            try {
                                sunsetPeriodDays = Integer.parseInt(daysStr);
                            } catch (NumberFormatException nfe) {
                                log.debug("[DEPRECATION-GUIDE] Invalid sunset_period_days: " + daysStr);
                            }
                        }
                    }
                    log.debug("[DEPRECATION-GUIDE] Config loaded from YAML — mode: "
                            + enforcementMode + ", threshold: " + successorThreshold
                            + ", sunsetDays: " + sunsetPeriodDays);
                } else {
                    log.debug("[DEPRECATION-GUIDE] lifecycle-retirement-ruleset.yaml not found. "
                            + "Using defaults.");
                }
            } catch (java.io.IOException | RuntimeException e) {
                log.debug("[DEPRECATION-GUIDE] Could not read YAML config: " + e.getMessage());
            }
        }

        // Store sunsetPeriodDays for use in sunset header computation
        final int finalSunsetDays = sunsetPeriodDays;

        try {
            // Resolve the API being deprecated/retired
            String adminUsername = getAdminUsername(organization);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);
            API deprecatingApi = apiProvider.getAPIbyUUID(apiUuid, organization);

            if (deprecatingApi != null && deprecatingApi.getId() != null) {
                apiName = deprecatingApi.getId().getApiName();
                apiVersion = deprecatingApi.getId().getVersion();
            }

            // ── Successor Carryover: DEPRECATED → RETIRED ──────────────────
            // If we are retiring and a successor was already assigned during deprecation,
            // carry it over automatically instead of re-scanning.
            if ("Retire".equalsIgnoreCase(lifecycleAction) && deprecatingApi != null) {
                String carriedSuccessorUuid = deprecatingApi.getProperty("X-Deprecation-Successor-UUID");
                if (carriedSuccessorUuid != null && !carriedSuccessorUuid.isEmpty()) {
                    log.debug("[DEPRECATION-GUIDE] Successor carryover: API " + apiUuid
                            + " already has successor " + carriedSuccessorUuid
                            + " from DEPRECATED state. Carrying over to RETIRED.");
                    try {
                        API carriedApi = apiProvider.getAPIbyUUID(carriedSuccessorUuid, organization);
                        if (carriedApi != null
                                && APIStatus.PUBLISHED.getStatus().equalsIgnoreCase(carriedApi.getStatus())) {
                            String cName = carriedApi.getId() != null
                                    ? carriedApi.getId().getApiName() : "Unknown";
                            String cVersion = carriedApi.getId() != null
                                    ? carriedApi.getId().getVersion() : "Unknown";
                            String cContext = carriedApi.getContext() != null
                                    ? carriedApi.getContext() : "/" + cName;
                            String linkHeader = "<" + cContext + ">; rel=\"successor-version\"";
                            java.time.ZonedDateTime sunsetDate =
                                    java.time.ZonedDateTime.now().plusDays(finalSunsetDays);
                            String sunsetHeader = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                                    .format(sunsetDate);

                            boolean isOfficial = apiName.equals(cName);
                            String sType = isOfficial
                                    ? DeprecationGuideResult.TYPE_OFFICIAL_VERSION
                                    : DeprecationGuideResult.TYPE_SEMANTIC_NEIGHBOR;

                            // Build single-candidate list for the carried-over successor
                            List<DeprecationGuideResult.SuccessorCandidate> carried = new ArrayList<>();
                            carried.add(new DeprecationGuideResult.SuccessorCandidate(
                                    carriedSuccessorUuid, cName, cVersion,
                                    100.0, sType, "PUBLISHED", cContext));

                            DeprecationGuideResult result = DeprecationGuideResult.successorFound(
                                    apiUuid, apiName, apiVersion, organization,
                                    carriedSuccessorUuid, cName, cVersion,
                                    100.0, linkHeader, sunsetHeader,
                                    "PUBLISHED", sType,
                                    carried, lifecycleAction, enforcementMode);
                            result.setSuccessorCarriedOver(true);
                            return result;
                        }
                        log.debug("[DEPRECATION-GUIDE] Carried-over successor " + carriedSuccessorUuid
                                + " is no longer PUBLISHED. Re-scanning...");
                    } catch (APIManagementException | RuntimeException e) {
                        log.debug("[DEPRECATION-GUIDE] Could not resolve carried-over successor: "
                                + e.getMessage() + ". Re-scanning...");
                    }
                }
            }

            // Get the OpenAPI definition for the API being deprecated/retired
            String apiDefinition = getAPIDefinition(apiUuid, organization);
            if (apiDefinition == null || apiDefinition.isEmpty()) {
                log.warn("[DEPRECATION-GUIDE] No API definition found for " + apiUuid
                        + ". Cannot search for successor.");
                return DeprecationGuideResult.noSuccessor(
                        apiUuid, apiName, apiVersion, organization,
                        lifecycleAction, enforcementMode);
            }

            // Use the successor similarity threshold from the lifecycle ruleset config.
            // This is ISOLATED from the deduplication ruleset — changes in Admin Portal
            // for one ruleset do not affect the other.
            // skipFirstInRule=true: we WANT to find newer APIs as successors for the older API
            DeduplicationResult result = checkForDuplicates(
                    apiDefinition, apiUuid, organization, successorThreshold, true);

            if (!result.isDuplicate() || result.getConflictReports() == null
                    || result.getConflictReports().isEmpty()) {
                log.debug("[DEPRECATION-GUIDE] No structurally similar APIs found for " + apiUuid);
                return DeprecationGuideResult.noSuccessor(
                        apiUuid, apiName, apiVersion, organization,
                        lifecycleAction, enforcementMode);
            }

            // ── Multi-Version Candidate Discovery ──────────────────────────
            // Collect ALL eligible PUBLISHED candidates (official versions + semantic neighbors).
            // Sort by priority: official versions first, then by similarity descending.
            List<DeprecationGuideResult.SuccessorCandidate> allCandidates = new ArrayList<>();
            ConflictReport bestOfficialVersion = null;
            API bestOfficialVersionApi = null;
            ConflictReport bestSemanticNeighbor = null;
            API bestSemanticNeighborApi = null;

            for (ConflictReport conflict : result.getConflictReports()) {
                String matchedUuid = conflict.getMatchedApiUuid();
                if (matchedUuid == null || matchedUuid.equals(apiUuid)) {
                    continue;
                }

                try {
                    API matchedApi = apiProvider.getAPIbyUUID(matchedUuid, organization);
                    if (matchedApi == null) {
                        continue;
                    }

                    String status = matchedApi.getStatus();

                    // Strict lifecycle filter: ONLY PUBLISHED APIs are valid successors
                    if (!APIStatus.PUBLISHED.getStatus().equalsIgnoreCase(status)) {
                        log.debug(String.format("[DEPRECATION-GUIDE] Skipping %s (status: %s) "
                                        + "— only PUBLISHED APIs qualify as successors",
                                matchedUuid, status));
                        continue;
                    }

                    String matchedName = matchedApi.getId() != null
                            ? matchedApi.getId().getApiName() : null;
                    String matchedVersion = matchedApi.getId() != null
                            ? matchedApi.getId().getVersion() : "Unknown";
                    String matchedContext = matchedApi.getContext() != null
                            ? matchedApi.getContext() : "/" + matchedName;
                    double similarityPct = conflict.getSimilarityScore() * 100.0;
                    boolean isOfficialVersion = apiName != null && matchedName != null
                            && apiName.equals(matchedName);
                    String candidateType = isOfficialVersion
                            ? DeprecationGuideResult.TYPE_OFFICIAL_VERSION
                            : DeprecationGuideResult.TYPE_SEMANTIC_NEIGHBOR;

                    // Add to all-candidates list for multi-version selection
                    allCandidates.add(new DeprecationGuideResult.SuccessorCandidate(
                            matchedUuid, matchedName, matchedVersion,
                            similarityPct, candidateType, status, matchedContext));

                    // Track best in each category
                    if (isOfficialVersion) {
                        if (bestOfficialVersion == null
                                || conflict.getSimilarityScore()
                                > bestOfficialVersion.getSimilarityScore()) {
                            bestOfficialVersion = conflict;
                            bestOfficialVersionApi = matchedApi;
                        }
                    } else {
                        if (bestSemanticNeighbor == null
                                || conflict.getSimilarityScore()
                                > bestSemanticNeighbor.getSimilarityScore()) {
                            bestSemanticNeighbor = conflict;
                            bestSemanticNeighborApi = matchedApi;
                        }
                    }
                } catch (APIManagementException | RuntimeException e) {
                    log.debug("[DEPRECATION-GUIDE] Could not resolve API " + matchedUuid
                            + ": " + e.getMessage());
                }
            }

            // Sort candidates: official versions first, then by similarity descending
            allCandidates.sort((a, b) -> {
                boolean aOfficial = DeprecationGuideResult.TYPE_OFFICIAL_VERSION
                        .equals(a.getSuccessorType());
                boolean bOfficial = DeprecationGuideResult.TYPE_OFFICIAL_VERSION
                        .equals(b.getSuccessorType());
                if (aOfficial != bOfficial) {
                    return aOfficial ? -1 : 1;
                }
                return Double.compare(b.getSimilarityPercentage(), a.getSimilarityPercentage());
            });

            // Select the best successor using priority order
            ConflictReport bestSuccessor;
            API bestApi;
            String successorType;

            if (bestOfficialVersion != null) {
                bestSuccessor = bestOfficialVersion;
                bestApi = bestOfficialVersionApi;
                successorType = DeprecationGuideResult.TYPE_OFFICIAL_VERSION;
            } else if (bestSemanticNeighbor != null) {
                bestSuccessor = bestSemanticNeighbor;
                bestApi = bestSemanticNeighborApi;
                successorType = DeprecationGuideResult.TYPE_SEMANTIC_NEIGHBOR;
            } else {
                bestSuccessor = null;
                bestApi = null;
                successorType = null;
            }

            if (bestSuccessor == null) {
                log.debug("[DEPRECATION-GUIDE] No eligible PUBLISHED successor found for "
                        + apiUuid + " (all similar APIs are non-published)");
                return DeprecationGuideResult.noSuccessor(
                        apiUuid, apiName, apiVersion, organization,
                        lifecycleAction, enforcementMode);
            }

            // Build Scenario A result
            String successorName = bestApi.getId() != null
                    ? bestApi.getId().getApiName() : "Unknown";
            String successorVersion = bestApi.getId() != null
                    ? bestApi.getId().getVersion() : "Unknown";
            String successorStatus = bestApi.getStatus();
            double similarityPct = bestSuccessor.getSimilarityScore() * 100.0;

            String successorContext = bestApi.getContext() != null
                    ? bestApi.getContext() : "/" + successorName;
            String linkHeader = "<" + successorContext + ">; rel=\"successor-version\"";

            java.time.ZonedDateTime sunsetDate = java.time.ZonedDateTime.now().plusDays(finalSunsetDays);
            String sunsetHeader = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(sunsetDate);

            log.debug(String.format("[DEPRECATION-GUIDE] Successor found for %s v%s → %s v%s "
                            + "(%.1f%% similarity, type: %s, candidates: %d, action: %s)",
                    apiName, apiVersion, successorName, successorVersion,
                    similarityPct, successorType,
                    allCandidates.size(), lifecycleAction));

            return DeprecationGuideResult.successorFound(
                    apiUuid, apiName, apiVersion, organization,
                    bestSuccessor.getMatchedApiUuid(), successorName, successorVersion,
                    similarityPct, linkHeader, sunsetHeader,
                    successorStatus, successorType,
                    allCandidates, lifecycleAction, enforcementMode);

        } catch (APIManagementException | APIMGovernanceException | RuntimeException e) {
            log.error("[DEPRECATION-GUIDE] Error finding successor for API " + apiUuid, e);
            return DeprecationGuideResult.noSuccessor(
                    apiUuid, apiName, apiVersion, organization,
                    lifecycleAction, enforcementMode);
        }
    }

    /**
     * Reads the lifecycle ruleset content from GOV_RULESET_CONTENT in the database.
     * The Admin UI persists mode/threshold changes here when the user clicks "Update".
     *
     * @param organization The organization (tenant domain)
     * @return The YAML content string, or null if not found
     */
    private String getLifecycleRulesetContentFromDb(String organization) {
        // Match by name OR by content containing lifecycle_retirement
        // to support combined rulesets that don't have lifecycle/retirement in name
        // Returns the content with the STRICTEST lifecycle mode (block > warn)
        String sql = "SELECT rc.CONTENT FROM GOV_RULESET_CONTENT rc "
                + "JOIN GOV_RULESET r ON rc.RULESET_ID = r.RULESET_ID "
                + "WHERE r.RULE_CATEGORY = 'GENERIC' "
                + "AND (LOWER(r.NAME) LIKE '%lifecycle%' "
                + "OR LOWER(r.NAME) LIKE '%retirement%' "
                + "OR CAST(rc.CONTENT AS CHAR(4000)) "
                + "LIKE '%lifecycle\\_retirement:%')";
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            conn = org.wso2.carbon.apimgt.governance.impl.util
                    .APIMGovernanceDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            String bestContent = null;
            boolean foundBlock = false;
            while (rs.next()) {
                String content = null;
                try {
                    java.sql.Blob blob = rs.getBlob("CONTENT");
                    if (blob != null) {
                        byte[] bytes = blob.getBytes(
                                1, (int) blob.length());
                        content = new String(bytes,
                                java.nio.charset.StandardCharsets.UTF_8);
                    }
                } catch (java.sql.SQLException | RuntimeException blobEx) {
                    // Not a BLOB — try as string
                }
                if (content == null) {
                    content = rs.getString("CONTENT");
                }
                if (content == null || content.isEmpty()) {
                    continue;
                }
                // Parse lifecycle mode with section-aware logic
                String lifecycleMode = extractLifecycleMode(content);
                if ("block".equalsIgnoreCase(lifecycleMode)) {
                    // Block is the strictest — return immediately
                    log.debug("[DEPRECATION-GUIDE] Found lifecycle "
                            + "content with mode=block");
                    return content;
                }
                if (bestContent == null) {
                    bestContent = content;
                }
            }
            return bestContent;
        } catch (java.sql.SQLException e) {
            log.debug("[DEPRECATION-GUIDE] Could not read lifecycle "
                    + "ruleset content from DB: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (java.sql.SQLException | RuntimeException ignored) {
                    log.debug("[DEPRECATION-GUIDE] Failed to close ResultSet: " + ignored.getMessage());
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (java.sql.SQLException | RuntimeException ignored) {
                    log.debug("[DEPRECATION-GUIDE] Failed to close PreparedStatement: " + ignored.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (java.sql.SQLException | RuntimeException ignored) {
                    log.debug("[DEPRECATION-GUIDE] Failed to close Connection: " + ignored.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Extract the lifecycle mode from YAML content using
     * section-aware parsing. Returns "block", "warn", or null.
     */
    private String extractLifecycleMode(String content) {
        String currentSection = null;
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            int indent = line.length()
                    - line.stripLeading().length();
            if (indent == 0 && trimmed.endsWith(":")) {
                currentSection = trimmed.substring(
                        0, trimmed.length() - 1);
            }
            // Skip settings under deduplication section
            if ("deduplication".equals(currentSection)
                    && indent > 0) {
                continue;
            }
            if (trimmed.startsWith("mode:")) {
                return trimmed.substring(5).trim()
                        .replace("\"", "").replace("'", "");
            }
        }
        return null;
    }

    /**
     * Checks if the lifecycle/retirement ruleset is associated with at least one
     * governance policy for the given organization. If not, the transition checks
     * should be skipped — the ruleset is not actively enforced.
     *
     * @param organization The organization (tenant domain)
     * @return true if the lifecycle ruleset is in an active policy, false otherwise
     */
    public boolean isLifecycleRulesetInActivePolicy(String organization) {
        String tenantDomain = organization != null ? organization : "carbon.super";
        // Match by name OR by content containing lifecycle_retirement
        // to support combined rulesets
        String sql = "SELECT COUNT(*) AS CNT FROM GOV_POLICY_RULESET pr "
                + "JOIN GOV_RULESET r ON pr.RULESET_ID = r.RULESET_ID "
                + "LEFT JOIN GOV_RULESET_CONTENT rc ON r.RULESET_ID = rc.RULESET_ID "
                + "WHERE r.RULE_CATEGORY = 'GENERIC' "
                + "AND (LOWER(r.NAME) LIKE '%lifecycle%' "
                + "OR LOWER(r.NAME) LIKE '%retirement%' "
                + "OR CAST(rc.CONTENT AS CHAR(4000)) "
                + "LIKE '%lifecycle\\_retirement:%') "
                + "AND (r.ORGANIZATION = ? OR r.ORGANIZATION = 'carbon.super')";
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            conn = org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, tenantDomain);
            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("CNT");
                if (log.isDebugEnabled()) {
                    log.debug("[DEPRECATION-GUIDE] Lifecycle policy association count for org "
                            + tenantDomain + ": " + count);
                }
                return count > 0;
            }
        } catch (java.sql.SQLException e) {
            log.warn("[DEPRECATION-GUIDE] Error checking lifecycle policy membership: "
                    + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (java.sql.SQLException | RuntimeException ignored) {
                    log.debug("[DEPRECATION-GUIDE] Failed to close ResultSet: " + ignored.getMessage());
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (java.sql.SQLException | RuntimeException ignored) {
                    log.debug("[DEPRECATION-GUIDE] Failed to close PreparedStatement: " + ignored.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (java.sql.SQLException | RuntimeException ignored) {
                    log.debug("[DEPRECATION-GUIDE] Failed to close Connection: " + ignored.getMessage());
                }
            }
        }
        // Fail-closed: if we cannot verify, do NOT enforce
        return false;
    }

    /**
     * Builds a DeprecationGuideResult for a user-confirmed successor UUID.
     * This bypasses MinHash/LSH discovery — the user has already chosen the successor
     * (either in the current request or from a persisted mapping).
     *
     * @param apiUuid          UUID of the API being deprecated/retired
     * @param successorUuid    UUID of the user-selected successor API
     * @param organization     The organization (tenant domain)
     * @param lifecycleAction  "Deprecate" or "Retire"
     * @return DeprecationGuideResult with the selected successor details
     */
    public DeprecationGuideResult buildGuideForKnownSuccessor(String apiUuid, String successorUuid,
                                                               String organization,
                                                               String lifecycleAction) {
        log.debug("[DEPRECATION-GUIDE] Building guide for known successor: "
                + apiUuid + " → " + successorUuid + " (action: " + lifecycleAction + ")");

        String apiName = "Unknown";
        String apiVersion = "Unknown";

        // Read enforcement mode & sunset period from config (DB-first, YAML-fallback)
        String enforcementMode = DeprecationGuideResult.MODE_WARN;
        int sunsetPeriodDays = 90;
        boolean configLoadedFromDb = false;

        try {
            String dbContent = getLifecycleRulesetContentFromDb(organization);
            if (dbContent != null && !dbContent.isEmpty()) {
                // Section-aware parsing: skip deduplication section
                String currentSection = null;
                for (String line : dbContent.split("\n")) {
                    String trimmed = line.trim();
                    int indent = line.length()
                            - line.stripLeading().length();
                    if (indent == 0 && trimmed.endsWith(":")) {
                        currentSection = trimmed.substring(
                                0, trimmed.length() - 1);
                    }
                    if ("deduplication".equals(currentSection)
                            && indent > 0) {
                        continue;
                    }
                    if (trimmed.startsWith("mode:")) {
                        String modeValue = trimmed.substring(5).trim()
                                .replace("\"", "").replace("'", "");
                        if ("block".equalsIgnoreCase(modeValue)) {
                            enforcementMode
                                    = DeprecationGuideResult.MODE_BLOCK;
                        } else if ("warn".equalsIgnoreCase(modeValue)) {
                            enforcementMode
                                    = DeprecationGuideResult.MODE_WARN;
                        }
                        configLoadedFromDb = true;
                    } else if (trimmed.startsWith(
                            "sunset_period_days:")) {
                        String daysStr = trimmed.substring(
                                "sunset_period_days:".length()).trim();
                        try {
                            sunsetPeriodDays = Integer.parseInt(daysStr);
                        } catch (NumberFormatException nfe) {
                            // ignore
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            log.debug("[DEPRECATION-GUIDE] Could not read config "
                    + "from DB for known successor: "
                    + e.getMessage());
        }

        if (!configLoadedFromDb) {
            try {
                String carbonHome = System.getProperty("carbon.home", "");
                java.io.File rulesetFile = new java.io.File(carbonHome
                        + "/repository/resources/governance/default-rulesets/"
                        + "lifecycle-retirement-ruleset.yaml");
                if (rulesetFile.exists()) {
                    String content = new String(java.nio.file.Files.readAllBytes(
                            rulesetFile.toPath()),
                            java.nio.charset.StandardCharsets.UTF_8);
                    for (String line : content.split("\n")) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith("mode:")) {
                            String modeValue = trimmed.substring(5).trim()
                                    .replace("\"", "").replace("'", "");
                            if ("block".equalsIgnoreCase(modeValue)) {
                                enforcementMode = DeprecationGuideResult.MODE_BLOCK;
                            }
                        } else if (trimmed.startsWith("sunset_period_days:")) {
                            String daysStr = trimmed.substring("sunset_period_days:".length()).trim();
                            try {
                                sunsetPeriodDays = Integer.parseInt(daysStr);
                            } catch (NumberFormatException nfe) {
                                // ignore
                            }
                        }
                    }
                }
            } catch (java.io.IOException | RuntimeException e) {
                log.debug("[DEPRECATION-GUIDE] Could not read YAML config for known successor: "
                        + e.getMessage());
            }
        }

        final int finalSunsetDays = sunsetPeriodDays;

        try {
            String adminUsername = getAdminUsername(organization);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);

            // Resolve the deprecating API
            API deprecatingApi = apiProvider.getAPIbyUUID(apiUuid, organization);
            if (deprecatingApi != null && deprecatingApi.getId() != null) {
                apiName = deprecatingApi.getId().getApiName();
                apiVersion = deprecatingApi.getId().getVersion();
            }

            // Resolve the successor API
            API successorApi = apiProvider.getAPIbyUUID(successorUuid, organization);
            if (successorApi == null) {
                log.warn("[DEPRECATION-GUIDE] User-selected successor " + successorUuid
                        + " not found. Returning no-successor result.");
                return DeprecationGuideResult.noSuccessor(
                        apiUuid, apiName, apiVersion, organization,
                        lifecycleAction, enforcementMode);
            }

            String successorStatus = successorApi.getStatus();
            if (!APIStatus.PUBLISHED.getStatus().equalsIgnoreCase(successorStatus)) {
                log.warn("[DEPRECATION-GUIDE] User-selected successor " + successorUuid
                        + " is not PUBLISHED (status: " + successorStatus + ").");
                return DeprecationGuideResult.noSuccessor(
                        apiUuid, apiName, apiVersion, organization,
                        lifecycleAction, enforcementMode);
            }

            String successorName = successorApi.getId() != null
                    ? successorApi.getId().getApiName() : "Unknown";
            String successorVersion = successorApi.getId() != null
                    ? successorApi.getId().getVersion() : "Unknown";
            String successorContext = successorApi.getContext() != null
                    ? successorApi.getContext() : "/" + successorName;
            double similarityPct = 100.0; // User explicitly confirmed

            boolean isOfficialVersion = apiName.equals(successorName);
            String successorType = isOfficialVersion
                    ? DeprecationGuideResult.TYPE_OFFICIAL_VERSION
                    : DeprecationGuideResult.TYPE_SEMANTIC_NEIGHBOR;

            String linkHeader = "<" + successorContext + ">; rel=\"successor-version\"";
            java.time.ZonedDateTime sunsetDate = java.time.ZonedDateTime.now().plusDays(finalSunsetDays);
            String sunsetHeader = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(sunsetDate);

            // Build single-candidate list
            List<DeprecationGuideResult.SuccessorCandidate> candidates = new ArrayList<>();
            candidates.add(new DeprecationGuideResult.SuccessorCandidate(
                    successorUuid, successorName, successorVersion,
                    similarityPct, successorType, successorStatus, successorContext));

            log.debug(String.format("[DEPRECATION-GUIDE] Known successor resolved: %s v%s → %s v%s "
                            + "(user-confirmed, type: %s, action: %s)",
                    apiName, apiVersion, successorName, successorVersion,
                    successorType, lifecycleAction));

            return DeprecationGuideResult.successorFound(
                    apiUuid, apiName, apiVersion, organization,
                    successorUuid, successorName, successorVersion,
                    similarityPct, linkHeader, sunsetHeader,
                    successorStatus, successorType,
                    candidates, lifecycleAction, enforcementMode);

        } catch (APIManagementException | RuntimeException e) {
            log.error("[DEPRECATION-GUIDE] Error building guide for known successor "
                    + successorUuid + " of API " + apiUuid, e);
            return DeprecationGuideResult.noSuccessor(
                    apiUuid, apiName, apiVersion, organization,
                    lifecycleAction, enforcementMode);
        }
    }

    /**
     * Backward-compatible overload — defaults to "Deprecate" action.
     */
    public DeprecationGuideResult findSuccessorForDeprecation(String apiUuid, String organization) {
        return findSuccessorForDeprecation(apiUuid, organization, "Deprecate");
    }
}
