package org.wso2.carbon.apimgt.rest.api.admin;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.factories.PoliciesApiServiceFactory;

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
    name = "org.wso2.carbon.apimgt.rest.api.admin.PoliciesApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/admin/v1.[\\d]+/policies")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/policies")
@io.swagger.annotations.Api(description = "the policies API")
public class PoliciesApi implements Microservice  {
   private final PoliciesApiService delegate = PoliciesApiServiceFactory.getPoliciesApi();

    @GET
    @Path("/throttling/advanced")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Advanced level throttle policies", notes = "Get all Advanced level throttle policies ", response = TierListDTO.class, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policies returned ", response = TierListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = TierListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = TierListDTO.class) })
    public Response policiesThrottlingAdvancedGet(@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedGet(accept,ifNoneMatch,ifModifiedSince, request);
    }
    @DELETE
    @Path("/throttling/advanced/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an Advanced level throttle policy", notes = "Delete an Advanced level throttle policy ", response = void.class, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response policiesThrottlingAdvancedPolicyIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedPolicyIdDelete(policyId,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/advanced/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve an Advanced Policy", notes = "Retrieve a Advanced Policy providing the policy name. ", response = TierDTO.class, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy returned ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Policy does not exist. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = TierDTO.class) })
    public Response policiesThrottlingAdvancedPolicyIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedPolicyIdGet(policyId,ifNoneMatch,ifModifiedSince, request);
    }
    @PUT
    @Path("/throttling/advanced/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an Advanced level throttle policy", notes = "Update an Advanced level throttle policy ", response = TierDTO.class, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy updated. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = TierDTO.class) })
    public Response policiesThrottlingAdvancedPolicyIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Policy object that needs to be modified " ,required=true) TierDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedPolicyIdPut(policyId,body,contentType,ifMatch,ifUnmodifiedSince, request);
    }
    @POST
    @Path("/throttling/advanced")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an Advanced level throttle policy", notes = "Add an Advanced level throttle policy ", response = TierDTO.class, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = TierDTO.class) })
    public Response policiesThrottlingAdvancedPost(@ApiParam(value = "Advanced level policy object that should to be added " ,required=true) TierDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedPost(body,contentType, request);
    }
    @GET
    @Path("/throttling/application")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Application level throttle policies", notes = "Get all Application level throttle policies ", response = TierListDTO.class, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policies returned ", response = TierListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = TierListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = TierListDTO.class) })
    public Response policiesThrottlingApplicationGet(@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationGet(accept,ifNoneMatch,ifModifiedSince, request);
    }
    @DELETE
    @Path("/throttling/application/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an Application level throttle policy", notes = "Delete an Application level throttle policy ", response = void.class, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response policiesThrottlingApplicationPolicyIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationPolicyIdDelete(policyId,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/application/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve an Application Policy", notes = "Retrieve an Application Policy providing the policy name. ", response = TierDTO.class, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy returned ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Tier does not exist. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = TierDTO.class) })
    public Response policiesThrottlingApplicationPolicyIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationPolicyIdGet(policyId,ifNoneMatch,ifModifiedSince, request);
    }
    @PUT
    @Path("/throttling/application/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an Application level throttle policy", notes = "Update an Application level throttle policy ", response = TierDTO.class, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy updated. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = TierDTO.class) })
    public Response policiesThrottlingApplicationPolicyIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Policy object that needs to be modified " ,required=true) TierDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationPolicyIdPut(policyId,body,contentType,ifMatch,ifUnmodifiedSince, request);
    }
    @POST
    @Path("/throttling/application")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an Application level throttle policy", notes = "Add an Application level throttle policy ", response = TierDTO.class, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = TierDTO.class) })
    public Response policiesThrottlingApplicationPost(@ApiParam(value = "Application level policy object that should to be added " ,required=true) TierDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationPost(body,contentType, request);
    }
    @GET
    @Path("/throttling/custom")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Custom Rules", notes = "Get all Custom Rules ", response = CustomRuleListDTO.class, tags={ "Custom Rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policies returned ", response = CustomRuleListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = CustomRuleListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = CustomRuleListDTO.class) })
    public Response policiesThrottlingCustomGet(@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingCustomGet(accept,ifNoneMatch,ifModifiedSince, request);
    }
    @POST
    @Path("/throttling/custom")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a Custom Rule", notes = "Add a Custom Rule ", response = CustomRuleDTO.class, tags={ "Custom Rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = CustomRuleDTO.class) })
    public Response policiesThrottlingCustomPost(@ApiParam(value = "Custom Rule object that should to be added " ,required=true) CustomRuleDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingCustomPost(body,contentType, request);
    }
    @DELETE
    @Path("/throttling/custom/{ruleId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Custom Rule", notes = "Delete a Custom Rule ", response = void.class, tags={ "Custom Rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response policiesThrottlingCustomRuleIdDelete(@ApiParam(value = "Custom rule UUID ",required=true) @PathParam("ruleId") String ruleId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingCustomRuleIdDelete(ruleId,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/custom/{ruleId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a Custom Rule", notes = "Retrieve a Custom Rule providing the policy name. ", response = CustomRuleDTO.class, tags={ "Custom Rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy returned ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Policy does not exist. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = CustomRuleDTO.class) })
    public Response policiesThrottlingCustomRuleIdGet(@ApiParam(value = "Custom rule UUID ",required=true) @PathParam("ruleId") String ruleId
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingCustomRuleIdGet(ruleId,ifNoneMatch,ifModifiedSince, request);
    }
    @PUT
    @Path("/throttling/custom/{ruleId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Custom Rule", notes = "Update a Custom Rule ", response = CustomRuleDTO.class, tags={ "Custom Rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy updated. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = CustomRuleDTO.class) })
    public Response policiesThrottlingCustomRuleIdPut(@ApiParam(value = "Custom rule UUID ",required=true) @PathParam("ruleId") String ruleId
,@ApiParam(value = "Policy object that needs to be modified " ,required=true) CustomRuleDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingCustomRuleIdPut(ruleId,body,contentType,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Subscription level throttle policies", notes = "Get all Subscription level throttle policies ", response = TierListDTO.class, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policies returned ", response = TierListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = TierListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = TierListDTO.class) })
    public Response policiesThrottlingSubscriptionGet(@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionGet(accept,ifNoneMatch,ifModifiedSince, request);
    }
    @DELETE
    @Path("/throttling/subscription/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Subscription level throttle policy", notes = "Delete a Subscription level throttle policy ", response = void.class, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response policiesThrottlingSubscriptionPolicyIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionPolicyIdDelete(policyId,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/subscription/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a Subscription Policy", notes = "Retrieve a Subscription Policy providing the policy name. ", response = TierDTO.class, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy returned ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Policy does not exist. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = TierDTO.class) })
    public Response policiesThrottlingSubscriptionPolicyIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionPolicyIdGet(policyId,ifNoneMatch,ifModifiedSince, request);
    }
    @PUT
    @Path("/throttling/subscription/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Subscription level throttle policy", notes = "Update a Subscription level throttle policy ", response = TierDTO.class, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy updated. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = TierDTO.class) })
    public Response policiesThrottlingSubscriptionPolicyIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId
,@ApiParam(value = "Policy object that needs to be modified " ,required=true) TierDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionPolicyIdPut(policyId,body,contentType,ifMatch,ifUnmodifiedSince, request);
    }
    @POST
    @Path("/throttling/subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a Subscription level throttle policy", notes = "Add a Subscription level throttle policy ", response = TierDTO.class, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = TierDTO.class) })
    public Response policiesThrottlingSubscriptionPost(@ApiParam(value = "Subscripion level policy object that should to be added " ,required=true) TierDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionPost(body,contentType, request);
    }
}
