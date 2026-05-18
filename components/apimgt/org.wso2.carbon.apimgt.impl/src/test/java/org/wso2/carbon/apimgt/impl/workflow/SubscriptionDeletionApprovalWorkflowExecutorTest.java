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
 * SubscriptionDeletionApprovalWorkflowExecutor test cases.
 *
 * Covers the following transitions:
 *   Deletion Flow — admin approves:  DELETE PENDING → No Subscription (removed)
 *   Rollback Deletion — admin rejects: DELETE PENDING → UNBLOCKED
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class SubscriptionDeletionApprovalWorkflowExecutorTest {

    private SubscriptionDeletionApprovalWorkflowExecutor executor;
    private ApiMgtDAO apiMgtDAO;

    @Before
    public void init() {
        executor = new SubscriptionDeletionApprovalWorkflowExecutor();
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
    }

    @Test
    public void testGetWorkflowType() {
        Assert.assertEquals(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION, executor.getWorkflowType());
    }

    @Test
    public void testExecuteSubscriptionDeletionApprovalWorkflow() {
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
     * Deletion Flow: DELETE PENDING → admin approves → subscription removed (No Subscription).
     * Verifies that completing the deletion workflow with APPROVED status calls removeSubscriptionById.
     */
    @Test
    public void testCompleteSubscriptionDeletionApprovedRemovesSubscription()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("7");
        workflowDTO.setStatus(WorkflowStatus.APPROVED);

        executor.complete(workflowDTO);

        Mockito.verify(apiMgtDAO, Mockito.times(1)).removeSubscriptionById(7);
        Mockito.verify(apiMgtDAO, Mockito.never())
                .updateSubscriptionStatus(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testCompleteSubscriptionDeletionApprovedDAOExceptionThrowsWorkflowException()
            throws APIManagementException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("7");
        workflowDTO.setStatus(WorkflowStatus.APPROVED);

        Mockito.doThrow(new APIManagementException("DB error")).when(apiMgtDAO).removeSubscriptionById(7);

        try {
            executor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue("Expected error message to mention subscription deletion workflow",
                    e.getMessage().contains("subscription deletion workflow"));
        }
    }

    /**
     * Rollback Deletion: DELETE PENDING → admin rejects → UNBLOCKED.
     * Verifies that completing the deletion workflow with REJECTED status restores the subscription
     * to UNBLOCKED without removing it.
     */
    @Test
    public void testCompleteSubscriptionDeletionRejectedRestoresStatusToUnblocked()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("7");
        workflowDTO.setStatus(WorkflowStatus.REJECTED);

        executor.complete(workflowDTO);

        Mockito.verify(apiMgtDAO, Mockito.times(1))
                .updateSubscriptionStatus(7, APIConstants.SubscriptionStatus.UNBLOCKED);
        Mockito.verify(apiMgtDAO, Mockito.never()).removeSubscriptionById(Mockito.anyInt());
    }

    @Test
    public void testCompleteSubscriptionDeletionRejectedDAOExceptionThrowsWorkflowException()
            throws APIManagementException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("7");
        workflowDTO.setStatus(WorkflowStatus.REJECTED);

        Mockito.doThrow(new APIManagementException("DB error"))
                .when(apiMgtDAO).updateSubscriptionStatus(7, APIConstants.SubscriptionStatus.UNBLOCKED);

        try {
            executor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            // When the DAO exception has a message, it is propagated directly as the WorkflowException message
            Assert.assertEquals("DB error", e.getMessage());
        }
    }

    /**
     * When the DAO exception has a null message (uncommon but possible), the executor falls back
     * to a hardcoded message rather than propagating null.
     */
    @Test
    public void testCompleteSubscriptionDeletionRejectedNullExceptionMessage_fallsBackToHardcodedMessage()
            throws APIManagementException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("7");
        workflowDTO.setStatus(WorkflowStatus.REJECTED);

        APIManagementException nullMsgException = Mockito.mock(APIManagementException.class);
        Mockito.when(nullMsgException.getMessage()).thenReturn(null);
        Mockito.doThrow(nullMsgException)
                .when(apiMgtDAO).updateSubscriptionStatus(7, APIConstants.SubscriptionStatus.UNBLOCKED);

        try {
            executor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertNotNull("WorkflowException must have a non-null message even when DAO exception message is null",
                    e.getMessage());
            Assert.assertTrue(e.getMessage().contains("simple application deletion workflow"));
        }
    }

    /**
     * Verifies that complete() with a neutral status (e.g. CREATED) is a no-op —
     * neither removeSubscriptionById nor updateSubscriptionStatus is called.
     */
    @Test
    public void testCompleteSubscriptionDeletionWithNeutralStatus_isNoOp()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("7");
        workflowDTO.setStatus(WorkflowStatus.CREATED);

        executor.complete(workflowDTO);

        Mockito.verify(apiMgtDAO, Mockito.never()).removeSubscriptionById(Mockito.anyInt());
        Mockito.verify(apiMgtDAO, Mockito.never()).updateSubscriptionStatus(Mockito.anyInt(), Mockito.anyString());
    }

    /**
     * Verifies that execute() sets the human-readable workflow description and the
     * tier-name properties consumed by the admin workflow UI.
     */
    @Test
    public void testExecuteSubscriptionDeletionSetsDescriptionAndTierProperties() throws WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        workflowDTO.setApiName("WeatherAPI");
        workflowDTO.setApiVersion("v1");
        workflowDTO.setSubscriber("testUser");
        workflowDTO.setApplicationName("TestApp");
        workflowDTO.setTierName("Gold");
        workflowDTO.setRequestedTierName("Silver");

        executor.execute(workflowDTO);

        String description = workflowDTO.getWorkflowDescription();
        Assert.assertNotNull("Workflow description must be set by execute()", description);
        Assert.assertTrue(description.contains("WeatherAPI"));
        Assert.assertTrue(description.contains("v1"));
        Assert.assertTrue(description.contains("testUser"));
        Assert.assertTrue(description.contains("TestApp"));
    }

    /**
     * deleteMonetizedSubscription(API) delegates to execute() and must return a non-null response.
     */
    @Test
    public void testDeleteMonetizedSubscriptionWithAPI_delegatesToExecute() throws WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        workflowDTO.setApiName("WeatherAPI");
        workflowDTO.setApiVersion("v1");
        workflowDTO.setSubscriber("testUser");
        workflowDTO.setApplicationName("TestApp");

        org.wso2.carbon.apimgt.api.model.API api =
                Mockito.mock(org.wso2.carbon.apimgt.api.model.API.class);

        Assert.assertNotNull(executor.deleteMonetizedSubscription(workflowDTO, api));
    }

    /**
     * deleteMonetizedSubscription(APIProduct) delegates to execute() and must return a non-null response.
     */
    @Test
    public void testDeleteMonetizedSubscriptionWithAPIProduct_delegatesToExecute() throws WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        workflowDTO.setApiName("WeatherAPI");
        workflowDTO.setApiVersion("v1");
        workflowDTO.setSubscriber("testUser");
        workflowDTO.setApplicationName("TestApp");

        org.wso2.carbon.apimgt.api.model.APIProduct product =
                Mockito.mock(org.wso2.carbon.apimgt.api.model.APIProduct.class);

        Assert.assertNotNull(executor.deleteMonetizedSubscription(workflowDTO, product));
    }

    @Test
    public void testCleanUpPendingTaskDeletesWorkflowRequest() throws APIManagementException {
        String workflowExtRef = "deletion-ext-ref-456";
        try {
            executor.cleanUpPendingTask(workflowExtRef);
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException during cleanUpPendingTask: " + e.getMessage());
        }
        Mockito.verify(apiMgtDAO, Mockito.times(1)).deleteWorkflowRequest(workflowExtRef);
    }

    @Test
    public void testCleanUpPendingTaskDAOExceptionThrowsWorkflowException() throws APIManagementException {
        String workflowExtRef = "deletion-ext-ref-456";
        Mockito.doThrow(new APIManagementException("DB error"))
                .when(apiMgtDAO).deleteWorkflowRequest(workflowExtRef);

        try {
            executor.cleanUpPendingTask(workflowExtRef);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue("Expected error message to mention cancellation of pending deletion approval",
                    e.getMessage().contains("cancel pending subscription deletion"));
        }
    }
}
