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
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIDTO;
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
}
