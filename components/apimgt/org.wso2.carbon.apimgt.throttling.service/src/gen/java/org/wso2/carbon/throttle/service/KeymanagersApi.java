package org.wso2.carbon.throttle.service;

import org.wso2.carbon.throttle.service.dto.*;
import org.wso2.carbon.throttle.service.KeymanagersApiService;
import org.wso2.carbon.throttle.service.factories.KeymanagersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.throttle.service.dto.ErrorDTO;
import org.wso2.carbon.throttle.service.dto.KeyManagerDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/keymanagers")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/keymanagers", description = "the keymanagers API")
public class KeymanagersApi  {

   private final KeymanagersApiService delegate = KeymanagersApiServiceFactory.getKeymanagersApi();

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "An Array of key managers configured", notes = "this will provide key managers configured\n", response = KeyManagerDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "An array of key managers"),
        
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error") })

    public Response keymanagersGet()
    {
    return delegate.keymanagersGet();
    }
}

