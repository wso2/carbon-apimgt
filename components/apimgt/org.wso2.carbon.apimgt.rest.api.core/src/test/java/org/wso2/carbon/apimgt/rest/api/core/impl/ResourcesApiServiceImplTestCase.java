/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.rest.api.core.dto.ResourcesListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIManagerFactory.class)
public class ResourcesApiServiceImplTestCase {

    private static final String API_CONTEXT = "/api";
    private static final String API_VERSION = "1.0.0";

    @Test
    public void resourcesGetTest() throws Exception {
        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        ResourcesApiServiceImpl resourcesApiService = new ResourcesApiServiceImpl();

        UriTemplate uriTemplateOne = SampleTestObjectCreator.createUniqueUriTemplate();
        UriTemplate uriTemplateTwo = SampleTestObjectCreator.createUniqueUriTemplate();
        UriTemplate uriTemplateThree = SampleTestObjectCreator.createUniqueUriTemplate();

        List<UriTemplate> uriTemplateList = new ArrayList<>();
        uriTemplateList.add(uriTemplateOne);
        uriTemplateList.add(uriTemplateTwo);
        uriTemplateList.add(uriTemplateThree);

        Mockito.when(apiMgtAdminService.getAllResourcesForApi(API_CONTEXT, API_VERSION)).thenReturn(uriTemplateList);

        Response response = resourcesApiService.resourcesGet(API_CONTEXT, API_VERSION, null, getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(((ResourcesListDTO) response.getEntity()).getList().size(), 3);

    }

    @Test
    public void resourcesGetApiContextEmptyTest() throws Exception {
        Response response = getResponse(null, API_VERSION);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(((ResourcesListDTO) response.getEntity()).getList().size(), 0);

    }

    @Test
    public void resourcesGetApiVersionEmptyTest() throws Exception {
        Response response = getResponse(API_CONTEXT, null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(((ResourcesListDTO) response.getEntity()).getList().size(), 0);
    }

    @Test
    public void policiesGetExceptionTest() throws Exception {
        ResourcesApiServiceImpl resourcesApiService = new ResourcesApiServiceImpl();
        Response response = resourcesApiService.resourcesGet(API_CONTEXT, API_VERSION, null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void policiesGetApiContextEmptyExceptionTest() throws Exception {
        ResourcesApiServiceImpl resourcesApiService = new ResourcesApiServiceImpl();
        Response response = resourcesApiService.resourcesGet(null, API_VERSION, null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void policiesGetApiVersionEmptyExceptionTest() throws Exception {
        ResourcesApiServiceImpl resourcesApiService = new ResourcesApiServiceImpl();
        Response response = resourcesApiService.resourcesGet(API_CONTEXT, null, null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private Response getResponse(String apiContext, String apiVersion) throws Exception {
        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        ResourcesApiServiceImpl resourcesApiService = new ResourcesApiServiceImpl();
        return resourcesApiService.resourcesGet(apiContext, apiVersion, null, getRequest());
    }

    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }

}
