package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.WorkflowsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/workflows")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/workflows", description = "the workflows API")
public class WorkflowsApi  {

   private final WorkflowsApiService delegate = WorkflowsApiServiceFactory.getWorkflowsApi();

    @POST
    @Path("/update-workflow-status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update workflow status", notes = "This operation can be used to approve or reject a workflow task.\n", response = WorkflowDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nWorkflow request information is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nWorkflow for the given reference is not found.\n") })

    public Response workflowsUpdateWorkflowStatusPost(@ApiParam(value = "Workflow reference id\n",required=true) @QueryParam("workflowReferenceId")  String workflowReferenceId,
    @ApiParam(value = "Workflow event that need to be updated\n" ,required=true ) WorkflowDTO body)
    {
    return delegate.workflowsUpdateWorkflowStatusPost(workflowReferenceId,body);
    }
}

