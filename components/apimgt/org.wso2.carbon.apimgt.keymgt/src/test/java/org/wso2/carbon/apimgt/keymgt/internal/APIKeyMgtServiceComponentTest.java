/*
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
 */

package org.wso2.carbon.apimgt.keymgt.internal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.keymgt.handlers.DefaultKeyValidationHandler;
import org.wso2.carbon.apimgt.keymgt.issuers.AbstractScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.listeners.KeyManagerUserOperationListener;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.net.InetAddress;
import java.util.Dictionary;

@RunWith(PowerMockRunner.class) @PrepareForTest({ APIKeyMgtDataHolder.class, ServiceReferenceHolder.class,
        ApiMgtDAO.class, ServiceRegistration.class, ComponentContext.class,
        org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class })
public class APIKeyMgtServiceComponentTest {
    private APIKeyMgtDataHolder apiKeyMgtDataHolder;
    private ServiceReferenceHolder serviceReferenceHolder;
    private APIManagerConfigurationService apiManagerConfigurationService;
    private ApiMgtDAO apiMgtDAO;
    private APIManagerConfiguration apiManagerConfiguration;
    private ComponentContext ctxt;
    private org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder serviceReferenceHolder1;
    private ServiceRegistration serviceRegistration;

    @Before
    public void Init() throws Exception {
        serviceReferenceHolder = PowerMockito.mock(ServiceReferenceHolder.class);
        apiManagerConfigurationService = PowerMockito.mock(APIManagerConfigurationService.class);
        apiManagerConfiguration = PowerMockito.mock(APIManagerConfiguration.class);
        apiMgtDAO = PowerMockito.mock(ApiMgtDAO.class);
        apiKeyMgtDataHolder = PowerMockito.mock(APIKeyMgtDataHolder.class);
        ctxt = PowerMockito.mock(ComponentContext.class);
        serviceRegistration = PowerMockito.mock(ServiceRegistration.class);
        serviceReferenceHolder1 = PowerMockito.mock(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class);
        BundleContext bundleContext = PowerMockito.mock(BundleContext.class);

        PowerMockito.mockStatic(ServiceRegistration.class);
        PowerMockito.mockStatic(ComponentContext.class);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        PowerMockito.mockStatic(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        PowerMockito.when(apiManagerConfigurationService.getAPIManagerConfiguration())
                .thenReturn(apiManagerConfiguration);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        PowerMockito.when(apiManagerConfiguration
                .getFirstProperty(APIConstants.API_KEY_MANGER_VALIDATIONHANDLER_CLASS_NAME))
                .thenReturn(DefaultKeyValidationHandler.class.getName());
        PowerMockito.when(APIKeyMgtDataHolder.getAmConfigService()).thenReturn(apiManagerConfigurationService);
        PowerMockito.when(ctxt.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext
                .registerService(Mockito.anyString(), Mockito.any(KeyManagerUserOperationListener.class),
                        Mockito.any(Dictionary.class))).thenReturn(serviceRegistration);
        PowerMockito.when(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance())
                .thenReturn(serviceReferenceHolder1);
        PowerMockito.when(serviceReferenceHolder1.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        PowerMockito.when(apiManagerConfiguration.getProperty(APIConstants.WHITELISTED_SCOPES)).thenReturn(null);
    }

    @Test
    public void testSetRegistryService() throws Exception {
        RegistryService registryService = Mockito.mock(RegistryService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.setRegistryService(registryService);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce())
                .setRegistryService(Mockito.any(RegistryService.class));
    }

    @Test
    public void testUnsetRegistryService() throws Exception {
        RegistryService registryService = Mockito.mock(RegistryService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.unsetRegistryService(registryService);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce()).setRegistryService(null);
    }

    @Test
    public void testSetRealmService() throws Exception {

        RealmService realmService = Mockito.mock(RealmService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.setRealmService(realmService);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce()).setRealmService(Mockito.any(RealmService.class));

    }

    @Test
    public void testUnsetRealmService() throws Exception {

        RealmService realmService = Mockito.mock(RealmService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.unsetRealmService(realmService);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce()).setRealmService(null);
    }

    @Test
    public void testSetAPIManagerConfigurationService() throws Exception {

        APIManagerConfigurationService amConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.setAPIManagerConfigurationService(amConfigurationService);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce())
                .setAmConfigService(Mockito.any(APIManagerConfigurationService.class));

    }

    @Test
    public void testUnSetAPIManagerConfigurationService() throws Exception {

        APIManagerConfigurationService amConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.unsetAPIManagerConfigurationService(amConfigurationService);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce()).setAmConfigService(null);
    }

    @Test
    public void testAddScopeIssuer() throws Exception {

        AbstractScopesIssuer abstractScopesIssuer = Mockito.mock(AbstractScopesIssuer.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.addScopeIssuer(abstractScopesIssuer);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce())
                .addScopesIssuer(Mockito.anyString(), Mockito.any(AbstractScopesIssuer.class));
    }

    @Test
    public void testRemoveScopeIssuer() throws Exception {

        AbstractScopesIssuer abstractScopesIssuer = Mockito.mock(AbstractScopesIssuer.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.removeScopeIssuers(abstractScopesIssuer);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce()).setScopesIssuers(null);
    }

    @Test
    public void testActivate() throws Exception {

        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.activate(ctxt);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce())
                .addScopesIssuer(Mockito.anyString(), Mockito.any(AbstractScopesIssuer.class));
    }


    @Test
    public void testActivateCaseNullConfigurationService() throws Exception {
        PowerMockito.when(serviceReferenceHolder1.getAPIManagerConfigurationService()).thenReturn(null);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.activate(ctxt);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce())
                .addScopesIssuer(Mockito.anyString(), Mockito.any(AbstractScopesIssuer.class));
    }

    @Test
    public void testActivateCaseNullAmConfig() throws Exception {
        PowerMockito.when(APIKeyMgtDataHolder.getAmConfigService()).thenReturn(null);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.activate(ctxt);
        Mockito.verify(apiKeyMgtDataHolder, Mockito.atLeastOnce())
                .addScopesIssuer(Mockito.anyString(), Mockito.any(AbstractScopesIssuer.class));
    }


    @Test
    public void testDeactivate() throws Exception {
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.activate(ctxt);
        apiKeyMgtServiceComponent.deactivate(ctxt);
        Mockito.verify(serviceRegistration, Mockito.atLeastOnce()).unregister();
    }
}