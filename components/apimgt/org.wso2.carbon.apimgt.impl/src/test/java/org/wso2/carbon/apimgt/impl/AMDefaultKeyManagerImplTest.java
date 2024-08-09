/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;
import org.wso2.carbon.apimgt.impl.kmclient.model.ClientInfo;
import org.wso2.carbon.apimgt.impl.kmclient.model.DCRClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.IntrospectInfo;
import org.wso2.carbon.apimgt.impl.kmclient.model.IntrospectionClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, Base64.class, APIUtil.class})
public class AMDefaultKeyManagerImplTest {

    @Mock
    protected DCRClient dcrClient;
    @Mock
    protected IntrospectionClient introspectionClient;
    @InjectMocks
    AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();


    private final String APP_OWNER = "lakmali";
    private final String APP_NAME = "app1";
    private final String APP_UUID = "XXXXX";

    //Same client_id client_secret are used in AMDefaultKeyManagerImplWrapper mock class
    private final String CLIENT_SECRET = "GGGGGGG";
    private final String CLIENT_ID = "XXXXXXXXXX";
    private final String KEY_TYPE = "PRODUCTION";
    private final String[] REDIRECT_URIS = new String[]{"http://locahost, https://client.example.org/callback"};
    private final String[] GRANT_TYPES = new String[]{"client_credentials", "password"};

    @Before
    public void init() {
        PowerMockito.mockStatic(APIUtil.class);
    }
        
    @Test
    public void testCreateApplication() throws APIManagementException, KeyManagerClientException {

        PowerMockito.mockStatic(APIUtil.class);
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setAppOwner(APP_OWNER);
        oauthApplication.setCallBackURL(StringUtils.join(REDIRECT_URIS, ","));
        oauthApplication.setClientName(APP_NAME);
        oauthApplication.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, APP_OWNER);
        oauthApplication.addParameter(ApplicationConstants.APP_KEY_TYPE, KEY_TYPE);
        oauthApplication.setJsonString(getJSONString());
        oauthRequest.setMappingId("123");
        oauthRequest.setOAuthApplicationInfo(oauthApplication);

        PowerMockito.when(APIUtil.isCrossTenantSubscriptionsEnabled()).thenReturn(false);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        ClientInfo response = new ClientInfo();
        response.setClientId(CLIENT_ID);
        response.setClientName(APP_UUID);
        response.setClientSecret(CLIENT_SECRET);
        response.setRedirectUris(Arrays.asList(REDIRECT_URIS));
        response.setGrantTypes(Arrays.asList(GRANT_TYPES));

