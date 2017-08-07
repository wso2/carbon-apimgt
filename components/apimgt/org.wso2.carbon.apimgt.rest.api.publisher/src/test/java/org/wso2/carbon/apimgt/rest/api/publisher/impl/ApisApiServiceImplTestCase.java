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
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIPublisherImpl;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestAPIPublisherUtil.class)
public class ApisApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testDeleteApi () throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiPublisher).deleteAPI(api1Id);
        Response response = apisApiService.apisApiIdDelete(api1Id, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDeleteApiErrorCase () throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.API_NOT_FOUND))
                                                                               .when(apiPublisher).deleteAPI(api1Id);
        Response response = apisApiService.apisApiIdDelete(api1Id, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("API not found"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentGetInline () throws Exception {
        String inlineContent = "INLINE CONTENT";
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo().build();
        DocumentContent documentContent = DocumentContent.newDocumentContent().inlineContent(inlineContent)
                                            .documentInfo(documentInfo).build();
        Mockito.doReturn(documentContent).doThrow(new IllegalArgumentException()).when(apiPublisher).
                                                        getDocumentationContent(documentId);
        Response response = apisApiService.
                            apisApiIdDocumentsDocumentIdContentGet(api1Id, documentId, null, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertEquals(inlineContent, response.getEntity().toString());
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentGetFile() throws Exception {
        String fileName = "mytext.txt";
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo()
                                    .sourceType(DocumentInfo.SourceType.FILE).fileName(fileName).build();
        DocumentContent documentContent = DocumentContent.newDocumentContent().documentInfo(documentInfo).build();
        Mockito.doReturn(documentContent).doThrow(new IllegalArgumentException()).when(apiPublisher).
                                            getDocumentationContent(documentId);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentGet(api1Id, documentId, null, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getStringHeaders().get("Content-Disposition").toString().contains(fileName));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentGetErrorAPIManagementException() throws Exception {
        String fileName = "mytext.txt";
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error Occurred",
                ExceptionCodes.DOCUMENT_CONTENT_NOT_FOUND)).when(apiPublisher).
                getDocumentationContent(documentId);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentGet(api1Id, documentId, null, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Document content not found"));
    }

    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
