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

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

/**
 * Configurator for the gateway connect WebSocket endpoint. Passes the registration token
 * (api-key header) from the handshake request into the endpoint config so it can be verified in onOpen.
 */
public class GatewayConnectConfigurator extends ServerEndpointConfig.Configurator {

    private static final Log log = LogFactory.getLog(GatewayConnectConfigurator.class);

    /**
     * User property key for the api-key header value (gateway registration token).
     */
    public static final String API_KEY_PROPERTY = "api-key";

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Modifying WebSocket handshake for gateway connection");
        }
        Map<String, List<String>> headers = request.getHeaders();
        if (headers != null) {
            String apiKey = getFirstHeader(headers, "api-key");
            if (apiKey != null && !apiKey.isEmpty()) {
                sec.getUserProperties().put(API_KEY_PROPERTY, apiKey);
                if (log.isDebugEnabled()) {
                    log.debug("API key found in handshake request headers");
                }
            } else {
                log.warn("No api-key header found in WebSocket handshake request");
            }
        }
    }

    private static String getFirstHeader(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            if (name.equalsIgnoreCase(e.getKey()) && e.getValue() != null && !e.getValue().isEmpty()) {
                return e.getValue().get(0);
            }
        }
        return null;
    }
}
