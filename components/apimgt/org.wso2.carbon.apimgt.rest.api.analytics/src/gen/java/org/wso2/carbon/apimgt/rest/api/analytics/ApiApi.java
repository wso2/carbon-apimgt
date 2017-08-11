package org.wso2.carbon.apimgt.rest.api.analytics;


import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APICountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APISubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.factories.ApiApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
@ApplicationPath("/api")
@io.swagger.annotations.Api(description = "the api API")
public class ApiApi implements Microservice  {
   private final ApiApiService delegate = ApiApiServiceFactory.getApiApi();

    @GET
    @Path("/api_info")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve APIs created over time details ", notes = "Get application created over time details from summarized data. ", response = APIInfoListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested APIs created over time information is returned ", response = APIInfoListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = APIInfoListDTO.class) })
    public Response apiApiInfoGet(@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("from") String from
,@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("to") String to
,@ApiParam(value = "application creator name. In case of any creator, the value shold be equal to 'all' ",required=true) @QueryParam("created_by") String createdBy
,@ApiParam(value = "api_filter could take two possible values. 'All' or 'My'. In case of 'My', only the current user's Apis will be filtered. ",required=true) @QueryParam("api_filter") String apiFilter
, @Context Request request)
    throws NotFoundException {
        return delegate.apiApiInfoGet(from,to,createdBy,apiFilter, request);
    }
    @GET
    @Path("/apis_created_over_time")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve APIs created over time details ", notes = "Get application created over time details from summarized data. ", response = APICountListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested APIs created over time information is returned ", response = APICountListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = APICountListDTO.class) })
    public Response apiApisCreatedOverTimeGet(@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("from") String from
,@ApiParam(value = "Defines the ending timestamp of the interval ",required=true) @QueryParam("to") String to
,@ApiParam(value = "application creator name. In case of any creator, the value shold be equal to 'all' ",required=true) @QueryParam("created_by") String createdBy
, @Context Request request)
    throws NotFoundException {
        return delegate.apiApisCreatedOverTimeGet(from,to,createdBy, request);
    }
    @GET
    @Path("/subscriber_count_by_apis")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve Subscriber count by APIs ", notes = "Get subscriber count by APIs from summarized data. ", response = APISubscriptionListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested subscriber count by API information is returned ", response = APISubscriptionListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = APISubscriptionListDTO.class) })
    public Response apiSubscriberCountByApisGet(@ApiParam(value = "application creator name. In case of any creator, the value shold be equal to 'all' ",required=true) @QueryParam("created_by") String createdBy
, @Context Request request)
    throws NotFoundException {
        return delegate.apiSubscriberCountByApisGet(createdBy, request);
    }
}
