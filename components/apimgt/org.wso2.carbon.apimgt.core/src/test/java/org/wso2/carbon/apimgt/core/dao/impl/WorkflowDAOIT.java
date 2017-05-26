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
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.workflow.Workflow;

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
        Assert.assertEquals(retrieveWorflow.getAttributes(), workflow.getAttributes());
    }

    @Test
    public void testAddWorkflowEntryWithoutAttributes() throws Exception {

        String workflowRefId = UUID.randomUUID().toString();
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowRefId);
        workflow.setAttributes(null);
        
        workflowDAO.addWorkflowEntry(workflow);
        Workflow retrieveWorflow = workflowDAO.retrieveWorkflow(workflow.getExternalWorkflowReference());

        Assert.assertEquals(retrieveWorflow.getExternalWorkflowReference(), workflowRefId);
        Assert.assertEquals(retrieveWorflow.getStatus(), workflow.getStatus());
        Assert.assertEquals(retrieveWorflow.getWorkflowReference(), workflow.getWorkflowReference());
        Assert.assertNull(retrieveWorflow.getAttributes());
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
    
    @Test 
    public void testUpdateWorkflowStatusWithoutAddingEntry() throws Exception {
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        String workflowRefId = UUID.randomUUID().toString();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowRefId);
        workflow.setStatus(WorkflowStatus.APPROVED);
        workflow.setUpdatedTime(LocalDateTime.now());
        workflowDAO.updateWorkflowStatus(workflow);
        Workflow retrieveWorflow = workflowDAO.retrieveWorkflow(workflow.getExternalWorkflowReference());

        Assert.assertNull(retrieveWorflow, "workflow entry is in the database");
    }

    @Test
    public void testGetExternalWorkflowReferenceForAPIStateChange() throws Exception {
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        String workflowExtRefId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowExtRefId);
        workflow.setWorkflowReference(apiId);
        workflow.setStatus(WorkflowStatus.CREATED);
        workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_API_STATE);
        workflowDAO.addWorkflowEntry(workflow);
        String externalRefFromDb = workflowDAO.getExternalWorkflowReferenceForPendingTask(apiId,
                WorkflowConstants.WF_TYPE_AM_API_STATE);

        Assert.assertEquals(externalRefFromDb, workflowExtRefId);
    }

    @Test
    public void testGetExternalWorkflowReferenceForSubscription() throws Exception {
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        String workflowExtRefId = UUID.randomUUID().toString();
        String subscriptionId = UUID.randomUUID().toString();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowExtRefId);
        workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        workflow.setWorkflowReference(subscriptionId);
        workflow.setStatus(WorkflowStatus.CREATED);
        workflowDAO.addWorkflowEntry(workflow);
        String externalRefFromDb = workflowDAO.getExternalWorkflowReferenceForPendingTask(subscriptionId,
                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);

        Assert.assertEquals(externalRefFromDb, workflowExtRefId);
    }

    @Test
    public void testGetExternalWorkflowReferenceForApplication() throws Exception {
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        String workflowExtRefId = UUID.randomUUID().toString();
        String applicationId = UUID.randomUUID().toString();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowExtRefId);
        workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        workflow.setWorkflowReference(applicationId);
        workflow.setStatus(WorkflowStatus.CREATED);
        workflowDAO.addWorkflowEntry(workflow);
        String externalRefFromDb = workflowDAO.getExternalWorkflowReferenceForPendingTask(applicationId,
                WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);

        Assert.assertEquals(externalRefFromDb, workflowExtRefId);
    }
    
    @Test
    public void testGetPendinExternalWorkflowReferenceForApprovedasks() throws Exception {
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        String workflowExtRefId = UUID.randomUUID().toString();
        String applicationId = UUID.randomUUID().toString();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowExtRefId);
        workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        workflow.setWorkflowReference(applicationId);
        workflow.setStatus(WorkflowStatus.APPROVED);
        workflowDAO.addWorkflowEntry(workflow);
        String externalRefFromDb = workflowDAO.getExternalWorkflowReferenceForPendingTask(applicationId,
                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);

        Assert.assertNull(externalRefFromDb, "Should return only if the wf task is in CREATED state");
    }

    @Test
    public void testremoveWorkflowEntry() throws Exception {

        String workflowRefId = UUID.randomUUID().toString();
        WorkflowDAO workflowDAO = DAOFactory.getWorkflowDAO();
        Workflow workflow = SampleTestObjectCreator.createWorkflow(workflowRefId);
        workflowDAO.addWorkflowEntry(workflow);
        workflowDAO.deleteWorkflowEntryforExternalReference(workflow.getExternalWorkflowReference());
        
        Workflow retrieveWorflow = workflowDAO.retrieveWorkflow(workflow.getExternalWorkflowReference());
        Assert.assertNull(retrieveWorflow, "Workflow entry still exists");

    }
}
