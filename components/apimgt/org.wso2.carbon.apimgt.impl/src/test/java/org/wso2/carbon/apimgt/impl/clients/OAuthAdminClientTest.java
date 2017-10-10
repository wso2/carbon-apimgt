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
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.clients.util.OAuthAdminClientWrapper;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Util.class, KeyManagerHolder.class, ConfigurationContextFactory.class })
public class OAuthAdminClientTest {
    private KeyManager km;
    private KeyManagerConfiguration kmConfig;
    private ConfigurationContextFactory configFactory;
    private ConfigurationContext configurationContext;
    private OAuthAdminServiceStub serviceStub;
    private ServiceClient serviceClient;
    private final String USERNAME = "admin";
    private final String CONSUMER_KEY = "1234-abcd";

    @Before
    public void setup() {
        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.mockStatic(ConfigurationContextFactory.class);
        km = Mockito.mock(KeyManager.class);
        kmConfig = Mockito.mock(KeyManagerConfiguration.class);
        configFactory = Mockito.mock(ConfigurationContextFactory.class);
        configurationContext = Mockito.mock(ConfigurationContext.class);
        serviceStub = Mockito.mock(OAuthAdminServiceStub.class);
        serviceClient = Mockito.mock(ServiceClient.class);

        Mockito.when(serviceClient.getOptions()).thenReturn(new Options());
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance()).thenReturn(km);
    }

    @Test
    public void testShouldInitializeClient() throws Exception {
        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("");
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);

        OAuthAdminClientWrapper.setServiceStub(serviceStub);
        new OAuthAdminClientWrapper();
    }

    @Test
    public void testShouldThrowExceptionWhenErrorOccurs() throws AxisFault, APIManagementException {
        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("").thenReturn(null);
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenThrow(AxisFault.class);
        OAuthAdminClientWrapper.setServiceStub(serviceStub);
        try {
            new OAuthAdminClientWrapper();
            Assert.fail();
        } catch (APIManagementException e) {
            // APIManagementException is expected here
        }

        try {
            new OAuthAdminClientWrapper();
            Assert.fail();
        } catch (APIManagementException e) {
            // APIManagementException is expected here
        }
    }

    @Test
    public void testShouldRetrieveOAuthApplicationData() throws Exception {
        OAuthConsumerAppDTO dto = new OAuthConsumerAppDTO();
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.getOAuthApplicationData(Mockito.anyString())).thenReturn(dto);
        Mockito.when(serviceStub.getOAuthApplicationDataByAppName(Mockito.anyString())).thenReturn(dto);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("");
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        OAuthAdminClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, "setAuthHeaders", serviceClient, USERNAME);

        OAuthAdminClientWrapper client = new OAuthAdminClientWrapper();
        OAuthConsumerAppDTO appData = client.getOAuthApplicationData(CONSUMER_KEY, USERNAME);

        if (appData == null) {
            Assert.fail("Application DTO cannot be empty.");
        }


        OAuthConsumerAppDTO appDataByName = client.getOAuthApplicationDataByAppName(CONSUMER_KEY, USERNAME);
        if (appDataByName == null) {
            Assert.fail("Application DTO cannot be empty.");
        }
    }

    @Test
    public void testShouldRegisterOAuthApplicationData() throws Exception {
        OAuthConsumerAppDTO dto = new OAuthConsumerAppDTO();
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.doNothing().when(serviceStub).registerOAuthApplicationData(dto);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("");
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        OAuthAdminClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, "setAuthHeaders", serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        OAuthAdminClientWrapper client = new OAuthAdminClientWrapper();
        client.registerOAuthApplicationData(dto, USERNAME);
    }


    @Test
    public void testShouldRemoveOAuthApplicationData() throws Exception {
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.doNothing().when(serviceStub).removeOAuthApplicationData(CONSUMER_KEY);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("");
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        OAuthAdminClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, "setAuthHeaders", serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        OAuthAdminClientWrapper client = new OAuthAdminClientWrapper();
        client.removeOAuthApplicationData(CONSUMER_KEY, USERNAME);
    }


    @Test
    public void testShouldUpdateOAuthApplicationData() throws Exception {
        OAuthConsumerAppDTO dto = new OAuthConsumerAppDTO();
        PowerMockito.mockStatic(Util.class);

        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.doNothing().when(serviceStub).updateConsumerApplication(dto);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("");
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);
        OAuthAdminClientWrapper.setServiceStub(serviceStub);
        PowerMockito.doNothing().when(Util.class, "setAuthHeaders", serviceClient, USERNAME);

        // Test is successful if there are no exceptions thrown
        OAuthAdminClientWrapper client = new OAuthAdminClientWrapper();
        client.updateOAuthApplicationData(dto, USERNAME);
    }
}
