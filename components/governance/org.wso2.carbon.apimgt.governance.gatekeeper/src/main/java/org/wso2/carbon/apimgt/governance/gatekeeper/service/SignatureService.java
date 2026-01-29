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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.gatekeeper.GatekeeperConstants;
import org.wso2.carbon.apimgt.governance.gatekeeper.minhash.MinHashGenerator;
import org.wso2.carbon.apimgt.governance.gatekeeper.minhash.ShinglingUtil;
import org.wso2.carbon.apimgt.governance.gatekeeper.util.APIPruningUtil;

import java.io.Serializable;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/**
 * Service for generating API signatures in a format suitable for both local LSH
 * and future AI Remote Plane integration.
 * <p>
 * This service encapsulates signature generation logic and provides serializable
 * output formats (JSON/Vector) ready for Phase 2 AI integration.
 */
public class SignatureService {

    private static final Log log = LogFactory.getLog(SignatureService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MinHashGenerator minHashGenerator;
    private final int numHashFunctions;

    /**
     * Creates a SignatureService with the specified configuration.
     *
     * @param numHashFunctions Number of hash functions to use
     * @param seed             Random seed for reproducibility
     */
    public SignatureService(int numHashFunctions, long seed) {
        this.numHashFunctions = numHashFunctions;
        this.minHashGenerator = new MinHashGenerator(numHashFunctions, seed);
    }

    /**
     * Creates a SignatureService with default configuration.
     */
    public SignatureService() {
        this(GatekeeperConstants.DEFAULT_NUM_HASH_FUNCTIONS, 42L);
    }

    /**
     * Generates a complete API signature from an API definition.
     *
     * @param apiDefinition The OpenAPI/AsyncAPI definition
     * @param apiUuid       The API UUID
     * @param organization  The organization
     * @return APISignatureDTO containing signature in multiple formats
     * @throws APIMGovernanceException If signature generation fails
     */
    public APISignatureDTO generateSignature(String apiDefinition, String apiUuid, 
                                              String organization) throws APIMGovernanceException {
        try {
            // Step 1: Prune API definition
            String prunedDefinition = APIPruningUtil.pruneAPIDefinition(apiDefinition);

            // Step 2: Extract features
            List<String> features = APIPruningUtil.extractFeatures(prunedDefinition);

            // Step 3: Create shingles
            Set<String> shingles = ShinglingUtil.createShinglesFromFeatures(features);

            // Step 4: Generate MinHash signature
            int[] signature = minHashGenerator.computeSignatureFromStrings(shingles);

            // Step 5: Create DTO with multiple formats
            APISignatureDTO dto = new APISignatureDTO();
            dto.setApiUuid(apiUuid);
            dto.setOrganization(organization);
            dto.setSignatureArray(signature);
            dto.setSignatureBlob(MinHashGenerator.signatureToBytes(signature));
            dto.setSignatureBase64(Base64.getEncoder().encodeToString(dto.getSignatureBlob()));
            dto.setNumHashFunctions(numHashFunctions);
            dto.setFeatureCount(features.size());
            dto.setShingleCount(shingles.size());

            if (log.isDebugEnabled()) {
                log.debug("Generated signature for API " + apiUuid + ": " + 
                        features.size() + " features, " + shingles.size() + " shingles");
            }

            return dto;

        } catch (APIMGovernanceException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error generating signature for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    /**
     * Generates signature from pre-extracted features.
     *
     * @param features     List of feature strings
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @return APISignatureDTO
     */
    public APISignatureDTO generateSignatureFromFeatures(List<String> features, 
                                                          String apiUuid, String organization) {
        Set<String> shingles = ShinglingUtil.createShinglesFromFeatures(features);
        int[] signature = minHashGenerator.computeSignatureFromStrings(shingles);

        APISignatureDTO dto = new APISignatureDTO();
        dto.setApiUuid(apiUuid);
        dto.setOrganization(organization);
        dto.setSignatureArray(signature);
        dto.setSignatureBlob(MinHashGenerator.signatureToBytes(signature));
        dto.setSignatureBase64(Base64.getEncoder().encodeToString(dto.getSignatureBlob()));
        dto.setNumHashFunctions(numHashFunctions);
        dto.setFeatureCount(features.size());
        dto.setShingleCount(shingles.size());

        return dto;
    }

    /**
     * Converts a signature DTO to JSON format for AI plane transmission.
     *
     * @param signatureDTO The signature DTO
     * @return JSON string representation
     * @throws APIMGovernanceException If serialization fails
     */
    public String toJson(APISignatureDTO signatureDTO) throws APIMGovernanceException {
        try {
            return objectMapper.writeValueAsString(signatureDTO);
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException("Failed to serialize signature to JSON", e);
        }
    }

    /**
     * Parses a signature from JSON format.
     *
     * @param json The JSON string
     * @return APISignatureDTO
     * @throws APIMGovernanceException If parsing fails
     */
    public APISignatureDTO fromJson(String json) throws APIMGovernanceException {
        try {
            return objectMapper.readValue(json, APISignatureDTO.class);
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException("Failed to parse signature from JSON", e);
        }
    }

    /**
     * Estimates similarity between two signatures.
     *
     * @param sig1 First signature
     * @param sig2 Second signature
     * @return Estimated Jaccard similarity (0.0 to 1.0)
     */
    public double estimateSimilarity(int[] sig1, int[] sig2) {
        return minHashGenerator.estimateSimilarity(sig1, sig2);
    }

    /**
     * Gets the MinHash generator instance.
     *
     * @return MinHashGenerator
     */
    public MinHashGenerator getMinHashGenerator() {
        return minHashGenerator;
    }

    /**
     * DTO for API signatures with multiple format representations.
     * Designed for serialization to AI Remote Plane.
     */
    public static class APISignatureDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("apiUuid")
        private String apiUuid;

        @JsonProperty("organization")
        private String organization;

        @JsonProperty("signatureArray")
        private int[] signatureArray;

        @JsonProperty("signatureBase64")
        private String signatureBase64;

        @JsonProperty("numHashFunctions")
        private int numHashFunctions;

        @JsonProperty("featureCount")
        private int featureCount;

        @JsonProperty("shingleCount")
        private int shingleCount;

        // Transient - not serialized to JSON
        private transient byte[] signatureBlob;

        public String getApiUuid() {
            return apiUuid;
        }

        public void setApiUuid(String apiUuid) {
            this.apiUuid = apiUuid;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public int[] getSignatureArray() {
            return signatureArray != null ? signatureArray.clone() : null;
        }

        public void setSignatureArray(int[] signatureArray) {
            this.signatureArray = signatureArray != null ? signatureArray.clone() : null;
        }

        public String getSignatureBase64() {
            return signatureBase64;
        }

        public void setSignatureBase64(String signatureBase64) {
            this.signatureBase64 = signatureBase64;
        }

        public byte[] getSignatureBlob() {
            return signatureBlob != null ? signatureBlob.clone() : null;
        }

        public void setSignatureBlob(byte[] signatureBlob) {
            this.signatureBlob = signatureBlob != null ? signatureBlob.clone() : null;
        }

        public int getNumHashFunctions() {
            return numHashFunctions;
        }

        public void setNumHashFunctions(int numHashFunctions) {
            this.numHashFunctions = numHashFunctions;
        }

        public int getFeatureCount() {
            return featureCount;
        }

        public void setFeatureCount(int featureCount) {
            this.featureCount = featureCount;
        }

        public int getShingleCount() {
            return shingleCount;
        }

        public void setShingleCount(int shingleCount) {
            this.shingleCount = shingleCount;
        }

        @Override
        public String toString() {
            return "APISignatureDTO{" +
                    "apiUuid='" + apiUuid + '\'' +
                    ", organization='" + organization + '\'' +
                    ", numHashFunctions=" + numHashFunctions +
                    ", featureCount=" + featureCount +
                    ", shingleCount=" + shingleCount +
                    '}';
        }
    }
}
