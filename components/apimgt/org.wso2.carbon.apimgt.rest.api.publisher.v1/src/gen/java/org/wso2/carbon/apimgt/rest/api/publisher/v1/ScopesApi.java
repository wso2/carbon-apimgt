package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ScopesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ScopesApiServiceImpl;
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
@Path("/scopes")

@Api(description = "the scopes API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ScopesApi  {

  @Context MessageContext securityContext;

ScopesApiService delegate = new ScopesApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new Shared Scope", notes = "This operation can be used to add a new Shared Scope. ", response = ScopeDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:shared_scope_manage", description = "Manage shared scopes")
        })
    }, tags={ "Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created Scope object as an entity in the body. ", response = ScopeDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = Void.class) })
    public Response addSharedScope(@ApiParam(value = "Scope object that needs to be added " ,required=true) ScopeDTO body) throws APIManagementException{
        return delegate.addSharedScope(body, securityContext);
    }

    @DELETE
    @Path("/{scopeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Shared Scope", notes = "This operation can be used to delete a Shared Scope proving the Id of the scope. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:shared_scope_manage", description = "Manage shared scopes")
        })
    }, tags={ "Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be deleted does not exist. ", response = ErrorDTO.class) })
    public Response deleteSharedScope(@ApiParam(value = "Scope Id consisting the UUID of the shared scope ",required=true) @PathParam("scopeId") String scopeId) throws APIManagementException{
        return delegate.deleteSharedScope(scopeId, securityContext);
    }

    @GET
    @Path("/{scopeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Shared Scope by Scope Id", notes = "This operation can be used to retrieve details of a Shared Scope by a given scope Id. ", response = ScopeDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested Shared Scope is returned. ", response = ScopeDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Shared Scope does not exist. ", response = ErrorDTO.class) })
    public Response getSharedScope(@ApiParam(value = "Scope Id consisting the UUID of the shared scope ",required=true) @PathParam("scopeId") String scopeId) throws APIManagementException{
        return delegate.getSharedScope(scopeId, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all available Shared Scopes", notes = "This operation can be used to get all the available Shared Scopes. ", response = ScopeListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Shared Scope list is returned. ", response = ScopeListDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while retrieving shared scope list", response = ErrorDTO.class) })
    public Response getSharedScopes( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.getSharedScopes(limit, offset, securityContext);
    }

    @PUT
    @Path("/{scopeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Shared Scope", notes = "This operation can be used to update a Shared Scope by a given scope Id. ", response = ScopeDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:shared_scope_manage", description = "Manage shared scopes")
        })
    }, tags={ "Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated Scope object ", response = ScopeDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class) })
    public Response updateSharedScope(@ApiParam(value = "Scope Id consisting the UUID of the shared scope ",required=true) @PathParam("scopeId") String scopeId, @ApiParam(value = "Scope object that needs to be updated " ,required=true) ScopeDTO body) throws APIManagementException{
        return delegate.updateSharedScope(scopeId, body, securityContext);
    }

    @HEAD
    @Path("/{scopeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Check given scope name is already exist", notes = "Using this operation, user can check a given scope name exists or not. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Scopes" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested scope name exists.", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested scope name does not exist.", response = Void.class) })
    public Response validateScope(@ApiParam(value = "Scope name ",required=true) @PathParam("scopeId") String scopeId) throws APIManagementException{
        return delegate.validateScope(scopeId, securityContext);
    }
}
