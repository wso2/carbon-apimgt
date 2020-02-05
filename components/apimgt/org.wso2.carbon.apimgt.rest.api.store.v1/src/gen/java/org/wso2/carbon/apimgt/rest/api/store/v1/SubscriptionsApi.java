package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIMonetizationUsageDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SubscriptionsApiServiceImpl;
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
@Path("/subscriptions")

@Api(description = "the subscriptions API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class SubscriptionsApi  {

  @Context MessageContext securityContext;

SubscriptionsApiService delegate = new SubscriptionsApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all subscriptions ", notes = "This operation can be used to retrieve a list of subscriptions of the user associated with the provided access token. This operation is capable of  1. Retrieving applications which are subscibed to a specific API. `GET https://localhost:9443/api/am/store/v1/subscriptions?apiId=c43a325c-260b-4302-81cb-768eafaa3aed`  2. Retrieving APIs which are subscribed by a specific application. `GET https://localhost:9443/api/am/store/v1/subscriptions?applicationId=c43a325c-260b-4302-81cb-768eafaa3aed`  **IMPORTANT:** * It is mandatory to provide either **apiId** or **applicationId**. ", response = SubscriptionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:sub_manage", description = "Retrieve, Manage subscriptions"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Subscription list returned. ", response = SubscriptionListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response subscriptionsGet( @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ")  @QueryParam("apiId") String apiId,  @ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ")  @QueryParam("applicationId") String applicationId,  @ApiParam(value = "Application Group Id ")  @QueryParam("groupId") String groupId, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.subscriptionsGet(apiId, applicationId, groupId, xWSO2Tenant, offset, limit, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/multiple")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add new subscriptions ", notes = "This operation can be used to add a new subscriptions providing the ids of the APIs and the applications. ", response = SubscriptionDTO.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:sub_manage", description = "Retrieve, Manage subscriptions"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the newly created objects as entity in the body. ", response = SubscriptionDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = Void.class) })
    public Response subscriptionsMultiplePost(@ApiParam(value = "Subscription objects that should to be added " ,required=true) List<SubscriptionDTO> body, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.subscriptionsMultiplePost(body, xWSO2Tenant, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new subscription ", notes = "This operation can be used to add a new subscription providing the id of the API and the application. ", response = SubscriptionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:sub_manage", description = "Retrieve, Manage subscriptions"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = SubscriptionDTO.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = Void.class) })
    public Response subscriptionsPost(@ApiParam(value = "Subscription object that should to be added " ,required=true) SubscriptionDTO body, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.subscriptionsPost(body, xWSO2Tenant, securityContext);
    }

    @DELETE
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Remove a subscription ", notes = "This operation can be used to remove a subscription. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:sub_manage", description = "Retrieve, Manage subscriptions"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response subscriptionsSubscriptionIdDelete(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.subscriptionsSubscriptionIdDelete(subscriptionId, ifMatch, securityContext);
    }

    @GET
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of a subscription ", notes = "This operation can be used to get details of a single subscription. ", response = SubscriptionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:sub_manage", description = "Retrieve, Manage subscriptions"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Subscription returned ", response = SubscriptionDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Subscription does not exist. ", response = ErrorDTO.class) })
    public Response subscriptionsSubscriptionIdGet(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.subscriptionsSubscriptionIdGet(subscriptionId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{subscriptionId}/usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of a pending invoice for a monetized subscription with metered billing.", notes = "This operation can be used to get details of a pending invoice for a monetized subscription with metered billing. ", response = APIMonetizationUsageDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:sub_manage", description = "Retrieve, Manage subscriptions"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "API Monetization" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Details of a pending invoice returned. ", response = APIMonetizationUsageDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Subscription does not exist. ", response = ErrorDTO.class) })
    public Response subscriptionsSubscriptionIdUsageGet(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId) throws APIManagementException{
        return delegate.subscriptionsSubscriptionIdUsageGet(subscriptionId, securityContext);
    }
}
