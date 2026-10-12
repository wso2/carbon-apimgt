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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.tm.healthcheck.internal.ServiceReferenceHolder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the live, currently-configured Control Plane connection details for each
 * {@link ControlPlaneConnectionId}, reading {@link APIManagerConfiguration#getEventHubConfigurationDto()}
 * fresh on every call - the same configuration object the Traffic Manager's own Event Hub
 * publisher/receiver code already uses to open these exact connections (Research &sect;2).
 *
 * <p>This class never caches a resolved host/port/TLS-enabled value across calls (FR-012): every public
 * method here re-reads the live {@link EventHubConfigurationDto} from scratch, so a live configuration
 * change (e.g. a Control Plane migration) is reflected starting with the very next call, not just after
 * a restart.</p>
 */
public class ControlPlaneConnectionConfigResolver {

    private static final Log log = LogFactory.getLog(ControlPlaneConnectionConfigResolver.class);

    /**
     * The key under which the Control Plane message broker's connection URL is stored inside
     * {@link EventHubConfigurationDto.EventHubReceiverConfiguration#getJmsConnectionParameters()}
     * (Research &sect;2).
     */
    private static final String BROKER_CONNECTION_FACTORY_PROPERTY = "connectionfactory.TopicConnectionFactory";

    /**
     * Matches a {@code tcp://host:port} or {@code ssl://host:port} fragment embedded inside the broker
     * connection URL, regardless of the surrounding AMQP {@code brokerlist='...'} quoting.
     */
    private static final Pattern BROKER_HOST_PORT_PATTERN =
            Pattern.compile("(tcp|ssl)://([A-Za-z0-9_.-]+):(\\d+)");

    /**
     * Matches an {@code ssl='true'} (or {@code ssl=true}) marker anywhere in the broker connection URL,
     * which is how TLS messaging being enabled is signalled in this configuration - there is no separate
     * boolean property for it (Research &sect;2).
     */
    private static final Pattern TLS_MARKER_PATTERN =
            Pattern.compile("ssl\\s*=\\s*'?true'?", Pattern.CASE_INSENSITIVE);

    /**
     * Resolves the current connection details for the given connection.
     *
     * @param connectionId which Control Plane connection to resolve
     * @return the resolved host/port/TLS details, or {@code null} if the live configuration does not
     *         (yet) have enough information to resolve this connection - callers must treat a
     *         {@code null} result the same as a failed connectivity check for that connection, never as
     *         an exception
     */
    public ResolvedConnectionConfig resolve(ControlPlaneConnectionId connectionId) {

        EventHubConfigurationDto eventHubConfigurationDto = getEventHubConfigurationDto();
        if (eventHubConfigurationDto == null) {
            log.warn("Cannot resolve Control Plane connection " + connectionId
                    + " - no live EventHubConfigurationDto is available");
            return null;
        }
        switch (connectionId) {
            case THROTTLING_DATA:
                return resolveFromUrlGroup(connectionId, getReceiverUrlGroup(eventHubConfigurationDto), false);
            case THROTTLING_DATA_SECURE:
                return resolveFromUrlGroup(connectionId, getAuthUrlGroup(eventHubConfigurationDto), true);
            case MESSAGE_BROKER:
                return resolveMessageBroker(connectionId, eventHubConfigurationDto, false);
            case MESSAGE_BROKER_SECURE:
                return resolveMessageBroker(connectionId, eventHubConfigurationDto, true);
            case MANAGEMENT_API:
                return resolveManagementApi(connectionId, eventHubConfigurationDto);
            default:
                return null;
        }
    }

    /**
     * Returns whether the live configuration indicates TLS messaging is enabled - i.e. whether
     * {@link ControlPlaneConnectionId#MESSAGE_BROKER_SECURE} is applicable at all for this invocation
     * (data-model.md validation rule: 8672 counts toward {@code overallStatus} only when this is true).
     *
     * @return {@code true} if the broker connection URL indicates TLS messaging is configured
     */
    public boolean isTlsMessagingEnabled() {

        EventHubConfigurationDto eventHubConfigurationDto = getEventHubConfigurationDto();
        if (eventHubConfigurationDto == null) {
            return false;
        }
        String brokerConnectionUrl = getBrokerConnectionUrl(eventHubConfigurationDto);
        return brokerConnectionUrl != null && TLS_MARKER_PATTERN.matcher(brokerConnectionUrl).find();
    }

    /**
     * Returns the live {@code receiverUrlGroup} string (e.g. {@code "tcp://host:9611"}, possibly a
     * multi-node failover group) for constructing a private {@code DataPublisher} in
     * {@link ThrottlingDataAcceptConfirmationChecker} - the full URL-group string is needed there, not just the
     * single resolved host {@link #resolve(ControlPlaneConnectionId)} exposes.
     *
     * @return the live receiver URL group, or {@code null} if not currently resolvable
     */
    public String getThrottlingDataReceiverUrlGroup() {

        EventHubConfigurationDto eventHubConfigurationDto = getEventHubConfigurationDto();
        return eventHubConfigurationDto == null ? null : getReceiverUrlGroup(eventHubConfigurationDto);
    }

    /**
     * Returns the live {@code authUrlGroup} string (e.g. {@code "ssl://host:9711"}) for the same reason as
     * {@link #getThrottlingDataReceiverUrlGroup()}.
     *
     * @return the live auth URL group, or {@code null} if not currently resolvable
     */
    public String getThrottlingDataAuthUrlGroup() {

        EventHubConfigurationDto eventHubConfigurationDto = getEventHubConfigurationDto();
        return eventHubConfigurationDto == null ? null : getAuthUrlGroup(eventHubConfigurationDto);
    }

    /**
     * Returns the live Event Hub username, read fresh on every call - the same credential the Traffic Manager's
     * own Event Hub publisher/receiver and management-sync code already authenticates with.
     *
     * @return the live username, or {@code null} if not currently resolvable
     */
    public String getEventHubUsername() {

        EventHubConfigurationDto eventHubConfigurationDto = getEventHubConfigurationDto();
        return eventHubConfigurationDto == null ? null : eventHubConfigurationDto.getUsername();
    }

    /**
     * Returns the live Event Hub password, read fresh on every call. This value exists only to authenticate an
     * outbound accept-confirmation call during {@code checkHealth()} - it is never placed on the response
     * (FR-014).
     *
     * @return the live password, or {@code null} if not currently resolvable
     */
    public String getEventHubPassword() {

        EventHubConfigurationDto eventHubConfigurationDto = getEventHubConfigurationDto();
        return eventHubConfigurationDto == null ? null : eventHubConfigurationDto.getPassword();
    }

    /**
     * Returns the live Event Hub service URL (the management/REST connection's base URL), read fresh on every
     * call, for {@link ManagementApiAcceptConfirmationChecker} to build its one lightweight HTTPS call against.
     *
     * @return the live service URL, or {@code null} if not currently resolvable
     */
    public String getManagementServiceUrl() {

        EventHubConfigurationDto eventHubConfigurationDto = getEventHubConfigurationDto();
        return eventHubConfigurationDto == null ? null : eventHubConfigurationDto.getServiceUrl();
    }

    /**
     * Returns the live message broker {@code jmsConnectionParameters} bag, read fresh on every call, for
     * {@link MessageBrokerAcceptConfirmationChecker} to open its short-lived producer with - the same
     * connection-factory properties the Traffic Manager's own JMS consumer code already uses
     * ({@code JMSTransportHandler}).
     *
     * @return the live JMS connection parameters, or {@code null} if not currently resolvable
     */
    public java.util.Properties getJmsConnectionParameters() {

        EventHubConfigurationDto eventHubConfigurationDto = getEventHubConfigurationDto();
        if (eventHubConfigurationDto == null || eventHubConfigurationDto.getEventHubReceiverConfiguration() == null) {
            return null;
        }
        return eventHubConfigurationDto.getEventHubReceiverConfiguration().getJmsConnectionParameters();
    }

    private EventHubConfigurationDto getEventHubConfigurationDto() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getApiManagerConfiguration();
        if (apiManagerConfiguration == null) {
            return null;
        }
        return apiManagerConfiguration.getEventHubConfigurationDto();
    }

    private String getReceiverUrlGroup(EventHubConfigurationDto eventHubConfigurationDto) {

        if (eventHubConfigurationDto.getEventHubPublisherConfiguration() == null) {
            return null;
        }
        return eventHubConfigurationDto.getEventHubPublisherConfiguration().getReceiverUrlGroup();
    }

    private String getAuthUrlGroup(EventHubConfigurationDto eventHubConfigurationDto) {

        if (eventHubConfigurationDto.getEventHubPublisherConfiguration() == null) {
            return null;
        }
        return eventHubConfigurationDto.getEventHubPublisherConfiguration().getAuthUrlGroup();
    }

    private String getBrokerConnectionUrl(EventHubConfigurationDto eventHubConfigurationDto) {

        if (eventHubConfigurationDto.getEventHubReceiverConfiguration() == null) {
            return null;
        }
        Properties jmsConnectionParameters =
                eventHubConfigurationDto.getEventHubReceiverConfiguration().getJmsConnectionParameters();
        if (jmsConnectionParameters == null) {
            return null;
        }
        return jmsConnectionParameters.getProperty(BROKER_CONNECTION_FACTORY_PROPERTY);
    }

    private ResolvedConnectionConfig resolveFromUrlGroup(ControlPlaneConnectionId connectionId, String urlGroup,
                                                           boolean tlsEnabled) {

        if (urlGroup == null) {
            return null;
        }
        try {
            String host = new URI(urlGroup).getHost();
            if (host == null) {
                return null;
            }
            return new ResolvedConnectionConfig(connectionId, host, connectionId.getPort(), tlsEnabled);
        } catch (URISyntaxException e) {
            log.warn("Could not parse the configured URL for " + connectionId + ": " + e.getMessage());
            return null;
        }
    }

    private ResolvedConnectionConfig resolveMessageBroker(ControlPlaneConnectionId connectionId,
                                                            EventHubConfigurationDto eventHubConfigurationDto,
                                                            boolean secureVariant) {

        String brokerConnectionUrl = getBrokerConnectionUrl(eventHubConfigurationDto);
        if (brokerConnectionUrl == null) {
            return null;
        }
        if (secureVariant && !TLS_MARKER_PATTERN.matcher(brokerConnectionUrl).find()) {
            // 8672 is not applicable when TLS messaging is not configured (data-model.md validation rule).
            return null;
        }
        Matcher matcher = BROKER_HOST_PORT_PATTERN.matcher(brokerConnectionUrl);
        if (!matcher.find()) {
            log.warn("Could not extract a host/port from the configured message broker connection URL for "
                    + connectionId);
            return null;
        }
        String host = matcher.group(2);
        return new ResolvedConnectionConfig(connectionId, host, connectionId.getPort(), secureVariant);
    }

    private ResolvedConnectionConfig resolveManagementApi(ControlPlaneConnectionId connectionId,
                                                            EventHubConfigurationDto eventHubConfigurationDto) {

        String serviceUrl = eventHubConfigurationDto.getServiceUrl();
        if (serviceUrl == null) {
            return null;
        }
        try {
            URI uri = new URI(serviceUrl);
            String host = uri.getHost();
            if (host == null) {
                return null;
            }
            boolean tlsEnabled = "https".equalsIgnoreCase(uri.getScheme());
            int port = uri.getPort();
            if (port < 0) {
                port = tlsEnabled ? 443 : 80;
            }
            return new ResolvedConnectionConfig(connectionId, host, port, tlsEnabled);
        } catch (URISyntaxException e) {
            log.warn("Could not parse the configured management connection service URL: " + e.getMessage());
            return null;
        }
    }
}
