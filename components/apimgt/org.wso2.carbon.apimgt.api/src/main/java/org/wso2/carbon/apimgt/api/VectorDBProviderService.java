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

import org.wso2.carbon.apimgt.api.dto.VectorDBProviderConfigurationDTO;

import java.io.Serializable;
import java.util.Map;

/**
 * This interface defines the contract for a Vector Database Provider Service.
 * It provides methods to initialize the provider, create indexes, store responses,
 * and retrieve cached responses based on embeddings.
 */
public interface VectorDBProviderService {

    /**
     * Initialize the provider with required HTTP client and configuration properties.
     *
     */
    void init(VectorDBProviderConfigurationDTO providerConfig) throws APIManagementException;

    /**
     * The type identifier for this provider (e.g., "REDIS", "ZILLIZ").
     *
     * @return A unique string identifier.
     */
    String getType();

    /**
     * Creates a new index in the vector database with the given configuration.
     * @param config A map of provider-specific configuration values, such as index name, dimension, etc.
     */
    void createIndex(Map<String, String> config) throws APIManagementException;

    /**
     * Stores a response along with its embedding in the vector database.
     * @param embeddings the embedding vector to store.
     * @param response the response to store.
     * @param filter a map of filter criteria to apply when storing the response.
     * @throws APIManagementException if an error occurs during the storage operation.
     */
    <T extends Serializable> void store(double[] embeddings, T response, Map<String, String> filter) throws APIManagementException;

    /**
     * Retrieves the most relevant response from the vector database for the given embedding.
     *
     * @param embeddings The embedding to use for similarity search.
     * @return The most relevant cached response.
     * @throws APIManagementException if an error occurs while retrieving the response.
     */
    <T extends Serializable> T retrieve(double[] embeddings, Map<String, String> filter) throws APIManagementException;
}
