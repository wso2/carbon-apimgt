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

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class AMDefaultKeyManagerImplTest {
    
    private String APP_OWNER = "lakmali";    
    private String APP_NAME = "app1";
    
    //Same client_id client_secret are used in AMDefaultKeyManagerImplWrapper mock class
    private String CLIENT_SECRET = "GGGGGGG";
    private String CLIENT_ID = "XXXXXXXXXX";
    
        
    @Test
    public void testCreateApplication() throws APIManagementException {
        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setAppOwner(APP_OWNER);
        oauthApplication.setCallBackURL("http://locahost");
        oauthApplication.setClientName(APP_NAME);
        oauthApplication.setJsonString(getJSONString());
        oauthRequest.setMappingId("123");
        oauthRequest.setOAuthApplicationInfo(oauthApplication);
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        
        OAuthApplicationInfo oauthApplicationResponse = keyManager.createApplication(oauthRequest);
        Assert.assertEquals(APP_OWNER, oauthApplicationResponse.getAppOwner());
        Assert.assertEquals(APP_NAME, oauthApplicationResponse.getClientName());
    }

    @Test
    public void testCreateApplicationWithKeyType() throws APIManagementException {
        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setAppOwner(APP_OWNER);
        oauthApplication.setCallBackURL("http://locahost");        
        oauthApplication.setClientName(APP_NAME);
        oauthApplication.setJsonString(getJSONString());
        oauthApplication.addParameter(ApplicationConstants.APP_KEY_TYPE, "PRODUCTION");
        oauthRequest.setMappingId("123");
        oauthRequest.setOAuthApplicationInfo(oauthApplication);
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        
        OAuthApplicationInfo oauthApplicationResponse = keyManager.createApplication(oauthRequest);
        Assert.assertEquals(APP_OWNER, oauthApplicationResponse.getAppOwner());
        Assert.assertEquals(APP_NAME + "_PRODUCTION", oauthApplicationResponse.getClientName());
    }
    
    @Test(expected = APIManagementException.class)
    public void testCreateApplicationWithException() throws APIManagementException {
        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthRequest.setOAuthApplicationInfo(oauthApplication);
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();   

        keyManager.createApplication(oauthRequest);
    }
    
    @Test
    public void testUpdateApplication() throws APIManagementException {
        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setAppOwner(APP_OWNER);
        oauthApplication.setCallBackURL("http://locahost");
        oauthApplication.setClientId(CLIENT_ID);
        oauthApplication.setClientName(APP_NAME);
        
        oauthRequest.setMappingId("123");
        oauthRequest.setOAuthApplicationInfo(oauthApplication);
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        keyManager.createApplication(oauthRequest);
        
        oauthApplication.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, APP_OWNER);
        oauthApplication.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT, "client_credentials, password");
        oauthApplication.addParameter(ApplicationConstants.APP_KEY_TYPE, "PRODUCTION");
        oauthApplication.setCallBackURL("http://newcallback.com");
                

        OAuthApplicationInfo oauthApplicationResponse = keyManager.updateApplication(oauthRequest);
        //Check whether callback URL change is affected
        Assert.assertEquals("http://newcallback.com", oauthApplicationResponse.getCallBackURL());
        }
    
    @Test
    public void testGetApplication() throws APIManagementException {
        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setAppOwner(APP_OWNER);
        oauthApplication.setCallBackURL("http://locahost");
        oauthApplication.setClientName(APP_NAME);
        oauthApplication.setJsonString(getJSONString());
        oauthRequest.setMappingId("123");
        oauthRequest.setOAuthApplicationInfo(oauthApplication);
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        keyManager.createApplication(oauthRequest);
        
        OAuthApplicationInfo oauthApplicationResponse = keyManager.retrieveApplication(CLIENT_ID);
        Assert.assertNotNull(oauthApplicationResponse);
        Assert.assertEquals(APP_NAME, oauthApplicationResponse.getClientName());
    }
    
    @Test
    public void testGetApplicationWithInvalidConsumerKey() throws APIManagementException {
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper(); 

        OAuthApplicationInfo oauthApplicationResponse = keyManager.retrieveApplication("YYYYYYY");
        Assert.assertNull(oauthApplicationResponse);
    }
    
    @Test
    public void testGetNewApplicationAccessToken() throws APIManagementException {
        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setTokenToRevoke("ert567yhk");
        tokenRequest.setClientId(CLIENT_ID);
        tokenRequest.setClientSecret(CLIENT_SECRET);
        tokenRequest.setValidityPeriod(3600);
        
        String [] scopeArray = {"test", "read"};
        tokenRequest.setScope(scopeArray);
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper(); 
        
        AccessTokenInfo tokenResponse = keyManager.getNewApplicationAccessToken(tokenRequest);
        
        Assert.assertNotNull(tokenResponse.getAccessToken());
        
    }
    
    @Test
    public void testMapOAuthApplication() throws APIManagementException {
        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setAppOwner(APP_OWNER);
        oauthApplication.setCallBackURL("http://locahost");
        oauthApplication.setClientId(CLIENT_ID);
        oauthApplication.setClientName(APP_NAME);
        oauthApplication.setClientSecret(CLIENT_SECRET);
        oauthApplication.setJsonString(getJSONString());
        
        oauthApplication.addParameter("tokenScope", "read_scope");
        oauthApplication.addParameter("client_secret", CLIENT_SECRET);
        
        oauthRequest.setMappingId("123");
        oauthRequest.setOAuthApplicationInfo(oauthApplication);
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        keyManager.createApplication(oauthRequest);
        
        oauthApplication.addParameter("tokenScope", "read_scope");
        oauthApplication.setClientId(CLIENT_ID);
        
        OAuthApplicationInfo oauthApplicationResponse = keyManager.mapOAuthApplication(oauthRequest);

        Assert.assertEquals(CLIENT_SECRET, oauthApplicationResponse.getClientSecret());        
    }
    
    @Test(expected = APIManagementException.class)
    public void testMapOAuthApplicationWithException() throws APIManagementException {
        OAuthAppRequest oauthRequest = new OAuthAppRequest();
        
        OAuthApplicationInfo oauthApplication = new OAuthApplicationInfo();
        oauthApplication.setAppOwner(APP_OWNER);
        oauthApplication.setCallBackURL("http://locahost");
        oauthApplication.setClientId(CLIENT_ID);
        oauthApplication.setClientName(APP_NAME);
        oauthApplication.setClientSecret(CLIENT_SECRET);
        oauthApplication.setJsonString(getJSONString());
        
        oauthApplication.addParameter("tokenScope", "read_scope");
        oauthApplication.addParameter("client_secret", "SSSSS");
        
        oauthRequest.setMappingId("123");
        oauthRequest.setOAuthApplicationInfo(oauthApplication);
        
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        keyManager.createApplication(oauthRequest);
        
        oauthApplication.addParameter("tokenScope", "read_scope");
        oauthApplication.setClientId(CLIENT_ID);
        
        keyManager.mapOAuthApplication(oauthRequest);        
    }
    
    @Test
    public void testGetTokenMetaData() throws APIManagementException {
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        AccessTokenInfo tokenInfo = keyManager.getTokenMetaData("ert567yhk");
        
        Assert.assertNotNull(tokenInfo);
        Assert.assertTrue(tokenInfo.isTokenValid());
    }
    
    @Test
    public void testGetTokenMetaDataWithInvalidToken() throws APIManagementException {
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        //"invalid_token" is mocked as an invalid token in AMDefaultKeyManagerImplWrapper 
        AccessTokenInfo tokenInfo = keyManager.getTokenMetaData("invalid_token");
        
        Assert.assertNotNull(tokenInfo);
        Assert.assertFalse(tokenInfo.isTokenValid());
    }
    
    @Test
    public void testLoadConfiguration() throws APIManagementException {
        AMDefaultKeyManagerImplWrapper keyManager = new AMDefaultKeyManagerImplWrapper();
        keyManager.loadConfiguration(null);
        
        KeyManagerConfiguration configuration = keyManager.getKeyManagerConfiguration();
        
        Assert.assertNotNull(configuration);
    }
    
    private String getJSONString() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ApplicationConstants.OAUTH_CLIENT_GRANT, "client_credentials, password");
        parameters.put(ApplicationConstants.OAUTH_REDIRECT_URIS, "http://locahost");
        parameters.put(ApplicationConstants.OAUTH_CLIENT_NAME, APP_NAME);
        
        return JSONObject.toJSONString(parameters);
    }

}
