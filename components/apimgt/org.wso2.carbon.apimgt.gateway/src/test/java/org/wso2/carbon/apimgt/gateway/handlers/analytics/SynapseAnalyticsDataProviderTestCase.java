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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsCustomDataProvider;
import org.wso2.carbon.apimgt.common.analytics.exceptions.DataNotFoundException;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.EventCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.mcp.request.Params;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SynapseAnalyticsDataProviderTestCase {

    private static final Set<String> CORE_AI_TOKEN_USAGE_KEYS = new HashSet<>(Arrays.asList(
            Constants.AI_PROMPT_TOKEN_USAGE, Constants.AI_COMPLETION_TOKEN_USAGE, Constants.AI_TOTAL_TOKEN_USAGE,
            Constants.HOUR));

    private Map<String, String> previousReporterProperties;

    @Before
    public void resetStaticState() throws Exception {
        Field field = SynapseAnalyticsDataProvider.class.getDeclaredField("reporterProperties");
        field.setAccessible(true);
        previousReporterProperties = (Map<String, String>) field.get(null);
        field.set(null, null);
    }

    @After
    public void restoreStaticState() throws Exception {
        Field field = SynapseAnalyticsDataProvider.class.getDeclaredField("reporterProperties");
        field.setAccessible(true);
        field.set(null, previousReporterProperties);
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

        Assert.assertEquals(FaultCategory.OTHER, provider.getFaultType());
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

        Assert.assertFalse(customProperties.containsKey(APIMgtGatewayConstants.MCP_ANALYTICS));
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
        Assert.assertFalse(mcpAnalytics.containsKey(APIMgtGatewayConstants.MCP_CAPABILITY));
    }

    /**
     * Verifies an AI API event without any PRODUCT_ANALYTICS_PROPS_TO_PUBLISH sections has only the core
     * aiTokenUsage fields and no additionalProperties.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesWithoutProductAnalyticsPropsHasOnlyCoreAiTokenUsage() throws Exception {

        Map<String, Object> customProperties = buildProperties(aiRequestProperties(), null);

        Map<String, Object> aiTokenUsage = (Map<String, Object>) customProperties.get(Constants.AI_TOKEN_USAGE);
        Assert.assertEquals(CORE_AI_TOKEN_USAGE_KEYS, aiTokenUsage.keySet());
        Assert.assertFalse(customProperties.containsKey(Constants.ADDITIONAL_PROPERTIES));
    }

    /**
     * Verifies a PRODUCT_ANALYTICS_PROPS_TO_PUBLISH value that is not a map is ignored without failing the event.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesIgnoresProductAnalyticsPropsThatAreNotAMap() throws Exception {

        for (Object notAMap : Arrays.asList("aiTokenUsage", Collections.singletonList("featureUsage"), 1)) {
            Map<String, Object> properties = aiRequestProperties();
            properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS, notAMap);

            Map<String, Object> customProperties = buildProperties(properties, null);

            Map<String, Object> aiTokenUsage = (Map<String, Object>) customProperties.get(Constants.AI_TOKEN_USAGE);
            Assert.assertEquals(CORE_AI_TOKEN_USAGE_KEYS, aiTokenUsage.keySet());
            Assert.assertFalse(customProperties.containsKey(Constants.ADDITIONAL_PROPERTIES));
        }
    }

    /**
     * Verifies the counters of the aiTokenUsage section are added to the event's aiTokenUsage, next to the core
     * fields.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesAddsProductTokenUsageCounters() throws Exception {

        Map<String, Object> counters = new LinkedHashMap<>();
        counters.put("cacheReadInputTokens", 800);
        counters.put("reasoningTokens", 120);
        Map<String, Object> properties = aiRequestProperties();
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS,
                Collections.singletonMap(Constants.AI_TOKEN_USAGE, counters));

        Map<String, Object> customProperties = buildProperties(properties, null);

        Map<String, Object> aiTokenUsage = (Map<String, Object>) customProperties.get(Constants.AI_TOKEN_USAGE);
        Assert.assertEquals(800, aiTokenUsage.get("cacheReadInputTokens"));
        Assert.assertEquals(120, aiTokenUsage.get("reasoningTokens"));
        Assert.assertEquals(1200, aiTokenUsage.get(Constants.AI_PROMPT_TOKEN_USAGE));
        Assert.assertFalse(customProperties.containsKey(Constants.ADDITIONAL_PROPERTIES));
    }

    /**
     * Verifies the aiTokenUsage section never overwrites a field the event's aiTokenUsage already has, such as a
     * core token count.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesProductTokenUsageDoesNotOverwriteExistingFields() throws Exception {

        Map<String, Object> counters = new LinkedHashMap<>();
        counters.put(Constants.AI_PROMPT_TOKEN_USAGE, 1);
        counters.put(Constants.AI_COMPLETION_TOKEN_USAGE, 1);
        counters.put(Constants.AI_TOTAL_TOKEN_USAGE, 1);
        counters.put(Constants.HOUR, 99);
        counters.put("cacheReadInputTokens", 800);
        Map<String, Object> properties = aiRequestProperties();
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS,
                Collections.singletonMap(Constants.AI_TOKEN_USAGE, counters));

        Map<String, Object> aiTokenUsage =
                (Map<String, Object>) buildProperties(properties, null).get(Constants.AI_TOKEN_USAGE);

        Assert.assertEquals(1200, aiTokenUsage.get(Constants.AI_PROMPT_TOKEN_USAGE));
        Assert.assertEquals(300, aiTokenUsage.get(Constants.AI_COMPLETION_TOKEN_USAGE));
        Assert.assertEquals(1500, aiTokenUsage.get(Constants.AI_TOTAL_TOKEN_USAGE));
        Assert.assertNotEquals(99, aiTokenUsage.get(Constants.HOUR));
        Assert.assertEquals(800, aiTokenUsage.get("cacheReadInputTokens"));
    }

    /**
     * Verifies an aiTokenUsage section that is not a map is ignored, and is not published under
     * additionalProperties either.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesIgnoresProductTokenUsageSectionThatIsNotAMap() throws Exception {

        for (Object notAMap : Arrays.asList("800", Collections.singletonList(800), 800)) {
            Map<String, Object> properties = aiRequestProperties();
            properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS,
                    Collections.singletonMap(Constants.AI_TOKEN_USAGE, notAMap));

            Map<String, Object> customProperties = buildProperties(properties, null);

            Map<String, Object> aiTokenUsage = (Map<String, Object>) customProperties.get(Constants.AI_TOKEN_USAGE);
            Assert.assertEquals(CORE_AI_TOKEN_USAGE_KEYS, aiTokenUsage.keySet());
            Assert.assertFalse(customProperties.containsKey(Constants.ADDITIONAL_PROPERTIES));
        }
    }

    /**
     * Verifies keys of the aiTokenUsage section that are not strings are skipped while the other counters are added.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesSkipsProductTokenUsageCountersWithNonStringKeys() throws Exception {

        Map<Object, Object> counters = new LinkedHashMap<>();
        counters.put(1, 100);
        counters.put("cacheReadInputTokens", 800);
        Map<String, Object> properties = aiRequestProperties();
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS,
                Collections.singletonMap(Constants.AI_TOKEN_USAGE, counters));

        Map<String, Object> aiTokenUsage =
                (Map<String, Object>) buildProperties(properties, null).get(Constants.AI_TOKEN_USAGE);

        Assert.assertEquals(800, aiTokenUsage.get("cacheReadInputTokens"));
        Assert.assertFalse(aiTokenUsage.containsKey(1));
    }

    /**
     * Verifies the aiTokenUsage section does not create an aiTokenUsage for a request that is not an AI API request
     * (it would lack the core fields), nor end up under additionalProperties.
     */
    @Test
    public void testGetPropertiesIgnoresProductTokenUsageForNonAiRequest() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.API_TYPE, "REST");
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS,
                Collections.singletonMap(Constants.AI_TOKEN_USAGE,
                        Collections.singletonMap("cacheReadInputTokens", 800)));

        Map<String, Object> customProperties = buildProperties(properties, null);

        Assert.assertFalse(customProperties.containsKey(Constants.AI_TOKEN_USAGE));
        Assert.assertFalse(customProperties.containsKey(Constants.ADDITIONAL_PROPERTIES));
    }

    /**
     * Verifies every section other than aiTokenUsage is published as is, in order, under a single
     * additionalProperties object, and not at the root of the event under its own name.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesPublishesOtherProductSectionsUnderAdditionalProperties() throws Exception {

        Map<String, Object> sections = new LinkedHashMap<>();
        sections.put("featureUsage", Collections.singletonMap("requests", 1));
        sections.put(Constants.AI_TOKEN_USAGE, Collections.singletonMap("cacheReadInputTokens", 800));
        sections.put("featureFlags", Collections.singletonList("beta"));
        sections.put("featureMode", "strict");
        Map<String, Object> properties = aiRequestProperties();
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS, sections);

        Map<String, Object> customProperties = buildProperties(properties, null);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("featureUsage", Collections.singletonMap("requests", 1));
        expected.put("featureFlags", Collections.singletonList("beta"));
        expected.put("featureMode", "strict");
        Map<String, Object> additionalProperties =
                (Map<String, Object>) customProperties.get(Constants.ADDITIONAL_PROPERTIES);
        Assert.assertEquals(expected, additionalProperties);
        Assert.assertEquals(Arrays.asList("featureUsage", "featureFlags", "featureMode"),
                new ArrayList<>(additionalProperties.keySet()));
        Assert.assertFalse(customProperties.containsKey("featureUsage"));
        Assert.assertEquals(800,
                ((Map<String, Object>) customProperties.get(Constants.AI_TOKEN_USAGE)).get("cacheReadInputTokens"));
    }

    /**
     * Verifies sections are published under additionalProperties for a request that is not an AI API request.
     */
    @Test
    public void testGetPropertiesPublishesOtherProductSectionsForNonAiRequest() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.API_TYPE, "REST");
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS,
                Collections.singletonMap("featureUsage", Collections.singletonMap("requests", 1)));

        Map<String, Object> customProperties = buildProperties(properties, null);

        Assert.assertEquals(Collections.singletonMap("featureUsage", Collections.singletonMap("requests", 1)),
                customProperties.get(Constants.ADDITIONAL_PROPERTIES));
    }

    /**
     * Verifies sections with a null value or a key that is not a string are skipped, and that no
     * additionalProperties object is added when nothing is left to publish.
     */
    @Test
    public void testGetPropertiesSkipsProductSectionsWithNullValueOrNonStringKey() throws Exception {

        Map<Object, Object> sections = new LinkedHashMap<>();
        sections.put("nullSection", null);
        sections.put(1, Collections.singletonMap("requests", 1));
        sections.put(Constants.AI_TOKEN_USAGE, null);
        Map<String, Object> properties = aiRequestProperties();
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS, sections);

        Map<String, Object> customProperties = buildProperties(properties, null);

        Assert.assertFalse(customProperties.containsKey(Constants.ADDITIONAL_PROPERTIES));
        Assert.assertFalse(customProperties.containsKey("nullSection"));
    }

    /**
     * Verifies an additionalProperties value already set by a custom analytics data provider is kept as it is.
     */
    @Test
    public void testGetPropertiesKeepsAdditionalPropertiesSetByCustomDataProvider() throws Exception {

        Object customValue = Collections.singletonMap("tenantTier", "gold");
        AnalyticsCustomDataProvider customDataProvider = Mockito.mock(AnalyticsCustomDataProvider.class);
        Mockito.when(customDataProvider.getCustomProperties(Mockito.any()))
                .thenReturn(new HashMap<>(Collections.singletonMap(Constants.ADDITIONAL_PROPERTIES, customValue)));
        Map<String, Object> properties = aiRequestProperties();
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS,
                Collections.singletonMap("featureUsage", Collections.singletonMap("requests", 1)));

        Map<String, Object> customProperties = buildProperties(properties, customDataProvider);

        Assert.assertSame(customValue, customProperties.get(Constants.ADDITIONAL_PROPERTIES));
    }

    /**
     * Verifies building the event neither fails on nor changes an unmodifiable PRODUCT_ANALYTICS_PROPS_TO_PUBLISH
     * map, and publishes copies rather than the message context's own maps.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesDoesNotChangeProductAnalyticsProps() throws Exception {

        Map<String, Object> sections = new LinkedHashMap<>();
        sections.put(Constants.AI_TOKEN_USAGE,
                Collections.unmodifiableMap(Collections.singletonMap("cacheReadInputTokens", 800)));
        sections.put("featureUsage", Collections.singletonMap("requests", 1));
        Map<String, Object> productProps = Collections.unmodifiableMap(sections);
        Map<String, Object> snapshot = new LinkedHashMap<>(productProps);
        Map<String, Object> properties = aiRequestProperties();
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS, productProps);

        Map<String, Object> customProperties = buildProperties(properties, null);
        ((Map<String, Object>) customProperties.get(Constants.ADDITIONAL_PROPERTIES)).put("changedLater", true);

        Assert.assertEquals(snapshot, properties.get(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS));
        Assert.assertNotSame(productProps, customProperties.get(Constants.ADDITIONAL_PROPERTIES));
        Assert.assertNotSame(productProps.get(Constants.AI_TOKEN_USAGE),
                customProperties.get(Constants.AI_TOKEN_USAGE));
    }

    /**
     * Verifies extension listener data in ADDITIONAL_ANALYTICS_PROPS_TO_PUBLISH is not published, even when it uses
     * the same names as product sections.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertiesDoesNotPublishListenerAnalyticsProps() throws Exception {

        Map<String, Object> listenerProps = new LinkedHashMap<>();
        listenerProps.put("salesRegion", "EMEA");
        listenerProps.put(Constants.AI_TOKEN_USAGE, Collections.singletonMap("cacheReadInputTokens", 1));
        listenerProps.put(Constants.ADDITIONAL_PROPERTIES, "listener-value");
        Map<String, Object> properties = aiRequestProperties();
        properties.put(APIMgtGatewayConstants.ADDITIONAL_ANALYTICS_PROPS, listenerProps);

        Map<String, Object> customProperties = buildProperties(properties, null);

        Assert.assertFalse(customProperties.containsKey("salesRegion"));
        Assert.assertFalse(customProperties.containsKey(Constants.ADDITIONAL_PROPERTIES));
        Map<String, Object> aiTokenUsage = (Map<String, Object>) customProperties.get(Constants.AI_TOKEN_USAGE);
        Assert.assertEquals(CORE_AI_TOKEN_USAGE_KEYS, aiTokenUsage.keySet());
    }

    /**
     * Message context properties of an AI API request whose response reported 1200 prompt, 300 completion and 1500
     * total tokens.
     */
    private static Map<String, Object> aiRequestProperties() {

        Map<String, Object> aiResponseMetadata = new HashMap<>();
        aiResponseMetadata.put(AIAPIConstants.NAME, "OpenAI");
        aiResponseMetadata.put(AIAPIConstants.API_VERSION, "2.0.0");
        aiResponseMetadata.put(AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_PROMPT_TOKEN_COUNT, "1200");
        aiResponseMetadata.put(AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT, "300");
        aiResponseMetadata.put(AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT, "1500");
        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.API_TYPE, "REST");
        properties.put(AIAPIConstants.AI_API_RESPONSE_METADATA, aiResponseMetadata);
        return properties;
    }

    private static Map<String, Object> buildProperties(Map<String, Object> properties,
                                                       AnalyticsCustomDataProvider customDataProvider)
            throws Exception {

        setReporterProperties(singleProperty(Constants.SEND_HEADER, "false"));
        Axis2MessageContext messageContext = mockAxis2MessageContext(properties, Collections.emptyMap());
        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext, customDataProvider);
        setBuildResponseMessage(provider, false);
        return provider.getProperties();
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

    /**
     * Client Credentials grant: username is "anonymous" but applicationUUID is populated because
     * subscription validation succeeded. isAnonymous() must return false so that real app fields
     * are not overwritten with "anonymous" in analytics.
     */
    @Test
    public void testIsAnonymousReturnsFalseForClientCredentialsGrantWithPopulatedApplicationUUID() {
        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setUsername(APIConstants.END_USER_ANONYMOUS);
        authContext.setApplicationUUID("550e8400-e29b-41d4-a716-446655440000");

        Axis2MessageContext messageContext = mockAxis2MessageContext(Collections.emptyMap(), Collections.emptyMap());
        Mockito.when(messageContext.getProperty(APISecurityUtils.API_AUTH_CONTEXT)).thenReturn(authContext);

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertFalse(provider.isAnonymous());
    }

    /**
     * Open/unsecured API: username is "anonymous" and applicationUUID is null because
     * APIAuthenticationHandler never sets it. isAnonymous() must return true so that
     * analytics correctly reports "anonymous".
     */
    @Test
    public void testIsAnonymousReturnsTrueForOpenApiWithNullApplicationUUID() {
        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setUsername(APIConstants.END_USER_ANONYMOUS);
        authContext.setApplicationUUID(null);

        Axis2MessageContext messageContext = mockAxis2MessageContext(Collections.emptyMap(), Collections.emptyMap());
        Mockito.when(messageContext.getProperty(APISecurityUtils.API_AUTH_CONTEXT)).thenReturn(authContext);

        SynapseAnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);

        Assert.assertTrue(provider.isAnonymous());
    }
}
