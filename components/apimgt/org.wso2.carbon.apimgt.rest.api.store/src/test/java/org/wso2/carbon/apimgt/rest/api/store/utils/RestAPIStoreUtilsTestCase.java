/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.apache.axis2.client.ServiceClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@RunWith(PowerMockRunner.class) @PrepareForTest({ ServiceReferenceHolder.class, APIUtil.class,
        CarbonUtils.class, Caching.class, RestApiUtil.class })
public class RestAPIStoreUtilsTestCase {
    private final String ADMIN = "admin";
    private Application application;
    private Set<SubscribedAPI> subscriptions;

    @Before @SuppressWarnings("unchecked")
    public void init() throws Exception {
        String applicationUuid = "applicationUuid";
        application = new Application(applicationUuid, new Subscriber(ADMIN));
        application.setGroupId("group1");
        application.setUUID(applicationUuid);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.doReturn(serviceReferenceHolder).when(ServiceReferenceHolder.class, "getInstance");
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.doReturn(apiManagerConfigurationService).when(serviceReferenceHolder)
                .getAPIManagerConfigurationService();
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ServiceClient serviceClient = Mockito.mock(ServiceClient.class);

        // Creating the configuration values that need to be returned.
        Mockito.doReturn(apiManagerConfiguration).when(apiManagerConfigurationService).getAPIManagerConfiguration();
        Mockito.doReturn(ADMIN).when(apiManagerConfiguration).getFirstProperty(APIConstants.KEYMANAGER_SERVERURL);
        Mockito.doReturn(ADMIN).when(apiManagerConfiguration).getFirstProperty(APIConstants.KEY_MANAGER_USERNAME);
        Mockito.doReturn(ADMIN).when(apiManagerConfiguration).getFirstProperty(APIConstants.KEY_MANAGER_PASSWORD);
        Mockito.doReturn("true").when(apiManagerConfiguration).getFirstProperty(APIConstants.SCOPE_CACHE_ENABLED);

        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.doNothing()
                .when(CarbonUtils.class, "setBasicAccessSecurityHeaders", Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyBoolean(), Mockito.any(ServiceClient.class));
        PowerMockito.mockStatic(Caching.class);

        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.doReturn(cacheManager)
                .when(Caching.class, "getCacheManager", APIConstants.API_MANAGER_CACHE_MANAGER);
        Cache appCache = Mockito.mock(Cache.class);
        Cache filteredAppCache = Mockito.mock(Cache.class);
        Mockito.doReturn(appCache).when(cacheManager).getCache(APIConstants.APP_SUBSCRIPTION_SCOPE_CACHE);
        Mockito.doReturn(filteredAppCache).when(cacheManager)
                .getCache(APIConstants.APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE);
        Mockito.doReturn(new LinkedHashSet<Set<Scope>>()).when(appCache).get(applicationUuid);
        Mockito.doReturn(new LinkedHashSet<Set<Scope>>()).when(appCache).get(ADMIN + "-" + applicationUuid);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getListOfRoles(Mockito.anyString())).thenReturn(new String[] { ADMIN });
    }

    /**
     * This method tests the behaviour of convertScopeSetToScopeList, under various conditions.
     */
    @Test
    public void testConvertScopeSetToScopeList() {
        ScopeListDTO scopeListDTO = RestAPIStoreUtils.convertScopeSetToScopeList(null);
        Assert.assertNull("Scope list was returned for a scope set of null", scopeListDTO);

        Set<Scope> scopeSet = new LinkedHashSet<>();
        scopeListDTO = RestAPIStoreUtils.convertScopeSetToScopeList(scopeSet);
        Assert.assertNotNull("Scope list was not returned for a scope set", scopeListDTO);
        Assert.assertEquals("Scope list size is different from the scope set size", scopeSet.size(),
                scopeListDTO.getList().size());

        scopeSet = getScopes();
        scopeListDTO = RestAPIStoreUtils.convertScopeSetToScopeList(scopeSet);
        Assert.assertNotNull("Scope list was not returned for a scope set", scopeListDTO);
        Assert.assertEquals("Scope list size is different from the scope set size", scopeSet.size(),
                scopeListDTO.getList().size());
    }

    /**
     * This method tests the behaviour of method getScopesForApplicationWithCache, when the cache is enabled and
     * disabled.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test @SuppressWarnings("unchecked")
    public void getScopesForApplicationWithCache() throws Exception {
        ScopeListDTO scopeListDTO = RestAPIStoreUtils.getScopesForApplication(ADMIN, application, false);
        Assert.assertNotNull("Scope list was null for the application", scopeListDTO);
        Assert.assertEquals("Random scope list has been added to the application with no scopes", 0,
                scopeListDTO.getList().size());

        // Creating the mock and objects needed for testing.
        createSubscriptions();
        PowerMockito.mockStatic(RestApiUtil.class);
        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.doReturn(apiConsumer).when(RestApiUtil.class, "getConsumer", Mockito.anyString());
        PowerMockito.doReturn(ADMIN).when(RestApiUtil.class, "getLoggedInUserTenantDomain");
        Mockito.doReturn(subscriptions).when(apiConsumer)
                .getSubscribedAPIs(Mockito.any(Subscriber.class), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(getScopes()).when(apiConsumer).getScopesBySubscribedAPIs(Mockito.anyList());
        PowerMockito.doReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)
                .when(RestApiUtil.class, "getLoggedInUserTenantDomain");

        scopeListDTO = RestAPIStoreUtils.getScopesForApplication(ADMIN + "@", application, true);
        Assert.assertNotNull("Scope list was null for a application with scope list", scopeListDTO);
        Assert.assertEquals("Random scope list has been added to the application with one scopes", 1,
                scopeListDTO.getList().size());

        Field isStoreCacheEnabled = RestAPIStoreUtils.class.getDeclaredField("isStoreCacheEnabled");
        isStoreCacheEnabled.setAccessible(true);
        isStoreCacheEnabled.set(null, false);
        scopeListDTO = RestAPIStoreUtils.getScopesForApplication(ADMIN, application, false);
        Assert.assertNotNull("Scope list was null for a application with scope list", scopeListDTO);
        Assert.assertEquals("Random scope list has been added to the application with one scopes", 1,
                scopeListDTO.getList().size());
    }

    /**
     * Create sample subscriptions/
     *
     * @throws APIManagementException API Management Exception.
     */
    private void createSubscriptions() throws APIManagementException {
        subscriptions = new HashSet<>();
        SubscribedAPI subscribedAPI = new SubscribedAPI(new Subscriber(ADMIN),
                new APIIdentifier(ADMIN + "_" + ADMIN + "_" + ADMIN));
        subscribedAPI.setApplication(application);
        subscriptions.add(subscribedAPI);
    }

    /**
     * To get the scopes
     *
     * @return set of random scopes.
     */
    private Set<Scope> getScopes() {
        Set<Scope> scopeSet = new LinkedHashSet<>();
        Scope scope = new Scope();
        scope.setId("1");
        scope.setDescription("");
        scope.setKey("test");
        scope.setName("test");
        scope.setRoles("admin");
        scopeSet.add(scope);
        return scopeSet;
    }

}
