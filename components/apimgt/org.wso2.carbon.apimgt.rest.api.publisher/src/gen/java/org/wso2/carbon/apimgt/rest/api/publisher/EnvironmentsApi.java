package org.wso2.carbon.apimgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.EnvironmentsApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1/environments")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the environments API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-23T18:39:28.727+05:30")
public class EnvironmentsApi implements Microservice  {
   private final EnvironmentsApiService delegate = EnvironmentsApiServiceFactory.getEnvironmentsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all gateway environments", notes = "This operation can be used to retrieve the list of gateway environments available. ", response = EnvironmentListDTO.class, tags={ "Environment (Collection)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Environment list is returned. ", response = EnvironmentListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = EnvironmentListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = EnvironmentListDTO.class) })
    public Response environmentsGet(@ApiParam(value = "Will return environment list for the provided API. ") @QueryParam("apiId") String apiId
,@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
,@ApiParam(value = "Validator for API Minor Version " , defaultValue="1.0")@HeaderParam("Minor-Version") String minorVersion
)
    throws NotFoundException {
        return delegate.environmentsGet(apiId,accept,ifNoneMatch,ifModifiedSince,minorVersion);
    }
}
