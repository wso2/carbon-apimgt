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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.api.model.subscription.CacheableEntity;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMappingCacheKey;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Policy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.util.SubscriptionDataStoreUtil;
import org.wso2.carbon.base.MultitenantConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SubscriptionDataStoreImpl implements SubscriptionDataStore {

    private static final Log log = LogFactory.getLog(SubscriptionDataStoreImpl.class);
    private boolean scopesInitialized;

    public enum POLICY_TYPE {
        SUBSCRIPTION,
        APPLICATION,
        API
    }
    public static final String DELEM_PERIOD = ":";

    // Maps for keeping Subscription related details.
    private Map<ApplicationKeyMappingCacheKey, ApplicationKeyMapping> applicationKeyMappingMap;
    private Map<Integer, Application> applicationMap;
    private Map<String, API> apiMap;
    private Map<String, ApiPolicy> apiPolicyMap;
    private Map<String, SubscriptionPolicy> subscriptionPolicyMap;
    private Map<String, ApplicationPolicy> appPolicyMap;
    private Map<String, Subscription> subscriptionMap;
    private Map<String,Scope> scopesMap;
    private boolean apisInitialized;
    private boolean applicationsInitialized;
    private boolean subscriptionsInitialized;
    private boolean applicationKeysInitialized;
    private boolean applicationPoliciesInitialized;
    private boolean subscriptionPoliciesInitialized;
    private boolean apiPoliciesInitialized;
    public static final int LOADING_POOL_SIZE = 7;
    private String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(LOADING_POOL_SIZE);

    public SubscriptionDataStoreImpl(String tenantDomain) {

        this.tenantDomain = tenantDomain;
        initializeStore();
    }

    private void initializeStore() {

        this.applicationKeyMappingMap = new ConcurrentHashMap<>();
        this.applicationMap = new ConcurrentHashMap<>();
        this.apiMap = new ConcurrentHashMap<>();
        this.subscriptionPolicyMap = new ConcurrentHashMap<>();
        this.appPolicyMap = new ConcurrentHashMap<>();
        this.apiPolicyMap = new ConcurrentHashMap<>();
        this.subscriptionMap = new ConcurrentHashMap<>();
        this.scopesMap = new ConcurrentHashMap<>();
    }

    @Override
    public void init() {
        initializeLoadingTasks();
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
                    log.debug("Application not found. applicationId = " + application.getId());
                }
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

    @Override public ApplicationKeyMapping getKeyMappingByKeyAndKeyManager(String key, String keyManager) {
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
            log.debug("Retrieving Application information with Consumer Key : " + key + " and keymanager : "
                    + keyManager);
            if (applicationKeyMapping != null) {
                log.debug("Retrieved Application information with Consumer Key : " + key + " and keymanager : "
                        + keyManager + " is " + applicationKeyMapping.toString());
            } else {
                log.debug("Retrieving Application information with Consumer Key : " + key + " and keymanager : "
                        + keyManager + " is empty");
            }
        }
        return applicationKeyMapping;
    }

    @Override
    public API getApiByContextAndVersion(String context, String version) {
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
                log.debug("Retrieved API information with Context  : " + context + " and Version : " + version + " is"
                        + " " + api.toString());
            } else {
                log.debug("Retrieved API information with Context  : " + context + " and Version : " + version + " is"
                        + " empty");
            }
        }
        return api;
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
                    subscription = new SubscriptionDataLoaderImpl()
                            .getSubscriptionById(Integer.toString(apiId), Integer.toString(appId));
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
                log.debug("Retrieved API Subscription with Application " + appId + " and APIId : " + apiId + " is "
                        + subscription.toString());
            } else {
                log.debug("Retrieved API Subscription with Application " + appId + " and APIId : " + apiId + " is "
                        + "empty.");
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

        Runnable apiTask = new PopulateTask<String, API>(apiMap,
                () -> {
                    try {
                        log.debug("Calling loadAllApis. ");
                        List<API> apiList = new SubscriptionDataLoaderImpl().loadAllApis(tenantDomain);
                        apisInitialized = true;
                        return apiList;
                    } catch (APIManagementException e) {
                        log.error("Exception while loading APIs " + e);
                    }
                    return null;
                });

        executorService.schedule(apiTask, 0, TimeUnit.SECONDS);

        Runnable subscriptionLoadingTask = new PopulateTask<String, Subscription>(subscriptionMap,
                () -> {
                    try {
                        log.debug("Calling loadAllSubscriptions.");
                        List<Subscription> subscriptionList =
                                new SubscriptionDataLoaderImpl().loadAllSubscriptions(tenantDomain);
                        subscriptionsInitialized = true;
                        return subscriptionList;
                    } catch (APIManagementException e) {
                        log.error("Exception while loading Subscriptions " + e);
                    }
                    return null;
                });

        executorService.schedule(subscriptionLoadingTask, 0, TimeUnit.SECONDS);

        Runnable applicationLoadingTask = new PopulateTask<Integer, Application>(applicationMap,
                () -> {
                    try {
                        log.debug("Calling loadAllApplications.");
                        List<Application> applicationList =
                                new SubscriptionDataLoaderImpl().loadAllApplications(tenantDomain);
                        applicationsInitialized = true;
                        return applicationList;
                    } catch (APIManagementException e) {
                        log.error("Exception while loading Applications " + e);
                    }
                    return null;
                });

        executorService.schedule(applicationLoadingTask, 0, TimeUnit.SECONDS);

        Runnable keyMappingsTask =
                new PopulateTask<ApplicationKeyMappingCacheKey, ApplicationKeyMapping>(applicationKeyMappingMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllKeyMappings.");
                                List<ApplicationKeyMapping> applicationKeyMappingList =
                                        new SubscriptionDataLoaderImpl().loadAllKeyMappings(tenantDomain);
                                applicationKeysInitialized = true;
                                return applicationKeyMappingList;
                            } catch (APIManagementException e) {
                                log.error("Exception while loading ApplicationKeyMapping " + e);
                            }
                            return null;
                        });

        executorService.schedule(keyMappingsTask, 0, TimeUnit.SECONDS);

        Runnable apiPolicyLoadingTask =
                new PopulateTask<String, ApiPolicy>(apiPolicyMap,
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

        executorService.schedule(apiPolicyLoadingTask, 0, TimeUnit.SECONDS);

        Runnable subPolicyLoadingTask =
                new PopulateTask<String, SubscriptionPolicy>(subscriptionPolicyMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllSubscriptionPolicies.");
                                List<SubscriptionPolicy> subscriptionPolicyList =
                                        new SubscriptionDataLoaderImpl().loadAllSubscriptionPolicies(tenantDomain);
                                subscriptionPoliciesInitialized = true;
                                return subscriptionPolicyList;
                            } catch (APIManagementException e) {
                                log.error("Exception while loading Subscription Policies " + e);
                            }
                            return null;
                        });

        executorService.schedule(subPolicyLoadingTask, 0, TimeUnit.SECONDS);

        Runnable appPolicyLoadingTask =
                new PopulateTask<String, ApplicationPolicy>(appPolicyMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllAppPolicies.");
                                List<ApplicationPolicy> applicationPolicyList =
                                        new SubscriptionDataLoaderImpl().loadAllAppPolicies(tenantDomain);
                                applicationPoliciesInitialized = true;
                                return applicationPolicyList;
                            } catch (APIManagementException e) {
                                log.error("Exception while loading Application Policies " + e);
                            }
                            return null;
                        });

        executorService.schedule(appPolicyLoadingTask, 0, TimeUnit.SECONDS);
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

        executorService.schedule(scopesLoadingTask, 0, TimeUnit.SECONDS);
    }

    private <T extends Policy> T getPolicy(String policyName, int tenantId,
                                           Map<String, T> policyMap) {

        return policyMap.get(SubscriptionDataStoreUtil.getPolicyCacheKey(policyName, tenantId));
    }

    private class PopulateTask<K, V extends CacheableEntity<K>> implements Runnable {

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

    public boolean isApisInitialized() {

        return apisInitialized;
    }

    public boolean isApplicationsInitialized() {

        return applicationsInitialized;
    }

    public boolean isSubscriptionsInitialized() {

        return subscriptionsInitialized;
    }

    public boolean isApplicationKeysInitialized() {

        return applicationKeysInitialized;
    }

    public boolean isApplicationPoliciesInitialized() {

        return applicationPoliciesInitialized;
    }

    public boolean isSubscriptionPoliciesInitialized() {

        return subscriptionPoliciesInitialized;
    }
    
    public boolean isApiPoliciesInitialized() {

        return apiPoliciesInitialized;
    }

    public boolean isSubscriptionValidationDataInitialized() {

        return apisInitialized &&
                applicationsInitialized &&
                subscriptionsInitialized &&
                applicationKeysInitialized &&
                applicationPoliciesInitialized &&
                subscriptionPoliciesInitialized &&
                apiPoliciesInitialized;
    }

    @Override
    public void addOrUpdateSubscription(Subscription subscription) {
        synchronized (subscriptionMap) {
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
    public void removeSubscription(Subscription subscription) {
        subscriptionMap.remove(subscription.getCacheKey());
    }

    @Override
    public void addOrUpdateAPI(API api) {
        apiMap.put(api.getCacheKey(), api);
    }

    @Override
    public void addOrUpdateAPIWithUrlTemplates(API api) {
        try {
            API newAPI = new SubscriptionDataLoaderImpl().getApi(api.getContext(), api.getApiVersion());
            apiMap.put(api.getCacheKey(), newAPI);
        } catch (DataLoadingException e) {
            log.error("Exception while loading api for " + api.getContext() + " " + api.getApiVersion(), e);
        }

    }
    @Override
    public void removeAPI(API api) {
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
}
