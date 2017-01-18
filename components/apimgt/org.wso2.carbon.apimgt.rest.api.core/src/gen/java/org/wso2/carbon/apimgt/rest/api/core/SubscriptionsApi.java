package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.carbon.apimgt.rest.api.core.factories.SubscriptionsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionListDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.SubscriptionsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/subscriptions")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the subscriptions API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-06T17:54:33.855+05:30")
public class SubscriptionsApi implements Microservice  {
   private final SubscriptionsApiService delegate = SubscriptionsApiServiceFactory.getSubscriptionsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Return the list of subscriptions of an API ", notes = "Return the list of subscriptions of an API, by proving API context and version. Response consist of API Context, API version, Consumer Key and Subscription Policy ", response = SubscriptionListDTO.class, tags={ "Subscriptions of API", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. The list of Subscriptions. ", response = SubscriptionListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = SubscriptionListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = SubscriptionListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = SubscriptionListDTO.class) })
    public Response subscriptionsGet(@ApiParam(value = "Context of the API. ") @QueryParam("apiContext") String apiContext
,@ApiParam(value = "Version of the API. ") @QueryParam("apiVersion") String apiVersion
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
)
    throws NotFoundException {
        return delegate.subscriptionsGet(apiContext,apiVersion,accept);
    }
}
