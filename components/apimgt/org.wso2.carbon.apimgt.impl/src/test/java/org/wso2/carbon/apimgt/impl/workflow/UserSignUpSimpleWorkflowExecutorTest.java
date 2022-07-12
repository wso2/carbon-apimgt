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
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.SelfSignUpUtil;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Arrays;
import java.util.Collections;
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
    private UserStoreManager userStoreManager;
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
        ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
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

    }

    @Test
    public void testRetrievingWorkflowType() {
        Assert.assertEquals(userSignUpSimpleWorkflowExecutor.getWorkflowType(), "AM_USER_SIGNUP");
    }

    @Test
    public void testExecutingUserSignUpSimpleWorkflow() throws APIManagementException, UserStoreException {
        Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
        roleMap.put(signUpRole, false);

        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setAdminUserName("admin");
        userRegistrationConfigDTO.setAdminPassword("admin");
        userRegistrationConfigDTO.setRoles(roleMap);

        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenReturn(userRegistrationConfigDTO);
        PowerMockito.when(SelfSignUpUtil.getRoleNames(userRegistrationConfigDTO)).thenCallRealMethod();
        Mockito.when(userStoreManager.isExistingUser(username)).thenReturn(true);
        Mockito.when(userStoreManager.isExistingRole("Internal/" + signUpRole)).thenReturn(true);
        Mockito.doNothing().when(userStoreManager).updateRoleListOfUser(username, null,
                new String[]{"Internal/" + signUpRole});

        try {
            Assert.assertNotNull(userSignUpSimpleWorkflowExecutor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException has thrown while executing the user signup simple workflow");
        }
    }

    @Test
    public void testFailuresToCompleteUserSignUpSimpleWorkflow() throws Exception {
        Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
        roleMap.put(signUpRole, false);

        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setRoles(roleMap);
        workflowDTO.setTenantDomain(tenantDomain);
        PowerMockito.when(SelfSignUpUtil.class, "getSignupConfiguration", tenantDomain).thenReturn(userRegistrationConfigDTO);
        PowerMockito.when(SelfSignUpUtil.class, "getRoleNames", userRegistrationConfigDTO).thenReturn(Collections.singletonList(
                "Internal/" + signUpRole));

        Mockito.when(userStoreManager.isExistingUser(username)).thenReturn(true);

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
