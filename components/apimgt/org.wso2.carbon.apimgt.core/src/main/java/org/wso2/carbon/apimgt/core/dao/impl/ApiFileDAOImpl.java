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
package org.wso2.carbon.apimgt.core.dao.impl;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * File based implementation of the ApiDAO interface.
 */
public class ApiFileDAOImpl implements ApiDAO {

    private static final Logger log = LoggerFactory.getLogger(ApiFileDAOImpl.class);
    private String storagePath;
    private static final String API_DEFINITION_FILE_NAME = "api-";
    private static final String JSON_EXTENSION = ".json";
    private static final String DOCUMENTATION_DEFINITION_FILE = "doc.json";
    private static final String SWAGGER_DEFINITION_FILE_NAME = "swagger-";
    private static final String THUMBNAIL_FILE_NAME = "thumbnail";
    private static final String GATEWAY_CONFIGURATION_DEFINITION_FILE = "gateway-configuration";
    private static final String DOCUMENTS_ROOT_DIRECTORY = "Documents";
    private static final String ENDPOINTS_ROOT_DIRECTORY = "Endpoints";
    private static final String IMPORTED_APIS_DIRECTORY_NAME = "imported-apis";

    public ApiFileDAOImpl(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * @see ApiDAO#addAPI(API api)
     */
    @Override
    public void addAPI(API api) throws APIMgtDAOException {
        String apiExportDirectory = storagePath + File.separator +
                api.getProvider() + "-" + api.getName() + "-" + api.getVersion();
        APIFileUtils.createDirectory(apiExportDirectory);
        exportApiDefinitionToFileSystem(api, apiExportDirectory);
        APIFileUtils.createDirectory(apiExportDirectory + File.separator + ENDPOINTS_ROOT_DIRECTORY);
        // Export endpoints to file system.
        api.getEndpoint().forEach((key, value) -> {
            try {
                exportEndpointToFileSystem(getEndpoint(value),
                        apiExportDirectory + File.separator + ENDPOINTS_ROOT_DIRECTORY);
            } catch (APIMgtDAOException e) {
                throw new RuntimeException("Error while saving endpoint with id : " + value + " to file system", e);
            }
        });
        //Export gateway config to file system
        exportGatewayConfigToFileSystem(api.getGatewayConfig(), api, apiExportDirectory);
        //Export swagger definition to file system.
        exportSwaggerDefinitionToFileSystem(api.getApiDefinition(), api, apiExportDirectory);
    }

    /**
     * @see ApiDAO#updateAPI(String apiID, API substituteAPI)
     */
    @Override
    public void updateAPI(String apiID, API substituteAPI) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#deleteAPI(String apiID)
     */
    @Override
    public void deleteAPI(String apiID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getSwaggerDefinition(String apiID)
     */
    @Override
    public String getSwaggerDefinition(String apiID) throws APIMgtDAOException {
        String swaggerFileName = SWAGGER_DEFINITION_FILE_NAME + apiID + JSON_EXTENSION;
        String swaggerFilepath = APIFileUtils.findInFileSystem(new File(storagePath), swaggerFileName);
        if (swaggerFilepath != null) {
            return APIFileUtils.readFileContentAsText(swaggerFilepath);
        }
        return null;
    }

    /**
     * @see ApiDAO#getImage(String apiID)
     */
    @Override
    public InputStream getImage(String apiID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#updateImage(String apiID, InputStream image, String dataType, String updatedBy)
     */
    @Override
    public void updateImage(String apiID, InputStream image, String dataType, String updatedBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#changeLifeCycleStatus(String apiID, String status)
     */
    @Override
    public void changeLifeCycleStatus(String apiID, String status) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getDocumentsInfoList(String apiID)
     */
    @Override
    public List<DocumentInfo> getDocumentsInfoList(String apiID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getDocumentInfo(String resourceID)
     */
    @Override
    public DocumentInfo getDocumentInfo(String resourceID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getDocumentFileContent(String resourceID)
     */
    @Override
    public InputStream getDocumentFileContent(String resourceID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getDocumentInlineContent(String resourceID)
     */
    @Override
    public String getDocumentInlineContent(String resourceID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#addDocumentInfo(String apiId, DocumentInfo documentInfo)
     */
    @Override
    public void addDocumentInfo(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#updateDocumentInfo(String apiId, DocumentInfo documentInfo, String updatedBy)
     */
    @Override
    public void updateDocumentInfo(String apiId, DocumentInfo documentInfo, String updatedBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#addDocumentFileContent(String resourceID, InputStream content, String fileName,
     *      String updatedBy)
     */
    @Override
    public void addDocumentFileContent(String resourceID, InputStream content, String fileName,
            String updatedBy) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#addDocumentInlineContent(String resourceID, String content, String updatedBy)
     */
    @Override
    public void addDocumentInlineContent(String resourceID, String content, String updatedBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#deleteDocument(String resourceID)
     */
    @Override
    public void deleteDocument(String resourceID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#deprecateOlderVersions(String identifier)
     */
    @Override
    public void deprecateOlderVersions(String identifier) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#isDocumentExist(String apiId, DocumentInfo documentInfo)
     */
    @Override
    public boolean isDocumentExist(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#addEndpoint(Endpoint endpoint)
     */
    @Override
    public void addEndpoint(Endpoint endpoint) throws APIMgtDAOException {
        String endpointExportDirectory = storagePath + File.separator + ENDPOINTS_ROOT_DIRECTORY;
        APIFileUtils.createDirectory(endpointExportDirectory);
        exportEndpointToFileSystem(endpoint, endpointExportDirectory);
    }

    /**
     * @see ApiDAO#deleteEndpoint(String endpointId)
     */
    @Override
    public boolean deleteEndpoint(String endpointId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#updateEndpoint(Endpoint endpoint)
     */
    @Override
    public boolean updateEndpoint(Endpoint endpoint) throws APIMgtDAOException {
        String endpointExportDirectory = storagePath + File.separator + ENDPOINTS_ROOT_DIRECTORY;
        exportEndpointToFileSystem(endpoint, endpointExportDirectory);
        return true;
    }

    /**
     * @see ApiDAO#getEndpoint(String endpointId)
     */
    @Override
    public Endpoint getEndpoint(String endpointId) throws APIMgtDAOException {
        String endpointPartialName = endpointId + JSON_EXTENSION;
        String endpointFilePath = APIFileUtils
                .findInFileSystem(new File(storagePath + File.separator + ENDPOINTS_ROOT_DIRECTORY),
                        endpointPartialName);
        if (endpointFilePath != null) {
            return (Endpoint) constructObjectSummaryFromFile(endpointFilePath, Endpoint.class);
        }
        return null;
    }

    /**
     * @see ApiDAO#getEndpointByName(String name)
     */
    @Override
    public Endpoint getEndpointByName(String name) throws APIMgtDAOException {
        String endpointFilePath = APIFileUtils.findInFileSystem(new File(storagePath), name);
        if (endpointFilePath != null) {
            return (Endpoint) constructObjectSummaryFromFile(endpointFilePath, Endpoint.class);
        }
        return null;
    }

    /**
     * @see ApiDAO#getEndpoints()
     */
    @Override
    public List<Endpoint> getEndpoints() throws APIMgtDAOException {
        File[] files = new File(storagePath + File.separator + ENDPOINTS_ROOT_DIRECTORY).listFiles();
        List<Endpoint> endpointList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                endpointList.add((Endpoint) fetchObject(file, Endpoint.class, null));
            }
        }
        endpointList.removeIf(Objects::isNull);
        return endpointList;
    }

    /**
     * @see ApiDAO#updateSwaggerDefinition(String apiID, String swaggerDefinition, String updatedBy)
     */
    @Override
    public void updateSwaggerDefinition(String apiID, String swaggerDefinition, String updatedBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getGatewayConfig(String apiID)
     */
    @Override
    public String getGatewayConfig(String apiID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#updateGatewayConfig(String apiID, String gatewayConfig, String updatedBy)
     */
    @Override
    public void updateGatewayConfig(String apiID, String gatewayConfig, String updatedBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfDocument(String documentId)
     */
    @Override
    public String getLastUpdatedTimeOfDocument(String documentId) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfDocumentContent(String apiId, String documentId)
     */
    @Override
    public String getLastUpdatedTimeOfDocumentContent(String apiId, String documentId)
            throws APIMgtDAOException {
        return null;
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfAPIThumbnailImage(String apiId)
     */
    @Override
    public String getLastUpdatedTimeOfAPIThumbnailImage(String apiId) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfEndpoint(String endpointId)
     */
    @Override
    public String getLastUpdatedTimeOfEndpoint(String endpointId) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see ApiDAO#updateAPIWorkflowStatus(String apiID, APIMgtConstants.APILCWorkflowStatus workflowStatus)
     */
    @Override
    public void updateAPIWorkflowStatus(String apiID, APIMgtConstants.APILCWorkflowStatus workflowStatus)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getCommentByUUID(String commentId, String apiId)
     */
    @Override
    public Comment getCommentByUUID(String commentId, String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getAPIs()
     */
    @Override
    public List<API> getAPIs() throws APIMgtDAOException {

        File[] files = new File(storagePath).listFiles();
        List<API> apiList = new ArrayList<>();
        final FilenameFilter filenameFilter = (dir, name) -> (name.endsWith(JSON_EXTENSION) && name
                .contains(API_DEFINITION_FILE_NAME));
        if (files != null) {
            for (File file : files) {
                apiList.add((API) fetchObject(file, API.class, filenameFilter));
            }
        }
        apiList.removeIf(Objects::isNull);
        return apiList;
    }

    /**
     * @see ApiDAO#getAPIsForProvider(String providerName)
     */
    @Override
    public List<API> getAPIsForProvider(String providerName) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getAPIsByStatus(List)
     */
    @Override
    public List<API> getAPIsByStatus(List<String> statuses) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getAPIsByStatus(List , List)
     */
    @Override
    public List<API> getAPIsByStatus(List<String> roles, List<String> statuses) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#searchAPIs(List roles, String user, String searchString, int offset, int limit)
     */
    @Override
    public List<API> searchAPIs(List<String> roles, String user, String searchString, int offset, int limit)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#attributeSearchAPIs(List roles, String user, Map attributeMap, int offset, int limit)
     */
    @Override
    public List<API> attributeSearchAPIs(List<String> roles, String user, Map<String, String> attributeMap,
            int offset, int limit) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#searchAPIsByStatus(String searchString, List statuses)
     */
    @Override
    public List<API> searchAPIsByStatus(String searchString, List<String> statuses)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#isAPINameExists(String apiName, String providerName)
     */
    @Override
    public
    boolean isAPINameExists(String apiName, String providerName) throws APIMgtDAOException {
        return false;
    }

    /**
     * @see ApiDAO#isAPIContextExists(String contextName)
     */
    @Override
    public boolean isAPIContextExists(String contextName) throws APIMgtDAOException {
        return false;
    }

    /**
     * @see ApiDAO#getAPI(String apiID)
     */
    @Override
    public API getAPI(String apiID) throws APIMgtDAOException {
        String apiFileName = API_DEFINITION_FILE_NAME + apiID + JSON_EXTENSION;
        String apiFilePath = APIFileUtils.findInFileSystem(new File(storagePath), apiFileName);
        if (apiFilePath != null) {
            return (API) constructObjectSummaryFromFile(apiFilePath, API.class);
        }
        return null;
    }

    /**
     * @see ApiDAO#getAPISummary(String apiID)
     */
    @Override
    public API getAPISummary(String apiID) throws APIMgtDAOException {
        String apiFileName = API_DEFINITION_FILE_NAME + apiID + JSON_EXTENSION;
        String apiFilePath = APIFileUtils.findInFileSystem(new File(storagePath), apiFileName);
        if (apiFilePath != null) {
            return (API) constructObjectSummaryFromFile(apiFilePath, API.class);
        }
        return null;
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfAPI(String apiId)
     */
    @Override
    public String getLastUpdatedTimeOfAPI(String apiId) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfSwaggerDefinition(String apiId)
     */
    @Override
    public String getLastUpdatedTimeOfSwaggerDefinition(String apiId) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfGatewayConfig(String apiId)
     */
    @Override
    public String getLastUpdatedTimeOfGatewayConfig(String apiId) throws APIMgtDAOException {
        return null;
    }

    /**
     * write the given API definition to file system
     *
     * @param api {@link API} object to be exported
     * @param exportLocation file system location to write the API definition
     * @throws APIMgtDAOException if an error occurs while writing the API definition
     */
    private void exportApiDefinitionToFileSystem(API api, String exportLocation) throws APIMgtDAOException {

        String apiFileLocation =
                exportLocation + File.separator + API_DEFINITION_FILE_NAME + api.getId() + JSON_EXTENSION;
        APIFileUtils.writeObjectAsJsonToFile(api, apiFileLocation);

        if (log.isDebugEnabled()) {
            log.debug("Successfully saved API definition for api: " + api.getName() + ", version: " + api
                    .getVersion());
        }
    }

    /**
     * write the given Endpoint definition to file system
     *
     * @param endpoint {@link Endpoint} object to be exported
     * @param exportLocation file system location to write the Endpoint
     * @throws APIMgtDAOException if an error occurs while writing the Endpoint
     */
    private void exportEndpointToFileSystem (Endpoint endpoint, String exportLocation) throws APIMgtDAOException {
        String endpointFileLocation =
                exportLocation + File.separator  + endpoint.getName() + "-" + endpoint.getId() + JSON_EXTENSION;
        APIFileUtils.writeObjectAsJsonToFile(endpoint, endpointFileLocation);

        if (log.isDebugEnabled()) {
            log.debug("Successfully saved endpoint  definition for endpoint: " + endpoint.getName());
        }
    }

    /**
     * write the given API gateway config to file system
     *
     * @param config gateway config of the api
     * @param api {@link API} instance
     * @param exportLocation file system location to write the API gateway config.
     * @throws APIMgtDAOException if an error occurs while writing the API definition
     */
    private void exportGatewayConfigToFileSystem(String config, API api, String exportLocation)
            throws APIMgtDAOException {

        if (config == null) {
            // not gateway config found, return
            log.warn("No gateway configuration found for API with api: " + api.getName() + ", version: " + api
                    .getVersion());
            return;
        }

        String gatewayConfigLocation = exportLocation + File.separator + GATEWAY_CONFIGURATION_DEFINITION_FILE;
        APIFileUtils.createFile(gatewayConfigLocation);
        APIFileUtils.writeToFile(gatewayConfigLocation, config);
        if (log.isDebugEnabled()) {
            log.debug("Successfully exported gateway configuration for api: " + api.getName() + ", version: " + api
                    .getVersion());
        }
    }

    /**
     * write the given Endpoint definition to file system
     *
     * @param swaggerDefinition swagger definition
     * @param api {@link API} instance relevant to the swagger definition
     * @param exportLocation file system location to which the swagger definition will be written
     * @throws APIMgtDAOException if an error occurs while writing the Endpoint
     */
    private void exportSwaggerDefinitionToFileSystem(String swaggerDefinition, API api, String exportLocation)
            throws APIMgtDAOException {
        String swaggerDefinitionLocation =
                exportLocation + File.separator + SWAGGER_DEFINITION_FILE_NAME + api.getId() + JSON_EXTENSION;
        APIFileUtils.createFile(swaggerDefinitionLocation);
        APIFileUtils.writeStringAsJsonToFile(swaggerDefinition, swaggerDefinitionLocation);

        if (log.isDebugEnabled()) {
            log.debug("Successfully exported Swagger definition for api: " + api.getName() + ", version: " + api
                    .getVersion());
        }
    }

    private Object fetchObject(File file, Class c, FilenameFilter filenameFilter) {
        File[] files = (filenameFilter != null) ? file.listFiles(filenameFilter) : file.listFiles();
        if (files != null && files.length > 0) {
            return constructObjectSummaryFromFile(files[0].getAbsolutePath(), c);
        } else if (!file.isDirectory()) {
            return constructObjectSummaryFromFile(file.getAbsolutePath(), c);
        }
        return null;
    }

    private Object constructObjectSummaryFromFile(String filePath, Class c) {
        Gson gson = new Gson();
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)) {
            JsonReader reader = new JsonReader(inputStreamReader);
            return gson.fromJson(reader, c);
        } catch (IOException e) {
            log.error("Error while reading object in the path " + filePath);
        }
        return null;
    }

}
