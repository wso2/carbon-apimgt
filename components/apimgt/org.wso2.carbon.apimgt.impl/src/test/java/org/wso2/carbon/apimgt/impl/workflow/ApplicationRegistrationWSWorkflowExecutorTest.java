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
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.TestUtils;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.stream.XMLStreamException;

/**
 * ApplicationRegistrationWSWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class, ApplicationRegistrationWSWorkflowExecutor.class,
        ServiceReferenceHolder.class, AXIOMUtil.class, KeyManagerHolder.class})
public class ApplicationRegistrationWSWorkflowExecutorTest {

    private ApplicationRegistrationWSWorkflowExecutor applicationRegistrationWSWorkflowExecutor;
    private ApiMgtDAO apiMgtDAO;
    private ConfigurationContext configurationContext;
    private ApplicationRegistrationWorkflowDTO workflowDTO;
    private Application application;
    private ServiceClient serviceClient;
    private KeyManager keyManager;
    private String adminUsername = "admin";
    private String adminPassword = "admin";
    private String callBaclURL = "http://localhost:8090/playground2.0/oauth2client";


    @Before
    public void init() throws Exception {
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        ConfigurationContextService configurationContextService = Mockito.mock(ConfigurationContextService.class);
        configurationContext = Mockito.mock(ConfigurationContext.class);
        PowerMockito.when(serviceReferenceHolder.getContextService()).thenReturn(configurationContextService);
        PowerMockito.when(configurationContextService.getClientConfigContext()).thenReturn(configurationContext);
        serviceClient = Mockito.mock(ServiceClient.class);
        PowerMockito.whenNew(ServiceClient.class).withAnyArguments().thenReturn(serviceClient);
        applicationRegistrationWSWorkflowExecutor = new ApplicationRegistrationWSWorkflowExecutor();
        apiMgtDAO = TestUtils.getApiMgtDAO();
        application = new Application("test", new Subscriber("testUser"));
        application.setCallbackUrl(callBaclURL);
        application.setTier("Unlimited");
        PowerMockito.mockStatic(KeyManagerHolder.class);
        keyManager = Mockito.mock(KeyManager.class);
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance()).thenReturn(keyManager);
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        PowerMockito.when(keyManager.createApplication((OAuthAppRequest) Mockito.anyObject())).thenReturn
                (oAuthApplicationInfo);
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        workflowDTO = new ApplicationRegistrationWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        workflowDTO.setApplication(application);
        workflowDTO.setCallbackUrl(callBaclURL);
        workflowDTO.setTenantDomain("carbon.super");
        workflowDTO.setUserName("testUser");
        workflowDTO.setExternalWorkflowReference("testUser");
        workflowDTO.setKeyType("PRODUCTION");
        workflowDTO.setAppInfoDTO(oAuthAppRequest);

    }

    @Test
    public void testRetrievingWorkflowType() {
        Assert.assertEquals(applicationRegistrationWSWorkflowExecutor.getWorkflowType(),
                "AM_APPLICATION_REGISTRATION_PRODUCTION");
    }

    @Test
    public void testExecutingApplicationRegistrationWSWFWithoutAuthentication() throws APIManagementException {
        //Service credentials are not provided since the backend call authentication is not required
        PowerMockito.doNothing().when(apiMgtDAO).createApplicationRegistrationEntry(workflowDTO, false);
        try {
            Assert.assertNotNull(applicationRegistrationWSWorkflowExecutor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing application registration workflow");
        }
    }

    @Test
    public void testExecutingApplicationRegistrationWSWFWithAuthentication() throws APIManagementException {
        //Service credentials are provided since the backend call authentication is  required
        PowerMockito.doNothing().when(apiMgtDAO).createApplicationRegistrationEntry(workflowDTO, false);
        applicationRegistrationWSWorkflowExecutor.setUsername(adminUsername);
        applicationRegistrationWSWorkflowExecutor.setPassword(adminPassword.toCharArray());
        try {
            Assert.assertNotNull(applicationRegistrationWSWorkflowExecutor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing application registration workflow");
        }
    }

    @Test
    public void testFailureToExecuteApplicationRegistrationWSWFWhenMessageSendingFailed() throws Exception {
        applicationRegistrationWSWorkflowExecutor.setUsername(adminUsername);
        applicationRegistrationWSWorkflowExecutor.setPassword(adminPassword.toCharArray());
        //Test failure to execute application registration workflow, when AxisFault has been thrown while sending the
        // message out
        PowerMockito.doThrow(new AxisFault("Error sending out message")).when(serviceClient).fireAndForget(
                (OMElement) Mockito.anyObject());

        try {
            applicationRegistrationWSWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not occurred while executing application registration " +
                    "workflow");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error sending out message");
        }
    }

    @Test
    public void testFailureToExecuteApplicationRegistrationWSWFWhenPayloadProcessingFailed() throws Exception {
        //Test failure to execute application registration workflow, when XMLStreamException has been thrown while
        // building payload
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(new XMLStreamException("Error " +
                "converting String to OMElement"));
        try {
            applicationRegistrationWSWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not occurred while executing application registration " +
                    "workflow");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error converting String to OMElement");
        }
    }

    @Test
    public void testFailureToExecuteApplicationRegistrationWSWFWhenPreservingWFEntry() throws Exception {
        PowerMockito.doThrow(new APIManagementException("Error while creating Application Registration entry.")).when
                (apiMgtDAO).createApplicationRegistrationEntry(workflowDTO, false);
        try {
            applicationRegistrationWSWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException has not occurred while executing application registration " +
                    "workflow");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error while creating Application Registration entry.");
        }
    }

    @Test
    public void testCompletingApplicationRegistrationWSWFWhenWFRejected() throws Exception {
        applicationRegistrationWSWorkflowExecutor.setUsername(adminUsername);
        applicationRegistrationWSWorkflowExecutor.setPassword(adminPassword.toCharArray());
        workflowDTO.setStatus(WorkflowStatus.REJECTED);
        try {
            Assert.assertNotNull(applicationRegistrationWSWorkflowExecutor.complete(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while completing application registration workflow");
        }
    }

    @Test
    public void testCompletingApplicationRegistrationWSWFWhenWFCreated() throws Exception {
        applicationRegistrationWSWorkflowExecutor.setUsername(adminUsername);
        applicationRegistrationWSWorkflowExecutor.setPassword(adminPassword.toCharArray());
        workflowDTO.setStatus(WorkflowStatus.CREATED);
        try {
            Assert.assertNotNull(applicationRegistrationWSWorkflowExecutor.complete(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while completing application registration workflow");
        }
    }

    @Test
    public void testCompletingApplicationRegistrationWSWFWhenWFApproved() throws Exception {
        applicationRegistrationWSWorkflowExecutor.setUsername(adminUsername);
        applicationRegistrationWSWorkflowExecutor.setPassword(adminPassword.toCharArray());
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        try {
            Assert.assertNotNull(applicationRegistrationWSWorkflowExecutor.complete(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while completing application registration workflow");
        }
    }

    @Test
    public void testFailureToCompleteApplicationRegistrationWSWFWhenKeyGenerationFailed() throws Exception {
        applicationRegistrationWSWorkflowExecutor.setUsername(adminUsername);
        applicationRegistrationWSWorkflowExecutor.setPassword(adminPassword.toCharArray());
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        PowerMockito.doThrow(new APIManagementException("Error occurred when updating the status of the Application " +
                "Registration process")).when(keyManager).createApplication((OAuthAppRequest) Mockito
                .anyObject());
        try {
            applicationRegistrationWSWorkflowExecutor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException has not occurred while completing application registration " +
                    "workflow");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred when updating the status of the Application " +
                    "Registration process");
        }
    }

    @Test
    public void testCleaningUpApplicationRegistrationWSWF() throws Exception {
        try {
            applicationRegistrationWSWorkflowExecutor.cleanUpPendingTask("1");
            Assert.assertTrue(true);
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while cleaning up application registration workflow");
        }
    }

    @Test
    public void testFailureToCleanUpApplicationRegistrationWSWFWhenMessageSendingFailed() throws Exception {
        //Test failure to clean up pending tasks when AxisFault has been thrown while sending the message out
        PowerMockito.doThrow(new AxisFault("Error sending out message")).when(serviceClient).fireAndForget(
                (OMElement) Mockito.anyObject());
        try {
            applicationRegistrationWSWorkflowExecutor.cleanUpPendingTask("1");
            Assert.fail("Expected WorkflowException has not occurred while cleaning up pending tasks");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error sending out cancel pending registration approval process " +
                    "message. Cause: Error sending out message");
        }
    }

    @Test
    public void testFailureToCleanUpApplicationRegistrationWSWFWhenPayloadProcessingFailed() throws Exception {

        //Test failure to clean up pending tasks when XMLStreamException has been thrown while building payload
        applicationRegistrationWSWorkflowExecutor.setContentType("text/xml");
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(new XMLStreamException("Error " +
                "converting String to OMElement"));
        try {
            applicationRegistrationWSWorkflowExecutor.cleanUpPendingTask("1");
            Assert.fail("Expected WorkflowException has not occurred while cleaning up pending tasks");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error converting registration cleanup String to OMElement. Cause: " +
                    "Error converting String to OMElement");
        }
    }

    @Test
    public void testExecutorProperties() {
        applicationRegistrationWSWorkflowExecutor.setContentType("text/xml");
        applicationRegistrationWSWorkflowExecutor.setServiceEndpoint("http://localhost:9443/services");
        applicationRegistrationWSWorkflowExecutor.setUsername(adminUsername);
        applicationRegistrationWSWorkflowExecutor.setPassword(adminPassword.toCharArray());
        applicationRegistrationWSWorkflowExecutor.setCallbackURL(callBaclURL);

        Assert.assertEquals(applicationRegistrationWSWorkflowExecutor.getContentType(), "text/xml");
        Assert.assertEquals(applicationRegistrationWSWorkflowExecutor.getServiceEndpoint(),
                "http://localhost:9443/services");
        Assert.assertEquals(applicationRegistrationWSWorkflowExecutor.getUsername(), adminUsername);
        Assert.assertEquals(String.valueOf(applicationRegistrationWSWorkflowExecutor.getPassword()), adminPassword);
        Assert.assertEquals(applicationRegistrationWSWorkflowExecutor.getCallbackURL(), callBaclURL);

    }

    @Test
    public void testGetWorkflowDetails() {
        try {
            applicationRegistrationWSWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }
}
