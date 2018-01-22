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
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import javax.ws.rs.core.Response;

import static junit.framework.TestCase.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class ImportApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(ImportApiServiceImpl.class);
    private static String USER = "admin";

    @Test
    public void testImportApplicationsPostError() throws Exception {
        printTestMethodName();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("sampleApp.json").getFile());
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        ImportApiServiceImpl importApiService = new ImportApiServiceImpl();
        APIStore consumer = Mockito.mock(APIStoreImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER))
                .thenReturn(consumer);
        Response response = importApiService.importApplicationsPost(fis, null, getRequest());
        assertEquals(response.getStatus(), 500);
    }

    @Test
    public void testImportApplicationsPost() throws Exception {
        printTestMethodName();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("sampleApp.zip").getFile());
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        ImportApiServiceImpl importApiService = new ImportApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStore.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName("sampleApp.zip");
        fileInfo.setContentType("application/zip");
        Response response = importApiService.importApplicationsPost(fis, fileInfo, request);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testImportApplicationsPutError() throws Exception {
        printTestMethodName();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("sampleApp.json").getFile());
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        ImportApiServiceImpl importApiService = new ImportApiServiceImpl();
        APIStore consumer = Mockito.mock(APIStoreImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER))
                .thenReturn(consumer);
        Response response = importApiService.importApplicationsPut(fis, null, getRequest());
        assertEquals(response.getStatus(), 500);
    }
    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Mockito.when(carbonMessage.getProperty("LOGGED_IN_USER")).thenReturn(USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    private static void printTestMethodName() {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}

