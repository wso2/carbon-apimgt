package org.wso2.carbon.apimgt.rest.api.analytics;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.factories.SubscriptionApiServiceFactory;

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
    name = "org.wso2.carbon.apimgt.rest.api.analytics.SubscriptionApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/analytics/v1.[\\d]+/subscription")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/subscription")
@io.swagger.annotations.Api(description = "the subscription API")
public class SubscriptionApi implements Microservice  {
   private final SubscriptionApiService delegate = SubscriptionApiServiceFactory.getSubscriptionApi();

    @OPTIONS
    @GET
    @Path("/count-over-time")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve APIs created over time details ", notes = "Get application created over time details from summarized data. ", response = SubscriptionCountListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:subscription_graph", description = "View Graphs Releated to subscriptions")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested subscription count created over time information is returned ", response = SubscriptionCountListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Request paramters attribute does not meet requiremnts. ", response = SubscriptionCountListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = SubscriptionCountListDTO.class) })
    public Response subscriptionCountOverTimeGet(@ApiParam(value = "Defines the starting timestamp of the interval ",required=true) @QueryParam("startTime") String startTime
,@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("endTime") String endTime
,@ApiParam(value = "application/api creator name. In case of any creator is not provided all the details will be provided ") @QueryParam("createdBy") String createdBy
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.subscriptionCountOverTimeGet(startTime,endTime,createdBy,request);
    }
    @OPTIONS
    @GET
    @Path("/list")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve Subscription details ", notes = "Get Subscription details from summarized data. ", response = SubscriptionInfoListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:subscription_graph", description = "View Graphs Releated to subscriptions")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested Subscription Information is returned for the given time period ", response = SubscriptionInfoListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Request paramters attribute does not meet requiremnts. ", response = SubscriptionInfoListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = SubscriptionInfoListDTO.class) })
    public Response subscriptionListGet(@ApiParam(value = "Defines the starting timestamp of the interval ",required=true) @QueryParam("startTime") String startTime
,@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("endTime") String endTime
,@ApiParam(value = "application/api creator name. In case of any creator is not provided all the details will be provided ") @QueryParam("createdBy") String createdBy
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.subscriptionListGet(startTime,endTime,createdBy,request);
    }
}
