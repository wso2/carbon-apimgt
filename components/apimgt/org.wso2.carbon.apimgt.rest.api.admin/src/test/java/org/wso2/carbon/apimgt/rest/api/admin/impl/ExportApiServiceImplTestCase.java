/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

import static org.powermock.api.mockito.PowerMockito.doThrow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, APIUtil.class})
public class ExportApiServiceImplTestCase {
    private final String USER = "admin";
    private ExportApiService exportApiService;
    private APIConsumer apiConsumer;

    @Before
    public void init() throws Exception {
        exportApiService = new ExportApiServiceImpl();
        apiConsumer = Mockito.mock(APIConsumer.class);
    }

    /**
     * This method tests the functionality of ExportApplicationsGet, when there exist no application
     * is not found with given parameters
     *
     * @throws Exception Exception.
     */
    @Test
    public void testExportApplicationsGetNotFound() throws Exception {

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiConsumer);
        Response response = exportApiService.exportApplicationsGet(null, null);
        Assert.assertEquals(response.getStatus(), 404);
    }

    /**
     * This method tests the functionality of ExportApplicationsGet, for a cross tenant application
     * export attempt
     *
     * @throws Exception Exception.
     */
    @Test (expected = ForbiddenException.class)
    public void testExportApplicationsGetCrossTenantError() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiConsumer);
        PowerMockito.mockStatic(APIUtil.class);
        Application testApp = new Application("sampleApp", new Subscriber("admin@hr.lk"));
        Subscriber subscriber = new Subscriber("admin@hr.lk");
        Mockito.when(apiConsumer.getSubscriber("admin@hr.lk")).thenReturn(subscriber);
        Mockito.when(APIUtil.getApplicationId("sampleApp", "admin@hr.lk")).thenReturn(1);
        Mockito.when(apiConsumer.getApplicationById(1)).thenReturn(testApp);

        PowerMockito.doThrow(new ForbiddenException()).when(RestApiUtil.class);
        RestApiUtil.handleMigrationSpecificPermissionViolations("hr.lk","admin");//this do define the mocking method
        exportApiService.exportApplicationsGet("sampleApp", "admin@hr.lk");
    }

    /**
     * This method tests the functionality of ExportApplicationsGet, for a successful export of an application
     *
     * @throws Exception Exception.
     */
    @Test
    public void testExportApplicationsGet() throws Exception {
        Application testApp = new Application("sampleApp", new Subscriber("admin"));
        testApp.setId(1);
        testApp.setUUID("testUUID");
        testApp.setDescription("testDesc");
        testApp.setStatus("APPROVED");
        testApp.setCreatedTime("testDateTime");
        testApp.setLastUpdatedTime("testDateTime");
        testApp.setGroupId("testId");
        testApp.setCallbackUrl("testURL");
        testApp.setIsBlackListed(false);
        testApp.setTier("Unlimited");
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiConsumer);
        Subscriber subscriber = new Subscriber("admin");
        Mockito.when(apiConsumer.getSubscriber("admin")).thenReturn(subscriber);
        Mockito.when(APIUtil.getApplicationId("sampleApp", "admin")).thenReturn(1);
        Mockito.when(apiConsumer.getApplicationById(1)).thenReturn(testApp);
        Response response = exportApiService.exportApplicationsGet("sampleApp", "admin");
        Assert.assertEquals(response.getStatus(), 200);
    }
}