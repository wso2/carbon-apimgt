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

package org.wso2.carbon.apimgt.core.workflow;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;

public class ApprovalWorkflowExecutorTestCase {

    @Test(description = "Test workflow responses")
    public void testWorkflowResponses() throws WorkflowException {
        
        WorkflowExecutor executor = new ApprovalWorkflowExecutor();

        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        Workflow workflow = new SubscriptionCreationWorkflow(apiSubscriptionDAO, workflowDAO, apiGateway);
   
        WorkflowResponse respone = executor.execute(workflow);
        
        Assert.assertEquals(respone.getJSONPayload(), "");
        Assert.assertEquals(respone.getWorkflowStatus(), WorkflowStatus.CREATED);
        
        workflow.setStatus(WorkflowStatus.APPROVED);
        respone = executor.complete(workflow);
        
        Assert.assertEquals(respone.getWorkflowStatus(), WorkflowStatus.APPROVED);
        
        executor.cleanUpPendingTask(workflow.getExternalWorkflowReference());
        

    }
    

}
