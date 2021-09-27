package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TenantListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.TenantsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.TenantsApiServiceImpl;
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
@Path("/tenants")

@Api(description = "the tenants API")




public class TenantsApi  {

  @Context MessageContext securityContext;

TenantsApiService delegate = new TenantsApiServiceImpl();


    @HEAD
    @Path("/{tenantDomain}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Check Whether the Given Tenant already Exists", notes = "Using this operation, user can check whether a given tenant exists or not. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Tenants",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested tenant exists.", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getTenantExistence(@ApiParam(value = "The domain of a specific tenant ",required=true) @PathParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.getTenantExistence(tenantDomain, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Tenants by State ", notes = "This operation is to get tenants by state ", response = TenantListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Tenants" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Tenant names returned. ", response = TenantListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getTenantsByState( @ApiParam(value = "The state represents the current state of the tenant  Supported states are [active, inactive] ", allowableValues="active, inactive", defaultValue="active") @DefaultValue("active") @QueryParam("state") String state,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.getTenantsByState(state, limit, offset, securityContext);
    }
}
