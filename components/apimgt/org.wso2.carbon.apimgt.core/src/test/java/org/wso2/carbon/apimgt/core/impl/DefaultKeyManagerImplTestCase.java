/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import com.google.gson.Gson;
import feign.Response;
import feign.Util;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.auth.DCRMServiceStub;
import org.wso2.carbon.apimgt.core.auth.OAuth2ServiceStubs;
import org.wso2.carbon.apimgt.core.auth.ScopeRegistration;
import org.wso2.carbon.apimgt.core.auth.dto.DCRClientInfo;
import org.wso2.carbon.apimgt.core.auth.dto.OAuth2IntrospectionResponse;
import org.wso2.carbon.apimgt.core.auth.dto.OAuth2TokenInfo;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.any;

public class DefaultKeyManagerImplTestCase {

    private static final String consumerKey = "xxx-xxx-xxx-xxx";
    private static final String consumerSecret = "yyy-yyy-yyy-yyy";

    @Test
    public void testCreateApplication() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        ScopeRegistration scopeRegistration = Mockito.mock(ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub, scopeRegistration);

        //happy path - 201
        ////request object to key manager
        List<String> grantTypesList = new ArrayList<>();
        grantTypesList.add("password");
        grantTypesList.add("client-credentials");
        OAuthAppRequest oauthAppRequest = new OAuthAppRequest("app1", "https://sample.callback/url", "PRODUCTION",
                grantTypesList);

        ////request object to dcr api
        DCRClientInfo dcrClientInfo = new DCRClientInfo();
        dcrClientInfo.setClientName(oauthAppRequest.getClientName() + '_' + oauthAppRequest.getKeyType());
        dcrClientInfo.setGrantTypes(oauthAppRequest.getGrantTypes());
        dcrClientInfo.addCallbackUrl(oauthAppRequest.getCallBackURL());
/*
        dcrClientInfo.setUserinfoSignedResponseAlg(ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getKeyManagerConfigs().getOidcUserinfoJWTSigningAlgo());
*/

        ////mocked response object from dcr api
        DCRClientInfo dcrClientInfoResponse = new DCRClientInfo();
        dcrClientInfoResponse.setClientName(oauthAppRequest.getClientName());
        dcrClientInfoResponse.setGrantTypes(oauthAppRequest.getGrantTypes());
        dcrClientInfoResponse.addCallbackUrl(oauthAppRequest.getCallBackURL());
/*
        dcrClientInfoResponse.setUserinfoSignedResponseAlg(ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getKeyManagerConfigs().getOidcUserinfoJWTSigningAlgo());
*/
        dcrClientInfoResponse.setClientId("xxx-xxx-xxx-xxx");
        dcrClientInfoResponse.setClientSecret("yyy-yyy-yyy-yyy");
        dcrClientInfoResponse.setClientIdIssuedAt("now");
        dcrClientInfoResponse.setClientSecretExpiresAt("future");
        dcrClientInfoResponse.setRegistrationClientUri("https://localhost:9443/oauth/xxx-xxx-xxx-xxx");

        ////expected response object from key manager
        OAuthApplicationInfo oAuthApplicationInfoResponse = new OAuthApplicationInfo();
        oAuthApplicationInfoResponse.setClientName(dcrClientInfoResponse.getClientName());
        oAuthApplicationInfoResponse.setGrantTypes(dcrClientInfoResponse.getGrantTypes());
        oAuthApplicationInfoResponse.setCallBackURL(dcrClientInfoResponse.getRedirectURIs().get(0));
        oAuthApplicationInfoResponse.setClientId(dcrClientInfoResponse.getClientId());
        oAuthApplicationInfoResponse.setClientSecret(dcrClientInfoResponse.getClientSecret());

        Response dcrResponse = Response.builder()
                .status(201)
                .headers(new HashMap<>())
                .body(new Gson().toJson(dcrClientInfoResponse), feign.Util.UTF_8)
                .build();
        Mockito.when(dcrmServiceStub.registerApplication(dcrClientInfo)).thenReturn(dcrResponse);

