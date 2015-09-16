package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.model.*;
import org.wso2.carbon.apimgt.rest.api.ExternalStoresApiService;
import org.wso2.carbon.apimgt.rest.api.factories.ExternalStoresApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.model.Error;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/externalStores")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/externalStores", description = "the externalStores API")
public class ExternalStoresApi  {

   private final ExternalStoresApiService delegate = ExternalStoresApiServiceFactory.getExternalStoresApi();

    @GET
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of External API Stores", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. External API store list is returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response externalStoresGet(@ApiParam(value = "Maximum size of API array to return.",required=true) @QueryParam("limit") String limit,
    @ApiParam(value = "Starting point of the item list.",required=true) @QueryParam("offset") String offset,
    @ApiParam(value = "Search condition.") @QueryParam("query") String query,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    throws NotFoundException {
    return delegate.externalStoresGet(limit,offset,query,accept,ifNoneMatch);
    }
    @POST
    @Path("/publish-externalstore")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Publish to external API store", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully publish to external store"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized. User not allowed to update tier permission"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested tier does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response externalStoresPublishExternalstorePost(@ApiParam(value = "", required=true )@FormParam("apiId")  String apiId,
    @ApiParam(value = "", required=true )@FormParam("externalStoreId")  String externalStoreId)
    throws NotFoundException {
    return delegate.externalStoresPublishExternalstorePost(apiId,externalStoreId);
    }
}

