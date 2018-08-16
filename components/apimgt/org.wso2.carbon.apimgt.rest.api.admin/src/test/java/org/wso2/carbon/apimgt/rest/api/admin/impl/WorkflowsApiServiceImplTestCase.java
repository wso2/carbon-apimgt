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
package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.workflow.APIStateChangeWorkflow;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowRequestDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;

public class WorkflowsApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(WorkflowsApiServiceImplTestCase.class);

    @Test
    public void testWorkflowsWorkflowReferenceIdPutException() throws Exception {
        printTestMethodName();
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);
        String workflowRefId = UUID.randomUUID().toString();
        String message = "Error while retrieving workflow entry for :" + workflowRefId;
        Mockito.doThrow(new APIManagementException(message, ExceptionCodes.APIMGT_DAO_EXCEPTION)).when(adminService)
                .retrieveWorkflow(workflowRefId);

        WorkflowRequestDTO workflowRequestDTO = new WorkflowRequestDTO();
        workflowRequestDTO.setDescription("Test Desc");
        workflowRequestDTO.setStatus(WorkflowRequestDTO.StatusEnum.APPROVED);
        Response response = workflowsApiService.workflowsWorkflowReferenceIdPut(workflowRefId, workflowRequestDTO,
                getRequest());
        assertEquals(500, response.getStatus());

    }
    
    @Test
    public void testWorkflowsWorkflowReferenceIdPutNotExist() throws Exception {
        printTestMethodName();

        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);

        String workflowRefId = UUID.randomUUID().toString();
        WorkflowRequestDTO workflowRequestDTO = new WorkflowRequestDTO();
        workflowRequestDTO.setDescription("Test Desc");
        workflowRequestDTO.setStatus(WorkflowRequestDTO.StatusEnum.APPROVED);
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(adminService).retrieveWorkflow(workflowRefId);   
        Response response = workflowsApiService.
                    workflowsWorkflowReferenceIdPut(workflowRefId, workflowRequestDTO, getRequest());
        assertEquals(404, response.getStatus());  
    }

    @Test
    public void testWorkflowsWorkflowReferenceIdPutAlreadyPublished() throws Exception {
        printTestMethodName();
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);

        String workflowRefId = UUID.randomUUID().toString();
        WorkflowRequestDTO workflowRequestDTO = new WorkflowRequestDTO();
        workflowRequestDTO.setDescription("Test Desc");
        workflowRequestDTO.setStatus(WorkflowRequestDTO.StatusEnum.APPROVED);
        Workflow workflow = new APIStateChangeWorkflow(null, null, null, null, null, null);
        workflow.setStatus(WorkflowStatus.APPROVED);
        Mockito.doReturn(workflow).doThrow(new IllegalArgumentException())
                .when(adminService).retrieveWorkflow(workflowRefId);
        Response response = workflowsApiService.
                workflowsWorkflowReferenceIdPut(workflowRefId, workflowRequestDTO, getRequest());
        assertEquals(400, response.getStatus());

    }
    
    @Test
    public void testWorkflowsWorkflowReferenceIdGet() throws Exception {
        printTestMethodName();
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);

        String workflowRefId = UUID.randomUUID().toString();
        WorkflowRequestDTO workflowRequestDTO = new WorkflowRequestDTO();
        workflowRequestDTO.setDescription("Test Desc");
        workflowRequestDTO.setStatus(WorkflowRequestDTO.StatusEnum.APPROVED);
        Workflow workflow = new ApplicationCreationWorkflow(null, null, null);
        workflow.setStatus(WorkflowStatus.APPROVED);
        Instant date1 = Instant.now();
        workflow.setCreatedTime(date1);
        workflow.setWorkflowDescription("Description 1");
        workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        String ref1 = UUID.randomUUID().toString();
        workflow.setExternalWorkflowReference(ref1);
        Mockito.doReturn(workflow).doThrow(new IllegalArgumentException())
                .when(adminService).retrieveWorkflow(workflowRefId);
        Response response = workflowsApiService.
                workflowsWorkflowReferenceIdGet(workflowRefId, getRequest());
        assertEquals(200, response.getStatus());

    }
    @Test
    public void testWorkflowsWorkflowReferenceIdGetException() throws Exception {
        printTestMethodName();
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);

        String workflowRefId = UUID.randomUUID().toString();
        String message = "Error while retrieving workflow entry for :" + workflowRefId;
        Mockito.doThrow(new APIManagementException(message, ExceptionCodes.APIMGT_DAO_EXCEPTION)).when(adminService)
                .retrieveWorkflow(workflowRefId);

        Response response = workflowsApiService.
                workflowsWorkflowReferenceIdGet(workflowRefId, getRequest());
        assertEquals(500, response.getStatus());

    }
    
    @Test
    public void testWorkflowsWorkflowReferenceIdGetForInvalidReference() throws Exception {
        printTestMethodName();
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);

        String workflowRefId = UUID.randomUUID().toString();

        String message = "Workflow not found for : " + workflowRefId;
        Mockito.doThrow( new APIMgtDAOException(message, ExceptionCodes.WORKFLOW_NOT_FOUND)).when(adminService)
                .retrieveWorkflow(workflowRefId);

        Response response = workflowsApiService.workflowsWorkflowReferenceIdGet(workflowRefId, getRequest());
        assertEquals(404, response.getStatus());

    }
    @Test
    public void testWorkflowsGetWithoutType() throws Exception {
        printTestMethodName();
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);

        List<Workflow> workflowList = new ArrayList<>();
        Mockito.doReturn(workflowList).doThrow(new IllegalArgumentException()).when(adminService)
                .retrieveUncompletedWorkflows();

        Response response = workflowsApiService.workflowsGet(null, null, null, getRequest());
        assertEquals(200, response.getStatus());
    }   
    
    @Test
    public void testWorkflowsGetWithType() throws Exception {
        printTestMethodName();
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);

        List<Workflow> workflowList = new ArrayList<>();
        Mockito.doReturn(workflowList).doThrow(new IllegalArgumentException()).when(adminService)
                .retrieveUncompletedWorkflowsByType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);

        Response response = workflowsApiService.workflowsGet(null, null,
                WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION, getRequest());
        assertEquals(200, response.getStatus());
    }  
    
    @Test
    public void testWorkflowsGetException() throws Exception {
        printTestMethodName();
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl(adminService);

        String message = "Error while retrieving workflow information";
        Mockito.doThrow(new APIManagementException(message, ExceptionCodes.APIMGT_DAO_EXCEPTION)).when(adminService)
                .retrieveUncompletedWorkflows();

        Response response = workflowsApiService.workflowsGet(null, null, null, getRequest());
        assertEquals(500, response.getStatus());
    }
    
    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        return Mockito.mock(Request.class);
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
