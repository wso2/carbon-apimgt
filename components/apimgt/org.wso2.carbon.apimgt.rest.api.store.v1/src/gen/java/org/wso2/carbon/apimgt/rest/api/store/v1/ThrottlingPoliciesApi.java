package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ThrottlingPoliciesApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

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
@Path("/throttling-policies")

@Api(description = "the throttling-policies API")




public class ThrottlingPoliciesApi  {

  @Context MessageContext securityContext;

ThrottlingPoliciesApiService delegate = new ThrottlingPoliciesApiServiceImpl();


    @GET
    @Path("/{policyLevel}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get All Available Throttling Policies", notes = "This operation can be used to get all available application or subscription level throttling policies ", response = ThrottlingPolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Throttling Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of throttling policies returned. ", response = ThrottlingPolicyListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesPolicyLevelGet(@ApiParam(value = "List Application or Subscription type thro. ",required=true, allowableValues="application, subscription") @PathParam("policyLevel") String policyLevel,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.throttlingPoliciesPolicyLevelGet(policyLevel, limit, offset, ifNoneMatch, xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{policyLevel}/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of a Throttling Policy ", notes = "This operation can be used to retrieve details of a single throttling policy by specifying the policy level and policy name.  `X-WSO2-Tenant` header can be used to retrive throttling policy that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = ThrottlingPolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Throttling Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Throttling Policy returned ", response = ThrottlingPolicyDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesPolicyLevelPolicyIdGet(@ApiParam(value = "The name of the policy ",required=true) @PathParam("policyId") String policyId, @ApiParam(value = "List Application or Subscription type thro. ",required=true, allowableValues="application, subscription") @PathParam("policyLevel") String policyLevel,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.throttlingPoliciesPolicyLevelPolicyIdGet(policyId, policyLevel, xWSO2Tenant, ifNoneMatch, securityContext);
    }
}
