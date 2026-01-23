package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApiKeysApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.ApiKeysApiServiceImpl;
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
@Path("/api-keys")

@Api(description = "the api-keys API")




public class ApiKeysApi  {

  @Context MessageContext securityContext;

ApiKeysApiService delegate = new ApiKeysApiServiceImpl();


    @DELETE
    @Path("/{applicationId}/{keyType}/{keyDisplayName}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Revoke API Key", notes = "Revoke an API Key for the application ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "API Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Api key revoked successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response apiKeysApplicationIdKeyTypeKeyDisplayNameDelete(@ApiParam(value = "Application UUID ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox). ",required=true, allowableValues="PRODUCTION, SANDBOX") @PathParam("keyType") String keyType, @ApiParam(value = "Name of the API key. ",required=true) @PathParam("keyDisplayName") String keyDisplayName) throws APIManagementException{
        return delegate.apiKeysApplicationIdKeyTypeKeyDisplayNameDelete(applicationId, keyType, keyDisplayName, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all API Keys", notes = "Retrieve all API Keys. ", response = APIKeyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "API Keys (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API keys returned. ", response = APIKeyListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response apiKeysGet() throws APIManagementException{
        return delegate.apiKeysGet(securityContext);
    }
}
