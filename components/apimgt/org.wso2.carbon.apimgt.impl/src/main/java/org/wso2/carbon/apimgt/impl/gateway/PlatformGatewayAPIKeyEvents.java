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

package org.wso2.carbon.apimgt.impl.gateway;

/**
 * Typed API key lifecycle event models used by the control plane when publishing
 * platform gateway websocket events.
 */
public final class PlatformGatewayAPIKeyEvents {

    private PlatformGatewayAPIKeyEvents() {
    }

    public static final class Created {

        private final String organizationId;
        private final String apiId;
        private final String apiKey;
        private final String keyName;
        private final String keyUuid;
        private final String externalRefId;
        private final String expiresAt;
        private final Integer expiresInDuration;
        private final String expiresInUnit;
        private final String userId;

        public Created(String organizationId, String apiId, String apiKey, String keyName) {
            this(organizationId, apiId, apiKey, keyName, null, null, null, null, null, null);
        }

        private Created(String organizationId, String apiId, String apiKey, String keyName, String keyUuid,
                        String externalRefId, String expiresAt, Integer expiresInDuration, String expiresInUnit,
                        String userId) {
            this.organizationId = organizationId;
            this.apiId = apiId;
            this.apiKey = apiKey;
            this.keyName = keyName;
            this.keyUuid = keyUuid;
            this.externalRefId = externalRefId;
            this.expiresAt = expiresAt;
            this.expiresInDuration = expiresInDuration;
            this.expiresInUnit = expiresInUnit;
            this.userId = userId;
        }

        public String getOrganizationId() {
            return organizationId;
        }

        public String getApiId() {
            return apiId;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getKeyName() {
            return keyName;
        }

        public String getKeyUuid() {
            return keyUuid;
        }

        public String getExternalRefId() {
            return externalRefId;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public Integer getExpiresInDuration() {
            return expiresInDuration;
        }

        public String getExpiresInUnit() {
            return expiresInUnit;
        }

        public String getUserId() {
            return userId;
        }

        public Created withKeyUuid(String value) {
            return new Created(organizationId, apiId, apiKey, keyName, value, externalRefId, expiresAt,
                    expiresInDuration, expiresInUnit, userId);
        }

        public Created withExternalRefId(String value) {
            return new Created(organizationId, apiId, apiKey, keyName, keyUuid, value, expiresAt,
                    expiresInDuration, expiresInUnit, userId);
        }

        public Created withExpiresAt(String value) {
            return new Created(organizationId, apiId, apiKey, keyName, keyUuid, externalRefId, value,
                    expiresInDuration, expiresInUnit, userId);
        }

        public Created withExpiresIn(Integer duration, String unit) {
            return new Created(organizationId, apiId, apiKey, keyName, keyUuid, externalRefId, expiresAt,
                    duration, unit, userId);
        }

        public Created withUserId(String value) {
            return new Created(organizationId, apiId, apiKey, keyName, keyUuid, externalRefId, expiresAt,
                    expiresInDuration, expiresInUnit, value);
        }
    }

    public static final class Updated {

        private final String organizationId;
        private final String apiId;
        private final String keyName;
        private final String apiKey;
        private final String keyUuid;
        private final String externalRefId;
        private final String expiresAt;
        private final Integer expiresInDuration;
        private final String expiresInUnit;
        private final String userId;

        public Updated(String organizationId, String apiId, String keyName, String apiKey) {
            this(organizationId, apiId, keyName, apiKey, null, null, null, null, null, null);
        }

        private Updated(String organizationId, String apiId, String keyName, String apiKey, String keyUuid,
                        String externalRefId, String expiresAt, Integer expiresInDuration, String expiresInUnit,
                        String userId) {
            this.organizationId = organizationId;
            this.apiId = apiId;
            this.keyName = keyName;
            this.apiKey = apiKey;
            this.keyUuid = keyUuid;
            this.externalRefId = externalRefId;
            this.expiresAt = expiresAt;
            this.expiresInDuration = expiresInDuration;
            this.expiresInUnit = expiresInUnit;
            this.userId = userId;
        }

        public String getOrganizationId() {
            return organizationId;
        }

        public String getApiId() {
            return apiId;
        }

        public String getKeyName() {
            return keyName;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getKeyUuid() {
            return keyUuid;
        }

        public String getExternalRefId() {
            return externalRefId;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public Integer getExpiresInDuration() {
            return expiresInDuration;
        }

        public String getExpiresInUnit() {
            return expiresInUnit;
        }

        public String getUserId() {
            return userId;
        }

        public Updated withKeyUuid(String value) {
            return new Updated(organizationId, apiId, keyName, apiKey, value, externalRefId, expiresAt,
                    expiresInDuration, expiresInUnit, userId);
        }

        public Updated withExternalRefId(String value) {
            return new Updated(organizationId, apiId, keyName, apiKey, keyUuid, value, expiresAt,
                    expiresInDuration, expiresInUnit, userId);
        }

        public Updated withExpiresAt(String value) {
            return new Updated(organizationId, apiId, keyName, apiKey, keyUuid, externalRefId, value,
                    expiresInDuration, expiresInUnit, userId);
        }

        public Updated withExpiresIn(Integer duration, String unit) {
            return new Updated(organizationId, apiId, keyName, apiKey, keyUuid, externalRefId, expiresAt,
                    duration, unit, userId);
        }

        public Updated withUserId(String value) {
            return new Updated(organizationId, apiId, keyName, apiKey, keyUuid, externalRefId, expiresAt,
                    expiresInDuration, expiresInUnit, value);
        }
    }

    public static final class Revoked {

        private final String organizationId;
        private final String apiId;
        private final String keyName;
        private final String userId;

        public Revoked(String organizationId, String apiId, String keyName) {
            this(organizationId, apiId, keyName, null);
        }

        private Revoked(String organizationId, String apiId, String keyName, String userId) {
            this.organizationId = organizationId;
            this.apiId = apiId;
            this.keyName = keyName;
            this.userId = userId;
        }

        public String getOrganizationId() {
            return organizationId;
        }

        public String getApiId() {
            return apiId;
        }

        public String getKeyName() {
            return keyName;
        }

        public String getUserId() {
            return userId;
        }

        public Revoked withUserId(String value) {
            return new Revoked(organizationId, apiId, keyName, value);
        }
    }
}
