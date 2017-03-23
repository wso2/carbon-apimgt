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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test for workflowDAO
 */
public class WorkflowDAOIT extends DAOIntegrationTestBase {

    @Test
    public void testAddWorkflowEntry() throws Exception {

        String workflowRefId = UUID.randomUUID().toString();
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowRefId);
        workflowDAO.addWorkflowEntry(workflow);
        Workflow retrieveWorflow = workflowDAO.retrieveWorkflow(workflow.getExternalWorkflowReference());

        Assert.assertEquals(retrieveWorflow.getExternalWorkflowReference(), workflowRefId);
        Assert.assertEquals(retrieveWorflow.getStatus(), workflow.getStatus());
        Assert.assertEquals(retrieveWorflow.getWorkflowReference(), workflow.getWorkflowReference());
    }

    @Test
    public void testUpdateWorkflowStatus() throws Exception {
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        String workflowRefId = UUID.randomUUID().toString();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowRefId);
        workflowDAO.addWorkflowEntry(workflow);
        Workflow retrieveWorflow = workflowDAO.retrieveWorkflow(workflow.getExternalWorkflowReference());
        Assert.assertEquals(retrieveWorflow.getStatus(), workflow.getStatus());

        workflow.setStatus(WorkflowStatus.APPROVED);
        workflow.setUpdatedTime(LocalDateTime.now());
        workflowDAO.updateWorkflowStatus(workflow);
        retrieveWorflow = workflowDAO.retrieveWorkflow(workflow.getExternalWorkflowReference());

        Assert.assertEquals(retrieveWorflow.getStatus(), WorkflowStatus.APPROVED);
    }

}
