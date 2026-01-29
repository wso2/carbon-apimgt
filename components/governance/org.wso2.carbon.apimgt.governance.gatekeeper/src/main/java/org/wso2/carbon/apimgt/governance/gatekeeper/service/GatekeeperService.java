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
import org.wso2.carbon.apimgt.governance.gatekeeper.GatekeeperConstants;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.MinHashDAO;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.impl.MinHashDAOImpl;
import org.wso2.carbon.apimgt.governance.gatekeeper.lsh.LSHIndex;
import org.wso2.carbon.apimgt.governance.gatekeeper.minhash.MinHashGenerator;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.APISignature;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.ConflictReport;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationResult;

import java.util.ArrayList;
import java.util.List;

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

        log.info("Initializing GatekeeperService - hydrating LSH index from database");
        hydrateIndex();
        initialized = true;
        log.info("GatekeeperService initialization complete. LSH index contains " + lshIndex.size() + " APIs");
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
        if (threshold < GatekeeperConstants.MIN_SIMILARITY_THRESHOLD ||
                threshold > GatekeeperConstants.MAX_SIMILARITY_THRESHOLD) {
            threshold = GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD;
        }

        // Generate signature for the query API
        SignatureService.APISignatureDTO signatureDTO =
                signatureService.generateSignature(apiDefinition, apiUuid, organization);

        int[] querySignature = signatureDTO.getSignatureArray();

        // Find similar APIs using LSH
        List<LSHIndex.SimilarityResult> similarApis =
                lshIndex.findSimilar(querySignature, organization, threshold, minHashGenerator);

        // Filter out the API itself if it's already in the index
        similarApis.removeIf(r -> r.getApiUuid().equals(apiUuid));

        if (similarApis.isEmpty()) {
            DeduplicationResult result = DeduplicationResult.unique(apiUuid, organization);
            result.setThreshold(threshold);
            return result;
        }

        // Build conflict reports
        List<ConflictReport> conflictReports = new ArrayList<>();
        boolean highConfidence = false;

        for (LSHIndex.SimilarityResult similar : similarApis) {
            double similarity = similar.getSimilarity();

            if (similarity >= 0.95) {
                highConfidence = true;
            }

            ConflictReport report = new ConflictReport.Builder()
                    .matchedApiUuid(similar.getApiUuid())
                    .similarityScore(similarity)
                    .message(String.format("API has %.1f%% similarity with existing API",
                            similarity * 100))
                    .recommendation(similarity >= 0.95
                            ? "Consider reusing the existing API or creating a new version"
                            : "Review the similar API to ensure this is not a duplicate")
                    .build();

            conflictReports.add(report);
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
        
        DeduplicationResult result = checkForDuplicates(
                apiDefinition, apiId, organization, GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD);
        
        // Convert to DuplicateCheckResult
        org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult checkResult = 
                new org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult();
        
        checkResult.setApiId(apiId);
        checkResult.setHasDuplicates(result.isDuplicate());
        
        if (result.isDuplicate() && result.getConflictReports() != null) {
            List<org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult.SimilarAPI> similarApis 
                    = new ArrayList<>();
            
            for (ConflictReport report : result.getConflictReports()) {
                org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult.SimilarAPI similar = 
                        new org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult.SimilarAPI(
                                report.getMatchedApiUuid(), report.getSimilarityScore());
                similarApis.add(similar);
            }
            checkResult.setSimilarAPIs(similarApis);
        }
        
        return checkResult;
    }
}
