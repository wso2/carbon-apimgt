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
import org.wso2.carbon.apimgt.api.dto.EmbeddingProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link AzureOpenAIEmbeddingProviderServiceImpl}.
 * <p>
 * Covers init() auth-type branching and getEmbedding() header selection for both
 * API-key and UMI authentication modes.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AzureOpenAIEmbeddingProviderServiceImpl.class, APIUtil.class})
public class AzureOpenAIEmbeddingProviderServiceImplTest {

    private static final String ENDPOINT =
            "https://my-resource.openai.azure.com/openai/deployments/my-dep/embeddings?api-version=2024-02-15-preview";
    private static final String API_KEY = "test-api-key-abc";
    private static final String UMI_TOKEN = "umi-bearer-token-xyz";

    // Single-element embedding response from Azure OpenAI
    private static final String EMBEDDING_RESPONSE =
            "{\"data\":[{\"embedding\":[0.1,0.2,0.3]}]}";

    private AzureOpenAIEmbeddingProviderServiceImpl service;
    private AzureUmiTokenProvider mockUmiProvider;
    private HttpClient mockHttpClient;
    private CloseableHttpResponse mockHttpResponse;
    private StatusLine mockStatusLine;

    @Before
    public void setUp() throws Exception {
        service = new AzureOpenAIEmbeddingProviderServiceImpl();

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
    // init() — API-key path
    // -------------------------------------------------------------------------

    @Test
    public void testInit_apiKeyAuth_storesApiKeyAndNullsUmiProvider() throws APIManagementException {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_API_KEY, ENDPOINT, API_KEY, null));

        // Provider should not be created
        Mockito.verify(mockUmiProvider, Mockito.never()).init(Mockito.anyMap());
    }

    @Test(expected = APIManagementException.class)
    public void testInit_apiKeyAuth_missingApiKey_throwsException() throws APIManagementException {
        Map<String, String> props = new HashMap<>();
        props.put(APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_ENDPOINT, ENDPOINT);
        props.put(APIConstants.AI.AUTH_TYPE, APIConstants.AI.AUTH_TYPE_API_KEY);
        // apikey property intentionally omitted
        service.init(configFrom(props));
    }

    @Test(expected = APIManagementException.class)
    public void testInit_missingEndpoint_throwsException() throws APIManagementException {
        Map<String, String> props = new HashMap<>();
        props.put(APIConstants.AI.AUTH_TYPE, APIConstants.AI.AUTH_TYPE_API_KEY);
        props.put(APIConstants.AI.EMBEDDING_PROVIDER_API_KEY, API_KEY);
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
                "Default scope for OpenAI embeddings should be cognitiveservices",
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
    // getEmbedding() — header selection
    // -------------------------------------------------------------------------

    @Test
    public void testGetEmbedding_umiAuth_setsAuthorizationBearerHeader() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_UMI, ENDPOINT, null, null));
        stubHttpResponse(200, EMBEDDING_RESPONSE);

        service.getEmbedding("test input");

        ArgumentCaptor<HttpPost> postCaptor = ArgumentCaptor.forClass(HttpPost.class);
        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.executeHTTPRequestWithRetries(
                postCaptor.capture(), Mockito.eq(mockHttpClient),
                Mockito.anyLong(), Mockito.anyInt(), Mockito.anyDouble());

        String authHeader = postCaptor.getValue()
                .getFirstHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT).getValue();
        Assert.assertEquals(APIConstants.AUTHORIZATION_BEARER + UMI_TOKEN, authHeader);
    }

    @Test
    public void testGetEmbedding_apiKeyAuth_setsApiKeyHeader() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_API_KEY, ENDPOINT, API_KEY, null));
        stubHttpResponse(200, EMBEDDING_RESPONSE);

        service.getEmbedding("test input");

        ArgumentCaptor<HttpPost> postCaptor = ArgumentCaptor.forClass(HttpPost.class);
        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.executeHTTPRequestWithRetries(
                postCaptor.capture(), Mockito.eq(mockHttpClient),
                Mockito.anyLong(), Mockito.anyInt(), Mockito.anyDouble());

        String apiKeyHeader = postCaptor.getValue()
                .getFirstHeader(APIConstants.API_KEY_AUTH).getValue();
        Assert.assertEquals(API_KEY, apiKeyHeader);
    }

    @Test(expected = APIManagementException.class)
    public void testGetEmbedding_unexpectedStatusCode_throwsException() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_API_KEY, ENDPOINT, API_KEY, null));
        stubHttpResponse(401, "{\"error\":\"Unauthorized\"}");

        service.getEmbedding("test input");
    }

    @Test(expected = APIManagementException.class)
    public void testGetEmbedding_umiTokenProviderThrows_propagatesException() throws Exception {
        service.init(buildConfig(APIConstants.AI.AUTH_TYPE_UMI, ENDPOINT, null, null));
        Mockito.when(mockUmiProvider.getAccessToken())
                .thenThrow(new APIManagementException("Workload identity credential unavailable"));

        service.getEmbedding("test input");
    }

    // -------------------------------------------------------------------------
    // getType()
    // -------------------------------------------------------------------------

    @Test
    public void testGetType_returnsAzureOpenAIType() {
        Assert.assertEquals(APIConstants.AI.AZURE_OPENAI_EMBEDDING_PROVIDER_TYPE, service.getType());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private EmbeddingProviderConfigurationDTO buildConfig(
            String authType, String endpoint, String apiKey, String umiScope)
            throws APIManagementException {
        Map<String, String> props = new HashMap<>();
        if (endpoint != null) {
            props.put(APIConstants.AI.EMBEDDING_PROVIDER_EMBEDDING_ENDPOINT, endpoint);
        }
        props.put(APIConstants.AI.AUTH_TYPE, authType);
        if (apiKey != null) {
            props.put(APIConstants.AI.EMBEDDING_PROVIDER_API_KEY, apiKey);
        }
        if (umiScope != null) {
            props.put(APIConstants.AI.AZURE_UMI_SCOPE_KEY, umiScope);
        }
        // Default retry/timeout values to avoid NumberFormatException
        props.put(APIConstants.AI.RETRIEVAL_TIMEOUT, APIConstants.AI.DEFAULT_RETRIEVAL_TIMEOUT);
        props.put(APIConstants.AI.RETRY_COUNT, APIConstants.AI.DEFAULT_RETRY_COUNT);
        props.put(APIConstants.AI.RETRY_PROGRESSION_FACTOR,
                APIConstants.AI.DEFAULT_RETRY_PROGRESSION_FACTOR);
        return configFrom(props);
    }

    private EmbeddingProviderConfigurationDTO configFrom(Map<String, String> props) {
        EmbeddingProviderConfigurationDTO dto = new EmbeddingProviderConfigurationDTO();
        dto.setProperties(props);
        return dto;
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
