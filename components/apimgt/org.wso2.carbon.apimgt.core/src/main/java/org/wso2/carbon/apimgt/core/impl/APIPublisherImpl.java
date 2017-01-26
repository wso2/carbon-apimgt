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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIResource;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleHistoryBean;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of API Publisher operations
 */
public class APIPublisherImpl extends AbstractAPIManager implements APIPublisher {

    APIDefinition apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();

    private static final Logger log = LoggerFactory.getLogger(APIPublisherImpl.class);

    public APIPublisherImpl(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO, PolicyDAO policyDAO, APILifecycleManager apiLifecycleManager) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO, apiLifecycleManager);
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
            String errorMsg = "Unable to fetch APIs of " + providerName;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<String>
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
     * @return Set<String>
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
            subscriptionCount =  getApiSubscriptionDAO().getSubscriptionCountByAPI(id);
        } catch (APIMgtDAOException e) {
            log.error("Couldn't retrieve Subscriptions for API " + id, e, log);
            throw new APIManagementException("Couldn't retrieve Subscriptions for API " + id, e, ExceptionCodes
                    .SUBSCRIPTION_NOT_FOUND);
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

        apiBuilder.provider(getUsername());
        if (StringUtils.isEmpty(apiBuilder.getId())) {
            apiBuilder.id(UUID.randomUUID().toString());
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        apiBuilder.createdTime(localDateTime);
        apiBuilder.lastUpdatedTime(localDateTime);
        apiBuilder.createdBy(getUsername());
        try {
            if (!isApiNameExist(apiBuilder.getName()) && !isContextExist(apiBuilder.getContext())) {
                LifecycleState lifecycleState = getApiLifecycleManager().addLifecycle(APIMgtConstants.API_LIFECYCLE,
                        getUsername());
                apiBuilder.associateLifecycle(lifecycleState);
/*                APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(apiBuilder, apiResources);
                try {
                    String gatewayConfig = apiTemplateBuilder.getConfigStringForTemplate();
                    if (log.isDebugEnabled()) {
                        log.debug("API " + apiBuilder.getName() + "gateway config: " + gatewayConfig);
                    }
                    apiBuilder.gatewayConfig(new StringBuilder(gatewayConfig));
                } catch (APITemplateException e) {
                    log.error("Error generating API configuration for API " + apiBuilder.getName(), e);
                }*/
                Map<String, UriTemplate> uriTemplateMap = new HashMap();
                if (apiBuilder.getUriTemplates().isEmpty()) {
                    apiDefinitionFromSwagger20.setDefaultSwaggerDefinition(apiBuilder);
                } else {
                    for (UriTemplate uriTemplate : apiBuilder.getUriTemplates().values()) {
                        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder
                                (uriTemplate);
                        if (StringUtils.isEmpty(uriTemplateBuilder.getTemplateId())) {
                            uriTemplateBuilder.templateId(APIUtils.generateOperationIdFromPath(uriTemplate
                                    .getUriTemplate(), uriTemplate.getHttpVerb()));
                        }
                        if (uriTemplate.getEndpoint().isEmpty()) {
                            uriTemplateBuilder.endpoint(apiBuilder.getEndpoint());
                        }
                        uriTemplateMap.put(uriTemplateBuilder.getTemplateId(), uriTemplateBuilder.build());
                    }
                    apiBuilder.uriTemplates(uriTemplateMap);
                }
                if (StringUtils.isEmpty(apiBuilder.getApiDefinition())) {
                    apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
                }
                createdAPI = apiBuilder.build();
                APIUtils.validate(createdAPI);
                getApiDAO().addAPI(createdAPI);
                APIUtils.logDebug("API " + createdAPI.getName() + "-" + createdAPI.getVersion() + " was created " +
                        "successfully.", log);
            } else {
                String message = "Duplicate API already Exist with name/Context " + apiBuilder.getName();
                log.error(message);
                throw new APIManagementException(message, ExceptionCodes.API_ALREADY_EXISTS);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while creating the API - " + apiBuilder.getName();
            log.error(errorMsg);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Error occurred while Associating the API - " + apiBuilder.getName();
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
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
        apiBuilder.provider(getUsername());
        try {
            API originalAPI = getAPIbyUUID(apiBuilder.getId());
            if (originalAPI != null) {
                apiBuilder.createdTime(originalAPI.getCreatedTime());
                if ((originalAPI.getName().equals(apiBuilder.getName())) && (originalAPI.getVersion().equals
                        (apiBuilder.getVersion())) && (originalAPI.getProvider().equals(apiBuilder.getProvider())) &&
                        originalAPI.getLifeCycleStatus().equalsIgnoreCase(apiBuilder.getLifeCycleStatus())) {
                    API api = apiBuilder.build();
                    if (originalAPI.getContext() != null && !originalAPI.getContext().equals(apiBuilder.getContext())) {
                        if (!checkIfAPIContextExists(api.getContext())) {
                            getApiDAO().updateAPI(api.getId(), api);
                        } else {
                            throw new APIManagementException("Context already Exist", ExceptionCodes
                                    .API_ALREADY_EXISTS);
                        }
                    } else {
                        getApiDAO().updateAPI(api.getId(), api);
                    }
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

                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
                } else if (!originalAPI.getName().equals(apiBuilder.getName())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "API Name Change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
                } else if (!originalAPI.getContext().equals(apiBuilder.getContext())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "Context change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
                } else if (!originalAPI.getVersion().equals(apiBuilder.getVersion())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "Version change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
                } else if (!originalAPI.getProvider().equals(apiBuilder.getProvider())) {
                    String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as" +
                            " API have " +
                            "provider change";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
                }
            } else {

                log.error("Couldn't found API with ID " + apiBuilder.getId());
                throw new APIManagementException("Couldn't found API with ID " + apiBuilder.getId(),
                        ExceptionCodes.API_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating the API - " + apiBuilder.getName();
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
        boolean requireReSubscriptions = false;
        boolean deprecateOlderVersion = false;
        try {
            API api = getApiDAO().getAPI(apiId);
            if (api != null) {
                API.APIBuilder apiBuilder = new API.APIBuilder(api);
                apiBuilder.lifecycleState(getApiLifecycleManager().getCurrentLifecycleState(apiBuilder
                        .getLifecycleInstanceId()));
                for (Map.Entry<String, Boolean> checkListItem : checkListItemMap.entrySet()) {
                    if (APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS.equals(checkListItem.getKey())) {
                        deprecateOlderVersion = checkListItem.getValue();
                    } else if (APIMgtConstants.REQUIRE_RE_SUBSCRIPTIONS.equals(checkListItem.getKey())) {
                        requireReSubscriptions = checkListItem.getValue();
                    } else {
                        apiBuilder.lifecycleState(getApiLifecycleManager().checkListItemEvent(api.getLifecycleInstanceId
                                (), status, checkListItem.getKey(), checkListItem.getValue()));
                    }
                }
                API originalAPI = apiBuilder.build();
                getApiLifecycleManager().executeLifecycleEvent(status, apiBuilder
                        .getLifecycleInstanceId(), getUsername(), originalAPI);
                if (deprecateOlderVersion) {
                    if (StringUtils.isNotEmpty(api.getCopiedFromApiId())) {
                        API oldAPI = getApiDAO().getAPI(api.getCopiedFromApiId());
                        if (oldAPI != null) {
                            API.APIBuilder previousAPI = new API.APIBuilder(oldAPI);
                            previousAPI.setLifecycleStateInfo(getApiLifecycleManager().getCurrentLifecycleState
                                    (previousAPI.getLifecycleInstanceId()));
                            getApiLifecycleManager().executeLifecycleEvent(APIStatus.DEPRECATED.getStatus(), previousAPI
                                    .getLifecycleInstanceId(), getUsername(), previousAPI.build());
                        }
                    }
                }
                if (!requireReSubscriptions) {
                    if (StringUtils.isNotEmpty(api.getCopiedFromApiId())) {
                        List<Subscription> subscriptions = getApiSubscriptionDAO().getAPISubscriptionsByAPI(api
                                .getCopiedFromApiId());
                        List<Subscription> subscriptionList = new ArrayList<>();
                        for (Subscription subscription : subscriptions) {
                            if (api.getPolicies().contains(subscription.getSubscriptionTier())) {
                                if (!APIMgtConstants.SubscriptionStatus.ON_HOLD.equals(subscription.getStatus())) {
                                    subscriptionList.add(new Subscription(UUID.randomUUID().toString(), subscription
                                            .getApplication(), subscription.getApi(), subscription
                                            .getSubscriptionTier()));
                                }
                            }
                            getApiSubscriptionDAO().copySubscriptions(subscriptionList);
                        }
                    }
                }
            } else {
                throw new APIMgtResourceNotFoundException("Requested API " + apiId + " Not Available");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't change the status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't change the status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
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
                lifecycleState = getApiLifecycleManager().addLifecycle(APIMgtConstants.API_LIFECYCLE, getUsername());
                apiBuilder.associateLifecycle(lifecycleState);
                apiBuilder.copiedFromApiId(api.getId());
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
            String errorMsg = "Couldn't create new API version from " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't Associate  new API Lifecycle from " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        }
        return newVersionedId;
    }

    /**
     * Attach Documentation (without content) to an API
     *
     * @param apiId        UUID of API
     * @param documentInfo Document Summary
     * @return UUID of document
     * @throws APIManagementException if failed to add documentation
     */
    @Override
    public String addDocumentationInfo(String apiId, DocumentInfo documentInfo) throws APIManagementException {
        try {
            DocumentInfo.Builder docBuilder = new DocumentInfo.Builder(documentInfo);
            DocumentInfo document = null;
            if (StringUtils.isEmpty(docBuilder.getId())) {
                docBuilder = docBuilder.id(UUID.randomUUID().toString());
            }
            document = docBuilder.build();
            if (!getApiDAO().isDocumentExist(apiId, document)) {
                getApiDAO().addDocumentInfo(apiId, document);
                return document.getId();
            } else {
                String msg = "Document already exist for the api " + apiId;
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.DOCUMENT_ALREADY_EXISTS);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to add documentation";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Add a document (of source type FILE) with a file
     *
     * @param resourceId         UUID of API
     * @param content       content of the file as an Input Stream
     * @param fileName
     * @throws APIManagementException if failed to add the file
     */
    @Override
    public void uploadDocumentationFile(String resourceId, InputStream content, String fileName) throws
            APIManagementException {
        try {
            getApiDAO().addDocumentFileContent(resourceId, content, fileName);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to add documentation with file";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            getApiDAO().deleteDocument(docId);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to add documentation with file";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            String errorMsg = "Couldn't get APISummary for " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return status;
    }

    /**
     * Checks if a given API Context exists in the registry
     *
     * @param context
     * @return boolean result
     * @throws APIManagementException
     */
    @Override
    public boolean checkIfAPIContextExists(String context) throws APIManagementException {
        return isContextExist(context);
    }

    /**
     * Checks if a given API name exists in the registry
     *
     * @param name
     * @return boolean result
     * @throws APIManagementException
     */
    @Override
    public boolean checkIfAPINameExists(String name) throws APIManagementException {
        return isApiNameExist(name);
    }

    /**
     * This method used to save the documentation content
     *
     * @param docId
     * @param text  @throws APIManagementException if failed to add the document as a resource to registry
     */
    @Override
    public void addDocumentationContent(String docId, String text) throws APIManagementException {
        getApiDAO().addDocumentInlineContent(docId, text);
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
            String errorMsg = "Couldn't find APISummary Resource for ID " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't find APILifecycle History for ID " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
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
            String errorMsg = "Error occurred while deleting the API with id " + identifier;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Error occurred while Disassociating the API with Lifecycle id " + identifier;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
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
                apiResults = getApiDAO().searchAPIs(query, offset, limit);
            } else {
                apiResults = getApiDAO().getAPIs();
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while Searching the API with query " + query;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiResults;
    }

    /**
     * Update the subscription status
     *
     * @param subId     Subscription ID
     * @param subStatus Subscription Status
     * @return int value with subscription id
     * @throws APIManagementException If failed to update subscription status
     */
    @Override
    public void updateSubscriptionStatus(String subId, APIMgtConstants.SubscriptionStatus subStatus) throws
            APIManagementException {
        try {
            getApiSubscriptionDAO().updateSubscriptionStatus(subId, subStatus);
        } catch (APIMgtDAOException e) {
            throw new APIManagementException(e);
        }
    }

    /**
     * Update the subscription Policy
     *
     * @param subId     Subscription ID
     * @param newPolicy New Subscription Policy
     * @throws APIManagementException If failed to update subscription policy
     */
    @Override
    public void updateSubscriptionPolicy(String subId, String newPolicy) throws APIManagementException {
        try {
            getApiSubscriptionDAO().updateSubscriptionPolicy(subId, newPolicy);
        } catch (APIMgtDAOException e) {
            throw new APIManagementException(e);
        }
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
            API api = getAPIbyUUID(apiId);
            Map<String, UriTemplate> oldUriTemplateMap = api.getUriTemplates();
            List<APIResource> apiResourceList = apiDefinitionFromSwagger20.parseSwaggerAPIResources(new StringBuilder
                    (jsonText));
            Map<String, UriTemplate> updatedUriTemplateMap = new HashMap<>();
            for (APIResource apiResource : apiResourceList) {
                updatedUriTemplateMap.put(apiResource.getUriTemplate().getTemplateId(), apiResource.getUriTemplate());
            }
            Map<String, UriTemplate> uriTemplateMapNeedTobeUpdate = APIUtils.getMergedUriTemplates(oldUriTemplateMap,
                    updatedUriTemplateMap);
            API.APIBuilder apiBuilder = new API.APIBuilder(api);
            apiBuilder.uriTemplates(uriTemplateMapNeedTobeUpdate);
            getApiDAO().updateAPI(apiId, apiBuilder.build());
            getApiDAO().updateSwaggerDefinition(apiId, jsonText);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't update the Swagger Definition";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            String errorMsg = "Couldn't retrieve API Summary for " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't retrieve API Lifecycle for " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
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
    public void saveThumbnailImage(String apiId, InputStream inputStream, String dataType)
                                                                            throws APIManagementException {
        try {
            getApiDAO().updateImage(apiId, inputStream, dataType);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't save the thumbnail image";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            String errorMsg = "Couldn't retrieve thumbnail for api " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void updateApiGatewayConfig(String apiId, String configString) throws APIManagementException {
        //TODO implement logic here
    }

    @Override
    public String getApiGatewayConfig(String apiId) throws APIManagementException {
        try {
            return getApiDAO().getGatewayConfig(apiId);

        } catch (APIMgtDAOException e) {
            log.error("Couldn't retrieve swagger definition for apiId " + apiId, e);
            throw new APIMgtDAOException("Couldn't retrieve gateway configuration for apiId " + apiId,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Return list of endpoints
     *
     * @return
     * @throws APIManagementException
     */
    @Override
    public List<Endpoint> getAllEndpoints() throws APIManagementException {
        return getApiDAO().getEndpoints();
    }

    /**
     * Get endpoint details according to the endpointId
     *
     * @param endpointId uuid of endpoint
     * @return details of endpoint
     * @throws APIManagementException
     */
    @Override
    public Endpoint getEndpoint(String endpointId) throws APIManagementException {
        return getApiDAO().getEndpoint(endpointId);
    }

    /**
     * Add an endpoint
     *
     * @param endpoint
     * @throws APIManagementException
     */
    @Override
    public String addEndpoint(Endpoint endpoint) throws APIManagementException {
        Endpoint.Builder builder = new Endpoint.Builder(endpoint);
        builder.id(UUID.randomUUID().toString());
        Endpoint endpoint1 = builder.build();
        getApiDAO().addEndpoint(endpoint1);
        return endpoint1.getId();
    }

    /**
     * Update and endpoint
     *
     * @param endpoint
     * @throws APIManagementException
     */
    @Override
    public void updateEndpoint(Endpoint endpoint) throws APIManagementException {
        getApiDAO().updateEndpoint(endpoint);
    }

    /**
     * Delete an endpoint
     *
     * @param endpointId
     * @throws APIManagementException
     */
    @Override
    public void deleteEndpoint(String endpointId) throws APIManagementException {
        getApiDAO().deleteEndpoint(endpointId);
    }

    /**
     * Create api from Definition
     *
     * @param apiDefinition
     * @return
     * @throws APIManagementException
     */
    @Override
    public String addApiFromDefinition(InputStream apiDefinition) throws APIManagementException {
        try {
            String apiDefinitionString = IOUtils.toString(apiDefinition);
            API.APIBuilder apiBuilder = apiDefinitionFromSwagger20.generateApiFromSwaggerResource(getUsername(),
                    apiDefinitionString);
            apiBuilder.corsConfiguration(new CorsConfiguration());
            apiBuilder.apiDefinition(apiDefinitionString);
            addAPI(apiBuilder);
            return apiBuilder.getId();
        } catch (IOException e) {
            throw new APIManagementException("Couldn't Generate ApiDefinition from file", ExceptionCodes
                    .API_DEFINITION_MALFORMED);
        }
    }
}
