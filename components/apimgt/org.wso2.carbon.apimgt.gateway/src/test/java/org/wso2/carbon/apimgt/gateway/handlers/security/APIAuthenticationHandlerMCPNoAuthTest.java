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
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.authenticator.InternalAPIKeyAuthenticator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

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

    @Test
    public void testNoAuthRequestCredentialsRemovedFromMessageContext() {

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getFirstProperty(APIConstants.OAUTH_CONFIGS + APIConstants.AUTHORIZATION_HEADER))
                .thenReturn("X-Global-Auth");
        APIAuthenticationHandler handler = new APIAuthenticationHandler() {
            @Override
            protected APIManagerConfiguration getApiManagerConfiguration() {
                return configuration;
            }
        };
        handler.setAuthorizationHeader(CUSTOM_AUTH_HEADER);
        handler.setSubType(APIConstants.API_SUBTYPE_SERVER_PROXY);
        Map<String, String> headers = headers("X-Global-Auth");
        headers.put(CUSTOM_AUTH_HEADER, CLIENT_TOKEN);

        handler.removeClientCredentialsFromMCPNoAuthRequest(messageContext(headers));

        Assert.assertFalse(headers.containsKey("X-Global-Auth"));
        Assert.assertFalse(headers.containsKey(CUSTOM_AUTH_HEADER));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
        assertOtherHeadersKept(headers);
    }

    @Test
    public void testInternalKeyAuthenticatorGetsAPILevelAuthHeader() {

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getJwtConfigurationDto()).thenReturn(new ExtendedJWTConfigurationDto());
        ServiceReferenceHolder holder = ServiceReferenceHolder.getInstance();
        APIManagerConfigurationService previous = holder.getApiManagerConfigurationService();
        ThrottleProperties previousThrottleProperties = holder.getThrottleProperties();
        holder.setAPIManagerConfigurationService(configurationService(configuration));
        try {
            APIAuthenticationHandler handler = new APIAuthenticationHandler();
            // Only the Internal Key authenticator is added for this security setting
            handler.setAPISecurity(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY);
            handler.setAuthorizationHeader(CUSTOM_AUTH_HEADER);

            handler.initializeAuthenticators();

            InternalAPIKeyAuthenticator internalKeyAuthenticator = null;
            for (Authenticator authenticator : handler.authenticators) {
                if (authenticator instanceof InternalAPIKeyAuthenticator) {
                    internalKeyAuthenticator = (InternalAPIKeyAuthenticator) authenticator;
                }
            }
            Assert.assertNotNull(internalKeyAuthenticator);
            Assert.assertEquals(CUSTOM_AUTH_HEADER,
                    Whitebox.getInternalState(internalKeyAuthenticator, "authorizationHeader"));
        } finally {
            holder.setAPIManagerConfigurationService(previous);
            holder.setThrottleProperties(previousThrottleProperties);
        }
    }

    @Test
    public void testMCPToolWithoutSecurityRemovesClientCredentials() throws Exception {

        Map<String, String> headers = headers(CUSTOM_AUTH_HEADER);
        headers.put(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN);

        Assert.assertTrue(handleRequestForResourceWithoutSecurity(APIConstants.API_TYPE_MCP,
                APIConstants.API_SUBTYPE_SERVER_PROXY, headers));

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
        Assert.assertFalse(headers.containsKey(CUSTOM_AUTH_HEADER));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
        assertOtherHeadersKept(headers);
    }

    @Test
    public void testRESTResourceWithoutSecurityKeepsHeaders() throws Exception {

        Map<String, String> headers = headers(CUSTOM_AUTH_HEADER);
        headers.put(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN);

        Assert.assertTrue(handleRequestForResourceWithoutSecurity(APIConstants.ApiTypes.API.name(), null, headers));

        Assert.assertEquals(CLIENT_TOKEN, headers.get(HttpHeaders.AUTHORIZATION));
        Assert.assertEquals(CLIENT_TOKEN, headers.get(CUSTOM_AUTH_HEADER));
        Assert.assertEquals(INTERNAL_KEY, headers.get(APIMgtGatewayConstants.INTERNAL_KEY));
    }

    // Runs handleRequest for a tools/call (or a REST call) whose resource-level security is None
    private static boolean handleRequestForResourceWithoutSecurity(String apiType, String subType,
                                                                   Map<String, String> headers) throws Exception {

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getExtensionListenerMap()).thenReturn(new TreeMap<>());
        Mockito.when(configuration.getDoMediateExtensionFaultSequenceMap()).thenReturn(new TreeMap<>());
        APIKeyValidator validator = Mockito.mock(APIKeyValidator.class);
        Mockito.when(validator.getResourceAuthenticationScheme(Mockito.any()))
                .thenReturn(APIConstants.AUTH_NO_AUTHENTICATION);
        APIAuthenticationHandler handler = new APIAuthenticationHandler() {
            @Override
            protected APIManagerConfiguration getApiManagerConfiguration() {
                return configuration;
            }

            @Override
            protected APIKeyValidator getAPIKeyValidator() {
                return validator;
            }

            @Override
            protected org.wso2.carbon.metrics.manager.Timer.Context startMetricTimer() {
                return Mockito.mock(org.wso2.carbon.metrics.manager.Timer.Context.class);
            }

            @Override
            protected void stopMetricTimer(org.wso2.carbon.metrics.manager.Timer.Context context) {
            }

            @Override
            protected boolean isAnalyticsEnabled() {
                return false;
            }

            @Override
            protected void setAPIParametersToMessageContext(org.apache.synapse.MessageContext messageContext) {
            }
        };
        handler.isAuthenticatorsInitialized = true;
        handler.isOauthParamsInitialized = true;
        handler.setApiType(apiType);
        handler.setSubType(subType);
        handler.setAuthorizationHeader(CUSTOM_AUTH_HEADER);

        Axis2MessageContext messageContext = messageContext(headers);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.MCP_METHOD))
                .thenReturn(APIConstants.MCP.METHOD_TOOL_CALL);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.MCP_NO_AUTH_REQUEST)).thenReturn(false);

        // ExtensionListenerUtil reads the configuration from the impl component's holder
        org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder holder =
                org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance();
        APIManagerConfigurationService previous = holder.getAPIManagerConfigurationService();
        holder.setAPIManagerConfigurationService(configurationService(configuration));
        try {
            return handler.handleRequest(messageContext);
        } finally {
            holder.setAPIManagerConfigurationService(previous);
        }
    }

    private static APIManagerConfigurationService configurationService(APIManagerConfiguration configuration) {

        APIManagerConfigurationService service = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(service.getAPIManagerConfiguration()).thenReturn(configuration);
        return service;
    }

    private static Axis2MessageContext messageContext(Map<String, String> headers) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getAxis2MessageContext()).thenReturn(axis2MessageContext);
        return messageContext;
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
