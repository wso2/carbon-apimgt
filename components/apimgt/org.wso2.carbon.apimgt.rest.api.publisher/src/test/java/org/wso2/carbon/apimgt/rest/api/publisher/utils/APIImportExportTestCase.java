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

package org.wso2.carbon.apimgt.rest.api.publisher.utils;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIDetails;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class APIImportExportTestCase {

    private static final Logger log = LoggerFactory.getLogger(APIImportExportTestCase.class);

    private static String api1Definition;
    private static String api1GatewayConfig;
    private static String api2Definition;
    private static String api2GatewayConfig;
    private static InputStream api1Doc2Stream;
    private static String importExportRootDirectory = System.getProperty("java.io.tmpdir") + File.separator +
            "import-export-test";
    private APIPublisher apiPublisher;

    @BeforeClass(description = "Initialize")
    protected void setUp () throws Exception  {
        api1Definition = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("api1_swagger_definition.json"));
        api1GatewayConfig = "api 1 dummy gateway config";
        api2Definition = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("api2_swagger_definition.json"));
        api2GatewayConfig = "api 2 dummy gateway config";
        api1Doc2Stream = getClass().getClassLoader().getResourceAsStream("api1_doc2.pdf");
        log.info("Test directory: " + importExportRootDirectory);
    }

    @Test(description = "Test getAPIDetails - single API")
    void testGetApiDetails () throws APIManagementException {
        printTestMethodName();
        apiPublisher = Mockito.mock(APIPublisher.class);

        String api1Id = UUID.randomUUID().toString();
        Endpoint api1SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api1ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api1 = createApi("provider1", api1Id, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                createEndpointTypeToIdMap(api1SandBoxEndpointId, api1ProdEndpointId)).build();

        String api1Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc1Info = createAPIDoc(api1Doc1Id, "api1doc1", "", "API 1 DOC 1", DocumentInfo.DocType.HOWTO,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);
        String api1Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc2Info = createAPIDoc(api1Doc2Id, "api1doc2.pdf", "api1doc2.pdf", "API 1 DOC 2",
                DocumentInfo.DocType.PUBLIC_FORUM, "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.API_LEVEL);
        String api1Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc3Info = createAPIDoc(api1Doc3Id, "api1doc3", "", "API 1 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        List<DocumentInfo> api1DocumentInfo = new ArrayList<>();
        api1DocumentInfo.add(api1Doc1Info);
        api1DocumentInfo.add(api1Doc2Info);
        api1DocumentInfo.add(api1Doc3Info);

        // contents for documents
        DocumentContent api1Doc1Content = createDocContent(api1Doc1Info, "Sample inline content for API1 DOC 1", null);
        DocumentContent api1Doc2Content = createDocContent(api1Doc2Info, "", api1Doc2Stream);
        DocumentContent api1Doc3Content = createDocContent(api1Doc3Info, "", null);

        Mockito.when(apiPublisher.getAPIbyUUID(api1Id)).thenReturn(api1);
        Mockito.when(apiPublisher.getApiSwaggerDefinition(api1Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getApiGatewayConfig(api1Id)).thenReturn(api1GatewayConfig);
        Mockito.when(apiPublisher.getAllDocumentation(api1Id, 0, Integer.MAX_VALUE)).thenReturn(api1DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc1Id)).thenReturn(api1Doc1Content);
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc2Id)).thenReturn(api1Doc2Content);
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc3Id)).thenReturn(api1Doc3Content);
        Mockito.when(apiPublisher.getThumbnailImage(api1Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api1_thumbnail.png"));
        List<API> apis = new ArrayList<>();
        apis.add(api1);
        Mockito.when(apiPublisher.searchAPIs(Integer.MAX_VALUE, 0, "*")).thenReturn(apis);

        ApiImportExportManager importExportManager = new ApiImportExportManager(apiPublisher);
        Set<APIDetails> apiDetailsSet = importExportManager.getAPIDetails(Integer.MAX_VALUE, 0, "*");
        Assert.assertEquals(new ArrayList<>(apiDetailsSet).get(0).getApi().getId().equals(api1Id), true,
                "APIDetails not retrieved correctly for API: " + api1.getName() + ", version: " + api1.getVersion());
    }

    @Test(description = "Test getAPIDetails - multiple APIs with non-critical error in retrieving information of one API")
    void testGetMultipleApiDetailsWithNonFatalErrors () throws APIManagementException {
        printTestMethodName();
        apiPublisher = Mockito.mock(APIPublisher.class);

        String api4Id = UUID.randomUUID().toString();
        Endpoint api4SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api4ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api4 = createApi("provider4", api4Id, "testapi4", "1.0.0", "Test API 4 - version 1.0.0",
                createEndpointTypeToIdMap(api4SandBoxEndpointId, api4ProdEndpointId)).build();

        String api4Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api4Doc1Info = createAPIDoc(api4Doc1Id, "api1doc1", "", "API 4 DOC 1", DocumentInfo.DocType.HOWTO,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);
        String api4Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api4Doc2Info = createAPIDoc(api4Doc2Id, "api1doc2.pdf", "api1doc2.pdf", "API 4 DOC 2",
                DocumentInfo.DocType.PUBLIC_FORUM, "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.API_LEVEL);
        String api4Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api4Doc3Info = createAPIDoc(api4Doc3Id, "api1doc3", "", "API 4 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        List<DocumentInfo> api1DocumentInfo = new ArrayList<>();
        api1DocumentInfo.add(api4Doc1Info);
        api1DocumentInfo.add(api4Doc2Info);
        api1DocumentInfo.add(api4Doc3Info);

        // contents for documents
        DocumentContent api4Doc1Content = createDocContent(api4Doc1Info, "Sample inline content for API1 DOC 1", null);
        DocumentContent api4Doc2Content = createDocContent(api4Doc2Info, "", api1Doc2Stream);
        DocumentContent api4Doc3Content = createDocContent(api4Doc3Info, "", null);

        Mockito.when(apiPublisher.getAPIbyUUID(api4Id)).thenReturn(api4);
        Mockito.when(apiPublisher.getApiSwaggerDefinition(api4Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getApiGatewayConfig(api4Id)).thenReturn(api1GatewayConfig);
        Mockito.when(apiPublisher.getAllDocumentation(api4Id, 0, Integer.MAX_VALUE)).thenReturn(api1DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api4Doc1Id)).thenReturn(api4Doc1Content);
        Mockito.when(apiPublisher.getDocumentationContent(api4Doc2Id)).thenReturn(api4Doc2Content);
        Mockito.when(apiPublisher.getDocumentationContent(api4Doc3Id)).thenReturn(api4Doc3Content);
        Mockito.when(apiPublisher.getThumbnailImage(api4Id)).thenReturn(null);

        String api5Id = UUID.randomUUID().toString();
        Endpoint api5SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api5ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api5 = createApi("provider5", api5Id, "testapi4", "1.0.0", "Test API 5 - version 1.0.0",
                createEndpointTypeToIdMap(api5SandBoxEndpointId, api5ProdEndpointId)).build();

        String api5Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api5Doc1Info = createAPIDoc(api5Doc1Id, "api1doc1", "", "API 5 DOC 1", DocumentInfo.DocType.HOWTO,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);
        String api5Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api5Doc3Info = createAPIDoc(api5Doc3Id, "api1doc3", "", "API 5 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        List<DocumentInfo> api5DocumentInfo = new ArrayList<>();
        api5DocumentInfo.add(api5Doc1Info);
        api5DocumentInfo.add(api5Doc3Info);

        // contents for documents
        DocumentContent api5Doc1Content = createDocContent(api5Doc1Info, "Sample inline content for API1 DOC 1", null);
        DocumentContent api5Doc3Content = createDocContent(api5Doc3Info, "", null);

        Mockito.when(apiPublisher.getAPIbyUUID(api5Id)).thenReturn(api5);
        Mockito.when(apiPublisher.getApiSwaggerDefinition(api5Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getApiGatewayConfig(api5Id)).thenReturn(api1GatewayConfig);
        Mockito.when(apiPublisher.getAllDocumentation(api5Id, 0, Integer.MAX_VALUE)).thenReturn(api5DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api5Doc1Id)).thenReturn(api5Doc1Content);
        Mockito.when(apiPublisher.getDocumentationContent(api5Doc3Id)).thenReturn(api5Doc3Content);
        Mockito.when(apiPublisher.getThumbnailImage(api5Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api1_thumbnail.png"));

        Mockito.when(apiPublisher.getAllDocumentation(api4Id, 0, Integer.MAX_VALUE)).thenThrow(APIManagementException.class);

        List<API> apis = new ArrayList<>();
        apis.add(api4);
        apis.add(api5);
        Mockito.when(apiPublisher.searchAPIs(Integer.MAX_VALUE, 0, "*")).thenReturn(apis);

        ApiImportExportManager importExportManager = new ApiImportExportManager(apiPublisher);
        Set<APIDetails> apiDetailsSet = importExportManager.getAPIDetails(Integer.MAX_VALUE, 0, "*");
        Assert.assertEquals(apiDetailsSet.size() == 2, true, "Error getting API details");
    }

    @Test(description = "Test getAPIDetails - multiple APIs with critical error in retrieving information of one API")
    void testGetMultipleApiDetailsWithFatalErrors () throws APIManagementException {
        printTestMethodName();
        apiPublisher = Mockito.mock(APIPublisher.class);

        String api6Id = UUID.randomUUID().toString();
        Endpoint api6SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api6ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api6 = createApi("provider4", api6Id, "testapi6", "1.0.0", "Test API 6 - version 1.0.0",
                createEndpointTypeToIdMap(api6SandBoxEndpointId, api6ProdEndpointId)).build();

        String api6Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api6Doc1Info = createAPIDoc(api6Doc1Id, "api1doc1", "", "API 6 DOC 1", DocumentInfo.DocType.HOWTO,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);
        String api6Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api6Doc2Info = createAPIDoc(api6Doc2Id, "api1doc2.pdf", "api1doc2.pdf", "API 4 DOC 2",
                DocumentInfo.DocType.PUBLIC_FORUM, "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.API_LEVEL);
        String api6Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api6Doc3Info = createAPIDoc(api6Doc3Id, "api1doc3", "", "API 6 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        List<DocumentInfo> api1DocumentInfo = new ArrayList<>();
        api1DocumentInfo.add(api6Doc1Info);
        api1DocumentInfo.add(api6Doc2Info);
        api1DocumentInfo.add(api6Doc3Info);

        // contents for documents
        DocumentContent api6Doc1Content = createDocContent(api6Doc1Info, "Sample inline content for API1 DOC 1", null);
        DocumentContent api6Doc2Content = createDocContent(api6Doc2Info, "", api1Doc2Stream);
        DocumentContent api6Doc3Content = createDocContent(api6Doc3Info, "", null);

        Mockito.when(apiPublisher.getAPIbyUUID(api6Id)).thenReturn(api6);
        Mockito.when(apiPublisher.getApiSwaggerDefinition(api6Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getApiGatewayConfig(api6Id)).thenReturn(api1GatewayConfig);
        Mockito.when(apiPublisher.getAllDocumentation(api6Id, 0, Integer.MAX_VALUE)).thenReturn(api1DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api6Doc1Id)).thenReturn(api6Doc1Content);
        Mockito.when(apiPublisher.getDocumentationContent(api6Doc2Id)).thenReturn(api6Doc2Content);
        Mockito.when(apiPublisher.getDocumentationContent(api6Doc3Id)).thenReturn(api6Doc3Content);
        Mockito.when(apiPublisher.getThumbnailImage(api6Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api1_thumbnail.png"));

        String api7Id = UUID.randomUUID().toString();
        Endpoint api7SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api7ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();     API api7 = createApi("provider5", api7Id, "testapi4", "1.0.0", "Test API 7 - version 1.0.0",
                createEndpointTypeToIdMap(api7SandBoxEndpointId, api7ProdEndpointId)).build();

        String api7Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api7Doc1Info = createAPIDoc(api7Doc1Id, "api1doc1", "", "API 7 DOC 1", DocumentInfo.DocType.HOWTO,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);
        String api7Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api7Doc3Info = createAPIDoc(api7Doc3Id, "api1doc3", "", "API 7 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        List<DocumentInfo> api7DocumentInfo = new ArrayList<>();
        api7DocumentInfo.add(api7Doc1Info);
        api7DocumentInfo.add(api7Doc3Info);

        // contents for documents
        DocumentContent api7Doc1Content = createDocContent(api7Doc1Info, "Sample inline content for API1 DOC 1", null);
        DocumentContent api7Doc3Content = createDocContent(api7Doc3Info, "", null);

        Mockito.when(apiPublisher.getAPIbyUUID(api7Id)).thenReturn(api7);
        Mockito.when(apiPublisher.getApiSwaggerDefinition(api7Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getApiGatewayConfig(api7Id)).thenReturn(api1GatewayConfig);
        Mockito.when(apiPublisher.getAllDocumentation(api7Id, 0, Integer.MAX_VALUE)).thenReturn(api7DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api7Doc1Id)).thenReturn(api7Doc1Content);
        Mockito.when(apiPublisher.getDocumentationContent(api7Doc3Id)).thenReturn(api7Doc3Content);
        Mockito.when(apiPublisher.getThumbnailImage(api7Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api1_thumbnail.png"));

        Mockito.when(apiPublisher.getApiSwaggerDefinition(api7Id)).thenThrow(APIManagementException.class);

        List<API> apis = new ArrayList<>();
        apis.add(api6);
        apis.add(api7);
        Mockito.when(apiPublisher.searchAPIs(Integer.MAX_VALUE, 0, "*")).thenReturn(apis);

        ApiImportExportManager importExportManager = new ApiImportExportManager(apiPublisher);
        Set<APIDetails> apiDetailsSet = importExportManager.getAPIDetails(Integer.MAX_VALUE, 0, "*");
        Assert.assertEquals(apiDetailsSet.size() == 1, true, "Error getting API details");
    }

    @Test(description = "Test updateAPIDetails")
    void testUpdateApiDetails () throws APIManagementException {
        printTestMethodName();
        apiPublisher = Mockito.mock(APIPublisher.class);

        String api2Id = UUID.randomUUID().toString();
        Endpoint api2SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api2ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();   API api2 = createApi("provider1", api2Id, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                createEndpointTypeToIdMap(api2SandBoxEndpointId, api2ProdEndpointId)).build();

        String api2Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api2Doc1Info = createAPIDoc(api2Doc1Id, "api1doc1", "", "API 2 DOC 1", DocumentInfo.DocType.HOWTO,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);
        String api2Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api2Doc2Info = createAPIDoc(api2Doc2Id, "api1doc2.pdf", "api1doc2.pdf", "API 2 DOC 2",
                DocumentInfo.DocType.PUBLIC_FORUM, "other type", DocumentInfo.SourceType.FILE, "",
                DocumentInfo.Visibility.API_LEVEL);
        String api2Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api2Doc3Info = createAPIDoc(api2Doc3Id, "api1doc3", "", "API 2 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        Set<DocumentInfo> api2DocumentInfo = new HashSet<>();
        api2DocumentInfo.add(api2Doc1Info);
        api2DocumentInfo.add(api2Doc2Info);
        api2DocumentInfo.add(api2Doc3Info);

        // contents for documents
        DocumentContent api2Doc1Content = createDocContent(api2Doc1Info, "Sample inline content for API1 DOC 1", null);
        DocumentContent api2Doc2Content = createDocContent(api2Doc2Info, "", api1Doc2Stream);
        DocumentContent api2Doc3Content = createDocContent(api2Doc3Info, "", null);

        Set<DocumentContent> api2DocContents = new HashSet<>();
        api2DocContents.add(api2Doc1Content);
        api2DocContents.add(api2Doc2Content);
        api2DocContents.add(api2Doc3Content);

        APIDetails api2Details = new APIDetails(api2, api2Definition);
        api2Details.setGatewayConfiguration(api2GatewayConfig);
        api2Details.addDocumentInformation(api2DocumentInfo);
        api2Details.addDocumentContents(api2DocContents);
        api2Details.setThumbnailStream(getClass().getClassLoader().getResourceAsStream("api2_thumbnail.jpg"));

        ApiImportExportManager importExportManager = new ApiImportExportManager(apiPublisher);
        importExportManager.addAPIDetails(api2Details);
    }

    @Test(description = "Test API export and import")
    public void testApiExportAndImport () throws Exception {
        printTestMethodName();
        apiPublisher = Mockito.mock(APIPublisher.class);
        testApiExport(importExportRootDirectory);
        testApiImport(importExportRootDirectory);
    }

    private void testApiExport (String exportDir) throws Exception {

        String api1Id = UUID.randomUUID().toString();
        Endpoint api1SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api1ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api1 = createApi("provider1", api1Id, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                createEndpointTypeToIdMap(api1SandBoxEndpointId, api1ProdEndpointId)).build();

        String api1Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc1Info = createAPIDoc(api1Doc1Id, "api1doc1", "", "API 1 DOC 1", DocumentInfo.DocType.HOWTO,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);
        String api1Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc2Info = createAPIDoc(api1Doc2Id, "api1doc2.pdf", "api1doc2.pdf", "API 1 DOC 2",
                DocumentInfo.DocType.PUBLIC_FORUM, "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.API_LEVEL);
        String api1Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc3Info = createAPIDoc(api1Doc3Id, "api1doc3", "", "API 1 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        Set<DocumentInfo> api1DocumentInfo = new HashSet<>();
        api1DocumentInfo.add(api1Doc1Info);
        api1DocumentInfo.add(api1Doc2Info);
        api1DocumentInfo.add(api1Doc3Info);

        // contents for documents
        DocumentContent api1Doc1Content = createDocContent(api1Doc1Info, "Sample inline content for API1 DOC 1", null);
        DocumentContent api1Doc2Content = createDocContent(api1Doc2Info, "", api1Doc2Stream);
        DocumentContent api1Doc3Content = createDocContent(api1Doc3Info, "", null);

        Set<DocumentContent> api1DocContent = new HashSet<>();
        api1DocContent.add(api1Doc1Content);
        api1DocContent.add(api1Doc2Content);
        api1DocContent.add(api1Doc3Content);

        APIDetails api1Details = new APIDetails(api1, api1Definition);
        api1Details.setGatewayConfiguration(api1GatewayConfig);
        api1Details.addDocumentInformation(api1DocumentInfo);
        api1Details.addDocumentContents(api1DocContent);
        api1Details.setThumbnailStream(getClass().getClassLoader().getResourceAsStream("api1_thumbnail.png"));

        Endpoint api1SandboxEndpoint = createEndpoint(api1SandBoxEndpointId.getId(), "api1SandBoxEndpoint", "SANDBOX",
                "{'type':'http','url':'http://localhost:8280'}",
                "{'enabled':'true','type':'basic','properties':{'username':'admin','password':'admin'}}", 10l);
        Endpoint api1ProdEndpoint = createEndpoint(api1ProdEndpointId.getId(), "api1ProdEndpoint", "PRODUCTION",
                "{'type':'http','url':'http://localhost:8280'}",
                "{'enabled':'true','type':'basic','properties':{'username':'admin','password':'admin'}}", 10l);
        api1Details.addEndpoint(api1SandboxEndpoint);
        api1Details.addEndpoint(api1ProdEndpoint);

        String api2Id = UUID.randomUUID().toString();
        Endpoint api2SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api2ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api2 = createApi("provider2", api2Id, "testapi2", "3.0.0", "Test API 2 - version 3.0.0",
                createEndpointTypeToIdMap(api2SandBoxEndpointId, api2ProdEndpointId)).build();
        List<API> apis = new ArrayList<>();
        apis.add(api1);
        apis.add(api2);

        String api2Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api2Doc1Info = createAPIDoc(api2Doc1Id, "api2doc1", "", "API 2 DOC 1", DocumentInfo.DocType.API_MESSAGE_FORMAT,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.API_LEVEL);
        String api2Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api2Doc2Info = createAPIDoc(api2Doc2Id, "api2doc2", "", "API 2 DOC 2", DocumentInfo.DocType.PUBLIC_FORUM,
                "other type", DocumentInfo.SourceType.URL, "http://api2.org/documentation/1", DocumentInfo.Visibility.PRIVATE);

        Set<DocumentInfo> api2DocumentInfo = new HashSet<>();
        api2DocumentInfo.add(api2Doc1Info);
        api2DocumentInfo.add(api2Doc2Info);

        DocumentContent api2Doc1Content = createDocContent(api2Doc1Info, "Sample inline content for API2 DOC 1", null);
        DocumentContent api2Doc2Content = createDocContent(api2Doc2Info, "", null);

        Set<DocumentContent> api2DocContent = new HashSet<>();
        api2DocContent.add(api2Doc1Content);
        api2DocContent.add(api2Doc2Content);

        APIDetails api2Details = new APIDetails(api2, api2Definition);
        api2Details.setGatewayConfiguration(api2GatewayConfig);
        api2Details.addDocumentInformation(api2DocumentInfo);
        api2Details.addDocumentContents(api2DocContent);
        api2Details.setThumbnailStream(getClass().getClassLoader().getResourceAsStream("api2_thumbnail.jpg"));

        Endpoint api2SandboxEndpoint = createEndpoint(api2SandBoxEndpointId.getId(), "api2SandBoxEndpoint", "SANDBOX",
                "{'type':'http','url':'http://localhost:8280'}",
                "{'enabled':'true','type':'basic','properties':{'username':'admin','password':'admin'}}", 20l);
        Endpoint api2ProdEndpoint = createEndpoint(api2ProdEndpointId.getId(), "api2ProdEndpoint", "PRODUCTION",
                "{'type':'http','url':'http://localhost:8280'}",
                "{'enabled':'true','type':'basic','properties':{'username':'admin','password':'admin'}}", 20l);
        api2Details.addEndpoint(api2SandboxEndpoint);
        api2Details.addEndpoint(api2ProdEndpoint);

        Set<APIDetails> apiDetailsSet = new HashSet<>();
        apiDetailsSet.add(api1Details);
        apiDetailsSet.add(api2Details);

        // mock the method calls for retrieving APIs
        Mockito.when(apiPublisher.getAPIbyUUID(api1Id)).thenReturn(api1);
        Mockito.when(apiPublisher.getAPIbyUUID(api2Id)).thenReturn(api2);

        // export
        FileBasedApiImportExportManager importExportManager = new FileBasedApiImportExportManager(apiPublisher, exportDir);

        String exportedApiDirName = "exported-apis";
        String exportedApiDirPath = importExportManager.exportAPIs(apiDetailsSet, exportedApiDirName);
        String exportedApiArchiveFilePath = importExportManager.createArchiveFromExportedApiArtifacts(
                exportedApiDirPath, exportDir, exportedApiDirName);

        // check if two APIs are written to the file system
        String unzipPath = importExportRootDirectory + File.separator + "unzipped-export-archive";
        APIFileUtils.extractArchive(exportedApiArchiveFilePath, unzipPath);
        Assert.assertEquals(APIFileUtils.getDirectoryList(unzipPath).size() == 2, true,
                "Exported API count is not equal to 2");

        Mockito.when(apiPublisher.checkIfAPIExists(api2Id)).thenReturn(true);
    }

    private APIListDTO testApiImport(String importDir) throws APIManagementException, IOException {

        // Read the export archive
        FileInputStream exportedApiArchiveStream = null;
        try {
            exportedApiArchiveStream = new FileInputStream(new File(importDir + File.separator + "exported-apis.zip"));
            FileBasedApiImportExportManager importExportManager = new FileBasedApiImportExportManager(apiPublisher, importDir);
            return importExportManager.importAPIs(exportedApiArchiveStream, null);

        } finally {
            if (exportedApiArchiveStream != null) {
                exportedApiArchiveStream.close();
            }
        }
    }

    private static API.APIBuilder createApi(String provider, String apiId, String name, String version, String
            description, Map<String, Endpoint> endpointTypeToIdMap)
            throws APIManagementException {
        Set<String> transport = new HashSet<>();
        transport.add("http");


        Set<Policy> policies = new HashSet<>();
        policies.add(new SubscriptionPolicy("Silver"));
        policies.add(new SubscriptionPolicy("Bronze"));

        Set<String> tags = new HashSet<>();
        tags.add("food");
        tags.add("beverage");

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner("John Doe");
        businessInformation.setBusinessOwnerEmail("john.doe@annonymous.com");
        businessInformation.setTechnicalOwner("Jane Doe");
        businessInformation.setBusinessOwnerEmail("jane.doe@annonymous.com");

        CorsConfiguration corsConfiguration =  new CorsConfiguration();
        corsConfiguration.setEnabled(true);
        corsConfiguration.setAllowMethods(Arrays.asList("GET", "POST", "DELETE"));
        corsConfiguration.setAllowHeaders(Arrays.asList("Authorization", "X-Custom"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowOrigins(Collections.singletonList("*"));

        API.APIBuilder apiBuilder = new API.APIBuilder(provider, name, version).
                id(apiId).
                context(UUID.randomUUID().toString()).
                description(description).
                lifeCycleStatus("CREATED").
                apiDefinition(api1Definition).
                wsdlUri("http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl").
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy(new APIPolicy("Gold")).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(new HashSet<>(Arrays.asList("customer", "manager", "employee"))).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("Adam Doe").
                lastUpdatedTime(LocalDateTime.now()).
                endpoint(endpointTypeToIdMap);


        apiBuilder.uriTemplates(Collections.emptyMap());

        return apiBuilder;
    }

    private static Endpoint createEndpoint (String id, String name, String type, String endpointConfig, String
            endpointSecurity, long maxTps) {
        return new Endpoint.Builder().id(id).name(name).type(type).endpointConfig(endpointConfig).
                security(endpointSecurity).maxTps(maxTps).build();
    }

    private static Map<String, Endpoint> createEndpointTypeToIdMap (Endpoint sandboxEndpointId, Endpoint
            productionEndpointId) {

        Map<String, Endpoint> endpointTypeToIdMap = new HashedMap();
        endpointTypeToIdMap.put("PRODUCTION", productionEndpointId);
        endpointTypeToIdMap.put("SANDBOX", sandboxEndpointId);
        return endpointTypeToIdMap;
    }

    private static DocumentInfo createAPIDoc (String docId, String name, String fileName, String summary,
                                              DocumentInfo.DocType docType, String otherType, DocumentInfo.SourceType
                                                      sourceType, String sourceUrl, DocumentInfo.Visibility visibility) {

        return new DocumentInfo.Builder().
                id(docId).
                name(name).
                fileName(fileName).
                summary(summary).
                type(docType).
                otherType(otherType).
                sourceType(sourceType).
                sourceURL(sourceUrl).
                visibility(visibility).build();
    }

    private static DocumentContent createDocContent (DocumentInfo documentInfo, String
            inlineContent, InputStream fileContent) {

        return new DocumentContent.Builder().documentInfo(documentInfo).inlineContent(inlineContent).
                fileContent(fileContent).build();

    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }

    @AfterClass
    protected void tearDown () {
        try {
            APIFileUtils.deleteDirectory(importExportRootDirectory);
        } catch (APIMgtDAOException e) {
            log.warn("Unable to delete directory "+importExportRootDirectory);
        }
    }
}
