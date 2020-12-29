/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.persistence;

import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CarbonContext.class, RegistryPersistenceUtil.class, ServiceReferenceHolder.class })
public class RegistryPersistenceImplTestCase {
    private final int SUPER_TENANT_ID = -1234;
    private final String SUPER_TENANT_DOMAIN = "carbon.super";
    private final int TENANT_ID = 1;
    private final String TENANT_DOMAIN = "wso2.com";

    @Before
    public void setupClass() {
        System.setProperty("carbon.home", "");
    }

    @Test
    public void normal() throws Exception {
        String username = "admin";
        Registry registry = Mockito.mock(Registry.class);
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(username, registry);
        System.out.println(apiPersistenceInstance
                .addMediationPolicy(new Organization(SUPER_TENANT_DOMAIN), "xxxxx", null).getName());
    }

    @Test
    public void testRegistrySelectionForSuperTenantUser() throws Exception {

        RegistryService registryService = Mockito.mock(RegistryService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(SUPER_TENANT_ID);

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);

        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);

        UserRealm realm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(SUPER_TENANT_ID)).thenReturn(realm);
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadloadTenantAPIRXT", Mockito.any(String.class),
                Mockito.any(Integer.class));
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadTenantAPIPolicy", Mockito.any(String.class),
                Mockito.any(Integer.class));

        Mockito.when(context.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN);
        Mockito.when(context.getTenantId()).thenReturn(SUPER_TENANT_ID);
        
        String username = "admin";
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(username, tenantManager,
                registryService);
        // return null artifact because we are not testing artifact related params. this is only to get the registry obj
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class); 
        PowerMockito.when(
                RegistryPersistenceUtil.getArtifactManager(Mockito.any(Registry.class), Mockito.any(String.class)))
                .thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.any(String.class))).thenReturn(null);
        
        // trigger registry object creation
        apiPersistenceInstance.getDevPortalAPI(new Organization(SUPER_TENANT_DOMAIN), "xxxxx");
        Mockito.verify(registryService, times(1)).getGovernanceUserRegistry("admin", SUPER_TENANT_ID);
        
        username = "wso2.anonymous.user";
        apiPersistenceInstance = new RegistryPersistenceImplWrapper(username, tenantManager,
                registryService);
        // trigger registry object creation
        apiPersistenceInstance.getDevPortalAPI(new Organization(SUPER_TENANT_DOMAIN), "xxxxx");
        Mockito.verify(registryService, times(1)).getGovernanceUserRegistry("wso2.anonymous.user", SUPER_TENANT_ID);

    }
    @Test
    public void testRegistrySelectionForTenantUser() throws Exception {

        RegistryService registryService = Mockito.mock(RegistryService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);

        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);

        UserRealm realm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(realm);
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadloadTenantAPIRXT", Mockito.any(String.class),
                Mockito.any(Integer.class));
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadTenantAPIPolicy", Mockito.any(String.class),
                Mockito.any(Integer.class));

        Mockito.when(context.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(context.getTenantId()).thenReturn(TENANT_ID);
        
        // return null artifact because we are not testing artifact related params. this is only to get the registry obj
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(
                RegistryPersistenceUtil.getArtifactManager(Mockito.any(Registry.class), Mockito.any(String.class)))
                .thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.any(String.class))).thenReturn(null);
        
        String username = "admin@wso2.com";
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(username, tenantManager,
                registryService);
        
        // trigger registry object creation
        apiPersistenceInstance.getDevPortalAPI(new Organization(TENANT_DOMAIN), "xxxxx");
        
        Mockito.verify(registryService, times(1)).getGovernanceUserRegistry("admin", TENANT_ID);
        
        username = "wso2.anonymous.user@wso2.com";
        apiPersistenceInstance = new RegistryPersistenceImplWrapper(username, tenantManager,
                registryService);
        // trigger registry object creation
        apiPersistenceInstance.getDevPortalAPI(new Organization(TENANT_DOMAIN), "xxxxx");
        Mockito.verify(registryService, times(1)).getGovernanceUserRegistry("wso2.anonymous.user", TENANT_ID);
        
        
    }
    
    @Test
    public void testRegistrySelectionForTenantUserCrossTenatAccess() throws Exception {

        RegistryService registryService = Mockito.mock(RegistryService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        Mockito.when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(SUPER_TENANT_ID);

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        PowerMockito.mockStatic(RegistryPersistenceUtil.class);

        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);

        UserRealm realm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(realm);
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadloadTenantAPIRXT", Mockito.any(String.class),
                Mockito.any(Integer.class));
        PowerMockito.doNothing().when(RegistryPersistenceUtil.class, "loadTenantAPIPolicy", Mockito.any(String.class),
                Mockito.any(Integer.class));

        Mockito.when(context.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(context.getTenantId()).thenReturn(TENANT_ID);
        
        String username = "test@wso2.com";
        APIPersistence apiPersistenceInstance = new RegistryPersistenceImplWrapper(username, tenantManager,
                registryService);
        
        // return null artifact because we are not testing artifact related params. this is only to get the registry obj
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class); 
        PowerMockito.when(
                RegistryPersistenceUtil.getArtifactManager(Mockito.any(Registry.class), Mockito.any(String.class)))
                .thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.any(String.class))).thenReturn(null);
        
        // trigger registry object creation. access super tenant api
        apiPersistenceInstance.getDevPortalAPI(new Organization(SUPER_TENANT_DOMAIN), "xxxxx");
        // check whether super tenant's system registy is accessed
        Mockito.verify(registryService, times(1)).getGovernanceSystemRegistry((SUPER_TENANT_ID));

    }
}
