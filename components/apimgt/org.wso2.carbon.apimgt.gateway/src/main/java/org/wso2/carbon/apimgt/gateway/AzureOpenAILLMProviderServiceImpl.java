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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.AILLMProviderService;
import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.LLMProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Azure OpenAI LLM Provider Service.
 * This service interacts with the Azure OpenAI API to send chat completion requests.
 */
public class AzureOpenAILLMProviderServiceImpl implements AILLMProviderService {
    private static final Log log = LogFactory.getLog(AzureOpenAILLMProviderServiceImpl.class);
    private HttpClient httpClient;
    private String azureApiKey;
    private String endpointUrl;

    private long retrievalTimeout;
    private int maxRetryCount;
    private double retryProgressionFactor;

    private final Gson gson = new Gson();

    @Override
    public void init(LLMProviderConfigurationDTO providerConfig) throws APIManagementException {
        
        azureApiKey = providerConfig.getProperties().get(APIConstants.AI.LLM_PROVIDER_API_KEY);
        endpointUrl = providerConfig.getProperties().get(APIConstants.AI.LLM_PROVIDER_LLM_ENDPOINT);

        boolean isApiKeyMissing = StringUtils.isEmpty(this.azureApiKey);
        boolean isEndpointMissing = StringUtils.isEmpty(this.endpointUrl);

        if (isApiKeyMissing || isEndpointMissing) {
            StringBuilder missingPropertiesBuilder = new StringBuilder();
            if (isApiKeyMissing) {
                missingPropertiesBuilder.append(APIConstants.AI.LLM_PROVIDER_API_KEY).append(", ");
            }
            if (isEndpointMissing) {
                missingPropertiesBuilder.append(APIConstants.AI.LLM_PROVIDER_LLM_ENDPOINT).append(", ");
            }
            String missing = missingPropertiesBuilder.substring(0, missingPropertiesBuilder.length() - 2);
            throw new APIManagementException("Missing required properties: " + missing);
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
        
        return APIConstants.AI.AZURE_OPENAI_LLM_PROVIDER_TYPE;
    }

    @Override
    public String getChatCompletion(String systemPrompt, String userMessage) throws APIManagementException {
        
        HttpPost httpPostRequest = new HttpPost(endpointUrl);
        httpPostRequest.setHeader(AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_KEY, azureApiKey);
        httpPostRequest.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    APIConstants.AI.LLM_PROVIDER_MESSAGE_ROLE, APIConstants.AI.LLM_PROVIDER_MESSAGE_ROLE_SYSTEM,
                    APIConstants.AI.LLM_PROVIDER_MESSAGE_CONTENT, systemPrompt));
            messages.add(Map.of(
                    APIConstants.AI.LLM_PROVIDER_MESSAGE_ROLE, APIConstants.AI.LLM_PROVIDER_MESSAGE_ROLE_USER,
                    APIConstants.AI.LLM_PROVIDER_MESSAGE_CONTENT, userMessage));

            requestBody.put(APIConstants.AI.LLM_PROVIDER_REQUEST_MESSAGES, messages);
            String json = gson.toJson(requestBody);
            httpPostRequest.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(
                    httpPostRequest, httpClient, retrievalTimeout, maxRetryCount, retryProgressionFactor)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode == HttpStatus.SC_OK) {
                    Map<String, Object> root = gson.fromJson(responseBody,
                            new TypeToken<Map<String, Object>>(){}.getType());
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get(
                            APIConstants.AI.LLM_PROVIDER_RESPONSE_CHOICES);

                    if (choices == null || choices.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Response body: " + responseBody);
                        }
                        throw new APIManagementException("Missing 'choices' in Azure OpenAI LLM provider response");
                    }

                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get(
                            APIConstants.AI.LLM_PROVIDER_MESSAGE);
                    if (message == null || !message.containsKey(APIConstants.AI.LLM_PROVIDER_MESSAGE_CONTENT)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Response body: " + responseBody);
                        }
                        throw new APIManagementException("Missing 'content' in Azure OpenAI LLM provider response");
                    }

                    return (String) message.get(APIConstants.AI.LLM_PROVIDER_MESSAGE_CONTENT);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Azure OpenAI API returned status code: " + statusCode + ", Response body: " + responseBody);
                    }
                    throw new APIManagementException("Unexpected status code: " + statusCode);
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error occurred while sending chat completion request", e);
        }
    }
}
