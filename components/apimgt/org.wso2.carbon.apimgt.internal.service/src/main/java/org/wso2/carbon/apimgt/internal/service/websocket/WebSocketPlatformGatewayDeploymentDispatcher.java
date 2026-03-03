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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayDeploymentDispatcher;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

/**
 * Pushes deploy/undeploy events to connected platform gateways via WebSocket.
 * Message format is aligned with API Platform (api-platform gateway-controller) so the same
 * gateway binary can work with on-prem APIM: type "api.deployed" / "api.undeployed" with
 * nested payload (apiId, deploymentId, vhost, etc.) and timestamp, correlationId.
 */
public class WebSocketPlatformGatewayDeploymentDispatcher implements PlatformGatewayDeploymentDispatcher {

    private static final Log log = LogFactory.getLog(WebSocketPlatformGatewayDeploymentDispatcher.class);

    @Override
    public void dispatchDeploy(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds) {
        if (log.isDebugEnabled()) {
            log.debug("Dispatching deploy to " + platformGatewayIds.size() + " platform gateway(s): apiId="
                    + event.getUuid());
        }
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        PlatformGatewayService platformGatewayService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        for (String gatewayId : platformGatewayIds) {
            String vhost = resolveVhost(platformGatewayService, gatewayId);
            String message = buildDeployMessage(event, vhost);
            registry.sendToGateways(Collections.singleton(gatewayId), message);
        }
    }

    @Override
    public void dispatchUndeploy(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds) {
        if (log.isDebugEnabled()) {
            log.debug("Dispatching undeploy to " + platformGatewayIds.size() + " platform gateway(s): apiId="
                    + event.getUuid());
        }
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        PlatformGatewayService platformGatewayService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        for (String gatewayId : platformGatewayIds) {
            String vhost = resolveVhost(platformGatewayService, gatewayId);
            String message = buildUndeployMessage(event, vhost);
            registry.sendToGateways(Collections.singleton(gatewayId), message);
        }
    }

    /**
     * Resolve vhost for the given gateway from platform gateway config; empty string if not available.
     */
    private static String resolveVhost(PlatformGatewayService platformGatewayService, String gatewayId) {
        if (platformGatewayService == null || StringUtils.isBlank(gatewayId)) {
            return "";
        }
        try {
            PlatformGateway gateway = platformGatewayService.getGatewayById(gatewayId);
            if (gateway != null && StringUtils.isNotBlank(gateway.getVhost())) {
                return gateway.getVhost().trim();
            }
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not resolve vhost for gateway " + gatewayId + ", using empty: " + e.getMessage());
            }
        }
        return "";
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
     * Build message in API Platform format: type "api.deployed", payload { apiId, deploymentId, vhost },
     * timestamp, correlationId. Gateway uses apiId to fetch API definition and deploy.
     * vhost is from the target platform gateway config so the gateway-controller can route correctly.
     */
    private static String buildDeployMessage(DeployAPIInGatewayEvent event, String vhost) {
        String timestamp = Instant.now().toString();
        String apiId = escapeJson(event.getUuid());
        String deploymentId = escapeJson(event.getEventId());
        String vhostEscaped = escapeJson(vhost != null ? vhost : "");
        return "{\"type\":\"api.deployed\",\"payload\":{\"apiId\":\"" + apiId + "\",\"deploymentId\":\""
                + deploymentId + "\",\"vhost\":\"" + vhostEscaped + "\"},\"timestamp\":\"" + escapeJson(timestamp)
                + "\",\"correlationId\":\"" + escapeJson(event.getEventId()) + "\"}";
    }

    /**
     * Build message in API Platform format: type "api.undeployed", payload { apiId, deploymentId, vhost },
     * timestamp, correlationId. Uses deploymentId to mirror deploy message format.
     * vhost is from the target platform gateway config so the gateway-controller can route correctly.
     */
    private static String buildUndeployMessage(DeployAPIInGatewayEvent event, String vhost) {
        String timestamp = Instant.now().toString();
        String apiId = escapeJson(event.getUuid());
        String deploymentId = escapeJson(event.getEventId());
        String vhostEscaped = escapeJson(vhost != null ? vhost : "");
        return "{\"type\":\"api.undeployed\",\"payload\":{\"apiId\":\"" + apiId + "\",\"deploymentId\":\""
                + deploymentId + "\",\"vhost\":\"" + vhostEscaped + "\"},\"timestamp\":\"" + escapeJson(timestamp)
                + "\",\"correlationId\":\"" + escapeJson(event.getEventId()) + "\"}";
    }
}
