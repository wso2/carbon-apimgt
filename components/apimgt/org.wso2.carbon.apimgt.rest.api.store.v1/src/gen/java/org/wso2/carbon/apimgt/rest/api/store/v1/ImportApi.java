package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.ImportApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
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
    @Path("/applications")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports an Application.", notes = "This operation can be used to import an existing Application.\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with the updated object as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response importApplicationsPost(@ApiParam(value = "Zip archive consisting on exported application configuration\n") @Multipart(value = "file") InputStream fileInputStream,
    @ApiParam(value = "Zip archive consisting on exported application configuration\n : details") @Multipart(value = "file" ) Attachment fileDetail)
    {
    return delegate.importApplicationsPost(fileInputStream,fileDetail);
    }
    @PUT
    @Path("/applications")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports an Updates an Application.", notes = "This operation can be used to import an existing Application.\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with the updated object as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response importApplicationsPut(@ApiParam(value = "Zip archive consisting on exported application configuration\n") @Multipart(value = "file") InputStream fileInputStream,
    @ApiParam(value = "Zip archive consisting on exported application configuration\n : details") @Multipart(value = "file" ) Attachment fileDetail)
    {
    return delegate.importApplicationsPut(fileInputStream,fileDetail);
    }
}

