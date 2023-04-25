/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.*;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.dto.*;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentSourceType;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentVisibility;
import org.wso2.carbon.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManager;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ServiceCatalogDAO;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.*;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.monetization.DefaultMonetizationImpl;
import org.wso2.carbon.apimgt.impl.notification.NotificationDTO;
import org.wso2.carbon.apimgt.impl.notification.NotificationExecutor;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.notifier.events.KeyTemplateEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.*;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.CertificateEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ScopeEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.publishers.WSO2APIPublisher;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderDetailsExtractor;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderEventPublisher;
import org.wso2.carbon.apimgt.impl.token.ApiKeyGenerator;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.token.InternalAPIKeyGenerator;
import org.wso2.carbon.apimgt.impl.utils.*;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.impl.workflow.*;
import org.wso2.carbon.apimgt.impl.wsdl.WSDLProcessor;
import org.wso2.carbon.apimgt.impl.lifecycle.LCManagerFactory;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.*;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mapper.APIProductMapper;
import org.wso2.carbon.apimgt.persistence.mapper.DocumentMapper;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.CheckListItem;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class provides the core API provider functionality. It is implemented in a very
 * self-contained and 'pure' manner, without taking requirements like security into account,
 * which are subject to frequent change. Due to this 'pure' nature and the significance of
 * the class to the overall API management functionality, the visibility of the class has
 * been reduced to package level. This means we can still use it for internal purposes and
 * possibly even extend it, but it's totally off the limits of the users. Users wishing to
 * pragmatically access this functionality should use one of the extensions of this
 * class which is visible to them. These extensions may add additional features like
 * security to this class.
 */
class APIProviderImpl extends AbstractAPIManager implements APIProvider {

    private static final Log log = LogFactory.getLog(APIProviderImpl.class);
    private static final String ENDPOINT_CONFIG_SEARCH_TYPE_PREFIX  = "endpointConfig:";
    private ServiceCatalogDAO serviceCatalogDAO = ServiceCatalogDAO.getInstance();

    private final String userNameWithoutChange;
    private CertificateManager certificateManager;
    protected  ArtifactSaver artifactSaver;
    protected ImportExportAPI importExportAPI;
    protected GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    private RecommendationEnvironment recommendationEnvironment;
    String migrationEnabled = System.getProperty(APIConstants.MIGRATE);
    private GlobalMediationPolicyImpl globalMediationPolicyImpl;

    public APIProviderImpl(String username) throws APIManagementException {
        super(username);
        this.userNameWithoutChange = username;
        certificateManager = CertificateManagerImpl.getInstance();
        this.artifactSaver = ServiceReferenceHolder.getInstance().getArtifactSaver();
        this.importExportAPI = ServiceReferenceHolder.getInstance().getImportExportService();
        this.gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
        this.recommendationEnvironment = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getApiRecommendationEnvironment();
        globalMediationPolicyImpl = new GlobalMediationPolicyImpl(organization);
    }

