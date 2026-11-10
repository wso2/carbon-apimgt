/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.http.HttpHeaders;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;
import java.util.TreeMap;

/**
 * Tests the removal of the client's credentials from MCP requests that skip authentication (initialize,
 * notifications/initialized, tools/list, ping, ... when [apim.mcp] enforce_auth_for_all_mcp_methods = false).
 */
public class APIAuthenticationHandlerMCPNoAuthTest {

    private static final String CLIENT_TOKEN = "Bearer client-token";
    private static final String INTERNAL_KEY = "internal-key-value";
    private static final String CUSTOM_AUTH_HEADER = "X-Custom-Auth";
    private static final String BACKEND_KEY_HEADER = "X-Backend-Key";
    private static final String SESSION_HEADER = "Mcp-Session-Id";

    @Test
    public void testServerProxyRemovesAuthorizationAndInternalKey() {

        Map<String, String> headers = headers(HttpHeaders.AUTHORIZATION);

        APIAuthenticationHandler.removeClientCredentialsFromMCPNoAuthRequest(headers, null, HttpHeaders.AUTHORIZATION,
                APIConstants.API_SUBTYPE_SERVER_PROXY);

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
        assertOtherHeadersKept(headers);
    }

    @Test
    public void testServerProxyRemovesAPILevelCustomAuthHeader() {

        Map<String, String> headers = headers(CUSTOM_AUTH_HEADER);
        headers.put(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN);

        APIAuthenticationHandler.removeClientCredentialsFromMCPNoAuthRequest(headers, null, CUSTOM_AUTH_HEADER,
                APIConstants.API_SUBTYPE_SERVER_PROXY);

        Assert.assertFalse(headers.containsKey(CUSTOM_AUTH_HEADER));
        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
        assertOtherHeadersKept(headers);
    }

    @Test
    public void testDirectBackendRemovesInternalKey() {

        Map<String, String> headers = headers(HttpHeaders.AUTHORIZATION);

        APIAuthenticationHandler.removeClientCredentialsFromMCPNoAuthRequest(headers, null, null,
                APIConstants.API_SUBTYPE_DIRECT_BACKEND);

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
        assertOtherHeadersKept(headers);
    }

    @Test
    public void testExistingAPIKeepsInternalKey() {

        Map<String, String> headers = headers(CUSTOM_AUTH_HEADER);

        APIAuthenticationHandler.removeClientCredentialsFromMCPNoAuthRequest(headers, null, CUSTOM_AUTH_HEADER,
                APIConstants.API_SUBTYPE_EXISTING_API);

        Assert.assertFalse(headers.containsKey(CUSTOM_AUTH_HEADER));
        Assert.assertEquals(INTERNAL_KEY, headers.get(APIMgtGatewayConstants.INTERNAL_KEY));
        assertOtherHeadersKept(headers);
    }

    @Test
    public void testConfiguredGlobalAuthHeaderRemoved() {

        // [apim.oauth_config] auth_header = "X-Global-Auth", API without its own authorization header
        Map<String, String> headers = headers("X-Global-Auth");

        APIAuthenticationHandler.removeClientCredentialsFromMCPNoAuthRequest(headers, "X-Global-Auth", null,
                APIConstants.API_SUBTYPE_SERVER_PROXY);

        Assert.assertFalse(headers.containsKey("X-Global-Auth"));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
        assertOtherHeadersKept(headers);
    }

    @Test
    public void testHeaderNamesMatchedCaseInsensitively() {

        // Synapse keeps transport headers in a case-insensitive map
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put("authorization", CLIENT_TOKEN);
        headers.put("x-custom-auth", CLIENT_TOKEN);
        headers.put("internal-key", INTERNAL_KEY);

        APIAuthenticationHandler.removeClientCredentialsFromMCPNoAuthRequest(headers, null, CUSTOM_AUTH_HEADER,
                APIConstants.API_SUBTYPE_SERVER_PROXY);

        Assert.assertTrue(headers.isEmpty());
    }

    @Test
    public void testNullHeaders() {

        APIAuthenticationHandler.removeClientCredentialsFromMCPNoAuthRequest(null, null, CUSTOM_AUTH_HEADER,
                APIConstants.API_SUBTYPE_SERVER_PROXY);
    }

    private static Map<String, String> headers(String clientTokenHeader) {

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put(clientTokenHeader, CLIENT_TOKEN);
        headers.put(APIMgtGatewayConstants.INTERNAL_KEY, INTERNAL_KEY);
        headers.put(BACKEND_KEY_HEADER, "backend-key");
        headers.put(SESSION_HEADER, "session-1");
        return headers;
    }

    private static void assertOtherHeadersKept(Map<String, String> headers) {

        Assert.assertEquals("backend-key", headers.get(BACKEND_KEY_HEADER));
        Assert.assertEquals("session-1", headers.get(SESSION_HEADER));
    }
}
