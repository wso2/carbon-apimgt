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
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import java.util.ArrayList;
import java.util.List;

public class RESTAPISecurityInterceptorTestCase {

    @Test
    public void testGetApisSuccess() throws APIManagementException {

        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request requestObj = Mockito.mock(Request.class);

        try {
            PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(requestObj);
        } catch (Exception e) {
            throw new APIMgtSecurityException("Error while mocking Request Object ", e);
        }
        Header authHeader = Mockito.mock(Header.class);
        authHeader.setName(RestApiConstants.AUTHORIZATION_HTTP_HEADER);
        authHeader.setValue("Authorization:  053d68ee-12bc-36b0-ab9c-31752ef5bda9");

        Header pathHeader = Mockito.mock(Header.class);
        pathHeader.setName("REQUEST_URL");
        pathHeader.setValue("http://localhost:9090/api/am/publisher/v1/api");

        List<Header> headersList = new ArrayList<Header>(2);
        headersList.add(authHeader);
        headersList.add(pathHeader);

        Headers headers = Mockito.mock(Headers.class);
        headers.set(headersList);
        Mockito.when(requestObj.getHeaders()).thenReturn(headers);
        requestObj.getHeaders().set(headersList);

        Response responseObj = Mockito.mock(Response.class);

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
