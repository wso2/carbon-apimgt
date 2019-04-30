package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ThreatProtectionPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.factories.ThreatProtectionPoliciesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/threat-protection-policies")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/threat-protection-policies", description = "the threat-protection-policies API")
public class ThreatProtectionPoliciesApi  {

   private final ThreatProtectionPoliciesApiService delegate = ThreatProtectionPoliciesApiServiceFactory.getThreatProtectionPoliciesApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get All Threat Protection Policies", notes = "This can be used to get all defined threat protection policies", response = ThreatProtectionPolicyListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. List of policies is returned") })

    public Response threatProtectionPoliciesGet()
    {
    return delegate.threatProtectionPoliciesGet();
    }
    @GET
    @Path("/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a threat protection policy", notes = "", response = ThreatProtectionPolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. Policy is returned"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "No policy found for given policy ID") })

    public Response threatProtectionPoliciesPolicyIdGet(@ApiParam(value = "The UUID of a Policy\n",required=true ) @PathParam("policyId")  String policyId)
    {
    return delegate.threatProtectionPoliciesPolicyIdGet(policyId);
    }
}

