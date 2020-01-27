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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
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
import org.wso2.carbon.apimgt.impl.TestUtils;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SelfSignUpUtil;
import org.wso2.carbon.apimgt.impl.workflow.events.APIMgtWorkflowDataPublisher;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

/**
 * UserSignUpWSWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, UserSignUpWSWorkflowExecutor.class, ApiMgtDAO.class, APIUtil.class,
        AXIOMUtil.class, SelfSignUpUtil.class, CarbonUtils.class, APIMgtWorkflowDataPublisher.class})
public class UserSignUpWSWorkflowExecutorTest {

    private UserSignUpWSWorkflowExecutor userSignUpWSWorkflowExecutor;
    private ServiceClient serviceClient;
    private ApiMgtDAO apiMgtDAO;
    private WorkflowDTO workflowDTO;
    private APIManagerConfiguration apiManagerConfiguration;
    private UserAdminStub userAdminStub;
    private UserStoreManager userStoreManager;
    private String callBackURL = "https://localhost:8243/services/WorkflowCallbackService";
    private String tenantDomain = "carbon.super";
    private String externalWFReference = UUIDGenerator.generateUUID();
    private String username = "admin";
    private String password = "admin";
    private String signUpRole = "subscriber";
    private int tenantID = -1234;
    private String testUsername = "PRIMARY/testuser";

    @Before
    public void init() throws Exception {
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        ConfigurationContextService configurationContextService = Mockito.mock(ConfigurationContextService.class);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        userStoreManager = Mockito.mock(UserStoreManager.class);
        userAdminStub = Mockito.mock(UserAdminStub.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        serviceClient = Mockito.mock(ServiceClient.class);
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration);
        PowerMockito.mockStatic(SelfSignUpUtil.class);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(true);
        PowerMockito.doNothing().when(CarbonUtils.class, "setBasicAccessSecurityHeaders", Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean(), Mockito.any());
        PowerMockito.when(serviceReferenceHolder.getContextService()).thenReturn(configurationContextService);
        PowerMockito.when(configurationContextService.getClientConfigContext()).thenReturn(configurationContext);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn
                (apiManagerConfigurationService);
        PowerMockito.whenNew(ServiceClient.class).withAnyArguments().thenReturn(serviceClient);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        Mockito.when(realmService.getTenantUserRealm(tenantID)).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        APIMgtWorkflowDataPublisher apiMgtWorkflowDataPublisher = Mockito.mock(APIMgtWorkflowDataPublisher.class);
        Mockito.when(serviceReferenceHolder.getApiMgtWorkflowDataPublisher()).thenReturn(apiMgtWorkflowDataPublisher);
        PowerMockito.whenNew(UserAdminStub.class).withAnyArguments().thenReturn(userAdminStub);
        apiMgtDAO = TestUtils.getApiMgtDAO();
        userSignUpWSWorkflowExecutor = new UserSignUpWSWorkflowExecutor();
        workflowDTO = new WorkflowDTO();
        workflowDTO.setCallbackUrl(callBackURL);
        workflowDTO.setTenantDomain(tenantDomain);
        workflowDTO.setExternalWorkflowReference(externalWFReference);
        workflowDTO.setWorkflowReference(testUsername + "@carbon.super");
    }

    @Test
    public void testRetrievingWorkflowType() {
        Assert.assertEquals(userSignUpWSWorkflowExecutor.getWorkflowType(), "AM_USER_SIGNUP");
    }

    @Test
    public void testExecutingUserSignUpWorkflow() throws Exception {
        userSignUpWSWorkflowExecutor.setUsername(username);
        userSignUpWSWorkflowExecutor.setPassword(password.toCharArray());
        userSignUpWSWorkflowExecutor.setContentType("text/xml");
        PowerMockito.doNothing().when(apiMgtDAO).addWorkflowEntry(workflowDTO);
        try {
            Assert.assertNotNull(userSignUpWSWorkflowExecutor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing user sign up workflow");
        }
    }

    @Test
    public void testFailureToExecuteUserSignUpWSWorkflow() throws Exception {
        userSignUpWSWorkflowExecutor.setUsername(username);
        userSignUpWSWorkflowExecutor.setPassword(password.toCharArray());
        PowerMockito.doNothing().when(apiMgtDAO).addWorkflowEntry(workflowDTO);

        //Test failure to execute user sign up workflow, when APIManagementException has been thrown while persisting
        // workflow entry in database
        PowerMockito.doThrow(new APIManagementException("Error while persisting workflow")).when(apiMgtDAO)
                .addWorkflowEntry(workflowDTO);
        try {
            userSignUpWSWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not occurred while executing user sign up workflow");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while persisting workflow");
        }

        //Test failure to execute user sign up workflow, when AxisFault has been thrown while sending the message out
        PowerMockito.doThrow(new AxisFault("Error sending out message")).when(serviceClient).fireAndForget(
                (OMElement) Mockito.anyObject());
        try {
            userSignUpWSWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not occurred while executing user sign up workflow");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error sending out message");
        }

        //Test failure to execute user sign up workflow, when XMLStreamException has been thrown while building payload
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(new XMLStreamException("Error " +
                "converting String to OMElement"));
        try {
            userSignUpWSWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not occurred while executing user sign up workflow");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error converting String to OMElement");
        }
    }

    @Test
    public void testCompletingUserSignUpWorkflowApprovedByAdmin() throws Exception {
        Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
        roleMap.put(signUpRole, false);
        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setAdminUserName("admin");
        userRegistrationConfigDTO.setAdminPassword("admin");
        userRegistrationConfigDTO.setRoles(roleMap);
        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenReturn(userRegistrationConfigDTO);
        PowerMockito.when(SelfSignUpUtil.getRoleNames(userRegistrationConfigDTO)).thenCallRealMethod();
        PowerMockito.doNothing().when(apiMgtDAO).updateWorkflowStatus(workflowDTO);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_URL)).thenReturn
                ("https://localhost:9443/services/");
        Mockito.when(userStoreManager.isExistingUser(testUsername)).thenReturn(true);
        Mockito.when(userStoreManager.isExistingRole("Internal/" + signUpRole)).thenReturn(true);

        //Set workflow status to be approved
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        try {
            Assert.assertNotNull(userSignUpWSWorkflowExecutor.complete(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while completing 'APPROVED' user sign up workflow");
        }
    }

    @Test
    public void testFailureToCompleteUserSignUpWorkflowApprovedByAdmin() throws Exception {
        Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
        roleMap.put(signUpRole, false);
        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setRoles(roleMap);
        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenReturn(userRegistrationConfigDTO);
        PowerMockito.when(SelfSignUpUtil.getRoleNames(userRegistrationConfigDTO)).thenCallRealMethod();
        PowerMockito.doNothing().when(apiMgtDAO).updateWorkflowStatus(workflowDTO);
        Mockito.when(userStoreManager.isExistingUser(testUsername)).thenReturn(true);
        Mockito.when(userStoreManager.isExistingRole("Internal/" + signUpRole)).thenReturn(true);

        //Set workflow status to be approved
        workflowDTO.setStatus(WorkflowStatus.APPROVED);

        //Test failure to complete workflow execution when AuthManager server url is not configured
        try {
            userSignUpWSWorkflowExecutor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown when auth manager url is not configured");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Can't connect to the authentication manager. serverUrl is missing");
        }
        //Set AuthManager endpoint url
        PowerMockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_URL)).thenReturn
                ("https://localhost:9443/services/");

        //Test failure to complete workflow execution when tenant admin username is not found
        try {
            userSignUpWSWorkflowExecutor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown when admin username is not found");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Can't connect to the authentication manager. adminUsername is " +
                    "missing");
        }

        //Test failure to complete workflow execution when tenant admin password is not found
        userRegistrationConfigDTO.setAdminUserName(username);
        try {
            userSignUpWSWorkflowExecutor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException has not been occurred when admin password is not found");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Can't connect to the authentication manager. adminPassword is " +
                    "missing");
        }

        //Set tenant admin credentials
        userRegistrationConfigDTO.setAdminUserName("admin");
        userRegistrationConfigDTO.setAdminPassword("admin");

        //Test failure to complete workflow execution, when error has been occurred while updating user with signup roles
        Mockito.doThrow(new RemoteException()).when(userAdminStub).updateRolesOfUser(Mockito.anyString(), new
                String[]{Mockito.anyString()});
        try {
            userSignUpWSWorkflowExecutor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown when signup user role update failed");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while assigning role to user");
        }

        //Test failure to complete workflow execution, when sign up roles are not existing in user realm
        Mockito.when(userStoreManager.isExistingRole("Internal/" + signUpRole)).thenReturn(false);
        try {
            userSignUpWSWorkflowExecutor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown when signup role is not existing");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while assigning role to user");
        }

        //Test failure to complete workflow execution, when error has been occurred while retrieving signup config
        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenThrow(new APIManagementException
                ("Error occurred while retrieving signup configuration"));
        try {
            userSignUpWSWorkflowExecutor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown when signup role is not existing");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while accessing signup configuration");
        }
    }

    @Test
    public void testCompletingUserSignUpWorkflowRejectedByAdmin() throws Exception {
        Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
        roleMap.put(signUpRole, false);
        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setAdminUserName("admin");
        userRegistrationConfigDTO.setAdminPassword("admin");
        userRegistrationConfigDTO.setRoles(roleMap);
        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenReturn(userRegistrationConfigDTO);
        PowerMockito.doNothing().when(apiMgtDAO).updateWorkflowStatus(workflowDTO);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_URL)).thenReturn
                ("https://localhost:9443/services/");

        //Set workflow status to be approved
        workflowDTO.setStatus(WorkflowStatus.REJECTED);
        try {
            Assert.assertNotNull(userSignUpWSWorkflowExecutor.complete(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while completing 'REJECTED' user sign up workflow");
        }
    }

    @Test
    public void testFailureToCompleteUserSignUpWorkflowRejectedByAdmin() throws Exception {
        Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
        roleMap.put(signUpRole, false);
        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setAdminUserName("admin");
        userRegistrationConfigDTO.setAdminPassword("admin");
        userRegistrationConfigDTO.setRoles(roleMap);
        PowerMockito.when(SelfSignUpUtil.getSignupConfiguration(tenantDomain)).thenReturn(userRegistrationConfigDTO);
        PowerMockito.doNothing().when(apiMgtDAO).updateWorkflowStatus(workflowDTO);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.AUTH_MANAGER_URL)).thenReturn
                ("https://localhost:9443/services/");

        //Set workflow status to be approved
        workflowDTO.setStatus(WorkflowStatus.REJECTED);
        Mockito.doThrow(new AxisFault("Error occurred while deleting user")).when(userAdminStub).deleteUser(Mockito
                .anyString());
        try {
            userSignUpWSWorkflowExecutor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException has not been thrown when user deletion failed");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while deleting the user");
        }
    }

    @Test
    public void testCleaningUpPendingTasks() {
        try {
            userSignUpWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getWorkflowReference());
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while cleaning up pending tasks");
        }
    }

    @Test
    public void testFailuresToCleanUpPendingTasks() throws AxisFault, XMLStreamException {

        //Test failure to clean up pending tasks when AxisFault has been thrown while sending the message out
        PowerMockito.doThrow(new AxisFault("Error sending out message")).when(serviceClient).fireAndForget(
                (OMElement) Mockito.anyObject());
        try {
            userSignUpWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getWorkflowReference());
            Assert.fail("Expected WorkflowException has not occurred while executing user sign up workflow");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Error sending out message"));
        }

        //Test failure to clean up pending tasks, when XMLStreamException has been thrown while building payload
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(new XMLStreamException("Error " +
                "converting String to OMElement"));
        try {
            userSignUpWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getWorkflowReference());
            Assert.fail("Expected WorkflowException has not occurred while executing user sign up workflow");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Error converting String to OMElement"));
        }
    }

    @Test
    public void testGetWorkflowDetails(){
        try {
            userSignUpWSWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }
}

