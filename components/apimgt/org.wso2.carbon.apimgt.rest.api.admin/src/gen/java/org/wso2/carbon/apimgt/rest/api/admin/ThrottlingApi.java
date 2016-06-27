package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.ThrottlingApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.GlobalThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.GlobalThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/throttling")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/throttling", description = "the throttling API")
public class ThrottlingApi  {

   private final ThrottlingApiService delegate = ThrottlingApiServiceFactory.getThrottlingApi();

    @GET
    @Path("/blocking-conditions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all blocking condtions", notes = "Get all blocking condtions\n", response = BlockingConditionListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nBlocking conditions returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingBlockingConditionsGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingBlockingConditionsGet(limit,offset,accept,ifNoneMatch,ifModifiedSince);
    }
    @POST
    @Path("/blocking-conditions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a Blocking condition", notes = "Add a Blocking condition\n", response = BlockingConditionDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response throttlingBlockingConditionsPost(@ApiParam(value = "Blocking condition object that should to be added\n" ,required=true ) BlockingConditionDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.throttlingBlockingConditionsPost(body,contentType);
    }
    @GET
    @Path("/blocking-conditions/{conditionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a Blocking Condition", notes = "Retrieve a Blocking Condition providing the condition Id\n", response = BlockingConditionDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingBlockingConditionsConditionIdGet(@ApiParam(value = "Blocking condition identifier \n",required=true ) @PathParam("conditionId") String conditionId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingBlockingConditionsConditionIdGet(conditionId,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/blocking-conditions/{conditionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Blocking condition", notes = "Update a Blocking condition\n", response = BlockingConditionDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicy updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingBlockingConditionsConditionIdPut(@ApiParam(value = "Blocking condition identifier \n",required=true ) @PathParam("conditionId") String conditionId,
    @ApiParam(value = "Blocking condition object that needs to be modified\n" ,required=true ) BlockingConditionDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingBlockingConditionsConditionIdPut(conditionId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/blocking-conditions/{conditionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Blocking condition", notes = "Delete a Blocking condition\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingBlockingConditionsConditionIdDelete(@ApiParam(value = "Blocking condition identifier \n",required=true ) @PathParam("conditionId") String conditionId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingBlockingConditionsConditionIdDelete(conditionId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/policies/advanced-policies")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Advanced level throttle policies", notes = "Get all Advanced level throttle policies\n", response = AdvancedThrottlePolicyListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicies returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingPoliciesAdvancedPoliciesGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingPoliciesAdvancedPoliciesGet(limit,offset,accept,ifNoneMatch,ifModifiedSince);
    }
    @POST
    @Path("/policies/advanced-policies")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an Advanced level throttle policy", notes = "Add an Advanced level throttle policy\n", response = AdvancedThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response throttlingPoliciesAdvancedPoliciesPost(@ApiParam(value = "Advanced level policy object that should to be added\n" ,required=true ) AdvancedThrottlePolicyDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.throttlingPoliciesAdvancedPoliciesPost(body,contentType);
    }
    @GET
    @Path("/policies/advanced-policies/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve an Advanced Policy", notes = "Retrieve a Advanced Policy providing the policy name.\n", response = AdvancedThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingPoliciesAdvancedPoliciesPolicyNameGet(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingPoliciesAdvancedPoliciesPolicyNameGet(policyName,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/policies/advanced-policies/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an Advanced level throttle policy", notes = "Update an Advanced level throttle policy\n", response = AdvancedThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicy updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingPoliciesAdvancedPoliciesPolicyNamePut(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Policy object that needs to be modified\n" ,required=true ) AdvancedThrottlePolicyDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingPoliciesAdvancedPoliciesPolicyNamePut(policyName,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/policies/advanced-policies/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an Advanced level throttle policy", notes = "Delete an Advanced level throttle policy\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingPoliciesAdvancedPoliciesPolicyNameDelete(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingPoliciesAdvancedPoliciesPolicyNameDelete(policyName,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/policies/application")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Application level throttle policies", notes = "Get all Application level throttle policies\n", response = ApplicationThrottlePolicyListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicies returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingPoliciesApplicationGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingPoliciesApplicationGet(limit,offset,accept,ifNoneMatch,ifModifiedSince);
    }
    @POST
    @Path("/policies/application")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an Application level throttle policy", notes = "Add an Application level throttle policy\n", response = ApplicationThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response throttlingPoliciesApplicationPost(@ApiParam(value = "Application level policy object that should to be added\n" ,required=true ) ApplicationThrottlePolicyDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.throttlingPoliciesApplicationPost(body,contentType);
    }
    @GET
    @Path("/policies/application/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve an Application Policy", notes = "Retrieve an Application Policy providing the policy name.\n", response = ApplicationThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingPoliciesApplicationPolicyNameGet(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingPoliciesApplicationPolicyNameGet(policyName,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/policies/application/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an Application level throttle policy", notes = "Update an Application level throttle policy\n", response = ApplicationThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicy updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingPoliciesApplicationPolicyNamePut(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Policy object that needs to be modified\n" ,required=true ) ApplicationThrottlePolicyDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingPoliciesApplicationPolicyNamePut(policyName,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/policies/application/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an Application level throttle policy", notes = "Delete an Application level throttle policy\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingPoliciesApplicationPolicyNameDelete(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingPoliciesApplicationPolicyNameDelete(policyName,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/policies/global")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Global level throttle policies", notes = "Get all Global level throttle policies\n", response = GlobalThrottlePolicyListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicies returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingPoliciesGlobalGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingPoliciesGlobalGet(limit,offset,accept,ifNoneMatch,ifModifiedSince);
    }
    @POST
    @Path("/policies/global")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a Global level throttle policy", notes = "Add a Global level throttle policy\n", response = GlobalThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response throttlingPoliciesGlobalPost(@ApiParam(value = "Global level policy object that should to be added\n" ,required=true ) GlobalThrottlePolicyDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.throttlingPoliciesGlobalPost(body,contentType);
    }
    @GET
    @Path("/policies/global/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a Global Policy", notes = "Retrieve a Global Policy providing the policy name.\n", response = GlobalThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingPoliciesGlobalPolicyNameGet(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingPoliciesGlobalPolicyNameGet(policyName,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/policies/global/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Global level throttle policy", notes = "Update a Global level throttle policy\n", response = GlobalThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicy updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingPoliciesGlobalPolicyNamePut(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Policy object that needs to be modified\n" ,required=true ) GlobalThrottlePolicyDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingPoliciesGlobalPolicyNamePut(policyName,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/policies/global/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Global level throttle policy", notes = "Delete a Global level throttle policy\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingPoliciesGlobalPolicyNameDelete(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingPoliciesGlobalPolicyNameDelete(policyName,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/policies/subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Subscription level throttle policies", notes = "Get all Subscription level throttle policies\n", response = SubscriptionThrottlePolicyListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicies returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingPoliciesSubscriptionGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingPoliciesSubscriptionGet(limit,offset,accept,ifNoneMatch,ifModifiedSince);
    }
    @POST
    @Path("/policies/subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a Subscription level throttle policy", notes = "Add a Subscription level throttle policy\n", response = SubscriptionThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response throttlingPoliciesSubscriptionPost(@ApiParam(value = "Subscripion level policy object that should to be added\n" ,required=true ) SubscriptionThrottlePolicyDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.throttlingPoliciesSubscriptionPost(body,contentType);
    }
    @GET
    @Path("/policies/subscription/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a Subscription Policy", notes = "Retrieve a Subscription Policy providing the policy name.\n", response = SubscriptionThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTier returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Tier does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response throttlingPoliciesSubscriptionPolicyNameGet(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.throttlingPoliciesSubscriptionPolicyNameGet(policyName,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/policies/subscription/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Subscription level throttle policy", notes = "Update a Subscription level throttle policy\n", response = SubscriptionThrottlePolicyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nPolicy updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingPoliciesSubscriptionPolicyNamePut(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Policy object that needs to be modified\n" ,required=true ) SubscriptionThrottlePolicyDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingPoliciesSubscriptionPolicyNamePut(policyName,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/policies/subscription/{policyName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Subscription level throttle policy", notes = "Delete a Subscription level throttle policy\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response throttlingPoliciesSubscriptionPolicyNameDelete(@ApiParam(value = "Thorttle policy name\n",required=true ) @PathParam("policyName") String policyName,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.throttlingPoliciesSubscriptionPolicyNameDelete(policyName,ifMatch,ifUnmodifiedSince);
    }
}

