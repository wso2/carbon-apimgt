package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.factories.ThrottlingPoliciesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/throttling-policies")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/throttling-policies", description = "the throttling-policies API")
public class ThrottlingPoliciesApi  {

   private final ThrottlingPoliciesApiService delegate = ThrottlingPoliciesApiServiceFactory.getThrottlingPoliciesApi();

    @GET
    @Path("/{policyLevel}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all throttling policies for the given type", notes = "This operation can be used to list the available policies for a given policy level. Tier level should be specified as a path parameter and should be one of `subscription` and `api`.\n", response = ThrottlingPolicyListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of policies returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response getAllThrottlingPolicies(@ApiParam(value = "List API or Application or Resource type policies.\n",required=true, allowableValues="{values=[api, application, resource]}" ) @PathParam("policyLevel")  String policyLevel,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.getAllThrottlingPolicies(policyLevel,limit,offset,ifNoneMatch);
    }
    @GET
    @Path("/{policyLevel}/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of a policy", notes = "This operation can be used to retrieve details of a single policy by specifying the policy level and policy name.\n", response = ThrottlingPolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response getThrottlingPolicyByName(@ApiParam(value = "Tier name\n",required=true ) @PathParam("policyName")  String policyName,
    @ApiParam(value = "List API or Application or Resource type policies.\n",required=true, allowableValues="{values=[api, application, resource]}" ) @PathParam("policyLevel")  String policyLevel,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.getThrottlingPolicyByName(policyName,policyLevel,ifNoneMatch);
    }
}

