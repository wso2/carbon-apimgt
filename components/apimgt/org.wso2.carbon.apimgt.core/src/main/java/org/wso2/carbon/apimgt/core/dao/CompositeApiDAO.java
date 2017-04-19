/*
 *
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

package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;

import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;

/**
 * Provides access to Composite API data layer
 */
public interface CompositeApiDAO {

    /**
     * Retrieve a given instance of a Composite API
     * @param apiID The UUID that uniquely identifies a Composite API
     * @return valid {@link CompositeAPI} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @CheckForNull
    CompositeAPI getCompositeAPI(String apiID) throws APIMgtDAOException;

    /**
     * Retrieve a given instance of a Composite API with summarized data
     * @param apiID The UUID that uniquely identifies a Composite API
     * @return valid {@link CompositeAPI} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @CheckForNull
    CompositeAPI getCompositeAPISummary(String apiID) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Composite APIs of a given provider.
     * @param providerName A given CompositeAPI Provider
     * @return {@code List<CompositeAPI>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<CompositeAPI> getCompositeAPIsForProvider(String providerName) throws APIMgtDAOException;

    /**
     * Retrieves summary of paginated data of all available Composite APIs that match the given search criteria.
     * This will use the full text search for API table
     * @param user      Current user.
     * @param searchString The search string provided
     * @param offset  The starting point of the search results.
     * @param limit   Number of search results that will be returned.
     * @return {@code List<CompositeAPI>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<CompositeAPI> searchCompositeAPIs(String user, String searchString, int offset, int limit) throws
            APIMgtDAOException;


    /**
     * Retrieves summary of paginated data of all available Composite APIs that match the given search criteria.
     * @param roles     List of the roles of the user.
     * @param user      Current user.
     * @param attributeMap Map containing the attributes and search queries for those attributes
     * @param offset  The starting point of the search results.
     * @param limit   Number of search results that will be returned.
     * @return {@code List<CompositeAPI>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<CompositeAPI> attributeSearchCompositeAPIs(List<String> roles, String user, Map<String, String> attributeMap,
                                           int offset, int limit) throws APIMgtDAOException;

    /**
     * Checks if a given Composite API which is uniquely identified by the Provider, API Name and Version combination
     * already exists
     * @param apiName Name of Composite API
     * @param providerName Provider of the Composite API.
     * @return true if providerName, apiName, version combination already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isCompositeAPINameExists(String apiName, String providerName) throws APIMgtDAOException;

    /**
     * Checks if a given Composite API Context already exists
     * @param contextName Name of Composite API Context
     * @return true if contextName already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isCompositeAPIContextExists(String contextName) throws APIMgtDAOException;

    /**
     * Add a new instance of a Composite API
     * @param api The {@link CompositeAPI} object to be added
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void addCompositeAPI(CompositeAPI api) throws APIMgtDAOException;

    /**
     * Update an existing Composite API
     * @param apiID The UUID of the Composite API that needs to be updated
     * @param substituteAPI Substitute {@link CompositeAPI} object that will replace the existing Composite API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void updateCompositeAPI(String apiID, CompositeAPI substituteAPI) throws APIMgtDAOException;

    /**
     * Remove an existing Composite API
     * @param apiID The UUID of the API that needs to be deleted
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void deleteCompositeAPI(String apiID) throws APIMgtDAOException;

    /**
     * Get swagger definition of a given Composite API
     * @param apiID The UUID of the respective Composite API
     * @return Swagger definition String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    String getSwaggerDefinition(String apiID) throws APIMgtDAOException;

    /**
     * Update swagger definition of a given Composite API
     *
     * @param apiID             The UUID of the respective Composite API
     * @param swaggerDefinition Swagger definition String
     * @param updatedBy user who performs the update
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateSwaggerDefinition(String apiID, String swaggerDefinition, String updatedBy)
            throws APIMgtDAOException;
}
