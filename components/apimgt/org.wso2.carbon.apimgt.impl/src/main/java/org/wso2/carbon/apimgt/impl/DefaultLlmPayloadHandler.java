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

package org.wso2.carbon.apimgt.impl;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LlmMetadata;

import java.util.Map;

/**
 * DefaultLLMPayloadHandler is the default implementation of the LLMPayloadHandler interface.
 * It handles the extraction of metadata from Large Language Model (LLM) payloads.
 */
public class DefaultLlmPayloadHandler implements LlmPayloadHandler {

    private String totalTokensPath;
    private String promptTokensPath;
    private String completionTokensPath;
    private String modelPath;
    private boolean metadataInPayload;

    /**
     * Initializes the configuration for extracting metadata paths and flags from LLM payloads.
     *
     * @param modelPath          The JSON path for the model.
     * @param requestTokensPath  The JSON path for request tokens.
     * @param responseTokensPath The JSON path for response tokens.
     * @param totalTokensPath    The JSON path for total tokens.
     * @param metadataInPayload  Flag indicating if metadata is present in the payload.
     */
    @Override
    public void initConfiguration(String modelPath, String requestTokensPath, String responseTokensPath, String totalTokensPath, boolean metadataInPayload) {

        if (modelPath == null || modelPath.isEmpty()) {
            log.error("Model path is null or empty.");
        } else {
            this.modelPath = modelPath;
        }

        if (requestTokensPath == null || requestTokensPath.isEmpty()) {
            log.error("Request tokens path is null or empty.");
        } else {
            this.promptTokensPath = requestTokensPath;
        }

        if (responseTokensPath == null || responseTokensPath.isEmpty()) {
            log.error("Response tokens path is null or empty.");
        } else {
            this.completionTokensPath = responseTokensPath;
        }

        if (totalTokensPath == null || totalTokensPath.isEmpty()) {
            log.error("Total tokens path is null or empty.");
        } else {
            this.totalTokensPath = totalTokensPath;
        }

        this.metadataInPayload = metadataInPayload;
    }

    /**
     * Extracts metadata from the LLM response payload.
     *
     * @param payload The LLM response payload as a JSON string.
     * @param headers The HTTP headers associated with the payload.
     * @return A populated LlmMetadata object with the extracted metadata.
     * @throws APIManagementException If an error occurs while parsing the payload.
     */
    @Override
    public LlmMetadata getResponseMetadata(String payload, Map<String, String> headers) throws APIManagementException {

        LlmMetadata llmMetadata = new LlmMetadata();
        ReadContext ctx = JsonPath.parse(payload);
        if (getModelPath() != null) {
            try {
                String model = ctx.read(getModelPath(), String.class);
                if (model != null) {
                    llmMetadata.setModel(model);
                }
            } catch (Exception e) {
                log.error("Error reading model from payload using model path: " + getModelPath(), e);
            }
        } else {
            log.error("Model path is not set. Unable to retrieve model from payload.");
        }

        if (getPromptTokensPath() != null) {
            try {
                String promptTokens = ctx.read(getPromptTokensPath(), String.class);
                if (promptTokens != null) {
                    llmMetadata.setPromptTokens(Integer.parseInt(promptTokens));
                }
            } catch (NumberFormatException e) {
                log.error("Invalid number format for prompt tokens.", e);
            } catch (Exception e) {
                log.error("Error reading prompt tokens from payload using path: " + getPromptTokensPath(), e);
            }
        } else {
            log.error("Prompt tokens path is not set. Unable to retrieve prompt tokens from payload.");
        }

        if (getCompletionTokensPath() != null) {
            try {
                String completionTokens = ctx.read(getCompletionTokensPath(), String.class);
                if (completionTokens != null) {
                    llmMetadata.setCompletionTokens(Integer.parseInt(completionTokens));
                }
            } catch (NumberFormatException e) {
                log.error("Invalid number format for completion tokens.", e);
            } catch (Exception e) {
                log.error("Error reading completion tokens from payload using path: " + getCompletionTokensPath(), e);
            }
        } else {
            log.error("Completion tokens path is not set. Unable to retrieve completion tokens from payload.");
        }

        if (getTotalTokensPath() != null) {
            try {
                String totalTokens = ctx.read(getTotalTokensPath(), String.class);
                if (totalTokens != null) {
                    llmMetadata.setTotalTokens(Integer.parseInt(totalTokens));
                }
            } catch (NumberFormatException e) {
                log.error("Invalid number format for total tokens.", e);
            } catch (Exception e) {
                log.error("Error reading total tokens from payload using path: " + getTotalTokensPath(), e);
            }
        } else {
            log.error("Total tokens path is not set. Unable to retrieve total tokens from payload.");
        }

        return llmMetadata;
    }

    /**
     * Extracts metadata from the LLM request payload.
     *
     * @param payload The LLM request payload as a JSON string.
     * @param headers The HTTP headers associated with the payload.
     * @return A populated LlmMetadata object with the extracted metadata.
     */
    @Override
    public LlmMetadata getRequestMetadata(String payload, Map<String, String> headers) {

        LlmMetadata llmMetadata = new LlmMetadata();
        ReadContext ctx = JsonPath.parse(payload);
        if (getModelPath() != null) {
            try {
                String model = ctx.read(getModelPath(), String.class);
                if (model != null) {
                    llmMetadata.setModel(model);
                }
            } catch (Exception e) {
                log.error("Error reading model from payload using model path: " + getModelPath(), e);
            }
        } else {
            log.error("Model path is not set. Unable to retrieve model from payload.");
        }

        return llmMetadata;
    }

    /**
     * Gets the JSON path for total tokens.
     *
     * @return The JSON path for total tokens.
     */
    public String getTotalTokensPath() {

        return totalTokensPath;
    }

    /**
     * Gets the JSON path for prompt tokens.
     *
     * @return The JSON path for prompt tokens.
     */
    public String getPromptTokensPath() {

        return promptTokensPath;
    }

    /**
     * Gets the JSON path for completion tokens.
     *
     * @return The JSON path for completion tokens.
     */
    public String getCompletionTokensPath() {

        return completionTokensPath;
    }

    /**
     * Gets the JSON path for the model.
     *
     * @return The JSON path for the model.
     */
    public String getModelPath() {

        return modelPath;
    }

    /**
     * Checks if metadata is present in the payload.
     *
     * @return True if metadata is in the payload, otherwise false.
     */
    public boolean hasMetadataInPayload() {

        return metadataInPayload;
    }

}
