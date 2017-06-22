package org.wso2.carbon.apimgt.rest.api.analytics;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.analytics.factories.ApiApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
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
    name = "org.wso2.carbon.apimgt.rest.api.analytics.ApiApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/analytics/v1.[\\d]+/api")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the api API")
public class ApiApi implements Microservice  {
   private final ApiApiService delegate = ApiApiServiceFactory.getApiApi();

    @GET
    @Path("/api_usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve api usage details ", notes = "Get api usage information from summarized data. ", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested api usage information is returned ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = void.class) })
    public Response apiApiUsageGet(@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("from") String from
,@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("to") String to
, @Context Request request)
    throws NotFoundException {
        return delegate.apiApiUsageGet(from,to, request);
    }
}
