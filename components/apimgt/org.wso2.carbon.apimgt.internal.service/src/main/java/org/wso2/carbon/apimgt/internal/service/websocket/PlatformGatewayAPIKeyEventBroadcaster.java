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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Broadcasts API key lifecycle events (apikey.created, apikey.updated, apikey.revoked) to all
 * connected platform gateways via WebSocket. Message format is aligned with API Platform
 * (api-platform gateway-controller) so the same gateway binary can work with on-prem APIM.
 * <p>
 * Call this from the opaque API key lifecycle: when an API key is created, updated, or revoked
 * (e.g. from DevPortal/Admin or internal API), invoke the corresponding broadcast method so
 * connected gateways can update their key cache.
 */
public class PlatformGatewayAPIKeyEventBroadcaster {

    private static final Log log = LogFactory.getLog(PlatformGatewayAPIKeyEventBroadcaster.class);
    private static final PlatformGatewayAPIKeyEventBroadcaster INSTANCE =
            new PlatformGatewayAPIKeyEventBroadcaster();

    public static final String EVENT_APIKEY_CREATED = "apikey.created";
    public static final String EVENT_APIKEY_UPDATED = "apikey.updated";
    public static final String EVENT_APIKEY_REVOKED = "apikey.revoked";

    public static PlatformGatewayAPIKeyEventBroadcaster getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayAPIKeyEventBroadcaster() {
    }

    /**
     * Broadcast apikey.created to all connected platform gateways.
     * Payload must include apiId, apiKey (plain), name (URL-safe key identifier), operations;
     * optional: externalRefId, expiresAt (ISO 8601), expiresIn, displayName, userId.
     */
    public void broadcastAPIKeyCreated(String apiId, String apiKey, String name, String operations,
                                      String externalRefId, String expiresAt, Integer expiresInDuration,
                                      String expiresInUnit, String displayName, String userId) {
        if (apiId == null || apiKey == null || name == null || operations == null) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping apikey.created broadcast: missing required field (apiId, apiKey, name, operations)");
            }
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = userId != null ? userId : "";
        String apiKeyHashes = buildApiKeyHashesJson(apiKey);
        String maskedApiKey = maskApiKey(apiKey);

        StringBuilder payload = new StringBuilder();
        payload.append("\"apiId\":\"").append(escapeJson(apiId)).append("\"");
        payload.append(",\"apiKey\":\"").append(escapeJson(apiKey)).append("\"");
        payload.append(",\"apiKeyHashes\":\"").append(escapeJson(apiKeyHashes)).append("\"");
        payload.append(",\"maskedApiKey\":\"").append(escapeJson(maskedApiKey)).append("\"");
        payload.append(",\"name\":\"").append(escapeJson(name)).append("\"");
        if (externalRefId != null && !externalRefId.isEmpty()) {
            payload.append(",\"externalRefId\":\"").append(escapeJson(externalRefId)).append("\"");
        }
        payload.append(",\"operations\":\"").append(escapeJson(operations)).append("\"");
        if (expiresAt != null && !expiresAt.isEmpty()) {
            payload.append(",\"expiresAt\":\"").append(escapeJson(expiresAt)).append("\"");
        }
        if (expiresInDuration != null && expiresInUnit != null && !expiresInUnit.isEmpty()) {
            payload.append(",\"expiresIn\":{\"duration\":").append(expiresInDuration)
                    .append(",\"unit\":\"").append(escapeJson(expiresInUnit)).append("\"}");
        }
        if (displayName != null && !displayName.isEmpty()) {
            payload.append(",\"displayName\":\"").append(escapeJson(displayName)).append("\"");
        }

