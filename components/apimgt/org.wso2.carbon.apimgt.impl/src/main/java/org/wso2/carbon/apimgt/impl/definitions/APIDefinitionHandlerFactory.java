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

package org.wso2.carbon.apimgt.impl.definitions;

import org.wso2.carbon.apimgt.api.APIDefinitionHandler;
import org.wso2.carbon.apimgt.api.model.API;

/**
 * Factory class for obtaining the appropriate APIDefinitionHandler based on API type.
 * Supports extension by adding new definition handler implementations.
 */
public class APIDefinitionHandlerFactory {

    private static final APIDefinitionHandler OAS_HANDLER = new OASDefinitionHandler();
    private static final APIDefinitionHandler ASYNC_HANDLER = new AsyncAPIDefinitionHandler();

    private APIDefinitionHandlerFactory() {
        // Prevent instantiation
    }

    /**
     * Gets the appropriate definition handler from an API object.
     *
     * @param api The API object.
     * @return The corresponding definition handler implementation.
     */
    public static APIDefinitionHandler getDefinitionHandler(API api) {
        if (api != null && api.isAsync()) {
            return ASYNC_HANDLER;
        }
        return OAS_HANDLER;
    }

    /**
     * Gets the appropriate definition handler from an API type string.
     *
     * @param apiType The API type (e.g. "HTTP", "WS", "WEBSUB", "SSE", "ASYNC").
     * @return The corresponding definition handler implementation.
     */
    public static APIDefinitionHandler getDefinitionHandler(String apiType) {
        if ("WS".equalsIgnoreCase(apiType) ||
            "WEBSUB".equalsIgnoreCase(apiType) ||
            "SSE".equalsIgnoreCase(apiType) ||
            "ASYNC".equalsIgnoreCase(apiType)) {
            return ASYNC_HANDLER;
        }
        return OAS_HANDLER;
    }
}
