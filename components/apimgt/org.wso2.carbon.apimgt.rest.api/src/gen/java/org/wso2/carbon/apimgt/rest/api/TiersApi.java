package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.dto.*;
import org.wso2.carbon.apimgt.rest.api.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.factories.TiersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.TierPermissionDTO;

import java.util.List;

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
    {
    return delegate.tiersGet(accept,ifNoneMatch);
    }
    @POST
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new tier", response = TierDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format.") })

    public Response tiersPost(@ApiParam(value = "Subscription object that should to be added" ,required=true ) TierDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType)
    {
    return delegate.tiersPost(body,contentType);
    }
    @GET
    @Path("/{tierName}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get tier details", response = TierDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. tier returned"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Subscription does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response tiersTierNameGet(@ApiParam(value = "Tier name",required=true ) @PathParam("tierName") String tierName,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.tiersTierNameGet(tierName,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{tierName}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update tier details", response = TierDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Subscription updated."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response tiersTierNamePut(@ApiParam(value = "Tier name",required=true ) @PathParam("tierName") String tierName,
    @ApiParam(value = "Tier object that needs to be modified" ,required=true ) TierDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.tiersTierNamePut(tierName,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{tierName}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove a tier", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response tiersTierNameDelete(@ApiParam(value = "Tier name",required=true ) @PathParam("tierName") String tierName,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.tiersTierNameDelete(tierName,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/{tierName}/update-permission")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update tier permission", response = TierDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully updated tier permissions"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized. User not allowed to update tier permission"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested tier does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response tiersTierNameUpdatePermissionPost(@ApiParam(value = "Tier name",required=true ) @PathParam("tierName") String tierName,
    @ApiParam(value = ""  ) TierPermissionDTO permissions,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.tiersTierNameUpdatePermissionPost(tierName,permissions,contentType,ifMatch,ifUnmodifiedSince);
    }
}

