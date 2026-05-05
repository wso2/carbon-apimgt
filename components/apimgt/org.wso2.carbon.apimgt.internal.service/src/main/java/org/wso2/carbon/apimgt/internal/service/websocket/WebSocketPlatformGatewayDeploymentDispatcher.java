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
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayDeploymentIdUtil;

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
        if (log.isDebugEnabled()) {
            log.debug("Dispatching API deploy event for API: " + event.getName() + " to " + platformGatewayIds.size()
                    + " platform gateways");
        }
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        String apiId = event.getUuid();
        String revisionUuid = event.getEventId();
        for (String gatewayId : platformGatewayIds) {
            String deploymentId = resolveDeploymentId(event, gatewayId, apiId, revisionUuid);
            String message = buildDeployMessage(event, deploymentId);
            if (eventService != null) {
                try {
                    eventService.persistEvent(gatewayId, PlatformGatewayWebSocketConstants.EVENT_API_DEPLOYED,
                            PlatformGatewayEventEnvelopeUtil.wrapForStorage(message,
                                    deploymentEventMetadata(apiId, revisionUuid, deploymentId)));
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
            String deploymentId = resolveDeploymentId(event, gatewayId, apiId, revisionUuid);
            String message = buildUndeployMessage(event, deploymentId);
            if (eventService != null) {
                try {
                    eventService.persistEvent(gatewayId, PlatformGatewayWebSocketConstants.EVENT_API_UNDEPLOYED,
                            PlatformGatewayEventEnvelopeUtil.wrapForStorage(message,
                                    deploymentEventMetadata(apiId, revisionUuid, deploymentId)));
                } catch (Exception e) {
                    log.error("Failed to persist undeploy event for gateway " + gatewayId + ": " + e.getMessage(), e);
                }
            }
            registry.sendToGateways(Collections.singleton(gatewayId), message);
        }
    }

    @Override
    public void dispatchDelete(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds) {
        if (log.isDebugEnabled()) {
            log.debug("Dispatching API delete event for API: " + event.getName() + " to " + platformGatewayIds.size()
                    + " platform gateways");
        }
        PlatformGatewaySessionRegistry registry = PlatformGatewaySessionRegistry.getInstance();
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        String apiId = event.getUuid();
        String revisionUuid = event.getEventId();
        for (String gatewayId : platformGatewayIds) {
            String message = buildDeleteMessage(event);
            if (eventService != null) {
                try {
                    eventService.persistEvent(gatewayId, PlatformGatewayWebSocketConstants.EVENT_API_DELETED,
                            PlatformGatewayEventEnvelopeUtil.wrapForStorage(message,
                                    deploymentEventMetadata(apiId, revisionUuid, null)));
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
        // Force-close the session; gateway will see the connection close and log "Connection lost" (no new message type)
        PlatformGatewaySessionRegistry.getInstance().closeAndUnregister(gatewayId);
    }

    /**
     * Arbitrary key/value pairs persisted in the envelope {@code metadata} object (not sent on the WebSocket wire).
     */
    private static Map<String, String> deploymentEventMetadata(String apiId, String revisionUuid, String deploymentId) {
        Map<String, String> meta = new LinkedHashMap<>(3);
        if (StringUtils.isNotBlank(apiId)) {
            meta.put("apiId", apiId.trim());
        }
        if (StringUtils.isNotBlank(revisionUuid)) {
            meta.put("revisionUuid", revisionUuid.trim());
        }
        if (StringUtils.isNotBlank(deploymentId)) {
            meta.put("deploymentId", deploymentId.trim());
        }
        return meta;
    }

    /**
     * Resolves the deployment ID for a specific API, gateway, and revision. If an explicit deployment ID
     * is provided for the given gateway in the platform deployment event, it is used. Otherwise, a new
     * deployment ID is generated based on the API ID, gateway ID, and revision UUID.
     *
     * @param event The deployment event containing details of the API and platform gateway.
     * @param gatewayId The unique identifier of the gateway where the API is being deployed.
     * @param apiId The unique identifier of the API being deployed.
     * @param revisionUuid The unique identifier of the API revision to be deployed.
     * @return The resolved deployment ID, either explicitly provided or newly generated.
     */
    private static String resolveDeploymentId(DeployAPIInGatewayEvent event, String gatewayId, String apiId,
                                              String revisionUuid) {
        Map<String, String> explicitDeploymentIds = event.getPlatformGatewayDeploymentIds();
        if (explicitDeploymentIds != null) {
            String explicitDeploymentId = explicitDeploymentIds.get(gatewayId);
            if (StringUtils.isNotBlank(explicitDeploymentId)) {
                return explicitDeploymentId.trim();
            }
        }
        return PlatformGatewayDeploymentIdUtil.generate(apiId, gatewayId, revisionUuid);
    }

    /**
     * Resolves an operation event identifier based on the provided action and deployment ID.
     * If both the action and deployment ID are non-blank, their trimmed concatenation is returned in the format "action:deploymentId".
     * Otherwise, the fallback event ID is returned.
     *
     * @param action The action name associated with the operation event. This should be a non-blank string.
     * @param deploymentId The unique identifier of the deployment associated with the operation event. This should be a non-blank string.
     * @param fallbackEventId The identifier to return if either the action or deployment ID is blank.
     * @return A concatenated string in the format "action:deploymentId" if both action and deployment ID are non-blank.
     *         Otherwise, returns the fallback event ID.
     */
    private static String resolveOperationEventId(String action, String deploymentId, String fallbackEventId) {
        if (StringUtils.isNotBlank(action) && StringUtils.isNotBlank(deploymentId)) {
            return action.trim() + ":" + deploymentId.trim();
        }
        return fallbackEventId;
    }

    /**
     * Build message in the exact API Platform gateway-controller format:
     * type "api.deployed", payload { apiId, deploymentId, performedAt }, timestamp, correlationId.
     */
    private static String buildDeployMessage(DeployAPIInGatewayEvent event, String deploymentId) {
        String timestamp = Instant.now().toString();
        String operationEventId = resolveOperationEventId("deploy", deploymentId, event.getEventId());
        PlatformGatewayWebSocketModels.ApiDeploymentPayload payload =
                new PlatformGatewayWebSocketModels.ApiDeploymentPayload(event.getUuid(), deploymentId, timestamp);
        return PlatformGatewayWebSocketJsonUtil.toJson(
                new PlatformGatewayWebSocketModels.EventEnvelope<>(
                        PlatformGatewayWebSocketConstants.EVENT_API_DEPLOYED, payload, timestamp,
                        operationEventId, null));
    }

    /**
     * Build message in the exact API Platform gateway-controller format:
     * type "api.undeployed", payload { apiId, deploymentId, performedAt }, timestamp, correlationId.
     */
    private static String buildUndeployMessage(DeployAPIInGatewayEvent event, String deploymentId) {
        String timestamp = Instant.now().toString();
        String operationEventId = resolveOperationEventId("undeploy", deploymentId, event.getEventId());
        PlatformGatewayWebSocketModels.ApiDeploymentPayload payload =
                new PlatformGatewayWebSocketModels.ApiDeploymentPayload(event.getUuid(), deploymentId, timestamp);
        return PlatformGatewayWebSocketJsonUtil.toJson(
                new PlatformGatewayWebSocketModels.EventEnvelope<>(
                        PlatformGatewayWebSocketConstants.EVENT_API_UNDEPLOYED, payload, timestamp,
                        operationEventId, null));
    }

    /**
     * Build the message in the exact API Platform gateway-controller format:
     * type "api.deleted", payload { apiId }, timestamp, correlationId.
     */
    private static String buildDeleteMessage(DeployAPIInGatewayEvent event) {
        String timestamp = Instant.now().toString();
        PlatformGatewayWebSocketModels.ApiDeletePayload payload =
                new PlatformGatewayWebSocketModels.ApiDeletePayload(event.getUuid());
        return PlatformGatewayWebSocketJsonUtil.toJson(
                new PlatformGatewayWebSocketModels.EventEnvelope<>(
                        PlatformGatewayWebSocketConstants.EVENT_API_DELETED, payload, timestamp,
                        event.getEventId(), null));
    }
}
