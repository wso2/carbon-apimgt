package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.ResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ResourcesApiServiceImpl;
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
@Path("/resources")

@Api(description = "the resources API")




public class ResourcesApi  {

  @Context MessageContext securityContext;

ResourcesApiService delegate = new ResourcesApiServiceImpl();


    @POST
    @Path("/{resourceType}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Generate a URL to download a resource for an API", notes = "Generate a download URL for an API resource (WSDL / Swagger / OpenAPI). For public APIs the URL will be returned without `exp`/`sig` query params.", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Resources" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Returns JSON with the generated download URL.", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response generateUrlToDownloadResource(@ApiParam(value = "Type of the resource (wsdl, swagger, openapi)",required=true, allowableValues="wsdl, swagger, openapi") @PathParam("resourceType") String resourceType,  @NotNull @ApiParam(value = "API identifier",required=true)  @QueryParam("apiId") String apiId,  @ApiParam(value = "Name of the API gateway environment. ")  @QueryParam("environmentName") String environmentName) throws APIManagementException{
        return delegate.generateUrlToDownloadResource(resourceType, apiId, environmentName, securityContext);
    }
}
