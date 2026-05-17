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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.EmbeddingProviderService;
import org.wso2.carbon.apimgt.api.ManagedIdentityTokenProvider;
import org.wso2.carbon.apimgt.api.dto.EmbeddingProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Azure OpenAI Embedding Provider Service.
 * This service interacts with the Azure OpenAI API to generate embeddings for given input text.
 * Supports both API-key authentication and Azure Workload Identity (UMI) authentication.
 */
public class AzureOpenAIEmbeddingProviderServiceImpl implements EmbeddingProviderService {
    private HttpClient httpClient;
    private String azureApiKey;
    // e.g., https://<resource>.openai.azure.com/openai/deployments/<dep-id>/embeddings?api-version=2024-02-15-preview
    private String endpointUrl;

    private long retrievalTimeout;
    private int maxRetryCount;
    private double retryProgressionFactor;

    /** Non-null when {@code auth_type=umi} is configured; null for API-key auth. */
    private ManagedIdentityTokenProvider umiTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(EmbeddingProviderConfigurationDTO providerConfig) throws APIManagementException {
        endpointUrl = providerConfig.getProperties().get(APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_ENDPOINT);

        if (StringUtils.isEmpty(endpointUrl)) {
            throw new APIManagementException("Missing required properties: "
                    + APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_ENDPOINT);
        }

        String authType = providerConfig.getProperties().getOrDefault(
                APIConstants.AI.AUTH_TYPE, APIConstants.AI.AUTH_TYPE_API_KEY);

        if (APIConstants.AI.AUTH_TYPE_UMI.equalsIgnoreCase(authType)) {
            umiTokenProvider = new AzureUmiTokenProvider();
            // *.openai.azure.com requires cognitiveservices.azure.com scope.
            // Override via umi_scope in provider properties if needed.
            String scope = providerConfig.getProperties().getOrDefault(
                    APIConstants.AI.AZURE_UMI_SCOPE_KEY,
                    APIConstants.AI.AZURE_UMI_COGNITIVE_SERVICES_SCOPE);
            umiTokenProvider.init(Collections.singletonMap(APIConstants.AI.AZURE_UMI_SCOPE_KEY, scope));
            azureApiKey = null;
        } else {
            azureApiKey = providerConfig.getProperties().get(APIConstants.AI.EMBEDDING_PROVIDER_API_KEY);
            if (StringUtils.isEmpty(azureApiKey)) {
                throw new APIManagementException("Missing required properties: "
                        + APIConstants.AI.EMBEDDING_PROVIDER_API_KEY);
            }
            umiTokenProvider = null;
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
            throw new APIManagementException("Failed to parse retry configuration: " + e.getMessage(), e);
        }

        this.httpClient = APIUtil.getHttpClient(endpointUrl);
    }

    @Override
    public String getType() {
        return APIConstants.AI.AZURE_OPENAI_EMBEDDING_PROVIDER_TYPE;
    }

    @Override
    public int getEmbeddingDimension() throws APIManagementException {
        return getEmbedding(getType()).length;
    }

    @Override
    public double[] getEmbedding(String input) throws APIManagementException {
        HttpPost httpPostRequest = new HttpPost(endpointUrl);
        if (umiTokenProvider != null) {
            httpPostRequest.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                    APIConstants.AUTHORIZATION_BEARER + umiTokenProvider.getAccessToken());
        } else {
            httpPostRequest.setHeader(APIConstants.API_KEY_AUTH, azureApiKey);
        }
        httpPostRequest.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put(APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_REQUEST_INPUT, input);
            String json = objectMapper.writeValueAsString(requestBody);
            httpPostRequest.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(
                    httpPostRequest, httpClient, retrievalTimeout, maxRetryCount, retryProgressionFactor)) {
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
