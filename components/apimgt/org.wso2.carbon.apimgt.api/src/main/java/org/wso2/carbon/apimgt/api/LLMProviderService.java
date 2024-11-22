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

package org.wso2.carbon.apimgt.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

import java.util.List;
import java.util.Map;

/**
 * Interface for handling payloads specific to LLMs in the API Manager.
 */
public interface LLMProviderService {

    Log log = LogFactory.getLog(LLMProviderService.class);

    /**
     * Extracts response metadata from the payload, headers, or query parameters.
     *
     * @param payload     The response payload.
     * @param header      The response headers.
     * @param queryParams The query parameters.
     * @param metadata    The list of metadata to extract.
     * @param metadataMap Map of metadata
     * @return Map of extracted response metadata.
     * @throws APIManagementException If extraction fails.
     */
    Map<String, String> getResponseMetadata(String payload, Map<String, String> header,
                                            Map<String, String> queryParams, List<LLMProviderMetadata> metadata,
                                            Map<String, String> metadataMap)
            throws APIManagementException;

    /**
     * Extracts request metadata from the payload, headers, or query parameters.
     *
     * @param payload     The request payload.
     * @param header      The request headers.
     * @param queryParams The query parameters.
     * @param metadata    The list of metadata to extract.
     * @param metadataMap Map of metadata
     * @return Map of extracted request metadata.
     * @throws APIManagementException If extraction fails.
     */
    Map<String, String> getRequestMetadata(String payload, Map<String, String> header,
                                           Map<String, String> queryParams, List<LLMProviderMetadata> metadata,
                                           Map<String, String> metadataMap)
            throws APIManagementException;

    /**
     * Retrieves the type of the provider.
     *
     * @return The type as a String.
     */
    String getType();

    /**
     * Registers a new LLM Provider for the given organization.
     *
     * @return The registered LLM Provider.
     * @throws APIManagementException If registration fails.
     */
    LLMProvider getLLMProvider() throws APIManagementException;
}