        try {
            OAuthApplicationInfo app = kmImpl.createApplication(oauthAppRequest);
            Assert.assertEquals(app, oAuthApplicationInfoResponse);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error case - 400
        int errorSc = 400;
        String errorMsg = "{\"error\": \"invalid_redirect_uri\", \"error_description\": \"One or more " +
                "redirect_uri values are invalid\"}";
        Response errorResponse = Response.builder().status(errorSc).headers(new HashMap<>())
                .body(errorMsg.getBytes()).build();
        Mockito.when(dcrmServiceStub.registerApplication(any(DCRClientInfo.class))).thenReturn(errorResponse);

        try {
            kmImpl.createApplication(oauthAppRequest);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while DCR application creation."));
        }

        //error case - non-400
        errorSc = 500;
        errorMsg = "unknown error occurred";
        errorResponse = Response.builder().status(errorSc).headers(new HashMap<>()).body(errorMsg.getBytes()).build();
        Mockito.when(dcrmServiceStub.registerApplication(any(DCRClientInfo.class))).thenReturn(errorResponse);

        try {
            kmImpl.createApplication(oauthAppRequest);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while DCR application creation."));
        }
    }

    @Test
    public void testUpdateApplication() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);
        final String consumerKey = "xxx-xxx-xxx-xxx";

        //happy path - 200
        ////request object to key manager
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientName("app1");
        List<String> grantTypesList = new ArrayList<>();
        grantTypesList.add("password");
        grantTypesList.add("client-credentials");
        oAuthApplicationInfo.setGrantTypes(grantTypesList);
        oAuthApplicationInfo.setCallBackURL("https://sample.callback/url");
        oAuthApplicationInfo.setClientId(consumerKey);
        oAuthApplicationInfo.setClientSecret("yyy-yyy-yyy-yyy");

        ////request object to dcr api
        DCRClientInfo dcrClientInfo = new DCRClientInfo();
        dcrClientInfo.setClientName(oAuthApplicationInfo.getClientName());
        dcrClientInfo.setGrantTypes(oAuthApplicationInfo.getGrantTypes());
        dcrClientInfo.addCallbackUrl(oAuthApplicationInfo.getCallBackURL());
