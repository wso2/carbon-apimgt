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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.APIMObservable;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.api.GatewaySourceGenerator;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.api.WSDLProcessor;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.configuration.models.NotificationConfigurations;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.exception.NotificationException;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.executors.NotificationExecutor;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIResource;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.WSDLArchiveInfo;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.template.APIConfigContext;
import org.wso2.carbon.apimgt.core.template.APITemplateException;
import org.wso2.carbon.apimgt.core.template.dto.NotificationDTO;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMWSDLUtils;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of API Publisher operations
 */
public class APIPublisherImpl extends AbstractAPIManager implements APIPublisher, APIMObservable {

    private static final Logger log = LoggerFactory.getLogger(APIPublisherImpl.class);

    // Map to store observers, which observe APIPublisher events
    private Map<String, EventObserver> eventObservers = new HashMap<>();

    public APIPublisherImpl(String username, IdentityProvider idp, KeyManager keyManager, ApiDAO apiDAO,
                            ApplicationDAO applicationDAO, APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO,
                            APILifecycleManager apiLifecycleManager, LabelDAO labelDAO, WorkflowDAO workflowDAO,
                            TagDAO tagDAO, GatewaySourceGenerator gatewaySourceGenerator,
                            APIGateway apiGatewayPublisher) {
        super(username, idp, keyManager, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO, apiLifecycleManager,
                labelDAO, workflowDAO, tagDAO, gatewaySourceGenerator, apiGatewayPublisher);
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
     * @param offset       Starting index of the search results
     * @param limit        Number of search results returned
     * @param providerName if of the provider
     * @return {@code List<Subscriber>} List of subscriptions for provider's APIs
     * @throws APIManagementException if failed to get subscriptions
     */
    @Override
    public List<Subscription> getSubscribersOfProvider(int offset, int limit, String providerName)
            throws APIManagementException {
        try {
            return getApiSubscriptionDAO().getAPISubscriptionsForUser(offset, limit, providerName);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to fetch subscriptions APIs of provider " + providerName;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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

    @Override
    public API getAPIbyUUID(String uuid) throws APIManagementException {
        API api = null;
        try {
            api = super.getAPIbyUUID(uuid);
            if (api != null) {
                api.setUserSpecificApiPermissions(getAPIPermissionsOfLoggedInUser(getUsername(), api));
                String permissionString = api.getApiPermission();
                if (!StringUtils.isEmpty(permissionString)) {
                    api.setApiPermission(replaceGroupIdWithName(permissionString));
                }
            }
        } catch (ParseException e) {
            String errorMsg = "Error occurred while parsing the permission json string for API " + api.getName();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.JSON_PARSE_ERROR);
        }
        return api;
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
        if (apiBuilder.getLabels().isEmpty()) {
            Set<String> labelSet = new HashSet<>();
            labelSet.add(APIMgtConstants.DEFAULT_LABEL_NAME);
            apiBuilder.labels(labelSet);
        }
        Map<String, Endpoint> apiEndpointMap = apiBuilder.getEndpoint();
        validateEndpoints(apiEndpointMap, false);
        try {
            if (!isApiNameExist(apiBuilder.getName()) && !isContextExist(apiBuilder.getContext())) {
                LifecycleState lifecycleState = getApiLifecycleManager().addLifecycle(APIMgtConstants.API_LIFECYCLE,
                        getUsername());
                apiBuilder.associateLifecycle(lifecycleState);

                createUriTemplateList(apiBuilder, false);

                List<UriTemplate> list = new ArrayList<>(apiBuilder.getUriTemplates().values());
                List<TemplateBuilderDTO> resourceList = new ArrayList<>();

                validateApiPolicy(apiBuilder.getApiPolicy());
                validateSubscriptionPolicies(apiBuilder);
                for (UriTemplate uriTemplate : list) {
                    TemplateBuilderDTO dto = new TemplateBuilderDTO();
                    dto.setTemplateId(uriTemplate.getTemplateId());
                    dto.setUriTemplate(uriTemplate.getUriTemplate());
                    dto.setHttpVerb(uriTemplate.getHttpVerb());
                    Map<String, Endpoint> map = uriTemplate.getEndpoint();
                    if (map.containsKey(APIMgtConstants.PRODUCTION_ENDPOINT)) {
                        Endpoint endpoint = map.get(APIMgtConstants.PRODUCTION_ENDPOINT);
                        dto.setProductionEndpoint(endpoint);
                    }
                    if (map.containsKey(APIMgtConstants.SANDBOX_ENDPOINT)) {
                        Endpoint endpoint = map.get(APIMgtConstants.SANDBOX_ENDPOINT);
                        dto.setSandboxEndpoint(endpoint);
                    }
                    resourceList.add(dto);
                }
                GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
                APIConfigContext apiConfigContext = new APIConfigContext(apiBuilder.build(), config
                        .getGatewayPackageName());
                gatewaySourceGenerator.setApiConfigContext(apiConfigContext);
                String gatewayConfig = gatewaySourceGenerator.getConfigStringFromTemplate(resourceList);
                if (log.isDebugEnabled()) {
                    log.debug("API " + apiBuilder.getName() + "gateway config: " + gatewayConfig);
                }
                apiBuilder.gatewayConfig(gatewayConfig);

                if (StringUtils.isEmpty(apiBuilder.getApiDefinition())) {
                    apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
                }
                if (!StringUtils.isEmpty(apiBuilder.getApiPermission())) {
                    Map<String, Integer> roleNamePermissionList;
                    roleNamePermissionList = getAPIPermissionArray(apiBuilder.getApiPermission());
                    apiBuilder.permissionMap(roleNamePermissionList);
                }

                createdAPI = apiBuilder.build();
                APIUtils.validate(createdAPI);

                //Add API to gateway
                gateway.addAPI(createdAPI);
                if (log.isDebugEnabled()) {
                    log.debug("API : " + apiBuilder.getName() + " has been identifier published to gateway");
                }

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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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

    private void validateEndpoints(Map<String, Endpoint> endpointMap, boolean apiUpdate) throws
            APIManagementException {
        if (endpointMap != null) {
            for (Map.Entry<String, Endpoint> entry : endpointMap.entrySet()) {
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
                        Endpoint existingEndpoint = getApiDAO().getEndpoint(endpoint.getId());
                        if (existingEndpoint == null) {
                            if (getApiDAO().getEndpointByName(endpoint.getName()) != null) {
                                String msg = "Endpoint Already Exist By Name : " + endpoint.getName();
                                throw new APIManagementException(msg, ExceptionCodes
                                        .ENDPOINT_ALREADY_EXISTS);
                            } else {
                                endpointMap.replace(entry.getKey(), endpointBuilder.build());
                            }
                        } else {
                            if (apiUpdate && !existingEndpoint.getName().equals(endpoint.getName())) {
                                if (getApiDAO().getEndpointByName(endpoint.getName()) != null) {
                                    String msg = "Endpoint Already Exist By Name : " + endpoint.getName();
                                    throw new APIManagementException(msg, ExceptionCodes
                                            .ENDPOINT_ALREADY_EXISTS);
                                } else {
                                    endpointMap.replace(entry.getKey(), endpointBuilder.build());
                                }
                            }
                        }
                    } catch (APIMgtDAOException e) {
                        String msg = "Couldn't find Endpoint By Name : " + endpoint.getName();
                        log.error(msg, e);
                        throw new APIManagementException(msg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                    }

                } else {
                    endpointMap.replace(entry.getKey(), getEndpoint(entry.getValue().getId()));
                }
            }
        }
    }

    private void validateApiPolicy(Policy policy) throws APIManagementException {
        if (policy != null) {
            Policy apiPolicy = getPolicyByName(APIMgtAdminService.PolicyLevel.api, policy.getPolicyName());
            policy.setUuid(apiPolicy.getUuid());
        }
    }

    private void createUriTemplateList(API.APIBuilder apiBuilder, boolean update) throws APIManagementException {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        if (apiBuilder.getUriTemplates().isEmpty()) {
            apiBuilder.uriTemplates(APIUtils.getDefaultUriTemplates());
            apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
        }
        for (UriTemplate uriTemplate : apiBuilder.getUriTemplates().values()) {
            UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder
                    (uriTemplate);
            if (StringUtils.isEmpty(uriTemplateBuilder.getTemplateId())) {
                uriTemplateBuilder.templateId(APIUtils.generateOperationIdFromPath(uriTemplate
                        .getUriTemplate(), uriTemplate.getHttpVerb()));
            }
            Map<String, Endpoint> endpointMap = uriTemplateBuilder.getEndpoint();
            validateEndpoints(endpointMap, update);
            validateApiPolicy(uriTemplateBuilder.getPolicy());
            uriTemplateMap.put(uriTemplateBuilder.getTemplateId(), uriTemplateBuilder.build());
        }
        apiBuilder.uriTemplates(uriTemplateMap);
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
                //Checks whether the logged in user has the "UPDATE" permission for the API
                verifyUserPermissionsToUpdateAPI(getUsername(), originalAPI);
                apiBuilder.createdTime(originalAPI.getCreatedTime());
                //workflow status is an internal property and shouldn't be allowed to update externally
                apiBuilder.workflowStatus(originalAPI.getWorkflowStatus());
                if ((originalAPI.getName().equals(apiBuilder.getName())) && (originalAPI.getVersion().equals
                        (apiBuilder.getVersion())) && (originalAPI.getProvider().equals(apiBuilder.getProvider())) &&
                        originalAPI.getLifeCycleStatus().equalsIgnoreCase(apiBuilder.getLifeCycleStatus())) {

                    if (!StringUtils.isEmpty(apiBuilder.getApiPermission())) {
                        apiBuilder.apiPermission(replaceGroupNamesWithId(apiBuilder.getApiPermission()));
                        Map<String, Integer> roleNamePermissionList;
                        roleNamePermissionList = getAPIPermissionArray(apiBuilder.getApiPermission());
                        apiBuilder.permissionMap(roleNamePermissionList);
                    }
                    Map<String, Endpoint> apiEndpointMap = apiBuilder.getEndpoint();
                    validateEndpoints(apiEndpointMap, true);
                    createUriTemplateList(apiBuilder, true);
                    validateApiPolicy(apiBuilder.getApiPolicy());
                    validateSubscriptionPolicies(apiBuilder);
                    String updatedSwagger = apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder);
                    String gatewayConfig = getApiGatewayConfig(apiBuilder.getId());
                    GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
                    APIConfigContext apiConfigContext = new APIConfigContext(apiBuilder.build(), config
                            .getGatewayPackageName());
                    gatewaySourceGenerator.setApiConfigContext(apiConfigContext);
                    String updatedGatewayConfig = gatewaySourceGenerator
                            .getGatewayConfigFromSwagger(gatewayConfig, updatedSwagger);

                    API api = apiBuilder.build();

                    //Add API to gateway
                    gateway.updateAPI(api);
                    if (log.isDebugEnabled()) {
                        log.debug("API : " + apiBuilder.getName() + " has been successfully updated in gateway");
                    }

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
                            getApiDAO().updateApiDefinition(api.getId(), updatedSwagger, api.getUpdatedBy());
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
                        getApiDAO().updateApiDefinition(api.getId(), updatedSwagger, api.getUpdatedBy());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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

    private void validateSubscriptionPolicies(API.APIBuilder apiBuilder) throws APIManagementException {
        Set<Policy> subPolicies = new HashSet();
        for (Policy subscriptionPolicy : apiBuilder.getPolicies()) {
            Policy policy = getPolicyByName(APIMgtAdminService.PolicyLevel.subscription,
                    subscriptionPolicy.getPolicyName());
            if (policy == null) {
                throw new APIManagementException("Api Policy " + apiBuilder.getApiPolicy() + "Couldn't " +
                        "find",
                        ExceptionCodes.POLICY_NOT_FOUND);
            }
            subPolicies.add(policy);
        }
        apiBuilder.policies(subPolicies);
    }

    /**
     * This method checks whether the currently logged in user has the "UPDATE" permission for the API
     *
     * @param user - currently logged in user
     * @param api  - the api to be updated
     * @throws APIManagementException - If the user does not have "UPDATE" permission for the API
     */
    private void verifyUserPermissionsToUpdateAPI(String user, API api) throws APIManagementException {
        List<String> userPermissions = api.getUserSpecificApiPermissions();
        if (!userPermissions.contains(APIMgtConstants.Permission.UPDATE)) {
            String message = "The user " + user + " does not have permission to update the api " + api.getName();
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new APIManagementException(message, ExceptionCodes.NO_UPDATE_PERMISSIONS);
        }
    }

    /**
     * This method checks whether the currently logged in user has the "DELETE" permission for the API
     *
     * @param user - currently logged in user
     * @param api  - the api to be deleted
     * @throws APIManagementException - If the user does not have "DELETE" permission for the API
     */
    private void verifyUserPermissionsToDeleteAPI(String user, API api) throws APIManagementException {
        List<String> userPermissions = api.getUserSpecificApiPermissions();
        if (!userPermissions.contains(APIMgtConstants.Permission.DELETE)) {
            String message = "The user " + user + " does not have permission to delete the api " + api.getName();
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new APIManagementException(message, ExceptionCodes.NO_DELETE_PERMISSIONS);
        }
    }

    /**
     * This method will return map with role names and its permission values.
     *
     * @param permissionJsonString Permission json object a string
     * @return Map of permission values.
     * @throws ParseException If failed to parse the json string.
     */
    private HashMap<String, Integer> getAPIPermissionArray(String permissionJsonString)
            throws ParseException, APIManagementException {

        HashMap<String, Integer> rolePermissionList = new HashMap<String, Integer>();
        JSONParser jsonParser = new JSONParser();
        JSONArray baseJsonArray = (JSONArray) jsonParser.parse(permissionJsonString);
        for (Object aBaseJsonArray : baseJsonArray) {
            JSONObject jsonObject = (JSONObject) aBaseJsonArray;
            String groupId = jsonObject.get(APIMgtConstants.Permission.GROUP_ID).toString();
            JSONArray subJsonArray = (JSONArray) jsonObject.get(APIMgtConstants.Permission.PERMISSION);
            int totalPermissionValue = 0;
            for (Object aSubJsonArray : subJsonArray) {
                if (APIMgtConstants.Permission.READ.equals(aSubJsonArray.toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.READ_PERMISSION;
                } else if (APIMgtConstants.Permission.UPDATE.equals(aSubJsonArray.toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.UPDATE_PERMISSION;
                } else if (APIMgtConstants.Permission.DELETE.equals(aSubJsonArray.toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.DELETE_PERMISSION;
                } else if (APIMgtConstants.Permission.MANAGE_SUBSCRIPTION.equals(aSubJsonArray.toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.MANAGE_SUBSCRIPTION_PERMISSION;
                }
            }
            rolePermissionList.put(groupId, totalPermissionValue);
        }
        return rolePermissionList;
    }

    /**
     * This method replaces the groupId field's value to the role id instead of the name passed by the user
     *
     * @param permissionString - the permission json string which contains role names in groupId field
     * @return permission string with replaced groupId
     * @throws ParseException         - if there is an error parsing the json string
     * @throws APIManagementException - if there is an error getting the IdentityProvider instance
     */
    private String replaceGroupNamesWithId(String permissionString) throws ParseException, APIManagementException {
        JSONArray updatedPermissionArray = new JSONArray();
        JSONParser jsonParser = new JSONParser();
        JSONArray originalPermissionArray = (JSONArray) jsonParser.parse(permissionString);
        try {
            for (Object permissionObj : originalPermissionArray) {
                JSONObject jsonObject = (JSONObject) permissionObj;
                String groupName = (String) jsonObject.get(APIMgtConstants.Permission.GROUP_ID);
                String groupId = getIdentityProvider().getRoleId(groupName);
                JSONObject updatedPermissionJsonObj = new JSONObject();
                updatedPermissionJsonObj.put(APIMgtConstants.Permission.GROUP_ID, groupId);
                updatedPermissionJsonObj.put(APIMgtConstants.Permission.PERMISSION,
                        jsonObject.get(APIMgtConstants.Permission.PERMISSION));
                updatedPermissionArray.add(updatedPermissionJsonObj);
            }
        } catch (IdentityProviderException e) {
            String errorMessage = "There are invalid roles in the permission string";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, ExceptionCodes.UNSUPPORTED_ROLE);
        }
        return updatedPermissionArray.toJSONString();
    }

    /**
     * This method retrieves the set of overall permissions for a given api for the logged in user
     *
     * @param loggedInUserName - Logged in user
     * @param api              - The API whose permissions for the logged in user is retrieved
     * @return The overall list of permissions for the given API for the logged in user
     */
    private List<String> getAPIPermissionsOfLoggedInUser(String loggedInUserName, API api)
            throws APIManagementException {
        Set<String> permissionArrayForUser = new HashSet<>();
        Map<String, Integer> permissionMap = api.getPermissionMap();
        String provider = api.getProvider();
        //TODO: Remove the check for admin after IS adds an ID to admin user
        if (loggedInUserName.equals(provider) || permissionMap == null || permissionMap.isEmpty() || "admin"
                .equals(loggedInUserName)) {
            permissionArrayForUser.add(APIMgtConstants.Permission.READ);
            permissionArrayForUser.add(APIMgtConstants.Permission.UPDATE);
            permissionArrayForUser.add(APIMgtConstants.Permission.DELETE);
            permissionArrayForUser.add(APIMgtConstants.Permission.MANAGE_SUBSCRIPTION);
        } else {
            try {
                String userId = getIdentityProvider().getIdOfUser(loggedInUserName);
                List<String> loggedInUserRoles = getIdentityProvider().getRoleIdsOfUser(userId);
                List<String> permissionRoleList = getRolesFromPermissionMap(permissionMap);
                List<String> rolesOfUserWithAPIPermissions = null;
                //To prevent a possible null pointer exception
                if (loggedInUserRoles == null) {
                    loggedInUserRoles = new ArrayList<>();
                }
                //get the intersection - retainAll() transforms first set to the result of intersection
                loggedInUserRoles.retainAll(permissionRoleList);
                if (!loggedInUserRoles.isEmpty()) {
                    rolesOfUserWithAPIPermissions = loggedInUserRoles;
                }
                if (rolesOfUserWithAPIPermissions != null) {
                    Integer aggregatePermissions = 0;
                    //Calculating aggregate permissions using Bitwise OR operation
                    for (String role : rolesOfUserWithAPIPermissions) {
                        aggregatePermissions |= permissionMap.get(role);
                    }
                    permissionArrayForUser = new HashSet<>(
                            APIUtils.constructApiPermissionsListForValue(aggregatePermissions));
                }
            } catch (IdentityProviderException e) {
                String errorMsg = "Error occurred while calling SCIM endpoint to retrieve user " + loggedInUserName
                        + "'s information";
                log.error(errorMsg, e);
                throw new APIManagementException(errorMsg, e, e.getErrorHandler());
            }
        }
        List<String> finalAggregatedPermissionList = new ArrayList<>();
        finalAggregatedPermissionList.addAll(permissionArrayForUser);
        if (log.isDebugEnabled()) {
            String message = "Aggregate permissions of user " + loggedInUserName + " for the API " + api.getName() +
                    " are " + StringUtils.join(finalAggregatedPermissionList, ", ") + ".";
            log.debug(message);
        }
        return finalAggregatedPermissionList;
    }

    /**
     * This method is used to extract the groupIds or roles from the permissionMap
     *
     * @param permissionMap - The map containing the group IDs(roles) and their permissions
     * @return - The list of groupIds specified for permissions
     */
    private List<String> getRolesFromPermissionMap(Map<String, Integer> permissionMap) {
        List<String> permissionRoleList = new ArrayList<>();
        for (String groupId : permissionMap.keySet()) {
            permissionRoleList.add(groupId);
        }
        return permissionRoleList;
    }

    /**
     * This method replaces the groupId field's value of the api permissions string to the role name before sending to
     * frontend
     *
     * @param permissionString - permissions string containing role ids in the groupId field
     * @return the permission string replacing the groupId field's value to role name
     * @throws ParseException         - if there is an error parsing the permission json
     * @throws APIManagementException - if there is an error getting the IdentityProvider instance
     */
    private String replaceGroupIdWithName(String permissionString) throws ParseException, APIManagementException {
        JSONArray updatedPermissionArray = new JSONArray();
        JSONParser jsonParser = new JSONParser();
        JSONArray originalPermissionArray = (JSONArray) jsonParser.parse(permissionString);

        for (Object permissionObj : originalPermissionArray) {
            JSONObject jsonObject = (JSONObject) permissionObj;
            String groupId = (String) jsonObject.get(APIMgtConstants.Permission.GROUP_ID);
            try {
                String groupName = getIdentityProvider().getRoleName(groupId);
                JSONObject updatedPermissionJsonObj = new JSONObject();
                updatedPermissionJsonObj.put(APIMgtConstants.Permission.GROUP_ID, groupName);
                updatedPermissionJsonObj.put(APIMgtConstants.Permission.PERMISSION,
                        jsonObject.get(APIMgtConstants.Permission.PERMISSION));
                updatedPermissionArray.add(updatedPermissionJsonObj);
            } catch (IdentityProviderException e) {
                //lets the execution continue after logging the exception
                String errorMessage = "Error occurred while calling SCIM endpoint to retrieve role name of role " +
                        "with Id " + groupId;
                log.warn(errorMessage, e);
            }
        }
        return updatedPermissionArray.toJSONString();
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
                        getWorkflowDAO(), getApiLifecycleManager(), getApiGateway());
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

                String workflowDescription = "API [" + workflow.getApiName() + " - " + workflow.getApiVersion()
                        + "] state change [" + workflow.getCurrentState() + " to " + workflow.getTransitionState()
                        + "] request from " + getUsername();
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            sendEmailNotification(apiId, apiBuilder.getName(), newVersion);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't create new API version from " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
                API api = getAPIbyUUID(identifier);

                //Checks whether the user has required permissions to delete the API
                verifyUserPermissionsToDeleteAPI(getUsername(), api);
                String apiWfStatus = api.getWorkflowStatus();
                API.APIBuilder apiBuilder = new API.APIBuilder(api);

                //Delete API in gateway

                gateway.deleteAPI(api);
                if (log.isDebugEnabled()) {
                    log.debug("API : " + api.getName() + " has been successfully removed from the gateway");
                }

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
            } else {
                throw new ApiDeleteFailureException("API with " + identifier + " already have subscriptions");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting the API with id " + identifier;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
        String user = getUsername();
        Set<String> roles = new HashSet<>();
        try {
            //TODO: Need to validate users roles against results returned
            if (!"admin".equals(user)) {
                String userId = getIdentityProvider().getIdOfUser(user);
                roles = new HashSet<>(getIdentityProvider().getRoleIdsOfUser(userId));
            }
            if (query != null && !query.isEmpty()) {

                String[] attributes = query.split(",");
                Map<String, String> attributeMap = new HashMap<>();

                boolean isFullTextSearch = false;
                String searchAttribute, searchValue;
                if (!query.contains(":")) {
                    isFullTextSearch = true;
                } else {
                    log.info("Query: " + query);
                    for (String attribute : attributes) {
                        searchAttribute = attribute.split(":")[0];
                        searchValue = attribute.split(":")[1];
                        log.info(searchAttribute + ":" + searchValue);
                        attributeMap.put(searchAttribute, searchValue);
                    }
                }

                if (isFullTextSearch) {
                    apiResults = getApiDAO().searchAPIs(roles, user, query, offset, limit);
                } else {
                    log.info("Attributes:", attributeMap.toString());
                    apiResults = getApiDAO().attributeSearchAPIs(roles, user, attributeMap, offset, limit);
                }

            } else {
                apiResults = getApiDAO().getAPIs(roles, user);
            }
            return apiResults;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while Searching the API with query " + query;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        } catch (IdentityProviderException e) {
            String errorMsg = "Error occurred while calling SCIM endpoint to retrieve user " + user + "'s information";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
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
            Subscription subscription = getApiSubscriptionDAO().getAPISubscription(subId);
            if (subscription != null) {
                API subscribedApi = subscription.getApi();
                List<SubscriptionValidationData> subscriptionValidationDataList = getApiSubscriptionDAO()
                        .getAPISubscriptionsOfAPIForValidation(subscribedApi.getContext(), subscribedApi
                                .getVersion(), subscription.getApplication().getId());
                getApiGateway().updateAPISubscriptionStatus(subscriptionValidationDataList);
            }
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
            return getApiLifecycleManager()
                    .getLifecycleDataForState(api.getLifecycleInstanceId(), api.getLifeCycleStatus());
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't retrieve API Summary for " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            throw new APIManagementException(msg, e, e.getErrorHandler());
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
            throw new APIManagementException(msg, e, e.getErrorHandler());
        }
    }

    @Override
    public Endpoint getEndpointByName(String endpointName) throws APIManagementException {
        try {
            return getApiDAO().getEndpointByName(endpointName);
        } catch (APIMgtDAOException e) {
            String msg = "Failed to get Endpoint : " + endpointName;
            log.error(msg, e);
            throw new APIManagementException(msg, e, e.getErrorHandler());
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
            log.error(String.format("Endpoint already exist with name %s", key));
            throw new APIManagementException("Endpoint already exist with name " + key,
                    ExceptionCodes.ENDPOINT_ALREADY_EXISTS);
        }
        gateway.addEndpoint(endpoint1);
        String config = getGatewaySourceGenerator().getEndpointConfigStringFromTemplate(endpoint1);
        endpoint1 = new Endpoint.Builder(endpoint1).config(config).build();
        try {

            getApiDAO().addEndpoint(endpoint1);
        } catch (APIMgtDAOException e) {
            String msg = "Failed to add Endpoint : " + endpoint.getName();
            log.error(msg, e);
            throw new APIManagementException(msg, e, e.getErrorHandler());
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
        String config = getGatewaySourceGenerator().getEndpointConfigStringFromTemplate(endpoint);
        Endpoint updatedEndpoint = new Endpoint.Builder(endpoint).config(config).build();
        try {
            getApiDAO().updateEndpoint(updatedEndpoint);
        } catch (APIMgtDAOException e) {
            String msg = "Failed to update Endpoint : " + endpoint.getName();
            log.error(msg, e);
            throw new APIManagementException(msg, e, e.getErrorHandler());
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
                throw new APIManagementException(msg, e, e.getErrorHandler());
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
        } catch (IOException e) {
            String msg = "Error while getting the swagger resource from url";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.API_DEFINITION_MALFORMED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSwagger20Definition(String apiId, String jsonText) throws APIManagementException {
        try {
            LocalDateTime localDateTime = LocalDateTime.now();
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
            apiBuilder.updatedBy(getUsername());
            apiBuilder.lastUpdatedTime(localDateTime);

            api = apiBuilder.build();
            GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
            APIConfigContext apiConfigContext = new APIConfigContext(apiBuilder.build(), config.getGatewayPackageName
                    ());
            gatewaySourceGenerator.setApiConfigContext(apiConfigContext);
            String existingGatewayConfig = getApiGatewayConfig(apiId);
            String updatedGatewayConfig = gatewaySourceGenerator
                    .getGatewayConfigFromSwagger(existingGatewayConfig, jsonText);
            getApiDAO().updateAPI(apiId, api);
            getApiDAO().updateApiDefinition(apiId, jsonText, getUsername());
            getApiDAO().updateGatewayConfig(apiId, updatedGatewayConfig, getUsername());
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't update the Swagger Definition";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public String getAPIWSDL(String apiId) throws APIMgtDAOException {
        return getApiDAO().getWSDL(apiId);
    }

    @Override
    public InputStream getAPIWSDLArchive(String apiId) throws APIMgtDAOException {
        return getApiDAO().getWSDLArchive(apiId);
    }

    @Override
    public String addAPIFromWSDLArchive(API.APIBuilder apiBuilder, InputStream inputStream, boolean isHttpBinding)
            throws APIManagementException {
        WSDLArchiveInfo archiveInfo = extractAndValidateWSDLArchive(inputStream);
        if (log.isDebugEnabled()) {
            log.debug("Successfully extracted and validated WSDL file. Location: " + archiveInfo.getAbsoluteFilePath());
        }

        apiBuilder.uriTemplates(APIMWSDLUtils
                .getUriTemplatesForWSDLOperations(archiveInfo.getWsdlInfo().getHttpBindingOperations(), isHttpBinding));
        String uuid = addAPI(apiBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Successfully added the API. uuid: " + uuid);
        }

        try (InputStream fileInputStream = new FileInputStream(archiveInfo.getAbsoluteFilePath())) {
            getApiDAO().addOrUpdateWSDLArchive(uuid, fileInputStream, getUsername());
            if (log.isDebugEnabled()) {
                log.debug("Successfully added/updated the WSDL archive. uuid: " + uuid);
            }

            if (APIMgtConstants.WSDLConstants.WSDL_VERSION_20.equals(archiveInfo.getWsdlInfo().getVersion())) {
                log.info("Extraction of HTTP Binding operations is not supported for WSDL 2.0.");
            }
            return uuid;
        } catch (IOException e) {
            throw new APIMgtWSDLException("Unable to process WSDL archive at " + archiveInfo.getAbsoluteFilePath(), e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        } finally {
            try {
                APIFileUtils.deleteDirectory(archiveInfo.getLocation());
            } catch (APIMgtDAOException e) {
                //This is not a blocker. Give a warning and continue
                log.warn("Error occured while deleting processed WSDL artifacts folder : " + archiveInfo.getLocation());
            }
        }
    }

    @Override
    public String addAPIFromWSDLFile(API.APIBuilder apiBuilder, InputStream inputStream, boolean isHttpBinding)
            throws APIManagementException {
        byte[] wsdlContent;
        try {
            wsdlContent = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new APIMgtWSDLException("Error while converting input stream to byte array", e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        }
        WSDLProcessor processor = WSDLProcessFactory.getInstance().getWSDLProcessor(wsdlContent);
        apiBuilder.uriTemplates(APIMWSDLUtils
                .getUriTemplatesForWSDLOperations(processor.getWsdlInfo().getHttpBindingOperations(), isHttpBinding));
        if (!processor.canProcess()) {
            throw new APIMgtWSDLException("Unable to process WSDL by the processor " + processor.getClass().getName(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT);
        }

        String uuid = addAPI(apiBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Successfully added the API. uuid: " + uuid);
        }
        getApiDAO().addOrUpdateWSDL(uuid, wsdlContent, getUsername());
        if (log.isDebugEnabled()) {
            log.debug("Successfully added the WSDL file to database. API uuid: " + uuid);
        }
        if (APIMgtConstants.WSDLConstants.WSDL_VERSION_20.equals(processor.getWsdlInfo().getVersion())) {
            log.info("Extraction of HTTP Binding operations is not supported for WSDL 2.0.");
        }
        return uuid;
    }

    @Override
    public String addAPIFromWSDLURL(API.APIBuilder apiBuilder, String wsdlUrl, boolean isHttpBinding)
            throws APIManagementException {
        WSDLProcessor processor = WSDLProcessFactory.getInstance().getWSDLProcessor(wsdlUrl);
        if (!processor.canProcess()) {
            throw new APIMgtWSDLException("Unable to process WSDL by the processor " + processor.getClass().getName(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT);
        }
        apiBuilder.uriTemplates(APIMWSDLUtils
                .getUriTemplatesForWSDLOperations(processor.getWsdlInfo().getHttpBindingOperations(), isHttpBinding));
        String uuid = addAPI(apiBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Successfully added the API. uuid: " + uuid);
        }
        byte[] wsdlContentBytes = processor.getWSDL();
        getApiDAO().addOrUpdateWSDL(uuid, wsdlContentBytes, getUsername());
        if (log.isDebugEnabled()) {
            log.debug("Successfully added the content of WSDL URL to database. WSDL URL: " + wsdlUrl);
        }
        if (APIMgtConstants.WSDLConstants.WSDL_VERSION_20.equals(processor.getWsdlInfo().getVersion())) {
            log.info("Extraction of HTTP Binding operations is not supported for WSDL 2.0.");
        }
        return uuid;
    }

    @Override
    public String updateAPIWSDL(String apiId, InputStream inputStream)
            throws APIMgtDAOException, APIMgtWSDLException {
        byte[] wsdlContent;
        try {
            wsdlContent = IOUtils.toByteArray(inputStream);
            WSDLProcessor processor = WSDLProcessFactory.getInstance().getWSDLProcessor(wsdlContent);
            if (!processor.canProcess()) {
                throw new APIMgtWSDLException(
                        "Unable to process WSDL by the processor " + processor.getClass().getName(),
                        ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT);
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully validated the content of WSDL. API uuid: " + apiId);
            }
            getApiDAO().addOrUpdateWSDL(apiId, wsdlContent, getUsername());
            if (log.isDebugEnabled()) {
                log.debug("Successfully added WSDL to the DB. API uuid: " + apiId);
            }
            return new String(wsdlContent, APIMgtConstants.ENCODING_UTF_8);
        } catch (IOException e) {
            throw new APIMgtWSDLException("Error while updating WSDL of API " + apiId, e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        }
    }

    @Override
    public void updateAPIWSDLArchive(String apiId, InputStream inputStream)
            throws APIMgtDAOException, APIMgtWSDLException {
        WSDLArchiveInfo archiveInfo = null;
        InputStream fileInputStream = null;
        try {
            archiveInfo = extractAndValidateWSDLArchive(inputStream);
            if (log.isDebugEnabled()) {
                log.debug("Successfully extracted and validated WSDL file. Location: " + archiveInfo
                        .getAbsoluteFilePath());
            }
            fileInputStream = new FileInputStream(archiveInfo.getAbsoluteFilePath());
            getApiDAO().addOrUpdateWSDLArchive(apiId, fileInputStream, getUsername());
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated the WSDL archive in DB. API uuid: " + apiId);
            }
        } catch (IOException e) {
            throw new APIMgtWSDLException("Unable to process WSDL archive at " + archiveInfo.getAbsoluteFilePath(), e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (archiveInfo != null) {
                    APIFileUtils.deleteDirectory(archiveInfo.getLocation());
                }
            } catch (APIMgtDAOException | IOException e) {
                //This is not a blocker. Give a warning and continue
                log.warn("Error occured while deleting processed WSDL artifacts folder : " + archiveInfo.getLocation());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateApiGatewayConfig(String apiId, String configString) throws APIManagementException {
        API api = getAPIbyUUID(apiId);
        GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
        APIConfigContext apiConfigContext = new APIConfigContext(api, config.getGatewayPackageName());
        gatewaySourceGenerator.setApiConfigContext(apiConfigContext);
        try {
            String swagger = gatewaySourceGenerator.getSwaggerFromGatewayConfig(configString);
            getApiDAO().updateApiDefinition(apiId, swagger, getUsername());
            getApiDAO().updateGatewayConfig(apiId, configString, getUsername());
        } catch (APIMgtDAOException e) {
            log.error("Couldn't update configuration for apiId " + apiId, e);
            throw new APIManagementException("Couldn't update configuration for apiId " + apiId,
                    e.getErrorHandler());
        } catch (APITemplateException e) {
            log.error("Error generating swagger from gateway config " + apiId, e);
            throw new APIManagementException("Error generating swagger from gateway config " + apiId,
                    ExceptionCodes.TEMPLATE_EXCEPTION);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getApiGatewayConfig(String apiId) throws APIManagementException {
        try {
            return getApiDAO().getGatewayConfigOfAPI(apiId);

        } catch (APIMgtDAOException e) {
            log.error("Couldn't retrieve swagger definition for apiId " + apiId, e);
            throw new APIManagementException("Couldn't retrieve gateway configuration for apiId " + apiId,
                    e.getErrorHandler());
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
    public List<Policy> getAllPoliciesByLevel(APIMgtAdminService.PolicyLevel tierLevel) throws APIManagementException {
        try {
            return getPolicyDAO().getPoliciesByLevel(tierLevel);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error while retrieving Policies for level: " + tierLevel;
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public Policy getPolicyByName(APIMgtAdminService.PolicyLevel tierLevel, String tierName)
            throws APIManagementException {
        try {
            return getPolicyDAO().getSimplifiedPolicyByLevelAndName(tierLevel, tierName);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error while retrieving Policy for level: " + tierLevel + ", name: " + tierName;
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
        try {
            API api = getApiDAO().getAPI(apiId);
            if (APILCWorkflowStatus.PENDING.toString().equals(api.getWorkflowStatus())) {

                //change the state back
                getApiDAO().updateAPIWorkflowStatus(apiId, APILCWorkflowStatus.APPROVED);

                // call executor's cleanup task
                cleanupPendingTaskForAPIStateChange(apiId);


            } else {
                String msg = "API does not have a pending lifecycle state change.";
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.WORKFLOW_NO_PENDING_TASK);
            }
        } catch (APIMgtDAOException e) {
            String msg = "Error occurred while changing api lifecycle workflow status";
            log.error(msg, e);
            throw new APIManagementException(msg, e.getErrorHandler());
        }
    }

    @Override
    public boolean isEndpointExist(String name) throws APIManagementException {
        try {
            return getApiDAO().isEndpointExist(name);
        } catch (APIMgtDAOException e) {
            String msg = "Couldn't find existence of endpoint :" + name;
            throw new APIManagementException(msg, e.getErrorHandler());
        }
    }

    @Override
    public WSDLArchiveInfo extractAndValidateWSDLArchive(InputStream inputStream)
            throws APIMgtDAOException, APIMgtWSDLException {

        String path = System.getProperty(APIMgtConstants.JAVA_IO_TMPDIR)
                + File.separator + APIMgtConstants.WSDLConstants.WSDL_ARCHIVES_FOLDERNAME
                + File.separator + UUID.randomUUID().toString();
        String archivePath = path + File.separator + APIMgtConstants.WSDLConstants.WSDL_ARCHIVE_FILENAME;
        String extractedLocation = APIFileUtils.extractUploadedArchive(inputStream,
                APIMgtConstants.WSDLConstants.EXTRACTED_WSDL_ARCHIVE_FOLDERNAME, archivePath, path);
        if (log.isDebugEnabled()) {
            log.debug("Successfully extracted WSDL archive. Location: " + extractedLocation);
        }

        WSDLProcessor processor = WSDLProcessFactory.getInstance().getWSDLProcessorForPath(extractedLocation);
        if (!processor.canProcess()) {
            throw new APIMgtWSDLException("Unable to process WSDL by the processor " + processor.getClass().getName(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT);
        }
        WSDLArchiveInfo archiveInfo = new WSDLArchiveInfo(path, APIMgtConstants.WSDLConstants.WSDL_ARCHIVE_FILENAME);
        archiveInfo.setWsdlInfo(processor.getWsdlInfo());
        return archiveInfo;
    }

    @Override
    public Set<String> getSubscribersByAPIId(String apiId) throws APIManagementException {
        List<Subscription> subscriptionList = new ArrayList();
        Set<String> subscriberList = new HashSet<>();
        if (StringUtils.isNotEmpty(apiId)) {
            subscriptionList = getSubscriptionsByAPI(apiId);
        }
        for (Subscription listItem : subscriptionList) {
            subscriberList.add(listItem.getApplication().getCreatedUser());
        }
        return subscriberList;
    }

    private void cleanupPendingTaskForAPIStateChange(String apiId) throws APIManagementException {
        Optional<String> workflowExtRef = getWorkflowDAO().getExternalWorkflowReferenceForPendingTask(apiId,
                WorkflowConstants.WF_TYPE_AM_API_STATE);
        if (workflowExtRef.isPresent()) {
            WorkflowExecutor executor = WorkflowExecutorFactory.getInstance()
                    .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE);
            try {
                executor.cleanUpPendingTask(workflowExtRef.get());
            } catch (WorkflowException e) {
                String warn = "Failed to clean pending api state change task for " + apiId;
                // failed cleanup processes are ignored to prevent failing the deletion process
                log.warn(warn, e.getLocalizedMessage());
            }
            getWorkflowDAO().deleteWorkflowEntryforExternalReference(workflowExtRef.get());
        }
    }

    private void sendEmailNotification(String apiId, String apiName, String newVersion)
            throws APIManagementException {
        Set<String> subscriberList;
        NotificationConfigurations notificationConfigurations = ServiceReferenceHolder.getInstance().
                getAPIMConfiguration().getNotificationConfigurations();

        // check notification Enabled
        if (notificationConfigurations.getNotificationEnable()) {
            subscriberList = getSubscribersByAPIId(apiId);
            //Notifications are sent only if there are subscribers
            if (subscriberList.size() > 0) {
                try {
                    Properties prop = new Properties();
                    prop.put(NotifierConstants.NEW_API_VERSION, newVersion);
                    prop.put(NotifierConstants.API_NAME, apiName);
                    prop.put(NotifierConstants.SUBSCRIBERS_PER_API, subscriberList);
                    NotificationDTO notificationDTO = new NotificationDTO(prop, NotifierConstants
                            .NOTIFICATION_TYPE_NEW_VERSION);
                    new NotificationExecutor().sendAsyncNotifications(notificationDTO);
                } catch (NotificationException e) {
                    String msg = "Error occurred while sending Async Notifications";
                    log.error(msg, e);
                }
            }
        }
    }
}
