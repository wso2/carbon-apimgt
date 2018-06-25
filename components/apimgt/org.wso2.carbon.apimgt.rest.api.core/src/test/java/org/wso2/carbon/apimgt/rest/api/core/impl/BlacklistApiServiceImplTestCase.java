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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.rest.api.core.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class BlacklistApiServiceImplTestCase {

    @Test
    public void blacklistGetTestCase() throws Exception {

        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl(adminService);

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
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl(adminService);

        String message = "Error while retrieving applications.";

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.APPLICATION_INACTIVE);
        Mockito.when(adminService.getBlockConditions()).thenThrow(apiManagementException);

        Response response = blacklistApiService.blacklistGet(null, getRequest());
        Assert.assertEquals(response.getStatus(), 400);

    }

    private Request getRequest() {
        return Mockito.mock(Request.class);
    }
}
