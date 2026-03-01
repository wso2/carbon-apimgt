package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CreatePlatformGatewayRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayWithTokenDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.GatewaysApiServiceImpl;
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
@Path("/gateways")

@Api(description = "the gateways API")




public class GatewaysApi  {

  @Context MessageContext securityContext;

GatewaysApiService delegate = new GatewaysApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all platform gateways", notes = "Get all registered platform gateways for the organization. ", response = PlatformGatewayListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Platform Gateways",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of platform gateways returned (without registration tokens). ", response = PlatformGatewayListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response gatewaysGet() throws APIManagementException{
        return delegate.gatewaysGet(securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Register a platform gateway", notes = "Register a new platform gateway. A registration token is generated and returned once in the response; store it (e.g. as GATEWAY_CONTROL_PLANE_TOKEN in Docker Compose) for the gateway to connect to the control plane WebSocket. The token is stored hashed and cannot be retrieved later. ", response = PlatformGatewayWithTokenDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Platform Gateways" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Gateway and registration token (returned once) in the response body. ", response = PlatformGatewayWithTokenDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class) })
    public Response gatewaysPost(@ApiParam(value = "" ,required=true) CreatePlatformGatewayRequestDTO createPlatformGatewayRequestDTO) throws APIManagementException{
        return delegate.gatewaysPost(createPlatformGatewayRequestDTO, securityContext);
    }
}
