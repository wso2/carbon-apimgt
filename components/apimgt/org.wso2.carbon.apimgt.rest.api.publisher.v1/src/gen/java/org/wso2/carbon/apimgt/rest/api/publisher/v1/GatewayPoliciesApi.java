package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingsDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.GatewayPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.GatewayPoliciesApiServiceImpl;
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
@Path("/gateway-policies")

@Api(description = "the gateway-policies API")




public class GatewayPoliciesApi  {

  @Context MessageContext securityContext;

GatewayPoliciesApiService delegate = new GatewayPoliciesApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Engage gateway policies to the request, response, fault flows", notes = "This operation can be used to apply gateway policies to the request, response, fault flows. ", response = APIOperationPoliciesDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Gateway Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy mapping added successfully. ", response = APIOperationPoliciesDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response addGatewayPoliciesToFlows(@ApiParam(value = "Policy details object that needs to be added." ,required=true) GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO) throws APIManagementException{
        return delegate.addGatewayPoliciesToFlows(gatewayPolicyMappingsDTO, securityContext);
    }

    @POST
    @Path("/deploy")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Engage gateway policy mapping to the gateways", notes = "This operation can be used to engage gateway policy mapping to the gateway/s. ", response = GatewayPolicyDeploymentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Gateway Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Gateway policy mapping engaged successfully. ", response = GatewayPolicyDeploymentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response engageGlobalPolicy(@ApiParam(value = "Policy details object that needs to be added." ,required=true) List<GatewayPolicyDeploymentDTO> gatewayPolicyDeploymentDTO) throws APIManagementException{
        return delegate.engageGlobalPolicy(gatewayPolicyDeploymentDTO, securityContext);
    }
}
