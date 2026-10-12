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

package org.wso2.carbon.apimgt.tm.healthcheck;

/**
 * The live host/port/TLS details resolved for one {@link ControlPlaneConnectionId}, as produced by
 * {@link ControlPlaneConnectionConfigResolver} fresh on a single health check invocation.
 *
 * <p>This is purely an internal, in-memory value used to actually dial a connection during the
 * port-responsiveness and accept-confirmation checks (Phase 3). It is never returned from
 * {@code checkHealth()} or placed on the response (FR-014) - only the fixed port/label on
 * {@link ControlPlaneConnectionId} may identify a connection externally; the resolved host here is for
 * internal use (and internal logging, Research &sect;5) only.</p>
 */
public final class ResolvedConnectionConfig {

    private final ControlPlaneConnectionId connectionId;
    private final String host;
    private final int port;
    private final boolean tlsEnabled;

    public ResolvedConnectionConfig(ControlPlaneConnectionId connectionId, String host, int port,
                                     boolean tlsEnabled) {

        this.connectionId = connectionId;
        this.host = host;
        this.port = port;
        this.tlsEnabled = tlsEnabled;
    }

    /**
     * Returns which Control Plane connection this resolved configuration is for.
     *
     * @return the connection identifier
     */
    public ControlPlaneConnectionId getConnectionId() {

        return connectionId;
    }

    /**
     * Returns the resolved Control Plane host to dial for this connection, read fresh from the live
     * {@code APIManagerConfiguration} - never a cached or hardcoded value (FR-012).
     *
     * @return the resolved host name/IP
     */
    public String getHost() {

        return host;
    }

    /**
     * Returns the port to actually dial for this connection. For the four fixed-port connections this is
     * always the connection's own well-known port ({@link ControlPlaneConnectionId#getPort()}), never a
     * value read out of configuration; for {@link ControlPlaneConnectionId#MANAGEMENT_API} it is the port
     * parsed out of the configured service URL, since that connection has no fixed product-wide port.
     *
     * @return the port to dial
     */
    public int getPort() {

        return port;
    }

    /**
     * Returns whether this connection should be dialed with a TLS handshake (9711, 8672, and the
     * management connection) rather than a plain TCP connect (9611, 5672).
     *
     * @return {@code true} if a TLS handshake is required for this connection
     */
    public boolean isTlsEnabled() {

        return tlsEnabled;
    }
}
