package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.store.v1.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ExportApiServiceImpl;

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
    @Path("/applications")
    @Consumes({ "application/json" })
    @Produces({ "application/zip" })
    @ApiOperation(value = "Export details related to an Application.", notes = "This operation can be used to export details related to a perticular application. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Import and Export Applications" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Configuration returned. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Application does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response exportApplicationsGet( @NotNull @ApiParam(value = "Application Search Query ",required=true)  @QueryParam("appId") String appId) {
        return delegate.exportApplicationsGet(appId, securityContext);
    }
}
