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

import java.io.InputStream;
import java.util.List;
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
     * Retrieves summary data of all available APIs.
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIs() throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs of a given provider.
     * @param providerName A given API Provider
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIsForProvider(String providerName) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs with life cycle status that matches the status list provided
     * @param statuses A list of matching life cycle statuses
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIsByStatus(List<String> statuses) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs that match the given search criteria.
     * @param searchString The search string provided
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> searchAPIs(String searchString) throws APIMgtDAOException;

    /**
     * Checks if a given API which is uniquely identified by the Provider, API Name and Version combination already
     * exists
     * @param apiName Name of API
     * @return true if providerName, apiName, version combination already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isAPINameExists(String apiName) throws APIMgtDAOException;

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
     * @return The updated {@link API} object
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    API updateAPI(String apiID, API substituteAPI) throws APIMgtDAOException;

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
     * Update swagger definition of a given API
     * @param apiID The UUID of the respective API
     * @param swaggerDefinition Swagger definition String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void updateSwaggerDefinition(String apiID, String swaggerDefinition) throws APIMgtDAOException;

    /**
     * Get image of a given API
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    InputStream getImage(String apiID) throws APIMgtDAOException;

    /**
     * Update swagger definition of a given API
     * @param apiID The UUID of the respective API
     * @param image Image stream
     * @param dataType Data Type of image
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void updateImage(String apiID, InputStream image, String dataType) throws APIMgtDAOException;

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
     * @return {@link List<DocumentInfo>} matching results
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
     * Add Document File content
     *
     * @param resourceID         UUID of resource
     * @param content            File content as an InputStream
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addDocumentFileContent(String resourceID, InputStream content) throws APIMgtDAOException;

    /**
     * Add Document File content
     *
     * @param resourceID         UUID of resource
     * @param content            Inline content as a String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addDocumentInlineContent(String resourceID, String content) throws APIMgtDAOException;

    /**
     * Delete a document
     *
     * @param resourceID   UUID of resource
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void deleteDocument(String resourceID) throws APIMgtDAOException;

    /**
     * used to deprecate older versions of the api
     * @param identifier
     */
    void deprecateOlderVersions(String identifier);
    }
