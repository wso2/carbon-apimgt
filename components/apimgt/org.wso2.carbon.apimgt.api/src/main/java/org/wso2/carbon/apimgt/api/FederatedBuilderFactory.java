/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract factory for creating and managing FederatedAPIBuilder instances.
 * This provides a gateway-agnostic way to select the appropriate builder
 * for different API types (REST, WebSocket, GraphQL, etc.).
 * 
 * Gateway-specific implementations (AzureBuilderFactory, AWSBuilderFactory, etc.)
 * should extend this class and register their specific builders.
 * 
 * @param <T> The type of raw API data from the gateway (e.g., ApiContract for Azure, RestApi for AWS)
 */
public abstract class FederatedBuilderFactory<T> {
    
    private final List<FederatedAPIBuilder<T>> builders;
    
    /**
     * Constructor initializes the builders list.
     * Subclasses should call this and then register their builders.
     */
    public FederatedBuilderFactory() {
        this.builders = new ArrayList<>();
    }
    
    /**
     * Gets the appropriate builder for the given raw API data.
     * Uses the Strategy pattern - iterates through registered builders
     * and returns the first one that can handle the API type.
     * 
     * @param sourceApi The raw API data from the gateway
     * @return The builder that can handle this API type, or exception if unsupported
     */
    public FederatedAPIBuilder<T> getBuilder(T sourceApi) {
        for (FederatedAPIBuilder<T> builder : builders) {
            if (builder.canHandle(sourceApi)) {
                return builder;
            }
        }
        throw new IllegalStateException(
        "No registered builder can handle the given API data");
    }
    
    /**
     * Registers a builder in the factory.
     * Subclasses can use this to add builders in their constructor.
     * 
     * @param builder The builder to register
     */
    protected void registerBuilder(FederatedAPIBuilder<T> builder) {
        if (builder != null) {
            // 1. Check if ANY builder in the list has the same CLASS as the new one
            boolean alreadyExists = false;
            for (FederatedAPIBuilder<T> existing : builders) {
                if (existing.getClass().equals(builder.getClass())) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                builders.add(builder);
            }
        }
    }
    
    /**
     * Gets all registered builders.
     * Useful for testing and debugging.
     * 
     * @return List of all registered builders
     */
    public List<FederatedAPIBuilder<T>> getRegisteredBuilders() {
        return new ArrayList<>(builders); // Return a copy for safety
    }
    
    /**
     * Gets the count of registered builders.
     * 
     * @return Number of registered builders
     */
    public int getBuilderCount() {
        return builders.size();
    }
}
