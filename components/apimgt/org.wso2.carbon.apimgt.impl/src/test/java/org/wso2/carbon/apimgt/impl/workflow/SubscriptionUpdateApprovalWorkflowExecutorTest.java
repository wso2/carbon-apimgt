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
 * SubscriptionUpdateApprovalWorkflowExecutor test cases.
 *
 * Covers the following transitions:
 *   Update Flow — admin approves:  TIER UPDATE PENDING → UNBLOCKED (tier applied)
 *   Update Flow — admin rejects/deletes: TIER UPDATE PENDING → UNBLOCKED (tier rolled back)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class SubscriptionUpdateApprovalWorkflowExecutorTest {

    private SubscriptionUpdateApprovalWorkflowExecutor executor;
    private ApiMgtDAO apiMgtDAO;

    @Before
    public void init() {
        executor = new SubscriptionUpdateApprovalWorkflowExecutor();
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
    }

    @Test
    public void testGetWorkflowType() {
        Assert.assertEquals(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE, executor.getWorkflowType());
    }

    /**
     * Update Flow: TIER UPDATE PENDING → admin approves → UNBLOCKED (requested tier applied).
     * Verifies that completing the update workflow with APPROVED status calls
     * updateSubscriptionStatusAndTier, which both activates the subscription and commits the new tier.
     */
    @Test
    public void testCompleteSubscriptionUpdateApprovedUpdatesStatusAndTierToUnblocked()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("3");
        workflowDTO.setStatus(WorkflowStatus.APPROVED);

        executor.complete(workflowDTO);

        Mockito.verify(apiMgtDAO, Mockito.times(1))
                .updateSubscriptionStatusAndTier(3, APIConstants.SubscriptionStatus.UNBLOCKED);
        Mockito.verify(apiMgtDAO, Mockito.never())
                .updateSubscriptionStatus(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testCompleteSubscriptionUpdateApprovedDAOExceptionThrowsWorkflowException()
            throws APIManagementException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("3");
        workflowDTO.setStatus(WorkflowStatus.APPROVED);

        Mockito.doThrow(new APIManagementException("DB error"))
                .when(apiMgtDAO).updateSubscriptionStatusAndTier(3, APIConstants.SubscriptionStatus.UNBLOCKED);

        try {
            executor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Could not complete subscription update workflow"));
        }
    }

    /**
     * Update Flow: TIER UPDATE PENDING → admin rejects/deletes → UNBLOCKED (tier rolled back).
     * Verifies that completing the update workflow with REJECTED status restores the subscription to
     * UNBLOCKED without applying the tier change (uses updateSubscriptionStatus, not
     * updateSubscriptionStatusAndTier).
     */
    @Test
    public void testCompleteSubscriptionUpdateRejectedRestoresStatusToUnblocked()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("3");
        workflowDTO.setStatus(WorkflowStatus.REJECTED);

        executor.complete(workflowDTO);

        Mockito.verify(apiMgtDAO, Mockito.times(1))
                .updateSubscriptionStatus(3, APIConstants.SubscriptionStatus.UNBLOCKED);
        Mockito.verify(apiMgtDAO, Mockito.never())
                .updateSubscriptionStatusAndTier(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testCompleteSubscriptionUpdateRejectedDAOExceptionThrowsWorkflowException()
            throws APIManagementException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("3");
        workflowDTO.setStatus(WorkflowStatus.REJECTED);

        Mockito.doThrow(new APIManagementException("DB error"))
                .when(apiMgtDAO).updateSubscriptionStatus(3, APIConstants.SubscriptionStatus.UNBLOCKED);

        try {
            executor.complete(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Could not complete subscription update workflow"));
        }
    }

    @Test
    public void testCleanUpPendingTaskDeletesWorkflowRequest() throws APIManagementException {
        String workflowExtRef = "update-ext-ref-789";
        try {
            executor.cleanUpPendingTask(workflowExtRef);
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException during cleanUpPendingTask: " + e.getMessage());
        }
        Mockito.verify(apiMgtDAO, Mockito.times(1)).deleteWorkflowRequest(workflowExtRef);
    }

    @Test
    public void testCleanUpPendingTaskDAOExceptionThrowsWorkflowException() throws APIManagementException {
        String workflowExtRef = "update-ext-ref-789";
        Mockito.doThrow(new APIManagementException("DB error"))
                .when(apiMgtDAO).deleteWorkflowRequest(workflowExtRef);

        try {
            executor.cleanUpPendingTask(workflowExtRef);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue("Expected error message to mention cancellation of pending update approval",
                    e.getMessage().contains("cancel pending subscription update"));
        }
    }

    /**
     * Verifies that complete() with a neutral status (e.g. CREATED) is a no-op —
     * neither updateSubscriptionStatusAndTier nor updateSubscriptionStatus is called.
     */
    @Test
    public void testCompleteSubscriptionUpdateWithNeutralStatus_isNoOp()
            throws APIManagementException, WorkflowException {
        SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
        workflowDTO.setWorkflowReference("3");
        workflowDTO.setStatus(WorkflowStatus.CREATED);

        executor.complete(workflowDTO);

        Mockito.verify(apiMgtDAO, Mockito.never())
                .updateSubscriptionStatusAndTier(Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(apiMgtDAO, Mockito.never())
                .updateSubscriptionStatus(Mockito.anyInt(), Mockito.anyString());
    }
}
