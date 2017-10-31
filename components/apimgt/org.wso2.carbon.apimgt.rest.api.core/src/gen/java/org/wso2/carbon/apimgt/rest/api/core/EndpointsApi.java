package org.wso2.carbon.apimgt.rest.api.core;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.EndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.core.factories.EndpointsApiServiceFactory;

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
    name = "org.wso2.carbon.apimgt.rest.api.core.EndpointsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/core/v1.[\\d]+/endpoints")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/endpoints")
@io.swagger.annotations.Api(description = "the endpoints API")
public class EndpointsApi implements Microservice  {
   private final EndpointsApiService delegate = EndpointsApiServiceFactory.getEndpointsApi();

    @GET
    @Path("/{endpointId}/gateway-config")
    @Consumes({ "application/json" })
    @Produces({ "text/plain", "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get gateway definition", notes = "This operation can be used to retrieve the gateway configuration of an Endpoint. ", response = void.class, tags={ "Endpoints (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested gateway configuration of the Endpoint is returned ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = void.class) })
    public Response endpointsEndpointIdGatewayConfigGet(@ApiParam(value = "The UUID of an Endpoint ",required=true) @PathParam("endpointId") String endpointId
,@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
, @Context Request request)
    throws NotFoundException {
        return delegate.endpointsEndpointIdGatewayConfigGet(endpointId,accept, request);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve Endpoint list", notes = "Retrieve available Endpoint", response = EndpointListDTO.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. List of Endpoints. ", response = EndpointListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = EndpointListDTO.class) })
    public Response endpointsGet(@ApiParam(value = "Number of entities that should be retrieved. ") @QueryParam("limit") Integer limit
,@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
, @Context Request request)
    throws NotFoundException {
        return delegate.endpointsGet(limit,accept, request);
    }
}
