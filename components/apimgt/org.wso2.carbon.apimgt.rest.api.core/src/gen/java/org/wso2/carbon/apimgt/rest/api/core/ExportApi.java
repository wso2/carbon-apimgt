package org.wso2.carbon.apimgt.rest.api.core;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.core.factories.ExportApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.ExportApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/core/v1.[\\d]+/export")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/export")
@io.swagger.annotations.Api(description = "the export API")
public class ExportApi implements Microservice  {
   private final ExportApiService delegate = ExportApiServiceFactory.getExportApi();

    @OPTIONS
    @GET
    @Path("/policies/throttle")
    @Consumes({ "application/json" })
    @Produces({ "application/zip" })
    @io.swagger.annotations.ApiOperation(value = "Export Siddhi apps generated from throttle policies", notes = "This operation can be used to export Siddhi apps available in DB ", response = File.class, tags={ "Export Configuration", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Export Policies returned. ", response = File.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = File.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Policies does not exist. ", response = File.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = File.class) })
    public Response exportPoliciesThrottleGet(@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
 ,@Context Request request)
    throws NotFoundException {
        accept=accept==null?String.valueOf("application/json"):accept;
        
        return delegate.exportPoliciesThrottleGet(accept,request);
    }
}
