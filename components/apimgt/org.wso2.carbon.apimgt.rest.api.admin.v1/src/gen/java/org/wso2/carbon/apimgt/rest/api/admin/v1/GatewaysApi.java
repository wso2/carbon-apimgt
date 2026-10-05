package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CreatePlatformGatewayRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayResponseWithTokenDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UpdatePlatformGatewayRequestDTO;
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


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Register a platform gateway", notes = "Register a new platform gateway. A registration token is generated and returned once in the response; store it (e.g. as GATEWAY_CONTROL_PLANE_TOKEN in Docker Compose) for the gateway to connect to the control plane WebSocket. The token is stored hashed and cannot be retrieved later. ", response = GatewayResponseWithTokenDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Platform Gateways",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Gateway and registration token (returned once) in the response body. ", response = GatewayResponseWithTokenDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class) })
    public Response createPlatformGateway(@ApiParam(value = "" ,required=true) CreatePlatformGatewayRequestDTO createPlatformGatewayRequestDTO) throws APIManagementException{
        return delegate.createPlatformGateway(createPlatformGatewayRequestDTO, securityContext);
    }

    @DELETE
    @Path("/{gatewayId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a platform gateway", notes = "Delete a platform gateway and all its references (tokens, instance mappings, revision deployment records, gateway environment, permissions). Fails with 409 if any API revisions are currently deployed to this gateway; undeploy all APIs from the gateway first. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Platform Gateways",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Gateway and all references removed.", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Cannot delete gateway while API revisions are deployed to it.", response = ErrorDTO.class) })
    public Response deletePlatformGateway(@ApiParam(value = "Gateway UUID",required=true) @PathParam("gatewayId") String gatewayId) throws APIManagementException{
        return delegate.deletePlatformGateway(gatewayId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all platform gateways", notes = "Get all registered platform gateways for the organization. ", response = GatewayListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Platform Gateways",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of platform gateways returned (without registration tokens). ", response = GatewayListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getPlatformGateways() throws APIManagementException{
        return delegate.getPlatformGateways(securityContext);
    }

    @POST
    @Path("/{gatewayId}/regenerate-token")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Regenerate registration token for a platform gateway", notes = "Regenerate the registration token for an existing platform gateway. The old token is revoked and a new one is generated. Store the new token (e.g. as GATEWAY_CONTROL_PLANE_TOKEN in Docker Compose) for the gateway to reconnect to the control plane WebSocket. The token is returned only once. ", response = GatewayResponseWithTokenDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Platform Gateways",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Gateway and new registration token (returned once) in the response body. ", response = GatewayResponseWithTokenDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response regeneratePlatformGatewayToken(@ApiParam(value = "Gateway UUID",required=true) @PathParam("gatewayId") String gatewayId) throws APIManagementException{
        return delegate.regeneratePlatformGatewayToken(gatewayId, securityContext);
    }

    @PUT
    @Path("/{gatewayId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a platform gateway", notes = "Update platform gateway metadata. Request body must include all updatable fields (displayName, description, properties, permissions). Name and vhost cannot be changed. UI should send the full resource representation to align with PUT semantics. ", response = PlatformGatewayResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Platform Gateways" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Updated platform gateway in the response body.", response = PlatformGatewayResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response updatePlatformGateway(@ApiParam(value = "Gateway UUID",required=true) @PathParam("gatewayId") String gatewayId, @ApiParam(value = "" ,required=true) UpdatePlatformGatewayRequestDTO updatePlatformGatewayRequestDTO) throws APIManagementException{
        return delegate.updatePlatformGateway(gatewayId, updatePlatformGatewayRequestDTO, securityContext);
    }
}
