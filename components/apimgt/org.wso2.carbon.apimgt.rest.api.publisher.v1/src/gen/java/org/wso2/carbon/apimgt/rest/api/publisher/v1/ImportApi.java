package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.factories.ImportApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/import")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/import", description = "the import API")
public class ImportApi  {

   private final ImportApiService delegate = ImportApiServiceFactory.getImportApi();

    @POST
    @Path("/apis")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports API(s).", notes = "This operation can be used to import one or more existing APIs.\n", response = APIListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with the updated object as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response importApisPost(@ApiParam(value = "Zip archive consisting on exported api configuration\n") @Multipart(value = "file") InputStream fileInputStream,
    @ApiParam(value = "Zip archive consisting on exported api configuration\n : details") @Multipart(value = "file" ) Attachment fileDetail,
    @ApiParam(value = "If defined, updates the existing provider of each API with the specified provider.\nThis is to cater scenarios where the current API provider does not exist in the environment\nthat the API is imported to.\n") @QueryParam("provider")  String provider)
    {
    return delegate.importApisPost(fileInputStream,fileDetail,provider);
    }
    @PUT
    @Path("/apis")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports API(s).", notes = "This operation can be used to import one or more existing APIs.\n", response = APIListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with the updated object as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response importApisPut(@ApiParam(value = "Zip archive consisting on exported api configuration\n") @Multipart(value = "file") InputStream fileInputStream,
    @ApiParam(value = "Zip archive consisting on exported api configuration\n : details") @Multipart(value = "file" ) Attachment fileDetail,
    @ApiParam(value = "If defined, updates the existing provider of each API with the specified provider.\nThis is to cater scenarios where the current API provider does not exist in the environment\nthat the API is imported to.\n") @QueryParam("provider")  String provider)
    {
    return delegate.importApisPut(fileInputStream,fileDetail,provider);
    }
}

