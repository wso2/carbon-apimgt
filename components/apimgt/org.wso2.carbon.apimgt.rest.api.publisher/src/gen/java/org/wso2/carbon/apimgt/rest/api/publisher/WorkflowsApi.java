package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.factories.WorkflowsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowResponseDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.WorkflowsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1/workflows")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the workflows API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-02T13:40:47.496+05:30")
public class WorkflowsApi implements Microservice  {
   private final WorkflowsApiService delegate = WorkflowsApiServiceFactory.getWorkflowsApi();

    @POST
    @Path("/update-workflow-status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update workflow status", notes = "This operation can be used to approve or reject a workflow task. . ", response = WorkflowResponseDTO.class, tags={ "Workflows (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Workflow request information is returned. ", response = WorkflowResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = WorkflowResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = WorkflowResponseDTO.class) })
    public Response workflowsUpdateWorkflowStatusPost(@ApiParam(value = "Workflow reference id ",required=true) @QueryParam("workflowReferenceId") String workflowReferenceId
,@ApiParam(value = "Workflow event that need to be updated " ,required=true) WorkflowDTO body
)
    throws NotFoundException {
        return delegate.workflowsUpdateWorkflowStatusPost(workflowReferenceId,body);
    }
}
