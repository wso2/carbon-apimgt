package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyAttachmentDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyAttachmentListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyAttachmentsApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.PolicyAttachmentsApiServiceImpl;
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
@Path("/policy-attachments")

@Api(description = "the policy-attachments API")




public class PolicyAttachmentsApi  {

  @Context MessageContext securityContext;

PolicyAttachmentsApiService delegate = new PolicyAttachmentsApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Creates a new governance policy attachment.", notes = "Creates a new governance policy attachment for the user's organization.", response = APIMGovernancePolicyAttachmentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_attachment_manage", description = "Manage governance policy attachments")
        })
    }, tags={ "Governance Policy Attachments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Governance policy attachment created successfully.", response = APIMGovernancePolicyAttachmentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response createGovernancePolicyAttachment(@ApiParam(value = "JSON object containing the details of the new governance policy attachment." ,required=true) APIMGovernancePolicyAttachmentDTO apIMGovernancePolicyAttachmentDTO) throws APIMGovernanceException{
        return delegate.createGovernancePolicyAttachment(apIMGovernancePolicyAttachmentDTO, securityContext);
    }

    @DELETE
    @Path("/{policyAttachmentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a specific governance policy attachment", notes = "Deletes an existing governance policy attachment identified by the policyAttachmentId.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_attachment_manage", description = "Manage governance policy attachments")
        })
    }, tags={ "Governance Policy Attachments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "OK. Governance policy attachment deleted successfully.", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response deleteGovernancePolicyAttachment(@ApiParam(value = "**UUID** of the Policy Attachment. ",required=true) @PathParam("policyAttachmentId") String policyAttachmentId) throws APIMGovernanceException{
        return delegate.deleteGovernancePolicyAttachment(policyAttachmentId, securityContext);
    }

    @GET
    @Path("/{policyAttachmentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a specific governance policy attachment", notes = "Retrieves details of a specific governance policy attachment identified by the policyAttachmentId.", response = APIMGovernancePolicyAttachmentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_attachment_read", description = "Read governance policy attachments")
        })
    }, tags={ "Governance Policy Attachments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Governance policy attachment details retrieved successfully.", response = APIMGovernancePolicyAttachmentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getGovernancePolicyAttachmentById(@ApiParam(value = "**UUID** of the Policy Attachment. ",required=true) @PathParam("policyAttachmentId") String policyAttachmentId) throws APIMGovernanceException{
        return delegate.getGovernancePolicyAttachmentById(policyAttachmentId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves a list of all governance policy-attachments.", notes = "Retrieves a list of governance policy-attachments for the user's organization.", response = APIMGovernancePolicyAttachmentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_attachment_read", description = "Read governance policy attachments")
        })
    }, tags={ "Governance Policy Attachments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with a list of governance policy attachments.", response = APIMGovernancePolicyAttachmentListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getGovernancePolicyAttachments( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "You can search for governance policy attachments using following format.    - \"query=name:{NAME}\" searches policy attachments by name.   - \"query=state:{STATE} \" searches policy attachments by state.  You can also use multiple attributes to search for policy attachments.   - \"query=name:{NAME} state:{STATE}\" searches policy attachments by name, state, and label.  Remember to use URL encoding if your client doesn't support it (e.g., curl). ")  @QueryParam("query") String query) throws APIMGovernanceException{
        return delegate.getGovernancePolicyAttachments(limit, offset, query, securityContext);
    }

    @PUT
    @Path("/{policyAttachmentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a specific governance policy attachment", notes = "Updates the details of an existing governance policy attachment identified by the policyAttachmentId.", response = APIMGovernancePolicyAttachmentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_attachment_manage", description = "Manage governance policy attachments")
        })
    }, tags={ "Governance Policy Attachments" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Governance policy attachment updated successfully.", response = APIMGovernancePolicyAttachmentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response updateGovernancePolicyAttachmentById(@ApiParam(value = "**UUID** of the Policy Attachment. ",required=true) @PathParam("policyAttachmentId") String policyAttachmentId, @ApiParam(value = "JSON object containing the updated governance policy attachment details." ,required=true) APIMGovernancePolicyAttachmentDTO apIMGovernancePolicyAttachmentDTO) throws APIMGovernanceException{
        return delegate.updateGovernancePolicyAttachmentById(policyAttachmentId, apIMGovernancePolicyAttachmentDTO, securityContext);
    }
}
