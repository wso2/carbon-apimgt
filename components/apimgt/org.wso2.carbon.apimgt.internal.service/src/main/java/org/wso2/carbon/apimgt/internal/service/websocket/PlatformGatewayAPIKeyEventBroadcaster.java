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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayDeploymentEventService;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayEventEnvelopeUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Broadcasts API key lifecycle events (apikey.created, apikey.updated, apikey.revoked) to
 * platform gateways via WebSocket. Events are also persisted to AM_GW_PLATFORM_EVENT so gateways
 * connected to a different control-plane node still receive them in active-active deployments.
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
     * Broadcast apikey.created to platform gateways in the given organization.
     * Payload is kept aligned with the current gateway-controller contract.
     */
    public void broadcastAPIKeyCreated(String organizationId, String apiId, String keyUuid, String apiKey,
                                      String name, String operations, String externalRefId, String expiresAt,
                                      Integer expiresInDuration,
                                      String expiresInUnit, String displayName, String userId) {
        if (organizationId == null || apiId == null || apiKey == null || name == null || operations == null) {
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = userId != null ? userId : "";
        String apiKeyHashes = buildApiKeyHashesJson(apiKey);
        String maskedApiKey = maskApiKey(apiKey);

        StringBuilder payload = new StringBuilder();
        payload.append("\"apiId\":\"").append(escapeJson(apiId)).append("\"");
        if (keyUuid != null && !keyUuid.isEmpty()) {
            payload.append(",\"uuid\":\"").append(escapeJson(keyUuid)).append("\"");
        }
        if (apiKeyHashes != null && !apiKeyHashes.isEmpty()) {
            payload.append(",\"apiKeyHashes\":\"").append(escapeJson(apiKeyHashes)).append("\"");
        }
        payload.append(",\"maskedApiKey\":\"").append(escapeJson(maskedApiKey)).append("\"");
        payload.append(",\"name\":\"").append(escapeJson(name)).append("\"");
        if (externalRefId != null && !externalRefId.isEmpty()) {
            payload.append(",\"externalRefId\":\"").append(escapeJson(externalRefId)).append("\"");
        }
        if (expiresAt != null && !expiresAt.isEmpty()) {
            payload.append(",\"expiresAt\":\"").append(escapeJson(expiresAt)).append("\"");
        }
        if (expiresInDuration != null && expiresInUnit != null && !expiresInUnit.isEmpty()) {
            payload.append(",\"expiresIn\":{\"duration\":").append(expiresInDuration)
                    .append(",\"unit\":\"").append(escapeJson(expiresInUnit)).append("\"}");
        }

        String message = "{\"type\":\"" + EVENT_APIKEY_CREATED + "\",\"payload\":{" + payload + "},\"timestamp\":\""
                + escapeJson(timestamp) + "\",\"correlationId\":\"" + escapeJson(correlationId) + "\",\"userId\":\""
                + escapeJson(userIdSafe) + "\"}";
        dispatchToOrganizationGateways(organizationId, message, EVENT_APIKEY_CREATED,
                apiKeyEventMetadata(apiId, keyUuid, name));
    }

    /**
     * Broadcast apikey.updated to platform gateways in the given organization.
     * Payload is kept aligned with the current gateway-controller contract.
     */
    public void broadcastAPIKeyUpdated(String organizationId, String apiId, String keyUuid, String keyName,
                                      String apiKey, String externalRefId, String operations, String displayName,
                                      String expiresAt,
                                      Integer expiresInDuration, String expiresInUnit, String userId) {
        if (organizationId == null || apiId == null || keyName == null || apiKey == null || displayName == null) {
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = userId != null ? userId : "";
        String externalRefIdSafe = externalRefId != null ? externalRefId : "";
        String apiKeyHashes = buildApiKeyHashesJson(apiKey);
        String maskedApiKey = maskApiKey(apiKey);

        StringBuilder payload = new StringBuilder();
        payload.append("\"apiId\":\"").append(escapeJson(apiId)).append("\"");
        payload.append(",\"keyName\":\"").append(escapeJson(keyName)).append("\"");
        if (apiKeyHashes != null && !apiKeyHashes.isEmpty()) {
            payload.append(",\"apiKeyHashes\":\"").append(escapeJson(apiKeyHashes)).append("\"");
        }
        payload.append(",\"maskedApiKey\":\"").append(escapeJson(maskedApiKey)).append("\"");
        payload.append(",\"externalRefId\":\"").append(escapeJson(externalRefIdSafe)).append("\"");
        if (expiresAt != null && !expiresAt.isEmpty()) {
            payload.append(",\"expiresAt\":\"").append(escapeJson(expiresAt)).append("\"");
        }
        if (expiresInDuration != null && expiresInUnit != null && !expiresInUnit.isEmpty()) {
            payload.append(",\"expiresIn\":{\"duration\":").append(expiresInDuration)
                    .append(",\"unit\":\"").append(escapeJson(expiresInUnit)).append("\"}");
        }
        String message = "{\"type\":\"" + EVENT_APIKEY_UPDATED + "\",\"payload\":{" + payload + "},\"timestamp\":\""
                + escapeJson(timestamp) + "\",\"correlationId\":\"" + escapeJson(correlationId) + "\",\"userId\":\""
                + escapeJson(userIdSafe) + "\"}";
        dispatchToOrganizationGateways(organizationId, message, EVENT_APIKEY_UPDATED,
                apiKeyEventMetadata(apiId, keyUuid, keyName));
    }

    /**
     * Broadcast apikey.revoked to platform gateways in the given organization.
     * Payload: apiId, keyName; optional userId.
     */
    public void broadcastAPIKeyRevoked(String organizationId, String apiId, String keyName, String userId) {
        if (organizationId == null || apiId == null || keyName == null || apiId.isEmpty() || keyName.isEmpty()) {
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = userId != null ? userId : "";

        String payload = "\"apiId\":\"" + escapeJson(apiId) + "\",\"keyName\":\"" + escapeJson(keyName) + "\"";
        String message = "{\"type\":\"" + EVENT_APIKEY_REVOKED + "\",\"payload\":{" + payload + "},\"timestamp\":\""
                + escapeJson(timestamp) + "\",\"correlationId\":\"" + escapeJson(correlationId) + "\",\"userId\":\""
                + escapeJson(userIdSafe) + "\"}";
        dispatchToOrganizationGateways(organizationId, message, EVENT_APIKEY_REVOKED,
                apiKeyEventMetadata(apiId, null, keyName));
    }

    private static void dispatchToOrganizationGateways(String organizationId, String message, String eventType,
                                                       Map<String, String> metadata) {
        Set<String> gatewayIds = resolveTargetGatewayIds(organizationId);
        if (gatewayIds.isEmpty()) {
            return;
        }
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        if (eventService != null) {
            for (String gatewayId : gatewayIds) {
                try {
                    eventService.persistEvent(gatewayId, eventType,
                            PlatformGatewayEventEnvelopeUtil.wrapForStorage(message, metadata));
                } catch (APIManagementException e) {
                    log.error("Failed to persist " + eventType + " event for gateway " + gatewayId + ": "
                            + e.getMessage(), e);
                }
            }
        }
        PlatformGatewaySessionRegistry.getInstance().sendToGateways(gatewayIds, message);
    }

    private static Set<String> resolveTargetGatewayIds(String organizationId) {
        if (organizationId == null || organizationId.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        PlatformGatewayService platformGatewayService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        if (platformGatewayService == null) {
            return java.util.Collections.emptySet();
        }
        try {
            List<PlatformGateway> gateways = platformGatewayService.listGatewaysByOrganizationWithInstance(organizationId);
            Set<String> gatewayIds = new LinkedHashSet<>(gateways.size());
            for (PlatformGateway gateway : gateways) {
                if (gateway != null && gateway.getId() != null && !gateway.getId().isEmpty()) {
                    gatewayIds.add(gateway.getId());
                }
            }
            return gatewayIds;
        } catch (APIManagementException e) {
            log.warn("Failed to resolve platform gateways for organization " + organizationId + ": "
                    + e.getMessage(), e);
            return java.util.Collections.emptySet();
        }
    }

    private static Map<String, String> apiKeyEventMetadata(String apiId, String keyUuid, String keyName) {
        Map<String, String> meta = new LinkedHashMap<>(3);
        if (apiId != null && !apiId.isEmpty()) {
            meta.put("apiId", apiId);
        }
        if (keyUuid != null && !keyUuid.isEmpty()) {
            meta.put("keyUuid", keyUuid);
        }
        if (keyName != null && !keyName.isEmpty()) {
            meta.put("keyName", keyName);
        }
        return meta;
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
            log.warn("Failed to compute sha256 hash for api key event payload: " + e.getMessage(), e);
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
