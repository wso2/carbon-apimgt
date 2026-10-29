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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;

public class EndpointResolverTest {

    private final Map<String, String> systemProperties = new HashMap<>();
    private final Map<String, String> environment = new HashMap<>();
    private int portOffset;

    private final EndpointResolver resolver = new EndpointResolver(() -> portOffset, systemProperties::get,
            environment::get);

    public EndpointResolverTest() {

        systemProperties.put(EndpointResolver.LOCAL_IP_PROPERTY, "10.0.0.5");
    }

    @Test
    public void probesTheLocalReceiverOnTheStandardPortsWhenNoAddressIsConfigured() {

        List<ProbeEndpoint> endpoints = resolver.resolveThrottleReceivers(credentials());

        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals(CheckType.THROTTLE_RECEIVER, endpoints.get(0).getCheckType());
        Assert.assertEquals("tcp://10.0.0.5:9611", endpoints.get(0).getUrl());
        Assert.assertEquals("ssl://10.0.0.5:9711", endpoints.get(0).getAuthUrl());
        Assert.assertEquals(EndpointResolver.DEFAULT_SOURCE, endpoints.get(0).getSource());
    }

    @Test
    public void probesTheLocalBrokerOnBothStandardPortsWhenNoAddressIsConfigured() {

        List<ProbeEndpoint> endpoints = resolver.resolveMessageBrokers(credentials(), true);

        Assert.assertEquals(2, endpoints.size());
        Assert.assertEquals("amqp://10.0.0.5:5672", endpoints.get(0).getUrl());
        Assert.assertEquals("amqps://10.0.0.5:8672", endpoints.get(1).getUrl());
        Assert.assertEquals(EndpointResolver.DEFAULT_SOURCE, endpoints.get(0).getSource());
    }

    @Test
    public void addsThePortOffsetToTheDefaultPorts() {

        portOffset = 3;

        ProbeEndpoint receiver = resolver.resolveThrottleReceivers(credentials()).get(0);
        List<ProbeEndpoint> brokers = resolver.resolveMessageBrokers(credentials(), true);

        Assert.assertEquals(9614, receiver.getPort());
        Assert.assertEquals(9714, receiver.getAuthPort());
        Assert.assertEquals(5675, brokers.get(0).getPort());
        Assert.assertEquals(8675, brokers.get(1).getPort());
    }

    @Test
    public void prefersThePortsTheServerPublishedAtStartup() {

        portOffset = 3;
        systemProperties.put(EndpointResolver.RECEIVER_PORT_PROPERTY, "9620");
        systemProperties.put(EndpointResolver.AUTH_PORT_PROPERTY, "9720");
        systemProperties.put(EndpointResolver.JMS_PORT_PROPERTY, "5680");

        ProbeEndpoint receiver = resolver.resolveThrottleReceivers(credentials()).get(0);
        ProbeEndpoint broker = resolver.resolveMessageBrokers(credentials(), false).get(0);

        Assert.assertEquals(9620, receiver.getPort());
        Assert.assertEquals(9720, receiver.getAuthPort());
        Assert.assertEquals(5680, broker.getPort());
    }

    @Test
    public void usesLocalhostWhenTheLocalIpIsNotKnown() {

        systemProperties.remove(EndpointResolver.LOCAL_IP_PROPERTY);

        Assert.assertEquals("localhost", resolver.resolveThrottleReceivers(credentials()).get(0).getHost());
        Assert.assertEquals("localhost", resolver.resolveMessageBrokers(credentials(), false).get(0).getHost());
    }

