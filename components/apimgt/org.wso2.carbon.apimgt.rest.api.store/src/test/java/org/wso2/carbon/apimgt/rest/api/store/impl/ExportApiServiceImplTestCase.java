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
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;


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

    /*public void testExportApplicationsError() throws Exception {
        printTestMethodName();
        ExportApiServiceImpl exportApiService = new ExportApiServiceImpl();
        APIStore consumer = Mockito.mock(APIStoreImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER))
                .thenReturn(consumer);
        Response response= exportApiService.exportApplicationsGet("oihrhgeorig", getRequest());
        assertEquals(response.getStatus(),404);
    }*/

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