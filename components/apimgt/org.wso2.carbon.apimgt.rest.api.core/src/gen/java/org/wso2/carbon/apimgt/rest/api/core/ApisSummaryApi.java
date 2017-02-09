package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.carbon.apimgt.rest.api.core.factories.ApisSummaryApiServiceFactory;

import org.wso2.carbon.apimgt.rest.api.core.dto.APISummaryListDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.ApisSummaryApi",
    service = Microservice.class,
    immediate = true
)
@Path("/apis-summary")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the apis-summary API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-09T14:55:54.962+05:30")
public class ApisSummaryApi implements Microservice  {
   private final ApisSummaryApiService delegate = ApisSummaryApiServiceFactory.getApisSummaryApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Summary of all available apis", notes = "Request summary of all available apis.", response = APISummaryListDTO.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Subscription Validation Information. ", response = APISummaryListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = APISummaryListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = APISummaryListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = APISummaryListDTO.class) })
    public Response apisSummaryGet()
    throws NotFoundException {
        return delegate.apisSummaryGet();
    }
}