        Mockito.when(dcrClient.createApplication(Mockito.any(ClientInfo.class))).thenReturn(response);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).
                thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.when(APIUtil.getApplicationUUID(Mockito.anyString(), Mockito.anyString())).thenReturn(APP_UUID);

        OAuthApplicationInfo oauthApplicationResponse = keyManager.createApplication(oauthRequest);
        Assert.assertEquals(StringUtils.join(REDIRECT_URIS, ","), oauthApplicationResponse.getCallBackURL());
        Assert.assertEquals(APP_UUID, oauthApplicationResponse.getClientName());
    }
    
    @Test(expected = APIManagementException.class)
    public void testCreateApplicationWithException() throws APIManagementException {
        Mockito.when(APIUtil.getApplicationUUID(Mockito.anyString(), Mockito.anyString())).thenReturn(APP_UUID);

        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthRequest.setOAuthApplicationInfo(oauthApplication);

        keyManager.createApplication(oauthRequest);
    }

    @Test
    public void testTokenUnlimitedExpirationTime() throws KeyManagerClientException, APIManagementException {
        String accessToken = "155ddde3-68db-35b1-82dc-1247616b2da9";
        IntrospectInfo response = new IntrospectInfo();
        response.setActive(true);
        response.setExpiry(Long.MAX_VALUE);
        response.setIat(new Date().getTime());

        Mockito.when(introspectionClient.introspect(accessToken)).thenReturn(response);
        AccessTokenInfo info = keyManager.getTokenMetaData(accessToken);
        Assert.assertEquals(Long.MAX_VALUE, info.getValidityPeriod());
        
    }

    @Test
    public void testCreateApplicationAppNameWithSpecialChars()
            throws APIManagementException, KeyManagerClientException {
        String applicationName = "ÅÄÖÅÄÖ";

        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        OAuthAppRequest oauthRequest = new OAuthAppRequest();

        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setAppOwner(APP_OWNER);
        oauthApplication.setCallBackURL(StringUtils.join(REDIRECT_URIS, ","));
        oauthApplication.setClientName(applicationName);
        oauthApplication.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, APP_OWNER);
        oauthApplication.addParameter(ApplicationConstants.APP_KEY_TYPE, KEY_TYPE);
        oauthApplication.setJsonString(getJSONString());
        oauthRequest.setMappingId("123");
        oauthRequest.setOAuthApplicationInfo(oauthApplication);

        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        ClientInfo response = new ClientInfo();
        response.setClientId(CLIENT_ID);
        response.setClientName(APP_UUID);
        response.setClientSecret(CLIENT_SECRET);
        response.setRedirectUris(Arrays.asList(REDIRECT_URIS));
        response.setGrantTypes(Arrays.asList(GRANT_TYPES));

        Mockito.when(dcrClient.createApplication(Mockito.any(ClientInfo.class))).thenReturn(response);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).
                thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.when(APIUtil.getApplicationUUID(Mockito.anyString(), Mockito.anyString())).thenReturn(APP_UUID);

        OAuthApplicationInfo oauthApplicationResponse = keyManager.createApplication(oauthRequest);
        Assert.assertEquals(StringUtils.join(REDIRECT_URIS, ","), oauthApplicationResponse.getCallBackURL());
        Assert.assertEquals(APP_UUID, oauthApplicationResponse.getClientName());
    }


    @Test
    public void testTokenAuthorizedUserType() throws KeyManagerClientException, APIManagementException {
        String accessToken = "155ddde3-68db-35b1-82dc-1247616b2da9";
        IntrospectInfo response = new IntrospectInfo();
        response.setActive(true);
        response.setExpiry(Long.MAX_VALUE);
        response.setIat(new Date().getTime());
        response.setAut("application");
        Mockito.when(introspectionClient.introspect(accessToken)).thenReturn(response);
        AccessTokenInfo accessTokenInfo = keyManager.getTokenMetaData(accessToken);
        Assert.assertTrue(accessTokenInfo.isApplicationToken());

        response.setAut("application_user");
        Mockito.when(introspectionClient.introspect(accessToken)).thenReturn(response);
        accessTokenInfo = keyManager.getTokenMetaData(accessToken);
        Assert.assertFalse(accessTokenInfo.isApplicationToken());

        response.setAut("APPLICATION");
        Mockito.when(introspectionClient.introspect(accessToken)).thenReturn(response);
        accessTokenInfo = keyManager.getTokenMetaData(accessToken);
        Assert.assertTrue(accessTokenInfo.isApplicationToken());

        response.setAut("APPLICATION_USER");
        Mockito.when(introspectionClient.introspect(accessToken)).thenReturn(response);
        accessTokenInfo = keyManager.getTokenMetaData(accessToken);
        Assert.assertFalse(accessTokenInfo.isApplicationToken());

        response.setAut("");
        Mockito.when(introspectionClient.introspect(accessToken)).thenReturn(response);
        accessTokenInfo = keyManager.getTokenMetaData(accessToken);
        Assert.assertFalse(accessTokenInfo.isApplicationToken());
    }

    @Test
    public void testGetTokenTypeToSendInRequestUpdateJWTToJWT() throws Exception {
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setClientId("test");
        oauthApplication.setTokenType(APIConstants.TOKEN_TYPE_JWT);

        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setTokenType(APIConstants.TOKEN_TYPE_JWT);

        Mockito.when(dcrClient.getApplication(java.util.Base64.getUrlEncoder().encodeToString(
                oauthApplication.getClientId().getBytes(StandardCharsets.UTF_8)))).thenReturn(clientInfo);

        String tokenType = Whitebox.invokeMethod(keyManager, "getTokenTypeToSendInRequest", oauthApplication, true);
        Assert.assertEquals(APIConstants.TOKEN_TYPE_JWT, tokenType);
    }

    @Test
    public void testGetTokenTypeToSendInRequestUpdateJWTToOAuth() throws Exception {
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setClientId("test");
        oauthApplication.setTokenType(APIConstants.TOKEN_TYPE_JWT);

        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);

        Mockito.when(dcrClient.getApplication(java.util.Base64.getUrlEncoder().encodeToString(
                oauthApplication.getClientId().getBytes(StandardCharsets.UTF_8)))).thenReturn(clientInfo);

        String tokenType = Whitebox.invokeMethod(keyManager, "getTokenTypeToSendInRequest",
                oauthApplication, true);
        Assert.assertEquals(APIConstants.TOKEN_TYPE_JWT, tokenType);
    }

    @Test
    public void testGetTokenTypeToSendInRequestUpdateOAuthToOAuth() throws Exception {
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setClientId("test");
        oauthApplication.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);

        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);

        Mockito.when(dcrClient.getApplication(java.util.Base64.getUrlEncoder().encodeToString(
                oauthApplication.getClientId().getBytes(StandardCharsets.UTF_8)))).thenReturn(clientInfo);

        String tokenType = Whitebox.invokeMethod(keyManager, "getTokenTypeToSendInRequest", oauthApplication, true);
        Assert.assertEquals(APIConstants.TOKEN_TYPE_OAUTH, tokenType);
    }

    @Test
    public void testGetTokenTypeToSendInRequestUpdateCustomType() throws Exception {
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setClientId("test");
        oauthApplication.setTokenType(APIConstants.TOKEN_TYPE_OAUTH);

        String customTokenType = "customTokenType";
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setTokenType(customTokenType);

        Mockito.when(dcrClient.getApplication(java.util.Base64.getUrlEncoder().encodeToString(
                oauthApplication.getClientId().getBytes(StandardCharsets.UTF_8)))).thenReturn(clientInfo);

        String tokenType = Whitebox.invokeMethod(keyManager, "getTokenTypeToSendInRequest", oauthApplication, true);
        Assert.assertEquals(customTokenType, tokenType);
    }

    @Test
    public void testGetTokenTypeToSendInRequestCreateJWT() throws Exception {
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setClientId("test");
        oauthApplication.setTokenType(APIConstants.TOKEN_TYPE_JWT);

        String tokenType = Whitebox.invokeMethod(keyManager, "getTokenTypeToSendInRequest", oauthApplication, false);
        Assert.assertEquals(APIConstants.TOKEN_TYPE_JWT, tokenType);
    }

    @Test
    public void testGetTokenTypeToSendInRequestCreateDefault() throws Exception {
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setClientId("test");
        oauthApplication.setTokenType(APIConstants.DEFAULT_TOKEN_TYPE);

        String tokenType = Whitebox.invokeMethod(keyManager, "getTokenTypeToSendInRequest", oauthApplication, false);
        Assert.assertEquals(APIConstants.DEFAULT_TOKEN_TYPE, tokenType);
    }

