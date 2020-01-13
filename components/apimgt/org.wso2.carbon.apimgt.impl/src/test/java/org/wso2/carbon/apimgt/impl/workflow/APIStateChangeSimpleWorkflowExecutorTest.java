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
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

/**
 * APIStateChangeSimpleWorkflowExecutor test cases
 */
public class APIStateChangeSimpleWorkflowExecutorTest {

    private APIStateChangeSimpleWorkflowExecutor apiStateChangeSimpleWorkflowExecutor;

    @Before
    public void init() {
        apiStateChangeSimpleWorkflowExecutor = new APIStateChangeSimpleWorkflowExecutor();
    }

    @Test
    public void testRetrievingWorkFlowType() {
        Assert.assertEquals(apiStateChangeSimpleWorkflowExecutor.getWorkflowType(), "AM_API_STATE");
    }

    @Test
    public void testExecutingAPIStateChangeWorkFlow() {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        try {
            Assert.assertNotNull(apiStateChangeSimpleWorkflowExecutor.execute(workflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing API state change simple workflow");
        }
    }

    @Test
    public void testRetrievingWorkFlowDetails(){
        try {
            Assert.assertTrue(apiStateChangeSimpleWorkflowExecutor.getWorkflowDetails("APPROVED").isEmpty());
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while retrieving Workflow details");
        }
    }

    @Test
    public void testGetWorkflowDetails(){
        try {
            apiStateChangeSimpleWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }

}
