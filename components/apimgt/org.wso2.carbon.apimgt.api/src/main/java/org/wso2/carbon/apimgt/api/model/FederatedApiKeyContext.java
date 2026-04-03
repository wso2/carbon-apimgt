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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Context object for federated API key operations.
 */
public class FederatedApiKeyContext {

    private final String apiUuid;
    private final String apiName;
    private final String apiReferenceArtifact;
    private final String apiKeyUuid;
    private final String apiKeyName;
    @JsonIgnore
    private final transient String apiKeyValue;
    private final String remoteApiKeyId;
    private final String authzUser;
    private final String applicationUuid;
    private final String organizationId;
    private final String environmentId;
    private final Long validityPeriod;
    private final String permittedIP;
    private final String permittedReferer;

    private FederatedApiKeyContext(Builder builder) {
        this.apiUuid = builder.apiUuid;
        this.apiName = builder.apiName;
        this.apiReferenceArtifact = builder.apiReferenceArtifact;
        this.apiKeyUuid = builder.apiKeyUuid;
        this.apiKeyName = builder.apiKeyName;
        this.apiKeyValue = builder.apiKeyValue;
        this.remoteApiKeyId = builder.remoteApiKeyId;
        this.authzUser = builder.authzUser;
        this.applicationUuid = builder.applicationUuid;
        this.organizationId = builder.organizationId;
        this.environmentId = builder.environmentId;
        this.validityPeriod = builder.validityPeriod;
        this.permittedIP = builder.permittedIP;
        this.permittedReferer = builder.permittedReferer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiUuid() {
        return apiUuid;
    }

    public String getApiName() {
        return apiName;
    }

    public String getApiReferenceArtifact() {
        return apiReferenceArtifact;
    }

    public String getApiKeyUuid() {
        return apiKeyUuid;
    }

    public String getApiKeyName() {
        return apiKeyName;
    }

    /**
     * Returns the raw API key value for connector operations.
     * This value is sensitive and must not be logged or serialized.
     *
     * @return API key value
     */
    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public String getRemoteApiKeyId() {
        return remoteApiKeyId;
    }

    public String getAuthzUser() {
        return authzUser;
    }

    public String getApplicationUuid() {
        return applicationUuid;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public Long getValidityPeriod() {
        return validityPeriod;
    }

    public String getPermittedIP() {
        return permittedIP;
    }

    public String getPermittedReferer() {
        return permittedReferer;
    }

    public static class Builder {

        private String apiUuid;
        private String apiName;
        private String apiReferenceArtifact;
        private String apiKeyUuid;
        private String apiKeyName;
        private String apiKeyValue;
        private String remoteApiKeyId;
        private String authzUser;
        private String applicationUuid;
        private String organizationId;
        private String environmentId;
        private Long validityPeriod;
        private String permittedIP;
        private String permittedReferer;

        public Builder apiUuid(String apiUuid) {
            this.apiUuid = apiUuid;
            return this;
        }

        public Builder apiName(String apiName) {
            this.apiName = apiName;
            return this;
        }

        public Builder apiReferenceArtifact(String apiReferenceArtifact) {
            this.apiReferenceArtifact = apiReferenceArtifact;
            return this;
        }

        public Builder apiKeyUuid(String apiKeyUuid) {
            this.apiKeyUuid = apiKeyUuid;
            return this;
        }

        public Builder apiKeyName(String apiKeyName) {
            this.apiKeyName = apiKeyName;
            return this;
        }

        /**
         * Sets the raw API key value for connector operations.
         * The value is sensitive and must not be logged.
         *
         * @param apiKeyValue raw API key value
         * @return builder instance
         */
        public Builder apiKeyValue(String apiKeyValue) {
            this.apiKeyValue = apiKeyValue;
            return this;
        }

        public Builder remoteApiKeyId(String remoteApiKeyId) {
            this.remoteApiKeyId = remoteApiKeyId;
            return this;
        }

        public Builder authzUser(String authzUser) {
            this.authzUser = authzUser;
            return this;
        }

        public Builder applicationUuid(String applicationUuid) {
            this.applicationUuid = applicationUuid;
            return this;
        }

        public Builder organizationId(String organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder environmentId(String environmentId) {
            this.environmentId = environmentId;
            return this;
        }

        public Builder validityPeriod(Long validityPeriod) {
            this.validityPeriod = validityPeriod;
            return this;
        }

        public Builder permittedIP(String permittedIP) {
            this.permittedIP = permittedIP;
            return this;
        }

        public Builder permittedReferer(String permittedReferer) {
            this.permittedReferer = permittedReferer;
            return this;
        }

        public FederatedApiKeyContext build() {
            return new FederatedApiKeyContext(this);
        }
    }
}
