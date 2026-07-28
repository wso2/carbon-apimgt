/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

import java.io.Closeable;
import java.util.Map;

/**
 * Cloud-agnostic interface for acquiring bearer tokens via a managed/workload identity mechanism.
 * Implementations handle token acquisition, caching, and renewal internally.
 * The same interface can be implemented for different cloud providers (e.g. Azure UMI, AWS IAM
 * instance roles) so that callers such as embedding providers or guardrail providers remain
 * independent of the underlying cloud credential mechanism.
 *
 * Implementations must be thread-safe as instances will be shared and accessed concurrently
 * in the gateway environment.
 */
public interface ManagedIdentityTokenProvider extends Closeable {

    /**
     * Initialises the provider. Credentials are read from environment variables injected by the
     * cloud workload identity mechanism. The properties map may contain optional overrides.
     * This method must be called before {@link #getAccessToken()}.
     * Calling this method multiple times is permitted and will re-initialize the provider,
     * replacing any previously built credentials and scopes.
     *
     * @param properties key-value configuration map from the provider's Property block.
     * @throws APIManagementException if required environment variables or properties are missing.
     */
    void init(Map<String, String> properties) throws APIManagementException;

    /**
     * The type identifier for this provider (e.g., "azure-umi").
     *
     * @return A unique string identifier.
     */
    String getType();

    /**
     * Returns a valid bearer access token for the configured target scope.
     * Implementations must cache the token and return the cached value until it is close to
     * expiry, then transparently refresh it.
     * {@link #init(Map)} must be called successfully before invoking this method;
     * behaviour is undefined if called on an uninitialised provider.
     *
     * @return an access token string without the "Bearer " prefix.
     * @throws APIManagementException if the token cannot be acquired or refreshed.
     */
    String getAccessToken() throws APIManagementException;
}
