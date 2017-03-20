/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.core.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;

public class WorkflowExecutorFactoryTestCase {
    @Test(description = "Test workflow executors creating")
    public void testWorkflowCreation() {
        Workflow appCreation = WorkflowExecutorFactory.getInstance()
                .createWorkflow(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        Assert.assertEquals(appCreation.getWorkflowType(), WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);

        Workflow prodAppReg = WorkflowExecutorFactory.getInstance()
                .createWorkflow(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
        Assert.assertEquals(prodAppReg.getWorkflowType(),
                WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);

        Workflow sandboxAppReg = WorkflowExecutorFactory.getInstance()
                .createWorkflow(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
        Assert.assertEquals(sandboxAppReg.getWorkflowType(),
                WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);

        Workflow subcreation = WorkflowExecutorFactory.getInstance()
                .createWorkflow(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        Assert.assertEquals(subcreation.getWorkflowType(), WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);

        Workflow signup = WorkflowExecutorFactory.getInstance()
                .createWorkflow(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);
        Assert.assertEquals(signup.getWorkflowType(), WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);

        Workflow apistate = WorkflowExecutorFactory.getInstance()
                .createWorkflow(WorkflowConstants.WF_TYPE_AM_API_STATE);
        Assert.assertEquals(apistate.getWorkflowType(), WorkflowConstants.WF_TYPE_AM_API_STATE);

    }
   /* @Test(description = "Exception when workflow executors creation" ,expectedExceptions = WorkflowException.class)
    public void testWorkflowCreationException() throws Exception {
        
     
        
        WorkflowExecutor executor = WorkflowExecutorFactory.getInstance().getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        
        
    }
    
*/
}
