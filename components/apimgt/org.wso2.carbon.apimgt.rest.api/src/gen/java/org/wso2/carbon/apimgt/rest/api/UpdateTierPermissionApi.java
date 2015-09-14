package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.model.*;
import org.wso2.carbon.apimgt.rest.api.UpdateTierPermissionApiService;
import org.wso2.carbon.apimgt.rest.api.factories.UpdateTierPermissionApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.model.Tier;
import org.wso2.carbon.apimgt.rest.api.model.TierPermission;
import org.wso2.carbon.apimgt.rest.api.model.Error;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/update-tier-permission")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/update-tier-permission", description = "the update-tier-permission API")
public class UpdateTierPermissionApi  {

   private final UpdateTierPermissionApiService delegate = UpdateTierPermissionApiServiceFactory.getUpdateTierPermissionApi();

    @POST
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update tier permission", response = Tier.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully updated tier permissions"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized. User not allowed to update tier permission"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested tier does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response updateTierPermissionPost(@ApiParam(value = "",required=true) @QueryParam("tierName") String tierName,
    @ApiParam(value = ""  ) TierPermission permissions,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    throws NotFoundException {
    return delegate.updateTierPermissionPost(tierName,permissions,contentType,ifMatch,ifUnmodifiedSince);
    }
}

