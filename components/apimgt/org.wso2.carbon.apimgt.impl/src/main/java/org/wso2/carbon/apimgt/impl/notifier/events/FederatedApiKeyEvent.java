/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.UUID;

/**
 * Event for federated API key operations (create, revoke, apply/remove rate limit policy).
 * This event is processed asynchronously by FederatedApiKeyNotifier to push changes to external gateways.
 */
public class FederatedApiKeyEvent extends Event {

    /**
     * Event types for federated API key operations.
     */
    public enum EventType {
        CREATE,
        REVOKE,
        APPLY_RATE_LIMIT_POLICY,
        REMOVE_RATE_LIMIT_POLICY
    }

    private EventType federatedEventType;
    private String keyUuid;
    private String keyName;
    private String keyType;
    private String apiKeyHash;
    private String apiKeyValue;
    private String apiUuid;
    private String apiName;
    private String apiReferenceArtifact;
    private String applicationUuid;
    private String environmentId;
    private String organization;
    private String authzUser;
    private String remoteApiKeyId;
    private Long validityPeriod;
    private String permittedIP;
    private String permittedReferer;
    private String localTierName;

    public FederatedApiKeyEvent() {
        // Default constructor for serialization
    }

    private FederatedApiKeyEvent(Builder builder) {
        super(UUID.randomUUID().toString(), System.currentTimeMillis(), 
              builder.federatedEventType.name(), builder.tenantId, builder.tenantDomain);
        this.federatedEventType = builder.federatedEventType;
        this.keyUuid = builder.keyUuid;
        this.keyName = builder.keyName;
        this.keyType = builder.keyType;
        this.apiKeyHash = builder.apiKeyHash;
        this.apiKeyValue = builder.apiKeyValue;
        this.apiUuid = builder.apiUuid;
        this.apiName = builder.apiName;
        this.apiReferenceArtifact = builder.apiReferenceArtifact;
        this.applicationUuid = builder.applicationUuid;
        this.environmentId = builder.environmentId;
        this.organization = builder.organization;
        this.authzUser = builder.authzUser;
        this.remoteApiKeyId = builder.remoteApiKeyId;
        this.validityPeriod = builder.validityPeriod;
        this.permittedIP = builder.permittedIP;
        this.permittedReferer = builder.permittedReferer;
        this.localTierName = builder.localTierName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public EventType getFederatedEventType() {
        return federatedEventType;
    }

    public void setFederatedEventType(EventType federatedEventType) {
        this.federatedEventType = federatedEventType;
    }

    public String getKeyUuid() {
        return keyUuid;
    }

    public void setKeyUuid(String keyUuid) {
        this.keyUuid = keyUuid;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public String getApiUuid() {
        return apiUuid;
    }

    public void setApiUuid(String apiUuid) {
        this.apiUuid = apiUuid;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiReferenceArtifact() {
        return apiReferenceArtifact;
    }

    public void setApiReferenceArtifact(String apiReferenceArtifact) {
        this.apiReferenceArtifact = apiReferenceArtifact;
    }

    public String getApplicationUuid() {
        return applicationUuid;
    }

    public void setApplicationUuid(String applicationUuid) {
        this.applicationUuid = applicationUuid;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAuthzUser() {
        return authzUser;
    }

    public void setAuthzUser(String authzUser) {
        this.authzUser = authzUser;
    }

    public String getRemoteApiKeyId() {
        return remoteApiKeyId;
    }

    public void setRemoteApiKeyId(String remoteApiKeyId) {
        this.remoteApiKeyId = remoteApiKeyId;
    }

    public Long getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(Long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getPermittedIP() {
        return permittedIP;
    }

    public void setPermittedIP(String permittedIP) {
        this.permittedIP = permittedIP;
    }

    public String getPermittedReferer() {
        return permittedReferer;
    }

    public void setPermittedReferer(String permittedReferer) {
        this.permittedReferer = permittedReferer;
    }

    public String getLocalTierName() {
        return localTierName;
    }

    public void setLocalTierName(String localTierName) {
        this.localTierName = localTierName;
    }

    @Override
    public String toString() {
        return "FederatedApiKeyEvent{" +
                "federatedEventType=" + federatedEventType +
                ", keyUuid='" + keyUuid + '\'' +
                ", apiUuid='" + apiUuid + '\'' +
                ", environmentId='" + environmentId + '\'' +
                ", organization='" + organization + '\'' +
                '}';
    }

    public static class Builder {

        private EventType federatedEventType;
        private int tenantId;
        private String tenantDomain;
        private String keyUuid;
        private String keyName;
        private String keyType;
        private String apiKeyHash;
        private String apiKeyValue;
        private String apiUuid;
        private String apiName;
        private String apiReferenceArtifact;
        private String applicationUuid;
        private String environmentId;
        private String organization;
        private String authzUser;
        private String remoteApiKeyId;
        private Long validityPeriod;
        private String permittedIP;
        private String permittedReferer;
        private String localTierName;

        public Builder federatedEventType(EventType federatedEventType) {
            this.federatedEventType = federatedEventType;
            return this;
        }

        public Builder tenantId(int tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder tenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public Builder keyUuid(String keyUuid) {
            this.keyUuid = keyUuid;
            return this;
        }

        public Builder keyName(String keyName) {
            this.keyName = keyName;
            return this;
        }

        public Builder keyType(String keyType) {
            this.keyType = keyType;
            return this;
        }

        public Builder apiKeyHash(String apiKeyHash) {
            this.apiKeyHash = apiKeyHash;
            return this;
        }

        public Builder apiKeyValue(String apiKeyValue) {
            this.apiKeyValue = apiKeyValue;
            return this;
        }

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

        public Builder applicationUuid(String applicationUuid) {
            this.applicationUuid = applicationUuid;
            return this;
        }

        public Builder environmentId(String environmentId) {
            this.environmentId = environmentId;
            return this;
        }

        public Builder organization(String organization) {
            this.organization = organization;
            return this;
        }

        public Builder authzUser(String authzUser) {
            this.authzUser = authzUser;
            return this;
        }

        public Builder remoteApiKeyId(String remoteApiKeyId) {
            this.remoteApiKeyId = remoteApiKeyId;
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

        public Builder localTierName(String localTierName) {
            this.localTierName = localTierName;
            return this;
        }

        public FederatedApiKeyEvent build() {
            if (federatedEventType == null) {
                throw new IllegalStateException("federatedEventType is required");
            }
            if (keyUuid == null) {
                throw new IllegalStateException("keyUuid is required");
            }
            if (environmentId == null) {
                throw new IllegalStateException("environmentId is required");
            }
            if (organization == null) {
                throw new IllegalStateException("organization is required");
            }
            return new FederatedApiKeyEvent(this);
        }
    }
}
