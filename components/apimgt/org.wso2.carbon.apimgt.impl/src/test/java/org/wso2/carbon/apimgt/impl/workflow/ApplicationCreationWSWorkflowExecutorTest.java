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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ServiceReferenceHolderMockCreator;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.xml.stream.XMLStreamException;
import java.util.UUID;

/**
 * ApplicationCreationWSWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, ApiMgtDAO.class, ApplicationCreationWSWorkflowExecutor.class,
		AXIOMUtil.class})
public class ApplicationCreationWSWorkflowExecutorTest {

	private ApplicationCreationWSWorkflowExecutor applicationCreationWSWorkflowExecutor;
	private ApiMgtDAO apiMgtDAO;
	private ServiceClient serviceClient;

	@Before
	public void init() {
		applicationCreationWSWorkflowExecutor = new ApplicationCreationWSWorkflowExecutor();
		applicationCreationWSWorkflowExecutor.setPassword("admin".toCharArray());
		applicationCreationWSWorkflowExecutor.setUsername("admin");
		applicationCreationWSWorkflowExecutor.setServiceEndpoint("http://localhost:9445/service");
		applicationCreationWSWorkflowExecutor.setCallbackURL("http://localhost:8243/workflow-callback");

		PowerMockito.mockStatic(ApiMgtDAO.class);
		apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
		serviceClient = Mockito.mock(ServiceClient.class);
		PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
	}

	@Test
	public void testRetrievingWorkFlowType() {
		Assert.assertEquals(applicationCreationWSWorkflowExecutor.getWorkflowType(), "AM_APPLICATION_CREATION");
	}

	@Test
	public void testWorkflowApprove() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.APPROVED);

		Application app = Mockito.mock(Application.class);
		PowerMockito.doReturn(app).when(apiMgtDAO)
				.getApplicationById(Integer.parseInt(workflowDTO.getWorkflowReference()));
		PowerMockito.doNothing().when(apiMgtDAO).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_APPROVED);

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);
		Mockito.verify(apiMgtDAO, Mockito.times(1)).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_APPROVED);
	}

	@Test
	public void testWorkflowReject() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.REJECTED);

		Application app = Mockito.mock(Application.class);
		PowerMockito.doReturn(app).when(apiMgtDAO)
				.getApplicationById(Integer.parseInt(workflowDTO.getWorkflowReference()));
		PowerMockito.doNothing().when(apiMgtDAO).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_REJECTED);

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);
		Mockito.verify(apiMgtDAO, Mockito.times(1)).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_REJECTED);
	}

	@Test
	public void testWorkflowCreate() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.CREATED);

		Application app = Mockito.mock(Application.class);
		PowerMockito.doReturn(app).when(apiMgtDAO)
				.getApplicationById(Integer.parseInt(workflowDTO.getWorkflowReference()));
		PowerMockito.doNothing().when(apiMgtDAO).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_CREATED);

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);
		Mockito.verify(apiMgtDAO, Mockito.times(1)).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_CREATED);
	}

	@Test(expected = WorkflowException.class)
	public void testWorkflowRejectException() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.REJECTED);
		PowerMockito.doThrow(new APIManagementException("")).when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);

	}
	
	@Test(expected = WorkflowException.class)
	public void testWorkflowCompleteException() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.APPROVED);

		Application app = Mockito.mock(Application.class);
		//application is not in DB for the given id
		PowerMockito.doReturn(null).when(apiMgtDAO)
				.getApplicationById(Integer.parseInt(workflowDTO.getWorkflowReference()));
		PowerMockito.doNothing().when(apiMgtDAO).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_APPROVED);

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);
		
	}

	@Test(expected = WorkflowException.class)
	public void testWorkflowCompleteExceptionWhenStatusUpdateFailed() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.APPROVED);

		Application app = Mockito.mock(Application.class);
		//application is not in DB for the given id
		PowerMockito.doReturn(app).when(apiMgtDAO)
				.getApplicationById(Integer.parseInt(workflowDTO.getWorkflowReference()));
		PowerMockito.doThrow(new APIManagementException("Error occurred when updating the status of the Application " +
				"creation process")).when(apiMgtDAO).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_APPROVED);
		applicationCreationWSWorkflowExecutor.complete(workflowDTO);

	}
	
	@Test(expected = WorkflowException.class)
	public void testWorkflowCompleteExceptionWhenReadingDB() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.APPROVED);

		Application app = Mockito.mock(Application.class);

		PowerMockito.doThrow(new APIManagementException("")).when(apiMgtDAO)
				.getApplicationById(Integer.parseInt(workflowDTO.getWorkflowReference()));
		PowerMockito.doNothing().when(apiMgtDAO).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_APPROVED);

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);
		
	}

	@Test(expected = WorkflowException.class)
	public void testWorkflowCompleteExceptionWhenUpdatingApplication() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.APPROVED);

		PowerMockito.doThrow(new APIManagementException("")).when(apiMgtDAO).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_APPROVED);

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);
		
	}
	@Test(expected = WorkflowException.class)
	public void testWorkflowApproveException() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.APPROVED);
		PowerMockito.doThrow(new APIManagementException("")).when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.UNBLOCKED);

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);

	}

	@Test
	public void testWorkflowNotAllowedStatus() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.REGISTERED);
		Application app = Mockito.mock(Application.class);
		PowerMockito.doReturn(app).when(apiMgtDAO)
				.getApplicationById(Integer.parseInt(workflowDTO.getWorkflowReference()));

		applicationCreationWSWorkflowExecutor.complete(workflowDTO);
		// shouldn't update status
		Mockito.verify(apiMgtDAO, Mockito.never()).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_CREATED);
		Mockito.verify(apiMgtDAO, Mockito.never()).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_APPROVED);
		Mockito.verify(apiMgtDAO, Mockito.never()).updateApplicationStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()),
				APIConstants.ApplicationStatus.APPLICATION_REJECTED);
	}

	@Test
	public void testWorkflowCleanupTask() throws Exception {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		PowerMockito.whenNew(ServiceClient.class)
				.withArguments(Mockito.any(ConfigurationContext.class), Mockito.any(AxisService.class))
				.thenReturn(serviceClient);

		try {
			applicationCreationWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getExternalWorkflowReference());
		} catch (WorkflowException e) {
			Assert.fail("Error while calling the cleanup task");
		}

	}
	
	@Test(expected = WorkflowException.class)
	public void testWorkflowCleanupTaskException() throws Exception {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();
		applicationCreationWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getExternalWorkflowReference());
	}

	@Test(expected = WorkflowException.class)
	public void testWorkflowCleanupTaskExceptionWhenMessageProcessingFailed() throws Exception {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();
		PowerMockito.mockStatic(AXIOMUtil.class);
		PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(new XMLStreamException("Error " +
				"converting String to OMElement"));
		applicationCreationWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getExternalWorkflowReference());
	}

	@Test
	public void testWorkflowExecute() throws Exception {
		ApplicationWorkflowDTO workflowDTO = new ApplicationWorkflowDTO();       

		Application application = new Application("TestAPP", new Subscriber(null));
		
		application.setTier("Gold");
		application.setCallbackUrl("www.wso2.com");
		application.setDescription("Description");	
		workflowDTO.setApplication(application);
		workflowDTO.setTenantDomain("wso2");
		workflowDTO.setUserName("admin");
		workflowDTO.setCallbackUrl("http://localhost:8280/workflow-callback");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		PowerMockito.whenNew(ServiceClient.class)
				.withArguments(Mockito.any(ConfigurationContext.class), Mockito.any(AxisService.class))
				.thenReturn(serviceClient);

		try {
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}

	}

	@Test
	public void testWorkflowExecuteFailWhenMessageProcessingFailed() throws Exception {
		ApplicationWorkflowDTO workflowDTO = new ApplicationWorkflowDTO();
		PowerMockito.mockStatic(AXIOMUtil.class);
		PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(new XMLStreamException("Error " +
				"converting String to OMElement"));
		Application application = new Application("TestAPP", new Subscriber(null));

		application.setTier("Gold");
		application.setCallbackUrl("www.wso2.com");
		application.setDescription("Description");
		workflowDTO.setApplication(application);
		workflowDTO.setTenantDomain("wso2");
		workflowDTO.setUserName("admin");
		workflowDTO.setCallbackUrl("http://localhost:8280/workflow-callback");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		PowerMockito.whenNew(ServiceClient.class)
				.withArguments(Mockito.any(ConfigurationContext.class), Mockito.any(AxisService.class))
				.thenReturn(serviceClient);
		try {
			applicationCreationWSWorkflowExecutor.execute(workflowDTO);
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		} catch (WorkflowException e) {
			Assert.assertEquals(e.getMessage(), "Error converting String to OMElement");
		}
	}

	@Test
	public void testWorkflowExecuteWithLimitedParam() throws Exception {
		//application without a callback url 
		ApplicationWorkflowDTO workflowDTO = new ApplicationWorkflowDTO();       

		Application application = new Application("TestAPP", new Subscriber(null));
		
		application.setTier("Gold");
		application.setDescription("Description");	
		workflowDTO.setApplication(application);
		workflowDTO.setTenantDomain("wso2");
		workflowDTO.setUserName("admin");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		PowerMockito.whenNew(ServiceClient.class)
				.withArguments(Mockito.any(ConfigurationContext.class), Mockito.any(AxisService.class))
				.thenReturn(serviceClient);

		try {
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		}

	}
	
	@Test(expected = WorkflowException.class)
	public void testWorkflowExecuteException() throws Exception {
		ApplicationWorkflowDTO workflowDTO = new ApplicationWorkflowDTO();       

		Application application = new Application("TestAPP", new Subscriber(null));
		
		application.setTier("Gold");
		application.setCallbackUrl("www.wso2.com");
		application.setDescription("Description");	
		workflowDTO.setApplication(application);
		workflowDTO.setTenantDomain("wso2");
		workflowDTO.setUserName("admin");
		workflowDTO.setCallbackUrl("http://localhost:8280/workflow-callback");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		applicationCreationWSWorkflowExecutor.execute(workflowDTO);

	}

	@Test
	public void testWorkflowExecuteWithoutExecutorParam() throws Exception {
		ApplicationWorkflowDTO workflowDTO = new ApplicationWorkflowDTO();       

		Application application = new Application("TestAPP", new Subscriber(null));
		
		application.setTier("Gold");
		application.setCallbackUrl("www.wso2.com");
		application.setDescription("Description");	
		workflowDTO.setApplication(application);
		workflowDTO.setTenantDomain("wso2");
		workflowDTO.setUserName("admin");
		workflowDTO.setCallbackUrl("http://localhost:8280/workflow-callback");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		PowerMockito.whenNew(ServiceClient.class)
				.withArguments(Mockito.any(ConfigurationContext.class), Mockito.any(AxisService.class))
				.thenReturn(serviceClient);

		applicationCreationWSWorkflowExecutor.setUsername(null);
		applicationCreationWSWorkflowExecutor.setPassword(null);
		try {
			// shouldn't fail. this checks for unsecured enpoint use case
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		}
		// empty values
		applicationCreationWSWorkflowExecutor.setUsername("");
		applicationCreationWSWorkflowExecutor.setPassword("".toCharArray());
		try {
			// shouldn't fail. this checks for unsecured enpoint use case
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		}

		// one empty value and other null
		applicationCreationWSWorkflowExecutor.setUsername("");
		applicationCreationWSWorkflowExecutor.setPassword(null);
		try {
			// shouldn't fail. this checks for unsecured enpoint use case
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		}

		applicationCreationWSWorkflowExecutor.setUsername(null);
		applicationCreationWSWorkflowExecutor.setPassword("".toCharArray());
		try {
			// shouldn't fail. this checks for unsecured enpoint use case
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		}

		// without a password
		applicationCreationWSWorkflowExecutor.setUsername("admin");
		applicationCreationWSWorkflowExecutor.setPassword("".toCharArray());
		try {
			// shouldn't fail. this checks for unsecured enpoint use case.
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		}

		applicationCreationWSWorkflowExecutor.setUsername("admin");
		applicationCreationWSWorkflowExecutor.setPassword(null);
		try {
			// shouldn't fail. this checks for unsecured enpoint use case.
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		}

	}

	@Test
	public void testWorkflowExecuteWithDifferentMediatype() throws Exception {
		ApplicationWorkflowDTO workflowDTO = new ApplicationWorkflowDTO();       

		Application application = new Application("TestAPP", new Subscriber(null));
		
		application.setTier("Gold");
		application.setCallbackUrl("www.wso2.com");
		application.setDescription("Description");	
		workflowDTO.setApplication(application);
		workflowDTO.setTenantDomain("wso2");
		workflowDTO.setUserName("admin");
		workflowDTO.setCallbackUrl("http://localhost:8280/workflow-callback");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		PowerMockito.whenNew(ServiceClient.class)
				.withArguments(Mockito.any(ConfigurationContext.class), Mockito.any(AxisService.class))
				.thenReturn(serviceClient);

		applicationCreationWSWorkflowExecutor.setContentType("application/xml");
		try {
			// shouldn't fail.
			Assert.assertNotNull(applicationCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Application creation ws workflow");
		}

	}

	@Test
	public void testExecutorProperties() throws WorkflowException {
		applicationCreationWSWorkflowExecutor.setPassword("admin".toCharArray());
		applicationCreationWSWorkflowExecutor.setUsername("admin");
		applicationCreationWSWorkflowExecutor.setServiceEndpoint("http://localhost:9445/service");
		applicationCreationWSWorkflowExecutor.setCallbackURL("http://localhost:8243/workflow-callback");
		applicationCreationWSWorkflowExecutor.setContentType("application/xml");

		Assert.assertEquals("Invalid password", "admin",
				String.valueOf(applicationCreationWSWorkflowExecutor.getPassword()));
		Assert.assertEquals("Invalid user", "admin", applicationCreationWSWorkflowExecutor.getUsername());
		Assert.assertEquals("Invalid callback", "http://localhost:8243/workflow-callback",
				applicationCreationWSWorkflowExecutor.getCallbackURL());
		Assert.assertEquals("Invalid service ep", "http://localhost:9445/service",
				applicationCreationWSWorkflowExecutor.getServiceEndpoint());
		Assert.assertEquals("Invalid content type", "application/xml",
				applicationCreationWSWorkflowExecutor.getContentType());

		Assert.assertNull(applicationCreationWSWorkflowExecutor.getWorkflowDetails(""));

	}
}
