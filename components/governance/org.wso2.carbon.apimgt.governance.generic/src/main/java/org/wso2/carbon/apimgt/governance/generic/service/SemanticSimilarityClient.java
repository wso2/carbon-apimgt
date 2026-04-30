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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP client for the Phase 2 governance semantic comparison endpoint.
 * <p>
 * Reuses the Marketplace Assistant FastAPI infrastructure by calling the
 * {@code /governance/semantic-match} endpoint, which performs vector similarity
 * search against the Milvus collection without invoking the LLM chat pipeline.
 * <p>
 * This client is fail-open: if the service is unreachable or misconfigured,
 * it logs a warning and returns an empty result, allowing governance to proceed
 * without the semantic check.
 */
public class SemanticSimilarityClient {

    private static final Log log = LogFactory.getLog(SemanticSimilarityClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Path appended to the marketplace assistant base endpoint. */
    public static final String SEMANTIC_MATCH_RESOURCE = "/governance/semantic-match";

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Result of a single semantic match call.
     */
    public static class SemanticMatchResult {
        private final boolean matchesFound;
        private final int candidateCount;
        private final List<SemanticCandidate> topCandidates;

        public SemanticMatchResult(boolean matchesFound, int candidateCount,
                                   List<SemanticCandidate> topCandidates) {
            this.matchesFound = matchesFound;
            this.candidateCount = candidateCount;
            this.topCandidates = new ArrayList<>(topCandidates);
        }

        public boolean isMatchesFound() {
            return matchesFound;
        }

        public int getCandidateCount() {
            return candidateCount;
        }

        public List<SemanticCandidate> getTopCandidates() {
            return new ArrayList<>(topCandidates);
        }
    }

    /**
     * Represents one semantically similar API returned by the AI plane.
     */
    public static class SemanticCandidate {
        private final String apiName;
        private final String apiUuid;
        private final double similarityScore;
        private final String apiType;

        public SemanticCandidate(String apiName, String apiUuid,
                                 double similarityScore, String apiType) {
            this.apiName = apiName;
            this.apiUuid = apiUuid;
            this.similarityScore = similarityScore;
            this.apiType = apiType;
        }

        public String getApiName() {
            return apiName;
        }

        public String getApiUuid() {
            return apiUuid;
        }

        public double getSimilarityScore() {
            return similarityScore;
        }

        public String getApiType() {
            return apiType;
        }
    }

    // -----------------------------------------------------------------------
    // Core method
    // -----------------------------------------------------------------------

    /**
     * Calls the {@code /governance/semantic-match} endpoint on the AI plane and
     * returns a structured result.  Returns {@code null} (fail-open) if:
     * <ul>
     *   <li>The Marketplace Assistant feature is not configured / not enabled.</li>
     *   <li>The service is unreachable (IOException).</li>
     *   <li>The response cannot be parsed.</li>
     * </ul>
     *
     * @param draftApiContent Raw API specification content (OAS/YAML/JSON).
     * @param tenantDomain    Tenant domain used to scope the vector search.
     * @param limit           Maximum number of candidates to return.
     * @param threshold       Minimum cosine similarity score (0.0–1.0).
     * @return Parsed result, or {@code null} on fail-open conditions.
     */
    public SemanticMatchResult findSemanticNeighbors(String draftApiContent, String tenantDomain,
                                                     int limit, double threshold) {
        MarketplaceAssistantConfigurationDTO config = getConfiguration();
        if (config == null || !config.isEnabled()
                || (!config.isKeyProvided() && !config.isAuthTokenProvided())) {
            if (log.isDebugEnabled()) {
                log.debug("Marketplace Assistant not enabled or not configured "
                        + "— skipping semantic similarity check.");
            }
            return null;
        }

        String endpoint = config.getEndpoint();
        if (endpoint == null || endpoint.isEmpty()) {
            log.debug("Marketplace Assistant endpoint not set — skipping semantic similarity check.");
            return null;
        }

        try {
            String requestJson = buildRequestJson(draftApiContent, tenantDomain, limit, threshold);
            String responseBody = post(endpoint, config, requestJson);
            return parseResponse(responseBody);
        } catch (IOException e) {
            log.warn("SemanticSimilarityClient: could not reach AI service "
                    + "— semantic check skipped. " + e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("SemanticSimilarityClient: unexpected error during semantic check "
                    + "— skipping. " + e.getMessage(), e);
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private MarketplaceAssistantConfigurationDTO getConfiguration() {
        try {
            APIManagerConfiguration cfg = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration();
            if (cfg == null) {
                return null;
            }
            return cfg.getMarketplaceAssistantConfigurationDto();
        } catch (Exception e) {
            log.debug("Could not retrieve Marketplace Assistant configuration", e);
            return null;
        }
    }

    private String buildRequestJson(String draftApiContent, String tenantDomain,
                                    int limit, double threshold) throws IOException {
        java.util.LinkedHashMap<String, Object> requestBody = new java.util.LinkedHashMap<>();
        requestBody.put("draft_api_content", draftApiContent);
        requestBody.put("tenant_domain", tenantDomain);
        requestBody.put("limit", limit);
        requestBody.put("threshold", threshold);
        return MAPPER.writeValueAsString(requestBody);
    }

    private String post(String baseEndpoint, MarketplaceAssistantConfigurationDTO config,
                        String payload) throws IOException, APIManagementException {
        String fullUrl = baseEndpoint + SEMANTIC_MATCH_RESOURCE;
        HttpPost request = new HttpPost(fullUrl);

        // Attach auth header
        if (config.isKeyProvided()) {
            // OAuth2 token exchange via AccessTokenGenerator
            try {
                String token = APIUtil.invokeAIService(baseEndpoint,
                        config.getTokenEndpoint(), config.getKey(),
                        SEMANTIC_MATCH_RESOURCE, payload, null);
                // invokeAIService returned the body (for 201); we only needed its auth.
                // For the 200-returning semantic endpoint we make the call ourselves.
                // Fall through to manual call below but we've already got a result.
                // Note: if we reach here, invokeAIService made the call for us.
                // For endpoints that return 200 (not 201) we need to call directly.
                return token; // invokeAIService already returned the response body
            } catch (APIManagementException e) {
                // invokeAIService throws on non-201 — fall through to manual HTTP call
                if (log.isDebugEnabled()) {
                    log.debug("invokeAIService threw for semantic endpoint (expected for 200 response): "
                            + e.getMessage());
                }
            }
            // Manual call with bearer token
            try {
                String token = getTokenViaKey(config.getTokenEndpoint(), config.getKey());
                request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            } catch (Exception e) {
                log.warn("Could not obtain access token for semantic check: " + e.getMessage());
                throw new IOException("Token acquisition failed: " + e.getMessage(), e);
            }
        } else if (config.isAuthTokenProvided()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getAccessToken());
        }

        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        URL url = new URL(fullUrl);
        HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());

        try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(request, httpClient)) {
            int status = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());
            if (status == HttpStatus.SC_OK) {
                return body;
            }
            log.warn("Semantic match endpoint returned unexpected status " + status + ": " + body);
            return null;
        }
    }

