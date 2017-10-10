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

package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.clients.util.TierCacheInvalidationClientWrapper;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.tier.cache.stub.TierCacheServiceStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;

import java.rmi.RemoteException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, ConfigurationContextFactory.class })
public class TierCacheInvalidationClientTest {
    private APIManagerConfiguration amConfig;
    private ConfigurationContextFactory configFactory;
    private ConfigurationContext configurationContext;
    private ServiceClient serviceClient;
    private AuthenticationAdminStub authStub;
    private TierCacheServiceStub cacheStub;
    private final String STORE_URL = "https://localhost/store";
    private final String USERNAME = "admin";
    private final String TENANT_DOMAIN = "carbon.super";

    @Before
    public void setup() throws APIManagementException, RemoteException {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ConfigurationContextFactory.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        amConfig = Mockito.mock(APIManagerConfiguration.class);
        configFactory = Mockito.mock(ConfigurationContextFactory.class);
        configurationContext = Mockito.mock(ConfigurationContext.class);
        serviceClient = Mockito.mock(ServiceClient.class);
        authStub = Mockito.mock(AuthenticationAdminStub.class);
        cacheStub = Mockito.mock(TierCacheServiceStub.class);
        OperationContext opContext = Mockito.mock(OperationContext.class);
        ServiceContext serviceContext = Mockito.mock(ServiceContext.class);

        Mockito.when(opContext.getServiceContext()).thenReturn(serviceContext);
        Mockito.when(serviceContext.getProperty(Mockito.anyString())).thenReturn("cookie");
        Mockito.when(authStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(cacheStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceClient.getLastOperationContext()).thenReturn(opContext);
        Mockito.when(serviceClient.getOptions()).thenReturn(new Options());
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        Mockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
    }

    @Test
    public void testShouldClearCachesWhenTenantDomainIsProvided()
            throws RemoteException, APIManagementException, LoginAuthenticationExceptionException {
        Mockito.when(authStub.login(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString()))
                .thenReturn(STORE_URL)
                .thenReturn(USERNAME)
                .thenReturn(USERNAME);

        new TierCacheInvalidationClientWrapper(authStub, cacheStub).clearCaches(TENANT_DOMAIN);
    }

    @Test
    public void testShouldNotClearCachesWhenParametersIncorrect() throws APIManagementException, RemoteException {
        Mockito.doThrow(Exception.class).when(cacheStub).invalidateCache(TENANT_DOMAIN);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString()))
                .thenReturn(null)
                .thenReturn(USERNAME)
                .thenReturn(USERNAME)
                .thenReturn("")
                .thenReturn(USERNAME)
                .thenReturn(USERNAME)
                .thenReturn(STORE_URL);

        // check init parameter null check ex: serverURL
        try {
            new TierCacheInvalidationClientWrapper(authStub, cacheStub).clearCaches(TENANT_DOMAIN);
        } catch (Exception e) {
            Assert.fail("Should not throw an exception here");
        }

        // Test malformed url check
        try {
            new TierCacheInvalidationClientWrapper(authStub, cacheStub).clearCaches(TENANT_DOMAIN);
        } catch (Exception e) {
            Assert.fail("Should not throw an exception here");
        }
    }

    @Test
    public void testShouldNotClearCachesWhenStubErrorOccurs()
            throws RemoteException, APIManagementException, LoginAuthenticationExceptionException {
        Mockito.when(authStub.login(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(RemoteException.class)
                .thenThrow(LoginAuthenticationExceptionException.class)
                .thenReturn(true);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString()))
                .thenReturn(STORE_URL)
                .thenReturn(USERNAME)
                .thenReturn(USERNAME)
                .thenReturn(STORE_URL)
                .thenReturn(USERNAME)
                .thenReturn(USERNAME)
                .thenReturn(STORE_URL)
                .thenReturn(USERNAME)
                .thenReturn(USERNAME);
        Mockito.doThrow(RemoteException.class).when(cacheStub).invalidateCache(TENANT_DOMAIN);

        // check remote exception
        try {
            new TierCacheInvalidationClientWrapper(authStub, cacheStub).clearCaches(TENANT_DOMAIN);
        } catch (Exception e) {
            Assert.fail("Should not throw an exception here");
        }

        // check login exception
        try {
            new TierCacheInvalidationClientWrapper(authStub, cacheStub).clearCaches(TENANT_DOMAIN);
        } catch (Exception e) {
            Assert.fail("Should not throw an exception here");
        }

        // check remote exception
        try {
            new TierCacheInvalidationClientWrapper(authStub, cacheStub).clearCaches(TENANT_DOMAIN);
        } catch (Exception e) {
            Assert.fail("Should not throw an exception here");
        }
    }
}
