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
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.IntSupplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.Context;

/**
 * Resolves the throttle receiver and message broker endpoints to probe from this checker's own
 * {@code [[health_checker]]} properties. An address that is not configured defaults to {@code carbon.local.ip} on the
 * standard port plus the server's port offset. Values may use {@code ${name}} or {@code $sys{name}} for a system
 * property and {@code $env{NAME}} for an environment variable, resolved on every check.
 */
public class EndpointResolver {

    private static final Log log = LogFactory.getLog(EndpointResolver.class);

    /** {@code .source} of an address from the checker's properties. */
    static final String CONFIG_SOURCE = "health_checker";
    /** {@code .source} of an address derived from {@code carbon.local.ip} and the standard ports. */
    static final String DEFAULT_SOURCE = "default";

    static final String RECEIVER_URLS_PROPERTY = "receiver.urls";
    static final String RECEIVER_AUTH_URLS_PROPERTY = "receiver.auth.urls";
    static final String BROKER_URLS_PROPERTY = "broker.urls";
    static final String USERNAME_PROPERTY = "username";
    static final String PASSWORD_PROPERTY = "password";
    static final String RECEIVER_USERNAME_PROPERTY = "receiver.username";
    static final String RECEIVER_PASSWORD_PROPERTY = "receiver.password";
    static final String BROKER_USERNAME_PROPERTY = "broker.username";
    static final String BROKER_PASSWORD_PROPERTY = "broker.password";

    static final String LOCAL_IP_PROPERTY = "carbon.local.ip";
    static final String DEFAULT_HOST = "localhost";
    static final String RECEIVER_PORT_PROPERTY = "receiver.url.port";
    static final String AUTH_PORT_PROPERTY = "auth.url.port";
    static final String JMS_PORT_PROPERTY = "jms.port";
    static final int DEFAULT_RECEIVER_PORT = 9611;
    static final int DEFAULT_AUTH_PORT = 9711;
    static final int DEFAULT_BROKER_PORT = 5672;
    static final int DEFAULT_BROKER_SSL_PORT = 8672;

    /** The same JMS settings the product renders for the throttling topic connection factory. */
    static final String CONNECTION_FACTORY_JNDI_NAME = "TopicConnectionFactory";
    static final String DESTINATION_TYPE = "topic";
    static final String INITIAL_CONTEXT_FACTORY = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";

    /** {@code ${name}}, {@code $sys{name}} or {@code $env{NAME}}; group 1 is {@code sys}, {@code env} or empty. */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$(sys|env)?\\{([^{}]+)}");

    /** Host name characters only, so an unresolved placeholder is never taken as a host. */
    private static final String HOST = "([A-Za-z0-9_.-]+)";

    /** Finds each {@code tcp://} or {@code ssl://} host and optional port in a receiver URL list. */
    private static final Pattern RECEIVER_URL_PATTERN =
            Pattern.compile("(?:tcp|ssl)://" + HOST + "(?::(\\d+))?(?![A-Za-z0-9_.$-])");

    /** Finds each {@code tcp://} host and optional port in a broker URL list. */
    private static final Pattern BROKER_URL_PATTERN =
            Pattern.compile("tcp://" + HOST + "(?::(\\d+))?(?![A-Za-z0-9_.$-])");

    private static final Pattern SSL_OPTION_PATTERN = Pattern.compile("ssl\\s*=\\s*'?true", Pattern.CASE_INSENSITIVE);

    /**
     * Per-broker TLS options kept in the probe URL, with their leading separator as group 1. Retry options are not
     * kept, so a down broker fails fast.
     */
    private static final Pattern TLS_OPTION_PATTERN = Pattern.compile(
            "(\\?|&|%26)(?:ssl_cert_alias|trust_store_password|trust_store|key_store_password|key_store"
                    + "|ssl_verify_hostname)\\s*=\\s*'[^']*'");

    /** Separator between options of one broker inside the brokerlist, as the product renders it. */
    private static final String OPTION_SEPARATOR = "%26";

