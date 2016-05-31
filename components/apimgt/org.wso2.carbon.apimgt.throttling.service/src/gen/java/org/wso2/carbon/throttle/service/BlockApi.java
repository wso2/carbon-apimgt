package org.wso2.carbon.throttle.service;

import org.wso2.carbon.throttle.service.dto.*;
import org.wso2.carbon.throttle.service.BlockApiService;
import org.wso2.carbon.throttle.service.factories.BlockApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.throttle.service.dto.BlockConditionsDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/block")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/block", description = "the block API")
public class BlockApi  {

   private final BlockApiService delegate = BlockApiServiceFactory.getBlockApi();

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "throttled events available", notes = "This will provide access to throttled events in database.", response = BlockConditionsDTO.class, responseContainer = "Map")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "An array of shops around you") })

    public Response blockGet()
    {
    return delegate.blockGet();
    }
}

