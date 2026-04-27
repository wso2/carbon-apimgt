/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.FederatedApiKeyContext;
import org.wso2.carbon.apimgt.api.model.FederatedApiKeyCreationResult;

/**
 * Interface for managing API-bound API keys in external gateways.
 */
public interface FederatedApiKeyConnector {

    /**
     * Initializes the API key agent.
     *
     * @param environment gateway environment configuration
     * @throws APIManagementException if initialization fails
     */
    void init(Environment environment) throws APIManagementException;

    /**
     * Creates/pushes an API key in the external gateway.
     *
     * @param context API key operation context
     * @return credential creation result containing connector-owned reference artifact and metadata
     * @throws APIManagementException if operation fails
     */
    FederatedApiKeyCreationResult createApiKey(FederatedApiKeyContext context) throws APIManagementException;

    /**
     * Replaces/regenerates an API key in the external gateway.
     * Implementations may update the existing remote credential in place, or create a new remote credential and migrate
     * connector-owned associations before deleting the old one.
     *
     * @param context API key operation context containing the old connector-owned reference artifact and new key value
     * @return credential creation result containing the retained or newly created connector-owned reference artifact
     * @throws APIManagementException if operation fails
     */
    FederatedApiKeyCreationResult replaceApiKey(FederatedApiKeyContext context) throws APIManagementException;

    /**
     * Revokes/deletes an API key in the external gateway.
     *
     * @param context API key operation context
     * @throws APIManagementException if operation fails
     */
    void revokeApiKey(FederatedApiKeyContext context) throws APIManagementException;

    /**
     * Applies a rate limiting policy to an API key.
     * Different gateways implement this differently:
     * - AWS: Associates key with usage plan
     * - Kong: Adds consumer to consumer group and ACL
     * - Azure: Associates subscription with tier
     *
     * @param context API key operation context
     * @throws APIManagementException if operation fails
     */
    void applyRateLimitPolicy(FederatedApiKeyContext context) throws APIManagementException;

    /**
     * Removes rate limiting policy from an API key.
     *
     * @param context API key operation context
     * @throws APIManagementException if operation fails
     */
    void removeRateLimitPolicy(FederatedApiKeyContext context) throws APIManagementException;

    /**
     * Gets gateway type identifier.
     *
     * @return gateway type
     */
    String getGatewayType();

    /**
     * Returns whether this gateway supports federated API key management.
     *
     * @return true if supported
     */
    default boolean isApiKeySupport() {
        return false;
    }
}
