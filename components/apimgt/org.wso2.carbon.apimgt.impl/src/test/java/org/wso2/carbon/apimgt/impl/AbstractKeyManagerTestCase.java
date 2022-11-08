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
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.factory.ModelKeyManagerForTest;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class AbstractKeyManagerTestCase {

    @Test
    public void buildAccessTokenRequestFromJSONTest() throws APIManagementException {
        String jsonPayload = "{ \"callbackUrl\": \"www.google.lk\", \"clientName\": \"rest_api_publisher\", " +
                "\"tokenScope\": \"Production\", \"owner\": \"admin\", \"grantType\": \"password refresh_token\", " +
                "\"saasApp\": true }";

        AbstractKeyManager keyManager = new AMDefaultKeyManagerImpl();
        // test AccessTokenRequest null scenario
        AccessTokenRequest accessTokenRequest1 = keyManager.buildAccessTokenRequestFromJSON(jsonPayload, null);
        Assert.notNull(accessTokenRequest1);

        // test json payload without required parameters
        AccessTokenRequest accessTokenRequest2 = keyManager.buildAccessTokenRequestFromJSON(jsonPayload,
                accessTokenRequest1);
        Assert.notNull(accessTokenRequest2);
        assertNull(accessTokenRequest2.getClientId());

        // test json payload null
        assertNull(keyManager.buildAccessTokenRequestFromJSON(null, null));

        String jsonPayload2 = "{ \"callbackUrl\": \"www.google.lk\", \"client_id\": \"XBPcXSfGK47WiEX7enchoP2Dcvga\"," +
                "\"client_secret\": \"4UD8VX8NaQMtrHCwqzI1tHJLPoca\", \"owner\": \"admin\", \"grantType\": \"password" +
                " refresh_token\", " +
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

        KeyManagerConnectorConfiguration keyManagerConnectorConfiguration = Mockito
                .mock(DefaultKeyManagerConnectorConfiguration.class);
        ServiceReferenceHolder serviceReferenceHolder = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder
                .getKeyManagerConnectorConfiguration(APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE))
                .thenReturn(keyManagerConnectorConfiguration);

        // test with empty json payload
        assertNotNull(keyManager.buildFromJSON(new OAuthApplicationInfo(), "{}"));

        // test with valid json
        String jsonPayload2 = "{ \"callbackUrl\": \"www.google.lk\", \"client_id\": \"XBPcXSfGK47WiEX7enchoP2Dcvga\"," +
                "\"client_secret\": \"4UD8VX8NaQMtrHCwqzI1tHJLPoca\", \"owner\": \"admin\", \"grantType\": \"password" +
                "  refresh_token\", " +
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

        //test with invalid additionalProperties
        OAuthApplicationInfo applicationInfo = new OAuthApplicationInfo();
        applicationInfo.addParameter("additionalProperties", "{invalid}");
        try {
            keyManager.buildFromJSON(applicationInfo, "{}");
            fail();
        } catch (APIManagementException e) {
            assertEquals("Error while parsing the addition properties of OAuth application", e.getMessage());
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

    @Test
    public void testCanHandleToken() throws APIManagementException {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        KeyManager keyManager = new ModelKeyManagerForTest();
        keyManager.loadConfiguration(keyManagerConfiguration);
        assertTrue(keyManager.canHandleToken(UUID.randomUUID().toString()));
    }

    @Test
    public void testCanHandleTokenEmptyConfiguration() throws APIManagementException {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        KeyManager keyManager = new ModelKeyManagerForTest();
        keyManagerConfiguration.addParameter(APIConstants.KeyManager.TOKEN_FORMAT_STRING, "[]");
        keyManager.loadConfiguration(keyManagerConfiguration);
        assertTrue(keyManager.canHandleToken(UUID.randomUUID().toString()));
    }

    @Test
    public void testCanHandleTokenWithConfiguration() throws APIManagementException {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        keyManagerConfiguration.addParameter(APIConstants.KeyManager.TOKEN_FORMAT_STRING,
                "[{\"enable\": true,\"type\": \"JWT\",\"value\": {\"body\": {\"iss\": \"https://localhost:9443\"}}}]");
        KeyManager keyManager = new ModelKeyManagerForTest();
        keyManager.loadConfiguration(keyManagerConfiguration);
        assertFalse(keyManager.canHandleToken(UUID.randomUUID().toString()));
    }
    @Test
    public void testCanHandleTokenWithConfigurationJWT() throws APIManagementException {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        keyManagerConfiguration.addParameter(APIConstants.KeyManager.TOKEN_FORMAT_STRING,
                "[{\"enable\": true,\"type\": \"JWT\",\"value\": {\"body\": {\"iss\": \"https://localhost:9443\"}}}]");
        KeyManager keyManager = new ModelKeyManagerForTest();
        keyManager.loadConfiguration(keyManagerConfiguration);
        assertTrue(keyManager.canHandleToken(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                        ".eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQzIiwiaWF0IjoxNTkwMTM0NzIyLCJleHAiOjE2MjE2NzA3MjAsImF1ZC" +
                        "I6Ind3dy5leGFtcGxlLmNvbSIsInN1YiI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJFbWFpbCI6ImJlZUBleGFtcGxlLmNvb" +
                        "SJ9.HIxL7_WqeLPkxYdROAwRyL0YEY1YNJRfLghsaHEc7C4"));
        assertFalse(keyManager.canHandleToken(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ0IiwiaWF0IjoxN" +
                        "TkwMTM0NzIyLCJleHAiOjE2MjE2NzA3MjAsImF1ZCI6Ind3dy5leGFtcGxlLmNvbSIsInN1YiI6Impyb2NrZXRAZXhhb" +
                        "XBsZS5jb20iLCJFbWFpbCI6ImJlZUBleGFtcGxlLmNvbSJ9.QjwcCl7Xs0zmioqsr85VQmW5lgRnkfba-v8OgKwhKyA"));
    }

    @Test
    public void testCanHandleTokenWithConfigurationJWTMultipleClaim() throws APIManagementException {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        keyManagerConfiguration.addParameter(APIConstants.KeyManager.TOKEN_FORMAT_STRING,
                "[{\"enable\": true,\"type\": \"JWT\",\"value\": {\"body\": {\"iss\": \"https://localhost:9443\"," +
                        "\"domain\": \"abc.com\"}}}]");
        KeyManager keyManager = new ModelKeyManagerForTest();
        keyManager.loadConfiguration(keyManagerConfiguration);
        assertTrue(keyManager.canHandleToken(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ0IiwiaWF0IjoxN" +
                        "TkwMTM0NzIyLCJleHAiOjE2MjE2NzA3MjAsImF1ZCI6Ind3dy5leGFtcGxlLmNvbSIsInN1YiI6Impyb2NrZXRAZXhhbX" +
                        "BsZS5jb20iLCJkb21haW4iOiJhYmMuY29tIn0.pHI2MUhvdGjcOj2yJ-05cHMwtx5kanMhO71m0wFhjic"));
        assertFalse(keyManager.canHandleToken(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                        ".eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQzIiwiaWF0IjoxNTkwMTM0NzIyLCJleHAiOjE2MjE2NzA3MjAsImF1ZC" +
                        "I6Ind3dy5leGFtcGxlLmNvbSIsInN1YiI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJFbWFpbCI6ImJlZUBleGFtcGxlLmNvb" +
                        "SJ9.HIxL7_WqeLPkxYdROAwRyL0YEY1YNJRfLghsaHEc7C4"));

    }
    @Test
    public void testCanHandleTokenWithConfigurationJWTAndOpaue() throws APIManagementException {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        keyManagerConfiguration.addParameter(APIConstants.KeyManager.TOKEN_FORMAT_STRING,
                "[{\"enable\": true,\"type\": \"JWT\",\"value\": {\"body\": {\"iss\": \"https://localhost:9443\"}}}," +
                        "{\"enable\": true,\"type\": \"REFERENCE\",\"value\": \"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0" +
                        "-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}\"}]");
        KeyManager keyManager = new ModelKeyManagerForTest();
        keyManager.loadConfiguration(keyManagerConfiguration);
        assertTrue(keyManager.canHandleToken(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                        ".eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQzIiwiaWF0IjoxNTkwMTM0NzIyLCJleHAiOjE2MjE2NzA3MjAsImF1ZC" +
                        "I6Ind3dy5leGFtcGxlLmNvbSIsInN1YiI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJFbWFpbCI6ImJlZUBleGFtcGxlLmNvb" +
                        "SJ9.HIxL7_WqeLPkxYdROAwRyL0YEY1YNJRfLghsaHEc7C4"));
        assertTrue(keyManager.canHandleToken(UUID.randomUUID().toString()));
    }
    @Test
    public void testCanHandleTokenWithConfigurationJWTAndOpaueDisableOne() throws APIManagementException {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        keyManagerConfiguration.addParameter(APIConstants.KeyManager.TOKEN_FORMAT_STRING,
                "[{\"enable\": true,\"type\": \"JWT\",\"value\": {\"body\": {\"iss\": \"https://localhost:9443\"}}}," +
                        "{\"enable\": false,\"type\": \"REFERENCE\",\"value\": " +
                        "\"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0" +
                        "-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}\"}]");
        KeyManager keyManager = new ModelKeyManagerForTest();
        keyManager.loadConfiguration(keyManagerConfiguration);
        assertTrue(keyManager.canHandleToken(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                        ".eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQzIiwiaWF0IjoxNTkwMTM0NzIyLCJleHAiOjE2MjE2NzA3MjAsImF1ZC" +
                        "I6Ind3dy5leGFtcGxlLmNvbSIsInN1YiI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJFbWFpbCI6ImJlZUBleGFtcGxlLmNvb" +
                        "SJ9.HIxL7_WqeLPkxYdROAwRyL0YEY1YNJRfLghsaHEc7C4"));
        assertFalse(keyManager.canHandleToken(UUID.randomUUID().toString()));
    }
    @Test
    public void testCanHandleTokenWithConfigurationJWTAndOpaueNegative() throws APIManagementException {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        keyManagerConfiguration.addParameter(APIConstants.KeyManager.TOKEN_FORMAT_STRING,
                "[{\"enable\": true,\"type\": \"JWT\",\"value\": {\"body\": {\"iss\": \"https://localhost:9443\"}}}," +
                        "{\"enable\": true,\"type\": \"REFERENCE\",\"value\": \"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0" +
                        "-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}\"}]");
        KeyManager keyManager = new ModelKeyManagerForTest();
        keyManager.loadConfiguration(keyManagerConfiguration);
        assertFalse(keyManager.canHandleToken("avffr.erwrwrwr.ergrtyttwre"));
    }
}
