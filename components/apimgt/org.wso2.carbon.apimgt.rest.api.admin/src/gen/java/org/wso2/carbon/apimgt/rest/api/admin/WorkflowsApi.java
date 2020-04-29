package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.WorkflowsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowListDTO;
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

    @GET
    @Path("/{externalWorkflowRef}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of a the pending workflow request according to the External Workflow Reference.\n", notes = "Using this operation, you can retrieve complete details of a pending workflow request that either belongs to application creation, application subscription, application registration, api state change, user self sign up.. You need to provide the External_Workflow_Reference of the workflow Request to retrive it.\n", response = WorkflowInfoDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested Workflow Pending is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested workflow pendding process does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response workflowsExternalWorkflowRefGet(@ApiParam(value = "from the externel workflow reference we decide what is the the pending request that the are requesting.\n",required=true ) @PathParam("externalWorkflowRef")  String externalWorkflowRef,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource (Will be supported in future).\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.workflowsExternalWorkflowRefGet(externalWorkflowRef,ifNoneMatch);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve All pending workflow processes\n", notes = "This operation can be used to retrieve list of workflow pending processes.\n", response = WorkflowListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nWorkflow pendding process list returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested user does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response workflowsGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource (Will be supported in future).\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "We need to show the values of each workflow process separately .for that we use workflow type.\nWorkflow type can be AM_APPLICATION_CREATION, AM_SUBSCRIPTION_CREATION,   AM_USER_SIGNUP, AM_APPLICATION_REGISTRATION_PRODUCTION, AM_APPLICATION_REGISTRATION_SANDBOX.\n", allowableValues="{values=[AM_APPLICATION_CREATION, AM_SUBSCRIPTION_CREATION, AM_USER_SIGNUP, AM_APPLICATION_REGISTRATION_PRODUCTION, AM_APPLICATION_REGISTRATION_SANDBOX, AM_SUBSCRIPTION_DELETION, AM_APPLICATION_DELETION, AM_API_STATE]}") @QueryParam("workflowType")  String workflowType)
    {
    return delegate.workflowsGet(limit,offset,accept,ifNoneMatch,workflowType);
    }
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

