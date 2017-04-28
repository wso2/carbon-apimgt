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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtEntityImportExportException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIDetails;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manager class for File system based API Import and Export handling
 */
public class FileBasedApiImportExportManager extends ApiImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(FileBasedApiImportExportManager.class);
    private static final String DOCUMENTATION_DEFINITION_FILE = "doc.json";
    private static final String DOCUMENTS_ROOT_DIRECTORY = "Documents";
    private static final String ENDPOINTS_ROOT_DIRECTORY = "Endpoints";
    private static final String IMPORTED_APIS_DIRECTORY_NAME = "imported-apis";
    private String path;

    public FileBasedApiImportExportManager(APIPublisher apiPublisher, String path) {
        super(apiPublisher);
        this.path = path;
    }

    /**
     * Export a given set of APIs to the file system as a zip archive.
     * The export root location is given by {@link FileBasedApiImportExportManager#path}/exported-apis.
     *
     * @param apiDetailSet        Set of {@link APIDetails} objects to be exported
     * @param exportDirectoryName Name of the directory to do the export
     * @return Path to the directory  with exported artifacts
     * @throws APIMgtEntityImportExportException if an error occurred while exporting APIs to file system or
     *                                           no APIs are exported successfully
     */
    public String exportAPIs(Set<APIDetails> apiDetailSet, String exportDirectoryName) throws
            APIMgtEntityImportExportException {

        // this is the base directory for the archive. after export happens, this directory will
        // be archived to be sent as a application/zip response to the client
        String apiArtifactsBaseDirectoryPath = path + File.separator + exportDirectoryName;
        try {
            APIFileUtils.createDirectory(apiArtifactsBaseDirectoryPath);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to create directory for export API at :" + apiArtifactsBaseDirectoryPath;
            throw new APIMgtEntityImportExportException(errorMsg, e);
        }

        for (APIDetails apiDetails : apiDetailSet) {
            // derive the folder structure
            String apiExportDirectory = APIFileUtils.getAPIBaseDirectory(apiArtifactsBaseDirectoryPath, apiDetails
                    .getApi());
            API exportAPI = apiDetails.getApi();
            try {
                // create per-api export directory
                APIFileUtils.createDirectory(apiExportDirectory);

                //export API definition
                APIFileUtils.exportApiDefinitionToFileSystem(exportAPI, apiExportDirectory);

                //export swagger definition
                APIFileUtils.exportSwaggerDefinitionToFileSystem(apiDetails.getSwaggerDefinition(), exportAPI,
                        apiExportDirectory);

                //export gateway configs
                APIFileUtils.exportGatewayConfigToFileSystem(apiDetails.getGatewayConfiguration(), exportAPI,
                        apiExportDirectory);
                exportEndpointsToFileSystem(apiDetails.getEndpoints(), exportAPI, apiExportDirectory);

            } catch (APIMgtDAOException e) {
                // no need to throw, log
                log.error("Error in exporting API: " + exportAPI.getName() + ", version: " + apiDetails
                        .getApi().getVersion(), e);
                // cleanup the API directory
                APIFileUtils.deleteDirectory(apiExportDirectory);
                // skip this API
                continue;
            }

            // export docs and thumbnail - these are non critical; even if they fail the API is considered
            // as exported correctly.
            try {
                APIFileUtils.exportThumbnailToFileSystem(apiDetails.getThumbnailStream(), apiExportDirectory);
            } catch (APIMgtDAOException warn) {
                // log the warning without throwing
                log.warn("Error in exporting thumbnail to file system for api: " + exportAPI.getName() + ", version: " +
                        exportAPI.getVersion());
            }
            exportDocumentationToFileSystem(apiDetails.getAllDocumentInformation(), apiDetails, apiExportDirectory);
            log.info("Successfully exported API: " + exportAPI.getName() + ", version: "
                    + exportAPI.getVersion());
        }

        // if the directory is empty, no APIs have been exported!
        if (ImportExportUtils.getDirectoryList(apiArtifactsBaseDirectoryPath).isEmpty()) {
            // cleanup the archive root directory
            APIFileUtils.deleteDirectory(path);
            String errorMsg = "No APIs exported successfully";
            throw new APIMgtEntityImportExportException(errorMsg, ExceptionCodes.API_EXPORT_ERROR);
        }

        return apiArtifactsBaseDirectoryPath;
    }

    public String createArchiveFromExportedApiArtifacts(String sourceDirectory, String archiveLocation,
            String archiveName) throws APIManagementException {

        try {
            ImportExportUtils.archiveDirectory(sourceDirectory, archiveLocation, archiveName);

        } catch (APIMgtEntityImportExportException e) {
            // cleanup the archive root directory
            APIFileUtils.deleteDirectory(path);
            String errorMsg = "Error while archiving directory " + sourceDirectory;
            throw new APIManagementException(errorMsg, e, ExceptionCodes.API_EXPORT_ERROR);
        }

        return archiveLocation + File.separator + archiveName + ".zip";
    }

    /**
     * Imports and creates a set of new APIs to API Manager by reading and decoding the
     * input stream. Will fail if the APIs already exists
     *
     * @param uploadedApiArchiveInputStream InputStream to be read ana decoded to a set of APIs
     * @param provider                      API provider, if needs to be updated
     * @return {@link APIListDTO} object comprising of successfully imported APIs
     * @throws APIManagementException if any error occurs while importing or no APIs are imported successfully
     */
    public APIListDTO importAndCreateAPIs(InputStream uploadedApiArchiveInputStream, String provider)
            throws APIManagementException {

        String apiArchiveLocation = path + File.separator + IMPORTED_APIS_DIRECTORY_NAME + ".zip";
        String archiveExtractLocation = extractUploadedArchive(uploadedApiArchiveInputStream, IMPORTED_APIS_DIRECTORY_NAME,
                apiArchiveLocation);

        // List to contain newly created/updated APIs
        Set<APIDetails> apiDetailsSet = decodeApiInformationFromDirectoryStructure(archiveExtractLocation, provider);
        List<API> apis = new ArrayList<>();
        for (APIDetails apiDetails : apiDetailsSet) {
            try {
                apis.add(importAndCreateApi(apiDetails));
            } catch (APIManagementException e) {
                log.error("Error while importing API: " + apiDetails.getApi().getName() + ", version: " +
                        apiDetails.getApi().getVersion());
                // skip importing the API
                continue;
            }
            log.info("Successfully imported API: " + apiDetails.getApi().getName() + ", version: " + apiDetails.getApi()
                    .getVersion());
        }

        APIFileUtils.deleteDirectory(path);
        // if no APIs are corrected exported, throw an error
        if (apis.isEmpty()) {
            String errorMsg = "No APIs imported successfully";
            throw new APIManagementException(errorMsg, ExceptionCodes.API_IMPORT_ERROR);
        }

        return MappingUtil.toAPIListDTO(apis);
    }

    /**
     * Imports a set of APIs to API Manager by reading and decoding the input stream
     *
     * @param uploadedApiArchiveInputStream InputStream to be read ana decoded to a set of APIs
     * @param provider                      API provider, if needs to be updated
     * @return {@link APIListDTO} object comprising of successfully imported APIs
     * @throws APIManagementException if any error occurs while importing or no APIs are imported successfully
     */
    public APIListDTO importAPIs(InputStream uploadedApiArchiveInputStream, String provider)
            throws APIManagementException {

        String apiArchiveLocation = path + File.separator + IMPORTED_APIS_DIRECTORY_NAME + ".zip";
        String archiveExtractLocation = extractUploadedArchive(uploadedApiArchiveInputStream, IMPORTED_APIS_DIRECTORY_NAME,
                apiArchiveLocation);

        // List to contain newly created/updated APIs
        Set<APIDetails> apiDetailsSet = decodeApiInformationFromDirectoryStructure(archiveExtractLocation, provider);
        List<API> apis = new ArrayList<>();
        for (APIDetails apiDetails : apiDetailsSet) {
            try {
                apis.add(importApi(apiDetails));
            } catch (APIManagementException e) {
                log.error("Error while importing API: " + apiDetails.getApi().getName() + ", version: " +
                        apiDetails.getApi().getVersion());
                // skip importing the API
                continue;
            }

            log.info("Successfully imported API: " + apiDetails.getApi().getName() + ", version: " + apiDetails.getApi()
                    .getVersion());
        }

        APIFileUtils.deleteDirectory(path);
        // if no APIs are corrected exported, throw an error
        if (apis.isEmpty()) {
            String errorMsg = "No APIs imported successfully";
            throw new APIManagementException(errorMsg, ExceptionCodes.API_IMPORT_ERROR);
        }

        return MappingUtil.toAPIListDTO(apis);
    }

    /**
     * Reads and decodes APIs and relevant information from the given set of paths
     *
     * @param apiArtifactsBasePath path to the directory with API related artifacts
     * @param newApiProvider       API newApiProvider to be updated
     * @return Set of {@link APIDetails} objects
     * @throws APIManagementException if any error occurs while decoding the APIs
     */
    public Set<APIDetails> decodeApiInformationFromDirectoryStructure(String apiArtifactsBasePath,
            String newApiProvider) throws APIManagementException {

        Set<String> apiDefinitionsRootDirectoryPaths = ImportExportUtils.getDirectoryList(apiArtifactsBasePath);
        if (apiDefinitionsRootDirectoryPaths.isEmpty()) {
            APIFileUtils.deleteDirectory(path);
            String errorMsg = "Unable to find API definitions at: " + apiArtifactsBasePath;
            throw new APIManagementException(errorMsg, ExceptionCodes.API_IMPORT_ERROR);
        }

        Set<APIDetails> apiDetailsSet = new HashSet<>();

        for (String apiDefinitionDirectoryPath : apiDefinitionsRootDirectoryPaths) {
            File apiDefinitionFile = getFileFromPrefix(apiDefinitionDirectoryPath, APIMgtConstants
                    .APIFileUtilConstants.API_DEFINITION_FILE_PREFIX);
            File swaggerDefinitionFile = getFileFromPrefix(apiDefinitionDirectoryPath, APIMgtConstants
                    .APIFileUtilConstants.SWAGGER_DEFINITION_FILE_PREFIX);
            API api;
            String swaggerDefinition, gatewayConfiguration;
            Set<Endpoint> endpoints;

            try {
                api = getApiDefinitionFromExtractedArchive(apiDefinitionFile.getPath());
                swaggerDefinition = getSwaggerDefinitionFromExtractedArchive(swaggerDefinitionFile.getPath());
                gatewayConfiguration = getGatewayConfigurationFromExtractedArchive(apiDefinitionDirectoryPath
                        + File.separator + APIMgtConstants.APIFileUtilConstants.GATEWAY_CONFIGURATION_DEFINITION_FILE);
                endpoints = getEndpointsFromExtractedArchive(apiDefinitionDirectoryPath + File.separator +
                        ENDPOINTS_ROOT_DIRECTORY, api.getName(), api.getVersion());

            } catch (APIManagementException e) {
                log.error("Error occurred while importing api from path: " + apiDefinitionDirectoryPath, e);
                // skip this API
                continue;
            }

            if (newApiProvider != null && !newApiProvider.isEmpty()) {
                // update the newApiProvider
                api = new API.APIBuilder(api).provider(newApiProvider).build();
            }

            String documentsRootDirectory = apiDefinitionDirectoryPath + File.separator + DOCUMENTS_ROOT_DIRECTORY;
            Set<DocumentInfo> documentInfoSet = getDocumentInfoFromExtractedArchive(documentsRootDirectory,
                    api.getName(), api.getVersion());
            Set<DocumentContent> documentContents = new HashSet<>();
            for (DocumentInfo aDocumentInfo : documentInfoSet) {
                DocumentContent aDocumentContent = getDocumentContentFromExtractedArchive(aDocumentInfo,
                        documentsRootDirectory + File.separator + aDocumentInfo.getId());
                if (aDocumentContent != null) {
                    documentContents.add(aDocumentContent);
                }
            }

            InputStream thumbnailStream = APIFileUtils.getThumbnailImage(apiDefinitionDirectoryPath +
                    File.separator + APIMgtConstants.APIFileUtilConstants.THUMBNAIL_FILE_NAME);

            APIDetails apiDetails = new APIDetails(api, swaggerDefinition);
            apiDetails.setGatewayConfiguration(gatewayConfiguration);
            apiDetails.setEndpoints(endpoints);
            if (!documentInfoSet.isEmpty()) {
                apiDetails.addDocumentInformation(documentInfoSet);
            }
            if (!documentContents.isEmpty()) {
                apiDetails.addDocumentContents(documentContents);
            }
            if (thumbnailStream != null) {
                apiDetails.setThumbnailStream(thumbnailStream);
            }

            apiDetailsSet.add(apiDetails);
        }

        return apiDetailsSet;
    }

    /**
     * Creates {@link APIDTO} instance from API Definition file
     *
     * @param apiDefinitionFilePath path to api definition file
     * @return {@link APIDTO} instance
     * @throws APIManagementException if an error occurs while creating API definition object
     */
    private API getApiDefinitionFromExtractedArchive(String apiDefinitionFilePath) throws APIManagementException {

        String apiDefinitionString;
        try {
            apiDefinitionString = APIFileUtils.readFileContentAsText(apiDefinitionFilePath);
        } catch (APIMgtDAOException e) {
            // Unable to read the API definition file, skip this API
            String errorMsg = "Error reading API definition from file at: " + apiDefinitionFilePath;
            throw new APIManagementException(errorMsg, e);
        }

        // convert to bean
        Gson gson = new GsonBuilder().create();
        try {
            return gson.fromJson(apiDefinitionString, API.class);
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

    /**
     * Retrieves gateway configuration from extracted archive
     *
     * @param gatewayConfigFilePath path to gateway config file
     * @return gateway configuration
     * @throws APIManagementException if an error occurs while reading the gateway configuration
     */
    private String getGatewayConfigurationFromExtractedArchive(String gatewayConfigFilePath) throws
            APIManagementException {

        try {
            return ImportExportUtils.readFileContentAsText(gatewayConfigFilePath);
        } catch (APIMgtEntityImportExportException e) {
            String errorMsg = "Error in reading Gateway configuration from file at: " + gatewayConfigFilePath;
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Create {@link DocumentContent} instance from the file
     *
     * @param documentInfo        {@link DocumentInfo} instance
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
     * @param importedDirectoryName         directory to extract the archive
     * @param apiArchiveLocation            full path to the location to which the archive will be written
     * @return location to which APIs were extracted
     * @throws APIManagementException if an error occurs while extracting the archive
     */
    private String extractUploadedArchive(InputStream uploadedApiArchiveInputStream, String importedDirectoryName,
            String apiArchiveLocation) throws APIManagementException {
        String archiveExtractLocation;
        try {
            // create api import directory structure
            APIFileUtils.createDirectory(path);
            // create archive
            ImportExportUtils.createArchiveFromInputStream(uploadedApiArchiveInputStream, apiArchiveLocation);
            // extract the archive
            archiveExtractLocation = path + File.separator + importedDirectoryName;
            ImportExportUtils.extractArchive(apiArchiveLocation, archiveExtractLocation);

        } catch (APIMgtEntityImportExportException e) {
            APIFileUtils.deleteDirectory(path);
            String errorMsg = "Error in accessing uploaded API archive";
            throw new APIManagementException(errorMsg, e, ExceptionCodes.API_IMPORT_ERROR);
        }
        return archiveExtractLocation;
    }

    private void exportEndpointsToFileSystem(Set<Endpoint> endpoints, API api, String exportLocation)
            throws APIMgtEntityImportExportException {

        if (endpoints.isEmpty()) {
            // no endpoint, can't continue
            throw new APIMgtEntityImportExportException("No Endpoint information available for API: " + api.getName()
                    + ", version: " + api.getVersion() + " to export");
        }

        String endpointsRootDirectory = exportLocation + File.separator + ENDPOINTS_ROOT_DIRECTORY;
        try {
            APIFileUtils.createDirectory(endpointsRootDirectory);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error while creating directory for endpoint" + endpointsRootDirectory;
            log.error(errorMsg, e);
            throw new APIMgtEntityImportExportException(errorMsg, e);
        }
        for (Endpoint endpoint : endpoints) {
            try {
                APIFileUtils.exportEndpointToFileSystem(endpoint, endpointsRootDirectory);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully exported endpoint " + endpoint.getId() + " for API: " + api.getName() + ","
                            + " version: " + api.getVersion());
                }
            } catch (APIMgtDAOException e) {
                String errorMsg = "Error while saving endpoint " + endpoint.getName();
                log.error(errorMsg, e);
                throw new APIMgtEntityImportExportException(errorMsg, e);
            }
        }
    }


    /**
     * Writes the given List of {@link DocumentInfo} objects to the file system
     *
     * @param documentInfo   list of {@link DocumentInfo} objects
     * @param apiDetails     {@link APIDetails} instance, to which the documents are related to
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
            APIFileUtils.createDirectory(documentsBaseDirectory);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            for (DocumentInfo aDocumentInfo : documentInfo) {
                // create the root directory for each document
                String apiExportDir = documentsBaseDirectory + File.separator + aDocumentInfo.getId();
                APIFileUtils.createDirectory(apiExportDir);
                // for each document, write a DocumentInfo to a separate json file
                String apiDocMetaFileLocation = apiExportDir + File.separator + DOCUMENTATION_DEFINITION_FILE;
                APIFileUtils.createFile(apiDocMetaFileLocation);
                APIFileUtils.writeToFile(apiDocMetaFileLocation, gson.toJson(aDocumentInfo));

                // if the document's SourceType is FILE, retrieve and write the content to a file
                DocumentContent content;
                if (aDocumentInfo.getSourceType().equals(DocumentInfo.SourceType.FILE)) {
                    content = apiDetails.getDocumentContent(aDocumentInfo.getId());
                    if (content != null) {
                        APIFileUtils
                                .createFile(apiExportDir + File.separator + content.getDocumentInfo().getFileName());
                        ImportExportUtils.writeStreamToFile(
                                apiExportDir + File.separator + content.getDocumentInfo().getFileName(),
                                content.getFileContent());
                        // modify the document metadata to contain the file name
                        DocumentInfo modifiedDocInfo = new DocumentInfo.Builder(aDocumentInfo)
                                .fileName(content.getDocumentInfo().
                                        getFileName()).build();
                        APIFileUtils.writeToFile(apiDocMetaFileLocation, gson.toJson(modifiedDocInfo));
                    }

                } else if (aDocumentInfo.getSourceType().equals(DocumentInfo.SourceType.INLINE)) {
                    content = apiDetails.getDocumentContent(aDocumentInfo.getId());
                    if (content != null) {
                        APIFileUtils
                                .createFile(apiExportDir + File.separator + content.getDocumentInfo().getName());
                        APIFileUtils
                                .writeToFile(apiExportDir + File.separator + content.getDocumentInfo().getName(),
                                        content.getInlineContent());
                        // modify the document metadata to contain the inline content name
                        DocumentInfo modifiedDocInfo = new DocumentInfo.Builder(aDocumentInfo)
                                .name(content.getDocumentInfo().
                                        getName()).build();
                        APIFileUtils.writeToFile(apiDocMetaFileLocation, gson.toJson(modifiedDocInfo));
                    }
                }
            }

        } catch (APIMgtEntityImportExportException | APIMgtDAOException e) {
            log.error("Error in exporting documents to file system for api: " + apiDetails.getApi().getName() +
                    ", version: " + apiDetails.getApi().getVersion());
            // cleanup
            APIFileUtils.deleteDirectory(documentsBaseDirectory);
        }

        if (log.isDebugEnabled()) {
            log.debug("Successfully exported documentation for api: " + apiDetails.getApi().getName() + ", version: " +
                    apiDetails.getApi().getVersion());
        }
    }


    /**
     * Imports the given API instance to this API Manager - will create if not exists and update if the api is
     * already there
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

        if (log.isDebugEnabled()) {
            log.debug("Successfully imported API definition for: " + apiDetails.getApi().getName() + ", version: " +
                    apiDetails.getApi().getVersion());
        }
        return apiPublisher.getAPIbyUUID(apiDetails.getApi().getId());
    }

    /**
     * Imports the given API instance to this API Manager. Operation will fail if the API already exists.
     *
     * @param apiDetails {@link org.wso2.carbon.apimgt.core.models.APIDetails} instance to be imported
     * @return {@link API} instance that was imported
     * @throws APIManagementException if an error occurs while importing the API
     */
    private API importAndCreateApi(APIDetails apiDetails) throws APIManagementException {
        addAPIDetails(apiDetails);

        if (log.isDebugEnabled()) {
            log.debug("Successfully imported API definition for: " + apiDetails.getApi().getName() + ", version: " +
                    apiDetails.getApi().getVersion());
        }
        return apiPublisher.getAPIbyUUID(apiDetails.getApi().getId());
    }

    private Set<Endpoint> getEndpointsFromExtractedArchive(String endpointLocation, String apiName, String version)
            throws APIManagementException {
        File endpointsRootDirectory = new File(endpointLocation);
        if (!endpointsRootDirectory.isDirectory()) {
            // no Endpoints, can't continue
            String errorMsg = "Endpoints root directory " + endpointLocation + " not found for API name: " + apiName
                    + ", version: " + version;
            throw new APIManagementException(errorMsg);
        }

        File[] endpointFiles = endpointsRootDirectory.listFiles(File::isFile);
        if (endpointFiles == null) {
            // no endpoints in the given location, can't continue
            String errorMsg = "No endpoints found at " + endpointsRootDirectory;
            throw new APIManagementException(errorMsg);
        }

        Gson gson = new GsonBuilder().create();
        Set<Endpoint> endpoints = new HashSet<>();
        for (File endpointFile : endpointFiles) {
            // read everything
            String content = ImportExportUtils.readFileContentAsText(endpointFile.getPath());
            endpoints.add(gson.fromJson(content, Endpoint.class));
        }

        return endpoints;
    }

    /**
     * Retrieves {@link DocumentInfo} instance from the directory containing docs
     *
     * @param documentImportLocation path to the directory containing docs
     * @param apiName                API name
     * @param version                API version
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
                content = APIFileUtils
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
     * Return the file with given prefix
     *
     * @param apiDirectoryPath Path to find the file
     * @param prefix
     * @return File with given prefix
     * @throws APIManagementException if file not found or more than one file is present with given prefix
     */
    private File getFileFromPrefix(String apiDirectoryPath, String prefix) throws APIManagementException {
        File dir = new File(apiDirectoryPath);
        File[] files = dir.listFiles((d, name) ->
                name.startsWith(prefix));
        if (files == null) {
            String errorMsg = "Unable find file with prefix: " + prefix + " at path: " + apiDirectoryPath;
            log.error(errorMsg);
            throw new APIManagementException(errorMsg);
        }
        if (files.length != 1) {
            String errorMsg = "More than one file with prefix: " + prefix + " found at path: " + apiDirectoryPath;
            log.error(errorMsg);
            throw new APIManagementException(errorMsg);
        }
        return files[0];
    }
}
