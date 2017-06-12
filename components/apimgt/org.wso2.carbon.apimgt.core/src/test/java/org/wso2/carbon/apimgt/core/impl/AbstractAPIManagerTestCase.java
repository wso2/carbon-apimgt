/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.core.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Subscription;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractAPIManagerTestCase {

    private static final String USER_NAME = "username";
    private static final String API_VERSION = "1.0.0";
    private static final String PROVIDER_NAME = "provider";
    private static final String API_NAME = "api_name";
    private static final String API_CONTEXT = "/testapi";
    private static final String APP_NAME = "app_name";
    private static final String LAST_UPDATED_TIME = "2017-03-19T13:45:30";
    public static final String UUID = "7a2298c4-c905-403f-8fac-38c73301631f";
    public static final String DOC_ID = "docId";
    public static final String LABEL_NAME = "testLabel";

    @Test(description = "Search API by UUID")
    public void testSearchAPIByUUID() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiDAO);
        API apiFromDAO = new API.APIBuilder(PROVIDER_NAME, API_NAME, API_VERSION).build();
        when(apiDAO.getAPI(UUID)).thenReturn(apiFromDAO);
        API api = apiStore.getAPIbyUUID(UUID);
        Assert.assertEquals(api.getName(), API_NAME);
        verify(apiDAO, atLeastOnce()).getAPI(UUID);
    }

    @Test(description = "Retrieve an application by uuid")
    public void testGetApplicationByUuid() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(applicationDAO);
        Application applicationFromDAO = new Application(APP_NAME, USER_NAME);
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        Application application = apiStore.getApplication(UUID, USER_NAME);
        Assert.assertNotNull(application);
        verify(applicationDAO, times(1)).getApplication(UUID);
    }

    @Test(description = "Retrieve documentation summary given the id")
    public void testGetDocumentationSummary() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiDAO);
        DocumentInfo documentInfoMock = SampleTestObjectCreator.getMockDocumentInfoObject(UUID);
        when(apiDAO.getDocumentInfo(UUID)).thenReturn(documentInfoMock);
        DocumentInfo documentInfo = apiStore.getDocumentationSummary(UUID);
        Assert.assertNotNull(documentInfo);
        verify(apiDAO, times(1)).getDocumentInfo(UUID);
    }

    @Test(description = "Retrieve list of documentations")
    public void testAllDocumentation() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiDAO);
        List<DocumentInfo> documentInfoMockList = SampleTestObjectCreator.getMockDocumentInfoObjectsList();
        when(apiDAO.getDocumentsInfoList(UUID)).thenReturn(documentInfoMockList);
        List<DocumentInfo> documentInfoList = apiStore.getAllDocumentation(UUID, 1, 10);
        Assert.assertNotNull(documentInfoList);
        verify(apiDAO, times(1)).getDocumentsInfoList(UUID);
    }

    @Test(description = "Getting last updated time of API")
    public void testGetLastUpdatedTimeOfAPI() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfAPI(UUID)).thenReturn(LAST_UPDATED_TIME);
        apiPublisher.getLastUpdatedTimeOfAPI(UUID);
        verify(apiDAO, times(1)).getLastUpdatedTimeOfAPI(UUID);
    }

    @Test(description = "Getting last updated time of Swagger Definition")
    public void testGetLastUpdatedTimeOfSwaggerDefinition() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfSwaggerDefinition(UUID)).thenReturn(LAST_UPDATED_TIME);
        apiPublisher.getLastUpdatedTimeOfSwaggerDefinition(UUID);
        verify(apiDAO, times(1)).getLastUpdatedTimeOfSwaggerDefinition(UUID);
    }

    @Test(description = "Check if context exist when context is null")
    public void testIsContextExistWhenContextNull() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        Boolean isExists = apiPublisher.isContextExist("");
        Assert.assertEquals(isExists, Boolean.FALSE);
    }

    @Test(description = "Get swagger definition for API")
    public void testGetSwagger20Definition() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        String swaggerDefinition = SampleTestObjectCreator.apiDefinition;
        when(apiDAO.getApiSwaggerDefinition(UUID)).thenReturn(swaggerDefinition);
        apiPublisher.getApiSwaggerDefinition(UUID);
        verify(apiDAO, times(1)).getApiSwaggerDefinition(UUID);
    }

    @Test(description = "Get subscription by UUID")
    public void testGetSubscriptionByUUID() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiSubscriptionDAO);
        when(apiSubscriptionDAO.getAPISubscription(UUID)).thenReturn(new Subscription(UUID, null, null, null));
        apiStore.getSubscriptionByUUID(UUID);
        verify(apiSubscriptionDAO, times(1)).getAPISubscription(UUID);
    }

    @Test(description = "Getting last updated time of Document")
    public void testGetLastUpdatedTimeOfDocument() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfDocument(DOC_ID)).thenReturn(LAST_UPDATED_TIME);
        apiPublisher.getLastUpdatedTimeOfDocument(DOC_ID);
        verify(apiDAO, times(1)).getLastUpdatedTimeOfDocument(DOC_ID);
    }

    @Test(description = "Getting last updated time of Document content")
    public void testGetLastUpdatedTimeOfDocumentContent() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfDocumentContent(UUID, DOC_ID)).thenReturn(LAST_UPDATED_TIME);
        apiPublisher.getLastUpdatedTimeOfDocumentContent(UUID, DOC_ID);
        verify(apiDAO, times(1)).getLastUpdatedTimeOfDocumentContent(UUID, DOC_ID);
    }

    @Test(description = "Getting last updated time of API Thumbnail Image")
    public void testGetLastUpdatedTimeOfAPIThumbnailImage() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfAPIThumbnailImage(UUID)).thenReturn(LAST_UPDATED_TIME);
        apiPublisher.getLastUpdatedTimeOfAPIThumbnailImage(UUID);
        verify(apiDAO, times(1)).getLastUpdatedTimeOfAPIThumbnailImage(UUID);
    }

    @Test(description = "Getting last updated time of Application")
    public void testGetLastUpdatedTimeOfApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(applicationDAO);
        when(applicationDAO.getLastUpdatedTimeOfApplication(UUID)).thenReturn(LAST_UPDATED_TIME);
        apiStore.getLastUpdatedTimeOfApplication(UUID);
        verify(applicationDAO, times(1)).getLastUpdatedTimeOfApplication(UUID);
    }

    @Test(description = "Getting last updated time of Subscription")
    public void testGetLastUpdatedTimeOfSubscription() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiSubscriptionDAO);
        when(apiSubscriptionDAO.getLastUpdatedTimeOfSubscription(UUID)).thenReturn(LAST_UPDATED_TIME);
        apiStore.getLastUpdatedTimeOfSubscription(UUID);
        verify(apiSubscriptionDAO, times(1)).getLastUpdatedTimeOfSubscription(UUID);
    }

    @Test(description = "Getting subscriptions by API")
    public void testGetSubscriptionsByAPI() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiSubscriptionDAO);
        when(apiSubscriptionDAO.getAPISubscriptionsByAPI(UUID)).thenReturn(new ArrayList<Subscription>());
        apiStore.getSubscriptionsByAPI(UUID);
        verify(apiSubscriptionDAO, times(1)).getAPISubscriptionsByAPI(UUID);
    }

    @Test(description = "Getting Documentation content when source type is FILE")
    public void testGetDocumentationContentFile() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        DocumentInfo.Builder builder = new DocumentInfo.Builder();
        builder.name("CalculatorDoc");
        builder.sourceType(DocumentInfo.SourceType.FILE);
        DocumentInfo documentInfo = builder.build();
        when(apiDAO.getDocumentInfo(DOC_ID)).thenReturn(documentInfo);
        String stream = "This is sample file content";
        InputStream inputStream = new ByteArrayInputStream(stream.getBytes());
        when(apiDAO.getDocumentFileContent(DOC_ID)).thenReturn(inputStream);
        apiPublisher.getDocumentationContent(DOC_ID);
        verify(apiDAO, times(1)).getDocumentFileContent(DOC_ID);
    }

    @Test(description = "Getting Documentation content when source type is INLINE")
    public void testGetDocumentationContentInline() throws APIManagementException, IOException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        when(apiDAO.getDocumentInfo(DOC_ID)).thenReturn(documentInfo);
        when(apiDAO.getDocumentInlineContent(DOC_ID))
                .thenReturn(SampleTestObjectCreator.createDefaultInlineDocumentationContent());
        apiPublisher.getDocumentationContent(DOC_ID);
        verify(apiDAO, times(1)).getDocumentInlineContent(DOC_ID);
    }

    /**
     * Test cases for exceptions
     */

    @Test(description = "Exception when retrieving an application by uuid",
            expectedExceptions = APIManagementException.class)
    public void testGetApplicationByUuidException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(applicationDAO);
        doThrow(new APIMgtDAOException("Error occurred while retrieving application")).when(applicationDAO)
                .getApplication(UUID);
        apiStore.getApplication(UUID, USER_NAME);
    }

    @Test(description = "Exception when retrieving documentation summary given the id",
            expectedExceptions = APIManagementException.class)
    public void testGetDocumentationSummaryException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiDAO);
        when(apiDAO.getDocumentInfo(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving documents"));
        apiStore.getDocumentationSummary(UUID);
    }

    @Test(description = "Exception when retrieving list of documentations",
            expectedExceptions = APIManagementException.class)
    public void testAllDocumentationException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiDAO);
        when(apiDAO.getDocumentsInfoList(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving documents"));
        apiStore.getAllDocumentation(UUID, 1, 10);
    }

    @Test(description = "Exception when getting API by UUID", expectedExceptions = APIManagementException.class)
    public void testSearchAPIByUUIDException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiDAO);
        when(apiDAO.getAPI(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving API with id " + UUID));
        apiStore.getAPIbyUUID(UUID);
    }

    @Test(description = "Exception when getting last updated time of API",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfAPIException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfAPI(UUID)).thenThrow(
                new APIMgtDAOException("Error occurred while retrieving the last update time of API with id " + UUID));
        apiPublisher.getLastUpdatedTimeOfAPI(UUID);
        verify(apiDAO, times(0)).getLastUpdatedTimeOfAPI(UUID);
    }

    @Test(description = "Exception when getting last updated time of Swagger Definition",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfSwaggerDefinitionException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfSwaggerDefinition(UUID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last update time of the swagger definition of API with id "
                        + UUID));
        apiPublisher.getLastUpdatedTimeOfSwaggerDefinition(UUID);
        verify(apiDAO, times(0)).getLastUpdatedTimeOfSwaggerDefinition(UUID);
    }

    @Test(description = "Exception when checking for context exist", expectedExceptions = APIManagementException.class)
    public void testIsContextExistException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.isAPIContextExists(API_CONTEXT))
                .thenThrow(new APIMgtDAOException("Couldn't check API Context " + API_CONTEXT + " Exists."));
        apiPublisher.isContextExist(API_CONTEXT);
    }

    @Test(description = "Exception when checking for api name exists",
            expectedExceptions = APIManagementException.class)
    public void testIsApiNameExistException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.isAPINameExists(API_NAME, USER_NAME))
                .thenThrow(new APIMgtDAOException("Couldn't check API Name " + API_NAME + " Exists."));
        apiPublisher.isApiNameExist(API_NAME);
    }

    @Test(description = "Exception when getting swagger definition for API",
            expectedExceptions = APIManagementException.class)
    public void testGetSwagger20DefinitionException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getApiSwaggerDefinition(UUID))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve swagger definition for apiId " + UUID));
        apiPublisher.getApiSwaggerDefinition(UUID);
        verify(apiDAO, times(0)).getApiSwaggerDefinition(UUID);
    }

    @Test(description = "Exception when getting subscription by UUID",
            expectedExceptions = APIManagementException.class)
    public void testGetSubscriptionByUUIDException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiSubscriptionDAO);
        when(apiSubscriptionDAO.getAPISubscription(UUID))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve subscription for id " + UUID));
        apiStore.getSubscriptionByUUID(UUID);
        verify(apiSubscriptionDAO, times(0)).getAPISubscription(UUID);
    }

    @Test(description = "Exception when getting last updated time of Document",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfDocumentException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfDocument(DOC_ID)).thenThrow(
                new APIMgtDAOException("Error occurred while retrieving the last updated time of document " + DOC_ID));
        apiPublisher.getLastUpdatedTimeOfDocument(DOC_ID);
        verify(apiDAO, times(0)).getLastUpdatedTimeOfDocument(DOC_ID);
    }

    @Test(description = "Exception when getting last updated time of Document Content",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfDocumentContentException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfDocumentContent(UUID, DOC_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last updated time of the document's content " + DOC_ID));
        apiPublisher.getLastUpdatedTimeOfDocumentContent(UUID, DOC_ID);
        verify(apiDAO, times(0)).getLastUpdatedTimeOfDocumentContent(UUID, DOC_ID);
    }

    @Test(description = "Exception when getting last updated time of API Thumbnail Image",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfAPIThumbnailImageException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getLastUpdatedTimeOfAPIThumbnailImage(UUID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last updated time of the thumbnail image of the API " + UUID));
        apiPublisher.getLastUpdatedTimeOfAPIThumbnailImage(UUID);
        verify(apiDAO, times(0)).getLastUpdatedTimeOfAPIThumbnailImage(UUID);
    }

    @Test(description = "Exception when getting last updated time of Application",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(applicationDAO);
        when(applicationDAO.getLastUpdatedTimeOfApplication(UUID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last updated time of the application " + UUID));
        apiStore.getLastUpdatedTimeOfApplication(UUID);
        verify(applicationDAO, times(0)).getLastUpdatedTimeOfApplication(UUID);
    }

    @Test(description = "Exception when getting last updated time of Subscription",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfSubscriptionException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiSubscriptionDAO);
        when(apiSubscriptionDAO.getLastUpdatedTimeOfSubscription(UUID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last updated time of the subscription " + UUID));
        apiStore.getLastUpdatedTimeOfSubscription(UUID);
        verify(apiSubscriptionDAO, times(0)).getLastUpdatedTimeOfSubscription(UUID);
    }

    @Test(description = "Exception when getting subscriptions by API",
            expectedExceptions = APIManagementException.class)
    public void testGetSubscriptionsByAPIException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        AbstractAPIManager apiStore = getAPIStoreImpl(apiSubscriptionDAO);
        when(apiSubscriptionDAO.getAPISubscriptionsByAPI(UUID))
                .thenThrow(new APIMgtDAOException("Couldn't find subscriptions for apiId " + UUID));
        apiStore.getSubscriptionsByAPI(UUID);
        verify(apiSubscriptionDAO, times(0)).getAPISubscriptionsByAPI(UUID);
    }

    @Test(description = "Getting Documentation content when source type is FILE and the input stream is null",
            expectedExceptions = APIManagementException.class)
    public void testGetDocumentationContentFileWithNullStream() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        DocumentInfo.Builder builder = new DocumentInfo.Builder();
        builder.name("CalculatorDoc");
        builder.sourceType(DocumentInfo.SourceType.FILE);
        DocumentInfo documentInfo = builder.build();
        when(apiDAO.getDocumentInfo(DOC_ID)).thenReturn(documentInfo);
        when(apiDAO.getDocumentFileContent(DOC_ID)).thenReturn(null);
        apiPublisher.getDocumentationContent(DOC_ID);
        verify(apiDAO, times(0)).getDocumentFileContent(DOC_ID);
    }

    @Test(description = "Getting Documentation content when source type is INLINE and inline content is null",
            expectedExceptions = APIManagementException.class)
    public void testGetDocumentationContentInlineWithNullContent() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        when(apiDAO.getDocumentInfo(DOC_ID)).thenReturn(documentInfo);
        when(apiDAO.getDocumentInlineContent(DOC_ID)).thenReturn(null);
        apiPublisher.getDocumentationContent(DOC_ID);
        verify(apiDAO, times(0)).getDocumentInlineContent(DOC_ID);
    }

    @Test(description = "Getting Documentation content when document cannot be found",
            expectedExceptions = APIManagementException.class)
    public void testGetDocumentationContentWhenDocumentNotFound() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getDocumentInfo(DOC_ID)).thenReturn(null);
        apiPublisher.getDocumentationContent(DOC_ID);
        verify(apiDAO, times(0)).getDocumentFileContent(DOC_ID);
        verify(apiDAO, times(0)).getDocumentInlineContent(DOC_ID);
    }

    @Test(description = "Exception when getting Documentation content due to error retrieving document content",
            expectedExceptions = APIManagementException.class)
    public void testGetDocumentationContentErrorOnRetrieval() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiPublisher = getApiPublisherImpl(apiDAO);
        when(apiDAO.getDocumentInfo(DOC_ID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving document content"));
        apiPublisher.getDocumentationContent(DOC_ID);
        verify(apiDAO, times(0)).getDocumentFileContent(DOC_ID);
        verify(apiDAO, times(0)).getDocumentInlineContent(DOC_ID);
    }

    @Test(description = "Get Label by name")
    public void testGetLabelByName() throws APIManagementException {
        LabelDAO labelDAO = mock(LabelDAO.class);
        Label label = SampleTestObjectCreator.createLabel(LABEL_NAME).build();
        AbstractAPIManager apiManager = getApiPublisherImpl(labelDAO);
        when(labelDAO.getLabelByName(LABEL_NAME)).thenReturn(label);
        apiManager.getLabelByName(LABEL_NAME);
        verify(labelDAO, times(1)).getLabelByName(LABEL_NAME);
    }

    private APIStoreImpl getAPIStoreImpl(ApiDAO apiDAO) {
        return new APIStoreImpl(USER_NAME, null, null, apiDAO, null, null, null, null, null, null, null, null);
    }

    private APIStoreImpl getAPIStoreImpl(ApplicationDAO applicationDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, null, null, null, null, null, null, null);
    }

    private APIStoreImpl getAPIStoreImpl(APISubscriptionDAO apiSubscriptionDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, null, apiSubscriptionDAO, null, null, null, null,
                null, null);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APILifecycleManager apiLifecycleManager) {
        return new APIPublisherImpl(USER_NAME, null, null, apiDAO, null, null, null, apiLifecycleManager, null,
                null, null, new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO) {
        return new APIPublisherImpl(USER_NAME, null, null, apiDAO, null, null, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APISubscriptionDAO apiSubscriptionDAO,
                                                 APILifecycleManager apiLifecycleManager) {
        return new APIPublisherImpl(USER_NAME, null, null, apiDAO, null, apiSubscriptionDAO, null, apiLifecycleManager,
                null, null, null, new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APISubscriptionDAO apiSubscriptionDAO) {
        return new APIPublisherImpl(USER_NAME, null, null, apiDAO, null, apiSubscriptionDAO, null, null, null, null,
                null, new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO, APILifecycleManager apiLifecycleManager) {
        return new APIPublisherImpl(USER_NAME, null, null, apiDAO, applicationDAO, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null, null, new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(LabelDAO labelDAO) {
        return new APIPublisherImpl(USER_NAME, null, null, null, null, null, null, null, labelDAO, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(PolicyDAO policyDAO) {
        return new APIPublisherImpl(USER_NAME, null, null, null, null, null, policyDAO, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(APISubscriptionDAO apiSubscriptionDAO) {
        return new APIPublisherImpl(USER_NAME, null, null, null, null, apiSubscriptionDAO, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(IdentityProvider identityProvider) {
        return new APIPublisherImpl(USER_NAME, identityProvider, null, null, null, null, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }
}