//
//    @Test
//    public void testUpdateApplication() throws APIManagementException {
//        OAuthAppRequest oauthRequest = new OAuthAppRequest();
//
//        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
//        oauthApplication.setAppOwner(APP_OWNER);
//        oauthApplication.setCallBackURL("http://locahost");
//        oauthApplication.setClientId(CLIENT_ID);
//        oauthApplication.setClientName(APP_NAME);
//
//        oauthRequest.setMappingId("123");
//        oauthRequest.setOAuthApplicationInfo(oauthApplication);
//
//        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
//        keyManager.createApplication(oauthRequest);
//
//        oauthApplication.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, APP_OWNER);
//        oauthApplication.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT, "client_credentials, password");
//        oauthApplication.addParameter(ApplicationConstants.APP_KEY_TYPE, "PRODUCTION");
//        oauthApplication.setCallBackURL("http://newcallback.com");
//
//
//        OAuthApplicationInfo oauthApplicationResponse = keyManager.updateApplication(oauthRequest);
//        //Check whether callback URL change is affected
//        Assert.assertEquals("http://newcallback.com", oauthApplicationResponse.getCallBackURL());
//        }
//
//    @Test
//    public void testGetApplication() throws APIManagementException {
//        OAuthAppRequest oauthRequest = new OAuthAppRequest();
//
//        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
//        oauthApplication.setAppOwner(APP_OWNER);
//        oauthApplication.setCallBackURL("http://locahost");
//        oauthApplication.setClientName(APP_NAME);
//        oauthApplication.setJsonString(getJSONString());
//        oauthRequest.setMappingId("123");
//        oauthRequest.setOAuthApplicationInfo(oauthApplication);
//
//        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
//        keyManager.createApplication(oauthRequest);
//
//        OAuthApplicationInfo oauthApplicationResponse = keyManager.retrieveApplication(CLIENT_ID);
//        Assert.assertNotNull(oauthApplicationResponse);
//        Assert.assertEquals(APP_NAME, oauthApplicationResponse.getClientName());
//    }
//
//    @Test
//    public void testGetApplicationWithInvalidConsumerKey() throws APIManagementException {
//
//        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
//
//        OAuthApplicationInfo oauthApplicationResponse = keyManager.retrieveApplication("YYYYYYY");
//        Assert.assertNull(oauthApplicationResponse);
//    }

