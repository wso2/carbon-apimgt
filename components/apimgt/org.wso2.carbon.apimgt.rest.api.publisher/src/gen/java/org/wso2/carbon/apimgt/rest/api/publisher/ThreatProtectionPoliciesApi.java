package org.wso2.carbon.apimgt.rest.api.publisher;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ThreatProtectionPoliciesApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.ThreatProtectionPoliciesApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1.[\\d]+/threat-protection-policies")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/threat-protection-policies")
@io.swagger.annotations.Api(description = "the threat-protection-policies API")
public class ThreatProtectionPoliciesApi implements Microservice  {
   private final ThreatProtectionPoliciesApiService delegate = ThreatProtectionPoliciesApiServiceFactory.getThreatProtectionPoliciesApi();

    @OPTIONS
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get All Threat Protection Policies", notes = "This can be used to get all defined threat protection policies", response = ThreatProtectionPolicyListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Threat Protection Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. List of policies is returned", response = ThreatProtectionPolicyListDTO.class) })
    public Response threatProtectionPoliciesGet( @Context Request request)
    throws NotFoundException {
        
        return delegate.threatProtectionPoliciesGet(request);
    }
    @OPTIONS
    @GET
    @Path("/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a threat protection policy", notes = "", response = ThreatProtectionPolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Threat Protection Policy", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. Policy is returned", response = ThreatProtectionPolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "No policy found for given policy ID", response = ThreatProtectionPolicyDTO.class) })
    public Response threatProtectionPoliciesPolicyIdGet(@ApiParam(value = "The UUID of a Policy ",required=true) @PathParam("policyId") String policyId
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.threatProtectionPoliciesPolicyIdGet(policyId,request);
    }
}
