/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.axis2.context.MessageContext;
import org.apache.http.HttpHeaders;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tests for the request/response body wiring in {@link SynapseAnalyticsDataProvider#getProperties()}: the
 * request body stashed on the message context during the request flow is republished under the wire keys, the
 * response body is captured at event-collection time, and neither appears when body capture is disabled.
 *
 * <p>Kept separate from {@link SynapseAnalyticsDataProviderTestCase}, which deliberately uses plain Mockito;
 * these tests need PowerMock to stub the static {@link AnalyticsPayloadUtil} seam.</p>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AnalyticsPayloadUtil.class, SynapseAnalyticsDataProvider.class, ServiceReferenceHolder.class})
public class SynapseAnalyticsDataProviderBodyTestCase {

    private static final String REQUEST_BODY = "{\"in\":1}";
    private static final String RESPONSE_BODY = "{\"out\":2}";

    // Held as Object: it is only ever written straight back into the field, so there is nothing to gain from
    // an unchecked cast to Map<String, String>.
    private Object previousReporterProperties;

    /**
     * {@code reporterProperties} is a static cache that would otherwise leak between tests, and the provider
     * loads it (plus the mask configuration) from {@link ServiceReferenceHolder}, which has no OSGi service in a
     * unit test. Reset the cache and stand up a minimal configuration with headers off; the header tests
     * override it via {@link #configureHeaders}.
     */
    @Before
    public void resetStaticState() throws Exception {
        Field field = SynapseAnalyticsDataProvider.class.getDeclaredField("reporterProperties");
        field.setAccessible(true);
        previousReporterProperties = field.get(null);
        field.set(null, null);
        configureHeaders(false);
    }

    /**
     * Points {@link ServiceReferenceHolder} at an analytics configuration that reports the given
     * {@code send_headers} value and declares no header masks.
     */
    private static void configureHeaders(boolean sendHeaders) {
        APIManagerAnalyticsConfiguration analyticsConfiguration =
                Mockito.mock(APIManagerAnalyticsConfiguration.class);
        Mockito.when(analyticsConfiguration.getReporterProperties())
                .thenReturn(singleStringProperty(Constants.SEND_HEADER, String.valueOf(sendHeaders)));
        Mockito.when(analyticsConfiguration.getMaskDataProperties()).thenReturn(new HashMap<>());

        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIAnalyticsConfiguration()).thenReturn(analyticsConfiguration);

