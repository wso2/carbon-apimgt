/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl;

import org.compass.core.util.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class AbstractKeyManagerTestCase {

    @Test
    public void buildAccessTokenRequestFromJSONTest() throws APIManagementException {
        String jsonPayload = "{ \"callbackUrl\": \"www.google.lk\", \"clientName\": \"rest_api_publisher\", " +
                "\"tokenScope\": \"Production\", \"owner\": \"admin\", \"grantType\": \"password refresh_token\", \"saasApp\": true }";

        AbstractKeyManager keyManager = new AMDefaultKeyManagerImpl();
        // test AccessTokenRequest null scenario
        AccessTokenRequest accessTokenRequest1 = keyManager.buildAccessTokenRequestFromJSON(jsonPayload, null);
        Assert.notNull(accessTokenRequest1);

        // test json payload without required parameters
        AccessTokenRequest accessTokenRequest2 = keyManager.buildAccessTokenRequestFromJSON(jsonPayload, accessTokenRequest1);
        Assert.notNull(accessTokenRequest2);
        assertNull(accessTokenRequest2.getClientId());

        // test json payload null
        assertNull(keyManager.buildAccessTokenRequestFromJSON(null, null));

        String jsonPayload2 = "{ \"callbackUrl\": \"www.google.lk\", \"client_id\": \"XBPcXSfGK47WiEX7enchoP2Dcvga\", " +
                "\"client_secret\": \"4UD8VX8NaQMtrHCwqzI1tHJLPoca\", \"owner\": \"admin\", \"grantType\": \"password refresh_token\", " +
                "\"validityPeriod\": \"3600\" }";
        AccessTokenRequest accessTokenRequest3 = keyManager.buildAccessTokenRequestFromJSON(jsonPayload2,
                new AccessTokenRequest());

        assertEquals("XBPcXSfGK47WiEX7enchoP2Dcvga", accessTokenRequest3.getClientId());
        assertEquals("4UD8VX8NaQMtrHCwqzI1tHJLPoca", accessTokenRequest3.getClientSecret());
        assertEquals(3600, accessTokenRequest3.getValidityPeriod());

        //Error path with invalid json
        try {
            keyManager.buildAccessTokenRequestFromJSON("{dd}", null);
            assertTrue(false);
        } catch (APIManagementException e) {
            assertEquals("Error occurred while parsing JSON String", e.getMessage());
        }

        //Error path with empty JSON
        assertNull(keyManager.buildAccessTokenRequestFromJSON("{}", null));
        keyManager.buildAccessTokenRequestFromJSON(null, new AccessTokenRequest());
    }

    @Test
    public void buildFromJSONTest() throws APIManagementException {
        AbstractKeyManager keyManager = new AMDefaultKeyManagerImpl();

        // test with empty json payload
        assertNotNull(keyManager.buildFromJSON(new OAuthApplicationInfo(), "{}"));

        // test with valid json
        String jsonPayload2 = "{ \"callbackUrl\": \"www.google.lk\", \"client_id\": \"XBPcXSfGK47WiEX7enchoP2Dcvga\", " +
                "\"client_secret\": \"4UD8VX8NaQMtrHCwqzI1tHJLPoca\", \"owner\": \"admin\", \"grantType\": \"password refresh_token\", " +
                "\"validityPeriod\": \"3600\" }";
        OAuthApplicationInfo oAuthApplicationInfo1 = keyManager.buildFromJSON(new OAuthApplicationInfo(), jsonPayload2);
        assertEquals("XBPcXSfGK47WiEX7enchoP2Dcvga", oAuthApplicationInfo1.getClientId());

        //test with invalid json
        try {
            keyManager.buildFromJSON(new OAuthApplicationInfo(), "{invalid}");
            assertTrue(false);
        } catch (APIManagementException e) {
            assertEquals("Error occurred while parsing JSON String", e.getMessage());
        }
    }

    @Test
    public void buildAccessTokenRequestFromOAuthAppTest() throws APIManagementException {
        AbstractKeyManager keyManager = new AMDefaultKeyManagerImpl();

        //test null flow
        assertNull(keyManager.buildAccessTokenRequestFromOAuthApp(null, null));

        // test without client id and secret
        try {
            keyManager.buildAccessTokenRequestFromOAuthApp(new OAuthApplicationInfo(), new AccessTokenRequest());
            assertTrue(false);
        } catch (APIManagementException e) {
            assertEquals("Consumer key or Consumer Secret missing.", e.getMessage());
        }

        // test with all the parameters
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId("XBPcXSfGK47WiEX7enchoP2Dcvga");
        oAuthApplicationInfo.setClientSecret("4UD8VX8NaQMtrHCwqzI1tHJLPoca");
        oAuthApplicationInfo.addParameter("tokenScope", new String[]{"view", "update"});
        oAuthApplicationInfo.addParameter("validityPeriod", "1200");
        AccessTokenRequest accessTokenRequest = keyManager.buildAccessTokenRequestFromOAuthApp(oAuthApplicationInfo,
                null);
        assertNotNull(accessTokenRequest);
        assertEquals("XBPcXSfGK47WiEX7enchoP2Dcvga", accessTokenRequest.getClientId());
        assertEquals("4UD8VX8NaQMtrHCwqzI1tHJLPoca", accessTokenRequest.getClientSecret());
        assertEquals(1200, accessTokenRequest.getValidityPeriod());
    }
}
