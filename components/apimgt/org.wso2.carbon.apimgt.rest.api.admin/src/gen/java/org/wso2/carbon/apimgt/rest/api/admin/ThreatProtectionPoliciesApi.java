package org.wso2.carbon.apimgt.rest.api.admin;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.factories.ThreatProtectionPoliciesApiServiceFactory;

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
    name = "org.wso2.carbon.apimgt.rest.api.admin.ThreatProtectionPoliciesApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/admin/v1.[\\d]+/threat-protection-policies")
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
    @io.swagger.annotations.ApiOperation(value = "Get all threat protection policies", notes = "all", response = ThreatProtectionPolicyListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "All Threat Protection Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ThreatProtectionPolicyListDTO.class) })
    public Response threatProtectionPoliciesGet( @Context Request request)
    throws NotFoundException {
        
        return delegate.threatProtectionPoliciesGet(request);
    }
    @OPTIONS
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "add a threat protection policy", notes = "add a threat protection policy", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Add Threat Protection Policy", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response threatProtectionPoliciesPost(@ApiParam(value = "Threat protection json policy request parameter " ,required=true) ThreatProtectionPolicyDTO threatProtectionPolicy
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.threatProtectionPoliciesPost(threatProtectionPolicy,request);
    }
    @OPTIONS
    @DELETE
    @Path("/{threatProtectionPolicyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a threat protection policy", notes = "Delete a threat protection policy", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Delete Threat Protection Policy", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. Policy is deleted", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal server error occurred during the operation", response = void.class) })
    public Response threatProtectionPoliciesThreatProtectionPolicyIdDelete(@ApiParam(value = "The UUID of a Policy ",required=true) @PathParam("threatProtectionPolicyId") String threatProtectionPolicyId
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.threatProtectionPoliciesThreatProtectionPolicyIdDelete(threatProtectionPolicyId,request);
    }
    @OPTIONS
    @GET
    @Path("/{threatProtectionPolicyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get one threat protection policy", notes = "Get one threat protection policy", response = ThreatProtectionPolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Threat Protection Policy", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ThreatProtectionPolicyDTO.class) })
    public Response threatProtectionPoliciesThreatProtectionPolicyIdGet(@ApiParam(value = "The UUID of a Policy ",required=true) @PathParam("threatProtectionPolicyId") String threatProtectionPolicyId
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.threatProtectionPoliciesThreatProtectionPolicyIdGet(threatProtectionPolicyId,request);
    }
    @OPTIONS
    @POST
    @Path("/{threatProtectionPolicyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "post", notes = "desc", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Update Threat Protection Policy", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok", response = void.class) })
    public Response threatProtectionPoliciesThreatProtectionPolicyIdPost(@ApiParam(value = "The UUID of a Policy ",required=true) @PathParam("threatProtectionPolicyId") String threatProtectionPolicyId
,@ApiParam(value = "Threat protection json policy request parameter " ,required=true) ThreatProtectionPolicyDTO threatProtectionPolicy
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.threatProtectionPoliciesThreatProtectionPolicyIdPost(threatProtectionPolicyId,threatProtectionPolicy,request);
    }
}
