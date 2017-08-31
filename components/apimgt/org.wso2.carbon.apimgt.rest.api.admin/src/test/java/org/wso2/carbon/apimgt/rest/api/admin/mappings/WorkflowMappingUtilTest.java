/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.core.workflow.SubscriptionCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowResponseDTO.WorkflowStatusEnum;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.WorkflowMappingUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unite test for WorkflowMappingUtil class
 */
public class WorkflowMappingUtilTest {
    
    @Test(description = "Convert WorkflowResponse to WorkflowResponseDTO")
    public void testToWorkflowResponseDTO() throws Exception {
        WorkflowResponse response = new GeneralWorkflowResponse();
        response.setWorkflowStatus(WorkflowStatus.APPROVED);        
        WorkflowResponseDTO dto = WorkflowMappingUtil.toWorkflowResponseDTO(response);
        Assert.assertEquals(dto.getWorkflowStatus(), WorkflowStatusEnum.APPROVED, "Invalid workflow status");
        Assert.assertEquals(dto.getJsonPayload(), "", "Invalid workflow payload");        
    }
    
    @Test(description = "Convert List<Workflow> workflowList to WorkflowListDTO")
    public void testToWorkflowListDTO() throws Exception {
        
        List<Workflow> wfList = new ArrayList<>();
        Workflow workflow1 = new ApplicationCreationWorkflow(null, null, null);
        workflow1.setStatus(WorkflowStatus.APPROVED);
        LocalDateTime date1 = LocalDateTime.now();
        workflow1.setCreatedTime(date1);
        workflow1.setWorkflowDescription("Description 1");
        workflow1.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        String ref1 = UUID.randomUUID().toString();
        workflow1.setExternalWorkflowReference(ref1);
        wfList.add(workflow1);
        
        Workflow workflow2 = new SubscriptionCreationWorkflow(null, null, null);
        workflow2.setStatus(WorkflowStatus.APPROVED);
        LocalDateTime date2 = LocalDateTime.now();
        workflow2.setCreatedTime(date2);
        workflow2.setWorkflowDescription("Description 2");
        workflow2.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        String ref2 = UUID.randomUUID().toString();
        workflow2.setExternalWorkflowReference(ref2);
        wfList.add(workflow2);
        
        WorkflowListDTO wfDtoList =  WorkflowMappingUtil.toWorkflowListDTO(wfList);
        
        int count = wfDtoList.getCount();
        Assert.assertEquals(count, 2, "Mismatch in the workflow list item count");
        
        Assert.assertEquals(wfDtoList.getList().get(0).getDescription(), "Description 1",
                "Invalid description for workflow item 1");
        Assert.assertEquals(wfDtoList.getList().get(0).getType(), WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION,
                "Invalid type for workflow item 1");
        Assert.assertEquals(wfDtoList.getList().get(0).getWorkflowStatus(), WorkflowStatus.APPROVED.toString(),
                "Invalid status for workflow item 1");
        Assert.assertEquals(wfDtoList.getList().get(0).getReferenceId(), ref1,
                "Invalid reference id for workflow item 1");
        
        Assert.assertEquals(wfDtoList.getList().get(1).getDescription(), "Description 2",
                "Invalid description for workflow item 2");
        Assert.assertEquals(wfDtoList.getList().get(1).getType(), WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION,
                "Invalid type for workflow item 2");
        Assert.assertEquals(wfDtoList.getList().get(1).getWorkflowStatus(), WorkflowStatus.APPROVED.toString(),
                "Invalid status for workflow item 2");
        Assert.assertEquals(wfDtoList.getList().get(1).getReferenceId(), ref2,
                "Invalid reference id for workflow item 2");                

    } 
    
    @Test(description = "Convert Workflow to WorkflowDTO")
    public void testToWorkflowDTO() throws Exception {
        Workflow workflow1 = new ApplicationCreationWorkflow(null, null, null);
        workflow1.setStatus(WorkflowStatus.APPROVED);
        LocalDateTime date1 = LocalDateTime.now();
        workflow1.setCreatedTime(date1);
        workflow1.setWorkflowDescription("Description 1");
        workflow1.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        String ref1 = UUID.randomUUID().toString();
        workflow1.setExternalWorkflowReference(ref1);
        
        WorkflowDTO dto = WorkflowMappingUtil.toWorkflowDTO(workflow1);
        Assert.assertEquals(dto.getDescription(), "Description 1", "Invalid description for workflow item 1");
        Assert.assertEquals(dto.getType(), WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION,
                "Invalid type for workflow item 1");
        Assert.assertEquals(dto.getWorkflowStatus(), WorkflowStatus.APPROVED.toString(),
                "Invalid status for workflow item 1");
        Assert.assertEquals(dto.getReferenceId(), ref1, "Invalid reference id for workflow item 1");
    }
    
}
