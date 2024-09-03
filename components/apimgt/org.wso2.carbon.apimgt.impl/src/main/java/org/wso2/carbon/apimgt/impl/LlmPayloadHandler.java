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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LlmMetadata;

import java.util.Map;

/**
 * Interface for handling payloads specific to Large Language Models (LLMs) in the API Manager.
 */
public interface LlmPayloadHandler {

    Log log = LogFactory.getLog(LlmPayloadHandler.class);

    /**
     * Initializes the configuration for handling LLM payloads.
     *
     * @param modelPath            The path to the model file.
     * @param promptTokensPath     The path to the prompt tokens file.
     * @param completionTokensPath The path to the completion tokens file.
     * @param totalTokensPath      The path to the total tokens file.
     * @param metadataInPayload    Indicates whether metadata is included in the payload.
     */
    void initConfiguration(String modelPath, String promptTokensPath, String completionTokensPath, String totalTokensPath, boolean metadataInPayload);

    /**
     * Extracts metadata from the response payload.
     *
     * @param payload The response payload as a string.
     * @param headers A map of headers associated with the response.
     * @return The extracted metadata as an {@link LlmMetadata} object.
     * @throws APIManagementException If there is an error extracting the metadata.
     */
    LlmMetadata getResponseMetadata(String payload, Map<String, String> headers) throws APIManagementException;

    /**
     * Extracts metadata from the request payload.
     *
     * @param payload The request payload as a string.
     * @param headers A map of headers associated with the request.
     * @return The extracted metadata as an {@link LlmMetadata} object.
     */
    LlmMetadata getRequestMetadata(String payload, Map<String, String> headers);

    /**
     * Checks if metadata is included within the payload.
     *
     * @return {@code true} if metadata is included in the payload; {@code false} otherwise.
     */
    boolean hasMetadataInPayload();
}
