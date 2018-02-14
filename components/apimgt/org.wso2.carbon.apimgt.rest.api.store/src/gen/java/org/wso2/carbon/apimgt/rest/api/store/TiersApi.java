package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.factories.TiersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;

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
    @io.swagger.annotations.ApiOperation(value = "Get available tiers\n", notes = "This operation can be used to retrieve all the tiers available for the provided tier level. Tier level should be specified as a path parameter and should be one of `api` and `application`.\n\n`X-WSO2-Tenant` header can be used to retrive tiers that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n\n**NOTE**:\n* API tiers are the ones that is available during subscription of an application to an API. Hence they are also called subscription tiers and are same as the subscription policies in Admin REST API.\n", response = TierListDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of tiers returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response tiersTierLevelGet(@ApiParam(value = "List API or Application type tiers.\n",required=true, allowableValues="{values=[api, application]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.tiersTierLevelGet(tierLevel,limit,offset,xWSO2Tenant,accept,ifNoneMatch);
    }

    public String tiersTierLevelGetGetLastUpdatedTime(String tierLevel,Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch)
    {
        return delegate.tiersTierLevelGetGetLastUpdatedTime(tierLevel,limit,offset,xWSO2Tenant,accept,ifNoneMatch);
    }
    @GET
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of a tier\n", notes = "This operation can be used to retrieve details of a single tier by specifying the tier level and tier name.\n\n`X-WSO2-Tenant` header can be used to retrive tiers that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n", response = TierDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response tiersTierLevelTierNameGet(@ApiParam(value = "Tier name\n",required=true ) @PathParam("tierName")  String tierName,
    @ApiParam(value = "List API or Application type tiers.\n",required=true, allowableValues="{values=[api, application]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource (Will be supported in future).\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.tiersTierLevelTierNameGet(tierName,tierLevel,xWSO2Tenant,accept,ifNoneMatch,ifModifiedSince);
    }

    public String tiersTierLevelTierNameGetGetLastUpdatedTime(String tierName,String tierLevel,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince)
    {
        return delegate.tiersTierLevelTierNameGetGetLastUpdatedTime(tierName,tierLevel,xWSO2Tenant,accept,ifNoneMatch,ifModifiedSince);
    }
}

