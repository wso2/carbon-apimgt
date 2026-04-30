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
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayAPIKeyEvents;
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

    public static PlatformGatewayAPIKeyEventBroadcaster getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayAPIKeyEventBroadcaster() {
    }

    /**
     * Broadcast apikey.created to platform gateways in the given organization.
     * Payload is kept aligned with the current gateway-controller contract.
     */
    public void broadcastAPIKeyCreated(PlatformGatewayAPIKeyEvents.Created event) {
        if (event == null || event.getOrganizationId() == null || event.getApiId() == null
                || event.getApiKey() == null || event.getKeyName() == null) {
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = event.getUserId() != null ? event.getUserId() : "";
        String apiKeyHashes = buildApiKeyHashesJson(event.getApiKey());
        String maskedApiKey = maskApiKey(event.getApiKey());

        PlatformGatewayWebSocketModels.ApiKeyCreatedPayload payload =
                new PlatformGatewayWebSocketModels.ApiKeyCreatedPayload(
                        event.getApiId(),
                        blankToNull(event.getKeyUuid()),
                        blankToNull(apiKeyHashes),
                        maskedApiKey,
                        event.getKeyName(),
                        blankToNull(event.getExternalRefId()),
                        blankToNull(event.getExpiresAt()),
                        buildExpiresIn(event.getExpiresInDuration(), event.getExpiresInUnit()));
        String message = PlatformGatewayWebSocketJsonUtil.toJson(
                new PlatformGatewayWebSocketModels.EventEnvelope<>(
                        PlatformGatewayWebSocketConstants.EVENT_APIKEY_CREATED, payload, timestamp,
                        correlationId, userIdSafe));
        dispatchToOrganizationGateways(event.getOrganizationId(), message,
                PlatformGatewayWebSocketConstants.EVENT_APIKEY_CREATED,
                apiKeyEventMetadata(event.getApiId(), event.getKeyUuid(), event.getKeyName()));
    }

    /**
     * Broadcast apikey.updated to platform gateways in the given organization.
     * Payload is kept aligned with the current gateway-controller contract.
     */
    public void broadcastAPIKeyUpdated(PlatformGatewayAPIKeyEvents.Updated event) {
        if (event == null || event.getOrganizationId() == null || event.getApiId() == null
                || event.getKeyName() == null || event.getApiKey() == null) {
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = event.getUserId() != null ? event.getUserId() : "";
        String apiKeyHashes = buildApiKeyHashesJson(event.getApiKey());
        String maskedApiKey = maskApiKey(event.getApiKey());

        PlatformGatewayWebSocketModels.ApiKeyUpdatedPayload payload =
                new PlatformGatewayWebSocketModels.ApiKeyUpdatedPayload(
                        event.getApiId(),
                        event.getKeyName(),
                        blankToNull(apiKeyHashes),
                        maskedApiKey,
                        blankToNull(event.getExternalRefId()),
                        blankToNull(event.getExpiresAt()),
                        buildExpiresIn(event.getExpiresInDuration(), event.getExpiresInUnit()));
        String message = PlatformGatewayWebSocketJsonUtil.toJson(
                new PlatformGatewayWebSocketModels.EventEnvelope<>(
                        PlatformGatewayWebSocketConstants.EVENT_APIKEY_UPDATED, payload, timestamp,
                        correlationId, userIdSafe));
        dispatchToOrganizationGateways(event.getOrganizationId(), message,
                PlatformGatewayWebSocketConstants.EVENT_APIKEY_UPDATED,
                apiKeyEventMetadata(event.getApiId(), event.getKeyUuid(), event.getKeyName()));
    }

    /**
     * Broadcast apikey.revoked to platform gateways in the given organization.
     * Payload: apiId, keyName; optional userId.
     */
    public void broadcastAPIKeyRevoked(PlatformGatewayAPIKeyEvents.Revoked event) {
        if (event == null || event.getOrganizationId() == null || event.getApiId() == null
                || event.getKeyName() == null || event.getApiId().isEmpty() || event.getKeyName().isEmpty()) {
            return;
        }
        String timestamp = Instant.now().toString();
        String correlationId = UUID.randomUUID().toString();
        String userIdSafe = event.getUserId() != null ? event.getUserId() : "";
        PlatformGatewayWebSocketModels.ApiKeyRevokedPayload payload =
                new PlatformGatewayWebSocketModels.ApiKeyRevokedPayload(event.getApiId(), event.getKeyName());
        String message = PlatformGatewayWebSocketJsonUtil.toJson(
                new PlatformGatewayWebSocketModels.EventEnvelope<>(
                        PlatformGatewayWebSocketConstants.EVENT_APIKEY_REVOKED, payload, timestamp,
                        correlationId, userIdSafe));
        dispatchToOrganizationGateways(event.getOrganizationId(), message,
                PlatformGatewayWebSocketConstants.EVENT_APIKEY_REVOKED,
                apiKeyEventMetadata(event.getApiId(), null, event.getKeyName()));
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

    private static PlatformGatewayWebSocketModels.ApiKeyExpiresIn buildExpiresIn(Integer duration, String unit) {
        if (duration == null || unit == null || unit.isEmpty()) {
            return null;
        }
        return new PlatformGatewayWebSocketModels.ApiKeyExpiresIn(duration, unit);
    }

    private static String blankToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
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
