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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

import java.util.List;
import java.util.Properties;

public class EndpointResolverTest {

    private static final String LOCAL_DEFAULT = "tcp://10.0.0.5:9611";
    private static final String LOCAL_DEFAULT_AUTH = "ssl://10.0.0.5:9711";
    private static final String BROKER_CREDENTIALS = "amqp://tmUser:tmPass@clientid/carbon?brokerlist=";

    private final EndpointResolver resolver = new EndpointResolver(() -> null, () -> 0);

    @Before
    @After
    public void clearPortProperties() {

        System.clearProperty(EndpointResolver.RECEIVER_PORT_PROPERTY);
        System.clearProperty(EndpointResolver.AUTH_PORT_PROPERTY);
        System.clearProperty(EndpointResolver.JMS_PORT_PROPERTY);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Throttle receiver
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    public void usesHostAndOffsetPortFromConfiguredReceiverUrls() {

        List<ProbeEndpoint> endpoints = parseReceivers("tcp://tm-node-1:9612", "ssl://tm-node-1:9712");

        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(CheckType.THROTTLE_RECEIVER, endpoints.get(0).getCheckType());
        Assert.assertEquals("tm-node-1", endpoints.get(0).getHost());
        Assert.assertEquals(9612, endpoints.get(0).getPort());
        Assert.assertEquals("tm-node-1", endpoints.get(0).getAuthHost());
        Assert.assertEquals(9712, endpoints.get(0).getAuthPort());
    }

    @Test
    public void returnsOneReceiverPerUrlInFailoverAndLoadBalancedGroups() {

        List<ProbeEndpoint> failover = parseReceivers("tcp://a:9611|tcp://b:9611", "ssl://a:9711|ssl://b:9711");
        List<ProbeEndpoint> loadBalanced = parseReceivers("{tcp://a:9611},{tcp://b:9611}",
                "{ssl://a:9711},{ssl://b:9711}");

        Assert.assertEquals(2, failover.size());
        Assert.assertEquals("b", failover.get(1).getHost());
        Assert.assertEquals("b", failover.get(1).getAuthHost());
        Assert.assertEquals(2, loadBalanced.size());
        Assert.assertEquals("tcp://b:9611", loadBalanced.get(1).getUrl());
    }

    @Test
    public void fallsBackToStartupPortPropertiesWhenReceiverUrlHasNoPort() {

        System.setProperty(EndpointResolver.RECEIVER_PORT_PROPERTY, "9613");
        System.setProperty(EndpointResolver.AUTH_PORT_PROPERTY, "9713");

        List<ProbeEndpoint> endpoints = parseReceivers("tcp://tm-node-1", null);

        Assert.assertEquals(9613, endpoints.get(0).getPort());
        Assert.assertEquals("tm-node-1", endpoints.get(0).getAuthHost());
        Assert.assertEquals(9713, endpoints.get(0).getAuthPort());
    }

    @Test
    public void reusesFirstAuthUrlWhenThereAreFewerAuthUrls() {

        List<ProbeEndpoint> endpoints = parseReceivers("tcp://a:9611|tcp://b:9611", "ssl://auth:9711");

        Assert.assertEquals("ssl://auth:9711", endpoints.get(1).getAuthUrl());
    }

    @Test
    public void returnsNoReceiverWhenUrlGroupIsMissingOrUnresolved() {

        Assert.assertTrue(parseReceivers(null, null).isEmpty());
        Assert.assertTrue(parseReceivers("tcp://${carbon.local.ip}:9611", null).isEmpty());
        Assert.assertTrue(resolver.resolveThrottleReceivers().isEmpty());
    }

    @Test
    public void usesThrottlingReceiverUrlsAndCredentials() {

        EndpointResolver configured = resolverFor(
                trafficManager("tcp://tm-lb:9611", "ssl://tm-lb:9711", "tmUser", "tmPass"), null, null, 0);

        List<ProbeEndpoint> endpoints = configured.resolveThrottleReceivers();

        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals("tcp://tm-lb:9611", endpoints.get(0).getUrl());
        Assert.assertEquals(EndpointResolver.THROTTLING_SOURCE, endpoints.get(0).getSource());
        Assert.assertEquals("tmUser", endpoints.get(0).getUsername());
        Assert.assertEquals("tmPass", endpoints.get(0).getPassword());
    }

    @Test
    public void usesTheLocalDefaultWhenCustomerDidNotSetThrottlingUrls() {

        // The configuration template writes this default when no [apim.throttling] URL is set.
        EndpointResolver configured = resolverFor(
                trafficManager(LOCAL_DEFAULT, LOCAL_DEFAULT_AUTH, "tmUser", "tmPass"), null, null, 0);

        Assert.assertEquals(LOCAL_DEFAULT, configured.resolveThrottleReceivers().get(0).getUrl());
    }

    @Test
    public void probesARepeatedReceiverUrlOnlyOnce() {

        EndpointResolver configured = resolverFor(
                trafficManager("tcp://tm-1:9611|tcp://tm-1:9611", "ssl://tm-1:9711", "tmUser", "tmPass"), null, null,
                0);

        Assert.assertEquals(1, configured.resolveThrottleReceivers().size());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Message broker
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    public void returnsThePlainBrokerPortAndTheOffsetSslPortForTheConfiguredHost() {

        List<ProbeEndpoint> endpoints = resolveBrokers(BROKER_CREDENTIALS + "'tcp://tm-node-1:5673'", 1, true);

        Assert.assertEquals(2, endpoints.size());
        Assert.assertEquals(CheckType.MESSAGE_BROKER, endpoints.get(0).getCheckType());
        Assert.assertEquals("amqp://tm-node-1:5673", endpoints.get(0).getUrl());
        Assert.assertEquals("amqps://tm-node-1:8673", endpoints.get(1).getUrl());
        Assert.assertEquals(EndpointResolver.THROTTLING_SOURCE, endpoints.get(0).getSource());
    }

    @Test
    public void rewritesTheBrokerUrlToOneBrokerAndKeepsTheCredentials() {

        List<ProbeEndpoint> endpoints = resolveBrokers(BROKER_CREDENTIALS
                + "'tcp://a:5672?retries='5'%26connectdelay='50';tcp://b:5672?retries='5'%26connectdelay='50';'", 0,
                true);

        Assert.assertEquals(4, endpoints.size());
        Assert.assertEquals(BROKER_CREDENTIALS + "'tcp://b:5672'", brokerUrl(endpoints.get(1)));
        Assert.assertEquals(BROKER_CREDENTIALS + "'tcp://a:8672?ssl='true''", brokerUrl(endpoints.get(2)));
        Assert.assertEquals("TopicConnectionFactory",
                endpoints.get(0).getJmsParameters().getProperty(JMSConstants.PARAM_CONFAC_JNDI_NAME));
    }

    @Test
    public void usesTheConfiguredPortAsTheSslPortWhenTheBrokerUrlIsAlreadySsl() {

        System.setProperty(EndpointResolver.JMS_PORT_PROPERTY, "5674");

        List<ProbeEndpoint> endpoints = resolveBrokers(BROKER_CREDENTIALS + "'tcp://tm-node-1:8674?ssl='true''", 2,
                true);

        Assert.assertEquals("amqp://tm-node-1:5674", endpoints.get(0).getUrl());
        Assert.assertEquals("amqps://tm-node-1:8674", endpoints.get(1).getUrl());
    }

    @Test
    public void skipsTheBrokerSslPortWhenItIsTurnedOff() {

        List<ProbeEndpoint> endpoints = resolveBrokers(BROKER_CREDENTIALS + "'tcp://tm-node-1:5672'", 0, false);

        Assert.assertEquals(1, endpoints.size());
        Assert.assertFalse(endpoints.get(0).isSecure());
    }

    @Test
    public void probesARepeatedBrokerHostOnlyOnce() {

        List<ProbeEndpoint> endpoints = resolveBrokers(BROKER_CREDENTIALS + "'tcp://a:5672;tcp://a:5672'", 0, true);

        Assert.assertEquals(2, endpoints.size());
    }

    @Test
    public void returnsNoBrokerWhenTheUrlIsMissingOrUnresolved() {

        Assert.assertTrue(resolveBrokers(null, 0, true).isEmpty());
        Assert.assertTrue(resolveBrokers(BROKER_CREDENTIALS + "'tcp://${carbon.local.ip}:${jms.port}'", 0, true)
                .isEmpty());
        Assert.assertTrue(resolver.resolveMessageBrokers(true).isEmpty());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Both
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    public void ignoresTheEventHubConfiguration() {

        Properties eventHubJms = new Properties();
        eventHubJms.setProperty(JMSConstants.CONNECTION_STRING_TOPIC, BROKER_CREDENTIALS + "'tcp://cp-1:5672'");
        EventHubConfigurationDto.EventHubReceiverConfiguration receiver =
                new EventHubConfigurationDto.EventHubReceiverConfiguration();
        receiver.setJmsConnectionParameters(eventHubJms);
        EventHubConfigurationDto.EventHubPublisherConfiguration publisher =
                new EventHubConfigurationDto.EventHubPublisherConfiguration();
        publisher.setReceiverUrlGroup("tcp://cp-1:9611");
        publisher.setAuthUrlGroup("ssl://cp-1:9711");
        EventHubConfigurationDto eventHub = new EventHubConfigurationDto();
        eventHub.setEventHubReceiverConfiguration(receiver);
        eventHub.setEventHubPublisherConfiguration(publisher);
        eventHub.setUsername("hubUser");
        eventHub.setPassword("hubPass".toCharArray());

        EndpointResolver eventHubOnly = resolverFor(null, null, eventHub, 0);

        Assert.assertTrue(eventHubOnly.resolveThrottleReceivers().isEmpty());
        Assert.assertTrue(eventHubOnly.resolveMessageBrokers(true).isEmpty());
    }

    private List<ProbeEndpoint> parseReceivers(String receiverUrlGroup, String authUrlGroup) {

        return resolver.parseThrottleReceivers("test", receiverUrlGroup, authUrlGroup, "user", "pass");
    }

    private static List<ProbeEndpoint> resolveBrokers(String connectionUrl, int portOffset, boolean includeSslPort) {

        Properties jms = new Properties();
        jms.setProperty(JMSConstants.PARAM_CONFAC_JNDI_NAME, "TopicConnectionFactory");
        if (connectionUrl != null) {
            jms.setProperty(JMSConstants.CONNECTION_STRING_TOPIC, connectionUrl);
        }
        return resolverFor(null, jms, null, portOffset).resolveMessageBrokers(includeSslPort);
    }

    private static String brokerUrl(ProbeEndpoint endpoint) {

        return endpoint.getJmsParameters().getProperty(JMSConstants.CONNECTION_STRING_TOPIC);
    }

    private static EndpointResolver resolverFor(ThrottleProperties.TrafficManager trafficManager,
                                                Properties throttlingJms, EventHubConfigurationDto eventHub,
                                                int portOffset) {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setTrafficManager(trafficManager);
        if (throttlingJms != null) {
            ThrottleProperties.JMSConnectionProperties jmsConnection =
                    new ThrottleProperties.JMSConnectionProperties();
            jmsConnection.setJmsConnectionProperties(throttlingJms);
            throttleProperties.setJmsConnectionProperties(jmsConnection);
        }
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(configuration.getEventHubConfigurationDto()).thenReturn(eventHub);
        return new EndpointResolver(() -> configuration, () -> portOffset);
    }

    private static ThrottleProperties.TrafficManager trafficManager(String receiverUrlGroup, String authUrlGroup,
                                                                    String username, String password) {

        ThrottleProperties.TrafficManager trafficManager = new ThrottleProperties.TrafficManager();
        trafficManager.setReceiverUrlGroup(receiverUrlGroup);
        trafficManager.setAuthUrlGroup(authUrlGroup);
        trafficManager.setUsername(username);
        trafficManager.setPassword(password);
        return trafficManager;
    }
}
