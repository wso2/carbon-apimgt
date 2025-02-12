package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyListDTO;
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
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new policy.", notes = "Creates a new policy in the user's organization.", response = PolicyInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_manage", description = "Manage governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Policy created successfully.", response = PolicyInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response createPolicy(@Multipart(value = "name")  String name,  @Multipart(value = "policyContent") InputStream policyContentInputStream, @Multipart(value = "policyContent" ) Attachment policyContentDetail, @Multipart(value = "policyType")  String policyType, @Multipart(value = "artifactType")  String artifactType, @Multipart(value = "description", required = false)  String description, @Multipart(value = "policyCategory", required = false)  String policyCategory, @Multipart(value = "documentationLink", required = false)  String documentationLink, @Multipart(value = "provider", required = false)  String provider) throws APIMGovernanceException{
        return delegate.createPolicy(name, policyContentInputStream, policyContentDetail, policyType, artifactType, description, policyCategory, documentationLink, provider, securityContext);
    }

    @DELETE
    @Path("/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Deletes a specific policy.", notes = "Deletes an existing policy identified by the policyId.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_manage", description = "Manage governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "OK. Policy deleted successfully.", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response deletePolicy(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId) throws APIMGovernanceException{
        return delegate.deletePolicy(policyId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves a list of policies.", notes = "Returns a list of all policies associated with the requested organization.", response = PolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_read", description = "Read governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with a list of policies.", response = PolicyListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicies( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "You can search for policies using the following format:    - \"query=name:{NAME}\" searches policies by name.   - \"query=artifactType:{ARTIFACT_TYPE}\" searches policies by artifact type.   - \"query=policyType:{POLICY_TYPE}\" searches policies by policy type.  You can combine multiple attributes to search for policies:   - \"query=name:{NAME} artifactType:{ARTIFACT_TYPE} policyType:{POLICY_TYPE}\" searches policies by name, artifact type, and policy type.  Remember to use URL encoding if your client does not support it (e.g., curl). ")  @QueryParam("query") String query) throws APIMGovernanceException{
        return delegate.getPolicies(limit, offset, query, securityContext);
    }

    @GET
    @Path("/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves details of a specific policy.", notes = "Retrieves details of the policy identified by the policyId.", response = PolicyInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_read", description = "Read governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy details retrieved successfully.", response = PolicyInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyById(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId) throws APIMGovernanceException{
        return delegate.getPolicyById(policyId, securityContext);
    }

    @GET
    @Path("/{policyId}/content")
    
    @Produces({ "application/x-yaml", "application/json" })
    @ApiOperation(value = "Retrieves the content of a specific policy.", notes = "Retrieves the content of the policy identified by the policyId.", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_read", description = "Read governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy content retrieved successfully.", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyContent(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId) throws APIMGovernanceException{
        return delegate.getPolicyContent(policyId, securityContext);
    }

    @GET
    @Path("/{policyId}/usage")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves the policy attachment usage of a specific policy.", notes = "Retrieves the list of policy attachments using the policy identified by the policyId.", response = String.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_read", description = "Read governance policies")
        })
    }, tags={ "Governance Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy usage retrieved successfully.", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyUsage(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId) throws APIMGovernanceException{
        return delegate.getPolicyUsage(policyId, securityContext);
    }

    @PUT
    @Path("/{policyId}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a specific policy.", notes = "Updates the details of the policy identified by the `policyId`.", response = PolicyInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_policy_manage", description = "Manage governance policies")
        })
    }, tags={ "Governance Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy updated successfully.", response = PolicyInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response updatePolicyById(@ApiParam(value = "**UUID** of the Policy. ",required=true) @PathParam("policyId") String policyId, @Multipart(value = "name")  String name,  @Multipart(value = "policyContent") InputStream policyContentInputStream, @Multipart(value = "policyContent" ) Attachment policyContentDetail, @Multipart(value = "policyType")  String policyType, @Multipart(value = "artifactType")  String artifactType, @Multipart(value = "description", required = false)  String description, @Multipart(value = "policyCategory", required = false)  String policyCategory, @Multipart(value = "documentationLink", required = false)  String documentationLink, @Multipart(value = "provider", required = false)  String provider) throws APIMGovernanceException{
        return delegate.updatePolicyById(policyId, name, policyContentInputStream, policyContentDetail, policyType, artifactType, description, policyCategory, documentationLink, provider, securityContext);
    }
}
