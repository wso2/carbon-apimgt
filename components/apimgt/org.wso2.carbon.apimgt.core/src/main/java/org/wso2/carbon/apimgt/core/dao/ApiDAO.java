/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;

/**
 * Provides access to API data layer
 */
public interface ApiDAO {
    /**
     * Retrieve a given instance of an API
     * @param apiID The UUID that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @CheckForNull API getAPI(String apiID) throws APIMgtDAOException;

    /**
     * Retrieve a given instance of an APISummary object
     * @param apiID The UUID that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @CheckForNull
    API getAPISummary(String apiID) throws APIMgtDAOException;


    /**
     *
     * @param apiId UUID of API
     * @return Last updated time of API given its uuid
     * @throws APIMgtDAOException
     */
    String getLastUpdatedTimeOfAPI(String apiId) throws APIMgtDAOException;

    /**
     *
     * @param apiId UUID of API
     * @return Last updated time of Swagger definition given the uuid of API
     * @throws APIMgtDAOException
     */
    String getLastUpdatedTimeOfSwaggerDefinition(String apiId) throws APIMgtDAOException;

    /**
     *
     * @param apiId UUID of API
     * @return Last updated time of gateway configuration given the uuid of API
     * @throws APIMgtDAOException
     */ 
    String getLastUpdatedTimeOfGatewayConfig(String apiId) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs.
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIs() throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs of a given provider.
     * @param providerName A given API Provider
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIsForProvider(String providerName) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs with life cycle status that matches the status list provided
     * @param statuses A list of matching life cycle statuses
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIsByStatus(List<String> statuses) throws APIMgtDAOException;

    /**
     * Retrieves summary of paginated data of all available APIs that match the given search criteria. This will use
     * the full text search for API table
     * @param roles     List of the roles of the user.
     * @param user      Current user.
     * @param searchString The search string provided
     * @param offset  The starting point of the search results.
     * @param limit   Number of search results that will be returned.
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> searchAPIs(List<String> roles, String user, String searchString, int offset, int limit) throws
            APIMgtDAOException;

    /**
     * Retrieves summary of paginated data of all available APIs that match the given search criteria.
     * @param roles     List of the roles of the user.
     * @param user      Current user.
     * @param attributeMap Map containing the attributes and search queries for those attributes
     * @param offset  The starting point of the search results.
     * @param limit   Number of search results that will be returned.
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> attributeSearchAPIs(List<String> roles, String user, Map<String, String> attributeMap, int offset, int
            limit) throws
            APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs with life cycle status that matches the status list provided
     * and matches the given search criteria.
     * @param searchString The search string provided
     * @param statuses A list of matching life cycle statuses
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> searchAPIsByStatus(String searchString, List<String> statuses) throws APIMgtDAOException;

    /**
     * Checks if a given API which is uniquely identified by the Provider, API Name and Version combination already
     * exists
     * @param apiName Name of API
     * @param providerName Provider of the API.
     * @return true if providerName, apiName, version combination already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isAPINameExists(String apiName, String providerName) throws APIMgtDAOException;

    /**
     * Checks if a given API Context already exists
     * @param contextName Name of API Context
     * @return true if contextName already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isAPIContextExists(String contextName) throws APIMgtDAOException;

    /**
     * Add a new instance of an API
     * @param api The {@link API} object to be added
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void addAPI(API api) throws APIMgtDAOException;

    /**
     * Update an existing API
     * @param apiID The UUID of the API that needs to be updated
     * @param substituteAPI Substitute {@link API} object that will replace the existing API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void updateAPI(String apiID, API substituteAPI) throws APIMgtDAOException;

    /**
     * Remove an existing API
     * @param apiID The UUID of the API that needs to be deleted
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void deleteAPI(String apiID) throws APIMgtDAOException;

    /**
     * Get swagger definition of a given API
     * @param apiID The UUID of the respective API
     * @return Swagger definition String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    String getSwaggerDefinition(String apiID) throws APIMgtDAOException;

    /**
     * Get image of a given API
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    InputStream getImage(String apiID) throws APIMgtDAOException;

    /**
     * Update image of a given API
     *
     * @param apiID    The UUID of the respective API
     * @param image    Image stream
     * @param dataType Data Type of image
     * @param updatedBy user who adds/updates the API
     * @param updatedTime the time of adding/updating the API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateImage(String apiID, InputStream image, String dataType, String updatedBy,
            LocalDateTime updatedTime) throws APIMgtDAOException;

    /**
     * Change the lifecycle status of a given API
     * @param apiID The UUID of the respective API
     * @param status The lifecycle status that the API must be set to
     * @throws APIMgtDAOException  if error occurs while accessing data layer
     *
     */
    void changeLifeCycleStatus(String apiID, String status) throws APIMgtDAOException;