        ServiceReferenceHolder holder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(holder.getApiManagerConfigurationService()).thenReturn(configurationService);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(holder);
    }

    @After
    public void restoreStaticState() throws Exception {
        Field field = SynapseAnalyticsDataProvider.class.getDeclaredField("reporterProperties");
        field.setAccessible(true);
        field.set(null, previousReporterProperties);
    }

    // ----- request body -----

    @Test
    public void testGetPropertiesPublishesStashedRequestBody() throws Exception {
        mockPayloadUtil(true);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.REQUEST_BODY_PROPERTY, REQUEST_BODY);
        properties.put(Constants.REQUEST_BODY_TRANSFER_ENCODING_PROPERTY, Constants.TRANSFER_ENCODING_BASE64);

        Map<String, Object> custom = collect(properties, Collections.emptyMap(), new HashMap<>());

        Assert.assertEquals(REQUEST_BODY, custom.get(Constants.REQUEST_BODY));
        Assert.assertEquals(Constants.TRANSFER_ENCODING_BASE64,
                custom.get(Constants.REQUEST_BODY_TRANSFER_ENCODING));
    }

    @Test
    public void testGetPropertiesOmitsRequestEncodingWhenNotStashed() throws Exception {
        mockPayloadUtil(true);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.REQUEST_BODY_PROPERTY, REQUEST_BODY);

        Map<String, Object> custom = collect(properties, Collections.emptyMap(), new HashMap<>());

        Assert.assertEquals(REQUEST_BODY, custom.get(Constants.REQUEST_BODY));
        Assert.assertFalse(custom.containsKey(Constants.REQUEST_BODY_TRANSFER_ENCODING));
    }

    @Test
    public void testGetPropertiesIgnoresNonStringStashedRequestBody() throws Exception {
        mockPayloadUtil(true);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.REQUEST_BODY_PROPERTY, 42);

        Map<String, Object> custom = collect(properties, Collections.emptyMap(), new HashMap<>());

        Assert.assertFalse(custom.containsKey(Constants.REQUEST_BODY));
    }

    @Test
    public void testGetPropertiesPublishesRequestContentTypeCaseInsensitively() throws Exception {
        mockPayloadUtil(true);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.REQUEST_BODY_PROPERTY, REQUEST_BODY);

        // Lower-case header name, as delivered over HTTP/2.
        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("content-type", "application/xml");
        Map<String, Object> analyticsMetadata = new HashMap<>();
        analyticsMetadata.put(Constants.REQUEST_HEADERS, requestHeaders);

        Map<String, Object> custom = collect(properties, Collections.emptyMap(), analyticsMetadata);

        Assert.assertEquals("the body's content type must ride along even when send_headers is off",
                "application/xml", custom.get(Constants.REQUEST_CONTENT_TYPE));
    }

    @Test
    public void testGetPropertiesOmitsRequestContentTypeWhenHeaderAbsent() throws Exception {
        mockPayloadUtil(true);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.REQUEST_BODY_PROPERTY, REQUEST_BODY);

        Map<String, Object> analyticsMetadata = new HashMap<>();
        analyticsMetadata.put(Constants.REQUEST_HEADERS, new HashMap<String, Object>());

        Map<String, Object> custom = collect(properties, Collections.emptyMap(), analyticsMetadata);

        Assert.assertFalse(custom.containsKey(Constants.REQUEST_CONTENT_TYPE));
    }

    // ----- response body -----

    @Test
    public void testGetPropertiesPublishesResponseBody() throws Exception {
        mockPayloadUtil(true);
        stubExtractedResponseBody(new AnalyticsPayloadUtil.CapturedBody(RESPONSE_BODY,
                Constants.TRANSFER_ENCODING_BASE64));

        Map<String, Object> custom = collect(new HashMap<>(), Collections.emptyMap(), new HashMap<>());

        Assert.assertEquals(RESPONSE_BODY, custom.get(Constants.RESPONSE_BODY));
        Assert.assertEquals(Constants.TRANSFER_ENCODING_BASE64,
                custom.get(Constants.RESPONSE_BODY_TRANSFER_ENCODING));
    }

    @Test
    public void testGetPropertiesOmitsResponseEncodingWhenNull() throws Exception {
        mockPayloadUtil(true);
        stubExtractedResponseBody(new AnalyticsPayloadUtil.CapturedBody(RESPONSE_BODY, null));

        Map<String, Object> custom = collect(new HashMap<>(), Collections.emptyMap(), new HashMap<>());

        Assert.assertEquals(RESPONSE_BODY, custom.get(Constants.RESPONSE_BODY));
        Assert.assertFalse(custom.containsKey(Constants.RESPONSE_BODY_TRANSFER_ENCODING));
    }

    @Test
    public void testGetPropertiesOmitsResponseBodyWhenNothingCaptured() throws Exception {
        mockPayloadUtil(true);
        stubExtractedResponseBody(null);

        Map<String, Object> custom = collect(new HashMap<>(), Collections.emptyMap(), new HashMap<>());

        Assert.assertFalse(custom.containsKey(Constants.RESPONSE_BODY));
        Assert.assertFalse(custom.containsKey(Constants.RESPONSE_BODY_TRANSFER_ENCODING));
    }

    // ----- disabled -----

    @Test
    public void testGetPropertiesPublishesNoBodiesWhenCaptureDisabled() throws Exception {
        mockPayloadUtil(false);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.REQUEST_BODY_PROPERTY, REQUEST_BODY);
        properties.put(Constants.REQUEST_BODY_TRANSFER_ENCODING_PROPERTY, Constants.TRANSFER_ENCODING_BASE64);

        Map<String, Object> custom = collect(properties, Collections.emptyMap(), new HashMap<>());

        Assert.assertFalse("body capture is opt-in; a stashed body must stay unpublished",
                custom.containsKey(Constants.REQUEST_BODY));
        Assert.assertFalse(custom.containsKey(Constants.REQUEST_BODY_TRANSFER_ENCODING));
        Assert.assertFalse(custom.containsKey(Constants.RESPONSE_BODY));
    }

    // ----- response header sanitising -----

    /**
     * Uses the real {@link AnalyticsPayloadUtil#isSensitiveHeader} rather than a PowerMock stub: the production
     * call site is a method reference, which PowerMock does not reliably intercept. Body capture stays off here
     * because {@code shouldSendPayloads()} needs analytics to be enabled, which it is not in a unit test.
     */
    @Test
    public void testGetPropertiesStripsSensitiveResponseHeaders() throws Exception {
        configureHeaders(true);

        Map<String, Object> transportHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        transportHeaders.put(APIConstants.AUTHORIZATION_HEADER_DEFAULT, "Bearer secret");
        transportHeaders.put(APIConstants.API_KEY_HEADER_DEFAULT, "key-secret");
        transportHeaders.put(APIConstants.COOKIE, "session=abc");
        transportHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json");

        Map<String, Object> custom = collect(new HashMap<>(), transportHeaders, new HashMap<>());

        Map<?, ?> published = (Map<?, ?>) custom.get(Constants.RESPONSE_HEADERS);
        Assert.assertNotNull(published);
        Assert.assertFalse("Authorization must never reach analytics",
                published.containsKey(APIConstants.AUTHORIZATION_HEADER_DEFAULT));
        Assert.assertFalse(published.containsKey(APIConstants.API_KEY_HEADER_DEFAULT));
        Assert.assertFalse(published.containsKey(APIConstants.COOKIE));
        Assert.assertTrue("ordinary headers must still be published",
                published.containsKey(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void testGetPropertiesLeavesLiveTransportHeaderMapUntouched() throws Exception {
        configureHeaders(true);

        Map<String, Object> transportHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        transportHeaders.put(APIConstants.AUTHORIZATION_HEADER_DEFAULT, "Bearer secret");
        transportHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json");

        collect(new HashMap<>(), transportHeaders, new HashMap<>());

        Assert.assertTrue("the headers are copied before stripping; the live map must not be mutated",
                transportHeaders.containsKey(APIConstants.AUTHORIZATION_HEADER_DEFAULT));
    }

    // ----- helpers -----

    /** Stubs the static config seam so body capture can be switched on without a real analytics configuration. */
    private static void mockPayloadUtil(boolean sendPayloads) {
        PowerMockito.mockStatic(AnalyticsPayloadUtil.class);
        PowerMockito.when(AnalyticsPayloadUtil.shouldSendPayloads()).thenReturn(sendPayloads);
        PowerMockito.when(AnalyticsPayloadUtil.getPayloadSizeLimit())
                .thenReturn(Constants.DEFAULT_PAYLOAD_SIZE_LIMIT_BYTES);
    }

    private static void stubExtractedResponseBody(AnalyticsPayloadUtil.CapturedBody captured) {
        PowerMockito.when(AnalyticsPayloadUtil.extractPayload(Mockito.any(org.apache.synapse.MessageContext.class),
                Mockito.anyInt(), Mockito.anyString())).thenReturn(captured);
    }

    private static Map<String, Object> collect(Map<String, Object> properties, Map<String, Object> transportHeaders,
                                               Map<String, Object> analyticsMetadata) throws Exception {
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, transportHeaders, analyticsMetadata);
        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        setBuildResponseMessage(provider, false);
        return provider.getProperties();
    }

    private static Axis2MessageContext mockAxis2MessageContext(Map<String, Object> properties,
                                                               Map<String, Object> transportHeaders,
                                                               Map<String, Object> analyticsMetadata) {
        Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);

        Mockito.when(messageContext.getAxis2MessageContext()).thenReturn(axis2MessageContext);
        Mockito.when(axis2MessageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(messageContext.getPropertyKeySet()).thenAnswer(invocation -> properties.keySet());
        Mockito.when(messageContext.getProperty(Mockito.anyString()))
                .thenAnswer(invocation -> properties.get((String) invocation.getArguments()[0]));
        Mockito.doReturn(analyticsMetadata).when(messageContext).getAnalyticsMetadata();

        return messageContext;
    }

    private static Map<String, String> singleStringProperty(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static void setBuildResponseMessage(SynapseAnalyticsDataProvider provider, boolean value)
            throws Exception {
        Field field = SynapseAnalyticsDataProvider.class.getDeclaredField("buildResponseMessage");
        field.setAccessible(true);
        field.set(provider, value);
    }
}
