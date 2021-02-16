package org.wso2.carbon.apimgt.rest.api.service.catalog;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.impl.SettingsApiServiceImpl;
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
    @ApiOperation(value = "Retrieve service catalog API settings", notes = "Retrieve Service Catalog API settings ", response = SettingsDTO.class, tags={ "Settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Settings returned ", response = SettingsDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getSettings() throws APIManagementException{
        return delegate.getSettings(securityContext);
    }
}
