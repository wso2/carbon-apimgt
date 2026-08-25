/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.LLMModel;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

/**
 * Google Vertex AI - Gemini LLM Provider Service.
 * <p>
 * Fronts Google's own Gemini models hosted on Vertex AI via the {@code publishers/google} model
 * resource and the {@code :generateContent} custom method. The response format is identical to the
 * plain Gemini API (token usage under {@code usageMetadata.*}); only the URL shape and authentication
 * (GCP service-account OAuth2 bearer token) differ. Auth is handled by the {@code gcp} endpoint
 * security type and the {@code GCPOAuth2TokenInjector} gateway mediator.
 */
@Component(
        name = "vertexAiGemini.llm.provider.service",
        immediate = true,
        service = LLMProviderService.class
)
public class VertexAIGeminiLLMProviderService extends BuiltInLLMProviderService {

    @Override
    public String getType() {

        return APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_VERTEX_AI_GEMINI_CONNECTOR;
    }

    @Override
    public LLMProvider getLLMProvider()
            throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Initializing Vertex AI - Gemini LLM Provider: " + this.getType());
        }
        try {
            LLMProvider llmProvider = new LLMProvider();
            llmProvider.setName(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_VERTEX_AI_GEMINI_NAME);
            llmProvider.setApiVersion(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_VERTEX_AI_VERSION);
            llmProvider.setDescription(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_VERTEX_AI_GEMINI_DESCRIPTION);
            llmProvider.setBuiltInSupport(true);

            llmProvider.setApiDefinition(readApiDefinition("repository" + File.separator + "resources"
                    + File.separator + "api_definitions" + File.separator
                    + APIConstants.AIAPIConstants
                    .LLM_PROVIDER_SERVICE_VERTEX_AI_GEMINI_API_DEFINITION_FILE_NAME));

            LLMProviderConfiguration llmProviderConfiguration = new LLMProviderConfiguration();
            llmProviderConfiguration.setAuthenticationConfiguration(getLlmProviderAuthenticationConfiguration());
            llmProviderConfiguration.setConnectorType(this.getType());

            List<LLMProviderMetadata> llmProviderMetadata = new ArrayList<>();
            // Model is carried in the URL path (.../models/{model}:generateContent) for both request and
            // response, since Vertex routes by path. Reuse the path regex for both.
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REQUEST_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PATH,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_VERTEX_AI_METADATA_IDENTIFIER_MODEL, false));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_RESPONSE_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PATH,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_VERTEX_AI_METADATA_IDENTIFIER_MODEL, false));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_PROMPT_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_PROMPT_TOKEN_COUNT,
                    true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_CANDIDATES_TOKEN_COUNT,
                    true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT,
                    false));
            llmProviderConfiguration.setMetadata(llmProviderMetadata);

            // The model-group name must equal the provider's own name: the admin UI populates the
            // models section by finding the model group whose name matches the provider name.
            List<LLMModel> modelList = new ArrayList<>();
            modelList.add(new LLMModel(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_VERTEX_AI_GEMINI_NAME,
                    Arrays.asList("gemini-2.5-flash-lite", "gemini-2.5-flash", "gemini-2.5-pro")));
            llmProvider.setModelList(modelList);

            llmProvider.setConfigurations(llmProviderConfiguration.toJsonString());
            if (log.isDebugEnabled()) {
                log.debug("Successfully configured Vertex AI - Gemini LLM Provider: " + this.getType());
            }
            return llmProvider;
        } catch (Exception e) {
            log.error("Error occurred when registering LLM Provider: " + this.getType());
            throw new APIManagementException("Error occurred when registering LLM Provider: " + this.getType(), e);
        }
    }

    /**
     * Builds the GCP OAuth2 authentication configuration. The service-account key itself is supplied
     * per-API as endpoint security; the vendor config only declares the auth type and the OAuth2 scope.
     *
     * @return LLMProviderAuthenticationConfiguration
     */
    private static LLMProviderAuthenticationConfiguration getLlmProviderAuthenticationConfiguration() {

        LLMProviderAuthenticationConfiguration llmProviderAuthenticationConfiguration =
                new LLMProviderAuthenticationConfiguration();
        llmProviderAuthenticationConfiguration.setEnabled(true);
        llmProviderAuthenticationConfiguration.setType(APIConstants.ENDPOINT_SECURITY_TYPE_GCP);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("scope", APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_VERTEX_AI_SCOPE);
        llmProviderAuthenticationConfiguration.setParameters(parameters);
        return llmProviderAuthenticationConfiguration;
    }
}
