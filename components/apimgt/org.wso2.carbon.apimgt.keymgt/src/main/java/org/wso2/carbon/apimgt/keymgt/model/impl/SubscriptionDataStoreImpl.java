/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.keymgt.model.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.model.subscription.CacheableEntity;
import org.wso2.carbon.apimgt.common.gateway.constants.JWTConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheInvalidationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMappingCacheKey;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.util.SubscriptionDataStoreUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SubscriptionDataStoreImpl implements SubscriptionDataStore {

    public static final String DELEM_PERIOD = ":";
    public static final int LOADING_POOL_SIZE = 7;
    private static final Log log = LogFactory.getLog(SubscriptionDataStoreImpl.class);
    private final EventHubConfigurationDto eventHubConfiguration;
    private boolean scopesInitialized;
    // Maps for keeping Subscription related details.
    private Map<ApplicationKeyMappingCacheKey, ApplicationKeyMapping> applicationKeyMappingMap;
    private Map<Integer, Application> applicationMap;
    private Map<String, API> apiMap;
    private Map<String, API> apiNameVersionMap;
    private Map<String, API> apiByUUIDMap;
    private Map<String, ApiPolicy> apiPolicyMap;
    private Map<String, SubscriptionPolicy> subscriptionPolicyMap;
    private Map<String, ApplicationPolicy> appPolicyMap;
    private Map<String, Subscription> subscriptionMap;
    private Map<String, Scope> scopesMap;
    private boolean apisInitialized;
    private boolean apiPoliciesInitialized;
    private String tenantDomain;

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(LOADING_POOL_SIZE,
            new ThreadFactory() {
                final AtomicInteger threadNumber = new AtomicInteger(1);
                final ThreadGroup group = (System.getSecurityManager() != null) ?
                        System.getSecurityManager().getThreadGroup() :
                        Thread.currentThread().getThreadGroup();
                final String namePrefix = "SubscriptionDataStore-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";

                @Override
                public Thread newThread(Runnable r) {

                    return new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
                }
            });

    private final ExecutorService subscriptionExecutorService = Executors.newFixedThreadPool(10,
            new InternalSubscriptionThreadFactory());

    public SubscriptionDataStoreImpl(String tenantDomain) {

        this.eventHubConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getEventHubConfigurationDto();
        this.tenantDomain = tenantDomain;
        initializeStore();
    }

    private void initializeStore() {

        this.applicationKeyMappingMap = new ConcurrentHashMap<>();
        this.applicationMap = new ConcurrentHashMap<>();
        this.apiMap = new ConcurrentHashMap<>();
        this.apiByUUIDMap = new ConcurrentHashMap<>();
        this.subscriptionPolicyMap = new ConcurrentHashMap<>();
        this.appPolicyMap = new ConcurrentHashMap<>();
        this.apiPolicyMap = new ConcurrentHashMap<>();
        this.subscriptionMap = new ConcurrentHashMap<>();
        this.scopesMap = new ConcurrentHashMap<>();
        this.apiNameVersionMap = new ConcurrentHashMap<>();
    }

    @Override
    public void init() {
        initializeLoadingTasks();
    }

    @Override
    public Application getApplicationById(int appId, boolean validationDisabled) {
        Application application;
        if (validationDisabled) {
            try {
                application = getApplicationById(appId);
            } catch (Exception e) {
                application = null;
            }
        } else {
            return getApplicationById(appId);
        }
        return application;
    }
    @Override
    public Application getApplicationById(int appId) {

        String synchronizeKey = "SubscriptionDataStoreImpl-Application-" + appId;
        Application application = applicationMap.get(appId);
        if (application == null) {
            synchronized (synchronizeKey.intern()) {
                application = applicationMap.get(appId);
                if (application != null) {
                    return application;
                }
            }
            try {
                application = new SubscriptionDataLoaderImpl().getApplicationById(appId);
            } catch (DataLoadingException e) {
                log.error("Error while Retrieving Application Metadata From Internal API.", e);
            }
            if (application != null && application.getId() != null && application.getId() != 0) {
                // load to the memory
                log.debug("Loading Application to the in-memory datastore. applicationId = " + application.getId());
                addOrUpdateApplication(application);
            } else {
                log.debug("Application not found. applicationId = " + appId);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Retrieving Application information with Application Id : " + appId);
            if (application != null) {
                log.debug("Retrieved Application :" + application.toString());
            } else {
                log.debug("Retrieved Application information with Application Id : " + appId + " is empty");
            }
        }
        return application;
    }

    @Override
    public ApplicationKeyMapping getKeyMappingByKeyAndKeyManager(String key, String keyManager,
                                                                 boolean validationDisabled) {

        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        boolean disableRetrieveKeyMappings =
                Boolean.parseBoolean(config.getFirstProperty(APIConstants.DISABLE_RETRIEVE_KEY_MAPPING));
        ApplicationKeyMappingCacheKey applicationKeyMappingCacheKey = new ApplicationKeyMappingCacheKey(key,
                keyManager);
        String synchronizeKey = "SubscriptionDataStoreImpl-KeyMapping-" + applicationKeyMappingCacheKey;

        ApplicationKeyMapping applicationKeyMapping = applicationKeyMappingMap.get(applicationKeyMappingCacheKey);
        if (applicationKeyMapping == null) {
            synchronized (synchronizeKey.intern()) {
                applicationKeyMapping = applicationKeyMappingMap.get(applicationKeyMappingCacheKey);
                if (applicationKeyMapping != null) {
                    return applicationKeyMapping;
                }
                try {
                    if (!validationDisabled || !disableRetrieveKeyMappings) {
                        if (log.isDebugEnabled()) {
                            log.debug("Attempting to load key mapping from internal API");
                        }
                        applicationKeyMapping = new SubscriptionDataLoaderImpl()
                                .getKeyMapping(key, keyManager, tenantDomain);
                    }
                } catch (DataLoadingException e) {
                    log.error("Error while Loading KeyMapping Information from Internal API.", e);
                }
                if (applicationKeyMapping != null && !StringUtils.isEmpty(applicationKeyMapping.getConsumerKey())) {
                    // load to the memory
                    log.debug("Loading Keymapping to the in-memory datastore.");
                    addOrUpdateApplicationKeyMapping(applicationKeyMapping);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Retrieving Application information with Consumer Key : " + key + " and keymanager : " + keyManager);
            if (applicationKeyMapping != null) {
                log.debug("Retrieved Application information with Consumer Key : " + key + " and keymanager : " + keyManager + " is " + applicationKeyMapping.toString());
            } else {
                log.debug("Retrieving Application information with Consumer Key : " + key + " and keymanager : " + keyManager + " is empty");
            }
        }
        return applicationKeyMapping;
    }

    @Override
    public ApplicationKeyMapping getKeyMappingByKeyAndKeyManager(String key, String keyManager) {

        ApplicationKeyMappingCacheKey applicationKeyMappingCacheKey = new ApplicationKeyMappingCacheKey(key,
                keyManager);
        String synchronizeKey = "SubscriptionDataStoreImpl-KeyMapping-" + applicationKeyMappingCacheKey;

        ApplicationKeyMapping applicationKeyMapping = applicationKeyMappingMap.get(applicationKeyMappingCacheKey);
        if (applicationKeyMapping == null) {
            synchronized (synchronizeKey.intern()) {
                applicationKeyMapping = applicationKeyMappingMap.get(applicationKeyMappingCacheKey);
                if (applicationKeyMapping != null) {
                    return applicationKeyMapping;
                }
                try {
                    applicationKeyMapping = new SubscriptionDataLoaderImpl()
                            .getKeyMapping(key, keyManager, tenantDomain);
                } catch (DataLoadingException e) {
                    log.error("Error while Loading KeyMapping Information from Internal API.", e);
                }
                if (applicationKeyMapping != null && !StringUtils.isEmpty(applicationKeyMapping.getConsumerKey())) {
                    // load to the memory
                    log.debug("Loading Keymapping to the in-memory datastore.");
                    addOrUpdateApplicationKeyMapping(applicationKeyMapping);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Retrieving Application information with Consumer Key : " + key + " and keymanager : " + keyManager);
            if (applicationKeyMapping != null) {
                log.debug("Retrieved Application information with Consumer Key : " + key + " and keymanager : " + keyManager + " is " + applicationKeyMapping.toString());
            } else {
                log.debug("Retrieving Application information with Consumer Key : " + key + " and keymanager : " + keyManager + " is empty");
            }
        }
        return applicationKeyMapping;
    }

    @Override
    public API getApiByContextAndVersion(String context, String version) {

        if (context == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve API information with null context");
            }
            return null;
        }
        if (JWTConstants.GATEWAY_JWKS_API_CONTEXT.equals(context) && StringUtils.isEmpty(version)
                && ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getJwtConfigurationDto().isJWKSApiEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve API information for JWKS API");
            }
            return null;
        }
        String key = context + DELEM_PERIOD + version;
        String synchronizeKey = "SubscriptionDataStoreImpl-API-" + key;
        API api = apiMap.get(key);
        if (api == null) {
            synchronized (synchronizeKey.intern()) {
                api = apiMap.get(key);
                if (api != null) {
                    return api;
                }
                try {
                    api = new SubscriptionDataLoaderImpl().getApi(context, version);
                } catch (DataLoadingException e) {
                    log.error("Error while Retrieving Data From Internal Rest API", e);
                }
                if (api != null && api.getApiId() != 0) {
                    // load to the memory
                    log.debug("Loading API to the in-memory datastore.");
                    addOrUpdateAPI(api);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API information with Context " + context + " and Version : " + version);
            if (api != null) {
                log.debug("Retrieved API information with Context  : " + context + " and Version : " + version + " is" +
                        " " + api.toString());
            } else {
                log.debug("Retrieved API information with Context  : " + context + " and Version : " + version + " is" +
                        " empty");
            }
        }
        return api;
    }

    @Override
    public API getApiByNameAndVersion(String name, String version) {

        String key = name + DELEM_PERIOD + version;
        return apiNameVersionMap.get(key);
    }

    @Override
    public API getAPIByUUID(String apiUUID) {

        return apiByUUIDMap.get(apiUUID);
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByName(String policyName, int tenantId) {

        String key = POLICY_TYPE.SUBSCRIPTION +
                SubscriptionDataStoreUtil.getPolicyCacheKey(policyName, tenantId);
        return subscriptionPolicyMap.get(key);
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByName(String policyName, int tenantId) {

        String key = POLICY_TYPE.APPLICATION + DELEM_PERIOD +
                SubscriptionDataStoreUtil.getPolicyCacheKey(policyName, tenantId);
        return appPolicyMap.get(key);
    }

    @Override
    public Subscription getSubscriptionById(int appId, int apiId, boolean validationDisabled) {
        Subscription subscription;
        if (validationDisabled) {
            try {
                subscription = getSubscriptionById(appId, apiId);
            } catch (Exception e) {
                subscription = null;
            }
        } else {
            return getSubscriptionById(appId, apiId);
        }
        return subscription;
    }
    @Override
    public Subscription getSubscriptionById(int appId, int apiId) {

        String subscriptionCacheKey = SubscriptionDataStoreUtil.getSubscriptionCacheKey(appId, apiId);
        String synchronizeKey = "SubscriptionDataStoreImpl-Subscription-" + subscriptionCacheKey;
        Subscription subscription = subscriptionMap.get(subscriptionCacheKey);
        if (subscription == null) {
            synchronized (synchronizeKey.intern()) {
                subscription = subscriptionMap.get(subscriptionCacheKey);
                if (subscription != null) {
                    return subscription;
                }
                try {
                    subscription = new SubscriptionDataLoaderImpl().getSubscriptionById(Integer.toString(apiId),
                            Integer.toString(appId));
                } catch (DataLoadingException e) {
                    log.error("Error while Retrieving Subscription Data From Internal API", e);
                }
                if (subscription != null && !StringUtils.isEmpty(subscription.getSubscriptionId())) {
                    // load to the memory
                    log.debug("Loading Subscription to the in-memory datastore.");
                    subscriptionMap.put(subscription.getCacheKey(), subscription);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API Subscription with Application " + appId + " and APIId : " + apiId);
            if (subscription != null) {
                log.debug("Retrieved API Subscription with Application " + appId + " and APIId : " + apiId + " is " + subscription.toString());
            } else {
                log.debug("Retrieved API Subscription with Application " + appId + " and APIId : " + apiId + " is " +
                        "empty.");
            }
        }

        return subscription;
    }

    @Override
    public ApiPolicy getApiPolicyByName(String policyName, int tenantId) {

        String key = POLICY_TYPE.API + DELEM_PERIOD +
                SubscriptionDataStoreUtil.getPolicyCacheKey(policyName, tenantId);
        return apiPolicyMap.get(key);
    }

    public void initializeLoadingTasks() {

        Runnable apiTask = new PopulateTask<>(apiMap,
                () -> {
                    try {
                        log.debug("Calling loadAllApis. ");
                        List<API> apiList = new SubscriptionDataLoaderImpl().loadAllApis(tenantDomain);
                        apiByUUIDMap.clear();
                        for (API api : apiList) {
                            apiByUUIDMap.put(api.getUuid(), api);
                            String key = api.getApiName().concat(":").concat(api.getApiVersion());
                            apiNameVersionMap.put(key, api);
                        }
                        apisInitialized = true;
                        return apiList;
                    } catch (APIManagementException e) {
                        log.error("Exception while loading APIs " + e);
                    }
                    return null;
                });

        executorService.schedule(apiTask, eventHubConfiguration.getInitDelay(), TimeUnit.MILLISECONDS);

        Runnable subscriptionLoadingTask = new PopulateTask<>(subscriptionMap,
                () -> {
                    try {
                        log.debug("Calling loadAllSubscriptions.");
                        return new SubscriptionDataLoaderImpl().loadAllSubscriptions(tenantDomain);
                    } catch (APIManagementException e) {
                        log.error("Exception while loading Subscriptions " + e);
                    }
                    return null;
                });

        executorService.schedule(subscriptionLoadingTask, eventHubConfiguration.getInitDelay(), TimeUnit.MILLISECONDS);

        Runnable applicationLoadingTask = new PopulateTask<>(applicationMap,
                () -> {
                    try {
                        log.debug("Calling loadAllApplications.");
                        return new SubscriptionDataLoaderImpl().loadAllApplications(tenantDomain);
                    } catch (APIManagementException e) {
                        log.error("Exception while loading Applications " + e);
                    }
                    return null;
                });

        executorService.schedule(applicationLoadingTask, eventHubConfiguration.getInitDelay(), TimeUnit.MILLISECONDS);

        Runnable keyMappingsTask =
                new PopulateTask<>(applicationKeyMappingMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllKeyMappings.");
                                return new SubscriptionDataLoaderImpl().loadAllKeyMappings(tenantDomain);
                            } catch (APIManagementException e) {
                                log.error("Exception while loading ApplicationKeyMapping " + e);
                            }
                            return null;
                        });

        executorService.schedule(keyMappingsTask, eventHubConfiguration.getInitDelay(), TimeUnit.MILLISECONDS);

        Runnable apiPolicyLoadingTask =
                new PopulateTask<>(apiPolicyMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllSubscriptionPolicies.");
                                List<ApiPolicy> apiPolicyList =
                                        new SubscriptionDataLoaderImpl().loadAllAPIPolicies(tenantDomain);
                                apiPoliciesInitialized = true;
                                return apiPolicyList;
                            } catch (APIManagementException e) {
                                log.error("Exception while loading api Policies " + e);
                            }
                            return null;
                        });

        executorService.schedule(apiPolicyLoadingTask, eventHubConfiguration.getInitDelay(), TimeUnit.MILLISECONDS);

        Runnable subPolicyLoadingTask =
                new PopulateTask<>(subscriptionPolicyMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllSubscriptionPolicies.");
                                return new SubscriptionDataLoaderImpl().loadAllSubscriptionPolicies(tenantDomain);
                            } catch (APIManagementException e) {
                                log.error("Exception while loading Subscription Policies " + e);
                            }
                            return null;
                        });

        executorService.schedule(subPolicyLoadingTask, eventHubConfiguration.getInitDelay(), TimeUnit.MILLISECONDS);

        Runnable appPolicyLoadingTask =
                new PopulateTask<>(appPolicyMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllAppPolicies.");
                                return new SubscriptionDataLoaderImpl().loadAllAppPolicies(tenantDomain);
                            } catch (APIManagementException e) {
                                log.error("Exception while loading Application Policies " + e);
                            }
                            return null;
                        });

        executorService.schedule(appPolicyLoadingTask, eventHubConfiguration.getInitDelay(), TimeUnit.MILLISECONDS);
        Runnable scopesLoadingTask =
                new PopulateTask<>(scopesMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllScopes.");
                                List<Scope> scopeList =
                                        new SubscriptionDataLoaderImpl().loadAllScopes(tenantDomain);
                                scopesInitialized = true;
                                return scopeList;
                            } catch (APIManagementException e) {
                                log.error("Exception while loading Scopes " + e);
                            }
                            return null;
                        });

        executorService.schedule(scopesLoadingTask, eventHubConfiguration.getInitDelay(), TimeUnit.MILLISECONDS);
    }

    public boolean isApisInitialized() {

        return apisInitialized;
    }

    public boolean isApiPoliciesInitialized() {

        return apiPoliciesInitialized;
    }

    @Override
    public void addOrUpdateSubscription(Subscription subscription) {
        String synchronizeKey = "SubscriptionDataStoreImpl-API-" + subscription.getCacheKey();

        synchronized (synchronizeKey.intern()) {
            Subscription retrievedSubscription = subscriptionMap.get(subscription.getCacheKey());
            if (retrievedSubscription == null) {
                subscriptionMap.put(subscription.getCacheKey(), subscription);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved Subscription from Map :" + retrievedSubscription.toString());
                }
                if (subscription.getTimeStamp() < retrievedSubscription.getTimeStamp()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Drop the Event " + subscription.toString() + " since the event timestamp was old");
                    }
                } else {
                    if (!APIConstants.SubscriptionStatus.ON_HOLD.equals(subscription.getSubscriptionState())) {
                        subscriptionMap.put(subscription.getCacheKey(), subscription);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Drop the Event " + subscription.toString() + " since the event was marked as " +
                                    "ON_HOLD");
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                Subscription updatedSubscription = subscriptionMap.get(subscription.getCacheKey());
                log.debug("Updated Subscription From map :" + updatedSubscription.toString());
            }

        }
    }

    @Override
    public void subscribeToAPIInternally(API api, Application app, String tenantDomain) {
        if (log.isDebugEnabled()) {
            log.debug("Subscribing internally to API " + api.getApiName() + " with version " + api.getApiVersion() +
                    " from application " + app.getName());
        }
        // Hand over the internal subscription to a separate thread pool to be carried out secretly
        subscriptionExecutorService.submit(() -> {
            if (getSubscriptionById(app.getId(), api.getApiId()) != null) {
                return;
            }
            new SubscriptionDataLoaderImpl().subscribeToAPIInternally(api, app, tenantDomain);
        });
    }

    @Override
    public void removeSubscription(Subscription subscription) {

        subscriptionMap.remove(subscription.getCacheKey());
    }

    @Override
    public void addOrUpdateAPI(API api) {

        String key = api.getApiName().concat(":").concat(api.getApiVersion());
        apiByUUIDMap.put(api.getUuid(), api);
        apiNameVersionMap.put(key, api);
        apiMap.put(api.getCacheKey(), api);
    }

    @Override
    public void addOrUpdateAPIWithUrlTemplates(API api) {

        try {
            API newAPI = new SubscriptionDataLoaderImpl().getApi(api.getContext(), api.getApiVersion());
            if (newAPI != null) {
                apiMap.put(api.getCacheKey(), newAPI);
                String key = newAPI.getApiName().concat(":").concat(newAPI.getApiVersion());
                apiNameVersionMap.put(key, newAPI);
                apiByUUIDMap.put(newAPI.getUuid(), newAPI);
            }
        } catch (DataLoadingException e) {
            log.error("Exception while loading api for " + api.getContext() + " " + api.getApiVersion(), e);
        }

    }

    @Override
    public void removeAPI(API api) {

        String key = api.getApiName().concat(":").concat(api.getApiVersion());
        apiByUUIDMap.remove(api.getUuid());
        apiNameVersionMap.remove(key);
        apiMap.remove(api.getCacheKey());
    }

    @Override
    public void addOrUpdateApplicationKeyMapping(ApplicationKeyMapping applicationKeyMapping) {

        applicationKeyMappingMap.remove(applicationKeyMapping.getCacheKey());
        applicationKeyMappingMap.put(applicationKeyMapping.getCacheKey(), applicationKeyMapping);
    }

    @Override
    public void removeApplicationKeyMapping(ApplicationKeyMapping applicationKeyMapping) {

        applicationKeyMappingMap.remove(applicationKeyMapping.getCacheKey());
    }

    @Override
    public void addOrUpdateSubscriptionPolicy(SubscriptionPolicy subscriptionPolicy) {

        subscriptionPolicyMap.remove(subscriptionPolicy.getCacheKey());
        subscriptionPolicyMap.put(subscriptionPolicy.getCacheKey(), subscriptionPolicy);
    }

    @Override
    public void addOrUpdateApplicationPolicy(ApplicationPolicy applicationPolicy) {

        appPolicyMap.remove(applicationPolicy.getCacheKey());
        appPolicyMap.put(applicationPolicy.getCacheKey(), applicationPolicy);
    }

    @Override
    public void removeApplicationPolicy(ApplicationPolicy applicationPolicy) {

        appPolicyMap.remove(applicationPolicy.getCacheKey());
    }

    @Override
    public void removeSubscriptionPolicy(SubscriptionPolicy subscriptionPolicy) {

        subscriptionPolicyMap.remove(subscriptionPolicy.getCacheKey());
    }

    @Override
    public void addOrUpdateApplication(Application application) {

        applicationMap.remove(application.getId());
        applicationMap.put(application.getId(), application);
    }

    @Override
    public void removeApplication(Application application) {

        applicationMap.remove(application.getId());
        subscriptionMap.values().removeIf(subscription ->
                subscription != null && application.getUUID().equals(subscription.getApplicationUUID()));
    }

    @Override
    public void addOrUpdateApiPolicy(ApiPolicy apiPolicy) {

        try {
            ApiPolicy policy = new SubscriptionDataLoaderImpl().getAPIPolicy(apiPolicy.getName(), tenantDomain);
            apiPolicyMap.remove(apiPolicy.getCacheKey());
            apiPolicyMap.put(apiPolicy.getCacheKey(), policy);
        } catch (DataLoadingException e) {
            log.error("Exception while loading api policy for " + apiPolicy.getName() + " for domain " + tenantDomain,
                    e);
        }
    }

    @Override
    public void removeApiPolicy(ApiPolicy apiPolicy) {

        apiPolicyMap.remove(apiPolicy.getCacheKey());
    }

    @Override
    public API getDefaultApiByContext(String context) {

        Set<String> set = apiMap.keySet()
                .stream()
                .filter(s -> s.startsWith(context))
                .collect(Collectors.toSet());
        for (String key : set) {
            API api = apiMap.get(key);
            if (api.isDefaultVersion() && (api.getContext().replace("/" + api.getApiVersion(), "")).equals(context)) {
                return api;
            }
        }
        return null;
    }

    public boolean isScopesInitialized() {

        return scopesInitialized;
    }
    @Override
    public Subscription getSubscriptionBySubscriptionUUID(String subscriptionUUID) {

        for (Subscription subscription : subscriptionMap.values()) {
            if (subscriptionUUID.equals(subscription.getSubscriptionUUId())) {
                return subscription;
            }
        }
        return null;
    }

    @Override
    public List<Application> getApplicationsByName(String name) {

        List<Application> applicationList = new ArrayList<>();
        if (applicationMap != null) {
            for (Application application : applicationMap.values()) {
                if (application.getName().equals(name)) {
                    applicationList.add(application);
                }
            }
        }
        return applicationList;
    }

    @Override
    public Application getApplicationByUUID(String uuid) {

        if (applicationMap != null) {
            for (Application application : applicationMap.values()) {
                if (application.getUUID().equals(uuid)) {
                    return application;
                }
            }
        }
        return null;
    }

    @Override
    public List<Subscription> getSubscriptionsByAPIId(int apiId) {

        List<Subscription> subscriptionList = new ArrayList<>();
        if (subscriptionMap != null) {
            for (Subscription subscription : subscriptionMap.values()) {
                if (subscription.getApiId() == apiId) {
                    subscriptionList.add(subscription);
                }
            }
        }
        return subscriptionList;
    }

    @Override
    public List<API> getAPIs() {

        return new ArrayList<>(apiMap.values());
    }

    @Override
    public Subscription getSubscriptionByUUID(String apiUUID, String appUUID) {

        if (subscriptionMap != null) {
            for (Subscription subscription : subscriptionMap.values()) {
                if (subscription.getApiUUID().equals(apiUUID) && subscription.getApplicationUUID().equals(appUUID)) {
                    return subscription;
                }
            }
        }
        return null;
    }

    @Override
    public List<ApplicationKeyMapping> getKeyMappingByApplicationId(int applicationId) {

        List<ApplicationKeyMapping> applicationKeyMappings = new ArrayList<>();
        if (applicationKeyMappingMap != null) {
            for (ApplicationKeyMapping applicationKeyMapping : applicationKeyMappingMap.values()) {
                if (applicationKeyMapping.getApplicationId() == applicationId) {
                    applicationKeyMappings.add(applicationKeyMapping);
                }
            }
        }
        return applicationKeyMappings;
    }

    @Override
    public void destroy() {

        shutdownExecutor(executorService);
        shutdownExecutor(subscriptionExecutorService);
    }

    private void shutdownExecutor(ExecutorService executor) {

        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void addOrUpdateScope(Scope scope) {

        scopesMap.put(scope.getCacheKey(), scope);
    }

    @Override
    public void deleteScope(Scope scope) {

        scopesMap.remove(scope.getCacheKey());
    }

    @Override
    public Map<String, Scope> getScopesByTenant(String tenantDomain) {

        return scopesMap;
    }

    @Override
    public Map<String, API> getAllAPIsByContextList() {

        Map<String, API> apiContextAPIMap = new HashMap<>();
        for (API api : apiMap.values()) {
            apiContextAPIMap.put(api.getContext(), api);
            if (api.isDefaultVersion()) {
                if (api.getContextTemplate() != null) {
                    String context = api.getContextTemplate().replace("/" + APIConstants.VERSION_PLACEHOLDER, "")
                            .replace(APIConstants.VERSION_PLACEHOLDER, "");
                    apiContextAPIMap.put(context, api);
                } else {
                    String context = api.getContext();
                    int index =  context.lastIndexOf("/" + api.getApiVersion());
                    if (index >= 0) {
                        context = context.substring(0, index);
                    }
                    apiContextAPIMap.put(context, api);
                }
            }
        }
        return apiContextAPIMap;
    }

    @Override
    public void addOrUpdateAPIRevisionWithUrlTemplates(DeployAPIInGatewayEvent event) {

        try {
            API api = apiMap.get(event.getContext() + ":" + event.getVersion());
            if (APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(event.getType())) {
                if (api != null) {
                    removeAPI(api);
                }
            } else {
                API newAPI = new SubscriptionDataLoaderImpl().getApi(event.getContext(), event.getVersion());
                if (newAPI != null) {
                    addOrUpdateAPI(newAPI);
                }
            }
            if (api != null) {
                clearResourceCache(api, event.getTenantDomain());
            }
        } catch (DataLoadingException e) {
            log.error("Exception while loading api for " + event.getContext() + " " + event.getVersion(), e);
        }
    }

    private void clearResourceCache(API api, String tenantDomain) {

        if (isAPIResourceValidationEnabled()) {
            new CacheInvalidationServiceImpl().invalidateResourceCache(api.getContext(), api.getApiVersion(),
                    tenantDomain, api.getResources());
        }
    }

    public boolean isAPIResourceValidationEnabled() {

        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(APIConstants.GATEWAY_RESOURCE_CACHE_ENABLED);
        if (StringUtils.isNotEmpty(serviceURL)) {
            return Boolean.parseBoolean(serviceURL);
        }
        return true;
    }

    public enum POLICY_TYPE {
        SUBSCRIPTION,
        APPLICATION,
        API
    }

    private static class PopulateTask<K, V extends CacheableEntity<K>> implements Runnable {

        private Map<K, V> entityMap;
        private Supplier<List<V>> supplier;

        PopulateTask(Map<K, V> entityMap, Supplier<List<V>> supplier) {

            this.entityMap = entityMap;
            this.supplier = supplier;
        }

        public void run() {

            List<V> list = supplier.get();
            HashMap<K, V> tempMap = new HashMap<>();

            if (list != null) {
                for (V v : list) {
                    tempMap.put(v.getCacheKey(), v);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Adding entry Key : %s Value : %s", v.getCacheKey(), v));
                    }

                    if (!tempMap.isEmpty()) {
                        entityMap.clear();
                        entityMap.putAll(tempMap);
                    }
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("List is null for " + supplier.getClass());
                }
            }
        }
    }

    /**
     * Thread factory to create internal subscription threads.
     */
    private static class InternalSubscriptionThreadFactory implements ThreadFactory {
        private int count = 0;
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "InternalSubscriptionThread-thread-" + count++);
        }
    }

    /**
     * Updates API properties in the data store using the given {@link GatewayAPIDTO}.
     * Synchronizes on API context/version key, loads API if missing, and updates its properties.
     *
     * @param gatewayAPIDTO DTO with API context, version, and properties.
     */
    @Override
    public void updateAPIPropertiesFromGatewayDTO(GatewayAPIDTO gatewayAPIDTO) {
        String key = gatewayAPIDTO.getApiContext() + DELEM_PERIOD + gatewayAPIDTO.getVersion();
        String synchronizeKey = "SubscriptionDataStoreImpl-API-" + key;
        synchronized (synchronizeKey.intern()) {
            // Direct map access to avoid nested synchronization
            API subscriptionAPI = apiMap.get(key);
            if (subscriptionAPI == null) {
                // If API not found, try to load it without nested synchronization
                try {
                    subscriptionAPI = new SubscriptionDataLoaderImpl().getApi(gatewayAPIDTO.getApiContext(),
                            gatewayAPIDTO.getVersion());
                    if (subscriptionAPI != null && subscriptionAPI.getApiId() != 0) {
                        // load to the memory
                        addOrUpdateAPI(subscriptionAPI);
                    }
                } catch (DataLoadingException e) {
                    log.error("Error while Retrieving Data From Internal Rest API", e);
                }
            }
            if (subscriptionAPI != null) {
                subscriptionAPI.setApiProperties(gatewayAPIDTO.getAdditionalProperties());
                if (log.isDebugEnabled()) {
                    log.debug("Updated API properties in SubscriptionDataStore for API: " + subscriptionAPI.getName() +
                            " (Context: " + subscriptionAPI.getContext() + ", Version: " +
                            subscriptionAPI.getVersion() + ")");
                }
            } else if (log.isDebugEnabled()) {
                log.debug("API not found in SubscriptionDataStore for key: " + key);
            }
        }
    }
}
