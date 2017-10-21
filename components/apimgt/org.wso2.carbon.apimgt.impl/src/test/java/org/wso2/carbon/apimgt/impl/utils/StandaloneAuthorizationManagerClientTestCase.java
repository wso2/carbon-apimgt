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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.ServiceReferenceHolderMockCreator;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.StandaloneAuthorizationManagerClient;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceReferenceHolder.class)
public class StandaloneAuthorizationManagerClientTestCase {

    private ServiceReferenceHolder serviceReferenceHolder;
    private UserRealm userRealm= Mockito.mock(UserRealm.class);
    private UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
    private AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);

    @Before
    public void setup() throws Exception{
        ServiceReferenceHolderMockCreator serviceReferenceHolderMockCreator = new ServiceReferenceHolderMockCreator(4444);
        serviceReferenceHolder = serviceReferenceHolderMockCreator.getMock();
        Mockito.when(serviceReferenceHolder.getUserRealm()).thenReturn(userRealm);
        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
    }

    @Test
    public void testGetRoleNames() throws Exception {
        StandaloneAuthorizationManagerClient standaloneAuthorizationManagerClient = new StandaloneAuthorizationManagerClient();
        standaloneAuthorizationManagerClient.getRoleNames();
        Mockito.verify(userStoreManager, Mockito.times(1)).getRoleNames();
    }

    @Test(expected = APIManagementException.class)
    public void testGetRoleNamesException() throws Exception {
        StandaloneAuthorizationManagerClient standaloneAuthorizationManagerClient = new StandaloneAuthorizationManagerClient();
        Mockito.when(userStoreManager.getRoleNames()).thenThrow(new UserStoreException());
        standaloneAuthorizationManagerClient.getRoleNames();
    }

    @Test
    public void testGetRolesOfUser() throws Exception {
        StandaloneAuthorizationManagerClient standaloneAuthorizationManagerClient = new StandaloneAuthorizationManagerClient();
        standaloneAuthorizationManagerClient.getRolesOfUser("john");
        Mockito.verify(userStoreManager, Mockito.times(1)).getRoleListOfUser("john");
    }

    @Test(expected = APIManagementException.class)
    public void testGetRolesOfUserException() throws Exception {
        StandaloneAuthorizationManagerClient standaloneAuthorizationManagerClient = new StandaloneAuthorizationManagerClient();
        Mockito.when(userStoreManager.getRoleListOfUser("john")).thenThrow(new UserStoreException());
        standaloneAuthorizationManagerClient.getRolesOfUser("john");

    }

    @Test
    public void testIsUserAuthorized() throws Exception {
        StandaloneAuthorizationManagerClient standaloneAuthorizationManagerClient = new StandaloneAuthorizationManagerClient();
        standaloneAuthorizationManagerClient.isUserAuthorized("john", "create");
        Mockito.verify(authorizationManager, Mockito.times(1))
                .isUserAuthorized("john", "create", CarbonConstants.UI_PERMISSION_ACTION);
    }

    @Test(expected = APIManagementException.class)
    public void testIsUserAuthorizedException() throws Exception {
        StandaloneAuthorizationManagerClient standaloneAuthorizationManagerClient = new StandaloneAuthorizationManagerClient();
        Mockito.when(authorizationManager.isUserAuthorized("john", "create", CarbonConstants.UI_PERMISSION_ACTION))
                .thenThrow(new UserStoreException());
        standaloneAuthorizationManagerClient.isUserAuthorized("john", "create");

    }
}
