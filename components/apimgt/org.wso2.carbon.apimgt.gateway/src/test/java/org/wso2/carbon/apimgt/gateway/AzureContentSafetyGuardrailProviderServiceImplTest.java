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

package org.wso2.carbon.apimgt.gateway;

import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.GuardrailProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link AzureContentSafetyGuardrailProviderServiceImpl}.
 * <p>
 * Covers init() auth-type branching and callOut() header selection for both
 * API-key and UMI authentication modes.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AzureContentSafetyGuardrailProviderServiceImpl.class, APIUtil.class})
public class AzureContentSafetyGuardrailProviderServiceImplTest {

    private static final String ENDPOINT = "https://my-resource.cognitiveservices.azure.com";
    private static final String API_KEY = "test-content-safety-key-abc";
    private static final String UMI_TOKEN = "umi-bearer-token-xyz";
    private static final String SERVICE_PATH = "contentsafety/text:analyze?api-version=2023-10-01";

    private static final String SUCCESS_RESPONSE =
            "{\"categoriesAnalysis\":[{\"category\":\"Hate\",\"severity\":0}]}";

    private AzureContentSafetyGuardrailProviderServiceImpl service;
    private AzureUmiTokenProvider mockUmiProvider;
    private HttpClient mockHttpClient;
    private CloseableHttpResponse mockHttpResponse;
    private StatusLine mockStatusLine;

    @Before
    public void setUp() throws Exception {
        service = new AzureContentSafetyGuardrailProviderServiceImpl();

        mockUmiProvider = Mockito.mock(AzureUmiTokenProvider.class);
        Mockito.when(mockUmiProvider.getAccessToken()).thenReturn(UMI_TOKEN);
        PowerMockito.whenNew(AzureUmiTokenProvider.class).withNoArguments()
                .thenReturn(mockUmiProvider);

        mockHttpClient = Mockito.mock(HttpClient.class);
        mockHttpResponse = Mockito.mock(CloseableHttpResponse.class);
        mockStatusLine = Mockito.mock(StatusLine.class);

        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.given(APIUtil.getHttpClient(Mockito.anyString())).willReturn(mockHttpClient);
    }

    // -------------------------------------------------------------------------
    // init() — null / missing config
    // -------------------------------------------------------------------------

    @Test(expected = APIManagementException.class)
    public void testInit_nullConfig_throwsException() throws APIManagementException {
        service.init(null);
    }

    @Test(expected = APIManagementException.class)
    public void testInit_missingEndpoint_throwsException() throws APIManagementException {
        Map<String, String> props = new HashMap<>();
        props.put(APIConstants.AI.AUTH_TYPE, APIConstants.AI.AUTH_TYPE_API_KEY);
        props.put(APIConstants.AI.GUARDRAIL_PROVIDER_AZURE_CONTENTSAFETY_KEY, API_KEY);
        service.init(configFrom(props));
    }

    // -------------------------------------------------------------------------
    // init() — API-key path
    // -------------------------------------------------------------------------

