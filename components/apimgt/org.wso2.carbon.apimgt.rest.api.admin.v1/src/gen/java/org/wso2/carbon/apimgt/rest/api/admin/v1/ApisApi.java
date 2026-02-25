package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SearchResultListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.ApisApiServiceImpl;
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
@Path("/apis")

@Api(description = "the apis API")




public class ApisApi  {

  @Context MessageContext securityContext;

ApisApiService delegate = new ApisApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search APIs ", notes = "This operation provides you a list of available APIs qualifying under a given search condition. Each retrieved API is represented with a minimal amount of attributes. If you want to get complete details of an API, you need to use **Get details of an API** operation. ", response = SearchResultListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:api_provider_change", description = "Retrieve and manage applications")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying APIs is returned. ", response = SearchResultListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAllAPIs( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "**Search and get all apis in admin portal**.  You can search by proving a keyword. ")  @QueryParam("query") String query,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAllAPIs(limit, offset, query, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/{apiId}/change-provider")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Update the api provider", notes = "Update the api provider ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:api_provider_change", description = "Retrieve and manage applications")
        })
    }, tags={ "Api Provider Change" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Api Provider updated. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response providerNamePost( @NotNull @ApiParam(value = "",required=true)  @QueryParam("provider") String provider, @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.providerNamePost(provider, apiId, securityContext);
    }
}
