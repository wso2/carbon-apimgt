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
package org.wso2.carbon.apimgt.impl.utils;

import junit.framework.Assert;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.RemoteAuthorizationManagerClient;
import org.wso2.carbon.um.ws.api.stub.RemoteAuthorizationManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, RemoteAuthorizationManagerServiceStub.class,
        RemoteUserStoreManagerServiceStub.class, RemoteAuthorizationManagerClient.class })
public class RemoteAuthorizationManagerClientTest {

    private final String SESSION_COOKIE = "abcd-efgh";
    private final String USER = "john";
    private final String PASSWORD = "john123";
    private final String SERVICE_URL = "https://test.com";
    private final String USER_WRONG = "smith";
    private final String PERMISSION = "permission";
    private final String[] ROLE_LIST = {"subscriber"};
    private final String[] ROLE_NAMES = {"admin", "subscriber", "publisher"};
    private ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
    private OperationContext operationContext = Mockito.mock(OperationContext.class);
    private ServiceContext serviceContext = Mockito.mock(ServiceContext.class);
    private Options options = Mockito.mock(Options.class);
    private APIManagerConfigurationService apiManagerConfigurationService = Mockito
            .mock(APIManagerConfigurationService.class);
    private APIManagerConfiguration apiManagerConfiguration =  Mockito.mock(APIManagerConfiguration.class);
    private RemoteAuthorizationManagerServiceStub authorizationManager = Mockito
            .mock(RemoteAuthorizationManagerServiceStub.class);
    private RemoteUserStoreManagerServiceStub userStoreManager = Mockito
            .mock(RemoteUserStoreManagerServiceStub.class);
    private RemoteAuthorizationManagerClient remoteAuthorizationManagerClient;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_URL)).thenReturn(SERVICE_URL);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_USERNAME)).thenReturn(USER);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_PASSWORD)).thenReturn(PASSWORD);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(userStoreManager.getRoleNames()).thenReturn(ROLE_NAMES);
        Mockito.when(userStoreManager.getRoleListOfUser(USER)).thenReturn(ROLE_LIST);
        Mockito.when(
                authorizationManager.isUserAuthorized(USER_WRONG, PERMISSION, CarbonConstants.UI_PERMISSION_ACTION))
                .thenReturn(false);
        Mockito.when(authorizationManager.isUserAuthorized(USER, PERMISSION, CarbonConstants.UI_PERMISSION_ACTION))
                .thenReturn(true);
        Mockito.when(serviceContext.getProperty(HTTPConstants.COOKIE_STRING)).thenReturn(SESSION_COOKIE);
        Mockito.when(operationContext.getServiceContext()).thenReturn(serviceContext);
        Mockito.when(serviceClient.getOptions()).thenReturn(options);
        Mockito.when(serviceClient.getLastOperationContext()).thenReturn(operationContext);
        Mockito.when(authorizationManager._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(userStoreManager._getServiceClient()).thenReturn(serviceClient);
        PowerMockito.whenNew(RemoteUserStoreManagerServiceStub.class).withAnyArguments().thenReturn(userStoreManager);
        PowerMockito.whenNew(RemoteAuthorizationManagerServiceStub.class).withAnyArguments()
                .thenReturn(authorizationManager);
    }

    @Test
    public void testValidIsUserAuthorized() throws Exception {
        remoteAuthorizationManagerClient = new RemoteAuthorizationManagerClient();
        boolean status = remoteAuthorizationManagerClient.isUserAuthorized(USER, PERMISSION);
        Assert.assertTrue(status);
    }

    @Test
    public void testInValidIsUserAuthorized() throws Exception {
        remoteAuthorizationManagerClient = new RemoteAuthorizationManagerClient();
        boolean status = remoteAuthorizationManagerClient.isUserAuthorized(USER_WRONG, PERMISSION);
        Assert.assertFalse(status);
    }

    @Test
    public void testGetRolesOfUser() throws Exception {
        remoteAuthorizationManagerClient = new RemoteAuthorizationManagerClient();
        String[] roleList = remoteAuthorizationManagerClient.getRolesOfUser(USER);
        Assert.assertEquals("subscriber", roleList[0]);
    }

    @Test
    public void testGetRoleNames() throws Exception {
        remoteAuthorizationManagerClient = new RemoteAuthorizationManagerClient();
        String[] roleList = remoteAuthorizationManagerClient.getRoleNames();
        Assert.assertEquals(ROLE_NAMES[0], roleList[0]);
        Assert.assertEquals(ROLE_NAMES[1], roleList[1]);
        Assert.assertEquals(ROLE_NAMES[2], roleList[2]);
    }
}
