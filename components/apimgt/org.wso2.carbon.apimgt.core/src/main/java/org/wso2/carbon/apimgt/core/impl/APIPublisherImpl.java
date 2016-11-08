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
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.models.Subscriber;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of API Publisher operations
 */
public class APIPublisherImpl extends AbstractAPIManager implements APIPublisher {

    private static final Logger log = LoggerFactory.getLogger(APIPublisherImpl.class);

    public APIPublisherImpl(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO);
    }

    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.core.models.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws APIManagementException if failed to get Providers
     */
    @Override
    public Set<Provider> getAllProviders() throws APIManagementException {
        return null;
    }

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerId , provider id
     * @return set of API
     * @throws APIManagementException if failed to get set of API
     */
    @Override
    public List<API> getAPIsByProvider(String providerId) throws APIManagementException {
        return null;
    }

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    @Override
    public Set<Subscriber> getSubscribersOfProvider(String providerId) throws APIManagementException {
        return null;
    }

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws APIManagementException if failed to get Provider
     */
    @Override
    public Provider getProvider(String providerName) throws APIManagementException {
        return null;
    }

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier String
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get Subscribers
     */
    @Override
    public Set<Subscriber> getSubscribersOfAPI(API identifier) throws APIManagementException {
        return null;
    }


    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param id String
     * @return Set<APISubscriptionCount>
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    @Override
    public long getAPISubscriptionCountByAPI(String id) throws APIManagementException {
        return 0;
    }

    @Override
    public String getDefaultVersion(String apiid) throws APIManagementException {
        return null;
    }

    /**
     * Adds a new API to the system
     *
     * @param apiBuilder API model object
     * @throws APIManagementException if failed to add API
     */
    @Override
    public void addAPI(API.APIBuilder apiBuilder) throws APIManagementException {

        try {
            apiBuilder.createLifecycleEntry("API_LIFECYCLE", getUsername());
            API createdAPI = apiBuilder.build();
            getApiDAO().addAPI(createdAPI);
            APIUtils.logDebug("API " + createdAPI.getName() + "-" + createdAPI.getVersion() + " was created " +
                    "successfully.",
                    log);
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Error occurred while creating the API - " + apiBuilder.getName(), e, log);
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Error occurred while Associating the API - " + apiBuilder.getName(), e, log);
        }
    }

    /**
     * @param api
     * @return
     * @throws APIManagementException
     */
    @Override
    public boolean isAPIUpdateValid(API api) throws APIManagementException {
        return false;
    }

    /**
     * Updates design and implementation of an existing API. This method must not be used to change API status.
     * Implementations should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param apiBuilder {@link org.wso2.carbon.apimgt.core.models.API.APIBuilder} model object
     * @throws APIManagementException if failed to update API
     */
    @Override
    public void updateAPI(API.APIBuilder apiBuilder) throws APIManagementException {
        API api = apiBuilder.build();
        try {
            if (log.isDebugEnabled()) {
                log.debug("API " + api.getName() + "-" + api.getVersion() + " was updated successfully.");
            }
             getApiDAO().updateAPI(api.getId(), api);
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Error occurred while updating the API - " + api.getName(), e, log);
        }
    }


    /**
     * This method used to Update the status of API
     *
     * @param apiId
     * @param status
     * @param deprecateOldVersions
     * @param makeKeysForwardCompatible
     * @return
     * @throws APIManagementException
     */
    @Override
    public boolean updateAPIStatus(String apiId, String status, boolean
            deprecateOldVersions, boolean makeKeysForwardCompatible) throws APIManagementException {
        try {
            APISummary.Builder apiSummaryBuilder = getApiDAO().getAPISummary(apiId);
            if (apiSummaryBuilder != null){

                apiSummaryBuilder.executeLifecycleEvent(status, getUsername(), "API_LIFECYCLE");
                APISummary apiSummary = apiSummaryBuilder.build();
                getApiDAO().changeLifeCycleStatus(apiSummary.getId(), status, deprecateOldVersions,
                        makeKeysForwardCompatible);
                return true;
            }else{
                return false;
            }
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Couldn't change the status of api ID " + apiId, e, log);
            return false;
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Couldn't change the status of api ID " + apiId, e, log);
            return false;
        }
    }


    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    @Override
    public void createNewAPIVersion(API api, String newVersion) throws APIManagementException {
        try {

           API.APIBuilder apiBuilder = getApiDAO().createNewAPIVersion(api.getId(), newVersion);
            apiBuilder.createLifecycleEntry("API_LIFECYCLE",getUsername());
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Couldn't create new API version from " + api.getName(), e, log);
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Couldn't Associate  new API Lifecycle from " + api.getName(), e, log);
        }
    }

    /**
     * Removes a given documentation
     *
     * @param apiId   String
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws APIManagementException if failed to remove documentation
     */
    @Override
    public void removeDocumentation(String apiId, String docType, String docName) throws APIManagementException {

    }

    /**
     * Removes a given documentation
     *
     * @param apiId String
     * @param docId UUID of the doc
     * @throws APIManagementException if failed to remove documentation
     */
    @Override
    public void removeDocumentation(String apiId, String docId) throws APIManagementException {

    }

    /**
     * Adds Documentation to an API
     *
     * @param apiId         String
     * @param documentation Documentation
     * @throws APIManagementException if failed to add documentation
     */
    @Override
    public void addDocumentation(String apiId, DocumentInfo documentation) throws APIManagementException {

    }

    /**
     * Add a file to a document of source type FILE
     *
     * @param apiId         API identifier the document belongs to
     * @param documentation document
     * @param filename      name of the file
     * @param content       content of the file as an Input Stream
     * @param contentType   content type of the file
     * @throws APIManagementException if failed to add the file
     */
    @Override
    public void addFileToDocumentation(String apiId, DocumentInfo documentation, String filename, InputStream
            content, String contentType) throws APIManagementException {

    }

    /**
     * Checks if a given API exists in the registry
     *
     * @param apiId
     * @return boolean result
     * @throws APIManagementException
     */
    @Override
    public boolean checkIfAPIExists(String apiId) throws APIManagementException {
        return false;
    }

    /**
     * This method used to save the documentation content
     *
     * @param api
     * @param documentationName
     * @param text              @throws APIManagementException if failed to add the document as a resource to registry
     */
    @Override
    public void addDocumentationContent(API api, String documentationName, String text) throws APIManagementException {

    }

    /**
     * Updates a given documentation
     *
     * @param apiId         String
     * @param documentation Documentation
     * @throws APIManagementException if failed to update docs
     */
    @Override
    public void updateDocumentation(String apiId, DocumentInfo documentation) throws APIManagementException {

    }

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param apiId     id of the String
     * @param toVersion Version to which Documentation should be copied.
     * @throws APIManagementException if failed to copy docs
     */
    @Override
    public void copyAllDocumentation(String apiId, String toVersion) throws APIManagementException {

    }

    /**
     * Returns the details of all the life-cycle changes done per API.
     *
     * @param apiId id of the String
     * @return List of life-cycle events per given API
     * @throws APIManagementException if failed to copy docs
     */
    @Override
    public List<LifeCycleEvent> getLifeCycleEvents(String apiId) throws APIManagementException {
        return null;
    }

    /**
     * Delete an API
     *
     * @param identifier String
     * @throws APIManagementException if failed to remove the API
     */
    @Override
    public void deleteAPI(String identifier) throws APIManagementException {
        try {
            if (getAPISubscriptionCountByAPI(identifier) < 0) {
                APISummary.Builder apiSummaryBuilder = getApiDAO().getAPISummary(identifier);
                if (apiSummaryBuilder != null){

                    apiSummaryBuilder.removeLifecycleEntry("API_LIFECYCLE");
                    getApiDAO().deleteAPI(identifier);
                    APIUtils.logDebug("API with id " + identifier + " was deleted successfully.", log);
                }
            } else {
                throw new ApiDeleteFailureException("API with " + identifier + " already have subscriptions");
            }
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Error occurred while deleting the API with id " + identifier, e, log);
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Error occurred while Disassociating the API with Lifecycle id " + identifier,
                    e, log);
        }
    }

    /**
     * @param limit
     * @param offset
     * @param query
     * @return
     * @throws APIManagementException
     */
    @Override
    public APISummaryResults searchAPIs(Integer limit, Integer offset, String query) throws APIManagementException {

        APISummaryResults apiSummaryResults = null;
        try {
            List<String> roles = new ArrayList<>();
            if (query != null && !query.isEmpty()) {
                apiSummaryResults = getApiDAO().searchAPIsForRoles(query, offset, limit, roles);
            } else {
                apiSummaryResults = getApiDAO().getAPIsForRoles(offset, limit, roles);
            }
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Error occurred while Searching the API with query " + query, e, log);
        }
        return apiSummaryResults;
    }

    /**
     * Update the subscription status
     *
     * @param apiId     API Identifier
     * @param subStatus Subscription Status
     * @param appId     Application Id
     * @return int value with subscription id
     * @throws APIManagementException If failed to update subscription status
     */
    @Override
    public void updateSubscription(String apiId, String subStatus, int appId) throws APIManagementException {

    }

    /**
     * This method updates Swagger 2.0 resources in the registry
     *
     * @param apiId    id of the String
     * @param jsonText json text to be saved in the registry
     * @throws APIManagementException
     */
    @Override
    public void saveSwagger20Definition(String apiId, String jsonText) throws APIManagementException {

    }

    /**
     * This method is to change registry lifecycle states for an API artifact
     *
     * @param string string
     * @param action Action which need to execute from registry lifecycle
     */
    @Override
    public boolean changeLifeCycleStatus(String string, String action) throws APIManagementException {
        return false;
    }

    /**
     * This method is to set checklist item values for a particular life-cycle state of an API
     *
     * @param string         string
     * @param checkItem      Order of the checklist item
     * @param checkItemValue Value of the checklist item
     */
    @Override
    public boolean changeAPILCCheckListItems(String string, int checkItem, boolean checkItemValue)
            throws APIManagementException {
        return false;
    }

    /**
     * This method is to set a lifecycle check list item given the String and the checklist item name.
     * If the given item not in the allowed lifecycle check items list or item is already checked, this will stay
     * silent and return false. Otherwise, the checklist item will be updated and returns true.
     *
     * @param string         String
     * @param checkItemName  Name of the checklist item
     * @param checkItemValue Value to be set to the checklist item
     * @return boolean value representing success not not
     * @throws APIManagementException
     */
    @Override
    public boolean checkAndChangeAPILCCheckListItem(String string, String checkItemName, boolean
            checkItemValue) throws APIManagementException {
        return false;
    }

    /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId String
     * @return Map<String,Object> a map with lifecycle data
     */
    @Override
    public Map<String, Object> getAPILifeCycleData(String apiId) throws APIManagementException {
        return null;
    }

    /**
     * Push api related state changes to the gateway. Api related configurations will be deployed or destroyed
     * according to the new state.
     *
     * @param identifier Api identifier
     * @param newStatus  new state of the lifecycle
     * @return collection of failed gateways. Map contains gateway name as the key and the error as the value
     * @throws APIManagementException
     */
    @Override
    public Map<String, String> propergateAPIStatusChangeToGateways(String identifier, APIStatus newStatus)
            throws APIManagementException {
        return null;
    }

    /**
     * Update api related information such as database entries, registry updates for state change.
     *
     * @param identifier
     * @param newStatus  accepted if changes are not pushed to a gateway
     * @return boolean value representing success not not
     * @throws APIManagementException
     */
    @Override
    public boolean updateAPIForStateChange(String identifier, APIStatus newStatus) throws
            APIManagementException {
        return false;
    }

    /**
     * Get the current lifecycle status of the api
     *
     * @param string Api identifier
     * @return Current lifecycle status
     * @throws APIManagementException
     */
    @Override
    public String getAPILifeCycleStatus(String string) throws APIManagementException {
        return null;
    }

    /**
     * Get the paginated APIs from publisher
     *
     * @param start starting number
     * @param end   ending number
     * @return set of API
     * @throws APIManagementException if failed to get Apis
     */
    @Override
    public Map<String, Object> getAllPaginatedAPIs(int start, int end) throws APIManagementException {
        return null;
    }
}
