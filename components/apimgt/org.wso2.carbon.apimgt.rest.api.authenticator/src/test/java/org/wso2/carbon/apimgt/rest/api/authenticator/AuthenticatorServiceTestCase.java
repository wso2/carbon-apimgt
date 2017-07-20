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
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

/**
 * Test class for AuthenticatorService.
 */
public class AuthenticatorServiceTestCase {
    @Test(description = "Provide DCR application information to the SSO-IS login")
    public void testGetDCRApplicationDetails() throws Exception {
        // Happy Path - 200
        //// Mocked response object from DCR api
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId("xxx-xxx-xxx-xxx");
        oAuthApplicationInfo.setCallBackURL("https://localhost/9292/login/callback/store");

        //// Expected data object to be passed to the front-end
        JsonObject oAuthData = new JsonObject();
        String scopes = "apim:workflow_approve apim:subscribe openid";
        oAuthData.addProperty(KeyManagerConstants.OAUTH_CLIENT_ID, oAuthApplicationInfo.getClientId());
        oAuthData.addProperty(KeyManagerConstants.OAUTH_CALLBACK_URIS, oAuthApplicationInfo.getCallBackURL());
        oAuthData.addProperty(KeyManagerConstants.TOKEN_SCOPES , scopes);
        oAuthData.addProperty("is_sso_enabled" , true);

        //// Get data object to be passed to the front-end
        KeyManager keyManager = Mockito.mock(KeyManager.class);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager);
        Mockito.when(keyManager.createApplication(Mockito.any())).thenReturn(oAuthApplicationInfo);
        JsonObject responseOAuthDataObj = authenticatorService.getDCRApplicationDetails("store");
        Assert.assertEquals(responseOAuthDataObj, oAuthData);

        // Error Path - 500 - When OAuthApllicationInfo is null
        JsonObject emptyOAuthDataObj = new JsonObject();
        Mockito.when(keyManager.createApplication(Mockito.any())).thenReturn(null);
        JsonObject responseEmptyOAuthDataObj = authenticatorService.getDCRApplicationDetails("store");
        Assert.assertEquals(responseEmptyOAuthDataObj, emptyOAuthDataObj);
    }

    @Test
    public void testGetTokens() throws Exception {
    }

    @Test
    public void testRevokeAccessToken() throws Exception {
    }

    @Test
    public void testSetAccessTokenData() throws Exception {
    }
}