        String message = "{\"type\":\"" + EVENT_APIKEY_CREATED + "\",\"payload\":{" + payload + "},\"timestamp\":\""
                + escapeJson(timestamp) + "\",\"correlationId\":\"" + escapeJson(correlationId) + "\",\"userId\":\""
                + escapeJson(userIdSafe) + "\"}";
        sendToAllGateways(message, EVENT_APIKEY_CREATED);
    }

    /**
     * Broadcast apikey.updated to all connected platform gateways.
     * Payload: apiId, keyName, apiKey (plain), externalRefId, operations, displayName;
     * optional: expiresAt, expiresIn, userId.
     */
    public void broadcastAPIKeyUpdated(String apiId, String keyName, String apiKey, String externalRefId,
                                      String operations, String displayName, String expiresAt,
                                      Integer expiresInDuration, String expiresInUnit, String userId) {
        if (apiId == null || keyName == null || apiKey == null || displayName == null) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping apikey.updated broadcast: missing required field (apiId, keyName, apiKey, displayName)");
            }
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = userId != null ? userId : "";
        String externalRefIdSafe = externalRefId != null ? externalRefId : "";
        String operationsSafe = operations != null ? operations : "";
        String apiKeyHashes = buildApiKeyHashesJson(apiKey);
        String maskedApiKey = maskApiKey(apiKey);

        StringBuilder payload = new StringBuilder();
        payload.append("\"apiId\":\"").append(escapeJson(apiId)).append("\"");
        payload.append(",\"keyName\":\"").append(escapeJson(keyName)).append("\"");
        payload.append(",\"apiKey\":\"").append(escapeJson(apiKey)).append("\"");
        payload.append(",\"apiKeyHashes\":\"").append(escapeJson(apiKeyHashes)).append("\"");
        payload.append(",\"maskedApiKey\":\"").append(escapeJson(maskedApiKey)).append("\"");
        payload.append(",\"externalRefId\":\"").append(escapeJson(externalRefIdSafe)).append("\"");
        payload.append(",\"operations\":\"").append(escapeJson(operationsSafe)).append("\"");
        if (expiresAt != null && !expiresAt.isEmpty()) {
            payload.append(",\"expiresAt\":\"").append(escapeJson(expiresAt)).append("\"");
        }
        if (expiresInDuration != null && expiresInUnit != null && !expiresInUnit.isEmpty()) {
            payload.append(",\"expiresIn\":{\"duration\":").append(expiresInDuration)
                    .append(",\"unit\":\"").append(escapeJson(expiresInUnit)).append("\"}");
        }
        payload.append(",\"displayName\":\"").append(escapeJson(displayName)).append("\"");

        String message = "{\"type\":\"" + EVENT_APIKEY_UPDATED + "\",\"payload\":{" + payload + "},\"timestamp\":\""
                + escapeJson(timestamp) + "\",\"correlationId\":\"" + escapeJson(correlationId) + "\",\"userId\":\""
                + escapeJson(userIdSafe) + "\"}";
        sendToAllGateways(message, EVENT_APIKEY_UPDATED);
    }

    /**
     * Broadcast apikey.revoked to all connected platform gateways.
     * Payload: apiId, keyName; optional userId.
     */
    public void broadcastAPIKeyRevoked(String apiId, String keyName, String userId) {
        if (apiId == null || keyName == null || apiId.isEmpty() || keyName.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping apikey.revoked broadcast: apiId and keyName are required");
            }
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = userId != null ? userId : "";

        String payload = "\"apiId\":\"" + escapeJson(apiId) + "\",\"keyName\":\"" + escapeJson(keyName) + "\"";
        String message = "{\"type\":\"" + EVENT_APIKEY_REVOKED + "\",\"payload\":{" + payload + "},\"timestamp\":\""
                + escapeJson(timestamp) + "\",\"correlationId\":\"" + escapeJson(correlationId) + "\",\"userId\":\""
                + escapeJson(userIdSafe) + "\"}";
        sendToAllGateways(message, EVENT_APIKEY_REVOKED);
    }

    private static void sendToAllGateways(String message, String eventType) {
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        Set<String> gatewayIds = registry.getConnectedGatewayIds();
        if (gatewayIds.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No connected platform gateways; skipping " + eventType + " broadcast");
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Broadcasting " + eventType + " to " + gatewayIds.size() + " platform gateway(s)");
        }
        registry.sendToGateways(gatewayIds, message);
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c <= 0x1F) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    private static String buildApiKeyHashesJson(String apiKey) {
        if (apiKey == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return "{\"sha256\":\"" + toHex(hash) + "\"}";
        } catch (NoSuchAlgorithmException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to compute sha256 hash for api key event payload: " + e.getMessage());
            }
            return "";
        }
    }

    private static String toHex(byte[] data) {
        if (data == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }
        int keep = Math.min(4, apiKey.length());
        String suffix = apiKey.substring(apiKey.length() - keep);
        return "****" + suffix;
    }
}
