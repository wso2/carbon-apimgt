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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

/**
 * Tests for {@link AnalyticsPayloadUtil} — the opt-in request/response body capture used by the Moesif
 * analytics integration, plus the shared sensitive-header predicate.
 *
 * <p>Body capture must never disrupt the proxied call, so most of the surface here is about deciding
 * <em>not</em> to capture: entity-less messages, excluded content types, content types with no registered
 * message builder, and bodies that cannot be size-bounded before building.</p>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIManagerConfiguration.class, APIUtil.class, RelayUtils.class, JsonUtil.class,
        AnalyticsPayloadUtil.class})
public class AnalyticsPayloadUtilTestCase {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String JSON_BODY = "{\"a\":1}";
    private static final String TEXT_BODY = "hello world";

    private boolean previousInvalidLimitWarned;

    /**
     * {@code getPayloadSizeLimit} logs its warning at most once by flipping a static flag, which would
     * otherwise make these tests order-dependent. Snapshot and reset it around every test, mirroring the
     * static-state fixture in {@link SynapseAnalyticsDataProviderTestCase}.
     */
    @Before
    public void resetStaticState() throws Exception {
        previousInvalidLimitWarned = (boolean) invalidLimitWarnedField().get(null);
        invalidLimitWarnedField().set(null, false);
    }

    @After
    public void restoreStaticState() throws Exception {
        invalidLimitWarnedField().set(null, previousInvalidLimitWarned);
    }

    // ----- isSensitiveHeader -----

    @Test
    public void testIsSensitiveHeaderMatchesCredentialHeaders() {
        Assert.assertTrue(AnalyticsPayloadUtil.isSensitiveHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT));
        Assert.assertTrue(AnalyticsPayloadUtil.isSensitiveHeader(APIConstants.API_KEY_HEADER_DEFAULT));
    }

    @Test
    public void testIsSensitiveHeaderMatchesSessionHeaders() {
        Assert.assertTrue(AnalyticsPayloadUtil.isSensitiveHeader(APIConstants.COOKIE));
        Assert.assertTrue(AnalyticsPayloadUtil.isSensitiveHeader("Set-Cookie"));
    }

    @Test
    public void testIsSensitiveHeaderIsCaseInsensitive() {
        // HTTP/2 delivers header names in lower case, so a case-sensitive match would leak credentials.
        Assert.assertTrue(AnalyticsPayloadUtil.isSensitiveHeader("authorization"));
        Assert.assertTrue(AnalyticsPayloadUtil.isSensitiveHeader("AUTHORIZATION"));
        Assert.assertTrue(AnalyticsPayloadUtil.isSensitiveHeader("set-cookie"));
        Assert.assertTrue(AnalyticsPayloadUtil.isSensitiveHeader("cookie"));
    }

    @Test
    public void testIsSensitiveHeaderAllowsOrdinaryHeaders() {
        Assert.assertFalse(AnalyticsPayloadUtil.isSensitiveHeader(HttpHeaders.CONTENT_TYPE));
        Assert.assertFalse(AnalyticsPayloadUtil.isSensitiveHeader("user-agent"));
        Assert.assertFalse(AnalyticsPayloadUtil.isSensitiveHeader("X-Custom"));
    }

    @Test
    public void testIsSensitiveHeaderHandlesNull() {
        Assert.assertFalse(AnalyticsPayloadUtil.isSensitiveHeader(null));
    }

    // ----- CapturedBody -----

    @Test
    public void testCapturedBodyExposesBodyAndEncoding() {
        AnalyticsPayloadUtil.CapturedBody captured =
                new AnalyticsPayloadUtil.CapturedBody(JSON_BODY, Constants.TRANSFER_ENCODING_BASE64);

        Assert.assertEquals(JSON_BODY, captured.getBody());
        Assert.assertEquals(Constants.TRANSFER_ENCODING_BASE64, captured.getTransferEncoding());
    }

    // ----- shouldSendPayloads -----

    @Test
    public void testShouldSendPayloadsFalseWhenAnalyticsDisabled() {
        mockAnalyticsEnabled(false);
        mockAnalyticsProperties(singleProperty(Constants.SEND_PAYLOAD, "true"));

        Assert.assertFalse("capture must not run when analytics is off; nothing would be published",
                AnalyticsPayloadUtil.shouldSendPayloads());
    }

    @Test
    public void testShouldSendPayloadsFalseWhenPropertiesNull() {
        mockAnalyticsEnabled(true);
        mockAnalyticsProperties(null);

        Assert.assertFalse(AnalyticsPayloadUtil.shouldSendPayloads());
    }

    @Test
    public void testShouldSendPayloadsFalseWhenPropertyAbsent() {
        mockAnalyticsEnabled(true);
        mockAnalyticsProperties(new HashMap<>());

        Assert.assertFalse("body capture is opt-in and must default to off",
                AnalyticsPayloadUtil.shouldSendPayloads());
    }

    @Test
    public void testShouldSendPayloadsFalseWhenPropertyFalse() {
        mockAnalyticsEnabled(true);
        mockAnalyticsProperties(singleProperty(Constants.SEND_PAYLOAD, "false"));

        Assert.assertFalse(AnalyticsPayloadUtil.shouldSendPayloads());
    }

    @Test
    public void testShouldSendPayloadsTrueWhenPropertyTrue() {
        mockAnalyticsEnabled(true);
        mockAnalyticsProperties(singleProperty(Constants.SEND_PAYLOAD, "true"));

        Assert.assertTrue(AnalyticsPayloadUtil.shouldSendPayloads());
    }

    // ----- getPayloadSizeLimit -----

    @Test
    public void testGetPayloadSizeLimitDefaultsWhenPropertiesNull() {
        mockAnalyticsProperties(null);

        Assert.assertEquals(Constants.DEFAULT_PAYLOAD_SIZE_LIMIT_BYTES, AnalyticsPayloadUtil.getPayloadSizeLimit());
    }

    @Test
    public void testGetPayloadSizeLimitDefaultsWhenPropertyAbsent() {
        mockAnalyticsProperties(new HashMap<>());

        Assert.assertEquals(Constants.DEFAULT_PAYLOAD_SIZE_LIMIT_BYTES, AnalyticsPayloadUtil.getPayloadSizeLimit());
    }

    @Test
    public void testGetPayloadSizeLimitReadsConfiguredValue() {
        mockAnalyticsProperties(singleProperty(Constants.PAYLOAD_SIZE_LIMIT, "2048"));

        Assert.assertEquals(2048, AnalyticsPayloadUtil.getPayloadSizeLimit());
    }

    @Test
    public void testGetPayloadSizeLimitDefaultsWhenZero() {
        // A zero limit would silently drop every body, so it is treated as misconfiguration.
        mockAnalyticsProperties(singleProperty(Constants.PAYLOAD_SIZE_LIMIT, "0"));

        Assert.assertEquals(Constants.DEFAULT_PAYLOAD_SIZE_LIMIT_BYTES, AnalyticsPayloadUtil.getPayloadSizeLimit());
    }

    @Test
    public void testGetPayloadSizeLimitDefaultsWhenNegative() {
        mockAnalyticsProperties(singleProperty(Constants.PAYLOAD_SIZE_LIMIT, "-1"));

        Assert.assertEquals(Constants.DEFAULT_PAYLOAD_SIZE_LIMIT_BYTES, AnalyticsPayloadUtil.getPayloadSizeLimit());
    }

    @Test
    public void testGetPayloadSizeLimitDefaultsWhenNotANumber() {
        mockAnalyticsProperties(singleProperty(Constants.PAYLOAD_SIZE_LIMIT, "not-a-number"));

        Assert.assertEquals(Constants.DEFAULT_PAYLOAD_SIZE_LIMIT_BYTES, AnalyticsPayloadUtil.getPayloadSizeLimit());
    }

    @Test
    public void testGetPayloadSizeLimitRecoversAfterInvalidValue() throws Exception {
        mockAnalyticsProperties(singleProperty(Constants.PAYLOAD_SIZE_LIMIT, "oops"));
        Assert.assertEquals(Constants.DEFAULT_PAYLOAD_SIZE_LIMIT_BYTES, AnalyticsPayloadUtil.getPayloadSizeLimit());
        Assert.assertTrue("an invalid value must arm the warn-once flag",
                (boolean) invalidLimitWarnedField().get(null));

        mockAnalyticsProperties(singleProperty(Constants.PAYLOAD_SIZE_LIMIT, "512"));
        Assert.assertEquals(512, AnalyticsPayloadUtil.getPayloadSizeLimit());
        Assert.assertFalse("a later valid value must re-arm warning for a future misconfiguration",
                (boolean) invalidLimitWarnedField().get(null));
    }

    // ----- shouldCapturePayloadsWithoutContentLength -----

    @Test
    public void testShouldCapturePayloadsWithoutContentLengthDefaultsFalse() {
        mockAnalyticsProperties(new HashMap<>());

        Assert.assertFalse("skipping unbounded bodies keeps the default configuration memory-safe",
                AnalyticsPayloadUtil.shouldCapturePayloadsWithoutContentLength());
    }

    @Test
    public void testShouldCapturePayloadsWithoutContentLengthFalseWhenPropertiesNull() {
        mockAnalyticsProperties(null);

        Assert.assertFalse(AnalyticsPayloadUtil.shouldCapturePayloadsWithoutContentLength());
    }

    @Test
    public void testShouldCapturePayloadsWithoutContentLengthTrueWhenConfigured() {
        mockAnalyticsProperties(singleProperty(Constants.CAPTURE_PAYLOADS_WITHOUT_CONTENT_LENGTH, "true"));

        Assert.assertTrue(AnalyticsPayloadUtil.shouldCapturePayloadsWithoutContentLength());
    }

    // ----- extractPayload: cases that must not capture -----

    @Test
    public void testExtractPayloadSkipsWhenNoEntityBody() throws Exception {
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, "10", JSON_CONTENT_TYPE);
        axis2Of(messageContext).setProperty(PassThroughConstants.NO_ENTITY_BODY, Boolean.TRUE);

        Assert.assertNull("GET/DELETE style messages have nothing to capture",
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request"));
    }

    @Test
    public void testExtractPayloadSkipsEventStreamContentType() throws Exception {
        assertNotCaptured("text/event-stream");
    }

    @Test
    public void testExtractPayloadSkipsMultipartContentType() throws Exception {
        assertNotCaptured("multipart/form-data; boundary=abc");
    }

    @Test
    public void testExtractPayloadSkipsFormUrlEncodedContentType() throws Exception {
        assertNotCaptured("application/x-www-form-urlencoded");
    }

    @Test
    public void testExtractPayloadSkipsWhenNoMessageBuilderRegistered() throws Exception {
        // image/png has no registered builder, so building would fall back to the XML/SOAP builder and
        // fail on binary data — corrupting the body being forwarded. It must be left unbuilt.
        MessageContext messageContext = messageContext("image/png", "10", JSON_CONTENT_TYPE);

        Assert.assertNull(AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request"));
    }

    @Test
    public void testExtractPayloadSkipsWhenContentTypeMissing() throws Exception {
        MessageContext messageContext = messageContext(null, "10", JSON_CONTENT_TYPE);

        Assert.assertNull(AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request"));
    }

    @Test
    public void testExtractPayloadDropsWhenDeclaredLengthExceedsLimit() throws Exception {
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, "5000", JSON_CONTENT_TYPE);
        mockJsonPayload(JSON_BODY);

        Assert.assertNull("an oversized body must be dropped before it is ever built",
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request"));
        PowerMockito.verifyStatic(RelayUtils.class, Mockito.never());
        RelayUtils.buildMessage(axis2Of(messageContext));
    }

    @Test
    public void testExtractPayloadSkipsWhenNoContentLengthAndNotOptedIn() throws Exception {
        mockAnalyticsProperties(new HashMap<>());
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, null, JSON_CONTENT_TYPE);
        mockJsonPayload(JSON_BODY);

        Assert.assertNull("a chunked body cannot be size-bounded, so it is skipped by default",
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request"));
    }

    @Test
    public void testExtractPayloadCapturesWithoutContentLengthWhenOptedIn() throws Exception {
        mockAnalyticsProperties(singleProperty(Constants.CAPTURE_PAYLOADS_WITHOUT_CONTENT_LENGTH, "true"));
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, null, JSON_CONTENT_TYPE);
        mockJsonPayload(JSON_BODY);

        AnalyticsPayloadUtil.CapturedBody captured =
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request");

        Assert.assertNotNull(captured);
        Assert.assertEquals(JSON_BODY, captured.getBody());
    }

    @Test
    public void testExtractPayloadCapturesWithoutContentLengthWhenAlreadyBuilt() throws Exception {
        // The memory was already spent by an upstream mediator, so skipping would drop the body for free.
        mockAnalyticsProperties(new HashMap<>());
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, null, JSON_CONTENT_TYPE);
        axis2Of(messageContext).setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        mockJsonPayload(JSON_BODY);

        AnalyticsPayloadUtil.CapturedBody captured =
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request");

        Assert.assertNotNull(captured);
        Assert.assertEquals(JSON_BODY, captured.getBody());
    }

    // ----- extractPayload: captured payloads by kind -----

    @Test
    public void testExtractPayloadCapturesJsonPayload() throws Exception {
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, "7", JSON_CONTENT_TYPE);
        mockJsonPayload(JSON_BODY);

        AnalyticsPayloadUtil.CapturedBody captured =
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request");

        Assert.assertNotNull(captured);
        Assert.assertEquals(JSON_BODY, captured.getBody());
        Assert.assertNull("JSON is published as-is; the publisher decides how to render it",
                captured.getTransferEncoding());
    }

    @Test
    public void testExtractPayloadDropsOversizedJsonPayload() throws Exception {
        String oversized = "{\"a\":\"" + repeat('x', 200) + "\"}";
        // No declared Content-Length here, so the pre-build gate cannot fire; the post-build byte check must.
        mockAnalyticsProperties(singleProperty(Constants.CAPTURE_PAYLOADS_WITHOUT_CONTENT_LENGTH, "true"));
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, null, JSON_CONTENT_TYPE);
        mockJsonPayload(oversized);

        Assert.assertNull(AnalyticsPayloadUtil.extractPayload(messageContext, 100, "response"));
    }

    @Test
    public void testExtractPayloadCapturesBinaryPayloadAsBase64() throws Exception {
        byte[] raw = new byte[] {0x00, 0x01, 0x02, (byte) 0xFF};
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, "4", JSON_CONTENT_TYPE);
        mockNonJsonPayload();
        axis2Of(messageContext).setEnvelope(
                envelopeWithWrapper(BaseConstants.DEFAULT_BINARY_WRAPPER, Base64.encodeBase64String(raw)));

        AnalyticsPayloadUtil.CapturedBody captured =
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request");

        Assert.assertNotNull(captured);
        Assert.assertArrayEquals(raw, Base64.decodeBase64(captured.getBody()));
        Assert.assertEquals(Constants.TRANSFER_ENCODING_BASE64, captured.getTransferEncoding());
    }

    @Test
    public void testExtractPayloadDropsOversizedBinaryPayload() throws Exception {
        byte[] raw = new byte[256];
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, null, JSON_CONTENT_TYPE);
        mockAnalyticsProperties(singleProperty(Constants.CAPTURE_PAYLOADS_WITHOUT_CONTENT_LENGTH, "true"));
        mockNonJsonPayload();
        axis2Of(messageContext).setEnvelope(
                envelopeWithWrapper(BaseConstants.DEFAULT_BINARY_WRAPPER, Base64.encodeBase64String(raw)));

        Assert.assertNull("the limit is compared against the decoded byte count",
                AnalyticsPayloadUtil.extractPayload(messageContext, 100, "request"));
    }

    @Test
    public void testExtractPayloadCapturesTextPayload() throws Exception {
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, "11", JSON_CONTENT_TYPE);
        mockNonJsonPayload();
        axis2Of(messageContext).setEnvelope(
                envelopeWithWrapper(BaseConstants.DEFAULT_TEXT_WRAPPER, TEXT_BODY));

        AnalyticsPayloadUtil.CapturedBody captured =
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request");

        Assert.assertNotNull(captured);
        Assert.assertEquals(TEXT_BODY, captured.getBody());
        Assert.assertNull(captured.getTransferEncoding());
    }

    @Test
    public void testExtractPayloadCapturesAllXmlChildElements() throws Exception {
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, "40", JSON_CONTENT_TYPE);
        mockNonJsonPayload();

        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(element(factory, "first", "one"));
        envelope.getBody().addChild(element(factory, "second", "two"));
        axis2Of(messageContext).setEnvelope(envelope);

        AnalyticsPayloadUtil.CapturedBody captured =
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request");

        Assert.assertNotNull(captured);
        Assert.assertTrue("a multi-element body must not be truncated to its first element",
                captured.getBody().contains("one") && captured.getBody().contains("two"));
        Assert.assertFalse("the synthetic SOAP Body wrapper must not be serialised",
                captured.getBody().contains("Body"));
    }

    @Test
    public void testExtractPayloadReturnsNullWhenBodyIsEmpty() throws Exception {
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, "0", JSON_CONTENT_TYPE);
        mockNonJsonPayload();
        axis2Of(messageContext).setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());

        Assert.assertNull(AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request"));
    }

    @Test
    public void testExtractPayloadFailsSafeWhenBuildThrows() throws Exception {
        MessageContext messageContext = messageContext(JSON_CONTENT_TYPE, "7", JSON_CONTENT_TYPE);
        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.mockStatic(RelayUtils.class);
        PowerMockito.doThrow(new IllegalStateException("boom")).when(RelayUtils.class);
        RelayUtils.buildMessage(axis2Of(messageContext));

        Assert.assertNull("capture must never propagate a failure into the proxied call",
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request"));
    }

    // ----- helpers -----

    private static Field invalidLimitWarnedField() throws Exception {
        Field field = AnalyticsPayloadUtil.class.getDeclaredField("invalidLimitWarned");
        field.setAccessible(true);
        return field;
    }

    private static void mockAnalyticsEnabled(boolean enabled) {
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(enabled);
    }

    private static void mockAnalyticsProperties(Map<String, String> properties) {
        PowerMockito.mockStatic(APIManagerConfiguration.class);
        PowerMockito.when(APIManagerConfiguration.getAnalyticsProperties()).thenReturn(properties);
    }

    private static Map<String, String> singleProperty(String key, String value) {
        Map<String, String> properties = new HashMap<>();
        properties.put(key, value);
        return properties;
    }

    /**
     * Stubs the JSON detection so {@code extractPayload} takes the JSON branch, and neutralises
     * {@code RelayUtils.buildMessage} (the real one would consume a pass-through pipe that does not exist).
     */
    private static void mockJsonPayload(String payload) {
        PowerMockito.mockStatic(RelayUtils.class);
        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.when(JsonUtil.hasAJsonPayload(Mockito.any(org.apache.axis2.context.MessageContext.class)))
                .thenReturn(true);
        PowerMockito.when(JsonUtil.jsonPayloadToString(Mockito.any(org.apache.axis2.context.MessageContext.class)))
                .thenReturn(payload);
    }

    /** Neutralises the build and makes the JSON check fall through to the SOAP envelope branches. */
    private static void mockNonJsonPayload() {
        PowerMockito.mockStatic(RelayUtils.class);
        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.when(JsonUtil.hasAJsonPayload(Mockito.any(org.apache.axis2.context.MessageContext.class)))
                .thenReturn(false);
    }

    private void assertNotCaptured(String contentType) throws Exception {
        MessageContext messageContext = messageContext(contentType, "10", JSON_CONTENT_TYPE);

        Assert.assertNull("content type '" + contentType + "' must never be captured",
                AnalyticsPayloadUtil.extractPayload(messageContext, 1024, "request"));
    }

    /**
     * Builds a real {@link Axis2MessageContext} rather than a mock, so the message-builder registry that
     * {@code hasRegisteredBuilder} consults is genuinely present: a builder is registered for
     * {@code registeredContentType} and for nothing else.
     *
     * @param contentType           the message's {@code Content-Type} transport header, or {@code null} for none
     * @param contentLength         the {@code Content-Length} transport header, or {@code null} for none
     * @param registeredContentType the single content type that gets a registered message builder
     */
    private static MessageContext messageContext(String contentType, String contentLength,
                                                 String registeredContentType) throws Exception {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        axisConfiguration.addMessageBuilder(registeredContentType, Mockito.mock(Builder.class));
        ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
        SynapseConfiguration synapseConfiguration = new SynapseConfiguration();
        org.apache.axis2.context.MessageContext axis2MessageContext =
                new org.apache.axis2.context.MessageContext();
        axis2MessageContext.setConfigurationContext(configurationContext);

        // Real transport headers are case-insensitive; mirror that so lookups behave as they do in production.
        Map<String, Object> transportHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (contentType != null) {
            transportHeaders.put(HttpHeaders.CONTENT_TYPE, contentType);
        }
        if (contentLength != null) {
            transportHeaders.put(HttpHeaders.CONTENT_LENGTH, contentLength);
        }
        axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, transportHeaders);

        return new Axis2MessageContext(axis2MessageContext, synapseConfiguration,
                new Axis2SynapseEnvironment(configurationContext, synapseConfiguration));
    }

    private static org.apache.axis2.context.MessageContext axis2Of(MessageContext messageContext) {
        return ((Axis2MessageContext) messageContext).getAxis2MessageContext();
    }

    private static SOAPEnvelope envelopeWithWrapper(QName wrapper, String text) {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        OMNamespace namespace = factory.createOMNamespace(wrapper.getNamespaceURI(), wrapper.getPrefix());
        OMElement element = factory.createOMElement(wrapper.getLocalPart(), namespace);
        element.setText(text);
        envelope.getBody().addChild(element);
        return envelope;
    }

    private static OMElement element(SOAPFactory factory, String localName, String text) {
        OMElement element = factory.createOMElement(localName, factory.createOMNamespace("http://test", "t"));
        element.setText(text);
        return element;
    }

    private static String repeat(char character, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(character);
        }
        return builder.toString();
    }
}