    @Test
    public void testInit_apiKeyAuth_storesKeyAndDoesNotCreateUmiProvider()
            throws APIManagementException {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_API_KEY, ENDPOINT, API_KEY, null));

        Mockito.verify(mockUmiProvider, Mockito.never()).init(Mockito.anyMap());
    }

    @Test(expected = APIManagementException.class)
    public void testInit_apiKeyAuth_missingKey_throwsException() throws APIManagementException {
        Map<String, String> props = new HashMap<>();
        props.put(APIConstants.AI.GUARDRAIL_PROVIDER_AZURE_CONTENTSAFETY_ENDPOINT, ENDPOINT);
        props.put(APIConstants.AI.AUTH_TYPE, APIConstants.AI.AUTH_TYPE_API_KEY);
        // key property intentionally omitted
        service.init(configFrom(props));
    }

    // -------------------------------------------------------------------------
    // init() — UMI path
    // -------------------------------------------------------------------------

    @Test
    public void testInit_umiAuth_initialisesUmiProviderWithDefaultScope() throws APIManagementException {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_UMI, ENDPOINT, null, null));

        ArgumentCaptor<Map> scopeCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(mockUmiProvider).init(scopeCaptor.capture());
        Assert.assertEquals(
                "Default scope for Content Safety should be cognitiveservices",
                APIConstants.AI.AZURE_UMI_COGNITIVE_SERVICES_SCOPE,
                scopeCaptor.getValue().get(APIConstants.AI.AZURE_UMI_SCOPE_KEY));
    }

    @Test
    public void testInit_umiAuth_customScopeOverride_passesCustomScope() throws APIManagementException {
        String customScope = "https://custom.scope/.default";
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_UMI, ENDPOINT, null, customScope));

        ArgumentCaptor<Map> scopeCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(mockUmiProvider).init(scopeCaptor.capture());
        Assert.assertEquals(customScope,
                scopeCaptor.getValue().get(APIConstants.AI.AZURE_UMI_SCOPE_KEY));
    }

    // -------------------------------------------------------------------------
    // callOut() — header selection
    // -------------------------------------------------------------------------

    @Test
    public void testCallOut_umiAuth_setsAuthorizationBearerHeader() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_UMI, ENDPOINT, null, null));
        stubHttpResponse(200, SUCCESS_RESPONSE);

        service.callOut(buildCalloutConfig());

        ArgumentCaptor<HttpPost> postCaptor = ArgumentCaptor.forClass(HttpPost.class);
        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.executeHTTPRequestWithRetries(
                postCaptor.capture(), Mockito.eq(mockHttpClient),
                Mockito.anyLong(), Mockito.anyInt(), Mockito.anyDouble());

        String authHeader = postCaptor.getValue()
                .getFirstHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT).getValue();
        Assert.assertEquals(APIConstants.AUTHORIZATION_BEARER + UMI_TOKEN, authHeader);

        // Subscription key header must NOT be present when UMI is active
        Assert.assertNull(postCaptor.getValue()
                .getFirstHeader(APIConstants.AI.AZURE_OCP_APIM_SUBSCRIPTION_KEY_HEADER));
    }

    @Test
    public void testCallOut_apiKeyAuth_setsSubscriptionKeyHeader() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_API_KEY, ENDPOINT, API_KEY, null));
        stubHttpResponse(200, SUCCESS_RESPONSE);

        service.callOut(buildCalloutConfig());

        ArgumentCaptor<HttpPost> postCaptor = ArgumentCaptor.forClass(HttpPost.class);
        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.executeHTTPRequestWithRetries(
                postCaptor.capture(), Mockito.eq(mockHttpClient),
                Mockito.anyLong(), Mockito.anyInt(), Mockito.anyDouble());

        String subscriptionKey = postCaptor.getValue()
                .getFirstHeader(APIConstants.AI.AZURE_OCP_APIM_SUBSCRIPTION_KEY_HEADER).getValue();
        Assert.assertEquals(API_KEY, subscriptionKey);

        // Authorization header must NOT be present when API key is active
        Assert.assertNull(postCaptor.getValue()
                .getFirstHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT));
    }

    @Test(expected = APIManagementException.class)
    public void testCallOut_unexpectedStatusCode_throwsException() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_API_KEY, ENDPOINT, API_KEY, null));
        stubHttpResponse(403, "{\"error\":\"Forbidden\"}");

        service.callOut(buildCalloutConfig());
    }

    @Test(expected = APIManagementException.class)
    public void testCallOut_missingServiceParam_throwsException() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_API_KEY, ENDPOINT, API_KEY, null));

        Map<String, Object> config = new HashMap<>();
        // service key intentionally omitted
        config.put(APIConstants.AI.GUARDRAIL_PROVIDER_AZURE_CONTENTSAFETY_CALLOUT_PAYLOAD,
                new HashMap<>());
        service.callOut(config);
    }

    @Test(expected = APIManagementException.class)
    public void testCallOut_umiTokenProviderThrows_propagatesException() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_UMI, ENDPOINT, null, null));
        Mockito.when(mockUmiProvider.getAccessToken())
                .thenThrow(new APIManagementException("Workload identity credential unavailable"));

        service.callOut(buildCalloutConfig());
    }

    // -------------------------------------------------------------------------
    // getType()
    // -------------------------------------------------------------------------

    @Test
    public void testGetType_returnsAzureContentSafetyType() {
        Assert.assertEquals(APIConstants.AI.GUARDRAIL_PROVIDER_AZURE_CONTENTSAFETY_TYPE,
                service.getType());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private GuardrailProviderConfigurationDTO buildConfig(
            String authType, String endpoint, String apiKey, String umiScope)
            throws APIManagementException {
        Map<String, String> props = new HashMap<>();
        if (endpoint != null) {
            props.put(APIConstants.AI.GUARDRAIL_PROVIDER_AZURE_CONTENTSAFETY_ENDPOINT, endpoint);
        }
        props.put(APIConstants.AI.AUTH_TYPE, authType);
        if (apiKey != null) {
            props.put(APIConstants.AI.GUARDRAIL_PROVIDER_AZURE_CONTENTSAFETY_KEY, apiKey);
        }
        if (umiScope != null) {
            props.put(APIConstants.AI.AZURE_UMI_SCOPE_KEY, umiScope);
        }
        props.put(APIConstants.AI.RETRIEVAL_TIMEOUT, APIConstants.AI.DEFAULT_RETRIEVAL_TIMEOUT);
        props.put(APIConstants.AI.RETRY_COUNT, APIConstants.AI.DEFAULT_RETRY_COUNT);
        props.put(APIConstants.AI.RETRY_PROGRESSION_FACTOR,
                APIConstants.AI.DEFAULT_RETRY_PROGRESSION_FACTOR);
        return configFrom(props);
    }

    private GuardrailProviderConfigurationDTO configFrom(Map<String, String> props) {
        GuardrailProviderConfigurationDTO dto = new GuardrailProviderConfigurationDTO();
        dto.setProperties(props);
        return dto;
    }

    private Map<String, Object> buildCalloutConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(APIConstants.AI.GUARDRAIL_PROVIDER_AZURE_CONTENTSAFETY_CALLOUT_SERVICE,
                SERVICE_PATH);
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", "sample input");
        config.put(APIConstants.AI.GUARDRAIL_PROVIDER_AZURE_CONTENTSAFETY_CALLOUT_PAYLOAD, payload);
        return config;
    }

    private void stubHttpResponse(int statusCode, String body) throws Exception {
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getEntity()).thenReturn(
                new StringEntity(body, StandardCharsets.UTF_8));

        BDDMockito.given(APIUtil.executeHTTPRequestWithRetries(
                        Mockito.any(HttpPost.class), Mockito.any(HttpClient.class),
                        Mockito.anyLong(), Mockito.anyInt(), Mockito.anyDouble()))
                .willReturn(mockHttpResponse);
    }
}
