package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.store.factories.ImportApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
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

    public Response importApplicationsPost(@ApiParam(value = "Zip archive consisting of exported application configuration\n") @Multipart(value = "file") InputStream fileInputStream,
    @ApiParam(value = "Zip archive consisting of exported application configuration\n : details") @Multipart(value = "file" ) Attachment fileDetail,
    @ApiParam(value = "Preserve Original Creator of the Application\n") @QueryParam("preserveOwner")  Boolean preserveOwner,
    @ApiParam(value = "Import Subscriptions of the Application\n") @QueryParam("addSubscriptions")  Boolean addSubscriptions)
    {
    return delegate.importApplicationsPost(fileInputStream,fileDetail,preserveOwner,addSubscriptions);
    }

    public String importApplicationsPostGetLastUpdatedTime(InputStream fileInputStream,Attachment fileDetail,Boolean preserveOwner,Boolean addSubscriptions)
    {
        return delegate.importApplicationsPostGetLastUpdatedTime(fileInputStream,fileDetail,preserveOwner,addSubscriptions);
    }
}

