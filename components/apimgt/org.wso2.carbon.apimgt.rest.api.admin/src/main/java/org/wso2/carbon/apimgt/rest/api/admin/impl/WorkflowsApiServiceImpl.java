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
package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.WorkflowMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class WorkflowsApiServiceImpl extends WorkflowsApiService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowsApiServiceImpl.class);

    @Override
    public Response workflowsGet(String ifNoneMatch, String ifModifiedSince, String workflowType,
            Request request) throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<Workflow> workflowList;
            if (workflowType == null) {
                workflowList = apiMgtAdminService.retrieveUncompletedWorkflows();
            } else {
                workflowList = apiMgtAdminService.retrieveUncompletedWorkflowsByType(workflowType);
            }

            WorkflowListDTO workflowListDTO = WorkflowMappingUtil.toWorkflowListDTO(workflowList);
            return Response.ok().entity(workflowListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving workflows list";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

    }

    @Override
    public Response workflowsWorkflowReferenceIdGet(String workflowReferenceId, Request request)
            throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Workflow workflow = apiMgtAdminService.retrieveWorkflow(workflowReferenceId);
            WorkflowDTO workflowDTO = WorkflowMappingUtil.toWorkflowDTO(workflow);
            return Response.ok().entity(workflowDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving workflows list";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response workflowsWorkflowReferenceIdPut(String workflowReferenceId, WorkflowRequestDTO body,
            Request request) throws NotFoundException {
        try {

            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Workflow workflow = apiMgtAdminService.retrieveWorkflow(workflowReferenceId);
            if (workflow == null) {
                String errorMessage = "Workflow entry not found for: " + workflowReferenceId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.WORKFLOW_NOT_FOUND);
                Map<String, String> paramList = new HashMap<>();
                paramList.put(APIMgtConstants.ExceptionsConstants.WORKFLOW_REF_ID, workflowReferenceId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            } else {
                if (WorkflowStatus.APPROVED == workflow.getStatus()) {
                    String errorMessage = "Workflow is already in complete state";
                    APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                            ExceptionCodes.WORKFLOW_ALREADY_COMPLETED);
                    Map<String, String> paramList = new HashMap<>();
                    paramList.put(APIMgtConstants.ExceptionsConstants.WORKFLOW_REF_ID, workflowReferenceId);
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                    log.error(errorMessage, e);
                    return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
                } else {
                    WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getInstance()
                            .getWorkflowExecutor(workflow.getWorkflowType());

                    if (body == null) {
                        RestApiUtil.handleBadRequest("Request payload is missing", log);
                    }

                    if (body.getDescription() != null) {
                        workflow.setWorkflowDescription(body.getDescription());
                    }

                    if (body.getStatus() == null) {
                        String errorMessage = "Workflow status is not defined";
                        APIManagementException e = new APIManagementException(errorMessage,
                                ExceptionCodes.WORKFLOW_STATE_MISSING);
                        Map<String, String> paramList = new HashMap<>();
                        paramList.put(APIMgtConstants.ExceptionsConstants.WORKFLOW_REF_ID, workflowReferenceId);
                        ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                        log.error(errorMessage, e);
                        return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
                    } else {
                        workflow.setStatus(WorkflowStatus.valueOf(body.getStatus().toString()));
                    }

                    if (body.getAttributes() != null) {
                        Map<String, String> existingAttributs = workflow.getAttributes();
                        Map<String, String> newAttributes = body.getAttributes();
                        if (existingAttributs == null) {
                            workflow.setAttributes(newAttributes);
                        } else {
                            newAttributes.forEach(existingAttributs::putIfAbsent);
                            workflow.setAttributes(existingAttributs);
                        }
                    }

                    WorkflowResponse response = apiMgtAdminService.completeWorkflow(workflowExecutor, workflow);
                    WorkflowResponseDTO workflowResponseDTO = WorkflowMappingUtil.toWorkflowResponseDTO(response);
                    return Response.ok().entity(workflowResponseDTO).build();
                }
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while completing workflow for reference : " + workflowReferenceId + ". "
                    + e.getMessage();
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.WORKFLOW_REF_ID, workflowReferenceId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
