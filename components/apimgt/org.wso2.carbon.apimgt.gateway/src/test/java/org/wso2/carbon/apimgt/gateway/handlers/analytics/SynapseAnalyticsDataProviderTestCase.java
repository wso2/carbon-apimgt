/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsCustomDataProvider;
import org.wso2.carbon.apimgt.common.analytics.exceptions.DataNotFoundException;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.EventCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.mcp.request.Params;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SynapseAnalyticsDataProviderTestCase {

    @Before
    public void resetStaticState() throws Exception {
        Field field = SynapseAnalyticsDataProvider.class.getDeclaredField("reporterProperties");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void testMetricsWhenMessageContextPropertiesAreNull() {
        Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(messageContext.getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(Collections.emptyMap());
        Mockito.when(messageContext.getPropertyKeySet()).thenReturn(Collections.emptySet());

        AnalyticsCustomDataProvider analyticsCustomDataProvider = Mockito.mock(AnalyticsCustomDataProvider.class);
        SynapseAnalyticsDataProvider synapseAnalyticsDataProvider = new SynapseAnalyticsDataProvider(messageContext,
                analyticsCustomDataProvider);

        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS)).thenReturn(null);
        Mockito.when(messageContext.getProperty(SynapseConstants.HTTP_SC)).thenReturn(null);
        Mockito.when(messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY)).thenReturn(null);
        Mockito.when(messageContext.getProperty(Constants.BACKEND_END_TIME_PROPERTY)).thenReturn(null);

        Assert.assertEquals(APIMgtGatewayConstants.DUMMY_ENDPOINT_ADDRESS,
                synapseAnalyticsDataProvider.getTarget().getDestination());
        Assert.assertEquals(200, synapseAnalyticsDataProvider.getProxyResponseCode());
        Assert.assertEquals(0, synapseAnalyticsDataProvider.getBackendLatency());
        Assert.assertEquals(0, synapseAnalyticsDataProvider.getResponseMediationLatency());
    }

    /**
     * Verifies that error code 900515 (exclusive upper bound of the guardrail range) is NOT classified
     * as GUARDRAIL_FAULT, confirming the range check is [900514, 900515).
     */
    @Test
    public void testGetFaultTypeForErrorCodeAtGuardrailRangeUpperBoundIsNotGuardrailFault() {
        // GUARDRAIL_FAILURE__END = 900515 is exclusive; this code must fall through to OTHER
        Axis2MessageContext messageContext = mockAxis2MessageContext(singleProperty(
                SynapseConstants.ERROR_CODE, Constants.ERROR_CODE_RANGES.GUARDRAIL_FAILURE__END),
                Collections.emptyMap());

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertNotEquals(FaultCategory.GUARDRAIL_FAULT, provider.getFaultType());
    }

    /**
     * Verifies guardrail error codes are classified under the GUARDRAIL_FAULT category.
     */
    @Test
    public void testGetFaultTypeForGuardrailFailure() {
        Axis2MessageContext messageContext = mockAxis2MessageContext(singleProperty(
                SynapseConstants.ERROR_CODE, Constants.GUARDRAIL_ERROR_CODE), Collections.emptyMap());

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertEquals(FaultCategory.GUARDRAIL_FAULT, provider.getFaultType());
    }

    /**
     * Verifies MCP requests without an authentication context use the anonymous application object.
     */
    @Test
    public void testGetApplicationReturnsAnonymousForMcpWithoutAuthContext() throws DataNotFoundException {
        Map<String, Object> properties = singleProperty(Constants.RESOURCE_PATH, APIMgtGatewayConstants.MCP_RESOURCE + "/tools");
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        Application application = provider.getApplication();

        Assert.assertEquals(Constants.ANONYMOUS_VALUE, application.getApplicationId());
        Assert.assertEquals(Constants.ANONYMOUS_VALUE, application.getApplicationName());
        Assert.assertEquals(Constants.ANONYMOUS_VALUE, application.getApplicationOwner());
        Assert.assertEquals(Constants.ANONYMOUS_VALUE, application.getKeyType());
    }

    /**
     * Verifies non-MCP requests without an authentication context still fail with DataNotFoundException.
     */
    @Test(expected = DataNotFoundException.class)
    public void testGetApplicationThrowsForNonMcpWithoutAuthContext() throws DataNotFoundException {
        Map<String, Object> properties = singleProperty(Constants.RESOURCE_PATH, "/weather/v1/forecast");
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        provider.getApplication();
    }

    /**
     * Verifies invalid MCP request size values fall back to the Content-Length header.
     */
    @Test
    public void testGetRequestSizeFallsBackToContentLengthForInvalidMcpRequestSize() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.API_TYPE, APIConstants.AI.MCP);
        properties.put(APIMgtGatewayConstants.MCP_REQUEST_SIZE_KEY, "not-a-number");

        Map<String, Object> headers = singleProperty(HttpHeaders.CONTENT_LENGTH, "512");
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, headers);

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertEquals(512L, provider.getRequestSize());
    }

    /**
     * Verifies non-MCP requests derive request size from the Content-Length header.
     */
    @Test
    public void testGetRequestSizeForNonMcpUsesContentLengthHeader() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.API_TYPE, "REST");

        Map<String, Object> headers = singleProperty(HttpHeaders.CONTENT_LENGTH, "777");
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, headers);

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertEquals(777L, provider.getRequestSize());
    }

    /**
     * Verifies guardrail and MCP analytics fields are emitted in custom properties for MCP guardrail failures.
     */
    @Test
    public void testGetPropertiesIncludesGuardrailAndMcpAnalyticsData() throws Exception {
        setReporterProperties(singleProperty(Constants.SEND_HEADER, "false"));

        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.API_TYPE, APIConstants.AI.MCP);
        properties.put(SynapseConstants.ERROR_CODE, Constants.GUARDRAIL_ERROR_CODE);
        properties.put(SynapseConstants.ERROR_MESSAGE,
                "{\"interveningGuardrail\":\"sensitive-content\",\"direction\":\"REQUEST\"}");
        properties.put(APIMgtGatewayConstants.MCP_REQUEST_SIZE_KEY, "128");
        properties.put(APIMgtGatewayConstants.MCP_SESSION_ID_KEY, "session-1");
        properties.put(APIMgtGatewayConstants.MCP_METHOD, "tools/call");
        properties.put(APIMgtGatewayConstants.MCP_HTTP_METHOD_KEY, "POST");
        properties.put(APIMgtGatewayConstants.MCP_CAPABILITY_NAME_KEY, "viewPizzaMenu");
        properties.put(APIMgtGatewayConstants.MCP_REQUESTED_PROTOCOL_VERSION_KEY, "2025-06-18");
        properties.put(APIMgtGatewayConstants.MCP_PROTOCOL_VERSION_KEY, "2025-06-18");
        properties.put(APIMgtGatewayConstants.MCP_SERVER_NAME_KEY, "PizzaMCP");
        properties.put(APIMgtGatewayConstants.MCP_SERVER_VERSION_KEY, "1.0");
        properties.put(APIMgtGatewayConstants.MCP_IS_ERROR_KEY, true);
        properties.put(APIMgtGatewayConstants.MCP_ERROR_CODE_KEY, -32600);

        Params.ClientInfo clientInfo = new Params.ClientInfo();
        clientInfo.setName("mcp-playground");
        clientInfo.setVersion("0.13.0");
        properties.put(APIMgtGatewayConstants.MCP_CLIENT_INFO_KEY, clientInfo);

        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put(HttpHeaders.CONTENT_LENGTH, "64");
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");

        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, headers);
        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        setBuildResponseMessage(provider, false);

        Map<String, Object> customProperties = provider.getProperties();

        Assert.assertEquals(128L, customProperties.get(Constants.REQUEST_SIZE));
        Assert.assertEquals(Boolean.TRUE, customProperties.get(Constants.IS_GUARDRAIL_HIT));
        Assert.assertEquals("sensitive-content", customProperties.get(Constants.GUARDRAIL_NAME));

        Map<String, Object> mcpAnalytics = (Map<String, Object>) customProperties.get(APIMgtGatewayConstants.MCP_ANALYTICS);
        Assert.assertNotNull(mcpAnalytics);
        Assert.assertEquals("session-1", mcpAnalytics.get(APIMgtGatewayConstants.MCP_SESSION_ID));
        Assert.assertEquals("tools/call", mcpAnalytics.get(Constants.MCP_METHOD));
        Assert.assertEquals(APIMgtGatewayConstants.TOOL, mcpAnalytics.get(APIMgtGatewayConstants.MCP_CAPABILITY));
        Assert.assertEquals("viewPizzaMenu", mcpAnalytics.get(APIMgtGatewayConstants.MCP_CAPABILITY_NAME));
        Assert.assertEquals(Boolean.TRUE, mcpAnalytics.get(APIMgtGatewayConstants.MCP_IS_ERROR));
        Assert.assertEquals(-32600, mcpAnalytics.get(APIMgtGatewayConstants.MCP_ERROR_CODE));

        Map<String, Object> clientInfoMap = (Map<String, Object>) mcpAnalytics.get(APIMgtGatewayConstants.MCP_CLIENT_INFO);
        Assert.assertNotNull(clientInfoMap);
        Assert.assertEquals("2025-06-18", clientInfoMap.get(APIMgtGatewayConstants.MCP_REQUESTED_PROTOCOL_VERSION));
        Assert.assertEquals("mcp-playground", clientInfoMap.get(APIMgtGatewayConstants.MCP_CLIENT_NAME));
        Assert.assertEquals("0.13.0", clientInfoMap.get(APIMgtGatewayConstants.MCP_CLIENT_VERSION));

        Map<String, Object> serverInfo = (Map<String, Object>) mcpAnalytics.get(APIMgtGatewayConstants.MCP_SERVER_INFO);
        Assert.assertNotNull(serverInfo);
        Assert.assertEquals("2025-06-18", serverInfo.get(APIMgtGatewayConstants.MCP_PROTOCOL_VERSION));
        Assert.assertEquals("PizzaMCP", serverInfo.get(APIMgtGatewayConstants.MCP_SERVER_NAME));
        Assert.assertEquals("1.0", serverInfo.get(APIMgtGatewayConstants.MCP_SERVER_VERSION));
    }

    /**
     * Verifies getEventCategory returns SUCCESS for an MCP resource path with no error code.
     */
    @Test
    public void testGetEventCategorySuccessForMcpResourceWithNoErrorCode() {
        Map<String, Object> properties = singleProperty(Constants.RESOURCE_PATH,
                APIMgtGatewayConstants.MCP_RESOURCE + "/tools");
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertEquals(EventCategory.SUCCESS, provider.getEventCategory());
    }

    /**
     * Verifies getEventCategory returns FAULT for an MCP resource path when an error code is present.
     */
    @Test
    public void testGetEventCategoryFaultForMcpResourceWithErrorCode() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.RESOURCE_PATH, APIMgtGatewayConstants.MCP_RESOURCE + "/tools");
        properties.put(SynapseConstants.ERROR_CODE, Constants.GUARDRAIL_ERROR_CODE);
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertEquals(EventCategory.FAULT, provider.getEventCategory());
    }

    /**
     * Verifies MCP request size is read directly from the MCP_REQUEST_SIZE_KEY property when valid.
     */
    @Test
    public void testGetRequestSizeForMcpWithValidSize() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.API_TYPE, APIConstants.AI.MCP);
        properties.put(APIMgtGatewayConstants.MCP_REQUEST_SIZE_KEY, "1024");

        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());
        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertEquals(1024L, provider.getRequestSize());
    }

    /**
     * Verifies getRequestSize returns -1 when no Content-Length header is present.
     */
    @Test
    public void testGetRequestSizeReturnsNegativeOneWhenNoContentLengthHeader() {
        Map<String, Object> properties = singleProperty(APIConstants.API_TYPE, "REST");
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertEquals(-1L, provider.getRequestSize());
    }

    /**
     * Verifies getApplication returns anonymous app for MCP requests matched via API_ELECTED_RESOURCE.
     */
    @Test
    public void testGetApplicationForMcpUsingApiElectedResource() throws DataNotFoundException {
        Map<String, Object> properties = singleProperty(
                APIMgtGatewayConstants.API_ELECTED_RESOURCE, APIMgtGatewayConstants.MCP_RESOURCE + "/prompts");
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        Application application = provider.getApplication();

        Assert.assertEquals(Constants.ANONYMOUS_VALUE, application.getApplicationId());
        Assert.assertEquals(Constants.ANONYMOUS_VALUE, application.getApplicationName());
        Assert.assertEquals(Constants.ANONYMOUS_VALUE, application.getApplicationOwner());
        Assert.assertEquals(Constants.ANONYMOUS_VALUE, application.getKeyType());
    }

    /**
     * Verifies IS_GUARDRAIL_HIT is false for an MCP request that does not trigger a guardrail.
     */
    @Test
    public void testGetPropertiesIsGuardrailHitFalseForNonGuardrailMcpRequest() throws Exception {
        setReporterProperties(singleProperty(Constants.SEND_HEADER, "false"));

        Map<String, Object> properties = singleProperty(APIConstants.API_TYPE, APIConstants.AI.MCP);
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());
        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        setBuildResponseMessage(provider, false);

        Map<String, Object> customProperties = provider.getProperties();

        Assert.assertEquals(Boolean.FALSE, customProperties.get(Constants.IS_GUARDRAIL_HIT));
        Assert.assertFalse(customProperties.containsKey(Constants.GUARDRAIL_NAME));
    }

    /**
     * Verifies MCP_ANALYTICS is absent from getProperties output for non-MCP requests.
     */
    @Test
    public void testGetPropertiesHasNoMcpAnalyticsForNonMcpRequest() throws Exception {
        setReporterProperties(singleProperty(Constants.SEND_HEADER, "false"));

        Map<String, Object> properties = singleProperty(APIConstants.API_TYPE, "REST");
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());
        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        setBuildResponseMessage(provider, false);

        Map<String, Object> customProperties = provider.getProperties();

        Assert.assertNull(customProperties.get(APIMgtGatewayConstants.MCP_ANALYTICS));
    }

    /**
     * Verifies that the capability field is absent from MCP analytics when the method is not tools/call.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesMcpAnalyticsHasNoCapabilityForNonToolsCallMethod() throws Exception {
        setReporterProperties(singleProperty(Constants.SEND_HEADER, "false"));

        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.API_TYPE, APIConstants.AI.MCP);
        properties.put(APIMgtGatewayConstants.MCP_METHOD, "tools/list");

        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());
        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        setBuildResponseMessage(provider, false);

        Map<String, Object> customProperties = provider.getProperties();
        Map<String, Object> mcpAnalytics = (Map<String, Object>) customProperties.get(APIMgtGatewayConstants.MCP_ANALYTICS);

        Assert.assertNotNull(mcpAnalytics);
        Assert.assertEquals("tools/list", mcpAnalytics.get(Constants.MCP_METHOD));
        Assert.assertNull(mcpAnalytics.get(APIMgtGatewayConstants.MCP_CAPABILITY));
    }

    private static Axis2MessageContext mockAxis2MessageContext(Map<String, Object> properties,
                                                               Map<String, Object> transportHeaders) {
        Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);

        Mockito.when(messageContext.getAxis2MessageContext()).thenReturn(axis2MessageContext);
        Mockito.when(axis2MessageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(messageContext.getPropertyKeySet()).thenAnswer(invocation -> properties.keySet());
        Mockito.when(messageContext.getProperty(Mockito.anyString()))
                .thenAnswer(invocation -> properties.get((String) invocation.getArguments()[0]));
        Mockito.doReturn(new HashMap<String, Object>()).when(messageContext).getAnalyticsMetadata();

        return messageContext;
    }

    private static Map<String, Object> singleProperty(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static void setReporterProperties(Map<String, Object> values) throws Exception {
        Field field = SynapseAnalyticsDataProvider.class.getDeclaredField("reporterProperties");
        field.setAccessible(true);

        Map<String, String> reporterProperties = new HashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            reporterProperties.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        field.set(null, reporterProperties);
    }

    private static void setBuildResponseMessage(SynapseAnalyticsDataProvider provider, boolean value) throws Exception {
        Field field = SynapseAnalyticsDataProvider.class.getDeclaredField("buildResponseMessage");
        field.setAccessible(true);
        field.set(provider, value);
    }
}
