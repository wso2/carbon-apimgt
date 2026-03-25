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

package org.wso2.carbon.apimgt.internal.service.websocket;

/**
 * Shared websocket DTOs for platform gateway control-plane messages.
 */
public final class PlatformGatewayWebSocketModels {

    private PlatformGatewayWebSocketModels() {
    }

    public static final class EventEnvelope<T> {
        private final String type;
        private final T payload;
        private final String timestamp;
        private final String correlationId;
        private final String userId;

        public EventEnvelope(String type, T payload, String timestamp, String correlationId, String userId) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
            this.correlationId = correlationId;
            this.userId = userId;
        }
    }

    public static final class ConnectionAck {
        private final String type;
        private final String gatewayId;
        private final String connectionId;
        private final String timestamp;

        public ConnectionAck(String type, String gatewayId, String connectionId, String timestamp) {
            this.type = type;
            this.gatewayId = gatewayId;
            this.connectionId = connectionId;
            this.timestamp = timestamp;
        }
    }

    public static final class ApiDeploymentPayload {
        private final String apiId;
        private final String deploymentId;
        private final String performedAt;

        public ApiDeploymentPayload(String apiId, String deploymentId, String performedAt) {
            this.apiId = apiId;
            this.deploymentId = deploymentId;
            this.performedAt = performedAt;
        }
    }

    public static final class ApiDeletePayload {
        private final String apiId;

        public ApiDeletePayload(String apiId) {
            this.apiId = apiId;
        }
    }

    public static final class ApiKeyExpiresIn {
        private final Integer duration;
        private final String unit;

        public ApiKeyExpiresIn(Integer duration, String unit) {
            this.duration = duration;
            this.unit = unit;
        }
    }

    public static final class ApiKeyCreatedPayload {
        private final String apiId;
        private final String uuid;
        private final String apiKeyHashes;
        private final String maskedApiKey;
        private final String name;
        private final String externalRefId;
        private final String expiresAt;
        private final ApiKeyExpiresIn expiresIn;

        public ApiKeyCreatedPayload(String apiId, String uuid, String apiKeyHashes, String maskedApiKey, String name,
                                    String externalRefId, String expiresAt, ApiKeyExpiresIn expiresIn) {
            this.apiId = apiId;
            this.uuid = uuid;
            this.apiKeyHashes = apiKeyHashes;
            this.maskedApiKey = maskedApiKey;
            this.name = name;
            this.externalRefId = externalRefId;
            this.expiresAt = expiresAt;
            this.expiresIn = expiresIn;
        }
    }

    public static final class ApiKeyUpdatedPayload {
        private final String apiId;
        private final String keyName;
        private final String apiKeyHashes;
        private final String maskedApiKey;
        private final String externalRefId;
        private final String expiresAt;
        private final ApiKeyExpiresIn expiresIn;

        public ApiKeyUpdatedPayload(String apiId, String keyName, String apiKeyHashes, String maskedApiKey,
                                    String externalRefId, String expiresAt, ApiKeyExpiresIn expiresIn) {
            this.apiId = apiId;
            this.keyName = keyName;
            this.apiKeyHashes = apiKeyHashes;
            this.maskedApiKey = maskedApiKey;
            this.externalRefId = externalRefId;
            this.expiresAt = expiresAt;
            this.expiresIn = expiresIn;
        }
    }

    public static final class ApiKeyRevokedPayload {
        private final String apiId;
        private final String keyName;

        public ApiKeyRevokedPayload(String apiId, String keyName) {
            this.apiId = apiId;
            this.keyName = keyName;
        }
    }

    public static final class DeploymentAckMessage {
        private String type;
        private DeploymentAckPayload payload;

        public String getType() {
            return type;
        }

        public DeploymentAckPayload getPayload() {
            return payload;
        }
    }

    public static final class DeploymentAckPayload {
        private String deploymentId;
        private String artifactId;
        private String resourceType;
        private String action;
        private String status;
        private String performedAt;
        private String errorCode;

        public String getDeploymentId() {
            return deploymentId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getResourceType() {
            return resourceType;
        }

        public String getAction() {
            return action;
        }

        public String getStatus() {
            return status;
        }

        public String getPerformedAt() {
            return performedAt;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
