package org.wso2.carbon.apimgt.rest.api.admin;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;
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
    @io.swagger.annotations.ApiOperation(value = "Get all Advanced level throttle policies", notes = "Get all Advanced level throttle policies ", response = AdvancedThrottlePolicyListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policies returned ", response = AdvancedThrottlePolicyListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = AdvancedThrottlePolicyListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = AdvancedThrottlePolicyListDTO.class) })
    public Response policiesThrottlingAdvancedGet(@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedGet(ifNoneMatch,ifModifiedSince, request);
    }
    @DELETE
    @Path("/throttling/advanced/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an Advanced level throttle policy", notes = "Delete an Advanced level throttle policy ", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response policiesThrottlingAdvancedIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedIdDelete(id,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/advanced/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve an Advanced Policy", notes = "Retrieve a Advanced Policy providing the policy name. ", response = AdvancedThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy returned ", response = AdvancedThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = AdvancedThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Policy does not exist. ", response = AdvancedThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = AdvancedThrottlePolicyDTO.class) })
    public Response policiesThrottlingAdvancedIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedIdGet(id,ifNoneMatch,ifModifiedSince, request);
    }
    @PUT
    @Path("/throttling/advanced/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an Advanced level throttle policy", notes = "Update an Advanced level throttle policy ", response = AdvancedThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy updated. ", response = AdvancedThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = AdvancedThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = AdvancedThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = AdvancedThrottlePolicyDTO.class) })
    public Response policiesThrottlingAdvancedIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Policy object that needs to be modified " ,required=true) AdvancedThrottlePolicyDTO body
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedIdPut(id,body,ifMatch,ifUnmodifiedSince, request);
    }
    @POST
    @Path("/throttling/advanced")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an Advanced level throttle policy", notes = "Add an Advanced level throttle policy ", response = AdvancedThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Advanced Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = AdvancedThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = AdvancedThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = AdvancedThrottlePolicyDTO.class) })
    public Response policiesThrottlingAdvancedPost(@ApiParam(value = "Advanced level policy object that should to be added " ,required=true) AdvancedThrottlePolicyDTO body
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingAdvancedPost(body, request);
    }
    @GET
    @Path("/throttling/application")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Application level throttle policies", notes = "Get all Application level throttle policies ", response = ApplicationThrottlePolicyListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policies returned ", response = ApplicationThrottlePolicyListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = ApplicationThrottlePolicyListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ApplicationThrottlePolicyListDTO.class) })
    public Response policiesThrottlingApplicationGet(@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationGet(ifNoneMatch,ifModifiedSince, request);
    }
    @DELETE
    @Path("/throttling/application/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an Application level throttle policy", notes = "Delete an Application level throttle policy ", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response policiesThrottlingApplicationIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationIdDelete(id,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/application/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve an Application Policy", notes = "Retrieve an Application Policy providing the policy name. ", response = ApplicationThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy returned ", response = ApplicationThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = ApplicationThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Tier does not exist. ", response = ApplicationThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ApplicationThrottlePolicyDTO.class) })
    public Response policiesThrottlingApplicationIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationIdGet(id,ifNoneMatch,ifModifiedSince, request);
    }
    @PUT
    @Path("/throttling/application/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an Application level throttle policy", notes = "Update an Application level throttle policy ", response = ApplicationThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy updated. ", response = ApplicationThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ApplicationThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ApplicationThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ApplicationThrottlePolicyDTO.class) })
    public Response policiesThrottlingApplicationIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Policy object that needs to be modified " ,required=true) ApplicationThrottlePolicyDTO body
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationIdPut(id,body,ifMatch,ifUnmodifiedSince, request);
    }
    @POST
    @Path("/throttling/application")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an Application level throttle policy", notes = "Add an Application level throttle policy ", response = ApplicationThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = ApplicationThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ApplicationThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = ApplicationThrottlePolicyDTO.class) })
    public Response policiesThrottlingApplicationPost(@ApiParam(value = "Application level policy object that should to be added " ,required=true) ApplicationThrottlePolicyDTO body
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingApplicationPost(body, request);
    }
    @GET
    @Path("/throttling/custom")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Custom Rules", notes = "Get all Custom Rules ", response = CustomRuleListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Custom Rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policies returned ", response = CustomRuleListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = CustomRuleListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = CustomRuleListDTO.class) })
    public Response policiesThrottlingCustomGet(@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingCustomGet(ifNoneMatch,ifModifiedSince, request);
    }
    @POST
    @Path("/throttling/custom")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a Custom Rule", notes = "Add a Custom Rule ", response = CustomRuleDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Custom Rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = CustomRuleDTO.class) })
    public Response policiesThrottlingCustomPost(@ApiParam(value = "Custom Rule object that should to be added " ,required=true) CustomRuleDTO body
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingCustomPost(body, request);
    }
    @DELETE
    @Path("/throttling/custom/{ruleId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Custom Rule", notes = "Delete a Custom Rule ", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Custom Rules", })
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
    @io.swagger.annotations.ApiOperation(value = "Retrieve a Custom Rule", notes = "Retrieve a Custom Rule providing the policy name. ", response = CustomRuleDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Custom Rules", })
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
    @io.swagger.annotations.ApiOperation(value = "Update a Custom Rule", notes = "Update a Custom Rule ", response = CustomRuleDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Custom Rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy updated. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = CustomRuleDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = CustomRuleDTO.class) })
    public Response policiesThrottlingCustomRuleIdPut(@ApiParam(value = "Custom rule UUID ",required=true) @PathParam("ruleId") String ruleId
,@ApiParam(value = "Policy object that needs to be modified " ,required=true) CustomRuleDTO body
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingCustomRuleIdPut(ruleId,body,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Subscription level throttle policies", notes = "Get all Subscription level throttle policies ", response = SubscriptionThrottlePolicyListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policies returned ", response = SubscriptionThrottlePolicyListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = SubscriptionThrottlePolicyListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = SubscriptionThrottlePolicyListDTO.class) })
    public Response policiesThrottlingSubscriptionGet(@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionGet(ifNoneMatch,ifModifiedSince, request);
    }
    @DELETE
    @Path("/throttling/subscription/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a Subscription level throttle policy", notes = "Delete a Subscription level throttle policy ", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response policiesThrottlingSubscriptionIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionIdDelete(id,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @Path("/throttling/subscription/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a Subscription Policy", notes = "Retrieve a Subscription Policy providing the policy name. ", response = SubscriptionThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_view", description = "View Tier")
        })
    }, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy returned ", response = SubscriptionThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = SubscriptionThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Policy does not exist. ", response = SubscriptionThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = SubscriptionThrottlePolicyDTO.class) })
    public Response policiesThrottlingSubscriptionIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionIdGet(id,ifNoneMatch,ifModifiedSince, request);
    }
    @PUT
    @Path("/throttling/subscription/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Subscription level throttle policy", notes = "Update a Subscription level throttle policy ", response = SubscriptionThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Policy updated. ", response = SubscriptionThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = SubscriptionThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = SubscriptionThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = SubscriptionThrottlePolicyDTO.class) })
    public Response policiesThrottlingSubscriptionIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("id") String id
,@ApiParam(value = "Policy object that needs to be modified " ,required=true) SubscriptionThrottlePolicyDTO body
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionIdPut(id,body,ifMatch,ifUnmodifiedSince, request);
    }
    @POST
    @Path("/throttling/subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a Subscription level throttle policy", notes = "Add a Subscription level throttle policy ", response = SubscriptionThrottlePolicyDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:tier_manage", description = "Manage Tier")
        })
    }, tags={ "Subscription Policies", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = SubscriptionThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = SubscriptionThrottlePolicyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = SubscriptionThrottlePolicyDTO.class) })
    public Response policiesThrottlingSubscriptionPost(@ApiParam(value = "Subscripion level policy object that should to be added " ,required=true) SubscriptionThrottlePolicyDTO body
, @Context Request request)
    throws NotFoundException {
        return delegate.policiesThrottlingSubscriptionPost(body, request);
    }
}
