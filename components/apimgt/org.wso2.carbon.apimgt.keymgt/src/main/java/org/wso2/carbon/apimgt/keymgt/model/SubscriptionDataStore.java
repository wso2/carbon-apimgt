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
package org.wso2.carbon.apimgt.keymgt.model;

import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;

import java.util.List;
import java.util.Map;

/**
 * A Facade for obtaining Subscription related Data.
 */
public interface SubscriptionDataStore {

    /**
     * Initialize SubscriptionDataStore.
     */
    void init();

    /**
     * Gets an {@link Application} by Id
     *
     * @param appId Id of the Application
     * @return {@link Application} with the appId
     */
    Application getApplicationById(int appId);

    /**
     * Gets the {@link ApplicationKeyMapping} entry by Key
     *
     * @param key <ApplicationIs>.<keyType>
     * @param keyManager Keymanager Name
     * @return {@link ApplicationKeyMapping} entry
     */
    ApplicationKeyMapping getKeyMappingByKeyAndKeyManager(String key, String keyManager);

    /**
     * Get API by Context and Version
     *
     * @param context Context of the API
     * @param version Version of the API
     * @return {@link API} entry represented by Context and Version.
     */
    API getApiByContextAndVersion(String context, String version);

    /**
     * Get API by Name and Version
     *
     * @param name Name of the API
     * @param version Version of the API
     * @return {@link API} entry represented by Context and Version.
     */
    API getApiByNameAndVersion(String name, String version);


    /**
     * Get API by UUID.
     *
     * @param apiUUID UUID of API
     * @return {@link API} entry represented by Context and Version.
     */
    API getAPIByUUID(String apiUUID);
    /**
     * Gets Subscription by ID
     *
     * @param appId Application associated with the Subscription
     * @param apiId Api associated with the Subscription
     * @return {@link Subscription}
     */
    Subscription getSubscriptionById(int appId, int apiId);

    /**
     * Gets API Throttling Policy by the name and Tenant Id
     *
     * @param policyName Name of the Throttling Policy
     * @param tenantId   Tenant ID in the Policy
     * @return API Throttling Policy
     */
    ApiPolicy getApiPolicyByName(String policyName, int tenantId);
    /**
     * Gets Subscription Throttling Policy by the name and Tenant Id
     *
     * @param policyName Name of the Throttling Policy
     * @param tenantId   Tenant ID in the Policy
     * @return Subscription Throttling Policy
     */
    SubscriptionPolicy getSubscriptionPolicyByName(String policyName, int tenantId);

    /**
     * Gets Application Throttling Policy by the name and Tenant Id
     *
     * @param policyName Name of the Throttling Policy
     * @param tenantId   Tenant ID in the Policy
     * @return Application Throttling Policy
     */
    ApplicationPolicy getApplicationPolicyByName(String policyName, int tenantId);
    
    void addOrUpdateApplication(Application application);

    void addOrUpdateSubscription(Subscription subscription);

    void addOrUpdateAPI(API api);
    
    void addOrUpdateAPIWithUrlTemplates(API api);

    void addOrUpdateAPIRevisionWithUrlTemplates(DeployAPIInGatewayEvent event);

    void addOrUpdateApplicationKeyMapping(ApplicationKeyMapping applicationKeyMapping);
    
    void addOrUpdateSubscriptionPolicy(SubscriptionPolicy subscriptionPolicy);

    void addOrUpdateApplicationPolicy(ApplicationPolicy applicationPolicy);
    
    void addOrUpdateApiPolicy(ApiPolicy apiPolicy);
    
    void removeApplication(Application application);

    void removeAPI(API api);

    void removeSubscription(Subscription subscription);

    void removeApplicationKeyMapping(ApplicationKeyMapping applicationKeyMapping);

    void removeSubscriptionPolicy(SubscriptionPolicy subscriptionPolicy);

    void removeApplicationPolicy(ApplicationPolicy applicationPolicy);
    
    void removeApiPolicy(ApiPolicy apiPolicy);

    boolean isApisInitialized();

    boolean isApiPoliciesInitialized();

    API getDefaultApiByContext(String context);

    void addOrUpdateScope(Scope scope);

    void deleteScope(Scope scope);

    Map<String, Scope> getScopesByTenant(String tenantDomain);

    Map<String,API> getAllAPIsByContextList();

    boolean isScopesInitialized();

    Subscription getSubscriptionBySubscriptionUUID(String subscriptionUUID);

    List<Application> getApplicationsByName(String name);

    Application getApplicationByUUID(String uuid);

    List<Subscription> getSubscriptionsByAPIId(int apiId);

    List<API> getAPIs();

    Subscription getSubscriptionByUUID(String apiUUID, String appUUID);

    List<ApplicationKeyMapping> getKeyMappingByApplicationId(int applicationId);

    void destroy();
}

