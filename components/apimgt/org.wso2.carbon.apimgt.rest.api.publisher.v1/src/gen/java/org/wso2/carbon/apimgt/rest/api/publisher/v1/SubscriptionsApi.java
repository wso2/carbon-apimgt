package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationUsageDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriberInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.SubscriptionsApiServiceImpl;
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


    @POST
    @Path("/block-subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Block a subscription", notes = "This operation can be used to block a subscription. Along with the request, `blockState` must be specified as a query parameter.  1. `BLOCKED` : Subscription is completely blocked for both Production and Sandbox environments. 2. `PROD_ONLY_BLOCKED` : Subscription is blocked for Production environment only. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscription_block", description = "Block Subscription")
        })
    }, tags={ "Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Subscription was blocked successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested subscription does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response subscriptionsBlockSubscriptionPost( @NotNull @ApiParam(value = "Subscription Id ",required=true)  @QueryParam("subscriptionId") String subscriptionId,  @NotNull @ApiParam(value = "Subscription block state. ",required=true, allowableValues="BLOCKED, PROD_ONLY_BLOCKED")  @QueryParam("blockState") String blockState, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.subscriptionsBlockSubscriptionPost(subscriptionId, blockState, ifMatch, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Subscriptions", notes = "This operation can be used to retrieve a list of subscriptions of the user associated with the provided access token. This operation is capable of  1. Retrieving all subscriptions for the user's APIs. `GET https://127.0.0.1:9443/api/am/publisher/v1/subscriptions`  2. Retrieving subscriptions for a specific API. `GET https://127.0.0.1:9443/api/am/publisher/v1/subscriptions?apiId=c43a325c-260b-4302-81cb-768eafaa3aed` ", response = SubscriptionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscription_view", description = "View Subscription")
        })
    }, tags={ "Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Subscription list returned. ", response = SubscriptionListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response subscriptionsGet( @ApiParam(value = "**API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**. ")  @QueryParam("apiId") String apiId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Keywords to filter subscriptions ")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.subscriptionsGet(apiId, limit, offset, ifNoneMatch, query, securityContext);
    }

    @GET
    @Path("/{subscriptionId}/subscriber-info")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of a user who subscribed an API", notes = "This operation can be used to get details of a user who subscribed to the API. ", response = SubscriberInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscription_view", description = "View Subscription")
        })
    }, tags={ "Subscriber",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK.  Details of the subscriber are returned. ", response = SubscriberInfoDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Subscription does not exist. ", response = ErrorDTO.class) })
    public Response subscriptionsSubscriptionIdSubscriberInfoGet(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId) throws APIManagementException{
        return delegate.subscriptionsSubscriptionIdSubscriberInfoGet(subscriptionId, securityContext);
    }

    @GET
    @Path("/{subscriptionId}/usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of a pending invoice for a monetized subscription with meterd billing.", notes = "This operation can be used to get details of a pending invoice for a monetized subscription with meterd billing. ", response = APIMonetizationUsageDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscription_view", description = "View Subscription")
        })
    }, tags={ "API Monetization",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Details of a pending invoice returned. ", response = APIMonetizationUsageDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Subscription does not exist. ", response = ErrorDTO.class) })
    public Response subscriptionsSubscriptionIdUsageGet(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId) throws APIManagementException{
        return delegate.subscriptionsSubscriptionIdUsageGet(subscriptionId, securityContext);
    }

    @POST
    @Path("/unblock-subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Unblock a Subscription", notes = "This operation can be used to unblock a subscription specifying the subscription Id. The subscription will be fully unblocked after performing this operation. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscription_block", description = "Block Subscription")
        })
    }, tags={ "Subscriptions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Subscription was unblocked successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested subscription does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response subscriptionsUnblockSubscriptionPost( @NotNull @ApiParam(value = "Subscription Id ",required=true)  @QueryParam("subscriptionId") String subscriptionId, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.subscriptionsUnblockSubscriptionPost(subscriptionId, ifMatch, securityContext);
    }
}
