package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.ExportApiServiceImpl;
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
@Path("/export")

@Api(description = "the export API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ExportApi  {

  @Context MessageContext securityContext;

ExportApiService delegate = new ExportApiServiceImpl();


    @GET
    @Path("/api")
    @Consumes({ "application/json" })
    @Produces({ "application/zip" })
    @ApiOperation(value = "Export an API", notes = "This operation can be used to export the details of a particular API as a zip file. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs")
        })
    }, tags={ "API (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Successful. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. Error in exporting API. ", response = ErrorDTO.class) })
    public Response exportApiGet( @NotNull @ApiParam(value = "API Name ",required=true)  @QueryParam("name") String name,  @NotNull @ApiParam(value = "Version of the API ",required=true)  @QueryParam("version") String version,  @NotNull @ApiParam(value = "Format of output documents. Can be YAML or JSON. ",required=true, allowableValues="JSON, YAML")  @QueryParam("format") String format,  @ApiParam(value = "Provider name of the API ")  @QueryParam("providerName") String providerName,  @ApiParam(value = "Preserve API Status on export ")  @QueryParam("preserveStatus") Boolean preserveStatus) throws APIManagementException{
        return delegate.exportApiGet(name, version, format, providerName, preserveStatus, securityContext);
    }

    @GET
    @Path("/applications")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/zip" })
    @ApiOperation(value = "Export an Application", notes = "This operation can be used to export the details of a particular Application as a zip file. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_import_export", description = "Import and export applications")
        })
    }, tags={ "Application (Individual)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Successful. ", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Application does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response exportApplicationsGet( @NotNull @ApiParam(value = "Application Name ",required=true)  @QueryParam("appName") String appName,  @NotNull @ApiParam(value = "Owner of the Application ",required=true)  @QueryParam("appOwner") String appOwner,  @ApiParam(value = "Export application keys ")  @QueryParam("withKeys") Boolean withKeys) throws APIManagementException{
        return delegate.exportApplicationsGet(appName, appOwner, withKeys, securityContext);
    }
}
