/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.EmbeddingProviderService;
import org.wso2.carbon.apimgt.api.dto.EmbeddingProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * OpenAI Embedding Provider Service.
 * This service interacts with the OpenAI API to generate embeddings for given input text.
 */
public class OpenAIEmbeddingProviderServiceImpl implements EmbeddingProviderService {
    private HttpClient httpClient;
    private String openAiApiKey;
    private String endpointUrl;
    private String model;

    private long retrievalTimeout;
    private int maxRetryCount;
    private double retryProgressionFactor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(EmbeddingProviderConfigurationDTO providerConfig) throws APIManagementException {
        openAiApiKey = providerConfig.getProperties().get(APIConstants.AI.EMBEDDING_PROVIDER_API_KEY);
        endpointUrl = providerConfig.getProperties().get(APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_ENDPOINT);
        model = providerConfig.getProperties().get(APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_MODEL);

        if (openAiApiKey == null || endpointUrl == null || model == null) {
            throw new APIManagementException(
                    "Missing required OpenAI configuration: 'apikey', 'embedding_endpoint', or 'embedding_model'");
        }

        // Retry parameters
        try {
            retrievalTimeout = Long.parseLong(providerConfig.getProperties()
                    .getOrDefault(APIConstants.AI.RETRIEVAL_TIMEOUT, APIConstants.AI.DEFAULT_RETRIEVAL_TIMEOUT));
            maxRetryCount = Integer.parseInt(providerConfig.getProperties()
                    .getOrDefault(APIConstants.AI.RETRY_COUNT, APIConstants.AI.DEFAULT_RETRY_COUNT));
            retryProgressionFactor = Double.parseDouble(providerConfig.getProperties()
                    .getOrDefault(APIConstants.AI.RETRY_PROGRESSION_FACTOR,
                            APIConstants.AI.DEFAULT_RETRY_PROGRESSION_FACTOR));
        } catch (NumberFormatException e) {
            throw new APIManagementException("Invalid retry configuration provided: " +
                    "'retrieval_timeout', 'retry_count', 'retry_progression_factor'");
        }

        httpClient = APIUtil.getHttpClient(endpointUrl);
    }

    @Override
    public String getType() {
        return APIConstants.AI.OPENAI_EMBEDDING_PROVIDER_TYPE;
    }

    @Override
    public int getEmbeddingDimension() throws APIManagementException {
        return getEmbedding(getType()).length;
    }

    @Override
    public double[] getEmbedding(String input) throws APIManagementException {
        HttpPost post = new HttpPost(endpointUrl);
        post.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                APIConstants.AUTHORIZATION_BEARER + openAiApiKey);
        post.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        try {
            // Build request JSON
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put(APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_REQUEST_MODEL, model);
            requestBody.put(APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_REQUEST_INPUT, input);
            String json = objectMapper.writeValueAsString(requestBody);
            post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(
                    post, httpClient, retrievalTimeout, maxRetryCount, retryProgressionFactor)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode == HttpStatus.SC_OK) {
                    JsonNode root = objectMapper.readTree(responseBody);
                    JsonNode embeddingArray = root.at(APIConstants.AI.EMBEDDING_PROVIDER_RESPONSE_EMBEDDING_PATH);

                    if (embeddingArray.isMissingNode() || !embeddingArray.isArray()) {
                        throw new APIManagementException(
                                "Missing or invalid 'embedding' array in response: " + responseBody);
                    }

                    double[] embedding = new double[embeddingArray.size()];
                    for (int i = 0; i < embedding.length; i++) {
                        embedding[i] = embeddingArray.get(i).asDouble();
                    }
                    return embedding;
                } else {
                    throw new APIManagementException("Unexpected response code " + statusCode + ": " + responseBody);
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error occurred while generating embedding", e);
        }
    }
}
