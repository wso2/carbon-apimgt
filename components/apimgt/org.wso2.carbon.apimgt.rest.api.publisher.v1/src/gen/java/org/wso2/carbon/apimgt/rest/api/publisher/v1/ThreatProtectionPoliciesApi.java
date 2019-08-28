package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ThreatProtectionPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ThreatProtectionPoliciesApiServiceImpl;

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
@Path("/threat-protection-policies")

@Api(description = "the threat-protection-policies API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ThreatProtectionPoliciesApi  {

  @Context MessageContext securityContext;

ThreatProtectionPoliciesApiService delegate = new ThreatProtectionPoliciesApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get All Threat Protection Policies", notes = "This can be used to get all defined threat protection policies", response = ThreatProtectionPolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Threat Protection Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. List of policies is returned", response = ThreatProtectionPolicyListDTO.class) })
    public Response threatProtectionPoliciesGet() {
        return delegate.threatProtectionPoliciesGet(securityContext);
    }

    @GET
    @Path("/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a threat protection policy", notes = "", response = ThreatProtectionPolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Threat Protection Policy" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. Policy is returned", response = ThreatProtectionPolicyDTO.class),
        @ApiResponse(code = 404, message = "No policy found for given policy ID", response = Void.class) })
    public Response threatProtectionPoliciesPolicyIdGet(@ApiParam(value = "The UUID of a Policy ",required=true) @PathParam("policyId") String policyId) {
        return delegate.threatProtectionPoliciesPolicyIdGet(policyId, securityContext);
    }
}
