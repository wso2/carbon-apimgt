package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ServiceDiscoveriesInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ServiceDiscoverySystemTypeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ServiceDiscoveryApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ServiceDiscoveryApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/service-discovery")

@Api(description = "the service-discovery API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ServiceDiscoveryApi  {

  @Context MessageContext securityContext;

ServiceDiscoveryApiService delegate = new ServiceDiscoveryApiServiceImpl();


    @GET
    @Path("/endpoints")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get list of services discovery system.", notes = "Using this operation, you can retrieve complete list of available services discovery system. ", response = ServiceDiscoverySystemTypeListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Service Discovery Types",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. List of service discovery system is returned. ", response = ServiceDiscoverySystemTypeListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. No services discovery systems are available . ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response serviceDiscoveryEndpointsGet( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.serviceDiscoveryEndpointsGet(limit, offset, securityContext);
    }

    @GET
    @Path("/endpoints/{type}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get list of services in the cluster.", notes = "Using this operation, you can retrieve complete list of available services in the clusters. ", response = ServiceDiscoveriesInfoListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Service Discovery" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. List of services for requested type is returned. ", response = ServiceDiscoveriesInfoListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. No services are available in the cluster in requested namespace or type. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response serviceDiscoveryEndpointsTypeGet(@ApiParam(value = "Service discovery system type ",required=true) @PathParam("type") String type,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.serviceDiscoveryEndpointsTypeGet(type, limit, offset, securityContext);
    }
}
