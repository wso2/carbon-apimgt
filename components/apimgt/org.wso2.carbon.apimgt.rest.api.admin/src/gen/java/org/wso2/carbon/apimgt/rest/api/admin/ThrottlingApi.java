package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.ThrottlingApiServiceImpl;
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
@Path("/throttling")

@Api(description = "the throttling API")




public class ThrottlingApi  {

  @Context MessageContext securityContext;

ThrottlingApiService delegate = new ThrottlingApiServiceImpl();


    @GET
    @Path("/deny-policies")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Deny Policies", notes = "Retrieves all existing deny policies. ", response = BlockingConditionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:bl_view", description = "View deny policies")
        })
    }, tags={ "Deny Policies (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Deny Policies returned ", response = BlockingConditionListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingDenyPoliciesGet( @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.throttlingDenyPoliciesGet(accept, securityContext);
    }

    @POST
    @Path("/deny-policies")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a deny policy", notes = "Adds a new deny policy ", response = BlockingConditionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:bl_manage", description = "Update and delete deny policies")
        })
    }, tags={ "Deny Policies (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = BlockingConditionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response throttlingDenyPoliciesPost( @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Blocking condition object that should to be added " ,required=true) BlockingConditionDTO blockingConditionDTO) throws APIManagementException{
        return delegate.throttlingDenyPoliciesPost(contentType, blockingConditionDTO, securityContext);
    }

    @DELETE
    @Path("/deny-policy/{conditionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Deny Policy", notes = "Deletes an existing deny policy ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:bl_manage", description = "Update and delete deny policies")
        })
    }, tags={ "Deny Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingDenyPolicyConditionIdDelete(@ApiParam(value = "Blocking condition identifier ",required=true) @PathParam("conditionId") String conditionId) throws APIManagementException{
        return delegate.throttlingDenyPolicyConditionIdDelete(conditionId, securityContext);
    }

    @GET
    @Path("/deny-policy/{conditionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Deny Policy", notes = "Retrieves a Deny policy providing the condition Id ", response = BlockingConditionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:bl_view", description = "View deny policies")
        })
    }, tags={ "Deny Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Condition returned ", response = BlockingConditionDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingDenyPolicyConditionIdGet(@ApiParam(value = "Blocking condition identifier ",required=true) @PathParam("conditionId") String conditionId) throws APIManagementException{
        return delegate.throttlingDenyPolicyConditionIdGet(conditionId, securityContext);
    }

    @PATCH
    @Path("/deny-policy/{conditionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Deny Policy", notes = "Update a deny policy by Id ", response = BlockingConditionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:bl_manage", description = "Update and delete deny policies")
        })
    }, tags={ "Deny Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully updated. ", response = BlockingConditionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingDenyPolicyConditionIdPatch(@ApiParam(value = "Blocking condition identifier ",required=true) @PathParam("conditionId") String conditionId,  @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Blocking condition with updated status " ,required=true) BlockingConditionStatusDTO blockingConditionStatusDTO) throws APIManagementException{
        return delegate.throttlingDenyPolicyConditionIdPatch(conditionId, contentType, blockingConditionStatusDTO, securityContext);
    }

    @GET
    @Path("/policies/advanced")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Advanced Throttling Policies", notes = "Retrieves all existing advanced throttling policies. ", response = AdvancedThrottlePolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_view", description = "View throttling policies")
        })
    }, tags={ "Advanced Policy (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policies returned ", response = AdvancedThrottlePolicyListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesAdvancedGet( @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.throttlingPoliciesAdvancedGet(accept, securityContext);
    }

    @DELETE
    @Path("/policies/advanced/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an Advanced Throttling Policy", notes = "Deletes an advanced throttling policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Advanced Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingPoliciesAdvancedPolicyIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId) throws APIManagementException{
        return delegate.throttlingPoliciesAdvancedPolicyIdDelete(policyId, securityContext);
    }

    @GET
    @Path("/policies/advanced/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get an Advanced Throttling Policy", notes = "Retrieves an advanced throttling policy. ", response = AdvancedThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_view", description = "View throttling policies")
        })
    }, tags={ "Advanced Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy returned ", response = AdvancedThrottlePolicyDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesAdvancedPolicyIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId) throws APIManagementException{
        return delegate.throttlingPoliciesAdvancedPolicyIdGet(policyId, securityContext);
    }

    @PUT
    @Path("/policies/advanced/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an Advanced Throttling Policy", notes = "Updates an existing Advanced throttling policy. ", response = AdvancedThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Advanced Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy updated. ", response = AdvancedThrottlePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingPoliciesAdvancedPolicyIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId,  @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Policy object that needs to be modified " ,required=true) AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO) throws APIManagementException{
        return delegate.throttlingPoliciesAdvancedPolicyIdPut(policyId, contentType, advancedThrottlePolicyDTO, securityContext);
    }

    @POST
    @Path("/policies/advanced")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an Advanced Throttling Policy", notes = "Add a new advanced throttling policy. ", response = AdvancedThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Advanced Policy (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = AdvancedThrottlePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response throttlingPoliciesAdvancedPost( @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Advanced level policy object that should to be added " ,required=true) AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO) throws APIManagementException{
        return delegate.throttlingPoliciesAdvancedPost(contentType, advancedThrottlePolicyDTO, securityContext);
    }

    @GET
    @Path("/policies/application")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Application Throttling Policies", notes = "Retrieves all existing application throttling policies. ", response = ApplicationThrottlePolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_view", description = "View throttling policies")
        })
    }, tags={ "Application Policy (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policies returned ", response = ApplicationThrottlePolicyListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesApplicationGet( @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.throttlingPoliciesApplicationGet(accept, securityContext);
    }

    @DELETE
    @Path("/policies/application/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an Application Throttling policy", notes = "Deletes an application level throttling policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Application Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingPoliciesApplicationPolicyIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId) throws APIManagementException{
        return delegate.throttlingPoliciesApplicationPolicyIdDelete(policyId, securityContext);
    }

    @GET
    @Path("/policies/application/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get an Application Throttling Policy", notes = "Retrieves an application throttling policy. ", response = ApplicationThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_view", description = "View throttling policies")
        })
    }, tags={ "Application Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy returned ", response = ApplicationThrottlePolicyDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesApplicationPolicyIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId) throws APIManagementException{
        return delegate.throttlingPoliciesApplicationPolicyIdGet(policyId, securityContext);
    }

    @PUT
    @Path("/policies/application/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an Application Throttling policy", notes = "Updates an existing application level throttling policy. Upon a succesfull update, you will receive the updated application policy as the response. ", response = ApplicationThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Application Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy updated. ", response = ApplicationThrottlePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingPoliciesApplicationPolicyIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId,  @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Policy object that needs to be modified " ,required=true) ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO) throws APIManagementException{
        return delegate.throttlingPoliciesApplicationPolicyIdPut(policyId, contentType, applicationThrottlePolicyDTO, securityContext);
    }

    @POST
    @Path("/policies/application")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an Application Throttling Policy", notes = "This operation can be used to add a new application level throttling policy. ", response = ApplicationThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Application Policy (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = ApplicationThrottlePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response throttlingPoliciesApplicationPost( @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Application level policy object that should to be added " ,required=true) ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO) throws APIManagementException{
        return delegate.throttlingPoliciesApplicationPost(contentType, applicationThrottlePolicyDTO, securityContext);
    }

    @GET
    @Path("/policies/custom")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Custom Rules", notes = "Retrieves all custom rules.  **NOTE:** * Only super tenant users are allowed for this operation. ", response = CustomRuleListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_view", description = "View throttling policies")
        })
    }, tags={ "Custom Rules (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policies returned ", response = CustomRuleListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesCustomGet( @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.throttlingPoliciesCustomGet(accept, securityContext);
    }

    @POST
    @Path("/policies/custom")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a Custom Rule", notes = "Adds a new custom rule.  **NOTE:** * Only super tenant users are allowed for this operation. ", response = CustomRuleDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Custom Rules (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = CustomRuleDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response throttlingPoliciesCustomPost( @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Custom Rule object that should to be added " ,required=true) CustomRuleDTO customRuleDTO) throws APIManagementException{
        return delegate.throttlingPoliciesCustomPost(contentType, customRuleDTO, securityContext);
    }

    @DELETE
    @Path("/policies/custom/{ruleId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Custom Rule", notes = "Delete a custom rule. We need to provide the Id of the policy as a path parameter.  **NOTE:** * Only super tenant users are allowed for this operation. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Custom Rules (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingPoliciesCustomRuleIdDelete(@ApiParam(value = "Custom rule UUID ",required=true) @PathParam("ruleId") String ruleId) throws APIManagementException{
        return delegate.throttlingPoliciesCustomRuleIdDelete(ruleId, securityContext);
    }

    @GET
    @Path("/policies/custom/{ruleId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Custom Rule", notes = "Retrieves a custom rule. We need to provide the policy Id as a path parameter.  **NOTE:** * Only super tenant users are allowed for this operation. ", response = CustomRuleDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_view", description = "View throttling policies")
        })
    }, tags={ "Custom Rules (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy returned ", response = CustomRuleDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesCustomRuleIdGet(@ApiParam(value = "Custom rule UUID ",required=true) @PathParam("ruleId") String ruleId) throws APIManagementException{
        return delegate.throttlingPoliciesCustomRuleIdGet(ruleId, securityContext);
    }

    @PUT
    @Path("/policies/custom/{ruleId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Custom Rule", notes = "Updates an existing custom rule.  **NOTE:** * Only super tenant users are allowed for this operation. ", response = CustomRuleDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Custom Rules (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy updated. ", response = CustomRuleDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingPoliciesCustomRuleIdPut(@ApiParam(value = "Custom rule UUID ",required=true) @PathParam("ruleId") String ruleId,  @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Policy object that needs to be modified " ,required=true) CustomRuleDTO customRuleDTO) throws APIManagementException{
        return delegate.throttlingPoliciesCustomRuleIdPut(ruleId, contentType, customRuleDTO, securityContext);
    }

    @GET
    @Path("/policies/subscription")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Subscription Throttling Policies", notes = "This operation can be used to retrieve all Subscription level throttling policies. ", response = SubscriptionThrottlePolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_view", description = "View throttling policies")
        })
    }, tags={ "Subscription Policy (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policies returned ", response = SubscriptionThrottlePolicyListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesSubscriptionGet( @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.throttlingPoliciesSubscriptionGet(accept, securityContext);
    }

    @DELETE
    @Path("/policies/subscription/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Subscription Policy", notes = "This operation can be used to delete a subscription level throttling policy by specifying the Id of the policy as a path paramter. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Subscription Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingPoliciesSubscriptionPolicyIdDelete(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId) throws APIManagementException{
        return delegate.throttlingPoliciesSubscriptionPolicyIdDelete(policyId, securityContext);
    }

    @GET
    @Path("/policies/subscription/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Subscription Policy", notes = "This operation can be used to retrieves subscription level throttling policy by specifying the Id of the policy as a path paramter ", response = SubscriptionThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_view", description = "View throttling policies")
        })
    }, tags={ "Subscription Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy returned ", response = SubscriptionThrottlePolicyDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response throttlingPoliciesSubscriptionPolicyIdGet(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId) throws APIManagementException{
        return delegate.throttlingPoliciesSubscriptionPolicyIdGet(policyId, securityContext);
    }

    @PUT
    @Path("/policies/subscription/{policyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Subscription Policy", notes = "Updates an existing subscription level throttling policy. ", response = SubscriptionThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Subscription Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Policy updated. ", response = SubscriptionThrottlePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response throttlingPoliciesSubscriptionPolicyIdPut(@ApiParam(value = "Thorttle policy UUID ",required=true) @PathParam("policyId") String policyId,  @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Policy object that needs to be modified " ,required=true) SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO) throws APIManagementException{
        return delegate.throttlingPoliciesSubscriptionPolicyIdPut(policyId, contentType, subscriptionThrottlePolicyDTO, securityContext);
    }

    @POST
    @Path("/policies/subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a Subscription Throttling Policy", notes = "This operation can be used to add a Subscription level throttling policy specifying the details of the policy in the payload. ", response = SubscriptionThrottlePolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tier_manage", description = "Update and delete throttling policies")
        })
    }, tags={ "Subscription Policy (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = SubscriptionThrottlePolicyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response throttlingPoliciesSubscriptionPost( @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Subscripion level policy object that should to be added " ,required=true) SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO) throws APIManagementException{
        return delegate.throttlingPoliciesSubscriptionPost(contentType, subscriptionThrottlePolicyDTO, securityContext);
    }
}
