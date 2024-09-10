package org.wso2.carbon.apimgt.api;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI LLM Provider Service.
 */
@Component(
        name = "openAi.llm.provider.service",
        immediate = true,
        service = LLMProviderService.class
)
public class OpenAiLLMProviderService extends BuiltInLLMProviderService {

    @Override
    public String getType() {

        return APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_CONNECTOR;
    }

    @Override
    public LLMProvider registerLLMProvider(String organization, String apiDefinitionFilePath)
            throws APIManagementException {

        try {
            LLMProvider llmProvider = new LLMProvider();
            llmProvider.setName(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_NAME);
            llmProvider.setApiVersion(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_VERSION);
            llmProvider.setOrganization(organization);
            llmProvider.setDescription(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_DESCRIPTION);
            llmProvider.setBuiltInSupport(true);

            llmProvider.setApiDefinition(readApiDefinition(
                    apiDefinitionFilePath
                            + APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_API_DEFINITION_FILE_NAME));

            LLMProviderConfiguration llmProviderConfiguration = new LLMProviderConfiguration();
            List<String> additionalHeader = new ArrayList<>();
            additionalHeader.add(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_KEY);
            llmProviderConfiguration.setAdditionalHeaders(additionalHeader);
            llmProviderConfiguration.setConnectorType(this.getType());
            List<String> additionalQueryParameters = new ArrayList<>();
            llmProviderConfiguration.setAdditionalQueryParameters(additionalQueryParameters);

            List<LLMProviderMetadata> llmProviderMetadata = new ArrayList<>();
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_MODEL));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_PROMPT_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_PROMPT_TOKEN_COUNT));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants
                            .LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_COMPLETION_TOKEN_COUNT));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT));
            llmProviderConfiguration.setMetadata(llmProviderMetadata);

            llmProvider.setConfigurations(llmProviderConfiguration.toJsonString());
            return llmProvider;
        } catch (Exception e) {
            throw new APIManagementException("Error occurred when registering LLM Provider:" + this.getType());
        }
    }
}
