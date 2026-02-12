/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.List;

/**
 * DefinitionHandler interface for handling different API definition types (OpenAPI, AsyncAPI, GraphQL).
 * This allows extensibility without modifying core logic.
 */
public interface APIDefinitionHandler {

    /**
     * Returns the API type identifier (e.g., "HTTP", "WS", "GRAPHQL").
     *
     * @return The API type.
     */
    String getType(API api);

    /**
     * Indicates whether this API type uses AsyncAPI specification.
     *
     * @return True if AsyncAPI, false for OpenAPI/Swagger.
     */
    boolean isAsync(API api);

    /**
     * Extracts the definition string (Swagger/AsyncAPI/GraphQL) from the API object.
     *
     * @param api The API object.
     * @return The definition string.
     */
    String getDefinitionFromAPI(API api);

    /**
     * Sets the definition string back to the API object.
     *
     * @param api        The API object.
     * @param definition The definition string.
     */
    void setDefinitionToAPI(API api, String definition);

    /**
     * Updates the version in the definition string.
     *
     * @param api The API object containing the definition and version.
     * @return The updated definition string.
     * @throws APIManagementException If an error occurs during update.
     */
    void updateAPIDefinitionWithVersion(API api) throws APIManagementException;

    /**
     * Extracts endpoint URL from the definition for discovered APIs.
     *
     * @param definition The definition string.
     * @return The extracted endpoint URL, or null if not found.
     */
    String extractEndpointUrl(String definition);

    /**
     * Extracts operations/URI templates from the definition.
     *
     * @param definition The definition string.
     * @return List of URI templates.
     * @throws APIManagementException If an error occurs during extraction.
     */
    List<URITemplate> extractOperations(String definition) throws APIManagementException;

    /**
     * Returns the filename for the definition in the export zip.
     *
     * @return The filename (e.g., "swagger.yaml", "asyncapi.yaml").
     */
    String getDefinitionFileName();
}
