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

import java.util.UUID;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.http.HTTPConstants;
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
import org.wso2.carbon.apimgt.impl.ServiceReferenceHolderMockCreator;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.xml.stream.XMLStreamException;

/**
 * SubscriptionCreationSimpleWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, ApiMgtDAO.class, SubscriptionCreationWSWorkflowExecutor.class,
		AXIOMUtil.class })
public class SubscriptionCreationWSWorkflowExecutorTest {

	private SubscriptionCreationWSWorkflowExecutor subscriptionCreationWSWorkflowExecutor;
	private ApiMgtDAO apiMgtDAO;
	private ServiceClient serviceClient;

	@Before
	public void init() {
		subscriptionCreationWSWorkflowExecutor = new SubscriptionCreationWSWorkflowExecutor();
		subscriptionCreationWSWorkflowExecutor.setPassword("admin".toCharArray());
		subscriptionCreationWSWorkflowExecutor.setUsername("admin");
		subscriptionCreationWSWorkflowExecutor.setServiceEndpoint("http://localhost:9445/service");
		subscriptionCreationWSWorkflowExecutor.setCallbackURL("http://localhost:8243/workflow-callback");

		PowerMockito.mockStatic(ApiMgtDAO.class);
		apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
		serviceClient = Mockito.mock(ServiceClient.class);
		PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
	}

	@Test
	public void testRetrievingWorkFlowType() {
		Assert.assertEquals(subscriptionCreationWSWorkflowExecutor.getWorkflowType(), "AM_SUBSCRIPTION_CREATION");
	}

	@Test
	public void testWorkflowApprove() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.APPROVED);
		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.UNBLOCKED);

		subscriptionCreationWSWorkflowExecutor.complete(workflowDTO);
		Mockito.verify(apiMgtDAO, Mockito.times(1)).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.UNBLOCKED);

	}

	@Test
	public void testWorkflowReject() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.REJECTED);
		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		subscriptionCreationWSWorkflowExecutor.complete(workflowDTO);
		Mockito.verify(apiMgtDAO, Mockito.times(1)).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);
	}
	
	@Test (expected = WorkflowException.class)
	public void testWorkflowRejectException() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.REJECTED);
		PowerMockito.doThrow(new APIManagementException("")).when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		subscriptionCreationWSWorkflowExecutor.complete(workflowDTO);

	}
	
	@Test (expected = WorkflowException.class)
	public void testWorkflowApproveException() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.APPROVED);
		PowerMockito.doThrow(new APIManagementException("")).when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.UNBLOCKED);

		subscriptionCreationWSWorkflowExecutor.complete(workflowDTO);

	}

	@Test
	public void testWorkflowNotAllowedStatus() throws APIManagementException, WorkflowException {
		WorkflowDTO workflowDTO = new WorkflowDTO();
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setStatus(WorkflowStatus.CREATED);

		subscriptionCreationWSWorkflowExecutor.complete(workflowDTO);
		// shouldn't update status
		Mockito.verify(apiMgtDAO, Mockito.never()).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.UNBLOCKED);
		Mockito.verify(apiMgtDAO, Mockito.never()).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);
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
			subscriptionCreationWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getExternalWorkflowReference());
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
		subscriptionCreationWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getExternalWorkflowReference());

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
		subscriptionCreationWSWorkflowExecutor.cleanUpPendingTask(workflowDTO.getExternalWorkflowReference());

	}



	@Test
	public void testWorkflowExecute() throws Exception {
		SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
		workflowDTO.setApiContext("/test");
		workflowDTO.setApiName("TestAPI");
		workflowDTO.setApiVersion("1.0");
		workflowDTO.setApiProvider("admin");
		workflowDTO.setSubscriber("admin");
		workflowDTO.setApplicationName("TestApp");
		workflowDTO.setTierName("Gold");
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
			Assert.assertNotNull(subscriptionCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}

	}
	
	@Test(expected = WorkflowException.class)
	public void testWorkflowExecuteException() throws Exception {
		SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
		workflowDTO.setApiContext("/test");
		workflowDTO.setApiName("TestAPI");
		workflowDTO.setApiVersion("1.0");
		workflowDTO.setApiProvider("admin");
		workflowDTO.setSubscriber("admin");
		workflowDTO.setApplicationName("TestApp");
		workflowDTO.setTierName("Gold");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();
		subscriptionCreationWSWorkflowExecutor.execute(workflowDTO);

	}

	@Test(expected = WorkflowException.class)
	public void testWorkflowExecuteExceptionWhenMessageProcessingFailed() throws Exception {
		SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
		workflowDTO.setApiContext("/test");
		workflowDTO.setApiName("TestAPI");
		workflowDTO.setApiVersion("1.0");
		workflowDTO.setApiProvider("admin");
		workflowDTO.setSubscriber("admin");
		workflowDTO.setApplicationName("TestApp");
		workflowDTO.setTierName("Gold");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);
		PowerMockito.mockStatic(AXIOMUtil.class);
		PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(new XMLStreamException("Error " +
				"converting String to OMElement"));
		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();
		subscriptionCreationWSWorkflowExecutor.execute(workflowDTO);
	}

	@Test
	public void testWorkflowExecuteWithoutExecutorParam() throws Exception {
		SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
		workflowDTO.setApiContext("/test");
		workflowDTO.setApiName("TestAPI");
		workflowDTO.setApiVersion("1.0");
		workflowDTO.setApiProvider("admin");
		workflowDTO.setSubscriber("admin");
		workflowDTO.setApplicationName("TestApp");
		workflowDTO.setTierName("Gold");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
		workflowDTO.setCallbackUrl("http://test");

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		PowerMockito.whenNew(ServiceClient.class)
				.withArguments(Mockito.any(ConfigurationContext.class), Mockito.any(AxisService.class))
				.thenReturn(serviceClient);

		subscriptionCreationWSWorkflowExecutor.setUsername(null);
		subscriptionCreationWSWorkflowExecutor.setPassword(null);
		try {
			// shouldn't fail. this checks for unsecured enpoint use case
			Assert.assertNotNull(subscriptionCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}
		//empty values
		subscriptionCreationWSWorkflowExecutor.setUsername("");
		subscriptionCreationWSWorkflowExecutor.setPassword("".toCharArray());
		try {
			// shouldn't fail. this checks for unsecured enpoint use case
			Assert.assertNotNull(subscriptionCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}
		
		//one empty value and other null
		subscriptionCreationWSWorkflowExecutor.setUsername("");
		subscriptionCreationWSWorkflowExecutor.setPassword(null);
		try {
			// shouldn't fail. this checks for unsecured enpoint use case
			Assert.assertNotNull(subscriptionCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}
		
		subscriptionCreationWSWorkflowExecutor.setUsername(null);
		subscriptionCreationWSWorkflowExecutor.setPassword("".toCharArray());
		try {
			// shouldn't fail. this checks for unsecured enpoint use case
			Assert.assertNotNull(subscriptionCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}
		
		//without a password
		subscriptionCreationWSWorkflowExecutor.setUsername("admin");
		subscriptionCreationWSWorkflowExecutor.setPassword("".toCharArray());
		try {
			// shouldn't fail. this checks for unsecured enpoint use case. 
			Assert.assertNotNull(subscriptionCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}
		
		subscriptionCreationWSWorkflowExecutor.setUsername("admin");
		subscriptionCreationWSWorkflowExecutor.setPassword(null);
		try {
			// shouldn't fail. this checks for unsecured enpoint use case. 
			Assert.assertNotNull(subscriptionCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}
	}

	@Test
	public void testWorkflowExecuteWithDifferentMediatype() throws Exception {
		SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
		workflowDTO.setApiContext("/test");
		workflowDTO.setApiName("TestAPI");
		workflowDTO.setApiVersion("1.0");
		workflowDTO.setApiProvider("admin");
		workflowDTO.setSubscriber("admin");
		workflowDTO.setApplicationName("TestApp");
		workflowDTO.setTierName("Gold");
		workflowDTO.setWorkflowReference("1");
		workflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());

		PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(
				Integer.parseInt(workflowDTO.getWorkflowReference()), APIConstants.SubscriptionStatus.REJECTED);

		ServiceReferenceHolderMockCreator serviceRefMock = new ServiceReferenceHolderMockCreator(-1234);
		ServiceReferenceHolderMockCreator.initContextService();

		PowerMockito.whenNew(ServiceClient.class)
				.withArguments(Mockito.any(ConfigurationContext.class), Mockito.any(AxisService.class))
				.thenReturn(serviceClient);

		subscriptionCreationWSWorkflowExecutor.setContentType("application/xml");
		try {
			// shouldn't fail.
			Assert.assertNotNull(subscriptionCreationWSWorkflowExecutor.execute(workflowDTO));
		} catch (WorkflowException e) {
			Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation ws workflow");
		}

	}

	@Test
	public void testExecutorProperties() throws WorkflowException {
		subscriptionCreationWSWorkflowExecutor.setPassword("admin".toCharArray());
		subscriptionCreationWSWorkflowExecutor.setUsername("admin");
		subscriptionCreationWSWorkflowExecutor.setServiceEndpoint("http://localhost:9445/service");
		subscriptionCreationWSWorkflowExecutor.setCallbackURL("http://localhost:8243/workflow-callback");
		subscriptionCreationWSWorkflowExecutor.setContentType("application/xml");

		Assert.assertEquals("Invalid password", "admin",
				String.valueOf(subscriptionCreationWSWorkflowExecutor.getPassword()));
		Assert.assertEquals("Invalid user", "admin", subscriptionCreationWSWorkflowExecutor.getUsername());
		Assert.assertEquals("Invalid callback", "http://localhost:8243/workflow-callback",
				subscriptionCreationWSWorkflowExecutor.getCallbackURL());
		Assert.assertEquals("Invalid service ep", "http://localhost:9445/service",
				subscriptionCreationWSWorkflowExecutor.getServiceEndpoint());
		Assert.assertEquals("Invalid content type", "application/xml",
				subscriptionCreationWSWorkflowExecutor.getContentType());

		Assert.assertNull(subscriptionCreationWSWorkflowExecutor.getWorkflowDetails(""));

	}
}
