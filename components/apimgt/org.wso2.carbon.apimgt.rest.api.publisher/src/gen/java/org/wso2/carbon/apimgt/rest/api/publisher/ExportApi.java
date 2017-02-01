package org.wso2.carbon.apimgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ExportApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;
import java.io.File;

@Component(
    name = "org.wso2.carbon.apimgt.import.export.ExportApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v0.10/export")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the export API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-19T18:57:34.679+05:30")
public class ExportApi implements Microservice  {
   private final ExportApiService delegate = ExportApiServiceFactory.getExportApi();

    @GET
    @Path("/apis")
    @Consumes({ "application/json" })
    @Produces({ "application/zip" })
    @io.swagger.annotations.ApiOperation(value = "Export information related to an API.", notes = "This operation can be used to export information related to a particular API. ", response = File.class, tags={ "Export Configuration", })
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Export Configuration returned. ", response = File.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = File.class),

            @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = File.class),

            @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = File.class) })
    public Response exportApisGet(@ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType
            ,@ApiParam(value = "API search query ",required=true) @QueryParam("query") String query
            ,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
            ,@ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
    )
            throws NotFoundException {
        return delegate.exportApisGet(contentType,query,limit,offset);
    }
}
