/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.publishers;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.TestUtils;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EntityUtils.class, CarbonUtils.class, ServiceReferenceHolder.class, URL.class, HttpURLConnection.class,
        FileUtils.class, WSO2APIPublisher.class, MultipartEntityBuilder.class})
public class WSO2APIPublisherTestCase {

    private int tenantID = -1234;
    private String username = "admin";
    private String tenantDomain = "carbon.super";
    private String apiIdentifier = "P1_API1_v1.0.0";
    private String storeName = "Sample";
    private String storeUserName = "admin";
    private String storePassword = "admin";
    private String storeEndpoint = "https://localhost:9292/sample";
    private String storeRedirectURL = "http://localhost:9292/redirect";
    private APIStore store;
    private APIIdentifier identifier;
    private API api;
    private WSO2APIPublisher wso2APIPublisher;
    private TenantManager tenantManager;
    private String apiArtifactDir = "/tmp/test";
    private StatusLine statusLine;
    private ImportExportAPI importExportAPI;
    private CloseableHttpClient defaultHttpClient;

    @Before
    public void init() throws Exception {
        store = new APIStore();
        store.setDisplayName(storeName);
        store.setUsername(storeUserName);
        store.setPassword(storePassword);
        store.setEndpoint(storeEndpoint);
        identifier = new APIIdentifier(apiIdentifier);
        api = new API(identifier);
        defaultHttpClient = Mockito.mock(CloseableHttpClient.class);
        wso2APIPublisher = new WSO2APIPublisherWrapper(defaultHttpClient, username, Mockito.mock(APIProvider.class));
        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        statusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        PowerMockito.mockStatic(EntityUtils.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.EXTERNAL_API_STORES + "."
                + APIConstants.EXTERNAL_API_STORES_STORE_URL)).thenReturn(storeRedirectURL);
        HttpGet httpGet = Mockito.mock(HttpGet.class);
        HttpPost httpPost = Mockito.mock(HttpPost.class);
        HttpDelete httpDelete = Mockito.mock(HttpDelete.class);
        PowerMockito.whenNew(HttpGet.class).withAnyArguments().thenReturn(httpGet);
        PowerMockito.whenNew(HttpPost.class).withAnyArguments().thenReturn(httpPost);
        PowerMockito.whenNew(HttpDelete.class).withAnyArguments().thenReturn(httpDelete);
        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(httpPost);
        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(httpGet);
        Mockito.doReturn(httpResponse).when(defaultHttpClient).execute(httpDelete);
        MultipartEntityBuilder multipartEntityBuilder = Mockito.mock(MultipartEntityBuilder.class);
        PowerMockito.mockStatic(MultipartEntityBuilder.class);
        Mockito.when(MultipartEntityBuilder.create()).thenReturn(multipartEntityBuilder);
        Mockito.when(multipartEntityBuilder.build()).thenReturn(Mockito.mock(HttpEntity.class));
        Mockito.doNothing().when(httpPost).setEntity(Matchers.any());
        importExportAPI = Mockito.mock(ImportExportAPI.class);
    }

    @Test
    public void testPublishToStoreWithNullStoreArguments() {

        //Error path - When username or password or endpoint is not defined
        APIStore nullStore = new APIStore();
        nullStore.setDisplayName(storeName);
        try {
            wso2APIPublisher.publishToStore(api, nullStore);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String msg = "External APIStore endpoint URL or credentials are not defined. " +
                    "Cannot proceed with publishing API to the APIStore - " + nullStore.getDisplayName();
            Assert.assertEquals(msg, e.getMessage());
        }
    }

    @Test
    public void testPublishAndUpdateToStore() throws Exception {

        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        Mockito.when(APIImportExportUtil.getImportExportAPI()).thenReturn(importExportAPI);
        Mockito.doReturn(new File(apiArtifactDir)).when(importExportAPI)
                .exportAPI(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
                        Matchers.anyString(), Matchers.anyBoolean(), Matchers.any(ExportFormat.class),
                        Matchers.anyBoolean(), Matchers.anyBoolean(), Matchers.anyBoolean(), Matchers.anyString());
        //Test Unauthenticated scenario for publishing API
        Mockito.doReturn(HttpStatus.SC_UNAUTHORIZED).when(statusLine).getStatusCode();
        String unauthenticatedResponse = "{\"code\":401,\"message\":\"\",\"description\":\"Unauthenticated request\"," +
                "\"moreInfo\":\"\",\"error\":[]}";
        PowerMockito.when(EntityUtils.toString(Matchers.any())).thenReturn(unauthenticatedResponse);
        String errorMsg = "Import API service call received unsuccessful response: " + unauthenticatedResponse
                + " status: " + HttpStatus.SC_UNAUTHORIZED;
        try {
            wso2APIPublisher.publishToStore(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertEquals(errorMsg, e.getMessage());
        }
        //Test Unauthenticated scenario for updating API
        try {
            wso2APIPublisher.updateToStore(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertEquals(errorMsg, e.getMessage());
        }
        //Test Successful scenario for publishing and updating API
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLine).getStatusCode();
        String successResponse = "API imported successfully.";
        PowerMockito.when(EntityUtils.toString(Matchers.any())).thenReturn(successResponse);
        Assert.assertTrue("API Publish is unsuccessful", wso2APIPublisher.publishToStore(api, store));
        Assert.assertTrue("API Update is unsuccessful", wso2APIPublisher.updateToStore(api, store));
    }

    @Test
    public void testFailureWhileExportingAPI() throws Exception {

        //Error path - When exporting API failed
        Mockito.when(APIImportExportUtil.getImportExportAPI()).thenReturn(importExportAPI);
        PowerMockito.doThrow(new APIImportExportException("Error while exporting API")).when(importExportAPI)
                .exportAPI(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
                        Matchers.anyString(), Matchers.anyBoolean(), Matchers.any(ExportFormat.class),
                        Matchers.anyBoolean(), Matchers.anyBoolean(), Matchers.anyBoolean(), Matchers.anyString());
        try {
            wso2APIPublisher.publishToStore(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String errorMsg = "Error while exporting API: " + api.getId().getApiName() + " version: "
                    + api.getId().getVersion();
            Assert.assertEquals(errorMsg, e.getMessage());
        }

        PowerMockito.doThrow(
                new UserStoreException("Error in getting the tenant id with tenant domain: " + tenantDomain + "."))
                .when(tenantManager).getTenantId(tenantDomain);
        try {
            wso2APIPublisher.publishToStore(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String errorMsg = "Error while getting tenantId for tenant domain: " + tenantDomain
                    + " when exporting API:" + api.getId().getApiName() + " version: " + api.getId().getVersion();
            Assert.assertEquals(errorMsg, e.getMessage());
        }
    }

    @Test
    public void testCheckingAPIExists() throws Exception {

        //Test error path when multiple APIs received for search request
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLine).getStatusCode();
        String apiGetResponse = "{\n" +
                "    \"count\": 2,\n" +
                "    \"list\": [\n" +
                "        {\n" +
                "            \"id\": \"735ad20d-f382-4ab3-8000-97fce885c853\",\n" +
                "            \"name\": \"API1\",\n" +
                "            \"version\": \"v1.0.0\",\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"2346e0cc-926c-4b1d-8624-9d08371494c6\",\n" +
                "            \"name\": \"TestAPI1\",\n" +
                "            \"version\": \"v1.0.0\",\n" +
                "        }\n" +
                "    ],\n" +
                "    \"pagination\": {\n" +
                "        \"offset\": 0,\n" +
                "        \"limit\": 25,\n" +
                "        \"total\": 2,\n" +
                "        \"next\": \"\",\n" +
                "        \"previous\": \"\"\n" +
                "    }\n" +
                "}";
        PowerMockito.when(EntityUtils.toString(Matchers.any())).thenReturn(apiGetResponse);
        String errorMessage = "Duplicate APIs exists in external store for API name:"
                + identifier.getApiName() + " version: " + identifier.getVersion();
        try {
            wso2APIPublisher.isAPIAvailable(api, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertEquals(errorMessage, e.getMessage());
        }

        //Test successful API non existence response
        apiGetResponse = "{\n" +
                "    \"count\": 0,\n" +
                "    \"list\": [],\n" +
                "    \"pagination\": {\n" +
                "        \"offset\": 0,\n" +
                "        \"limit\": 25,\n" +
                "        \"total\": 0,\n" +
                "        \"next\": \"\",\n" +
                "        \"previous\": \"\"\n" +
                "    }\n" +
                "}";
        PowerMockito.when(EntityUtils.toString(Matchers.any())).thenReturn(apiGetResponse);
        Assert.assertFalse("API Exists response received", wso2APIPublisher.isAPIAvailable(api, store));

        //Test successful API existence response
        apiGetResponse = "{\n" +
                "    \"count\": 1,\n" +
                "    \"list\": [\n" +
                "        {\n" +
                "            \"id\": \"735ad20d-f382-4ab3-8000-97fce885c853\",\n" +
                "            \"name\": \"API1\",\n" +
                "            \"version\": \"1.0.0\",\n" +
                "        }\n" +
                "    ],\n" +
                "    \"pagination\": {\n" +
                "        \"offset\": 0,\n" +
                "        \"limit\": 25,\n" +
                "        \"total\": 1,\n" +
                "        \"next\": \"\",\n" +
                "        \"previous\": \"\"\n" +
                "    }\n" +
                "}";
        PowerMockito.when(EntityUtils.toString(Matchers.any())).thenReturn(apiGetResponse);
        Assert.assertTrue("API non exists response received", wso2APIPublisher.isAPIAvailable(api, store));
    }

    @Test
    public void testDeletingAPI() throws Exception {

        //Test error path when deleting non existing API
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLine).getStatusCode();
        String apiGetResponse = "{\n" +
                "    \"count\": 0,\n" +
                "    \"list\": [],\n" +
                "    \"pagination\": {\n" +
                "        \"offset\": 0,\n" +
                "        \"limit\": 25,\n" +
                "        \"total\": 0,\n" +
                "        \"next\": \"\",\n" +
                "        \"previous\": \"\"\n" +
                "    }\n" +
                "}";
        PowerMockito.when(EntityUtils.toString(Matchers.any())).thenReturn(apiGetResponse);
        try {
            wso2APIPublisher.deleteFromStore(identifier, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String errorMessage = "API: " + identifier.getApiName() + " version: " + identifier.getVersion()
                    + " does not exist in external store: " + store.getName();
            Assert.assertEquals(errorMessage, e.getMessage());
        }

        //Test successful API deletion
        apiGetResponse = "{\n" +
                "    \"count\": 1,\n" +
                "    \"list\": [\n" +
                "        {\n" +
                "            \"id\": \"735ad20d-f382-4ab3-8000-97fce885c853\",\n" +
                "            \"name\": \"API1\",\n" +
                "            \"version\": \"1.0.0\",\n" +
                "        }\n" +
                "    ],\n" +
                "    \"pagination\": {\n" +
                "        \"offset\": 0,\n" +
                "        \"limit\": 25,\n" +
                "        \"total\": 1,\n" +
                "        \"next\": \"\",\n" +
                "        \"previous\": \"\"\n" +
                "    }\n" +
                "}";
        PowerMockito.when(EntityUtils.toString(Matchers.any())).thenReturn(apiGetResponse);
        Assert.assertTrue("API deletion failed", wso2APIPublisher.deleteFromStore(identifier, store));

        //Test error path API deletion failed due to server error
        String apiDeleteResponse = "{\"code\":500,\"message\":\"\",\"description\":\"Internal Server Error\"," +
                "\"moreInfo\":\"\",\"error\":[]}";
        HttpDelete httpDeleteFail = Mockito.mock(HttpDelete.class);
        PowerMockito.whenNew(HttpDelete.class).withAnyArguments().thenReturn(httpDeleteFail);
        CloseableHttpResponse deletionFailedResponse = Mockito.mock(CloseableHttpResponse.class);
        StatusLine deletionFailedStatusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(deletionFailedStatusLine).when(deletionFailedResponse).getStatusLine();
        HttpEntity deletionFailedEntity = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(deletionFailedEntity).when(deletionFailedResponse).getEntity();
        PowerMockito.when(EntityUtils.toString(deletionFailedEntity)).thenReturn(apiDeleteResponse);
        Mockito.doReturn(deletionFailedResponse).when(defaultHttpClient).execute(httpDeleteFail);

        //Error path when deleting API
        Mockito.doReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR).when(deletionFailedStatusLine).getStatusCode();
        try {
            wso2APIPublisher.deleteFromStore(identifier, store);
            Assert.fail("APIManagement exception not thrown for error scenario");
        } catch (APIManagementException e) {
            String errorMessage = "API Delete service call received unsuccessful response status: "
                    + HttpStatus.SC_INTERNAL_SERVER_ERROR + " response: " + apiDeleteResponse;
            Assert.assertEquals(errorMessage, e.getMessage());
        }
    }
}
