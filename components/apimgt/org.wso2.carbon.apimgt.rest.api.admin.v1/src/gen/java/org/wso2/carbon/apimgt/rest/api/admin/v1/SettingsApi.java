package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeSettingsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.SettingsApiServiceImpl;
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
@Path("/settings")

@Api(description = "the settings API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class SettingsApi  {

  @Context MessageContext securityContext;

SettingsApiService delegate = new SettingsApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retreive admin settings", notes = "Retreive admin settings ", response = SettingsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_settings", description = "Retrieve admin settings")
        })
    }, tags={ "Settings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Settings returned ", response = SettingsDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Settings does not exist. ", response = ErrorDTO.class) })
    public Response settingsGet() throws APIManagementException{
        return delegate.settingsGet(securityContext);
    }

    @GET
    @Path("/scopes/{scope}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve scopes for a particular user", notes = "This operation will return the scope list of particular user In order to get it, we need to pass the userId as a query parameter ", response = ScopeSettingsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_settings", description = "Retrieve admin settings")
        })
    }, tags={ "Settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Particular scope exists for the given user. ", response = ScopeSettingsDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested user does not exist. ", response = ErrorDTO.class) })
    public Response settingsScopesScopeGet( @NotNull @ApiParam(value = "",required=true)  @QueryParam("username") String username, @ApiParam(value = "scope name to be validated ",required=true) @PathParam("scope") String scope) throws APIManagementException{
        return delegate.settingsScopesScopeGet(username, scope, securityContext);
    }
}
