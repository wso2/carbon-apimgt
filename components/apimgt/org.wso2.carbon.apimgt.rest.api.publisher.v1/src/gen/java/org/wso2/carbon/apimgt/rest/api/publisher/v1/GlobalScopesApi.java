package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GlobalScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.GlobalScopesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.GlobalScopesApiServiceImpl;
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
@Path("/global-scopes")

@Api(description = "the global-scopes API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class GlobalScopesApi  {

  @Context MessageContext securityContext;

GlobalScopesApiService delegate = new GlobalScopesApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new Global Scope", notes = "This operation can be used to add a new Global Scope. ", response = ScopeDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:global_scope_manage", description = "Manage global scopes")
        })
    }, tags={ "Global Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created Scope object as an entity in the body. ", response = ScopeDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = Void.class) })
    public Response addGlobalScope(@ApiParam(value = "Scope object that needs to be added " ,required=true) ScopeDTO body) throws APIManagementException{
        return delegate.addGlobalScope(body, securityContext);
    }

    @DELETE
    @Path("/{scopeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Global Scope", notes = "This operation can be used to delete a Global Scope proving the Id of the scope. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:global_scope_manage", description = "Manage global scopes")
        })
    }, tags={ "Global Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be deleted does not exist. ", response = ErrorDTO.class) })
    public Response deleteGlobalScope(@ApiParam(value = "Scope Id consisting the UUID of the global scope ",required=true) @PathParam("scopeId") String scopeId) throws APIManagementException{
        return delegate.deleteGlobalScope(scopeId, securityContext);
    }

    @GET
    @Path("/{scopeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Global Scope by Scope Id", notes = "This operation can be used to retrieve details of a Global Scope by a given scope Id. ", response = ScopeDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Global Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested Global Scope is returned. ", response = ScopeDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Global Scope does not exist. ", response = ErrorDTO.class) })
    public Response getGlobalScope(@ApiParam(value = "Scope Id consisting the UUID of the global scope ",required=true) @PathParam("scopeId") String scopeId) throws APIManagementException{
        return delegate.getGlobalScope(scopeId, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all available Global Scopes", notes = "This operation can be used to get all the available Global Scopes. ", response = GlobalScopeListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Global Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Global Scope list is returned. ", response = GlobalScopeListDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while retrieving global scope list", response = ErrorDTO.class) })
    public Response getGlobalScopes() throws APIManagementException{
        return delegate.getGlobalScopes(securityContext);
    }

    @PUT
    @Path("/{scopeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Global Scope", notes = "This operation can be used to update a Global Scope by a given scope Id. ", response = ScopeDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:global_scope_manage", description = "Manage global scopes")
        })
    }, tags={ "Global Scopes" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated Scope object ", response = ScopeDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class) })
    public Response updateGlobalScope(@ApiParam(value = "Scope Id consisting the UUID of the global scope ",required=true) @PathParam("scopeId") String scopeId, @ApiParam(value = "Scope object that needs to be updated " ,required=true) ScopeDTO body) throws APIManagementException{
        return delegate.updateGlobalScope(scopeId, body, securityContext);
    }
}
