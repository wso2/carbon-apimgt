/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package org.wso2.carbon.apimgt.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

/**
 * Azure OpenAI LLM Provider Service.
 */
@Component(
        name = "azureOpenAiV2.llm.provider.service",
        immediate = true,
        service = LLMProviderService.class
)
public class AzureOpenAiLLMProviderServiceV2 extends BuiltInLLMProviderService {

    @Override
    public String getType() {

        return APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_CONNECTOR_V2;
    }

    @Override
    public LLMProvider getLLMProvider()
            throws APIManagementException {

        log.debug("Initializing Azure OpenAI AI Service Provider Version: " + this.getType());
        try {
            LLMProvider llmProvider = new LLMProvider();
            llmProvider.setName(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_NAME);
            llmProvider.setApiVersion(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_VERSION_V2);
            llmProvider.setDescription(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_DESCRIPTION);
            llmProvider.setBuiltInSupport(true);

            llmProvider.setApiDefinition(readApiDefinition("repository" + File.separator + "resources"
                    + File.separator + "api_definitions" + File.separator
                    + APIConstants.AIAPIConstants
                    .LLM_PROVIDER_SERVICE_AZURE_OPENAI_API_DEFINITION_FILE_NAME_V2));

            LLMProviderConfiguration llmProviderConfiguration = new LLMProviderConfiguration();
            LLMProviderAuthenticationConfiguration llmProviderAuthenticationConfiguration =
                    getLlmProviderAuthenticationConfiguration();
            llmProviderConfiguration.setAuthenticationConfiguration(llmProviderAuthenticationConfiguration);
            llmProviderConfiguration.setAuthHeader(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_KEY);
            llmProviderConfiguration.setAuthQueryParam(null);
            llmProviderConfiguration.setConnectorType(this.getType());

            List<LLMProviderMetadata> llmProviderMetadata = new ArrayList<>();
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REQUEST_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_MODEL, false));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_RESPONSE_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_MODEL, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_PROMPT_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_INPUT_TOKEN, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_OUTPUT_TOKEN, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REMAINING_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_HEADER,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_REMAINING_TOKEN_COUNT, false));
            llmProviderConfiguration.setMetadata(llmProviderMetadata);

            llmProvider.setConfigurations(llmProviderConfiguration.toJsonString());
            log.debug("Successfully configured Azure OpenAI LLM Provider Version: " + this.getType());
            return llmProvider;
        } catch (Exception e) {
            log.error("Error occurred when registering LLM Provider: " + this.getType());
            throw new APIManagementException("Error occurred when registering LLM Provider: " + this.getType(), e);
        }
    }

    /**
     * Get LLM Provider Authentication Configuration.
     * @return LLMProviderAuthenticationConfiguration
     */
    private static LLMProviderAuthenticationConfiguration getLlmProviderAuthenticationConfiguration() {
        LLMProviderAuthenticationConfiguration llmProviderAuthenticationConfiguration =
                new LLMProviderAuthenticationConfiguration();
        llmProviderAuthenticationConfiguration.setType(APIConstants.AIAPIConstants.API_KEY_AUTHENTICATION_TYPE);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(APIConstants.AIAPIConstants.API_KEY_HEADER_NAME,
                APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_KEY);
        parameters.put(APIConstants.AIAPIConstants.API_KEY_HEADER_ENABLED, true);
        parameters.put(APIConstants.AIAPIConstants.API_KEY_QUERY_PARAMETER_ENABLED, false);
        llmProviderAuthenticationConfiguration.setParameters(parameters);
        llmProviderAuthenticationConfiguration.setEnabled(true);
        return llmProviderAuthenticationConfiguration;
    }
}
