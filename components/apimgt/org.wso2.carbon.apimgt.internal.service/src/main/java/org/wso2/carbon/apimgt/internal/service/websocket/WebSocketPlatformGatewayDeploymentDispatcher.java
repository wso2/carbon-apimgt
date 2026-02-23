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
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayDeploymentDispatcher;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;

import java.time.Instant;
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
        String message = buildDeployMessage(event);
        if (log.isDebugEnabled()) {
            log.debug("Dispatching deploy to " + platformGatewayIds.size() + " platform gateway(s): apiId="
                    + event.getUuid());
        }
        PlatformGatewaySessionRegistry.getInstance().sendToGateways(platformGatewayIds, message);
    }

    @Override
    public void dispatchUndeploy(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds) {
        String message = buildUndeployMessage(event);
        if (log.isDebugEnabled()) {
            log.debug("Dispatching undeploy to " + platformGatewayIds.size() + " platform gateway(s): apiId="
                    + event.getUuid());
        }
        PlatformGatewaySessionRegistry.getInstance().sendToGateways(platformGatewayIds, message);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Build message in API Platform format: type "api.deployed", payload { apiId, deploymentId, vhost },
     * timestamp, correlationId. Gateway uses apiId to fetch API definition and deploy.
     */
    private static String buildDeployMessage(DeployAPIInGatewayEvent event) {
        String timestamp = Instant.now().toString();
        String apiId = escapeJson(event.getUuid());
        String deploymentId = escapeJson(event.getEventId());
        String vhost = "";
        return "{\"type\":\"api.deployed\",\"payload\":{\"apiId\":\"" + apiId + "\",\"deploymentId\":\""
                + deploymentId + "\",\"vhost\":\"" + vhost + "\"},\"timestamp\":\"" + escapeJson(timestamp)
                + "\",\"correlationId\":\"" + escapeJson(event.getEventId()) + "\"}";
    }

    /**
     * Build message in API Platform format: type "api.undeployed", payload { apiId, environment, vhost },
     * timestamp, correlationId.
     */
    private static String buildUndeployMessage(DeployAPIInGatewayEvent event) {
        String timestamp = Instant.now().toString();
        String apiId = escapeJson(event.getUuid());
        String environment = escapeJson(event.getContext());
        String vhost = "";
        return "{\"type\":\"api.undeployed\",\"payload\":{\"apiId\":\"" + apiId + "\",\"environment\":\""
                + environment + "\",\"vhost\":\"" + vhost + "\"},\"timestamp\":\"" + escapeJson(timestamp)
                + "\",\"correlationId\":\"" + escapeJson(event.getEventId()) + "\"}";
    }
}
