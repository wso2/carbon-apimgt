package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.KeyManagersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.KeyManagerDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/key-managers")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/key-managers", description = "the key-managers API")
public class KeyManagersApi  {

   private final KeyManagersApiService delegate = KeyManagersApiServiceFactory.getKeyManagersApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Key managers", notes = "Get all Key managers\n", response = KeyManagerListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeyManagers returned\n") })

    public Response keyManagersGet()
    {
    return delegate.keyManagersGet();
    }
    @DELETE
    @Path("/{keyManagerId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Key Manager", notes = "Delete a Key Manager by keyManager id\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKey Manager successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nKey Manager to be deleted does not exist.\n") })

    public Response keyManagersKeyManagerIdDelete(@ApiParam(value = "Key Manager UUID\n",required=true ) @PathParam("keyManagerId")  String keyManagerId)
    {
    return delegate.keyManagersKeyManagerIdDelete(keyManagerId);
    }
    @GET
    @Path("/{keyManagerId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a Key Manager Configuration", notes = "Retrieve a single Key Manager Configuration. We should provide the Id of the KeyManager as a path parameter.\n", response = KeyManagerDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeyManager Configuration returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested KeyManager Configuration does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response keyManagersKeyManagerIdGet(@ApiParam(value = "Key Manager UUID\n",required=true ) @PathParam("keyManagerId")  String keyManagerId)
    {
    return delegate.keyManagersKeyManagerIdGet(keyManagerId);
    }
    @PUT
    @Path("/{keyManagerId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Key Manager", notes = "Update a Key Manager by keyManager id\n", response = KeyManagerDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nLabel updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n") })

    public Response keyManagersKeyManagerIdPut(@ApiParam(value = "Key Manager UUID\n",required=true ) @PathParam("keyManagerId")  String keyManagerId,
    @ApiParam(value = "Key Manager object with updated information\n" ,required=true ) KeyManagerDTO body)
    {
    return delegate.keyManagersKeyManagerIdPut(keyManagerId,body);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new API Key Manager", notes = "Add a new API Key Manager\n", response = KeyManagerDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n") })

    public Response keyManagersPost(@ApiParam(value = "Key Manager object that should to be added\n" ,required=true ) KeyManagerDTO body)
            throws APIManagementException {
    return delegate.keyManagersPost(body);
    }
}

