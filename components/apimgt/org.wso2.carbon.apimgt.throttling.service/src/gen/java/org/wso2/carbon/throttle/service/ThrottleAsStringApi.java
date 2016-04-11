package org.wso2.carbon.throttle.service;

import org.wso2.carbon.throttle.service.dto.*;
import org.wso2.carbon.throttle.service.ThrottleAsStringApiService;
import org.wso2.carbon.throttle.service.factories.ThrottleAsStringApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.throttle.service.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/throttleAsString")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/throttleAsString", description = "the throttleAsString API")
public class ThrottleAsStringApi  {

   private final ThrottleAsStringApiService delegate = ThrottleAsStringApiServiceFactory.getThrottleAsStringApi();

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "throttled events available", notes = "This will provide access to throttled events in database.", response = String.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "An array of shops around you"),
        
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error") })

    public Response throttleAsStringGet(@ApiParam(value = "**Search condition**.\n\nYou can search for an application by specifying the name as \"query\" attribute.\n\nEg.\n\"app1\" will match an application if the name is exactly \"app1\".\n\nCurrently this does not support wildcards. Given name must exactly match the application name.") @QueryParam("query") String query)
    {
    return delegate.throttleAsStringGet(query);
    }
}

