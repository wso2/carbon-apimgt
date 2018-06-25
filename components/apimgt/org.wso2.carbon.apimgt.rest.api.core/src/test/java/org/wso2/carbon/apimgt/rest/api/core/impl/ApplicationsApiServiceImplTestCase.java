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
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.core.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class ApplicationsApiServiceImplTestCase {

    @Test
    public void applicationsGetTestCase() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl(adminService);

        List<Application> applicationList = new ArrayList<>();

        Application applicationOne = SampleTestObjectCreator.createRandomApplication();
        Application applicationTwo = SampleTestObjectCreator.createRandomApplication();
        Application applicationThree = SampleTestObjectCreator.createRandomApplication();

        applicationList.add(applicationOne);
        applicationList.add(applicationTwo);
        applicationList.add(applicationThree);

        Mockito.when(adminService.getAllApplications()).thenReturn(applicationList);

        Response response = applicationsApiService.applicationsGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(((ApplicationListDTO) response.getEntity()).getList().size(), 3);
    }

    @Test
    public void applicationsGetExceptionTestCase() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl(adminService);

        String message = "Error while retrieving applications.";

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.APPLICATION_NOT_FOUND);
        Mockito.when(adminService.getAllApplications()).thenThrow(apiManagementException);

        Response response = applicationsApiService.applicationsGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), 404);

    }

    private Request getRequest() {
        return Mockito.mock(Request.class);
    }
}
