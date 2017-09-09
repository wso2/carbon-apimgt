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
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.impl.BlacklistApiServiceImpl;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.BlockingConditionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.apimgt.core.util.APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, BlockingConditionMappingUtil.class})
public class BlacklistApiServiceImplTest {
    private final static Logger logger = LoggerFactory.getLogger(BlacklistApiServiceImplTest.class);

    @Test
    public void blacklistConditionIdDeleteTest()  throws NotFoundException, APIManagementException {
        printTestMethodName();
        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl();
        String uuid = UUID.randomUUID().toString();

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        Mockito.doReturn(true).doThrow(new IllegalArgumentException())
                .when(adminService).deleteBlockConditionByUuid(uuid);
        Response response = blacklistApiService.blacklistConditionIdDelete(uuid, null,
                                                                           null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void blacklistConditionIdGetTest() throws APIManagementException, NotFoundException   {
        printTestMethodName();
        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl();
        String uuid = UUID.randomUUID().toString();

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        BlockConditions conditions = new BlockConditions();
        conditions.setUuid(uuid);
        Mockito.doReturn(conditions).doThrow(new IllegalArgumentException()).when(adminService)
                .getBlockConditionByUUID(uuid);
        Response response = blacklistApiService.blacklistConditionIdGet(uuid, null, null,
                                                                        getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void blacklistGetTest() throws APIManagementException, NotFoundException {
        printTestMethodName();
        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        BlockConditions conditions1 = new BlockConditions();
        conditions1.setUuid(uuid);
        BlockConditions conditions2 = new BlockConditions();
        conditions2.setUuid(UUID.randomUUID().toString());
        List<BlockConditions> list = new ArrayList<>();
        list.add(conditions1);
        list.add(conditions2);
        Mockito.doReturn(list).doThrow(new IllegalArgumentException()).when(adminService).getBlockConditions();

        Response response = blacklistApiService.blacklistGet(null, null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);

    }

    @Test
    public void blacklistPostTest() throws APIManagementException, NotFoundException   {
        printTestMethodName();
        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setConditionId(uuid);
        dto.setStatus(true);
        dto.setConditionType(BLOCKING_CONDITIONS_IP);
        dto.setConditionValue("12.32.45.3");

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.mockStatic(BlockingConditionMappingUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        BlockConditions conditions = BlockingConditionMappingUtil.fromBlockingConditionDTOToBlockCondition(dto);
        Mockito.doReturn(uuid).doThrow(new IllegalArgumentException()).when(adminService).addBlockCondition(conditions);
        Mockito.doReturn(conditions).doThrow(new IllegalArgumentException()).when(adminService)
                .getBlockConditionByUUID(uuid);
        PowerMockito.when(BlockingConditionMappingUtil.fromBlockingConditionDTOToBlockCondition(dto))
                .thenReturn(conditions);
        Response response = blacklistApiService.blacklistPost(dto, getRequest());
        Assert.assertEquals(201, response.getStatus());
    }

    @Test
    public void blacklistConditionIdPutTest()   throws APIManagementException, NotFoundException {
        printTestMethodName();
        BlacklistApiServiceImpl blacklistApiService = new BlacklistApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setConditionId(UUID.randomUUID().toString());
        dto.setStatus(true);
        dto.setConditionType(BLOCKING_CONDITIONS_IP);
        dto.setConditionValue("12.32.45.3");
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        BlockConditions conditions = BlockingConditionMappingUtil.fromBlockingConditionDTOToBlockCondition(dto);
        Mockito.doReturn(true).doThrow(new IllegalArgumentException()).when(adminService)
                .updateBlockConditionStateByUUID(uuid, true);
        Mockito.doReturn(conditions).doThrow(new IllegalArgumentException()).when(adminService)
                .getBlockConditionByUUID(uuid);

        Response response = blacklistApiService.blacklistConditionIdPut(uuid, dto, null, null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);

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


