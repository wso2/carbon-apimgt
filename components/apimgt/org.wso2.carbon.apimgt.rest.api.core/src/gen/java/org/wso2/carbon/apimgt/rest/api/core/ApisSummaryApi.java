package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.carbon.apimgt.rest.api.core.factories.ApisSummaryApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.APISummaryListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.ApisSummaryApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/core/v1.[\\d]+/apis-summary")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the apis-summary API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-26T22:35:57.497+05:30")
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
    public Response apisSummaryGet(@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.apisSummaryGet(accept,ifNoneMatch,ifModifiedSince, request);
    }
}
