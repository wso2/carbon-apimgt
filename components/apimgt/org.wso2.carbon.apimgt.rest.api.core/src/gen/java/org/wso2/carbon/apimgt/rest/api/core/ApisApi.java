package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.carbon.apimgt.rest.api.core.factories.ApisApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.ApisApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/core/v1.[\\d]+/apis")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the apis API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-26T22:35:57.497+05:30")
public class ApisApi implements Microservice  {
   private final ApisApiService delegate = ApisApiServiceFactory.getApisApi();

    @GET
    @Path("/{apiId}/gateway-config")
    @Consumes({ "application/json" })
    @Produces({ "text/plain" })
    @io.swagger.annotations.ApiOperation(value = "Get gateway definition", notes = "This operation can be used to retrieve the gateway configuration of an API. ", response = void.class, tags={ "API (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested gateway configuration of the API is returned ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = void.class) })
    public Response apisApiIdGatewayConfigGet(@ApiParam(value = "The UUID of an API ",required=true) @PathParam("apiId") String apiId
,@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.apisApiIdGatewayConfigGet(apiId,accept,ifNoneMatch,ifModifiedSince, request);
    }
}