    private final IntSupplier portOffsetSupplier;
    private final UnaryOperator<String> systemProperties;
    private final UnaryOperator<String> environment;
    /** The super admin account from {@code user-mgt.xml}, read on first use; {@code null} if it cannot be read. */
    private volatile RealmConfiguration realmConfiguration;

    public EndpointResolver(IntSupplier portOffsetSupplier) {

        this.portOffsetSupplier = portOffsetSupplier;
        this.systemProperties = this::systemPropertyOrAdminAccount;
        this.environment = System::getenv;
    }

    /**
     * Looks the name up as a system property, then, as the API Manager does for its own configuration, as
     * {@code admin.username} or {@code admin.password} of the super admin account, so the credentials can be written
     * as {@code ${admin.username}} and {@code ${admin.password}}.
     */
    private String systemPropertyOrAdminAccount(String name) {

        String value = System.getProperty(name);
        if (value != null) {
            return value;
        }
        boolean username = "admin.username".equals(name);
        if (!username && !"admin.password".equals(name)) {
            return null;
        }
        RealmConfiguration realm = realmConfiguration;
        if (realm == null) {
            try {
                realm = new RealmConfigXMLProcessor().buildRealmConfigurationFromFile();
                realmConfiguration = realm;
            } catch (UserStoreException | RuntimeException e) {
                log.warn("Could not read the super admin account to resolve ${" + name + "}", e);
                return null;
            }
        }
        return username ? realm.getAdminUserName() : realm.getAdminPassword();
    }

    EndpointResolver(IntSupplier portOffsetSupplier, UnaryOperator<String> systemProperties,
                     UnaryOperator<String> environment) {

        this.portOffsetSupplier = portOffsetSupplier;
        this.systemProperties = systemProperties;
        this.environment = environment;
    }

    /**
     * Pairs the n-th receiver URL with the n-th auth URL, falling back to the first auth URL, or to the receiver's
     * host on the auth port when there is none.
     *
     * @param config the checker's properties; {@code null} is treated as empty
     * @return one endpoint per distinct receiver URL; empty if a configured list has no valid host
     */
    public List<ProbeEndpoint> resolveThrottleReceivers(Properties config) {

        String receiverUrls = value(config, RECEIVER_URLS_PROPERTY);
        String authUrls = value(config, RECEIVER_AUTH_URLS_PROPERTY);
        String source = receiverUrls != null || authUrls != null ? CONFIG_SOURCE : DEFAULT_SOURCE;
        String username = credential(config, RECEIVER_USERNAME_PROPERTY, USERNAME_PROPERTY);
        String password = credential(config, RECEIVER_PASSWORD_PROPERTY, PASSWORD_PROPERTY);

        int receiverPort = portProperty(RECEIVER_PORT_PROPERTY, DEFAULT_RECEIVER_PORT);
        int authPort = portProperty(AUTH_PORT_PROPERTY, DEFAULT_AUTH_PORT);
        List<HostAndPort> receivers = receiverUrls == null
                ? singleLocal(receiverPort) : parseUrls(RECEIVER_URLS_PROPERTY, receiverUrls, receiverPort);
        List<HostAndPort> auths = authUrls == null
                ? new ArrayList<>() : parseUrls(RECEIVER_AUTH_URLS_PROPERTY, authUrls, authPort);

        Map<String, ProbeEndpoint> endpointsByUrl = new LinkedHashMap<>();
        for (int i = 0; i < receivers.size(); i++) {
            HostAndPort receiver = receivers.get(i);
            HostAndPort auth;
            if (i < auths.size()) {
                auth = auths.get(i);
            } else if (!auths.isEmpty()) {
                auth = auths.get(0);
            } else {
                auth = new HostAndPort(receiver.host, authPort);
            }
            ProbeEndpoint endpoint = ProbeEndpoint.throttleReceiver(source, receiver.host, receiver.port, auth.host,
                    auth.port, username, password);
            endpointsByUrl.putIfAbsent(endpoint.getUrl(), endpoint);
        }
        return new ArrayList<>(endpointsByUrl.values());
    }

