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

package org.wso2.carbon.apimgt.authenticator;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for AuthenticatorAPI.
 */
public class AuthenticatorAPITestCase {
    @Test
    public void testAuthenticate() throws Exception {
    }

    @Test(description = "Redirect to IS login")
    public void testRedirect() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request requestObj = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(requestObj);
        Mockito.when(requestObj.getProperty("REQUEST_URL")).thenReturn("/store/auth/apis/login/dcr");
        KeyManager keyManager = Mockito.mock(KeyManager.class);
        //AuthenticatorAPI authenticatorAPI = new AuthenticatorAPI(keyManager);
        // Happy Path - 200
        //// Mocked response object from DCR api
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("authorization_code");
        grantTypes.add("refresh_token");
        oAuthApplicationInfo.setGrantTypes(grantTypes);
        oAuthApplicationInfo.setClientName("store");
        oAuthApplicationInfo.setClientId("n69QigpPbFTzCr9VoSPv_l2BI6oa");
        oAuthApplicationInfo.setClientSecret("oBvJZen3FanevdlCVEqQvCRnW04a");
        oAuthApplicationInfo.setCallBackURL("https://localhost:9292/store/auth/apis/login/callback");

        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        Mockito.when(keyManager.createApplication(oAuthAppRequest)).thenReturn(oAuthApplicationInfo);
        //// Redirect to IS Login
        //Response responseObj = authenticatorAPI.redirect(requestObj);
        //Assert.assertEquals(responseObj.getStatus(), 200);
    }

    @Test
    public void testCallback() throws Exception {
    }

    @Test
    public void testLogout() throws Exception {
    }
}
