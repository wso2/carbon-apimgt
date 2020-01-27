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

import org.apache.axis2.client.ServiceClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.SelfSignUpUtil;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * UserSignUpSimpleWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, SelfSignUpUtil.class, UserSignUpSimpleWorkflowExecutor.class,
        CarbonUtils.class})
public class UserSignUpSimpleWorkflowExecutorTest {

    private UserSignUpSimpleWorkflowExecutor userSignUpSimpleWorkflowExecutor;
    private WorkflowDTO workflowDTO;
    private APIManagerConfiguration apiManagerConfiguration;
    private UserAdminStub userAdminStub;
    private UserStoreManager userStoreManager;
    private ServiceClient serviceClient;
    private String tenantDomain = "carbon.super";
    private int tenantID = -1234;
    private String username = "testuser";
    private String signUpRole = "subscriber";

    @Before
    public void init() throws Exception {
        userSignUpSimpleWorkflowExecutor = new UserSignUpSimpleWorkflowExecutor();
        workflowDTO = new WorkflowDTO();
        workflowDTO.setExternalWorkflowReference("12345");
        workflowDTO.setTenantDomain(tenantDomain);
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        workflowDTO.setWorkflowReference(username);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        userStoreManager = Mockito.mock(UserStoreManager.class);
        userAdminStub = Mockito.mock(UserAdminStub.class);
        serviceClient = Mockito.mock(ServiceClient.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(SelfSignUpUtil.class);
        PowerMockito.mockStatic(CarbonUtils.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn
                (apiManagerConfigurationService);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        Mockito.when(realmService.getTenantUserRealm(tenantID)).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        PowerMockito.whenNew(UserAdminStub.class).withAnyArguments().thenReturn(userAdminStub);
        Mockito.when(userAdminStub._getServiceClient()).thenReturn(serviceClient);
        PowerMockito.doNothing().when(CarbonUtils.class, "setBasicAccessSecurityHeaders", Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean(), (ServiceClient) Mockito.anyObject());

    }

    @Test
    public void testRetrievingWorkflowType() {
        Assert.assertEquals(userSignUpSimpleWorkflowExecutor.getWorkflowType(), "AM_USER_SIGNUP");
    }

    @Test
    public void testExecutingUserSignUpSimpleWorkflow() throws APIManagementException, org
            .wso2.carbon.user.core.UserStoreException, RemoteException, UserAdminUserAdminException {
        Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
        roleMap.put(signUpRole, false);

        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setAdminUserName("admin");
        userRegistrationConfigDTO.setAdminPassword("admin");
        userRegistrationConfigDTO.setRoles(roleMap);

        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenReturn(userRegistrationConfigDTO);
        PowerMockito.when(SelfSignUpUtil.getRoleNames(userRegistrationConfigDTO)).thenCallRealMethod();
        PowerMockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_URL)).thenReturn
                ("https://localhost:9443/services/");
        Mockito.when(userStoreManager.isExistingUser(username)).thenReturn(true);
        Mockito.when(userStoreManager.isExistingRole("Internal/" + signUpRole)).thenReturn(true);
        FlaggedName flaggedName = new FlaggedName();
        flaggedName.setSelected(true);
        flaggedName.setItemName(signUpRole);
        FlaggedName[] flaggedNames = {flaggedName};
        Mockito.when(userAdminStub.getRolesOfUser(username, "*", -1)).thenReturn(flaggedNames);

        try {
            Assert.assertNotNull(userSignUpSimpleWorkflowExecutor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException has thrown while executing the user signup simple workflow");
        }
    }

    @Test
    public void testFailuresToCompleteUserSignUpSimpleWorkflow() throws APIManagementException,
            org.wso2.carbon.user.core.UserStoreException, RemoteException, UserAdminUserAdminException {
        Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
        roleMap.put(signUpRole, false);

        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setRoles(roleMap);

        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenReturn(userRegistrationConfigDTO);
        PowerMockito.when(SelfSignUpUtil.getRoleNames(userRegistrationConfigDTO)).thenCallRealMethod();

        Mockito.when(userStoreManager.isExistingUser(username)).thenReturn(true);
        Mockito.when(userStoreManager.isExistingRole("Internal/" + signUpRole)).thenReturn(true);

        //Test failure to complete workflow execution when AuthManager server url is not configured
        try {
            userSignUpSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown auth manager URL is not found");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while assigning role to user");
        }
        //Set AuthManager endpoint url
        PowerMockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_URL)).thenReturn
                ("https://localhost:9443/services/");

        //Test failure to complete workflow execution when tenant admin credentials are not found
        try {
            userSignUpSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown when admin credentials are not found");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while assigning role to user");
        }

        //Set tenant admin credentials
        userRegistrationConfigDTO.setAdminUserName("admin");
        userRegistrationConfigDTO.setAdminPassword("admin");

        //Test failure to complete workflow execution, when error has been occurred while updating user with signup
        // roles
        Mockito.when(userAdminStub.getRolesOfUser(username, "*", -1)).thenThrow(new RemoteException());
        try {
            userSignUpSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown while signup user role update failed");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while assigning role to user");
        }

        //Test failure to complete workflow execution, when sign up roles are not existing in user realm
        Mockito.when(userStoreManager.isExistingRole("Internal/" + signUpRole)).thenReturn(false);
        try {
            userSignUpSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown when signup role is not existing");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while assigning role to user");
        }

        //Test failure to complete workflow execution, when error has been occurred while retrieving signup config
        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenThrow(new APIManagementException
                ("Error occurred while retrieving signup configuration"));
        try {
            userSignUpSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown retrieving sign up configuration");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while accessing signup configuration");
        }
    }


    @Test
    public void testGetWorkflowDetails(){
        try {
            userSignUpSimpleWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }
}
