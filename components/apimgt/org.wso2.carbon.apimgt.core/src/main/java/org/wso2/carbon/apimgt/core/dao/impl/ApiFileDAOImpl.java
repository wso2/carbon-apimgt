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
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.FileApi;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
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
import java.util.Set;

/**
 * File based implementation of the ApiDAO interface.
 */
public class ApiFileDAOImpl implements ApiDAO {

    private static final Logger log = LoggerFactory.getLogger(ApiFileDAOImpl.class);
    private String storagePath;

    public ApiFileDAOImpl(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * @see ApiDAO#addAPI(API api)
     */
    @Override
    public void addAPI(API api) throws APIMgtDAOException {
        //Save API definition
        FileApi fileApi = new FileApi(api);
        String apiExportDirectory = APIFileUtils.getAPIBaseDirectory(storagePath, fileApi);
        APIFileUtils.createDirectory(apiExportDirectory);
        APIFileUtils.exportApiDefinitionToFileSystem(fileApi, apiExportDirectory);

        //Export gateway config to file system
        APIFileUtils.exportGatewayConfigToFileSystem(api.getGatewayConfig(), api, apiExportDirectory);

        //Export swagger definition to file system.
        APIFileUtils.exportSwaggerDefinitionToFileSystem(api.getApiDefinition(), api, apiExportDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addApplicationAssociatedAPI(CompositeAPI api) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#updateAPI(String apiID, API substituteAPI)
     */
    @Override
    public void updateAPI(String apiID, API substituteAPI) throws APIMgtDAOException {
        API oldAPI = getAPI(apiID);
        if (oldAPI == null) {
            String errorMsg = "Error while updating API. Unable to find API with Id: " + apiID;
            log.error(errorMsg);
            throw new APIMgtDAOException(errorMsg, ExceptionCodes.API_NOT_FOUND);
        }

        // set immutable properties from old API
        API updatedAPI = new API.APIBuilder(substituteAPI).
                id(apiID).
                provider(oldAPI.getProvider()).
                name(oldAPI.getName()).
                version(oldAPI.getVersion()).
                context(oldAPI.getContext()).
                createdTime(oldAPI.getCreatedTime()).
                createdBy(oldAPI.getCreatedBy()).
                lifecycleInstanceId(oldAPI.getLifecycleInstanceId()).
                lifeCycleStatus(oldAPI.getLifeCycleStatus()).
                copiedFromApiId(oldAPI.getCopiedFromApiId()).build();

        // Adding the API override the existing files.
        addAPI(updatedAPI);
    }

    /**
     * @see ApiDAO#deleteAPI(String apiID)
     */
    @Override
    public void deleteAPI(String apiID) throws APIMgtDAOException {
        API api = getAPI(apiID);
        if (api == null) {
            String errorMsg = "API with Id " + apiID + " not found";
            log.error(errorMsg);
            throw new APIMgtDAOException(errorMsg, ExceptionCodes.API_NOT_FOUND);
        }
        APIFileUtils.deleteDirectory(APIFileUtils.getAPIBaseDirectory(storagePath, new FileApi(api)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCompositeApi(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getApiSwaggerDefinition(String apiID)
     */
    @Override
    public String getApiSwaggerDefinition(String apiID) throws APIMgtDAOException {
        String swaggerFileName = APIMgtConstants.APIFileUtilConstants.SWAGGER_DEFINITION_FILE_PREFIX + apiID +
                APIMgtConstants.APIFileUtilConstants.JSON_EXTENSION;
        String swaggerFilepath = APIFileUtils.findInFileSystem(new File(storagePath), swaggerFileName);
        if (swaggerFilepath != null) {
            return APIFileUtils.readFileContentAsText(swaggerFilepath);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCompositeApiSwaggerDefinition(String apiID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getImage(String apiID)
     */
    @Override
    public InputStream getImage(String apiID) throws APIMgtDAOException {
        API api = getAPI(apiID);
        if (api == null) {
            String errorMsg = "Unable to find API with Id: " + apiID;
            log.error(errorMsg);
            throw new APIMgtDAOException(errorMsg, ExceptionCodes.API_NOT_FOUND);
        }
        String thumbnailPath = APIFileUtils.getAPIBaseDirectory(storagePath, new FileApi(api)) + File.separator +
                APIMgtConstants
                .APIFileUtilConstants.THUMBNAIL_FILE_NAME;
        return APIFileUtils.getThumbnailImage(thumbnailPath);
    }

    /**
     * @see ApiDAO#updateImage(String apiID, InputStream image, String dataType, String updatedBy)
     */
    @Override
    public void updateImage(String apiID, InputStream image, String dataType, String updatedBy)
            throws APIMgtDAOException {
        API api = getAPI(apiID);
        if (api != null) {
            APIFileUtils.exportThumbnailToFileSystem(image, APIFileUtils.getAPIBaseDirectory(storagePath, new FileApi
                    (api)));
        }
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
     * String updatedBy)
     */
    @Override
    public void addDocumentFileContent(String resourceID, InputStream content, String dataType,
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
        // global endpoints are not supported in editor mode
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#deleteEndpoint(String endpointId)
     */
    @Override
    public boolean deleteEndpoint(String endpointId) throws APIMgtDAOException {
        // global endpoints are not supported in editor mode
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#updateEndpoint(Endpoint endpoint)
     */
    @Override
    public boolean updateEndpoint(Endpoint endpoint) throws APIMgtDAOException {
        // global endpoints are not supported in editor mode
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getEndpoint(String endpointId)
     */
    @Override
    public Endpoint getEndpoint(String endpointId) throws APIMgtDAOException {
        // global endpoints are not supported in editor mode
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getEndpointByName(String name)
     */
    @Override
    public Endpoint getEndpointByName(String name) throws APIMgtDAOException {
        // global endpoints are not supported in editor mode
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getEndpoints()
     */
    @Override
    public List<Endpoint> getEndpoints() throws APIMgtDAOException {
        // global endpoints are not supported in editor mode
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#updateApiDefinition(String apiID, String swaggerDefinition, String updatedBy)
     */
    @Override
    public void updateApiDefinition(String apiID, String swaggerDefinition, String updatedBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWSDLArchiveExists(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWSDLExists(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getWSDL(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getWSDLArchive(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeWSDL(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override public void addOrUpdateWSDL(String apiId, byte[] wsdlContent, String createdBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addOrUpdateWSDLArchive(String apiID, InputStream inputStream, String updatedBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeWSDLArchiveOfAPI(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getGatewayConfigOfAPI(String apiID)
     */
    @Override
    public String getGatewayConfigOfAPI(String apiID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getCompositeAPIGatewayConfig(String apiID) throws APIMgtDAOException {
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
     * {@inheritDoc}
     */
    @Override
    public void updateCompositeAPIGatewayConfig(String apiID, InputStream gatewayConfig, String updatedBy)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfDocument(String documentId)
     */
    @Override
    public String getLastUpdatedTimeOfDocument(String documentId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfDocumentContent(String apiId, String documentId)
     */
    @Override
    public String getLastUpdatedTimeOfDocumentContent(String apiId, String documentId)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfAPIThumbnailImage(String apiId)
     */
    @Override
    public String getLastUpdatedTimeOfAPIThumbnailImage(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfEndpoint(String endpointId)
     */
    @Override
    public String getLastUpdatedTimeOfEndpoint(String endpointId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
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
     * Check Endpoint is exist
     *
     * @param name name of endpoint
     * @return existence of endpoint
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public boolean isEndpointExist(String name) throws APIMgtDAOException {
        // global endpoints are not supported in editor mode
        throw new UnsupportedOperationException();
    }

    /**
     * Check endpoint use in api or operation
     *
     * @param endpointId id of endpoint
     * @return true if used
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public boolean isEndpointAssociated(String endpointId) throws APIMgtDAOException {
        return false;
    }

    /**
     * @see ApiDAO#getAPIsByStatus(List, String)
     */
    @Override
    public List<API> getAPIsByStatus(List<String> gatewayLabels, String status) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getAPIsByGatewayLabel(List)
     */
    @Override
    public List<API> getAPIsByGatewayLabel(List<String> gatewayLabels) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#addComment(Comment, String)
     */
    @Override
    public void addComment(Comment comment, String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#deleteComment(String, String)
     */
    @Override
    public void deleteComment(String commentId, String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#updateComment(Comment, String, String)
     */
    @Override
    public void updateComment(Comment comment, String commentId, String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getCommentsForApi(String)
     */
    @Override
    public List<Comment> getCommentsForApi(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLastUpdatedTimeOfComment(String commentId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addRating(String apiId, Rating rating) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rating getRatingByUUID(String apiId, String ratingId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rating getUserRatingForApiFromUser(String apiId, String userId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRating(String apiId, String ratingId, Rating rating) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Rating> getRatingsListForApi(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getUUIDsOfGlobalEndpoints() throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEndpointConfig(String endpointId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getAverageRating(String apiId) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<UriTemplate> getResourcesOfApi(String apiContext, String apiVersion) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#getAPIs(Set, String)
     */
    @Override
    public List<API> getAPIs(Set<String> roles, String user) throws APIMgtDAOException {

        File[] files = new File(storagePath).listFiles();
        List<API> apiList = new ArrayList<>();
        final FilenameFilter filenameFilter = (dir, name) ->
                (name.endsWith(APIMgtConstants.APIFileUtilConstants.JSON_EXTENSION) &&
                        name.contains(APIMgtConstants.APIFileUtilConstants.API_DEFINITION_FILE_PREFIX) &&
                        !dir.isHidden());
        if (files != null) {
            for (File file : files) {
                apiList.add((API) fetchObject(file, FileApi.class, filenameFilter));
            }
        }
        apiList.removeIf(Objects::isNull);
        return apiList;
    }

    @Override
    public List<CompositeAPI> getCompositeAPIs(Set<String> roles, String user, int offset, int limit) {
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
     * @see ApiDAO#getAPIsByStatus(Set, List)
     */
    @Override
    public List<API> getAPIsByStatus(Set<String> roles, List<String> statuses)
            throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#searchAPIs(Set, String, String, int, int)
     */
    @Override
    public List<API> searchAPIs(Set<String> roles, String user, String searchString,
                                int offset, int limit) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CompositeAPI> searchCompositeAPIs(Set<String> roles, String user, String searchString, int offset,
                                                  int limit) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#attributeSearchAPIs(Set, String, Map, int, int)
     */
    @Override
    public List<API> attributeSearchAPIs(Set<String> roles, String user, Map<String, String> attributeMap, int offset,
                                         int limit) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#searchAPIsByAttributeInStore(List roles, Map attributeMap, int offset, int limit)
     */
    @Override
    public List<API> searchAPIsByAttributeInStore(List<String> roles, Map<String, String> attributeMap, int offset,
                                                  int limit) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see ApiDAO#isAPINameExists(String, String)
     */
    @Override
    public boolean isAPINameExists(String apiName, String providerName) throws APIMgtDAOException {
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
        String apiFileName = APIMgtConstants.APIFileUtilConstants.API_DEFINITION_FILE_PREFIX + apiID + APIMgtConstants
                .APIFileUtilConstants.JSON_EXTENSION;
        String apiFilePath = APIFileUtils.findInFileSystem(new File(storagePath), apiFileName);
        if (apiFilePath != null) {
            return new API.APIBuilder((FileApi) constructObjectSummaryFromFile(apiFilePath, FileApi.class)).build();
        }
        return null;
    }

    /**
     * @see ApiDAO#getAPISummary(String apiID)
     */
    @Override
    public API getAPISummary(String apiID) throws APIMgtDAOException {
        String apiFileName = APIMgtConstants.APIFileUtilConstants.API_DEFINITION_FILE_PREFIX + apiID + APIMgtConstants
                .APIFileUtilConstants.JSON_EXTENSION;
        String apiFilePath = APIFileUtils.findInFileSystem(new File(storagePath), apiFileName);
        if (apiFilePath != null) {
            return new API.APIBuilder((FileApi) constructObjectSummaryFromFile(apiFilePath, FileApi.class)).build();
        }
        return null;
    }

    @Override
    public CompositeAPI getCompositeAPISummary(String apiID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompositeAPI getCompositeAPI(String apiID) throws APIMgtDAOException {
        throw new UnsupportedOperationException();
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

    private Object fetchObject(File file, Class c, FilenameFilter filenameFilter) {
        File[] files = (filenameFilter != null) ? file.listFiles(filenameFilter) : file.listFiles();
        if (files != null && files.length > 0) {
            return constructObjectSummaryFromFile(files[0].getAbsolutePath(), c);
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
