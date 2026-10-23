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

package org.wso2.carbon.apimgt.healthcheck.probe;

import java.util.Properties;

/**
 * One Traffic Manager address to probe. All ports are the real, offset-applied ports read from configuration.
 *
 * <ul>
 *   <li>A throttle receiver ({@link #throttleReceiver}) is where to send events (the 9611 side, plain TCP) plus
 *       where to log in first (the 9711 side, SSL) and the credentials to log in with.</li>
 *   <li>A message broker port ({@link #messageBroker}) is a host and port (5672 plain, or 8672 SSL) plus the JMS
 *       connection properties to connect to exactly that host and port with. The credentials are inside those
 *       properties.</li>
 * </ul>
 */
public final class ProbeEndpoint {

    private final CheckType checkType;
    private final String source;
    private final String host;
    private final int port;
    private final boolean secure;

    // Throttle receiver only
    private final String authHost;
    private final int authPort;
    private final String username;
    private final String password;

    // Message broker only
    private final Properties jmsParameters;

    private ProbeEndpoint(CheckType checkType, String source, String host, int port, boolean secure, String authHost,
                          int authPort, String username, String password, Properties jmsParameters) {

        this.checkType = checkType;
        this.source = source;
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.authHost = authHost;
        this.authPort = authPort;
        this.username = username;
        this.password = password;
        this.jmsParameters = copy(jmsParameters);
    }

    public static ProbeEndpoint throttleReceiver(String source, String host, int port, String authHost, int authPort,
                                                 String username, String password) {

        return new ProbeEndpoint(CheckType.THROTTLE_RECEIVER, source, host, port, false, authHost, authPort, username,
                password, new Properties());
    }

    public static ProbeEndpoint messageBroker(String source, String host, int port, boolean secure,
                                              Properties jmsParameters) {

        return new ProbeEndpoint(CheckType.MESSAGE_BROKER, source, host, port, secure, null, 0, null, null,
                jmsParameters);
    }

    /**
     * @return which part of the Traffic Manager this address belongs to
     */
    public CheckType getCheckType() {

        return checkType;
    }

    /**
     * @return the configuration section this address was read from, e.g. {@code apim.throttling}
     */
    public String getSource() {

        return source;
    }

    public String getHost() {

        return host;
    }

    public int getPort() {

        return port;
    }

    /**
     * @return {@code true} for the message broker's SSL port
     */
    public boolean isSecure() {

        return secure;
    }

    /**
     * @return a readable address without credentials, shown in the health check response, e.g.
     *         {@code tcp://tm-node-1:9611}, {@code amqp://tm-node-1:5672} or {@code amqps://tm-node-1:8672}
     */
    public String getUrl() {

        if (checkType == CheckType.THROTTLE_RECEIVER) {
            return "tcp://" + host + ":" + port;
        }
        return (secure ? "amqps://" : "amqp://") + host + ":" + port;
    }

    /**
     * Throttle receiver only: the host to log in on.
     */
    public String getAuthHost() {

        return authHost;
    }

    /**
     * Throttle receiver only: the port to log in on.
     */
    public int getAuthPort() {

        return authPort;
    }

    /**
     * Throttle receiver only: e.g. {@code ssl://tm-node-1:9711}.
     */
    public String getAuthUrl() {

        return "ssl://" + authHost + ":" + authPort;
    }

    /**
     * Throttle receiver only.
     */
    public String getUsername() {

        return username;
    }

    /**
     * Throttle receiver only. Used only to log in during the probe - never logged or put on the health check
     * response.
     */
    public String getPassword() {

        return password;
    }

    /**
     * Message broker only: the JMS connection-factory properties, including the login credentials. Never log or
     * report these.
     */
    public Properties getJmsParameters() {

        return copy(jmsParameters);
    }

    @Override
    public String toString() {

        if (checkType == CheckType.THROTTLE_RECEIVER) {
            return getUrl() + " (auth " + getAuthUrl() + ", from " + source + ")";
        }
        return getUrl() + " (from " + source + ")";
    }

    private static Properties copy(Properties properties) {

        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }
}
