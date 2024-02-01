package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingInfoDTO;
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
    @ApiOperation(value = "Engage gateway policies to the request, response, fault flows", notes = "This operation can be used to apply gateway policies to the request, response, fault flows. ", response = GatewayPolicyMappingInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gateway_policy_manage", description = "Add, Update and Delete gateway policies")
        })
    }, tags={ "Gateway Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Policy mapping created successfully. ", response = GatewayPolicyMappingInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response addGatewayPoliciesToFlows(@ApiParam(value = "Policy details object that needs to be added." ,required=true) GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO) throws APIManagementException{
        return delegate.addGatewayPoliciesToFlows(gatewayPolicyMappingsDTO, securityContext);
    }

    @DELETE
    @Path("/{gatewayPolicyMappingId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a gateway policy mapping", notes = "This operation can be used to delete an existing gateway policy mapping by providing the Id of the policy mapping. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gateway_policy_manage", description = "Add, Update and Delete gateway policies")
        })
    }, tags={ "Gateway Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteGatewayPolicyByPolicyId(@ApiParam(value = "Gateway policy mapping Id ",required=true) @PathParam("gatewayPolicyMappingId") String gatewayPolicyMappingId) throws APIManagementException{
        return delegate.deleteGatewayPolicyByPolicyId(gatewayPolicyMappingId, securityContext);
    }

    @POST
    @Path("/{gatewayPolicyMappingId}/deploy")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Engage gateway policy mapping to the gateways", notes = "This operation can be used to engage gateway policy mapping to the gateway/s. ", response = GatewayPolicyDeploymentDTO.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gateway_policy_manage", description = "Add, Update and Delete gateway policies")
        })
    }, tags={ "Gateway Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Gateway policy mapping engaged successfully. ", response = GatewayPolicyDeploymentDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response engageGlobalPolicy(@ApiParam(value = "Gateway policy mapping Id ",required=true) @PathParam("gatewayPolicyMappingId") String gatewayPolicyMappingId, @ApiParam(value = "Policy details object that needs to be added." ,required=true) List<GatewayPolicyDeploymentDTO> gatewayPolicyDeploymentDTO) throws APIManagementException{
        return delegate.engageGlobalPolicy(gatewayPolicyMappingId, gatewayPolicyDeploymentDTO, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all gateway policies mapping information ", notes = "This operation provides you a list of all gateway policies mapping information. ", response = GatewayPolicyMappingDataListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gateway_policy_view", description = "View gateway policies"),
            @AuthorizationScope(scope = "apim:gateway_policy_manage", description = "Add, Update and Delete gateway policies")
        })
    }, tags={ "Gateway Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of gateway policies is returned. ", response = GatewayPolicyMappingDataListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAllGatewayPolicies( @ApiParam(value = "Maximum size of policy array to return. ")  @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"gatewayLabel:\"** modifier.  Eg. The entry \"gatewayLabel:gateway1\" will result in a match with a Gateway Policy Mapping only if the policy mapping is deployed on \"gateway1\".  If query attribute is provided, this returns the Gateway policy Mapping available under the given limit.  Please note that you need to use encoded URL (URL encoding) if you are using a client which does not support URL encoding (such as curl) ")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.getAllGatewayPolicies(limit, offset, query, securityContext);
    }

    @GET
    @Path("/{gatewayPolicyMappingId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve information of a selected gateway policy mapping", notes = "This operation can be used to retrieve information of a selected gateway policy mapping. ", response = GatewayPolicyMappingsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gateway_policy_view", description = "View gateway policies"),
            @AuthorizationScope(scope = "apim:gateway_policy_manage", description = "Add, Update and Delete gateway policies")
        })
    }, tags={ "Gateway Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Gateway policy mapping information returned. ", response = GatewayPolicyMappingsDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getGatewayPolicyMappingContentByPolicyMappingId(@ApiParam(value = "Gateway policy mapping Id ",required=true) @PathParam("gatewayPolicyMappingId") String gatewayPolicyMappingId) throws APIManagementException{
        return delegate.getGatewayPolicyMappingContentByPolicyMappingId(gatewayPolicyMappingId, securityContext);
    }

    @PUT
    @Path("/{gatewayPolicyMappingId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update gateway policies added to the request, response, fault flows", notes = "This operation can be used to update already added gateway policies to the request, response, fault flows. ", response = GatewayPolicyMappingsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gateway_policy_manage", description = "Add, Update and Delete gateway policies")
        })
    }, tags={ "Gateway Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy mapping updated successfully. ", response = GatewayPolicyMappingsDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response updateGatewayPoliciesToFlows(@ApiParam(value = "Gateway policy mapping Id ",required=true) @PathParam("gatewayPolicyMappingId") String gatewayPolicyMappingId, @ApiParam(value = "Policy details object that needs to be updated." ,required=true) GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO) throws APIManagementException{
        return delegate.updateGatewayPoliciesToFlows(gatewayPolicyMappingId, gatewayPolicyMappingsDTO, securityContext);
    }
}
