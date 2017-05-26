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
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.APIMObservable;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.api.GatewaySourceGenerator;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.template.APITemplateException;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.APILCWorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.workflow.APIStateChangeWorkflow;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleEventManager;
import org.wso2.carbon.lcm.core.impl.LifecycleState;
import org.wso2.carbon.lcm.sql.beans.LifecycleHistoryBean;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of API Publisher operations
 */
public class APIPublisherImpl extends AbstractAPIManager implements APIPublisher, APIMObservable {

    private static final Logger log = LoggerFactory.getLogger(APIPublisherImpl.class);

    // Map to store observers, which observe APIPublisher events
    private Map<String, EventObserver> eventObservers = new HashMap<>();

    public APIPublisherImpl(String username, IdentityProvider idp, ApiDAO apiDAO, ApplicationDAO applicationDAO,
                            APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO, APILifecycleManager
                                    apiLifecycleManager, LabelDAO labelDAO, WorkflowDAO workflowDAO, TagDAO tagDAO,
                            GatewaySourceGenerator gatewaySourceGenerator, APIGateway apiGatewayPublisher) {
        super(username, idp, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO, apiLifecycleManager, labelDAO,
                workflowDAO, tagDAO, gatewaySourceGenerator, apiGatewayPublisher);
    }

    /**
     * Returns a list of all #{@code org.wso2.carbon.apimgt.core.models.Provider} available on the system.
     *
     * @return {@code Set<Provider>}
     * @throws APIManagementException if failed to get Providers
     */
    @Override
    public Set<Provider> getAllProviders() throws APIManagementException {
        return null;
    }

    /**
     * Get a list of subscriptions for provider's APIs
     *
     * @param offset Starting index of the search results
     * @param limit Number of search results returned
     * @param providerName if of the provider
     * @return {@code List<Subscriber>} List of subscriptions for provider's APIs
     * @throws APIManagementException
     */
    @Override
    public List<Subscription> getSubscribersOfProvider(int offset, int limit, String providerName)
            throws APIManagementException {
        try {
            return getApiSubscriptionDAO().getAPISubscriptionsForUser(offset, limit, providerName);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to fetch subscriptions APIs of provider " + providerName;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
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
     * @return {@code Set<String>}
     * @throws APIManagementException if failed to get Subscribers
     */
    @Override
    public Set<String> getSubscribersOfAPI(API identifier) throws APIManagementException {
        return null;
    }

    /**
     * this method returns the {@code Set<APISubscriptionCount>} for given provider and api
     *
     * @param id String
     * @return {@code Set<APISubscriptionCount>}
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    @Override
    public long getAPISubscriptionCountByAPI(String id) throws APIManagementException {
        long subscriptionCount;
        try {
            subscriptionCount = getApiSubscriptionDAO().getSubscriptionCountByAPI(id);
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
     * @return UUID of the added API.
     * @throws APIManagementException if failed to add API
     */
    @Override
    public String addAPI(API.APIBuilder apiBuilder) throws APIManagementException {

        API createdAPI;
        APIGateway gateway = getApiGateway();

        apiBuilder.provider(getUsername());
        if (StringUtils.isEmpty(apiBuilder.getId())) {
            apiBuilder.id(UUID.randomUUID().toString());
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        apiBuilder.createdTime(localDateTime);
        apiBuilder.lastUpdatedTime(localDateTime);
        apiBuilder.createdBy(getUsername());
        apiBuilder.updatedBy(getUsername());
        Map<String, Endpoint> apiEndpointMap = apiBuilder.getEndpoint();
        if (apiEndpointMap != null) {
            for (Map.Entry<String, Endpoint> entry : apiEndpointMap.entrySet()) {
                if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(entry.getValue().getApplicableLevel())) {
                    Endpoint.Builder endpointBuilder = new Endpoint.Builder(entry.getValue());
                    if (StringUtils.isEmpty(endpointBuilder.getId())) {
                        endpointBuilder.id(UUID.randomUUID().toString());
                    }
                    if (StringUtils.isEmpty(endpointBuilder.getApplicableLevel())) {
                        endpointBuilder.applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT);
                    }
                    Endpoint endpoint = endpointBuilder.build();
                    try {
                        if (getApiDAO().getEndpointByName(endpoint.getName()) != null) {
                            String msg = "Endpoint Already Exist By Name : " + endpoint.getName();
                            throw new APIManagementException(msg, ExceptionCodes
                                    .ENDPOINT_ALREADY_EXISTS);
                        } else {
                            apiEndpointMap.replace(entry.getKey(), endpointBuilder.build());
                        }
                    } catch (APIMgtDAOException e) {
                        String msg = "Couldn't find Endpoint By Name : " + endpoint.getName();
                        log.error(msg, e);
                        throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                    }

                }
            }
        }
        try {
            if (!isApiNameExist(apiBuilder.getName()) && !isContextExist(apiBuilder.getContext())) {
                LifecycleState lifecycleState = getApiLifecycleManager().addLifecycle(APIMgtConstants.API_LIFECYCLE,
                        getUsername());
                apiBuilder.associateLifecycle(lifecycleState);

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
                        if (uriTemplate.getEndpoint() != null && !uriTemplate.getEndpoint().isEmpty()) {
                            for (Map.Entry<String, Endpoint> entry : uriTemplate.getEndpoint().entrySet()) {
                                if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(entry.getValue().getApplicableLevel
                                        ())) {
                                    Endpoint.Builder endpointBuilder = new Endpoint.Builder(entry.getValue
                                            ());
                                    if (StringUtils.isEmpty(endpointBuilder.getId())) {
                                        endpointBuilder.id(UUID.randomUUID().toString());
                                    }
                                    if (StringUtils.isEmpty(endpointBuilder.getApplicableLevel())) {
                                        endpointBuilder.applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT);
                                    }
                                    Endpoint endpoint = endpointBuilder.build();
                                    try {
                                        if (getApiDAO().getEndpointByName(endpoint.getName()) != null) {
                                            String msg = "Endpoint Already Exist By Name : " + endpoint.getName();
                                            throw new APIManagementException(msg, ExceptionCodes
                                                    .ENDPOINT_ALREADY_EXISTS);
                                        } else {
                                            uriTemplate.getEndpoint().replace(entry.getKey(), endpoint);
                                        }
                                    } catch (APIMgtDAOException e) {
                                        String msg = "Couldn't find Endpoint By Name : " + endpoint.getName();
                                        log.error(msg, e);
                                        throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                                    }
                                }
                            }
                        }
                        uriTemplateMap.put(uriTemplateBuilder.getTemplateId(), uriTemplateBuilder.build());
                    }
                    apiBuilder.uriTemplates(uriTemplateMap);
                }

