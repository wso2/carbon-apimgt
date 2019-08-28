package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ExportApiServiceImpl;

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
    @Path("/apis")
    @Consumes({ "application/json" })
    @Produces({ "application/zip" })
    @ApiOperation(value = "Export information related to an API.", notes = "This operation can be used to export information related to a particular API. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Export Configuration" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Configuration returned. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response exportApisGet( @NotNull @ApiParam(value = "API search query ",required=true)  @QueryParam("query") String query,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) {
        return delegate.exportApisGet(query, limit, offset, securityContext);
    }
}
