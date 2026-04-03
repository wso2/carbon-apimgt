/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Tests for application deletion workflow executor covering scenarios with
 * generated OAuth keys. These tests specifically target the flow described in
 * issue #4866 where the deletion of an application with generated keys causes
 * errors in the IS OAuthApplicationMgtListener.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class ApplicationDeletionWorkflowWithKeysTest {

    private ApplicationDeletionSimpleWorkflowExecutor workflowExecutor;
    private ApiMgtDAO apiMgtDAO;
    private ApplicationWorkflowDTO workflowDTO;
    private Application application;

    @Before
    public void init() {
        workflowExecutor = new ApplicationDeletionSimpleWorkflowExecutor();
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        application = new Application("TestAppWithKeys", new Subscriber("testUser"));
        application.setId(100);
        application.setUUID("app-uuid-with-keys");

        workflowDTO = new ApplicationWorkflowDTO();
        workflowDTO.setWorkflowReference("100");
        workflowDTO.setApplication(application);
    }

    /**
     * Test that the workflow executor successfully deletes an application.
     * The DAO's deleteApplication internally calls keyManager.deleteApplication,
     * and when the DAO completes without error the workflow should succeed.
     */
    @Test
    public void testSuccessfulDeletionOfApplicationWithKeys() throws Exception {
        PowerMockito.doNothing().when(apiMgtDAO).deleteApplication(application);

        WorkflowResponse response = workflowExecutor.execute(workflowDTO);

        Assert.assertNotNull(response);
        verify(apiMgtDAO, times(1)).deleteApplication(application);
    }

    /**
     * Test that the workflow executor propagates errors from the DAO layer.
     * If the DAO's deleteApplication throws (e.g., due to database errors),
     * the workflow should throw WorkflowException.
     */
    @Test
    public void testDeletionFailsWhenDAOThrowsException() throws Exception {
        Mockito.doThrow(new APIManagementException("Database error during deletion"))
                .when(apiMgtDAO).deleteApplication(application);

        try {
            workflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Database error during deletion"));
        }
    }

    /**
     * Test the complete() method directly, which is what actually calls
     * apiMgtDAO.deleteApplication(). The execute() method sets status to
     * APPROVED and then calls complete().
     */
    @Test
    public void testCompleteMethodCallsDAODelete() throws Exception {
        PowerMockito.doNothing().when(apiMgtDAO).deleteApplication(application);

        WorkflowResponse response = workflowExecutor.complete(workflowDTO);

        Assert.assertNotNull(response);
        verify(apiMgtDAO, times(1)).deleteApplication(application);
    }

    /**
     * Test that after execute(), the workflow status is set to APPROVED.
     * This ensures the deletion is immediately approved in the simple workflow.
     */
    @Test
    public void testWorkflowStatusIsApprovedAfterExecution() throws Exception {
        PowerMockito.doNothing().when(apiMgtDAO).deleteApplication(application);

        workflowExecutor.execute(workflowDTO);

        Assert.assertEquals(WorkflowStatus.APPROVED, workflowDTO.getStatus());
    }

    /**
     * Test deletion when the APIManagementException has a null message.
     * The workflow executor should generate a fallback error message.
     */
    @Test
    public void testDeletionFailsWithNullErrorMessage() throws Exception {
        Mockito.doThrow(new APIManagementException((String) null))
                .when(apiMgtDAO).deleteApplication(application);

        try {
            workflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException was not thrown");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains(
                    "Couldn't complete simple application deletion workflow for application: TestAppWithKeys"));
        }
    }
}
