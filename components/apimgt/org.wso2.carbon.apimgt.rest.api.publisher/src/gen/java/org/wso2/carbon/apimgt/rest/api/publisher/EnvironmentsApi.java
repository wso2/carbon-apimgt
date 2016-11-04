package org.wso2.carbon.apimgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.EnvironmentsApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/environments")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the environments API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:47:43.416+05:30")
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
)
    throws NotFoundException {
        return delegate.environmentsGet(apiId);
    }
}
