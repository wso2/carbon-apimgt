package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.carbon.apimgt.rest.api.core.factories.GatewaysApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationSummaryDTO;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.GatewaysApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/core/v1.[\\d]+/gateways")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the gateways API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-26T22:35:57.497+05:30")
public class GatewaysApi implements Microservice  {
   private final GatewaysApiService delegate = GatewaysApiServiceFactory.getGatewaysApi();

    @POST
    @Path("/register")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Register a Gateway", notes = "This operation can be used to register a gateway. ", response = RegistrationSummaryDTO.class, tags={ "Register Gateway", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with Registration Summary details in the body. ", response = RegistrationSummaryDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = RegistrationSummaryDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format. ", response = RegistrationSummaryDTO.class) })
    public Response gatewaysRegisterPost(@ApiParam(value = "Register object that needs to be added " ,required=true) RegistrationDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.gatewaysRegisterPost(body,contentType,ifMatch,ifUnmodifiedSince, request);
    }
}
