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
 * Gemini LLM Provider Service.
 */
@Component(
        name = "gemini.llm.provider.service",
        immediate = true,
        service = LLMProviderService.class
)
public class GeminiLLMProviderService extends BuiltInLLMProviderService {

    @Override
    public String getType() {

        return APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_CONNECTOR;
    }

    @Override
    public LLMProvider getLLMProvider()
            throws APIManagementException {

        log.debug("Initializing Gemini LLM Provider");
        try {
            LLMProvider llmProvider = new LLMProvider();
            llmProvider.setName(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_NAME);
            llmProvider.setApiVersion(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_VERSION);
            llmProvider.setDescription(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_DESCRIPTION);
            llmProvider.setBuiltInSupport(true);

            llmProvider.setApiDefinition(readApiDefinition("repository" + File.separator + "resources"
                    + File.separator + "api_definitions" + File.separator
                    + APIConstants.AIAPIConstants
                    .LLM_PROVIDER_SERVICE_GEMINI_API_DEFINITION_FILE_NAME));

            LLMProviderConfiguration llmProviderConfiguration = new LLMProviderConfiguration();
            llmProviderConfiguration.setAuthenticationConfiguration(getLlmProviderAuthenticationConfiguration());
            llmProviderConfiguration.setConnectorType(this.getType());

            List<LLMProviderMetadata> llmProviderMetadata = new ArrayList<>();
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REQUEST_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PATH,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_REQUEST_METADATA_IDENTIFIER_MODEL, false));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_RESPONSE_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_RESPONSE_METADATA_IDENTIFIER_MODEL, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_PROMPT_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_PROMPT_TOKEN_COUNT, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_CANDIDATES_TOKEN_COUNT, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT, false));
            llmProviderConfiguration.setMetadata(llmProviderMetadata);

            // Set default model List
            List<LLMModel> modelList = new ArrayList<>();
            modelList.add(new LLMModel(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_NAME,
                    Arrays.asList("gemini-2.5-flash-lite", "gemini-2.5-flash", "gemini-2.5-pro")));
            llmProvider.setModelList(modelList);

            llmProvider.setConfigurations(llmProviderConfiguration.toJsonString());
            log.debug("Successfully configured Gemini LLM Provider");
            return llmProvider;
        } catch (Exception e) {
            log.error("Error occurred when registering LLM Provider: " + this.getType());
            throw new APIManagementException("Error occurred when registering LLM Provider: " + this.getType(), e);
        }
    }

    private static LLMProviderAuthenticationConfiguration getLlmProviderAuthenticationConfiguration() {

        LLMProviderAuthenticationConfiguration llmProviderAuthenticationConfiguration =
                new LLMProviderAuthenticationConfiguration();
        llmProviderAuthenticationConfiguration.setType(APIConstants.AIAPIConstants.API_KEY_AUTHENTICATION_TYPE);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(APIConstants.AIAPIConstants.API_KEY_HEADER_NAME,
                APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_GEMINI_KEY);
        parameters.put(APIConstants.AIAPIConstants.API_KEY_HEADER_ENABLED, true);
        parameters.put(APIConstants.AIAPIConstants.API_KEY_QUERY_PARAMETER_ENABLED, false);
        llmProviderAuthenticationConfiguration.setParameters(parameters);
        llmProviderAuthenticationConfiguration.setEnabled(true);
        return llmProviderAuthenticationConfiguration;
    }
}
