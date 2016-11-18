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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.util.APIConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleHistoryBean;

import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of API Publisher operations
 */
public class APIPublisherImpl extends AbstractAPIManager implements APIPublisher {

    private static final Logger log = LoggerFactory.getLogger(APIPublisherImpl.class);

    public APIPublisherImpl(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO, APILifecycleManager apiLifecycleManager) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO, apiLifecycleManager);
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
     * @param providerName username of the the user who created the API
     * @return set of APIs
     * @throws APIManagementException if failed to get set of API
     */
    @Override
    public List<API> getAPIsByProvider(String providerName) throws APIManagementException {
        try {
            getApiDAO().getAPI(providerName); //todo: call correct doa method
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Unable to fetch APIs of " + providerName, e, log);
        }
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
    public Set<String> getSubscribersOfProvider(String providerId) throws APIManagementException {
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
    public Set<String> getSubscribersOfAPI(API identifier) throws APIManagementException {
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
       long subscriptionCount = 0;
        try {
            subscriptionCount =  getApiSubscriptionDAO().getAPISubscriptionCountByAPI(id);
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Couldn't retrieve Subscriptions for API " + id, e, log);
        }
        return subscriptionCount;
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
    public String addAPI(API.APIBuilder apiBuilder) throws APIManagementException {

        API createdAPI;
        if (apiBuilder.getId() == null) {
            apiBuilder.id(UUID.randomUUID().toString());
        }
        if (apiBuilder.getApiDefinition() == null) {
            APIUtils.logAndThrowException("Couldn't find swagger definition of API " + apiBuilder.getName(), log);
        }
        APIDefinition apiDefinition = new APIDefinitionFromSwagger20();
        apiBuilder.uriTemplates(apiDefinition.getURITemplates(apiBuilder.getApiDefinition()));
        LocalDateTime localDateTime = LocalDateTime.now();
        apiBuilder.createdTime(localDateTime);
        apiBuilder.lastUpdatedTime(localDateTime);
        try {
            if (!isApiNameExist(apiBuilder.getName()) && !isContextExist(apiBuilder.getContext())) {
                LifecycleState lifecycleState = getApiLifecycleManager().addLifecycle(APIConstants.API_LIFECYCLE,
                        getUsername());
                apiBuilder.associateLifecycle(lifecycleState);
                createdAPI = apiBuilder.build();
                getApiDAO().addAPI(createdAPI);
                APIUtils.logDebug("API " + createdAPI.getName() + "-" + createdAPI.getVersion() + " was created " +
                        "successfully.", log);
            } else {
                APIUtils.logAndThrowException("Duplicate API already Exist with name/Context " + apiBuilder.getName(),
                        log);
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Error occurred while creating the API - " + apiBuilder.getName(), e, log);
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Error occurred while Associating the API - " + apiBuilder.getName(), e, log);
        }
        return apiBuilder.getId();
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
        apiBuilder.lastUpdatedTime(LocalDateTime.now());
        try {
            API originalAPI = getAPIbyUUID(apiBuilder.getId());
            if (originalAPI != null) {
                apiBuilder.createdTime(originalAPI.getCreatedTime());
                if (StringUtils.isNotEmpty(apiBuilder.getApiDefinition()) && (originalAPI.getName().equals(apiBuilder
                        .getName())) && (originalAPI.getContext().equals(apiBuilder.getContext())) && (originalAPI
                        .getVersion().equals(apiBuilder.getVersion())) && (originalAPI.getProvider().equals
                        (apiBuilder.getProvider())) && originalAPI.getLifeCycleStatus().equalsIgnoreCase(apiBuilder
                        .getLifeCycleStatus())) {
                    apiBuilder.uriTemplates(new APIDefinitionFromSwagger20().getURITemplates(apiBuilder
                            .getApiDefinition()));
                    API api = apiBuilder.build();
                    getApiDAO().updateAPI(api.getId(), api);
                    if (log.isDebugEnabled()) {
                        log.debug("API " + api.getName() + "-" + api.getVersion() + " was updated successfully.");
                    }
                } else if (!originalAPI.getLifeCycleStatus().equals(apiBuilder.getLifeCycleStatus())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "status change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    APIUtils.logAndThrowException(msg, log);
                } else if (!originalAPI.getName().equals(apiBuilder.getName())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "API Name Change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    APIUtils.logAndThrowException(msg, log);
                } else if (!originalAPI.getContext().equals(apiBuilder.getContext())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "Context change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    APIUtils.logAndThrowException(msg, log);
                } else if (!originalAPI.getVersion().equals(apiBuilder.getVersion())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "Version change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    APIUtils.logAndThrowException(msg, log);
                } else if (!originalAPI.getProvider().equals(apiBuilder.getProvider())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "provider change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    APIUtils.logAndThrowException(msg, log);
                }
            } else {
                APIUtils.logAndThrowException("Couldn't found API with ID " + apiBuilder.getId(), log);
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Error occurred while updating the API - " + apiBuilder.getName(), e, log);
        }
    }


    /**
     * This method used to Update the status of API
     *
     * @param apiId
     * @param status
     * @return
     * @throws APIManagementException
     */
    @Override
    public void updateAPIStatus(String apiId, String status, Map<String, Boolean> checkListItemMap) throws
            APIManagementException {
        try {
            API api = getApiDAO().getAPI(apiId);
            if (api != null) {
                API.APIBuilder apiBuilder = new API.APIBuilder(api);
                apiBuilder.lifecycleState(getApiLifecycleManager().getCurrentLifecycleState(apiBuilder
                        .getLifecycleInstanceId()));
                for (Map.Entry<String, Boolean> checkListItem : checkListItemMap.entrySet()) {
                    apiBuilder.lifecycleState(getApiLifecycleManager().checkListItemEvent(api.getLifecycleInstanceId
                            (), status, checkListItem.getKey(), checkListItem.getValue()));
                }
                API originalAPI = apiBuilder.build();
                getApiLifecycleManager().executeLifecycleEvent(status, apiBuilder
                        .getLifecycleInstanceId(), getUsername(), originalAPI);
            } else {
                throw new APIMgtResourceNotFoundException("Requested API " + apiId + " Not Available");
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Couldn't change the status of api ID " + apiId, e, log);
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Couldn't change the status of api ID " + apiId + " ", e, log);
        }
    }


    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param apiId        The API to be copied
     * @param newVersion The version of the new API
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    @Override
    public String createNewAPIVersion(String apiId, String newVersion) throws APIManagementException {
        String newVersionedId = null;
        LifecycleState lifecycleState = null;
        try {
            API api = getApiDAO().getAPI(apiId);
            if (api != null) {
                API.APIBuilder apiBuilder = new API.APIBuilder(api);
                apiBuilder.id(UUID.randomUUID().toString());
                apiBuilder.version(newVersion);
                apiBuilder.context(api.getContext().replace(api.getVersion(), newVersion));
                lifecycleState = getApiLifecycleManager().addLifecycle(APIConstants.API_LIFECYCLE, getUsername());
                apiBuilder.associateLifecycle(lifecycleState);
                getApiDAO().addAPI(apiBuilder.build());
                newVersionedId = apiBuilder.getId();
            } else {
                throw new APIMgtResourceNotFoundException("Requested API on UUID " + apiId + "Couldn't be found");
            }
        } catch (APIMgtDAOException e) {
            if (lifecycleState != null) {
                try {
                    getApiLifecycleManager().removeLifecycle(lifecycleState.getLifecycleId());
                } catch (LifecycleException e1) {
                    log.error("Couldn't disassociate lifecycle " + lifecycleState.getLifecycleId());
                }
            }
            APIUtils.logAndThrowException("Couldn't create new API version from " + apiId, e, log);
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Couldn't Associate  new API Lifecycle from " + apiId, e, log);
        }
        return newVersionedId;
    }

    /**
     * Attach Documentation (without content) to an API
     *
     * @param apiId         UUID of API
     * @param documentation Documentat Summary
     * @throws APIManagementException if failed to add documentation
     */
    @Override
    public void addDocumentationInfo(String apiId, DocumentInfo documentation) throws APIManagementException {
        try {
            getApiDAO().addDocumentationInfo(apiId, documentation);
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Unable to add documentation", e, log);
        }
    }

    /**
     * Add a document (of source type FILE) with a file
     *
     * @param apiId         UUID of API
     * @param documentation Document Summary
     * @param filename      name of the file
     * @param content       content of the file as an Input Stream
     * @param contentType   content type of the file
     * @throws APIManagementException if failed to add the file
     */
    @Override
    public void addDocumentationWithFile(String apiId, DocumentInfo documentation, String filename, InputStream content,
                                         String contentType) throws APIManagementException {
        try {
            getApiDAO().addDocumentationWithFile(apiId, documentation, filename, content, contentType);
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Unable to add documentation with file", e, log);
        }
    }

    /**
     * Removes a given documentation
     *
     * @param docId Document Id
     * @throws APIManagementException if failed to remove documentation
     */
    @Override
    public void removeDocumentation(String docId) throws APIManagementException {
        try {
            getApiDAO().removeDocumentation(docId);
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Unable to add documentation with file", e, log);
        }
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
        boolean status = false;
        try {
            if (getApiDAO().getAPISummary(apiId) == null) {
                status = false;
            } else {
                status = true;
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Couldn't get APISummary for " + apiId, e, log);
        }
        return status;
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
        List<LifeCycleEvent> lifeCycleEventList = new ArrayList<>();
        try {
            API apiSummary = getApiDAO().getAPISummary(apiId);
            if (apiSummary != null) {
                List<LifecycleHistoryBean> lifecycleHistoryBeanList = getApiLifecycleManager().getLifecycleHistory
                        (apiSummary.getLifecycleInstanceId());
                for (LifecycleHistoryBean lifecycleHistoryBean : lifecycleHistoryBeanList) {
                    lifeCycleEventList.add(new LifeCycleEvent(apiId, lifecycleHistoryBean.getPreviousState(),
                            lifecycleHistoryBean.getPostState(), lifecycleHistoryBean.getUser(), lifecycleHistoryBean
                            .getUpdatedTime()));
                }
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Couldn't find APISummary Resource for ID " + apiId, e, log);
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Couldn't find APILifecycle History for ID " + apiId, e, log);
        }
        return lifeCycleEventList;
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
            if (getAPISubscriptionCountByAPI(identifier) == 0) {
                API api = getApiDAO().getAPI(identifier);
                if (api != null) {
                    API.APIBuilder apiBuilder = new API.APIBuilder(api);
                    getApiDAO().deleteAPI(identifier);
                    getApiLifecycleManager().removeLifecycle(apiBuilder.getLifecycleInstanceId());
                    APIUtils.logDebug("API with id " + identifier + " was deleted successfully.", log);
                }
            } else {
                throw new ApiDeleteFailureException("API with " + identifier + " already have subscriptions");
            }
        } catch (APIMgtDAOException e) {
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
    public List<API> searchAPIs(Integer limit, Integer offset, String query) throws APIManagementException {

        List<API> apiResults = null;
        try {
            //TODO: Need to validate users roles against results returned
            if (query != null && !query.isEmpty()) {
                apiResults = getApiDAO().searchAPIs(query);
            } else {
                apiResults = getApiDAO().getAPIs();
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Error occurred while Searching the API with query " + query, e, log);
        }
        return apiResults;
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
        try {
            getApiDAO().updateSwaggerDefinition(apiId, jsonText);
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Couldn't update the Swagger Definition", e, log);
        }
    }


    /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId String
     * @return Map<String,Object> a map with lifecycle data
     */
    @Override
    public LifecycleState getAPILifeCycleData(String apiId) throws APIManagementException {
        try {
            API api = getApiDAO().getAPISummary(apiId);
            if (api != null) {

                return getApiLifecycleManager().getCurrentLifecycleState(api.getLifecycleInstanceId());
            } else {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve API Summary for " + apiId);
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Couldn't retrieve API Summary for " + apiId, e, log);
        } catch (LifecycleException e) {
            APIUtils.logAndThrowException("Couldn't retrieve API Lifecycle for " + apiId, e, log);
        }
        return null;
    }


    /**
     * Update api related information such as database entries, registry updates for state change.
     *
     * @param identifier
     * @param newStatus  accepted if changes are not pushed to a gateway
     * @param deprecateOlderVersions
     *@param requireReSubscriptions @return boolean value representing success not not
     * @throws APIManagementException
     */
    @Override
    public void updateAPIForStateChange(String identifier, String newStatus, boolean deprecateOlderVersions, boolean
            requireReSubscriptions) throws
            APIManagementException {
        try {
            getApiDAO().changeLifeCycleStatus(identifier, newStatus);
            if (deprecateOlderVersions) {
                getApiDAO().deprecateOlderVersions(identifier);
            }
            if (!requireReSubscriptions) {
                getApiSubscriptionDAO().copySubscriptions(identifier);
            }

        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Couldn't change the API Status to " + newStatus, e, log);
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Couldn't change the API Status to " + newStatus, e, log);
        }
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

    /**
     * Save the thumbnail icon for api
     * @param apiId apiId of api
     * @param inputStream inputStream of image
     * @throws APIManagementException
     */
    @Override
    public void saveThumbnailImage(String apiId, InputStream inputStream) throws APIManagementException {
        try {
            getApiDAO().addThumbnailImage(apiId, inputStream);
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Couldn't save the thumbnail image", e, log);
        }
    }

    /**
     * Get the thumbnail icon for api
     *
     * @param apiId apiId of api
     * @throws APIManagementException
     */
    @Override
    public InputStream getThumbnailImage(String apiId) throws APIManagementException {
        try {
            return getApiDAO().getImage(apiId);
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Couldn't retrieve thumbnail for api " + apiId, e, log);
        }
        return null;
    }
}
