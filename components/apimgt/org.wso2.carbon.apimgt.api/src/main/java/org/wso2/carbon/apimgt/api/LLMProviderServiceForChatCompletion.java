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

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.dto.LLMProviderConfigurationDTO;

/**
 * Interface for LLM provider services for chat completion.
 * This interface defines methods for initializing the provider, retrieving the type,
 * and sending chat completion requests to LLM providers.
 */
public interface LLMProviderServiceForChatCompletion {
    /**
     * Initialize the provider with required HTTP client and configuration properties.
     *
     * @param providerConfig the configuration DTO containing provider settings.
     * @throws APIManagementException if initialization fails.
     */
    void init(LLMProviderConfigurationDTO providerConfig) throws APIManagementException;

    /**
     * The type identifier for this provider (e.g., "openai", "azure-openai", "mistral").
     *
     * @return A unique string identifier.
     */
    String getType();

    /**
     * Sends a chat completion request to the LLM provider and returns the response.
     *
     * @param systemPrompt the system prompt to set the behavior of the assistant.
     * @param userMessage the user message to send to the LLM.
     * @return the response content string from the LLM provider.
     * @throws APIManagementException if an error occurs while sending the request.
     */
    String getChatCompletion(String systemPrompt, String userMessage) throws APIManagementException;
}
