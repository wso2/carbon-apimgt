/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.OAuthTokenGenerator;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.TokenCache;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.OAuthClient;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.TokenResponse;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OAuthClient.class, OAuthTokenGenerator.class, TokenCache.class, ServiceReferenceHolder.class,
        APIUtil.class})
public class OAuthClientTest {
    @Mock
    private CloseableHttpClient mockHttpClient;

    @Mock
    private CloseableHttpResponse mockResponse;

    @Mock
    private StatusLine mockStatusLine;

    private static final String TOKEN_URL = "https://example.com/token";
    private static final String CLIENT_ID = "testClientId";
    private static final String CLIENT_SECRET = "testClientSecret";
    private static final String USERNAME = "testUser";
    private static final char[] PASSWORD = "testPassword".toCharArray();
    private static final String GRANT_TYPE = "PASSWORD";
    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String ACCESS_TOKEN_VALUE = "testAccessToken";
    private static final String REFRESH_TOKEN_FIELD = "refresh_token";
    private static final String REFRESH_TOKEN_VALUE = "testRefreshToken";
    private static final String TOKEN_TYPE_FIELD = "token_type";
    private static final String TOKEN_TYPE_VALUE = "Bearer";
    private static final String SCOPE_FIELD = "scope";
    private static final String READ_SCOPE = "read";
    private static final String WRITE_SCOPE = "write";
    private static final String EXPIRES_IN_FIELD = "expires_in";
    private static final long EXPIRES_IN_VALUE = 1440;

    @Before
    public void setup() throws IOException {
        // Mocking dependencies
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        // Mock APIUtil.getHttpClient() static call
        mockStatic(APIUtil.class);
        when(APIUtil.getHttpClient(anyInt(), anyString())).thenReturn(mockHttpClient);
    }

    /**
     * Test generated token response when token endpoint is invoked with client credentials grant type
     */
    @Test
    public void testGenerateTokenWithClientCredentialsGrant() throws IOException, APIManagementException, ParseException {

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put(ACCESS_TOKEN_FIELD, ACCESS_TOKEN_VALUE);
        tokenData.put(TOKEN_TYPE_FIELD, TOKEN_TYPE_VALUE);
        tokenData.put(SCOPE_FIELD, READ_SCOPE);
        tokenData.put(EXPIRES_IN_FIELD, EXPIRES_IN_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(tokenData);

        when(mockResponse.getEntity()).thenReturn(new StringEntity(jsonResponse));

        // Call method under test
        TokenResponse tokenResponse = OAuthClient.generateToken(
                TOKEN_URL, CLIENT_ID, CLIENT_SECRET, null, null, "CLIENT_CREDENTIALS", null, null);

        // Validate results
        validateResults(tokenResponse);
        assertNull(tokenResponse.getRefreshToken());
        assertEquals(Stream.of(READ_SCOPE).collect(Collectors.toSet()), tokenResponse.getScope());
    }

    /**
     * Test generated token response when token endpoint is invoked with password grant type
     */
    @Test
    public void testGenerateTokenWithPasswordGrant() throws Exception {

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put(ACCESS_TOKEN_FIELD, ACCESS_TOKEN_VALUE);
        tokenData.put(REFRESH_TOKEN_FIELD, REFRESH_TOKEN_VALUE);
        tokenData.put(TOKEN_TYPE_FIELD, TOKEN_TYPE_VALUE);
        tokenData.put(SCOPE_FIELD, READ_SCOPE + " " + WRITE_SCOPE);
        tokenData.put(EXPIRES_IN_FIELD, EXPIRES_IN_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(tokenData);
        when(mockResponse.getEntity()).thenReturn(new StringEntity(jsonResponse));

        // Call method under test
        TokenResponse tokenResponse = OAuthClient.generateToken(
                TOKEN_URL, CLIENT_ID, CLIENT_SECRET, USERNAME, PASSWORD, GRANT_TYPE, null, null);

        // Validate results
        validateResults(tokenResponse);
        assertEquals(REFRESH_TOKEN_VALUE, tokenResponse.getRefreshToken());
        assertEquals(Stream.of(READ_SCOPE + " " + WRITE_SCOPE).collect(Collectors.toSet()), tokenResponse.getScope());
    }

    /**
     * Test generated token response when token endpoint is invoked with password grant type and when scope field
     * is not present in the endpoint response
     */
    @Test
    public void testTokenResponseWithNoScopeField() throws Exception {

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put(ACCESS_TOKEN_FIELD, ACCESS_TOKEN_VALUE);
        tokenData.put(REFRESH_TOKEN_FIELD, REFRESH_TOKEN_VALUE);
        tokenData.put(TOKEN_TYPE_FIELD, TOKEN_TYPE_VALUE);
        tokenData.put(EXPIRES_IN_FIELD, EXPIRES_IN_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(tokenData);
        when(mockResponse.getEntity()).thenReturn(new StringEntity(jsonResponse));

        // Call method under test
        TokenResponse tokenResponse = OAuthClient.generateToken(
                TOKEN_URL, CLIENT_ID, CLIENT_SECRET, USERNAME, PASSWORD, GRANT_TYPE, null, null);

        // Validate results
        validateResults(tokenResponse);
        assertEquals(REFRESH_TOKEN_VALUE, tokenResponse.getRefreshToken());
        assertNull(tokenResponse.getScope());
    }

    /**
     * Test generated token response when token endpoint is invoked with password grant type and when the value of the
     * scope field present in the endpoint response is null
     */
    @Test
    public void testTokenResponseWithScopeValueNull() throws Exception {

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put(ACCESS_TOKEN_FIELD, ACCESS_TOKEN_VALUE);
        tokenData.put(REFRESH_TOKEN_FIELD, REFRESH_TOKEN_VALUE);
        tokenData.put(TOKEN_TYPE_FIELD, TOKEN_TYPE_VALUE);
        tokenData.put(SCOPE_FIELD, null);
        tokenData.put(EXPIRES_IN_FIELD, EXPIRES_IN_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(tokenData);
        when(mockResponse.getEntity()).thenReturn(new StringEntity(jsonResponse));

        // Call method under test
        TokenResponse tokenResponse = OAuthClient.generateToken(
                TOKEN_URL, CLIENT_ID, CLIENT_SECRET, USERNAME, PASSWORD, GRANT_TYPE, null, null);

        // Validate results
        validateResults(tokenResponse);
        assertEquals(REFRESH_TOKEN_VALUE, tokenResponse.getRefreshToken());
        assertNull(tokenResponse.getScope());
    }

    /**
     * Method to validate the generated token response object
     *
     * @param tokenResponse TokenResponse object returned by the token endpoint
     */
    private void validateResults(TokenResponse tokenResponse) {

        assertNotNull(tokenResponse);
        assertEquals(ACCESS_TOKEN_VALUE, tokenResponse.getAccessToken());
        assertEquals(TOKEN_TYPE_VALUE, tokenResponse.getTokenType());
        assertEquals(String.valueOf(EXPIRES_IN_VALUE), tokenResponse.getExpiresIn());
        assertNotNull(tokenResponse.getValidTill());
    }
}
