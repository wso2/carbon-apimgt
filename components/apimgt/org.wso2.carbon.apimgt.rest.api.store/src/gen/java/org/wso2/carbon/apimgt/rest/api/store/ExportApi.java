package org.wso2.carbon.apimgt.rest.api.store;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.store.factories.ExportApiServiceFactory;

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
    name = "org.wso2.carbon.apimgt.rest.api.store.ExportApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/store/v1.[\\d]+/export")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/export")
@io.swagger.annotations.Api(description = "the export API")
public class ExportApi implements Microservice  {
   private final ExportApiService delegate = ExportApiServiceFactory.getExportApi();

    @OPTIONS
    @GET
    @Path("/applications")
    @Consumes({ "application/json" })
    @Produces({ "application/zip" })
    @io.swagger.annotations.ApiOperation(value = "Export details related to an Application.", notes = "This operation can be used to export details related to a perticular application. ", response = File.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Export Configuration returned. ", response = File.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Application does not exist. ", response = File.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = File.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = File.class) })
    public Response exportApplicationsGet(@ApiParam(value = "Application Search Query ",required=true) @QueryParam("appId") String appId
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.exportApplicationsGet(appId,request);
    }
}
