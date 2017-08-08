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
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.RatingMappingUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class ApisApiServiceImplTestCase {

    private final static Logger logger = LoggerFactory.getLogger(ApisApiServiceImplTestCase.class);

    private static final String USER = "admin";
    private static final String contentType = "application/json";

    @Test
    public void testApisApiIdCommentsCommentIdDelete() throws NotFoundException, APIManagementException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String commentId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiStore)
                .deleteComment(commentId, apiId, USER);

        javax.ws.rs.core.Response response =
                apisApiService.apisApiIdCommentsCommentIdDelete
                        (commentId, apiId, null, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdCommentsCommentIdGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String commentId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Comment comment = new Comment();
        comment.setUuid(commentId);
        comment.setApiId(apiId);
        comment.setCommentedUser("commentedUser");
        comment.setCommentText("this is a comment");
        comment.setCreatedUser("createdUser");
        comment.setUpdatedUser("updatedUser");
        comment.setCreatedTime(LocalDateTime.now().minusHours(1));
        comment.setUpdatedTime(LocalDateTime.now());

        Mockito.when(apiStore.getCommentByUUID(commentId, apiId)).thenReturn(comment);

        Response response = apisApiService.apisApiIdCommentsCommentIdGet
                (commentId, apiId, null, null, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdCommentsGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Comment comment1 = new Comment();
        comment1.setUuid(UUID.randomUUID().toString());
        comment1.setCommentedUser("commentedUser1");
        comment1.setCommentText("this is a comment 1");
        comment1.setCreatedUser("createdUser1");
        comment1.setUpdatedUser("updatedUser1");
        comment1.setCreatedTime(LocalDateTime.now().minusHours(1));
        comment1.setUpdatedTime(LocalDateTime.now());

        Comment comment2 = new Comment();
        comment2.setUuid(UUID.randomUUID().toString());
        comment2.setCommentedUser("commentedUser2");
        comment2.setCommentText("this is a comment 2");
        comment2.setCreatedUser("createdUser2");
        comment2.setUpdatedUser("updatedUser2");
        comment2.setCreatedTime(LocalDateTime.now().minusHours(1));
        comment2.setUpdatedTime(LocalDateTime.now());

        List<Comment> commentList = new ArrayList<>();
        commentList.add(comment1);
        commentList.add(comment2);

        Mockito.when(apiStore.getCommentsForApi(apiId)).thenReturn(commentList);
        Response response = apisApiService.apisApiIdCommentsGet(apiId, 3, 0, contentType, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdCommentsCommentIdPut() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String commentId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setApiId(apiId);
        commentDTO.setCommentText("comment text");
        commentDTO.setCreatedBy("creater");
        commentDTO.setLastUpdatedBy("updater");

        Comment comment = new Comment();
        comment.setCommentedUser("commentedUser");
        comment.setCommentText("this is a comment");
        comment.setCreatedUser("createdUser");
        comment.setUpdatedUser("updatedUser");
        comment.setCreatedTime(LocalDateTime.now().minusHours(1));
        comment.setUpdatedTime(LocalDateTime.now());

        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiStore)
                .updateComment(comment, commentId, apiId, USER);
        Mockito.when(apiStore.getCommentByUUID(commentId, apiId)).thenReturn(comment);

        Response response = apisApiService.apisApiIdCommentsCommentIdPut
                (commentId, apiId, commentDTO, contentType, null, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdContentGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        String documentIdFile = UUID.randomUUID().toString();
        String documentIdInline = UUID.randomUUID().toString();

        DocumentInfo documentInfoFile =
                TestUtil.createAPIDoc(documentIdFile, "documentInfoFile", "", "API1 documentation file", DocumentInfo.DocType.HOWTO,
                        "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.PRIVATE);
        DocumentInfo documentInfoInline =
                TestUtil.createAPIDoc(documentIdInline, "documentInfoInline", "", "API1 documentation inline", DocumentInfo.DocType.HOWTO,
                        "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);

        DocumentContent documentContentFIle = TestUtil.createDocContent(documentInfoFile, "Sample inline content for API1 DOC 1", null);
        DocumentContent documentContentInline = TestUtil.createDocContent(documentInfoInline, "Sample inline content for API1 DOC 2", null);

        Mockito.when(apiStore.getDocumentationContent(documentIdFile)).thenReturn(documentContentFIle);
        Mockito.when(apiStore.getDocumentationContent(documentIdInline)).thenReturn(documentContentInline);

        Response responseFile = apisApiService.apisApiIdDocumentsDocumentIdContentGet
                (apiId, documentIdFile, contentType, null, null, getRequest());
        Response responseInline = apisApiService.apisApiIdDocumentsDocumentIdContentGet
                (apiId, documentIdInline, contentType, null, null, getRequest());

        Assert.assertEquals(200, responseFile.getStatus());
        Assert.assertEquals(200, responseInline.getStatus());
    }

    @Test
    public void testApisApiIdDocumentsDocumentIdGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        DocumentInfo documentInfoFile =
                TestUtil.createAPIDoc(documentId, "documentInfo", "", "API1 documentation file", DocumentInfo.DocType.HOWTO,
                        "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.PRIVATE);

        Mockito.when(apiStore.getDocumentationSummary(documentId)).thenReturn(documentInfoFile);

        Response response = apisApiService.apisApiIdDocumentsDocumentIdGet
                (apiId, documentId, contentType, null, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdDocumentsGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        DocumentInfo documentInfo1 =
                TestUtil.createAPIDoc(UUID.randomUUID().toString(), "documentInfo1", "", "API1 documentation 1", DocumentInfo.DocType.HOWTO,
                        "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.PRIVATE);
        DocumentInfo documentInfo2 =
                TestUtil.createAPIDoc(UUID.randomUUID().toString(), "documentInfo2", "", "API1 documentation 2", DocumentInfo.DocType.HOWTO,
                        "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.PRIVATE);

        List<DocumentInfo> documentInfoList = new ArrayList<>();
        documentInfoList.add(documentInfo1);
        documentInfoList.add(documentInfo2);

        Mockito.when(apiStore.getAllDocumentation(apiId, 0, 10)).thenReturn(documentInfoList);

        Response response = apisApiService.apisApiIdDocumentsGet
                (apiId, 10, 0, contentType, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Endpoint api1SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api1ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api = TestUtil.createApi("provider1", apiId, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                TestUtil.createEndpointTypeToIdMap(api1SandBoxEndpointId, api1ProdEndpointId)).build();

        Mockito.when(apiStore.getAPIbyUUID(apiId)).thenReturn(api);

        Response response = apisApiService.apisApiIdGet(apiId, contentType, null, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdRatingsGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String rateId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Rating rating = new Rating();
        rating.setApiId(apiId);
        rating.setRating(5);
        rating.setUsername(USER);
        rating.setUuid(rateId);
        rating.setCreatedUser(USER);
        rating.setCreatedTime(LocalDateTime.now().minusHours(2));
        rating.setLastUpdatedUser(USER);
        rating.setLastUpdatedTime(LocalDateTime.now());

        Mockito.when(apiStore.getRatingForApiFromUser(apiId, USER)).thenReturn(rating);
        Mockito.when(apiStore.getAvgRating(apiId)).thenReturn(5.0);

        Rating rating1 = new Rating();
        rating1.setApiId(apiId);
        rating1.setRating(3);
        rating1.setUsername(USER);
        rating1.setUuid(rateId);
        rating1.setCreatedUser(USER);
        rating1.setCreatedTime(LocalDateTime.now().minusHours(2));
        rating1.setLastUpdatedUser(USER);
        rating1.setLastUpdatedTime(LocalDateTime.now());

        List<Rating> ratingList = new ArrayList<>();
        ratingList.add(rating);
        ratingList.add(rating1);

        Mockito.when(apiStore.getRatingsListForApi(apiId)).thenReturn(ratingList);

        Response response = apisApiService.apisApiIdRatingsGet(apiId, 10, 0, contentType, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdRatingsRatingIdGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String rateId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Rating rating = new Rating();
        rating.setApiId(apiId);
        rating.setRating(5);
        rating.setUsername(USER);
        rating.setUuid(rateId);
        rating.setCreatedUser(USER);
        rating.setCreatedTime(LocalDateTime.now().minusHours(2));
        rating.setLastUpdatedUser(USER);
        rating.setLastUpdatedTime(LocalDateTime.now());

        Mockito.when(apiStore.getRatingByUUID(apiId, rateId)).thenReturn(rating);

        Response response = apisApiService.apisApiIdRatingsGet(apiId, 10, 0, contentType, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdUserRatingPut() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String rateId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Rating rating = new Rating();
        rating.setApiId(apiId);
        rating.setRating(5);
        rating.setUsername(USER);
        rating.setUuid(rateId);
        rating.setCreatedUser(USER);
        rating.setCreatedTime(LocalDateTime.now().minusHours(2));
        rating.setLastUpdatedUser(USER);
        rating.setLastUpdatedTime(LocalDateTime.now());

        RatingDTO ratingDTO = RatingMappingUtil.fromRatingToDTO(rating);

        Rating ratingNow = new Rating();
        ratingNow.setApiId(apiId);
        ratingNow.setRating(3);
        ratingNow.setUsername(USER);
        ratingNow.setUuid(rateId);
        ratingNow.setCreatedUser(USER);
        ratingNow.setCreatedTime(LocalDateTime.now().minusHours(2));
        ratingNow.setLastUpdatedUser(USER);
        ratingNow.setLastUpdatedTime(LocalDateTime.now());

        Mockito.when(apiStore.getRatingForApiFromUser(apiId, USER)).thenReturn(ratingNow);
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiStore)
                .updateRating(apiId, ratingNow.getUuid(), rating);
        Mockito.when(apiStore.getRatingByUUID(apiId, ratingNow.getUuid())).thenReturn(ratingNow);

        Response response = apisApiService.apisApiIdUserRatingPut(apiId, ratingDTO, contentType, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisApiIdSwaggerGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Mockito.when(apiStore.getApiSwaggerDefinition(apiId)).thenReturn("SWAGGER DEFINITION");

        Response response = apisApiService.apisApiIdSwaggerGet(apiId, contentType, null, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApisGet() throws APIManagementException, NotFoundException {
        printTestMethodName();
        String apiId = UUID.randomUUID().toString();

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Endpoint api1SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api1ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api = TestUtil.createApi("provider1", apiId, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                TestUtil.createEndpointTypeToIdMap(api1SandBoxEndpointId, api1ProdEndpointId)).build();

        List<API> apiList = new ArrayList<>();
        apiList.add(api);

        Mockito.when(apiStore.searchAPIs("", 0, 1)).thenReturn(apiList);

        Response response = apisApiService.apisGet(10, 0, "", contentType, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    // Sample request to be used by tests
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
