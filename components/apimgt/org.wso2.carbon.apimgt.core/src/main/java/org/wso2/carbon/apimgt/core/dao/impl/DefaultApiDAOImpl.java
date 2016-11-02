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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.dao.APIManagementDAOException;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;
import org.wso2.carbon.apimgt.core.models.DocumentInfoResults;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.CheckForNull;

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class DefaultApiDAOImpl implements ApiDAO {

    DefaultApiDAOImpl() {}

    /**
     * Retrieve a given instance of an API
     *
     * @param apiID The {@link String} that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIManagementDAOException
     */
    @Override
    @CheckForNull
    public API getAPI(String apiID) throws APIManagementDAOException {
        return null;
    }

    /**
     * Retrieves summary data of all available APIs. This method supports result pagination as well as
     * doing a permission check to ensure results returned are only those that match the list of roles provided
     *
     * @param offset        The number of results from the beginning that is to be ignored
     * @param limit         The maximum number of results to be returned after the offset
     * @param roles The list of roles of the user making the query
     * @return {@link APISummaryResults} matching results
     * @throws APIManagementDAOException
     */
    @Override
    public APISummaryResults getAPIsForRoles(int offset, int limit, List<String> roles)
                                                                            throws APIManagementDAOException {
        return null;
    }

    /**
     * Retrieves summary data of all available APIs that match the given search criteria. This method supports result
     * pagination as well as doing a permission check to ensure results returned are only those that match
     * the list of roles provided
     *
     * @param searchAttribute The attribute of an API against which the search will be performed
     * @param searchString    The search string provided
     * @param offset          The number of results from the beginning that is to be ignored
     * @param limit           The maximum number of results to be returned after the offset
     * @param roles   The list of roles of the user making the query
     * @return {@link APISummaryResults} matching results
     * @throws APIManagementDAOException
     */
    @Override
    public APISummaryResults searchAPIsForRoles(String searchAttribute, String searchString, int offset, int limit,
                                                List<String> roles) throws APIManagementDAOException {
        return null;
    }

    /**
     * Add a new instance of an API
     *
     * @param api The {@link API} object to be added
     * @return true if addition is successful else false
     * @throws APIManagementDAOException
     */
    @Override
    public API addAPI(API api) throws APIManagementDAOException {
        return null;
    }

    /**
     * Update an existing API
     *
     * @param apiID      The {@link String} of the API that needs to be updated
     * @param substituteAPI Substitute {@link API} object that will replace the existing API
     * @return true if update is successful else false
     * @throws APIManagementDAOException
     */
    @Override
    public API updateAPI(String apiID, API substituteAPI) throws APIManagementDAOException {
        return null;
    }

    /**
     * Remove an existing API
     *
     * @param apiID The {@link String} of the API that needs to be deleted
     * @return true if update is successful else false
     * @throws APIManagementDAOException
     */
    @Override
    public void deleteAPI(String apiID) throws APIManagementDAOException {
    }

    /**
     * Get swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Swagger definition stream
     * @throws APIManagementDAOException
     */
    @Override
    public OutputStream getSwaggerDefinition(String apiID) throws APIManagementDAOException {
        return null;
    }

    /**
     * Update swagger definition of a given API
     *
     * @param apiID             The UUID of the respective API
     * @param swaggerDefinition Swagger definition stream
     * @throws APIManagementDAOException
     */
    @Override
    public void updateSwaggerDefinition(String apiID, InputStream swaggerDefinition) throws APIManagementDAOException {

    }

    /**
     * Get image of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws APIManagementDAOException
     */
    @Override
    public OutputStream getImage(String apiID) throws APIManagementDAOException {
        return null;
    }

    /**
     * Update swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @param image Image stream
     * @throws APIManagementDAOException
     */
    @Override
    public void updateImage(String apiID, InputStream image) throws APIManagementDAOException {

    }

    /**
     * Change the lifecycle status of a given API
     *
     * @param apiID                     The UUID of the respective API
     * @param status                    The lifecycle status that the API must be set to
     * @param deprecateOldVersions      if true for deprecate older versions
     * @param makeKeysForwardCompatible if true for make subscriptions get forward
     * @throws APIManagementDAOException
     */
    @Override
    public void changeLifeCycleStatus(String apiID, String status, boolean deprecateOldVersions, boolean
            makeKeysForwardCompatible) throws APIManagementDAOException {

    }

    /**
     * Create a new version of an existing API
     *
     * @param apiID   The UUID of the respective API
     * @param version The new version of the API
     * @throws APIManagementDAOException
     */
    @Override
    public void createNewAPIVersion(String apiID, String version) throws APIManagementDAOException {

    }

    /**
     * Return list of all Document info belonging to a given API. This method supports result pagination
     *
     * @param apiID  The UUID of the respective API
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit  The maximum number of results to be returned after the offset
     * @throws APIManagementDAOException
     */
    @Override
    public DocumentInfoResults getDocumentsInfoList(String apiID, int offset, int limit)
                                                                    throws APIManagementDAOException {
        return null;
    }

}
