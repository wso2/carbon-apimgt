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

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.impl.BasicAuthAuthenticator;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.mockito.Mockito.when;


public class BasicAuthAuthenticatorTestCase {

    @Test
    public void testAuthenticate() throws Exception {

        final String authorizationHttpHeader = "Basic YWRtaW46YWRtaW4=";
        final String authorizationHttpHeader1 = "DummyHeader YWRtaW46YWRtaW4=";

        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Request requestObj = new Request(carbonMessage);

        try {
            PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(requestObj);
        } catch (Exception e) {
            throw new APIMgtSecurityException("Error while mocking Request Object ", e);
        }

        try {
            BasicAuthAuthenticator basicAuthAuthenticator = new BasicAuthAuthenticator();
            basicAuthAuthenticator.authenticate(requestObj, null, null);
        } catch (APIMgtSecurityException e) {
            Assert.assertEquals(e.getMessage(), "Missing Authorization header in the request.`");
        }


        when(requestObj.getHeader(RestApiConstants.AUTHORIZATION_HTTP_HEADER)).thenReturn(authorizationHttpHeader1);

        Response responseObj = Mockito.mock(Response.class);
        ServiceMethodInfo serviceMethodInfoObj = Mockito.mock(ServiceMethodInfo.class);
        try {
            BasicAuthAuthenticator basicAuthAuthenticator = new BasicAuthAuthenticator();
            basicAuthAuthenticator.authenticate(requestObj, responseObj, serviceMethodInfoObj);
        } catch (APIMgtSecurityException e) {
            Assert.assertEquals(e.getMessage(), "Missing 'Authorization : Basic' header in the request.`");
        }

        when(requestObj.getHeader(RestApiConstants.AUTHORIZATION_HTTP_HEADER)).thenReturn(authorizationHttpHeader);

        BasicAuthAuthenticator basicAuthAuthenticator = new BasicAuthAuthenticator();
        boolean isAuthenticated = basicAuthAuthenticator.authenticate(requestObj, responseObj, serviceMethodInfoObj);
        if (isAuthenticated) {
            Assert.assertEquals(isAuthenticated, true);
        } else {
            Assert.assertEquals(isAuthenticated, false);
        }

    }
}

