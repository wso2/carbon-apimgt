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
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.rest.api.common.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.lcm.core.impl.LifecycleState;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestAPIPublisherUtil.class)
public class ApisApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testDeleteApi() throws Exception {
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
    public void testDeleteApiErrorCase() throws Exception {
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
    public void testApisApiIdDocumentsDocumentIdContentGetInline() throws Exception {
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

    @Test
    public void testApisApiIdDocumentsDocumentIdContentPostNotAllowed() throws Exception {
        String inlineContent = "INLINE CONTENT";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("swagger.json").getFile());
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiPublisher).
                uploadDocumentationFile(documentId, fis, "application/pdf");
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentPost(api1Id, documentId, "application/pdf",
                                                        fis, null, inlineContent, null, null, getRequest());
        fis.close();
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Only one of 'file' and 'inlineContent' " +
                                                            "should be specified"));
    }


    @Test
    public void testApisApiIdDocumentsDocumentIdContentPostNotFound() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        Mockito.doReturn(null).doThrow(new IllegalArgumentException()).when(apiPublisher).
                getDocumentationSummary(documentId);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentPost(api1Id, documentId, "application/pdf",
                        null, null, null, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Documentation not found"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentPostFile() throws Exception {
        String fileName = "swagger.json";
        String contentType = "text/json";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        FileInfo fileDetail = new FileInfo();
        fileDetail.setFileName(fileName);
        fileDetail.setContentType(contentType);
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo().fileName(fileName)
                                                            .sourceType(DocumentInfo.SourceType.FILE).build();
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException()).when(apiPublisher).
                getDocumentationSummary(documentId);
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiPublisher).
                uploadDocumentationFile(documentId, fis, contentType);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentPost(api1Id, documentId, null,
                        fis, fileDetail, null, null, null, getRequest());
        fis.close();
        assertEquals(response.getStatus(), 201);
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentPostFileWrongSource() throws Exception {
        String fileName = "swagger.json";
        String contentType = "text/json";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        FileInfo fileDetail = new FileInfo();
        fileDetail.setFileName(fileName);
        fileDetail.setContentType(contentType);
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo().fileName(fileName)
                .sourceType(DocumentInfo.SourceType.INLINE).build();
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException()).when(apiPublisher).
                getDocumentationSummary(documentId);
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiPublisher).
                uploadDocumentationFile(documentId, fis, contentType);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentPost(api1Id, documentId, null,
                        fis, fileDetail, null, null, null, getRequest());
        fis.close();
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("is not FILE"));

    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentPostInline() throws Exception {
        String inlineContent = "INLINE CONTENT";
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo()
                .sourceType(DocumentInfo.SourceType.INLINE).build();
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException()).when(apiPublisher).
                getDocumentationSummary(documentId);
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiPublisher).
                addDocumentationContent(documentId, inlineContent);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentPost(api1Id, documentId, null,
                        null, null, inlineContent, null, null, getRequest());
        assertEquals(response.getStatus(), 201);
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentPostInlineWrongSource() throws Exception {
        String inlineContent = "INLINE CONTENT";
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo()
                                            .sourceType(DocumentInfo.SourceType.FILE).build();
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException()).when(apiPublisher).
                getDocumentationSummary(documentId);
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiPublisher).
                addDocumentationContent(documentId, inlineContent);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentPost(api1Id, documentId, null,
                        null, null, inlineContent, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("is not INLINE"));

    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentPostWrongSource() throws Exception {
        String inlineContent = "INLINE CONTENT";
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo()
                .sourceType(DocumentInfo.SourceType.OTHER).build();
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException()).when(apiPublisher).
                getDocumentationSummary(documentId);
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiPublisher).
                addDocumentationContent(documentId, inlineContent);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentPost(api1Id, documentId, null,
                        null, null, null, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Either 'file' or 'inlineContent' should be specified"));

    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentPostAPIManagementException() throws Exception {
        String inlineContent = "INLINE CONTENT";
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String api1Id = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.DOCUMENT_CONTENT_NOT_FOUND))
                                                            .when(apiPublisher).getDocumentationSummary(documentId);
        Response response = apisApiService.
                apisApiIdDocumentsDocumentIdContentPost(api1Id, documentId, null,
                        null, null, null, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Document content not found"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdDelete() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiPublisher).removeDocumentation(documentId);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdDelete(apiId,
                                                                                documentId, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdDeleteErrorCase() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.DOCUMENT_NOT_FOUND))
                .when(apiPublisher).removeDocumentation(documentId);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdDelete(apiId, documentId,
                                                                                null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Document not found"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdGet() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo().build();
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException())
                                                    .when(apiPublisher).getDocumentationSummary(documentId);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdGet(apiId, documentId,
                null, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("Summary of Calculator Documentation"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdGetNotFound() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getDocumentationSummary(documentId);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdGet(apiId, documentId,
                null, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Documntation not found"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdGetException() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.INVALID_DOCUMENT_CONTENT_DATA))
                .when(apiPublisher).getDocumentationSummary(documentId);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdGet(apiId, documentId,
                null, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid document content data provided"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdPutNotFound() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo().build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getDocumentationSummary(documentId);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdPut(apiId, documentId,
                documentDTO, null, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Error while getting document"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdPutOtherTypeNameEmpty() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo().otherType("")
                                                                    .type(DocumentInfo.DocType.OTHER).build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getDocumentationSummary(documentId);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdPut(apiId, documentId,
                documentDTO, null, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("otherTypeName cannot be empty if type is OTHER."));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdPutUrlEmpty() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo().build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getDocumentationSummary(documentId);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdPut(apiId, documentId,
                documentDTO, null, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("Invalid document sourceUrl Format"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdPut() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        DocumentInfo documentInfo1 = SampleTestObjectCreator.createDefaultDocumentationInfo().build();
        DocumentInfo documentInfo2 = SampleTestObjectCreator.createDefaultDocumentationInfo().id(documentInfo1.getId())
                                        .summary("My new summary").build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo2);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(documentInfo1).doReturn(documentInfo2).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getDocumentationSummary(documentId);
        Mockito.doReturn("updated").doThrow(new IllegalArgumentException())
                .when(apiPublisher).updateDocumentation(apiId, documentInfo1);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdPut(apiId, documentId,
                documentDTO, null, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("My new summary"));
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdPutException() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        DocumentInfo documentInfo1 = SampleTestObjectCreator.createDefaultDocumentationInfo().build();
        DocumentInfo documentInfo2 = SampleTestObjectCreator.createDefaultDocumentationInfo().id(documentInfo1.getId())
                .summary("My new summary").build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo2);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String documentId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(documentInfo1).doReturn(documentInfo2).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getDocumentationSummary(documentId);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.INVALID_DOCUMENT_CONTENT_DATA))
                .when(apiPublisher).updateDocumentation(apiId, documentInfo1);
        Response response = apisApiService.apisApiIdDocumentsDocumentIdPut(apiId, documentId,
                documentDTO, null, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid document content data provided"));
    }

    @Test
    public void testApisApiIdDocumentsGet() throws Exception {
        printTestMethodName();
        Integer offset = 0;
        Integer limit = 10;
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        List<DocumentInfo> documentInfos = new ArrayList<>();
        documentInfos.add(SampleTestObjectCreator.createDefaultDocumentationInfo().name("NewName1").build());
        documentInfos.add(SampleTestObjectCreator.createDefaultDocumentationInfo().name("NewName2").build());
        Mockito.doReturn(documentInfos).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getAllDocumentation(apiId, offset, limit);
        Response response = apisApiService.apisApiIdDocumentsGet(apiId, 10,
                0, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("NewName1"));
    }

    @Test
    public void testApisApiIdDocumentsGetException() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.INVALID_DOCUMENT_CONTENT_DATA))
                .when(apiPublisher).getAllDocumentation(apiId, 0, 10);
        Response response = apisApiService.apisApiIdDocumentsGet(apiId, 10,
                0, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid document content data provided"));
    }

    @Test(expected = BadRequestException.class)
    public void testApisApiIdDocumentsPostEmptyOtherType() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo()
                                    .type(DocumentInfo.DocType.OTHER).otherType("").build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo);
        Response response = apisApiService.apisApiIdDocumentsPost(apiId, documentDTO,
                null, null, null, getRequest());
    }

    @Test(expected = BadRequestException.class)
    public void testApisApiIdDocumentsPostEmptySourceUrlType() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo()
                .sourceType(DocumentInfo.SourceType.URL).sourceURL("").build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo);
        Response response = apisApiService.apisApiIdDocumentsPost(apiId, documentDTO,
                null, null, null, getRequest());
    }

    @Test
    public void testApisApiIdDocumentsPost() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo()
                .sourceType(DocumentInfo.SourceType.INLINE).build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo);
        Mockito.doReturn(documentDTO.getDocumentId()).doThrow(new IllegalArgumentException())
                .when(apiPublisher).addDocumentationInfo(apiId, documentInfo);
        Mockito.doReturn(documentInfo).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getDocumentationSummary(documentDTO.getDocumentId());
        Mockito.doNothing().doThrow(new IllegalArgumentException())
                .when(apiPublisher).addDocumentationContent(documentDTO.getDocumentId(), "");
        Response response = apisApiService.apisApiIdDocumentsPost(apiId, documentDTO,
                null, null, null, getRequest());
        assertEquals(response.getStatus(), 201);
        assertTrue(response.getEntity().toString().contains(documentDTO.getDocumentId()));
    }

    @Test
    public void testApisApiIdDocumentsPostException() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo()
                .sourceType(DocumentInfo.SourceType.INLINE).build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.INVALID_DOCUMENT_CONTENT_DATA))
                .when(apiPublisher).addDocumentationInfo(apiId, documentInfo);
        Response response = apisApiService.apisApiIdDocumentsPost(apiId, documentDTO,
                null, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid document content data provided"));
    }

    @Test
    public void testApisApiIdGatewayConfigGet() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn("Sample Config").doThrow(new IllegalArgumentException())
                .when(apiPublisher).getApiGatewayConfig(apiId);
        Response response = apisApiService.apisApiIdGatewayConfigGet(apiId, null,
                null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("Sample Config"));
    }

    @Test
    public void testApisApiIdGatewayConfigGetException() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.GATEWAY_EXCEPTION))
                                .when(apiPublisher).getApiGatewayConfig(apiId);
        Response response = apisApiService.apisApiIdGatewayConfigGet(apiId, null,
                null, null, getRequest());
        assertEquals(response.getStatus(), 500);
        assertTrue(response.getEntity().toString().contains("Gateway publishing Error"));
    }


    @Test
    public void testApisApiIdGatewayConfigPut() throws Exception {
        printTestMethodName();
        String gatewayConfig = "Test Config";
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doNothing().doThrow(new IllegalArgumentException())
                .when(apiPublisher).updateApiGatewayConfig(apiId, gatewayConfig);
        Response response = apisApiService.apisApiIdGatewayConfigPut(apiId, gatewayConfig,
                null, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testApisApiIdGatewayConfigPutException() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        String gatewayConfig = "Test Config";
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.GATEWAY_EXCEPTION))
                .when(apiPublisher).updateApiGatewayConfig(apiId, gatewayConfig);
        Response response = apisApiService.apisApiIdGatewayConfigPut(apiId, gatewayConfig, null,
                null, null, getRequest());
        assertEquals(response.getStatus(), 500);
        assertTrue(response.getEntity().toString().contains("Gateway publishing Error"));
    }

    @Test
    public void testApisApiIdGetNotExist() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(false).doThrow(new IllegalArgumentException())
                .when(apiPublisher).checkIfAPIExists(apiId);
        Response response = apisApiService.apisApiIdGet(apiId, null,
                null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("API not found"));
    }

    @Test
    public void testApisApiIdGet() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        API api = SampleTestObjectCreator.createDefaultAPI().id(apiId).build();
        Mockito.doReturn(true).doThrow(new IllegalArgumentException())
                .when(apiPublisher).checkIfAPIExists(apiId);
        Mockito.doReturn(api).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getAPIbyUUID(apiId);
        Response response = apisApiService.apisApiIdGet(apiId, null,
                null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains(api.getId()));
        assertTrue(response.getEntity().toString().contains(api.getName()));
    }

    @Test
    public void testApisApiIdGetException() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.API_TYPE_INVALID))
                .when(apiPublisher).checkIfAPIExists(apiId);
        Response response = apisApiService.apisApiIdGet(apiId, null,
                null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("API Type specified is invalid"));
    }

    @Test
    public void testApisApiIdLifecycleGet() throws Exception {
        printTestMethodName();
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setLcName("APIMDefault");
        lifecycleState.setState("PUBLISHED");
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(lifecycleState).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getAPILifeCycleData(apiId);
        Response response = apisApiService.apisApiIdLifecycleGet(apiId, null,
                null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        LifecycleState lifecycleStateRetrieve = (LifecycleState) response.getEntity();
        assertEquals(lifecycleStateRetrieve.getLcName(), lifecycleState.getLcName());
        assertEquals(lifecycleStateRetrieve.getState(), lifecycleState.getState());
    }

    @Test
    public void testApisApiIdLifecycleGetException() throws Exception {
        printTestMethodName();
        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.API_TYPE_INVALID))
                .when(apiPublisher).getAPILifeCycleData(apiId);
        Response response = apisApiService.apisApiIdLifecycleGet(apiId, null,
                null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("API Type specified is invalid"));
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
