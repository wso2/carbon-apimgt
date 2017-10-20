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

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.TestUtils;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * UserSignUpWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, CarbonUtils.class, UserSignUpWorkflowExecutor.class})
public class UserSignUpWorkflowExecutorTest {
    private String serverURL = "https://localhost:9443/services/";
    private String adminUsername = "admin";
    private String adminPassword = "admin";
    private String role = "subscriber";
    private String username = "testUser";
    private String tenantDomain = "carbon.super";
    private UserStoreManager userStoreManager;
    private UserAdminStub userAdminStub;
    private UserRegistrationAdminServiceStub userRegistrationAdminServiceStub;
    private FlaggedName[] flaggedNames;
    private ServiceClient serviceClient;


    @Before
    public void init() throws Exception {
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        userStoreManager = Mockito.mock(UserStoreManager.class);
        PowerMockito.mockStatic(CarbonUtils.class);
        userAdminStub = Mockito.mock(UserAdminStub.class);
        userRegistrationAdminServiceStub = Mockito.mock(UserRegistrationAdminServiceStub.class);
        serviceClient =  Mockito.mock(ServiceClient.class);;
        PowerMockito.whenNew(UserAdminStub.class).withAnyArguments().thenReturn(userAdminStub);
        PowerMockito.whenNew(UserRegistrationAdminServiceStub.class).withAnyArguments().thenReturn
                (userRegistrationAdminServiceStub);
        PowerMockito.when(userRegistrationAdminServiceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceClient.getOptions()).thenReturn(new Options());
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getBootstrapRealm()).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        PowerMockito.doNothing().when(CarbonUtils.class, "setBasicAccessSecurityHeaders", Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean(), (ServiceClient) Mockito.anyObject());
        FlaggedName flaggedName = new FlaggedName();
        flaggedName.setSelected(true);
        flaggedName.setItemName(role);
        flaggedNames = new FlaggedName[]{flaggedName};
    }

    @Test
    public void testUpdatingRoleOfUser() throws UserStoreException, RemoteException, UserAdminUserAdminException {
        Mockito.when(userAdminStub.getRolesOfUser(username, "*", -1)).thenReturn(flaggedNames);
        Mockito.when(userStoreManager.isExistingRole(role)).thenReturn(true);
        try {
            UserSignUpWorkflowExecutor.updateRolesOfUser(serverURL, adminUsername, adminPassword, username, role);
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail("Unexpected exception occurred while updating role of the given user");
        }
    }

    @Test
    public void testFailuresToUpdateRoleOfUserWhenRemoteServiceCallFailed() throws UserStoreException, RemoteException,
            UserAdminUserAdminException {
        Mockito.when(userAdminStub.getRolesOfUser(username, "*", -1)).thenReturn(flaggedNames);
        Mockito.when(userStoreManager.isExistingRole(role)).thenReturn(true);

        //Test failure to update the user role when
        Mockito.doThrow(new RemoteException("Exception occurred while updating the roles of user")).when(userAdminStub)
                .updateRolesOfUser(Mockito.anyString(), new
                        String[]{Mockito.anyString()});
        try {
            UserSignUpWorkflowExecutor.updateRolesOfUser(serverURL, adminUsername, adminPassword, username, role);
            Assert.fail("Expected exception has been not thrown while updating the roles of user failed");
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Exception occurred while updating the roles of user");
        }
    }

    @Test
    public void testFailuresToUpdateRoleOfUserWhenRoleIsNotExisting() throws UserStoreException, RemoteException,
            UserAdminUserAdminException {
        Mockito.when(userAdminStub.getRolesOfUser(username, "*", -1)).thenReturn(flaggedNames);
        Mockito.when(userStoreManager.isExistingRole(role)).thenReturn(false);

        //Test failure to update the user role when role is not existing
        try {
            UserSignUpWorkflowExecutor.updateRolesOfUser(serverURL, adminUsername, adminPassword, username, role);
            Assert.fail("Expected exception has been not thrown while failed to update the roles of user");
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Could not find role " + role + " in the user store");
        }
    }

    @Test
    public void testAddingUsersToUserStore() throws UserStoreException, RemoteException, UserAdminUserAdminException {
        try {
            UserSignUpWorkflowExecutor userSignUpWorkflowExecutor = new UserSignUpWSWorkflowExecutor();
            userSignUpWorkflowExecutor.addUserToUserStore(serverURL, new UserDTO());
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail("Unexpected exception occurred while adding users to user store");
        }
    }

    @Test
    public void testFailureToAddUsersToUserStoreWhenRemoteServiceCallFailed() throws UserStoreException,
            RemoteException, UserAdminUserAdminException, UserRegistrationAdminServiceException {
        PowerMockito.doThrow(new RemoteException("Exception occurred while adding user to user store")).when
                (userRegistrationAdminServiceStub).addUser((UserDTO) Mockito
                .anyObject());
        try {
            UserSignUpWorkflowExecutor userSignUpWorkflowExecutor = new UserSignUpWSWorkflowExecutor();
            userSignUpWorkflowExecutor.addUserToUserStore(serverURL, new UserDTO());
            Assert.fail("Expected exception has been not thrown while adding user to user store");
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Exception occurred while adding user to user store");
        }
    }
}
