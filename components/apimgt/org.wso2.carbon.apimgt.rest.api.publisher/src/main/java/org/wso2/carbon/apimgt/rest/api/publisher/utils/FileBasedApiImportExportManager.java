/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
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
import org.wso2.carbon.apimgt.core.models.APIDetails;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Manager class for File system based API Import and Export handling
 */
public class FileBasedApiImportExportManager extends ApiImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(FileBasedApiImportExportManager.class);

    private static final String API_DEFINITION_FILE_NAME = "api.json";
    private static final String DOCUMENTATION_DEFINITION_FILE = "doc.json";
    private static final String SWAGGER_DEFINITION_FILE_NAME = "swagger.json";
    private static final String THUMBNAIL_FILE_NAME = "thumbnail";
    // TODO
    /*
    private static final String GATEWAY_CONFIGURATION_DEFINITION_FILE = "gateway-config.json";
    */
    private static final String DOCUMENTS_ROOT_DIRECTORY = "Documents";
    private String path;

    public FileBasedApiImportExportManager(APIPublisher apiPublisher, String path) {
        super(apiPublisher);
        this.path = path;
    }

    /**
     * Export a given set of APIs to the file system as a zip archive.
     * The export root location is given by {@link FileBasedApiImportExportManager#path}/exported-apis.
     *
     * @param apiDetailSet Set of {@link APIDetails} objects to be exported
     * @return Path to the zip archive with exported artifacts
     * @throws APIManagementException if an error occurred while exporting APIs to file system or
     * no APIs are exported successfully
     */
    public String exportAPIs(Set<APIDetails> apiDetailSet) throws APIManagementException {

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

        for (APIDetails apiDetails : apiDetailSet) {
            // derive the folder structure
            // TODO: use util method to concat strings
            String apiExportDirectory = archiveBaseDirectoryPath + File.separator +
                    apiDetails.getApi().getProvider() + "-" + apiDetails.getApi().getName() + "-" + apiDetails.
                    getApi().getVersion();
            // create per-api export directory
            try {
                ImportExportUtils.createDirectory(apiExportDirectory);
                // export API data
                exportApiDefinitionToFileSystem(apiDetails.getApi(), apiExportDirectory);
                exportSwaggerDefinitionToFileSystem(apiDetails.getSwaggerDefinition(), apiDetails.getApi(),
                        apiExportDirectory);
                //exportGatewayConfigToFileSystem(apiDetails.getGatewayConfiguration(), apiDetails.getApi(), apiExportDirectory);

            } catch (APIMgtEntityImportExportException e) {
                // no need to throw, log
                log.error(
                        "Error in exporting API: " + apiDetails.getApi().getName() + ", version: " + apiDetails.getApi()
                                .getVersion(), e);
                // cleanup the API directory
                ImportExportUtils.deleteDirectory(apiExportDirectory);
                // skip this API
                continue;
            }

            // export docs and thumbnail - these are non critical; even if they fail the API is considered
            // as exported correctly.
            exportDocumentationToFileSystem(apiDetails.getAllDocumentInformation(), apiDetails, apiExportDirectory);
            exportThumbnailToFileSystem(apiDetails.getThumbnailStream(), apiDetails.getApi(), apiExportDirectory);

            log.info(
                    "Successfully exported API: " + apiDetails.getApi().getName() + ", version: " + apiDetails.getApi().
                            getVersion());
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
     * @param provider API provider, if needs to be updated
     * @return {@link APIListDTO} object comprising of successfully imported APIs
     * @throws APIManagementException if any error occurs while importing or no APIs are imported
     * successfully
     */
    public APIListDTO importAPIs(InputStream uploadedApiArchiveInputStream, String provider)
            throws APIManagementException {

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
        List<API> apis = importApisFromExtractedArchive(apiDefinitionsRootDirectoryPaths, provider);

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
     * @param newApiProvider API newApiProvider to be updated
     * @return List of {@link API} objects
     */
    private List<API> importApisFromExtractedArchive(Set<String> apiDefinitionsRootDirectoryPaths,
            String newApiProvider) {

        List<API> apis = new ArrayList<>();

        for (String apiDefinitionDirectoryPath : apiDefinitionsRootDirectoryPaths) {
            File apiDefinitionFile = new File(apiDefinitionDirectoryPath + File.separator + API_DEFINITION_FILE_NAME);
            if (!apiDefinitionFile.exists()) {
                // API definition file not found, skip this API
                log.error("Unable to locate API definition file at: " + apiDefinitionFile.getPath());
                continue;
            }

            APIDTO apiDto;
            try {
                apiDto = getApiDefinitionFromExtractedArchive(apiDefinitionFile.getPath());
            } catch (APIManagementException e) {
                log.error(e.getMessage(), e);
                // skip this API
                continue;
            }
            if (newApiProvider != null && !newApiProvider.isEmpty()) {
                // update the newApiProvider
                apiDto.setProvider(newApiProvider);
            }

            String swaggerDefinition;
            try {
                swaggerDefinition = getSwaggerDefinitionFromExtractedArchive(apiDefinitionDirectoryPath +
                        File.separator + SWAGGER_DEFINITION_FILE_NAME);
            } catch (APIManagementException e) {
                log.error(e.getMessage(), e);
                // skip this API
                continue;
            }

            // TODO:
            /*
            String gatewayConfiguration;
            try {
                gatewayConfiguration = getGatewayConfigurationFromExtractedArchive(apiDefinitionDirectoryPath + File.separator +
                        GATEWAY_CONFIGURATION_DEFINITION_FILE);
            } catch (APIMgtEntityImportExportException e) {
                log.error(e.getMessage(), e);
                // skip this API
                continue;
            }
            */

            // TODO
            String documentsRootDirectory = apiDefinitionDirectoryPath + File.separator + DOCUMENTS_ROOT_DIRECTORY;
            Set<DocumentInfo> documentInfoSet = getDocumentInfoFromExtractedArchive(documentsRootDirectory,
                    apiDto.getName(), apiDto.getVersion());
            Set<DocumentContent> documentContents = new HashSet<>();
            for (DocumentInfo aDocumentInfo : documentInfoSet) {
                DocumentContent aDocumentContent = getDocumentContentFromExtractedArchive(aDocumentInfo,
                        documentsRootDirectory + File.separator + aDocumentInfo.getId());
                if (aDocumentContent != null) {
                    documentContents.add(aDocumentContent);
                }
            }

            InputStream thumbnailStream = getThumbnailFromExtractedArchive(apiDefinitionDirectoryPath + File.separator +
                    THUMBNAIL_FILE_NAME);

            APIDetails apiDetails = new APIDetails(MappingUtil.toAPI(apiDto).build(), swaggerDefinition);
            // TODO
            /*
            if (gatewayConfiguration != null) {
                apiDetails.setGatewayConfiguration(gatewayConfiguration);
            }
            */
            if (!documentInfoSet.isEmpty()) {
                apiDetails.addDocumentInformation(documentInfoSet);
            }
            if (!documentContents.isEmpty()) {
                apiDetails.addDocumentContents(documentContents);
            }
            if (thumbnailStream != null) {
                apiDetails.setThumbnailStream(thumbnailStream);
            }

            try {
                apis.add(importApi(apiDetails));
            } catch (APIManagementException e) {
                log.error("Error while importing API: " + apiDetails.getApi().getName() + ", version: " +
                        apiDetails.getApi().getVersion());
                // skip the API
                continue;
            }

            log.info("Successfully imported API: " + apiDto.getName() + ", version: " + apiDto.getVersion());
        }

        return apis;
    }

    /**
     * Creates {@link APIDTO} instance from API Definition file
     *
     * @param apiDefinitionFilePath path to api definition file
     * @return {@link APIDTO} instance
     * @throws APIManagementException if an error occurs while creating API definition object
     */
    private APIDTO getApiDefinitionFromExtractedArchive(String apiDefinitionFilePath) throws APIManagementException {

        String apiDefinitionString;
        try {
            apiDefinitionString = ImportExportUtils.readFileContentAsText(apiDefinitionFilePath);
        } catch (APIMgtEntityImportExportException e) {
            // Unable to read the API definition file, skip this API
            String errorMsg = "Error reading API definition from file at: " + apiDefinitionFilePath;
            throw new APIManagementException(errorMsg, e);
        }

        // convert to bean
        Gson gson = new GsonBuilder().create();
        try {
            return gson.fromJson(apiDefinitionString, APIDTO.class);
        } catch (Exception e) {
            String errorMsg =
                    "Error in building APIDTO from api definition read from file at: " + apiDefinitionFilePath;
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Creates swagger definition from file
     *
     * @param swaggerDefinitionFilePath path to swagger definition file
     * @return swagger definition
     * @throws APIManagementException if an error occurs while swagger definition
     */
    private String getSwaggerDefinitionFromExtractedArchive(String swaggerDefinitionFilePath)
            throws APIManagementException {

        try {
            return ImportExportUtils.readFileContentAsText(swaggerDefinitionFilePath);
        } catch (APIMgtEntityImportExportException e) {
            String errorMsg = "Error in reading Swagger definition from file at: " + swaggerDefinitionFilePath;
            throw new APIManagementException(errorMsg, e);
        }
    }

    /*
    private String getGatewayConfigurationFromExtractedArchive (String gatewayConfigFilePath) throws APIManagementException {

        try {
            return importExportUtils.readFileContentAsText(gatewayConfigFilePath);
        } catch (APIMgtEntityImportExportException e) {
            String errorMsg = "Error in reading Gateway configuration from file at: " + gatewayConfigFilePath;
            throw new APIManagementException(errorMsg, e);
        }
    }
    */

    /**
     * Create {@link DocumentContent} instance from the file
     *
     * @param documentInfo {@link DocumentInfo} instance
     * @param documentContentPath path to document content file
     * @return {@link DocumentContent} instance
     */
    private DocumentContent getDocumentContentFromExtractedArchive(DocumentInfo documentInfo,
            String documentContentPath) {

        DocumentContent.Builder documentContentBuilder = new DocumentContent.Builder();
        if (documentInfo.getSourceType().equals(DocumentInfo.SourceType.FILE)) {
            InputStream inputStream = getDocumentContentAsStream(documentContentPath + File.separator +
                    documentInfo.getFileName());
            if (inputStream != null) {
                return documentContentBuilder.fileContent(inputStream).documentInfo(documentInfo).build();
            }
        } else if (documentInfo.getSourceType().equals(DocumentInfo.SourceType.INLINE)) {
            String inlineContent = getDocumentContentAsText(documentContentPath + File.separator +
                    documentInfo.getName());
            if (inlineContent != null) {
                return documentContentBuilder.inlineContent(inlineContent).documentInfo(documentInfo).build();
            }
        }

        return null;
    }

    /**
     * Reads Document content as text
     *
     * @param documentPath path to document content file
     * @return document content as text
     */
    private String getDocumentContentAsText(String documentPath) {

        try {
            return ImportExportUtils.readFileContentAsText(documentPath);
        } catch (APIMgtEntityImportExportException e) {
            log.error("Error in reading document content file at: " + documentPath);
            return null;
        }
    }

    /**
     * Reads Document content as a stream
     *
     * @param documentPath path to document content file
     * @return document content as a stream
     */
    private InputStream getDocumentContentAsStream(String documentPath) {

        try {
            return ImportExportUtils.readFileContentAsStream(documentPath);
        } catch (APIMgtEntityImportExportException e) {
            log.error("Error in reading document content file at: " + documentPath);
            return null;
        }
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
    private String extractUploadedArchive(InputStream uploadedApiArchiveInputStream, String importedDirectoryName,
            String apiArchiveLocation) throws APIManagementException {
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
    private void exportApiDefinitionToFileSystem(API api, String exportLocation)
            throws APIMgtEntityImportExportException {

        APIDTO apidto = MappingUtil.toAPIDto(api);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ImportExportUtils.createFile(exportLocation + File.separator + API_DEFINITION_FILE_NAME);
        ImportExportUtils.writeToFile(exportLocation + File.separator + API_DEFINITION_FILE_NAME, gson.toJson(apidto));

        log.debug("Successfully exported API definition for api: " + api.getName() + ", version: " + api.getVersion());
    }

    // TODO
    /*
    private void exportGatewayConfigToFileSystem(String gatewayConfig, API api, String exportLocation) throws APIManagementException {

        if (gatewayConfig == null) {
            // not gateway config found, return
            log.info("No gateway configuration found for API with api: " + api.getName() + ", version: " + api.getVersion());
            return;
        }

        String gatewayConfigFileLocation = exportLocation + File.separator + GATEWAY_CONFIGURATION_DEFINITION_FILE;
        importExportUtils.createFile(gatewayConfigFileLocation);
        importExportUtils.writeToFile(gatewayConfigFileLocation, gatewayConfig);
    }
    */

    /**
     * Writes the given List of {@link DocumentInfo} objects to the file system
     *
     * @param documentInfo list of {@link DocumentInfo} objects
     * @param apiDetails {@link APIDetails} instance, to which the documents are related to
     * @param exportLocation file system location to which documents will be written
     */
    private void exportDocumentationToFileSystem(Set<DocumentInfo> documentInfo, APIDetails apiDetails,
            String exportLocation) {

        if (documentInfo == null || documentInfo.isEmpty()) {
            log.debug("No documentation found for API with api: " + apiDetails.getApi().getName() + ", " + "version: " +
                    apiDetails.getApi().getVersion());
            return;
        }

        // create Documents root directory
        String documentsBaseDirectory = exportLocation + File.separator + DOCUMENTS_ROOT_DIRECTORY;
        try {
            ImportExportUtils.createDirectory(documentsBaseDirectory);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            for (DocumentInfo aDocumentInfo : documentInfo) {
                // create the root directory for each document
                String apiExportDir = documentsBaseDirectory + File.separator + aDocumentInfo.getId();
                ImportExportUtils.createDirectory(apiExportDir);
                // for each document, write a DocumentInfo to a separate json file
                String apiDocMetaFileLocation = apiExportDir + File.separator + DOCUMENTATION_DEFINITION_FILE;
                ImportExportUtils.createFile(apiDocMetaFileLocation);
                ImportExportUtils.writeToFile(apiDocMetaFileLocation, gson.toJson(aDocumentInfo));

                // if the document's SourceType is FILE, retrieve and write the content to a file
                DocumentContent content;
                if (aDocumentInfo.getSourceType().equals(DocumentInfo.SourceType.FILE)) {
                    content = apiDetails.getDocumentContent(aDocumentInfo.getId());
                    if (content != null) {
                        ImportExportUtils
                                .createFile(apiExportDir + File.separator + content.getDocumentInfo().getFileName());
                        ImportExportUtils.writeStreamToFile(
                                apiExportDir + File.separator + content.getDocumentInfo().getFileName(),
                                content.getFileContent());
                        // modify the document metadata to contain the file name
                        DocumentInfo modifiedDocInfo = new DocumentInfo.Builder(aDocumentInfo)
                                .fileName(content.getDocumentInfo().
                                        getFileName()).build();
                        ImportExportUtils.writeToFile(apiDocMetaFileLocation, gson.toJson(modifiedDocInfo));
                    }

                } else if (aDocumentInfo.getSourceType().equals(DocumentInfo.SourceType.INLINE)) {
                    content = apiDetails.getDocumentContent(aDocumentInfo.getId());
                    if (content != null) {
                        ImportExportUtils
                                .createFile(apiExportDir + File.separator + content.getDocumentInfo().getName());
                        ImportExportUtils
                                .writeToFile(apiExportDir + File.separator + content.getDocumentInfo().getName(),
                                        content.getInlineContent());
                        // modify the document metadata to contain the inline content name
                        DocumentInfo modifiedDocInfo = new DocumentInfo.Builder(aDocumentInfo)
                                .name(content.getDocumentInfo().
                                        getName()).build();
                        ImportExportUtils.writeToFile(apiDocMetaFileLocation, gson.toJson(modifiedDocInfo));
                    }
                }
            }

        } catch (APIMgtEntityImportExportException e) {
            log.error("Error in exporting documents to file system for api: " + apiDetails.getApi().getName() +
                    ", version: " + apiDetails.getApi().getVersion());
            // cleanup
            ImportExportUtils.deleteDirectory(documentsBaseDirectory);
        }

        log.debug("Successfully exported documentation for api: " + apiDetails.getApi().getName() + ", version: " +
                apiDetails.getApi().getVersion());
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
    private void exportSwaggerDefinitionToFileSystem(String swaggerDefinition, API api, String exportLocation)
            throws APIMgtEntityImportExportException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(swaggerDefinition).getAsJsonObject();

        String swaggerFileLocation = exportLocation + File.separator + SWAGGER_DEFINITION_FILE_NAME;
        ImportExportUtils.createFile(swaggerFileLocation);
        ImportExportUtils.writeToFile(swaggerFileLocation, gson.toJson(json));

        log.debug("Successfully exported Swagger definition for api: " + api.getName() + ", version: " + api
                .getVersion());
    }

    /**
     * Writes the API thumbnail to file system
     *
     * @param thumbnailInputStream {@link InputStream} instance with thumbnail data
     * @param api {@link API} instance relevant to thumbnail
     * @param exportLocation file system location to which the thumbnail will be written
     */
    private void exportThumbnailToFileSystem(InputStream thumbnailInputStream, API api, String exportLocation) {

        if (thumbnailInputStream == null) {
            // no thumbnail found, return
            log.debug("No thumbnail found for API with api: " + api.getName() + ", version: " + api.getVersion());
            return;
        }
        String thumbnailFileLocation = exportLocation + File.separator + THUMBNAIL_FILE_NAME;

        try {
            ImportExportUtils.createFile(thumbnailFileLocation);
            ImportExportUtils.writeStreamToFile(thumbnailFileLocation, thumbnailInputStream);

        } catch (APIMgtEntityImportExportException e) {
            log.error("Error in exporting thumbnail to file system for api: " + api.getName() + ", version: " +
                    api.getVersion());
            // cleanup
            ImportExportUtils.deleteFile(thumbnailFileLocation);
        }

        log.debug("Successfully exported Thumbnail for api: " + api.getName() + ", version: " + api.getVersion());
    }

    /**
     * Imports the given API instance to this API Manager
     *
     * @param apiDetails {@link org.wso2.carbon.apimgt.core.models.APIDetails} instance to be imported
     * @return {@link API} instance that was imported
     * @throws APIManagementException if an error occurs while importing the API
     */
    private API importApi(APIDetails apiDetails) throws APIManagementException {

        // if the API already exists, can't import again
        if (apiPublisher.checkIfAPIExists(apiDetails.getApi().getId())) {
            updateAPIDetails(apiDetails);
        } else {
            addAPIDetails(apiDetails);
        }

        log.debug("Successfully imported API definition for: " + apiDetails.getApi().getName() + ", version: " +
                apiDetails.getApi().getVersion());
        return apiPublisher.getAPIbyUUID(apiDetails.getApi().getId());
    }

    /**
     * Retrieves {@link DocumentInfo} instance from the directory containing docs
     *
     * @param documentImportLocation path to the directory containing docs
     * @param apiName API name
     * @param version API version
     * @return Set of {@link DocumentInfo} insjtaces
     */
    private Set<DocumentInfo> getDocumentInfoFromExtractedArchive(String documentImportLocation, String apiName,
            String version) {

        Set<DocumentInfo> documents = new HashSet<>();

        File rootDocumentationDirectoryForAPI = new File(documentImportLocation);
        if (!rootDocumentationDirectoryForAPI.isDirectory()) {
            // no Docs!
            log.debug("No documentation found for API name: " + apiName + ", version: " + version);
            return documents;
        }

        File[] documentationDirectories = rootDocumentationDirectoryForAPI.listFiles(File::isDirectory);
        if (documentationDirectories == null) {
            // do docs!
            log.debug("No documents found at " + documentImportLocation);
            return documents;
        }

        for (File docDir : documentationDirectories) {
            // read the 'doc.json'
            String content;
            try {
                content = ImportExportUtils
                        .readFileContentAsText(docDir.getPath() + File.separator + DOCUMENTATION_DEFINITION_FILE);
                Gson gson = new GsonBuilder().create();
                documents.add(gson.fromJson(content, DocumentInfo.class));
                // add the doc
            } catch (APIManagementException e) {
                // no need to throw, log and continue
                log.error("Error in importing documentation from file: " + docDir.getPath() + " for API: " + apiName +
                        ", version: " + version);
            }
        }

        return documents;
    }

    /**
     * Retrieves thumbnail as a binary stream from the file
     *
     * @param thumbnailFilePath path to file
     * @return thumbnail as a {@link InputStream} instance
     */
    private InputStream getThumbnailFromExtractedArchive(String thumbnailFilePath) {

        File thumbnailFile = new File(thumbnailFilePath);
        if (!thumbnailFile.exists()) {
            // Thumbnail not found
            log.debug("Thumbnail file " + thumbnailFilePath + " does not exist");
            return null;
        }

        try {
            return ImportExportUtils.readFileContentAsStream(thumbnailFilePath);
        } catch (APIManagementException e) {
            // no need to throw, log and continue
            log.error("Error in reading thumbnail from file: " + thumbnailFilePath, e);
        }
        return null;
    }

}
