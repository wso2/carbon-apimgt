package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.dto.*;
import org.wso2.carbon.apimgt.rest.api.BlockSubscriptionApiService;
import org.wso2.carbon.apimgt.rest.api.factories.BlockSubscriptionApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/block-subscription")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/block-subscription", description = "the block-subscription API")
public class BlockSubscriptionApi  {

   private final BlockSubscriptionApiService delegate = BlockSubscriptionApiServiceFactory.getBlockSubscriptionApi();

    @POST
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Block a subscription.", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Subscription was blocked successfully."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested subscription does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response blockSubscriptionPost(@ApiParam(value = "Subscription Id",required=true) @QueryParam("subscriptionId") String subscriptionId,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.blockSubscriptionPost(subscriptionId,ifMatch,ifUnmodifiedSince);
    }
}

