/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 * /
 */

package org.wso2.carbon.apimgt.rest.api.admin.throttling.impl;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.impl.LabelsApiServiceImpl;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class LabelsApiServiceImplTest {
    private final static Logger logger = LoggerFactory.getLogger(LabelsApiServiceImplTest.class);

    @Test
    public void labelsLabelIdDeleteTest()   throws NotFoundException, APIManagementException {
        printTestMethodName();

        String labelId = UUID.randomUUID().toString();
        LabelsApiServiceImpl labelsApiService = new LabelsApiServiceImpl();
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(adminService).deleteLabel(labelId);
        javax.ws.rs.core.Response response =
                labelsApiService.labelsLabelIdDelete(labelId, null, null, getRequest());

        Assert.assertEquals(response.getStatus(),200);
    }

    @Test
    public void labelsLabelIdDeleteTestException()    throws NotFoundException, APIManagementException {
        printTestMethodName();

        String labelId = UUID.randomUUID().toString();
        LabelsApiServiceImpl labelsApiService = new LabelsApiServiceImpl();
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        Mockito.doThrow(new APIManagementException("Error", ExceptionCodes.APIMGT_DAO_EXCEPTION)).when(adminService).deleteLabel(labelId);
        javax.ws.rs.core.Response response =
                labelsApiService.labelsLabelIdDelete(labelId, null, null, getRequest());

        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void labelsLabelIdDeleteTestNullLabel()    throws NotFoundException, APIManagementException {
        printTestMethodName();
        LabelsApiServiceImpl labelsApiService = new LabelsApiServiceImpl();
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        javax.ws.rs.core.Response response =
                labelsApiService.labelsLabelIdDelete(null, null, null, getRequest());

        Assert.assertEquals(400, response.getStatus());
    }


    private Request getRequest() throws APIMgtSecurityException {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);

        try {
            PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        } catch (Exception e) {
            throw new APIMgtSecurityException("Error while mocking Request Object ", e);
        }
        return request;
    }

    private static void printTestMethodName() {
        logger.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                    " ------------------");
    }
}
