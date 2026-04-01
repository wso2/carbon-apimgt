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

import org.wso2.carbon.apimgt.api.model.*;

import java.util.Collections;
import java.util.List;

/**
 * Interface for managing API-bound API keys in external gateways.
 */
public interface FederatedApiKeyConnector {

    /**
     * Initializes the API key agent.
     *
     * @param environment gateway environment configuration
     * @param organization organization identifier
     * @throws APIManagementException if initialization fails
     */
    void init(Environment environment, String organization) throws APIManagementException;

    /**
     * Creates/pushes an API key in the external gateway.
     *
     * @param context API key operation context
     * @return credential creation result containing remote identifier and metadata
     * @throws APIManagementException if operation fails
     */
    FederatedApiKeyCreationResult createApiKey(FederatedApiKeyContext context) throws APIManagementException;

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
     * @param policyId remote rate limit policy identifier
     * @throws APIManagementException if operation fails
     */
    void applyRateLimitPolicy(FederatedApiKeyContext context, String policyId)
            throws APIManagementException;

    /**
     * Removes rate limiting policy from an API key.
     *
     * @param context API key operation context
     * @throws APIManagementException if operation fails
     */
    void removeRateLimitPolicy(FederatedApiKeyContext context) throws APIManagementException;

    /**
     * Resolves the remote rate limit policy ID from the stored policy reference.
     * Each gateway implementation parses its own format (JSON, raw ID, etc.).
     *
     * @param remotePolicyReference the stored policy reference (may be JSON or raw ID)
     * @return the resolved policy ID, or null if not resolvable
     * @throws APIManagementException if parsing fails
     */
    String resolveRemotePolicyId(String remotePolicyReference) throws APIManagementException;

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

    /**
     * Returns whether this gateway supports listing remote plans for onboarding and tier mapping.
     *
     * @return true if remote plan listing is supported
     */
    default boolean supportsRemotePlanListing() {
        return false;
    }

    /**
     * Lists available rate limiting policies from gateway for environment onboarding and local tier mapping.
     * Different gateways call these different things (usage plans, consumer groups, subscription tiers, etc.).
     *
     * @param environment gateway environment configuration
     * @return list of rate limit policies
     * @throws APIManagementException if policy retrieval fails
     */
    default List<ExternalSubscriptionPolicy> listRateLimitPolicies(Environment environment) throws APIManagementException {
        return Collections.emptyList();
    }
}
