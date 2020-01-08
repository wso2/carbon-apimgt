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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

/**
 * ApplicationCreationSimpleWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class ApplicationCreationSimpleWorkflowExecutorTest {

    private ApplicationCreationSimpleWorkflowExecutor applicationCreationSimpleWorkflowExecutor;
    private ApiMgtDAO apiMgtDAO;
    private WorkflowDTO workflowDTO;

    @Before
    public void init() {
        applicationCreationSimpleWorkflowExecutor = new ApplicationCreationSimpleWorkflowExecutor();
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        workflowDTO = new ApplicationWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
    }

    @Test
    public void testRetrievingWorkFlowType() {
        Assert.assertEquals(applicationCreationSimpleWorkflowExecutor.getWorkflowType(), "AM_APPLICATION_CREATION");
    }

    @Test
    public void testExecutingApplicationDeletionWorkFlow() throws APIManagementException {
       PowerMockito.doNothing().when(apiMgtDAO).updateApplicationStatus(Mockito.anyInt(), Mockito.anyString());
        try {
            //Test workflow execution when workflow status = 'APPROVED'
            workflowDTO.setStatus(WorkflowStatus.APPROVED);
            Assert.assertNotNull(applicationCreationSimpleWorkflowExecutor.execute(workflowDTO));

        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing application deletion simple workflow");
        }
    }

    @Test
    public void testCompletingApplicationDeletionWorkflow() {
        try {
            //Test workflow execution when workflow status = 'CREATED'
            workflowDTO.setStatus(WorkflowStatus.CREATED);
            Assert.assertNotNull(applicationCreationSimpleWorkflowExecutor.complete(workflowDTO));

            //Test workflow execution when workflow status = 'REJECTED'
            workflowDTO.setStatus(WorkflowStatus.REJECTED);
            Assert.assertNotNull(applicationCreationSimpleWorkflowExecutor.complete(workflowDTO));

            //Test workflow execution when workflow status = 'APPROVED'
            workflowDTO.setStatus(WorkflowStatus.APPROVED);
            Assert.assertNotNull(applicationCreationSimpleWorkflowExecutor.complete(workflowDTO));

        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing application deletion simple workflow");
        }
    }

    @Test
    public void testFailureWhileExecutingApplicationDeletionWorkFlow() throws APIManagementException {
        PowerMockito.doThrow(new APIManagementException("Error occurred while updating application status")).when
                (apiMgtDAO).updateApplicationStatus(Mockito.anyInt(), Mockito.anyString());
        try {
            //Test workflow execution when workflow status = 'CREATED'
            workflowDTO.setStatus(WorkflowStatus.CREATED);
            applicationCreationSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException is not thrown while executing application deletion workflow");

        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Error occured when updating the status of the Application " +
                    "creation process"));
        }
    }

    @Test
    public void testGetWorkflowDetails(){
        try {
            applicationCreationSimpleWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }

}
