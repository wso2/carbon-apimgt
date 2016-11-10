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

import org.wso2.carbon.apimgt.core.models.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
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
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    @CheckForNull API getAPI(String apiID) throws SQLException;

    /**
     * Retrieve a given instance of an APISummary object
     * @param apiID The UUID that uniquely identifies an API
     * @return valid {@link APISummary} object or null
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    @CheckForNull
    APISummary getAPISummary(String apiID) throws SQLException;

    /**
     * Retrieves summary data of all available APIs. This method supports result pagination as well as
     * doing a permission check to ensure results returned are only those that match the list of roles provided
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param roles The list of roles of the user making the query
     * @return {@link APISummaryResults} matching results
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    APISummaryResults getAPIsForRoles(int offset, int limit, List<String> roles) throws SQLException;

    /**
     * Retrieves summary data of all available APIs. This method supports result pagination as well as
     * doing a permission check to ensure results returned are only those that match the list of roles provided
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param providerName A given API Provider
     * @return {@link APISummaryResults} matching results
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    APISummaryResults getAPIsForProvider(int offset, int limit, String providerName) throws SQLException;

    /**
     * Retrieves summary data of all available APIs that match the given search criteria. This method supports result
     * pagination as well as doing a permission check to ensure results returned are only those that match
     * the list of roles provided
     * @param searchString The search string provided
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param roles The list of roles of the user making the query
     * @return {@link APISummaryResults} matching results
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    APISummaryResults searchAPIsForRoles(String searchString, int offset, int limit,
                                         List<String> roles) throws SQLException;

    /**
     * Checks if a given API which is uniquely identified by the Provider, API Name and Version combination already
     * exists
     * @param apiName Name of API
     * @return true if providerName, apiName, version combination already exists else false
     * @throws SQLException if error occurs while accessing data layer
     */
    boolean isAPINameExists(String apiName) throws SQLException;

    /**
     * Checks if a given API Context already exists
     * @param contextName Name of API Context
     * @return true if contextName already exists else false
     * @throws SQLException if error occurs while accessing data layer
     */
    boolean isAPIContextExists(String contextName) throws SQLException;

    /**
     * Add a new instance of an API
     * @param api The {@link API} object to be added
     * @return The newly added {@link API} object
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    void addAPI(API api) throws SQLException;

    /**
     * Update an existing API
     * @param apiID The UUID of the API that needs to be updated
     * @param substituteAPI Substitute {@link API} object that will replace the existing API
     * @return The updated {@link API} object
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    API updateAPI(String apiID, API substituteAPI) throws SQLException;

    /**
     * Remove an existing API
     * @param apiID The UUID of the API that needs to be deleted
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    void deleteAPI(String apiID) throws SQLException;

    /**
     * Get swagger definition of a given API
     * @param apiID The UUID of the respective API
     * @return Swagger definition String
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    String getSwaggerDefinition(String apiID) throws SQLException;

    /**
     * Update swagger definition of a given API
     * @param apiID The UUID of the respective API
     * @param swaggerDefinition Swagger definition String
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    void updateSwaggerDefinition(String apiID, String swaggerDefinition) throws SQLException;

    /**
     * Get image of a given API
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    InputStream getImage(String apiID) throws SQLException;

    /**
     * Update swagger definition of a given API
     * @param apiID The UUID of the respective API
     * @param image Image stream
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    void updateImage(String apiID, OutputStream image) throws SQLException;

    /**
     * Change the lifecycle status of a given API
     * @param apiID The UUID of the respective API
     *              @param deprecateOldVersions  if true for deprecate older versions
     *              @param makeKeysForwardCompatible if true for make subscriptions get forward
     * @param status The lifecycle status that the API must be set to
     * @throws SQLException  if error occurs while accessing data layer
     *
     */
    void changeLifeCycleStatus(String apiID, String status, boolean
            deprecateOldVersions, boolean makeKeysForwardCompatible) throws SQLException;

    /**
     * Return list of all Document info belonging to a given API. This method supports result pagination
     * @param apiID The UUID of the respective API
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @return {@link DocumentInfoResults} matching results
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    DocumentInfoResults getDocumentsInfoList(String apiID, int offset, int limit) throws SQLException;

    /**
     *
     * @param docID The UUID of the respective Document
     * @return {@link DocumentInfo} Document Info object
     * @throws SQLException if error occurs while accessing data layer
     */
    DocumentInfo getDocumentInfo(String docID) throws SQLException;

    /**
     *
     * @param docID The UUID of the respective Document
     * @return {@link InputStream} Document Info object
     * @throws SQLException if error occurs while accessing data layer
     */
    InputStream getDocumentContent(String docID) throws SQLException;

    /**
     * Attach Documentation (without content) to an API
     *
     * @param apiId         UUID of API
     * @param documentation Documentat Summary
     * @throws SQLException if error occurs while accessing data layer
     */
    void addDocumentationInfo(String apiId, DocumentInfo documentation) throws SQLException;

    /**
     * Add a document (of source type FILE) with a file
     *
     * @param apiId         UUID of API
     * @param documentation Document Summary
     * @param filename      name of the file
     * @param content       content of the file as an Input Stream
     * @param contentType   content type of the file
     * @throws SQLException if error occurs while accessing data layer
     */
    void addDocumentationWithFile(String apiId, DocumentInfo documentation, String filename, InputStream content,
                                  String contentType) throws SQLException;

    /**
     * Removes a given documentation
     *
     * @param id   Document Id
     * @throws SQLException if error occurs while accessing data layer
     */
    void removeDocumentation(String id) throws SQLException;

}
