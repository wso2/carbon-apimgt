package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.ImportApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APIInfoListDTO;

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
    @Path("/api")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Import an API", notes = "This operation can be used to import an API.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Created.\nAPI Imported Successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden\nNot Authorized to import.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API to update not found.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict.\nAPI to import already exists.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.\nError in importing API.\n") })

    public Response importApiPost(@ApiParam(value = "Zip archive consisting on exported api configuration\n") @Multipart(value = "file") InputStream fileInputStream,
    @ApiParam(value = "Zip archive consisting on exported api configuration\n : details") @Multipart(value = "file" ) Attachment fileDetail,
    @ApiParam(value = "Preserve Original Provider of the API. This is the user choice to keep or replace the API provider.\n") @QueryParam("preserveProvider")  Boolean preserveProvider,
    @ApiParam(value = "Whether to update the API or not. This is used when updating already existing APIs.\n") @QueryParam("overwrite")  Boolean overwrite)
    {
    return delegate.importApiPost(fileInputStream,fileDetail,preserveProvider,overwrite);
    }
    @POST
    @Path("/applications")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Import an Application", notes = "This operation can be used to import an Application.\n", response = ApplicationInfoDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with the updated object information as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 207, message = "Multi Status.\nPartially successful response with skipped APIs information object as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response importApplicationsPost(@ApiParam(value = "Zip archive consisting of exported Application Configuration.\n") @Multipart(value = "file") InputStream fileInputStream,
    @ApiParam(value = "Zip archive consisting of exported Application Configuration.\n : details") @Multipart(value = "file" ) Attachment fileDetail,
    @ApiParam(value = "Preserve Original Creator of the Application\n") @QueryParam("preserveOwner")  Boolean preserveOwner,
    @ApiParam(value = "Skip importing Subscriptions of the Application\n") @QueryParam("skipSubscriptions")  Boolean skipSubscriptions,
    @ApiParam(value = "Expected Owner of the Application in the Import Environment\n") @QueryParam("appOwner")  String appOwner,
    @ApiParam(value = "Skip importing Keys of the Application\n") @QueryParam("skipApplicationKeys")  Boolean skipApplicationKeys,
    @ApiParam(value = "Update if application exists\n") @QueryParam("update")  Boolean update)
    {
    return delegate.importApplicationsPost(fileInputStream,fileDetail,preserveOwner,skipSubscriptions,appOwner,skipApplicationKeys,update);

    }
}

