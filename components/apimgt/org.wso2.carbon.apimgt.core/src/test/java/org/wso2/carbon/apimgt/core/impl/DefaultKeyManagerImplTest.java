package org.wso2.carbon.apimgt.core.impl;
/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.json.simple.parser.ParseException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.RestCallUtil;
import org.wso2.carbon.apimgt.core.configuration.models.CredentialConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.KeyMgtConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.HttpResponse;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class DefaultKeyManagerImplTest {
    private String clientName = "SampleApplication";
    private String callBackURL = "http://myhost/callback";
    private String applicationKeyType = "Application";
    private List<String> grantTypes = new ArrayList<>(Arrays.asList("apim:view"));

    @Test
    public void testCreateApplication() throws Exception {
        RestCallUtil restCallUtil = Mockito.mock(RestCallUtil.class);
        KeyMgtConfigurations keyManagerConfigs = Mockito.mock(KeyMgtConfigurations.class);
        CredentialConfigurations credentialConfigurations = Mockito.mock(CredentialConfigurations.class);
        Mockito.when(keyManagerConfigs.getDcrEndpoint())
                .thenReturn("https://localhost:9282/api/identity/oauth2/dcr/v1.0/register");
        Mockito.when(keyManagerConfigs.getKeyManagerCredentials()).thenReturn(credentialConfigurations);
        Mockito.when(credentialConfigurations.getUsername()).thenReturn("admin");
        Mockito.when(credentialConfigurations.getPassword()).thenReturn("admin");

        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(keyManagerConfigs, restCallUtil);
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest(clientName, callBackURL, applicationKeyType, grantTypes);

        HttpResponse response = new HttpResponse();
        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_201_CREATED);
        String clientId = "f9efe623-23ce-4a62-bb90-7c09217601b5";
        String clientSecret = "6c58f018-c9cb-47d7-894b-11dc3fdc2527";
        String payload = "{\"client_id\":\"" + clientId + "\",\"client_secret\":\"" + clientSecret
                + "\",\"redirect_uris\":[\"regexp\\u003d\"]}";
        response.setResults(payload);

        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(response);
        OAuthApplicationInfo oAuthApplicationInfo = kmImpl.createApplication(oAuthAppRequest);
        Assert.assertEquals(oAuthApplicationInfo.getClientId(), clientId);
        Assert.assertEquals(oAuthApplicationInfo.getClientSecret(), clientSecret);

        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_400_BAD_REQUEST);
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(response);
        try {
            oAuthApplicationInfo = kmImpl.createApplication(oAuthAppRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }

        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_500_INTERNAL_SERVER_ERROR);
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(response);
        try {
            oAuthApplicationInfo = kmImpl.createApplication(oAuthAppRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }

        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_201_CREATED);
        response.setResults("invalidJson");
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(response);
        try {
            oAuthApplicationInfo = kmImpl.createApplication(oAuthAppRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }

        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(null);
        try {
            oAuthApplicationInfo = kmImpl.createApplication(oAuthAppRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }

        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenThrow(APIManagementException.class);
        try {
            oAuthApplicationInfo = kmImpl.createApplication(oAuthAppRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }

        Mockito.when(keyManagerConfigs.getDcrEndpoint()).thenReturn("https: //localhost:9282/wrong endpoint");
        try {
            oAuthApplicationInfo = kmImpl.createApplication(oAuthAppRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            Assert.assertTrue(e.getCause() instanceof URISyntaxException);
        }
    }

    @Test
    public void testRetrieveApplication() throws Exception {
        String consumerKey = "f9efe623-23ce-4a62-bb90-7c09217601b5";
        RestCallUtil restCallUtil = Mockito.mock(RestCallUtil.class);
        KeyMgtConfigurations keyManagerConfigs = Mockito.mock(KeyMgtConfigurations.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(keyManagerConfigs, restCallUtil);
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            oAuthApplicationInfo = kmImpl.retrieveApplication(null);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        Mockito.when(keyManagerConfigs.getDcrEndpoint())
                .thenReturn("https://localhost:9282/api/identity/oauth2/dcr/v1.0/register");
        Mockito.when(
                restCallUtil.getRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class)))
                .thenReturn(null);
        try {
            oAuthApplicationInfo = kmImpl.retrieveApplication(consumerKey);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        HttpResponse response = new HttpResponse();
        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_200_OK);
        String clientId = "f9efe623-23ce-4a62-bb90-7c09217601b5";
        String clientSecret = "6c58f018-c9cb-47d7-894b-11dc3fdc2527";
        String payload = "{\"client_id\":\"" + clientId + "\",\"client_secret\":\"" + clientSecret
                + "\",\"redirect_uris\":[\"regexp\\u003d\"]}";
        response.setResults(payload);

        Mockito.when(
                restCallUtil.getRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class)))
                .thenReturn(response);
        oAuthApplicationInfo = kmImpl.retrieveApplication(consumerKey);
        Assert.assertEquals(oAuthApplicationInfo.getClientId(), clientId);
        Assert.assertEquals(oAuthApplicationInfo.getClientSecret(), clientSecret);

        response.setResults("invalidJson");
        try {
            oAuthApplicationInfo = kmImpl.retrieveApplication(consumerKey);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
            Assert.assertTrue(e.getCause() instanceof ParseException);
        }

        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_400_BAD_REQUEST);
        try {
            oAuthApplicationInfo = kmImpl.retrieveApplication(consumerKey);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        Mockito.when(
                restCallUtil.getRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class)))
                .thenThrow(APIManagementException.class);
        try {
            oAuthApplicationInfo = kmImpl.retrieveApplication(consumerKey);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        Mockito.when(keyManagerConfigs.getDcrEndpoint()).thenReturn("https: //localhost:9282/wrong endpoint");
        try {
            oAuthApplicationInfo = kmImpl.retrieveApplication(consumerKey);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

    }

    @Test
    public void testGetNewAccessToken() throws Exception {
        RestCallUtil restCallUtil = Mockito.mock(RestCallUtil.class);
        KeyMgtConfigurations keyManagerConfigs = Mockito.mock(KeyMgtConfigurations.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(keyManagerConfigs, restCallUtil);

        String clientId = "f9efe623-23ce-4a62-bb90-7c09217601b5";
        String clientSecret = "6c58f018-c9cb-47d7-894b-11dc3fdc2527";
        String authorizationCode = "5d66c8f0-c9cb-135-894b-11dcsdfdc2527";
        String scopes = "apim:view";
        String accessToken = "nTgf8peXegVLAH-r9pUtxim0MU9gNXo_99hhKZwokPA";
        String refreshToken = "PRpOn-rDHKhyLnlRIAZb-CsNBxOP0zg07yqPpDc1my0";

        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        accessTokenRequest.setClientId(clientId);
        accessTokenRequest.setClientSecret(clientSecret);
        accessTokenRequest.setGrantType(KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
        accessTokenRequest.setAuthorizationCode(authorizationCode);
        accessTokenRequest.setScopes(scopes);
        accessTokenRequest.setCallbackURI(callBackURL);

        //null token request
        AccessTokenInfo tokenInfo;
        try {
            tokenInfo = kmImpl.getNewAccessToken(null);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.INVALID_TOKEN_REQUEST);
        }

        HttpResponse response = new HttpResponse();
        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_200_OK);
        String payload = "{\"access_token\":\"" + accessToken + "\"," + "\"refresh_token\":\"" + refreshToken
                + "\",\"scope\":\"scope\"," + "\"token_type\":\"Bearer\",\"expires_in\":3600}";
        response.setResults(payload);

        //happy path
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(response);
        tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
        Assert.assertEquals(tokenInfo.getAccessToken(), accessToken);
        Assert.assertEquals(tokenInfo.getRefreshToken(), refreshToken);

        accessTokenRequest.setGrantType(KeyManagerConstants.PASSWORD_GRANT_TYPE);
        tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
        Assert.assertEquals(tokenInfo.getAccessToken(), accessToken);
        Assert.assertEquals(tokenInfo.getRefreshToken(), refreshToken);

        // 500 server response
        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_500_INTERNAL_SERVER_ERROR);
        try {
            tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        //invalid json response
        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_200_OK);
        response.setResults("invalidJson");
        try {
            tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        //null response from the http client
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(null);
        try {
            tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        // when grant type is invalid
        accessTokenRequest.setGrantType("noSuchGrant");
        try {
            tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        //when rest client exception occurred
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenThrow(KeyManagementException.class);
        accessTokenRequest.setGrantType(KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
        try {
            tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        accessTokenRequest.setGrantType(KeyManagerConstants.PASSWORD_GRANT_TYPE);
        try {
            tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        //invalid token endpoint url
        Mockito.when(keyManagerConfigs.getTokenEndpoint()).thenReturn("https: //localhost:9282/wrong endpoint");
        accessTokenRequest.setGrantType(KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
        try {
            tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        accessTokenRequest.setGrantType(KeyManagerConstants.PASSWORD_GRANT_TYPE);
        try {
            tokenInfo = kmImpl.getNewAccessToken(accessTokenRequest);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }
    }

    @Test
    public void testGetTokenMetaData() throws Exception {
        String accessToken = "nTgf8peXegVLAH-r9pUtxim0MU9gNXo_99hhKZwokPA";
        RestCallUtil restCallUtil = Mockito.mock(RestCallUtil.class);
        KeyMgtConfigurations keyManagerConfigs = Mockito.mock(KeyMgtConfigurations.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(keyManagerConfigs, restCallUtil);
        Mockito.when(keyManagerConfigs.getIntrospectEndpoint())
                .thenReturn("https://localhost:9282/api/identity/oauth2/introspect/v1.0/introspection");
        HttpResponse response = new HttpResponse();
        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_200_OK);
        String payload = "{\"active\":true,\"username\":\"admin\",\"scope\":\"\",\"tokenType\":"
                + "\"user and application\",\"client_id\":\"f9efe623-23ce-4a62-bb90-7c09217601b5\","
                + "\"exp\":1515067941,\"iat\":1515064341}";
        response.setResults(payload);

        //happy path
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(response);

        AccessTokenInfo tokenInfo;
        tokenInfo = kmImpl.getTokenMetaData(accessToken);
        Assert.assertTrue(tokenInfo.isTokenValid());
        Assert.assertEquals(tokenInfo.getAccessToken(), accessToken);

        //validation fail
        payload = "{\"active\":false,\"username\":\"admin\",\"scope\":\"\",\"tokenType\":"
                + "\"user and application\",\"client_id\":\"f9efe623-23ce-4a62-bb90-7c09217601b5\","
                + "\"exp\":1515067941,\"iat\":1515064341}";
        response.setResults(payload);

        tokenInfo = kmImpl.getTokenMetaData(accessToken);
        Assert.assertFalse(tokenInfo.isTokenValid());

        //invalid json response
        response.setResults("invalidJson");
        try {
            tokenInfo = kmImpl.getTokenMetaData(accessToken);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        }

        //500 server error
        response.setResponseCode(APIMgtConstants.HTTPStatusCodes.SC_500_INTERNAL_SERVER_ERROR);
        try {
            tokenInfo = kmImpl.getTokenMetaData(accessToken);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        }

        //null http client response
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenReturn(null);
        try {
            tokenInfo = kmImpl.getTokenMetaData(accessToken);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        }

        //when http client exception occurred
        Mockito.when(restCallUtil
                .postRequest(Mockito.any(URI.class), Mockito.any(MediaType.class), Mockito.any(List.class),
                        Mockito.any(Entity.class), Mockito.any(MediaType.class), Mockito.any(Map.class)))
                .thenThrow(APIManagementException.class);
        try {
            tokenInfo = kmImpl.getTokenMetaData(accessToken);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
            Assert.assertTrue(e.getCause() instanceof APIManagementException);
        }

        //invalid introspection endpoint url
        Mockito.when(keyManagerConfigs.getIntrospectEndpoint()).thenReturn("https: //localhost:9282/wrong endpoint");
        try {
            tokenInfo = kmImpl.getTokenMetaData(accessToken);
            Assert.fail("Exception excepted");
        } catch (KeyManagementException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
            Assert.assertTrue(e.getCause() instanceof URISyntaxException);
        }
    }
}
