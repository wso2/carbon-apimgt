package org.wso2.carbon.apimgt.rest.api.publisher;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ThreatProtectionApiServiceFactory;

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
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
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
    name = "org.wso2.carbon.apimgt.rest.api.publisher.ThreatProtectionApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1.[\\d]+/threat-protection")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/threat-protection")
@io.swagger.annotations.Api(description = "the threat-protection API")
public class ThreatProtectionApi implements Microservice  {
   private final ThreatProtectionApiService delegate = ThreatProtectionApiServiceFactory.getThreatProtectionApi();

    @POST
    @Path("/add-policy/{apiId}/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Associate a policy to an API", notes = "This can be used to asscociate a policy with an API", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. Successfully associated the policy with API", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Error accociating policy with API", response = void.class) })
    public Response threatProtectionAddPolicyApiIdPolicyIdPost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**. ",required=true) @PathParam("apiId") String apiId
,@ApiParam(value = "The UUID of a Policy ",required=true) @PathParam("ThreatProtectionPolicyId") String threatProtectionPolicyId
 ,@Context Request request)
    throws NotFoundException {
        return delegate.threatProtectionAddPolicyApiIdPolicyIdPost(apiId,threatProtectionPolicyId,request);
    }
    @GET
    @Path("/policies")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get All Threat Protection Policies", notes = "This can be used to get all defined threat protection policies", response = ThreatProtectionPolicyListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. List of policies is returned", response = ThreatProtectionPolicyListDTO.class) })
    public Response threatProtectionPoliciesGet( @Context Request request)
    throws NotFoundException {
        return delegate.threatProtectionPoliciesGet(request);
    }
    @GET
    @Path("/policy/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get policy associated to an API", notes = "This can be used to get a policy associated with an API", response = ThreatProtectionPolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. Associated policy is returned.", response = ThreatProtectionPolicyDTO.class) })
    public Response threatProtectionPolicyApiIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**. ",required=true) @PathParam("apiId") String apiId
 ,@Context Request request)
    throws NotFoundException {
        return delegate.threatProtectionPolicyApiIdGet(apiId,request);
    }
    @POST
    @Path("/remove-policy/{apiId}/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Remove a policy accociated with an API", notes = "This can be used to remove a policy associated with an API", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok. Policy association removed.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Error removing policy association.", response = void.class) })
    public Response threatProtectionRemovePolicyApiIdPolicyIdPost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**. ",required=true) @PathParam("apiId") String apiId
,@ApiParam(value = "The UUID of a Policy ",required=true) @PathParam("ThreatProtectionPolicyId") String threatProtectionPolicyId
 ,@Context Request request)
    throws NotFoundException {
        return delegate.threatProtectionRemovePolicyApiIdPolicyIdPost(apiId,threatProtectionPolicyId,request);
    }
}
