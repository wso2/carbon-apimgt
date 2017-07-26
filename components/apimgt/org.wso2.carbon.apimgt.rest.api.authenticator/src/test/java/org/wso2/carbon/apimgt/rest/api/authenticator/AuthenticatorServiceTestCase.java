/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.authenticator;

import com.google.gson.JsonObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

/**
 * Test class for AuthenticatorService.
 */
public class AuthenticatorServiceTestCase {
    @Test(description = "Provide DCR application information to the SSO-IS login")
    public void testGetAuthenticationConfigurations() throws Exception {
        // Happy Path - 200
        //// Mocked response object from DCR api
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId("xxx-client-id-xxx");
        oAuthApplicationInfo.setCallBackURL("https://localhost/9292/login/callback/store");

        //// Expected data object to be passed to the front-end
        JsonObject oAuthData = new JsonObject();
        String scopes = "apim:workflow_approve apim:subscribe openid";
        oAuthData.addProperty(KeyManagerConstants.OAUTH_CLIENT_ID, oAuthApplicationInfo.getClientId());
        oAuthData.addProperty(KeyManagerConstants.OAUTH_CALLBACK_URIS, oAuthApplicationInfo.getCallBackURL());
        oAuthData.addProperty(KeyManagerConstants.TOKEN_SCOPES , scopes);
        oAuthData.addProperty(KeyManagerConstants.AUTHORIZATION_ENDPOINT, "https://localhost:9443/oauth2/authorize");
        oAuthData.addProperty("is_sso_enabled" , true);

        KeyManager keyManager = Mockito.mock(KeyManager.class);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager);

        //// Get data object to be passed to the front-end
        Mockito.when(keyManager.createApplication(Mockito.any())).thenReturn(oAuthApplicationInfo);
        JsonObject responseOAuthDataObj = authenticatorService.getAuthenticationConfigurations("store");
        Assert.assertEquals(responseOAuthDataObj, oAuthData);

        // Error Path - 500 - When OAuthApplicationInfo is null
        JsonObject emptyOAuthDataObj = new JsonObject();
        Mockito.when(keyManager.createApplication(Mockito.any())).thenReturn(null);
        JsonObject responseEmptyOAuthDataObj = authenticatorService.getAuthenticationConfigurations("store");
        Assert.assertEquals(responseEmptyOAuthDataObj, emptyOAuthDataObj);

        // Error Path - When DCR application creation fails and throws an APIManagementException
        Mockito.when(keyManager.createApplication(Mockito.any())).thenThrow(APIManagementException.class);
        try {
            authenticatorService.getAuthenticationConfigurations("store");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error while creating the keys for OAuth application : store");
        }
    }

    @Test
    public void testGetTokens() throws Exception {
        // Happy Path - 200 - Authorization code grant type
        //// Mocked response from DCR endpoint
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId("xxx-client-id-xxx");
        oAuthApplicationInfo.setClientSecret("xxx-client-secret-xxx");

        //// Expected response object from KeyManager
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        tokenInfo.setAccessToken("xxx-access-token-xxx");
        tokenInfo.setScopes("apim:workflow_approve apim:subscribe openid");
        tokenInfo.setRefreshToken("xxx-refresh-token-xxx");
        tokenInfo.setIdToken("abcdefghijklmnopqrstuvwxyz");
        tokenInfo.setValidityPeriod(-2L);

        KeyManager keyManager = Mockito.mock(KeyManager.class);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager);
        Mockito.when(keyManager.createApplication(Mockito.any())).thenReturn(oAuthApplicationInfo);

        //// Actual response - When authorization code is not null
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(tokenInfo);
        AccessTokenInfo tokenInfoResponseForValidAuthCode = authenticatorService.getTokens("store", "https://localhost:9292/auth/callback/store?code=xxx-auth-code-xxx&session_state=xxx-session-state-xxx", "authorization_code",
                null, null, null, 0);
        Assert.assertEquals(tokenInfoResponseForValidAuthCode, tokenInfo);

        // Error Path - 500 - Authorization code grant type
        //// When an error occurred - Eg: Access denied
        AccessTokenInfo emptyTokenInfo = new AccessTokenInfo();
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(emptyTokenInfo);
        AccessTokenInfo tokenInfoResponseForInvalidAuthCode = authenticatorService.getTokens("store", "https://localhost:9292/auth/callback/store?error=access_denied&session_state=xxx-session-state-xxx", "authorization_code",
                null, null, null, 0);
        Assert.assertEquals(tokenInfoResponseForInvalidAuthCode, emptyTokenInfo);

        // Happy Path - 200 - Password grant type
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(tokenInfo);
        AccessTokenInfo tokenInfoResponseForPasswordGrant = authenticatorService.getTokens("store", null, "password",
                "admin", "admin", null, 0);
        Assert.assertEquals(tokenInfoResponseForPasswordGrant, tokenInfo);

        // Error Path - When token generation fails and throws APIManagementException
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenThrow(KeyManagementException.class);
        try {
            authenticatorService.getTokens("store", null, "password",
                    "admin", "admin", null, 0);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error while receiving tokens for OAuth application : store");
        }
    }

    @Test
    public void testRevokeAccessToken() throws Exception {
    }

    @Test
    public void testSetAccessTokenData() throws Exception {
    }
}
