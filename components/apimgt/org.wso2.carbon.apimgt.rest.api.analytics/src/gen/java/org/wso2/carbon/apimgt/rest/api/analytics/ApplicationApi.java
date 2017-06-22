package org.wso2.carbon.apimgt.rest.api.analytics;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.analytics.dto.ApplicationCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.factories.ApplicationApiServiceFactory;

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
    name = "org.wso2.carbon.apimgt.rest.api.analytics.ApplicationApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/analytics/v1.[\\d]+/application")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the application API")
public class ApplicationApi implements Microservice  {
   private final ApplicationApiService delegate = ApplicationApiServiceFactory.getApplicationApi();

    @GET
    @Path("/applications_created_over_time")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve application created over time details ", notes = "Get application created over time details from summarized data. ", response = ApplicationCountListDTO.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested applications created over time information is returned ", response = ApplicationCountListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ApplicationCountListDTO.class) })
    public Response applicationApplicationsCreatedOverTimeGet(@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("from") String from
,@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("to") String to
,@ApiParam(value = "application creator name. In case of any creator, the value shold be equal to 'all' ",required=true) @QueryParam("created_by") String createdBy
,@ApiParam(value = "api name. In case of any api the value should be equal to 'all' ",required=true) @QueryParam("subscribed_to") String subscribedTo
,@ApiParam(value = "api_filter could take two possible values. 'All' or 'My'. In case of 'My', only the current user's Apis will be filtered. ",required=true) @QueryParam("api_filter") String apiFilter
, @Context Request request)
    throws NotFoundException {
        return delegate.applicationApplicationsCreatedOverTimeGet(from,to,createdBy,subscribedTo,apiFilter, request);
    }
}
