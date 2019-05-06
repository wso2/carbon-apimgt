package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.TiersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TierDTO;

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
    @io.swagger.annotations.ApiOperation(value = "Get all available tiers", notes = "Get available tiers\n", response = TierListDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of tiers returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response tiersTierLevelGet(@ApiParam(value = "List Application or Subscription type tiers.\n",required=true, allowableValues="{values=[application, subscription]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.tiersTierLevelGet(tierLevel,limit,offset,ifNoneMatch);
    }
    @GET
    @Path("/{tierLevel}/{tierId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of a tier\n", notes = "This operation can be used to retrieve details of a single tier by specifying the tier level and tier name.\n\n`X-WSO2-Tenant` header can be used to retrive tiers that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n", response = TierDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThrottling Tier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Throttling Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response tiersTierLevelTierIdGet(@ApiParam(value = "Tier Id represented as a UUID\n",required=true ) @PathParam("tierId")  String tierId,
    @ApiParam(value = "List Application or Subscription type tiers.\n",required=true, allowableValues="{values=[application, subscription]}" ) @PathParam("tierLevel")  String tierLevel,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.tiersTierLevelTierIdGet(tierId,tierLevel,xWSO2Tenant,ifNoneMatch);
    }
}