/*
        dcrClientInfo.setUserinfoSignedResponseAlg(ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                .getKeyManagerConfigs().getOidcUserinfoJWTSigningAlgo());
*/
        dcrClientInfo.setClientId(oAuthApplicationInfo.getClientId());
        dcrClientInfo.setClientSecret(oAuthApplicationInfo.getClientSecret());

        ////mocked response object from dcr api
        DCRClientInfo dcrClientInfoResponse = new DCRClientInfo();
        dcrClientInfoResponse.setClientName(oAuthApplicationInfo.getClientName());
        dcrClientInfoResponse.setGrantTypes(oAuthApplicationInfo.getGrantTypes());
        dcrClientInfoResponse.addCallbackUrl(oAuthApplicationInfo.getCallBackURL());
        dcrClientInfoResponse.setClientId(consumerKey);
        dcrClientInfoResponse.setClientSecret("yyy-yyy-yyy-yyy");
        dcrClientInfoResponse.setClientIdIssuedAt("now");
        dcrClientInfoResponse.setClientSecretExpiresAt("future");
        dcrClientInfoResponse.setRegistrationClientUri("https://localhost:9443/oauth/xxx-xxx-xxx-xxx");

        ////expected response object from key manager
        OAuthApplicationInfo oAuthApplicationInfoResponse = new OAuthApplicationInfo();
        oAuthApplicationInfoResponse.setClientName(dcrClientInfoResponse.getClientName());
        oAuthApplicationInfoResponse.setGrantTypes(dcrClientInfoResponse.getGrantTypes());
        oAuthApplicationInfoResponse.setCallBackURL(dcrClientInfoResponse.getRedirectURIs().get(0));
        oAuthApplicationInfoResponse.setClientId(dcrClientInfoResponse.getClientId());
        oAuthApplicationInfoResponse.setClientSecret(dcrClientInfoResponse.getClientSecret());

        Response dcrResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(dcrClientInfoResponse), feign.Util.UTF_8)
                .build();
        Mockito.when(dcrmServiceStub.updateApplication(dcrClientInfo, consumerKey)).thenReturn(dcrResponse);

        try {
            OAuthApplicationInfo app = kmImpl.updateApplication(oAuthApplicationInfo);
            Assert.assertEquals(app, oAuthApplicationInfoResponse);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error case - 400
        int errorSc = 400;
        String errorMsg = "{\"error\": \"invalid_redirect_uri\", \"error_description\": \"One or more " +
                "redirect_uri values are invalid\"}";
        Response errorResponse = Response.builder().status(errorSc).headers(new HashMap<>())
                .body(errorMsg.getBytes()).build();
        Mockito.when(dcrmServiceStub.updateApplication(dcrClientInfo, consumerKey)).thenReturn(errorResponse);

        try {
            kmImpl.updateApplication(oAuthApplicationInfo);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while updating DCR application."));
        }

        //error case - non-400
        errorSc = 500;
        errorMsg = "unknown error occurred";
        errorResponse = Response.builder().status(errorSc).headers(new HashMap<>()).body(errorMsg.getBytes()).build();
        Mockito.when(dcrmServiceStub.updateApplication(dcrClientInfo, consumerKey)).thenReturn(errorResponse);

        try {
            kmImpl.updateApplication(oAuthApplicationInfo);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while updating DCR application."));
        }
    }

    @Test
    public void testDeleteApplication() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);
        final String consumerKey = "xxx-xxx-xxx-xxx";

        //happy path - 204
        Response okResponse = Response.builder().status(204).headers(new HashMap<>()).build();
        Mockito.when(dcrmServiceStub.deleteApplication(consumerKey)).thenReturn(okResponse);

        try {
            kmImpl.deleteApplication(consumerKey);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error case - empty consumer key
        try {
            kmImpl.deleteApplication("");
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().equals("Unable to delete OAuth Application. Consumer Key is null " +
                    "or empty"));
        }

        //error case - empty consumer null
        try {
            kmImpl.deleteApplication(null);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().equals("Unable to delete OAuth Application. Consumer Key is null " +
                    "or empty"));
        }

        //error case - non-204
        String errorMsg = "unknown error occurred";
        Response errorResponse = Response.builder().status(500).headers(new HashMap<>()).body(errorMsg.getBytes())
                .build();
        Mockito.when(dcrmServiceStub.deleteApplication(consumerKey)).thenReturn(errorResponse);

        try {
            kmImpl.deleteApplication(consumerKey);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while deleting DCR application."));
        }

    }

    @Test
    public void testRetrieveApplication() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);

        //happy path - 200
        ////mocked response object from dcr api
        DCRClientInfo dcrClientInfoResponse = new DCRClientInfo();
        dcrClientInfoResponse.setClientName("appx");
        List<String> grantTypesList = new ArrayList<>();
        grantTypesList.add("password");
        grantTypesList.add("client-credentials");
        dcrClientInfoResponse.setGrantTypes(grantTypesList);
        dcrClientInfoResponse.addCallbackUrl("https://sample.callback/url");
        dcrClientInfoResponse.setClientId(consumerKey);
        dcrClientInfoResponse.setClientSecret(consumerSecret);
        dcrClientInfoResponse.setClientIdIssuedAt("now");
        dcrClientInfoResponse.setClientSecretExpiresAt("future");
        dcrClientInfoResponse.setRegistrationClientUri("https://localhost:9443/oauth/xxx-xxx-xxx-xxx");

        ////expected response object from key manager
        OAuthApplicationInfo oAuthApplicationInfoResponse = new OAuthApplicationInfo();
        oAuthApplicationInfoResponse.setClientName(dcrClientInfoResponse.getClientName());
        oAuthApplicationInfoResponse.setGrantTypes(dcrClientInfoResponse.getGrantTypes());
        oAuthApplicationInfoResponse.setCallBackURL(dcrClientInfoResponse.getRedirectURIs().get(0));
        oAuthApplicationInfoResponse.setClientId(dcrClientInfoResponse.getClientId());
        oAuthApplicationInfoResponse.setClientSecret(dcrClientInfoResponse.getClientSecret());

        Response appGetResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(dcrClientInfoResponse), feign.Util.UTF_8)
                .build();
        Mockito.when(dcrmServiceStub.getApplication(consumerKey)).thenReturn(appGetResponse);

        try {
            OAuthApplicationInfo app = kmImpl.retrieveApplication(consumerKey);
            Assert.assertEquals(app, oAuthApplicationInfoResponse);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error case - empty consumer key
        try {
            kmImpl.retrieveApplication("");
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().equals("Unable to retrieve OAuth Application. Consumer Key is null " +
                    "or empty"));
        }

        //error case - empty consumer null
        try {
            kmImpl.retrieveApplication(null);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().equals("Unable to retrieve OAuth Application. Consumer Key is null " +
                    "or empty"));
        }

        //error case - backend error
        String errorMsg = "unknown error occurred";
        Response errorResponse = Response.builder().status(500).headers(new HashMap<>()).body(errorMsg.getBytes())
                .build();
        Mockito.when(dcrmServiceStub.getApplication(consumerKey)).thenReturn(errorResponse);

        try {
            kmImpl.retrieveApplication(consumerKey);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while retrieving DCR application."));
        }
    }

    @Test
    public void testGetNewAccessTokenByPasswordGrant() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        OAuth2ServiceStubs.TokenServiceStub tokenStub = Mockito.mock(OAuth2ServiceStubs.TokenServiceStub.class);
        OAuth2ServiceStubs.RevokeServiceStub revokeStub = Mockito.mock(OAuth2ServiceStubs.RevokeServiceStub.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);

        //happy path - 200 - password grant type
        ////request to key manager
        AccessTokenRequest tokenRequest = createKeyManagerTokenRequest(consumerKey, consumerSecret,
                KeyManagerConstants.PASSWORD_GRANT_TYPE, "user1", "pass1", "xxx-old-token-xxx", 7200L,
                null, null, null, null);

        ////mocked response from /token service
        OAuth2TokenInfo oAuth2TokenInfo = createTokenServiceResponse(tokenRequest);

        ////expected response from key manager
        AccessTokenInfo accessTokenInfo = createExpectedKeyManagerResponse(oAuth2TokenInfo);

        Response revokeTokenResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(oAuth2TokenInfo), feign.Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getRevokeServiceStub()).thenReturn(revokeStub);
        Mockito.when(revokeStub.revokeAccessToken(tokenRequest.getTokenToRevoke(),
                tokenRequest.getClientId(),
                tokenRequest.getClientSecret()))
                .thenReturn(revokeTokenResponse);

        Response newTokenResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(oAuth2TokenInfo), feign.Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub()).thenReturn(tokenStub);
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub().generatePasswordGrantAccessToken(
                tokenRequest.getResourceOwnerUsername(),
                tokenRequest.getResourceOwnerPassword(), tokenRequest.getScopes(),
                tokenRequest.getValidityPeriod(), tokenRequest.getClientId(),
                tokenRequest.getClientSecret()))
                .thenReturn(newTokenResponse);

        try {
            AccessTokenInfo newToken = kmImpl.getNewAccessToken(tokenRequest);
            Assert.assertEquals(newToken, accessTokenInfo);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testGetNewAccessTokenByRefreshGrant() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        OAuth2ServiceStubs.TokenServiceStub tokenStub = Mockito.mock(OAuth2ServiceStubs.TokenServiceStub.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);

        //happy path - 200 - refresh grant type
        ////request to key manager
        AccessTokenRequest tokenRequest = createKeyManagerTokenRequest(consumerKey, consumerSecret,
                KeyManagerConstants.REFRESH_GRANT_TYPE, null, null, null, -1L,
                null, null, "xxx-refresh-token-xxx", null);

        ////mocked response from /token service
        OAuth2TokenInfo oAuth2TokenInfo = createTokenServiceResponse(tokenRequest);

        ////expected response from key manager
        AccessTokenInfo accessTokenInfo = createExpectedKeyManagerResponse(oAuth2TokenInfo);

        Response newTokenResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(oAuth2TokenInfo), Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub()).thenReturn(tokenStub);
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub().generateRefreshGrantAccessToken(
                tokenRequest.getRefreshToken(), tokenRequest.getScopes(), -1L,
                tokenRequest.getClientId(), tokenRequest.getClientSecret()))
                .thenReturn(newTokenResponse);

        try {
            AccessTokenInfo newToken = kmImpl.getNewAccessToken(tokenRequest);
            Assert.assertEquals(newToken, accessTokenInfo);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testGetNewAccessTokenByClientCredentialsGrant() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        OAuth2ServiceStubs.TokenServiceStub tokenStub = Mockito.mock(OAuth2ServiceStubs.TokenServiceStub.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);

        //happy path - 200 - client credentials grant type
        ////request to key manager
        AccessTokenRequest tokenRequest = createKeyManagerTokenRequest(consumerKey, consumerSecret,
                KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE, null, null, null, -2L, null, null, null, null);

        ////mocked response from /token service
        OAuth2TokenInfo oAuth2TokenInfo = createTokenServiceResponse(tokenRequest);

        ////expected response from key manager
        AccessTokenInfo accessTokenInfo = createExpectedKeyManagerResponse(oAuth2TokenInfo);

        Response newTokenResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(oAuth2TokenInfo), Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub()).thenReturn(tokenStub);
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub().generateClientCredentialsGrantAccessToken(
                tokenRequest.getScopes(), tokenRequest.getValidityPeriod(), tokenRequest.getClientId(),
                tokenRequest.getClientSecret()))
                .thenReturn(newTokenResponse);

        try {
            AccessTokenInfo newToken = kmImpl.getNewAccessToken(tokenRequest);
            Assert.assertEquals(newToken, accessTokenInfo);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testGetNewAccessTokenByAuthorizationCodeGrant() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        OAuth2ServiceStubs.TokenServiceStub tokenStub = Mockito.mock(OAuth2ServiceStubs.TokenServiceStub.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);

        //happy path - 200 - authorization code grant type
        ////request to key manager
        AccessTokenRequest tokenRequest = createKeyManagerTokenRequest(consumerKey, consumerSecret,
                KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE, null, null, null, -2L,
                "xxx-auth-code-xxx", "http://test.callback/url", null, null);

        ////mocked response from /token service
        OAuth2TokenInfo oAuth2TokenInfo = createTokenServiceResponse(tokenRequest);

        ////expected response from key manager
        AccessTokenInfo accessTokenInfo = createExpectedKeyManagerResponse(oAuth2TokenInfo);

        Response newTokenResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(oAuth2TokenInfo), Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub()).thenReturn(tokenStub);
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub().generateAuthCodeGrantAccessToken(
                tokenRequest.getAuthorizationCode(),
                tokenRequest.getCallbackURI(), tokenRequest.getScopes(),
                tokenRequest.getValidityPeriod(), tokenRequest.getClientId(),
                tokenRequest.getClientSecret()))
                .thenReturn(newTokenResponse);

        try {
            AccessTokenInfo newToken = kmImpl.getNewAccessToken(tokenRequest);
            Assert.assertEquals(newToken, accessTokenInfo);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testGetNewAccessTokenByJWTGrant() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        OAuth2ServiceStubs.TokenServiceStub tokenStub = Mockito.mock(OAuth2ServiceStubs.TokenServiceStub.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);

        //happy path - 200 - JWT grant type
        ////request to key manager
        AccessTokenRequest tokenRequest = createKeyManagerTokenRequest(consumerKey, consumerSecret,
                KeyManagerConstants.JWT_GRANT_TYPE, null, null, null, -2L,
                null, null, null, "xxx-assertion-xxx");

        ////mocked response from /token service
        OAuth2TokenInfo oAuth2TokenInfo = createTokenServiceResponse(tokenRequest);

        ////expected response from key manager
        AccessTokenInfo accessTokenInfo = createExpectedKeyManagerResponse(oAuth2TokenInfo);

        Response newTokenResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(oAuth2TokenInfo), Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub()).thenReturn(tokenStub);
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub().generateJWTGrantAccessToken(
                tokenRequest.getAssertion(),
                tokenRequest.getGrantType(), tokenRequest.getScopes(),
                tokenRequest.getValidityPeriod(), tokenRequest.getClientId(),
                tokenRequest.getClientSecret()))
                .thenReturn(newTokenResponse);

        try {
            AccessTokenInfo newToken = kmImpl.getNewAccessToken(tokenRequest);
            Assert.assertEquals(newToken, accessTokenInfo);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testGetNewAccessTokenErrorCases() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        OAuth2ServiceStubs.TokenServiceStub tokenStub = Mockito.mock(OAuth2ServiceStubs.TokenServiceStub.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);

        //error case - tokenRequest is null
        try {
            kmImpl.getNewAccessToken(null);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().equals("No information available to generate Token. " +
                    "AccessTokenRequest is null"));
        }

        //error case - invalid grant type
        final String invalidGrantType = "invalid_grant";
        AccessTokenRequest tokenRequest = createKeyManagerTokenRequest(consumerKey, consumerSecret,
                invalidGrantType, null, null, null, -2L, null, null, null, null);
        try {
            kmImpl.getNewAccessToken(tokenRequest);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid access token request. Unsupported grant type: " +
                    invalidGrantType));
        }

        //error case - response is null (mock condition (validity period) is different)
        tokenRequest = createKeyManagerTokenRequest(consumerKey, consumerSecret,
                KeyManagerConstants.REFRESH_GRANT_TYPE, null, null, null, -1L,
                null, null, "xxx-refresh-token-xxx", null);

        Mockito.when(oAuth2ServiceStub.getTokenServiceStub()).thenReturn(tokenStub);
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub().generateRefreshGrantAccessToken(
                tokenRequest.getRefreshToken(), tokenRequest.getScopes(), tokenRequest.getValidityPeriod(),
                tokenRequest.getClientId(), tokenRequest.getClientSecret()))
                .thenReturn(null);
        try {
            kmImpl.getNewAccessToken(tokenRequest);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().equals("Error occurred while generating an access token. " +
                    "Response is null"));
        }

        //error case - token response non-200
        ////request to key manager
        tokenRequest = createKeyManagerTokenRequest(consumerKey, consumerSecret,
                KeyManagerConstants.REFRESH_GRANT_TYPE, null, null, null, 7200L,
                null, null, "xxx-refresh-token-xxx", null);

        final int errorCode = 500;
        Response errorResponse = Response.builder()
                .status(errorCode)
                .headers(new HashMap<>())
                .body("backend error occurred", Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub()).thenReturn(tokenStub);
        Mockito.when(oAuth2ServiceStub.getTokenServiceStub().generateRefreshGrantAccessToken(
                tokenRequest.getRefreshToken(), tokenRequest.getScopes(), tokenRequest.getValidityPeriod(),
                tokenRequest.getClientId(), tokenRequest.getClientSecret()))
                .thenReturn(errorResponse);
        try {
            kmImpl.getNewAccessToken(tokenRequest);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Token generation request failed. HTTP error code: "
                    + errorCode));
        }
    }

    @Test
    public void testGetTokenMetaData() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        OAuth2ServiceStubs.IntrospectionServiceStub introspectionStub = Mockito.mock(
                OAuth2ServiceStubs.IntrospectionServiceStub.class);
        Mockito.when(oAuth2ServiceStub.getIntrospectionServiceStub()).thenReturn(introspectionStub);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);
        final String accessToken = "aaa-aaa-aaa-aaa";

        //happy path - 200 - token is active
        ////mocked response from /introspect service
        OAuth2IntrospectionResponse introspectionResponse = new OAuth2IntrospectionResponse();
        introspectionResponse.setActive(true);
        introspectionResponse.setClientId(consumerKey);

        ////expected response from key manager
        AccessTokenInfo expectedTokenInfo = new AccessTokenInfo();
        expectedTokenInfo.setTokenValid(introspectionResponse.isActive());
        expectedTokenInfo.setAccessToken(accessToken);
        expectedTokenInfo.setConsumerKey(introspectionResponse.getClientId());

        Response introspectResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(introspectionResponse), feign.Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getIntrospectionServiceStub()).thenReturn(introspectionStub);
        Mockito.when(introspectionStub.introspectToken(accessToken)).thenReturn(introspectResponse);

        try {
            AccessTokenInfo tokenMetaData = kmImpl.getTokenMetaData(accessToken);
            Assert.assertEquals(tokenMetaData, expectedTokenInfo);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //happy path - 200 - token is not active
        ////mocked response from /introspect service
        introspectionResponse = new OAuth2IntrospectionResponse();
        introspectionResponse.setActive(false);
        introspectionResponse.setClientId(consumerKey);

        ////expected response from key manager
        expectedTokenInfo = new AccessTokenInfo();
        expectedTokenInfo.setTokenValid(introspectionResponse.isActive());
        expectedTokenInfo.setErrorCode(KeyManagerConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);

        introspectResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .body(new Gson().toJson(introspectionResponse), feign.Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getIntrospectionServiceStub()).thenReturn(introspectionStub);
        Mockito.when(introspectionStub.introspectToken(accessToken)).thenReturn(introspectResponse);

        try {
            AccessTokenInfo tokenMetaData = kmImpl.getTokenMetaData(accessToken);
            Assert.assertEquals(tokenMetaData, expectedTokenInfo);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error case - response is null
        Mockito.when(introspectionStub.introspectToken(accessToken)).thenReturn(null);
        try {
            kmImpl.getTokenMetaData(accessToken);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Error occurred while introspecting access token. " +
                    "Response is null"));
        }

        //error case - token response non-200
        ////request to key manager
        final int errorCode = 500;
        introspectResponse = Response.builder()
                .status(errorCode)
                .headers(new HashMap<>())
                .body("backend error occurred", Util.UTF_8)
                .build();
        Mockito.when(introspectionStub.introspectToken(accessToken)).thenReturn(introspectResponse);
        try {
            kmImpl.getTokenMetaData(accessToken);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Token introspection request failed. HTTP error code: "
                    + errorCode));
        }
    }

    //TODO:Enable after revoke endpoint implementation done in key manager.
    @Test(enabled = false)
    public void testRevokeToken() throws Exception {
        DCRMServiceStub dcrmServiceStub = Mockito.mock(DCRMServiceStub.class);
        OAuth2ServiceStubs oAuth2ServiceStub = Mockito.mock(OAuth2ServiceStubs.class);
        OAuth2ServiceStubs.RevokeServiceStub revokeStub = Mockito.mock(OAuth2ServiceStubs.RevokeServiceStub.class);
        ScopeRegistration scopeRegistration = Mockito.mock
                (ScopeRegistration.class);
        DefaultKeyManagerImpl kmImpl = new DefaultKeyManagerImpl(dcrmServiceStub, oAuth2ServiceStub,
                scopeRegistration);

        //happy path - 200
        Response revokeTokenResponse = Response.builder()
                .status(200)
                .headers(new HashMap<>())
                .build();
        Mockito.when(oAuth2ServiceStub.getRevokeServiceStub()).thenReturn(revokeStub);
        final String revokeToken = "xxx-revoke-token-xxx";
        Mockito.when(revokeStub.revokeAccessToken(revokeToken, consumerKey, consumerSecret))
                .thenReturn(revokeTokenResponse);

        try {
            kmImpl.revokeAccessToken(revokeToken, consumerKey, consumerSecret);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        //error case - response is null
        Mockito.when(oAuth2ServiceStub.getRevokeServiceStub()).thenReturn(revokeStub);
        Mockito.when(revokeStub.revokeAccessToken(revokeToken, consumerKey, consumerSecret)).thenReturn(null);
        try {
            kmImpl.revokeAccessToken(revokeToken, consumerKey, consumerSecret);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().equals("Error occurred while revoking current access token. " +
                    "Response is null"));
        }

        //error case - token response non-200
        final int errorCode = 500;
        Response errorResponse = Response.builder()
                .status(errorCode)
                .headers(new HashMap<>())
                .body("backend error occurred", Util.UTF_8)
                .build();
        Mockito.when(oAuth2ServiceStub.getRevokeServiceStub()).thenReturn(revokeStub);
        Mockito.when(revokeStub.revokeAccessToken(revokeToken, consumerKey, consumerSecret)).thenReturn(errorResponse);
        try {
            kmImpl.revokeAccessToken(revokeToken, consumerKey, consumerSecret);
            Assert.fail("Exception was expected, but wasn't thrown");
        } catch (KeyManagementException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Token revocation failed. HTTP error code: " + errorCode));
        }
    }


    private AccessTokenInfo createExpectedKeyManagerResponse(OAuth2TokenInfo oAuth2TokenInfo) {
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setAccessToken(oAuth2TokenInfo.getAccessToken());
        accessTokenInfo.setScopes(oAuth2TokenInfo.getScope());
        accessTokenInfo.setRefreshToken(oAuth2TokenInfo.getRefreshToken());
        accessTokenInfo.setIdToken(oAuth2TokenInfo.getIdToken());
        accessTokenInfo.setValidityPeriod(oAuth2TokenInfo.getExpiresIn());
        return accessTokenInfo;
    }

    private OAuth2TokenInfo createTokenServiceResponse(AccessTokenRequest tokenRequestForPasswordGrant) {
        OAuth2TokenInfo oAuth2TokenInfo = new OAuth2TokenInfo();
        oAuth2TokenInfo.setAccessToken("xxx-new-token-xxx");
        oAuth2TokenInfo.setRefreshToken("xxx-new-refresh-xxx");
        oAuth2TokenInfo.setIdToken("wdewedwedwedwedwedwedvcdvflkdnjlkjbkjhbvskjhbcdkjsabcdkjsavdcsacdascdsadcasdca");
        oAuth2TokenInfo.setTokenType("Bearer");
        oAuth2TokenInfo.setExpiresIn(tokenRequestForPasswordGrant.getValidityPeriod());
        oAuth2TokenInfo.setScope(tokenRequestForPasswordGrant.getScopes());
        return oAuth2TokenInfo;
    }

    private AccessTokenRequest createKeyManagerTokenRequest(String consumerKey, String consumerSecret,
                                                            String grantType, String username, String password,
                                                            String revokeToken, long validityPeriod,
                                                            String code, String callbackUrl, String refreshToken,
                                                            String assertion) {
        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setClientId(consumerKey);
        tokenRequest.setClientSecret(consumerSecret);
        tokenRequest.setGrantType(grantType);
        tokenRequest.setResourceOwnerUsername(username);
        tokenRequest.setResourceOwnerPassword(password);
        tokenRequest.setTokenToRevoke(revokeToken);
        tokenRequest.setValidityPeriod(validityPeriod);
        tokenRequest.setAuthorizationCode(code);
        tokenRequest.setCallbackURI(callbackUrl);
        tokenRequest.setRefreshToken(refreshToken);
        tokenRequest.setAssertion(assertion);
        tokenRequest.setScopes("openid apim:view_api");
        return tokenRequest;
    }
}
