/*
 *
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

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIPublisherImpl;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestAPIPublisherUtil.class)
public class ExternalResourcesApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(ExternalResourcesApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testExternalResourcesServicesGet() throws Exception {
        printTestMethodName();
        ExternalResourcesApiServiceImpl externalResourcesApiService = new ExternalResourcesApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint1 = SampleTestObjectCreator.createMockEndpoint();
        Endpoint endpoint2 = SampleTestObjectCreator.createMockEndpoint();
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(endpoint1);
        endpointList.add(endpoint2);
        Mockito.doReturn(endpointList).doThrow(new IllegalArgumentException())
                .when(apiPublisher).discoverServiceEndpoints();
        Response response = externalResourcesApiService
                .externalResourcesServicesGet(null, null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertTrue(response.getEntity().toString().contains(endpoint1.getId()));
        Assert.assertTrue(response.getEntity().toString().contains(endpoint2.getId()));
    }

    @Test
    public void testExternalResourcesServicesGetException() throws Exception {
        printTestMethodName();
        ExternalResourcesApiServiceImpl externalResourcesApiService = new ExternalResourcesApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doThrow(new APIManagementException("Error occurred",
                ExceptionCodes.ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES)).when(apiPublisher).discoverServiceEndpoints();
        Response response = externalResourcesApiService.
                externalResourcesServicesGet(null, null, getRequest());
        Assert.assertEquals(response.getStatus(), 500);
        Assert.assertTrue(response.getEntity().toString().contains("Error while discovering services"));
    }

    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Mockito.when(carbonMessage.getProperty("LOGGED_IN_USER")).thenReturn(USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
