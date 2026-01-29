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

package org.wso2.carbon.apimgt.governance.gatekeeper.lsh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.gatekeeper.GatekeeperConstants;
import org.wso2.carbon.apimgt.governance.gatekeeper.minhash.MinHashGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Locality Sensitive Hashing (LSH) index for efficient approximate nearest neighbor search.
 * Uses banding technique to find candidate pairs with high similarity.
 */
public class LSHIndex implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(LSHIndex.class);

    private final int numBands;
    private final int rowsPerBand;
    private final int signatureLength;

    // Each band has a hash table mapping band signatures to API UUIDs
    private final List<Map<String, Set<String>>> bandTables;

    // Store full signatures for similarity verification
    private final Map<String, int[]> signatureStore;

    // Store organization mapping for multi-tenant support
    private final Map<String, String> apiOrganizationMap;

    // Read-write lock for thread safety
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates an LSH index with the specified parameters.
     *
     * @param numBands        Number of bands for LSH
     * @param signatureLength Total length of MinHash signatures
     */
    public LSHIndex(int numBands, int signatureLength) {
        this.numBands = numBands;
        this.signatureLength = signatureLength;
        this.rowsPerBand = signatureLength / numBands;

        if (signatureLength % numBands != 0) {
            log.warn("Signature length " + signatureLength + " is not evenly divisible by number of bands " 
                    + numBands + ". Some signature elements will be ignored.");
        }

        // Initialize band tables
        this.bandTables = new ArrayList<>(numBands);
        for (int i = 0; i < numBands; i++) {
            bandTables.add(new ConcurrentHashMap<>());
        }

        this.signatureStore = new ConcurrentHashMap<>();
        this.apiOrganizationMap = new ConcurrentHashMap<>();
    }

    /**
     * Creates an LSH index with default parameters.
     */
    public LSHIndex() {
        this(GatekeeperConstants.DEFAULT_NUM_BANDS, GatekeeperConstants.DEFAULT_NUM_HASH_FUNCTIONS);
    }

    /**
     * Adds an API signature to the index.
     *
     * @param apiUuid      The API UUID
     * @param signature    The MinHash signature
     * @param organization The organization
     */
    public void addSignature(String apiUuid, int[] signature, String organization) {
        if (signature.length != signatureLength) {
            throw new IllegalArgumentException("Signature length mismatch. Expected " + signatureLength 
                    + ", got " + signature.length);
        }

        lock.writeLock().lock();
        try {
            // Store full signature
            signatureStore.put(apiUuid, signature);
            apiOrganizationMap.put(apiUuid, organization);

            // Add to each band table
            for (int band = 0; band < numBands; band++) {
                String bandSignature = computeBandSignature(signature, band);
                bandTables.get(band)
                        .computeIfAbsent(bandSignature, k -> ConcurrentHashMap.newKeySet())
                        .add(apiUuid);
            }

            if (log.isDebugEnabled()) {
                log.debug("Added API " + apiUuid + " to LSH index for organization " + organization);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes an API signature from the index.
     *
     * @param apiUuid The API UUID to remove
     */
    public void removeSignature(String apiUuid) {
        lock.writeLock().lock();
        try {
            int[] signature = signatureStore.remove(apiUuid);
            apiOrganizationMap.remove(apiUuid);

            if (signature != null) {
                // Remove from each band table
                for (int band = 0; band < numBands; band++) {
                    String bandSignature = computeBandSignature(signature, band);
                    Set<String> apiSet = bandTables.get(band).get(bandSignature);
                    if (apiSet != null) {
                        apiSet.remove(apiUuid);
                        if (apiSet.isEmpty()) {
                            bandTables.get(band).remove(bandSignature);
                        }
                    }
                }
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Finds candidate APIs that may be similar to the given signature.
     *
     * @param signature    The query signature
     * @param organization The organization to search within
     * @return Set of candidate API UUIDs
     */
    public Set<String> findCandidates(int[] signature, String organization) {
        Set<String> candidates = new HashSet<>();

        lock.readLock().lock();
        try {
            for (int band = 0; band < numBands; band++) {
                String bandSignature = computeBandSignature(signature, band);
                Set<String> bandCandidates = bandTables.get(band).get(bandSignature);

                if (bandCandidates != null) {
                    for (String apiUuid : bandCandidates) {
                        // Filter by organization
                        String apiOrg = apiOrganizationMap.get(apiUuid);
                        if (organization.equals(apiOrg)) {
                            candidates.add(apiUuid);
                        }
                    }
                }
            }

        } finally {
            lock.readLock().unlock();
        }

        return candidates;
    }

    /**
     * Finds similar APIs with similarity above the threshold.
     *
     * @param signature    The query signature
     * @param organization The organization to search within
     * @param threshold    Similarity threshold (0.0 to 1.0)
     * @param minHash      MinHash generator for similarity computation
     * @return List of similar API results
     */
    public List<SimilarityResult> findSimilar(int[] signature, String organization, 
                                               double threshold, MinHashGenerator minHash) {
        List<SimilarityResult> results = new ArrayList<>();

        Set<String> candidates = findCandidates(signature, organization);

        lock.readLock().lock();
        try {
            for (String candidateUuid : candidates) {
                int[] candidateSignature = signatureStore.get(candidateUuid);
                if (candidateSignature != null) {
                    double similarity = minHash.estimateSimilarity(signature, candidateSignature);
                    if (similarity >= threshold) {
                        results.add(new SimilarityResult(candidateUuid, similarity));
                    }
                }
            }

        } finally {
            lock.readLock().unlock();
        }

        // Sort by similarity descending
        results.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

        return results;
    }

    /**
     * Computes the band signature for a given band.
     */
    private String computeBandSignature(int[] signature, int band) {
        StringBuilder sb = new StringBuilder();
        int start = band * rowsPerBand;
        int end = Math.min(start + rowsPerBand, signature.length);

        for (int i = start; i < end; i++) {
            sb.append(signature[i]).append("_");
        }

        return sb.toString();
    }

    /**
     * Gets the stored signature for an API.
     *
     * @param apiUuid The API UUID
     * @return The signature or null if not found
     */
    public int[] getSignature(String apiUuid) {
        lock.readLock().lock();
        try {
            return signatureStore.get(apiUuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if an API exists in the index.
     *
     * @param apiUuid The API UUID
     * @return True if the API is in the index
     */
    public boolean containsApi(String apiUuid) {
        lock.readLock().lock();
        try {
            return signatureStore.containsKey(apiUuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the number of APIs in the index.
     *
     * @return Number of indexed APIs
     */
    public int size() {
        lock.readLock().lock();
        try {
            return signatureStore.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clears the entire index.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            signatureStore.clear();
            apiOrganizationMap.clear();
            for (Map<String, Set<String>> table : bandTables) {
                table.clear();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets all API UUIDs in the index for a given organization.
     *
     * @param organization The organization
     * @return Set of API UUIDs
     */
    public Set<String> getApisByOrganization(String organization) {
        Set<String> apis = new HashSet<>();

        lock.readLock().lock();
        try {
            for (Map.Entry<String, String> entry : apiOrganizationMap.entrySet()) {
                if (organization.equals(entry.getValue())) {
                    apis.add(entry.getKey());
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return apis;
    }

    /**
     * Result class for similarity search.
     */
    public static class SimilarityResult {
        private final String apiUuid;
        private final double similarity;

        public SimilarityResult(String apiUuid, double similarity) {
            this.apiUuid = apiUuid;
            this.similarity = similarity;
        }

        public String getApiUuid() {
            return apiUuid;
        }

        public double getSimilarity() {
            return similarity;
        }

        @Override
        public String toString() {
            return "SimilarityResult{apiUuid='" + apiUuid + "', similarity=" + similarity + "}";
        }
    }
}
