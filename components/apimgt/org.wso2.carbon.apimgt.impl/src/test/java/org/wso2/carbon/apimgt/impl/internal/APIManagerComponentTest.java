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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.util.APIManagerComponentWrapper;
import org.wso2.carbon.apimgt.impl.observers.CommonConfigDeployer;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.FileNotFoundException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIUtil.class, APIManagerComponent.class, ServiceReferenceHolder.class, AuthorizationUtils.class,
        RegistryUtils.class, APIMgtDBUtil.class,
        SQLConstantManagerFactory.class, ApiMgtDAO.class })
public class APIManagerComponentTest {

    @Before
    public void setup() {
        System.setProperty("carbon.home", "");
    }

    @Test
    public void testShouldActivateWhenAllPrerequisitesMet() throws Exception {
        PowerMockito.mockStatic(APIMgtDBUtil.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(AuthorizationUtils.class);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(SQLConstantManagerFactory.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        ComponentContext componentContext = Mockito.mock(ComponentContext.class);
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        AuthorizationManager authManager = Mockito.mock(AuthorizationManager.class);
        Registry registry = Mockito.mock(Registry.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        OutputEventAdapterService adapterService = Mockito.mock(OutputEventAdapterService.class);
        ThrottleProperties throttleProperties = new ThrottleProperties();

        Mockito.doNothing().when(configuration).load(Mockito.anyString());
        Mockito.doNothing().when(authManager)
                .authorizeRole(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(adapterService).create(null);
        Mockito.when(componentContext.getBundleContext()).thenReturn(bundleContext);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(configuration.getFirstProperty(Mockito.anyString())).thenReturn("").thenReturn(null);
        Mockito.when(bundleContext.registerService("", CommonConfigDeployer.class, null)).thenReturn(null);
        Mockito.when(authManager.isRoleAuthorized(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        Mockito.when(serviceReferenceHolder.getOutputEventAdapterService()).thenReturn(adapterService);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(authManager);
        Mockito.when(configuration.getThrottleProperties()).thenReturn(throttleProperties);
        PowerMockito.doNothing().when(APIMgtDBUtil.class, "initialize");
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantExternalStoreConfig", Mockito.anyInt());
        PowerMockito.doNothing().when(AuthorizationUtils.class ,"addAuthorizeRoleListener",
                Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        PowerMockito.doNothing().when(SQLConstantManagerFactory.class, "initializeSQLConstantManager");
        PowerMockito.when(APIUtil.getMountedPath(null, "")).thenReturn("");
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(RegistryUtils.getAbsolutePath(null, null)).thenReturn("");
        PowerMockito.whenNew(APIManagerConfiguration.class).withAnyArguments().thenReturn(configuration);

        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        APIManagerComponent apiManagerComponent = new APIManagerComponentWrapper(registry);
        GatewayArtifactSynchronizerProperties synchronizerProperties = new GatewayArtifactSynchronizerProperties();
        Mockito.when(config.getGatewayArtifactSynchronizerProperties()).thenReturn(synchronizerProperties);
        try {
            apiManagerComponent.activate(componentContext);
        } catch (FileNotFoundException f) {
            // Exception thrown here means that method was continued without the configuration file
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testShouldNotContinueWhenConfigurationUnAvailable() throws Exception {
        PowerMockito.mockStatic(APIUtil.class);
        ComponentContext componentContext = Mockito.mock(ComponentContext.class);
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Registry registry = Mockito.mock(Registry.class);

        Mockito.when(componentContext.getBundleContext()).thenReturn(bundleContext);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(configuration.getFirstProperty(Mockito.anyString())).thenThrow(FileNotFoundException.class);
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantExternalStoreConfig", Mockito.anyInt());

        APIManagerComponent apiManagerComponent = new APIManagerComponentWrapper(registry);
        try {
            apiManagerComponent.activate(componentContext);
        } catch (FileNotFoundException f) {
            // Exception thrown here means that method was continued without the configuration file
            Assert.fail("Should not throw an exception");
        }
    }



    @AfterClass
    public static void destroy() {
        System.clearProperty("carbon.home");
    }
}