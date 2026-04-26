package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UntraffickedListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.GovernanceApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.GovernanceApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/governance")

@Api(description = "the governance API")




public class GovernanceApi  {

  @Context MessageContext securityContext;

GovernanceApiService delegate = new GovernanceApiServiceImpl();


    @GET
    @Path("/discovery/apis/{discoveredApiId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get discovered API detail", notes = "", response = DiscoveredAPIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_discovery_view", description = "")
        })
    }, tags={ "Unmanaged APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = DiscoveredAPIDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getDiscoveredAPIById(@ApiParam(value = "",required=true) @PathParam("discoveredApiId") String discoveredApiId) throws APIManagementException{
        return delegate.getDiscoveredAPIById(discoveredApiId, securityContext);
    }

    @GET
    @Path("/discovery/apis")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List discovered APIs", notes = "Paginated list of unmanaged discovered APIs. Filterable by classification, service, and internal-only flag. ", response = DiscoveredAPIListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_discovery_view", description = "")
        })
    }, tags={ "Unmanaged APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = DiscoveredAPIListDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getDiscoveredAPIs( @ApiParam(value = "", allowableValues="shadow, drift")  @QueryParam("classification") String classification,  @ApiParam(value = "")  @QueryParam("service") String service,  @ApiParam(value = "Filter by internal flag. \"true\" = include internal, \"false\" = external only, \"only\" = internal only. Omit for all. ", allowableValues="true, false, only")  @QueryParam("internal") String internal,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.getDiscoveredAPIs(classification, service, internal, limit, offset, securityContext);
    }

    @GET
    @Path("/discovery/summary")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get summary of discovered APIs by classification ", notes = "Returns aggregate counts of discovered APIs by classification (shadow, drift), reachability (external, internal), and service. ", response = DiscoverySummaryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_discovery_view", description = "")
        })
    }, tags={ "Unmanaged APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Summary returned. ", response = DiscoverySummaryDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getDiscoverySummary() throws APIManagementException{
        return delegate.getDiscoverySummary(securityContext);
    }

    @GET
    @Path("/discovery/untrafficked")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List untrafficked managed APIs", notes = "Managed APIs registered in APIM but with no observed traffic. Useful for identifying dead APIs or DeepFlow coverage gaps. ", response = UntraffickedListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_discovery_view", description = "")
        })
    }, tags={ "Unmanaged APIs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = UntraffickedListDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getUntrafficked() throws APIManagementException{
        return delegate.getUntrafficked(securityContext);
    }
}
