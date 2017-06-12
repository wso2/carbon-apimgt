package org.wso2.carbon.apimgt.rest.api.admin;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.factories.WorkflowsApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
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
    name = "org.wso2.carbon.apimgt.rest.api.admin.WorkflowsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/admin/v1.[\\d]+/workflows")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/workflows")
@io.swagger.annotations.Api(description = "the workflows API")
public class WorkflowsApi implements Microservice  {
   private final WorkflowsApiService delegate = WorkflowsApiServiceFactory.getWorkflowsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all the uncompleted Workflows", notes = "Get all uncompleted workflows entries ", response = WorkflowListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:workflow_view", description = "Workflow view")
        })
    }, tags={ "Workflows (Collection)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Workflows returned ", response = WorkflowListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = WorkflowListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = WorkflowListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = WorkflowListDTO.class) })
    public Response workflowsGet(@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
,@ApiParam(value = "Type of the worklfow ") @QueryParam("workflowType") String workflowType
, @Context Request request)
    throws NotFoundException {
        return delegate.workflowsGet(ifNoneMatch,ifModifiedSince,workflowType, request);
    }
    @GET
    @Path("/{workflowReferenceId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve workflow information", notes = "This operation can be used to retrieve a workflow task.  ", response = WorkflowDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:workflow_view", description = "Workflow view")
        })
    }, tags={ "Workflows (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Workflow request information is returned. ", response = WorkflowDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = WorkflowDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = WorkflowDTO.class) })
    public Response workflowsWorkflowReferenceIdGet(@ApiParam(value = "Workflow reference id ",required=true) @PathParam("workflowReferenceId") String workflowReferenceId
, @Context Request request)
    throws NotFoundException {
        return delegate.workflowsWorkflowReferenceIdGet(workflowReferenceId, request);
    }
    @PUT
    @Path("/{workflowReferenceId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update workflow status", notes = "This operation can be used to approve or reject a workflow task. . ", response = WorkflowResponseDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:workflow_approve", description = "Workflow approve")
        })
    }, tags={ "Workflows (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Workflow request information is returned. ", response = WorkflowResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = WorkflowResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = WorkflowResponseDTO.class) })
    public Response workflowsWorkflowReferenceIdPut(@ApiParam(value = "Workflow reference id ",required=true) @PathParam("workflowReferenceId") String workflowReferenceId
,@ApiParam(value = "Workflow event that need to be updated " ,required=true) WorkflowRequestDTO body
, @Context Request request)
    throws NotFoundException {
        return delegate.workflowsWorkflowReferenceIdPut(workflowReferenceId,body, request);
    }
}
