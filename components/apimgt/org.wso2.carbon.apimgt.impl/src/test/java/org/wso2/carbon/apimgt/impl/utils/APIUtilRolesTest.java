/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.config.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, ServiceReferenceHolder.class, APIManagerComponent.class})
@PowerMockIgnore("org.w3c.dom.*")
public class APIUtilRolesTest {

    @Test
    public void testCreateDefaultRoles() throws Exception {
        System.setProperty("carbon.home", APIUtilRolesTest.class.getResource("/").getFile());
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            final int tenantId = MultitenantConstants.SUPER_TENANT_ID;
            final String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

            File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                    getResource("tenant-conf.json").getFile());
            String tenantConfValue = FileUtils.readFileToString(siteConfFile);
            InputStream signUpConfStream = new FileInputStream(Thread.currentThread().getContextClassLoader().
                    getResource("default-sign-up-config.xml").getFile());
            ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
            RealmService realmService = Mockito.mock(RealmService.class);
            RegistryService registryService = Mockito.mock(RegistryService.class);
            TenantManager tenantManager = Mockito.mock(TenantManager.class);
            TenantIndexingLoader indexingLoader = Mockito.mock(TenantIndexingLoader.class);
            UserRealm userRealm = Mockito.mock(UserRealm.class);
            UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
            RealmConfiguration realmConfiguration = Mockito.mock(RealmConfiguration.class);
            APIMConfigService apimConfigService = Mockito.mock(APIMConfigService.class);
            PowerMockito.mockStatic(PrivilegedCarbonContext.class);
            PowerMockito.mockStatic(ServiceReferenceHolder.class);
            PowerMockito.mockStatic(APIManagerComponent.class);

            Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
            Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
            Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
            Mockito.when(serviceReferenceHolder.getIndexLoaderService()).thenReturn(indexingLoader);
            Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
            Mockito.when(realmService.getBootstrapRealm()).thenReturn(userRealm);
            Mockito.when(realmService.getTenantUserRealm(tenantId)).thenReturn(userRealm);
            Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
            Mockito.when(userRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
            Mockito.when(realmConfiguration.getAdminUserName()).thenReturn("admin");
            Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantId);
            Mockito.when(tenantManager.getDomain(tenantId)).thenReturn(tenantDomain);
            Mockito.when(serviceReferenceHolder.getApimConfigService()).thenReturn(apimConfigService);
            Mockito.when(apimConfigService.getTenantConfig(tenantDomain)).thenReturn(tenantConfValue);
            Mockito.when(apimConfigService.getSelfSighupConfig(tenantDomain)).thenReturn(IOUtils.toString(signUpConfStream));
            APIUtil.createDefaultRoles(tenantId);

            String[] adminName = {"admin"};
            Mockito.verify(userStoreManager, Mockito.atLeastOnce()).addRole(eq("Internal/publisher"),
                    eq(adminName), new Permission[]{Mockito.any(Permission.class)});
            Mockito.verify(userStoreManager, Mockito.atLeastOnce()).addRole(eq("Internal/subscriber"),
                    eq(adminName), new Permission[]{Mockito.any(Permission.class)});
            Mockito.verify(userStoreManager, Mockito.atLeastOnce()).addRole(eq("Internal/creator"),
                    eq(adminName), new Permission[]{Mockito.any(Permission.class)});

        }finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }
}
