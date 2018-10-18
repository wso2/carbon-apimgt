package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.TiersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/tiers")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/tiers", description = "the tiers API")
public class TiersApi  {

   private final TiersApiService delegate = TiersApiServiceFactory.getTiersApi();

    @GET
    @Path("/{tierLevel}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all tiers", notes = "This operation can be used to list the available tiers for a given tier level. Tier level should be specified as a path parameter and should be one of `api`, `application` and `resource`.\n", response = TierListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of tiers returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response tiersTierLevelGet(@ApiParam(value = "List API or Application or Resource type tiers.\n",required=true, allowableValues="{values=[api, application, resource]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource (Will be supported in future).\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.tiersTierLevelGet(tierLevel,limit,offset,accept,ifNoneMatch);
    }
    @POST
    @Path("/{tierLevel}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a Tier", notes = "This operation can be used to create a new throttling tier. The only supported tier level is `api` tiers.\n`POST https://localhost:9443/api/am/publisher/v0.14/tiers/api`\n\n**IMPORTANT:**\n* This is only effective when Advanced Throttling is disabled in the Server. If enabled, we need to use Admin REST API for throttling tiers modification related operations.\n", response = TierDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response tiersTierLevelPost(@ApiParam(value = "Tier object that should to be added\n" ,required=true ) TierDTO body,
    @ApiParam(value = "List API or Application or Resource type tiers.\n",required=true, allowableValues="{values=[api]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.tiersTierLevelPost(body,tierLevel,contentType);
    }
    @DELETE
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Tier", notes = "This operation can be used to delete an existing tier. The only supported tier level is `api` tiers.\n`DELETE https://localhost:9443/api/am/publisher/v0.14/tiers/api/Low`\n\n**IMPORTANT:**\n* This is only effective when Advanced Throttling is disabled in the Server. If enabled, we need to use Admin REST API for throttling tiers modification related operations.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response tiersTierLevelTierNameDelete(@ApiParam(value = "Tier name\n",required=true ) @PathParam("tierName")  String tierName,
    @ApiParam(value = "List API or Application or Resource type tiers.\n",required=true, allowableValues="{values=[api]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "Validator for conditional requests; based on ETag (Will be supported in future).\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header (Will be supported in future).\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.tiersTierLevelTierNameDelete(tierName,tierLevel,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of a tier", notes = "This operation can be used to retrieve details of a single tier by specifying the tier level and tier name.\nNote that the scope of the API is mandatory while retreiving the access token with the following cURL command : `curl -k -d \\\"grant_type=password&username=username&password=password&scope=apim:tier_view\\\" -H \\\"Authorization: Basic <token>\\\" https://localhost:8243/token`.\nYou will receive the access token as the response, for example `\"access_token\":\"8644c013-7ff1-3217-b150-d7b92cae6be7\"`.\n", response = TierDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response tiersTierLevelTierNameGet(@ApiParam(value = "Tier name\n",required=true ) @PathParam("tierName")  String tierName,
    @ApiParam(value = "List API or Application or Resource type tiers.\n",required=true, allowableValues="{values=[api, application, resource]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource (Will be supported in future).\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource (Will be supported in future).\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.tiersTierLevelTierNameGet(tierName,tierLevel,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Tier", notes = "This operation can be used to update an existing tier. The only supported tier level is `api` tiers.\n`PUT https://localhost:9443/api/am/publisher/v0.14/tiers/api/Low`\n\n**IMPORTANT:**\n* This is only effective when Advanced Throttling is disabled in the Server. If enabled, we need to use Admin REST API for throttling tiers modification related operations.\n", response = TierDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSubscription updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response tiersTierLevelTierNamePut(@ApiParam(value = "Tier name\n",required=true ) @PathParam("tierName")  String tierName,
    @ApiParam(value = "Tier object that needs to be modified\n" ,required=true ) TierDTO body,
    @ApiParam(value = "List API or Application or Resource type tiers.\n",required=true, allowableValues="{values=[api]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag (Will be supported in future).\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header (Will be supported in future).\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.tiersTierLevelTierNamePut(tierName,body,tierLevel,contentType,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/update-permission")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update tier permission", notes = "This operation can be used to update tier permissions which controls access for the particular tier based on the subscribers' roles.\n", response = TierDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessfully updated tier permissions\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response tiersUpdatePermissionPost(@ApiParam(value = "Name of the tier\n",required=true) @QueryParam("tierName")  String tierName,
    @ApiParam(value = "List API or Application or Resource type tiers.\n",required=true, allowableValues="{values=[api, application, resource]}") @QueryParam("tierLevel")  String tierLevel,
    @ApiParam(value = "Validator for conditional requests; based on ETag (Will be supported in future).\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header (Will be supported in future).\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince,
    @ApiParam(value = ""  ) TierPermissionDTO permissions)
    {
    return delegate.tiersUpdatePermissionPost(tierName,tierLevel,ifMatch,ifUnmodifiedSince,permissions);
    }
}

