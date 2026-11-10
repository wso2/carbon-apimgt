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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.util.Map;
import java.util.TreeMap;

/**
 * Tests whether OAuthAuthenticator removes the client's access token from the outbound request, based on the API
 * type, the MCP subtype and the [apim.mcp] enable_outbound_auth_header configuration.
 */
public class OAuthAuthenticatorMCPAuthHeaderTest {

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
