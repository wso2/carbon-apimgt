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

package org.wso2.carbon.apimgt.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of a federated credential creation operation.
 * Captures the remote credential identifier and its type to support different gateway models.
 */
public class FederatedApiKeyCreationResult {

    private final String remoteCredentialId;
    private final String credentialType;
    private final Map<String, Object> metadata;

    private FederatedApiKeyCreationResult(Builder builder) {
        this.remoteCredentialId = builder.remoteCredentialId;
        this.credentialType = builder.credentialType;
        this.metadata = builder.metadata != null ? 
            Collections.unmodifiableMap(new HashMap<>(builder.metadata)) : Collections.emptyMap();
    }

    /**
     * Gets the remote credential identifier used for revocation and association operations.
     * The semantic meaning varies by gateway:
     * - AWS: API Key ID
     * - Kong: Consumer ID
     * - Azure: Subscription ID
     *
     * @return remote credential identifier
     */
    public String getRemoteCredentialId() {
        return remoteCredentialId;
    }

    /**
     * Gets the credential type for semantic clarity.
     * Examples: "AWS_API_KEY", "KONG_CONSUMER", "AZURE_SUBSCRIPTION"
     *
     * @return credential type identifier
     */
    public String getCredentialType() {
        return credentialType;
    }

    /**
     * Gets optional metadata about the created credential.
     * May include gateway-specific details like key value, consumer name, etc.
     *
     * @return metadata map (immutable)
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String remoteCredentialId;
        private String credentialType;
        private Map<String, Object> metadata;

        public Builder remoteCredentialId(String remoteCredentialId) {
            this.remoteCredentialId = remoteCredentialId;
            return this;
        }

        public Builder credentialType(String credentialType) {
            this.credentialType = credentialType;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public FederatedApiKeyCreationResult build() {
            if (remoteCredentialId == null || remoteCredentialId.trim().isEmpty()) {
                throw new IllegalStateException("remoteCredentialId must not be blank");
            }
            return new FederatedApiKeyCreationResult(this);
        }
    }
}
