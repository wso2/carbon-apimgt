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

package org.wso2.carbon.apimgt.gateway.handlers.security.oauth;

import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.JWTValidator;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.Map;
import java.util.TreeMap;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

/**
 * Tests whether OAuthAuthenticator removes the client's access token from the outbound request, based on the API
 * type, the MCP subtype and the [apim.mcp] enable_outbound_auth_header configuration.
 */
public class OAuthAuthenticatorMCPAuthHeaderTest {

    private static final String CLIENT_TOKEN = "Bearer client-token";

    @Test
    public void testNonMCPAPIFollowsOAuthConfigOnly() {

        // [apim.mcp] enable_outbound_auth_header must not affect non-MCP APIs
        OAuthAuthenticator authenticator = authenticator(true);

        Assert.assertTrue(authenticator.isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_DEFAULT), "HTTP"));
    }

    @Test
    public void testTokenRemovedForMCPServerProxyAndDirectBackendByDefault() {

        OAuthAuthenticator authenticator = authenticator(false);

        Assert.assertTrue(authenticator.isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_SERVER_PROXY), APIConstants.API_TYPE_MCP));
        Assert.assertTrue(authenticator.isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_DIRECT_BACKEND), APIConstants.API_TYPE_MCP));
    }

    @Test
    public void testTokenForwardedForMCPWhenOutboundAuthHeaderEnabled() {

        OAuthAuthenticator authenticator = authenticator(true);

        Assert.assertFalse(authenticator.isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_SERVER_PROXY), APIConstants.API_TYPE_MCP));
        Assert.assertFalse(authenticator.isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_DIRECT_BACKEND), APIConstants.API_TYPE_MCP));
    }

    @Test
    public void testTokenKeptForMCPExistingAPIInternalHop() {

        // The existing API on this Gateway applies [apim.oauth_config] enable_outbound_auth_header instead
        Assert.assertFalse(authenticator(false).isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_EXISTING_API), APIConstants.API_TYPE_MCP));
        Assert.assertFalse(authenticator(true).isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_EXISTING_API), APIConstants.API_TYPE_MCP));
    }

    @Test
    public void testNonMCPAPIForwardsTokenWhenOAuthConfigEnablesOutboundAuthHeader() {

        // removeOAuthHeader = false, i.e. [apim.oauth_config] enable_outbound_auth_header = true
        OAuthAuthenticator authenticator = new OAuthAuthenticator(HttpHeaders.AUTHORIZATION, true, false);

        Assert.assertFalse(authenticator.isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_DEFAULT), "HTTP"));
    }

    @Test
    public void testMCPWithoutSubtypeFollowsMCPConfig() {

        Assert.assertTrue(authenticator(false).isRemoveAuthHeaderFromOutMessage(
                messageContext(null), APIConstants.API_TYPE_MCP));
        Assert.assertFalse(authenticator(true).isRemoveAuthHeaderFromOutMessage(
                messageContext(null), APIConstants.API_TYPE_MCP));
    }

    @Test
    public void testTokenRemovedForMCPWhenConfigurationUnavailable() {

        OAuthAuthenticator authenticator = new OauthAuthenticatorWrapper(null);

        Assert.assertTrue(authenticator.isRemoveAuthHeaderFromOutMessage(
                messageContext(APIConstants.API_SUBTYPE_SERVER_PROXY), APIConstants.API_TYPE_MCP));
    }

    @Test
    public void testAuthHeaderRemovedWhenOnlyTheTokenWasSent() {

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer client-token");
        headers.put("X-Other", "value");

        authenticator(false).removeAuthHeaderFromOutMessage(headers, "");

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
        Assert.assertEquals("value", headers.get("X-Other"));
    }

    @Test
    public void testOtherAuthHeaderValuesPassedToBackend() {

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer client-token, Basic YWRtaW46YWRtaW4=");

        authenticator(false).removeAuthHeaderFromOutMessage(headers, " Basic YWRtaW46YWRtaW4=");

        Assert.assertEquals(" Basic YWRtaW46YWRtaW4=", headers.get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testTokenRemovedFromCustomAuthorizationHeader() {

        String customHeader = "X-Custom-Authorization";
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put(customHeader, "Bearer client-token");
        headers.put(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4=");
        OAuthAuthenticator authenticator = new OAuthAuthenticator(customHeader, true, true);

        authenticator.removeAuthHeaderFromOutMessage(headers, "");

        Assert.assertFalse(headers.containsKey(customHeader));
        Assert.assertEquals("Basic YWRtaW46YWRtaW4=", headers.get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testAuthHeaderRemovalWithoutTransportHeaders() {

        // Must not fail when the request carries no transport headers
        authenticator(false).removeAuthHeaderFromOutMessage(null, "");
    }

    @Test
    public void testAuthenticateRemovesTokenForMCPServerProxy() throws Exception {

        Map<String, String> headers = authenticateUntilHeaderDecision(false);

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testAuthenticateForwardsTokenForMCPServerProxyWhenOutboundAuthHeaderEnabled() throws Exception {

        Map<String, String> headers = authenticateUntilHeaderDecision(true);

        Assert.assertEquals(CLIENT_TOKEN, headers.get(HttpHeaders.AUTHORIZATION));
    }

    // Runs authenticate() for an MCP SERVER_PROXY request up to the token validation, which starts with getTimer
    private static Map<String, String> authenticateUntilHeaderDecision(boolean mcpOutboundAuthHeaderEnabled)
            throws Exception {

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.isMCPOutboundAuthHeaderEnabled()).thenReturn(mcpOutboundAuthHeaderEnabled);
        Mockito.when(apiManagerConfiguration.getJwtConfigurationDto()).thenReturn(new ExtendedJWTConfigurationDto());
        OAuthAuthenticator authenticator = new OAuthAuthenticator(HttpHeaders.AUTHORIZATION, true, true) {
            @Override
            protected APIManagerConfiguration getApiManagerConfiguration() {
                return apiManagerConfiguration;
            }

            @Override
            protected Timer getTimer(String name) {
                throw new TokenValidationReached();
            }
        };
        authenticator.keyValidator = Mockito.mock(APIKeyValidator.class);
        authenticator.jwtValidator = Mockito.mock(JWTValidator.class);

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getAxis2MessageContext()).thenReturn(axis2MessageContext);
        API api = new API();
        api.setUuid("mcp-server-uuid");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_OBJECT)).thenReturn(api);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_TYPE)).thenReturn(APIConstants.API_TYPE_MCP);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.SUB_TYPE))
                .thenReturn(APIConstants.API_SUBTYPE_SERVER_PROXY);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.MCP_HTTP_METHOD_KEY)).thenReturn("POST");

        // Needed by PrivilegedCarbonContext, which resolves the tenant domain
        if (System.getProperty(CARBON_HOME) == null) {
            System.setProperty(CARBON_HOME, "");
        }
        try {
            authenticator.authenticate(messageContext);
            Assert.fail("Token validation was not reached");
        } catch (TokenValidationReached expected) {
            // The authorization header has been handled by this point
        }
        return headers;
    }

    private static class TokenValidationReached extends RuntimeException {
    }

    private static OAuthAuthenticator authenticator(boolean mcpOutboundAuthHeaderEnabled) {

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.isMCPOutboundAuthHeaderEnabled()).thenReturn(mcpOutboundAuthHeaderEnabled);
        return new OauthAuthenticatorWrapper(apiManagerConfiguration);
    }

    private static MessageContext messageContext(String subType) {

        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.SUB_TYPE)).thenReturn(subType);
        return messageContext;
    }
}
