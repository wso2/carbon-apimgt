package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ThrottlingPoliciesApiServiceImpl;
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
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ThrottlingPoliciesApi  {

  @Context MessageContext securityContext;

ThrottlingPoliciesApiService delegate = new ThrottlingPoliciesApiServiceImpl();


    @GET
    @Path("/{policyLevel}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all throttling policies for the given type", notes = "This operation can be used to list the available policies for a given policy level. Tier level should be specified as a path parameter and should be one of `subscription` and `api`. `subscription` is for Subscription Level policies and `api` is for Resource Level policies ", response = ThrottlingPolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Throttling Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of policies returned. ", response = ThrottlingPolicyListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response getAllThrottlingPolicies(@ApiParam(value = "List API or Application or Resource type policies. ",required=true, allowableValues="api, subcription") @PathParam("policyLevel") String policyLevel,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAllThrottlingPolicies(policyLevel, limit, offset, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{policyLevel}/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of a policy", notes = "This operation can be used to retrieve details of a single policy by specifying the policy level and policy name. ", response = ThrottlingPolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Throttling Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Tier returned ", response = ThrottlingPolicyDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Tier does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ErrorDTO.class) })
    public Response getThrottlingPolicyByName(@ApiParam(value = "Tier name ",required=true) @PathParam("policyName") String policyName, @ApiParam(value = "List API or Application or Resource type policies. ",required=true, allowableValues="api, subcription") @PathParam("policyLevel") String policyLevel, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getThrottlingPolicyByName(policyName, policyLevel, ifNoneMatch, securityContext);
    }
}
