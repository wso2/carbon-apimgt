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

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.rest.api.core.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIManagerFactory.class)
public class BlacklistApiServiceImplTestCase {

    @Test
    public void blacklistGetTestCase() throws Exception {

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getAPIMgtAdminService()).thenReturn(adminService);

        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl();

        BlockConditions blockConditionOne = SampleTestObjectCreator.createUniqueBlockConditions("IP_RANGE");
        BlockConditions blockConditionTwo = SampleTestObjectCreator.createUniqueBlockConditions("IP");
        BlockConditions blockConditionThree = SampleTestObjectCreator.createUniqueBlockConditions("IP_RANGE");

        List<BlockConditions> blockConditions = new ArrayList<>();
        blockConditions.add(blockConditionOne);
        blockConditions.add(blockConditionTwo);
        blockConditions.add(blockConditionThree);

        Mockito.when(adminService.getBlockConditions()).thenReturn(blockConditions);

        Response response = blacklistApiService.blacklistGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(((BlockingConditionListDTO) response.getEntity()).getList().size(), 3);
    }

    @Test
    public void applicationsGetExceptionTestCase() throws Exception {

        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl();

        String message = "Error while retrieving applications.";

        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.APPLICATION_INACTIVE);
        Mockito.when(instance.getAPIMgtAdminService()).thenThrow(apiManagementException);

        Response response = blacklistApiService.blacklistGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), 400);

    }

    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }
}
