package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIKeyRevokeRequestDTO;
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


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all API Keys", notes = "Retrieve all API Keys. ", response = APIKeyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "APIKeys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API keys returned. ", response = APIKeyListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response getAllAPIKeys() throws APIManagementException{
        return delegate.getAllAPIKeys(securityContext);
    }

    @POST
    @Path("/revoke")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Revoke an API Key", notes = "Revoke an API Key for the API ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "APIKeys" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Api key revoked successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response revokeAPIKeyFromAdmin(@ApiParam(value = "API key revoke object " ,required=true) APIKeyRevokeRequestDTO apIKeyRevokeRequestDTO) throws APIManagementException{
        return delegate.revokeAPIKeyFromAdmin(apIKeyRevokeRequestDTO, securityContext);
    }
}