    /**
     * Returns one endpoint per broker host:port, plain ports first. With {@code includeSslPort}, a guessed paired
     * port is only added for a host with no entry of that kind.
     *
     * @param config         the checker's properties; {@code null} is treated as empty
     * @param includeSslPort whether to add guessed paired ports for each host
     */
    public List<ProbeEndpoint> resolveMessageBrokers(Properties config, boolean includeSslPort) {

        String brokerUrls = value(config, BROKER_URLS_PROPERTY);
        String source = brokerUrls == null ? DEFAULT_SOURCE : CONFIG_SOURCE;
        String username = credential(config, BROKER_USERNAME_PROPERTY, USERNAME_PROPERTY);
        String password = credential(config, BROKER_PASSWORD_PROPERTY, PASSWORD_PROPERTY);
        int plainPort = portProperty(JMS_PORT_PROPERTY, DEFAULT_BROKER_PORT);
        int sslPort = DEFAULT_BROKER_SSL_PORT + portOffsetSupplier.getAsInt();

        Map<String, ProbeEndpoint> plainByAddress = new LinkedHashMap<>();
        Map<String, ProbeEndpoint> sslByAddress = new LinkedHashMap<>();
        Map<String, Boolean> hostHasPlain = new LinkedHashMap<>();
        Map<String, Boolean> hostHasSsl = new LinkedHashMap<>();
        String urls = brokerUrls == null ? "tcp://" + localHost() + ":" + plainPort : brokerUrls;
        Matcher matcher = BROKER_URL_PATTERN.matcher(urls);
        boolean found = matcher.find();
        while (found) {
            String host = matcher.group(1);
            String portText = matcher.group(2);
            int optionsStart = matcher.end();
            found = matcher.find();
            String options = urls.substring(optionsStart, found ? matcher.start() : urls.length());
            boolean secure = SSL_OPTION_PATTERN.matcher(options).find();

            int port;
            try {
                port = portText == null ? (secure ? sslPort : plainPort) : Integer.parseInt(portText);
            } catch (NumberFormatException e) {
                log.warn("Ignoring " + host + " in " + BROKER_URLS_PROPERTY + ": invalid port '" + portText + "'");
                continue;
            }
            String address = host + ":" + port;
            if (secure) {
                sslByAddress.put(address, messageBroker(source, host, port, true, extractTlsOptions(options),
                        username, password));
                hostHasSsl.put(host, true);
                hostHasPlain.putIfAbsent(host, false);
            } else {
                plainByAddress.put(address, messageBroker(source, host, port, false, "", username, password));
                hostHasPlain.put(host, true);
                hostHasSsl.putIfAbsent(host, false);
            }
        }
        if (includeSslPort) {
            for (Map.Entry<String, Boolean> host : hostHasPlain.entrySet()) {
                if (!host.getValue()) {
                    plainByAddress.putIfAbsent(host.getKey() + ":" + plainPort,
                            messageBroker(source, host.getKey(), plainPort, false, "", username, password));
                }
            }
            for (Map.Entry<String, Boolean> host : hostHasSsl.entrySet()) {
                if (!host.getValue()) {
                    sslByAddress.putIfAbsent(host.getKey() + ":" + sslPort,
                            messageBroker(source, host.getKey(), sslPort, true, "", username, password));
                }
            }
        }
        if (plainByAddress.isEmpty() && sslByAddress.isEmpty()) {
            log.warn("Could not find any broker host in " + BROKER_URLS_PROPERTY + ": " + urls);
        }

        List<ProbeEndpoint> endpoints = new ArrayList<>(plainByAddress.values());
        endpoints.addAll(sslByAddress.values());
        return endpoints;
    }

    /**
     * Skips a host with an invalid port so one bad entry does not break the whole health check.
     */
    private List<HostAndPort> parseUrls(String property, String urls, int portIfMissing) {

        List<HostAndPort> result = new ArrayList<>();
        Matcher matcher = RECEIVER_URL_PATTERN.matcher(urls);
        while (matcher.find()) {
            String host = matcher.group(1);
            String port = matcher.group(2);
            if (port == null) {
                result.add(new HostAndPort(host, portIfMissing));
                continue;
            }
            try {
                result.add(new HostAndPort(host, Integer.parseInt(port)));
            } catch (NumberFormatException e) {
                log.warn("Ignoring " + host + " in " + property + ": invalid port '" + port + "'");
            }
        }
        if (result.isEmpty()) {
            log.warn("Could not find any host in " + property + ": " + urls);
        }
        return result;
    }

