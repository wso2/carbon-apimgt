/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

public class TestUtils {

    protected static Application getUniqueApplication() {
        return new Application("TestApplication", getUniqueSubscriber());
    }

    protected static Subscriber getUniqueSubscriber() {
        return new Subscriber(UUID.randomUUID().toString());
    }

    protected static APIIdentifier getUniqueAPIIdentifier() {
        return new APIIdentifier(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID()
                 .toString());
    }
    
    public static ServiceReferenceHolder mockRegistryAndUserRealm(int tenantId) throws UserStoreException, 
                                                                                        RegistryException {
        ServiceReferenceHolder sh = getServiceReferenceHolder();
        
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tm = Mockito.mock(TenantManager.class);
        
        PowerMockito.when(sh.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tm);
        
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        
        UserRegistry userReg = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceUserRegistry()).thenReturn(userReg);
        
        UserRegistry systemReg = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getConfigSystemRegistry()).thenReturn(systemReg);
        
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        UserRealm bootstrapRealm = Mockito.mock(UserRealm.class);
        
        PowerMockito.when(systemReg.getUserRealm()).thenReturn(userRealm);        
        PowerMockito.doNothing().when(ServiceReferenceHolder.class); 
        ServiceReferenceHolder.setUserRealm(userRealm);
        
        org.wso2.carbon.user.api.UserRealm userR = Mockito.mock(org.wso2.carbon.user.api.UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userR);
        AuthorizationManager authManager = Mockito.mock(AuthorizationManager.class);
        PowerMockito.when(userR.getAuthorizationManager()).thenReturn(authManager);
        
        PowerMockito.when(realmService.getBootstrapRealm()).thenReturn(bootstrapRealm);
        ServiceReferenceHolder.setUserRealm(bootstrapRealm);
        
        PowerMockito.when(tm.getTenantId(Matchers.anyString())).thenReturn(tenantId);
        
        return sh;
    }
    
    public static void mockAPIMConfiguration(String propertyName, String value) throws RegistryException, 
                                                                            UserStoreException {
        ServiceReferenceHolder sh = mockRegistryAndUserRealm(-1234);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        
        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getFirstProperty(propertyName)).thenReturn(value);
        
        Map<String, Environment> apiGatewayEnvironments = new HashMap<String, Environment>();
        Environment env1 = new Environment();
        apiGatewayEnvironments.put("PROD", env1);
        //Mocking some commonly used configs
        PowerMockito.when(amConfig.getApiGatewayEnvironments()).thenReturn(apiGatewayEnvironments);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).
                            thenReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS)).
            thenReturn("true", "false");
    }
    
    public static void mockAPIMConfiguration() throws RegistryException,
            UserStoreException {
        ServiceReferenceHolder sh = mockRegistryAndUserRealm(-1234);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);

        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);

        Map<String, Environment> apiGatewayEnvironments = new HashMap<String, Environment>();
        Environment env1 = new Environment();
        env1.setApiGatewayEndpoint("https://abc.com, http://abc.com");
        apiGatewayEnvironments.put("PROD", env1);
        // Mocking some commonly used configs
        PowerMockito.when(amConfig.getApiGatewayEnvironments()).thenReturn(apiGatewayEnvironments);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).thenReturn(
                APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS)).
                thenReturn("true", "false");
        
        ThrottleProperties throttleProperties = new ThrottleProperties();
        PowerMockito.when(amConfig.getThrottleProperties()).thenReturn(throttleProperties);
        
    }
    
    public static ServiceReferenceHolder getServiceReferenceHolder() {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        return sh;
    }
    
    public static void mockAPICacheClearence() {
        PowerMockito.mockStatic(Caching.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Cache<Object, Object> cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME)).thenReturn(cache);
        Mockito.doNothing().when(cache).removeAll();
    }

    public static ApiMgtDAO getApiMgtDAO(){
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        return apiMgtDAO;
    }
}
