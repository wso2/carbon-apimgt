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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.PlatformGatewayDeploymentEventService;
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayDeploymentDispatcher;
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayEventEnvelopeUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pushes deploy/undeploy events to connected platform gateways via WebSocket.
 * Message format is aligned with API Platform (api-platform gateway-controller) so the same
 * gateway binary can work with on-prem APIM: type "api.deployed" / "api.undeployed" / "api.deleted"
 * with nested payload and timestamp, correlationId.
 * api.undeployed = revision undeployed (config preserved). api.deleted = API removed from publisher (config removed).
 */
public class WebSocketPlatformGatewayDeploymentDispatcher implements PlatformGatewayDeploymentDispatcher {

    private static final Log log = LogFactory.getLog(WebSocketPlatformGatewayDeploymentDispatcher.class);

    @Override
    public void dispatchDeploy(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds) {
        log.info("Dispatching API deploy event for API: " + event.getName() + " to " + platformGatewayIds.size()
                + " platform gateways");
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        String apiId = event.getUuid();
        String revisionUuid = event.getEventId();
        for (String gatewayId : platformGatewayIds) {
            String message = buildDeployMessage(event);
            if (eventService != null) {
                try {
                    eventService.persistEvent(gatewayId, "api.deployed",
                            PlatformGatewayEventEnvelopeUtil.wrapForStorage(message,
                                    deploymentEventMetadata(apiId, revisionUuid)));
                } catch (Exception e) {
                    log.error("Failed to persist deploy event for gateway " + gatewayId + ": " + e.getMessage(), e);
                }
            }
            registry.sendToGateways(Collections.singleton(gatewayId), message);
        }
    }

    @Override
    public void dispatchUndeploy(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds) {
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        String apiId = event.getUuid();
        String revisionUuid = event.getEventId();
        for (String gatewayId : platformGatewayIds) {
            String message = buildUndeployMessage(event);
            if (eventService != null) {
                try {
                    eventService.persistEvent(gatewayId, "api.undeployed",
                            PlatformGatewayEventEnvelopeUtil.wrapForStorage(message,
                                    deploymentEventMetadata(apiId, revisionUuid)));
                } catch (Exception e) {
                    log.error("Failed to persist undeploy event for gateway " + gatewayId + ": " + e.getMessage(), e);
                }
            }
            registry.sendToGateways(Collections.singleton(gatewayId), message);
        }
    }

    @Override
    public void dispatchDelete(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds) {
        log.info("Dispatching API delete event for API: " + event.getName() + " to " + platformGatewayIds.size()
                + " platform gateways");
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        String apiId = event.getUuid();
        String revisionUuid = event.getEventId();
        for (String gatewayId : platformGatewayIds) {
            String message = buildDeleteMessage(event);
            if (eventService != null) {
                try {
                    eventService.persistEvent(gatewayId, "api.deleted",
                            PlatformGatewayEventEnvelopeUtil.wrapForStorage(message,
                                    deploymentEventMetadata(apiId, revisionUuid)));
                } catch (Exception e) {
                    log.error("Failed to persist delete event for gateway " + gatewayId + ": " + e.getMessage(), e);
                }
            }
            registry.sendToGateways(Collections.singleton(gatewayId), message);
        }
    }

    @Override
    public void closeGatewayConnection(String gatewayId) {
        if (StringUtils.isBlank(gatewayId)) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Closing WebSocket connection for deleted gateway: " + gatewayId);
        }
        // Force-close the session; gateway will see connection close and log "Connection lost" (no new message type)
        PlatformGatewaySessionRegistry.getInstance().closeAndUnregister(gatewayId);
    }

    /**
     * Arbitrary key/value pairs persisted in the envelope {@code metadata} object (not sent on the WebSocket wire).
     */
    private static Map<String, String> deploymentEventMetadata(String apiId, String revisionUuid) {
        Map<String, String> meta = new LinkedHashMap<>(2);
        if (StringUtils.isNotBlank(apiId)) {
            meta.put("apiId", apiId.trim());
        }
        if (StringUtils.isNotBlank(revisionUuid)) {
            meta.put("revisionUuid", revisionUuid.trim());
        }
        return meta;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
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

    /**
     * Build message in the exact API Platform gateway-controller format:
     * type "api.deployed", payload { apiId, deploymentId, performedAt }, timestamp, correlationId.
     */
    private static String buildDeployMessage(DeployAPIInGatewayEvent event) {
        String timestamp = Instant.now().toString();
        String apiId = escapeJson(event.getUuid());
        String deploymentId = escapeJson(event.getEventId());
        String performedAt = escapeJson(timestamp);
        return "{\"type\":\"api.deployed\",\"payload\":{\"apiId\":\"" + apiId + "\",\"deploymentId\":\""
                + deploymentId + "\",\"performedAt\":\"" + performedAt + "\"},\"timestamp\":\""
                + escapeJson(timestamp)
                + "\",\"correlationId\":\"" + escapeJson(event.getEventId()) + "\"}";
    }

    /**
     * Build message in the exact API Platform gateway-controller format:
     * type "api.undeployed", payload { apiId, deploymentId, performedAt }, timestamp, correlationId.
     */
    private static String buildUndeployMessage(DeployAPIInGatewayEvent event) {
        String timestamp = Instant.now().toString();
        String apiId = escapeJson(event.getUuid());
        String deploymentId = escapeJson(event.getEventId());
        String performedAt = escapeJson(timestamp);
        return "{\"type\":\"api.undeployed\",\"payload\":{\"apiId\":\"" + apiId + "\",\"deploymentId\":\""
                + deploymentId + "\",\"performedAt\":\"" + performedAt + "\"},\"timestamp\":\""
                + escapeJson(timestamp)
                + "\",\"correlationId\":\"" + escapeJson(event.getEventId()) + "\"}";
    }

    /**
     * Build message in the exact API Platform gateway-controller format:
     * type "api.deleted", payload { apiId }, timestamp, correlationId.
     */
    private static String buildDeleteMessage(DeployAPIInGatewayEvent event) {
        String timestamp = Instant.now().toString();
        String apiId = escapeJson(event.getUuid());
        String correlationId = escapeJson(event.getEventId());
        return "{\"type\":\"api.deleted\",\"payload\":{\"apiId\":\"" + apiId
                + "\"},\"timestamp\":\"" + escapeJson(timestamp) + "\",\"correlationId\":\"" + correlationId + "\"}";
    }
}
