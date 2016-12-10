package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.carbon.apimgt.rest.api.core.factories.ValidateSubscriptionApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionValidationInfoDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.ValidateSubscriptionApi",
    service = Microservice.class,
    immediate = true
)
@Path("/validate-subscription")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the validate-subscription API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-12-08T17:11:20.964+05:30")
public class ValidateSubscriptionApi implements Microservice  {
   private final ValidateSubscriptionApiService delegate = ValidateSubscriptionApiServiceFactory.getValidateSubscriptionApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Validate a subscription ", notes = "Validate a subscription by proving API context, version and consumer key of application. ", response = SubscriptionValidationInfoDTO.class, tags={ "Subscription Validation", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Subscription Validation Information. ", response = SubscriptionValidationInfoDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = SubscriptionValidationInfoDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = SubscriptionValidationInfoDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = SubscriptionValidationInfoDTO.class) })
    public Response validateSubscriptionGet(@ApiParam(value = "Context of the API. ",required=true) @QueryParam("apiContext") String apiContext
,@ApiParam(value = "Version of the API. ",required=true) @QueryParam("apiVersion") String apiVersion
,@ApiParam(value = "Consumer Key of the Application. ",required=true) @QueryParam("consumerKey") String consumerKey
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
)
    throws NotFoundException {
        return delegate.validateSubscriptionGet(apiContext,apiVersion,consumerKey,accept);
    }
}
