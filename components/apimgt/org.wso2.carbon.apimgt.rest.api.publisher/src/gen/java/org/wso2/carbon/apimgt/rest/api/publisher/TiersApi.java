package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.TiersApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierList;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Tier;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/tiers")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the tiers API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:47:36.442+05:30")
public class TiersApi  {
   private final TiersApiService delegate = TiersApiServiceFactory.getTiersApi();

    @GET
    @Path("/{tierLevel}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get available tiers ", response = TierList.class, responseContainer = "List", tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  List of tiers returned. ", response = TierList.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = TierList.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported ", response = TierList.class, responseContainer = "List") })
    public Response tiersTierLevelGet(@ApiParam(value = "List API or Application type tiers. ",required=true, allowableValues="api, application") @PathParam("tierLevel") String tierLevel
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Starting point within the complete list of items qualified.   ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
,@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be    retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
)
    throws NotFoundException {
        return delegate.tiersTierLevelGet(tierLevel,limit,offset,xWSO2Tenant,accept,ifNoneMatch);
    }
    @GET
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get tier details ", response = Tier.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Tier returned ", response = Tier.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = Tier.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Requested Tier does not exist. ", response = Tier.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported. ", response = Tier.class) })
    public Response tiersTierLevelTierNameGet(@ApiParam(value = "Tier name ",required=true) @PathParam("tierName") String tierName
,@ApiParam(value = "List API or Application type tiers. ",required=true, allowableValues="api, application") @PathParam("tierLevel") String tierLevel
,@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be    retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the  formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
)
    throws NotFoundException {
        return delegate.tiersTierLevelTierNameGet(tierName,tierLevel,xWSO2Tenant,accept,ifNoneMatch,ifModifiedSince);
    }
}
