package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import javax.ws.rs.core.Response;

public class WorkflowsApiServiceImpl extends WorkflowsApiService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowsApiServiceImpl.class);

    @Override
    public Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId, WorkflowDTO body)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {

            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Workflow workflow = apiPublisher.retrieveWorkflow(workflowReferenceId);
            if (workflow == null) {
                String errorMessage = "Workflow entry not found for: " + workflowReferenceId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.WORKFLOW_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<>();
                paramList.put(APIMgtConstants.ExceptionsConstants.WORKFLOW_REF_ID, workflowReferenceId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            } else if (WorkflowStatus.APPROVED == workflow.getStatus()) {
                String errorMessage = "Workflow is already in complete state";
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.WORKFLOW_COMPLETED);
                HashMap<String, String> paramList = new HashMap<>();
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
                    RestApiUtil.handleBadRequest("Workflow status is not defined", log);
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

                WorkflowResponse response = apiPublisher.completeWorkflow(workflowExecutor, workflow);
                WorkflowResponseDTO workflowResponseDTO = MappingUtil.toWorkflowResponseDTO(response);
                return Response.ok().entity(workflowResponseDTO).build();
            }

        } catch (APIManagementException e) {
            String errorMessage = "Error while completing workflow for reference : " + workflowReferenceId;
            HashMap<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.WORKFLOW_REF_ID, workflowReferenceId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
