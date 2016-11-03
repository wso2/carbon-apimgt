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
package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIManager;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementDAOException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.List;
import java.util.Set;
/**
 * This class contains the implementation of the common methods for Publisher and store
 */
public abstract class AbstractAPIManager implements APIManager {

    private static final Logger log = LoggerFactory.getLogger(DAOFactory.class);

    private ApiDAO apiDAO;
    private ApplicationDAO applicationDAO;
    private APISubscriptionDAO apiSubscriptionDAO;
    private String username;

    public AbstractAPIManager(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO)  {
        this.username = username;
        this.apiDAO = apiDAO;
        this.applicationDAO = applicationDAO;
        this.apiSubscriptionDAO = apiSubscriptionDAO;
    }

    /**
     * Returns a list of all existing APIs by all providers. The API objects returned by this
     * method may be partially initialized (due to performance reasons). Each API instance
     * is guaranteed to have the API name, version, provider name, context, status and icon URL.
     * All other fields may not be initialized. Therefore, the objects returned by this method
     * must not be used to access any metadata item related to an API, other than the ones listed
     * above. For that purpose a fully initialized API object instance should be acquired by
     * calling the getAPI(String) method.
     *
     * @return a List of API objects (partially initialized), possibly empty
     * @throws APIManagementException on error
     */
    @Override
    public List<API> getAllAPIs() throws APIManagementException {
        return null;
    }

    /**
     * Returns details of an API.
     *
     * @param uuid UUID of the API's registry artifact
     * @return An API object related to the given artifact id or null
     * @throws APIManagementException if failed get API from String
     */
    @Override
    public API getAPIbyUUID(String uuid) throws APIManagementException {
        API api = null;
        try {
            api = apiDAO.getAPI(uuid);
        } catch (APIManagementDAOException e) {
            APIUtils.logAndThrowException("Error occurred while retrieving API with id " + uuid, e, log);
        }
        return api;
    }

    /**
     * Checks the Availability of given String
     *
     * @param api
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     * @api
     */
    @Override
    public boolean isAPIAvailable(API api) throws APIManagementException {
        return false;
    }

    /**
     * Checks whether the given API context is already registered in the system
     *
     * @param context A String representing an API context
     * @return true if the context already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    @Override
    public boolean isContextExist(String context) throws APIManagementException {
        return false;
    }

    /**
     * Checks whether the given API name is already registered in the system
     *
     * @param apiName A String representing an API name
     * @return true if the api name already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    @Override
    public boolean isApiNameExist(String apiName) throws APIManagementException {
        return false;
    }

    /**
     * Returns a set of API versions for the given provider and API name
     *
     * @param providerName name of the provider (common)
     * @param apiName      name of the api
     * @return Set of version strings (possibly empty)
     * @throws APIManagementException if failed to get version for api
     */
    @Override
    public Set<String> getAPIVersions(String providerName, String apiName) throws APIManagementException {
        return null;
    }

    /**
     * Returns the swagger v2.0 definition as a string
     *
     * @param api id of the String
     * @return swagger string
     * @throws APIManagementException
     */
    @Override
    public String getSwagger20Definition(String api) throws APIManagementException {
        return null;
    }

    protected ApiDAO getApiDAO() {
        return apiDAO;
    }

    protected void setApiDAO(ApiDAO apiDAO) {
        this.apiDAO = apiDAO;
    }

    protected ApplicationDAO getApplicationDAO() {
        return applicationDAO;
    }

    protected void setApplicationDAO(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }

    protected APISubscriptionDAO getApiSubscriptionDAO() {
        return apiSubscriptionDAO;
    }

    protected void setApiSubscriptionDAO(APISubscriptionDAO apiSubscriptionDAO) {
        this.apiSubscriptionDAO = apiSubscriptionDAO;
    }

    protected String getUsername() {
        return username;
    }

    protected void setUsername(String username) {
        this.username = username;
    }
    
    
}
