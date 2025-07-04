/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 */

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.dto.EmbeddingProviderConfigurationDTO;

/**
 * Interface for embedding provider services.
 * This interface defines methods for initializing the provider, retrieving the type,
 * getting the embedding dimension, and generating embeddings for input text.
 */
public interface EmbeddingProviderService {
    /**
     * Initialize the provider with required HTTP client and configuration properties.
     *
     */
    void init(EmbeddingProviderConfigurationDTO providerConfig) throws APIManagementException;

    /**
     * The type identifier for this provider (e.g., "OPENAI", "MISTRAL").
     *
     * @return A unique string identifier.
     */
    String getType();

    /**
     * Returns the dimensionality of the embedding vector produced by the embedding model.
     * The dimension is determined by generating an embedding for an empty string input.
     *
     * @return the size of the embedding vector.
     * @throws APIManagementException if an error occurs while retrieving the embedding dimension.
     */
    int getEmbeddingDimension() throws APIManagementException;

    /**
     * Generates an embedding vector for the given input text using the embedding model.
     *
     * @param input the text to be embedded.
     * @return a double array representing the embedding vector of the input text.
     * @throws APIManagementException if an error occurs while generating the embedding.
     */
    double[] getEmbedding(String input) throws APIManagementException;
}

