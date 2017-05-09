package org.wso2.carbon.apimgt.rest.api.publisher;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.WorkflowsApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.WorkflowsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1.[\\d]+/workflows")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the workflows API")
public class WorkflowsApi implements Microservice  {
   private final WorkflowsApiService delegate = WorkflowsApiServiceFactory.getWorkflowsApi();

    @PUT
    @Path("/{workflowReferenceId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update workflow status", notes = "This operation can be used to approve or reject a workflow task. . ", response = WorkflowResponseDTO.class, tags={ "Workflows (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Workflow request information is returned. ", response = WorkflowResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = WorkflowResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = WorkflowResponseDTO.class) })
    public Response workflowsWorkflowReferenceIdPut(@ApiParam(value = "Workflow reference id ",required=true) @PathParam("workflowReferenceId") String workflowReferenceId
,@ApiParam(value = "Workflow event that need to be updated " ,required=true) WorkflowDTO body
, @Context Request request)
    throws NotFoundException {
        return delegate.workflowsWorkflowReferenceIdPut(workflowReferenceId,body, request);
    }
}
