package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.dto.*;
import org.wso2.carbon.apimgt.rest.api.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.factories.TiersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/tiers")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/tiers", description = "the tiers API")
public class TiersApi  {

   private final TiersApiService delegate = TiersApiServiceFactory.getTiersApi();

    @GET
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get available tiers", response = TierDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. List of tiers returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response tiersGet(@ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    throws NotFoundException {
    return delegate.tiersGet(accept,ifNoneMatch);
    }
}

