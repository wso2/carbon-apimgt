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
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;

/**
 * ApplicationDeletionSimpleWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class ApplicationDeletionSimpleWorkflowExecutorTest {

    private ApplicationDeletionSimpleWorkflowExecutor applicationDeletionSimpleWorkflowExecutor;
    private ApiMgtDAO apiMgtDAO;
    private ApplicationWorkflowDTO workflowDTO;
    private Application application;

    @Before
    public void init() {
        applicationDeletionSimpleWorkflowExecutor = new ApplicationDeletionSimpleWorkflowExecutor();
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        workflowDTO = new ApplicationWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        application = new Application("test", new Subscriber("testUser"));
        workflowDTO.setApplication(application);
    }

    @Test
    public void testRetrievingWorkFlowType() {
        Assert.assertEquals(applicationDeletionSimpleWorkflowExecutor.getWorkflowType(), "AM_APPLICATION_DELETION");
    }

    @Test
    public void testExecutingApplicationDeletionWorkFlow() throws APIManagementException {
        PowerMockito.doNothing().when(apiMgtDAO).deleteApplication(application);
        try {
            Assert.assertNotNull(applicationDeletionSimpleWorkflowExecutor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing application deletion simple workflow");
        }
    }

    @Test
    public void testFailureWhileExecutingApplicationDeletionWorkFlow() throws APIManagementException {
        //Test throwing WorkflowException with an error message
        Mockito.doThrow(new APIManagementException("Could not complete application deletion workflow"))
                .when(apiMgtDAO).deleteApplication(application);
        try {
            applicationDeletionSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException is not thrown when application deletion simple workflow" +
                    " execution failed");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Could not complete application deletion workflow"));
        }

        //Test throwing WorkflowException without an empty error message
        Mockito.doThrow(APIManagementException.class).when(apiMgtDAO).deleteApplication(application);
        try {
            applicationDeletionSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException is not thrown when application deletion simple workflow" +
                    " execution failed");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Couldn't complete simple application deletion workflow for " +
                    "application: " +application.getName()));
        }
    }


    @Test
    public void testGetWorkflowDetails(){
        try {
            applicationDeletionSimpleWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }
}