    /**
     * Return list of all Document info belonging to a given API.
     *
     * @param apiID The UUID of the respective API
     * @return {@code List<DocumentInfo>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<DocumentInfo> getDocumentsInfoList(String apiID) throws APIMgtDAOException;

    /**
     *
     * @param resourceID The UUID of the respective resource
     * @return {@link DocumentInfo} DocumentInfo meta data
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @CheckForNull
    DocumentInfo getDocumentInfo(String resourceID) throws APIMgtDAOException;

    /**
     *
     * @param resourceID The UUID of the respective resource
     * @return {@link InputStream} Document File content
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @CheckForNull
    InputStream getDocumentFileContent(String resourceID) throws APIMgtDAOException;

    /**
     *
     * @param resourceID The UUID of the respective resource
     * @return {@link String} Document inline content
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @CheckForNull
    String getDocumentInlineContent(String resourceID) throws APIMgtDAOException;

    /**
     * Add document info meta data to an API
     *
     * @param apiId    UUID of API
     * @param documentInfo {@link DocumentInfo}
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addDocumentInfo(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException;

    /**
     * Add document info meta data to an API
     *
     * @param apiId    UUID of API
     * @param documentInfo {@link DocumentInfo}
     * @param updatedBy user who performs the action 
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateDocumentInfo(String apiId, DocumentInfo documentInfo, String updatedBy) throws APIMgtDAOException;

    /**
     * Add Document File content
     *
     * @param resourceID UUID of resource
     * @param content    File content as an InputStream
     * @param fileName
     * @param updatedBy user who performs the action
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addDocumentFileContent(String resourceID, InputStream content, String fileName, String updatedBy) throws
            APIMgtDAOException;

    /**
     * Add Document Inline content
     *
     * @param resourceID UUID of resource
     * @param content    Inline content as a String
     * @param updatedBy user who updates the resource
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addDocumentInlineContent(String resourceID, String content, String updatedBy) throws APIMgtDAOException;

    /**
     * Delete a document
     *
     * @param resourceID   UUID of resource
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void deleteDocument(String resourceID) throws APIMgtDAOException;

    /**
     * used to deprecate older versions of the api
     * @param identifier API ID.
     */
    void deprecateOlderVersions(String identifier);

    /**
     * Check if document Exist
     * @param apiId UUID of the API.
     * @param documentInfo  Document.
     * @return  TRUE or false based on the existence of document.
     * @throws APIMgtDAOException   If error occurs while checking if document exits.
     */
    boolean isDocumentExist(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException;


    /**
     * Add an Endpoint
     *
     * @param endpoint  Endpoint Object.
     * @throws APIMgtDAOException   If failed to add endpoint.
     */
    void addEndpoint(Endpoint endpoint) throws APIMgtDAOException;



    /**
     * Delete an Endpoint
     *
     * @param endpointId    UUID of the endpoint.
     * @return  Success value of the endpoint delete operation
     * @throws APIMgtDAOException   If failed to delete API.
     */
    boolean deleteEndpoint(String endpointId) throws APIMgtDAOException;

    /**
     * Update an Endpoint
     *
     * @param endpoint  Endpoint object.
     * @return  Success value of the endpoint update operation
     * @throws APIMgtDAOException   If failed to update API.
     */
    boolean updateEndpoint(Endpoint endpoint) throws APIMgtDAOException;

    /**
     * Get an Endpoint
     *
     * @param endpointId uuid of endpoint
     * @return  Endpoint object.
     * @throws APIMgtDAOException   If failed to get endpoint.
     */
    Endpoint getEndpoint(String endpointId) throws APIMgtDAOException;

    /**
     * Get an Endpoint
     *
     * @param name name of endpoint
     * @return  Endpoint object.
     * @throws APIMgtDAOException   If failed to get endpoint.
     */
    Endpoint getEndpointByName(String name) throws APIMgtDAOException;

    /**
     * get all Endpoints
     * @return  List of endpoint objects.
     * @throws APIMgtDAOException   If failed to get endpoint.
     */
    List<Endpoint> getEndpoints() throws APIMgtDAOException;

    /**
     * Update swagger definition of a given API
     *
     * @param apiID             The UUID of the respective API
     * @param swaggerDefinition Swagger definition String
     * @param updatedBy user who performs the update
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateSwaggerDefinition(String apiID, String swaggerDefinition, String updatedBy)
            throws APIMgtDAOException;

    /**
     * Get gateway configuration of a given API
     * @param apiID The UUID of the respective API
     * @return gateway configuration String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    String getGatewayConfig(String apiID) throws APIMgtDAOException;

    /**
     * update gateway config
     *
     * @param apiID         api uuid
     * @param gatewayConfig config text
     * @param updatedBy user who performs the action
     * @throws APIMgtDAOException throws if any error occurred
     */
    void updateGatewayConfig(String apiID, String gatewayConfig, String updatedBy) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of a document of an API
     *
     * @param documentId UUID of document
     * @return Last updated time of document given its uuid
     * @throws APIMgtDAOException throws if any DB level error occurred
     */
    String getLastUpdatedTimeOfDocument(String documentId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the content of a document of an API
     *
     * @param apiId UUID of API
     * @param documentId UUID of document
     * @return  Last updated time of document's content
     * @throws APIMgtDAOException throws if any DB level error occurred
     */
    String getLastUpdatedTimeOfDocumentContent(String apiId, String documentId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the thumbnail image of an API
     *
     * @param apiId UUID of API
     * @return  Last updated time of document's content
     * @throws APIMgtDAOException throws if any db level error occurred
     */
    String getLastUpdatedTimeOfAPIThumbnailImage(String apiId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the endpoint given its endpointId
     *
     * @param endpointId Id of the endpoint
     * @return last updated time 
     */
    String getLastUpdatedTimeOfEndpoint(String endpointId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the subscription
     *
     * @param subscriptionId UUID of the subscription
     * @return  Last updated time of the resource
     * @throws APIMgtDAOException if DB level exception occurred
     */
    String getLastUpdatedTimeOfSubscription(String subscriptionId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the application
     *
     * @param applicationId UUID of the application
     * @return  Last updated time of the resource
     * @throws APIMgtDAOException if DB level exception occurred
     */
    String getLastUpdatedTimeOfApplication(String applicationId) throws APIMgtDAOException;
}
