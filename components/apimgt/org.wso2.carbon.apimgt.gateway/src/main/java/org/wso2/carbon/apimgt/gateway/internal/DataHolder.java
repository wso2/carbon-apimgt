/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.internal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.api.model.LLMProviderInfo;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants.GatewayNotification.GatewayRegistrationResponse;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataLoader;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class DataHolder {
    private static final Log log  = LogFactory.getLog(DataHolder.class);
    private static final DataHolder Instance = new DataHolder();
    private Map<String, List<String>> apiToCertificatesMap = new HashMap<>();
    private Map<String, String> googleAnalyticsConfigMap = new HashMap<>();
    private Map<String, GraphQLSchemaDTO> apiToGraphQLSchemaDTOMap = new HashMap<>();
    private Map<String, List<String>> apiToKeyManagersMap = new HashMap<>();
    private Map<String,Map<String, API>> tenantAPIMap  = new HashMap<>();
    private Map<String, Boolean> tenantDeployStatus = new HashMap<>();
    private Map<String, LLMProviderInfo> llmProviderMap = new HashMap<>();
    private Map<String, APIKeyInfo> apiKeyInfoHashMap = new HashMap<>();
    private final Map<String, Cache<String, Long>> apiSuspendedEndpoints = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AbstractAPIMgtGatewayJWTGenerator> jwtGeneratorTenantMap =
            new ConcurrentHashMap<>();

    private boolean isAllGatewayPoliciesDeployed = false;
    private boolean tenantsProvisioned = false;
    private static GatewayRegistrationResponse gatewayRegistrationResponse = GatewayRegistrationResponse.NOT_RESPONDED;
    private String gatewayID;

    private DataHolder() {
    }

    public boolean isTenantsProvisioned() {
        return tenantsProvisioned;
    }

    public void setTenantsProvisioned(boolean tenantsProvisioned) {
        boolean oldTenantsProvisioned = this.tenantsProvisioned;
        this.tenantsProvisioned = tenantsProvisioned;
        if (tenantsProvisioned && !oldTenantsProvisioned) {
            initializeTenantDeploymentStatusMap();
        }
    }

    public Map<String, List<String>> getApiToCertificatesMap() {

        return apiToCertificatesMap;
    }

    /**
     * Retrieves LLM Provider configurations by ID.
     *
     * @param id the ID of the LLM provider
     * @return the LLMProvider if found, otherwise null
     */
    public LLMProviderInfo getLLMProviderConfigurations(String id) {

        if (llmProviderMap.containsKey(id)) {
            return llmProviderMap.get(id);
        } else {
            log.warn("LLM Provider key " + id + " not found");
            return null;
        }
    }

    /**
     * Adds a new LLM Provider configuration.
     *
     * @param provider the LLMProvider to add
     */
    public void addLLMProviderConfigurations(LLMProviderInfo provider) {

        llmProviderMap.put(provider.getId(), provider);
    }

    /**
     * Removes an LLM Provider configuration by ID.
     *
     * @param id the ID of the LLM provider to remove
     */
    public void removeLLMProviderConfigurations(String id) {

        if (StringUtils.isEmpty(id)) {
            return;
        }
        llmProviderMap.remove(id);
    }

    /**
     * Updates an existing LLM Provider configuration.
     *
     * @param provider the LLMProvider to update
     */
    public void updateLLMProviderConfigurations(LLMProviderInfo provider) {

        this.removeLLMProviderConfigurations(provider.getId());
        this.addLLMProviderConfigurations(provider);
    }

    public void setApiToCertificatesMap(Map<String, List<String>> apiToCertificatesMap) {

        this.apiToCertificatesMap = apiToCertificatesMap;
    }

    public static DataHolder getInstance() {

        return Instance;
    }

    /**
     * Adds a new opaque api key info.
     *
     * @param apiKeyInfo the api key info to add
     */
    public void addOpaqueAPIKeyInfo(APIKeyInfo apiKeyInfo) {

        apiKeyInfoHashMap.put(apiKeyInfo.getLookupKey(), apiKeyInfo);
    }

    /**
     * Returns opaque api key info for the given lookup key
     *
     */
    public APIKeyInfo getOpaqueAPIKeyInfo(String lookupKey) {

        return apiKeyInfoHashMap.get(lookupKey);
    }

    /**
     * Removes an opaque api key info.
     *
     * @param lookupKey the reference for the api key hash to remove
     */
    public void removeOpaqueAPIKeyInfo(String lookupKey) {

        apiKeyInfoHashMap.remove(lookupKey);
    }

    public void addApiToAliasList(String apiId, List<String> aliasList) {

        apiToCertificatesMap.put(apiId, aliasList);
    }

    public List<String> getCertificateAliasListForAPI(String apiId) {

        return apiToCertificatesMap.getOrDefault(apiId, Collections.emptyList());
    }

    public void addGoogleAnalyticsConfig(String tenantDomain, String config) {

        googleAnalyticsConfigMap.put(tenantDomain, config);
    }

    public void removeGoogleAnalyticsConfig(String tenantDomain) {

        googleAnalyticsConfigMap.remove(tenantDomain);
    }

    public String getGoogleAnalyticsConfig(String tenantDomain) {

        return googleAnalyticsConfigMap.get(tenantDomain);
    }

    public Map<String, GraphQLSchemaDTO> getApiToGraphQLSchemaDTOMap() {

        return apiToGraphQLSchemaDTOMap;
    }

    public GraphQLSchemaDTO getGraphQLSchemaDTOForAPI(String apiId) {

        return apiToGraphQLSchemaDTOMap.get(apiId);
    }

    public void addApiToGraphQLSchemaDTO(String apiId, GraphQLSchemaDTO graphQLSchemaDTO) {

        apiToGraphQLSchemaDTOMap.put(apiId, graphQLSchemaDTO);
    }

    public boolean isAllApisDeployed() {
        for (Boolean b : tenantDeployStatus.values()) {
            if (!b.booleanValue()) {
                return false;
            }
        }
        return true;
    }

    public Map<String, Boolean> getTenantDeployStatus() {
        return tenantDeployStatus;
    }

    public void setTenantDeployStatus(String tenant) {
        tenantDeployStatus.put(tenant, true);
    }

    public void addKeyManagerToAPIMapping(String uuid, List<String> keyManagers) {

        apiToKeyManagersMap.put(uuid, keyManagers);
    }
    public void removeKeyManagerToAPIMapping(String uuid) {

        apiToKeyManagersMap.remove(uuid);
    }
    public List<String> getKeyManagersFromUUID(String apiUUID) {

        return apiToKeyManagersMap.get(apiUUID);
    }

    public boolean isAllGatewayPoliciesDeployed() {
        return isAllGatewayPoliciesDeployed;
    }

    public void setAllGatewayPoliciesDeployed(boolean allGatewayPoliciesDeployed) {
        isAllGatewayPoliciesDeployed = allGatewayPoliciesDeployed;
    }

    public void addAPIMetaData(API api) {
        if (log.isDebugEnabled()) {
            log.debug("Adding meta data of API : " + api.getApiName());
        }
        String context = api.getContext();
        String defaultContext = context;
        int index = context.lastIndexOf("/" + api.getApiVersion());
        if (index != -1) {
            defaultContext = context.substring(0, index);
        }
        Map<String, API> apiMap;
        if (tenantAPIMap.containsKey(api.getOrganization())) {
            apiMap = tenantAPIMap.get(api.getOrganization());
        } else {
            apiMap = new HashMap<>();
        }
        API oldAPI = apiMap.get(api.getContext());
        if (oldAPI != null) {
            apiMap.remove(api.getContext());
            if (oldAPI.isDefaultVersion()) {
                apiMap.remove(defaultContext);
            }
        }
        apiMap.put(api.getContext(), api);
        if (api.isDefaultVersion()) {
            apiMap.put(defaultContext, api);
        }
        tenantAPIMap.put(api.getOrganization(), apiMap);
    }

    public void markAPIAsDeployed(GatewayAPIDTO gatewayAPIDTO) {
        Map<String, API> apiMap = tenantAPIMap.get(gatewayAPIDTO.getTenantDomain());
        if (apiMap != null) {
            API api = apiMap.get(gatewayAPIDTO.getApiContext());
            if (api != null) {
                api.setDeployed(true);
                if (log.isDebugEnabled()) {
                    log.debug("API : " + api.getApiName() + "is deployed successfully");
                }
                api.setRevisionId(gatewayAPIDTO.getRevision());
            }
        }
    }

    public boolean isDuplicateEvent(String tenantDomain, String apiContext, String lastUpdatedEventId) {
        Map<String, API> apiMap = tenantAPIMap.get(tenantDomain);
        if (apiMap != null) {
            API api = apiMap.get(apiContext);
            if (api != null) {
                if (lastUpdatedEventId != null && api.getLastUpdatedEventId() != null) {
                    return api.getLastUpdatedEventId().equalsIgnoreCase(lastUpdatedEventId);
                }
            }
        }
        return false;
    }

    public void updateLastUpdatedEventId(GatewayAPIDTO gatewayAPIDTO, String lastUpdatedEventId) {
        Map<String, API> apiMap = tenantAPIMap.get(gatewayAPIDTO.getTenantDomain());
        if (apiMap != null) {
            API api = apiMap.get(gatewayAPIDTO.getApiContext());
            if (api != null) {
                if (lastUpdatedEventId != null) {
                    api.setLastUpdatedEventId(lastUpdatedEventId);
                }
            }
        }
    }

    /**
     * Populate vhosts information to API object
     *
     * @param gatewayAPIDTO gateway API DTO containing vhosts and other info
     */
    public void populateVhosts(GatewayAPIDTO gatewayAPIDTO) {
        Map<String, API> apiMap = tenantAPIMap.get(gatewayAPIDTO.getTenantDomain());
        if (apiMap != null) {
            API api = apiMap.get(gatewayAPIDTO.getApiContext());
            if (api != null) {
                List<VHost> vhosts = gatewayAPIDTO.getVhosts();
                api.setVhosts(vhosts != null ? vhosts : new ArrayList<>());
                if (log.isDebugEnabled()) {
                    log.debug("Populated vhosts info for API : " + api.getApiName());
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("API not found for context " + gatewayAPIDTO.getApiContext() + " in tenant domain "
                            + gatewayAPIDTO.getTenantDomain());
                }
            }
        }
    }

    public Map<String, Map<String, API>> getTenantAPIMap() {
        return tenantAPIMap;
    }

    public void removeAPIFromAllTenantMap(String apiContext, String tenantDomain) {
        Map<String, API> apiMap = tenantAPIMap.get(tenantDomain);
        if (apiMap != null) {
            API api = apiMap.get(apiContext);
            if (api != null) {
                apiMap.remove(apiContext);
                if (api.isDefaultVersion()) {
                    if (api.isDefaultVersion()) {
                        String context = api.getContext();
                        int index = context.lastIndexOf("/" + api.getApiVersion());
                        apiMap.remove(context.substring(0, index));
                    }

                }
            }
        }
    }

    public void addAPIMetaData(DeployAPIInGatewayEvent gatewayEvent) {
        SubscriptionDataLoader subscriptionDataLoader = new SubscriptionDataLoaderImpl();
        try {
            API api = subscriptionDataLoader.getApi(gatewayEvent.getContext(), gatewayEvent.getVersion());
            if (api != null){
                addAPIMetaData(api);
            }
        } catch (DataLoadingException e) {
            log.error("Error while loading API Metadata", e);
        }
    }

    public void addAPIMetaData(APIEvent event) {
        SubscriptionDataLoader subscriptionDataLoader = new SubscriptionDataLoaderImpl();
        try {
            API api = subscriptionDataLoader.getApi(event.getApiContext(), event.getApiVersion());
            if (api != null){
                addAPIMetaData(api);
            }
        } catch (DataLoadingException e) {
            log.error("Error while loading API Metadata", e);
        }
    }

    public void markApisAsUnDeployedInTenant(String tenantDomain) {
        if (tenantAPIMap.containsKey(tenantDomain)) {
            Map<String, API> apiMap = tenantAPIMap.get(tenantDomain);
            apiMap.values().forEach(api -> api.setDeployed(false));
        }
    }

    private void initializeTenantDeploymentStatusMap() {
        try {
            Set<String> tenants = GatewayUtils.getTenantsToBeDeployed();
            for (String str : tenants) {
                tenantDeployStatus.putIfAbsent(str, false);
            }
        } catch (APIManagementException e) {
            log.error("Error while initializing tenant deployment status map", e);
        }
    }

    /**
     * Initializes a cache for a specific API key if not already present.
     *
     * @param apiKey The key representing the API and tenant domain.
     */
    public synchronized void initCache(String apiKey) {
        apiSuspendedEndpoints.putIfAbsent(apiKey, CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build());
    }

    /**
     * Retrieves the cache for a given endpoint key if it exists.
     *
     * @param apiKey The key representing the API and tenant domain.
     * @return The cache associated with the specified endpoint key, or {@code null} if not initialized.
     */
    public Cache<String, Long> getCache(String apiKey) {
        return apiSuspendedEndpoints.get(apiKey);
    }

    /**
     * Suspends an endpoint for a specific API with a given expiry time.
     *
     * @param apiKey       The key representing the API and tenant domain.
     * @param endpointId   The identifier of the endpoint.
     * @param expiryMillis The suspension duration in milliseconds.
     */
    public void suspendEndpoint(String apiKey, String endpointId, long expiryMillis) {

        Cache<String, Long> cache = getCache(apiKey);
        if (cache != null) {
            cache.put(endpointId, System.currentTimeMillis() + expiryMillis);
        }
    }

    /**
     * Checks if an endpoint is currently suspended for a given API.
     *
     * @param apiKey     The key representing the API and tenant domain.
     * @param endpointId The identifier of the endpoint.
     * @return {@code true} if the endpoint is suspended and has not expired, otherwise {@code false}.
     */
    public boolean isEndpointSuspended(String apiKey, String endpointId) {

        Cache<String, Long> cache = getCache(apiKey);
        if (cache == null) {
            return false;
        }

        Long expirationTime = cache.getIfPresent(endpointId);
        if (expirationTime == null || System.currentTimeMillis() > expirationTime) {
            cache.invalidate(endpointId);
            return false;
        }
        return true;
    }

    /**
     * Removes an endpoint from the suspended list for a specific API.
     *
     * @param apiKey     The key representing the API and tenant domain.
     * @param endpointId The identifier of the endpoint.
     */
    public void removeSuspendedEndpoint(String apiKey, String endpointId) {
        Cache<String, Long> cache = getCache(apiKey);
        if (cache != null) {
            cache.invalidate(endpointId);
        }
    }

    /**
     * Releases an API's cache and removes it if no active references exist.
     *
     * @param apiKey The key representing the API and tenant domain.
     */
    public synchronized void releaseCache(String apiKey) {

        apiSuspendedEndpoints.remove(apiKey);
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public GatewayRegistrationResponse getGatewayRegistrationResponse() {
        return gatewayRegistrationResponse;
    }

    /**
     * Checks if the gateway is registered or acknowledged.
     *
     * @return true if the gateway registration response is REGISTERED or ACKNOWLEDGED, false otherwise
     */
    public boolean isGatewayRegistered() {
        return gatewayRegistrationResponse == GatewayRegistrationResponse.REGISTERED
                || gatewayRegistrationResponse == GatewayRegistrationResponse.ACKNOWLEDGED;
    }

    public static void setGatewayRegistrationResponse(GatewayRegistrationResponse gatewayRegistrationResponse) {
        DataHolder.gatewayRegistrationResponse = gatewayRegistrationResponse;
    }

    /**
     * Update API properties, revision ID, and deployment status in subscription data store
     *
     * @param gatewayAPIDTO Gateway API DTO containing additional properties and other info
     */
    public void updateAPIPropertiesFromGatewayDTO(GatewayAPIDTO gatewayAPIDTO) {
        Map<String, API> apiMap = tenantAPIMap.get(gatewayAPIDTO.getTenantDomain());
        if (apiMap != null) {
            API api = apiMap.get(gatewayAPIDTO.getApiContext());
            if (api != null) {
                api.setApiProperties(gatewayAPIDTO.getAdditionalProperties());
                if (log.isDebugEnabled()) {
                    log.debug("Updated API properties for API: " + api.getName() + " (Context: " + api.getContext() +
                            ")");
                }
            }
        }
    }

    /**
     * Returns the map of JWT generators per tenant domain.
     *
     * @return ConcurrentMap where the key is the tenant domain and the value is the corresponding
     * AbstractAPIMgtGatewayJWTGenerator instance.
     */
    public ConcurrentMap<String, AbstractAPIMgtGatewayJWTGenerator> getJwtGeneratorTenantMap() {

        return jwtGeneratorTenantMap;
    }
}
