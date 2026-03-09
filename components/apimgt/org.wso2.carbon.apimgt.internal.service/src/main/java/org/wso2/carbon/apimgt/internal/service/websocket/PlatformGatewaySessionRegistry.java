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

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 * Registry of platform gateway WebSocket sessions by gateway ID.
 * Used by {@link WebSocketPlatformGatewayDeploymentDispatcher} to push deploy/undeploy messages.
 */
public class PlatformGatewaySessionRegistry {

    private static final Log log = LogFactory.getLog(PlatformGatewaySessionRegistry.class);
    private static final PlatformGatewaySessionRegistry INSTANCE = new PlatformGatewaySessionRegistry();

    private final Map<String, Session> gatewaySessions = new ConcurrentHashMap<>();

    public static PlatformGatewaySessionRegistry getInstance() {
        return INSTANCE;
    }

    private PlatformGatewaySessionRegistry() {
    }

    /**
     * Register a gateway's WebSocket session. Replaces any existing session for the same gateway ID.
     * Atomically swaps in the new session and closes the previous one to avoid races with sendToGateways.
     */
    public void register(String gatewayId, Session session) {
        if (gatewayId == null || session == null) {
            return;
        }
        gatewaySessions.compute(gatewayId, (id, previous) -> {
            if (previous != null && previous != session && previous.isOpen()) {
                try {
                    previous.close();
                } catch (IOException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error closing previous session for gateway " + gatewayId + ": " + e.getMessage());
                    }
                }
            }
            return session;
        });
        if (log.isDebugEnabled()) {
            log.debug("Registered WebSocket session for gateway: " + gatewayId);
        }
    }

    /**
     * Unregister a gateway's session (e.g. on WebSocket close). Only removes if the current mapping
     * is still this exact session, so a newly registered session is not removed during reconnects.
     */
    public void unregister(String gatewayId, Session session) {
        if (gatewayId != null && session != null && gatewaySessions.remove(gatewayId, session)) {
            if (log.isDebugEnabled()) {
                log.debug("Unregistered WebSocket session for gateway: " + gatewayId);
            }
        }
    }

    /**
     * Close and unregister the WebSocket session for the given gateway (e.g. when gateway is deleted).
     * The gateway client will receive a close frame. Safe to call if no session is registered.
     */
    public void closeAndUnregister(String gatewayId) {
        if (gatewayId == null) {
            return;
        }
        Session session = gatewaySessions.remove(gatewayId);
        if (session == null) {
            if (log.isDebugEnabled()) {
                log.debug("No WebSocket session to close for gateway: " + gatewayId);
            }
            return;
        }
        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.warn("Error closing WebSocket session for gateway " + gatewayId + ": " + e.getMessage());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Closed and unregistered WebSocket session for gateway: " + gatewayId);
        }
    }

    /**
     * Send a text message to the given gateway IDs. Skips gateways with no registered session or closed session.
     *
     * @param gatewayIds target gateway IDs
     * @param message    JSON or text payload to send
     */
    public void sendToGateways(Set<String> gatewayIds, String message) {
        if (gatewayIds == null || message == null) {
            return;
        }
        for (String gatewayId : gatewayIds) {
            Session session = gatewaySessions.get(gatewayId);
            if (session == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No WebSocket session for gateway " + gatewayId + "; skipping deploy notification");
                }
                continue;
            }
            if (!session.isOpen()) {
                gatewaySessions.remove(gatewayId, session);
                if (log.isDebugEnabled()) {
                    log.debug("Removed closed session for gateway " + gatewayId);
                }
                continue;
            }
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(message);
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to send deploy message to gateway " + gatewayId + ": " + e.getMessage());
                if (gatewaySessions.get(gatewayId) == session) {
                    gatewaySessions.remove(gatewayId, session);
                }
            }
        }
    }
}
