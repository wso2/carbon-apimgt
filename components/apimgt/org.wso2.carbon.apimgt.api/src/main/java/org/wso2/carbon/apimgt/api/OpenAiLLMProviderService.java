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

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

import java.io.File;
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
    public LLMProvider getLLMProvider()
            throws APIManagementException {

        try {
            LLMProvider llmProvider = new LLMProvider();
            llmProvider.setName(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_NAME);
            llmProvider.setApiVersion(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_VERSION);
            llmProvider.setDescription(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_DESCRIPTION);
            llmProvider.setBuiltInSupport(true);

            llmProvider.setApiDefinition(readApiDefinition("repository" + File.separator + "resources"
                    + File.separator + "api_definitions" + File.separator
                    + APIConstants.AIAPIConstants
                    .LLM_PROVIDER_SERVICE_OPENAI_API_DEFINITION_FILE_NAME));

            LLMProviderConfiguration llmProviderConfiguration = new LLMProviderConfiguration();
            llmProviderConfiguration.setAuthHeader(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_OPENAI_KEY);
            llmProviderConfiguration.setAuthQueryParam(null);
            llmProviderConfiguration.setConnectorType(this.getType());

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
