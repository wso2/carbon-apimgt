package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.EnvironmentsApiServiceImpl;
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
@Path("/environments")

@Api(description = "the environments API")




public class EnvironmentsApi  {

  @Context MessageContext securityContext;

EnvironmentsApiService delegate = new EnvironmentsApiServiceImpl();


    @DELETE
    @Path("/{environmentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an Environment", notes = "Delete a Environment by Environment Id ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:environment_manage", description = "Manage gateway environments")
        })
    }, tags={ "Environment",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Environment successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response environmentsEnvironmentIdDelete(@ApiParam(value = "Environment UUID (or Environment name defined in config) ",required=true) @PathParam("environmentId") String environmentId) throws APIManagementException{
        return delegate.environmentsEnvironmentIdDelete(environmentId, securityContext);
    }

    @PUT
    @Path("/{environmentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an Environment", notes = "Update a gateway Environment by environment Id ", response = EnvironmentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:environment_manage", description = "Manage gateway environments")
        })
    }, tags={ "Environment",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Environment updated. ", response = EnvironmentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response environmentsEnvironmentIdPut(@ApiParam(value = "Environment UUID (or Environment name defined in config) ",required=true) @PathParam("environmentId") String environmentId, @ApiParam(value = "Environment object with updated information " ,required=true) EnvironmentDTO environmentDTO) throws APIManagementException{
        return delegate.environmentsEnvironmentIdPut(environmentId, environmentDTO, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all registered Environments", notes = "Get all Registered Environments ", response = EnvironmentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:environment_read", description = "Retrieve gateway environments")
        })
    }, tags={ "Environment Collection",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Environments returned ", response = EnvironmentListDTO.class) })
    public Response environmentsGet() throws APIManagementException{
        return delegate.environmentsGet(securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an Environment", notes = "Add a new geteway environment ", response = EnvironmentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:environment_manage", description = "Manage gateway environments")
        })
    }, tags={ "Environment" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. ", response = EnvironmentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response environmentsPost(@ApiParam(value = "Environment object that should to be added " ,required=true) EnvironmentDTO environmentDTO) throws APIManagementException{
        return delegate.environmentsPost(environmentDTO, securityContext);
    }
}
