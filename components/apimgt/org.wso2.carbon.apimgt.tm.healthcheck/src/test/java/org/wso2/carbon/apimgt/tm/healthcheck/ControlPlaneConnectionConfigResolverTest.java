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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.tm.healthcheck.internal.ServiceReferenceHolder;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ControlPlaneConnectionConfigResolver} (Convergence T033) - previously entirely
 * untested (always replaced with a Mockito mock in {@link TMControlPlaneConnectivityHealthCheckerTest}),
 * which let {@code resolveManagementApi()}'s hardcoded {@code tlsEnabled = true} bug go undetected.
 *
 * <p>Tests install a mocked {@link APIManagerConfigurationService} into {@link ServiceReferenceHolder} -
 * the same seam {@link org.wso2.carbon.apimgt.tm.healthcheck.internal.TMHealthCheckServiceComponent} uses
 * at runtime - and a real {@link EventHubConfigurationDto} (a plain data object with public setters, so no
 * mock is needed for it), then exercise {@link ControlPlaneConnectionConfigResolver} directly.</p>
 */
public class ControlPlaneConnectionConfigResolverTest {

    private ControlPlaneConnectionConfigResolver resolver;

    @Before
    public void setUp() {

        resolver = new ControlPlaneConnectionConfigResolver();
    }

    @After
    public void tearDown() {

        // ServiceReferenceHolder is a process-wide singleton - unbind so this test's stub never leaks into
        // another test class running in the same JVM.
        ServiceReferenceHolder.getInstance().bindApiManagerConfigurationService(null);
    }

    /**
     * Regression test for Convergence T033 (FR-002a): a management service URL configured over plain HTTP
     * must resolve to {@code tlsEnabled = false}, so stage 1 dials it with a plain TCP connect rather than
     * wrongly attempting a TLS handshake over a non-TLS port. Before this fix, {@code resolveManagementApi()}
     * hardcoded {@code tlsEnabled = true} regardless of the configured URL's actual scheme.
     */
    @Test
    public void testResolveManagementApiWithHttpServiceUrlIsNotTlsEnabled() {

        stubEventHubConfigurationDto(dtoWithServiceUrl("http://cp.example.internal:9763"));

        ResolvedConnectionConfig config = resolver.resolve(ControlPlaneConnectionId.MANAGEMENT_API);

        assertFalse("A plain-HTTP management service URL must not be treated as TLS", config.isTlsEnabled());
        assertEquals("cp.example.internal", config.getHost());
        assertEquals(9763, config.getPort());
    }

    /**
     * Companion to the HTTP case above: an HTTPS-configured management service URL must still resolve to
     * {@code tlsEnabled = true}, so this fix does not simply flip the flag to always {@code false}.
     */
    @Test
    public void testResolveManagementApiWithHttpsServiceUrlIsTlsEnabled() {

        stubEventHubConfigurationDto(dtoWithServiceUrl("https://cp.example.internal:9443"));

        ResolvedConnectionConfig config = resolver.resolve(ControlPlaneConnectionId.MANAGEMENT_API);

        assertTrue("An HTTPS management service URL must be treated as TLS", config.isTlsEnabled());
        assertEquals("cp.example.internal", config.getHost());
        assertEquals(9443, config.getPort());
    }

    /**
     * When the service URL omits an explicit port, the scheme-appropriate default must be used (443 for
     * HTTPS, 80 for HTTP) - and that default must stay paired with the correct {@code tlsEnabled} value for
     * its own scheme, not the hardcoded one this fix removes.
     */
    @Test
    public void testResolveManagementApiDefaultPortsMatchTheirOwnScheme() {

        stubEventHubConfigurationDto(dtoWithServiceUrl("https://cp.example.internal"));
        ResolvedConnectionConfig httpsConfig = resolver.resolve(ControlPlaneConnectionId.MANAGEMENT_API);
        assertEquals(443, httpsConfig.getPort());
        assertTrue(httpsConfig.isTlsEnabled());

        stubEventHubConfigurationDto(dtoWithServiceUrl("http://cp.example.internal"));
        ResolvedConnectionConfig httpConfig = resolver.resolve(ControlPlaneConnectionId.MANAGEMENT_API);
        assertEquals(80, httpConfig.getPort());
        assertFalse(httpConfig.isTlsEnabled());
    }

    @Test
    public void testResolveManagementApiWithNoServiceUrlReturnsNull() {

        stubEventHubConfigurationDto(new EventHubConfigurationDto());

        assertNull(resolver.resolve(ControlPlaneConnectionId.MANAGEMENT_API));
    }

