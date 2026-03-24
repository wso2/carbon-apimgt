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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayDeploymentEventService;
import org.wso2.carbon.apimgt.api.model.PlatformGatewayDeploymentEventRecord;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.dto.ConnectGatewayConfig;
import org.wso2.carbon.apimgt.impl.dto.PlatformGatewayConnectConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentListDTO;
import org.wso2.carbon.apimgt.internal.service.impl.NotifyApiDeploymentStatusApiServiceImpl;
import org.wso2.carbon.apimgt.impl.service.PlatformGatewayServiceImpl;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint for platform gateway control plane connections.
 * Path: /ws/gateways/connect (relative to Internal Data Service context, e.g. /internal/data/v1).
 * <p>
 * The gateway must send the registration token in the {@code api-key} HTTP header when opening
 * the WebSocket. The token is verified via {@link PlatformGatewayTokenUtil#verifyToken(String)}.
 * If not found in the database, the token is checked against
 * {@code [[apim.universal_gateway.connect]]} {@code registration_token} (deployment.toml); when
 * it matches, a new platform gateway is created and the connection is accepted (e.g. for migrating
 * an existing gateway to a new control plane). On success, the connection is associated with the
 * gateway and the gateway's active status is set to true; on close it is set to false. On
 * verification failure, the connection is closed with code 4401 (Unauthorized).
 */
@ServerEndpoint(
        value = "/ws/gateways/connect",
        configurator = GatewayConnectConfigurator.class
)
public class GatewayConnectEndpoint {

    private static final Log log = LogFactory.getLog(GatewayConnectEndpoint.class);

    /**
     * Close code for unauthorized (invalid or missing registration token). Within 4xxx private use range.
     */
    public static final int CLOSE_UNAUTHORIZED = 4401;

    /**
     * Session user property key for the verified platform gateway.
     */
    public static final String GATEWAY_PROPERTY = "platformGateway";

    /**
     * Session user property key for the heartbeat scheduled future (cancel on close).
     */
    private static final String HEARTBEAT_FUTURE_PROPERTY = "heartbeatFuture";

    /**
     * Session user property key for the deployment-event polling scheduled future (cancel on close).
     */
    private static final String DEPLOYMENT_POLL_FUTURE_PROPERTY = "deploymentPollFuture";

    /** Interval for server-to-client WebSocket PING (client timeout is 35s). */
    private static final long HEARTBEAT_INTERVAL_MS = 15_000;

    /**
     * Poll interval for pushing newly persisted multi-CP deployment events to the gateway
     * while the WebSocket session stays open (update "immediately", no reconnect needed).
     */
    private static final long DEPLOYMENT_EVENT_POLL_INTERVAL_MS = 2_000;

    /** Reusable empty payload for PING (avoids allocating a new ByteBuffer every interval). */
    private static final ByteBuffer EMPTY_PING_PAYLOAD = ByteBuffer.allocate(0);

    /** Shared scheduler for all gateway heartbeat pings (bounded thread count). */
    private static final ScheduledExecutorService HEARTBEAT_SCHEDULER =
            Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "gateway-ws-heartbeat");
                t.setDaemon(true);
                return t;
            });

    /** Dedicated scheduler for multi-CP deployment event polling/push (bounded thread count). */
    private static final ScheduledExecutorService DEPLOYMENT_EVENT_POLL_SCHEDULER =
            Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r, "gateway-ws-deployment-poll");
                t.setDaemon(true);
                return t;
            });

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String apiKey = (String) config.getUserProperties().get(GatewayConnectConfigurator.API_KEY_PROPERTY);
        if (apiKey == null || apiKey.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Gateway WebSocket connection rejected: missing api-key header");
            }
            closeWithUnauthorized(session, "API key is required. Provide 'api-key' header.");
            return;
        }

        PlatformGatewayDAO.PlatformGateway gateway;
        try {
            gateway = PlatformGatewayTokenUtil.verifyToken(apiKey);
        } catch (APIManagementException | NoSuchAlgorithmException e) {
            if (log.isDebugEnabled()) {
                log.debug("Gateway WebSocket token verification failed: " + e.getMessage());
            }
            closeWithUnauthorized(session, "Invalid or expired API key");
            return;
        }

        if (gateway == null) {
            // Accept token from [[apim.universal_gateway.connect]] registration_token (create gateway on first connect)
            log.info("Gateway WebSocket token not in DB; checking [[apim.universal_gateway.connect]] config");
            ConnectGatewayConfig matchedEntry = null;
            PlatformGatewayConnectConfig connectConfig = null;
            try {
                connectConfig = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getPlatformGatewayConnectConfig();
                if (connectConfig != null) {
                    List<ConnectGatewayConfig> list = connectConfig.getConnectGateways();
                    if (list == null || list.isEmpty()) {
                        log.info("Connect config present but ConnectGateways list is empty; ensure api-manager.xml " +
                                "contains PlatformGatewayConnectConfiguration/ConnectGateways from deployment.toml");
                    } else {
                        for (ConnectGatewayConfig entry : list) {
                            if (entry != null && StringUtils.isNotBlank(entry.getRegistrationToken())
                                    && apiKey.trim().equals(entry.getRegistrationToken().trim())) {
                                matchedEntry = entry;
                                break;
                            }
                        }
                        if (matchedEntry == null) {
                            log.info("No [[apim.universal_gateway.connect]] entry matched api-key; check " +
                                    "registration_token in deployment.toml and that api-manager.xml was generated with it");
                        }
                    }
                } else {
                    log.info("Platform gateway connect config is null; ensure api-manager.xml has " +
                            "PlatformGatewayConnectConfiguration (from deployment.toml apim.universal_gateway.connect)");
                }
            } catch (Exception e) {
                log.warn("Could not get platform gateway connect config: " + e.getMessage(), e);
            }
            if (matchedEntry != null && connectConfig != null) {
                String newGatewayId = UUID.randomUUID().toString();
                if (PlatformGatewayServiceImpl.ensurePlatformGatewayFromConnectToken(
                        connectConfig, newGatewayId, matchedEntry)) {
                    try {
                        gateway = PlatformGatewayTokenUtil.verifyToken(apiKey);
                    } catch (APIManagementException | NoSuchAlgorithmException e) {
                        log.warn("Re-verify after connect-with-token failed: " + e.getMessage());
                        closeWithUnauthorized(session, "Invalid or expired API key");
                        return;
                    }
                } else {
                    log.warn("Connect-with-token gateway creation failed; ensure registration_token is " +
                            "tokenId.plainToken format and url is set in [[apim.universal_gateway.connect]]");
                }
            }
            if (gateway == null) {
                log.info("Gateway WebSocket connection rejected: token did not match any gateway or config");
                closeWithUnauthorized(session, "Invalid or expired API key");
                return;
            }
        }

        session.getUserProperties().put(GATEWAY_PROPERTY, gateway);

        PlatformGatewaySessionRegistry.getInstance().register(gateway.id, session);

        try {
            PlatformGatewayServiceImpl.getInstance().updateGatewayActiveStatus(gateway.id, gateway.organizationId, true);
            if (log.isDebugEnabled()) {
                log.debug("Gateway active status updated to true: gatewayId=" + gateway.id);
            }
        } catch (APIManagementException e) {
            log.warn("Failed to update gateway active status to true: gatewayId=" + gateway.id + ", error="
                    + e.getMessage());
        }

        sendConnectionAck(session, gateway.id);

        sendPendingDeploymentEvents(session, gateway.id);

        // Push any platform events persisted by other CP nodes while this gateway stays connected.
        startDeploymentEventPolling(session, gateway.id);

        startHeartbeat(session);

        log.info("Gateway WebSocket connection established: gatewayId=" + gateway.id + " name=" + gateway.name);
    }

    /**
     * Push pending platform events for this gateway (multi-CP sync).
     * Claims a batch before sending so two CP nodes do not race to deliver the same row.
     * Events are marked delivered only after a successful WebSocket send.
     */
    private static void sendPendingDeploymentEvents(Session session, String gatewayId) {
        PlatformGatewayDeploymentEventService eventService =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentEventService();
        if (eventService == null) {
            return;
        }
        try {
            List<PlatformGatewayDeploymentEventRecord> events =
                    eventService.getAndMarkDeliveredPendingEventsForGateway(gatewayId);
            List<String> idsToMark = new ArrayList<>(events.size());
            for (PlatformGatewayDeploymentEventRecord event : events) {
                if (!session.isOpen()) {
                    break;
                }
                try {
                    session.getBasicRemote().sendText(event.getPayload());
                    if (event.getId() != null) {
                        idsToMark.add(event.getId());
                    }
                } catch (IOException e) {
                    log.warn("Failed to send pending deployment event to gateway " + gatewayId + ": "
                            + e.getMessage());
                    // Do not mark this event delivered; it stays pending for next connect
                    break;
                }
            }
            if (!idsToMark.isEmpty()) {
                eventService.markDelivered(idsToMark);
            }
        } catch (APIManagementException e) {
            log.warn("Failed to get pending deployment events for gateway " + gatewayId + ": " + e.getMessage());
        }
    }

    /**
     * Poll for pending platform events periodically and push them over the existing WS session.
     * This enables "immediate update without reconnect" in a multi-CP setup where events are persisted
     * by one CP while the gateway is connected to another.
     */
    private static void startDeploymentEventPolling(Session session, String gatewayId) {
        AtomicBoolean inFlight = new AtomicBoolean(false);
        ScheduledFuture<?> future = DEPLOYMENT_EVENT_POLL_SCHEDULER.scheduleAtFixedRate(() -> {
            if (!session.isOpen()) {
                return;
            }
            if (!inFlight.compareAndSet(false, true)) {
                return; // Skip if previous poll is still running
            }
            try {
                sendPendingDeploymentEvents(session, gatewayId);
            } finally {
                inFlight.set(false);
            }
        }, DEPLOYMENT_EVENT_POLL_INTERVAL_MS, DEPLOYMENT_EVENT_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
        session.getUserProperties().put(DEPLOYMENT_POLL_FUTURE_PROPERTY, future);
    }

    /**
     * Schedule periodic WebSocket PING frames via a shared scheduler so the gateway
     * client can update its heartbeat timestamp (avoids "Heartbeat timeout detected" and 35s disconnect).
     * The scheduled task is cancelled when the session closes.
     */
    private static void startHeartbeat(Session session) {
        ScheduledFuture<?> future = HEARTBEAT_SCHEDULER.scheduleAtFixedRate(
                () -> {
                    if (!session.isOpen()) {
                        return;
                    }
                    try {
                        session.getBasicRemote().sendPing(EMPTY_PING_PAYLOAD);
                    } catch (IOException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Heartbeat ping failed: " + e.getMessage());
                        }
                    }
                },
                HEARTBEAT_INTERVAL_MS,
                HEARTBEAT_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
        session.getUserProperties().put(HEARTBEAT_FUTURE_PROPERTY, future);
    }

    /**
     * Send connection.ack so the gateway client can complete the handshake (same format as API Platform).
     */
    private static void sendConnectionAck(Session session, String gatewayId) {
        String connectionId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();
        String json = "{\"type\":\"connection.ack\",\"gatewayId\":\"" + escapeJson(gatewayId) + "\",\"connectionId\":\""
                + escapeJson(connectionId) + "\",\"timestamp\":\"" + escapeJson(timestamp) + "\"}";
        try {
            session.getBasicRemote().sendText(json);
        } catch (IOException e) {
            log.warn("Failed to send connection.ack: gatewayId=" + gatewayId + ", error=" + e.getMessage());
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        PlatformGatewayDAO.PlatformGateway gateway =
                (PlatformGatewayDAO.PlatformGateway) session.getUserProperties().get(GATEWAY_PROPERTY);
        String gatewayId = gateway != null ? gateway.id : "unknown";
        if (StringUtils.isBlank(message)) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring empty gateway WebSocket message: gatewayId=" + gatewayId);
            }
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(message).getAsJsonObject();
            String type = getAsString(root, "type");
            if (!"deployment.ack".equals(type)) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring unsupported gateway WebSocket message type: gatewayId=" + gatewayId
                            + ", type=" + type);
                }
                return;
            }

            if (gateway == null) {
                log.warn("Ignoring deployment.ack because gateway session metadata is missing");
                return;
            }

            GatewayDeploymentStatusAcknowledgmentDTO acknowledgment = buildDeploymentAcknowledgment(gateway, root);
            if (acknowledgment == null) {
                return;
            }

            GatewayDeploymentStatusAcknowledgmentListDTO acknowledgmentList =
                    new GatewayDeploymentStatusAcknowledgmentListDTO();
            acknowledgmentList.setCount(1);
            acknowledgmentList.setList(Collections.singletonList(acknowledgment));

            new NotifyApiDeploymentStatusApiServiceImpl()
                    .processGatewayDeploymentStatusAcknowledgments(acknowledgmentList);
        } catch (JsonSyntaxException | IllegalStateException e) {
            log.warn("Failed to parse gateway WebSocket message as JSON: gatewayId=" + gatewayId + ", error="
                    + e.getMessage());
        } catch (APIManagementException e) {
            log.warn("Failed to process gateway deployment.ack: gatewayId=" + gatewayId + ", error="
                    + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.warn("Unexpected error while processing gateway WebSocket message: gatewayId=" + gatewayId
                    + ", error=" + e.getMessage(), e);
        }
    }

    private static GatewayDeploymentStatusAcknowledgmentDTO buildDeploymentAcknowledgment(
            PlatformGatewayDAO.PlatformGateway gateway, JsonObject root) {
        JsonObject payload = root.has("payload") && root.get("payload").isJsonObject()
                ? root.getAsJsonObject("payload") : null;
        if (payload == null) {
            log.warn("Ignoring deployment.ack without payload: gatewayId=" + gateway.id);
            return null;
        }

        String resourceType = getAsString(payload, "resourceType");
        String artifactId = getAsString(payload, "artifactId");
        if (!"api".equals(resourceType)) {
            return null;
        }
        if (StringUtils.isBlank(artifactId)) {
            log.warn("Ignoring deployment.ack with empty artifactId: gatewayId=" + gateway.id);
            return null;
        }

        GatewayDeploymentStatusAcknowledgmentDTO acknowledgment = new GatewayDeploymentStatusAcknowledgmentDTO();
        acknowledgment.setGatewayId(gateway.id);
        acknowledgment.setApiId(artifactId);
        acknowledgment.setTenantDomain(gateway.organizationId);
        acknowledgment.setRevisionId(getAsString(payload, "deploymentId"));
        acknowledgment.setAction(resolveAction(getAsString(payload, "action")));
        acknowledgment.setDeploymentStatus(resolveStatus(getAsString(payload, "status")));
        acknowledgment.setTimeStamp(resolveTimestamp(payload));
        acknowledgment.setErrorCode(parseErrorCode(getAsString(payload, "errorCode")));
        if (acknowledgment.getDeploymentStatus()
                == GatewayDeploymentStatusAcknowledgmentDTO.DeploymentStatusEnum.FAILURE) {
            acknowledgment.setErrorMessage(getAsString(payload, "errorCode"));
        }

        if (acknowledgment.getAction() == null || acknowledgment.getDeploymentStatus() == null) {
            log.warn("Ignoring deployment.ack with unsupported action/status: gatewayId=" + gateway.id + ", action="
                    + getAsString(payload, "action") + ", status=" + getAsString(payload, "status"));
            return null;
        }
        return acknowledgment;
    }

    private static String getAsString(JsonObject object, String member) {
        if (object == null || !object.has(member) || object.get(member).isJsonNull()) {
            return null;
        }
        return object.get(member).getAsString();
    }

    private static GatewayDeploymentStatusAcknowledgmentDTO.ActionEnum resolveAction(String action) {
        if ("deploy".equalsIgnoreCase(action)) {
            return GatewayDeploymentStatusAcknowledgmentDTO.ActionEnum.DEPLOY;
        }
        if ("undeploy".equalsIgnoreCase(action)) {
            return GatewayDeploymentStatusAcknowledgmentDTO.ActionEnum.UNDEPLOY;
        }
        return null;
    }

    private static GatewayDeploymentStatusAcknowledgmentDTO.DeploymentStatusEnum resolveStatus(String status) {
        if ("success".equalsIgnoreCase(status)) {
            return GatewayDeploymentStatusAcknowledgmentDTO.DeploymentStatusEnum.SUCCESS;
        }
        if ("failed".equalsIgnoreCase(status) || "failure".equalsIgnoreCase(status)) {
            return GatewayDeploymentStatusAcknowledgmentDTO.DeploymentStatusEnum.FAILURE;
        }
        return null;
    }

    private static long resolveTimestamp(JsonObject payload) {
        String performedAt = getAsString(payload, "performedAt");
        if (StringUtils.isNotBlank(performedAt)) {
            try {
                return Instant.parse(performedAt).toEpochMilli();
            } catch (DateTimeParseException e) {
                // Fall through to current time when performedAt cannot be parsed.
            }
        }
        return Instant.now().toEpochMilli();
    }

    private static Integer parseErrorCode(String errorCode) {
        if (StringUtils.isBlank(errorCode)) {
            return null;
        }
        try {
            return Integer.valueOf(errorCode);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        // When the client closes first, Tomcat may try to send a close frame and get
        // "Unable to wrap data, invalid status [CLOSED]". Treat as benign client disconnect.
        if (isConnectionClosedError(error)) {
            if (log.isDebugEnabled()) {
                log.debug("Gateway WebSocket error (connection already closed): " + error.getMessage());
            }
            return;
        }
        log.warn("Gateway WebSocket error: " + error.getMessage(), error);
    }

    private static boolean isConnectionClosedError(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            String msg = c.getMessage();
            if (msg != null && (msg.contains("CLOSED") || msg.contains("Unable to wrap") || msg.contains(
                    "Unable to unwrap"))) {
                return true;
            }
        }
        return false;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Object futureObj = session.getUserProperties().get(HEARTBEAT_FUTURE_PROPERTY);
        if (futureObj instanceof ScheduledFuture) {
            ((ScheduledFuture<?>) futureObj).cancel(false);
        }
        Object pollFutureObj = session.getUserProperties().get(DEPLOYMENT_POLL_FUTURE_PROPERTY);
        if (pollFutureObj instanceof ScheduledFuture) {
            ((ScheduledFuture<?>) pollFutureObj).cancel(false);
        }
        PlatformGatewayDAO.PlatformGateway gateway =
                (PlatformGatewayDAO.PlatformGateway) session.getUserProperties().get(GATEWAY_PROPERTY);
        if (gateway != null) {
            boolean wasRemoved = PlatformGatewaySessionRegistry.getInstance().unregister(gateway.id, session);
            if (wasRemoved) {
                try {
                    PlatformGatewayServiceImpl.getInstance().updateGatewayActiveStatus(gateway.id,
                            gateway.organizationId, false);
                } catch (APIManagementException e) {
                    log.warn("Failed to update gateway active status to false: gatewayId=" + gateway.id + ", error="
                            + e.getMessage());
                }
            }
            log.info("Gateway WebSocket connection closed: gatewayId=" + gateway.id + " closeCode="
                    + reason.getCloseCode().getCode());
        }
    }

    private static void closeWithUnauthorized(Session session, String message) {
        try {
            session.close(new CloseReason(new CloseReason.CloseCode() {
                @Override
                public int getCode() {
                    return CLOSE_UNAUTHORIZED;
                }
            }, message));
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error closing session with 4401: " + e.getMessage());
            }
        }
    }
}
