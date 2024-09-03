/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LlmProvider;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that holds references to various LLM (Large Language Model) providers and their handlers.
 */
public class LlmProviderReferenceHolder {

    private static final Log log = LogFactory.getLog(LlmProviderReferenceHolder.class);

    private static Map<String, LlmPayloadHandler> providerMap;

    /**
     * Returns the {@link LlmPayloadHandler} associated with the given key.
     *
     * @param key The key of the LLM provider.
     * @return The corresponding {@link LlmPayloadHandler} or null if the key is not found.
     */
    public static LlmPayloadHandler getLLMPayloadHandler(String key) {
        if (providerMap.containsKey(key)) {
            return providerMap.get(key);
        } else {
            log.warn("LLM Provider key " + key + " not found");
            return null;
        }
    }

    /**
     * Initializes the LLM providers and their payload handlers.
     *
     * @throws APIManagementException if an error occurs while initializing the providers.
     */
    public static void initProviders() throws APIManagementException {
        providerMap = new HashMap<>();
        APIAdmin apiAdmin = new APIAdminImpl();
        List<LlmProvider> providers = apiAdmin.getLLMProviders();

        List<String> predefinedProviders = new ArrayList<>();
        predefinedProviders.add(APIConstants.AIAPI.PREDEFINED_PROVIDER_OPENAI);
        predefinedProviders.add(APIConstants.AIAPI.PREDEFINED_PROVIDER_MISTRAL);
        predefinedProviders.add(APIConstants.AIAPI.PREDEFINED_PROVIDER_AZURE_OPENAI);

        String filePath = CarbonUtils.getCarbonHome() + APIConstants.AIAPI.AI_API_DEFINITION_FILE_PATH;
        for (LlmProvider provider : providers) {
            try {
                String key = provider.getOrganization() + APIConstants.DELEM_UNDERSCORE + provider.getName() + APIConstants.DELEM_UNDERSCORE + provider.getVersion();
                String payloadHandlerClassName = provider.getPayloadHandler();
                Class<?> clazz = Class.forName(payloadHandlerClassName);
                LlmPayloadHandler handlerInstance = (LlmPayloadHandler) clazz.getDeclaredConstructor().newInstance();
                handlerInstance.initConfiguration(provider.getModelPath(), provider.getPromptTokensPath(),
                        provider.getCompletionTokensPath(), provider.getTotalTokensPath(),
                        provider.hasMetadataInPayload());
                providerMap.put(key, handlerInstance);
                predefinedProviders.remove(key);

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                log.error("Error occurred while initiating the LLM Provider Map", e);
                throw new APIManagementException(e);
            }
        }
        for (String providerKey : predefinedProviders) {
            String providerName = providerKey.split(APIConstants.DELEM_UNDERSCORE)[1];
            String yamlFile = getYamlFileForProvider(providerName, filePath);
            LlmProvider llmProvider = createLLMProvider(providerName, yamlFile,
                    getHeaderKeyForProvider(providerName), getHeaderValueForProvider(providerName));
            apiAdmin.addLlmProvider(llmProvider);
        }
    }

    /**
     * Returns the YAML file path for the specified provider name.
     *
     * @param providerName The name of the provider.
     * @param basePath     The base path where YAML files are located.
     * @return The YAML file path for the provider.
     */
    private static String getYamlFileForProvider(String providerName, String basePath) {
        switch (providerName) {
            case "OpenAI":
                return basePath + "openai_api.yaml";
            case "Mistral":
                return basePath + "mistral_api.yaml";
            case "AzureOpenAI":
                return basePath + "azure_api.yaml";
            default:
                throw new IllegalArgumentException("Unknown provider: " + providerName);
        }
    }

    /**
     * Returns the header key for the specified provider name.
     *
     * @param providerName The name of the provider.
     * @return The header key for the provider.
     */
    private static String getHeaderKeyForProvider(String providerName) {
        switch (providerName) {
            case "OpenAI":
                return "Authorization";
            case "Mistral":
                return "ApiKey";
            case "AzureOpenAI":
                return "api-key";
            default:
                return "";
        }
    }

    /**
     * Returns the header value for the specified provider name.
     *
     * @param providerName The name of the provider.
     * @return The header value for the provider.
     */
    private static String getHeaderValueForProvider(String providerName) {
        switch (providerName) {
            case "OpenAI":
            case "Mistral":
                return "Bearer ";
            case "AzureOpenAI":
                return "";
            default:
                return "";
        }
    }

    /**
     * Creates a new {@link LlmProvider} with the specified parameters.
     *
     * @param name             The name of the LLM provider.
     * @param apiDefinitionPath The path to the API definition YAML file.
     * @param headerKey        The header key for the provider.
     * @param headerValue      The header value for the provider.
     * @return The created {@link LlmProvider} instance.
     * @throws APIManagementException if an error occurs while creating the provider.
     */
    private static LlmProvider createLLMProvider(String name, String apiDefinitionPath,
                                                 String headerKey, String headerValue)
            throws APIManagementException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        LlmProvider llmProvider = new LlmProvider();
        llmProvider.setName(name);
        llmProvider.setVersion(APIConstants.AIAPI.AI_API_VERSION_1_0_0);
        llmProvider.setDescription(name + " LLM Provider");
        llmProvider.setOrganization(tenantDomain);
        llmProvider.setPayloadHandler(APIConstants.AIAPI.DEFAULT_PAYLOAD_HANDLER);
        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put(headerKey, headerValue);
        llmProvider.setHeaders(additionalHeaders);
        llmProvider.setHasMetadataInPayload(true);
        llmProvider.setModelPath(APIConstants.AIAPI.DEFAULT_MODEL_PATH);
        llmProvider.setPromptTokensPath(APIConstants.AIAPI.PROMPT_TOKENS_PATH);
        llmProvider.setCompletionTokensPath(APIConstants.AIAPI.COMPLETION_TOKENS_PATH);
        llmProvider.setTotalTokensPath(APIConstants.AIAPI.TOTAL_TOKENS_PATH);
        String apiDefinition = readApiDefinition(apiDefinitionPath);
        llmProvider.setApiDefinition(apiDefinition);
        return llmProvider;
    }

    /**
     * Reads the API definition from the specified file path.
     *
     * @param filePath The path to the API definition file.
     * @return The API definition as a string.
     * @throws APIManagementException if an error occurs while reading the file.
     */
    private static String readApiDefinition(String filePath) throws APIManagementException {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new APIManagementException(e);
        }
    }
}
