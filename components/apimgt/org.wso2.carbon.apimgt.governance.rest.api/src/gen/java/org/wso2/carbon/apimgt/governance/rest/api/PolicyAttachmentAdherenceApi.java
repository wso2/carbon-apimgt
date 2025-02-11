package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyAttachmentAdherenceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.PolicyAttachmentAdherenceApiServiceImpl;
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
@Path("/policy-attachment-adherence")

@Api(description = "the policy-attachment-adherence API")




public class PolicyAttachmentAdherenceApi  {

  @Context MessageContext securityContext;

PolicyAttachmentAdherenceApiService delegate = new PolicyAttachmentAdherenceApiServiceImpl();


    @GET
    @Path("/{policyAttachmentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve adherence details for a specific policy attachment", notes = "Retrieve adherence details associated with a specific governance policy attachment within the organization using its unique ID.", response = PolicyAttachmentAdherenceDetailsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Policy Attachment Adherence",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. Successful response with adherence details for the specified policy attachment.", response = PolicyAttachmentAdherenceDetailsDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyAttachmentAdherenceByPolicyAttachmentId(@ApiParam(value = "**UUID** of the Policy Attachment. ",required=true) @PathParam("policyAttachmentId") String policyAttachmentId) throws APIMGovernanceException{
        return delegate.getPolicyAttachmentAdherenceByPolicyAttachmentId(policyAttachmentId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve adherence status for all policy attachments", notes = "Retrieve adherence status of all governance policy attachments within the organization.", response = PolicyAttachmentAdherenceListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Policy Attachments Adherence",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. Successful response with adherence details for all policy attachments.", response = PolicyAttachmentAdherenceListDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyAttachmentAdherenceForAllPolicyAttachments( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIMGovernanceException{
        return delegate.getPolicyAttachmentAdherenceForAllPolicyAttachments(limit, offset, securityContext);
    }

    @GET
    @Path("/summary")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves the summary of adherence for all policy attachments", notes = "Retrieves the summary of adherence for all governance policy attachments within the organization.", response = PolicyAttachmentAdherenceSummaryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Policy Attachments Adherence" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with adherence summary.", response = PolicyAttachmentAdherenceSummaryDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyAttachmentAdherenceSummary() throws APIMGovernanceException{
        return delegate.getPolicyAttachmentAdherenceSummary(securityContext);
    }
}
