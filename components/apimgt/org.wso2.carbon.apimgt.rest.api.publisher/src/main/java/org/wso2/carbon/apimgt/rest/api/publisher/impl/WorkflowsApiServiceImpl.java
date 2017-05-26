package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;

public class WorkflowsApiServiceImpl extends WorkflowsApiService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowsApiServiceImpl.class);

    /**
     * Workflow callback rest api to complete workflow task.
     *
     * @param workflowReferenceId workflow reference id
     * @param body                WorkflowDTO object
     * @param request             ms4j request object
     * @return the DTO object representing the workflwow response as the response payload
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response workflowsWorkflowReferenceIdPut(String workflowReferenceId, WorkflowDTO body, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {

            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Workflow workflow = apiPublisher.retrieveWorkflow(workflowReferenceId);
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
                if (!Workflow.Category.PUBLISHER.equals(workflow.getCategory())) {
                    String errorMessage = "Incompatible workflow request received by publisher. RefId: "
                            + workflowReferenceId + " Workflow category: " + workflow.getCategory().name();
                    APIManagementException ex = new APIManagementException(errorMessage,
                            ExceptionCodes.INCOMPATIBLE_WORKFLOW_REQUEST_FOR_PUBLISHER);
                    Map<String, String> paramList = new HashMap<>();
                    paramList.put(APIMgtConstants.ExceptionsConstants.WORKFLOW_REF_ID, workflowReferenceId);
                    paramList.put(APIMgtConstants.ExceptionsConstants.WORKFLOW_CATEGORY, workflow.getCategory().name());
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(ex.getErrorHandler(), paramList);
                    log.error(errorMessage, ex);
                    return Response.status(ex.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
                }
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

                    WorkflowResponse response = apiPublisher.completeWorkflow(workflowExecutor, workflow);
                    WorkflowResponseDTO workflowResponseDTO = MappingUtil.toWorkflowResponseDTO(response);
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
