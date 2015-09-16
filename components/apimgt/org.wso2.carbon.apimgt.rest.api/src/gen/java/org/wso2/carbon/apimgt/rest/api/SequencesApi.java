package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.model.*;
import org.wso2.carbon.apimgt.rest.api.SequencesApiService;
import org.wso2.carbon.apimgt.rest.api.factories.SequencesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.model.Error;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/sequences")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/sequences", description = "the sequences API")
public class SequencesApi  {

   private final SequencesApiService delegate = SequencesApiServiceFactory.getSequencesApi();

    @GET
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of predefined sequences", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. sequence list is returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response sequencesGet(@ApiParam(value = "sequence type IN | OUT | FAULT.") @QueryParam("type") String type,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    throws NotFoundException {
    return delegate.sequencesGet(type,accept,ifNoneMatch);
    }
}

