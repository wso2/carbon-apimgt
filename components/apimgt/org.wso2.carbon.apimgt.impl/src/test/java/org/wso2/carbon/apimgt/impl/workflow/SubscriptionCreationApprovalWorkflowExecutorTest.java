/*
 *   Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.workflow;

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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;

/**
 * SubscriptionCreationApprovalWorkflowExecutor test cases.
 *
 * Covers the following transitions:
 *   Creation Flow — admin approves:  ON HOLD → UNBLOCKED
 *   Admin Rejection:                 ON HOLD → REJECTED
 *   Illegal Approval Path (negative): ON HOLD admin-reject must NOT go to UNBLOCKED
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class SubscriptionCreationApprovalWorkflowExecutorTest {

    private SubscriptionCreationApprovalWorkflowExecutor executor;
    private ApiMgtDAO apiMgtDAO;

    @Before
    public void init() {
        executor = new SubscriptionCreationApprovalWorkflowExecutor();
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
    }

    @Test
    public void testGetWorkflowType() {
        Assert.assertEquals(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION, executor.getWorkflowType());
    }

    @Test
    public void testExecuteSubscriptionCreationApprovalWorkflow() {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        workflowDTO.setApiName("WeatherAPI");
        workflowDTO.setApiVersion("v1");
        workflowDTO.setSubscriber("testUser");
        workflowDTO.setApplicationName("TestApp");
        try {
            Assert.assertNotNull(executor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException during execute: " + e.getMessage());
        }
    }

    /**
     * Creation Flow: ON HOLD → admin approves → UNBLOCKED.
     * Verifies that completing the creation workflow with APPROVED status
     * calls updateSubscriptionStatus with UNBLOCKED.
     */
    @Test
    public void testCompleteSubscriptionCreationApprovedUpdatesStatusToUnblocked()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("5");
        workflowDTO.setStatus(WorkflowStatus.APPROVED);

        executor.complete(workflowDTO);

        Mockito.verify(apiMgtDAO, Mockito.times(1))
                .updateSubscriptionStatus(5, APIConstants.SubscriptionStatus.UNBLOCKED);
    }

    @Test
    public void testCompleteSubscriptionCreationApprovedDAOExceptionThrowsWorkflowException()
            throws APIManagementException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("5");
        workflowDTO.setStatus(WorkflowStatus.APPROVED);

        Mockito.doThrow(new APIManagementException("DB error"))
                .when(apiMgtDAO).updateSubscriptionStatus(5, APIConstants.SubscriptionStatus.UNBLOCKED);

        try {
            executor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Could not complete subscription creation workflow"));
        }
    }

    /**
     * Admin Rejection: ON HOLD → admin rejects → REJECTED.
     * Also validates Illegal Approval Path (negative): an admin rejection must NOT set the subscription to UNBLOCKED.
     */
    @Test
    public void testCompleteSubscriptionCreationRejectedUpdatesStatusToRejected()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("5");
        workflowDTO.setStatus(WorkflowStatus.REJECTED);

        executor.complete(workflowDTO);

        // Must go to REJECTED — proves the illegal approval path is blocked
        Mockito.verify(apiMgtDAO, Mockito.times(1))
                .updateSubscriptionStatus(5, APIConstants.SubscriptionStatus.REJECTED);
        Mockito.verify(apiMgtDAO, Mockito.never())
                .updateSubscriptionStatus(5, APIConstants.SubscriptionStatus.UNBLOCKED);
    }

    @Test
    public void testCompleteSubscriptionCreationRejectedDAOExceptionThrowsWorkflowException()
            throws APIManagementException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("5");
        workflowDTO.setStatus(WorkflowStatus.REJECTED);

        Mockito.doThrow(new APIManagementException("DB error"))
                .when(apiMgtDAO).updateSubscriptionStatus(5, APIConstants.SubscriptionStatus.REJECTED);

        try {
            executor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Could not complete subscription creation workflow"));
        }
    }

    @Test
    public void testCleanUpPendingTaskDeletesWorkflowRequest() throws APIManagementException {
        String workflowExtRef = "creation-ext-ref-123";
        try {
            executor.cleanUpPendingTask(workflowExtRef);
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException during cleanUpPendingTask: " + e.getMessage());
        }
        Mockito.verify(apiMgtDAO, Mockito.times(1)).deleteWorkflowRequest(workflowExtRef);
    }

    @Test
    public void testCleanUpPendingTaskDAOExceptionThrowsWorkflowException() throws APIManagementException {
        String workflowExtRef = "creation-ext-ref-123";
        Mockito.doThrow(new APIManagementException("DB error"))
                .when(apiMgtDAO).deleteWorkflowRequest(workflowExtRef);

        try {
            executor.cleanUpPendingTask(workflowExtRef);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue("Expected error message to mention cancellation of pending approval",
                    e.getMessage().contains("cancel pending subscription approval"));
        }
    }

    /**
     * Verifies that execute() sets the human-readable workflow description and all
     * properties required by the admin workflow UI.
     */
    @Test
    public void testExecuteSubscriptionCreationSetsWorkflowDescriptionAndProperties()
            throws WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        workflowDTO.setApiName("WeatherAPI");
        workflowDTO.setApiVersion("v1");
        workflowDTO.setSubscriber("testUser");
        workflowDTO.setApplicationName("TestApp");

        executor.execute(workflowDTO);

        String description = workflowDTO.getWorkflowDescription();
        Assert.assertNotNull("Workflow description must be set by execute()", description);
        Assert.assertTrue(description.contains("WeatherAPI"));
        Assert.assertTrue(description.contains("v1"));
        Assert.assertTrue(description.contains("testUser"));
        Assert.assertTrue(description.contains("TestApp"));
    }

    /**
     * Verifies that complete() with a status other than APPROVED or REJECTED (e.g. CREATED)
     * is a no-op — no subscription status update is written to the database.
     */
    @Test
    public void testCompleteSubscriptionCreationWithNeutralStatus_isNoOp()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("5");
        workflowDTO.setStatus(WorkflowStatus.CREATED);

        executor.complete(workflowDTO);

        Mockito.verify(apiMgtDAO, Mockito.never())
                .updateSubscriptionStatus(Mockito.anyInt(), Mockito.anyString());
    }
}
