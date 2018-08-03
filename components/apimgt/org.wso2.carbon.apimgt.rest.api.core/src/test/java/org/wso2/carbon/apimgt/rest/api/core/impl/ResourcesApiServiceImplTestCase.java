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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.rest.api.core.dto.ResourcesListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class ResourcesApiServiceImplTestCase {

    private static final String API_CONTEXT = "/api";
    private static final String API_VERSION = "1.0.0";

    @Test
    public void resourcesGetTest() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        ResourcesApiServiceImpl resourcesApiService = new ResourcesApiServiceImpl(apiMgtAdminService);

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
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        Mockito.when(apiMgtAdminService.getAllResourcesForApi(API_CONTEXT, API_VERSION)).thenThrow(
                new APIManagementException("", ExceptionCodes.APIMGT_DAO_EXCEPTION));

        ResourcesApiServiceImpl resourcesApiService = new ResourcesApiServiceImpl(apiMgtAdminService);
        Response response = resourcesApiService.resourcesGet(API_CONTEXT, API_VERSION, null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private Response getResponse(String apiContext, String apiVersion) throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        ResourcesApiServiceImpl resourcesApiService = new ResourcesApiServiceImpl(apiMgtAdminService);
        return resourcesApiService.resourcesGet(apiContext, apiVersion, null, getRequest());
    }

    private Request getRequest() throws Exception {
        return Mockito.mock(Request.class);
    }

}
