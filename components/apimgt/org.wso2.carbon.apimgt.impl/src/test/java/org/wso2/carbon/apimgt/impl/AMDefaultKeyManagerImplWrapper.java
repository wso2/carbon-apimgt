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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.json.simple.JSONObject;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;


/**
 * Wrapper class to mock AMDefaultKeyManagerImpl
 *
 */
public class AMDefaultKeyManagerImplWrapper extends AMDefaultKeyManagerImpl {
    
    org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo oauthApp;
    //Mocked App credentials
    private String CLIENT_SECRET = "GGGGGGG";
    private String CLIENT_ID = "XXXXXXXXXX";
    
    @Override
    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo createOAuthApplicationbyApplicationInfo(
                 org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationToCreate) throws Exception {
        if (applicationToCreate.getClientName() == null) {
            return null;
        }
        oauthApp = new org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo();
        oauthApp.setAppOwner(applicationToCreate.getAppOwner());
        oauthApp.setCallBackURL(applicationToCreate.getCallBackURL());
        oauthApp.setClientId(CLIENT_ID);
        oauthApp.setClientName(applicationToCreate.getClientName());
        oauthApp.setClientSecret(CLIENT_SECRET);
        oauthApp.setJsonString(applicationToCreate.getJsonString());
        return oauthApp;

    }
    
    @Override
    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo updateOAuthApplication(String userId, 
                  String applicationName, String callBackURL, String clientId, String[] grantTypes) throws Exception {
        String allowedGrantTypes = StringUtils.join(grantTypes, ",");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ApplicationConstants.OAUTH_CLIENT_GRANT, allowedGrantTypes);
        parameters.put(ApplicationConstants.OAUTH_CLIENT_USERNAME, userId);
        
        oauthApp.setAppOwner(userId);
        oauthApp.setCallBackURL(callBackURL);
        oauthApp.setClientId(clientId);
        oauthApp.setClientName(applicationName);
        oauthApp.setClientSecret(CLIENT_SECRET);
        oauthApp.setJsonString(JSONObject.toJSONString(parameters));
        
        return oauthApp;

    }
    
    @Override
    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo getOAuthApplication(String consumerKey)
                                                                                               throws Exception {
        return oauthApp;
    }
    
    @Override
    protected HttpResponse executeHTTPrequest(int port, String protocol, HttpPost httpPost)
                                                              throws ClientProtocolException, IOException {
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);
        HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
        
        InputStream responseStream = 
                IOUtils.toInputStream("{\"access_token\": \"ert567900\",\"expires_in\": 3600, \"scope\":\"default\"}", 
                                      "UTF-8");
        
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.when(httpEntity.getContent()).thenReturn(responseStream);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        
        return httpResponse;
    }
    
    @Override
    protected String getConfigurationElementValue(String property) {
        if (APIConstants.APPLICATION_TOKEN_SCOPE.equals(property)) {
            return "am_application_scope";
        }
        return "";
    }
    
    @Override
    protected String getConfigurationParamValue(String parameter) {
        if (APIConstants.TOKEN_URL.equals(parameter) || APIConstants.REVOKE_URL.equals(parameter)) {
            return "https://localhost:8243/token";
        } else if (APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD.equals(parameter)) {
            return "3600";
        }
        
        return "";
    }
    
    @Override
    protected OAuth2ClientApplicationDTO findOAuthConsumerIfTokenIsValid(OAuth2TokenValidationRequestDTO requestDTO) {
        OAuth2ClientApplicationDTO oauth2ClientAppDTO = Mockito.mock(OAuth2ClientApplicationDTO.class);
        OAuth2TokenValidationResponseDTO responseDTO = Mockito.mock(OAuth2TokenValidationResponseDTO.class);
                
        if (!"invalid_token".equals(requestDTO.getAccessToken().getIdentifier())) {
            Mockito.when(responseDTO.isValid()).thenReturn(true);
            Mockito.when(responseDTO.getAuthorizedUser()).thenReturn("user1");
            Mockito.when(responseDTO.getExpiryTime()).thenReturn(3600L);
            
            String [] scopeArray = {"test", "read"};
            Mockito.when(responseDTO.getScope()).thenReturn(scopeArray);
        } else {
            Mockito.when(responseDTO.isValid()).thenReturn(false);
        }
        Mockito.when(oauth2ClientAppDTO.getAccessTokenValidationResponse()).thenReturn(responseDTO); 
        
        return oauth2ClientAppDTO;
    }
    
    @Override
    protected OMElement getOAuthConfigElement() {
        OMElement oauthElem = Mockito.mock(OMElement.class);
        OMElement mockElement = OMAbstractFactory.getOMFactory().
                createOMElement("AccessTokenDefaultValidityPeriod", null);
        mockElement.setText("3600");
        
        Mockito.when(oauthElem.getFirstChildWithName(new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, 
                                                     "AccessTokenDefaultValidityPeriod"))).thenReturn(mockElement);
        
        return oauthElem;
    }

}
