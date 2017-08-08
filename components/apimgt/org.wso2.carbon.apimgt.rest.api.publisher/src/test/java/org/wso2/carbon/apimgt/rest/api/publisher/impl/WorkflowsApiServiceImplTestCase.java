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
package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.impl.APIPublisherImpl;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.workflow.APIStateChangeWorkflow;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestAPIPublisherUtil.class, WorkflowExecutorFactory.class})
public class WorkflowsApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(WorkflowsApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testWorkflowsWorkflowReferenceIdPutNotExist() throws Exception {
        printTestMethodName();
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).thenReturn(apiPublisher);
        String workflowRefId = UUID.randomUUID().toString();
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setDescription("Test Desc");
        workflowDTO.setStatus(WorkflowDTO.StatusEnum.APPROVED);
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).retrieveWorkflow(workflowRefId);
        Response response = workflowsApiService.
                    workflowsWorkflowReferenceIdPut(workflowRefId, workflowDTO, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().
                        contains("Workflow entry cannot be found for the given reference id"));
    }

    @Test
    public void testWorkflowsWorkflowReferenceIdPutIncompatibleRequest() throws Exception {
        printTestMethodName();
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).thenReturn(apiPublisher);
        String workflowRefId = UUID.randomUUID().toString();
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setDescription("Test Desc");
        workflowDTO.setStatus(WorkflowDTO.StatusEnum.APPROVED);
        Workflow workflow = new ApplicationCreationWorkflow(null, null, null);
        workflow.setStatus(WorkflowStatus.APPROVED);
        Mockito.doReturn(workflow).doThrow(new IllegalArgumentException())
                .when(apiPublisher).retrieveWorkflow(workflowRefId);
        Response response = workflowsApiService.
                workflowsWorkflowReferenceIdPut(workflowRefId, workflowDTO, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().
                contains("Incompatible workflow request"));
    }


    @Test
    public void testWorkflowsWorkflowReferenceIdPutAlreadyPublished() throws Exception {
        printTestMethodName();
        WorkflowsApiServiceImpl workflowsApiService = new WorkflowsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).thenReturn(apiPublisher);
        String workflowRefId = UUID.randomUUID().toString();
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setDescription("Test Desc");
        workflowDTO.setStatus(WorkflowDTO.StatusEnum.APPROVED);
        Workflow workflow = new APIStateChangeWorkflow(null, null, null, null, null);
        workflow.setStatus(WorkflowStatus.APPROVED);
        Mockito.doReturn(workflow).doThrow(new IllegalArgumentException())
                .when(apiPublisher).retrieveWorkflow(workflowRefId);
        Response response = workflowsApiService.
                workflowsWorkflowReferenceIdPut(workflowRefId, workflowDTO, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().
                contains("Workflow is already completed"));
    }

    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
