package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionDataListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.BotDetectionDataApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.BotDetectionDataApiServiceImpl;
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
@Path("/bot-detection-data")

@Api(description = "the bot-detection-data API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class BotDetectionDataApi  {

  @Context MessageContext securityContext;

BotDetectionDataApiService delegate = new BotDetectionDataApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all bot detected data ", notes = "Get all bot detected data ", response = BotDetectionDataListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:bot_data", description = "Retrieve bot detection data"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Bot detected data returned. ", response = BotDetectionDataListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Analytics Not Enabled. ", response = ErrorDTO.class) })
    public Response getBotDetectionData() throws APIManagementException{
        return delegate.getBotDetectionData(securityContext);
    }
}
