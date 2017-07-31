package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.UUID;

public class ApisApiServiceImplTestCase {

    private static final Logger logger = LoggerFactory.getLogger(ApisApiServiceImplTestCase.class);

    private ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();

    private TestUtil util = new TestUtil();

//    @Test(description = "delete api")
//    public void testApisApiIdDelete() throws NotFoundException, APIManagementException {
//        printTestMethodName();
//
//        APIPublisher apiPublisher = Mockito.mock(APIPublisher.class);
//
//        // Generate api id
//        String testApiId = UUID.randomUUID().toString();
//        Request request = util.getMockRequest();
//
//        Mockito.when(RestAPIPublisherUtil.getApiPublisher("admin"))
//                .thenReturn(APIManagerFactory.getInstance().getAPIProvider("admin"));
//
//        Mockito.when(apisApiService.apisApiIdDelete(testApiId, null, null, request)).
//                thenReturn(Response.status(200).build());
//        Assert.assertEquals(apisApiService.apisApiIdDelete
//                (testApiId, null, null, request).getStatus(), 200);
//    }
//
//    @Test(description = "get content of a document")
//    public void testApisApiIdDocumentsDocumentIdContentGet() throws NotFoundException {
//        printTestMethodName();
//
//        ApisApiServiceImpl apisApiService = Mockito.mock(ApisApiServiceImpl.class);
//
//        String apiId = UUID.randomUUID().toString();
//        Request request = util.getMockRequest();
//
//        Mockito.when(apisApiService.apisApiIdDocumentsDocumentIdContentGet
//                (apiId, "1", "application/json", null, null, request))
//                .thenReturn(Response.status(200).build());
//        Assert.assertEquals(apisApiService.apisApiIdDocumentsDocumentIdContentGet
//                        (apiId, "1", "application/json", null, null, request).getStatus(),
//                200);
//    }
//
//    @Test(description = "create single api")
//    public void testApiPost() throws NotFoundException, APIManagementException {
//        printTestMethodName();
//
//        APIPublisher apiPublisher = Mockito.mock(APIPublisher.class);
//        Mockito.when(RestAPIPublisherUtil.getApiPublisher("admin")).thenReturn(Mockito.mock(APIPublisher.class));
//        //Mockito.when(apiPublisher.addAPI())
//
//        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
//
//        String apiID = UUID.randomUUID().toString();
//
//        String contentType = "application/json";
//        Request request = util.getMockRequest();
//        APIDTO body = util.getAPIDTO(apiID, "TestAPI", "/testapi", "1.0.0");
//
//        apisApiService.apisPost(body, contentType, request);
//
////        Response response = apisApiService.apisPost(body, contentType, request);
////        Assert.assertEquals(response.getStatus(), 201);
//        //Assert.assertEquals(response.getEntity(), );
//    }
//
//    @Test
//    public void testApiGet() throws NotFoundException {
//        printTestMethodName();
//
////        ApisApiServiceImpl apisApiService = Mockito.mock(ApisApiServiceImpl.class);
////
////        Mockito.when(RestApiUtil.getLoggedInUsername()).thenReturn("testUser");
//    }


    private static void printTestMethodName() {
        logger.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
