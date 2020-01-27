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
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

/**
 * SubscriptionCreationSimpleWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class SubscriptionCreationSimpleWorkflowExecutorTest {

    private SubscriptionCreationSimpleWorkflowExecutor subscriptionCreationSimpleWorkflowExecutor;
    private ApiMgtDAO apiMgtDAO;

    @Before
    public void init() {
        subscriptionCreationSimpleWorkflowExecutor = new SubscriptionCreationSimpleWorkflowExecutor();
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
    }

    @Test
    public void testRetrievingWorkFlowType() {
        Assert.assertEquals(subscriptionCreationSimpleWorkflowExecutor.getWorkflowType(), "AM_SUBSCRIPTION_CREATION");
    }

    @Test
    public void testExecutingSubscriptionCreationWorkFlow() throws APIManagementException {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        try {
            PowerMockito.doNothing().when(apiMgtDAO).updateSubscriptionStatus(1, "UNBLOCKED");
            Assert.assertNotNull(subscriptionCreationSimpleWorkflowExecutor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing Subscription creation simple workflow");
        }
    }

    @Test
    public void testFailureWhileExecutingSubscriptionCreationWorkFlow() throws APIManagementException {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        try {
            PowerMockito.doThrow(new APIManagementException("Could not complete subscription creation workflow"))
                    .when(apiMgtDAO).updateSubscriptionStatus(1, "UNBLOCKED");
            subscriptionCreationSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException is not thrown when subscription creation simple workflow" +
                    " execution failed");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Could not complete subscription creation workflow"));
        }
    }

    @Test
    public void testGetWorkflowDetails(){
        try {
            subscriptionCreationSimpleWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }
}
