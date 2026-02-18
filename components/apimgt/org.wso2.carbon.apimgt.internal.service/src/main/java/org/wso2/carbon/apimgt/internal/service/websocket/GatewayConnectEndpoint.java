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
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint for platform gateway control plane connections.
 * Path: /ws/gateways/connect (relative to Internal Data Service context, e.g. /internal/data/v1).
 * <p>
 * The gateway must send the registration token in the {@code api-key} HTTP header when opening
 * the WebSocket. The token is verified via {@link PlatformGatewayTokenUtil#verifyToken(String)}.
 * On success, the connection is associated with the gateway and the gateway's active status is
 * set to true; on close it is set to false. On verification failure, the connection is closed
 * with code 4401 (Unauthorized).
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

    /** Interval for server-to-client WebSocket PING (client timeout is 35s). */
    private static final long HEARTBEAT_INTERVAL_MS = 15_000;

    /** Reusable empty payload for PING (avoids allocating a new ByteBuffer every interval). */
    private static final ByteBuffer EMPTY_PING_PAYLOAD = ByteBuffer.allocate(0);

    /** Shared scheduler for all gateway heartbeat pings (bounded thread count). */
    private static final ScheduledExecutorService HEARTBEAT_SCHEDULER =
            Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "gateway-ws-heartbeat");
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
            if (log.isDebugEnabled()) {
                log.debug("Gateway WebSocket connection rejected: token did not match any gateway");
            }
            closeWithUnauthorized(session, "Invalid or expired API key");
            return;
        }

        session.getUserProperties().put(GATEWAY_PROPERTY, gateway);

        try {
            PlatformGatewayDAO.getInstance().updateGatewayActiveStatus(gateway.id, true);
            if (log.isDebugEnabled()) {
                log.debug("Gateway active status updated to true: gatewayId=" + gateway.id);
            }
        } catch (APIManagementException e) {
            log.warn("Failed to update gateway active status to true: gatewayId=" + gateway.id + ", error="
                    + e.getMessage());
        }

        sendConnectionAck(session, gateway.id);

        startHeartbeat(session);

        if (log.isInfoEnabled()) {
            log.info("Gateway WebSocket connection established: gatewayId=" + gateway.id + " name=" + gateway.name);
        }
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
            if (log.isWarnEnabled()) {
                log.warn("Failed to send connection.ack: gatewayId=" + gatewayId + ", error=" + e.getMessage());
            }
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
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
        if (log.isWarnEnabled()) {
            log.warn("Gateway WebSocket error: " + error.getMessage(), error);
        }
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
        PlatformGatewayDAO.PlatformGateway gateway =
                (PlatformGatewayDAO.PlatformGateway) session.getUserProperties().get(GATEWAY_PROPERTY);
        if (gateway != null) {
            try {
                PlatformGatewayDAO.getInstance().updateGatewayActiveStatus(gateway.id, false);
            } catch (APIManagementException e) {
                log.warn("Failed to update gateway active status to false: gatewayId=" + gateway.id + ", error="
                        + e.getMessage());
            }
            if (log.isInfoEnabled()) {
                log.info("Gateway WebSocket connection closed: gatewayId=" + gateway.id + " closeCode="
                        + reason.getCloseCode().getCode());
            }
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