    protected String getUserNameWithoutChange() {
        return userNameWithoutChange;
    }


    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    @Override
    @Deprecated
    public Set<Subscriber> getSubscribersOfProvider(String providerId) throws APIManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfProvider(providerId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscribers for : " + providerId, e);
        }
        return subscriberSet;
    }

    /**
     * Return Usage of given APIIdentifier
     *
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    @Override
    public Usage getUsageByAPI(APIIdentifier apiIdentifier) {
        return null;
    }

    /**
     * Return Usage of given provider and API
     *
     * @param providerId if of the provider
     * @param apiName    name of the API
     * @return Usage
     */
    @Override
    public Usage getAPIUsageByUsers(String providerId, String apiName) {
        return null;
    }

    /**
     * Returns usage details of all APIs published by a provider
     *
     * @param providerName Provider Id
     * @return UserApplicationAPIUsages for given provider
     * @throws APIManagementException If failed to get UserApplicationAPIUsage
     */
    @Override
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerName) throws APIManagementException {
        return apiMgtDAO.getAllAPIUsageByProvider(providerName);
    }

    /**
     * Returns usage details of a particular API
     *
     * @param uuid API uuid
     * @param organization identifier of the organization
     * @return UserApplicationAPIUsages for given provider
     * @throws APIManagementException If failed to get UserApplicationAPIUsage
     */
    @Override
    public List<SubscribedAPI> getAPIUsageByAPIId(String uuid, String organization)
            throws APIManagementException {
        APIIdentifier identifier = apiMgtDAO.getAPIIdentifierFromUUID(uuid);
        List<SubscribedAPI> subscribedAPIs = new ArrayList<>();
        if (identifier != null) {
            APIIdentifier apiIdEmailReplaced = new APIIdentifier(APIUtil.replaceEmailDomain(identifier.getProviderName()),
                    identifier.getApiName(), identifier.getVersion());
            UserApplicationAPIUsage[] allApiResult = apiMgtDAO.getAllAPIUsageByProviderAndApiId(uuid, organization);
            for (UserApplicationAPIUsage usage : allApiResult) {
                for (SubscribedAPI apiSubscription : usage.getApiSubscriptions()) {
                    APIIdentifier subsApiId = apiSubscription.getAPIIdentifier();
                    APIIdentifier subsApiIdEmailReplaced = new APIIdentifier(
                            APIUtil.replaceEmailDomain(subsApiId.getProviderName()), subsApiId.getApiName(),
                            subsApiId.getVersion());
                    if (subsApiIdEmailReplaced.equals(apiIdEmailReplaced)) {
                        subscribedAPIs.add(apiSubscription);
                    }
                }
            }
        }
        return subscribedAPIs;
    }

    /**
     * Returns usage details of a particular API
     *
     * @param apiProductId API Product identifier
     * @return UserApplicationAPIUsages for given provider
     * @throws APIManagementException If failed to get UserApplicationAPIUsage
     */
    @Override
    public List<SubscribedAPI> getAPIProductUsageByAPIProductId(APIProductIdentifier apiProductId) throws APIManagementException {
        APIProductIdentifier apiIdEmailReplaced = new APIProductIdentifier(APIUtil.replaceEmailDomain(apiProductId.getProviderName()),
                apiProductId.getName(), apiProductId.getVersion());
        UserApplicationAPIUsage[] allApiProductResult = apiMgtDAO.getAllAPIProductUsageByProvider(apiProductId.getProviderName());
        List<SubscribedAPI> subscribedAPIs = new ArrayList<>();
        for (UserApplicationAPIUsage usage : allApiProductResult) {
            for (SubscribedAPI apiSubscription : usage.getApiSubscriptions()) {
                APIProductIdentifier subsApiProductId = apiSubscription.getProductId();
                APIProductIdentifier subsApiProductIdEmailReplaced = new APIProductIdentifier(
                        APIUtil.replaceEmailDomain(subsApiProductId.getProviderName()), subsApiProductId.getName(),
                        subsApiProductId.getVersion());
                if (subsApiProductIdEmailReplaced.equals(apiIdEmailReplaced)) {
                    subscribedAPIs.add(apiSubscription);
                }
            }
        }
        return subscribedAPIs;
    }

    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    @Override
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail) {
        return null;
    }

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier) throws APIManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get subscribers for API : " + identifier.getApiName(), e);
        }
        return subscriberSet;
    }

    /**
     * Returns full list of subscriptions of an API
     *
     * @param apiName    Name of the API
     * @param apiVersion Version of the API
     * @param provider Name of API creator
     * @return All subscriptions of a given API
     * @throws APIManagementException if failed to get Subscribers
     */
    public List<SubscribedAPI> getSubscriptionsOfAPI(String apiName, String apiVersion, String provider)
            throws APIManagementException {
        return apiMgtDAO.getSubscriptionsOfAPI(apiName, apiVersion, provider);
    }

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier) throws APIManagementException {
        long count = 0L;
        try {
            count = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get APISubscriptionCount for: " + identifier.getApiName(), e);
        }
        return count;
    }

    private OMElement createThrottlePolicy(Tier tier) throws APIManagementException {
        OMElement throttlePolicy = null;
        String policy = APIConstants.THROTTLE_POLICY_TEMPLATE;

        StringBuilder attributeBuilder = new StringBuilder();
        Map<String, Object> tierAttributes = tier.getTierAttributes();

        if (tierAttributes != null) {
            for (Map.Entry<String, Object> entry : tierAttributes.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String attributeName = entry.getKey().trim();
                    String attributeValue = ((String) entry.getValue()).trim();

                    // We see whether the attribute name is empty.
                    if (!attributeName.isEmpty()) {
                        attributeBuilder.append(String.format(APIConstants.THROTTLE_POLICY_ATTRIBUTE_TEMPLATE,
                                attributeName, attributeValue, attributeName));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Unrecognized throttle attribute value : " + entry.getValue() +
                                " of attribute name : " + entry.getKey());
                    }
                }
            }
        }

        // We add the "description", "billing plan" and "stop on quota reach" as custom attributes
        attributeBuilder.append(String.format(APIConstants.THROTTLE_POLICY_ATTRIBUTE_TEMPLATE,
                APIConstants.THROTTLE_TIER_DESCRIPTION_ATTRIBUTE,
                tier.getDescription().trim(),
                APIConstants.THROTTLE_TIER_DESCRIPTION_ATTRIBUTE));

        attributeBuilder.append(String.format(APIConstants.THROTTLE_POLICY_ATTRIBUTE_TEMPLATE,
                APIConstants.THROTTLE_TIER_PLAN_ATTRIBUTE,
                tier.getTierPlan().trim(),
                APIConstants.THROTTLE_TIER_PLAN_ATTRIBUTE));

        attributeBuilder.append(String.format(APIConstants.THROTTLE_POLICY_ATTRIBUTE_TEMPLATE,
                APIConstants.THROTTLE_TIER_QUOTA_ACTION_ATTRIBUTE,
                String.valueOf(tier.isStopOnQuotaReached()),
                APIConstants.THROTTLE_TIER_QUOTA_ACTION_ATTRIBUTE));

        // Note: We assume that the unit time is in milliseconds.
        policy = String.format(policy, tier.getName(), tier.getRequestCount(), tier.getUnitTime(),
                attributeBuilder.toString());

        try {
            throttlePolicy = AXIOMUtil.stringToOM(policy);
        } catch (XMLStreamException e) {
            handleException("Invalid policy xml generated", e);
        }
        return throttlePolicy;
    }

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws APIManagementException if failed to add API
     */

    public API addAPI(API api) throws APIManagementException {
        validateApiInfo(api);
        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
        validateResourceThrottlingTiers(api, tenantDomain);
        validateKeyManagers(api);
        String apiName = api.getId().getApiName();
        String provider = APIUtil.replaceEmailDomain(api.getId().getProviderName());

        if (api.isEndpointSecured() && StringUtils.isEmpty(api.getEndpointUTPassword())) {
            String errorMessage = "Empty password is given for endpointSecurity when creating API " + apiName;
            throw new APIManagementException(errorMessage);
        }
        //Validate Transports
        validateAndSetTransports(api);
        validateAndSetAPISecurity(api);

        //Set version timestamp to the API
        String latestTimestamp = calculateVersionTimestamp(provider, apiName,
                api.getId().getVersion(), api.getOrganization());
        api.setVersionTimestamp(latestTimestamp);

        // For Choreo-Connect gateway, gateway vendor type in the DB will be "wso2/choreo-connect".
        // This value is determined considering the gateway type comes with the request.
        api.setGatewayVendor(APIUtil.setGatewayVendorBeforeInsertion(
                api.getGatewayVendor(), api.getGatewayType()));
        try {
            PublisherAPI addedAPI = apiPersistenceInstance.addAPI(new Organization(api.getOrganization()),
                    APIMapper.INSTANCE.toPublisherApi(api));
            api.setUuid(addedAPI.getId());
            api.setCreatedTime(addedAPI.getCreatedTime());
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while persisting API ", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("API details successfully added to the registry. API Name: " + api.getId().getApiName()
                    + ", API Version : " + api.getId().getVersion() + ", API context : " + api.getContext());
        }
        int tenantId = APIUtil.getInternalOrganizationId(api.getOrganization());
        addAPI(api, tenantId);

        JSONObject apiLogObject = new JSONObject();
        apiLogObject.put(APIConstants.AuditLogConstants.NAME, api.getId().getApiName());
        apiLogObject.put(APIConstants.AuditLogConstants.CONTEXT, api.getContext());
        apiLogObject.put(APIConstants.AuditLogConstants.VERSION, api.getId().getVersion());
        apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, api.getId().getProviderName());

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                APIConstants.AuditLogConstants.CREATED, this.username);

        if (log.isDebugEnabled()) {
            log.debug("API details successfully added to the API Manager Database. API Name: " + api.getId()
                    .getApiName() + ", API Version : " + api.getId().getVersion() + ", API context : " + api
                    .getContext());
        }

        if (APIUtil.isAPIManagementEnabled()) {
            Cache contextCache = APIUtil.getAPIContextCache();
            Boolean apiContext = null;

            Object cachedObject = contextCache.get(api.getContext());
            if (cachedObject != null) {
                apiContext = Boolean.valueOf(cachedObject.toString());
            }
            if (apiContext == null) {
                contextCache.put(api.getContext(), Boolean.TRUE);
            }
        }

        if ("null".equals(api.getAccessControlRoles())) {
            api.setAccessControlRoles(null);
        }
        //notify key manager with API addition
        registerOrUpdateResourceInKeyManager(api, tenantDomain);
        return api;

    }

    /**
     * Add API metadata, local scopes and URI templates to the database and KeyManager.
     *
     * @param api      API to add
     * @param tenantId Tenant Id
     * @throws APIManagementException if an error occurs while adding the API
     */
    private void addAPI(API api, int tenantId) throws APIManagementException {
        int apiId = apiMgtDAO.addAPI(api, tenantId, api.getOrganization());
        addLocalScopes(api.getId().getApiName(), api.getUriTemplates(), api.getOrganization());
        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
        validateOperationPolicyParameters(api, tenantDomain);
        addURITemplates(apiId, api, tenantId);
        APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.API_CREATE.name(), tenantId, api.getOrganization(), api.getId().getApiName(),
                apiId, api.getUuid(), api.getId().getVersion(), api.getType(), api.getContext(),
                APIUtil.replaceEmailDomainBack(api.getId().getProviderName()), api.getStatus());
        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
    }

    /**
     * Add local scopes for the API if the scopes does not exist as shared scopes. The local scopes to add will be
     * take from the URI templates.
     *
     * @param apiName API name
     * @param uriTemplates  URI Templates
     * @param organization  Organization
     * @throws APIManagementException if fails to add local scopes for the API
     */
    private void addLocalScopes(String apiName, Set<URITemplate> uriTemplates, String organization)
            throws APIManagementException {

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        //Get the local scopes set to register for the API from URI templates
        Set<Scope> scopesToRegister = getScopesToRegisterFromURITemplates(apiName, organization, uriTemplates);
        //Register scopes
        for (Scope scope : scopesToRegister) {
            for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
                KeyManager keyManager = keyManagerDtoEntry.getValue().getKeyManager();
                if (keyManager != null) {
                    String scopeKey = scope.getKey();
                    try {
                        // Check if key already registered in KM. Scope Key may be already registered for a different
                        // version.
                        if (!keyManager.isScopeExists(scopeKey)) {
                            //register scope in KM
                            keyManager.registerScope(scope);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Scope: " + scopeKey +
                                        " already registered in KM. Skipping registering scope.");
                            }
                        }
                    } catch (APIManagementException e) {
                        log.error("Error while registering Scope " + scopeKey + "in Key Manager " +
                                keyManagerDtoEntry.getKey(), e);
                    }

                }
            }
        }
        addScopes(scopesToRegister, tenantId);
    }

    /**
     * Extract the scopes set from URI templates which needs to be registered as local scopes for the API.
     *
     * @param apiName API name
     * @param organization  Organization
     * @param uriTemplates  URI templates
     * @return Local Scopes set to register
     * @throws APIManagementException if fails to extract Scopes from URI templates
     */
    private Set<Scope> getScopesToRegisterFromURITemplates(String apiName, String organization,
            Set<URITemplate> uriTemplates) throws APIManagementException {

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        Set<Scope> scopesToRegister = new HashSet<>();
        Set<Scope> uriTemplateScopes = new HashSet<>();
        //Get the attached scopes set from the URI templates
        for (URITemplate uriTemplate : uriTemplates) {
            List<Scope> scopesFromURITemplate = uriTemplate.retrieveAllScopes();
            for (Scope scopeFromURITemplate : scopesFromURITemplate) {
                if (scopeFromURITemplate == null) {
                    continue; // No scopes attached for the URI Template
                }
                uriTemplateScopes.add(scopeFromURITemplate);
            }
        }

        //Validate and extract only the local scopes which need to be registered in KM
        for (Scope scope : uriTemplateScopes) {
            String scopeKey = scope.getKey();
            //Check if it an existing shared scope, if so skip adding scope
            if (!isSharedScopeNameExists(scopeKey, tenantId)) {
                // Check if scope key is already assigned locally to a different API (Other than different versions of
                // the same API).
                if (!isScopeKeyAssignedLocally(apiName, scope.getKey(), organization)) {
                    scopesToRegister.add(scope);
                } else {
                    throw new APIManagementException("Error while adding local scopes for API " + apiName
                            + ". Scope: " + scopeKey + " already assigned locally for a different API.");
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Scope " + scopeKey + " exists as a shared scope. Skip adding as a local scope.");
            }
        }
        return scopesToRegister;
    }

    /**
     * Add URI templates for the API.
     *
     * @param apiId    API Id
     * @param api      API
     * @param tenantId Tenant Id
     * @throws APIManagementException if fails to add URI templates for the API
     */
    private void addURITemplates(int apiId, API api, int tenantId) throws APIManagementException {

        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        validateAndUpdateURITemplates(api, tenantId);
        apiMgtDAO.addURITemplates(apiId, api, tenantId);
        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
            KeyManager keyManager = keyManagerDtoEntry.getValue().getKeyManager();
            if (keyManager != null) {
                try {
                    keyManager.attachResourceScopes(api, api.getUriTemplates());
                } catch (APIManagementException e) {
                    log.error("Error while Attaching Resource to scope in Key Manager " + keyManagerDtoEntry.getKey(),
                            e);
                }
            }
        }
    }

    /**
     * Notify the key manager with API update or addition
     *
     * @param api API
     * @param tenantDomain
     * @throws APIManagementException when error occurs when register/update API at Key Manager side
     */
    private void registerOrUpdateResourceInKeyManager(API api, String tenantDomain) throws APIManagementException {
        //get new key manager instance for  resource registration.
        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
            KeyManager keyManager = keyManagerDtoEntry.getValue().getKeyManager();
            if (keyManager != null) {
                try {
                    Map registeredResource = keyManager.getResourceByApiId(api.getId().toString());
                    if (registeredResource == null) {
                        boolean isNewResourceRegistered = keyManager.registerNewResource(api, null);
                        if (!isNewResourceRegistered) {
                            log.warn("APIResource registration is failed while adding the API- " +
                                    api.getId().getApiName()
                                    + "-" + api.getId().getVersion() + " into Key Manager : " +
                                    keyManagerDtoEntry.getKey());
                        }
                    } else {
                        //update APIResource.
                        String resourceId = (String) registeredResource.get("resourceId");
                        if (resourceId == null) {
                            handleException("APIResource update is failed because of empty resourceID.");
                        }
                        keyManager.updateRegisteredResource(api, registeredResource);
                    }
                } catch (APIManagementException e) {
                    log.error("API Resource Registration failed in Key Manager " + keyManagerDtoEntry.getKey(), e);
                }
            }
        }
    }


    /**
     * Validates the name and version of api against illegal characters.
     *
     * @param api API info object
     * @throws APIManagementException
     */
    private void validateApiInfo(API api) throws APIManagementException {
        String apiName = api.getId().getApiName();
        String apiVersion = api.getId().getVersion();
        if (apiName == null) {
            handleException("API Name is required.");
        } else if (containsIllegals(apiName)) {
            handleException("API Name contains one or more illegal characters  " +
                    "( " + APIConstants.REGEX_ILLEGAL_CHARACTERS_FOR_API_METADATA + " )");
        }
        if (apiVersion == null) {
            handleException("API Version is required.");
        } else if (containsIllegals(apiVersion)) {
            handleException("API Version contains one or more illegal characters  " +
                    "( " + APIConstants.REGEX_ILLEGAL_CHARACTERS_FOR_API_METADATA + " )");
        }
        if (!hasValidLength(apiName, APIConstants.MAX_LENGTH_API_NAME)
                || !hasValidLength(apiVersion, APIConstants.MAX_LENGTH_VERSION)
                || !hasValidLength(api.getId().getProviderName(), APIConstants.MAX_LENGTH_PROVIDER)
                || !hasValidLength(api.getContext(), APIConstants.MAX_LENGTH_CONTEXT)
                ) {
            throw new APIManagementException("Character length exceeds the allowable limit",
                    ExceptionCodes.LENGTH_EXCEEDS);
        }
    }

    public void deleteSubscriptionBlockCondition(String conditionValue)
            throws APIManagementException {
        BlockConditionsDTO blockCondition = apiMgtDAO.getSubscriptionBlockCondition(conditionValue, tenantDomain);
        if (blockCondition != null) {
            deleteBlockConditionByUUID(blockCondition.getUUID());
        }
    }

    /**
     * This method is used to get the context of API identified by the given uuid
     *
     * @param uuid api uuid
     * @return apiContext
     * @throws APIManagementException if failed to fetch the context for api uuid
     */
    public String getAPIContext(String uuid) throws APIManagementException {
        return apiMgtDAO.getAPIContext(uuid);
    }

    /**
     * Check whether a string contains illegal charactersA
     *
     * @param toExamine string to examine for illegal characters
     * @return true if found illegal characters, else false
     */
    public boolean containsIllegals(String toExamine) {
        Pattern pattern = Pattern.compile(APIConstants.REGEX_ILLEGAL_CHARACTERS_FOR_API_METADATA);
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }


    /**
     * Check whether the provided information exceeds the maximum length
     * @param field text field to validate
     * @param maxLength maximum allowd length
     * @return true if the length is valid
     */
    public boolean hasValidLength(String field, int maxLength) {
        return field.length() <= maxLength;
    }

    private String getDefaultVersion(APIIdentifier apiid) throws APIManagementException {

        String defaultVersion = null;
        try {
            defaultVersion = apiMgtDAO.getDefaultVersion(apiid);
        } catch (APIManagementException e) {
            handleException("Error while getting default version :" + apiid.getApiName(), e);
        }
        return defaultVersion;
    }


    public String getPublishedDefaultVersion(APIIdentifier apiid) throws APIManagementException {

        String defaultVersion = null;
        try {
            defaultVersion = apiMgtDAO.getPublishedDefaultVersion(apiid);
        } catch (APIManagementException e) {
            handleException("Error while getting published default version :" + apiid.getApiName(), e);
        }
        return defaultVersion;
    }


    private void sendUpdateEventToPreviousDefaultVersion(APIIdentifier apiIdentifier, String organization) throws APIManagementException {
        API api = apiMgtDAO.getLightWeightAPIInfoByAPIIdentifier(apiIdentifier, organization);
        APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.API_UPDATE.name(), tenantId, organization, apiIdentifier.getApiName(),
                api.getId().getId(), api.getUuid(), api.getId().getVersion(), api.getType(), api.getContext(),
                APIUtil.replaceEmailDomainBack(api.getId().getProviderName()),
                api.getStatus(), APIConstants.EventAction.DEFAULT_VERSION);
        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
    }

    public API updateAPI(API api, API existingAPI) throws APIManagementException {

        if (!existingAPI.getStatus().equals(api.getStatus())) {
            throw new APIManagementException("Invalid API update operation involving API status changes");
        }
        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
        //Validate Transports
        validateAndSetTransports(api);
        validateAndSetAPISecurity(api);
        validateKeyManagers(api);
        String publishedDefaultVersion = getPublishedDefaultVersion(api.getId());
        String prevDefaultVersion = getDefaultVersion(api.getId());
        api.setMonetizationEnabled(existingAPI.isMonetizationEnabled());
        Gson gson = new Gson();
        String organization = api.getOrganization();
        Map<String, String> oldMonetizationProperties =
                gson.fromJson(existingAPI.getMonetizationProperties().toString(),
                HashMap.class);
        if (oldMonetizationProperties != null && !oldMonetizationProperties.isEmpty()) {
            Map<String, String> newMonetizationProperties = gson.fromJson(api.getMonetizationProperties().toString(),
                    HashMap.class);
            if (newMonetizationProperties != null) {
                for (Map.Entry<String, String> entry : oldMonetizationProperties.entrySet()) {
                    String newValue = newMonetizationProperties.get(entry.getKey());
                    if (StringUtils.isAllBlank(newValue)) {
                        newMonetizationProperties.put(entry.getKey(), entry.getValue());
                    }
                }
                JSONParser parser = new JSONParser();
                try {
                    JSONObject jsonObj = (JSONObject) parser.parse(gson.toJson(newMonetizationProperties));
                    api.setMonetizationProperties(jsonObj);
                } catch (ParseException e) {
                    throw new APIManagementException("Error when parsing monetization properties ", e);
                }
            }
        }
        api.setVersionTimestamp(existingAPI.getVersionTimestamp());
        updateEndpointSecurity(existingAPI, api);

        if (!existingAPI.getContext().equals(api.getContext())) {
            api.setApiHeaderChanged(true);
        }
        int tenantId = APIUtil.getInternalOrganizationId(organization);
        validateResourceThrottlingTiers(api, tenantDomain);

        if (APIUtil.isSequenceDefined(api.getInSequence()) || APIUtil.isSequenceDefined(api.getOutSequence())
                || APIUtil.isSequenceDefined(api.getFaultSequence())) {
            migrateMediationPoliciesOfAPI(api, tenantDomain, false);
        }
        //Validate Operation Policies
        validateOperationPolicyParameters(api, tenantDomain);

        //get product resource mappings on API before updating the API. Update uri templates on api will remove all
        //product mappings as well.
        List<APIProductResource> productResources = apiMgtDAO.getProductMappingsForAPI(api);
        updateAPI(api, tenantId, userNameWithoutChange);
        updateProductResourceMappings(api, organization, productResources);

        if (log.isDebugEnabled()) {
            log.debug("Successfully updated the API: " + api.getId() + " in the database");
        }

        JSONObject apiLogObject = new JSONObject();
        apiLogObject.put(APIConstants.AuditLogConstants.NAME, api.getId().getApiName());
        apiLogObject.put(APIConstants.AuditLogConstants.CONTEXT, api.getContext());
        apiLogObject.put(APIConstants.AuditLogConstants.VERSION, api.getId().getVersion());
        apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, api.getId().getProviderName());
        try {
            api.setCreatedTime(existingAPI.getCreatedTime());
            apiPersistenceInstance.updateAPI(new Organization(organization), APIMapper.INSTANCE.toPublisherApi(api));
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while updating API details", e);
        }
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                APIConstants.AuditLogConstants.UPDATED, this.username);

        //Validate Transports
        validateAndSetTransports(api);
        validateAndSetAPISecurity(api);
        try {
            api.setCreatedTime(existingAPI.getCreatedTime());
            apiPersistenceInstance.updateAPI(new Organization(organization), APIMapper.INSTANCE.toPublisherApi(api));
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while updating API details", e);
        }


        //notify key manager with API update
        registerOrUpdateResourceInKeyManager(api, tenantDomain);

        int apiId = apiMgtDAO.getAPIID(api.getUuid());
        if (publishedDefaultVersion != null) {
            if (api.isPublishedDefaultVersion() && !api.getId().getVersion().equals(publishedDefaultVersion)) {
                APIIdentifier previousDefaultVersionIdentifier = new APIIdentifier(api.getId().getProviderName(),
                        api.getId().getApiName(), publishedDefaultVersion);
                sendUpdateEventToPreviousDefaultVersion(previousDefaultVersionIdentifier, organization);
            }
        }

        APIConstants.EventAction action = null;
        if (api.isDefaultVersion() ^ api.getId().getVersion().equals(prevDefaultVersion)) {
            action = APIConstants.EventAction.DEFAULT_VERSION;
        }

        APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.API_UPDATE.name(), tenantId, organization, api.getId().getApiName(), apiId,
                api.getUuid(), api.getId().getVersion(), api.getType(), api.getContext(),
                APIUtil.replaceEmailDomainBack(api.getId().getProviderName()), api.getStatus(), action);
        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());

        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher
                    extractor = new RecommenderDetailsExtractor(api, organization, APIConstants.ADD_API);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }

        return api;
    }

    private void validateKeyManagers(API api) throws APIManagementException {

        List<KeyManagerConfigurationDTO> keyManagerConfigurationsByTenant =
                apiMgtDAO.getKeyManagerConfigurationsByOrganization(tenantDomain);
        List<String> configuredMissingKeyManagers = new ArrayList<>();
        for (String keyManager : api.getKeyManagers()) {
            if (!APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS.equals(keyManager)) {
                KeyManagerConfigurationDTO selectedKeyManager = null;
                for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurationsByTenant) {
                    if (keyManager.equals(keyManagerConfigurationDTO.getName())) {
                        selectedKeyManager = keyManagerConfigurationDTO;
                        break;
                    }
                }
                if (selectedKeyManager == null) {
                    configuredMissingKeyManagers.add(keyManager);
                }
            }
        }
        if (!configuredMissingKeyManagers.isEmpty()) {
            throw new APIManagementException(
                    "Key Manager(s) Not found :" + String.join(" , ", configuredMissingKeyManagers),
                    ExceptionCodes.KEY_MANAGER_NOT_REGISTERED);
        }
    }

    /**
     * Update API metadata and resources.
     *
     * @param api      API to update
     * @param tenantId Tenant Id
     * @param username Username of the user who is updating
     * @throws APIManagementException If fails to update API.
     */
    private void updateAPI(API api, int tenantId, String username) throws APIManagementException {

        apiMgtDAO.updateAPI(api, username);
        if (log.isDebugEnabled()) {
            log.debug("Successfully updated the API: " + api.getId() + " metadata in the database");
        }
        updateAPIResources(api, tenantId);
    }

    /**
     * Update resources of the API including local scopes and resource to scope attachments.
     *
     * @param api      API
     * @param tenantId Tenant Id
     * @throws APIManagementException If fails to update local scopes of the API.
     */
    private void updateAPIResources(API api, int tenantId) throws APIManagementException {

        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        APIIdentifier apiIdentifier = api.getId();
        // Get the new URI templates for the API
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        // Get the existing local scope keys attached for the API
        Set<String> oldLocalScopeKeys = apiMgtDAO.getAllLocalScopeKeysForAPI(api.getUuid(), tenantId);
        // Get the existing URI templates for the API
        Set<URITemplate> oldURITemplates = apiMgtDAO.getURITemplatesOfAPI(api.getUuid());
        // Get the new local scope keys from URI templates
        Set<Scope> newLocalScopes = getScopesToRegisterFromURITemplates(api.getId().getApiName(), api.getOrganization(), uriTemplates);
        Set<String> newLocalScopeKeys = newLocalScopes.stream().map(Scope::getKey).collect(Collectors.toSet());
        // Get the existing versioned local scope keys attached for the API
        Set<String> oldVersionedLocalScopeKeys = apiMgtDAO.getVersionedLocalScopeKeysForAPI(api.getUuid(), tenantId);
        // Get the existing versioned local scope keys which needs to be removed (not updated) from the current updating
        // API and remove them from the oldLocalScopeKeys set before sending to KM, so that they will not be removed
        // from KM and can be still used by other versioned APIs.
        Iterator oldLocalScopesItr = oldLocalScopeKeys.iterator();
        while (oldLocalScopesItr.hasNext()) {
            String oldLocalScopeKey = (String) oldLocalScopesItr.next();
            // if the scope is used in versioned APIs and it is not in new local scope key set
            if (oldVersionedLocalScopeKeys.contains(oldLocalScopeKey)
                    && !newLocalScopeKeys.contains(oldLocalScopeKey)) {
                //remove from old local scope key set which will be send to KM
                oldLocalScopesItr.remove();
            }
        }
        validateAndUpdateURITemplates(api, tenantId);
        apiMgtDAO.updateURITemplates(api, tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Successfully updated the URI templates of API: " + apiIdentifier + " in the database");
        }
        // Update the resource scopes of the API in KM.
        // Need to remove the old local scopes and register new local scopes and, update the resource scope mappings
        // using the updated URI templates of the API.
        deleteScopes(oldLocalScopeKeys, tenantId);
        addScopes(newLocalScopes, tenantId);
        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
            KeyManager keyManager = keyManagerDtoEntry.getValue().getKeyManager();
            if (keyManager != null) {
                try {
                    keyManager.updateResourceScopes(api, oldLocalScopeKeys, newLocalScopes, oldURITemplates,
                            uriTemplates);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully updated the resource scopes of API: " + apiIdentifier +
                                " in Key Manager "+ keyManagerDtoEntry.getKey()+" .");
                    }
                } catch (APIManagementException e) {
                    log.error("Error while updating resource to scope attachment in Key Manager " +
                            keyManagerDtoEntry.getKey(), e);
                }
            }
        }
    }

    private void updateEndpointSecurity(API oldApi, API api) throws APIManagementException {
        try {
            if (api.isEndpointSecured() && StringUtils.isBlank(api.getEndpointUTPassword()) &&
                    !StringUtils.isBlank(oldApi.getEndpointUTPassword())) {
                if (log.isDebugEnabled()) {
                    log.debug("Given endpoint security password is empty");
                }
                api.setEndpointUTUsername(oldApi.getEndpointUTUsername());
                api.setEndpointUTPassword(oldApi.getEndpointUTPassword());

                if (log.isDebugEnabled()) {
                    log.debug("Using the previous username and password for endpoint security");
                }
            } else {
                String endpointConfig = api.getEndpointConfig();
                String oldEndpointConfig = oldApi.getEndpointConfig();
                if (StringUtils.isNotEmpty(endpointConfig) && StringUtils.isNotEmpty(oldEndpointConfig)) {
                    JSONObject endpointConfigJson = (JSONObject) new JSONParser().parse(endpointConfig);
                    JSONObject oldEndpointConfigJson = (JSONObject) new JSONParser().parse(oldEndpointConfig);
                    if ((endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY) != null) &&
                            (oldEndpointConfigJson.get(APIConstants.ENDPOINT_SECURITY) != null)) {
                        JSONObject endpointSecurityJson =
                                (JSONObject) endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY);
                        JSONObject oldEndpointSecurityJson =
                                (JSONObject) oldEndpointConfigJson.get(APIConstants.ENDPOINT_SECURITY);
                        if (endpointSecurityJson.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                            if (oldEndpointSecurityJson.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                                EndpointSecurity endpointSecurity = new ObjectMapper().convertValue(
                                        endpointSecurityJson.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION),
                                        EndpointSecurity.class);
                                EndpointSecurity oldEndpointSecurity = new ObjectMapper().convertValue(
                                        oldEndpointSecurityJson.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION),
                                        EndpointSecurity.class);
                                if (endpointSecurity.isEnabled() && oldEndpointSecurity.isEnabled() &&
                                        StringUtils.isBlank(endpointSecurity.getPassword())) {
                                    endpointSecurity.setUsername(oldEndpointSecurity.getUsername());
                                    endpointSecurity.setPassword(oldEndpointSecurity.getPassword());
                                    if (endpointSecurity.getType().equals(APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH)) {
                                        endpointSecurity.setUniqueIdentifier(oldEndpointSecurity.getUniqueIdentifier());
                                        endpointSecurity.setGrantType(oldEndpointSecurity.getGrantType());
                                        endpointSecurity.setTokenUrl(oldEndpointSecurity.getTokenUrl());
                                        endpointSecurity.setClientId(oldEndpointSecurity.getClientId());
                                        endpointSecurity.setClientSecret(oldEndpointSecurity.getClientSecret());
                                        endpointSecurity.setCustomParameters(oldEndpointSecurity.getCustomParameters());
                                    }
                                }
                                endpointSecurityJson.replace(APIConstants.ENDPOINT_SECURITY_PRODUCTION, new JSONParser()
                                        .parse(new ObjectMapper().writeValueAsString(endpointSecurity)));
                            }
                        }
                        if (endpointSecurityJson.get(APIConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                            if (oldEndpointSecurityJson.get(APIConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                                EndpointSecurity endpointSecurity = new ObjectMapper()
                                        .convertValue(endpointSecurityJson.get(APIConstants.ENDPOINT_SECURITY_SANDBOX),
                                                EndpointSecurity.class);
                                EndpointSecurity oldEndpointSecurity = new ObjectMapper()
                                        .convertValue(oldEndpointSecurityJson.get(APIConstants.ENDPOINT_SECURITY_SANDBOX),
                                                EndpointSecurity.class);
                                if (endpointSecurity.isEnabled() && oldEndpointSecurity.isEnabled() &&
                                        StringUtils.isBlank(endpointSecurity.getPassword())) {
                                    endpointSecurity.setUsername(oldEndpointSecurity.getUsername());
                                    endpointSecurity.setPassword(oldEndpointSecurity.getPassword());
                                    if (endpointSecurity.getType().equals(APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH)) {
                                        endpointSecurity.setUniqueIdentifier(oldEndpointSecurity.getUniqueIdentifier());
                                        endpointSecurity.setGrantType(oldEndpointSecurity.getGrantType());
                                        endpointSecurity.setTokenUrl(oldEndpointSecurity.getTokenUrl());
                                        endpointSecurity.setClientId(oldEndpointSecurity.getClientId());
                                        endpointSecurity.setClientSecret(oldEndpointSecurity.getClientSecret());
                                        endpointSecurity.setCustomParameters(oldEndpointSecurity.getCustomParameters());
                                    }
                                }
                                endpointSecurityJson.replace(APIConstants.ENDPOINT_SECURITY_SANDBOX,
                                        new JSONParser()
                                                .parse(new ObjectMapper().writeValueAsString(endpointSecurity)));
                            }
                            endpointConfigJson.replace(APIConstants.ENDPOINT_SECURITY,endpointSecurityJson);
                        }
                    }
                    api.setEndpointConfig(endpointConfigJson.toJSONString());
                }
            }
        } catch (ParseException | JsonProcessingException e) {
            throw new APIManagementException(
                    "Error while processing endpoint security for API " + api.getId().toString(), e);
        }
    }

    @Override
    public void loadMediationPoliciesToAPI(API api, String organization) throws APIManagementException {
        if (APIUtil.isSequenceDefined(api.getInSequence()) || APIUtil.isSequenceDefined(api.getOutSequence())
                || APIUtil.isSequenceDefined(api.getFaultSequence())) {
            Organization org = new Organization(organization);
            String apiUUID = api.getUuid();
            // get all policies
            try {
                List<MediationInfo> localPolicies = apiPersistenceInstance.getAllMediationPolicies(org, apiUUID);
                List<Mediation> globalPolicies = null;
                if (APIUtil.isSequenceDefined(api.getInSequence())) {
                    boolean found = false;
                    for (MediationInfo mediationInfo : localPolicies) {
                        if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equals(mediationInfo.getType())
                                && api.getInSequence().equals(mediationInfo.getName())) {
                            org.wso2.carbon.apimgt.persistence.dto.Mediation mediationPolicy = apiPersistenceInstance
                                        .getMediationPolicy(org, apiUUID, mediationInfo.getId());
                            Mediation mediation = new Mediation();
                            mediation.setConfig(mediationPolicy.getConfig());
                            mediation.setName(mediationPolicy.getName());
                            mediation.setUuid(mediationPolicy.getId());
                            mediation.setType(APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);
                            mediation.setGlobal(false);
                            api.setInSequenceMediation(mediation);
                            found = true;
                            break;
                        }
                    }
                    if (!found) { // global policy
                        if (globalPolicies == null) {
                            globalPolicies = globalMediationPolicyImpl.getAllGlobalMediationPolicies();
                        }
                        for (Mediation m : globalPolicies) {
                            if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equals(m.getType())
                                    && api.getInSequence().equals(m.getName())) {
                                Mediation mediation = globalMediationPolicyImpl.getGlobalMediationPolicy(m.getUuid());
                                mediation.setGlobal(true);
                                api.setInSequenceMediation(mediation);
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (APIUtil.isSequenceDefined(api.getOutSequence())) {
                    boolean found = false;
                    for (MediationInfo mediationInfo : localPolicies) {
                        if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equals(mediationInfo.getType())
                                && api.getOutSequence().equals(mediationInfo.getName())) {
                            org.wso2.carbon.apimgt.persistence.dto.Mediation mediationPolicy = apiPersistenceInstance
                                        .getMediationPolicy(org, apiUUID, mediationInfo.getId());
                            Mediation mediation = new Mediation();
                            mediation.setConfig(mediationPolicy.getConfig());
                            mediation.setName(mediationPolicy.getName());
                            mediation.setUuid(mediationPolicy.getId());
                            mediation.setType(APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT);
                            mediation.setGlobal(false);
                            api.setOutSequenceMediation(mediation);
                            found = true;
                            break;
                        }
                    }
                    if (!found) { // global policy
                        if (globalPolicies == null) {
                            globalPolicies = globalMediationPolicyImpl.getAllGlobalMediationPolicies();
                        }
                        for (Mediation m : globalPolicies) {
                            if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equals(m.getType())
                                    && api.getOutSequence().equals(m.getName())) {
                                Mediation mediation = globalMediationPolicyImpl.getGlobalMediationPolicy(m.getUuid());
                                mediation.setGlobal(true);
                                api.setOutSequenceMediation(mediation);
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (APIUtil.isSequenceDefined(api.getFaultSequence())) {
                    boolean found = false;
                    for (MediationInfo mediationInfo : localPolicies) {
                        if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT.equals(mediationInfo.getType())
                                && api.getFaultSequence().equals(mediationInfo.getName())) {
                            org.wso2.carbon.apimgt.persistence.dto.Mediation mediationPolicy = apiPersistenceInstance
                                        .getMediationPolicy(org, apiUUID, mediationInfo.getId());
                            Mediation mediation = new Mediation();
                            mediation.setConfig(mediationPolicy.getConfig());
                            mediation.setName(mediationPolicy.getName());
                            mediation.setUuid(mediationPolicy.getId());
                            mediation.setType(APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT);
                            mediation.setGlobal(false);
                            api.setFaultSequenceMediation(mediation);
                            found = true;
                            break;
                        }
                    }
                    if (!found) { // global policy
                        if (globalPolicies == null) {
                            globalPolicies = globalMediationPolicyImpl.getAllGlobalMediationPolicies();
                        }
                        for (Mediation m : globalPolicies) {
                            if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT.equals(m.getType())
                                    && api.getFaultSequence().equals(m.getName())) {
                                Mediation mediation = globalMediationPolicyImpl.getGlobalMediationPolicy(m.getUuid());
                                mediation.setGlobal(true);
                                api.setFaultSequenceMediation(mediation);
                                found = true;
                                break;
                            }
                        }
                    }
                }
            } catch (MediationPolicyPersistenceException e) {
                throw new APIManagementException("Error while loading medation policies", e);
            }
        }
    }

    /**
     * This method is used to visualize migrated APIs that has mediation policies attached. A dummy policy is added
     * to all the operations with the mediator's name as the policy name. Here policy Id is not set.
     *
     * @param api      API
     */
    protected void loadMediationPoliciesAsOperationPoliciesToAPI(API api, String organization) throws APIManagementException {
        // This method is used to handle the migration
        OperationPolicy inFlowPolicy = null;
        OperationPolicy outFlowPolicy = null;
        OperationPolicy faultFlowPolicy = null;
        // get all policies
        if (APIUtil.isSequenceDefined(api.getInSequence())) {
            OperationPolicyData existingPolicy = getAPISpecificOperationPolicyByPolicyName(api.getInSequence(),
                    APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, organization, false);
            inFlowPolicy = new OperationPolicy();
            inFlowPolicy.setPolicyName(api.getInSequence());
            inFlowPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST);
            inFlowPolicy.setOrder(1);
            if (existingPolicy != null) {
                inFlowPolicy.setPolicyId(existingPolicy.getPolicyId());
                api.setInSequence(null);
            }
        }
        if (APIUtil.isSequenceDefined(api.getOutSequence())) {
            OperationPolicyData existingPolicy = getAPISpecificOperationPolicyByPolicyName(api.getOutSequence(),
                    APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, organization, false);
            outFlowPolicy = new OperationPolicy();
            outFlowPolicy.setPolicyName(api.getOutSequence());
            outFlowPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE);
            outFlowPolicy.setOrder(1);
            if (existingPolicy != null) {
                outFlowPolicy.setPolicyId(existingPolicy.getPolicyId());
                api.setOutSequence(null);
            }
        }
        if (APIUtil.isSequenceDefined(api.getFaultSequence())) {
            OperationPolicyData existingPolicy = getAPISpecificOperationPolicyByPolicyName(api.getFaultSequence(),
                    APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, organization, false);
            faultFlowPolicy = new OperationPolicy();
            faultFlowPolicy.setPolicyName(api.getFaultSequence());
            faultFlowPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_FAULT);
            faultFlowPolicy.setOrder(1);
            if (existingPolicy != null) {
                faultFlowPolicy.setPolicyId(existingPolicy.getPolicyId());
                api.setFaultSequence(null);
            }
        }

        if (inFlowPolicy != null || outFlowPolicy != null || faultFlowPolicy != null) {
            Set<URITemplate> uriTemplates = api.getUriTemplates();
            for (URITemplate uriTemplate : uriTemplates) {
                List<OperationPolicy> operationPolicies = uriTemplate.getOperationPolicies();
                if (inFlowPolicy != null) {
                    operationPolicies.add(cloneOperationPolicy(inFlowPolicy));
                }
                if (outFlowPolicy != null) {
                    operationPolicies.add(cloneOperationPolicy(outFlowPolicy));
                }
                if (faultFlowPolicy != null) {
                    operationPolicies.add(cloneOperationPolicy(faultFlowPolicy));
                }
            }
        }
    }

    public static OperationPolicy cloneOperationPolicy(OperationPolicy operationPolicy) {

        Gson gson = new Gson();
        OperationPolicy clonedOperationPolicy = gson.fromJson(gson.toJson(operationPolicy), OperationPolicy.class);
        return clonedOperationPolicy;
    }

    /**
     * This method is used to migrate mediation policies of already migrated APIs. If a mediation policies are found
     * for three sequences, they will be imported as an API specific policy and that policy Id will be used.
     *
     * @param api          API
     * @param organization Organization Name
     * @throws APIManagementException
     */
    protected void migrateMediationPoliciesOfAPI(API api, String organization, boolean updatePolicyURLMapping)
            throws APIManagementException {

        Map<String, String> clonedPoliciesMap = new HashMap<>();
        String apiUUID = api.getUuid();

        loadMediationPoliciesToAPI(api, organization);

        if (APIUtil.isSequenceDefined(api.getInSequence())) {
            Mediation inSequenceMediation = api.getInSequenceMediation();
            OperationPolicyData existingPolicy =
                    getAPISpecificOperationPolicyByPolicyName(inSequenceMediation.getName(),
                            APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, organization, false);
            String inFlowPolicyId;
            if (existingPolicy == null) {
                OperationPolicyData inSeqPolicyData =
                        APIUtil.getPolicyDataForMediationFlow(api, APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST,
                                organization);
                inFlowPolicyId = addAPISpecificOperationPolicy(apiUUID, inSeqPolicyData, organization);
            } else {
                inFlowPolicyId = existingPolicy.getPolicyId();
            }
            clonedPoliciesMap.put(inSequenceMediation.getName(), inFlowPolicyId);
            api.setInSequence(null);
            api.setInSequenceMediation(null);
        }

        if (APIUtil.isSequenceDefined(api.getOutSequence())) {
            Mediation outSequenceMediation = api.getOutSequenceMediation();
            OperationPolicyData existingPolicy =
                    getAPISpecificOperationPolicyByPolicyName(outSequenceMediation.getName(),
                            APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, organization, false);
            String outFlowPolicyId;
            if (existingPolicy == null) {
                OperationPolicyData outSeqPolicyData =
                        APIUtil.getPolicyDataForMediationFlow(api, APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE,
                                organization);
                outFlowPolicyId = addAPISpecificOperationPolicy(apiUUID, outSeqPolicyData, organization);
            } else {
                outFlowPolicyId = existingPolicy.getPolicyId();
            }
            clonedPoliciesMap.put(outSequenceMediation.getName(), outFlowPolicyId);
            api.setOutSequence(null);
            api.setOutSequenceMediation(null);
        }

        if (APIUtil.isSequenceDefined(api.getFaultSequence())) {
            Mediation faultSequenceMediation = api.getFaultSequenceMediation();
            OperationPolicyData existingPolicy =
                    getAPISpecificOperationPolicyByPolicyName(faultSequenceMediation.getName(),
                            APIConstants.DEFAULT_POLICY_VERSION, api.getUuid(), null, organization, false);
            String faultFlowPolicyId;
            if (existingPolicy == null) {
                OperationPolicyData faultSeqPolicyData =
                        APIUtil.getPolicyDataForMediationFlow(api, APIConstants.OPERATION_SEQUENCE_TYPE_FAULT,
                                organization);
                faultFlowPolicyId = addAPISpecificOperationPolicy(apiUUID, faultSeqPolicyData, organization);
            } else {
                faultFlowPolicyId = existingPolicy.getPolicyId();
            }

            clonedPoliciesMap.put(faultSequenceMediation.getName(), faultFlowPolicyId);
            api.setFaultSequence(null);
            api.setFaultSequenceMediation(null);
        }

        setMigratedPolicyIdsToPolicies(api, clonedPoliciesMap, updatePolicyURLMapping);
    }

    /**
     * This method will update the policy Id of the selected operations policy from the given cloned policies map.
     *
     * @param api                    API
     * @param clonedPoliciesMap      Cloned policies map
     * @param updatePolicyURLMapping whether to update policy url mapping table or not
     * @throws APIManagementException
     */
    private void setMigratedPolicyIdsToPolicies(API api, Map<String, String> clonedPoliciesMap,
                                                boolean updatePolicyURLMapping)
            throws APIManagementException {

        boolean policyUpdated = false;
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        for (URITemplate uriTemplate : uriTemplates) {
            for (OperationPolicy policy : uriTemplate.getOperationPolicies()) {
                if (policy.getPolicyId() == null) {
                    if (clonedPoliciesMap.containsKey(policy.getPolicyName())) {
                        policy.setPolicyId(clonedPoliciesMap.get(policy.getPolicyName()));
                        policyUpdated = true;
                    }
                }
            }
        }
        if (policyUpdated && updatePolicyURLMapping) {
            apiMgtDAO.addOperationPolicyMapping(uriTemplates);
        }
    }


    /**
     * Returns the subscriber name for the given subscription id.
     *
     * @param subscriptionId The subscription id of the subscriber to be returned
     * @return The subscriber or null if the requested subscriber does not exist
     * @throws APIManagementException if failed to get Subscriber
     */
    @Override
    public String getSubscriber(String subscriptionId) throws APIManagementException {
        return apiMgtDAO.getSubscriberName(subscriptionId);
    }

    /**
     * Returns the claims of subscriber for the given subscriber.
     *
     * @param subscriber The name of the subscriber to be returned
     * @return The looked up claims of the subscriber or null if the requested subscriber does not exist
     * @throws APIManagementException if failed to get Subscriber
     */
    @Override
    public Map<String, String> getSubscriberClaims(String subscriber) throws APIManagementException {
        String tenantDomain = MultitenantUtils.getTenantDomain(subscriber);
        int tenantId = 0;
        Map<String, String> claimMap = new HashMap<>();
        Map<String, String> subscriberClaims = null;
        String configuredClaims = "";
        try {
            tenantId = getTenantId(tenantDomain);
            UserStoreManager userStoreManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getUserStoreManager();
            if (userStoreManager.isExistingUser(subscriber)) {
                subscriberClaims = APIUtil.getClaims(subscriber, tenantId, ClaimsRetriever.DEFAULT_DIALECT_URI);
                APIManagerConfiguration configuration = getAPIManagerConfiguration();
                configuredClaims = configuration.getFirstProperty(APIConstants.API_PUBLISHER_SUBSCRIBER_CLAIMS);
            }
            for (String claimURI : configuredClaims.split(",")) {
                if (subscriberClaims != null) {
                    claimMap.put(claimURI, subscriberClaims.get(claimURI));
                }
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while retrieving tenant id for tenant domain "
                    + tenantDomain, e);
        }
        return claimMap;
    }

    private Set<API> getAssociatedAPIs(APIProduct apiProduct) throws APIManagementException {
        List<APIProductResource> productResources = apiProduct.getProductResources();

        Set<API> apis = new HashSet<>();

        for (APIProductResource productResource : productResources) {
            API api = getAPIbyUUID(productResource.getApiId(),
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            apis.add(api);
        }

        return apis;
    }


    /**
     * This method used to validate and set transports in api
     * @param api
     * @throws APIManagementException
     */
    private void validateAndSetTransports(API api) throws APIManagementException {
        String transports = api.getTransports();
        if (!StringUtils.isEmpty(transports) && !("null".equalsIgnoreCase(transports))) {
            if (transports.contains(",")) {
                StringTokenizer st = new StringTokenizer(transports, ",");
                while (st.hasMoreTokens()) {
                    checkIfValidTransport(st.nextToken());
                }
            } else {
                checkIfValidTransport(transports);
            }
        } else {
            api.setTransports(Constants.TRANSPORT_HTTP + ',' + Constants.TRANSPORT_HTTPS);
        }
    }

    /**
     * This method used to validate and set transports in api product
     * @param apiProduct
     * @throws APIManagementException
     */
    private void validateAndSetTransports(APIProduct apiProduct) throws APIManagementException {
        String transports = apiProduct.getTransports();
        if (!StringUtils.isEmpty(transports) && !("null".equalsIgnoreCase(transports))) {
            if (transports.contains(",")) {
                StringTokenizer st = new StringTokenizer(transports, ",");
                while (st.hasMoreTokens()) {
                    checkIfValidTransport(st.nextToken());
                }
            } else {
                checkIfValidTransport(transports);
            }
        } else {
            apiProduct.setTransports(Constants.TRANSPORT_HTTP + ',' + Constants.TRANSPORT_HTTPS);
        }
    }

    private void validateOperationPolicyParameters(API api, String tenantDomain) throws APIManagementException {

        boolean isOperationPoliciesAllowedForAPIType = true;
        Set<URITemplate> uriTemplates = api.getUriTemplates();

        if (APIConstants.API_TYPE_WS.equals(api.getType()) || APIConstants.API_TYPE_SSE.equals(api.getType())
                || APIConstants.API_TYPE_WEBSUB.equals(api.getType())) {
            if (log.isDebugEnabled()) {
                log.debug("Operation policies are not allowed for " + api.getType() + " APIs");
            }
            isOperationPoliciesAllowedForAPIType = false;
        }

        for (URITemplate uriTemplate : uriTemplates) {
            List<OperationPolicy> operationPolicies = uriTemplate.getOperationPolicies();
            List<OperationPolicy> validatedPolicies = new ArrayList<>();
            if (operationPolicies != null && !operationPolicies.isEmpty() && isOperationPoliciesAllowedForAPIType) {
                for (OperationPolicy policy : operationPolicies) {
                    String policyId = policy.getPolicyId();
                    if (policyId != null) {
                        // First check the API specific operation policy list
                        OperationPolicyData policyData =
                                getAPISpecificOperationPolicyByPolicyId(policyId, api.getUuid(), tenantDomain, false);
                        if (policyData != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("A policy is found for " + policyId + " as " +
                                        policyData.getSpecification().getName()
                                        + ". Validating the policy");
                            }
                            if (policyData.isRevision()) {
                                throw new APIManagementException("Invalid policy selected. " + policyId
                                        + " policy is not found.", ExceptionCodes.INVALID_OPERATION_POLICY);
                            }

                            if (!policyData.getSpecification().getName().equals(policy.getPolicyName()) ||
                                    !policyData.getSpecification().getVersion().equals(policy.getPolicyVersion()) ) {
                                throw new APIManagementException("Applied policy for uriTemplate "
                                        + uriTemplate.getUriTemplate() + " : " + policy.getPolicyName()
                                        + "_" + policy.getPolicyVersion() + " does not match the specification");
                            }

                            OperationPolicySpecification policySpecification = policyData.getSpecification();
                            if (validateAppliedPolicyWithSpecification(policySpecification, policy, api.getType())) {
                                validatedPolicies.add(policy);
                            }
                        } else {
                            OperationPolicyData commonPolicyData =
                                    getCommonOperationPolicyByPolicyId(policyId, tenantDomain, false);
                            if (commonPolicyData != null) {
                                // A common policy is found for specified policy. This will be validated according to the provided
                                // attributes and added to API policy list
                                if (log.isDebugEnabled()) {
                                    log.debug(
                                            "A common policy is found for " + policyId + ". Validating the policy");
                                }

                                if (!commonPolicyData.getSpecification().getName().equals(policy.getPolicyName()) ||
                                        !commonPolicyData.getSpecification().getVersion().equals(policy.getPolicyVersion()) ) {
                                    throw new APIManagementException("Applied policy for uriTemplate "
                                            + uriTemplate.getUriTemplate() + " : " + policy.getPolicyName()
                                            + "_" + policy.getPolicyVersion() + " does not match the specification");
                                }

                                OperationPolicySpecification commonPolicySpec = commonPolicyData.getSpecification();
                                if (validateAppliedPolicyWithSpecification(commonPolicySpec, policy, api.getType())) {
                                    validatedPolicies.add(policy);
                                }
                            } else {
                                throw new APIManagementException("Selected policy " + policyId + " is not found.",
                                        ExceptionCodes.INVALID_OPERATION_POLICY);
                            }
                        }
                    } else {
                        // check the API specific operation policy list
                        OperationPolicyData policyData =
                                getAPISpecificOperationPolicyByPolicyName(policy.getPolicyName(),
                                policy.getPolicyVersion(), api.getUuid(), null, tenantDomain, false);
                        if (policyData != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Policy Id is not defined and an API specific policy is found for "
                                        + policy.getPolicyName() + ". Validating the policy");
                            }
                            OperationPolicySpecification policySpecification = policyData.getSpecification();
                            if (validateAppliedPolicyWithSpecification(policySpecification, policy, api.getType())) {
                                policy.setPolicyId(policyData.getPolicyId());
                                validatedPolicies.add(policy);
                            }
                        } else {
                            OperationPolicyData commonPolicyData =
                                    getCommonOperationPolicyByPolicyName(policy.getPolicyName(),
                                            policy.getPolicyVersion(), tenantDomain, false);
                            if (commonPolicyData != null) {
                                log.info(commonPolicyData.getPolicyId());
                                // A common policy is found for specified policy. This will be validated according to the provided
                                // attributes and added to API policy list
                                if (log.isDebugEnabled()) {
                                    log.debug("Policy Id is not defined and a common policy is found for "
                                            + policy.getPolicyName() + ". Validating the policy");
                                }
                                OperationPolicySpecification commonPolicySpec = commonPolicyData.getSpecification();
                                if (validateAppliedPolicyWithSpecification(commonPolicySpec, policy, api.getType())) {
                                    policy.setPolicyId(commonPolicyData.getPolicyId());
                                    validatedPolicies.add(policy);
                                }
                            } else {
                                log.error("Selected policy " + policy.getPolicyName() + " is not found");
                                throw new APIManagementException("Selected policy " + policy.getPolicyName() + " is not found.",
                                        ExceptionCodes.INVALID_OPERATION_POLICY);
                            }
                        }
                    }
                }
            }
            uriTemplate.setOperationPolicies(validatedPolicies);
        }
    }

    @Override
    public boolean validateAppliedPolicyWithSpecification(OperationPolicySpecification policySpecification,
                                                          OperationPolicy appliedPolicy, String apiType)
            throws APIManagementException {

        //Validate the policy applied direction
        if (!policySpecification.getApplicableFlows().contains(appliedPolicy.getDirection())) {
            throw new APIManagementException(policySpecification.getName() + " cannot be used in the "
                    + appliedPolicy.getDirection() + " flow.",
                    ExceptionCodes.OPERATION_POLICY_NOT_ALLOWED_IN_THE_APPLIED_FLOW);
        }

        //Validate the API type
        if (!policySpecification.getSupportedApiTypes().contains(apiType)) {
            throw new APIManagementException(policySpecification.getName() + " cannot be used for the "
                    + apiType + " API type.",
                    ExceptionCodes.OPERATION_POLICY_NOT_ALLOWED_IN_THE_APPLIED_FLOW);
        }

        //Validate policy Attributes
        if (policySpecification.getPolicyAttributes() != null) {
            for (OperationPolicySpecAttribute attribute : policySpecification.getPolicyAttributes()) {
                if (attribute.isRequired()) {
                    Object appliedPolicyAttribute = appliedPolicy.getParameters().get(attribute.getName());
                    if (appliedPolicyAttribute != null) {
                        if (attribute.getValidationRegex() != null) {
                            Pattern pattern = Pattern.compile(attribute.getValidationRegex(), Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher((String) appliedPolicyAttribute);
                            if (!matcher.matches()) {
                                throw new APIManagementException("Policy attribute " + attribute.getName()
                                        + " regex validation error.",
                                        ExceptionCodes.INVALID_OPERATION_POLICY_PARAMETERS);
                            }
                        }
                    } else {
                        throw new APIManagementException("Required policy attribute " + attribute.getName()
                                + " is not found for the policy " + policySpecification.getName() + " "
                                + appliedPolicy.getDirection() + " flow.",
                                ExceptionCodes.MISSING_MANDATORY_POLICY_ATTRIBUTES);
                    }
                }
            }
        }
        return true;
    }

    /**
     * This method used to select security level according to given api Security
     * @param apiSecurity
     * @return
     */
    private ArrayList<String> selectSecurityLevels(String apiSecurity) {
        ArrayList<String> securityLevels = new ArrayList<>();
        String[] apiSecurityLevels = apiSecurity.split(",");
        boolean isOauth2 = false;
        boolean isMutualSSL = false;
        boolean isBasicAuth = false;
        boolean isApiKey = false;
        boolean isMutualSSLMandatory = false;
        boolean isOauthBasicAuthMandatory = false;

        boolean securitySchemeFound = false;

        for (String apiSecurityLevel : apiSecurityLevels) {
            if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                isOauth2 = true;
                securityLevels.add(APIConstants.DEFAULT_API_SECURITY_OAUTH2);
                securitySchemeFound = true;
            }
            if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                isMutualSSL = true;
                securityLevels.add(APIConstants.API_SECURITY_MUTUAL_SSL);
                securitySchemeFound = true;
            }
            if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_BASIC_AUTH)) {
                isBasicAuth = true;
                securityLevels.add(APIConstants.API_SECURITY_BASIC_AUTH);
                securitySchemeFound = true;
            }
            if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_API_KEY)){
                isApiKey = true;
                securityLevels.add(APIConstants.API_SECURITY_API_KEY);
                securitySchemeFound = true;
            }
            if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY)) {
                isMutualSSLMandatory = true;
                securityLevels.add(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY);
            }
            if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY)) {
                isOauthBasicAuthMandatory = true;
                securityLevels.add(APIConstants.API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY);
            }
        }

        // If no security schema found, set OAuth2 as default
        if (!securitySchemeFound) {
            isOauth2 = true;
            securityLevels.add(APIConstants.DEFAULT_API_SECURITY_OAUTH2);
        }
        // If Only OAuth2/Basic-Auth specified, set it as mandatory
        if (!isMutualSSL && !isOauthBasicAuthMandatory) {
            securityLevels.add(APIConstants.API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY);
        }
        // If Only Mutual SSL specified, set it as mandatory
        if (!isBasicAuth && !isOauth2 && !isApiKey && !isMutualSSLMandatory) {
            securityLevels.add(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY);
        }
        // If OAuth2/Basic-Auth and Mutual SSL protected and not specified the mandatory scheme,
        // set OAuth2/Basic-Auth as mandatory
        if ((isOauth2 || isBasicAuth || isApiKey) && isMutualSSL && !isOauthBasicAuthMandatory && !isMutualSSLMandatory) {
            securityLevels.add(APIConstants.API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY);
        }
        return securityLevels;
    }

    /**
     * To validate the API Security options and set it.
     *
     * @param api Relevant API that need to be validated.
     */
    private void validateAndSetAPISecurity(API api) {
        String apiSecurity = APIConstants.DEFAULT_API_SECURITY_OAUTH2;
        String security = api.getApiSecurity();
        if (security!= null) {
            apiSecurity = security;
            ArrayList<String> securityLevels = selectSecurityLevels(apiSecurity);
            apiSecurity = String.join(",", securityLevels);
        }
        if (log.isDebugEnabled()) {
            log.debug("API " + api.getId() + " has following enabled protocols : " + apiSecurity);
        }

        api.setApiSecurity(apiSecurity);
    }

    /**
     * To validate the API Security options and set it.
     *
     * @param apiProduct Relevant APIProduct that need to be validated.
     */
    private void validateAndSetAPISecurity(APIProduct apiProduct) {
        String apiSecurity = APIConstants.DEFAULT_API_SECURITY_OAUTH2;
        String security = apiProduct.getApiSecurity();
        if (security!= null) {
            apiSecurity = security;
            ArrayList<String> securityLevels = selectSecurityLevels(apiSecurity);
            apiSecurity = String.join(",", securityLevels);
        }
        if (log.isDebugEnabled()) {
            log.debug("APIProduct " + apiProduct.getId() + " has following enabled protocols : " + apiSecurity);
        }
        apiProduct.setApiSecurity(apiSecurity);
    }

    private void checkIfValidTransport(String transport) throws APIManagementException {
        if (!Constants.TRANSPORT_HTTP.equalsIgnoreCase(transport) && !Constants.TRANSPORT_HTTPS.equalsIgnoreCase(transport)
                && !APIConstants.WS_PROTOCOL.equalsIgnoreCase(transport) && !APIConstants.WSS_PROTOCOL.equalsIgnoreCase(transport)) {
            handleException("Unsupported Transport [" + transport + ']');
        }
    }

    private void removeFromGateway(API api, Set<APIRevisionDeployment> gatewaysToRemove,
                                   Set<String> environmentsToAdd) {
        Set<String> environmentsToAddSet = new HashSet<>(environmentsToAdd);
        Set<String> environmentsToRemove = new HashSet<>();
        for (APIRevisionDeployment apiRevisionDeployment : gatewaysToRemove) {
            environmentsToRemove.add(apiRevisionDeployment.getDeployment());
        }
        environmentsToRemove.removeAll(environmentsToAdd);
        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        gatewayManager.unDeployFromGateway(api, tenantDomain, environmentsToRemove);
        if (log.isDebugEnabled()) {
            String logMessage = "API Name: " + api.getId().getApiName() + ", API Version " + api.getId().getVersion()
                    + " deleted from gateway";
            log.debug(logMessage);
        }
    }

    public API createNewAPIVersion(String existingApiId, String newVersion, Boolean isDefaultVersion,
                                   String organization) throws APIManagementException {
        API existingAPI = getAPIbyUUID(existingApiId, organization);
        if (existingAPI == null) {
            throw new APIMgtResourceNotFoundException("API not found for id " + existingApiId,
                    ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, existingApiId));
        }
        if (newVersion.equals(existingAPI.getId().getVersion())) {
            throw new APIMgtResourceAlreadyExistsException(
                    "Version " + newVersion + " exists for api " + existingAPI.getId().getApiName());
        }
        if (APIUtil.isSequenceDefined(existingAPI.getInSequence()) || APIUtil.isSequenceDefined(existingAPI.getOutSequence())
                || APIUtil.isSequenceDefined(existingAPI.getFaultSequence())) {
            migrateMediationPoliciesOfAPI(existingAPI, organization, true);
        }

        existingAPI.setOrganization(organization);
        APIIdentifier existingAPIId = existingAPI.getId();
        String existingAPICreatedTime = existingAPI.getCreatedTime();
        String existingAPIStatus = existingAPI.getStatus();
        boolean isExsitingAPIdefaultVersion = existingAPI.isDefaultVersion();
        String existingContext = existingAPI.getContext();
        String existingVersionTimestamp = existingAPI.getVersionTimestamp();
        APIIdentifier newApiId = new APIIdentifier(existingAPI.getId().getProviderName(),
                existingAPI.getId().getApiName(), newVersion);
        existingAPI.setUuid(null);
        existingAPI.setId(newApiId);
        existingAPI.setStatus(APIConstants.CREATED);
        existingAPI.setDefaultVersion(isDefaultVersion);
        existingAPI.setVersionTimestamp("");

        // We need to change the context by setting the new version
        // This is a change that is coming with the context version strategy
        String existingAPIContextTemplate = existingAPI.getContextTemplate();
        existingAPI.setContext(existingAPIContextTemplate.replace("{version}", newVersion));
        Map<String, List<OperationPolicy>> operationPoliciesMap = extractAndDropOperationPoliciesFromURITemplate(existingAPI.getUriTemplates());
        API newAPI = addAPI(existingAPI);
        String newAPIId = newAPI.getUuid();
        if (!operationPoliciesMap.isEmpty()){
            // clone common or API specific operation policy.
            cloneOperationPoliciesToAPI(existingApiId, newAPI, operationPoliciesMap);
        }
        // copy docs
        List<Documentation> existingDocs = getAllDocumentation(existingApiId, organization);

        if (existingDocs != null) {
            for (Documentation documentation : existingDocs) {
                Documentation newDoc = addDocumentation(newAPIId, documentation, organization);
                DocumentationContent content = getDocumentationContent(existingApiId, documentation.getId(),
                        organization); // TODO see whether we can optimize this
                if (content != null) {
                    addDocumentationContent(newAPIId, newDoc.getId(), organization, content);
                }
            }
        }

        // copy icon
        ResourceFile icon = getIcon(existingApiId, organization);
        if (icon != null) {
            setThumbnailToAPI(newAPIId, icon, organization);
        }

        // copy wsdl
        if (!APIConstants.API_TYPE_SOAPTOREST.equals(existingAPI.getType()) && existingAPI.getWsdlUrl() != null) {
            ResourceFile wsdl = getWSDL(existingApiId, organization);
            if (wsdl != null) {
                addWSDLResource(newAPIId, wsdl, null, organization);
            }
        }

        // copy graphql definition
        String graphQLSchema = getGraphqlSchemaDefinition(existingApiId, organization);
        if(graphQLSchema != null) {
            saveGraphqlSchemaDefinition(newAPIId, graphQLSchema, organization);
        }

        // update old api
        // revert back to old values before update.
        existingAPI.setUuid(existingApiId);
        existingAPI.setStatus(existingAPIStatus);
        existingAPI.setId(existingAPIId);
        existingAPI.setContext(existingContext);
        existingAPI.setCreatedTime(existingAPICreatedTime);
        // update existing api with the original timestamp
        existingAPI.setVersionTimestamp(existingVersionTimestamp);
        if (isDefaultVersion) {
            existingAPI.setDefaultVersion(false);
        } else {
            existingAPI.setDefaultVersion(isExsitingAPIdefaultVersion);
        }

        try {
            apiPersistenceInstance.updateAPI(new Organization(organization),
                    APIMapper.INSTANCE.toPublisherApi(existingAPI));
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while updating API details", e);
        }
        return getAPIbyUUID(newAPIId, organization);
    }

    private void cloneOperationPoliciesToAPI(String oldAPIUuid, API newAPI,
                                             Map<String, List<OperationPolicy>> extractedPoliciesMap)
            throws APIManagementException {
        Map<String, String> clonedPolicies = new HashMap<>();
        Set<URITemplate> uriTemplates = newAPI.getUriTemplates();
        List<ClonePolicyMetadataDTO> toBeClonedPolicyDetails = new ArrayList<>();
        for (URITemplate uriTemplate : uriTemplates) {
            String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
            if (extractedPoliciesMap.containsKey(key)) {
                List<OperationPolicy> operationPolicies = extractedPoliciesMap.get(key);
                for (OperationPolicy operationPolicy : operationPolicies) {
                    String clonedPolicyId;
                    if (!clonedPolicies.containsKey(operationPolicy.getPolicyId())) {
                        clonedPolicyId = UUID.randomUUID().toString();

                        ClonePolicyMetadataDTO toBeClonedSinglePolicyData = new ClonePolicyMetadataDTO();
                        toBeClonedSinglePolicyData.setClonedPolicyUUID(clonedPolicyId);
                        toBeClonedSinglePolicyData.setCurrentPolicyUUID(operationPolicy.getPolicyId());
                        toBeClonedPolicyDetails.add(toBeClonedSinglePolicyData);

                        clonedPolicies.put(operationPolicy.getPolicyId(), clonedPolicyId);
                    } else {
                        clonedPolicyId = clonedPolicies.get(operationPolicy.getPolicyId());
                    }
                    operationPolicy.setPolicyId(clonedPolicyId);
                }
                uriTemplate.setOperationPolicies(operationPolicies);
            }
        }

        if (uriTemplates != null) {
            apiMgtDAO.cloneAPISpecificPoliciesForVersioning(oldAPIUuid, newAPI.getUuid(), newAPI.getOrganization(),
                    toBeClonedPolicyDetails);
            apiMgtDAO.addOperationPolicyMapping(uriTemplates);
        }
    }

    public String retrieveServiceKeyByApiId(int apiId, int tenantId) throws APIManagementException {
        return apiMgtDAO.retrieveServiceKeyByApiId(apiId, tenantId);
    }


    @Override
    public void removeDocumentation(String apiId, String docId, String organization) throws APIManagementException {
        try {
            apiPersistenceInstance.deleteDocumentation(new Organization(organization), apiId, docId);
        } catch (DocumentationPersistenceException e) {
            throw new APIManagementException("Error while deleting the document " + docId);
        }

    }

    /**
     * Updates a given documentation
     *
     * @param apiId         id of the document
     * @param documentation Documentation
     * @param organization identifier of the organization
     * @return updated documentation Documentation
     * @throws APIManagementException if failed to update docs
     */
    public Documentation updateDocumentation(String apiId, Documentation documentation, String organization) throws APIManagementException {

        if (documentation != null) {
            org.wso2.carbon.apimgt.persistence.dto.Documentation mappedDoc = DocumentMapper.INSTANCE
                    .toDocumentation(documentation);
            try {
                org.wso2.carbon.apimgt.persistence.dto.Documentation updatedDoc = apiPersistenceInstance
                        .updateDocumentation(new Organization(organization), apiId, mappedDoc);
                if (updatedDoc != null) {
                    return DocumentMapper.INSTANCE.toDocumentation(updatedDoc);
                }
            } catch (DocumentationPersistenceException e) {
                handleException("Failed to add documentation", e);
            }
        }
        return null;
    }


    @Override
    public Documentation addDocumentation(String uuid, Documentation documentation, String organization) throws APIManagementException {
        if (documentation != null) {
            org.wso2.carbon.apimgt.persistence.dto.Documentation mappedDoc = DocumentMapper.INSTANCE
                    .toDocumentation(documentation);
            try {
                org.wso2.carbon.apimgt.persistence.dto.Documentation addedDoc = apiPersistenceInstance.addDocumentation(
                        new Organization(organization), uuid, mappedDoc);
                if (addedDoc != null) {
                    return DocumentMapper.INSTANCE.toDocumentation(addedDoc);
                }
            } catch (DocumentationPersistenceException e) {
                handleException("Failed to add documentation", e);
            }
        }
        return null;
    }

    @Override
    public boolean isDocumentationExist(String uuid, String docName, String organization) throws APIManagementException {
        boolean exist = false;
        UserContext ctx = null;
        try {
            DocumentSearchResult result = apiPersistenceInstance.searchDocumentation(new Organization(organization), uuid, 0, 0,
                    "name:" + docName, ctx);
            if (result != null && result.getDocumentationList() != null && !result.getDocumentationList().isEmpty()) {
                String returnDocName = result.getDocumentationList().get(0).getName();
                if (returnDocName != null && returnDocName.equals(docName)) {
                    exist = true;
                }
            }
        } catch (DocumentationPersistenceException e) {
            handleException("Failed to search documentation for name " + docName, e);
        }
        return exist;
    }

    /**
     * Returns the details of all the life-cycle changes done per API or API Product
     *
     * @param      uuid Unique UUID of the API or API Product
     * @return List of lifecycle events per given API or API Product
     * @throws APIManagementException if failed to copy docs
     */
    public List<LifeCycleEvent> getLifeCycleEvents(String uuid) throws APIManagementException {

        return apiMgtDAO.getLifeCycleEvents(uuid);
    }

    /**
     * Update the subscription status
     *
     * @param apiId     API Identifier
     * @param subStatus Subscription Status
     * @param appId     Application Id
     * @param organization Organization
     * @throws APIManagementException If failed to update subscription status
     */
    public void updateSubscription(APIIdentifier apiId, String subStatus, int appId, String organization)
            throws APIManagementException {
        apiMgtDAO.updateSubscription(apiId, subStatus, appId, organization);
    }

    /**
     * This method is used to update the subscription
     *
     * @param subscribedAPI subscribedAPI object that represents the new subscription detals
     * @throws APIManagementException if failed to update subscription
     */
    public void updateSubscription(SubscribedAPI subscribedAPI) throws APIManagementException {
        apiMgtDAO.updateSubscription(subscribedAPI);
        subscribedAPI = apiMgtDAO.getSubscriptionByUUID(subscribedAPI.getUUID());
        Identifier identifier =
                subscribedAPI.getAPIIdentifier() != null ? subscribedAPI.getAPIIdentifier() : subscribedAPI.getProductId();
        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
        String orgId = subscribedAPI.getOrganization();
        SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_UPDATE.name(), tenantId, orgId,
                subscribedAPI.getSubscriptionId(), subscribedAPI.getUUID(), identifier.getId(), identifier.getUUID(),
                subscribedAPI.getApplication().getId(), subscribedAPI.getApplication().getUUID(),
                subscribedAPI.getTier().getName(), subscribedAPI.getSubStatus());
        APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
    }

    public void deleteAPI(String apiUuid, String organization) throws APIManagementException {
        boolean isError = false;
        int apiId = -1;
        API api = null;

        // get api object by uuid
        try {
            api = getAPIbyUUID(apiUuid, organization);
        } catch (APIManagementException e) {
            log.error("Error while getting API by uuid for deleting API " + apiUuid + " on organization "
                    + organization);
            log.debug("Following steps will be skipped while deleting API " + apiUuid + "on organization "
                    + organization + " due to api being null. " +
                    "deleting Resource Registration from key managers, deleting on external API stores, " +
                    "event publishing to gateways, logging audit message, extracting API details for " +
                    "the recommendation system. "
            );
            isError = true;
        }

        // get api id from db
        try {
            apiId = apiMgtDAO.getAPIID(apiUuid);
        } catch (APIManagementException e) {
            log.error("Error while getting API ID from DB for deleting API " + apiUuid + " on organization "
                    + organization, e);
            log.debug("Following steps will be skipped while deleting the API " + apiUuid + " on organization "
                    + organization + "due to api id being null. cleanup workflow tasks of the API, " +
                    "delete event publishing to gateways");
            isError = true;
        }

        // DB delete operations
        if (!isError && api != null) {
            try {
                deleteAPIRevisions(apiUuid, organization);
                deleteAPIFromDB(api);
                if (log.isDebugEnabled()) {
                    String logMessage =
                            "API Name: " + api.getId().getApiName() + ", API Version " + api.getId().getVersion()
                                    + " successfully removed from the database.";
                    log.debug(logMessage);
                }

            } catch (APIManagementException e) {
                log.error("Error while executing API delete operations on DB for API " + apiUuid +
                        " on organization " + organization, e);
                isError = true;
            }
        }

        // Deleting Resource Registration from key managers
        if (api != null && api.getId() != null && api.getId().toString() != null) {
            Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
            for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
                KeyManager keyManager = keyManagerDtoEntry.getValue().getKeyManager();
                if (keyManager != null) {
                    try {
                        keyManager.deleteRegisteredResourceByAPIId(api.getId().toString());
                        log.debug("API " + apiUuid + " on organization " + organization +
                                " has successfully removed from the Key Manager " + keyManagerDtoEntry.getKey());
                    } catch (APIManagementException e) {
                        log.error("Error while deleting Resource Registration for API " + apiUuid +
                                " on organization " + organization + " in Key Manager "
                                + keyManagerDtoEntry.getKey(), e);
                    }
                }
            }
        }

        try {
            GatewayArtifactsMgtDAO.getInstance().deleteGatewayArtifacts(apiUuid);
            log.debug("API " + apiUuid + " on organization " + organization +
                    " has successfully removed from the gateway artifacts.");
        } catch (APIManagementException e) {
            log.error("Error while executing API delete operation on gateway artifacts for API " + apiUuid, e);
            isError = true;
        }

        try {
            apiPersistenceInstance.deleteAPI(new Organization(organization), apiUuid);
            log.debug("API " + apiUuid + " on organization " + organization +
                    " has successfully removed from the persistence instance.");
        } catch (APIPersistenceException e) {
            log.error("Error while executing API delete operation on persistence instance for API "
                    + apiUuid + " on organization " + organization, e);
            isError = true;
        }

        // Deleting on external API stores
        if (api != null) {
            // gatewayType check is required when API Management is deployed on
            // other servers to avoid synapse
            //Check if there are already published external APIStores.If yes,removing APIs from them.
            Set<APIStore> apiStoreSet;
            try {
                apiStoreSet = getPublishedExternalAPIStores(apiUuid);
                WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisher();
                if (apiStoreSet != null && !apiStoreSet.isEmpty()) {
                    for (APIStore store : apiStoreSet) {
                        wso2APIPublisher.deleteFromStore(api.getId(), APIUtil.getExternalAPIStore(store.getName(), tenantId));
                    }
                }
            } catch (APIManagementException e) {
                log.error("Error while executing API delete operation on external API stores for API "
                        + apiUuid + " on organization " + organization, e);
                isError = true;
            }
        }

        if (apiId != -1) {
            try {
                cleanUpPendingAPIStateChangeTask(apiId, false);
            } catch (WorkflowException | APIManagementException e) {
                log.error("Error while executing API delete operation on cleanup workflow tasks for API "
                        + apiUuid + " on organization " + organization, e);
                isError = true;
            }
        }

        // Delete event publishing to gateways
        if (api != null && apiId != -1) {
            APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.API_DELETE.name(), tenantId, organization, api.getId().getApiName(), apiId,
                    api.getUuid(), api.getId().getVersion(), api.getType(), api.getContext(),
                    APIUtil.replaceEmailDomainBack(api.getId().getProviderName()),
                    api.getStatus());
            APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
        } else {
            log.debug("Event has not published to gateways due to API id has failed to retrieve from DB for API "
                    + apiUuid + " on organization " + organization);
        }

        // Logging audit message for API delete
        if (api != null) {
            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, api.getId().getApiName());
            apiLogObject.put(APIConstants.AuditLogConstants.VERSION, api.getId().getVersion());
            apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, api.getId().getProviderName());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.DELETED, this.username);
        }

        // Extracting API details for the recommendation system
        if (api != null && recommendationEnvironment != null) {
            RecommenderEventPublisher
                    extractor = new RecommenderDetailsExtractor(api, organization, APIConstants.DELETE_API);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }

        // if one of the above has failed throw an error
        if (isError) {
            throw new APIManagementException("Error while deleting the API " + apiUuid + " on organization "
                    + organization);
        }
    }

    /**
     * Deletes API from the database and delete local scopes and resource scope attachments from KM.
     *
     * @param api API to delete
     * @throws APIManagementException if fails to delete the API
     */
    private void deleteAPIFromDB(API api) throws APIManagementException {

        APIIdentifier apiIdentifier = api.getId();
        int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        // Get local scopes for the given API which are not already assigned for different versions of the same API
        Set<String> localScopeKeysToDelete = apiMgtDAO.getUnversionedLocalScopeKeysForAPI(api.getUuid(), tenantId);
        // Get the URI Templates for the given API to detach the resources scopes from
        Set<URITemplate> uriTemplates = apiMgtDAO.getURITemplatesOfAPI(api.getUuid());
        // Detach all the resource scopes from the API resources in KM
        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
            KeyManager keyManager = keyManagerDtoEntry.getValue().getKeyManager();
            if (keyManager != null) {
                try {
                    keyManager.detachResourceScopes(api, uriTemplates);
                    if (log.isDebugEnabled()) {
                        log.debug("Resource scopes are successfully detached for the API : " + apiIdentifier
                                + " from Key Manager :" + keyManagerDtoEntry.getKey() + ".");
                    }
                    // remove the local scopes from the KM
                    for (String localScope : localScopeKeysToDelete) {
                        keyManager.deleteScope(localScope);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Local scopes are successfully deleted for the API : " + apiIdentifier
                                + " from Key Manager : " + keyManagerDtoEntry.getKey() + ".");
                    }
                } catch (APIManagementException e) {
                    log.error("Error while Detach and Delete Scope from Key Manager " + keyManagerDtoEntry.getKey(), e);
                }
            }
        }
        deleteScopes(localScopeKeysToDelete, tenantId);
        apiMgtDAO.deleteAPI(api.getUuid());
        if (log.isDebugEnabled()) {
            log.debug("API : " + apiIdentifier + " is successfully deleted from the database and Key Manager.");
        }
    }

    public void deleteAPIRevisions(String apiUUID, String organization) throws APIManagementException {
        List<APIRevision> apiRevisionList = apiMgtDAO.getRevisionsListByAPIUUID(apiUUID);
        for (APIRevision apiRevision : apiRevisionList) {
            if (apiRevision.getApiRevisionDeploymentList().size() != 0) {
                undeployAPIRevisionDeployment(apiUUID, apiRevision.getRevisionUUID(),
                        apiRevision.getApiRevisionDeploymentList(), organization);
            }
            deleteAPIRevision(apiUUID, apiRevision.getRevisionUUID(), organization);
        }
    }

    public void deleteAPIProductRevisions(String apiProductUUID, String organization) throws APIManagementException {
        List<APIRevision> apiRevisionList = apiMgtDAO.getRevisionsListByAPIUUID(apiProductUUID);
        for (APIRevision apiRevision : apiRevisionList) {
            if (apiRevision.getApiRevisionDeploymentList().size() != 0) {
                undeployAPIProductRevisionDeployment(apiProductUUID, apiRevision.getRevisionUUID(), apiRevision.getApiRevisionDeploymentList());
            }
            deleteAPIProductRevision(apiProductUUID, apiRevision.getRevisionUUID(), organization);
        }
    }

    /**
     * Update the Tier Permissions
     *
     * @param tierName       Tier Name
     * @param permissionType Permission Type
     * @param roles          Roles
     * @throws APIManagementException If failed to update subscription status
     */
    public void updateTierPermissions(String tierName, String permissionType, String roles) throws APIManagementException {
        apiMgtDAO.updateTierPermissions(tierName, permissionType, roles, tenantId);
    }

    @Override
    public void deleteTierPermissions(String tierName) throws APIManagementException {
        apiMgtDAO.deleteThrottlingPermissions(tierName, tenantId);
    }

    @Override
    public Set<TierPermissionDTO> getTierPermissions() throws APIManagementException {
        return apiMgtDAO.getTierPermissions(tenantId);
    }

    @Override
    public TierPermissionDTO getThrottleTierPermission(String tierName) throws APIManagementException {
        return apiMgtDAO.getThrottleTierPermission(tierName, tenantId);
    }

    /**
     * Update the Tier Permissions
     *
     * @param tierName       Tier Name
     * @param permissionType Permission Type
     * @param roles          Roles
     * @throws APIManagementException If failed to update subscription status
     */
    public void updateThrottleTierPermissions(String tierName, String permissionType, String roles) throws
            APIManagementException {
        apiMgtDAO.updateThrottleTierPermissions(tierName, permissionType, roles, tenantId);
    }

    @Override
    public Set<TierPermissionDTO> getThrottleTierPermissions() throws APIManagementException {
        return apiMgtDAO.getThrottleTierPermissions(tenantId);
    }

    /**
     * Publish API to external stores given by external store Ids
     *
     * @param api              API which need to published
     * @param externalStoreIds APIStore Ids which need to publish API
     * @throws APIManagementException If failed to publish to external stores
     */
    @Override
    public boolean publishToExternalAPIStores(API api, List<String> externalStoreIds) throws APIManagementException {

        Set<APIStore> inputStores = new HashSet<>();
        boolean apiOlderVersionExist = false;
        APIIdentifier apiIdentifier = api.getId();
        for (String store : externalStoreIds) {
            if (StringUtils.isNotEmpty(store)) {
                APIStore inputStore = APIUtil.getExternalAPIStore(store,
                        APIUtil.getTenantIdFromTenantDomain(tenantDomain));
                if (inputStore == null) {
                    String errorMessage = "Error while publishing to external stores. Invalid External Store Id: "
                            + store;
                    log.error(errorMessage);
                    ExceptionCodes exceptionCode = ExceptionCodes.EXTERNAL_STORE_ID_NOT_FOUND;
                    throw new APIManagementException(errorMessage,
                            new ErrorItem(exceptionCode.getErrorMessage(), errorMessage, exceptionCode.getErrorCode(),
                                    exceptionCode.getHttpStatusCode()));
                }
                inputStores.add(inputStore);
            }
        }
        Set<String> versions = getAPIVersions(apiIdentifier.getProviderName(),
                apiIdentifier.getName(), api.getOrganization());
        APIVersionStringComparator comparator = new APIVersionStringComparator();
        for (String tempVersion : versions) {
            if (comparator.compare(tempVersion, apiIdentifier.getVersion()) < 0) {
                apiOlderVersionExist = true;
                break;
            }
        }
        return updateAPIsInExternalAPIStores(api, inputStores, apiOlderVersionExist);
    }

    /**
     * When enabled publishing to external APIStores support,publish the API to external APIStores
     *
     * @param api         The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws APIManagementException If failed to update subscription status
     */
    @Override
    public void publishToExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException {

        Set<APIStore> publishedStores = new HashSet<APIStore>();
        StringBuilder errorStatus = new StringBuilder("Failure to publish to External Stores : ");
        boolean failure = false;

        for (APIStore store : apiStoreSet) {
            APIPublisher publisher = store.getPublisher();

            try {
                // First trying to publish the API to external APIStore
                boolean published;
                String version = ApiMgtDAO.getInstance().getLastPublishedAPIVersionFromAPIStore(api.getId(),
                        store.getName());

                if (apiOlderVersionExist && version != null && !(publisher instanceof WSO2APIPublisher)) {
                    published = publisher.createVersionedAPIToStore(api, store, version);
                    publisher.updateToStore(api, store);
                } else {
                    published = publisher.publishToStore(api, store);
                }

                if (published) { // If published,then save to database.
                    publishedStores.add(store);
                }
            } catch (APIManagementException e) {
                failure = true;
                log.error(e);
                errorStatus.append(store.getDisplayName()).append(',');
            }
        }
        if (!publishedStores.isEmpty()) {
            addExternalAPIStoresDetails(api.getUuid(), publishedStores);
        }

        if (failure) {
            throw new APIManagementException(errorStatus.substring(0, errorStatus.length() - 2));
        }

    }

    /**
     * Update the API to external APIStores and database
     *
     * @param api         The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws APIManagementException If failed to update subscription status
     */
    @Override
    public boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException {
        Set<APIStore> publishedStores = getPublishedExternalAPIStores(api.getUuid());
        Set<APIStore> notPublishedAPIStores = new HashSet<APIStore>();
        Set<APIStore> updateApiStores = new HashSet<APIStore>();
        Set<APIStore> removedApiStores = new HashSet<APIStore>();
        StringBuilder errorStatus = new StringBuilder("Failed to update External Stores : ");
        boolean failure = false;
        if (publishedStores != null) {
            removedApiStores.addAll(publishedStores);
            removedApiStores.removeAll(apiStoreSet);
        }
        for (APIStore apiStore : apiStoreSet) {
            boolean publishedToStore = false;
            if (publishedStores != null) {
                for (APIStore store : publishedStores) {  //If selected external store in edit page is already saved in db
                    if (store.equals(apiStore)) { //Check if there's a modification happened in config file external store definition
                        try {
                            if (!isAPIAvailableInExternalAPIStore(api, apiStore)) {
                                // API is not available
                                continue;
                            }
                        } catch (APIManagementException e) {
                            failure = true;
                            log.error(e);
                            errorStatus.append(store.getDisplayName()).append(',');
                        }
                        publishedToStore = true; //Already the API has published to external APIStore

                        //In this case,the API is already added to external APIStore,thus we don't need to publish it again.
                        //We need to update the API in external Store.
                        //Include to update API in external APIStore
                        updateApiStores.add(APIUtil.getExternalAPIStore(store.getName(), tenantId));
                    }
                }
            }
            if (!publishedToStore) {  //If the API has not yet published to selected external APIStore
                notPublishedAPIStores.add(APIUtil.getExternalAPIStore(apiStore.getName(), tenantId));
            }
        }
        //Publish API to external APIStore which are not yet published
        try {
            publishToExternalAPIStores(api, notPublishedAPIStores, apiOlderVersionExist);
        } catch (APIManagementException e) {
            handleException("Failed to publish API to external Store. ", e);
        }
        //Update the APIs which are already exist in the external APIStore
        updateAPIInExternalAPIStores(api, updateApiStores);
        //Update database saved published APIStore details
        updateExternalAPIStoresDetails(api.getUuid(), updateApiStores);

        deleteFromExternalAPIStores(api, removedApiStores);
        if (failure) {
            throw new APIManagementException(errorStatus.substring(0, errorStatus.length() - 2));
        }
        return true;
    }

    private void deleteFromExternalAPIStores(API api, Set<APIStore> removedApiStores) throws APIManagementException {
        Set<APIStore> removalCompletedStores = new HashSet<APIStore>();
        StringBuilder errorStatus = new StringBuilder("Failed to delete from External Stores : ");
        boolean failure = false;
        for (APIStore store : removedApiStores) {
            APIPublisher publisher =
                    APIUtil.getExternalAPIStore(store.getName(), tenantId).getPublisher();
            try {
                boolean deleted = publisher.deleteFromStore(
                        api.getId(), APIUtil.getExternalAPIStore(store.getName(), tenantId));
                if (deleted) {
                    // If the attempt is successful, database will be
                    // changed deleting the External store mappings.
                    removalCompletedStores.add(store);
                }
            } catch (APIManagementException e) {
                failure = true;
                log.error(e);
                errorStatus.append(store.getDisplayName()).append(',');
            }
        }
        if (!removalCompletedStores.isEmpty()) {
            removeExternalAPIStoreDetails(api.getUuid(), removalCompletedStores);
        }

        if (failure) {
            throw new APIManagementException(errorStatus.substring(0, errorStatus.length() - 2));
        }
    }

    private void removeExternalAPIStoreDetails(String id, Set<APIStore> removalCompletedStores)
            throws APIManagementException {
        apiMgtDAO.deleteExternalAPIStoresDetails(id, removalCompletedStores);
    }

    private boolean isAPIAvailableInExternalAPIStore(API api, APIStore store) throws APIManagementException {
        APIPublisher publisher = store.getPublisher();
        return publisher.isAPIAvailable(api, store);

    }


    /**
     * When enabled publishing to external APIStores support,updating the API existing in external APIStores
     *
     * @param api         The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws APIManagementException If failed to update subscription status
     */

    private void updateAPIInExternalAPIStores(API api, Set<APIStore> apiStoreSet)
            throws APIManagementException {
        if (apiStoreSet != null && !apiStoreSet.isEmpty()) {
            StringBuilder errorStatus = new StringBuilder("Failed to update External Stores : ");
            boolean failure = false;
            for (APIStore store : apiStoreSet) {
                try {
                    APIPublisher publisher = store.getPublisher();
                    publisher.updateToStore(api, store);
                } catch (APIManagementException e) {
                    failure = true;
                    log.error(e);
                    errorStatus.append(store.getDisplayName()).append(',');
                }
            }

            if (failure) {
                throw new APIManagementException(errorStatus.substring(0, errorStatus.length() - 2));
            }
        }


    }

    /**
     * When enabled publishing to external APIStores support,update external apistores data in db
     *
     * @param apiId       The API Identifier which need to update in db
     * @param apiStoreSet The APIStores set which need to update in db
     * @throws APIManagementException If failed to update subscription status
     */

    private void updateExternalAPIStoresDetails(String apiId, Set<APIStore> apiStoreSet)
            throws APIManagementException {
        apiMgtDAO.updateExternalAPIStoresDetails(apiId, apiStoreSet);


    }


    private boolean addExternalAPIStoresDetails(String apiId, Set<APIStore> apiStoreSet) throws APIManagementException {
        return apiMgtDAO.addExternalAPIStoresDetails(apiId, apiStoreSet);
    }

    /**
     * When enabled publishing to external APIStores support,get all the external apistore details which are
     * published and stored in db and which are not unpublished
     *
     * @param apiId The API uuid which need to update in db
     * @throws APIManagementException If failed to update subscription status
     */
    @Override
    public Set<APIStore> getExternalAPIStores(String apiId)
            throws APIManagementException {
        if (APIUtil.isAPIsPublishToExternalAPIStores(tenantId)) {
            SortedSet<APIStore> sortedApiStores = new TreeSet<APIStore>(new APIStoreNameComparator());
            Set<APIStore> publishedStores = apiMgtDAO.getExternalAPIStoresDetails(apiId);
            sortedApiStores.addAll(publishedStores);
            return APIUtil.getExternalAPIStores(sortedApiStores, tenantId);
        } else {
            return null;
        }
    }

    /**
     * When enabled publishing to external APIStores support,get only the published external apistore details which are
     * stored in db
     *
     * @param apiId The API uuid which need to update in db
     * @throws APIManagementException If failed to update subscription status
     */
    @Override
    public Set<APIStore> getPublishedExternalAPIStores(String apiId) throws APIManagementException {
        Set<APIStore> storesSet;
        SortedSet<APIStore> configuredAPIStores = new TreeSet<>(new APIStoreNameComparator());
        configuredAPIStores.addAll(APIUtil.getExternalStores(tenantId));
        if (APIUtil.isAPIsPublishToExternalAPIStores(tenantId)) {
            storesSet = apiMgtDAO.getExternalAPIStoresDetails(apiId);
            //Retains only the stores that contained in configuration
            storesSet.retainAll(configuredAPIStores);
            return storesSet;
        }
        return null;
    }


    @Override
    public boolean isSynapseGateway() throws APIManagementException {
        APIManagerConfiguration config = getAPIManagerConfiguration();
        String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
        return APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType);
    }

    @Override
    public void validateResourceThrottlingTiers(API api, String tenantDomain) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Validating x-throttling tiers defined in swagger api definition resource");
        }
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        checkResourceThrottlingTiersInURITemplates(uriTemplates, tenantDomain);
    }

    @Override
    public void validateResourceThrottlingTiers(String swaggerContent, String tenantDomain) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Validating x-throttling tiers defined in swagger api definition resource");
        }
        APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerContent);
        Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(swaggerContent);
        checkResourceThrottlingTiersInURITemplates(uriTemplates, tenantDomain);
    }

    @Override
    public void validateAPIThrottlingTier(API api, String tenantDomain) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Validating apiLevelPolicy defined in the API");
        }
        Map<String, Tier> tierMap = APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
        if (tierMap != null) {
            String apiLevelPolicy = api.getApiLevelPolicy();
            if (apiLevelPolicy != null && !tierMap.containsKey(apiLevelPolicy)) {
                String message = "Invalid API level throttling tier " + apiLevelPolicy + " found in api definition";
                throw new APIManagementException(message);
            }
        }
    }

    @Override
    public void validateProductThrottlingTier(APIProduct apiProduct, String tenantDomain) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Validating productLevelPolicy defined in the API Product");
        }
        Map<String, Tier> tierMap = APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
        if (tierMap != null) {
            String apiLevelPolicy = apiProduct.getProductLevelPolicy();
            if (apiLevelPolicy != null && !tierMap.containsKey(apiLevelPolicy)) {
                String message = "Invalid Product level throttling tier " + apiLevelPolicy + " found in api definition";
                throw new APIManagementException(message);
            }
        }
    }

    private void checkResourceThrottlingTiersInURITemplates(Set<URITemplate> uriTemplates, String tenantDomain)
            throws APIManagementException {
        Map<String, Tier> tierMap = APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
        if (tierMap != null) {
            for (URITemplate template : uriTemplates) {
                if (template.getThrottlingTier() != null && !tierMap.containsKey(template.getThrottlingTier())) {
                    String message = "Invalid x-throttling tier " + template.getThrottlingTier() +
                            " found in api definition for resource " + template.getHTTPVerb() + " " +
                            template.getUriTemplate();
                    log.error(message);
                    throw new APIManagementException(message, ExceptionCodes.TIER_NAME_INVALID);
                }
            }
        }
    }

    @Override
    public void saveSwaggerDefinition(API api, String jsonText, String organization) throws APIManagementException {

        String apiId;
        if (api.getUuid() != null) {
            apiId = api.getUuid();
        } else if (api.getId().getUUID() != null) {
            apiId = api.getId().getUUID();
        } else {
            apiId = apiMgtDAO.getUUIDFromIdentifier(api.getId().getProviderName(), api.getId().getApiName(),
                    api.getId().getVersion(), organization);
        }
        saveSwaggerDefinition(apiId, jsonText, organization);
    }

    @Override
    public void saveSwaggerDefinition(String apiId, String jsonText, String organization) throws APIManagementException {
        try {
            apiPersistenceInstance.saveOASDefinition(new Organization(organization), apiId, jsonText);
        } catch (OASPersistenceException e) {
            throw new APIManagementException("Error while persisting OAS definition ", e);
        }
    }

    @Override
    public void addAPIProductSwagger(String productId, Map<API, List<APIProductResource>> apiToProductResourceMapping,
                APIProduct apiProduct, String orgId) throws APIManagementException {
        APIDefinition parser = new OAS3Parser();
        SwaggerData swaggerData = new SwaggerData(apiProduct);
        String apiProductSwagger = parser.generateAPIDefinition(swaggerData);
        apiProductSwagger = OASParserUtil.updateAPIProductSwaggerOperations(apiToProductResourceMapping, apiProductSwagger);
        saveSwaggerDefinition(productId, apiProductSwagger, orgId);
        apiProduct.setDefinition(apiProductSwagger);
    }

    @Override
    public void updateAPIProductSwagger(String productId, Map<API,
            List<APIProductResource>> apiToProductResourceMapping, APIProduct apiProduct, String orgId)
            throws APIManagementException {
        APIDefinition parser = new OAS3Parser();
        SwaggerData updatedData = new SwaggerData(apiProduct);
        String existingProductSwagger = getOpenAPIDefinition(productId, orgId);
        String updatedProductSwagger = parser.generateAPIDefinition(updatedData, existingProductSwagger);
        updatedProductSwagger = OASParserUtil.updateAPIProductSwaggerOperations(apiToProductResourceMapping,
                updatedProductSwagger);
        saveSwaggerDefinition(productId, updatedProductSwagger, orgId);
        apiProduct.setDefinition(updatedProductSwagger);
    }

    /**
     * This method is to change registry lifecycle states for an API or API Product artifact
     *
     * @param orgId          UUID of the organization
     * @param apiTypeWrapper API Type Wrapper
     * @param action         Action which need to execute from registry lifecycle
     * @param checklist      checklist items
     * @return APIStateChangeResponse API workflow state and WorkflowResponse
     */
    @Override
    public APIStateChangeResponse changeLifeCycleStatus(String orgId, ApiTypeWrapper apiTypeWrapper, String action,
                                                        Map<String, Boolean> checklist) throws APIManagementException{
        APIStateChangeResponse response = new APIStateChangeResponse();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(this.tenantDomain, true);

            String targetStatus;
            String providerName;
            String apiName;
            String apiContext;
            String apiType;
            String apiVersion;
            String currentStatus;
            String uuid;
            int apiOrApiProductId;
            boolean isApiProduct = apiTypeWrapper.isAPIProduct();
            String workflowType;

            if (isApiProduct) {
                APIProduct apiProduct = apiTypeWrapper.getApiProduct();
                providerName = apiProduct.getId().getProviderName();
                apiName = apiProduct.getId().getName();
                apiContext = apiProduct.getContext();
                apiType = apiProduct.getType();
                apiVersion = apiProduct.getId().getVersion();
                currentStatus = apiProduct.getState();
                uuid = apiProduct.getUuid();
                apiOrApiProductId = apiMgtDAO.getAPIProductId(apiTypeWrapper.getApiProduct().getId());
                workflowType = WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE;
            } else {
                API api = apiTypeWrapper.getApi();
                providerName = api.getId().getProviderName();
                apiName = api.getId().getApiName();
                apiContext = api.getContext();
                apiType = api.getType();
                apiVersion = api.getId().getVersion();
                currentStatus = api.getStatus();
                uuid = api.getUuid();
                apiOrApiProductId = apiMgtDAO.getAPIID(uuid);
                workflowType = WorkflowConstants.WF_TYPE_AM_API_STATE;
            }
            String gatewayVendor = apiMgtDAO.getGatewayVendorByAPIUUID(uuid);

            WorkflowStatus apiWFState = null;
            WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiOrApiProductId),
                    workflowType);
            if (wfDTO != null) {
                apiWFState = wfDTO.getStatus();
            }

            // if the workflow has started, then executor should not fire again
            if (!WorkflowStatus.CREATED.equals(apiWFState)) {
                response = executeStateChangeWorkflow(currentStatus, action, apiName, apiContext, apiType,
                        apiVersion, providerName, apiOrApiProductId, uuid, gatewayVendor, workflowType);
                // get the workflow state once the executor is executed.
                wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiOrApiProductId),
                        workflowType);
                if (wfDTO != null) {
                    apiWFState = wfDTO.getStatus();
                    response.setStateChangeStatus(apiWFState.toString());
                } else {
                    response.setStateChangeStatus(WorkflowStatus.APPROVED.toString());
                }
            }

            // only change the lifecycle if approved
            // apiWFState is null when simple wf executor is used because wf state is not stored in the db.
            if (WorkflowStatus.APPROVED.equals(apiWFState) || apiWFState == null) {
                LifeCycleUtils.changeLifecycle(this.username, this, orgId, apiTypeWrapper, action, checklist);
                JSONObject apiLogObject = new JSONObject();
                apiLogObject.put(APIConstants.AuditLogConstants.NAME, apiName);
                apiLogObject.put(APIConstants.AuditLogConstants.CONTEXT, apiContext);
                apiLogObject.put(APIConstants.AuditLogConstants.VERSION, apiVersion);
                apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, providerName);
                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                        APIConstants.AuditLogConstants.LIFECYCLE_CHANGED, this.username);
            }
        } catch (APIPersistenceException e) {
            handleException("Error while accessing persistence layer", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return response;
    }
    /**
     * Execute state change workflow
     *
     * @param currentStatus     Current Status of the API or API Product
     * @param action            LC state change action
     * @param apiName           Name of API or API Product
     * @param apiContext        Context of API or API Product
     * @param apiType           API Type
     * @param apiVersion        Version of API or API Product
     * @param providerName      Provider of API or API Product
     * @param apiOrApiProductId Unique ID API or API Product
     * @param uuid              UUID of the API or API Product
     * @param gatewayVendor     Gateway vendor
     * @param workflowType      Workflow Type
     * @return  APIStateChangeResponse
     * @throws APIManagementException Error when executing the state change workflow
     */
    private APIStateChangeResponse executeStateChangeWorkflow(String currentStatus, String action, String apiName,
                                                              String apiContext, String apiType, String apiVersion,
                                                              String providerName, int apiOrApiProductId, String uuid,
                                                              String gatewayVendor, String workflowType)
            throws APIManagementException {

        APIStateChangeResponse response = new APIStateChangeResponse();
        try {
            WorkflowExecutor apiStateWFExecutor =
             WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
            APIStateWorkflowDTO apiStateWorkflow = setAPIStateWorkflowDTOParameters(currentStatus, action, apiName,
             apiContext, apiType, apiVersion, providerName, apiOrApiProductId, uuid, gatewayVendor, workflowType,
                    apiStateWFExecutor);
            WorkflowResponse workflowResponse = apiStateWFExecutor.execute(apiStateWorkflow);
            response.setWorkflowResponse(workflowResponse);
        } catch (WorkflowException e) {
            handleException("Failed to execute workflow for life cycle status change : " + e.getMessage(), e);
        }
        return response;
    }

    /**
     * Set API or API Product state change workflow parameters
     *
     * @param currentStatus Current state of the API or API Product
     * @param action        LC state change action
     * @param name          Name of the API or API Product
     * @param context       Context of the API or API Product
     * @param apiType       API or API Product
     * @param version       Version of API or API Product
     * @param providerName  Owner of the API or API Product
     * @param apiOrApiProductId Unique ID of the API or API Product
     * @param uuid              Unique UUID of the API or API Product
     * @param gatewayVendor     Gateway Vendor
     * @param workflowType      Workflow Type
     * @param apiStateWFExecutor    WorkflowExecutor
     * @return APIStateWorkflowDTO Object
     */
    private APIStateWorkflowDTO setAPIStateWorkflowDTOParameters(String currentStatus, String action, String name,
                                                                 String context, String apiType, String version,
                                                                 String providerName, int apiOrApiProductId,
                                                                 String uuid, String gatewayVendor, String workflowType,
                                                                 WorkflowExecutor apiStateWFExecutor) {

        WorkflowProperties workflowProperties = getAPIManagerConfiguration().getWorkflowProperties();
        APIStateWorkflowDTO stateWorkflowDTO = new APIStateWorkflowDTO();
        stateWorkflowDTO.setApiCurrentState(currentStatus);
        stateWorkflowDTO.setApiLCAction(action);
        stateWorkflowDTO.setApiName(name);
        stateWorkflowDTO.setApiContext(context);
        stateWorkflowDTO.setApiType(apiType);
        stateWorkflowDTO.setApiVersion(version);
        stateWorkflowDTO.setApiProvider(providerName);
        stateWorkflowDTO.setGatewayVendor(gatewayVendor);
        stateWorkflowDTO.setCallbackUrl(workflowProperties.getWorkflowCallbackAPI());
        stateWorkflowDTO.setExternalWorkflowReference(apiStateWFExecutor.generateUUID());
        stateWorkflowDTO.setTenantId(tenantId);
        stateWorkflowDTO.setTenantDomain(this.tenantDomain);
        stateWorkflowDTO.setWorkflowType(workflowType);
        stateWorkflowDTO.setStatus(WorkflowStatus.CREATED);
        stateWorkflowDTO.setCreatedTime(System.currentTimeMillis());
        stateWorkflowDTO.setWorkflowReference(Integer.toString(apiOrApiProductId));
        stateWorkflowDTO.setInvoker(this.username);
        stateWorkflowDTO.setApiUUID(uuid);
        String workflowDescription = "Pending lifecycle state change action: " + action;
        stateWorkflowDTO.setWorkflowDescription(workflowDescription);
        return stateWorkflowDTO;
    }

    private List<API> getAPIVersionsByProviderAndName(String provider, String apiName, String organization)
            throws APIManagementException {
        return apiMgtDAO.getAllAPIVersions(apiName, provider);
    }


    /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param uuid  ID of the API
     * @param orgId Identifier of an organization
     * @return Map<String, Object> a map with lifecycle data
     */
    public Map<String, Object> getAPILifeCycleData(String uuid, String orgId) throws APIManagementException {

        API api = getLightweightAPIByUUID(uuid, orgId);
        return getApiOrApiProductLifecycleData(api.getStatus());
    }

    private Map<String, Object> getApiOrApiProductLifecycleData(String status) throws APIManagementException {

        Map<String, Object> lcData = new HashMap<String, Object>();
        List<String> actionsList;
        actionsList = LCManagerFactory.getInstance().getLCManager().getAllowedActionsForState(status);
        if (actionsList != null) {
            String[] actionsArray = new String[actionsList.size()];
            actionsArray = actionsList.toArray(actionsArray);
            lcData.put(APIConstants.LC_NEXT_STATES, actionsArray);
        }
        ArrayList<CheckListItem> checkListItems = new ArrayList<CheckListItem>();
        List<String> checklistItemsList =
                LCManagerFactory.getInstance().getLCManager().getCheckListItemsForState(status);
        if (checklistItemsList != null) {
            for (String name : checklistItemsList) {
                CheckListItem item = new CheckListItem();
                item.setName(name);
                item.setValue("false");
                checkListItems.add(item);
            }
        }
        lcData.put("items", checkListItems);
        status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase(); // First letter capital
        lcData.put(APIConstants.LC_STATUS, status);
        return lcData;
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {
        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }

    /**
     * Deploy policy to global CEP and persist the policy object
     *
     * @param policy policy object
     */
    public void addPolicy(Policy policy) throws APIManagementException {

        if (policy instanceof APIPolicy) {
            APIPolicy apiPolicy = (APIPolicy) policy;
            //Check if there's a policy exists before adding the new policy
            Policy existingPolicy = getAPIPolicy(userNameWithoutChange, apiPolicy.getPolicyName());
            if (existingPolicy != null) {
                handleException("Advanced Policy with name " + apiPolicy.getPolicyName() + " already exists");
            }
            apiPolicy.setUserLevel(PolicyConstants.ACROSS_ALL);
            apiPolicy = apiMgtDAO.addAPIPolicy(apiPolicy);
            List<Integer> addedConditionGroupIds = new ArrayList<>();
            for (Pipeline pipeline : apiPolicy.getPipelines()) {
                addedConditionGroupIds.add(pipeline.getId());
            }
            APIPolicyEvent apiPolicyEvent = new APIPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), tenantId,
                    apiPolicy.getTenantDomain(), apiPolicy.getPolicyId(), apiPolicy.getPolicyName(),
                    apiPolicy.getDefaultQuotaPolicy().getType(), addedConditionGroupIds, null);
            APIUtil.sendNotification(apiPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else if (policy instanceof ApplicationPolicy) {
            ApplicationPolicy appPolicy = (ApplicationPolicy) policy;
            //Check if there's a policy exists before adding the new policy
            Policy existingPolicy = getApplicationPolicy(userNameWithoutChange, appPolicy.getPolicyName());
            if (existingPolicy != null) {
                handleException("Application Policy with name " + appPolicy.getPolicyName() + " already exists");
            }
            apiMgtDAO.addApplicationPolicy(appPolicy);
            //policy id is not set. retrieving policy to get the id.
            ApplicationPolicy retrievedPolicy = apiMgtDAO.getApplicationPolicy(appPolicy.getPolicyName(), tenantId);
            ApplicationPolicyEvent applicationPolicyEvent = new ApplicationPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), tenantId,
                    appPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(), appPolicy.getPolicyName(),
                    appPolicy.getDefaultQuotaPolicy().getType());
            APIUtil.sendNotification(applicationPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else if (policy instanceof SubscriptionPolicy) {
            SubscriptionPolicy subPolicy = (SubscriptionPolicy) policy;
            //Check if there's a policy exists before adding the new policy
            Policy existingPolicy = getSubscriptionPolicy(userNameWithoutChange, subPolicy.getPolicyName());
            if (existingPolicy != null) {
                handleException("Subscription Policy with name " + subPolicy.getPolicyName() + " already exists");
            }
            apiMgtDAO.addSubscriptionPolicy(subPolicy);
            String monetizationPlan = subPolicy.getMonetizationPlan();
            Map<String, String> monetizationPlanProperties = subPolicy.getMonetizationPlanProperties();
            if (StringUtils.isNotBlank(monetizationPlan) && MapUtils.isNotEmpty(monetizationPlanProperties)) {
                createMonetizationPlan(subPolicy);
            }
            //policy id is not set. retrieving policy to get the id.
            SubscriptionPolicy retrievedPolicy = apiMgtDAO.getSubscriptionPolicy(subPolicy.getPolicyName(), tenantId);
            SubscriptionPolicyEvent subscriptionPolicyEvent = new SubscriptionPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), tenantId, subPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(),
                    subPolicy.getPolicyName(), subPolicy.getDefaultQuotaPolicy().getType(),
                    subPolicy.getRateLimitCount(),subPolicy.getRateLimitTimeUnit(), subPolicy.isStopOnQuotaReach(),
                    subPolicy.getGraphQLMaxDepth(),subPolicy.getGraphQLMaxComplexity(),subPolicy.getSubscriberCount());
            APIUtil.sendNotification(subscriptionPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else if (policy instanceof GlobalPolicy) {
            GlobalPolicy globalPolicy = (GlobalPolicy) policy;

            // checking if policy already exist
            Policy existingPolicy = getGlobalPolicy(globalPolicy.getPolicyName());
            if (existingPolicy != null) {
                throw new APIManagementException("Policy name already exists");
            }

            apiMgtDAO.addGlobalPolicy(globalPolicy);

            KeyTemplateEvent keyTemplateEvent = new KeyTemplateEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.CUSTOM_POLICY_ADD.name(), tenantId, tenantDomain,
                    "add", globalPolicy.getKeyTemplate());
            APIUtil.sendNotification(keyTemplateEvent, APIConstants.NotifierType.KEY_TEMPLATE.name());

            GlobalPolicy retrievedPolicy = apiMgtDAO.getGlobalPolicy(globalPolicy.getPolicyName());
            GlobalPolicyEvent globalPolicyEvent = new GlobalPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), tenantId,
                    globalPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(),
                    globalPolicy.getPolicyName());
            APIUtil.sendNotification(globalPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else {
            String msg = "Policy type " + policy.getClass().getName() + " is not supported";
            log.error(msg);
            throw new UnsupportedPolicyTypeException(msg);
        }
    }

    @Override
    public void configureMonetizationInAPIArtifact(API api) throws APIManagementException {

        Organization org = new Organization(api.getOrganization());
        try {
            apiPersistenceInstance.updateAPI(org, APIMapper.INSTANCE.toPublisherApi(api));
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while updating API details", e);
        }
    }

    /**
     * This methods creates a monetization plan for a given subscription policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws APIManagementException if failed to create a monetization plan
     */
    private boolean createMonetizationPlan(SubscriptionPolicy subPolicy) throws APIManagementException {

        Monetization monetizationImplementation = getMonetizationImplClass();
        if (monetizationImplementation != null) {
            try {
                return monetizationImplementation.createBillingPlan(subPolicy);
            } catch (MonetizationException e) {
                APIUtil.handleException("Failed to create monetization plan for : " + subPolicy.getPolicyName(), e);
            }
        }
        return false;
    }

    /**
     * This methods updates the monetization plan for a given subscription policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws APIManagementException if failed to update the plan
     */
    private boolean updateMonetizationPlan(SubscriptionPolicy subPolicy) throws APIManagementException {

        Monetization monetizationImplementation = getMonetizationImplClass();
        if (monetizationImplementation != null) {
            try {
                return monetizationImplementation.updateBillingPlan(subPolicy);
            } catch (MonetizationException e) {
                APIUtil.handleException("Failed to update monetization plan for : " + subPolicy.getPolicyName(), e);
            }
        }
        return false;
    }

    /**
     * This methods delete the monetization plan for a given subscription policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws APIManagementException if failed to delete the plan
     */
    private boolean deleteMonetizationPlan(SubscriptionPolicy subPolicy) throws APIManagementException {

        Monetization monetizationImplementation = getMonetizationImplClass();
        if (monetizationImplementation != null) {
            try {
                return monetizationImplementation.deleteBillingPlan(subPolicy);
            } catch (MonetizationException e) {
                APIUtil.handleException("Failed to delete monetization plan of : " + subPolicy.getPolicyName(), e);
            }
        }
        return false;
    }

    /**
     * This methods loads the monetization implementation class
     *
     * @return monetization implementation class
     * @throws APIManagementException if failed to load monetization implementation class
     */
    public Monetization getMonetizationImplClass() throws APIManagementException {

        APIManagerConfiguration configuration = ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        Monetization monetizationImpl = null;
        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            String monetizationImplClass = configuration.getMonetizationConfigurationDto().getMonetizationImpl();
            if (monetizationImplClass == null) {
                monetizationImpl = new DefaultMonetizationImpl();
            } else {
                try {
                    monetizationImpl = (Monetization) APIUtil.getClassInstance(monetizationImplClass);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    APIUtil.handleException("Failed to load monetization implementation class.", e);
                }
            }
        }
        return monetizationImpl;
    }

    public void updatePolicy(Policy policy) throws APIManagementException {

        String oldKeyTemplate = null;
        String newKeyTemplate = null;
        if (policy instanceof APIPolicy) {
            APIPolicy apiPolicy = (APIPolicy) policy;
            apiPolicy.setUserLevel(PolicyConstants.ACROSS_ALL);
            //TODO this has done due to update policy method not deleting the second level entries when delete on cascade
            //TODO Need to fix appropriately
            List<Pipeline> pipelineList = apiPolicy.getPipelines();
            if (pipelineList != null && pipelineList.size() != 0) {
                Iterator<Pipeline> pipelineIterator = pipelineList.iterator();
                while (pipelineIterator.hasNext()) {
                    Pipeline pipeline = pipelineIterator.next();
                    if (!pipeline.isEnabled()) {
                        pipelineIterator.remove();
                    } else {
                        if (pipeline.getConditions() != null && pipeline.getConditions().size() != 0) {
                            Iterator<Condition> conditionIterator = pipeline.getConditions().iterator();
                            while (conditionIterator.hasNext()) {
                                Condition condition = conditionIterator.next();
                                if (JavaUtils.isFalseExplicitly(condition.getConditionEnabled())) {
                                    conditionIterator.remove();
                                }
                            }
                        } else {
                            pipelineIterator.remove();
                        }
                    }
                }
            }
            APIPolicy existingPolicy = apiMgtDAO.getAPIPolicy(policy.getPolicyName(), policy.getTenantId());
            apiPolicy = apiMgtDAO.updateAPIPolicy(apiPolicy);
            //TODO rename level to  resource or appropriate name

            APIManagerConfiguration config = getAPIManagerConfiguration();
            if (log.isDebugEnabled()) {
                log.debug("Calling invalidation cache for API Policy for tenant ");
            }
            String policyContext = APIConstants.POLICY_CACHE_CONTEXT + "/t/" + apiPolicy.getTenantDomain()
                    + "/";
            invalidateResourceCache(policyContext, null, Collections.EMPTY_SET);
            List<Integer> addedConditionGroupIds = new ArrayList<>();
            List<Integer> deletedConditionGroupIds = new ArrayList<>();
            for (Pipeline pipeline : existingPolicy.getPipelines()) {
                deletedConditionGroupIds.add(pipeline.getId());
            }
            for (Pipeline pipeline : apiPolicy.getPipelines()) {
                addedConditionGroupIds.add(pipeline.getId());
            }
            APIPolicyEvent apiPolicyEvent = new APIPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_UPDATE.name(), tenantId,
                    apiPolicy.getTenantDomain(), apiPolicy.getPolicyId(), apiPolicy.getPolicyName(),
                    apiPolicy.getDefaultQuotaPolicy().getType(), addedConditionGroupIds, deletedConditionGroupIds);
            APIUtil.sendNotification(apiPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else if (policy instanceof ApplicationPolicy) {
            ApplicationPolicy appPolicy = (ApplicationPolicy) policy;
            apiMgtDAO.updateApplicationPolicy(appPolicy);
            //policy id is not set. retrieving policy to get the id.
            ApplicationPolicy retrievedPolicy = apiMgtDAO.getApplicationPolicy(appPolicy.getPolicyName(), tenantId);
            ApplicationPolicyEvent applicationPolicyEvent = new ApplicationPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_UPDATE.name(), tenantId,
                    appPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(), appPolicy.getPolicyName(),
                    appPolicy.getDefaultQuotaPolicy().getType());
            APIUtil.sendNotification(applicationPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else if (policy instanceof SubscriptionPolicy) {
            SubscriptionPolicy subPolicy = (SubscriptionPolicy) policy;
            apiMgtDAO.updateSubscriptionPolicy(subPolicy);
            String monetizationPlan = subPolicy.getMonetizationPlan();
            Map<String, String> monetizationPlanProperties = subPolicy.getMonetizationPlanProperties();
            //call the monetization extension point to create plans (if any)
            if (StringUtils.isNotBlank(monetizationPlan) && MapUtils.isNotEmpty(monetizationPlanProperties)) {
                updateMonetizationPlan(subPolicy);
            }
            //policy id is not set. retrieving policy to get the id.
            SubscriptionPolicy retrievedPolicy = apiMgtDAO.getSubscriptionPolicy(subPolicy.getPolicyName(), tenantId);
            SubscriptionPolicyEvent subscriptionPolicyEvent = new SubscriptionPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_UPDATE.name(), tenantId,subPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(),
                    subPolicy.getPolicyName(), subPolicy.getDefaultQuotaPolicy().getType(),
                    subPolicy.getRateLimitCount(),subPolicy.getRateLimitTimeUnit(), subPolicy.isStopOnQuotaReach(),subPolicy.getGraphQLMaxDepth(),
                    subPolicy.getGraphQLMaxComplexity(), subPolicy.getSubscriberCount());
            APIUtil.sendNotification(subscriptionPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else if (policy instanceof GlobalPolicy) {
            GlobalPolicy globalPolicy = (GlobalPolicy) policy;

            // getting key templates before updating database
            GlobalPolicy oldGlobalPolicy = apiMgtDAO.getGlobalPolicy(policy.getPolicyName());
            oldKeyTemplate = oldGlobalPolicy.getKeyTemplate();
            newKeyTemplate = globalPolicy.getKeyTemplate();

            apiMgtDAO.updateGlobalPolicy(globalPolicy);

            GlobalPolicy retrievedPolicy = apiMgtDAO.getGlobalPolicy(globalPolicy.getPolicyName());
            GlobalPolicyEvent globalPolicyEvent = new GlobalPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_UPDATE.name(), tenantId,
                    globalPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(),
                    globalPolicy.getPolicyName());
            APIUtil.sendNotification(globalPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else {
            String msg = "Policy type " + policy.getClass().getName() + " is not supported";
            log.error(msg);
            throw new UnsupportedPolicyTypeException(msg);
        }
        //publishing keytemplate after update
        if (oldKeyTemplate != null && newKeyTemplate != null) {
            KeyTemplateEvent keyTemplateEvent = new KeyTemplateEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                    tenantId, tenantDomain, APIConstants.EventType.CUSTOM_POLICY_UPDATE.name(),
                    "update", oldKeyTemplate, newKeyTemplate );
            APIUtil.sendNotification(keyTemplateEvent, APIConstants.NotifierType.KEY_TEMPLATE.name());
        }
    }

    /**
     * @param username username to recognize tenant
     * @param level    policy level to be applied
     * @return
     * @throws APIManagementException
     */
    public String[] getPolicyNames(String username, String level) throws APIManagementException {
        String[] policyNames = apiMgtDAO.getPolicyNames(level, username);
        return policyNames;
    }

    /**
     * @param username    username to recognize the tenant
     * @param policyLevel policy level
     * @param policyName  name of the policy to be deleted
     * @throws APIManagementException
     */
    public void deletePolicy(String username, String policyLevel, String policyName) throws APIManagementException {
        int tenantID = APIUtil.getTenantId(username);

        if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
            //need to load whole policy object to get the pipelines
            APIPolicy policy = apiMgtDAO.getAPIPolicy(policyName, APIUtil.getTenantId(username));
            List<Integer> deletedConditionGroupIds = new ArrayList<>();
            for (Pipeline pipeline : policy.getPipelines()) {
                deletedConditionGroupIds.add(pipeline.getId());
            }
            APIPolicyEvent apiPolicyEvent = new APIPolicyEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.POLICY_DELETE.name(), tenantId, policy.getTenantDomain(),
                    policy.getPolicyId(), policy.getPolicyName(), policy.getDefaultQuotaPolicy().getType(),
                    null, deletedConditionGroupIds);
            APIUtil.sendNotification(apiPolicyEvent, APIConstants.NotifierType.POLICY.name());

        } else if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
            ApplicationPolicy appPolicy = apiMgtDAO.getApplicationPolicy(policyName, tenantID);
            ApplicationPolicyEvent applicationPolicyEvent = new ApplicationPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_DELETE.name(), tenantId,
                    appPolicy.getTenantDomain(), appPolicy.getPolicyId(), appPolicy.getPolicyName(),
                    appPolicy.getDefaultQuotaPolicy().getType());
            APIUtil.sendNotification(applicationPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
            SubscriptionPolicy subscriptionPolicy = apiMgtDAO.getSubscriptionPolicy(policyName, tenantID);
            //call the monetization extension point to delete plans if any
            deleteMonetizationPlan(subscriptionPolicy);
            SubscriptionPolicyEvent subscriptionPolicyEvent = new SubscriptionPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_DELETE.name(), tenantId,
                    subscriptionPolicy.getTenantDomain(), subscriptionPolicy.getPolicyId(),
                    subscriptionPolicy.getPolicyName(), subscriptionPolicy.getDefaultQuotaPolicy().getType(),
                    subscriptionPolicy.getRateLimitCount(), subscriptionPolicy.getRateLimitTimeUnit(),
                    subscriptionPolicy.isStopOnQuotaReach(), subscriptionPolicy.getGraphQLMaxDepth(),
                    subscriptionPolicy.getGraphQLMaxComplexity(), subscriptionPolicy.getSubscriberCount());
            APIUtil.sendNotification(subscriptionPolicyEvent, APIConstants.NotifierType.POLICY.name());
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
            GlobalPolicy globalPolicy = apiMgtDAO.getGlobalPolicy(policyName);
            GlobalPolicyEvent globalPolicyEvent = new GlobalPolicyEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.POLICY_DELETE.name(), tenantId,
                    globalPolicy.getTenantDomain(), globalPolicy.getPolicyId(), globalPolicy.getPolicyName());
            APIUtil.sendNotification(globalPolicyEvent, APIConstants.NotifierType.POLICY.name());
        }

        GlobalPolicy globalPolicy = null;
        if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
            globalPolicy = apiMgtDAO.getGlobalPolicy(policyName);
        }
        //remove from database
        apiMgtDAO.removeThrottlePolicy(policyLevel, policyName, tenantID);

        if (globalPolicy != null) {
            KeyTemplateEvent keyTemplateEvent = new KeyTemplateEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.CUSTOM_POLICY_DELETE.name(), tenantId, tenantDomain,
                    "remove", globalPolicy.getKeyTemplate());
            APIUtil.sendNotification(keyTemplateEvent, APIConstants.NotifierType.KEY_TEMPLATE.name());
        }
    }

    /**
     * Returns true if key template given by the global policy already exists.
     * But this check will exclude the policy represented by the policy name
     *
     * @param policy Global policy
     * @return true if Global policy key template already exists
     */
    public boolean isGlobalPolicyKeyTemplateExists(GlobalPolicy policy) throws APIManagementException {
        return apiMgtDAO.isKeyTemplatesExist(policy);
    }

    public boolean hasAttachments(String username, String policyName, String policyType, String organization) throws APIManagementException {
        if (PolicyConstants.POLICY_LEVEL_APP.equals(policyType)) {
            return apiMgtDAO.hasApplicationPolicyAttachedToApplication(policyName, organization);
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyType)) {
            return apiMgtDAO.hasSubscriptionPolicyAttached(policyName, organization);
        } else {
            return apiMgtDAO.hasAPIPolicyAttached(policyName, organization);
        }
    }

    @Override
    public List<BlockConditionsDTO> getBlockConditions() throws APIManagementException {
        return apiMgtDAO.getBlockConditions(tenantDomain);
    }

    @Override
    public BlockConditionsDTO getBlockCondition(int conditionId) throws APIManagementException {
        return apiMgtDAO.getBlockCondition(conditionId);
    }

    @Override
    public BlockConditionsDTO getBlockConditionByUUID(String uuid) throws APIManagementException {
        BlockConditionsDTO blockCondition = apiMgtDAO.getBlockConditionByUUID(uuid);
        if (blockCondition == null) {
            handleBlockConditionNotFoundException("Block condition: " + uuid + " was not found.");
        }
        return blockCondition;
    }

    @Override
    public boolean updateBlockCondition(int conditionId, String state) throws APIManagementException {
        boolean updateState = apiMgtDAO.updateBlockConditionState(conditionId, state);
        BlockConditionsDTO blockConditionsDTO = apiMgtDAO.getBlockCondition(conditionId);
        if (updateState) {
            publishBlockingEventUpdate(blockConditionsDTO);
        }
        return updateState;
    }

    @Override
    public boolean updateBlockConditionByUUID(String uuid, String state) throws APIManagementException {

        boolean updateState = apiMgtDAO.updateBlockConditionStateByUUID(uuid, state);
        BlockConditionsDTO blockConditionsDTO = apiMgtDAO.getBlockConditionByUUID(uuid);
        if (updateState && blockConditionsDTO != null) {
            publishBlockingEventUpdate(blockConditionsDTO);
        }
        return updateState;
    }

    @Override
    public String addBlockCondition(String conditionType, String conditionValue) throws APIManagementException {

        if (APIConstants.BLOCKING_CONDITIONS_USER.equals(conditionType)) {
            conditionValue = MultitenantUtils.getTenantAwareUsername(conditionValue);
            conditionValue = conditionValue + "@" + tenantDomain;
        }
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setConditionType(conditionType);
        blockConditionsDTO.setConditionValue(conditionValue);
        blockConditionsDTO.setTenantDomain(tenantDomain);
        blockConditionsDTO.setEnabled(true);
        blockConditionsDTO.setUUID(UUID.randomUUID().toString());
        BlockConditionsDTO createdBlockConditionsDto = apiMgtDAO.addBlockConditions(blockConditionsDTO);

        if (createdBlockConditionsDto != null) {
            publishBlockingEvent(createdBlockConditionsDto, "true");
            return createdBlockConditionsDto.getUUID();
        }
        return null;
    }

    @Override
    public String addBlockCondition(String conditionType, String conditionValue, boolean conditionStatus)
            throws APIManagementException {

        if (APIConstants.BLOCKING_CONDITIONS_USER.equals(conditionType)) {
            conditionValue = MultitenantUtils.getTenantAwareUsername(conditionValue);
            conditionValue = conditionValue + "@" + tenantDomain;
        }
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setConditionType(conditionType);
        blockConditionsDTO.setConditionValue(conditionValue);
        blockConditionsDTO.setTenantDomain(tenantDomain);
        blockConditionsDTO.setEnabled(conditionStatus);
        blockConditionsDTO.setUUID(UUID.randomUUID().toString());
        BlockConditionsDTO createdBlockConditionsDto = apiMgtDAO.addBlockConditions(blockConditionsDTO);

        if (createdBlockConditionsDto != null) {
            publishBlockingEvent(createdBlockConditionsDto, "true");
        }

        return createdBlockConditionsDto.getUUID();
    }

    @Override
    public boolean deleteBlockCondition(int conditionId) throws APIManagementException {

        BlockConditionsDTO blockCondition = apiMgtDAO.getBlockCondition(conditionId);
        boolean deleteState = apiMgtDAO.deleteBlockCondition(conditionId);
        if (deleteState && blockCondition != null) {
            unpublishBlockCondition(blockCondition);
        }
        return deleteState;
    }

    @Override
    public boolean deleteBlockConditionByUUID(String uuid) throws APIManagementException {
        boolean deleteState = false;
        BlockConditionsDTO blockCondition = apiMgtDAO.getBlockConditionByUUID(uuid);
        if (blockCondition != null) {
            deleteState = apiMgtDAO.deleteBlockCondition(blockCondition.getConditionId());
            if (deleteState) {
                unpublishBlockCondition(blockCondition);
            }
        }
        return deleteState;
    }

    /**
     * Unpublish a blocking condition.
     *
     * @param blockCondition Block Condition object
     */
    private void unpublishBlockCondition(BlockConditionsDTO blockCondition) {
        String blockingConditionType = blockCondition.getConditionType();
        String blockingConditionValue = blockCondition.getConditionValue();
        if (APIConstants.BLOCKING_CONDITIONS_USER.equalsIgnoreCase(blockingConditionType)) {
            blockingConditionValue = MultitenantUtils.getTenantAwareUsername(blockingConditionValue);
            blockingConditionValue = blockingConditionValue + "@" + tenantDomain;
            blockCondition.setConditionValue(blockingConditionValue);
        }
        publishBlockingEvent(blockCondition, "delete");
    }

    @Override
    public APIPolicy getAPIPolicy(String username, String policyName) throws APIManagementException {
        return apiMgtDAO.getAPIPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public APIPolicy getAPIPolicyByUUID(String uuid) throws APIManagementException {
        APIPolicy policy = apiMgtDAO.getAPIPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Advanced Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String username, String policyName) throws APIManagementException {
        return apiMgtDAO.getApplicationPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByUUID(String uuid) throws APIManagementException {
        ApplicationPolicy policy = apiMgtDAO.getApplicationPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Application Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String username, String policyName) throws APIManagementException {
        return apiMgtDAO.getSubscriptionPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByUUID(String uuid) throws APIManagementException {
        SubscriptionPolicy policy = apiMgtDAO.getSubscriptionPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Subscription Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public GlobalPolicy getGlobalPolicy(String policyName) throws APIManagementException {
        return apiMgtDAO.getGlobalPolicy(policyName);
    }

    @Override
    public GlobalPolicy getGlobalPolicyByUUID(String uuid) throws APIManagementException {
        GlobalPolicy policy = apiMgtDAO.getGlobalPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Global Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    /**
     * Publishes the changes on blocking conditions.
     *
     * @param blockCondition Block Condition object
     * @throws APIManagementException
     */
    private void publishBlockingEventUpdate(BlockConditionsDTO blockCondition) throws APIManagementException {
        if (blockCondition != null) {
            String blockingConditionType = blockCondition.getConditionType();
            String blockingConditionValue = blockCondition.getConditionValue();
            if (APIConstants.BLOCKING_CONDITIONS_USER.equalsIgnoreCase(blockingConditionType)) {
                blockingConditionValue = MultitenantUtils.getTenantAwareUsername(blockingConditionValue);
                blockingConditionValue = blockingConditionValue + "@" + tenantDomain;
                blockCondition.setConditionValue(blockingConditionValue);
            }

            publishBlockingEvent(blockCondition, Boolean.toString(blockCondition.isEnabled()));
        }
    }

    /**
     * Publishes the changes on blocking conditions.
     * @param blockConditionsDTO Blockcondition Dto event
     */
    private void publishBlockingEvent(BlockConditionsDTO blockConditionsDTO, String state) {
        String conditionType = blockConditionsDTO.getConditionType();
        String conditionValue = blockConditionsDTO.getConditionValue();
        if (APIConstants.BLOCKING_CONDITIONS_IP.equals(conditionType) ||
                APIConstants.BLOCK_CONDITION_IP_RANGE.equals(conditionType)) {
            conditionValue = StringEscapeUtils.escapeJava(conditionValue);
        }
        Object[] objects = new Object[]{blockConditionsDTO.getConditionId(), blockConditionsDTO.getConditionType(),
                conditionValue, state, tenantDomain};
        Event blockingMessage = new Event(APIConstants.BLOCKING_CONDITIONS_STREAM_ID, System.currentTimeMillis(),
                null, null, objects);
        EventPublisherEvent blockingEvent = new EventPublisherEvent(APIConstants.BLOCKING_CONDITIONS_STREAM_ID,
                System.currentTimeMillis(), objects, blockingMessage.toString());
        APIUtil.publishEvent(EventPublisherType.BLOCKING_EVENT, blockingEvent, blockingMessage.toString());
    }

    public String getExternalWorkflowReferenceId(int subscriptionId) throws APIManagementException {
        return apiMgtDAO.getExternalWorkflowReferenceForSubscription(subscriptionId);
    }

    @Override
    public int addCertificate(String userName, String certificate, String alias, String endpoint)
            throws APIManagementException {

        ResponseCode responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);

        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            responseCode = certificateManager
                    .addCertificateToParentNode(certificate, alias, endpoint, tenantId);
            CertificateEvent certificateEvent = new CertificateEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(),APIConstants.EventType.ENDPOINT_CERTIFICATE_ADD.toString(),
                    tenantDomain,alias,endpoint);
            APIUtil.sendNotification(certificateEvent, APIConstants.NotifierType.CERTIFICATE.name());
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information", e);
        }
        return responseCode.getResponseCode();
    }

    @Override
    public int addClientCertificate(String userName, ApiTypeWrapper apiTypeWrapper, String certificate, String alias,
                                    String tierName, String organization) throws APIManagementException {
        checkAccessControlPermission(userNameWithoutChange, apiTypeWrapper.getAccessControl(),
                apiTypeWrapper.getAccessControlRoles());
        ResponseCode responseCode = ResponseCode.INTERNAL_SERVER_ERROR;

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        responseCode = certificateManager
                .addClientCertificate(apiTypeWrapper.getId(), certificate, alias, tierName, tenantId, organization);
        return responseCode.getResponseCode();
    }

    @Override
    public int deleteCertificate(String userName, String alias, String endpoint) throws APIManagementException {

        ResponseCode responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);

        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            responseCode = certificateManager.deleteCertificateFromParentNode(alias, endpoint, tenantId);
            CertificateEvent certificateEvent = new CertificateEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.ENDPOINT_CERTIFICATE_REMOVE.toString(),
                    tenantDomain, alias, endpoint);
            APIUtil.sendNotification(certificateEvent, APIConstants.NotifierType.CERTIFICATE.name());
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information", e);
        }
        return responseCode.getResponseCode();
    }

    @Override
    public int deleteClientCertificate(String userName, ApiTypeWrapper apiTypeWrapper, String alias)
            throws APIManagementException {
        checkAccessControlPermission(userNameWithoutChange, apiTypeWrapper.getAccessControl(),
                apiTypeWrapper.getAccessControlRoles());

        ResponseCode responseCode = ResponseCode.INTERNAL_SERVER_ERROR;

        int tenantId = APIUtil.getInternalOrganizationId(apiTypeWrapper.getOrganization());
        responseCode = certificateManager.deleteClientCertificateFromParentNode(apiTypeWrapper.getId(), alias, tenantId);
        return responseCode.getResponseCode();
    }

    @Override
    public boolean isConfigured() {
        return certificateManager.isConfigured();
    }

    @Override
    public CertificateMetadataDTO getCertificate(String alias) throws APIManagementException {
        int tenantId = 0;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information", e);
        }
        return certificateManager.getCertificate(alias, tenantId);
    }

    @Override
    public List<CertificateMetadataDTO> getCertificates(String userName) throws APIManagementException {
        int tenantId = 0;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            return certificateManager.getCertificates(tenantId);
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information", e);
        }
        return null;
    }

    @Override
    public List<CertificateMetadataDTO> searchCertificates(int tenantId, String alias, String endpoint) throws
            APIManagementException {
        return certificateManager.getCertificates(tenantId, alias, endpoint);
    }

    @Override
    public List<ClientCertificateDTO> searchClientCertificates(int tenantId, String alias,
            APIIdentifier apiIdentifier, String organization) throws APIManagementException {
        return certificateManager.searchClientCertificates(tenantId, alias, apiIdentifier, organization);
    }

    @Override
    public List<ClientCertificateDTO> searchClientCertificates(int tenantId, String alias,
            APIProductIdentifier apiProductIdentifier, String organization) throws APIManagementException {
        APIIdentifier apiIdentifier = new APIIdentifier(apiProductIdentifier.getProviderName(),
                apiProductIdentifier.getName(), apiProductIdentifier.getVersion());
        apiIdentifier.setUuid(apiMgtDAO.getUUIDFromIdentifier(apiIdentifier));
        return certificateManager.searchClientCertificates(tenantId, alias, apiIdentifier, organization);
    }

    @Override
    public boolean isCertificatePresent(int tenantId, String alias) throws APIManagementException {
        return certificateManager.isCertificatePresent(tenantId, alias);
    }

    public ClientCertificateDTO getClientCertificate(int tenantId, String alias, String organization)
            throws APIManagementException {
        List<ClientCertificateDTO> clientCertificateDTOS = certificateManager
                .searchClientCertificates(tenantId, alias, null, organization);
        if (clientCertificateDTOS != null && clientCertificateDTOS.size() > 0) {
            return clientCertificateDTOS.get(0);
        }
        return null;
    }

    @Override
    public ClientCertificateDTO getClientCertificate(String alias, ApiTypeWrapper apiTypeWrapper,
                                                     String organization) throws APIManagementException {
        checkAccessControlPermission(userNameWithoutChange, apiTypeWrapper.getAccessControl(),
                apiTypeWrapper.getAccessControlRoles());
        int tenantId = APIUtil.getInternalOrganizationId(organization);
        List<ClientCertificateDTO> clientCertificateDTOS = certificateManager
                .searchClientCertificates(tenantId, alias, apiTypeWrapper.getId(), organization);
        if (clientCertificateDTOS != null && clientCertificateDTOS.size() > 0) {
            return clientCertificateDTOS.get(0);
        }
        return null;
    }

    @Override
    public CertificateInformationDTO getCertificateStatus(String tenantDomain, String alias) throws APIManagementException {
        int tenantId = -1;
        try {
            tenantId = getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information", e);
        }
        return certificateManager.getCertificateInformation(tenantId, alias);
    }

    @Override
    public int updateCertificate(String certificateString, String alias) throws APIManagementException {
        ResponseCode responseCode = certificateManager.updateCertificate(certificateString, alias);
        if (responseCode != null && responseCode.getResponseCode() == ResponseCode.SUCCESS.getResponseCode()) {
            CertificateEvent certificateEvent = new CertificateEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.ENDPOINT_CERTIFICATE_UPDATE.toString(),
                    tenantDomain, alias);
            APIUtil.sendNotification(certificateEvent, APIConstants.NotifierType.CERTIFICATE.name());
        }
        return responseCode != null ? responseCode.getResponseCode() :
                ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode();
    }


    @Override
    public int updateClientCertificate(String certificate, String alias, ApiTypeWrapper apiTypeWrapper,
                                       String tier, int tenantId, String organization) throws APIManagementException {
        checkAccessControlPermission(userNameWithoutChange, apiTypeWrapper.getAccessControl(),
                apiTypeWrapper.getAccessControlRoles());
        ResponseCode responseCode = certificateManager
                .updateClientCertificate(certificate, alias, tier, tenantId, organization);
        return responseCode != null ?
                responseCode.getResponseCode() :
                ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode();
    }

    @Override
    public int getCertificateCountPerTenant(int tenantId) throws APIManagementException {
        return certificateManager.getCertificateCount(tenantId);
    }

    @Override
    public int getClientCertificateCount(int tenantId) throws APIManagementException {
        return certificateManager.getClientCertificateCount(tenantId);
    }

    @Override
    public ByteArrayInputStream getCertificateContent(String tenantDomain, String alias) throws APIManagementException {
        int tenantId = -1;
        try {
            tenantId = getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information", e);
        }
        return certificateManager.getCertificateContent(tenantId, alias);
    }

    /**
     * Get the workflow status information for the given api for the given workflow type
     *
     * @param uuid Api uuid
     * @param workflowType  workflow type
     * @return WorkflowDTO
     * @throws APIManagementException
     */
    public WorkflowDTO getAPIWorkflowStatus(String uuid, String workflowType)
            throws APIManagementException {
        return APIUtil.getAPIWorkflowStatus(uuid, workflowType);
    }

    @Override
    public void deleteWorkflowTask(Identifier identifier) throws APIManagementException {
        int apiId;
        try {
            apiId = apiMgtDAO.getAPIID(identifier.getUUID());
            cleanUpPendingAPIStateChangeTask(apiId, identifier instanceof APIProductIdentifier);
        } catch (APIManagementException | WorkflowException e) {
            handleException("Error while deleting the workflow task.", e);
        }
    }

    private void cleanUpPendingAPIStateChangeTask(int apiId, boolean isAPIProduct) throws WorkflowException,
            APIManagementException {
        //Run cleanup task for workflow
        WorkflowExecutor apiStateChangeWFExecutor;
        WorkflowDTO wfDTO;

        if (isAPIProduct) {
            apiStateChangeWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE);
            wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId),
                    WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE);
        } else {
            apiStateChangeWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE);
            wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId),
                    WorkflowConstants.WF_TYPE_AM_API_STATE);
        }
        if (wfDTO != null && WorkflowStatus.CREATED == wfDTO.getStatus()) {
            apiStateChangeWFExecutor.cleanUpPendingTask(wfDTO.getExternalWorkflowReference());
        }
    }

    /**
     * Returns the given workflow executor
     *
     * @param workflowType Workflow executor type
     * @return WorkflowExecutor of given type
     * @throws WorkflowException if an error occurred while getting WorkflowExecutor
     */
    protected WorkflowExecutor getWorkflowExecutor(String workflowType) throws APIManagementException {
        try {
            return WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
        } catch (WorkflowException e) {
            handleException("Error while obtaining WorkflowExecutor instance for workflow type :" + workflowType);
        }
        return null;
    }

    protected void removeFromGateway(APIProduct apiProduct, String tenantDomain, Set<APIRevisionDeployment> gatewaysToRemove,
                                     Set<String> gatewaysToAdd)
            throws APIManagementException {
        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        Set<API> associatedAPIs = getAssociatedAPIs(apiProduct);
        Set<String> environmentsToRemove = new HashSet<>();
        for (APIRevisionDeployment apiRevisionDeployment : gatewaysToRemove) {
            environmentsToRemove.add(apiRevisionDeployment.getDeployment());
        }
        environmentsToRemove.removeAll(gatewaysToAdd);
        gatewayManager.unDeployFromGateway(apiProduct, tenantDomain, associatedAPIs, environmentsToRemove);
    }

    protected int getTenantId(String tenantDomain) throws UserStoreException {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
    }

    protected void sendAsncNotification(NotificationDTO notificationDTO) throws NotificationException {
        new NotificationExecutor().sendAsyncNotifications(notificationDTO);

    }

    protected void invalidateResourceCache(String apiContext, String apiVersion,Set<URITemplate> uriTemplates) {
        APIAuthenticationAdminClient client = new APIAuthenticationAdminClient();
        client.invalidateResourceCache(apiContext, apiVersion, uriTemplates);
    }

    /**
     * To get the query to retrieve user role list query based on current role list.
     *
     * @return the query with user role list.
     * @throws APIManagementException API Management Exception.
     */
    private String getUserRoleListQuery() throws APIManagementException {
        StringBuilder rolesQuery = new StringBuilder();
        rolesQuery.append('(');
        rolesQuery.append(APIConstants.NULL_USER_ROLE_LIST);
        String[] userRoles = APIUtil.getListOfRoles(userNameWithoutChange);
        String skipRolesByRegex = APIUtil.getSkipRolesByRegex();
        if (StringUtils.isNotEmpty(skipRolesByRegex)) {
            List<String> filteredUserRoles = new ArrayList<>(Arrays.asList(userRoles));
            String[] regexList = skipRolesByRegex.split(",");
            for (int i = 0; i < regexList.length; i++) {
                Pattern p = Pattern.compile(regexList[i]);
                Iterator<String> itr = filteredUserRoles.iterator();
                while(itr.hasNext()) {
                    String role = itr.next();
                    Matcher m = p.matcher(role);
                    if (m.matches()) {
                        itr.remove();
                    }
                }
            }
            userRoles = filteredUserRoles.toArray(new String[0]);
        }
        if (userRoles != null) {
            for (String userRole : userRoles) {
                rolesQuery.append(" OR ");
                rolesQuery.append(ClientUtils.escapeQueryChars(APIUtil.sanitizeUserRole(userRole.toLowerCase())));
            }
        }
        rolesQuery.append(")");
        if(log.isDebugEnabled()) {
        	log.debug("User role list solr query " + APIConstants.PUBLISHER_ROLES + "=" + rolesQuery.toString());
        }
        return APIConstants.PUBLISHER_ROLES + "=" + rolesQuery.toString();
    }

    @Override
    protected String getSearchQuery(String searchQuery) throws APIManagementException {
        if (!isAccessControlRestrictionEnabled || APIUtil.hasPermission(userNameWithoutChange, APIConstants.Permissions
                .APIM_ADMIN)) {
            return searchQuery;
        }
        String criteria = getUserRoleListQuery();
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            criteria = criteria + "&" + searchQuery;
        }
        return criteria;
    }

    @Override
    public Map<API, List<APIProductResource>> addAPIProductWithoutPublishingToGateway(APIProduct product)
            throws APIManagementException {
        Map<API, List<APIProductResource>> apiToProductResourceMapping = new HashMap<>();

        validateApiProductInfo(product);
        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(product.getId().getProviderName()));

        if (log.isDebugEnabled()) {
            log.debug("API Product details successfully added to the registry. API Product Name: " + product.getId().getName()
                    + ", API Product Version : " + product.getId().getVersion() + ", API Product context : " + "change"); //todo: log context
        }

        List<APIProductResource> resources = product.getProductResources();

        // list to hold resources which are actually in an existing api. If user has created an API product with invalid
        // API or invalid resource of a valid API, that content will be removed .validResources array will have only
        // legitimate apis
        List<APIProductResource> validResources = new ArrayList<APIProductResource>();
        for (APIProductResource apiProductResource : resources) {
            API api;
            String apiUUID;
            if (apiProductResource.getProductIdentifier() != null) {
                APIIdentifier productAPIIdentifier = apiProductResource.getApiIdentifier();
                String emailReplacedAPIProviderName = APIUtil.replaceEmailDomain(productAPIIdentifier.getProviderName());
                APIIdentifier emailReplacedAPIIdentifier = new APIIdentifier(emailReplacedAPIProviderName,
                        productAPIIdentifier.getApiName(), productAPIIdentifier.getVersion());
                apiUUID = apiMgtDAO.getUUIDFromIdentifier(emailReplacedAPIIdentifier, product.getOrganization());
                api = getAPIbyUUID(apiUUID, product.getOrganization());
            } else {
                apiUUID = apiProductResource.getApiId();
                api = getAPIbyUUID(apiUUID, product.getOrganization());
                // if API does not exist, getLightweightAPIByUUID() method throws exception.
            }
            if (api != null) {
                validateApiLifeCycleForApiProducts(api);
                if (api.getSwaggerDefinition() != null) {
                    api.setSwaggerDefinition(getOpenAPIDefinition(apiUUID, product.getOrganization()));
                }

                if (!apiToProductResourceMapping.containsKey(api)) {
                    apiToProductResourceMapping.put(api, new ArrayList<>());
                }

                List<APIProductResource> apiProductResources = apiToProductResourceMapping.get(api);
                apiProductResources.add(apiProductResource);

                apiProductResource.setApiIdentifier(api.getId());
                apiProductResource.setProductIdentifier(product.getId());
                if (api.isAdvertiseOnly()) {
                    apiProductResource.setEndpointConfig(APIUtil.generateEndpointConfigForAdvertiseOnlyApi(api));
                } else {
                    apiProductResource.setEndpointConfig(api.getEndpointConfig());
                }
                apiProductResource.setEndpointSecurityMap(APIUtil.setEndpointSecurityForAPIProduct(api));
                URITemplate uriTemplate = apiProductResource.getUriTemplate();

                Map<String, URITemplate> templateMap = apiMgtDAO.getURITemplatesForAPI(api);
                if (uriTemplate == null) {
                    //if no resources are define for the API, we ingore that api for the product
                } else {
                    String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getResourceURI();
                    if (templateMap.containsKey(key)) {

                        //Since the template ID is not set from the request, we manually set it.
                        uriTemplate.setId(templateMap.get(key).getId());
                        //request has a valid API id and a valid resource. we add it to valid resource map
                        validResources.add(apiProductResource);

                    } else {
                        //ignore
                        log.warn("API with id " + apiProductResource.getApiId()
                                + " does not have a resource " + uriTemplate.getResourceURI()
                                + " with http method " + uriTemplate.getHTTPVerb());

                    }
                }
            }
        }
        //set the valid resources only
        product.setProductResources(validResources);
        //now we have validated APIs and it's resources inside the API product. Add it to database
        String provider = APIUtil.replaceEmailDomain(product.getId().getProviderName());
        // Set version timestamp
        product.setVersionTimestamp(String.valueOf(System.currentTimeMillis()));

        // Create registry artifact
        String apiProductUUID = createAPIProduct(product);
        product.setUuid(apiProductUUID);

        // Add to database
        apiMgtDAO.addAPIProduct(product, product.getOrganization());

        return apiToProductResourceMapping;
    }

    private String calculateVersionTimestamp(String provider, String name, String version, String org)
            throws APIManagementException {

        if (StringUtils.isEmpty(provider) ||
                StringUtils.isEmpty(name) ||
                StringUtils.isEmpty(org)) {
            throw new APIManagementException("Invalid API information, name=" + name + " provider=" + provider +
                    " organization=" + org);
        }
        TreeMap<String, API> apiSortedMap = new TreeMap<>();
        List<API> apiList = getAPIVersionsByProviderAndName(provider,
                name, org);
        for (API mappedAPI : apiList) {
            apiSortedMap.put(mappedAPI.getVersionTimestamp(), mappedAPI);
        }
        APIVersionStringComparator comparator = new APIVersionStringComparator();
        String latestVersion = version;
        long previousTimestamp = 0L;
        String latestTimestamp = "";
        for (API tempAPI : apiSortedMap.values()) {
            if (comparator.compare(tempAPI.getId().getVersion(), latestVersion) > 0) {
                latestTimestamp = String.valueOf((previousTimestamp + Long.valueOf(tempAPI.getVersionTimestamp())) / 2);
                break;
            } else {
                previousTimestamp = Long.valueOf(tempAPI.getVersionTimestamp());
            }
        }
        if (StringUtils.isEmpty(latestTimestamp)) {
            latestTimestamp = String.valueOf(System.currentTimeMillis());
        }
        return latestTimestamp;
    }

    @Override
    public void saveToGateway(APIProduct product) throws APIManagementException {

        List<APIProductResource> productResources = product.getProductResources();

        //Only publish to gateways if the state is in Published state and has atleast one resource
    }

    public void deleteAPIProduct(APIProduct apiProduct) throws APIManagementException {

        APIProductIdentifier identifier = apiProduct.getId();

        try {
            //int apiId = apiMgtDAO.getAPIID(identifier, null);
            long subsCount = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
            if (subsCount > 0) {
                //Logging as a WARN since this isn't an error scenario.
                String message = "Cannot remove the API Product as active subscriptions exist.";
                log.warn(message);
                throw new APIManagementException(message);
            }

            // gatewayType check is required when API Management is deployed on
            // other servers to avoid synapse
            deleteAPIProductRevisions(apiProduct.getUuid(), apiProduct.getOrganization());

            apiPersistenceInstance.deleteAPIProduct(new Organization(apiProduct.getOrganization()), apiProduct.getUuid());
            apiMgtDAO.deleteAPIProduct(identifier);
            cleanUpPendingAPIStateChangeTask(apiProduct.getProductId(), true);
            if (log.isDebugEnabled()) {
                String logMessage =
                        "API Product Name: " + identifier.getName() + ", API Product Version " + identifier.getVersion()
                                + " successfully removed from the database.";
                log.debug(logMessage);
            }

            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, identifier.getName());
            apiLogObject.put(APIConstants.AuditLogConstants.VERSION, identifier.getVersion());
            apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API_PRODUCT, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.DELETED, this.username);

            GatewayArtifactsMgtDAO.getInstance().deleteGatewayArtifacts(apiProduct.getUuid());

        } catch (APIPersistenceException e) {
            handleException("Failed to remove the API product", e);
        } catch (WorkflowException e) {
            handleException("Error while removing the pending workflows of API Product", e);
        }
    }

    @Override
    public APIProduct getAPIProduct(APIProductIdentifier identifier) throws APIManagementException {
        String apiProductUUID = apiMgtDAO.getUUIDFromIdentifier(identifier, organization);
        return getAPIProductbyUUID(apiProductUUID, organization);
    }

    @Override
    public void deleteAPIProduct(APIProductIdentifier identifier, String apiProductUUID, String organization)
            throws APIManagementException {
        if (StringUtils.isEmpty(apiProductUUID)) {
            if (identifier.getUUID() != null) {
                apiProductUUID = identifier.getUUID();
            } else {
                apiProductUUID = apiMgtDAO.getUUIDFromIdentifier(identifier, organization);
            }
        }
        APIProduct apiProduct = getAPIProductbyUUID(apiProductUUID, organization);
        apiProduct.setOrganization(organization);
        deleteAPIProduct(apiProduct);
    }

    @Override
    public Map<API, List<APIProductResource>> updateAPIProduct(APIProduct product)
            throws APIManagementException, FaultGatewaysException {
        Map<API, List<APIProductResource>> apiToProductResourceMapping = new HashMap<>();
        //validate resources and set api identifiers and resource ids to product
        List<APIProductResource> resources = product.getProductResources();
        for (APIProductResource apiProductResource : resources) {
            API api;
            APIProductIdentifier productIdentifier = apiProductResource.getProductIdentifier();
            String apiUUID;
            if (productIdentifier != null) {
                APIIdentifier productAPIIdentifier = apiProductResource.getApiIdentifier();
                String emailReplacedAPIProviderName = APIUtil.replaceEmailDomain(productAPIIdentifier.getProviderName());
                APIIdentifier emailReplacedAPIIdentifier = new APIIdentifier(emailReplacedAPIProviderName,
                        productAPIIdentifier.getApiName(), productAPIIdentifier.getVersion());
                apiUUID = apiMgtDAO.getUUIDFromIdentifier(emailReplacedAPIIdentifier, product.getOrganization());
                api = getAPIbyUUID(apiUUID, tenantDomain);
            } else {
                apiUUID = apiProductResource.getApiId();
                api = getAPIbyUUID(apiUUID, tenantDomain);
            }
            if (api.getSwaggerDefinition() != null) {
                api.setSwaggerDefinition(getOpenAPIDefinition(apiUUID, tenantDomain));
            }

            if (!apiToProductResourceMapping.containsKey(api)) {
                apiToProductResourceMapping.put(api, new ArrayList<>());
            }

            List<APIProductResource> apiProductResources = apiToProductResourceMapping.get(api);
            apiProductResources.add(apiProductResource);

            // if API does not exist, getLightweightAPIByUUID() method throws exception. so no need to handle NULL
            apiProductResource.setApiIdentifier(api.getId());
            apiProductResource.setProductIdentifier(product.getId());
            if (api.isAdvertiseOnly()) {
                apiProductResource.setEndpointConfig(APIUtil.generateEndpointConfigForAdvertiseOnlyApi(api));
            } else {
                apiProductResource.setEndpointConfig(api.getEndpointConfig());
            }
            apiProductResource.setEndpointSecurityMap(APIUtil.setEndpointSecurityForAPIProduct(api));
            URITemplate uriTemplate = apiProductResource.getUriTemplate();

            Map<String, URITemplate> templateMap = apiMgtDAO.getURITemplatesForAPI(api);
            if (uriTemplate == null) {
                // TODO handle if no resource is defined. either throw an error or add all the resources of that API
                // to the product
            } else {
                String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
                if (templateMap.containsKey(key)) {

                    //Since the template ID is not set from the request, we manually set it.
                    uriTemplate.setId(templateMap.get(key).getId());

                } else {
                    throw new APIManagementException("API with id " + apiProductResource.getApiId()
                            + " does not have a resource " + uriTemplate.getUriTemplate()
                            + " with http method " + uriTemplate.getHTTPVerb());
                }
            }
        }

        APIProduct oldApi = getAPIProductbyUUID(product.getUuid(),
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        Gson gson = new Gson();
        Map<String, String> oldMonetizationProperties = gson.fromJson(oldApi.getMonetizationProperties().toString(),
                HashMap.class);
        if (oldMonetizationProperties != null && !oldMonetizationProperties.isEmpty()) {
            Map<String, String> newMonetizationProperties = gson.fromJson(product.getMonetizationProperties().toString(),
                    HashMap.class);
            if (newMonetizationProperties != null) {
                for (Map.Entry<String, String> entry : oldMonetizationProperties.entrySet()) {
                    String newValue = newMonetizationProperties.get(entry.getKey());
                    if (StringUtils.isAllBlank(newValue)) {
                        newMonetizationProperties.put(entry.getKey(), entry.getValue());
                    }
                }
                JSONParser parser = new JSONParser();
                try {
                    JSONObject jsonObj = (JSONObject) parser.parse(gson.toJson(newMonetizationProperties));
                    product.setMonetizationProperties(jsonObj);
                } catch (ParseException e) {
                    throw new APIManagementException("Error when parsing monetization properties ", e);
                }
            }
        }
        invalidateResourceCache(product.getContext(), product.getId().getVersion(), Collections.EMPTY_SET);

        //todo : check whether permissions need to be updated and pass it along
        updateApiProductArtifact(product, true, true);
        apiMgtDAO.updateAPIProduct(product, userNameWithoutChange);

        int productId = apiMgtDAO.getAPIProductId(product.getId());

        APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.API_UPDATE.name(), tenantId, organization, product.getId().getName(), productId,
                product.getId().getUUID(), product.getId().getVersion(), product.getType(), product.getContext(),
                product.getId().getProviderName(), APIConstants.LC_PUBLISH_LC_STATE);
        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());

        return apiToProductResourceMapping;
    }

    @Override
    public List<ResourcePath> getResourcePathsOfAPI(APIIdentifier apiId) throws APIManagementException {
        return apiMgtDAO.getResourcePathsOfAPI(apiId);
    }

    private void validateApiLifeCycleForApiProducts(API api) throws APIManagementException {
        String status = api.getStatus();

        if (APIConstants.BLOCKED.equals(status) ||
            APIConstants.PROTOTYPED.equals(status) ||
            APIConstants.DEPRECATED.equals(status) ||
            APIConstants.RETIRED.equals(status)) {
            throw new APIManagementException("Cannot create API Product using API with following status: " + status,
                    ExceptionCodes.from(ExceptionCodes.API_PRODUCT_WITH_UNSUPPORTED_LIFECYCLE_API, status));
        }
    }

    /**
     * Validates the name of api product against illegal characters.
     *
     * @param product APIProduct info object
     * @throws APIManagementException
     */
    private void validateApiProductInfo(APIProduct product) throws APIManagementException {
        String apiName = product.getId().getName();
        if (apiName == null) {
            handleException("API Name is required.");
        } else if (containsIllegals(apiName)) {
            handleException("API Name contains one or more illegal characters  " +
                    "( " + APIConstants.REGEX_ILLEGAL_CHARACTERS_FOR_API_METADATA + " )");
        }
        //version is not a mandatory field for now
        if (!hasValidLength(apiName, APIConstants.MAX_LENGTH_API_NAME)
                || !hasValidLength(product.getId().getVersion(), APIConstants.MAX_LENGTH_VERSION)
                || !hasValidLength(product.getId().getProviderName(), APIConstants.MAX_LENGTH_PROVIDER)
                || !hasValidLength(product.getContext(), APIConstants.MAX_LENGTH_CONTEXT)) {
            throw new APIManagementException("Character length exceeds the allowable limit",
                    ExceptionCodes.LENGTH_EXCEEDS);
        }
    }
    /**
     * Create an Api Product
     *
     * @param apiProduct API Product
     * @throws APIManagementException if failed to create APIProduct
     */
    protected String createAPIProduct(APIProduct apiProduct) throws APIManagementException {
        String apiProductUUID = null;
        // Validate Transports and Security
        validateAndSetTransports(apiProduct);
        validateAndSetAPISecurity(apiProduct);

        PublisherAPIProduct publisherAPIProduct = APIProductMapper.INSTANCE.toPublisherApiProduct(apiProduct);
        PublisherAPIProduct addedAPIProduct;
        try {
            publisherAPIProduct.setApiProductName(apiProduct.getId().getName());
            publisherAPIProduct.setProviderName(apiProduct.getId().getProviderName());
            publisherAPIProduct.setVersion(apiProduct.getId().getVersion());
            addedAPIProduct = apiPersistenceInstance.addAPIProduct(
                    new Organization(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()),
                    publisherAPIProduct);

            apiProductUUID = addedAPIProduct.getId();
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while creating API product ", e);
        }


        return apiProductUUID;
    }


    /**
     * Update API Product Artifact in Registry
     *
     * @param apiProduct
     * @param updateMetadata
     * @param updatePermissions
     * @throws APIManagementException
     */
    private void updateApiProductArtifact(APIProduct apiProduct, boolean updateMetadata, boolean updatePermissions)
            throws APIManagementException {

        //Validate Transports and Security
        validateAndSetTransports(apiProduct);
        validateAndSetAPISecurity(apiProduct);

        PublisherAPIProduct publisherAPIProduct = APIProductMapper.INSTANCE.toPublisherApiProduct(apiProduct);
        PublisherAPIProduct addedAPIProduct;
        try {
            publisherAPIProduct.setApiProductName(apiProduct.getId().getName());
            publisherAPIProduct.setProviderName(apiProduct.getId().getProviderName());
            publisherAPIProduct.setVersion(apiProduct.getId().getVersion());
            addedAPIProduct = apiPersistenceInstance.updateAPIProduct(
                    new Organization(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()),
                    publisherAPIProduct);
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while creating API product ");
        }
    }

    public void updateProductResourceMappings(API api, String organization, List<APIProductResource> productResources)
            throws APIManagementException {
        //get uri templates of API again
        Map<String, URITemplate> apiResources = apiMgtDAO.getURITemplatesForAPI(api);

        for (APIProductResource productResource : productResources) {
            URITemplate uriTemplate = productResource.getUriTemplate();
            String productResourceKey = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();

            //set new uri template ID to the product resource
            int updatedURITemplateId = apiResources.get(productResourceKey).getId();
            uriTemplate.setId(updatedURITemplateId);
        }

        apiMgtDAO.addAPIProductResourceMappings(productResources, organization, null);
    }

    /**
     * Check whether the given scope name exists as a shared scope in the tenant domain.
     *
     * @param scopeName    Shared Scope name
     * @param tenantId Tenant Id
     * @return Scope availability
     * @throws APIManagementException if failed to check the availability
     */
    @Override
    public boolean isSharedScopeNameExists(String scopeName, int tenantId) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Checking whether scope name: " + scopeName + " exists as a shared scope in tenant with ID: "
                    + tenantId);
        }
        return ApiMgtDAO.getInstance().isSharedScopeExists(scopeName, tenantId);
    }

    /**
     * Add Shared Scope by registering it in the KM and adding the scope as a Shared Scope in AM DB.
     *
     * @param scope        Shared Scope
     * @param tenantDomain Tenant domain
     * @return UUId of the added Shared Scope object
     * @throws APIManagementException if failed to add a scope
     */
    @Override
    public String addSharedScope(Scope scope, String tenantDomain) throws APIManagementException {

        Set<Scope> scopeSet = new HashSet<>();
        scopeSet.add(scope);
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        addScopes(scopeSet, tenantId);
        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
            KeyManager keyManager = keyManagerDtoEntry.getValue().getKeyManager();
            if (keyManager != null) {
                try {
                    keyManager.registerScope(scope);
                } catch (APIManagementException e) {
                    log.error("Error occurred while registering Scope in Key Manager " + keyManagerDtoEntry.getKey(), e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding shared scope mapping: " + scope.getKey() + " to  Key Manager : " +
                        keyManagerDtoEntry.getKey());
            }
        }
        return ApiMgtDAO.getInstance().addSharedScope(scope, tenantDomain);
    }

    /**
     * Get all available shared scopes.
     *
     * @param tenantDomain tenant domain
     * @return Shared Scope list
     * @throws APIManagementException if failed to get the scope list
     */
    @Override
    public List<Scope> getAllSharedScopes(String tenantDomain) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving all the shared scopes for tenant: " + tenantDomain);
        }
        //Get all shared scopes
        List<Scope> allSharedScopes = ApiMgtDAO.getInstance().getAllSharedScopes(tenantDomain);
        //Get all scopes from KM
        List<Scope> allScopes = scopesDAO.getScopes(APIUtil.getTenantIdFromTenantDomain(tenantDomain));
        for (Scope scope : allSharedScopes) {
            for (Scope tempScope : allScopes) {
                if (scope.getKey().equals(tempScope.getKey())) {
                    scope.setName(tempScope.getName());
                    scope.setDescription(tempScope.getDescription());
                    scope.setRoles(tempScope.getRoles());
                    break;
                }
            }
        }
        return allSharedScopes;
    }

    /**
     * Get all available shared scope keys.
     *
     * @param tenantDomain tenant domain
     * @return Shared Scope Keyset
     * @throws APIManagementException if failed to get the scope key set
     */
    @Override
    public Set<String> getAllSharedScopeKeys(String tenantDomain) throws APIManagementException {

        //Get all shared scope keys
        return ApiMgtDAO.getInstance().getAllSharedScopeKeys(tenantDomain);
    }

    /**
     * Get shared scope by UUID.
     *
     * @param sharedScopeId Shared scope Id
     * @param tenantDomain  tenant domain
     * @return Shared Scope
     * @throws APIManagementException If failed to get the scope
     */
    @Override
    public Scope getSharedScopeByUUID(String sharedScopeId, String tenantDomain) throws APIManagementException {

        Scope sharedScope;
        if (log.isDebugEnabled()) {
            log.debug("Retrieving shared scope: " + sharedScopeId);
        }
        String scopeKey = ApiMgtDAO.getInstance().getSharedScopeKeyByUUID(sharedScopeId);
        if (scopeKey != null) {
            sharedScope = scopesDAO.getScope(scopeKey, APIUtil.getTenantIdFromTenantDomain(tenantDomain));
            sharedScope.setId(sharedScopeId);
        } else {
            throw new APIMgtResourceNotFoundException("Shared Scope not found for scope ID: " + sharedScopeId,
                    ExceptionCodes.from(ExceptionCodes.SHARED_SCOPE_NOT_FOUND, sharedScopeId));
        }
        return sharedScope;
    }

    /**
     * Delete shared scope.
     *
     * @param scopeName    Shared scope name
     * @param tenantDomain tenant domain
     * @throws APIManagementException If failed to delete the scope
     */
    @Override
    public void deleteSharedScope(String scopeName, String tenantDomain) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting shared scope " + scopeName);
        }
        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        for (Map.Entry<String, KeyManagerDto> keyManagerEntry : tenantKeyManagers.entrySet()) {
            KeyManager keyManager = keyManagerEntry.getValue().getKeyManager();
            if (keyManager != null) {
                try {
                    keyManager.deleteScope(scopeName);
                } catch (APIManagementException e) {
                    log.error("Error while Deleting Shared Scope " + scopeName + " from Key Manager " +
                            keyManagerEntry.getKey(), e);
                }
            }
        }
        apiMgtDAO.deleteSharedScope(scopeName, tenantDomain);
        deleteScope(scopeName, APIUtil.getTenantIdFromTenantDomain(tenantDomain));
    }

    /**
     * Update a shared scope.
     *
     * @param sharedScope  Shared Scope
     * @param tenantDomain tenant domain
     * @throws APIManagementException If failed to update
     */
    @Override
    public void updateSharedScope(Scope sharedScope, String tenantDomain) throws APIManagementException {

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        for (Map.Entry<String, KeyManagerDto> keyManagerEntry : tenantKeyManagers.entrySet()) {
            KeyManager keyManager = keyManagerEntry.getValue().getKeyManager();
            if (keyManager != null) {
                try {
                    keyManager.updateScope(sharedScope);
                } catch (APIManagementException e) {
                    log.error("Error while Updating Shared Scope " + sharedScope.getKey() + " from Key Manager " +
                            keyManagerEntry.getKey(), e);
                }
            }
        }
        updateScope(sharedScope, tenantId);
    }

    /**
     * Validate a shared scopes set. Add the additional attributes (scope description, bindings etc).
     *
     * @param scopes Shared scopes set
     * @throws APIManagementException If failed to validate
     */
    @Override
    public void validateSharedScopes(Set<Scope> scopes, String tenantDomain) throws APIManagementException {

        Map<String, KeyManagerDto> tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
        for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
            KeyManager keyManager = keyManagerDtoEntry.getValue().getKeyManager();
            if (keyManager != null) {
                keyManager.validateScopes(scopes);
            }
        }
    }

    @Override
    /**
     * Get the API and URI usages of the given shared scope
     *
     * @param uuid       UUID of the shared scope
     * @param tenantId ID of the Tenant domain
     * @throws APIManagementException If failed to validate
     */
    public SharedScopeUsage getSharedScopeUsage(String uuid, int tenantId) throws APIManagementException {
        return ApiMgtDAO.getInstance().getSharedScopeUsage(uuid, tenantId);
    }

    /**
     * This method returns the security audit properties
     *
     * @param userId user id
     * @return JSONObject security audit properties
     * @throws APIManagementException
     */
    public JSONObject getSecurityAuditAttributesFromConfig(String userId) throws APIManagementException {
        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        JSONObject securityAuditConfig = APIUtil.getSecurityAuditAttributesFromRegistry(tenantDomain);
        if (securityAuditConfig != null) {
            if ((securityAuditConfig.get(APIConstants.SECURITY_AUDIT_OVERRIDE_GLOBAL) != null) &&
                    securityAuditConfig.get(APIConstants.SECURITY_AUDIT_OVERRIDE_GLOBAL) instanceof Boolean &&
                    (Boolean) securityAuditConfig.get(APIConstants.SECURITY_AUDIT_OVERRIDE_GLOBAL)) {
                String apiToken = (String) securityAuditConfig.get(APIConstants.SECURITY_AUDIT_API_TOKEN);
                String collectionId = (String) securityAuditConfig.get(APIConstants.SECURITY_AUDIT_COLLECTION_ID);
                JSONObject tenantProperties = new JSONObject();

                if (StringUtils.isNotEmpty(apiToken) && StringUtils.isNotEmpty(collectionId)) {
                    tenantProperties.put(APIConstants.SECURITY_AUDIT_API_TOKEN, apiToken);
                    tenantProperties.put(APIConstants.SECURITY_AUDIT_COLLECTION_ID, collectionId);
                    return tenantProperties;
                }
            } else {
                return getSecurityAuditConfigurationProperties(tenantDomain);
            }
        } else {
            return getSecurityAuditConfigurationProperties(tenantDomain);
        }
        return null;
    }

    @Override
    public void saveAsyncApiDefinition(API api, String jsonText) throws APIManagementException {
        String apiId;
        String organization = api.getOrganization();
        if (api.getUuid() != null) {
            apiId = api.getUuid();
        } else if (api.getId().getUUID() != null) {
            apiId = api.getId().getUUID();
        } else {
            apiId = apiMgtDAO.getUUIDFromIdentifier(api.getId().getProviderName(), api.getId().getApiName(),
                    api.getId().getVersion(), organization);
        }

        try {
            apiPersistenceInstance.saveAsyncDefinition(new Organization(organization), apiId, jsonText);
        } catch (AsyncSpecPersistenceException e) {
            throw new APIManagementException("Error while persisting Async API definition ", e);
        }
    }

    /**
     * This method returns security audit properties from the API Manager Configuration
     *
     * @param tenantDomain tenant domain name
     * @return JSONObject security audit properties
     */
    private JSONObject getSecurityAuditConfigurationProperties(String tenantDomain) {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String apiToken = configuration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_API_TOKEN);
        String collectionId = configuration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_CID);
        String baseUrl = configuration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_BASE_URL);
        boolean isGlobal = Boolean.parseBoolean(configuration.getFirstProperty(APIConstants.API_SECURITY_AUDIT_GLOBAL));
        JSONObject configProperties = new JSONObject();

        if (StringUtils.isNotEmpty(apiToken) && StringUtils.isNotEmpty(collectionId)) {
            configProperties.put(APIConstants.SECURITY_AUDIT_API_TOKEN, apiToken);
            configProperties.put(APIConstants.SECURITY_AUDIT_COLLECTION_ID, collectionId);
            configProperties.put(APIConstants.SECURITY_AUDIT_BASE_URL, baseUrl);
            if (isGlobal || "carbon.super".equals(tenantDomain)) {
                return configProperties;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public List<APIResource> getRemovedProductResources(Set<URITemplate> updatedUriTemplates, API existingAPI) {
        Set<URITemplate> existingUriTemplates = existingAPI.getUriTemplates();
        List<APIResource> removedReusedResources = new ArrayList<>();

        for (URITemplate existingUriTemplate : existingUriTemplates) {
            // If existing URITemplate is used by any API Products
            if (!existingUriTemplate.retrieveUsedByProducts().isEmpty()) {
                String existingVerb = existingUriTemplate.getHTTPVerb();
                String existingPath = existingUriTemplate.getUriTemplate();
                boolean isReusedResourceRemoved = true;

                for (URITemplate updatedUriTemplate : updatedUriTemplates) {
                    String updatedVerb = updatedUriTemplate.getHTTPVerb();
                    String updatedPath = updatedUriTemplate.getUriTemplate();

                    //Check if existing reused resource is among updated resources
                    if (existingVerb.equalsIgnoreCase(updatedVerb) &&
                            existingPath.equalsIgnoreCase(updatedPath)) {
                        isReusedResourceRemoved = false;
                        break;
                    }
                }
                // Existing reused resource is not among updated resources
                if (isReusedResourceRemoved) {
                    APIResource removedResource = new APIResource(existingVerb, existingPath);
                    removedReusedResources.add(removedResource);
                }
            }
        }
        return removedReusedResources;
    }

    private void addScopes(Set<Scope> scopes, int tenantId) throws APIManagementException {

        if (scopes != null) {
            scopesDAO.addScopes(scopes, tenantId);
            for (Scope scope : scopes) {
                ScopeEvent scopeEvent = new ScopeEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.SCOPE_CREATE.name(), tenantId,
                        tenantDomain, scope.getKey(), scope.getName(), scope.getDescription());
                if (StringUtils.isNotEmpty(scope.getRoles()) && scope.getRoles().trim().length() > 0) {
                    scopeEvent.setRoles(Arrays.asList(scope.getRoles().split(",")));
                }
                APIUtil.sendNotification(scopeEvent, APIConstants.NotifierType.SCOPE.name());
            }
        }

    }

    private void updateScope(Scope scope, int tenantId) throws APIManagementException {

        if (scope != null) {
            scopesDAO.updateScope(scope, tenantId);
            ScopeEvent scopeEvent = new ScopeEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.SCOPE_UPDATE.name(), tenantId,
                    tenantDomain, scope.getKey(), scope.getName(), scope.getDescription());
            if (StringUtils.isNotEmpty(scope.getRoles()) && scope.getRoles().trim().length() > 0) {
                scopeEvent.setRoles(Arrays.asList(scope.getRoles().split(",")));
            }
            APIUtil.sendNotification(scopeEvent, APIConstants.NotifierType.SCOPE.name());
        }
    }

    private void deleteScope(String scopeKey, int tenantId) throws APIManagementException {

        if (StringUtils.isNotEmpty(scopeKey)) {
            scopesDAO.deleteScope(scopeKey, tenantId);
            ScopeEvent scopeEvent = new ScopeEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.SCOPE_DELETE.name(), tenantId,
                    tenantDomain, scopeKey, null, null);
            APIUtil.sendNotification(scopeEvent, APIConstants.NotifierType.SCOPE.name());
        }
    }

    private void deleteScopes(Set<String> scopes, int tenantId) throws APIManagementException {

        if (scopes != null) {
            for (String scope : scopes) {
                deleteScope(scope, tenantId);
            }
        }
    }

    @Override
    public API getAPIbyUUID(String uuid, String organization) throws APIManagementException {
        Organization org = new Organization(organization);
        try {
            PublisherAPI publisherAPI = apiPersistenceInstance.getPublisherAPI(org, uuid);
            if (publisherAPI != null) {
                API api = APIMapper.INSTANCE.toApi(publisherAPI);
                APIIdentifier apiIdentifier = api.getId();
                apiIdentifier.setUuid(uuid);
                api.setId(apiIdentifier);
                //Gateway type is obtained considering the gateway vendor.
                api.setGatewayType(APIUtil.getGatewayType(publisherAPI.getGatewayVendor()));
                api.setGatewayVendor(APIUtil.handleGatewayVendorRetrieval(publisherAPI.getGatewayVendor()));
                checkAccessControlPermission(userNameWithoutChange, api.getAccessControl(), api.getAccessControlRoles());
                /////////////////// Do processing on the data object//////////
                populateRevisionInformation(api, uuid);
                populateAPIInformation(uuid, organization, api);
                if (APIUtil.isSequenceDefined(api.getInSequence()) || APIUtil.isSequenceDefined(api.getOutSequence())
                        || APIUtil.isSequenceDefined(api.getFaultSequence())) {
                    if (migrationEnabled == null) {
                        loadMediationPoliciesAsOperationPoliciesToAPI(api, organization);
                    } else {
                        loadMediationPoliciesToAPI(api, organization);
                    }
                }
                populateAPIStatus(api);
                populateDefaultVersion(api);
                return api;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Failed to get API", e);
        } catch (OASPersistenceException e) {
            throw new APIManagementException("Error while retrieving the OAS definition", e);
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing the OAS definition", e);
        } catch (AsyncSpecPersistenceException e) {
            throw new APIManagementException("Error while retrieving the Async API definition", e);
        }
    }

    @Override
    public APISearchResult searchPaginatedAPIsByFQDN(String endpoint, String tenantDomain,
                                                     int offset, int limit) throws APIManagementException {
        String fqdn;
        APISearchResult result = new APISearchResult();

        try {
            URI uri = new URI(endpoint);
            fqdn = uri.getHost();
            if(fqdn == null) {
                return result;
            }
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while extracting fully qualified domain name from the given url: " + endpoint, e);
        }

        String query = ENDPOINT_CONFIG_SEARCH_TYPE_PREFIX + fqdn;
        Organization org = new Organization(tenantDomain);
        String adminUser = APIUtil.getTenantAdminUserName(tenantDomain);
        String[] roles = APIUtil.getFilteredUserRoles(adminUser);
        Map<String, Object> properties = APIUtil.getUserProperties(adminUser);
        UserContext userCtx = new UserContext(adminUser, org, properties, roles);

        try {
            PublisherAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForPublisher(org, query,
                    offset, limit, userCtx, "createdTime", "desc");
            if (log.isDebugEnabled()) {
                log.debug("Running Solr query : " + query);
            }
            if (searchAPIs != null) {
                List<PublisherAPIInfo> list = searchAPIs.getPublisherAPIInfoList();
                List<API> apiList = new ArrayList<>(list.size());
                for (PublisherAPIInfo publisherAPIInfo : list) {
                    API mappedAPI = APIMapper.INSTANCE.toApi(publisherAPIInfo);
                    populateAPIStatus(mappedAPI);
                    populateDefaultVersion(mappedAPI);
                    apiList.add(mappedAPI);
                }
                result.setApis(apiList);
                result.setApiCount(searchAPIs.getTotalAPIsCount());
            }
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while searching for APIs with Solr query: " + query , e);
        }

        return result ;
    }

    private void populateAPITier(APIProduct apiProduct) throws APIManagementException {

        if (apiProduct.isRevision()) {
            String apiLevelTier = apiMgtDAO.getAPILevelTier(apiProduct.getRevisionedApiProductId(), apiProduct.getUuid());
            apiProduct.setProductLevelPolicy(apiLevelTier);
        }
    }

    private void populateRevisionInformation(API api, String revisionUUID) throws APIManagementException {
        APIRevision apiRevision = apiMgtDAO.checkAPIUUIDIsARevisionUUID(revisionUUID);
        if (apiRevision != null && !StringUtils.isEmpty(apiRevision.getApiUUID())) {
            api.setRevision(true);
            api.setRevisionedApiId(apiRevision.getApiUUID());
            api.setRevisionId(apiRevision.getId());
        }
    }
    private void populateRevisionInformation(APIProduct apiProduct, String revisionUUID) throws APIManagementException {
        APIRevision apiRevision = apiMgtDAO.checkAPIUUIDIsARevisionUUID(revisionUUID);
        if (apiRevision != null && !StringUtils.isEmpty(apiRevision.getApiUUID())) {
            apiProduct.setRevision(true);
            apiProduct.setRevisionedApiProductId(apiRevision.getApiUUID());
            apiProduct.setRevisionId(apiRevision.getId());
        }
    }

    private void populateAPIStatus(API api) throws APIManagementException {
        if (api.isRevision()) {
            api.setStatus(apiMgtDAO.getAPIStatusFromAPIUUID(api.getRevisionedApiId()));
        } else {
            api.setStatus(apiMgtDAO.getAPIStatusFromAPIUUID(api.getUuid()));
        }
    }


    private void populateAPIStatus(APIProduct apiProduct) throws APIManagementException {
        if (apiProduct.isRevision()) {
            apiProduct.setState(apiMgtDAO.getAPIStatusFromAPIUUID(apiProduct.getRevisionedApiProductId()));
        } else {
            apiProduct.setState(apiMgtDAO.getAPIStatusFromAPIUUID(apiProduct.getUuid()));
        }
    }

    public APIProduct getAPIProductbyUUID(String uuid, String organization) throws APIManagementException {
        try {
            Organization org = new Organization(organization);
            PublisherAPIProduct publisherAPIProduct = apiPersistenceInstance.getPublisherAPIProduct(org, uuid);
            if (publisherAPIProduct != null) {
                APIProduct product = APIProductMapper.INSTANCE.toApiProduct(publisherAPIProduct);
                product.setID(new APIProductIdentifier(publisherAPIProduct.getProviderName(),
                        publisherAPIProduct.getApiProductName(), publisherAPIProduct.getVersion(), uuid));
                checkAccessControlPermission(userNameWithoutChange, product.getAccessControl(),
                        product.getAccessControlRoles());
                populateRevisionInformation(product, uuid);
                populateAPIProductInformation(uuid, organization, product);
                populateAPIStatus(product);
                populateAPITier(product);
                return product;
            } else {
                String msg = "Failed to get API Product. API Product artifact corresponding to artifactId " + uuid
                        + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (APIPersistenceException | OASPersistenceException | ParseException e) {
            String msg = "Failed to get API Product";
            throw new APIManagementException(msg, e);
        }
    }

    @Override
    public Map<String, Object> searchPaginatedAPIs(String searchQuery, String organization, int start, int end,
            String sortBy, String sortOrder) throws APIManagementException {
        Map<String, Object> result = new HashMap<String, Object>();
        if (log.isDebugEnabled()) {
            log.debug("Original search query received : " + searchQuery);
        }

        Organization org = new Organization(organization);
        String[] roles = APIUtil.getFilteredUserRoles(userNameWithoutChange);
        Map<String, Object> properties = APIUtil.getUserProperties(userNameWithoutChange);
        UserContext userCtx = new UserContext(userNameWithoutChange, org, properties, roles);
        try {
            PublisherAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForPublisher(org, searchQuery,
                    start, end, userCtx, sortBy, sortOrder);
            if (log.isDebugEnabled()) {
                log.debug("searched APIs for query : " + searchQuery + " :-->: " + searchAPIs.toString());
            }
            Set<Object> apiSet = new LinkedHashSet<>();
            if (searchAPIs != null) {
                List<PublisherAPIInfo> list = searchAPIs.getPublisherAPIInfoList();
                List<Object> apiList = new ArrayList<>();
                for (PublisherAPIInfo publisherAPIInfo : list) {
                    API mappedAPI = APIMapper.INSTANCE.toApi(publisherAPIInfo);
                    populateAPIStatus(mappedAPI);
                    populateDefaultVersion(mappedAPI);
                    apiList.add(mappedAPI);
                }
                apiSet.addAll(apiList);
                result.put("apis", apiSet);
                result.put("length", searchAPIs.getTotalAPIsCount());
                result.put("isMore", true);
            } else {
                result.put("apis", apiSet);
                result.put("length", 0);
                result.put("isMore", false);
            }
        } catch (APIPersistenceException e) {
            String errorMsg = (e.getMessage() == null || e.getMessage().isEmpty()) ? "Error while searching the api" : e.getMessage();
            throw new APIManagementException(errorMsg, e);
        }
        return result ;
    }

    @Override
    public String addComment(String uuid, Comment comment, String user) throws APIManagementException {
        return apiMgtDAO.addComment(uuid, comment, user);
    }

    @Override
    public Comment getComment(ApiTypeWrapper apiTypeWrapper, String commentId, Integer replyLimit, Integer replyOffset)
            throws APIManagementException {
        return apiMgtDAO.getComment(apiTypeWrapper, commentId, replyLimit, replyOffset);
    }

    @Override
    public CommentList getComments(ApiTypeWrapper apiTypeWrapper, String parentCommentID,
                                                                    Integer replyLimit, Integer replyOffset) throws
            APIManagementException {
        return apiMgtDAO.getComments(apiTypeWrapper, parentCommentID, replyLimit, replyOffset);
    }

    @Override
    public boolean editComment(ApiTypeWrapper apiTypeWrapper, String commentId, Comment comment) throws
            APIManagementException {
        return apiMgtDAO.editComment(apiTypeWrapper, commentId, comment);
    }

    @Override
    public boolean deleteComment(ApiTypeWrapper apiTypeWrapper, String commentId) throws APIManagementException {
        return apiMgtDAO.deleteComment(apiTypeWrapper, commentId);
    }

    /**
     * Get minimal details of API by registry artifact id
     *
     * @param uuid Registry artifact id
     * @param organization identifier of the organization
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    @Override
    public API getLightweightAPIByUUID(String uuid, String organization) throws APIManagementException {
        try {
            Organization org = new Organization(organization);
            PublisherAPI publisherAPI = apiPersistenceInstance.getPublisherAPI(org, uuid);
            if (publisherAPI != null) {
                API api = APIMapper.INSTANCE.toApi(publisherAPI);
                checkAccessControlPermission(userNameWithoutChange, api.getAccessControl(), api.getAccessControlRoles());
                /// populate relavant external info
                // environment
                String environmentString = null;
                if (api.getEnvironments() != null) {
                    environmentString = String.join(",", api.getEnvironments());
                }
                api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environmentString, organization));
                //CORS . if null is returned, set default config from the configuration
                if (api.getCorsConfiguration() == null) {
                    api.setCorsConfiguration(APIUtil.getDefaultCorsConfiguration());
                }
                api.setOrganization(organization);
                String tiers = null;
                Set<Tier> apiTiers = api.getAvailableTiers();
                Set<String> tierNameSet = new HashSet<String>();
                for (Tier t : apiTiers) {
                    tierNameSet.add(t.getName());
                }
                if (api.getAvailableTiers() != null) {
                    tiers = String.join("||", tierNameSet);
                }
                Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);
                Set<Tier> availableTiers = APIUtil.getAvailableTiers(definedTiers, tiers, api.getId().getApiName());
                api.removeAllTiers();
                api.setAvailableTiers(availableTiers);
                return api;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (APIPersistenceException e) {
            String msg = "Failed to get API with uuid " + uuid;
            throw new APIManagementException(msg, e);
        }
    }

    @Override
    public List<APIResource> getUsedProductResources(String uuid) throws APIManagementException {
        List<APIResource> usedProductResources = new ArrayList<>();
        Map<Integer, URITemplate> uriTemplates = ApiMgtDAO.getInstance().getURITemplatesOfAPIWithProductMapping(uuid);
        for (URITemplate uriTemplate : uriTemplates.values()) {
            // If existing URITemplate is used by any API Products
            if (!uriTemplate.retrieveUsedByProducts().isEmpty()) {
                APIResource apiResource = new APIResource(uriTemplate.getHTTPVerb(), uriTemplate.getUriTemplate());
                usedProductResources.add(apiResource);
            }
        }

        return usedProductResources;
    }

    @Override
    public void addDocumentationContent(String uuid, String docId, String organization, DocumentationContent content)
            throws APIManagementException {
        DocumentContent mappedContent = null;
        try {
            mappedContent = DocumentMapper.INSTANCE.toDocumentContent(content);
            DocumentContent doc = apiPersistenceInstance.addDocumentationContent(new Organization(organization), uuid, docId,
                    mappedContent);
        } catch (DocumentationPersistenceException e) {
            throw new APIManagementException("Error while adding content to doc " + docId);
        }
    }

    @Override
    public void addWSDLResource(String apiId, ResourceFile resource, String url, String organization) throws APIManagementException {
        if (!StringUtils.isEmpty(url)) {
            URL wsdlUrl;
            try {
                wsdlUrl = new URL(url);
            } catch (MalformedURLException e) {
                throw new APIManagementException("Invalid/Malformed WSDL URL : " + url, e,
                        ExceptionCodes.INVALID_WSDL_URL_EXCEPTION);
            }
            // Get the WSDL 1.1 or 2.0 processor and process the content based on the version
            WSDLProcessor wsdlProcessor = APIMWSDLReader.getWSDLProcessorForUrl(wsdlUrl);
            InputStream wsdlContent = wsdlProcessor.getWSDL();
            // wsdlResource.setContentStream(wsdlContent);

            org.wso2.carbon.apimgt.persistence.dto.ResourceFile wsdlResourceFile = new org.wso2.carbon.apimgt.persistence.dto.ResourceFile(
                    wsdlContent, null);
            try {
                apiPersistenceInstance.saveWSDL(
                        new Organization(organization), apiId,
                        wsdlResourceFile);
            } catch (WSDLPersistenceException e) {
                throw new APIManagementException("Error while adding WSDL to api " + apiId, e);
            }
        } else if (resource != null) {
            org.wso2.carbon.apimgt.persistence.dto.ResourceFile wsdlResourceFile = new org.wso2.carbon.apimgt.persistence.dto.ResourceFile(
                    resource.getContent(), resource.getContentType());
            try {
                apiPersistenceInstance.saveWSDL(
                        new Organization(organization), apiId,
                        wsdlResourceFile);
            } catch (WSDLPersistenceException e) {
                throw new APIManagementException("Error while adding WSDL to api " + apiId, e);
            }
        }
    }

    @Override
    public Map<String, Object> searchPaginatedContent(String searchQuery, String organization, int start, int end) throws APIManagementException {
        ArrayList<Object> compoundResult = new ArrayList<Object>();
        Map<Documentation, API> docMap = new HashMap<Documentation, API>();
        Map<Documentation, APIProduct> productDocMap = new HashMap<Documentation, APIProduct>();
        Map<String, Object> result = new HashMap<String, Object>();
        SortedSet<API> apiSet = new TreeSet<API>(new APINameComparator());
        SortedSet<APIProduct> apiProductSet = new TreeSet<APIProduct>(new APIProductNameComparator());

        String userame = userNameWithoutChange;
        Organization org = new Organization(organization);
        Map<String, Object> properties = APIUtil.getUserProperties(userame);
        String[] roles = APIUtil.getFilteredUserRoles(userame);
        UserContext ctx = new UserContext(userame, org, properties, roles);


        try {
            PublisherContentSearchResult results = apiPersistenceInstance.searchContentForPublisher(org, searchQuery,
                    start, end, ctx);
            if (results != null) {
                List<SearchContent> resultList = results.getResults();
                for (SearchContent item : resultList) {
                    if ("API".equals(item.getType())) {
                        PublisherSearchContent publiserAPI = (PublisherSearchContent) item;
                        API api = new API(new APIIdentifier(publiserAPI.getProvider(), publiserAPI.getName(),
                                publiserAPI.getVersion()));
                        api.setUuid(publiserAPI.getId());
                        api.setContext(publiserAPI.getContext());
                        api.setContextTemplate(publiserAPI.getContext());
                        api.setStatus(publiserAPI.getStatus());
                        api.setThumbnailUrl(publiserAPI.getThumbnailUri());
                        apiSet.add(api);
                    } else if ("APIProduct".equals(item.getType())) {

                        PublisherSearchContent publiserAPI = (PublisherSearchContent) item;
                        APIProduct api = new APIProduct(new APIProductIdentifier(publiserAPI.getProvider(),
                                publiserAPI.getName(), publiserAPI.getVersion()));
                        api.setUuid(publiserAPI.getId());
                        api.setContextTemplate(publiserAPI.getContext());
                        api.setState(publiserAPI.getStatus());
                        api.setThumbnailUrl(publiserAPI.getThumbnailUri());
                        apiProductSet.add(api);
                    } else if (item instanceof DocumentSearchContent) {
                        // doc item
                        DocumentSearchContent docItem = (DocumentSearchContent) item;
                        Documentation doc = new Documentation(
                                DocumentationType.valueOf(docItem.getDocType().toString()), docItem.getName());
                        doc.setSourceType(DocumentSourceType.valueOf(docItem.getSourceType().toString()));
                        doc.setVisibility(DocumentVisibility.valueOf(docItem.getVisibility().toString()));
                        doc.setId(docItem.getId());
                        if ("API".equals(docItem.getAssociatedType())) {

                            API api = new API(new APIIdentifier(docItem.getApiProvider(), docItem.getApiName(),
                                    docItem.getApiVersion()));
                            api.setUuid(docItem.getApiUUID());
                            docMap.put(doc, api);
                        } else if ("APIProduct".equals(docItem.getAssociatedType())) {
                            APIProduct api = new APIProduct(new APIProductIdentifier(docItem.getApiProvider(),
                                    docItem.getApiName(), docItem.getApiVersion()));
                            api.setUuid(docItem.getApiUUID());
                            productDocMap.put(doc, api);
                        }
                    }
                }
                compoundResult.addAll(apiSet);
                compoundResult.addAll(apiProductSet);
                compoundResult.addAll(docMap.entrySet());
                compoundResult.addAll(productDocMap.entrySet());
                compoundResult.sort(new ContentSearchResultNameComparator());
                result.put("length", results.getTotalCount() );
            } else {
                result.put("length", compoundResult.size() );
            }

        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while searching content ", e);
        }
        result.put("apis", compoundResult);
        return result;
    }

    @Override
    public void setThumbnailToAPI(String apiId, ResourceFile resource, String organization) throws APIManagementException {

        try {
            org.wso2.carbon.apimgt.persistence.dto.ResourceFile iconResourceFile = new org.wso2.carbon.apimgt.persistence.dto.ResourceFile(
                    resource.getContent(), resource.getContentType());
            apiPersistenceInstance.saveThumbnail(new Organization(organization), apiId, iconResourceFile);
        } catch (ThumbnailPersistenceException e) {
            if (e.getErrorHandler() == ExceptionCodes.API_NOT_FOUND) {
                throw new APIMgtResourceNotFoundException(e);
            } else {
                throw new APIManagementException("Error while saving thumbnail ", e);
            }
        }
    }

    protected void checkAccessControlPermission(String userNameWithTenantDomain, String accessControlProperty,
            String publisherAccessControlRoles) throws APIManagementException {

        // String userNameWithTenantDomain = (userNameWithoutChange != null) ? userNameWithoutChange : username;

        if (accessControlProperty == null || accessControlProperty.trim().isEmpty()
                || accessControlProperty.equalsIgnoreCase(APIConstants.NO_ACCESS_CONTROL)) {
            if (log.isDebugEnabled()) {
                log.debug("API does not have any access control restriction");
            }
            return;
        }
        if (APIUtil.hasPermission(userNameWithTenantDomain, APIConstants.Permissions.APIM_ADMIN)) {
            return;
        }

        if (publisherAccessControlRoles != null && !publisherAccessControlRoles.trim().isEmpty()) {
            String[] accessControlRoleList = publisherAccessControlRoles.split(",");
            if (log.isDebugEnabled()) {
                log.debug("API has restricted access to creators and publishers with the roles : "
                        + Arrays.toString(accessControlRoleList));
            }
            String[] userRoleList = APIUtil.getListOfRoles(userNameWithTenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("User " + username + " has roles " + Arrays.toString(userRoleList));
            }
            for (String role : accessControlRoleList) {
                if (!role.equalsIgnoreCase(APIConstants.NULL_USER_ROLE_LIST)
                        && APIUtil.compareRoleList(userRoleList, role)) {
                    return;
                }
            }

            throw new APIManagementException(APIConstants.UN_AUTHORIZED_ERROR_MESSAGE + " view or modify the api");
        }

    }

    @Override
    public void saveGraphqlSchemaDefinition(String apiId, String definition, String organization)
            throws APIManagementException {

        try {
            apiPersistenceInstance.saveGraphQLSchemaDefinition(new Organization(organization), apiId, definition);

        } catch (GraphQLPersistenceException e) {
            if (e.getErrorHandler() == ExceptionCodes.API_NOT_FOUND) {
                throw new APIMgtResourceNotFoundException(e);
            } else {
                throw new APIManagementException("Error while saving graphql definition ", e);
            }
        }
    }

    /**
     * Returns APIProduct Search result based on the provided query.
     *
     * @param searchQuery Ex: provider=*admin*
     * @return APIProduct result
     * @throws APIManagementException
     */

    public Map<String, Object> searchPaginatedAPIProducts(String searchQuery, String organization, int start, int end) throws APIManagementException {
        SortedSet<APIProduct> productSet = new TreeSet<APIProduct>(new APIProductNameComparator());
        List<APIProduct> productList = new ArrayList<APIProduct>();
        Map<String, Object> result = new HashMap<String, Object>();
        if (log.isDebugEnabled()) {
            log.debug("Original search query received : " + searchQuery);
        }

        Organization org = new Organization(organization);
        String[] roles = APIUtil.getFilteredUserRoles(userNameWithoutChange);
        Map<String, Object> properties = APIUtil.getUserProperties(userNameWithoutChange);
        UserContext userCtx = new UserContext(userNameWithoutChange, org, properties, roles);
        try {
            PublisherAPIProductSearchResult searchAPIs = apiPersistenceInstance.searchAPIProductsForPublisher(org,
                    searchQuery, start, end, userCtx);
            if (log.isDebugEnabled()) {
                log.debug("searched API products for query : " + searchQuery + " :-->: " + searchAPIs.toString());
            }

            if (searchAPIs != null) {
                List<PublisherAPIProductInfo> list = searchAPIs.getPublisherAPIProductInfoList();
                List<Object> apiList = new ArrayList<>();
                for (PublisherAPIProductInfo publisherAPIInfo : list) {
                    APIProduct mappedAPI = new APIProduct(new APIProductIdentifier(publisherAPIInfo.getProviderName(),
                            publisherAPIInfo.getApiProductName(), publisherAPIInfo.getVersion()));
                    mappedAPI.setUuid(publisherAPIInfo.getId());
                    mappedAPI.setState(publisherAPIInfo.getState());
                    mappedAPI.setContext(publisherAPIInfo.getContext());
                    mappedAPI.setApiSecurity(publisherAPIInfo.getApiSecurity());
                    mappedAPI.setThumbnailUrl(publisherAPIInfo.getThumbnail());
                    populateAPIStatus(mappedAPI);
                    productList.add(mappedAPI);
                }
                productSet.addAll(productList);
                result.put("products", productSet);
                result.put("length", searchAPIs.getTotalAPIsCount());
                result.put("isMore", true);
            } else {
                result.put("products", productSet);
                result.put("length", 0);
                result.put("isMore", false);
            }
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while searching the api ", e);
        }
        return result ;
    }

    /**
     * Adds a new APIRevision to an existing API
     *
     * @param apiRevision APIRevision
     * @throws APIManagementException if failed to add APIRevision
     */
    @Override
    public String addAPIRevision(APIRevision apiRevision, String organization) throws APIManagementException {
        int revisionCountPerAPI = apiMgtDAO.getRevisionCountByAPI(apiRevision.getApiUUID());
        int maxRevisionCount = getMaxRevisionCount(organization);
        if (revisionCountPerAPI >= maxRevisionCount) {
            String errorMessage = "Maximum number of revisions per API has reached. " +
                    "Need to remove stale revision to create a new Revision for API with API UUID:"
                    + apiRevision.getApiUUID();
            throw new APIManagementException(errorMessage, ExceptionCodes.from(ExceptionCodes.MAXIMUM_REVISIONS_REACHED,
                    apiRevision.getApiUUID()));
        }

        int revisionId = apiMgtDAO.getMostRecentRevisionId(apiRevision.getApiUUID()) + 1;
        apiRevision.setId(revisionId);
        APIIdentifier apiId = APIUtil.getAPIIdentifierFromUUID(apiRevision.getApiUUID());
        if (apiId == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiRevision.getApiUUID(), ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                    apiRevision.getApiUUID()));
        }
        apiId.setUuid(apiRevision.getApiUUID());
        String revisionUUID;
        try {
            revisionUUID = apiPersistenceInstance.addAPIRevision(new Organization(organization),
                    apiId.getUUID(), revisionId);
        } catch (APIPersistenceException e) {
            String errorMessage = "Failed to add revision registry artifacts";
            throw new APIManagementException(errorMessage, ExceptionCodes.from(ExceptionCodes.
                    ERROR_CREATING_API_REVISION, apiRevision.getApiUUID()));
        }
        if (StringUtils.isEmpty(revisionUUID)) {
            String errorMessage = "Failed to retrieve revision uuid";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.API_REVISION_UUID_NOT_FOUND));
        }
        apiRevision.setRevisionUUID(revisionUUID);
        apiMgtDAO.addAPIRevision(apiRevision);
        if (importExportAPI != null) {
            try {
                File artifact = importExportAPI
                        .exportAPI(apiRevision.getApiUUID(), revisionUUID, true, ExportFormat.JSON, false, true,
                                organization);
                // Keeping the organization as tenant domain since MG does not support organization-wise deployment
                // Artifacts will be deployed in ST for all organizations
                gatewayArtifactsMgtDAO.addGatewayAPIArtifactAndMetaData(apiRevision.getApiUUID(), apiId.getApiName(),
                        apiId.getVersion(), apiRevision.getRevisionUUID(), organization, APIConstants.HTTP_PROTOCOL,
                         artifact);
                if (artifactSaver != null) {
                    // Keeping the organization as tenant domain since MG does not support organization-wise deployment
                    // Artifacts will be deployed in ST for all organizations
                    artifactSaver.saveArtifact(apiRevision.getApiUUID(), apiId.getApiName(), apiId.getVersion(),
                            apiRevision.getRevisionUUID(), organization, artifact);
                }
            } catch (APIImportExportException | ArtifactSynchronizerException e) {
                throw new APIManagementException("Error while Store the Revision Artifact",
                        ExceptionCodes.from(ExceptionCodes.API_REVISION_UUID_NOT_FOUND));
            }
        }
        return revisionUUID;
    }

    /**
     * Util method to read and return the max revision count per API, using the tenant configs
     *
     * @param organization organization name
     * @return max revision count per API
     * @throws APIManagementException
     */
    private int getMaxRevisionCount(String organization) throws APIManagementException {
        JSONObject jsonObject = APIUtil.getTenantConfig(organization);
        if (jsonObject.containsKey(APIConstants.API_MAX_REVISION_COUNT_PROPERTY_NAME)){
            return Integer.valueOf(jsonObject.get(APIConstants.API_MAX_REVISION_COUNT_PROPERTY_NAME).toString());
        } else {
            return 5;
        }
    }

    /**
     * Get a Revision related to provided and revision UUID
     *
     * @param revisionUUID API Revision UUID
     * @return API Revision
     * @throws APIManagementException if failed to get the related API revision
     */
    @Override
    public APIRevision getAPIRevision(String revisionUUID) throws APIManagementException {
        return apiMgtDAO.getRevisionByRevisionUUID(revisionUUID);
    }

    /**
     * Get the revision UUID from the Revision no and API UUID
     *
     * @param revisionNum   revision number
     * @param apiUUID       UUID of the API
     * @return UUID of the revision
     * @throws APIManagementException if failed to get the API revision uuid
     */
    @Override
    public String getAPIRevisionUUID(String revisionNum, String apiUUID) throws APIManagementException {
        return apiMgtDAO.getRevisionUUID(revisionNum, apiUUID);
    }

    /**
     * Get the revision UUID from the Revision no and API UUID
     *
     * @param revisionNum   revision number
     * @param apiUUID       UUID of the API
     * @param organization  organization ID of the API
     * @return UUID of the revision
     * @throws APIManagementException if failed to get the API revision uuid
     */
    @Override
    public String getAPIRevisionUUIDByOrganization(String revisionNum, String apiUUID, String organization) throws APIManagementException {
        return apiMgtDAO.getRevisionUUIDByOrganization(revisionNum, apiUUID, organization);
    }

    /**
     * Get the earliest revision UUID from the revision list for a given API
     *
     * @param apiUUID API UUID
     * @return Earliest revision's UUID
     * @throws APIManagementException if failed to get the revision
     */
    @Override
    public String getEarliestRevisionUUID(String apiUUID) throws APIManagementException {
        return apiMgtDAO.getEarliestRevision(apiUUID);
    }

    /**
     * Get the latest revision UUID from the revision list for a given API
     *
     * @param apiUUID API UUID
     * @return Latest revision's UUID
     * @throws APIManagementException if failed to get the revision
     */
    @Override
    public String getLatestRevisionUUID(String apiUUID) throws APIManagementException {
        return apiMgtDAO.getLatestRevisionUUID(apiUUID);
    }

    /**
     * Get a List of API Revisions related to provided API UUID
     *
     * @param apiUUID API  UUID
     * @return API Revision List
     * @throws APIManagementException if failed to get the related API revision
     */
    @Override
    public List<APIRevision> getAPIRevisions(String apiUUID) throws APIManagementException {
        return apiMgtDAO.getRevisionsListByAPIUUID(apiUUID);
    }

    /**
     * Adds a new APIRevisionDeployment to an existing API
     *
     * @param apiId API UUID
     * @param apiRevisionId API Revision UUID
     * @param apiRevisionDeployments List of APIRevisionDeployment objects
     * @param organization identifier of the organization
     * @throws APIManagementException if failed to add APIRevision
     */
    @Override
    public void deployAPIRevision(String apiId, String apiRevisionId,
            List<APIRevisionDeployment> apiRevisionDeployments, String organization)
            throws APIManagementException {
        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiId);
        if (organization ==  null) {
            String tenantDomain = getTenantDomain(apiIdentifier);
            if (tenantDomain != null) {
                organization = tenantDomain;
            }
        }
        if (apiIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        List<APIRevisionDeployment> currentApiRevisionDeploymentList =
                apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(apiId);
        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        API api = getLightweightAPIByUUID(apiId, organization);
        api.setRevisionedApiId(apiRevision.getRevisionUUID());
        api.setRevisionId(apiRevision.getId());
        api.setUuid(apiId);
        api.getId().setUuid(apiId);
        api.setOrganization(organization);
        Set<String> environmentsToAdd = new HashSet<>();
        Map<String, String> gatewayVhosts = new HashMap<>();
        Set<APIRevisionDeployment> environmentsToRemove = new HashSet<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeployments) {
            for (APIRevisionDeployment currentapiRevisionDeployment : currentApiRevisionDeploymentList) {
                if (StringUtils.equalsIgnoreCase(currentapiRevisionDeployment.getDeployment(),
                        apiRevisionDeployment.getDeployment())) {
                    environmentsToRemove.add(currentapiRevisionDeployment);
                }
            }
            environmentsToAdd.add(apiRevisionDeployment.getDeployment());
            gatewayVhosts.put(apiRevisionDeployment.getDeployment(), apiRevisionDeployment.getVhost());
        }
        if (environmentsToRemove.size() > 0) {
            apiMgtDAO.removeAPIRevisionDeployment(apiId, environmentsToRemove);
            removeFromGateway(api, environmentsToRemove, environmentsToAdd);
        }
        GatewayArtifactsMgtDAO.getInstance()
                .addAndRemovePublishedGatewayLabels(apiId, apiRevisionId, environmentsToAdd, gatewayVhosts,
                        environmentsToRemove);
        apiMgtDAO.addAPIRevisionDeployment(apiRevisionId, apiRevisionDeployments);
        if (environmentsToAdd.size() > 0) {
            // TODO remove this to organization once the microgateway can build gateway based on organization.
            gatewayManager.deployToGateway(api, organization, environmentsToAdd);
        }
        String publishedDefaultVersion = getPublishedDefaultVersion(apiIdentifier);
        String defaultVersion = getDefaultVersion(apiIdentifier);
        apiMgtDAO.updateDefaultAPIPublishedVersion(apiIdentifier);
        if (publishedDefaultVersion != null) {
            if (apiIdentifier.getVersion().equals(defaultVersion)) {
                api.setAsPublishedDefaultVersion(true);
            }
            if (api.isPublishedDefaultVersion() && !apiIdentifier.getVersion().equals(publishedDefaultVersion)) {
                APIIdentifier previousDefaultVersionIdentifier = new APIIdentifier(api.getId().getProviderName(),
                        api.getId().getApiName(), publishedDefaultVersion);
                sendUpdateEventToPreviousDefaultVersion(previousDefaultVersionIdentifier, organization);
            }
        }
    }

    /**
     * Adds a new APIRevisionDeployment to an existing API
     *
     * @param apiId                   API UUID
     * @param apiRevisionUUID         API Revision UUID
     * @param deployedAPIRevisionList List of APIRevisionDeployment objects
     * @throws APIManagementException if failed to add APIRevision
     */
    @Override
    public void addDeployedAPIRevision(String apiId, String apiRevisionUUID,
                                       List<DeployedAPIRevision> deployedAPIRevisionList)
            throws APIManagementException {

        List<DeployedAPIRevision> currentDeployedApiRevisionList =
                apiMgtDAO.getDeployedAPIRevisionByApiUUID(apiId);
        Set<DeployedAPIRevision> environmentsToRemove = new HashSet<>();

        // Deployments to add
        List<DeployedAPIRevision> environmentsToAdd = new ArrayList<>();

        List<String> envNames = new ArrayList<>();

        for (DeployedAPIRevision deployedAPIRevision : deployedAPIRevisionList) {
            // Remove duplicate entries for same revision uuid and env from incoming list
            if (!envNames.contains(deployedAPIRevision.getDeployment())) {
                envNames.add(deployedAPIRevision.getDeployment());
                environmentsToAdd.add(deployedAPIRevision);
                // Remove old deployed-revision entries of same env and apiid from existing db records
                for (DeployedAPIRevision currentapiRevisionDeployment : currentDeployedApiRevisionList) {
                    if (StringUtils.equalsIgnoreCase(currentapiRevisionDeployment.getDeployment(),
                            deployedAPIRevision.getDeployment())) {
                        environmentsToRemove.add(currentapiRevisionDeployment);
                    }
                }
            }
        }
        // Discard old deployment info
        if (environmentsToRemove.size() > 0) {
            apiMgtDAO.removeDeployedAPIRevision(apiId, environmentsToRemove);
        }
        // Add new deployed revision update to db
        if (deployedAPIRevisionList.size() > 0) {
            apiMgtDAO.addDeployedAPIRevision(apiRevisionUUID, environmentsToAdd);
        }
    }

    @Override
    public void removeUnDeployedAPIRevision(String apiId, String apiRevisionUUID,
                                            String environment)
            throws APIManagementException {
        Set<DeployedAPIRevision> environmentsToRemove = new HashSet<>();
        environmentsToRemove.add(new DeployedAPIRevision(apiRevisionUUID, environment));
        // TODO (shanakama) this logic should be enabled in near future.Commented to avoid breaking existing features.
        //apiMgtDAO.setUnDeployedAPIRevision(apiId, environmentsToRemove);
    }

    @Override
    public void updateAPIDisplayOnDevportal(String apiId, String apiRevisionId, APIRevisionDeployment apiRevisionDeployment)
            throws APIManagementException {

        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        List<APIRevisionDeployment> currentApiRevisionDeploymentList =
                apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(apiId);
        Set<APIRevisionDeployment> environmentsToUpdate = new HashSet<>();
        for (APIRevisionDeployment currentapiRevisionDeployment : currentApiRevisionDeploymentList) {
            if (StringUtils.equalsIgnoreCase(currentapiRevisionDeployment.getDeployment(),
                    apiRevisionDeployment.getDeployment())) {
                environmentsToUpdate.add(apiRevisionDeployment);
            }
        }
        // if the provided deployment doesn't exist we are not adding to update list
        if (environmentsToUpdate.size() > 0) {
            apiMgtDAO.updateAPIRevisionDeployment(apiId, environmentsToUpdate);
        } else {
            throw new APIMgtResourceNotFoundException("deployment with " + apiRevisionDeployment.getDeployment() +
                    " not found", ExceptionCodes.from(ExceptionCodes.EXISTING_DEPLOYMENT_NOT_FOUND,
                    apiRevisionDeployment.getDeployment()));
        }
    }

    private API getAPIbyUUID(String apiId, APIRevision apiRevision, String organization)
            throws APIManagementException {

        API api = getAPIbyUUID(apiRevision.getApiUUID(), organization);
        api.setRevisionedApiId(apiRevision.getRevisionUUID());
        api.setRevisionId(apiRevision.getId());
        api.setUuid(apiId);
        api.getId().setUuid(apiId);
        return api;
    }

    @Override
    public APIRevisionDeployment getAPIRevisionDeployment(String name, String revisionId) throws APIManagementException {
         return apiMgtDAO.getAPIRevisionDeploymentByNameAndRevsionID(name,revisionId);
    }

    @Override
    public List<APIRevisionDeployment> getAPIRevisionDeploymentList(String revisionUUID) throws APIManagementException {
         return apiMgtDAO.getAPIRevisionDeploymentByRevisionUUID(revisionUUID);
    }

    /**
     * Remove a new APIRevisionDeployment to an existing API
     *
     * @param apiId API UUID
     * @param apiRevisionId API Revision UUID
     * @param apiRevisionDeployments List of APIRevisionDeployment objects
     * @param organization
     * @throws APIManagementException if failed to add APIRevision
     */
    @Override
    public void undeployAPIRevisionDeployment(String apiId, String apiRevisionId,
            List<APIRevisionDeployment> apiRevisionDeployments, String organization)
            throws APIManagementException {

        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        API api = getAPIbyUUID(apiId, apiRevision, organization);
        Set<String> environmentsToRemove = new HashSet<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeployments) {
            environmentsToRemove.add(apiRevisionDeployment.getDeployment());
        }
        removeFromGateway(api, new HashSet<>(apiRevisionDeployments), Collections.emptySet());
        apiMgtDAO.removeAPIRevisionDeployment(apiRevisionId, apiRevisionDeployments);
        GatewayArtifactsMgtDAO.getInstance().removePublishedGatewayLabels(apiId, apiRevisionId, environmentsToRemove);
    }

    /**
     * Restore a provided API Revision as the current API of the API
     *
     * @param apiId API UUID
     * @param apiRevisionId API Revision UUID
     * @throws APIManagementException if failed to restore APIRevision
     */
    @Override
    public void restoreAPIRevision(String apiId, String apiRevisionId, String organization)
            throws APIManagementException {
        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        apiIdentifier.setUuid(apiId);
        try {
            apiPersistenceInstance.restoreAPIRevision(new Organization(organization),
                    apiIdentifier.getUUID(), apiRevision.getRevisionUUID(), apiRevision.getId());
        } catch (APIPersistenceException e) {
            String errorMessage = "Failed to restore registry artifacts";
            throw new APIManagementException(errorMessage,ExceptionCodes.from(ExceptionCodes.
                    ERROR_RESTORING_API_REVISION,apiRevision.getApiUUID()));
        }
        apiMgtDAO.restoreAPIRevision(apiRevision);
    }

    /**
     * Delete an API Revision
     *
     * @param apiId API UUID
     * @param apiRevisionId API Revision UUID
     * @param organization identifier of the organization
     * @throws APIManagementException if failed to delete APIRevision
     */
    @Override
    public void deleteAPIRevision(String apiId, String apiRevisionId, String organization)
            throws APIManagementException {
        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = getAPIRevisionDeploymentList(apiRevisionId);
        if (apiRevisionDeploymentsResponse.size() != 0) {
            String errorMessage = "Couldn't delete API revision since API revision is currently deployed to a gateway" +
                    "." +
                    "You need to undeploy the API Revision from the gateway before attempting deleting API Revision: "
                    + apiRevision.getRevisionUUID();
            throw new APIManagementException(errorMessage, ExceptionCodes.from(ExceptionCodes.
                    EXISTING_API_REVISION_DEPLOYMENT_FOUND, apiRevisionId));
        }
        apiIdentifier.setUuid(apiId);
        try {
            apiPersistenceInstance.deleteAPIRevision(new Organization(organization),
                    apiIdentifier.getUUID(), apiRevision.getRevisionUUID(), apiRevision.getId());
        } catch (APIPersistenceException e) {
            String errorMessage = "Failed to delete registry artifacts";
            throw new APIManagementException(errorMessage,ExceptionCodes.from(ExceptionCodes.
                    ERROR_DELETING_API_REVISION,apiRevision.getApiUUID()));
        }
        apiMgtDAO.deleteAPIRevision(apiRevision);
        gatewayArtifactsMgtDAO.deleteGatewayArtifact(apiRevision.getApiUUID(), apiRevision.getRevisionUUID());
        if (artifactSaver != null) {
            try {
                artifactSaver.removeArtifact(apiRevision.getApiUUID(), apiIdentifier.getApiName(),
                        apiIdentifier.getVersion(), apiRevision.getRevisionUUID(), organization);
            } catch (ArtifactSynchronizerException e) {
                log.error("Error while deleting Runtime artifacts from artifact Store", e);
            }
        }
    }

    @Override
    public String addAPIProductRevision(APIRevision apiRevision, String organization) throws APIManagementException {
        int revisionCountPerAPI = apiMgtDAO.getRevisionCountByAPI(apiRevision.getApiUUID());
        if (revisionCountPerAPI > 4) {
            String errorMessage = "Maximum number of revisions per API Product has reached. " +
                    "Need to remove stale revision to create a new Revision for API Product with id:"
                    + apiRevision.getApiUUID();
            throw new APIManagementException(errorMessage, ExceptionCodes.from(ExceptionCodes.MAXIMUM_REVISIONS_REACHED,
                    apiRevision.getApiUUID()));
        }
        int revisionId = apiMgtDAO.getMostRecentRevisionId(apiRevision.getApiUUID()) + 1;
        apiRevision.setId(revisionId);
        APIProductIdentifier apiProductIdentifier = APIUtil.getAPIProductIdentifierFromUUID(apiRevision.getApiUUID());
        if (apiProductIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Product with ID: "
                    + apiRevision.getApiUUID(), ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiRevision.getApiUUID()));
        }
        apiProductIdentifier.setUuid(apiRevision.getApiUUID());
        String revisionUUID;
        try {
            revisionUUID = apiPersistenceInstance.addAPIRevision(new Organization(tenantDomain),
                    apiProductIdentifier.getUUID(), revisionId);
        } catch (APIPersistenceException e) {
            String errorMessage = "Failed to add revision registry artifacts";
            throw new APIManagementException(errorMessage, ExceptionCodes.from(ExceptionCodes.
                    ERROR_CREATING_API_REVISION, apiRevision.getApiUUID()));
        }
        if (StringUtils.isEmpty(revisionUUID)) {
            String errorMessage = "Failed to retrieve revision uuid";
            throw new APIManagementException(errorMessage,ExceptionCodes.from(ExceptionCodes.API_REVISION_UUID_NOT_FOUND));
        }
        apiRevision.setRevisionUUID(revisionUUID);
        apiMgtDAO.addAPIProductRevision(apiRevision);
        try {
            File artifact = importExportAPI
                    .exportAPIProduct(apiRevision.getApiUUID(), revisionUUID, true, ExportFormat.JSON,
                            false, true, organization);
            gatewayArtifactsMgtDAO
                    .addGatewayAPIArtifactAndMetaData(apiRevision.getApiUUID(),apiProductIdentifier.getName(),
                    apiProductIdentifier.getVersion(), apiRevision.getRevisionUUID(), tenantDomain,
                            APIConstants.API_PRODUCT, artifact);
            if (artifactSaver != null) {
                artifactSaver.saveArtifact(apiRevision.getApiUUID(), apiProductIdentifier.getName(),
                        apiProductIdentifier.getVersion(), apiRevision.getRevisionUUID(), tenantDomain, artifact);
            }
        } catch (APIImportExportException | ArtifactSynchronizerException e) {
            throw new APIManagementException("Error while Store the Revision Artifact",
                    ExceptionCodes.from(ExceptionCodes.API_REVISION_UUID_NOT_FOUND));
        }
        return revisionUUID;
    }

    @Override
    public void deployAPIProductRevision(String apiProductId, String apiRevisionId,
                                         List<APIRevisionDeployment> apiRevisionDeployments)
            throws APIManagementException {
        APIProductIdentifier apiProductIdentifier = APIUtil.getAPIProductIdentifierFromUUID(apiProductId);
        if (apiProductIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Product with ID: "
                    + apiProductId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiProductId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        APIProduct product = getAPIProductbyUUID(apiRevisionId, tenantDomain);
        product.setUuid(apiProductId);
        List<APIRevisionDeployment> currentApiRevisionDeploymentList =
                apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(apiProductId);
        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        Set<String> environmentsToAdd = new HashSet<>();
        Map<String, String> gatewayVhosts = new HashMap<>();
        Set<APIRevisionDeployment> environmentsToRemove = new HashSet<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeployments) {
            for (APIRevisionDeployment currentapiRevisionDeployment : currentApiRevisionDeploymentList) {
                if (StringUtils.equalsIgnoreCase(currentapiRevisionDeployment.getDeployment(),
                        apiRevisionDeployment.getDeployment())) {
                    environmentsToRemove.add(currentapiRevisionDeployment);
                }
            }
            environmentsToAdd.add(apiRevisionDeployment.getDeployment());
            gatewayVhosts.put(apiRevisionDeployment.getDeployment(), apiRevisionDeployment.getVhost());
        }
        if (environmentsToRemove.size() > 0) {
            apiMgtDAO.removeAPIRevisionDeployment(apiProductId,environmentsToRemove);
            removeFromGateway(product, tenantDomain, environmentsToRemove, environmentsToAdd);
        }
        GatewayArtifactsMgtDAO.getInstance()
                .addAndRemovePublishedGatewayLabels(apiProductId, apiRevisionId, environmentsToAdd, gatewayVhosts,
                        environmentsToRemove);
        apiMgtDAO.addAPIRevisionDeployment(apiRevisionId, apiRevisionDeployments);
        if (environmentsToAdd.size() > 0) {
            gatewayManager.deployToGateway(product, tenantDomain, environmentsToAdd);
        }

    }

    @Override
    public void updateAPIProductDisplayOnDevportal(String apiProductId, String apiRevisionId,
                                                   APIRevisionDeployment apiRevisionDeployment) throws APIManagementException {

        APIProductIdentifier apiProductIdentifier = APIUtil.getAPIProductIdentifierFromUUID(apiProductId);
        if (apiProductIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Product with ID: "
                    + apiProductId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiProductId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        List<APIRevisionDeployment> currentApiRevisionDeploymentList =
                apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(apiProductId);
        Set<APIRevisionDeployment> environmentsToUpdate = new HashSet<>();
        for (APIRevisionDeployment currentapiRevisionDeployment : currentApiRevisionDeploymentList) {
            if (StringUtils.equalsIgnoreCase(currentapiRevisionDeployment.getDeployment(),
                    apiRevisionDeployment.getDeployment())) {
                environmentsToUpdate.add(apiRevisionDeployment);
            }
        }
        // if the provided deployment doesn't exist we are not adding to update list

        if (environmentsToUpdate.size() > 0) {
            apiMgtDAO.updateAPIRevisionDeployment(apiProductId, environmentsToUpdate);
        } else {
            throw new APIMgtResourceNotFoundException("deployment with " + apiRevisionDeployment.getDeployment() +
                    " not found", ExceptionCodes.from(ExceptionCodes.EXISTING_DEPLOYMENT_NOT_FOUND,
                    apiRevisionDeployment.getDeployment()));
        }
    }

    @Override
    public void undeployAPIProductRevisionDeployment(String apiProductId, String apiRevisionId,
                                                     List<APIRevisionDeployment> apiRevisionDeployments)
            throws APIManagementException {
        APIProductIdentifier apiProductIdentifier = APIUtil.getAPIProductIdentifierFromUUID(apiProductId);
        if (apiProductIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Product with ID: "
                    + apiProductId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiProductId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        APIProduct product = getAPIProductbyUUID(apiRevisionId, tenantDomain);
        product.setUuid(apiProductId);
        Set<String> environmentsToRemove = new HashSet<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeployments) {
            environmentsToRemove.add(apiRevisionDeployment.getDeployment());
        }
        product.setEnvironments(environmentsToRemove);
        removeFromGateway(product, tenantDomain, new HashSet<>(apiRevisionDeployments),Collections.emptySet());
        apiMgtDAO.removeAPIRevisionDeployment(apiRevisionId, apiRevisionDeployments);
        if (environmentsToRemove.size() > 0) {
            GatewayArtifactsMgtDAO.getInstance().removePublishedGatewayLabels(apiProductId, apiRevisionId,
                    environmentsToRemove);
        }
    }

    @Override
    public void restoreAPIProductRevision(String apiProductId, String apiRevisionId, String organization)
            throws APIManagementException {
        APIProductIdentifier apiProductIdentifier = APIUtil.getAPIProductIdentifierFromUUID(apiProductId);
        if (apiProductIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Product with ID: "
                    + apiProductId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiProductId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        apiProductIdentifier.setUuid(apiProductId);
        try {
            apiPersistenceInstance.restoreAPIRevision(new Organization(organization),
                    apiProductIdentifier.getUUID(), apiRevision.getRevisionUUID(), apiRevision.getId());
        } catch (APIPersistenceException e) {
            String errorMessage = "Failed to restore registry artifacts";
            throw new APIManagementException(errorMessage,ExceptionCodes.from(ExceptionCodes.
                    ERROR_RESTORING_API_REVISION,apiRevision.getApiUUID()));
        }
        apiMgtDAO.restoreAPIProductRevision(apiRevision);
    }

    @Override
    public void deleteAPIProductRevision(String apiProductId, String apiRevisionId, String organization)
            throws APIManagementException {
        APIProductIdentifier apiProductIdentifier = APIUtil.getAPIProductIdentifierFromUUID(apiProductId);
        if (apiProductIdentifier == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Product with ID: "
                    + apiProductId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiProductId));
        }
        APIRevision apiRevision = apiMgtDAO.getRevisionByRevisionUUID(apiRevisionId);
        if (apiRevision == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Revision with Revision UUID: "
                    + apiRevisionId, ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, apiRevisionId));
        }
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = getAPIRevisionDeploymentList(apiRevisionId);
        if (apiRevisionDeploymentsResponse.size() != 0) {
            String errorMessage = "Couldn't delete API revision since API revision is currently deployed to a gateway." +
                    "You need to undeploy the API Revision from the gateway before attempting deleting API Revision: "
                    + apiRevision.getRevisionUUID();
            throw new APIManagementException(errorMessage,ExceptionCodes.from(ExceptionCodes.
                    EXISTING_API_REVISION_DEPLOYMENT_FOUND, apiRevisionId));
        }
        apiProductIdentifier.setUuid(apiProductId);
        try {
            apiPersistenceInstance.deleteAPIRevision(new Organization(organization),
                    apiProductIdentifier.getUUID(), apiRevision.getRevisionUUID(), apiRevision.getId());
        } catch (APIPersistenceException e) {
            String errorMessage = "Failed to delete registry artifacts";
            throw new APIManagementException(errorMessage,ExceptionCodes.from(ExceptionCodes.
                    ERROR_DELETING_API_REVISION,apiRevision.getApiUUID()));
        }
        apiMgtDAO.deleteAPIProductRevision(apiRevision);
        gatewayArtifactsMgtDAO.deleteGatewayArtifact(apiRevision.getApiUUID(), apiRevision.getRevisionUUID());
        if (artifactSaver != null) {
            try {
                artifactSaver.removeArtifact(apiRevision.getApiUUID(), apiProductIdentifier.getName(),
                        apiProductIdentifier.getVersion(), apiRevision.getRevisionUUID(), tenantDomain);
            } catch (ArtifactSynchronizerException e) {
                log.error("Error while deleting Runtime artifacts from artifact Store", e);
            }
        }
    }

    @Override
    public String generateApiKey(String apiId, String organization) throws APIManagementException {
        APIInfo apiInfo = apiMgtDAO.getAPIInfoByUUID(apiId);
        if (apiInfo == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with ID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        SubscribedApiDTO subscribedApiInfo = new SubscribedApiDTO();
        subscribedApiInfo.setName(apiInfo.getName());
        subscribedApiInfo.setContext(apiInfo.getContext());
        subscribedApiInfo.setPublisher(apiInfo.getProvider());
        subscribedApiInfo.setVersion(apiInfo.getVersion());
        JwtTokenInfoDTO jwtTokenInfoDTO = new JwtTokenInfoDTO();
        jwtTokenInfoDTO.setEndUserName(username);
        jwtTokenInfoDTO.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
        if (!apiInfo.getApiType().equals(APIConstants.API_PRODUCT)) { // Check if this an actual API
            API api = getAPIbyUUID(apiId, organization);
            String endpointConfig = api.getEndpointConfig();
            log.debug("endpointConfig in generateApiKey - " + endpointConfig);
            if (!StringUtils.isEmpty(endpointConfig)) {
                try {
                    JSONObject endpointConfigJson = (JSONObject) new JSONParser().parse(endpointConfig);
                    if (endpointConfigJson.get(APIConstants.API_DATA_PRODUCTION_ENDPOINTS) == null
                            && endpointConfigJson.get(APIConstants.API_DATA_SANDBOX_ENDPOINTS) != null) {
                        // If no production endpoints available and a sandbox endpoint is available
                        jwtTokenInfoDTO.setKeyType(APIConstants.API_KEY_TYPE_SANDBOX);
                    }
                } catch (ParseException e) {
                    // Ignore since we set the default key type previously
                }
            }
        }
        jwtTokenInfoDTO.setSubscribedApiDTOList(Arrays.asList(subscribedApiInfo));
        jwtTokenInfoDTO.setExpirationTime(60000l);
        ApiKeyGenerator apiKeyGenerator = new InternalAPIKeyGenerator();
        return apiKeyGenerator.generateToken(jwtTokenInfoDTO);
    }

    @Override
    public List<APIRevisionDeployment> getAPIRevisionsDeploymentList(String apiId) throws APIManagementException {
        return apiMgtDAO.getAPIRevisionDeploymentByApiUUID(apiId);
    }

    @Override
    public void addEnvironmentSpecificAPIProperties(String apiUuid, String envUuid,
            EnvironmentPropertiesDTO environmentPropertyDTO) throws APIManagementException {
        String content = new Gson().toJson(environmentPropertyDTO);
        environmentSpecificAPIPropertyDAO.addOrUpdateEnvironmentSpecificAPIProperties(apiUuid, envUuid, content);
    }

    @Override
    public EnvironmentPropertiesDTO getEnvironmentSpecificAPIProperties(String apiUuid, String envUuid)
            throws APIManagementException {
        String content = environmentSpecificAPIPropertyDAO.getEnvironmentSpecificAPIProperties(apiUuid, envUuid);
        if (StringUtils.isBlank(content)) {
            return new EnvironmentPropertiesDTO();
        }
        return new Gson().fromJson(content, EnvironmentPropertiesDTO.class);
    }

    @Override
    public Environment getEnvironment(String organization, String uuid) throws APIManagementException {
        // priority for configured environments over dynamic environments
        // name is the UUID of environments configured in api-manager.xml
        Environment env = APIUtil.getReadOnlyEnvironments().get(uuid);
        if (env == null) {
            env = apiMgtDAO.getEnvironment(organization, uuid);
            if (env == null) {
                String errorMessage =
                        String.format("Failed to retrieve Environment with UUID %s. Environment not found", uuid);
                throw new APIMgtResourceNotFoundException(errorMessage, ExceptionCodes
                        .from(ExceptionCodes.GATEWAY_ENVIRONMENT_NOT_FOUND, String.format("UUID '%s'", uuid)));
            }
        }
        return env;
    }

    @Override
    public void setOperationPoliciesToURITemplates(String apiId, Set<URITemplate> uriTemplates)
            throws APIManagementException {

        Set<URITemplate> uriTemplatesWithPolicies = apiMgtDAO.getURITemplatesWithOperationPolicies(apiId);

        if (!uriTemplatesWithPolicies.isEmpty()) {
            //This is a temporary map to keep operation policies list of URI Templates against the URI mapping ID
            Map<String, List<OperationPolicy>> operationPoliciesMap = new HashMap<>();

            for (URITemplate uriTemplate : uriTemplatesWithPolicies) {
                String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
                List<OperationPolicy> operationPolicies = uriTemplate.getOperationPolicies();
                if (!operationPolicies.isEmpty()) {
                    operationPoliciesMap.put(key, operationPolicies);
                }
            }

            for (URITemplate uriTemplate : uriTemplates) {
                String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
                if (operationPoliciesMap.containsKey(key)) {
                    uriTemplate.setOperationPolicies(operationPoliciesMap.get(key));
                }
            }
        }
    }

    /**
     * This method will be used to import Operation policy. This will check existing API specific policy first and
     * then common policy.
     * If API specific policy exists and MD5 hash matches, it will not import and will return the existing API specific policy.
     * If the existing API specific policy is different in md5, it will be updated the existing policy
     * If a common policy exists and MD5 hash match, it will return the common policy's id. This policy will be imported at the API update.
     * If the common policy is different then the imported policy, a new API specific policy will be created.
     * If there aren't any existing policies, a new API specific policy will be created.
     *
     * @param importedPolicyData Imported policy
     * @param organization       Organization name
     * @return corrosponding policy ID for imported data
     * @throws APIManagementException if failed to delete APIRevision
     */
    @Override
    public String importOperationPolicy(OperationPolicyData importedPolicyData, String organization)
            throws APIManagementException {

        OperationPolicySpecification importedSpec = importedPolicyData.getSpecification();
        OperationPolicyData existingOperationPolicy =
                getAPISpecificOperationPolicyByPolicyName(importedSpec.getName(), importedSpec.getVersion(),
                        importedPolicyData.getApiUUID(), null, organization, false);
        String policyId = null;
        if (existingOperationPolicy != null) {
            if (existingOperationPolicy.getMd5Hash().equals(importedPolicyData.getMd5Hash())) {
                if (log.isDebugEnabled()) {
                    log.debug("Matching API specific policy found for imported policy and MD5 hashes match.");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Even though existing API specific policy name match with imported policy, "
                            + "the MD5 hashes does not match in the policy " + existingOperationPolicy.getPolicyId()
                            + ".Therefore updating the existing policy");
                }
                updateOperationPolicy(existingOperationPolicy.getPolicyId(), importedPolicyData, organization);
            }
            policyId = existingOperationPolicy.getPolicyId();
        } else {
            existingOperationPolicy = getCommonOperationPolicyByPolicyName(importedSpec.getName(),
                    importedSpec.getVersion(),organization, false);
            if (existingOperationPolicy != null) {
                if (existingOperationPolicy.getMd5Hash().equals(importedPolicyData.getMd5Hash())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matching common policy found for imported policy and Md5 hashes match.");
                    }
                    policyId = existingOperationPolicy.getPolicyId();
                } else {
                    importedSpec.setName(importedSpec.getName() + "_imported");
                    importedSpec.setDisplayName(importedSpec.getDisplayName() + " Imported");
                    importedPolicyData.setSpecification(importedSpec);
                    importedPolicyData.setMd5Hash(APIUtil.getMd5OfOperationPolicy(importedPolicyData));
                    policyId = addAPISpecificOperationPolicy(importedPolicyData.getApiUUID(), importedPolicyData,
                            organization);
                    if (log.isDebugEnabled()) {
                        log.debug("Even though existing common policy name match with imported policy, "
                                + "the MD5 hashes does not match in the policy " + existingOperationPolicy.getPolicyId()
                                + ". A new policy created with ID " + policyId);
                    }
                }
            } else {
                policyId = addAPISpecificOperationPolicy(importedPolicyData.getApiUUID(), importedPolicyData,
                        organization);
                if (log.isDebugEnabled()) {
                    log.debug(
                            "There aren't any existing policies for the imported policy. A new policy created with ID "
                                    + policyId);
                }
            }
        }

        return policyId;
    }

    @Override
    public String addAPISpecificOperationPolicy(String apiUUID, OperationPolicyData operationPolicyData,
                                                String tenantDomain)
            throws APIManagementException {

        return apiMgtDAO.addAPISpecificOperationPolicy(apiUUID, null, operationPolicyData);
    }

    @Override
    public String addCommonOperationPolicy(OperationPolicyData operationPolicyData, String tenantDomain)
            throws APIManagementException {

        return apiMgtDAO.addCommonOperationPolicy(operationPolicyData);
    }

    @Override
    public OperationPolicyData getAPISpecificOperationPolicyByPolicyName(String policyName, String policyVersion,
                                                                         String apiUUID, String revisionUUID,
                                                                         String tenantDomain,
                                                                         boolean isWithPolicyDefinition)
            throws APIManagementException {

        return apiMgtDAO.getAPISpecificOperationPolicyByPolicyName(policyName, policyVersion, apiUUID, revisionUUID,
                tenantDomain, isWithPolicyDefinition);
    }

    @Override
    public OperationPolicyData getCommonOperationPolicyByPolicyName(String policyName, String policyVersion,
                                                                    String tenantDomain,
                                                                    boolean isWithPolicyDefinition)
            throws APIManagementException {

        return apiMgtDAO.getCommonOperationPolicyByPolicyName(policyName, policyVersion, tenantDomain, isWithPolicyDefinition);
    }

    @Override
    public OperationPolicyData getAPISpecificOperationPolicyByPolicyId(String policyId, String apiUUID,
                                                                       String organization,
                                                                       boolean isWithPolicyDefinition)
            throws APIManagementException {

        return apiMgtDAO
                .getAPISpecificOperationPolicyByPolicyID(policyId, apiUUID, organization, isWithPolicyDefinition);
    }

    @Override
    public OperationPolicyData getCommonOperationPolicyByPolicyId(String policyId, String organization,
                                                                  boolean isWithPolicyDefinition)
            throws APIManagementException {

        return apiMgtDAO.getCommonOperationPolicyByPolicyID(policyId, organization, isWithPolicyDefinition);
    }

    @Override
    public void updateOperationPolicy(String operationPolicyId, OperationPolicyData operationPolicyData,
                                      String tenantDomain) throws APIManagementException {

        apiMgtDAO.updateOperationPolicy(operationPolicyId, operationPolicyData);
    }

    @Override
    public List<OperationPolicyData> getAllCommonOperationPolicies(String tenantDomain)
            throws APIManagementException {

        return apiMgtDAO.getLightWeightVersionOfAllOperationPolicies(null, tenantDomain);
    }

    @Override
    public List<OperationPolicyData> getAllAPISpecificOperationPolicies(String apiUUID, String tenantDomain)
            throws APIManagementException {

        return apiMgtDAO.getLightWeightVersionOfAllOperationPolicies(apiUUID, tenantDomain);
    }

    @Override
    public void deleteOperationPolicyById(String policyId, String tenantDomain) throws APIManagementException {

        apiMgtDAO.deleteOperationPolicyByPolicyId(policyId);
    }

    private static Map<String, List<OperationPolicy>> extractAndDropOperationPoliciesFromURITemplate
            (Set<URITemplate> uriTemplates) {
        Map<String, List<OperationPolicy>> operationPoliciesMap = new HashMap<>();
        for (URITemplate uriTemplate : uriTemplates) {
            String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
            List<OperationPolicy> operationPolicies = uriTemplate.getOperationPolicies();
            if (!operationPolicies.isEmpty()) {
                operationPoliciesMap.put(key, operationPolicies);
            }
            uriTemplate.setOperationPolicies(null);
        }
        return operationPoliciesMap;
    }

    public  APIRevision checkAPIUUIDIsARevisionUUID(String apiUUID) throws APIManagementException {
        return apiMgtDAO.checkAPIUUIDIsARevisionUUID(apiUUID);
    }

    @Override
    public ApiTypeWrapper getAPIorAPIProductByUUID(String uuid, String requestedTenantDomain) throws APIManagementException {
        APIInfo apiInfo = apiMgtDAO.getAPIInfoByUUID(uuid);
        if (apiInfo != null) {
            if (apiInfo.getOrganization().equals(requestedTenantDomain)) {
                if (APIConstants.API_PRODUCT.equals(apiInfo.getApiType())) {
                    return new ApiTypeWrapper(getAPIProductbyUUID(uuid, requestedTenantDomain));
                } else {
                    return new ApiTypeWrapper(getAPIbyUUID(uuid, requestedTenantDomain));
                }
            } else {
                throw new APIManagementException(
                        "User " + username + " does not have permission to view API Product : " + uuid);
            }
        } else {
            String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
            throw new APIMgtResourceNotFoundException(msg);
        }
    }

    @Override
    public boolean isValidContext(String providerName, String apiName, String contextTemplate, String userName,
                                  String organization) throws APIManagementException {
        if (isApiNameExist(apiName, organization)) {
            providerName = (StringUtils.isBlank(providerName)) ? userName : providerName;
            providerName = APIUtil.replaceEmailDomainBack(providerName);
            if (!contextTemplate.startsWith("/")) {
                contextTemplate = "/" + contextTemplate;
            }
            List<String> versions = getApiVersionsMatchingApiNameAndOrganization(apiName, providerName, organization);
            for (String version : versions) {
                APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
                String apiUUID = apiMgtDAO.getUUIDFromIdentifier(apiIdentifier, organization);
                String currentContextTemplate = getAPIbyUUID(apiUUID, organization).getContextTemplate();
                if (currentContextTemplate.equals(contextTemplate)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private void validateAndUpdateURITemplates(API api, int tenantId) throws APIManagementException {
        if (api.getUriTemplates() != null) {
            for (URITemplate uriTemplate : api.getUriTemplates()) {
                if (StringUtils.isEmpty(api.getApiLevelPolicy())) {
                    // API level policy not attached.
                    if (StringUtils.isEmpty(uriTemplate.getThrottlingTier())) {
                        uriTemplate.setThrottlingTier(APIUtil.getDefaultAPILevelPolicy(tenantId));
                    }
                } else {
                    uriTemplate.setThrottlingTier(api.getApiLevelPolicy());
                }
            }
        }
    }
}
