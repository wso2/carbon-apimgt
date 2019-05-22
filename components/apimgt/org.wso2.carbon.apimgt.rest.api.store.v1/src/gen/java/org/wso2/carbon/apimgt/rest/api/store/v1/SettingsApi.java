package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SettingsApiServiceImpl;

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
    @ApiOperation(value = "Retreive store settings", notes = "Retreive store settings ", response = SettingsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:store_setting", description = "Retrieve store settings")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Settings returned ", response = SettingsDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Settings does not exist. ", response = ErrorDTO.class) })
    public Response settingsGet() {
        return delegate.settingsGet(securityContext);
    }
}
