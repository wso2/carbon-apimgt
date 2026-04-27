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
 * Captures the connector-owned remote credential reference artifact and optional gateway-specific metadata.
 */
public class FederatedApiKeyCreationResult {

    private final String referenceArtifact;
    private final Map<String, Object> metadata;

    private FederatedApiKeyCreationResult(Builder builder) {
        this.referenceArtifact = builder.referenceArtifact;
        this.metadata = builder.metadata != null ? 
            Collections.unmodifiableMap(new HashMap<>(builder.metadata)) : Collections.emptyMap();
    }

    /**
     * Gets the opaque connector-owned reference artifact used for revocation and association operations.
     * APIM must store and pass this value without parsing gateway-specific fields.
     *
     * @return connector-owned reference artifact
     */
    public String getReferenceArtifact() {
        return referenceArtifact;
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
        private String referenceArtifact;
        private Map<String, Object> metadata;

        public Builder referenceArtifact(String referenceArtifact) {
            this.referenceArtifact = referenceArtifact;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public FederatedApiKeyCreationResult build() {
            if (referenceArtifact == null || referenceArtifact.trim().isEmpty()) {
                throw new IllegalStateException("referenceArtifact must not be blank");
            }
            return new FederatedApiKeyCreationResult(this);
        }
    }
}
