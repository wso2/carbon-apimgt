package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.WorkflowsApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/workflows")

@Api(description = "the workflows API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class WorkflowsApi  {

  @Context MessageContext securityContext;

WorkflowsApiService delegate = new WorkflowsApiServiceImpl();


    @POST
    @Path("/update-workflow-status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update workflow status", notes = "This operation can be used to approve or reject a workflow task. ", response = WorkflowDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_workflow", description = "Manage workflows")
        })
    }, tags={ "Workflows (Individual)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Workflow request information is returned. ", response = WorkflowDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Workflow for the given reference is not found. ", response = ErrorDTO.class) })
    public Response workflowsUpdateWorkflowStatusPost( @NotNull @ApiParam(value = "Workflow reference id ",required=true)  @QueryParam("workflowReferenceId") String workflowReferenceId, @ApiParam(value = "Workflow event that need to be updated " ,required=true) WorkflowDTO body) throws APIManagementException{
        return delegate.workflowsUpdateWorkflowStatusPost(workflowReferenceId, body, securityContext);
    }
}