                List<UriTemplate> list = new ArrayList<>(apiBuilder.getUriTemplates().values());
                List<TemplateBuilderDTO> resourceList = new ArrayList<>();

                for (UriTemplate uriTemplate : list) {
                    TemplateBuilderDTO dto = new TemplateBuilderDTO();
                    dto.setTemplateId(uriTemplate.getTemplateId());
                    dto.setUriTemplate(uriTemplate.getUriTemplate());
                    dto.setHttpVerb(uriTemplate.getHttpVerb());
                    dto.setAuthType(uriTemplate.getAuthType());
                    dto.setPolicy(uriTemplate.getPolicy());
                    Map<String, Endpoint> map = uriTemplate.getEndpoint();
                    if (map.containsKey(APIMgtConstants.PRODUCTION_ENDPOINT)) {
                        Endpoint endpoint = map.get(APIMgtConstants.PRODUCTION_ENDPOINT);
                        if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(endpoint.getApplicableLevel())) {
                            dto.setProductionEndpoint(endpoint.getName());
                        } else {
                            dto.setProductionEndpoint(getApiDAO().getEndpoint(endpoint.getId()).getName());
                        }
                    }
                    if (map.containsKey(APIMgtConstants.SANDBOX_ENDPOINT)) {
                        Endpoint endpoint = map.get(APIMgtConstants.SANDBOX_ENDPOINT);
                        if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(endpoint.getApplicableLevel())) {
                            dto.setSandboxEndpoint(endpoint.getName());
                        } else {
                            dto.setSandboxEndpoint(getApiDAO().getEndpoint(endpoint.getId()).getName());
                        }
                    }
                    resourceList.add(dto);
                }
                GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
                gatewaySourceGenerator.setAPI(apiBuilder.build());
                String gatewayConfig = gatewaySourceGenerator.getConfigStringFromTemplate(resourceList);
                if (log.isDebugEnabled()) {
                    log.debug("API " + apiBuilder.getName() + "gateway config: " + gatewayConfig);
                }
                apiBuilder.gatewayConfig(gatewayConfig);

                if (StringUtils.isEmpty(apiBuilder.getApiDefinition())) {
                    apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
                }
                if (apiBuilder.getPermission() != null && !("").equals(apiBuilder.getPermission())) {
                    HashMap roleNamePermissionList;
                    roleNamePermissionList = APIUtils.getAPIPermissionArray(apiBuilder.getPermission());
                    apiBuilder.permissionMap(roleNamePermissionList);
                }

                createdAPI = apiBuilder.build();
                APIUtils.validate(createdAPI);

                //Add API to gateway
                gateway.addAPI(createdAPI);

                Set<String> apiRoleList;
                //if the API has public visibility, add the API without any role checking
                //if the API has role based visibility, add the API with role checking
                if (API.Visibility.PUBLIC == createdAPI.getVisibility()) {
                    getApiDAO().addAPI(createdAPI);
                } else if (API.Visibility.RESTRICTED == createdAPI.getVisibility()) {
                    //get all the roles in the system
                    Set<String> allAvailableRoles = APIUtils.getAllAvailableRoles();
                    //get the roles needed to be associated with the API
                    apiRoleList = createdAPI.getVisibleRoles();
                    if (APIUtils.checkAllowedRoles(allAvailableRoles, apiRoleList)) {
                        getApiDAO().addAPI(createdAPI);
                    }
                }

                APIUtils.logDebug("API " + createdAPI.getName() + "-" + createdAPI.getVersion() + " was created " +
                        "successfully.", log);
                // 'API_M Functions' related code
                //Create a payload with event specific details
                Map<String, String> eventPayload = new HashMap<>();
                eventPayload.put(APIMgtConstants.FunctionsConstants.API_ID, createdAPI.getId());
                eventPayload.put(APIMgtConstants.FunctionsConstants.API_NAME, createdAPI.getName());
                eventPayload.put(APIMgtConstants.FunctionsConstants.API_VERSION, createdAPI.getVersion());
                eventPayload.put(APIMgtConstants.FunctionsConstants.API_DESCRIPTION, createdAPI.getDescription());
                eventPayload.put(APIMgtConstants.FunctionsConstants.API_CONTEXT, createdAPI.getContext());
                eventPayload.put(APIMgtConstants.FunctionsConstants.API_LC_STATUS, createdAPI.getLifeCycleStatus());
                eventPayload.put(APIMgtConstants.FunctionsConstants.API_PERMISSION, createdAPI.getApiPermission());
                // This will notify all the EventObservers(Asynchronous)
                ObserverNotifier observerNotifier = new ObserverNotifier(Event.API_CREATION, getUsername(),
                        ZonedDateTime.now(ZoneOffset.UTC), eventPayload, this);
                ObserverNotifierThreadPool.getInstance().executeTask(observerNotifier);
            } else {
                String message = "Duplicate API already Exist with name/Context " + apiBuilder.getName();
                log.error(message);
                throw new APIManagementException(message, ExceptionCodes.API_ALREADY_EXISTS);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while creating the API - " + apiBuilder.getName();
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException | ParseException e) {
            String errorMsg = "Error occurred while Associating the API - " + apiBuilder.getName();
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        } catch (APITemplateException e) {
            String message = "Error generating API configuration for API " + apiBuilder.getName();
            log.error(message, e);
            throw new APIManagementException(message, ExceptionCodes.TEMPLATE_EXCEPTION);
        } catch (GatewayException e) {
            String message = "Error occurred while adding API - " + apiBuilder.getName() + " to gateway";
            log.error(message, e);
            throw new APIManagementException(message, ExceptionCodes.GATEWAY_EXCEPTION);
        }
        return apiBuilder.getId();
    }

    /**
     * @param api API Object
     * @return If api definition is valid or not.
     * @throws APIManagementException If failed to validate the API.
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
     * @param apiBuilder {@code org.wso2.carbon.apimgt.core.models.API.APIBuilder} model object
     * @throws APIManagementException if failed to update API
     */
    @Override
    public void updateAPI(API.APIBuilder apiBuilder) throws APIManagementException {
        APIGateway gateway = getApiGateway();

        apiBuilder.provider(getUsername());
        apiBuilder.updatedBy(getUsername());
        try {
            API originalAPI = getAPIbyUUID(apiBuilder.getId());
            if (originalAPI != null) {
                apiBuilder.createdTime(originalAPI.getCreatedTime());
                //workflow status is an internal property and shouldn't be allowed to update externally
                apiBuilder.workflowStatus(originalAPI.getWorkflowStatus());
                if ((originalAPI.getName().equals(apiBuilder.getName())) && (originalAPI.getVersion().equals
                        (apiBuilder.getVersion())) && (originalAPI.getProvider().equals(apiBuilder.getProvider())) &&
                        originalAPI.getLifeCycleStatus().equalsIgnoreCase(apiBuilder.getLifeCycleStatus())) {

                    if (apiBuilder.getPermission() != null && !("").equals(apiBuilder.getPermission())) {
                        HashMap roleNamePermissionList;
                        roleNamePermissionList = APIUtils.getAPIPermissionArray(apiBuilder.getPermission());
                        apiBuilder.permissionMap(roleNamePermissionList);
                    }

                    String updatedSwagger = apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder);
                    String gatewayConfig = getApiGatewayConfig(apiBuilder.getId());
                    GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
                    gatewaySourceGenerator.setAPI(apiBuilder.build());
                    String updatedGatewayConfig = gatewaySourceGenerator
                            .getGatewayConfigFromSwagger(gatewayConfig, updatedSwagger);

                    API api = apiBuilder.build();

                    //Add API to gateway
                    gateway.updateAPI(api);

                    if (originalAPI.getContext() != null && !originalAPI.getContext().equals(apiBuilder.getContext())) {
                        if (!checkIfAPIContextExists(api.getContext())) {
                            //if the API has public visibility, update the API without any role checking
                            if (API.Visibility.PUBLIC == api.getVisibility()) {
                                getApiDAO().updateAPI(api.getId(), api);
                            } else if (API.Visibility.RESTRICTED == api.getVisibility()) {
                                //get all the roles in the system
                                Set<String> availableRoles = APIUtils.getAllAvailableRoles();
                                //get the roles needed to be associated with the API
                                Set<String> apiRoleList = api.getVisibleRoles();
                                //if the API has role based visibility, update the API with role checking
                                if (APIUtils.checkAllowedRoles(availableRoles, apiRoleList)) {
                                    getApiDAO().updateAPI(api.getId(), api);
                                }
                            }
                            getApiDAO().updateSwaggerDefinition(api.getId(), updatedSwagger, api.getUpdatedBy());
                            getApiDAO().updateGatewayConfig(api.getId(), updatedGatewayConfig, api.getUpdatedBy());
                        } else {
                            throw new APIManagementException("Context already Exist", ExceptionCodes
                                    .API_ALREADY_EXISTS);
                        }
                    } else {
                        //if the API has public visibility, update the API without any role checking
                        if (API.Visibility.PUBLIC == api.getVisibility()) {
                            getApiDAO().updateAPI(api.getId(), api);
                        } else if (API.Visibility.RESTRICTED == api.getVisibility()) {
                            //get all the roles in the system
                            Set<String> allAvailableRoles = APIUtils.getAllAvailableRoles();
                            //get the roles needed to be associated with the API
                            Set<String> apiRoleList = api.getVisibleRoles();
                            //if the API has role based visibility, update the API with role checking
                            if (APIUtils.checkAllowedRoles(allAvailableRoles, apiRoleList)) {
                                getApiDAO().updateAPI(api.getId(), api);
                            }
                        }
                        getApiDAO().updateSwaggerDefinition(api.getId(), updatedSwagger, api.getUpdatedBy());
                        getApiDAO().updateGatewayConfig(api.getId(), updatedGatewayConfig, api.getUpdatedBy());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("API " + api.getName() + "-" + api.getVersion() + " was updated successfully.");
                        // 'API_M Functions' related code
                        //Create a payload with event specific details
                        Map<String, String> eventPayload = new HashMap<>();
                        eventPayload.put(APIMgtConstants.FunctionsConstants.API_ID, api.getId());
                        eventPayload.put(APIMgtConstants.FunctionsConstants.API_NAME, api.getName());
                        eventPayload.put(APIMgtConstants.FunctionsConstants.API_VERSION, api.getVersion());
                        eventPayload.put(APIMgtConstants.FunctionsConstants.API_DESCRIPTION, api.getDescription());
                        eventPayload.put(APIMgtConstants.FunctionsConstants.API_CONTEXT, api.getContext());
                        eventPayload.put(APIMgtConstants.FunctionsConstants.API_LC_STATUS, api.getLifeCycleStatus());
                        // This will notify all the EventObservers(Asynchronous)
                        ObserverNotifier observerNotifier = new ObserverNotifier(Event.API_UPDATE, getUsername(),
                                ZonedDateTime.now(ZoneOffset.UTC), eventPayload, this);
                        ObserverNotifierThreadPool.getInstance().executeTask(observerNotifier);
                    }
                } else {
                    APIUtils.verifyValidityOfApiUpdate(apiBuilder, originalAPI);
                }
            } else {

                log.error("Couldn't found API with ID " + apiBuilder.getId());
                throw new APIManagementException("Couldn't found API with ID " + apiBuilder.getId(),
                        ExceptionCodes.API_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating the API - " + apiBuilder.getName();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (ParseException e) {
            String errorMsg = "Error occurred while parsing the permission json from swagger - " + apiBuilder.getName();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.SWAGGER_PARSE_EXCEPTION);
        } catch (GatewayException e) {
            String message = "Error occurred while updating API - " + apiBuilder.getName() + " in gateway";
            log.error(message, e);
            throw new APIManagementException(message, ExceptionCodes.GATEWAY_EXCEPTION);
        }
    }



    /**
     * @see org.wso2.carbon.apimgt.core.api.APIPublisher#updateAPIStatus(String, String, Map)
     */
    @Override
    public WorkflowResponse updateAPIStatus(String apiId, String status, Map<String, Boolean> checkListItemMap) throws
            APIManagementException {
        WorkflowResponse workflowResponse = null;
        try {
            API api = getApiDAO().getAPI(apiId);           
            if (api != null && !APILCWorkflowStatus.PENDING.toString().equals(api.getWorkflowStatus())) {
                API.APIBuilder apiBuilder = new API.APIBuilder(api);
                apiBuilder.lastUpdatedTime(LocalDateTime.now());
                apiBuilder.updatedBy(getUsername());
                LifecycleState currentState = getApiLifecycleManager().getLifecycleDataForState(apiBuilder
                        .getLifecycleInstanceId(), apiBuilder.getLifeCycleStatus());
                apiBuilder.lifecycleState(currentState);
                for (Map.Entry<String, Boolean> checkListItem : checkListItemMap.entrySet()) {
                    getApiLifecycleManager().checkListItemEvent(api.getLifecycleInstanceId(), api.getLifeCycleStatus(),
                            checkListItem.getKey(), checkListItem.getValue());
                }
                API originalAPI = apiBuilder.build();
                WorkflowExecutor executor = WorkflowExecutorFactory.getInstance()
                        .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE);
                APIStateChangeWorkflow workflow = new APIStateChangeWorkflow(getApiDAO(), getApiSubscriptionDAO(),
                        getWorkflowDAO(), getApiLifecycleManager());
                workflow.setApiName(originalAPI.getName());
                workflow.setApiProvider(originalAPI.getProvider());
                workflow.setApiVersion(originalAPI.getVersion());
                workflow.setCurrentState(currentState.getState());
                workflow.setTransitionState(status);

                workflow.setWorkflowReference(originalAPI.getId());
                workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
                workflow.setCreatedTime(LocalDateTime.now());
                workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_API_STATE);
                workflow.setInvoker(getUsername());
                
                //setting attributes for internal use. These are set to use from outside the executor's method
                //these will be saved in the AM_WORKFLOW table so these can be retrieved later
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_API_CUR_STATE, currentState.getState());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_API_TARGET_STATE, status);
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_API_LC_INVOKER, getUsername());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_API_LAST_UPTIME,
                        originalAPI.getLastUpdatedTime().toString());
                String workflowDescription = "API state change workflow for " + workflow.getApiName() + ":"
                        + workflow.getApiVersion() + ":" + workflow.getApiProvider() + " from "
                        + workflow.getCurrentState() + " to " + workflow.getTransitionState() + " state by "
                        + getUsername();
                workflow.setWorkflowDescription(workflowDescription);
                workflowResponse = executor.execute(workflow);
                workflow.setStatus(workflowResponse.getWorkflowStatus());

                if (WorkflowStatus.CREATED != workflowResponse.getWorkflowStatus()) {
                    completeWorkflow(executor, workflow);
                } else {
                    //add entry to workflow table if it is only in pending state
                    addWorkflowEntries(workflow);
                    getApiDAO().updateAPIWorkflowStatus(api.getId(), APILCWorkflowStatus.PENDING);
                }
            } else if (api != null && APILCWorkflowStatus.PENDING.toString().equals(api.getWorkflowStatus())) {
                String message = "Pending state transition for api :" + api.getName();
                log.error(message);
                throw new APIManagementException(message, ExceptionCodes.WORKFLOW_PENDING);
            } else {
                throw new APIMgtResourceNotFoundException("Requested API " + apiId + " Not Available");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't change the status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't change the status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        }
        return workflowResponse;
    }

    /**
     * This method used to Update the lifecycle checklist of API
     *
     * @param apiId            UUID of the API
     * @param status           Current API lifecycle status.
     * @param checkListItemMap Check list item values.
     * @throws APIManagementException If failed to update checklist item values.
     */
    @Override
    public void updateCheckListItem(String apiId, String status, Map<String, Boolean> checkListItemMap)
            throws APIManagementException {
        API api = getApiDAO().getAPI(apiId);
        try {
            if (api != null) {
                API.APIBuilder apiBuilder = new API.APIBuilder(api);
                apiBuilder.lastUpdatedTime(LocalDateTime.now());
                apiBuilder.updatedBy(getUsername());
                apiBuilder.lifecycleState(getApiLifecycleManager()
                        .getLifecycleDataForState(apiBuilder.getLifecycleInstanceId(),
                                apiBuilder.getLifeCycleStatus()));
                for (Map.Entry<String, Boolean> checkListItem : checkListItemMap.entrySet()) {
                    getApiLifecycleManager().checkListItemEvent(api.getLifecycleInstanceId
                                    (), api.getLifeCycleStatus(),
                            checkListItem.getKey(), checkListItem.getValue());
                }
            }
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't get the lifecycle status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        }
    }


    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param apiId      The API to be copied
     * @param newVersion The version of the new API
     * @return UUID of the newly created version.
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    @Override
    public String createNewAPIVersion(String apiId, String newVersion) throws APIManagementException {
        String newVersionedId;
        LifecycleState lifecycleState;
        // validate parameters
        if (StringUtils.isEmpty(newVersion)) {
            String errorMsg = "New API version cannot be empty";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        if (StringUtils.isEmpty(apiId)) {
            String errorMsg = "API ID cannot be empty";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        try {
            API api = getApiDAO().getAPI(apiId);
            if (api != null) {
                if (api.getVersion().equals(newVersion)) {
                    String errMsg = "New API version " + newVersion + " cannot be same as the previous version for " +
                            "API " + api.getName();
                    log.error(errMsg);
                    throw new APIManagementException(errMsg, ExceptionCodes.API_ALREADY_EXISTS);
                }
                API.APIBuilder apiBuilder = new API.APIBuilder(api);
                apiBuilder.id(UUID.randomUUID().toString());
                apiBuilder.version(newVersion);
                apiBuilder.context(api.getContext().replace(api.getVersion(), newVersion));
                lifecycleState = getApiLifecycleManager().addLifecycle(APIMgtConstants.API_LIFECYCLE, getUsername());
                apiBuilder.associateLifecycle(lifecycleState);
                apiBuilder.copiedFromApiId(api.getId());
                if (StringUtils.isEmpty(apiBuilder.getApiDefinition())) {
                    apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
                }
                getApiDAO().addAPI(apiBuilder.build());
                newVersionedId = apiBuilder.getId();
            } else {
                throw new APIMgtResourceNotFoundException("Requested API on UUID " + apiId + "Couldn't be found");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't create new API version from " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            LocalDateTime localDateTime = LocalDateTime.now();
            DocumentInfo document;
            DocumentInfo.Builder docBuilder = new DocumentInfo.Builder(documentInfo);
            docBuilder.createdBy(getUsername());
            docBuilder.updatedBy(getUsername());
            docBuilder.createdTime(localDateTime);
            docBuilder.lastUpdatedTime(localDateTime);
            if (StringUtils.isEmpty(docBuilder.getId())) {
                docBuilder = docBuilder.id(UUID.randomUUID().toString());
            }

            if (documentInfo.getPermission() != null && !("").equals(documentInfo.getPermission())) {
                HashMap roleNamePermissionList;
                roleNamePermissionList = APIUtils.getAPIPermissionArray(documentInfo.getPermission());
                docBuilder.permissionMap(roleNamePermissionList);
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
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (ParseException e) {
            String errorMsg = "Unable to add documentation due to json parse error";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.JSON_PARSE_ERROR);
        }
    }

    /**
     * Add a document (of source type FILE) with a file
     *
     * @param resourceId UUID of API
     * @param content    content of the file as an Input Stream
     * @param dataType   File mime type
     * @throws APIManagementException if failed to add the file
     */
    @Override
    public void uploadDocumentationFile(String resourceId, InputStream content, String dataType) throws
            APIManagementException {
        try {
            getApiDAO().addDocumentFileContent(resourceId, content, dataType, getUsername());
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
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Checks if a given API Context exists in the registry
     *
     * @param context Context of the API.
     * @return boolean result
     * @throws APIManagementException If failed to check if API exist.
     */
    @Override
    public boolean checkIfAPIContextExists(String context) throws APIManagementException {
        return isContextExist(context);
    }

    /**
     * Checks if a given API name exists in the registry
     *
     * @param name Name of the API.
     * @return boolean result
     * @throws APIManagementException If failed to check if API exist.
     */
    @Override
    public boolean checkIfAPINameExists(String name) throws APIManagementException {
        return isApiNameExist(name);
    }

    /**
     * This method used to save the documentation content
     *
     * @param docId UUID of the Doc
     * @param text  Plain text content of the doc.
     * @throws APIManagementException if failed to add the document as a resource to registry
     */
    @Override
    public void addDocumentationContent(String docId, String text) throws APIManagementException {
        getApiDAO().addDocumentInlineContent(docId, text, getUsername());
    }

    /**
     * Updates a given documentation
     *
     * @param apiId        UUID of the API.
     * @param documentInfo Documentation
     * @return UUID of the updated document.
     * @throws APIManagementException if failed to update docs
     */
    @Override
    public String updateDocumentation(String apiId, DocumentInfo documentInfo) throws APIManagementException {
        try {
            LocalDateTime localDateTime = LocalDateTime.now();
            DocumentInfo document;
            DocumentInfo.Builder docBuilder = new DocumentInfo.Builder(documentInfo);
            docBuilder.updatedBy(getUsername());
            docBuilder.lastUpdatedTime(localDateTime);
            if (StringUtils.isEmpty(docBuilder.getId())) {
                docBuilder = docBuilder.id(UUID.randomUUID().toString());
            }

            if (documentInfo.getPermission() != null && !("").equals(documentInfo.getPermission())) {
                HashMap roleNamePermissionList;
                roleNamePermissionList = APIUtils.getAPIPermissionArray(documentInfo.getPermission());
                docBuilder.permissionMap(roleNamePermissionList);
            }

            document = docBuilder.build();

            if (getApiDAO().isDocumentExist(apiId, document)) {
                getApiDAO().updateDocumentInfo(apiId, document, getUsername());
                return document.getId();
            } else {
                String msg = "Document " + document.getName() + " not found for the api " + apiId;
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.DOCUMENT_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to update the documentation";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (ParseException e) {
            String errorMsg = "Unable to update the documentation due to json parse error";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.JSON_PARSE_ERROR);
        }
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
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't find APILifecycle History for ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        }
        return lifeCycleEventList;
    }

    /**
     * Delete an API
     *
     * @param identifier UUID of the API.
     * @throws APIManagementException if failed to remove the API
     */
    @Override
    public void deleteAPI(String identifier) throws APIManagementException {
        APIGateway gateway = getApiGateway();
        try {
            if (getAPISubscriptionCountByAPI(identifier) == 0) {
                API api = getApiDAO().getAPI(identifier);
                if (api != null) {
                    String apiWfStatus = api.getWorkflowStatus();
                    API.APIBuilder apiBuilder = new API.APIBuilder(api);

                    //Delete API in gateway

                    gateway.deleteAPI(api);
                    getApiDAO().deleteAPI(identifier);
                    getApiLifecycleManager().removeLifecycle(apiBuilder.getLifecycleInstanceId());
                    APIUtils.logDebug("API with id " + identifier + " was deleted successfully.", log);
                    
                    if (APILCWorkflowStatus.PENDING.toString().equals(apiWfStatus)) {
                       cleanupPendingTaskForAPIStateChange(identifier);
                    }
                    // 'API_M Functions' related code
                    //Create a payload with event specific details
                    Map<String, String> eventPayload = new HashMap<>();
                    eventPayload.put(APIMgtConstants.FunctionsConstants.API_ID, api.getId());
                    eventPayload.put(APIMgtConstants.FunctionsConstants.API_NAME, api.getName());
                    eventPayload.put(APIMgtConstants.FunctionsConstants.API_VERSION, api.getVersion());
                    eventPayload.put(APIMgtConstants.FunctionsConstants.API_PROVIDER, api.getProvider());
                    eventPayload.put(APIMgtConstants.FunctionsConstants.API_DESCRIPTION, api.getDescription());
                    // This will notify all the EventObservers(Asynchronous)
                    ObserverNotifier observerNotifier = new ObserverNotifier(Event.API_DELETION, getUsername(),
                            ZonedDateTime.now(ZoneOffset.UTC), eventPayload, this);
                    ObserverNotifierThreadPool.getInstance().executeTask(observerNotifier);
                }
            } else {
                throw new ApiDeleteFailureException("API with " + identifier + " already have subscriptions");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting the API with id " + identifier;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Error occurred while Disassociating the API with Lifecycle id " + identifier;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        } catch (GatewayException e) {
            String message = "Error occurred while deleting API with id - " + identifier + " from gateway";
            log.error(message, e);
            throw new APIManagementException(message, ExceptionCodes.GATEWAY_EXCEPTION);
        }
    }

    /**
     * @param limit  Limit
     * @param offset Offset
     * @param query  Search query
     * @return List of APIS.
     * @throws APIManagementException If failed to formatApiSearch APIs.
     */
    @Override
    public List<API> searchAPIs(Integer limit, Integer offset, String query) throws APIManagementException {

        List<API> apiResults;
        try {
            //TODO: Need to validate users roles against results returned
            if (query != null && !query.isEmpty()) {
                String user = "admin";
                Set<String> roles = APIUtils.getAllRolesOfUser(user);
                //TODO get the logged in user and user roles from key manager.
                apiResults = getApiDAO().searchAPIs(roles, user, query, ApiType.STANDARD, offset, limit);
            } else {
                apiResults = getApiDAO().getAPIs(ApiType.STANDARD);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while Searching the API with query " + query;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiResults;
    }

    /**
     * Update the subscription status
     *
     * @param subId     Subscription ID
     * @param subStatus Subscription Status
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
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId String
     * @return {@code Map<String,Object>} a map with lifecycle data
     * @throws APIManagementException If failed to get Lifecycle data.
     */
    @Override
    public LifecycleState getAPILifeCycleData(String apiId) throws APIManagementException {
        try {
            API api = getApiDAO().getAPISummary(apiId);
            if (api != null) {
                return getApiLifecycleManager()
                        .getLifecycleDataForState(api.getLifecycleInstanceId(), api.getLifeCycleStatus());
            } else {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve API Summary for " + apiId);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't retrieve API Summary for " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't retrieve API Lifecycle for " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        }
    }

    /**
     * Get the current lifecycle status of the api
     *
     * @param string Api identifier
     * @return Current lifecycle status
     * @throws APIManagementException If failed to get lifecycle status.
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
     * Return list of endpoints
     *
     * @return List of Endpoints..
     * @throws APIManagementException If filed to get endpoints.
     */
    @Override
    public List<Endpoint> getAllEndpoints() throws APIManagementException {
        try {
            return getApiDAO().getEndpoints();
        } catch (APIMgtDAOException e) {
            String msg = "Failed to get all Endpoints";
            log.error(msg, e);
            throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

    }

    /**
     * Get endpoint details according to the endpointId
     *
     * @param endpointId uuid of endpoint
     * @return details of endpoint
     * @throws APIManagementException If failed to get endpoint.
     */
    @Override
    public Endpoint getEndpoint(String endpointId) throws APIManagementException {
        try {
            return getApiDAO().getEndpoint(endpointId);
        } catch (APIMgtDAOException e) {
            String msg = "Failed to get Endpoint : " + endpointId;
            log.error(msg, e);
            throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public Endpoint getEndpointByName(String endpointName) throws APIManagementException {
        try {
            return getApiDAO().getEndpointByName(endpointName);
        } catch (APIMgtDAOException e) {
            String msg = "Failed to get Endpoint : " + endpointName;
            log.error(msg, e);
            throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Add an endpoint
     *
     * @param endpoint Endpoint object.
     * @return UUID of the added endpoint.
     * @throws APIManagementException If failed to add endpoint.
     */
    @Override
    public String addEndpoint(Endpoint endpoint) throws APIManagementException {

        APIGateway gateway = getApiGateway();

        Endpoint.Builder builder = new Endpoint.Builder(endpoint);
        builder.id(UUID.randomUUID().toString());
        builder.applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT);
        Endpoint endpoint1 = builder.build();
        String key = endpoint.getName();
        if (key == null || StringUtils.isEmpty(key)) {
            log.error("Endpoint name not provided");
            throw new APIManagementException("Endpoint name is not provided", ExceptionCodes.ENDPOINT_ADD_FAILED);
        }
        Endpoint endpoint2 = getApiDAO().getEndpointByName(endpoint.getName());
        if (endpoint2 != null) {
            log.error("Endpoint already exist with name " + key);
            throw new APIManagementException("Endpoint already exist with name " + key,
                    ExceptionCodes.ENDPOINT_ALREADY_EXISTS);
        }
        gateway.addEndpoint(endpoint1);

        try {

            getApiDAO().addEndpoint(endpoint1);
        } catch (APIMgtDAOException e) {
            String msg = "Failed to add Endpoint : " + endpoint.getName();
            log.error(msg, e);
            throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        //update endpoint config in gateway
        return endpoint1.getId();
    }

    /**
     * Update and endpoint
     *
     * @param endpoint Endpoint object.
     * @throws APIManagementException If failed to update endpoint.
     */
    @Override
    public void updateEndpoint(Endpoint endpoint) throws APIManagementException {
        APIGateway gateway = getApiGateway();
        gateway.updateEndpoint(endpoint);

        try {
            getApiDAO().updateEndpoint(endpoint);
        } catch (APIMgtDAOException e) {
            String msg = "Failed to update Endpoint : " + endpoint.getName();
            log.error(msg, e);
            throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        //update endpoint config in gateway
    }

    /**
     * Delete an endpoint
     *
     * @param endpointId UUID of the endpoint.
     * @throws APIManagementException If failed to delete the endpoint.
     */
    @Override
    public void deleteEndpoint(String endpointId) throws APIManagementException {
        APIGateway gateway = getApiGateway();

        Endpoint endpoint = getEndpoint(endpointId);
        if (!getApiDAO().isEndpointAssociated(endpointId)) {
            try {

                getApiDAO().deleteEndpoint(endpointId);
            } catch (APIMgtDAOException e) {
                String msg = "Failed to delete Endpoint : " + endpointId;
                log.error(msg, e);
                throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
            gateway.deleteEndpoint(endpoint);

        } else {
            String msg = "Endpoint Already Have Associated With API";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.ENDPOINT_DELETE_FAILED);
        }
    }

    /**
     * Create api from Definition
     *
     * @param apiDefinition API definition stream.
     * @return UUID of the added API.
     * @throws APIManagementException If failed to add the API.
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

    @Override
    public String addApiFromDefinition(HttpURLConnection urlConn) throws APIManagementException {
        try {
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod(APIMgtConstants.HTTP_GET);
            urlConn.connect();
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                return addApiFromDefinition(urlConn.getInputStream());
            } else {
                throw new APIManagementException("Error while getting swagger resource from url : " + urlConn.getURL(),
                        ExceptionCodes.API_DEFINITION_MALFORMED);
            }
        } catch (ProtocolException e) {
            String msg = "Protocol exception while getting the swagger resource from url";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.API_DEFINITION_MALFORMED);
        }  catch (IOException e) {
            String msg = "Error while getting the swagger resource from url";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.API_DEFINITION_MALFORMED);
        }
    }

    /**
     * Retrieve all policies based on tier Level
     *
     * @param tierLevel Tier Level.
     * @return List of policies of the given level.
     * @throws APIManagementException If failed to get policies.
     */

    @Override
    public List<Policy> getAllPoliciesByLevel(String tierLevel) throws APIManagementException {
        return getPolicyDAO().getPolicies(tierLevel);
    }


    @Override
    public Policy getPolicyByName(String tierLevel, String tierName) throws APIManagementException {
        return getPolicyDAO().getPolicy(tierLevel, tierName);
    }

    @Override
    public List<LifecycleHistoryBean> getLifeCycleHistoryFromUUID(String uuid)
            throws APIManagementException {

        LifecycleEventManager lifecycleEventManager = new LifecycleEventManager();
        try {
            return lifecycleEventManager.getLifecycleHistoryFromId(uuid);
        } catch (LifecycleException e) {
            String errorMsg = "Error while retrieving the lifecycle history of the API ";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        }
    }

    @Override
    public List<Label> getAllLabels() throws LabelException {

        try {
            return getLabelDAO().getLabels();
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while retrieving labels";
            log.error(msg, e);
            throw new LabelException(msg, ExceptionCodes.LABEL_EXCEPTION);
        }
    }

    /**
     * @see APIPublisher#getLastUpdatedTimeOfEndpoint(String)
     */
    @Override
    public String getLastUpdatedTimeOfEndpoint(String endpointId) throws APIManagementException {
        String lastUpdatedTime;
        try {
            lastUpdatedTime = getApiDAO().getLastUpdatedTimeOfEndpoint(endpointId);
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while retrieving the last update time of the endpoint with id " + endpointId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return lastUpdatedTime;
    }

    /**
     * Add {@code org.wso2.carbon.apimgt.core.api.EventObserver} which needs to be registered to a Map.
     * Key should be class name of the observer. This is to prevent registering same observer twice to an
     * observable.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void registerObserver(EventObserver observer) {
        if (observer != null && !eventObservers.containsKey(observer.getClass().getName())) {
            eventObservers.put(observer.getClass().getName(), observer);
        }
    }

    /**
     * Notify each registered {@link org.wso2.carbon.apimgt.core.api.EventObserver}.
     * This calls
     * {@link org.wso2.carbon.apimgt.core.api.EventObserver#captureEvent(Event, String, ZonedDateTime, Map)}
     * method of that {@link org.wso2.carbon.apimgt.core.api.EventObserver}.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyObservers(Event event, String username, ZonedDateTime eventTime,
                                Map<String, String> metaData) {

        Set<Map.Entry<String, EventObserver>> eventObserverEntrySet = eventObservers.entrySet();
        eventObserverEntrySet.forEach(eventObserverEntry -> eventObserverEntry.getValue().captureEvent(event,
                username, eventTime, metaData));
    }

    /**
     * Remove {@link org.wso2.carbon.apimgt.core.api.EventObserver} from the Map, which stores observers to be
     * notified.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void removeObserver(EventObserver observer) {
        if (observer != null) {
            eventObservers.remove(observer.getClass().getName());
        }
    }

    /**
     * To get the Map of all observers, which registered to {@link org.wso2.carbon.apimgt.core.api.APIPublisher}.
     *
     * @return Map of observers.
     */
    public Map<String, EventObserver> getEventObservers() {
        return eventObservers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePendingLifecycleWorkflowTaskForAPI(String apiId) throws APIManagementException {
        
        API api = getApiDAO().getAPI(apiId);
        if (api != null) {
            if (APILCWorkflowStatus.PENDING.toString().equals(api.getWorkflowStatus())) {
                try {
                    //change the state back
                    getApiDAO().updateAPIWorkflowStatus(apiId, APILCWorkflowStatus.APPROVED);
                    
                    // call executor's cleanup task
                    cleanupPendingTaskForAPIStateChange(apiId);
                    
                } catch (APIMgtDAOException e) {
                    String msg = "Error occurred while changing api lifecycle workflow status";
                    log.error(msg, e);
                    throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            } else {
                String msg = "API does not have a pending lifecycle state change.";
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.WORKFLOW_NO_PENDING_TASK);
            }
        } else {
            String msg = "Couldn't found API with ID " + apiId;
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
        }      
    }
    
    private void cleanupPendingTaskForAPIStateChange(String apiId) throws APIManagementException {
        String workflowExtRef = getWorkflowDAO().getExternalWorkflowReferenceForPendingTask(apiId,
                WorkflowConstants.WF_TYPE_AM_API_STATE);
        if (workflowExtRef != null) {
            WorkflowExecutor executor = WorkflowExecutorFactory.getInstance()
                    .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE);
            try {
                executor.cleanUpPendingTask(workflowExtRef);
            } catch (WorkflowException e) {
                String warn = "Failed to clean pending api state change task for " + apiId;
                // failed cleanup processes are ignored to prevent failing the deletion process
                log.warn(warn, e.getLocalizedMessage());
            }
            getWorkflowDAO().deleteWorkflowEntryforExternalReference(workflowExtRef);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEndpointExist(String name) throws APIManagementException {
        try {
            return getApiDAO().isEndpointExist(name);
        } catch (APIMgtDAOException e) {
            String msg = "Couldn't find existence of endpoint :" + name;
            throw new APIManagementException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }
}