//    @Test
//    public void testGetNewApplicationAccessToken() throws APIManagementException {
//        AccessTokenRequest tokenRequest = new AccessTokenRequest();
//        tokenRequest.setTokenToRevoke("ert567yhk");
//        tokenRequest.setClientId(CLIENT_ID);
//        tokenRequest.setClientSecret(CLIENT_SECRET);
//        tokenRequest.setValidityPeriod(3600);
//
//        String [] scopeArray = {"test", "read"};
//        tokenRequest.setScope(scopeArray);
//
//        AccessTokenInfo tokenResponse = keyManager.getNewApplicationAccessToken(tokenRequest);
//
//        Assert.assertNotNull(tokenResponse.getAccessToken());
//
//    }

//    @Test
//    public void testMapOAuthApplication() throws APIManagementException {
//        OAuthAppRequest oauthRequest = new OAuthAppRequest();
//
//        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
//        oauthApplication.setAppOwner(APP_OWNER);
//        oauthApplication.setCallBackURL("http://locahost");
//        oauthApplication.setClientId(CLIENT_ID);
//        oauthApplication.setClientName(APP_NAME);
//        oauthApplication.setClientSecret(CLIENT_SECRET);
//        oauthApplication.setJsonString(getJSONString());
//
//        oauthApplication.addParameter("tokenScope", "read_scope");
//        oauthApplication.addParameter("client_secret", CLIENT_SECRET);
//
//        oauthRequest.setMappingId("123");
//        oauthRequest.setOAuthApplicationInfo(oauthApplication);
//
//        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
//        keyManager.createApplication(oauthRequest);
//
//        oauthApplication.addParameter("tokenScope", "read_scope");
//        oauthApplication.setClientId(CLIENT_ID);
//
//        OAuthApplicationInfo oauthApplicationResponse = keyManager.mapOAuthApplication(oauthRequest);
//
//        Assert.assertEquals(CLIENT_SECRET, oauthApplicationResponse.getClientSecret());
//    }
//
//    @Test(expected = APIManagementException.class)
//    public void testMapOAuthApplicationWithException() throws APIManagementException {
//        OAuthAppRequest oauthRequest = new OAuthAppRequest();
//
//        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
//        oauthApplication.setAppOwner(APP_OWNER);
//        oauthApplication.setCallBackURL("http://locahost");
//        oauthApplication.setClientId(CLIENT_ID);
//        oauthApplication.setClientName(APP_NAME);
//        oauthApplication.setClientSecret(CLIENT_SECRET);
//        oauthApplication.setJsonString(getJSONString());
//
//        oauthApplication.addParameter("tokenScope", "read_scope");
//        oauthApplication.addParameter("client_secret", "SSSSS");
//
//        oauthRequest.setMappingId("123");
//        oauthRequest.setOAuthApplicationInfo(oauthApplication);
//
//        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
//        keyManager.createApplication(oauthRequest);
//
//        oauthApplication.addParameter("tokenScope", "read_scope");
//        oauthApplication.setClientId(CLIENT_ID);
//
//        keyManager.mapOAuthApplication(oauthRequest);
//    }
//
//    @Test
//    public void testGetTokenMetaData() throws APIManagementException {
//        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
//        AccessTokenInfo tokenInfo = keyManager.getTokenMetaData("ert567yhk");
//
//        Assert.assertNotNull(tokenInfo);
//        Assert.assertTrue(tokenInfo.isTokenValid());
//    }
//
//    @Test
//    public void testGetTokenMetaDataWithInvalidToken() throws APIManagementException {
//        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
//        //"invalid_token" is mocked as an invalid token in AMDefaultKeyManagerImplWrapper
//        AccessTokenInfo tokenInfo = keyManager.getTokenMetaData("invalid_token");
//
//        Assert.assertNotNull(tokenInfo);
//        Assert.assertFalse(tokenInfo.isTokenValid());
//    }


    private String getJSONString() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ApplicationConstants.OAUTH_CLIENT_GRANT, "client_credentials, password");
        parameters.put(ApplicationConstants.OAUTH_REDIRECT_URIS, "http://locahost");
        parameters.put(ApplicationConstants.OAUTH_CLIENT_NAME, APP_NAME);
        
        return JSONObject.toJSONString(parameters);
    }

}
