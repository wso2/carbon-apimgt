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
package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.DedicatedGateway;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DedicatedGatewayDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.CompositeAPIMappingUtil;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class CompositeApisApiServiceImplTestCase {

    private static final String USER = "admin";
    private static final String contentType = "application/json";

    @Test
    public void testCompositeApisApiIdDelete() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiStore)
                .deleteCompositeApi(apiID);

        Response response = compositeApisApiService.compositeApisApiIdDelete
                (apiID, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        CompositeAPI compositeAPI = SampleTestObjectCreator.createCompositeAPIModelBuilder().build();

        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.TRUE);
        Mockito.when(apiStore.getCompositeAPIbyId(apiID)).thenReturn(compositeAPI);

        Response response = compositeApisApiService.compositeApisApiIdGet
                (apiID, null, null, request);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getEntity().toString().contains(compositeAPI.getName()));
    }

    @Test
    public void testCompositeApisApiIdGetNotFound() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.FALSE);

        Response response = compositeApisApiService.compositeApisApiIdGet
                (apiID, null, null, request);

        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdImplementationGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        InputStream implementation = null;

        Mockito.when(apiStore.getCompositeApiImplementation(apiID)).thenReturn(implementation);

        Response response = compositeApisApiService.compositeApisApiIdImplementationGet
                (apiID, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdImplementationPut() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        InputStream implmentation = null;

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName("sample.txt");
        fileInfo.setContentType(contentType);

        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiStore)
                .updateCompositeApiImplementation(apiID, implmentation);

        Response response = compositeApisApiService.compositeApisApiIdImplementationPut
                (apiID, implmentation, fileInfo, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdPut() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        CompositeAPI.Builder builder = null;
        InputStream implmentation = null;
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName("sample.txt");
        fileInfo.setContentType(contentType);

        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiStore)
                .updateCompositeApi(builder);

        Response response = compositeApisApiService.compositeApisApiIdImplementationPut
                (apiID, implmentation, fileInfo, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdSwaggerGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Mockito.when(apiStore.getCompositeApiDefinition(apiID)).thenReturn("SWAGGER_DEFINITION");

        Response response = compositeApisApiService.compositeApisApiIdSwaggerGet
                (apiID, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdSwaggerPut() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        String swagger = "sampleDef";

        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiStore)
                .updateCompositeApiDefinition(apiID, swagger);
        Mockito.when(apiStore.getCompositeApiDefinition(apiID)).thenReturn(swagger);

        Response response = compositeApisApiService.compositeApisApiIdSwaggerPut
                (apiID, swagger, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String query = "*";

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        CompositeAPI compositeAPI1 = SampleTestObjectCreator.createCompositeAPIModelBuilder().build();
        CompositeAPI compositeAPI2 = SampleTestObjectCreator.createCompositeAPIModelBuilder().build();

        List<CompositeAPI> compositeAPIList = new ArrayList<>();
        compositeAPIList.add(compositeAPI1);
        compositeAPIList.add(compositeAPI2);

        Mockito.when(apiStore.searchCompositeAPIs(query, 0, 10)).thenReturn(compositeAPIList);

        Response response = compositeApisApiService.compositeApisGet
                (10, 0, query, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisPost() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();

        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        CompositeAPI compositeAPI = SampleTestObjectCreator.createCompositeAPIModelBuilder().build();
        CompositeAPIDTO compositeAPIDTO = CompositeAPIMappingUtil.toCompositeAPIDTO(compositeAPI);

        Application application = SampleTestObjectCreator.createDefaultApplication();

        Mockito.when(apiStore.getApplicationByUuid(UUID.randomUUID().toString())).thenReturn(application);
        Mockito.when(apiStore.getCompositeAPIbyId(compositeAPI.getId())).thenReturn(compositeAPI);

        Response response = compositeApisApiService.compositeApisPost
                (compositeAPIDTO, request);

        Assert.assertEquals(201, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdDedicatedGatewayGetForInvalidAPI() throws Exception {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();
        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);
        Request request = TestUtil.getRequest();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.FALSE);
        Response response = compositeApisApiService.compositeApisApiIdDedicatedGatewayGet(apiID, null, null, request);
        Assert.assertEquals(ExceptionCodes.API_NOT_FOUND.getHttpStatusCode(), response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdDedicatedGatewayGet() throws Exception {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();
        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);
        Request request = TestUtil.getRequest();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.TRUE);
        DedicatedGateway dedicatedGateway = new DedicatedGateway();
        dedicatedGateway.setEnabled(true);
        Mockito.when(apiStore.getDedicatedGateway(apiID)).thenReturn(dedicatedGateway);
        Response response = compositeApisApiService.compositeApisApiIdDedicatedGatewayGet(apiID, null, null, request);
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdDedicatedGatewayGetForNull() throws Exception {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();
        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);
        Request request = TestUtil.getRequest();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.TRUE);
        Mockito.when(apiStore.getDedicatedGateway(apiID)).thenReturn(null);
        Response response = compositeApisApiService.compositeApisApiIdDedicatedGatewayGet(apiID, null, null, request);
        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdDedicatedGatewayGetForException() throws Exception {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();
        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);
        Request request = TestUtil.getRequest();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.TRUE);
        Mockito.when(apiStore.getDedicatedGateway(apiID)).thenThrow(new APIManagementException
                ("Dedicated gateway details not found for the API",
                        ExceptionCodes.DEDICATED_GATEWAY_DETAILS_NOT_FOUND));
        Response response = compositeApisApiService.compositeApisApiIdDedicatedGatewayGet(apiID, null, null, request);
        Assert.assertEquals(ExceptionCodes.DEDICATED_GATEWAY_DETAILS_NOT_FOUND.getHttpStatusCode(),
                response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdDedicatedGatewayPutForInvalidAPI() throws Exception {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();
        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);
        Request request = TestUtil.getRequest();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.FALSE);
        Response response = compositeApisApiService.compositeApisApiIdDedicatedGatewayPut(apiID,
                new DedicatedGatewayDTO(), null, null, request);
        Assert.assertEquals(ExceptionCodes.API_NOT_FOUND.getHttpStatusCode(), response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdDedicatedGatewayPut() throws Exception {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();
        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);
        Request request = TestUtil.getRequest();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.TRUE);
        Mockito.doNothing().when(apiStore).updateDedicatedGateway(Mockito.any());
        DedicatedGateway dedicatedGateway = new DedicatedGateway();
        dedicatedGateway.setEnabled(true);
        Mockito.when(apiStore.getDedicatedGateway(apiID)).thenReturn(dedicatedGateway);
        DedicatedGatewayDTO dedicatedGatewayDTO = new DedicatedGatewayDTO();
        dedicatedGatewayDTO.setIsEnabled(true);
        Response response = compositeApisApiService.compositeApisApiIdDedicatedGatewayPut(apiID,
                dedicatedGatewayDTO, null, null, request);
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompositeApisApiIdDedicatedGatewayPutForException() throws Exception {
        TestUtil.printTestMethodName();
        String apiID = UUID.randomUUID().toString();
        CompositeApisApiServiceImpl compositeApisApiService = new CompositeApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);
        Request request = TestUtil.getRequest();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Mockito.when(apiStore.isCompositeAPIExist(apiID)).thenReturn(Boolean.TRUE);
        Mockito.doThrow(new APIManagementException("Error while creating dedicated container based gateway",
                ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED)).when(apiStore)
                .updateDedicatedGateway(Mockito.any());
        Response response = compositeApisApiService.compositeApisApiIdDedicatedGatewayPut(apiID,
                new DedicatedGatewayDTO(), null, null, request);
        Assert.assertEquals(ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED.getHttpStatusCode(),
                response.getStatus());
    }

}
