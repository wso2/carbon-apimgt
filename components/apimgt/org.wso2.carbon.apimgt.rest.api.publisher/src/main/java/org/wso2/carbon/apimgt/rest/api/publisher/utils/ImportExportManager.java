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

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtEntityImportExportException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manager class for Import and Export handling
 */
public class ImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(ImportExportManager.class);

    private static final String API_DEFINITION_FILE_NAME = "api.json";
    private static final String DOCUMENTATION_DEFINITION_FILE = "doc.json";
    private static final String SWAGGER_DEFINITION_FILE_NAME = "swagger.json";
    private static final String THUMBNAIL_FILE_NAME = "thumbnail";
    // private static final String GATEWAY_CONFIGURATION_DEFINITION_FILE = "gateway-config.json";
    private static final String DOCUMENTS_ROOT_DIRECTORY = "Documents";

    private APIPublisher apiPublisher;
    private String path;

    public ImportExportManager (APIPublisher apiPublisher, String path) {
        this.apiPublisher = apiPublisher;
        this.path = path;
    }

    /**
     * Export a given set of APIs to the file system as a zip archive.
     * The export root location is given by {@link ImportExportManager#path}/exported-apis.
     *
     * @param apis List of {@link API} objects to be exported
     * @return Path to the zip archive with exported artifacts
     * @throws APIManagementException if an error occurred while exporting APIs to file system or
     * no APIs are exported successfully
     */
    public String exportAPIs (List<API> apis) throws APIManagementException {

        String exportDirectoryName = "exported-apis";
        // this is the base directory for the archive. after export happens, this directory will
        // be archived to be sent as a application/zip response to the client
        String archiveBaseDirectoryPath = path + File.separator + exportDirectoryName;

        try {
            ImportExportUtils.createDirectory(archiveBaseDirectoryPath);
        } catch (APIMgtEntityImportExportException e) {
            ImportExportUtils.deleteDirectory(archiveBaseDirectoryPath);
            String errorMsg = "Error in creating base directory for API export archive: " + archiveBaseDirectoryPath;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.API_EXPORT_ERROR);
        }

        for (API api : apis) {
            // derive the folder structure
            // TODO: use util method to concat strings
            String apiExportDirectory = archiveBaseDirectoryPath + File.separator + api.getProvider() + "-" + api.getName()
                    + "-" + api.getVersion();
            // create per-api export directory
            try {
                ImportExportUtils.createDirectory(apiExportDirectory);
                // export API data
                exportApiDefinitionToFileSystem(apiPublisher.getAPIbyUUID(api.getId()), apiExportDirectory);
                // TODO: complete Gateway config exporting when its supported
                // exportGatewayConfigToFileSystem(api);
                exportSwaggerDefinitionToFileSystem(apiPublisher.getSwagger20Definition(api.getId()), api, apiExportDirectory);
                exportDocumentationToFileSystem(apiPublisher.getAllDocumentation(api.getId(), 0, Integer.MAX_VALUE), api, apiExportDirectory);
                exportThumbnailToFileSystem(apiPublisher.getThumbnailImage(api.getId()), api, apiExportDirectory);
                log.info("Successfully exported API: " + api.getName() + ", version: " + api.getVersion());

            } catch (APIMgtEntityImportExportException e) {
                // no need to throw, log
                log.error("Error in exporting API: " + api.getName() + ", version: " + api.getVersion(), e);
                // cleanup the API directory
                ImportExportUtils.deleteDirectory(apiExportDirectory);
            }
        }

        // if the directory is empty, no APIs have been exported!
        if (ImportExportUtils.getDirectoryList(archiveBaseDirectoryPath).isEmpty()) {
            // cleanup the archive root directory
            ImportExportUtils.deleteDirectory(path);
            String errorMsg = "No APIs exported successfully";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.API_EXPORT_ERROR);
        }

        // create zip archive
        try {
            ImportExportUtils.archiveDirectory(archiveBaseDirectoryPath, path, exportDirectoryName);
        } catch (APIMgtEntityImportExportException e) {
            // cleanup the archive root directory
            ImportExportUtils.deleteDirectory(path);
            String errorMsg = "Error while archiving directory " + archiveBaseDirectoryPath;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.API_EXPORT_ERROR);
        }

        return archiveBaseDirectoryPath + ".zip";
    }

    /**
     * Imports a set of APIs to API Manager by reading and decoding the {@param uploadedApiArchiveInputStream}
     *
     * @param uploadedApiArchiveInputStream  InputStream to be read ana decoded to a set of APIs
     * @return {@link APIListDTO} object comprising of successfully imported APIs
     * @throws APIManagementException if any error occurs while importing or no APIs are imported
     * successfully
     */
    public APIListDTO importAPIs (InputStream uploadedApiArchiveInputStream) throws APIManagementException {

        String importedDirectoryName = "imported-apis";
        String apiArchiveLocation = path + File.separator + importedDirectoryName + ".zip";
        String archiveExtractLocation = extractUploadedArchive(uploadedApiArchiveInputStream, importedDirectoryName,
                apiArchiveLocation);

        Set<String> apiDefinitionsRootDirectoryPaths = ImportExportUtils.getDirectoryList(archiveExtractLocation);
        if (apiDefinitionsRootDirectoryPaths.isEmpty()) {
            ImportExportUtils.deleteDirectory(path);
            String errorMsg = "Unable to find API definitions at: " + archiveExtractLocation;
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.API_IMPORT_ERROR);
        }

        // List to contain newly created/updated APIs
        List<API> apis = importApisFromExtractedArchive(apiDefinitionsRootDirectoryPaths);

        ImportExportUtils.deleteDirectory(path);
        // if no APIs are corrected exported, throw an error
        if (apis.isEmpty()) {
            String errorMsg = "No APIs imported successfully";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.API_IMPORT_ERROR);
        }

        return MappingUtil.toAPIListDTO(apis);
    }

    /**
     * Reads and decodes APIs and relevant information from the given set of paths
     *
     * @param apiDefinitionsRootDirectoryPaths path to the directory with API related artifacts
     * @return List of {@link API} objects
     */
    private List<API> importApisFromExtractedArchive(Set<String> apiDefinitionsRootDirectoryPaths) {

        List<API> apis = new ArrayList<>();

        for (String apiDefinitionDirectoryPath : apiDefinitionsRootDirectoryPaths) {
            File apiDefinitionFile = new File(apiDefinitionDirectoryPath + File.separator + API_DEFINITION_FILE_NAME);
            if (!apiDefinitionFile.exists()) {
                // API definition file not found, skip this API
                log.error("Unable to locate API definition file at: " + apiDefinitionFile.getPath());
                continue;
            }

            // convert to bean
            Gson gson = new GsonBuilder().create();
            String apiDefinitionString;
            try {
                apiDefinitionString = ImportExportUtils.readFileContentAsText(apiDefinitionFile.getPath());
            } catch (APIMgtEntityImportExportException e) {
                // Unable to read the API definition file, skip this API
                log.error("Error reading API definition from file system", e);
                continue;
            }

            APIDTO apiDto;
            try {
                apiDto = gson.fromJson(apiDefinitionString, APIDTO.class);
            } catch (Exception e) {
                log.error("Error in building APIDTO from api definition read from file system", e);
                continue;
            }

            API.APIBuilder apiBuilder = MappingUtil.toAPI(apiDto);
            // import API data - should fail for this API if an error occurs,
            // but should continue with importing other APIs
            try {
                importApi(apiBuilder);
            } catch (APIManagementException e) {
                log.error("Error in importing API: " + apiBuilder.getName() + ", version: " + apiBuilder.getVersion(), e);
                continue;
            }
            apis.add(apiBuilder.build());

            // importing docs and thumbnails - will still continue if an error occurs
            importDocumentation(apiDefinitionDirectoryPath, apiBuilder.getId(), apiBuilder.getName(), apiBuilder.getVersion());
            importThumbnail(apiDefinitionDirectoryPath, apiBuilder.getId(), apiBuilder.getName(), apiBuilder.getVersion());

            log.info("Successfully imported API: " + apiBuilder.getName() + ", version: " + apiBuilder.getVersion());
        }

        return apis;
    }

    /**
     * Extracts the APIs to the file system by reading the incoming {@link InputStream} object uploadedApiArchiveInputStream
     *
     * @param uploadedApiArchiveInputStream Incoming {@link InputStream}
     * @param importedDirectoryName directory to extract the archive
     * @param apiArchiveLocation full path to the location to which the archive will be written
     * @return location to which APIs were extracted
     * @throws APIManagementException if an error occurs while extracting the archive
     */
    private String extractUploadedArchive(InputStream uploadedApiArchiveInputStream, String importedDirectoryName,  String apiArchiveLocation) throws APIManagementException {
        String archiveExtractLocation;
        try {
            // create api import directory structure
            ImportExportUtils.createDirectory(path);
            // create archive
            ImportExportUtils.createArchiveFromInputStream(uploadedApiArchiveInputStream, apiArchiveLocation);
            // extract the archive
            archiveExtractLocation = path + File.separator + importedDirectoryName;
            ImportExportUtils.extractArchive(apiArchiveLocation, archiveExtractLocation);

        } catch (APIMgtEntityImportExportException e) {
            ImportExportUtils.deleteDirectory(path);
            String errorMsg = "Error in accessing uploaded API archive";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.API_IMPORT_ERROR);
        }
        return archiveExtractLocation;
    }

    /**
     * write the given API definition to file system
     *
     * @param api {@link API} object to be exported
     * @param exportLocation file system location to write the API definition
     * @throws APIMgtEntityImportExportException if an error occurs while writing the API definition
     */
    private void exportApiDefinitionToFileSystem(API api, String exportLocation) throws APIMgtEntityImportExportException {

        APIDTO apidto = MappingUtil.toAPIDto(api);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ImportExportUtils.createFile(exportLocation + File.separator + API_DEFINITION_FILE_NAME);
        ImportExportUtils.writeToFile(exportLocation + File.separator + API_DEFINITION_FILE_NAME, gson.toJson(apidto));

        log.debug("Successfully exported API definition for api: " + api.getName() + ", version: " + api.getVersion());
    }

    /*
    private void exportGatewayConfigToFileSystem(API api, String exportLocation) throws APIManagementException {

        String gatewayConfig = apiPublisher.getApiGatewayConfig(api.getId());
        if (gatewayConfig == null) {
            // not gateway config found, return
            log.info("No gateway configuration found for API with api: " + api.getName() + ", version: " + api.getVersion());
            return;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(gatewayConfig).getAsJsonObject();

        String gatewayConfigFileLocation = exportLocation + File.separator + GATEWAY_CONFIGURATION_DEFINITION_FILE;
        ImportExportUtils.createFile(gatewayConfigFileLocation);
        ImportExportUtils.writeToFile(gatewayConfigFileLocation, gson.toJson(json));
    }
    */

    /**
     * Writes the given List of {@link DocumentInfo} objects to the file system
     *
     * @param documentInfo list of {@link DocumentInfo} objects
     * @param api {@link API} instance, to which the documents are related to
     * @param exportLocation file system location to which documents will be written
     * @throws APIMgtEntityImportExportException if any error occurs while writing the docs to the file system
     */
    private void exportDocumentationToFileSystem(List<DocumentInfo> documentInfo, API api, String exportLocation) throws APIMgtEntityImportExportException {

        if (documentInfo == null || documentInfo.isEmpty()) {
            log.debug("No documentation found for API with api: " + api.getName() + ", version: " + api.getVersion());
            return;
        }

        // create Documents root directory
        String documentsBaseDirectory = exportLocation + File.separator + DOCUMENTS_ROOT_DIRECTORY;
        ImportExportUtils.createDirectory(documentsBaseDirectory);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (DocumentInfo aDocumentInfo : documentInfo) {
            // create the root directory for each document
            String apiExportDir = documentsBaseDirectory + File.separator+ aDocumentInfo.getId();
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

    /**
     * Retrieves Document content for the given {@link DocumentInfo} object {@param aDocumentInfo}
     *
     * @param api {@link API} api instance
     * @param aDocumentInfo {@link DocumentInfo} instance
     * @return Content of the document if exists, else null
     */
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

    /**
     * Writes the Swagger definition to file system
     *
     * @param swaggerDefinition swagger definition
     * @param api {@link API} instance relevant to the swagger definition
     * @param exportLocation file system location to which the swagger definition will be written
     * @throws APIMgtEntityImportExportException if an error occurs while writing swagger
     * definition to the file system
     */
    private void exportSwaggerDefinitionToFileSystem(String swaggerDefinition, API api, String exportLocation) throws
            APIMgtEntityImportExportException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(swaggerDefinition).getAsJsonObject();

        String swaggerFileLocation = exportLocation + File.separator + SWAGGER_DEFINITION_FILE_NAME;
        ImportExportUtils.createFile(swaggerFileLocation);
        ImportExportUtils.writeToFile(swaggerFileLocation, gson.toJson(json));

        log.debug("Successfully exported Swagger definition for api: " + api.getName() + ", version: " + api.getVersion());
    }

    /**
     * Writes the API thumbnail to file system
     *
     * @param thumbnailInputStream {@link InputStream} instance with thumbnail data
     * @param api {@link API} instance relevant to thumbnail
     * @param exportLocation file system location to which the thumbnail will be written
     * @throws APIMgtEntityImportExportException if an error occurs while writing the thumbnail
     * to file system
     */
    private void exportThumbnailToFileSystem(InputStream thumbnailInputStream, API api, String exportLocation) throws
            APIMgtEntityImportExportException {

        if (thumbnailInputStream == null) {
            // no thumbnail found, return
            log.debug("No thumbnail found for API with api: " + api.getName() + ", version: " + api.getVersion());
            return;
        }
        String thumbnailFileLocation = exportLocation + File.separator + THUMBNAIL_FILE_NAME;

        ImportExportUtils.createFile(thumbnailFileLocation);
        ImportExportUtils.writeStreamToFile(thumbnailFileLocation, thumbnailInputStream);

        log.debug("Successfully exported Thumbnail for api: " + api.getName() + ", version: " + api.getVersion());
    }

    /**
     * Imports the given API instance to this API Manager
     *
     * @param apiBuilder {@link org.wso2.carbon.apimgt.core.models.API.APIBuilder} instance to be imported
     * @throws APIManagementException if an error occurs while importing the API
     */
    private void importApi(API.APIBuilder apiBuilder) throws APIManagementException {

        // if the API already exists, can't import again
        if (apiPublisher.getAPIbyUUID(apiBuilder.getId()) != null) {
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

    /*
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
    */

    /**
     * Imports a set of Documents to this API Manager
     *
     * @param importLocation file system location in which document artifacts reside
     * @param apiId uuid of the API
     * @param apiName name of the API
     * @param version version of the API
     */
    private void importDocumentation (String importLocation, String apiId, String apiName, String version) {

        File rootDocumentationDirectoryForAPI = new File(importLocation + DOCUMENTS_ROOT_DIRECTORY);
        if (!rootDocumentationDirectoryForAPI.isDirectory()) {
            // no Docs!
            log.debug("No documentation found for API name: " + apiName + ", version: " + version);
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
            String content;
            try {
                content = ImportExportUtils.readFileContentAsText(documentationPath + DOCUMENTATION_DEFINITION_FILE);
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
            } catch (APIManagementException e) {
                // no need to throw, log and continue
                log.error("Error in importing documentation for API: " + apiName + ", version: " + version);
            }
        }

        log.debug("Successfully imported documentation for API: " + apiName + ", version: " + version);
    }

    /**
     * Imports the thumbnail for the relevant API
     *
     * @param importLocation file system location where the thumbnail resides
     * @param apiId uuid of the API
     * @param apiName name of the API
     * @param version version of the API
     */
    private void importThumbnail(String importLocation, String apiId, String apiName, String version) {

        File thumbnailFile = new File(importLocation + File.separator + THUMBNAIL_FILE_NAME);
        if (!thumbnailFile.exists()) {
            // Thumbnail not found
            log.debug("Thumbnail image not found for API: " + apiName + ", version: " + version);
            return;
        }

        InputStream thumbnailStream;
        try {
            thumbnailStream = ImportExportUtils.readFileContentAsStream(importLocation + File.separator + THUMBNAIL_FILE_NAME);
            apiPublisher.saveThumbnailImage(apiId, thumbnailStream, THUMBNAIL_FILE_NAME);
        } catch (APIManagementException e) {
            // no need to throw, log and continue
            log.error("Error in importing thumbnail for API: " + apiName + ", version: " + version, e);
        }

        log.debug("Successfully imported Thumbnail for API: " + apiName + ", version: " + version);
    }

}
