package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.store.factories.ExportApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/export")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/export", description = "the export API")
public class ExportApi  {

   private final ExportApiService delegate = ExportApiServiceFactory.getExportApi();

    @GET
    @Path("/applications")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/zip" })
    @io.swagger.annotations.ApiOperation(value = "Export an Application", notes = "This operation can be used to export the details of a particular Application as a zip file.\n", response = File.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nExport Successful.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Application does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response exportApplicationsGet(@ApiParam(value = "Application Search Query\n",required=true) @QueryParam("appId")  String appId)
    {
    return delegate.exportApplicationsGet(appId);
    }

    public String exportApplicationsGetGetLastUpdatedTime(String appId)
    {
        return delegate.exportApplicationsGetGetLastUpdatedTime(appId);
    }
}

