package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyAdherenceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.PolicyAdherenceApiServiceImpl;
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
@Path("/policy-adherence")

@Api(description = "the policy-adherence API")




public class PolicyAdherenceApi  {

  @Context MessageContext securityContext;

PolicyAdherenceApiService delegate = new PolicyAdherenceApiServiceImpl();


    @GET
    @Path("/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve adherence details for a specific policy", notes = "Retrieve adherence details associated with a specific governance policy within the organization using its unique ID.", response = PolicyAdherenceDetailsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Policy Adherence",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. Successful response with adherence details for the specified policy.", response = PolicyAdherenceDetailsDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyAdherenceByPolicyId(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId) throws APIMGovernanceException{
        return delegate.getPolicyAdherenceByPolicyId(policyId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve adherence status for all policies", notes = "Retrieve adherence status of all governance policies within the organization.", response = PolicyAdherenceListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Policy Adherence",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. Successful response with adherence details for all policies.", response = PolicyAdherenceListDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyAdherenceForAllPolicies( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIMGovernanceException{
        return delegate.getPolicyAdherenceForAllPolicies(limit, offset, securityContext);
    }

    @GET
    @Path("/summary")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves the summary of adherence for all policies", notes = "Retrieves the summary of adherence for all governance policies within the organization.", response = PolicyAdherenceSummaryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Policy Adherence" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with adherence summary.", response = PolicyAdherenceSummaryDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyAdherenceSummary() throws APIMGovernanceException{
        return delegate.getPolicyAdherenceSummary(securityContext);
    }
}