    /**
     * Obtain a short-lived access token using client-credential key exchange.
     * Delegates to {@code AccessTokenGenerator} via reflection to avoid a hard
     * compile-time dependency on the private inner class in APIUtil.
     */
    private String getTokenViaKey(String tokenEndpoint, String key) throws APIManagementException {
        // Use the same AccessTokenGenerator that APIUtil uses internally.
        // It is package-private; access it via the public APIUtil.invokeAIService()
        // pathway is impractical for a 200-returning endpoint.  As a lightweight
        // workaround we reuse the client-credentials grant directly.
        org.apache.http.client.methods.HttpPost tokenRequest =
                new org.apache.http.client.methods.HttpPost(tokenEndpoint);
        tokenRequest.setHeader(HttpHeaders.CONTENT_TYPE,
                "application/x-www-form-urlencoded");
        String body = "grant_type=client_credentials&client_id=" + key;
        tokenRequest.setEntity(new StringEntity(body,
                ContentType.APPLICATION_FORM_URLENCODED));
        try {
            URL url = new URL(tokenEndpoint);
            HttpClient client = APIUtil.getHttpClient(url.getPort(), url.getProtocol());
            try (CloseableHttpResponse response =
                         APIUtil.executeHTTPRequestWithRetries(tokenRequest, client)) {
                String resp = EntityUtils.toString(response.getEntity());
                JsonNode node = MAPPER.readTree(resp);
                if (!node.has("access_token")) {
                    throw new APIManagementException(
                            "Token endpoint did not return access_token: " + resp);
                }
                return node.get("access_token").asText();
            }
        } catch (IOException e) {
            throw new APIManagementException(
                    "Failed to retrieve access token from " + tokenEndpoint, e);
        }
    }

    private SemanticMatchResult parseResponse(String responseBody) throws IOException {
        if (responseBody == null || responseBody.isEmpty()) {
            return new SemanticMatchResult(false, 0, new ArrayList<>());
        }
        JsonNode root = MAPPER.readTree(responseBody);
        boolean matchesFound = root.path("matches_found").asBoolean(false);
        int candidateCount = root.path("candidate_count").asInt(0);

        List<SemanticCandidate> candidates = new ArrayList<>();
        JsonNode topCandidatesNode = root.path("top_candidates");
        if (topCandidatesNode.isArray()) {
            for (JsonNode c : topCandidatesNode) {
                candidates.add(new SemanticCandidate(
                        c.path("api_name").asText("unknown"),
                        c.path("api_uuid").asText("unknown"),
                        c.path("similarity_score").asDouble(0.0),
                        c.path("api_type").asText(null)
                ));
            }
        }
        return new SemanticMatchResult(matchesFound, candidateCount, candidates);
    }
}
