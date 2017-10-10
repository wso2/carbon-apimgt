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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.clients.util.ApplicationManagementServiceClientWrapper;
import org.wso2.carbon.apimgt.impl.clients.util.OAuth2TokenValidationServiceClientWrapper;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;

import java.rmi.RemoteException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Util.class, ServiceReferenceHolder.class, ConfigurationContextFactory.class })
public class ApplicationManagementServiceClientTest {
    private APIManagerConfiguration amConfig;
    private ConfigurationContextFactory configFactory;
    private ConfigurationContext configurationContext;
    private IdentityApplicationManagementServiceStub serviceStub;
    private ServiceClient serviceClient;
    private final String USERNAME = "admin";
    private final String SET_AUTHHEADER = "setAuthHeaders";
    private final String APP_NAME = "app";

    @Before
    public void setup() {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ConfigurationContextFactory.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        amConfig = Mockito.mock(APIManagerConfiguration.class);
        configFactory = Mockito.mock(ConfigurationContextFactory.class);
        configurationContext = Mockito.mock(ConfigurationContext.class);
        serviceStub = Mockito.mock(IdentityApplicationManagementServiceStub.class);
        serviceClient = Mockito.mock(ServiceClient.class);

        Mockito.when(serviceClient.getOptions()).thenReturn(new Options());
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        Mockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
    }

    @Test
    public void testShouldInitializeClient() throws Exception {
        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);

        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        new ApplicationManagementServiceClientWrapper();
    }

    @Test
    public void testShouldThrowExceptionWhenErrorOccursWhileInitializing() throws AxisFault, APIManagementException {
        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("").thenReturn(null);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenThrow(AxisFault.class).thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        try {
            new ApplicationManagementServiceClientWrapper();
            Assert.fail();
        } catch (APIManagementException e) {
            // APIManagementException is expected here
        }

        try {
            new ApplicationManagementServiceClientWrapper();
            Assert.fail();
        } catch (APIManagementException e) {
            // APIManagementException is expected here
        }
    }

    @Test
    public void testShouldCreateApplication() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.doNothing()
                .doThrow(RemoteException.class)
                .doThrow(IdentityApplicationManagementServiceIdentityApplicationManagementException.class)
                .when(serviceStub).createApplication(serviceProvider);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.createApplication(serviceProvider, USERNAME);

        try {
            client.createApplication(serviceProvider, USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }

        try {
            client.createApplication(serviceProvider, USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldRetrieveApplication() throws Exception {
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.getApplication(APP_NAME))
                .thenReturn(new ServiceProvider())
                .thenThrow(RemoteException.class);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.getApplication(APP_NAME, USERNAME);

        try {
            client.getApplication(APP_NAME, USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldRetrieveAllApplicationBasicInfo() throws Exception {
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.getAllApplicationBasicInfo())
                .thenReturn(new ApplicationBasicInfo[0])
                .thenThrow(RemoteException.class)
                .thenThrow(IdentityApplicationManagementServiceIdentityApplicationManagementException.class);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.getAllApplicationBasicInfo(USERNAME);

        try {
            client.getAllApplicationBasicInfo(USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }

        try {
            client.getAllApplicationBasicInfo(USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldUpdateApplicationData() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.doNothing()
                .doThrow(RemoteException.class)
                .doThrow(IdentityApplicationManagementServiceIdentityApplicationManagementException.class)
                .when(serviceStub).updateApplication(serviceProvider);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.updateApplicationData(serviceProvider, USERNAME);

        try {
            client.updateApplicationData(serviceProvider, USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }

        try {
            client.updateApplicationData(serviceProvider, USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldDeleteApplication() throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.doNothing()
                .doThrow(RemoteException.class)
                .doThrow(IdentityApplicationManagementServiceIdentityApplicationManagementException.class)
                .when(serviceStub).deleteApplication(APP_NAME);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.deleteApplication(APP_NAME, USERNAME);

        try {
            client.deleteApplication(APP_NAME, USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }

        try {
            client.deleteApplication(APP_NAME, USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldRetrieveFederatedIdentityProvider() throws Exception {
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.getIdentityProvider(USERNAME))
                .thenReturn(new IdentityProvider())
                .thenThrow(RemoteException.class);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.getFederatedIdentityProvider(USERNAME, USERNAME);

        try {
            client.getFederatedIdentityProvider(USERNAME, USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldRetrieveAllRequestPathAuthenticators() throws Exception {
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.getAllRequestPathAuthenticators())
                .thenReturn(new RequestPathAuthenticatorConfig[0])
                .thenThrow(RemoteException.class);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.getAllRequestPathAuthenticators(USERNAME);

        try {
            client.getAllRequestPathAuthenticators(USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldRetrieveAllLocalAuthenticators() throws Exception {
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.getAllLocalAuthenticators())
                .thenReturn(new LocalAuthenticatorConfig[0])
                .thenThrow(RemoteException.class);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.getAllLocalAuthenticators(USERNAME);

        try {
            client.getAllLocalAuthenticators(USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldRetrieveAllFederatedIdentityProvider() throws Exception {
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.getAllIdentityProviders())
                .thenReturn(new IdentityProvider[0])
                .thenThrow(RemoteException.class);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.getAllFederatedIdentityProvider(USERNAME);

        try {
            client.getAllFederatedIdentityProvider(USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

    @Test
    public void testShouldRetrieveAllClaimUris() throws Exception {
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.getAllLocalClaimUris())
                .thenReturn(new String[0])
                .thenThrow(RemoteException.class);
        Mockito.when(amConfig.getFirstProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        ApplicationManagementServiceClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, SET_AUTHHEADER, serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        ApplicationManagementServiceClientWrapper client = new ApplicationManagementServiceClientWrapper();
        client.getAllClaimUris(USERNAME);

        try {
            client.getAllClaimUris(USERNAME);
            Assert.fail("Exception is expected.");
        } catch (Exception e) {
            // Exception is expected here
        }
    }

}
