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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Works out which Traffic Manager addresses to probe, using only the Traffic Manager's own {@value #THROTTLING_SOURCE}
 * configuration. The Event Hub configuration is not used. Configuration is read again on every call.
 *
 * <p><b>Throttle receiver</b> ({@link #resolveThrottleReceivers()}): the receiver address and login credentials
 * come from {@code receiver_url} / {@code [[apim.throttling.url_group]]} and {@code receiver_username} /
 * {@code receiver_password}, read from {@link ThrottleProperties#getTrafficManager()}. When a customer sets none of
 * these, the configuration template fills in the local default {@code tcp://${carbon.local.ip}:${receiver.url.port}}.
 * </p>
 *
 * <p><b>Message broker</b> ({@link #resolveMessageBrokers(boolean)}): the throttling JMS connection URL
 * ({@code [apim.throttling.jms]} / {@code throttle_decision_endpoints}, read from
 * {@link ThrottleProperties#getJmsConnectionProperties()}), for example
 * {@code amqp://admin:admin@clientid/carbon?brokerlist='tcp://10.0.0.5:5672'}. For every broker host in its
 * {@code brokerlist}, two endpoints are returned:</p>
 * <ul>
 *   <li>the plain port: the port written in the URL, or {@code jms.port} (5672 + offset);</li>
 *   <li>the SSL port: the URL's port if the URL itself is SSL ({@code ssl='true'}), otherwise 8672 + offset.</li>
 * </ul>
 * <p>Each broker endpoint gets its own copy of the JMS properties with the URL rewritten to that single host and
 * port, so the credentials in the URL are kept.</p>
 *
 * <p>Placeholders are already resolved by {@link APIManagerConfiguration}, and {@code receiver.url.port},
 * {@code auth.url.port} and {@code jms.port} are set to the default port + offset at startup, so a port offset is
 * followed automatically.</p>
 */
public class EndpointResolver {

    private static final Log log = LogFactory.getLog(EndpointResolver.class);

    static final String THROTTLING_SOURCE = "apim.throttling";

    static final String RECEIVER_PORT_PROPERTY = "receiver.url.port";
    static final String AUTH_PORT_PROPERTY = "auth.url.port";
    static final String JMS_PORT_PROPERTY = "jms.port";
    static final int DEFAULT_RECEIVER_PORT = 9611;
    static final int DEFAULT_AUTH_PORT = 9711;
    static final int DEFAULT_BROKER_PORT = 5672;
    static final int DEFAULT_BROKER_SSL_PORT = 8672;

    /**
     * A host name may only contain letters, digits, '.', '-' and '_', so an unresolved placeholder such as
     * {@code ${carbon.local.ip}} is never mistaken for a host.
     */
    private static final String HOST = "([A-Za-z0-9_.-]+)";

    /**
     * Finds every {@code tcp://host[:port]} or {@code ssl://host[:port]} inside a receiver URL group, whatever the
     * group syntax around it: a single URL, failover ({@code url1|url2}) or load-balanced ({@code {url1},{url2}}).
     */
    private static final Pattern RECEIVER_URL_PATTERN =
            Pattern.compile("(?:tcp|ssl)://" + HOST + "(?::(\\d+))?(?![A-Za-z0-9_.$-])");

    /**
     * Finds every {@code tcp://host:port} in a broker URL's {@code brokerlist}.
     */
    private static final Pattern BROKER_PATTERN = Pattern.compile("tcp://" + HOST + ":(\\d+)");
    private static final Pattern SSL_OPTION_PATTERN = Pattern.compile("ssl\\s*=\\s*'?true", Pattern.CASE_INSENSITIVE);
    private static final String BROKER_LIST_START = "brokerlist='";

    private final Supplier<APIManagerConfiguration> configurationSupplier;
    private final IntSupplier portOffsetSupplier;

    /**
     * @param configurationSupplier gives the live API Manager configuration
     * @param portOffsetSupplier    gives the server's port offset, used for the broker's SSL port
     */
    public EndpointResolver(Supplier<APIManagerConfiguration> configurationSupplier,
                            IntSupplier portOffsetSupplier) {

        this.configurationSupplier = configurationSupplier;
        this.portOffsetSupplier = portOffsetSupplier;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Throttle receiver
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * @return one endpoint per distinct throttle receiver URL; an empty list if nothing can be resolved
     */
    public List<ProbeEndpoint> resolveThrottleReceivers() {

        ThrottleProperties throttleProperties = readThrottleProperties();
        ThrottleProperties.TrafficManager trafficManager =
                throttleProperties == null ? null : throttleProperties.getTrafficManager();
        if (trafficManager == null) {
            log.warn("Cannot resolve the throttle receiver address - no " + THROTTLING_SOURCE
                    + " Traffic Manager configuration is available");
            return new ArrayList<>();
        }

        Map<String, ProbeEndpoint> endpointsByUrl = new LinkedHashMap<>();
        for (ProbeEndpoint endpoint : parseThrottleReceivers(THROTTLING_SOURCE, trafficManager.getReceiverUrlGroup(),
                trafficManager.getAuthUrlGroup(), trafficManager.getUsername(), trafficManager.getPassword())) {
            endpointsByUrl.putIfAbsent(endpoint.getUrl(), endpoint);
        }
        return new ArrayList<>(endpointsByUrl.values());
    }

    /**
     * Turns the receiver and auth URL groups into endpoints. Pairs receiver URL number {@code n} with auth URL number
     * {@code n}; if there are fewer auth URLs, the first one is reused; if there are none, the receiver's own host is
     * used with the auth port.
     */
    List<ProbeEndpoint> parseThrottleReceivers(String source, String receiverUrlGroup, String authUrlGroup,
                                               String username, String password) {

        List<HostAndPort> receivers = parseUrlGroup(source, receiverUrlGroup,
                portProperty(RECEIVER_PORT_PROPERTY, DEFAULT_RECEIVER_PORT));
        List<HostAndPort> auths = parseUrlGroup(source, authUrlGroup,
                portProperty(AUTH_PORT_PROPERTY, DEFAULT_AUTH_PORT));

        List<ProbeEndpoint> endpoints = new ArrayList<>(receivers.size());
        for (int i = 0; i < receivers.size(); i++) {
            HostAndPort receiver = receivers.get(i);
            HostAndPort auth;
            if (i < auths.size()) {
                auth = auths.get(i);
            } else if (!auths.isEmpty()) {
                auth = auths.get(0);
            } else {
                auth = new HostAndPort(receiver.host, portProperty(AUTH_PORT_PROPERTY, DEFAULT_AUTH_PORT));
            }
            endpoints.add(ProbeEndpoint.throttleReceiver(source, receiver.host, receiver.port, auth.host, auth.port,
                    username, password));
        }
        return endpoints;
    }

    private List<HostAndPort> parseUrlGroup(String source, String urlGroup, int portIfMissing) {

        List<HostAndPort> result = new ArrayList<>();
        if (urlGroup == null || urlGroup.trim().isEmpty()) {
            return result;
        }
        Matcher matcher = RECEIVER_URL_PATTERN.matcher(urlGroup);
        while (matcher.find()) {
            String port = matcher.group(2);
            result.add(new HostAndPort(matcher.group(1), port == null ? portIfMissing : Integer.parseInt(port)));
        }
        if (result.isEmpty()) {
            log.warn("Could not find any host in the " + source + " URL group: " + urlGroup);
        }
        return result;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Message broker
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * @param includeSslPort whether to also return the SSL (8672) endpoint for each host
     * @return the message broker endpoints to probe, plain ports first; an empty list if nothing can be resolved
     */
    public List<ProbeEndpoint> resolveMessageBrokers(boolean includeSslPort) {

        ThrottleProperties throttleProperties = readThrottleProperties();
        ThrottleProperties.JMSConnectionProperties jms =
                throttleProperties == null ? null : throttleProperties.getJmsConnectionProperties();
        Properties jmsProperties = jms == null ? null : jms.getJmsConnectionProperties();
        String url = jmsProperties == null ? null : jmsProperties.getProperty(JMSConstants.CONNECTION_STRING_TOPIC);
        if (url == null || !url.contains(BROKER_LIST_START)) {
            log.warn("Cannot resolve the message broker address - no " + THROTTLING_SOURCE
                    + " JMS connection URL with a brokerlist is configured");
            return new ArrayList<>();
        }

        // One entry per host; a host listed twice is probed once.
        Map<String, ProbeEndpoint> plainByHost = new LinkedHashMap<>();
        Map<String, ProbeEndpoint> sslByHost = new LinkedHashMap<>();
        String brokerList = url.substring(url.indexOf(BROKER_LIST_START) + BROKER_LIST_START.length());
        Matcher matcher = BROKER_PATTERN.matcher(brokerList);
        boolean found = matcher.find();
        while (found) {
            String host = matcher.group(1);
            int port = Integer.parseInt(matcher.group(2));
            int optionsStart = matcher.end();
            found = matcher.find();
            // This broker's options (such as ssl='true') are the text up to the next broker in the list.
            String options = brokerList.substring(optionsStart, found ? matcher.start() : brokerList.length());
            boolean secure = SSL_OPTION_PATTERN.matcher(options).find();

            int plainPort = secure ? portProperty(JMS_PORT_PROPERTY, DEFAULT_BROKER_PORT) : port;
            int sslPort = secure ? port : DEFAULT_BROKER_SSL_PORT + portOffsetSupplier.getAsInt();
            plainByHost.putIfAbsent(host, messageBroker(jmsProperties, url, host, plainPort, false));
            if (includeSslPort) {
                sslByHost.putIfAbsent(host, messageBroker(jmsProperties, url, host, sslPort, true));
            }
        }
        if (plainByHost.isEmpty()) {
            log.warn("Could not find any broker host in the " + THROTTLING_SOURCE + " JMS connection URL");
        }

        List<ProbeEndpoint> endpoints = new ArrayList<>(plainByHost.values());
        endpoints.addAll(sslByHost.values());
        return endpoints;
    }

    /**
     * Copies the JMS properties and points the URL's {@code brokerlist} at exactly one host and port. Everything
     * before the {@code brokerlist} (including the username and password) is kept as it is.
     */
    private static ProbeEndpoint messageBroker(Properties jmsProperties, String url, String host, int port,
                                               boolean secure) {

        String prefix = url.substring(0, url.indexOf(BROKER_LIST_START) + BROKER_LIST_START.length());
        String singleBrokerUrl = prefix + "tcp://" + host + ":" + port + (secure ? "?ssl='true'" : "") + "'";

        Properties copy = new Properties();
        copy.putAll(jmsProperties);
        copy.setProperty(JMSConstants.CONNECTION_STRING_TOPIC, singleBrokerUrl);
        return ProbeEndpoint.messageBroker(THROTTLING_SOURCE, host, port, secure, copy);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Shared
    // ---------------------------------------------------------------------------------------------------------------

    private ThrottleProperties readThrottleProperties() {

        APIManagerConfiguration configuration = configurationSupplier.get();
        return configuration == null ? null : configuration.getThrottleProperties();
    }

    /**
     * Reads a port the server already worked out at startup (default port + port offset), falling back to the
     * default port + offset if that system property is missing or not a number.
     */
    private int portProperty(String systemProperty, int defaultPort) {

        String value = System.getProperty(systemProperty);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                log.debug("Invalid " + systemProperty + " value: " + value);
            }
        }
        return defaultPort + portOffsetSupplier.getAsInt();
    }

    private static final class HostAndPort {

        private final String host;
        private final int port;

        private HostAndPort(String host, int port) {

            this.host = host;
            this.port = port;
        }
    }
}
