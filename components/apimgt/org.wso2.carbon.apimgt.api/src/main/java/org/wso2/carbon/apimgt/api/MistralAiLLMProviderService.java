/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Arrays;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.LLMModel;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mistral AI LLM Provider Service.
 */
@Component(
        name = "mistralAi.llm.provider.service",
        immediate = true,
        service = LLMProviderService.class
)
public class MistralAiLLMProviderService extends BuiltInLLMProviderService {

    @Override
    public String getType() {

        return APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_MISTRALAI_CONNECTOR;
    }

    @Override
    public LLMProvider getLLMProvider()
            throws APIManagementException {

        try {
            LLMProvider llmProvider = new LLMProvider();
            llmProvider.setName(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_MISTRALAI_NAME);
            llmProvider.setApiVersion(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_MISTRALAI_VERSION);
            llmProvider.setDescription(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_MISTRALAI_DESCRIPTION);
            llmProvider.setBuiltInSupport(true);

            llmProvider.setApiDefinition(readApiDefinition("repository" + File.separator + "resources"
                    + File.separator + "api_definitions" + File.separator
                    + APIConstants.AIAPIConstants
                    .LLM_PROVIDER_SERVICE_MISTRALAI_API_DEFINITION_FILE_NAME));

            LLMProviderConfiguration llmProviderConfiguration = new LLMProviderConfiguration();
            llmProviderConfiguration.setAuthHeader(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_MISTRALAI_KEY);
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
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_PROMPT_TOKEN_COUNT, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_COMPLETION_TOKEN_COUNT, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REMAINING_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_HEADER,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_REMAINING_TOKEN_COUNT, false));
            llmProviderConfiguration.setMetadata(llmProviderMetadata);

            // Set default model List
            List<LLMModel> modelList = new ArrayList<>();
            modelList.add(new LLMModel(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_MISTRALAI_NAME,
                    Arrays.asList("mistral-small-latest", "mistral-medium", "open-mistral-7b")));
            llmProvider.setModelList(modelList);

            llmProvider.setConfigurations(llmProviderConfiguration.toJsonString());
            return llmProvider;
        } catch (Exception e) {
            throw new APIManagementException("Error occurred when registering LLM Provider:" + this.getType());
        }
    }
}