    @Test
    public void usesTheConfiguredReceiverAndAuthUrls() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "tcp://tm-node-1:9612");
        config.setProperty(EndpointResolver.RECEIVER_AUTH_URLS_PROPERTY, "ssl://tm-node-1:9712");

        List<ProbeEndpoint> endpoints = resolver.resolveThrottleReceivers(config);

        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals("tcp://tm-node-1:9612", endpoints.get(0).getUrl());
        Assert.assertEquals("ssl://tm-node-1:9712", endpoints.get(0).getAuthUrl());
        Assert.assertEquals(EndpointResolver.CONFIG_SOURCE, endpoints.get(0).getSource());
    }

    @Test
    public void pairsEachReceiverWithItsAuthUrlThenTheFirstOne() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "tcp://a:9611, tcp://b:9611, tcp://c:9611");
        config.setProperty(EndpointResolver.RECEIVER_AUTH_URLS_PROPERTY, "ssl://a:9711,ssl://b:9711");

        List<ProbeEndpoint> endpoints = resolver.resolveThrottleReceivers(config);

        Assert.assertEquals(3, endpoints.size());
        Assert.assertEquals("ssl://a:9711", endpoints.get(0).getAuthUrl());
        Assert.assertEquals("ssl://b:9711", endpoints.get(1).getAuthUrl());
        Assert.assertEquals("ssl://a:9711", endpoints.get(2).getAuthUrl());
    }

    @Test
    public void readsReceiverUrlsInFailoverAndLoadBalancedGroupForm() {

        Properties failover = credentials();
        failover.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "tcp://a:9611|tcp://b:9611");
        Properties loadBalanced = credentials();
        loadBalanced.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "{tcp://a:9611},{tcp://b:9611}");

        Assert.assertEquals(2, resolver.resolveThrottleReceivers(failover).size());
        Assert.assertEquals("tcp://b:9611", resolver.resolveThrottleReceivers(loadBalanced).get(1).getUrl());
    }

    @Test
    public void logsInOnTheReceiversOwnHostWhenNoAuthUrlIsConfigured() {

        portOffset = 2;
        Properties config = credentials();
        config.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "tcp://tm-node-1");

        ProbeEndpoint endpoint = resolver.resolveThrottleReceivers(config).get(0);

        Assert.assertEquals("tcp://tm-node-1:9613", endpoint.getUrl());
        Assert.assertEquals("ssl://tm-node-1:9713", endpoint.getAuthUrl());
    }

    @Test
    public void probesEachReceiverUrlOnlyOnce() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "tcp://a:9611,tcp://a:9611");

        Assert.assertEquals(1, resolver.resolveThrottleReceivers(config).size());
    }

    @Test
    public void resolvesSystemPropertyPlaceholdersInAddresses() {

        systemProperties.put(EndpointResolver.RECEIVER_PORT_PROPERTY, "9615");
        Properties config = credentials();
        config.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "tcp://${carbon.local.ip}:${receiver.url.port}");
        config.setProperty(EndpointResolver.BROKER_URLS_PROPERTY, "tcp://$sys{carbon.local.ip}:5679");

        Assert.assertEquals("tcp://10.0.0.5:9615", resolver.resolveThrottleReceivers(config).get(0).getUrl());
        Assert.assertEquals("amqp://10.0.0.5:5679", resolver.resolveMessageBrokers(config, false).get(0).getUrl());
    }

    @Test
    public void skipsAnAddressWhosePlaceholderIsNotSetOrWhosePortIsInvalid() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY,
                "tcp://${tm.host}:9611,tcp://a:99999999999,tcp://b:9611");
        config.setProperty(EndpointResolver.BROKER_URLS_PROPERTY, "tcp://${tm.host}:5672;tcp://a:99999999999");

        List<ProbeEndpoint> receivers = resolver.resolveThrottleReceivers(config);

        Assert.assertEquals(1, receivers.size());
        Assert.assertEquals("b", receivers.get(0).getHost());
        Assert.assertTrue(resolver.resolveMessageBrokers(config, true).isEmpty());
    }

    @Test
    public void treatsABlankAddressAsNotConfigured() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "  ");

        ProbeEndpoint endpoint = resolver.resolveThrottleReceivers(config).get(0);

        Assert.assertEquals("tcp://10.0.0.5:9611", endpoint.getUrl());
        Assert.assertEquals(EndpointResolver.DEFAULT_SOURCE, endpoint.getSource());
    }

    @Test
    public void usesTheSharedCredentialsForBothChecks() {

        ProbeEndpoint receiver = resolver.resolveThrottleReceivers(credentials()).get(0);
        ProbeEndpoint broker = resolver.resolveMessageBrokers(credentials(), false).get(0);

        Assert.assertEquals("tmUser", receiver.getUsername());
        Assert.assertEquals("tmPass", receiver.getPassword());
        Assert.assertEquals("tmUser", broker.getUsername());
        Assert.assertEquals("tmPass", broker.getPassword());
    }

    @Test
    public void prefersEachChecksOwnCredentials() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.RECEIVER_USERNAME_PROPERTY, "receiverUser");
        config.setProperty(EndpointResolver.RECEIVER_PASSWORD_PROPERTY, "receiverPass");
        config.setProperty(EndpointResolver.BROKER_PASSWORD_PROPERTY, "brokerPass");

        ProbeEndpoint receiver = resolver.resolveThrottleReceivers(config).get(0);
        ProbeEndpoint broker = resolver.resolveMessageBrokers(config, false).get(0);

        Assert.assertEquals("receiverUser", receiver.getUsername());
        Assert.assertEquals("receiverPass", receiver.getPassword());
        Assert.assertEquals("tmUser", broker.getUsername());
        Assert.assertEquals("brokerPass", broker.getPassword());
    }

    @Test
    public void readsCredentialsFromSystemPropertiesAndEnvironmentVariables() {

        systemProperties.put("tm.hc.username", "sysUser");
        environment.put("TM_HC_PASSWORD", "envPass");
        Properties config = new Properties();
        config.setProperty(EndpointResolver.USERNAME_PROPERTY, "$sys{tm.hc.username}");
        config.setProperty(EndpointResolver.PASSWORD_PROPERTY, "$env{TM_HC_PASSWORD}");

        ProbeEndpoint receiver = resolver.resolveThrottleReceivers(config).get(0);

        Assert.assertEquals("sysUser", receiver.getUsername());
        Assert.assertEquals("envPass", receiver.getPassword());
    }

    /**
     * Sending the placeholder text as the password would fail the login and could count towards an account lock.
     */
    @Test
    public void treatsACredentialWhosePlaceholderIsNotSetAsNotConfigured() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.PASSWORD_PROPERTY, "$sys{admin.password}");

        ProbeEndpoint receiver = resolver.resolveThrottleReceivers(config).get(0);
        ProbeEndpoint broker = resolver.resolveMessageBrokers(config, false).get(0);

        Assert.assertNull(receiver.getPassword());
        Assert.assertNull(broker.getPassword());
        Assert.assertNull(broker.getJmsParameters().getProperty(JMSConstants.CONNECTION_STRING_TOPIC));
    }

    @Test
    public void leavesCredentialsUnsetWhenNoneAreConfigured() {

        ProbeEndpoint receiver = resolver.resolveThrottleReceivers(null).get(0);

        Assert.assertNull(receiver.getUsername());
        Assert.assertNull(receiver.getPassword());
    }

    @Test
    public void buildsTheSameJmsSettingsAsTheProductForOneBrokerWithoutRetries() {

        Properties jms = resolver.resolveMessageBrokers(credentials(), false).get(0).getJmsParameters();

        Assert.assertEquals("amqp://tmUser:tmPass@clientid/carbon?brokerlist='tcp://10.0.0.5:5672'",
                jms.getProperty(JMSConstants.CONNECTION_STRING_TOPIC));
        Assert.assertEquals("TopicConnectionFactory", jms.getProperty(JMSConstants.PARAM_CONFAC_JNDI_NAME));
        Assert.assertEquals("topic", jms.getProperty(JMSConstants.PARAM_DEST_TYPE));
        Assert.assertEquals("org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
                jms.getProperty(Context.INITIAL_CONTEXT_FACTORY));
    }

    @Test
    public void readsSemicolonSeparatedBrokersAndKeepsOnlyTheirTlsOptions() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.BROKER_URLS_PROPERTY, "tcp://a:5672?retries='5'&connectdelay='50';"
                + "tcp://a:8672?ssl='true'&trust_store='/ts.jks'&trust_store_password='secret'&retries='5'");

        List<ProbeEndpoint> endpoints = resolver.resolveMessageBrokers(config, true);

        Assert.assertEquals(2, endpoints.size());
        Assert.assertEquals("amqp://a:5672", endpoints.get(0).getUrl());
        Assert.assertEquals(EndpointResolver.CONFIG_SOURCE, endpoints.get(0).getSource());
        Assert.assertEquals("amqp://tmUser:tmPass@clientid/carbon?brokerlist='tcp://a:5672'",
                endpoints.get(0).getJmsParameters().getProperty(JMSConstants.CONNECTION_STRING_TOPIC));
        Assert.assertTrue(endpoints.get(1).isSecure());
        Assert.assertEquals("amqp://tmUser:tmPass@clientid/carbon?brokerlist='tcp://a:8672?ssl='true'"
                        + "%26trust_store='/ts.jks'%26trust_store_password='secret''",
                endpoints.get(1).getJmsParameters().getProperty(JMSConstants.CONNECTION_STRING_TOPIC));
    }

    @Test
    public void usesTheStandardPortForAConfiguredBrokerWithoutOne() {

        portOffset = 1;
        Properties config = credentials();
        config.setProperty(EndpointResolver.BROKER_URLS_PROPERTY, "tcp://a,tcp://b?ssl='true'");

        List<ProbeEndpoint> endpoints = resolver.resolveMessageBrokers(config, false);

        Assert.assertEquals("amqp://a:5673", endpoints.get(0).getUrl());
        Assert.assertEquals("amqps://b:8673", endpoints.get(1).getUrl());
    }

    @Test
    public void addsTheMissingPairedPortOnlyWhenSslPortCheckIsEnabled() {

        Properties config = credentials();
        config.setProperty(EndpointResolver.BROKER_URLS_PROPERTY, "tcp://a:5672;tcp://b:8672?ssl='true'");

        List<ProbeEndpoint> withPairs = resolver.resolveMessageBrokers(config, true);
        List<ProbeEndpoint> withoutPairs = resolver.resolveMessageBrokers(config, false);

        Assert.assertEquals(4, withPairs.size());
        Assert.assertEquals("amqp://a:5672", withPairs.get(0).getUrl());
        Assert.assertEquals("amqp://b:5672", withPairs.get(1).getUrl());
        Assert.assertEquals("amqps://b:8672", withPairs.get(2).getUrl());
        Assert.assertEquals("amqps://a:8672", withPairs.get(3).getUrl());
        Assert.assertEquals(2, withoutPairs.size());
    }

    @Test
    public void resolvesEnvironmentPlaceholdersAndKeepsUnknownOnes() {

        environment.put("TM_HOST", "tm-node-9");

        Assert.assertEquals("tcp://tm-node-9:${missing}",
                resolver.resolvePlaceholders("tcp://$env{TM_HOST}:${missing}"));
    }

    private static Properties credentials() {

        Properties config = new Properties();
        config.setProperty(EndpointResolver.USERNAME_PROPERTY, "tmUser");
        config.setProperty(EndpointResolver.PASSWORD_PROPERTY, "tmPass");
        return config;
    }
}
