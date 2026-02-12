/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Environment;

/**
 * Abstract builder for creating WSO2 API objects from external API contracts (federated discovery).
 *
 * @param <T> The type of the raw data/contract from the external gateway (e.g. Azure API object, Kong API object).
 */
public abstract class FederatedAPIBuilder<T> {

    /**
     * Builds a WSO2 API object from the raw external data.
     *
     * @param sourceApi The raw data object from the external gateway.
     * @param env     The environment where the API is discovered.
     * @param org     The organization context.
     * @return The constructed API object.
     * @throws APIManagementException If an error occurs during building.
     */
    public API build(T sourceApi, Environment env, String org) throws APIManagementException {
        // 1. Basic Identification
        String provider = org; // Usually defaults to the organization name
        APIIdentifier apiId = new APIIdentifier(provider, getName(sourceApi), getVersion(sourceApi));

        API api = new API(apiId);

        // 2. Map Common Properties
        api.setContext(getContext(sourceApi));
        api.setContextTemplate(getContextTemplate(sourceApi));
        api.setUuid(getGatewayId(sourceApi)); // Important for mapping updates later
        api.setDescription(getDescription(sourceApi));

        // 3. Set Standard WSO2 Flags
        api.setOrganization(org);
        if (env != null) {
            api.setGatewayType(env.getGatewayType());
        }
        api.setInitiatedFromGateway(true);
        api.setRevision(false);
        api.setGatewayVendor("external");

        // 4. Specific Mapping (Delegated to subclasses)
        mapSpecificDetails(api, sourceApi, env);

        return api;
    }

    /**
     * Extracts the name of the API from the raw data.
     *
     * @param sourceApi The raw data object.
     * @return The API name.
     */
    protected abstract String getName(T sourceApi);

    /**
     * Extracts the version of the API from the raw data.
     *
     * @param sourceApi The raw data object.
     * @return The API version.
     */
    protected abstract String getVersion(T sourceApi);

    /**
     * Extracts the context/path of the API from the raw data.
     *
     * @param sourceApi The raw data object.
     * @return The API context.
     */
    protected abstract String getContext(T sourceApi);

    /**
     * Extracts the context/path of the API from the raw data.
     *
     * @param sourceApi The raw data object.
     * @return The API context template.
     */
    protected abstract String getContextTemplate(T sourceApi);

    /**
     * Extracts the unique ID (UUID) from the external gateway.
     * This is used to link the WSO2 API to the External API for updates.
     *
     * @param sourceApi The raw data object.
     * @return The unique gateway ID.
     */
    protected abstract String getGatewayId(T sourceApi);

    /**
     * Extracts the description of the API from the raw data.
     *
     * @param sourceApi The raw data object.
     * @return The API description.
     */
    protected abstract String getDescription(T sourceApi);

    /**
     * Maps type-specific details (protocol, endpoints, definitions, etc.) to the API object.
     *
     * @param api     The WSO2 API object to populate.
     * @param sourceApi The raw data object.
     * @throws APIManagementException If an error occurs during mapping.
     */
    protected abstract void mapSpecificDetails(API api, T sourceApi, Environment env) throws APIManagementException;

    /**
     * Checks if this builder can handle the given raw data object.
     *
     * @param sourceApi The raw data object.
     * @return True if this builder can handle the object, false otherwise.
     */
    public abstract boolean canHandle(T sourceApi);
}
