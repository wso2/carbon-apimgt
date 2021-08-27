/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.oauth;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.OAuthTokenGenerator;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.TokenCache;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.OAuthClient;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.TokenResponse;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.conf.OAuthEndpoint;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OAuthClient.class, OAuthTokenGenerator.class, TokenCache.class})
public class OAuthTokenGeneratorTest {

    private TokenResponse mockTokenResponse;
    private TokenCache tokenCache;
    private CountDownLatch latch;
    private OAuthEndpoint oAuthEndpoint;

    @Before
    public void setup() throws ParseException, IOException, APIManagementException {

        PowerMockito.spy(TokenCache.class);
        tokenCache = TokenCache.getInstance();
        PowerMockito.when(TokenCache.getInstance()).thenReturn(tokenCache);
        PowerMockito.mockStatic(OAuthClient.class);

        latch = new CountDownLatch(1);
        // Initialize mock token response.
        mockTokenResponse = new TokenResponse();
        mockTokenResponse.setAccessToken("testAccessToken");
        mockTokenResponse.setTokenType("Bearer");
        PowerMockito.when(OAuthClient
                .generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyString()))
                .thenReturn(mockTokenResponse);

        // Initialize properties of oAuthEndpoint object having common values.
        oAuthEndpoint = new OAuthEndpoint();
        oAuthEndpoint.setTokenApiUrl("testTokenURL");
        oAuthEndpoint.setClientId("testClientID");
        oAuthEndpoint.setClientSecret("decryptedClientSecret");
        JSONParser parser = new JSONParser();
        oAuthEndpoint.setCustomParameters((JSONObject) parser.parse("{}"));
    }

    /**
     * Test OAuth backend security with client credentials grant type
     */
    @Test
    public void testOauthBackendSecurityWithClientCredentialsGrant()
            throws ParseException, IOException, APIManagementException, APISecurityException {

        // Assign values for test specific properties of mock token response and oAuthEndpoint object.
        mockTokenResponse.setExpiresIn("1800");
        long validTill = System.currentTimeMillis() / 1000 + Long.parseLong(mockTokenResponse.getExpiresIn());
        mockTokenResponse.setValidTill(validTill);
        mockTokenResponse.setRefreshToken("testRefreshToken");
        oAuthEndpoint.setId("testID1");
        oAuthEndpoint.setGrantType("CLIENT_CREDENTIALS");

        // First token generation operation. Token endpoint will be called and the token response will be cached.
        TokenResponse tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        Assert.assertNotNull(tokenCache.getTokenMap().get(oAuthEndpoint.getId()));
        // Second token generation operation. Since the token response was cached, the token endpoint will not be
        // called during this operation.
        tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        // Token endpoint will be called only one time (during the first token generation operation).
        PowerMockito.verifyStatic(OAuthClient.class, Mockito.times(1));
        OAuthClient.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
    }

    /**
     * Test OAuth backend security with client credentials grant type and when token is expired
     */
    @Test
    public void testOauthBackendSecurityWithClientCredentialsGrantWhenTokenExpired()
            throws ParseException, IOException, APIManagementException, APISecurityException {

        // Assign values for test specific properties of mock token response and oAuthEndpoint object.
        // expires_in value is subtracted to replicate the token expiry behaviour.
        mockTokenResponse.setExpiresIn("1800");
        long validTill = System.currentTimeMillis() / 1000 - Long.parseLong(mockTokenResponse.getExpiresIn());
        mockTokenResponse.setValidTill(validTill);
        mockTokenResponse.setRefreshToken(null);
        oAuthEndpoint.setId("testID2");
        oAuthEndpoint.setGrantType("CLIENT_CREDENTIALS");

        // First token generation operation. Token endpoint will be called and the token response will be cached.
        TokenResponse tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        Assert.assertNotNull(tokenCache.getTokenMap().get(oAuthEndpoint.getId()));
        // Second token generation operation. Since the token is expired, the token endpoint will be called during
        // this operation.
        tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        // Third token generation operation (replicating the behaviour when the mock token response contains a refresh
        // token).
        mockTokenResponse.setRefreshToken("testRefreshToken");
        tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        // Token endpoint will be called three times (during the first, second and third token generation operations).
        PowerMockito.verifyStatic(OAuthClient.class, Mockito.times(3));
        OAuthClient.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
    }

    /**
     * Test OAuth backend security with client credentials grant type and when expires_in is not present in the
     * Token Response
     */
    @Test
    public void testOauthBackendSecurityWithClientCredentialsGrantWhenExpiresInNotPresent()
            throws ParseException, IOException, APIManagementException, APISecurityException {

        // Assign values for test specific properties of oAuthEndpoint object. expires_in and validTill properties will
        // be null in the mock token response.
        mockTokenResponse.setRefreshToken("testRefreshToken");
        oAuthEndpoint.setId("testID3");
        oAuthEndpoint.setGrantType("CLIENT_CREDENTIALS");

        // First token generation operation. Token endpoint will be called and the token response will not be cached.
        TokenResponse tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        Assert.assertNull(tokenCache.getTokenMap().get(oAuthEndpoint.getId()));
        // Second token generation operation. Since the token response was not cached, the token endpoint will be
        // called during this operation.
        tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        // Token endpoint will be called two times (during the first and second token generation operations).
        PowerMockito.verifyStatic(OAuthClient.class, Mockito.times(2));
        OAuthClient.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
    }

    /**
     * Test OAuth backend security with password grant type
     */
    @Test
    public void testOauthBackendSecurityWithPasswordGrant()
            throws ParseException, IOException, APIManagementException, APISecurityException {

        // Assign values for test specific properties of mock token response and oAuthEndpoint object.
        mockTokenResponse.setExpiresIn("1800");
        long validTill = System.currentTimeMillis() / 1000 + Long.parseLong(mockTokenResponse.getExpiresIn());
        mockTokenResponse.setValidTill(validTill);
        mockTokenResponse.setRefreshToken("testRefreshToken");
        oAuthEndpoint.setId("testID4");
        oAuthEndpoint.setUsername("username");
        oAuthEndpoint.setPassword("password".toCharArray());
        oAuthEndpoint.setGrantType("PASSWORD");

        // First token generation operation. Token endpoint will be called and the token response will be cached.
        TokenResponse tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        Assert.assertNotNull(tokenCache.getTokenMap().get(oAuthEndpoint.getId()));
        // Second token generation operation. Since the token response was cached, the token endpoint will not be
        // called during this operation.
        tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        // Token endpoint will be called only one time (during the first token generation operation).
        PowerMockito.verifyStatic(OAuthClient.class, Mockito.times(1));
        OAuthClient.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
        Assert.assertNotNull(tokenCache.getTokenMap().get(oAuthEndpoint.getId()));
    }

    /**
     * Test OAuth backend security with password grant type and when token is expired
     */
    @Test
    public void testOauthBackendSecurityWithPasswordGrantWhenTokenExpired()
            throws ParseException, IOException, APIManagementException, APISecurityException {

        // Assign values for test specific properties of mock token response and oAuthEndpoint object.
        // expires_in value is subtracted to replicate the token expiry behaviour.
        mockTokenResponse.setExpiresIn("1800");
        long validTill = System.currentTimeMillis() / 1000 - Long.parseLong(mockTokenResponse.getExpiresIn());
        mockTokenResponse.setValidTill(validTill);
        mockTokenResponse.setRefreshToken(null);
        oAuthEndpoint.setId("testID5");
        oAuthEndpoint.setUsername("username");
        oAuthEndpoint.setPassword("password".toCharArray());
        oAuthEndpoint.setGrantType("PASSWORD");

        // First token generation operation. Token endpoint will be called and the token response will be cached.
        TokenResponse tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        Assert.assertNotNull(tokenCache.getTokenMap().get(oAuthEndpoint.getId()));
        // Second token generation operation. Since the token is expired, the token endpoint will be called during
        // this operation.
        tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        // Third token generation operation (replicating the behaviour when the mock token response contains a refresh
        // token).
        mockTokenResponse.setRefreshToken("testRefreshToken");
        tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        // Token endpoint will be called three times (during the first, second and third token generation operations).
        PowerMockito.verifyStatic(OAuthClient.class, Mockito.times(3));
        OAuthClient.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
    }

    /**
     * Test OAuth backend security with password grant type and when expires_in is not present in the Token Response
     */
    @Test
    public void testOauthBackendSecurityWithPasswordGrantWhenExpiresInNotPresent()
            throws ParseException, IOException, APIManagementException, APISecurityException {

        // Assign values for test specific properties of oAuthEndpoint object. expires_in and validTill properties will
        // be null in the mock token response.
        mockTokenResponse.setRefreshToken("testRefreshToken");
        oAuthEndpoint.setId("testID6");
        oAuthEndpoint.setUsername("username");
        oAuthEndpoint.setPassword("password".toCharArray());
        oAuthEndpoint.setGrantType("PASSWORD");

        // First token generation operation. Token endpoint will be called and the token response will not be cached.
        TokenResponse tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        Assert.assertNull(tokenCache.getTokenMap().get(oAuthEndpoint.getId()));
        // Second token generation operation. Since the token response was not cached, the token endpoint will be
        // called during this operation.
        tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
        Assert.assertNotNull(tokenResponse);
        // Token endpoint will be called two times (during the first and second token generation operations).
        PowerMockito.verifyStatic(OAuthClient.class, Mockito.times(2));
        OAuthClient.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
    }
}
