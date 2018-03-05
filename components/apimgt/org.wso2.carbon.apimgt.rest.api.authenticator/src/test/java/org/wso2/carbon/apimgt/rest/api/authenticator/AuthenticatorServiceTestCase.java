/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.configuration.models.EnvironmentConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.MultiEnvironmentOverview;
import org.wso2.carbon.apimgt.core.dao.SystemApplicationDao;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.APIMAppConfigurationService;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMAppConfigurations;
import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.bean.AuthResponseBean;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.NewCookie;

/**
 * Test class for AuthenticatorService.
 */
public class AuthenticatorServiceTestCase {
    @Test
    public void testGetAuthenticationConfigurations() throws Exception {
        // Happy Path - 200
        //// Mocked response object from DCR api
        SystemApplicationDao systemApplicationDao = Mockito.mock(SystemApplicationDao.class);
        Mockito.when(systemApplicationDao.isConsumerKeyExistForApplication("store")).thenReturn(false);

        APIMConfigurationService apimConfigurationService = Mockito.mock(APIMConfigurationService.class);
        EnvironmentConfigurations environmentConfigurations = new EnvironmentConfigurations();
        Mockito.when(apimConfigurationService.getEnvironmentConfigurations()).thenReturn(environmentConfigurations);

        APIMAppConfigurationService apimAppConfigurationService = Mockito.mock(APIMAppConfigurationService.class);
        APIMAppConfigurations apimAppConfigurations = new APIMAppConfigurations();
        Mockito.when(apimAppConfigurationService.getApimAppConfigurations()).thenReturn(apimAppConfigurations);

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId("xxx-client-id-xxx");
        oAuthApplicationInfo.setCallBackURL("https://localhost/9292/login/callback/store");

        //// Expected data object to be passed to the front-end
        JsonObject oAuthData = new JsonObject();
        String scopes = "apim:self-signup apim:dedicated_gateway apim:subscribe openid";
        oAuthData.addProperty(KeyManagerConstants.OAUTH_CLIENT_ID, oAuthApplicationInfo.getClientId());
        oAuthData.addProperty(KeyManagerConstants.OAUTH_CALLBACK_URIS, oAuthApplicationInfo.getCallBackURL());
        oAuthData.addProperty(KeyManagerConstants.TOKEN_SCOPES, scopes);
        oAuthData.addProperty(KeyManagerConstants.AUTHORIZATION_ENDPOINT, "https://localhost:9080/oauth2/authorize");
        oAuthData.addProperty(AuthenticatorConstants.SSO_ENABLED, ServiceReferenceHolder.getInstance()
                .getAPIMAppConfiguration().isSsoEnabled());
        oAuthData.addProperty(AuthenticatorConstants.MULTI_ENVIRONMENT_OVERVIEW_ENABLED, APIMConfigurationService.getInstance()
                .getEnvironmentConfigurations().getMultiEnvironmentOverview().isEnabled());
        MultiEnvironmentOverview multiEnvironmentOverview = new MultiEnvironmentOverview();
        environmentConfigurations.setMultiEnvironmentOverview(multiEnvironmentOverview);
        KeyManager keyManager = Mockito.mock(KeyManager.class);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager, systemApplicationDao,
                apimConfigurationService, apimAppConfigurationService);

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
        Mockito.when(keyManager.createApplication(Mockito.any())).thenThrow(KeyManagementException.class);
        try {
            authenticatorService.getAuthenticationConfigurations("store");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error while creating the keys for OAuth application : store");
        }
    }

    @Test
    public void testGetAuthenticationConfigurationsForPublisher() throws Exception {
        // Happy Path - 200
        //// Mocked response object from DCR api
        SystemApplicationDao systemApplicationDao = Mockito.mock(SystemApplicationDao.class);
        Mockito.when(systemApplicationDao.isConsumerKeyExistForApplication("store")).thenReturn(false);

        APIMConfigurationService apimConfigurationService = Mockito.mock(APIMConfigurationService.class);
        EnvironmentConfigurations environmentConfigurations = new EnvironmentConfigurations();
        Mockito.when(apimConfigurationService.getEnvironmentConfigurations()).thenReturn(environmentConfigurations);

        APIMAppConfigurationService apimAppConfigurationService = Mockito.mock(APIMAppConfigurationService.class);
        APIMAppConfigurations apimAppConfigurations = new APIMAppConfigurations();
        Mockito.when(apimAppConfigurationService.getApimAppConfigurations()).thenReturn(apimAppConfigurations);

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId("xxx-client-id-xxx");
        oAuthApplicationInfo.setCallBackURL("https://localhost:9292/login/callback/publisher");

        //// Expected data object to be passed to the front-end
        JsonObject oAuthData = new JsonObject();
        String scopes = "apim:api_view apim:api_create apim:api_update apim:api_delete apim:apidef_update "
                + "apim:api_publish apim:subscription_view apim:subscription_block openid "
                + "apim:external_services_discover apim:dedicated_gateway";
        oAuthData.addProperty(KeyManagerConstants.OAUTH_CLIENT_ID, oAuthApplicationInfo.getClientId());
        oAuthData.addProperty(KeyManagerConstants.OAUTH_CALLBACK_URIS, oAuthApplicationInfo.getCallBackURL());
        oAuthData.addProperty(KeyManagerConstants.TOKEN_SCOPES, scopes);
        oAuthData.addProperty(KeyManagerConstants.AUTHORIZATION_ENDPOINT, "https://localhost:9443/oauth2/authorize");
        oAuthData.addProperty(AuthenticatorConstants.SSO_ENABLED,
                ServiceReferenceHolder.getInstance().getAPIMAppConfiguration().isSsoEnabled());

        KeyManager keyManager = Mockito.mock(KeyManager.class);
        MultiEnvironmentOverview multiEnvironmentOverview = new MultiEnvironmentOverview();
        environmentConfigurations.setMultiEnvironmentOverview(multiEnvironmentOverview);
        multiEnvironmentOverview.setEnabled(true);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager, systemApplicationDao,
                apimConfigurationService, apimAppConfigurationService);

        //// Get data object to be passed to the front-end
        Mockito.when(keyManager.createApplication(Mockito.any())).thenReturn(oAuthApplicationInfo);
        JsonObject responseOAuthDataObj = authenticatorService.getAuthenticationConfigurations("publisher");
        String[] scopesActual = responseOAuthDataObj.get(KeyManagerConstants.TOKEN_SCOPES).toString().split(" ");
        String[] scopesExpected = oAuthData.get(KeyManagerConstants.TOKEN_SCOPES).toString().split(" ");
        Assert.assertEquals(scopesActual.length, scopesExpected.length);

        // Error Path - 500 - When OAuthApplicationInfo is null
        JsonObject emptyOAuthDataObj = new JsonObject();
        Mockito.when(keyManager.createApplication(Mockito.any())).thenReturn(null);
        JsonObject responseEmptyOAuthDataObj = authenticatorService.getAuthenticationConfigurations("publisher");
        Assert.assertEquals(responseEmptyOAuthDataObj, emptyOAuthDataObj);

        // Error Path - When DCR application creation fails and throws an APIManagementException
        Mockito.when(keyManager.createApplication(Mockito.any())).thenThrow(KeyManagementException.class);
        try {
            authenticatorService.getAuthenticationConfigurations("publisher");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error while creating the keys for OAuth application : publisher");
        }
    }

    @Test
    public void testGetTokens() throws Exception {
        // Happy Path - 200 - Authorization code grant type
        APIMConfigurationService apimConfigurationService = Mockito.mock(APIMConfigurationService.class);
        EnvironmentConfigurations environmentConfigurations = new EnvironmentConfigurations();
        Mockito.when(apimConfigurationService.getEnvironmentConfigurations()).thenReturn(environmentConfigurations);

        APIMAppConfigurationService apimAppConfigurationService = Mockito.mock(APIMAppConfigurationService.class);
        APIMAppConfigurations apimAppConfigurations = new APIMAppConfigurations();
        Mockito.when(apimAppConfigurationService.getApimAppConfigurations()).thenReturn(apimAppConfigurations);

        //// Mocked response from DCR endpoint
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId("xxx-client-id-xxx");
        oAuthApplicationInfo.setClientSecret("xxx-client-secret-xxx");

        //// Expected response object from KeyManager
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        tokenInfo.setAccessToken("xxx-access-token-xxx");
        tokenInfo.setScopes("apim:subscribe openid");
        tokenInfo.setRefreshToken("xxx-refresh-token-xxx");
        tokenInfo.setIdToken("xxx-id-token-xxx");
        tokenInfo.setValidityPeriod(-2L);

        KeyManager keyManager = Mockito.mock(KeyManager.class);
        SystemApplicationDao systemApplicationDao = Mockito.mock(SystemApplicationDao.class);
        Mockito.when(systemApplicationDao.isConsumerKeyExistForApplication("store")).thenReturn(false);
        MultiEnvironmentOverview multiEnvironmentOverview = new MultiEnvironmentOverview();
        environmentConfigurations.setMultiEnvironmentOverview(multiEnvironmentOverview);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager, systemApplicationDao,
                apimConfigurationService, apimAppConfigurationService);
        Mockito.when(keyManager.createApplication(Mockito.any())).thenReturn(oAuthApplicationInfo);

        //// Actual response - When authorization code is not null
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(tokenInfo);
        AccessTokenInfo tokenInfoResponseForValidAuthCode = authenticatorService.getTokens("store",
                "authorization_code", null, null, null, 0,
                "xxx-auth-code-xxx", null, null);
        Assert.assertEquals(tokenInfoResponseForValidAuthCode, tokenInfo);

        // Error Path - 500 - Authorization code grant type
        //// When an error occurred - Eg: Access denied
        AccessTokenInfo emptyTokenInfo = new AccessTokenInfo();
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(emptyTokenInfo);
        AccessTokenInfo tokenInfoResponseForInvalidAuthCode = new AccessTokenInfo();
        try {
            tokenInfoResponseForInvalidAuthCode = authenticatorService.getTokens("store",
                    "authorization_code", null, null, null, 0, null, null, null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "No Authorization Code available.");
            Assert.assertEquals(tokenInfoResponseForInvalidAuthCode, emptyTokenInfo);
        }

        // Happy Path - 200 - Password grant type
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(tokenInfo);
        AccessTokenInfo tokenInfoResponseForPasswordGrant = authenticatorService.getTokens("store", "password",
                "admin", "admin", null, 0, null, null, null);
        Assert.assertEquals(tokenInfoResponseForPasswordGrant, tokenInfo);

        // Error Path - When token generation fails and throws APIManagementException
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenThrow(KeyManagementException.class)
                .thenReturn(tokenInfo);
        try {
            authenticatorService.getTokens("store", "password",
                    "admin", "admin", null, 0, null, null, null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error while receiving tokens for OAuth application : store");
        }

        // Happy Path - 200 - Refresh grant type
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(tokenInfo);
        AccessTokenInfo tokenInfoResponseForRefreshGrant = authenticatorService.getTokens("store", "refresh_token",
                null, null, null, 0, null, null, null);
        Assert.assertEquals(tokenInfoResponseForPasswordGrant, tokenInfo);

        // Happy Path - 200 - JWT grant type
        // Multi-Environment Overview configuration
        multiEnvironmentOverview.setEnabled(true);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        String userFromIdentityProvider = "admin-user";
        Mockito.when(identityProvider.getIdOfUser(Mockito.anyString())).thenThrow(IdentityProviderException.class);
        Mockito.doReturn("xxx-admin-user-id-xxx").when(identityProvider).getIdOfUser(userFromIdentityProvider);
        // A valid jwt with user "admin-user"
        String idTokenWith_adminUser = "xxx+header+xxx.eyJzdWIiOiJhZG1pbi11c2VyIn0.xxx+signature+xxx";
        tokenInfo.setIdToken(idTokenWith_adminUser);
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(tokenInfo);

        AccessTokenInfo tokenInfoResponseForValidJWTGrant = authenticatorService.getTokens("store",
                "urn:ietf:params:oauth:grant-type:jwt-bearer", null, null, null, 0, null, "xxx-assertion-xxx", identityProvider);
        Assert.assertEquals(tokenInfoResponseForValidJWTGrant, tokenInfo);

        // Error Path - When invalid user in JWT Token
        // A valid jwt with user "John"
        String idTokenWith_johnUser = "xxx+header+xxx.eyJzdWIiOiJKb2huIn0.xxx+signature+xxx";
        tokenInfo.setIdToken(idTokenWith_johnUser);
        Mockito.when(keyManager.getNewAccessToken(Mockito.any())).thenReturn(tokenInfo);
        try {
            AccessTokenInfo tokenInfoResponseForInvalidJWTGrant = authenticatorService.getTokens("store",
                    "urn:ietf:params:oauth:grant-type:jwt-bearer", null, null, null, 0, null, "xxx-assertion-xxx", identityProvider);
            Assert.assertEquals(tokenInfoResponseForInvalidJWTGrant, tokenInfo);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "User John does not exists in this environment.");
        }
    }

    @Test
    public void testRevokeAccessToken() throws Exception {
    }

    @Test
    public void testGetUIServiceRedirectionURI() throws URISyntaxException, UnsupportedEncodingException {
        // Happy Path
        APIMConfigurationService apimConfigurationService = Mockito.mock(APIMConfigurationService.class);
        EnvironmentConfigurations environmentConfigurations = new EnvironmentConfigurations();
        Mockito.when(apimConfigurationService.getEnvironmentConfigurations()).thenReturn(environmentConfigurations);

        APIMAppConfigurationService apimAppConfigurationService = Mockito.mock(APIMAppConfigurationService.class);
        APIMAppConfigurations apimAppConfigurations = new APIMAppConfigurations();
        Mockito.when(apimAppConfigurationService.getApimAppConfigurations()).thenReturn(apimAppConfigurations);

        SystemApplicationDao systemApplicationDao = Mockito.mock(SystemApplicationDao.class);
        KeyManager keyManager = Mockito.mock(KeyManager.class);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager, systemApplicationDao,
                apimConfigurationService, apimAppConfigurationService);

        //// empty string for first value in allowed list
        environmentConfigurations.setAllowedHosts(Collections.singletonList(""));
        apimAppConfigurations.setApimBaseUrl("https://localhost:9443/");

        URI expectedUri = new URI("https://localhost:9443/publisher");
        URI actualUri = authenticatorService.getUIServiceRedirectionURI("publisher", null);
        Assert.assertEquals(expectedUri, actualUri);

        //// SSO callback
        AuthResponseBean authResponseBean = new AuthResponseBean();
        authResponseBean.setTokenValid(true);
        authResponseBean.setType("bearer");
        authResponseBean.setScopes("xxx-scopes-xxx");
        authResponseBean.setValidityPeriod(3449);
        authResponseBean.setAuthUser("admin");
        authResponseBean.setIdToken("xxx-id-token-xxx");
        authResponseBean.setPartialToken("xxx-partial-token-xxx");
        expectedUri = new URI("https://localhost:9443/publisher/login?user_name=admin&id_token=xxx-id-token-xxx&" +
                "partial_token=xxx-partial-token-xxx&scopes=xxx-scopes-xxx&validity_period=3449");
        actualUri = authenticatorService.getUIServiceRedirectionURI("publisher", authResponseBean);
        Assert.assertEquals(expectedUri, actualUri);

        //// non empty string for first value in allowed list
        environmentConfigurations.setAllowedHosts(Arrays.asList("localhost:9444", "localhost: 9445", "localhost:9446"));
        expectedUri = new URI("https://localhost:9444/publisher");
        actualUri = authenticatorService.getUIServiceRedirectionURI("publisher", null);
        Assert.assertEquals(expectedUri, actualUri);
    }

    @Test
    public void testSetupAccessTokenParts() {
        // Happy Path
        APIMConfigurationService apimConfigurationService = Mockito.mock(APIMConfigurationService.class);
        EnvironmentConfigurations environmentConfigurations = new EnvironmentConfigurations();
        Mockito.when(apimConfigurationService.getEnvironmentConfigurations()).thenReturn(environmentConfigurations);
        APIMAppConfigurationService apimAppConfigurationService = Mockito.mock(APIMAppConfigurationService.class);

        SystemApplicationDao systemApplicationDao = Mockito.mock(SystemApplicationDao.class);
        KeyManager keyManager = Mockito.mock(KeyManager.class);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager, systemApplicationDao,
                apimConfigurationService, apimAppConfigurationService);

        Map<String, NewCookie> cookies = new HashMap<>();
        AuthResponseBean authResponseBean = new AuthResponseBean();
        Map<String, String> contextPaths = new HashMap<>();
        contextPaths.put("APP_CONTEXT", "/publisher");
        contextPaths.put("LOGOUT_CONTEXT", "/login/logout/publisher");
        contextPaths.put("LOGIN_CONTEXT", "/login/token/publisher");
        contextPaths.put("REST_API_CONTEXT", "/api/am/publisher");
        String accessToken = "xxx-access_token_part_1-xxx-xxx-access_token_part_2-xxx-";
        environmentConfigurations.setEnvironmentLabel("Production");

        Map<String, NewCookie> expectedCookies = new HashMap<>();
        expectedCookies.put("REST_API_CONTEXT", new NewCookie("WSO2_AM_TOKEN_MSF4J_Production",
                "xxx-access_token_part_2-xxx-; path=/api/am/publisher; HttpOnly; Secure; "));
        expectedCookies.put("LOGOUT_CONTEXT", new NewCookie("WSO2_AM_TOKEN_2_Production",
                "xxx-access_token_part_2-xxx-; path=/login/logout/publisher; HttpOnly; Secure; "));
        AuthResponseBean expectedAuthResponseBean = new AuthResponseBean();
        expectedAuthResponseBean.setPartialToken("xxx-access_token_part_1-xxx-");

        authenticatorService.setupAccessTokenParts(cookies, authResponseBean, accessToken, contextPaths, false);
        Assert.assertEquals(expectedCookies, cookies);
        Assert.assertEquals(expectedAuthResponseBean.getPartialToken(), authResponseBean.getPartialToken());

        //// sso enabled
        cookies = new HashMap<>();
        authResponseBean = new AuthResponseBean();
        authResponseBean.setAuthUser("John");
        expectedCookies.put("LOGGED_IN_USER", new NewCookie("LOGGED_IN_USER_Production",
                "John; path=/publisher; Secure; "));
        authenticatorService.setupAccessTokenParts(cookies, authResponseBean, accessToken, contextPaths, true);
        Assert.assertEquals(expectedCookies, cookies);
        Assert.assertEquals(expectedAuthResponseBean.getPartialToken(), authResponseBean.getPartialToken());
    }

    @Test
    public void testSetupRefreshTokenParts() {
        // Happy Path
        APIMConfigurationService apimConfigurationService = Mockito.mock(APIMConfigurationService.class);
        EnvironmentConfigurations environmentConfigurations = new EnvironmentConfigurations();
        Mockito.when(apimConfigurationService.getEnvironmentConfigurations()).thenReturn(environmentConfigurations);
        APIMAppConfigurationService apimAppConfigurationService = Mockito.mock(APIMAppConfigurationService.class);

        SystemApplicationDao systemApplicationDao = Mockito.mock(SystemApplicationDao.class);
        KeyManager keyManager = Mockito.mock(KeyManager.class);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager, systemApplicationDao,
                apimConfigurationService, apimAppConfigurationService);

        Map<String, NewCookie> cookies = new HashMap<>();
        Map<String, String> contextPaths = new HashMap<>();
        contextPaths.put("APP_CONTEXT", "/store");
        contextPaths.put("LOGOUT_CONTEXT", "/login/logout/store");
        contextPaths.put("LOGIN_CONTEXT", "/login/token/store");
        contextPaths.put("REST_API_CONTEXT", "/api/am/store");
        String refreshToken = "xxx-refresh_token_part_1-xxx-xxx-refresh_token_part_2-xxx-";
        environmentConfigurations.setEnvironmentLabel("Development");

        Map<String, NewCookie> expectedCookies = new HashMap<>();
        expectedCookies.put("APP_CONTEXT", new NewCookie("WSO2_AM_REFRESH_TOKEN_1_Development",
                "xxx-refresh_token_part_1-xxx-; path=/store; Secure; "));
        expectedCookies.put("LOGIN_CONTEXT", new NewCookie("WSO2_AM_REFRESH_TOKEN_2_Development",
                "xxx-refresh_token_part_2-xxx-; path=/login/token/store; HttpOnly; Secure; "));

        authenticatorService.setupRefreshTokenParts(cookies, refreshToken, contextPaths);
        Assert.assertEquals(expectedCookies, cookies);
    }

    @Test
    public void testSetAccessTokenData() throws Exception {
        // Happy Path
        APIMConfigurationService apimConfigurationService = Mockito.mock(APIMConfigurationService.class);
        EnvironmentConfigurations environmentConfigurations = new EnvironmentConfigurations();
        Mockito.when(apimConfigurationService.getEnvironmentConfigurations()).thenReturn(environmentConfigurations);

        APIMAppConfigurationService apimAppConfigurationService = Mockito.mock(APIMAppConfigurationService.class);
        APIMAppConfigurations apimAppConfigurations = new APIMAppConfigurations();
        Mockito.when(apimAppConfigurationService.getApimAppConfigurations()).thenReturn(apimAppConfigurations);

        //// AccessTokenInfo object
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setIdToken
                ("eyJ4NXQiOiJObUptT0dVeE16WmxZak0yWkRSaE5UWmxZVEExWXpkaFpUUmlPV0UwTldJMk0ySm1PVGMxWkEiLCJraWQiOiJkMGVjNTE0YTMyYjZmODhjMGFiZDEyYTI4NDA2OTliZGQzZGViYTlkIiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoiWGg3bFZpSDZDS2pZLXRIT09JaWN5QSIsInN1YiI6ImFkbWluIiwiYXVkIjpbInR6NlJGQnhzdV93Z0RCd3FyUThvVmo3d25FTWEiXSwiYXpwIjoidHo2UkZCeHN1X3dnREJ3cXJROG9Wajd3bkVNYSIsImF1dGhfdGltZSI6MTUwMTczMzQ1NiwiaXNzIjoiaHR0cHM6XC9cL2xvY2FsaG9zdDo5NDQzXC9vYXV0aDJcL3Rva2VuIiwiZXhwIjoxNTAxNzM3MDU3LCJpYXQiOjE1MDE3MzM0NTd9.XXX-XXX");
        accessTokenInfo.setValidityPeriod(-2L);
        accessTokenInfo.setScopes("apim:subscribe openid");

        //// Expected AuthResponseBean object
        AuthResponseBean expectedAuthResponseBean = new AuthResponseBean();
        expectedAuthResponseBean.setTokenValid(true);
        expectedAuthResponseBean.setAuthUser("admin");
        expectedAuthResponseBean.setScopes(accessTokenInfo.getScopes());
        expectedAuthResponseBean.setType(AuthenticatorConstants.BEARER_PREFIX);
        expectedAuthResponseBean.setValidityPeriod(accessTokenInfo.getValidityPeriod());
        expectedAuthResponseBean.setIdToken(accessTokenInfo.getIdToken());

        KeyManager keyManager = Mockito.mock(KeyManager.class);
        SystemApplicationDao systemApplicationDao = Mockito.mock(SystemApplicationDao.class);
        Mockito.when(systemApplicationDao.isConsumerKeyExistForApplication("store")).thenReturn(false);
        MultiEnvironmentOverview multiEnvironmentOverview = new MultiEnvironmentOverview();
        environmentConfigurations.setMultiEnvironmentOverview(multiEnvironmentOverview);
        AuthenticatorService authenticatorService = new AuthenticatorService(keyManager, systemApplicationDao,
                apimConfigurationService, apimAppConfigurationService);

        //// Actual response
        AuthResponseBean authResponseBean = authenticatorService.getResponseBeanFromTokenInfo(accessTokenInfo);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expectedAuthResponseBean, authResponseBean));

        // Happy Path - When id token is null
        //// AccessTokenInfo object with null id token
        AccessTokenInfo invalidTokenInfo = new AccessTokenInfo();
        invalidTokenInfo.setValidityPeriod(-2L);
        invalidTokenInfo.setScopes("apim:subscribe openid");

        //// Expected AuthResponseBean object when id token is null
        AuthResponseBean expectedResponseBean = new AuthResponseBean();
        expectedResponseBean.setTokenValid(true);
        expectedResponseBean.setScopes(invalidTokenInfo.getScopes());
        expectedResponseBean.setType(AuthenticatorConstants.BEARER_PREFIX);
        expectedResponseBean.setValidityPeriod(invalidTokenInfo.getValidityPeriod());
        expectedResponseBean.setIdToken(invalidTokenInfo.getIdToken());
        expectedResponseBean.setAuthUser("admin");

        //// Actual response when id token is null
        AuthResponseBean responseBean = authenticatorService.getResponseBeanFromTokenInfo(invalidTokenInfo);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expectedResponseBean, responseBean));

        // Error Path - When parsing JWT fails and throws KeyManagementException
        //// AccessTokenInfo object with invalid ID token format
        AccessTokenInfo invalidAccessTokenInfo = new AccessTokenInfo();
        invalidAccessTokenInfo.setIdToken("xxx-invalid-id-token-xxx");
        invalidAccessTokenInfo.setValidityPeriod(-2L);
        invalidAccessTokenInfo.setScopes("apim:subscribe openid");

        try {
            AuthResponseBean errorResponseBean = authenticatorService.getResponseBeanFromTokenInfo(invalidAccessTokenInfo);
        } catch (KeyManagementException e) {
            Assert.assertEquals(900986, e.getErrorHandler().getErrorCode());
        }
    }
}
