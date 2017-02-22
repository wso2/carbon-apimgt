package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.ApplicationsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/applications")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the applications API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-22T11:40:36.320+05:30")
public class ApplicationsApi implements Microservice  {
   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

    @GET
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an application", notes = "This operation can be used to retrieve details of an individual application specifying the application id in the URI. ", response = ApplicationDTO.class, tags={ "Application (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Application returned. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested application does not exist. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ApplicationDTO.class) })
    public Response applicationsApplicationIdGet(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId
,@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
,@ApiParam(value = "Validator for API Minor Version " , defaultValue="1.0")@HeaderParam("Minor-Version") String minorVersion
)
    throws NotFoundException {
        return delegate.applicationsApplicationIdGet(applicationId,accept,ifNoneMatch,ifModifiedSince,minorVersion);
    }
}
