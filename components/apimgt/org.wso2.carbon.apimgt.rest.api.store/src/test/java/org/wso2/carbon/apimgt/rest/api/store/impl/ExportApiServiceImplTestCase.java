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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import java.time.Instant;
import java.time.LocalDateTime;
import javax.ws.rs.core.Response;

import static junit.framework.TestCase.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class ExportApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceImpl.class);
    private static String USER = "admin";

    @Test
    public void testExportApplicationsGetNotFound() throws Exception {
        printTestMethodName();
        ExportApiServiceImpl exportApiService = new ExportApiServiceImpl();
        APIStore consumer = Mockito.mock(APIStoreImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER))
                        .thenReturn(consumer);
        Response response= exportApiService.exportApplicationsGet(null, getRequest());
        assertEquals(response.getStatus(),404);
    }

    @Test
    public void testExportApplicationsGet() throws Exception {
        printTestMethodName();
        ExportApiServiceImpl exportApiService = new ExportApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        Application testApp = new Application("testApp", USER);
        testApp.setName("testApp");
        testApp.setId("testId");
        testApp.setDescription("testDesc");
        testApp.setPolicy(new Policy("50PerMin"));
        testApp.setCreatedUser("admin");
        testApp.setUpdatedUser("admin");
        testApp.setStatus("APPROVED");
        testApp.setCreatedTime(Instant.now());
        testApp.setUpdatedTime(Instant.now());
        testApp.setApplicationKeys(null);
        testApp.setPermissionString("7");
        testApp.setPermissionMap(null);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        Mockito.when(apiStore.getApplication("test", USER)).thenReturn(testApp);

        FileBasedApplicationImportExportManager importExportManager =
                Mockito.mock(FileBasedApplicationImportExportManager.class);

        Mockito.when(importExportManager.exportApplication(testApp, "testDir")).thenReturn("testPath");
        Response response = exportApiService.exportApplicationsGet("test", request);
        Assert.assertEquals(response.getStatus(), 200);
    }
    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Mockito.when(carbonMessage.getProperty("LOGGED_IN_USER")).thenReturn(USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
