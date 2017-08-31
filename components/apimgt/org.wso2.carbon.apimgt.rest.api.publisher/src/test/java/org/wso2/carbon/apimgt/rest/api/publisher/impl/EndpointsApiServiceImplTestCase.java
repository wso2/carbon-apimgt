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
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestAPIPublisherUtil.class, MappingUtil.class})
public class EndpointsApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(EndpointsApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testEndpointsEndpointIdDelete() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        String endpointId = endpoint.getId();
        Mockito.doNothing().doThrow(new IllegalArgumentException())
                .when(apiPublisher).deleteEndpoint(endpointId);
        Response response = endpointsApiService.
                endpointsEndpointIdDelete(endpointId, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testEndpointsEndpointIdDeleteException() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        String endpointId = endpoint.getId();
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.ENDPOINT_ADD_FAILED))
                .when(apiPublisher).deleteEndpoint(endpointId);
        Response response = endpointsApiService.
                endpointsEndpointIdDelete(endpointId, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Endpoint adding failed"));
    }


    @Test
    public void testEndpointsEndpointIdGet() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        String endpointId = endpoint.getId();
        Mockito.doReturn(endpoint).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getEndpoint(endpointId);
        Response response = endpointsApiService.
                endpointsEndpointIdGet(endpointId, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains(endpointId));
    }

    @Test
    public void testEndpointsEndpointIdGetNotExist() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        String endpointId = endpoint.getId();
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getEndpoint(endpointId);
        Response response = endpointsApiService.
                endpointsEndpointIdGet(endpointId, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Endpoint Not Found"));
    }

    @Test
    public void testEndpointsEndpointIdGetException() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        String endpointId = endpoint.getId();
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.APPLICATION_INACTIVE))
                .when(apiPublisher).getEndpoint(endpointId);
        Response response = endpointsApiService.
                endpointsEndpointIdGet(endpointId, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Application is not active"));
    }

    @Test
    public void testEndpointsEndpointIdPut() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpointOld = SampleTestObjectCreator.createMockEndpoint();
        String endpointOldId = endpointOld.getId();

        Endpoint.Builder endpointUpdateBuilder = SampleTestObjectCreator.createMockEndpointBuilder();
        endpointUpdateBuilder.name("newNameEndpoint").id(endpointOldId);
        Endpoint newEndpoint = endpointUpdateBuilder.build();
        EndPointDTO endPointDTO = MappingUtil.toEndPointDTO(newEndpoint);

        Mockito.doReturn(endpointOld).doReturn(newEndpoint).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getEndpoint(endpointOldId);
        Mockito.doNothing().doThrow(new IllegalArgumentException())
                .when(apiPublisher).updateEndpoint(newEndpoint);
        Response response = endpointsApiService.
                endpointsEndpointIdPut(endpointOldId, endPointDTO, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("newNameEndpoint"));
    }

    @Test
    public void testEndpointsEndpointIdPutNotExist() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        EndPointDTO endPointDTO = MappingUtil.toEndPointDTO(endpoint);
        String endpointId = endpoint.getId();
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getEndpoint(endpointId);
        Response response = endpointsApiService.
                endpointsEndpointIdPut(endpointId, endPointDTO, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Endpoint Not Found"));
    }

    @Test
    public void testEndpointsEndpointIdPutException() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        String endpointId = endpoint.getId();
        EndPointDTO endPointDTO = MappingUtil.toEndPointDTO(endpoint);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.ENDPOINT_ALREADY_EXISTS))
                .when(apiPublisher).getEndpoint(endpointId);
        Response response = endpointsApiService.
                endpointsEndpointIdPut(endpointId, endPointDTO, null, null, getRequest());
        assertEquals(response.getStatus(), 409);
        assertTrue(response.getEntity().toString().contains("Endpoint already exists"));
    }


    @Test
    public void testEndpointsGet() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
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
                .when(apiPublisher).getAllEndpoints();
        Response response = endpointsApiService.
                endpointsGet(null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains(endpoint1.getId()));
        assertTrue(response.getEntity().toString().contains(endpoint2.getId()));
    }

    @Test
    public void testEndpointsGetException() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.ENDPOINT_ADD_FAILED))
                .when(apiPublisher).getAllEndpoints();
        Response response = endpointsApiService.
                endpointsGet(null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Endpoint adding failed"));
    }

    @Test
    public void testEndpointsHead() throws Exception {
        printTestMethodName();
        String name = "test";
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doReturn(true).doThrow(new IllegalArgumentException())
                .when(apiPublisher).isEndpointExist(name);
        Response response = endpointsApiService.
                endpointsHead(name, null, getRequest());
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testEndpointsHeadNotExist() throws Exception {
        printTestMethodName();
        String name = "test";
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doReturn(false).doThrow(new IllegalArgumentException())
                .when(apiPublisher).isEndpointExist(name);
        Response response = endpointsApiService.
                endpointsHead(name, null, getRequest());
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void testEndpointsHeadException() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        String name = "test";
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.ENDPOINT_ADD_FAILED))
                .when(apiPublisher).isEndpointExist(name);
        Response response = endpointsApiService.
                endpointsHead(name, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Endpoint adding failed"));
    }

    @Test
    public void testEndpointsPost() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.mockStatic(MappingUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpointBuilder().build();
        EndPointDTO endPointDTO = MappingUtil.toEndPointDTO(endpoint);
        String endpointId = UUID.randomUUID().toString();
        PowerMockito.when(MappingUtil.toEndpoint(endPointDTO)).
                thenReturn(endpoint);
        Mockito.doReturn(endpointId).doThrow(new IllegalArgumentException())
                .when(apiPublisher).addEndpoint(endpoint);
        Mockito.doReturn(endpoint).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getEndpoint(endpointId);
        Response response = endpointsApiService.
                endpointsPost(endPointDTO, null, null, getRequest());
        assertEquals(response.getStatus(), 201);
    }

    @Test
    public void testEndpointsPostException() throws Exception {
        printTestMethodName();
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.mockStatic(MappingUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        EndPointDTO endPointDTO = MappingUtil.toEndPointDTO(endpoint);
        PowerMockito.when(MappingUtil.toEndpoint(endPointDTO)).
                thenReturn(endpoint);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.ENDPOINT_ALREADY_EXISTS))
                .when(apiPublisher).addEndpoint(endpoint);
        Response response = endpointsApiService.
                endpointsPost(endPointDTO, null, null, getRequest());
        assertEquals(response.getStatus(), 409);
        assertTrue(response.getEntity().toString().contains("Endpoint already exists"));
    }

    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = new HTTPCarbonMessage();
        carbonMessage.setProperty("LOGGED_IN_USER", USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
