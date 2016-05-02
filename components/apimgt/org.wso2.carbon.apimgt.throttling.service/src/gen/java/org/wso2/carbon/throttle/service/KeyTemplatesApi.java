package org.wso2.carbon.throttle.service;

import org.wso2.carbon.throttle.service.dto.*;
import org.wso2.carbon.throttle.service.KeyTemplatesApiService;
import org.wso2.carbon.throttle.service.factories.KeyTemplatesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.throttle.service.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/keyTemplates")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/keyTemplates", description = "the keyTemplates API")
public class KeyTemplatesApi  {

   private final KeyTemplatesApiService delegate = KeyTemplatesApiServiceFactory.getKeyTemplatesApi();

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "An Array of key templates according to custom policies", notes = "This will provide access to key templates define in custom policies", response = String.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "An array of shops around you"),
        
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error") })

    public Response keyTemplatesGet()
    {
    return delegate.keyTemplatesGet();
    }
}

