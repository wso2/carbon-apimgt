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

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class ExportApiServiceImplTestCase {

    @Test
    public void exportPoliciesThrottleGetTest() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        ExportApiServiceImpl exportApiService = new ExportApiServiceImpl(apiMgtAdminService);

        LogManager.getRootLogger().setLevel(Level.DEBUG);

        Response response = exportApiService.exportPoliciesThrottleGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    @Test
    public void exportPoliciesThrottleGetExceptionTest() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        Mockito.when(apiMgtAdminService.getApiPolicies()).thenThrow(
                new APIManagementException("Error while exporting policiesXXX", ExceptionCodes.APIMGT_DAO_EXCEPTION));

        LogManager.getRootLogger().setLevel(Level.INFO);
        ExportApiServiceImpl exportApiService = new ExportApiServiceImpl(apiMgtAdminService);
        Response response = exportApiService.exportPoliciesThrottleGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private Request getRequest() {
        return Mockito.mock(Request.class);
    }

}
