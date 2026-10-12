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
 * Identifies one of the Traffic Manager's connections to the Control Plane's Event Hub dependency.
 *
 * <p>Each constant is a stable identifier for a connection, independent of whatever host/port is
 * currently configured for it. {@link ControlPlaneConnectionConfigResolver} resolves the live
 * host/port/URL for each of these IDs fresh on every health check invocation, so this enum itself
 * never carries a resolved address - only the fixed, product-documented port (where one exists) and
 * a plain-language label safe to show to a non-expert operator.</p>
 */
public enum ControlPlaneConnectionId {

    /**
     * Plain (non-TLS) connection to the Control Plane's binary event receiver, used to publish
     * throttling data. Fixed product port 9611.
     */
    THROTTLING_DATA(9611, "Control Plane throttling data connection"),

    /**
     * SSL/auth connection to the same binary event receiver as {@link #THROTTLING_DATA}. Logging in
     * on this connection is also how the Throttling Data accept-confirmation check authenticates.
     * Fixed product port 9711.
     */
    THROTTLING_DATA_SECURE(9711, "Control Plane throttling data connection (secure)"),

    /**
     * Plain (non-TLS) message broker (JMS/AMQP) connection used to receive real-time event
     * notifications pushed from the Control Plane. Fixed product port 5672.
     */
    MESSAGE_BROKER(5672, "Control Plane message broker connection"),

    /**
     * Secure/TLS variant of {@link #MESSAGE_BROKER}, applicable only when the Traffic Manager is
     * configured for TLS messaging. Fixed product port 8672.
     */
    MESSAGE_BROKER_SECURE(8672, "Control Plane message broker connection (secure)"),

    /**
     * Control Plane management/REST connection used for Event Hub startup and periodic data sync.
     * Unlike the other four connections, this one has no single fixed product-wide port - its port is
     * entirely config-driven (the configured service URL), so {@link #getPort()} returns {@code null}.
     */
    MANAGEMENT_API(null, "Control Plane management connection");

    private final Integer port;
    private final String label;

    ControlPlaneConnectionId(Integer port, String label) {

        this.port = port;
        this.label = label;
    }

    /**
     * Returns the segment used to build this connection's {@code Properties} response keys (e.g.
     * {@code "TMControlPlaneConnectivity.9611.status"}) and {@code HealthCheckError} codes (e.g.
     * {@code "HC_TM_9611"}) - the port number as a string for the four fixed-port connections, or
     * {@code "management"}/{@code "MGMT"} for {@link #MANAGEMENT_API}, which has none
     * (contracts/health-checker-spi.md).
     *
     * @return the response-key segment for this connection
     */
    public String getResponseKeySegment() {

        return port == null ? "management" : String.valueOf(port);
    }

    /**
     * Returns the segment used to build this connection's {@code HealthCheckError} code (e.g.
     * {@code HC_TM_9611}, or {@code HC_TM_MGMT} for {@link #MANAGEMENT_API}).
     *
     * @return the error-code segment for this connection
     */
    public String getErrorCodeSegment() {

        return port == null ? "MGMT" : String.valueOf(port);
    }

    /**
     * Returns the fixed, well-known port for this connection, or {@code null} for
     * {@link #MANAGEMENT_API}, which has no single fixed product-wide port.
     *
     * @return the fixed port number, or {@code null} if this connection has none
     */
    public Integer getPort() {

        return port;
    }

    /**
     * Returns a plain-language description of this connection, safe to show to an operator with no
     * product-internals knowledge (e.g. "Control Plane throttling data connection").
     *
     * @return the plain-language label for this connection
     */
    public String getLabel() {

        return label;
    }
}
