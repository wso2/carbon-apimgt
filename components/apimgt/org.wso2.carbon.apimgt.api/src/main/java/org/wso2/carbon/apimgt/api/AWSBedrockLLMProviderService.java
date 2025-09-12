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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.LLMModel;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

/**
 * AWS Bedrock LLM Provider Service.
 */
@Component(
        name = "aws.llm.provider.service",
        immediate = true,
        service = LLMProviderService.class
)
public class AWSBedrockLLMProviderService extends BuiltInLLMProviderService {

    private static final Log log = LogFactory.getLog(AWSBedrockLLMProviderService.class);

    @Override
    public String getType() {

        return APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_BEDROCK_CONNECTOR;
    }

    @Override
    public LLMProvider getLLMProvider()
            throws APIManagementException {

        log.debug("Initializing AWS Bedrock LLM Provider");
        try {
            LLMProvider llmProvider = new LLMProvider();
            llmProvider.setName(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_BEDROCK_NAME);
            llmProvider.setApiVersion(APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_BEDROCK_VERSION);
            llmProvider.setDescription(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_BEDROCK_DESCRIPTION);
            llmProvider.setBuiltInSupport(true);
            llmProvider.setMultipleVendorSupport(true);
            llmProvider.setApiDefinition(readApiDefinition("repository" + File.separator + "resources"
                    + File.separator + "api_definitions" + File.separator
                    + APIConstants.AIAPIConstants
                    .LLM_PROVIDER_SERVICE_AWSBEDROCK_OPENAI_API_DEFINITION_FILE_NAME));

            LLMProviderConfiguration llmProviderConfiguration = new LLMProviderConfiguration();
            LLMProviderAuthenticationConfiguration llmProviderAuthenticationConfiguration =
                    getLlmProviderAuthenticationConfiguration();
            llmProviderConfiguration.setAuthenticationConfiguration(llmProviderAuthenticationConfiguration);
            llmProviderConfiguration.setConnectorType(this.getType());

            List<LLMProviderMetadata> llmProviderMetadata = new ArrayList<>();
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REQUEST_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PATH,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_BEDROCK_METADATA_IDENTIFIER_MODEL, false));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_RESPONSE_MODEL,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PATH,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_BEDROCK_METADATA_IDENTIFIER_MODEL, false));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_PROMPT_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_METADATA_IDENTIFIER_PROMPT_TOKEN_COUNT, true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_METADATA_IDENTIFIER_COMPLETION_TOKEN_COUNT,
                    true));
            llmProviderMetadata.add(new LLMProviderMetadata(
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT,
                    APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                    APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AWS_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT, true));
            llmProviderConfiguration.setMetadata(llmProviderMetadata);

            // Set default model List
            List<LLMModel> modelList = new ArrayList<>();
            modelList.add(new LLMModel(APIConstants.AIAPIConstants.LLM_MODEL_PROVIDER_AWS_BEDROCK_ANTHROPIC,
                    Arrays.asList("us.anthropic.claude-3-5-sonnet-20240620-v1:0",
                            "us.anthropic.claude-sonnet-4-20250514-v1:0")));
            modelList.add(new LLMModel(APIConstants.AIAPIConstants.LLM_MODEL_PROVIDER_AWS_BEDROCK_DEEPSEEK,
                    Arrays.asList("us.deepseek.r1-v1:0")));
            modelList.add(new LLMModel(APIConstants.AIAPIConstants.LLM_MODEL_PROVIDER_AWS_BEDROCK_META,
                    Arrays.asList("us.meta.llama3-3-70b-instruct-v1:0", "us.meta.llama4-maverick-17b-instruct-v1:0")));
            llmProvider.setModelList(modelList);

            llmProvider.setConfigurations(llmProviderConfiguration.toJsonString());
            log.debug("Successfully configured AWS Bedrock LLM Provider");
            return llmProvider;
        } catch (Exception e) {
            log.error("Error occurred when registering LLM Provider: " + this.getType(), e);
            throw new APIManagementException("Error occurred when registering LLM Provider: " + this.getType(), e);
        }
    }

    private static LLMProviderAuthenticationConfiguration getLlmProviderAuthenticationConfiguration() {
        LLMProviderAuthenticationConfiguration llmProviderAuthenticationConfiguration =
                new LLMProviderAuthenticationConfiguration();
        llmProviderAuthenticationConfiguration.setEnabled(true);
        llmProviderAuthenticationConfiguration.setType(APIConstants.ENDPOINT_SECURITY_TYPE_AWS);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("awsServiceName", APIConstants.LLM_PROVIDER_SERVICE_AWS_BEDROCK_SERVICE_NAME);
        llmProviderAuthenticationConfiguration.setParameters(parameters);
        return llmProviderAuthenticationConfiguration;
    }

}
