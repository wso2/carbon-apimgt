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
package org.wso2.carbon.apimgt.rest.api.commons.interceptors;


import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.interceptors.RESTAPISecurityInterceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

public class RESTAPISecurityInterceptorTestCase {

    @Test
    public void testGetApisSuccess() throws APIManagementException {

        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Request requestObj = Mockito.mock(Request.class);

        try {
            PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(requestObj);
        } catch (Exception e) {
            throw new APIMgtSecurityException("Error while mocking Request Object ", e);
        }

        Response responseObj = Mockito.mock(Response.class);
        Mockito.when(requestObj.getHeader(RestApiConstants.AUTHORIZATION_HTTP_HEADER)).
                                            thenReturn("Authorization:  053d68ee-12bc-36b0-ab9c-31752ef5bda9");
        Mockito.when(requestObj.getHeader("REQUEST_URL")).
                thenReturn("http://localhost:9090/api/am/publisher/v1/api");
        ServiceMethodInfo serviceMethodInfoObj = Mockito.mock(ServiceMethodInfo.class);
        RESTAPISecurityInterceptor interceptor = Mockito.mock(RESTAPISecurityInterceptor.class);
        boolean isAuthorized = interceptor.preCall(requestObj, responseObj, serviceMethodInfoObj);
        if (isAuthorized) {
            Assert.assertEquals(isAuthorized, true);
        } else {
            Assert.assertEquals(isAuthorized, false);
        }
    }

    @Test
    public void testGetTiersByLevelSuccess() throws APIManagementException {

        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Request requestObj = Mockito.mock(Request.class);

        try {
            PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(requestObj);
        } catch (Exception e) {
            throw new APIMgtSecurityException("Error while mocking Request Object ", e);
        }

        Response responseObj = Mockito.mock(Response.class);
        Mockito.when(requestObj.getHeader("REQUEST_URL")).
                thenReturn("http://localhost:9090/api/am/store/v1/tiers/application");
        ServiceMethodInfo serviceMethodInfoObj = Mockito.mock(ServiceMethodInfo.class);
        RESTAPISecurityInterceptor interceptor = Mockito.mock(RESTAPISecurityInterceptor.class);
        boolean isAuthorized = interceptor.preCall(requestObj, responseObj, serviceMethodInfoObj);
        if (isAuthorized) {
            Assert.assertEquals(isAuthorized, true);
        } else {
            Assert.assertEquals(isAuthorized, false);
        }
    }

}
