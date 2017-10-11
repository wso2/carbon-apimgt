/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.thrift;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.base.ServerConfiguration;

/**
 * ThriftAuthClient test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerConfiguration.class, ServiceReferenceHolder.class, ThriftUtils.class})
public class ThriftUtilsTest {

    private ServerConfiguration serverConfiguration;
    private ServiceReferenceHolder serviceReferenceHolder;
    private APIManagerConfiguration config;
    private String keyValidationURL = "https://localhost:9443/services";
    private String thriftServerHost = "localhost";
    private String thriftClientPort = "10397";
    private String connectionTimeout = "3600";
    private String keyValidatorUsername = "admin";
    private String keyValidatorPassword = "admin";
    private String keyStoreLocation = "repository/resources/security/wso2carbon.jks";
    private String keyStorePassword = "wso2carbon";
    private ThriftAuthClient thriftAuthClient;


    @Before
    public void init() {
        PowerMockito.mockStatic(ServerConfiguration.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        serverConfiguration = Mockito.mock(ServerConfiguration.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        config = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(config);
        PowerMockito.when(ServerConfiguration.getInstance()).thenReturn(serverConfiguration);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Whitebox.setInternalState(ThriftUtils.class, "thriftAuthClient", thriftAuthClient);
        thriftAuthClient = Mockito.mock(ThriftAuthClient.class);


    }

    @Test
    public void testThriftAuthClientInitialisation() throws Exception {

        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn(keyValidationURL);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_THRIFT_CLIENT_PORT)).thenReturn
                (thriftClientPort);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_CONNECTION_TIMEOUT)).thenReturn
                (connectionTimeout);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_MANAGER_THRIFT_SERVER_HOST)).thenReturn
                (thriftServerHost);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn(keyValidatorUsername);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn(keyValidatorPassword);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Location")).thenReturn(keyStoreLocation);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Password")).thenReturn(keyStorePassword);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/");
        PowerMockito.whenNew(ThriftAuthClient.class).withAnyArguments().thenReturn(thriftAuthClient);
        Mockito.when(thriftAuthClient.getSessionId(keyValidatorUsername, keyStorePassword)).thenReturn("12345");
        ThriftUtils.getInstance();
    }

    @Test
    public void testThriftAuthClientInitialisationWithDefaultConfigurations() throws Exception {

        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn(keyValidationURL);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_THRIFT_CLIENT_PORT)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_CONNECTION_TIMEOUT)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_MANAGER_THRIFT_SERVER_HOST)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn(keyValidatorUsername);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn(keyValidatorPassword);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Location")).thenReturn(keyStoreLocation);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Password")).thenReturn(keyStorePassword);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/");
        PowerMockito.whenNew(ThriftAuthClient.class).withAnyArguments().thenReturn(thriftAuthClient);
        Mockito.when(thriftAuthClient.getSessionId(keyValidatorUsername, keyStorePassword)).thenReturn("12345");
        ThriftUtils.getInstance();
    }

    @Test
    public void testThriftAuthClientInitialisationFailureWhenConnectionUsernameNotConfigured() throws Exception {

        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn(keyValidationURL);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_THRIFT_CLIENT_PORT)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_CONNECTION_TIMEOUT)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_MANAGER_THRIFT_SERVER_HOST)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn(keyValidatorPassword);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Location")).thenReturn(keyStoreLocation);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Password")).thenReturn(keyStorePassword);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/");
        PowerMockito.whenNew(ThriftAuthClient.class).withAnyArguments().thenReturn(thriftAuthClient);
        Mockito.when(thriftAuthClient.getSessionId(keyValidatorUsername, keyStorePassword)).thenReturn("12345");
        try {
            ThriftUtils.getInstance();
            Assert.fail("Expected APISecurityException not thrown when connection credentials are not found");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().contains("Required connection details for the thrift key management " +
                    "server not provided"));
        }
    }

    @Test
    public void testThriftAuthClientInitialisationFailureWhenConnectionPasswordNotConfigured() throws Exception {

        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn(keyValidationURL);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_THRIFT_CLIENT_PORT)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_CONNECTION_TIMEOUT)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_MANAGER_THRIFT_SERVER_HOST)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn(keyValidatorUsername);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn(null);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Location")).thenReturn(keyStoreLocation);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Password")).thenReturn(keyStorePassword);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/");
        PowerMockito.whenNew(ThriftAuthClient.class).withAnyArguments().thenReturn(thriftAuthClient);
        Mockito.when(thriftAuthClient.getSessionId(keyValidatorUsername, keyStorePassword)).thenReturn("12345");
        try {
            ThriftUtils.getInstance();
            Assert.fail("Expected APISecurityException not thrown when connection credentials are not found");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().contains("Required connection details for the thrift key management " +
                    "server not provided"));
        }
    }

    @Test(expected = APISecurityException.class)
    public void testThriftAuthClientInitialisationFailureWhenSessionCannotBeObtained() throws Exception {

        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn(keyValidationURL);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_THRIFT_CLIENT_PORT)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_CONNECTION_TIMEOUT)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_MANAGER_THRIFT_SERVER_HOST)).thenReturn(null);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn(keyValidatorUsername);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn(keyValidatorPassword);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Location")).thenReturn(keyStoreLocation);
        Mockito.when(serverConfiguration.getFirstProperty("Security.KeyStore.Password")).thenReturn(keyStorePassword);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/");
        PowerMockito.whenNew(ThriftAuthClient.class).withAnyArguments().thenReturn(thriftAuthClient);
        Mockito.doThrow(AuthenticationException.class).when(thriftAuthClient).getSessionId(Mockito.anyString(),
                Mockito.anyString());
        ThriftUtils.getInstance();
        Assert.fail("Expected APISecurityException is not thrown when session retrieval fails");
    }
}
