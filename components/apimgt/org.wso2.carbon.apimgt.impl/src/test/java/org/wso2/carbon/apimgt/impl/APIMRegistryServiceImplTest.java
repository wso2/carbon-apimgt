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


import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, ServiceReferenceHolder.class, APIUtil.class})
public class APIMRegistryServiceImplTest {
    private final int TENANT_ID = 1234;
    private final String TENANT_DOMAIN = "test.foo";
    @Test
    public void getConfigRegistryResourceContentTestCase() throws UserStoreException, RegistryException, APIManagementException{
        APIMRegistryServiceImpl apimRegistryService = new APIMRegistryServiceImplWrapper();

        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(TENANT_ID);

        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);


        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        Mockito.when(registryService.getConfigSystemRegistry(TENANT_ID)).thenReturn(registry);

        PowerMockito.mockStatic(APIUtil.class);
        APIUtil.loadTenantConf(TENANT_ID);
        Mockito.when(registry.resourceExists("testLocation")).thenReturn(true);
        Mockito.when(registry.get("testLocation")).thenReturn(resource);


        Assert.assertEquals("testContent", apimRegistryService.
                getConfigRegistryResourceContent("test.foo", "testLocation"));

    }

    @Test
    public void getGovernanceRegistryResourceContentTestCase() throws UserStoreException, RegistryException{
        APIMRegistryServiceImpl apimRegistryService = new APIMRegistryServiceImplWrapper();

        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(TENANT_ID);

        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);


        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        Mockito.when(registryService.getGovernanceSystemRegistry(TENANT_ID)).thenReturn(registry);

        Mockito.when(registry.resourceExists("testLocation")).thenReturn(true);
        Mockito.when(registry.get("testLocation")).thenReturn(resource);


        Assert.assertEquals("testContent", apimRegistryService.
                getGovernanceRegistryResourceContent("test.foo", "testLocation"));
    }
}
