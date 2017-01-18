/***********************************************************************************************************************
 *
 *  *
 *  *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *   WSO2 Inc. licenses this file to you under the Apache License,
 *  *   Version 2.0 (the "License"); you may not use this file except
 *  *   in compliance with the License.
 *  *   You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Manager class for Import and Export handling
 */
public class ImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(ImportExportManager.class);

    private static final String API_DEFINITION_FILE_NAME = "api.json";
    private static final String DOCUMENTATION_DEFINITION_FILE = "doc.json";
    private static final String SWAGGER_DEFINITION_FILE_NAME = "swagger.json";
    private static final String THUMBNAIL_FILE_NAME = "thumbnail";
    private static final String EXPORTED_API_DIRECTORY_NAME = "exported-apis";
    private static final String IMPORTED_API_DIRECTORY_NAME = "imported-apis";
    // private static final String GATEWAY_CONFIGURATION_DEFINITION_FILE = "gateway-config.json";
    private static final String DOCUMENTS_ROOT_DIRECTORY = "Documents";

    private APIPublisher apiPublisher;
    private String path;

    public ImportExportManager (APIPublisher apiPublisher, String path) {
        this.apiPublisher = apiPublisher;
        this.path = path;
    }

    public String exportAPIs (List<API> apis) throws APIManagementException {

        // this is a directory with a unique path to stop conflicts in case of parallel exports
        String uniqueDirectory = path + File.separator + EXPORTED_API_DIRECTORY_NAME + "-" + UUID.randomUUID().toString();
        // this is the base directory for the archive. after export happens, this directory will
        // be archived to be sent as a application/zip response to the client
        String archiveBaseDirectory = uniqueDirectory + File.separator + EXPORTED_API_DIRECTORY_NAME;

        for (API api : apis) {
            // derive the folder structure
            // TODO: use util method to concat strings
            String apiExportDirectory = archiveBaseDirectory + File.separator + api.getProvider() + "-" + api.getName()
                    + "-" + api.getVersion();
            // create api export directory
            try {
                ImportExportUtils.createDirectory(apiExportDirectory);
                // export API data
                exportApiDefinitionToFileSystem(api, apiExportDirectory);
                // exportGatewayConfigToFileSystem(api);
                exportDocumentationToFileSystem(api, apiExportDirectory);
                exportSwaggerDefinitionToFileSystem(api, apiExportDirectory);
                exportThumbnailToFileSystem(api, apiExportDirectory);
            } catch (APIManagementException e) {
                // no need to throw, log
                log.error("Error in exporting API: " + api.getName() + ", version: " + api.getVersion(), e);
                // cleanup the API directory
                ImportExportUtils.deleteDirectory(apiExportDirectory);
                // skip this API and continue
                continue;
            }
        }

        // create zip archive
        try {
            ImportExportUtils.archiveDirectory(archiveBaseDirectory, uniqueDirectory, EXPORTED_API_DIRECTORY_NAME);
        } catch (APIManagementException e) {
            // cleanup the archive root directory
            ImportExportUtils.deleteDirectory(archiveBaseDirectory);
            throw e;
        }

        return archiveBaseDirectory + ".zip";
    }

    public void importAPIs (InputStream fileInputStream) throws APIManagementException {

        String importedApiArchiveUniqueDirectory = path + File.separator + IMPORTED_API_DIRECTORY_NAME + "-" + UUID.randomUUID()
                .toString();
        String importedApiArchive = importedApiArchiveUniqueDirectory + File.separator + IMPORTED_API_DIRECTORY_NAME + ".zip";

        // create api export directory structure
        ImportExportUtils.createDirectory(importedApiArchiveUniqueDirectory);
        // create archive
        ImportExportUtils.createArchiveFromUploadedData(fileInputStream, importedApiArchive);
        // extract
        String archiveExtractLocation = importedApiArchiveUniqueDirectory + File.separator + IMPORTED_API_DIRECTORY_NAME;
        ImportExportUtils.extractArchive(importedApiArchive, archiveExtractLocation);

        File[] apiDefinitionsRootDirectories = new File(archiveExtractLocation).listFiles(File::isDirectory);
        if (apiDefinitionsRootDirectories == null) {
            throw new APIManagementException("Unable to find API definitions at " + archiveExtractLocation);
        }

        for (File apiDefinitionDirectory : apiDefinitionsRootDirectories) {

            File apiDefinitionFile = new File(apiDefinitionDirectory + File.separator + API_DEFINITION_FILE_NAME);
            if (!apiDefinitionFile.exists()) {
                // API definition file not found, can't continue
                throw new APIManagementException("Unable to locate API definition file at: " + apiDefinitionFile.getPath());
            }

            String apiDefinitionString = ImportExportUtils.readFileContentAsText(apiDefinitionFile.getPath());
            // convert to bean
            Gson gson = new GsonBuilder().create();
            APIDTO apiDto = gson.fromJson(apiDefinitionString, APIDTO.class);
            API.APIBuilder apiBuilder = MappingUtil.toAPI(apiDto);

            // import API data - should fail an error occurs
            importApi(apiBuilder);
            // import gateway configuration - should fail an error occurs
            // TODO: uncomment when gateway config is supported
            // importGatewayConfig(apiDefinitionDirectory.getPath(), apiBuilder.getId(), apiBuilder.getName(), apiBuilder.getVersion());

            try {
                importSwaggerDefinition(apiDefinitionDirectory.getPath(), apiBuilder.getId(), apiBuilder.getName(), apiBuilder.getVersion());
                importDocumentation(apiDefinitionDirectory.getPath(), apiBuilder.getId(), apiBuilder.getName(), apiBuilder.getVersion());
                importThumbnail(apiDefinitionDirectory.getPath(), apiBuilder.getId(), apiBuilder.getName(), apiBuilder.getVersion());

            } catch (APIManagementException e) {
                // no need to throw, log
                log.error("Error in exporting API: " + apiBuilder.getName() + ", version: " + apiBuilder.getVersion(), e);
                // skip this API and continue
                continue;
            }
        }

        //TODO: delete after importing
    }

    private void exportApiDefinitionToFileSystem(API api, String exportLocation) throws APIManagementException {

        APIDTO apidto = MappingUtil.toAPIDto(apiPublisher.getAPIbyUUID(api.getId()));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ImportExportUtils.createFile(exportLocation + File.separator + API_DEFINITION_FILE_NAME);
        ImportExportUtils.writeToFile(exportLocation + File.separator + API_DEFINITION_FILE_NAME, gson.toJson(apidto));

        log.debug("Successfully exported API definition for api: " + api.getName() + ", version: " + api.getVersion());
    }

    /*
    private void exportGatewayConfigToFileSystem(API api) throws APIManagementException {

        String gatewayConfig = apiPublisher.getApiGatewayConfig(api.getId());
        if (gatewayConfig == null) {
            // not gateway config found, return
            log.info("No gateway configuration found for API with api: " + api.getName() + ", version: " + api.getVersion());
            return;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(gatewayConfig).getAsJsonObject();

        String gatewayConfigFileLocation = path + File.separator + GATEWAY_CONFIGURATION_DEFINITION_FILE;
        ImportExportUtils.createFile(gatewayConfigFileLocation);
        ImportExportUtils.writeToFile(gatewayConfigFileLocation, gson.toJson(json));
    }
    */

    private void exportDocumentationToFileSystem(API api, String exportLocation) throws APIManagementException {

        List<DocumentInfo> documentInfo = apiPublisher.getAllDocumentation(api.getId(), 0, Integer.MAX_VALUE);
        if (documentInfo == null || documentInfo.isEmpty()) {
            log.info("No documentation found for API with api: " + api.getName() + ", version: " + api.getVersion());
            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (DocumentInfo aDocumentInfo : documentInfo) {
            // create the root directory for each document
            String apiExportDir = exportLocation + File.separator+ aDocumentInfo.getId();
            ImportExportUtils.createDirectory(apiExportDir);
            // for each document, write a DocumentInfo to a separate json file
            String apiDocMetaFileLocation = apiExportDir + File.separator + DOCUMENTATION_DEFINITION_FILE;
            ImportExportUtils.createFile(apiDocMetaFileLocation);
            ImportExportUtils.writeToFile(apiDocMetaFileLocation, gson.toJson(aDocumentInfo));

            // if the document's SourceType is FILE, retrieve and write the content to a file
            DocumentContent content;
            if (aDocumentInfo.getSourceType().equals(DocumentInfo.SourceType.FILE)) {
                // TODO: cleanup the logic in a better way
                content = getDocumentContent(api, aDocumentInfo);
                if (content != null) {
                    ImportExportUtils.createFile(apiExportDir + File.separator + content.getDocumentInfo().getFileName());
                    ImportExportUtils.writeStreamToFile(apiExportDir + File.separator + content.getDocumentInfo().getFileName(),
                            content.getFileContent());
                }
            } else if (aDocumentInfo.getSourceType().equals(DocumentInfo.SourceType.INLINE)) {
                content = getDocumentContent(api, aDocumentInfo);
                if (content != null) {
                    ImportExportUtils.createFile(apiExportDir + File.separator + content.getDocumentInfo().getName());
                    ImportExportUtils.writeToFile(apiExportDir + File.separator + content.getDocumentInfo().getName(),
                            content.getInlineContent());
                }
            }
        }

        log.debug("Successfully exported documentation for api: " + api.getName() + ", version: " + api.getVersion());
    }

    private DocumentContent getDocumentContent(API api, DocumentInfo aDocumentInfo) {
        try {
            return apiPublisher.getDocumentationContent(aDocumentInfo.getId());
        } catch (APIManagementException e) {
            // no need to throw, log and continue
            log.error("Error in retrieving content for document id: " + api.getId() + ", source type: " + aDocumentInfo.getSourceType()
                    + ", api: " + api.getName() + ", version: " + api.getVersion(), e);
        }
        return null;
    }

    private void exportSwaggerDefinitionToFileSystem(API api, String exportLocation) throws APIManagementException {

        String swaggerDefinition = apiPublisher.getSwagger20Definition(api.getId());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(swaggerDefinition).getAsJsonObject();

        String swaggerFileLocation = exportLocation + File.separator + SWAGGER_DEFINITION_FILE_NAME;
        ImportExportUtils.createFile(swaggerFileLocation);
        ImportExportUtils.writeToFile(swaggerFileLocation, gson.toJson(json));

        log.debug("Successfully exported Swagger definition for api: " + api.getName() + ", version: " + api.getVersion());
    }

    private void exportThumbnailToFileSystem(API api, String exportLocation) throws APIManagementException {

        InputStream thumbnailInputStream = apiPublisher.getThumbnailImage(api.getId());
        if (thumbnailInputStream == null) {
            // no thumbnail found, return
            log.info("No thumbnail found for API with api: " + api.getName() + ", version: " + api.getVersion());
            return;
        }
        String thumbnailFileLocation = exportLocation + File.separator + THUMBNAIL_FILE_NAME;

        ImportExportUtils.createFile(thumbnailFileLocation);
        ImportExportUtils.writeStreamToFile(thumbnailFileLocation, thumbnailInputStream);

        log.debug("Successfully exported Thumbnail for api: " + api.getName() + ", version: " + api.getVersion());
    }

    private void importApi(API.APIBuilder apiBuilder) throws APIManagementException {

        // if the API already exists, can't import again
        if (apiPublisher.getAPIbyUUID(apiBuilder.getId()) != null) {
            // TODO: should this be moved to PUT operation
            apiPublisher.updateAPI(apiBuilder);
        } else {
            apiPublisher.addAPI(apiBuilder);
        }

        log.debug("Successfully imported API definition for: " + apiBuilder.getName() + ", version: " + apiBuilder.getVersion());
    }

    /*
    private void importGatewayConfig (String importLocation, String apiId, String apiName, String version) throws APIManagementException {

        File gatewayConfigFile = new File(importLocation + File.separator + GATEWAY_CONFIGURATION_DEFINITION_FILE);
        if (!gatewayConfigFile.exists()) {
            throw new APIManagementException("Gateway Configuration file not found for API: " +
                    apiName + ", version: " + version + ", hence unable to import API");
        }

        String gatewayConfigString = ImportExportUtils.readFileContentAsText(gatewayConfigFile.getPath());
        apiPublisher.updateApiGatewayConfig(apiId, gatewayConfigString);

        log.debug("Successfully imported Gateway Configuration for API: " + apiName + ", version: " + version);
    }
    */

    private void importSwaggerDefinition(String importLocation, String apiId, String apiName, String version) throws APIManagementException {

        File swaggerDefinitionFile = new File(importLocation + File.separator + SWAGGER_DEFINITION_FILE_NAME);
        if (!swaggerDefinitionFile.exists()) {
            // Swagger definition not found
            log.warn("Swagger definition not found for API: " + apiName + ", version: " + version + ", hence unable to import the Swagger definition");
            return;
        }

        String swaggerDefinitionString = ImportExportUtils.readFileContentAsText(importLocation + File.separator + SWAGGER_DEFINITION_FILE_NAME);
        apiPublisher.saveSwagger20Definition(apiId, swaggerDefinitionString);
        log.debug("Successfully imported Swagger definition for API: " + apiName + ", version: " + version);
    }

    private void importDocumentation (String importLocation, String apiId, String apiName, String version) throws APIManagementException {

        File rootDocumentationDirectoryForAPI = new File(importLocation + DOCUMENTS_ROOT_DIRECTORY);
        if (!rootDocumentationDirectoryForAPI.isDirectory()) {
            // no Docs!
            log.warn("No documentation found for API name: " + apiName + ", version: " + version);
            return;
        }

        String[] documentationPathList = rootDocumentationDirectoryForAPI.list();
        if (documentationPathList == null) {
            // do docs!
            return;
        }

        for (String documentationPath : documentationPathList) {
            // each should be a directory
            File documentationDirectory = new File(documentationPath);
            if (!documentationDirectory.isDirectory()) {
                log.warn("Path " + documentationPath + " does not contain a directory, skipping");
                continue;
            }

            // first read the 'doc.json'
            String content = ImportExportUtils.readFileContentAsText(documentationPath + DOCUMENTATION_DEFINITION_FILE);
            Gson gson = new GsonBuilder().create();
            DocumentInfo aDocumentInfo = gson.fromJson(content, DocumentInfo.class);
            // add the doc
            apiPublisher.addDocumentationInfo(apiId, aDocumentInfo);
            // read doc content, if {@link SourceType} is either File or Inline
            // TODO: refactor this logic properly
            if (aDocumentInfo.getSourceType().equals(DocumentInfo.SourceType.FILE)) {
                InputStream inputStream = ImportExportUtils.readFileContentAsStream(documentationPath + File.separator + aDocumentInfo.getFileName());
                apiPublisher.uploadDocumentationFile(aDocumentInfo.getId(), inputStream, aDocumentInfo.getFileName());
            } else if (aDocumentInfo.getSourceType().equals(DocumentInfo.SourceType.INLINE)) {
                String inlineContent = ImportExportUtils.readFileContentAsText(documentationPath + File.separator + aDocumentInfo.getName());
                apiPublisher.addDocumentationContent(aDocumentInfo.getId(), inlineContent);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Successfully imported documentation for API: " + apiName + ", version: " + version);
        }
    }

    private void importThumbnail(String importLocation, String apiId, String apiName, String version) throws APIManagementException {

        File thumbnailFile = new File(importLocation + File.separator + THUMBNAIL_FILE_NAME);
        if (!thumbnailFile.exists()) {
            // Thumbnail not found
            log.warn("Thumbnail image not found for API: " + apiName + ", version: " + version + ", hence unable to import the Swagger definition");
            return;
        }

        InputStream thumbnailStream = ImportExportUtils.readFileContentAsStream(importLocation + File.separator + THUMBNAIL_FILE_NAME);
        // TODO: get the correct image name instead of THUMBNAIL_FILE_NAME
        apiPublisher.saveThumbnailImage(apiId, thumbnailStream, THUMBNAIL_FILE_NAME);

        log.debug("Successfully imported Thumbnail for API: " + apiName + ", version: " + version);
    }

}
