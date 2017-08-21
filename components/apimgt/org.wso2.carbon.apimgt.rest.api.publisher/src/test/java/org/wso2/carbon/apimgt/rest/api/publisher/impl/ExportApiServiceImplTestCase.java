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
package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.impl.APIPublisherImpl;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIDetails;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.FileBasedApiImportExportManager;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestAPIPublisherUtil.class)
public class ExportApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testExportApisGetNotFound() throws Exception {
        printTestMethodName();
        ExportApiServiceImpl exportApiService = new ExportApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Response response = exportApiService.
                exportApisGet("", null, null, getRequest());
        assertEquals(response.getStatus(), 404);
    }

    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = new HTTPCarbonMessage();
        carbonMessage.setProperty("LOGGED_IN_USER", USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
