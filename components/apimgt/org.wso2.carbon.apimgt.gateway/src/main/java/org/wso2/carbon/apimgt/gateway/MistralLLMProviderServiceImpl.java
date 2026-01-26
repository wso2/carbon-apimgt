/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.AILLMProviderService;
import org.wso2.carbon.apimgt.api.dto.LLMProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Mistral LLM Provider Service.
 * This service interacts with the Mistral API to send chat completion requests.
 */
public class MistralLLMProviderServiceImpl implements AILLMProviderService {
    private static final Log log = LogFactory.getLog(MistralLLMProviderServiceImpl.class);
    private HttpClient httpClient;
    private String mistralApiKey;
    private String endpointUrl;
    private String model;

    private long retrievalTimeout;
    private int maxRetryCount;
    private double retryProgressionFactor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(LLMProviderConfigurationDTO providerConfig) throws APIManagementException {
        mistralApiKey = providerConfig.getProperties().get(APIConstants.AI.LLM_PROVIDER_API_KEY);
        endpointUrl = providerConfig.getProperties().get(APIConstants.AI.LLM_PROVIDER_LLM_ENDPOINT);
        model = providerConfig.getProperties().get(APIConstants.AI.LLM_PROVIDER_LLM_MODEL);

        if (mistralApiKey == null || endpointUrl == null || model == null) {
            String errorMsg = "Missing required Mistral LLM configuration: 'apikey', 'llm_endpoint', or 'llm_model'";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg);
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
            String errorMsg = "Invalid retry configuration provided: 'retrieval_timeout', 'retry_count', 'retry_progression_factor'";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg);
        }

        httpClient = APIUtil.getHttpClient(endpointUrl);
    }

    @Override
    public String getType() {
        return APIConstants.AI.MISTRAL_LLM_PROVIDER_TYPE;
    }

    @Override
    public String getChatCompletion(String systemPrompt, String userMessage) throws APIManagementException {
        HttpPost post = new HttpPost(endpointUrl);
        post.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                APIConstants.AUTHORIZATION_BEARER + mistralApiKey);
        post.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        try {
            // Build request JSON
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put(APIConstants.AI.LLM_PROVIDER_REQUEST_MODEL, model);

            ArrayNode messagesArray = objectMapper.createArrayNode();

            // Add system message
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put(APIConstants.AI.LLM_PROVIDER_MESSAGE_ROLE, APIConstants.AI.LLM_PROVIDER_MESSAGE_ROLE_SYSTEM);
            systemMessage.put(APIConstants.AI.LLM_PROVIDER_MESSAGE_CONTENT, systemPrompt);
            messagesArray.add(systemMessage);

            // Add user message
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put(APIConstants.AI.LLM_PROVIDER_MESSAGE_ROLE, APIConstants.AI.LLM_PROVIDER_MESSAGE_ROLE_USER);
            userMsg.put(APIConstants.AI.LLM_PROVIDER_MESSAGE_CONTENT, userMessage);
            messagesArray.add(userMsg);

            requestBody.set(APIConstants.AI.LLM_PROVIDER_REQUEST_MESSAGES, messagesArray);
            String json = objectMapper.writeValueAsString(requestBody);
            post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(
                    post, httpClient, retrievalTimeout, maxRetryCount, retryProgressionFactor)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode == HttpStatus.SC_OK) {
                    JsonNode root = objectMapper.readTree(responseBody);
                    JsonNode contentNode = root.at(APIConstants.AI.LLM_PROVIDER_RESPONSE_CONTENT_PATH);

                    if (contentNode.isMissingNode()) {
                        String errorMsg = "Missing 'content' in response: " + responseBody;
                        log.error(errorMsg);
                        throw new APIManagementException(errorMsg);
                    }

                    return contentNode.asText();
                } else {
                    log.error("Mistral API returned unexpected status code: " + statusCode);
                    throw new APIManagementException("Unexpected status code " + statusCode + ": " + responseBody);
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error occurred while sending chat completion request", e);
        }
    }
}
