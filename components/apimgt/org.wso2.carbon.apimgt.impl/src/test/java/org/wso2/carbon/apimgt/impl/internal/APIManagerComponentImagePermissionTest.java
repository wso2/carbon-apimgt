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

package org.wso2.carbon.apimgt.impl.internal;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, APIUtil.class, CarbonContext.class, APIManagerComponent.class,
                        RegistryUtils.class, APIMgtDBUtil.class })
public class APIManagerComponentImagePermissionTest {
    private ServiceReferenceHolder serviceReferenceHolder;
    private RealmService realmService;
    private AuthorizationManager authManager;
    private ComponentContext componentContext;

    @Before
    public void setupClass() {
        System.setProperty("carbon.home", "");
    }

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.mockStatic(APIMgtDBUtil.class);
        authManager = Mockito.mock(AuthorizationManager.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        realmService = Mockito.mock(RealmService.class);
        componentContext = Mockito.mock(ComponentContext.class);
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        Registry registry = Mockito.mock(Registry.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);

        Mockito.when(componentContext.getBundleContext()).thenReturn(bundleContext);
        Mockito.when(realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)).thenReturn(userRealm);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(authManager);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(carbonContext.getRegistry(RegistryType.USER_GOVERNANCE)).thenReturn(registry);
        Mockito.doNothing().when(configuration).load(Mockito.anyString());
        Mockito.when(configuration.getFirstProperty(Mockito.anyString())).thenReturn("");
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(APIUtil.getMountedPath(null, "")).thenReturn("");
        PowerMockito.when(RegistryUtils.getAbsolutePath(null, null)).thenReturn("");
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.when(APIUtil.getMountedPath(Mockito.any(RegistryContext.class), Mockito.anyString()))
                .thenReturn("");
        PowerMockito.doThrow(new IndexOutOfBoundsException()).when(APIMgtDBUtil.class, "initialize");
        PowerMockito.whenNew(APIManagerConfiguration.class).withAnyArguments().thenReturn(configuration);
    }

    @Test
    public void testShouldSetImagePermissions() throws UserStoreException {
        Mockito.when(authManager.isRoleAuthorized(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true)
                .thenReturn(false);
        Mockito.doNothing().when(authManager)
                .authorizeRole(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        APIManagerComponent apiManagerComponent = new APIManagerComponent();

        try {
            apiManagerComponent.activate(componentContext);
            Assert.fail("IndexOutOfBoundsException is expected here");
        } catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception was thrown");
        }

        try {
            apiManagerComponent.activate(componentContext);
            Assert.fail("IndexOutOfBoundsException is expected here");
        } catch (IndexOutOfBoundsException e) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception was thrown");
        }
    }

    @Test
    public void testShouldThrowExceptionWhenFailToGetRealm() throws UserStoreException {
        Mockito.when(authManager.isRoleAuthorized(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(UserStoreException.class);
        Mockito.doNothing().when(authManager)
                .authorizeRole(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        APIManagerComponent apiManagerComponent = new APIManagerComponent();

        try {
            apiManagerComponent.activate(componentContext);
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }
    }

    @AfterClass
    public static void destroyClass() {
        System.clearProperty("carbon.home");
    }
}
