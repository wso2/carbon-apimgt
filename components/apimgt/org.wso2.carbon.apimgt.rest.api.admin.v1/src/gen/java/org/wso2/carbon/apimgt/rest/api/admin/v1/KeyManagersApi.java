package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerWellKnownResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.KeyManagersApiServiceImpl;
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
@Path("/key-managers")

@Api(description = "the key-managers API")




public class KeyManagersApi  {

  @Context MessageContext securityContext;

KeyManagersApiService delegate = new KeyManagersApiServiceImpl();


    @POST
    @Path("/discover")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve Well-known information from Key Manager Well-known Endpoint", notes = "Retrieve well-known information from key manager's well-known endpoint ", response = KeyManagerWellKnownResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations"),
            @AuthorizationScope(scope = "apim:keymanagers_manage", description = "Manage Key Managers")
        })
    }, tags={ "Key Manager (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. KeyManagers returned ", response = KeyManagerWellKnownResponseDTO.class) })
    public Response keyManagersDiscoverPost(@Multipart(value = "url", required = false)  String url, @Multipart(value = "type", required = false)  String type) throws APIManagementException{
        return delegate.keyManagersDiscoverPost(url, type, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Key managers", notes = "Get all Key managers ", response = KeyManagerListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations"),
            @AuthorizationScope(scope = "apim:keymanagers_manage", description = "Manage Key Managers")
        })
    }, tags={ "Key Manager (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. KeyManagers returned ", response = KeyManagerListDTO.class) })
    public Response keyManagersGet() throws APIManagementException{
        return delegate.keyManagersGet(securityContext);
    }

    @DELETE
    @Path("/{keyManagerId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Key Manager", notes = "Delete a Key Manager by keyManager id ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations")
        })
    }, tags={ "Key Manager (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Key Manager successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response keyManagersKeyManagerIdDelete(@ApiParam(value = "Key Manager UUID ",required=true) @PathParam("keyManagerId") String keyManagerId) throws APIManagementException{
        return delegate.keyManagersKeyManagerIdDelete(keyManagerId, securityContext);
    }

    @GET
    @Path("/{keyManagerId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Key Manager Configuration", notes = "Retrieve a single Key Manager Configuration. We should provide the Id of the KeyManager as a path parameter. ", response = KeyManagerDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations"),
            @AuthorizationScope(scope = "apim:keymanagers_manage", description = "Manage Key Managers")
        })
    }, tags={ "Key Manager (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. KeyManager Configuration returned ", response = KeyManagerDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response keyManagersKeyManagerIdGet(@ApiParam(value = "Key Manager UUID ",required=true) @PathParam("keyManagerId") String keyManagerId) throws APIManagementException{
        return delegate.keyManagersKeyManagerIdGet(keyManagerId, securityContext);
    }

    @PUT
    @Path("/{keyManagerId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Key Manager", notes = "Update a Key Manager by keyManager id ", response = KeyManagerDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations"),
            @AuthorizationScope(scope = "apim:keymanagers_manage", description = "Manage Key Managers")
        })
    }, tags={ "Key Manager (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Label updated. ", response = KeyManagerDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response keyManagersKeyManagerIdPut(@ApiParam(value = "Key Manager UUID ",required=true) @PathParam("keyManagerId") String keyManagerId, @ApiParam(value = "Key Manager object with updated information " ,required=true) KeyManagerDTO keyManagerDTO) throws APIManagementException{
        return delegate.keyManagersKeyManagerIdPut(keyManagerId, keyManagerDTO, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new API Key Manager", notes = "Add a new API Key Manager ", response = KeyManagerDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations"),
            @AuthorizationScope(scope = "apim:keymanagers_manage", description = "Manage Key Managers")
        })
    }, tags={ "Key Manager (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. ", response = KeyManagerDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response keyManagersPost(@ApiParam(value = "Key Manager object that should to be added " ,required=true) KeyManagerDTO keyManagerDTO) throws APIManagementException{
        return delegate.keyManagersPost(keyManagerDTO, securityContext);
    }
}
