package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.PoliciesApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.PoliciesApiServiceImpl;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

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
@Path("/policies")

@Api(description = "the policies API")




public class PoliciesApi  {

  @Context MessageContext securityContext;

PoliciesApiService delegate = new PoliciesApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Creates a new governance policy.", notes = "Creates a new governance policy for the user's organization.", response = APIMGovernancePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_manage", description = "Manage governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Governance policy created successfully.", response = APIMGovernancePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response createGovernancePolicy(@ApiParam(value = "JSON object containing the details of the new governance policy." ,required=true) APIMGovernancePolicyDTO apIMGovernancePolicyDTO) throws APIMGovernanceException{
        return delegate.createGovernancePolicy(apIMGovernancePolicyDTO, securityContext);
    }

    @DELETE
    @Path("/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a specific governance policy", notes = "Deletes an existing governance policy identified by the policyId.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_manage", description = "Manage governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "OK. Governance policy deleted successfully.", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response deleteGovernancePolicy(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId) throws APIMGovernanceException{
        return delegate.deleteGovernancePolicy(policyId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves a list of all governance policies.", notes = "Retrieves a list of governance policies for the user's organization.", response = APIMGovernancePolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_read", description = "Read governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with a list of governance policies.", response = APIMGovernancePolicyListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getGovernancePolicies( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "You can search for governance policies using following format.    - \"query=name:{NAME}\" searches policies by name.   - \"query=state:{STATE} \" searches policies by state.  You can also use multiple attributes to search for policies.   - \"query=name:{NAME} state:{STATE}\" searches policies by name, state, and label.  Remember to use URL encoding if your client doesn't support it (e.g., curl). ")  @QueryParam("query") String query) throws APIMGovernanceException{
        return delegate.getGovernancePolicies(limit, offset, query, securityContext);
    }

    @GET
    @Path("/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a specific governance policy", notes = "Retrieves details of a specific governance policy identified by the policyId.", response = APIMGovernancePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_read", description = "Read governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Governance policy details retrieved successfully.", response = APIMGovernancePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getGovernancePolicyById(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId) throws APIMGovernanceException{
        return delegate.getGovernancePolicyById(policyId, securityContext);
    }

    @PUT
    @Path("/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a specific governance policy", notes = "Updates the details of an existing governance policy identified by the policyId.", response = APIMGovernancePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_manage", description = "Manage governance policies")
        })
    }, tags={ "Governance Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Governance policy updated successfully.", response = APIMGovernancePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response updateGovernancePolicyById(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId, @ApiParam(value = "JSON object containing the updated governance policy details." ,required=true) APIMGovernancePolicyDTO apIMGovernancePolicyDTO) throws APIMGovernanceException{
        return delegate.updateGovernancePolicyById(policyId, apIMGovernancePolicyDTO, securityContext);
    }
}
