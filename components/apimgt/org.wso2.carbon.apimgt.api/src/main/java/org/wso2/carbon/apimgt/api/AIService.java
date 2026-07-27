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

/**
 * Common base for the AI-service extension points ({@link DesignAssistant}, {@link MarketplaceAssistant},
 * {@link APIChatAssistant}).
 * <p>
 * It defines a single configuration lifecycle hook: after an implementation is instantiated (via its public
 * no-argument constructor), the framework pushes its resolved settings in through {@link #init(AIServiceConfiguration)}
 * before the instance is used. Delivering configuration this way means a custom implementation reads everything it
 * needs from the {@link AIServiceConfiguration} it is handed and never has to reach into the {@code impl}-module
 * configuration ({@code APIManagerConfiguration}/{@code ServiceReferenceHolder}) - so a third-party jar can depend on
 * this {@code api} module alone. This mirrors the {@code KeyManager.loadConfiguration(...)} pattern used elsewhere in
 * the product.
 * <p>
 * The default {@link #init(AIServiceConfiguration)} is a no-op so that implementations which do not need any injected
 * configuration (and existing implementations compiled against the earlier contract) remain source- and
 * binary-compatible.
 */
public interface AIService {

    /**
     * Injects the resolved AI-service configuration into this implementation. Invoked once by the framework
     * immediately after construction and before the instance is used; the same instance is then cached and shared,
     * so implementations must be thread-safe.
     *
     * @param configuration the resolved configuration (endpoint, credentials and any implementation-specific
     *                       properties); never {@code null}
     * @throws APIManagementException if the implementation cannot be initialized with the given configuration
     */
    default void init(AIServiceConfiguration configuration) throws APIManagementException {
        // No-op by default; implementations that need injected configuration override this.
    }
}