    private List<HostAndPort> singleLocal(int port) {

        List<HostAndPort> result = new ArrayList<>();
        result.add(new HostAndPort(localHost(), port));
        return result;
    }

    /**
     * Builds the JMS properties for one broker. The credentials go in the connection URL, as in the product's own
     * throttling connection factory, so no failover or retry options are added and a down broker fails fast.
     */
    private static ProbeEndpoint messageBroker(String source, String host, int port, boolean secure,
                                               String tlsOptions, String username, String password) {

        Properties jmsParameters = new Properties();
        jmsParameters.setProperty(JMSConstants.PARAM_CONFAC_JNDI_NAME, CONNECTION_FACTORY_JNDI_NAME);
        jmsParameters.setProperty(JMSConstants.PARAM_DEST_TYPE, DESTINATION_TYPE);
        jmsParameters.setProperty(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        if (username != null && password != null) {
            jmsParameters.setProperty(JMSConstants.CONNECTION_STRING_TOPIC, "amqp://" + username + ":" + password
                    + "@clientid/carbon?brokerlist='tcp://" + host + ":" + port
                    + (secure ? "?ssl='true'" + tlsOptions : "") + "'");
        }
        return ProbeEndpoint.messageBroker(source, host, port, secure, username, password, jmsParameters);
    }

    /**
     * Returns the TLS options in one broker's options, each preceded by the product's option separator.
     */
    private static String extractTlsOptions(String options) {

        StringBuilder preserved = new StringBuilder();
        Matcher matcher = TLS_OPTION_PATTERN.matcher(options);
        while (matcher.find()) {
            String match = matcher.group();
            preserved.append(OPTION_SEPARATOR).append(match, matcher.group(1).length(), match.length());
        }
        return preserved.toString();
    }

    /**
     * Reads a credential, preferring the check's own property. A value whose placeholder cannot be resolved counts
     * as not configured, so a missing system property is never sent as the password.
     */
    private String credential(Properties config, String specificProperty, String sharedProperty) {

        String specific = value(config, specificProperty);
        String chosen = specific != null ? specificProperty : sharedProperty;
        String value = specific != null ? specific : value(config, sharedProperty);
        if (value != null && PLACEHOLDER.matcher(value).find()) {
            log.warn("Health checker property '" + chosen + "' refers to a system property or environment variable "
                    + "that is not set; treating it as not configured");
            return null;
        }
        return value;
    }

    /**
     * @return the trimmed property with placeholders resolved, or {@code null} if it is not set or blank
     */
    private String value(Properties config, String property) {

        String raw = config == null ? null : config.getProperty(property);
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        return resolvePlaceholders(raw.trim());
    }

    /**
     * Replaces each {@code ${name}}, {@code $sys{name}} and {@code $env{NAME}} that is set; others are left as is.
     * * {@code ${admin.username}} and {@code ${admin.password}} give the super admin account.
     */
    String resolvePlaceholders(String text) {

        Matcher matcher = PLACEHOLDER.matcher(text);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(2).trim();
            String replacement = "env".equals(matcher.group(1)) ? environment.apply(name)
                    : systemProperties.apply(name);
            matcher.appendReplacement(resolved,
                    Matcher.quoteReplacement(replacement == null ? matcher.group() : replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private String localHost() {

        String localIp = systemProperties.apply(LOCAL_IP_PROPERTY);
        return localIp == null || localIp.trim().isEmpty() ? DEFAULT_HOST : localIp.trim();
    }

    /**
     * Reads a port system property set at startup, falling back to the default port plus offset.
     */
    private int portProperty(String systemProperty, int defaultPort) {

        String value = systemProperties.apply(systemProperty);
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
