/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a LLM (Large Language Model) Provider.
 */
public class LlmProvider implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id = null;
    private String name = null;
    private String description = null;
    private String version = null;
    private String organization = null;
    private String apiDefinition = null;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();
    private String totalTokensPath;
    private String promptTokensPath;
    private String completionTokensPath;
    private String modelPath;
    private String payloadHandler;
    private Boolean hasMetadataInPayload;

    /**
     * Gets the organization associated with this LLM Provider.
     *
     * @return the organization as a {@link String}
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Sets the organization associated with this LLM Provider.
     *
     * @param organization the organization to set
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * Checks if metadata is present in the payload.
     *
     * @return {@code true} if metadata is present, {@code false} otherwise
     */
    public Boolean hasMetadataInPayload() {
        return hasMetadataInPayload;
    }

    /**
     * Sets whether metadata is present in the payload.
     *
     * @param hasMetadataInPayload a {@link Boolean} indicating if metadata is present
     */
    public void setHasMetadataInPayload(Boolean hasMetadataInPayload) {
        this.hasMetadataInPayload = hasMetadataInPayload;
    }

    /**
     * Gets the payload handler.
     *
     * @return the payload handler as a {@link String}
     */
    public String getPayloadHandler() {
        return payloadHandler;
    }

    /**
     * Sets the payload handler.
     *
     * @param payloadHandler the payload handler to set
     */
    public void setPayloadHandler(String payloadHandler) {
        this.payloadHandler = payloadHandler;
    }

    /**
     * Gets the ID of the LLM Provider.
     *
     * @return the ID as a {@link String}
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the LLM Provider.
     *
     * @param id the ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the LLM Provider.
     *
     * @return the name as a {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the LLM Provider.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the version of the LLM Provider.
     *
     * @return the version as a {@link String}
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the LLM Provider.
     *
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the description of the LLM Provider.
     *
     * @return the description as a {@link String}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the LLM Provider.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the API definition of the LLM Provider.
     *
     * @return the API definition as a {@link String}
     */
    public String getApiDefinition() {
        return apiDefinition;
    }

    /**
     * Sets the API definition of the LLM Provider.
     *
     * @param apiDefinition the API definition to set
     */
    public void setApiDefinition(String apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    /**
     * Gets the headers associated with the LLM Provider.
     *
     * @return a {@link Map} of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Sets the headers associated with the LLM Provider.
     *
     * @param headers a {@link Map} of headers to set
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Gets the query parameters associated with the LLM Provider.
     *
     * @return a {@link Map} of query parameters
     */
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    /**
     * Sets the query parameters associated with the LLM Provider.
     *
     * @param queryParams a {@link Map} of query parameters to set
     */
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Gets the path to the total tokens.
     *
     * @return the total tokens path as a {@link String}
     */
    public String getTotalTokensPath() {
        return totalTokensPath;
    }

    /**
     * Sets the path to the total tokens.
     *
     * @param totalTokensPath the total tokens path to set
     */
    public void setTotalTokensPath(String totalTokensPath) {
        this.totalTokensPath = totalTokensPath;
    }

    /**
     * Gets the path to the prompt tokens.
     *
     * @return the prompt tokens path as a {@link String}
     */
    public String getPromptTokensPath() {
        return promptTokensPath;
    }

    /**
     * Sets the path to the prompt tokens.
     *
     * @param promptTokensPath the prompt tokens path to set
     */
    public void setPromptTokensPath(String promptTokensPath) {
        this.promptTokensPath = promptTokensPath;
    }

    /**
     * Gets the path to the completion tokens.
     *
     * @return the completion tokens path as a {@link String}
     */
    public String getCompletionTokensPath() {
        return completionTokensPath;
    }

    /**
     * Sets the path to the completion tokens.
     *
     * @param completionTokensPath the completion tokens path to set
     */
    public void setCompletionTokensPath(String completionTokensPath) {
        this.completionTokensPath = completionTokensPath;
    }

    /**
     * Gets the path to the model.
     *
     * @return the model path as a {@link String}
     */
    public String getModelPath() {
        return modelPath;
    }

    /**
     * Sets the path to the model.
     *
     * @param modelPath the model path to set
     */
    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }
}
