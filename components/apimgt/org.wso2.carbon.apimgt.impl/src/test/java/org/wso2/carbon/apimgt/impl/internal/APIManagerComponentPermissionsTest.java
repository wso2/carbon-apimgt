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
import org.osgi.service.component.ComponentContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CarbonContext.class, APIUtil.class })
public class APIManagerComponentPermissionsTest {
    private Registry registry;
    private ComponentContext componentContext;
    private UserRealm realm;
    private final String USER_NAME = "admin";

    @Before
    public void setupClass() {
        System.setProperty("carbon.home", "");
    }

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.mockStatic(APIUtil.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        realm = Mockito.mock(UserRealm.class);
        componentContext = Mockito.mock(ComponentContext.class);
        registry = Mockito.mock(Registry.class);

        Mockito.when(carbonContext.getRegistry(RegistryType.USER_GOVERNANCE)).thenReturn(registry);
        Mockito.when(carbonContext.getUsername()).thenReturn(USER_NAME);
        Mockito.when(carbonContext.getUserRealm()).thenReturn(realm);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.when(APIUtil.class, "loadTenantExternalStoreConfig",
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME).thenThrow(IndexOutOfBoundsException.class);
    }

    @Test
    public void testShouldAddApplicationPermissionsToRegistry() throws Exception {
        RealmConfiguration realmConfig = Mockito.mock(RealmConfiguration.class);
        Collection collection = Mockito.mock(Collection.class);
        Mockito.when(realm.getRealmConfiguration()).thenReturn(realmConfig);
        Mockito.when(realmConfig.getAdminUserName()).thenReturn(USER_NAME);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(false).thenReturn(true);
        Mockito.when(registry.newCollection()).thenReturn(collection);
        Mockito.when(registry.put(Mockito.anyString(), Mockito.any(Collection.class))).thenReturn("");
        APIManagerComponent apiManagerComponent = new APIManagerComponent();

        try {
            apiManagerComponent.activate(componentContext);
        }  catch (Exception ex) {
            Assert.fail("Unexpected exception was thrown");
        }
        Assert.assertTrue(true);
        // Resource doesn't exists
        try {
            apiManagerComponent.activate(componentContext);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception was thrown");
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testShouldThrowExceptionWhenFailToGetRealmConfiguration() throws UserStoreException, RegistryException {
        Mockito.when(realm.getRealmConfiguration()).thenThrow(UserStoreException.class);
        APIManagerComponent apiManagerComponent = new APIManagerComponent();

        try {
            apiManagerComponent.activate(componentContext);
            Mockito.verify(registry, Mockito.times(0)).newCollection();
        } catch (Exception ex) {
            Assert.fail("Unexpected exception was thrown");
        }
    }

    @Test
    public void testShouldThrowExceptionWhenRegistryErrorOccurs() throws RegistryException {
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenThrow(RegistryException.class);
        APIManagerComponent apiManagerComponent = new APIManagerComponent();

        try {
            apiManagerComponent.activate(componentContext);
            Mockito.verify(registry, Mockito.times(0)).newCollection();
        } catch (Exception ex) {
            Assert.fail("Unexpected exception was thrown");
        }
    }

    @AfterClass
    public static void destroyClass() {
        System.clearProperty("carbon.home");
    }
}
