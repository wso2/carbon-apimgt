package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeSettingsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.SystemScopesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.SystemScopesApiServiceImpl;
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
@Path("/system-scopes")

@Api(description = "the system-scopes API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class SystemScopesApi  {

  @Context MessageContext securityContext;

SystemScopesApiService delegate = new SystemScopesApiServiceImpl();


    @GET
    @Path("/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the list of role scope mapping. ", notes = "This operation is used to get the list of role scope mapping from tenant-conf for the apim admin dashboard ", response = ScopeListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:scope_manage", description = "Manage scope"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "System Scopes",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The list of role scope mappings are returned. ", response = ScopeListDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. An internal server error occurred while retrieving the role scope mapping. ", response = ErrorDTO.class) })
    public Response systemScopesGet() throws APIManagementException{
        return delegate.systemScopesGet(securityContext);
    }

    @GET
    @Path("/{scopeName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve scopes for a particular user", notes = "This operation will return the scope list of particular user In order to get it, we need to pass the userId as a query parameter ", response = ScopeSettingsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:scope_manage", description = "Manage scope"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Syatem Scopes" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Particular scope exists for the given user. ", response = ScopeSettingsDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested user does not exist. ", response = ErrorDTO.class) })
    public Response systemScopesScopeNameGet(@ApiParam(value = "scope name to be validated ",required=true) @PathParam("scopeName") String scopeName,  @ApiParam(value = "")  @QueryParam("username") String username) throws APIManagementException{
        return delegate.systemScopesScopeNameGet(scopeName, username, securityContext);
    }
}