    @Test
    public void testResolveWithNoLiveConfigurationReturnsNullForEveryConnection() {

        // No APIManagerConfigurationService bound at all - ServiceReferenceHolder.getApiManagerConfiguration()
        // returns null, which every resolve() path must treat as "not currently resolvable", never throw.
        assertNull(resolver.resolve(ControlPlaneConnectionId.THROTTLING_DATA));
        assertNull(resolver.resolve(ControlPlaneConnectionId.MANAGEMENT_API));
        assertFalse(resolver.isTlsMessagingEnabled());
    }

    /**
     * Baseline sanity coverage for the fixed-port connections, so {@link ControlPlaneConnectionConfigResolver}
     * as a whole is no longer entirely untested: the throttling-data receiver/auth URL groups resolve to the
     * host embedded in their configured URL, paired with each connection's own fixed port - never a port
     * parsed out of the URL (ports 9611/9711 are fixed by product convention, per spec.md FR-002).
     */
    @Test
    public void testResolveThrottlingDataConnectionsUseTheirOwnFixedPorts() {

        EventHubConfigurationDto dto = new EventHubConfigurationDto();
        EventHubConfigurationDto.EventHubPublisherConfiguration publisherConfiguration =
                new EventHubConfigurationDto.EventHubPublisherConfiguration();
        publisherConfiguration.setReceiverUrlGroup("tcp://cp.example.internal:9611");
        publisherConfiguration.setAuthUrlGroup("ssl://cp.example.internal:9711");
        dto.setEventHubPublisherConfiguration(publisherConfiguration);
        stubEventHubConfigurationDto(dto);

        ResolvedConnectionConfig throttlingData = resolver.resolve(ControlPlaneConnectionId.THROTTLING_DATA);
        ResolvedConnectionConfig throttlingDataSecure =
                resolver.resolve(ControlPlaneConnectionId.THROTTLING_DATA_SECURE);

        assertEquals("cp.example.internal", throttlingData.getHost());
        assertEquals(9611, throttlingData.getPort());
        assertFalse(throttlingData.isTlsEnabled());
        assertEquals("cp.example.internal", throttlingDataSecure.getHost());
        assertEquals(9711, throttlingDataSecure.getPort());
        assertTrue(throttlingDataSecure.isTlsEnabled());
    }

    /**
     * {@code isTlsMessagingEnabled()} must reflect the live {@code ssl='true'} marker in the configured
     * broker connection URL - the only signal this configuration carries for whether 8672 is applicable.
     */
    @Test
    public void testIsTlsMessagingEnabledReflectsTheConfiguredBrokerUrl() {

        stubEventHubConfigurationDto(dtoWithBrokerConnectionUrl(
                "amqp://admin:admin@clientID/carbon?brokerlist='tcp://cp.example.internal:5672'"));
        assertFalse(resolver.isTlsMessagingEnabled());

        stubEventHubConfigurationDto(dtoWithBrokerConnectionUrl(
                "amqp://admin:admin@clientID/carbon?brokerlist='tcp://cp.example.internal:5672?ssl='true''"));
        assertTrue(resolver.isTlsMessagingEnabled());
    }

    private EventHubConfigurationDto dtoWithServiceUrl(String serviceUrl) {

        EventHubConfigurationDto dto = new EventHubConfigurationDto();
        dto.setServiceUrl(serviceUrl);
        return dto;
    }

    private EventHubConfigurationDto dtoWithBrokerConnectionUrl(String brokerConnectionUrl) {

        EventHubConfigurationDto dto = new EventHubConfigurationDto();
        EventHubConfigurationDto.EventHubReceiverConfiguration receiverConfiguration =
                new EventHubConfigurationDto.EventHubReceiverConfiguration();
        Properties jmsConnectionParameters = new Properties();
        jmsConnectionParameters.setProperty("connectionfactory.TopicConnectionFactory", brokerConnectionUrl);
        receiverConfiguration.setJmsConnectionParameters(jmsConnectionParameters);
        dto.setEventHubReceiverConfiguration(receiverConfiguration);
        return dto;
    }

    private void stubEventHubConfigurationDto(EventHubConfigurationDto dto) {

        APIManagerConfiguration apiManagerConfiguration = mock(APIManagerConfiguration.class);
        when(apiManagerConfiguration.getEventHubConfigurationDto()).thenReturn(dto);
        APIManagerConfigurationService configurationService = mock(APIManagerConfigurationService.class);
        when(configurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        ServiceReferenceHolder.getInstance().bindApiManagerConfigurationService(configurationService);
    }
}
