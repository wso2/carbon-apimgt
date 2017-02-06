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

package org.wso2.carbon.apimgt.rest.api.publisher.importexport.api;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtEntityImportExportException;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.models.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.ImportExportManager;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.ImportExportUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

public class APIImportExportTestCase {

    private static final Logger log = LoggerFactory.getLogger(APIImportExportTestCase.class);

    private static String api1Definition;
    private static String api2Definition;
    private static InputStream api1Doc2Stream;
    private static String importExportRootDirectory = System.getProperty("java.io.tmpdir") + File.separator +
            "import-export-test";

    @BeforeClass(description = "Initialize")
    protected void setUp () throws Exception  {
        api1Definition = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("api1_swagger_definition.json"));
        api2Definition = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("api2_swagger_definition.json"));
        api1Doc2Stream = getClass().getClassLoader().getResourceAsStream("api1_doc2.pdf");
        // if importExportRootDirectory exists, delete it
        if (new File(importExportRootDirectory).exists()) {
            log.info("The directory " + importExportRootDirectory + " already exists, and will be deleted");
            ImportExportUtils.deleteDirectory(importExportRootDirectory);
        }
    }

    @Test(description = "Test Single API export error", expectedExceptions=APIManagementException.class)
    void testSingleApiExportError () throws APIManagementException {
        APIPublisher apiPublisher = Mockito.mock(APIPublisher.class);
        String apiId = UUID.randomUUID().toString();
        API api1 = createApi("providerz", apiId, "testapi1", "1.0.0", "Test API version 1").build();
        List<API> apis = new ArrayList<>();
        apis.add(api1);

        Mockito.when(apiPublisher.getAPIbyUUID(apiId)).thenReturn(api1);
        Mockito.when(apiPublisher.getSwagger20Definition(apiId)).thenReturn(api1Definition);

        // throw an exception when apiPublisher.getAPIbyUUID method is called
        Mockito.when(apiPublisher.getAllDocumentation(apiId, 0, Integer.MAX_VALUE)).thenThrow(APIMgtEntityImportExportException.class);
        ImportExportManager importExportManager = new ImportExportManager(apiPublisher, importExportRootDirectory);
        importExportManager.exportAPIs(apis);
    }

    @Test(description = "Test Multiple APIs export with error in one API export")
    void testMultipleApiExportError () throws APIManagementException, IOException {
        APIPublisher apiPublisher = Mockito.mock(APIPublisher.class);
        String api1Id = UUID.randomUUID().toString();
        API api1 = createApi("providera", api1Id, "testapi1", "1.0.0", "Test API version 1").build();
        String api2Id = UUID.randomUUID().toString();
        API api2 = createApi("providerb", api2Id, "testapi2", "1.0.0", "Test API version 2").build();
        List<API> apis = new ArrayList<>();
        apis.add(api1);
        apis.add(api2);

        Mockito.when(apiPublisher.getAPIbyUUID(api1Id)).thenReturn(api1);
        Mockito.when(apiPublisher.getAPIbyUUID(api2Id)).thenReturn(api2);
        Mockito.when(apiPublisher.getSwagger20Definition(api1Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getSwagger20Definition(api2Id)).thenReturn(api2Definition);

        String api1Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc1Info = createAPIDoc(api1Doc1Id, "api2doc1", "", "API 2 DOC 1", DocumentInfo.DocType.API_MESSAGE_FORMAT,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.API_LEVEL);
        String api1Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc2Info = createAPIDoc(api1Doc2Id, "api2doc2", "", "API 2 DOC 2", DocumentInfo.DocType.PUBLIC_FORUM,
                "other type", DocumentInfo.SourceType.URL, "http://api2.org/documentation/1", DocumentInfo.Visibility.PRIVATE);

        List<DocumentInfo> api1DocumentInfo = new ArrayList<>();
        api1DocumentInfo.add(api1Doc1Info);
        api1DocumentInfo.add(api1Doc2Info);

        String api2Doc1Cotent = "Sample inline content for API2 DOC 1";
        DocumentContent.Builder api1Doc1ContentBuilder = new DocumentContent.Builder();
        api1Doc1ContentBuilder.documentInfo(api1Doc1Info);
        api1Doc1ContentBuilder.inlineContent(api2Doc1Cotent);
        DocumentContent.Builder api1Doc2ContentBuilder = new DocumentContent.Builder();
        api1Doc2ContentBuilder.documentInfo(api1Doc2Info);

        Mockito.when(apiPublisher.getAllDocumentation(api1Id, 0, Integer.MAX_VALUE)).thenReturn(api1DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc1Id)).thenReturn(api1Doc1ContentBuilder.build());
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc2Id)).thenReturn(api1Doc2ContentBuilder.build());

        // throw an exception when apiPublisher.getAPIbyUUID method is called
        Mockito.when(apiPublisher.getAllDocumentation(api2Id, 0, Integer.MAX_VALUE)).thenThrow(APIMgtEntityImportExportException.class);
        ImportExportManager importExportManager = new ImportExportManager(apiPublisher, importExportRootDirectory);
        String exportedApiArchiveFilePath = importExportManager.exportAPIs(apis);

        // check if only one API is exported
        String unzipPath = importExportRootDirectory + File.separator + "unzipped-export-archive";
        ImportExportUtils.extractArchive(exportedApiArchiveFilePath, unzipPath);
        Assert.assertEquals(ImportExportUtils.getDirectoryList(unzipPath).size() == 1, true,"Exported API count is not equal to 1");

        testApiImport(importExportRootDirectory);
    }

    @Test(description = "Test API export and import")
    void testCorrectApiExportAndImport () throws Exception {
        testApiExport(importExportRootDirectory);
        testApiImport(importExportRootDirectory);
    }

    @Test(description = "Test single API and import error", expectedExceptions = APIManagementException.class)
    void testSingleApiImportError () throws APIManagementException, IOException {

        APIPublisher apiPublisher = Mockito.mock(APIPublisher.class);
        String api1Id = UUID.randomUUID().toString();
        API api1 = createApi("provider1", api1Id, "testapi1", "1.0.0", "Test API 1 - version 1.0.0").build();
        List<API> apis = new ArrayList<>();
        apis.add(api1);

        Mockito.when(apiPublisher.getAPIbyUUID(api1Id)).thenReturn(api1);

        String api1Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc3Info = createAPIDoc(api1Doc3Id, "api1doc3", "", "API 1 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        List<DocumentInfo> api1DocumentInfo = new ArrayList<>();
        api1DocumentInfo.add(api1Doc3Info);

        DocumentContent.Builder api1Doc3ContentBuilder = new DocumentContent.Builder();
        api1Doc3ContentBuilder.documentInfo(api1Doc3Info);

        Mockito.when(apiPublisher.getAllDocumentation(api1Id, 0, Integer.MAX_VALUE)).thenReturn(api1DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc3Id)).thenReturn(api1Doc3ContentBuilder.build());

        Mockito.when(apiPublisher.getSwagger20Definition(api1Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getThumbnailImage(api1Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api1_thumbnail.png"));

        // export
        ImportExportManager exportManager = new ImportExportManager(apiPublisher, importExportRootDirectory);
        exportManager.exportAPIs(apis);

        // import
        FileInputStream archiveStream = null;
        doThrow(new APIManagementException("Error in updating API: " + api1.getName() + ", version: " +
                api1.getVersion())).when(apiPublisher).updateAPI(any(API.APIBuilder.class));
        try {
            archiveStream = new FileInputStream(new File(importExportRootDirectory + File.separator + "exported-apis.zip"));
            ImportExportManager importManager = new ImportExportManager(apiPublisher, importExportRootDirectory);
            importManager.importAPIs(archiveStream, null);

        } finally {
            if (archiveStream != null) {
                archiveStream.close();
            }
        }

        try {
            FileUtils.deleteDirectory(new File(importExportRootDirectory));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Test(description = "Test multiple APIs and import error")
    void testMultipleApisImportError () throws APIManagementException, IOException {

        APIPublisher apiPublisher = Mockito.mock(APIPublisher.class);
        String api1Id = UUID.randomUUID().toString();
        API api1 = createApi("provider1", api1Id, "testapi1", "1.0.0", "Test API 1 - version 1.0.0").build();
        String api2Id = UUID.randomUUID().toString();
        API api2 = createApi("provider2", api2Id, "testapi2", "2.0.0", "Test API 2 - version 2.0.0").build();
        List<API> apis = new ArrayList<>();
        apis.add(api1);
        apis.add(api2);

        Mockito.when(apiPublisher.getAPIbyUUID(api1Id)).thenReturn(api1);
        Mockito.when(apiPublisher.getAPIbyUUID(api2Id)).thenReturn(api2);

        String api1Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc3Info = createAPIDoc(api1Doc3Id, "api1doc3", "", "API 1 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);

        List<DocumentInfo> api1DocumentInfo = new ArrayList<>();
        api1DocumentInfo.add(api1Doc3Info);

        DocumentContent.Builder api1Doc3ContentBuilder = new DocumentContent.Builder();
        api1Doc3ContentBuilder.documentInfo(api1Doc3Info);

        Mockito.when(apiPublisher.getAllDocumentation(api1Id, 0, Integer.MAX_VALUE)).thenReturn(api1DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc3Id)).thenReturn(api1Doc3ContentBuilder.build());

        String api2Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api2Doc3Info = createAPIDoc(api2Doc3Id, "api2doc3", "", "API 2 DOC 3",
                DocumentInfo.DocType.OTHER, "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.PRIVATE);

        List<DocumentInfo> api2DocumentInfo = new ArrayList<>();
        api1DocumentInfo.add(api2Doc3Info);

        DocumentContent.Builder api2Doc3ContentBuilder = new DocumentContent.Builder();
        api1Doc3ContentBuilder.documentInfo(api2Doc3Info);

        Mockito.when(apiPublisher.getAllDocumentation(api2Id, 0, Integer.MAX_VALUE)).thenReturn(api2DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api2Doc3Id)).thenReturn(api2Doc3ContentBuilder.build());

        Mockito.when(apiPublisher.getSwagger20Definition(api1Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getThumbnailImage(api1Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api1_thumbnail.png"));

        Mockito.when(apiPublisher.getSwagger20Definition(api2Id)).thenReturn(api2Definition);
        Mockito.when(apiPublisher.getThumbnailImage(api2Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api2_thumbnail.jpg"));

        // export
        ImportExportManager exportManager = new ImportExportManager(apiPublisher, importExportRootDirectory);
        exportManager.exportAPIs(apis);
        // throw when update is called for API 2
        Mockito.when(apiPublisher.getAPIbyUUID(api2.getId())).thenThrow(APIManagementException.class);
//        doThrow(APIManagementException.class).when(apiPublisher).updateAPI(new API.APIBuilder("provider2", "testapi2", "2.0.0"));

        FileInputStream archiveStream = null;
        APIListDTO apiListDTO;
        try {
            archiveStream = new FileInputStream(new File(importExportRootDirectory + File.separator + "exported-apis.zip"));
            ImportExportManager importManager = new ImportExportManager(apiPublisher, importExportRootDirectory);
            apiListDTO = importManager.importAPIs(archiveStream, null);

        } finally {
            if (archiveStream != null) {
                archiveStream.close();
            }
        }

        try {
            FileUtils.deleteDirectory(new File(importExportRootDirectory));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        // only one API should be imported
        Assert.assertEquals(apiListDTO.getCount() == 1, true, "Imported API count is not equal to 1, but is " +
                apiListDTO.getCount());
        Assert.assertEquals(apiListDTO.getList().get(0).getName(), "testapi1", "Test API 1 not exported");
        Assert.assertEquals(apiListDTO.getList().get(0).getVersion(), "1.0.0", "Test API 1 not exported");
    }

    private void testApiExport (String exportDir) throws Exception {

        APIPublisher apiPublisher = Mockito.mock(APIPublisher.class);
        String api1Id = UUID.randomUUID().toString();
        API api1 = createApi("provider1", api1Id, "testapi1", "1.0.0", "Test API 1 - version 1.0.0").build();
        String api2Id = UUID.randomUUID().toString();
        API api2 = createApi("provider2", api2Id, "testapi2", "3.0.0", "Test API 2 - version 3.0.0").build();
        List<API> apis = new ArrayList<>();
        apis.add(api1);
        apis.add(api2);

        // mock the method calls for retrieving APIs
        Mockito.when(apiPublisher.getAPIbyUUID(api1Id)).thenReturn(api1);
        Mockito.when(apiPublisher.getAPIbyUUID(api2Id)).thenReturn(api2);

        // create mock DocumentInfo objects
        // API 1
        String api1Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc1Info = createAPIDoc(api1Doc1Id, "api1doc1", "", "API 1 DOC 1", DocumentInfo.DocType.HOWTO,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.PRIVATE);
        String api1Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc2Info = createAPIDoc(api1Doc2Id, "api1doc2.pdf", "api1doc2.pdf", "API 1 DOC 2",
                DocumentInfo.DocType.PUBLIC_FORUM, "other type", DocumentInfo.SourceType.FILE, "", DocumentInfo.Visibility.API_LEVEL);
        String api1Doc3Id = UUID.randomUUID().toString();
        DocumentInfo api1Doc3Info = createAPIDoc(api1Doc3Id, "api1doc3", "", "API 1 DOC 3", DocumentInfo.DocType.OTHER,
                "other type", DocumentInfo.SourceType.OTHER, "", DocumentInfo.Visibility.API_LEVEL);
        // API 2
        String api2Doc1Id = UUID.randomUUID().toString();
        DocumentInfo api2Doc1Info = createAPIDoc(api2Doc1Id, "api2doc1", "", "API 2 DOC 1", DocumentInfo.DocType.API_MESSAGE_FORMAT,
                "other type", DocumentInfo.SourceType.INLINE, "", DocumentInfo.Visibility.API_LEVEL);
        String api2Doc2Id = UUID.randomUUID().toString();
        DocumentInfo api2Doc2Info = createAPIDoc(api2Doc2Id, "api2doc2", "", "API 2 DOC 2", DocumentInfo.DocType.PUBLIC_FORUM,
                "other type", DocumentInfo.SourceType.URL, "http://api2.org/documentation/1", DocumentInfo.Visibility.PRIVATE);

        List<DocumentInfo> api1DocumentInfo = new ArrayList<>();
        api1DocumentInfo.add(api1Doc1Info);
        api1DocumentInfo.add(api1Doc2Info);
        api1DocumentInfo.add(api1Doc3Info);

        List<DocumentInfo> api2DocumentInfo = new ArrayList<>();
        api2DocumentInfo.add(api2Doc1Info);
        api2DocumentInfo.add(api2Doc2Info);

        // contents for documents
        // API 1
        String api1Doc1Cotent = "Sample inline content for API1 DOC 1";
        DocumentContent.Builder api1Doc1ContentBuilder = new DocumentContent.Builder();
        api1Doc1ContentBuilder.documentInfo(api1Doc1Info);
        api1Doc1ContentBuilder.inlineContent(api1Doc1Cotent);
        DocumentContent.Builder api1Doc2ContentBuilder = new DocumentContent.Builder();
        api1Doc2ContentBuilder.documentInfo(api1Doc2Info);
        api1Doc2ContentBuilder.fileContent(api1Doc2Stream);
        DocumentContent.Builder api1Doc3ContentBuilder = new DocumentContent.Builder();
        api1Doc3ContentBuilder.documentInfo(api1Doc3Info);
        // API 2
        String api2Doc1Cotent = "Sample inline content for API2 DOC 1";
        DocumentContent.Builder api2Doc1ContentBuilder = new DocumentContent.Builder();
        api2Doc1ContentBuilder.documentInfo(api2Doc1Info);
        api2Doc1ContentBuilder.inlineContent(api2Doc1Cotent);
        DocumentContent.Builder api2Doc2ContentBuilder = new DocumentContent.Builder();
        api2Doc1ContentBuilder.documentInfo(api2Doc2Info);

        // mock method calls for retrieving Docs and content
        // API 1
        Mockito.when(apiPublisher.getAllDocumentation(api1Id, 0, Integer.MAX_VALUE)).thenReturn(api1DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc1Id)).thenReturn(api1Doc1ContentBuilder.build());
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc2Id)).thenReturn(api1Doc2ContentBuilder.build());
        Mockito.when(apiPublisher.getDocumentationContent(api1Doc3Id)).thenReturn(api1Doc3ContentBuilder.build());
        // API 2
        Mockito.when(apiPublisher.getAllDocumentation(api2Id, 0, Integer.MAX_VALUE)).thenReturn(api2DocumentInfo);
        Mockito.when(apiPublisher.getDocumentationContent(api2Doc1Id)).thenReturn(api2Doc1ContentBuilder.build());
        Mockito.when(apiPublisher.getDocumentationContent(api2Doc2Id)).thenReturn(api2Doc2ContentBuilder.build());

        // mock method call to retrieve swagger definition
        Mockito.when(apiPublisher.getSwagger20Definition(api1Id)).thenReturn(api1Definition);
        Mockito.when(apiPublisher.getSwagger20Definition(api2Id)).thenReturn(api2Definition);

        // mock method call to retrieve thumbnail
        Mockito.when(apiPublisher.getThumbnailImage(api1Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api1_thumbnail.png"));
        Mockito.when(apiPublisher.getThumbnailImage(api2Id)).thenReturn(getClass().getClassLoader().getResourceAsStream
                ("api2_thumbnail.jpg"));

        // export
        ImportExportManager importExportManager = new ImportExportManager(apiPublisher, exportDir);
        importExportManager.exportAPIs(apis);
    }

    private void testApiImport (String importDir) throws APIManagementException, IOException {

        APIPublisher apiPublisher = Mockito.mock(APIPublisher.class);
        // Read the export archive
        FileInputStream exportedApiArchiveStream = null;
        try {
            exportedApiArchiveStream = new FileInputStream(new File(importDir + File.separator + "exported-apis.zip"));
            ImportExportManager importExportManager = new ImportExportManager(apiPublisher, importDir);
            importExportManager.importAPIs(exportedApiArchiveStream, null);
        } finally {
            if (exportedApiArchiveStream != null) {
                exportedApiArchiveStream.close();
            }
        }

        try {
            FileUtils.deleteDirectory(new File(importDir));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static API.APIBuilder createApi(String provider, String apiId, String name, String version, String description)
            throws APIManagementException {
        List<String> transport = new ArrayList<>();
        transport.add("http");


        List<String> policies = new ArrayList<>();
        policies.add("Silver");
        policies.add("Bronze");

        List<String> tags = new ArrayList<>();
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
                apiPolicy("Gold").
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(Arrays.asList("customer", "manager", "employee")).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("Adam Doe").
                lastUpdatedTime(LocalDateTime.now());


        apiBuilder.uriTemplates(Collections.emptyMap());

        return apiBuilder;
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

    @AfterClass
    protected void tearDown () {
       ImportExportUtils.deleteDirectory(importExportRootDirectory);
    }
}
