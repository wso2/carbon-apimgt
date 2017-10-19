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
package org.wso2.carbon.apimgt.rest.api.commons.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.impl.OAuth2Authenticator;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
@RunWith(PowerMockRunner.class)
@PrepareForTest(APIManagerFactory.class)
public class OAuth2AuthenticatorTestCase {
    @Test
    public void testOauthAuthenticate() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request requestObj = new Request(carbonMessage);
        Response responseObj = Mockito.mock(Response.class);
        ServiceMethodInfo serviceMethodInfoObj = Mockito.mock(ServiceMethodInfo.class);
        final String authorizationHttpHeader = "Bearer 7d33e3cd-60f0-3484-9651-cc31f2e09fb4";
        final String accessToken = "7d33e3cd-60f0-3484-9651-cc31f2e09fb4";


        //Valid AccessToken
        Header authHeader1 = Mockito.mock(Header.class);
        authHeader1.setName(RestApiConstants.AUTHORIZATION_HTTP_HEADER);
        authHeader1.setValue(authorizationHttpHeader);

        List<Header> headersList1 = new ArrayList<Header>(1);
        headersList1.add(authHeader1);

        Headers headers1 = Mockito.mock(Headers.class);
        headers1.set(headersList1);

        when(requestObj.getHeaders()).thenReturn(headers1);
        requestObj.getHeaders().set(headersList1);

        String authHeaderString2 = authorizationHttpHeader;
        when(headers1.contains(RestApiConstants.AUTHORIZATION_HTTP_HEADER)).thenReturn(true);
        when(headers1.get(RestApiConstants.AUTHORIZATION_HTTP_HEADER)).thenReturn(authHeaderString2);

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTokenValid(true);
        accessTokenInfo.setEndUserName("admin@carbon.super");
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);

        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getIdentityProvider()).thenReturn(identityProvider);
        Mockito.when(identityProvider.getTokenMetaData(accessToken)).thenReturn(accessTokenInfo);

        when((String) requestObj.getProperty(APIConstants.REQUEST_URL)).thenReturn("/api/am/publisher/");
        OAuth2Authenticator oAuth2Authenticator = new OAuth2Authenticator();
        oAuth2Authenticator.authenticate(requestObj, responseObj, serviceMethodInfoObj);
        Assert.assertEquals(0, responseObj.getStatusCode());
    }
}
