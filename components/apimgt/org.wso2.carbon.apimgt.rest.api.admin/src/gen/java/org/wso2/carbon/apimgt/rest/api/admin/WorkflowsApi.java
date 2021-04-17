package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowListDTO;
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




public class WorkflowsApi  {

  @Context MessageContext securityContext;

WorkflowsApiService delegate = new WorkflowsApiServiceImpl();


    @GET
    @Path("/{externalWorkflowRef}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Pending Workflow Details by External Workflow Reference ", notes = "Using this operation, you can retrieve complete details of a pending workflow request that either belongs to application creation, application subscription, application registration, api state change, user self sign up.. You need to provide the External_Workflow_Reference of the workflow Request to retrive it. ", response = WorkflowInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:api_workflow_view", description = "Retrive workflow requests")
        })
    }, tags={ "Workflows (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested Workflow Pending is returned ", response = WorkflowInfoDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response workflowsExternalWorkflowRefGet(@ApiParam(value = "from the externel workflow reference we decide what is the the pending request that the are requesting. ",required=true) @PathParam("externalWorkflowRef") String externalWorkflowRef) throws APIManagementException{
        return delegate.workflowsExternalWorkflowRefGet(externalWorkflowRef, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve All Pending Workflow Processes ", notes = "This operation can be used to retrieve list of workflow pending processes. ", response = WorkflowListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:api_workflow_view", description = "Retrive workflow requests")
        })
    }, tags={ "Workflow (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Workflow pendding process list returned. ", response = WorkflowListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response workflowsGet( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "We need to show the values of each workflow process separately .for that we use workflow type. Workflow type can be AM_APPLICATION_CREATION, AM_SUBSCRIPTION_CREATION,   AM_USER_SIGNUP, AM_APPLICATION_REGISTRATION_PRODUCTION, AM_APPLICATION_REGISTRATION_SANDBOX. ", allowableValues="AM_APPLICATION_CREATION, AM_SUBSCRIPTION_CREATION, AM_USER_SIGNUP, AM_APPLICATION_REGISTRATION_PRODUCTION, AM_APPLICATION_REGISTRATION_SANDBOX, AM_SUBSCRIPTION_DELETION, AM_APPLICATION_DELETION, AM_API_STATE")  @QueryParam("workflowType") String workflowType) throws APIManagementException{
        return delegate.workflowsGet(limit, offset, accept, workflowType, securityContext);
    }

    @POST
    @Path("/update-workflow-status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update Workflow Status", notes = "This operation can be used to approve or reject a workflow task. ", response = WorkflowDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:api_workflow_approve", description = "Manage workflows")
        })
    }, tags={ "Workflows (Individual)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Workflow request information is returned. ", response = WorkflowDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response workflowsUpdateWorkflowStatusPost( @NotNull @ApiParam(value = "Workflow reference id ",required=true)  @QueryParam("workflowReferenceId") String workflowReferenceId, @ApiParam(value = "Workflow event that need to be updated " ,required=true) WorkflowDTO workflowDTO) throws APIManagementException{
        return delegate.workflowsUpdateWorkflowStatusPost(workflowReferenceId, workflowDTO, securityContext);
    }
}
