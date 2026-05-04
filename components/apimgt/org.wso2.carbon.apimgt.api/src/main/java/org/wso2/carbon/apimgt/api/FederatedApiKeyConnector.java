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

import java.util.Map;

/**
 * Interface for managing API-bound API keys in external gateways.
 * Follows tight input patterns similar to {@link org.wso2.carbon.apimgt.api.model.GatewayDeployer}.
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
     * @param apiKeyUuid           local API key UUID
     * @param apiKeyValue          the generated API key value to push
     * @param apiReferenceArtifact connector-owned API reference artifact
     * @param localPolicyId        local subscription policy UUID for plan mapping (nullable)
     * @param properties           additional metadata (apiKeyName, authzUser, validityPeriod, permittedIP, etc.)
     * @return connector-owned reference artifact
     * @throws APIManagementException if operation fails
     */
    String createApiKey(String apiKeyUuid, String apiKeyValue, String apiReferenceArtifact,
                        String localPolicyId, Map<String, String> properties)
            throws APIManagementException;

    /**
     * Replaces/regenerates an API key in the external gateway.
     *
     * @param apiKeyReferenceArtifact connector-owned API key reference artifact of the key to replace
     * @param newApiKeyValue          the new API key value
     * @param properties              additional metadata (apiKeyName, validityPeriod, etc.)
     * @return retained or newly created connector-owned reference artifact
     * @throws APIManagementException if operation fails
     */
    String replaceApiKey(String apiKeyReferenceArtifact, String newApiKeyValue, Map<String, String> properties)
            throws APIManagementException;

    /**
     * Revokes/deletes an API key in the external gateway.
     *
     * @param apiKeyReferenceArtifact connector-owned API key reference artifact
     * @throws APIManagementException if operation fails
     */
    void revokeApiKey(String apiKeyReferenceArtifact) throws APIManagementException;

    /**
     * Applies a rate limiting policy to an API key.
     *
     * @param apiKeyReferenceArtifact connector-owned API key reference artifact
     * @param localPolicyId           local subscription policy UUID for plan mapping
     * @throws APIManagementException if operation fails
     */
    void associateSubscriptionPolicy(String apiKeyReferenceArtifact, String localPolicyId)
            throws APIManagementException;

    /**
     * Removes rate limiting policy from an API key.
     *
     * @param apiKeyReferenceArtifact connector-owned API key reference artifact
     * @param localPolicyId           local subscription policy UUID for plan mapping
     * @throws APIManagementException if operation fails
     */
    void dissociateSubscriptionPolicy(String apiKeyReferenceArtifact, String localPolicyId)
            throws APIManagementException;

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
