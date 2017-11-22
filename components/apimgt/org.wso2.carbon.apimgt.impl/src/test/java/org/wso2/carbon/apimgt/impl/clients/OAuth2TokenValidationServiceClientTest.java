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
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.clients.util.OAuth2TokenValidationServiceClientWrapper;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;

import java.rmi.RemoteException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Util.class, KeyManagerHolder.class, ConfigurationContextFactory.class })
public class OAuth2TokenValidationServiceClientTest {
    private KeyManager km;
    private KeyManagerConfiguration kmConfig;
    private ConfigurationContextFactory configFactory;
    private ConfigurationContext configurationContext;
    private OAuth2TokenValidationServiceStub serviceStub;
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
        serviceStub = Mockito.mock(OAuth2TokenValidationServiceStub.class);
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

        OAuth2TokenValidationServiceClientWrapper.setServiceStub(serviceStub);
        new OAuth2TokenValidationServiceClientWrapper();
    }

    @Test
    public void testShouldThrowExceptionWhenErrorOccurs() throws AxisFault, APIManagementException {
        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("").thenReturn(null);
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenThrow(AxisFault.class);
        OAuth2TokenValidationServiceClientWrapper.setServiceStub(serviceStub);
        try {
            new OAuth2TokenValidationServiceClientWrapper();
            Assert.fail();
        } catch (APIManagementException e) {
            // APIManagementException is expected here
        }

        try {
            new OAuth2TokenValidationServiceClientWrapper();
            Assert.fail();
        } catch (APIManagementException e) {
            // APIManagementException is expected here
        }
    }

    @Test
    public void testShouldValidateAuthenticationRequest() throws RemoteException, APIManagementException {
        OAuth2ClientApplicationDTO clientAppDTO = new OAuth2ClientApplicationDTO();
        OperationContext opContext = Mockito.mock(OperationContext.class);
        Mockito.when(serviceClient.getLastOperationContext()).thenReturn(opContext);
        Mockito.when(opContext.getServiceContext()).thenReturn(new ServiceContext());
        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.findOAuthConsumerIfTokenIsValid(Mockito.any(OAuth2TokenValidationRequestDTO.class)))
                .thenReturn(clientAppDTO);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("");
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);

        OAuth2TokenValidationServiceClientWrapper.setServiceStub(serviceStub);
        OAuth2TokenValidationServiceClientWrapper client = new OAuth2TokenValidationServiceClientWrapper();
        client.validateAuthenticationRequest("");
    }

    @Test(expected = APIManagementException.class)
    public void testShouldThrowExceptionWhenOAuthRequestValitionFails() throws RemoteException, APIManagementException {
        OAuth2ClientApplicationDTO clientAppDTO = new OAuth2ClientApplicationDTO();
        OperationContext opContext = Mockito.mock(OperationContext.class);
        Mockito.when(serviceClient.getLastOperationContext()).thenReturn(opContext);
        Mockito.when(opContext.getServiceContext()).thenReturn(new ServiceContext());
        Mockito.when(serviceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceStub.findOAuthConsumerIfTokenIsValid(Mockito.any(OAuth2TokenValidationRequestDTO.class)))
                .thenThrow(Exception.class);
        Mockito.when(kmConfig.getParameter(Mockito.anyString())).thenReturn("");
        Mockito.when(km.getKeyManagerConfiguration()).thenReturn(kmConfig);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext);

        OAuth2TokenValidationServiceClientWrapper.setServiceStub(serviceStub);
        OAuth2TokenValidationServiceClientWrapper client = new OAuth2TokenValidationServiceClientWrapper();
        client.validateAuthenticationRequest("");
    }

}
