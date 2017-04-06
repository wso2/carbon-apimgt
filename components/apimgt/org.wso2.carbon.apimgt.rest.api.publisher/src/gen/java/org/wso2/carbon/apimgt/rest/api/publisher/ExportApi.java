package org.wso2.carbon.apimgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;
import java.io.File;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ExportApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.ExportApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1.[\\d]+/export")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the export API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-06T17:02:03.158+05:30")
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
    public Response exportApisGet(@ApiParam(value = "API search query ",required=true) @QueryParam("query") String query
,@ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
, @Context Request request)
    throws NotFoundException {
        return delegate.exportApisGet(query,contentType,limit,offset, request);
    }
}
