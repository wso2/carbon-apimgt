package org.wso2.carbon.apimgt.rest.api.core;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.core.factories.ApplicationsApiServiceFactory;

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
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
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
    name = "org.wso2.carbon.apimgt.rest.api.core.ApplicationsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/core/v1.[\\d]+/applications")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/applications")
@io.swagger.annotations.Api(description = "the applications API")
public class ApplicationsApi implements Microservice  {
   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Return the list of applications of an API ", notes = "Return the list of applications ", response = ApplicationListDTO.class, tags={ "Applications of API", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. The list of Applications. ", response = ApplicationListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ApplicationListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ApplicationListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ApplicationListDTO.class) })
    public Response applicationsGet(@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
, @Context Request request)
    throws NotFoundException {
        return delegate.applicationsGet(accept, request);
    }
}
