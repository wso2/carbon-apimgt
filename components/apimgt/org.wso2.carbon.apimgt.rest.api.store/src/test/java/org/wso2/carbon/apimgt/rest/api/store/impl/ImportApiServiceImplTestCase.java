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
import org.wso2.msf4j.formparam.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import javax.ws.rs.core.Response;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class ImportApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(ImportApiServiceImpl.class);
    private static String USER ="admin";

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
        Response response= importApiService.importApplicationsPost(fis,null,getRequest());
        assertEquals(response.getStatus(),500);
    }

//    @Test
//    public void testImportApplicationsPost() throws Exception {
//        printTestMethodName();
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(classLoader.getResource("sampleApp.zip").getFile());
//        FileInputStream fis = null;
//        fis = new FileInputStream(file);
//        ImportApiServiceImpl importApiService = new ImportApiServiceImpl();
//        APIStore consumer = Mockito.mock(APIStore.class);
//        PowerMockito.mockStatic(RestApiUtil.class);
//        PowerMockito.when(RestApiUtil.getConsumer(USER))
//                .thenReturn(consumer);
//        FileInfo fileInfo = new FileInfo();
//        fileInfo.setFileName("sampleApp.zip");
//        fileInfo.setContentType("application/zip");
//        Response response= importApiService.importApplicationsPost(fis,fileInfo,getRequest());
//        assertEquals(response.getStatus(),200);
//    }



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
        Response response= importApiService.importApplicationsPut(fis,null,getRequest());
        assertEquals(response.getStatus(),500);
    }

//    @Test
//    public void testImportApplicationsPut() throws Exception {
//        printTestMethodName();
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(classLoader.getResource("sampleApp.json").getFile());
//        FileInputStream fis = null;
//        fis = new FileInputStream(file);
//        ImportApiServiceImpl importApiService = new ImportApiServiceImpl();
//        APIStore consumer = Mockito.mock(APIStoreImpl.class);
//        PowerMockito.mockStatic(RestApiUtil.class);
//        PowerMockito.when(RestApiUtil.getConsumer(USER))
//                .thenReturn(consumer);
//        FileInfo fileInfo = new FileInfo();
//        fileInfo.setFileName("exportedApps.zip");
//        fileInfo.setContentType("application/zip");
//        Response response= importApiService.importApplicationsPut(fis,fileInfo,getRequest());
//        assertEquals(response.getStatus(),200);
//    }

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

