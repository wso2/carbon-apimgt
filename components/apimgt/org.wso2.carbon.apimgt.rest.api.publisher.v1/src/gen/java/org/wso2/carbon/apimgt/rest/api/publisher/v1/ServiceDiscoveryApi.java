package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ServiceDiscoveriesInfoDTO;
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
    @Path("/endpoints/{type}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get list of services in the cluster.", notes = "Using this operation, you can retrieve complete list of available services in the clusters. ", response = ServiceDiscoveriesInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Service Discovery" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. List of services for requested type is returned. ", response = ServiceDiscoveriesInfoDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. No services are available in the cluster in requested namespace or type. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response serviceDiscoveryEndpointsTypeGet(@ApiParam(value = "Service discovery system type ",required=true) @PathParam("type") String type) throws APIManagementException{
        return delegate.serviceDiscoveryEndpointsTypeGet(type, securityContext);
    }
}
