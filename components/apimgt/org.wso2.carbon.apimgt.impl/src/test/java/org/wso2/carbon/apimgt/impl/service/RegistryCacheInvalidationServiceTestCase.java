/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.service;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.caching.RegistryCacheKey;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.ArrayList;
import java.util.List;
import javax.cache.Cache;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, PrivilegedCarbonContext.class, RegistryUtils.class})
public class RegistryCacheInvalidationServiceTestCase {

    private ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
    private RealmService realmService = Mockito.mock(RealmService.class);
    private TenantManager tenantManager = Mockito.mock(TenantManager.class);
    private RegistryService registryService = Mockito.mock(RegistryService.class);
    private UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
    private RegistryContext registryContext = Mockito.mock(RegistryContext.class);
    private Cache cache = Mockito.mock(Cache.class);


    private static String path = "/_system/config/myresource";
    private static String tenantDomain = "foo.com";

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(6543);

        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(6543)).thenReturn(userRegistry);
        PowerMockito.mockStatic(RegistryUtils.class);
        Mockito.when(userRegistry.getRegistryContext()).thenReturn(registryContext);

        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
    }

    @Test
    public void testInvalidateCacheWhenRegistryUnmountedAndNotCached() throws APIManagementException {

        List<RemoteConfiguration> remoteConfigurationList = new ArrayList<RemoteConfiguration>();
        Mockito.when(registryContext.getRemoteInstances()).thenReturn(remoteConfigurationList);
        DataBaseConfiguration dbConfiguration = Mockito.mock(DataBaseConfiguration.class);
        Mockito.when(registryContext.getDefaultDataBaseConfiguration()).thenReturn(dbConfiguration);
        PowerMockito.when(RegistryUtils.getResourceCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID)).thenReturn(cache);

        RegistryCacheInvalidationService registryCacheInvalidationService = new RegistryCacheInvalidationService();
        registryCacheInvalidationService.invalidateCache(path,tenantDomain);
        Mockito.verify(cache, Mockito.times(0)).remove(Matchers.any());
    }


    @Test
    public void testInvalidateCacheWhenRegistryUnmountedAndCached() throws APIManagementException {

        List<RemoteConfiguration> remoteConfigurationList = new ArrayList<RemoteConfiguration>();
        Mockito.when(registryContext.getRemoteInstances()).thenReturn(remoteConfigurationList);
        DataBaseConfiguration dbConfiguration = Mockito.mock(DataBaseConfiguration.class);
        Mockito.when(registryContext.getDefaultDataBaseConfiguration()).thenReturn(dbConfiguration);
        PowerMockito.when(RegistryUtils.getResourceCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID)).thenReturn(cache);
        Mockito.when(dbConfiguration.getUserName()).thenReturn("john@foo.com");
        Mockito.when(dbConfiguration.getDbUrl()).thenReturn("xyz.com/db");
        RegistryCacheKey registryCacheKey = Mockito.mock(RegistryCacheKey.class);
        PowerMockito.when(RegistryUtils.buildRegistryCacheKey("john@xyz.com/db", 6543, path)).thenReturn(registryCacheKey);
        Mockito.when(cache.containsKey(registryCacheKey)).thenReturn(true);

        RegistryCacheInvalidationService registryCacheInvalidationService = new RegistryCacheInvalidationService();
        registryCacheInvalidationService.invalidateCache(path,tenantDomain);
        Mockito.verify(cache, Mockito.times(1)).remove(Matchers.any());
    }

    @Test
    public void testInvalidateCacheWhenRegistryMountedAndCached() throws APIManagementException {

        List<RemoteConfiguration> remoteConfigurationList = new ArrayList<RemoteConfiguration>();
        RemoteConfiguration remoteConfiguration = new RemoteConfiguration();
        remoteConfiguration.setDbConfig("");
        remoteConfigurationList.add(remoteConfiguration);
        List<Mount> mountList = new ArrayList<Mount>();
        Mount mount = new Mount();
        mount.setPath("/_system/config");
        mountList.add(mount);
        DataBaseConfiguration dataBaseConfiguration = Mockito.mock(DataBaseConfiguration.class);
        Mockito.when(registryContext.getMounts()).thenReturn(mountList);
        Mockito.when(registryContext.getDBConfig(remoteConfiguration.getDbConfig())).thenReturn(dataBaseConfiguration);

        Mockito.when(registryContext.getRemoteInstances()).thenReturn(remoteConfigurationList);
        Mockito.when(registryContext.getDefaultDataBaseConfiguration()).thenReturn(dataBaseConfiguration);
        PowerMockito.when(RegistryUtils.getResourceCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID)).thenReturn(cache);
        Mockito.when(dataBaseConfiguration.getUserName()).thenReturn("john@foo.com");
        Mockito.when(dataBaseConfiguration.getDbUrl()).thenReturn("xyz.com/db");
        RegistryCacheKey registryCacheKey = Mockito.mock(RegistryCacheKey.class);
        PowerMockito.when(RegistryUtils.buildRegistryCacheKey("john@xyz.com/db", 6543, path)).thenReturn(registryCacheKey);
        Mockito.when(cache.containsKey(registryCacheKey)).thenReturn(true);

        RegistryCacheInvalidationService registryCacheInvalidationService = new RegistryCacheInvalidationService();
        registryCacheInvalidationService.invalidateCache(path,tenantDomain);
        Mockito.verify(cache, Mockito.times(1)).remove(Matchers.any());
    }
}