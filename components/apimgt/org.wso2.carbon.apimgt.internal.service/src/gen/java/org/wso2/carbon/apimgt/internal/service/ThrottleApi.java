package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ThrottledEventDTO;
import org.wso2.carbon.apimgt.internal.service.ThrottleApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ThrottleApiServiceImpl;
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
@Path("/throttle")

@Api(description = "the throttle API")

@Produces({ "application/json" })


public class ThrottleApi  {

  @Context MessageContext securityContext;

ThrottleApiService delegate = new ThrottleApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "throttled events available", notes = "This will provide access to throttled events in database. ", response = ThrottledEventDTO.class, responseContainer = "List", tags={ "Shops" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of shops around you", response = ThrottledEventDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response throttleGet( @ApiParam(value = "**Search condition**.  You can search for an application by specifying the name as \"query\" attribute.  Eg. \"app1\" will match an application if the name is exactly \"app1\".  Currently this does not support wildcards. Given name must exactly match the application name. ")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.throttleGet(query, securityContext);
    }
}
