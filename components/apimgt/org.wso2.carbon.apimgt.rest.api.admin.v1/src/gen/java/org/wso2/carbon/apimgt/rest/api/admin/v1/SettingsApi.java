package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
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




public class SettingsApi  {

  @Context MessageContext securityContext;

SettingsApiService delegate = new SettingsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retreive Admin Settings", notes = "Retreive admin settings ", response = SettingsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_settings", description = "Retrieve admin settings")
        })
    }, tags={ "Settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Settings returned ", response = SettingsDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response settingsGet() throws APIManagementException{
        return delegate.settingsGet(securityContext);
    }
}
