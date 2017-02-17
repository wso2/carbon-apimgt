package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.factories.SubscriptionsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/subscriptions")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/subscriptions", description = "the subscriptions API")
public class SubscriptionsApi  {

   private final SubscriptionsApiService delegate = SubscriptionsApiServiceFactory.getSubscriptionsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all subscriptions\n", notes = "This operation can be used to retrieve a list of subscriptions of the user associated with the provided access token. This operation is capable of\n\n1. Retrieving applications which are subscibed to a specific API.\n`GET https://127.0.0.1:9443/api/am/store/v0.11/subscriptions?apiId=c43a325c-260b-4302-81cb-768eafaa3aed`\n\n2. Retrieving APIs which are subscribed by a specific application.\n`GET https://127.0.0.1:9443/api/am/store/v0.11/subscriptions?applicationId=c43a325c-260b-4302-81cb-768eafaa3aed`\n\n**IMPORTANT:**\n* It is mandatory to provide either **apiId** or **applicationId**.\n", response = SubscriptionListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSubscription list returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported\n") })

    public Response subscriptionsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API I.\nShould be formatted as **provider-name-version**.\n",required=true) @QueryParam("apiId") String apiId,
    @ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true) @QueryParam("applicationId") String applicationId,
    @ApiParam(value = "Application Group Id\n") @QueryParam("groupId") String groupId,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.subscriptionsGet(apiId,applicationId,groupId,offset,limit,accept,ifNoneMatch);
    }

    public String subscriptionsGetGetLastUpdatedTime(String apiId,String applicationId,String groupId,Integer offset,Integer limit,String accept,String ifNoneMatch)
    {
        return delegate.subscriptionsGetGetLastUpdatedTime(apiId,applicationId,groupId,offset,limit,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new subscription\n", notes = "This operation can be used to add a new subscription providing the id of the API and the application.\n", response = SubscriptionDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response subscriptionsPost(@ApiParam(value = "Subscription object that should to be added\n" ,required=true ) SubscriptionDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.subscriptionsPost(body, contentType);
    }

    @POST
    @Path("/multiple")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add new subscriptions\n", notes = "Add new subscriptions\n", response = SubscriptionDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),

            @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response subscriptionsPost(@ApiParam(value = "Subscription object that should to be added\n" ,required=true ) List<SubscriptionDTO> body,
                                      @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType)
    {
        return delegate.subscriptionsPost(body,contentType);
    }

    public String subscriptionsPostGetLastUpdatedTime(SubscriptionDTO body,String contentType)
    {
        return delegate.subscriptionsPostGetLastUpdatedTime(body,contentType);
    }
    @DELETE
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Remove a subscription\n", notes = "This operation can be used to remove a subscription.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met (Will be supported in future).\n") })

    public Response subscriptionsSubscriptionIdDelete(@ApiParam(value = "Subscription Id\n",required=true ) @PathParam("subscriptionId") String subscriptionId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.subscriptionsSubscriptionIdDelete(subscriptionId,ifMatch,ifUnmodifiedSince);
    }

    public String subscriptionsSubscriptionIdDeleteGetLastUpdatedTime(String subscriptionId,String ifMatch,String ifUnmodifiedSince)
    {
        return delegate.subscriptionsSubscriptionIdDeleteGetLastUpdatedTime(subscriptionId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of a subscription\n", notes = "This operation can be used to get details of a single subscription.\n", response = SubscriptionDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSubscription returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Subscription does not exist.\n") })

    public Response subscriptionsSubscriptionIdGet(@ApiParam(value = "Subscription Id\n",required=true ) @PathParam("subscriptionId") String subscriptionId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.subscriptionsSubscriptionIdGet(subscriptionId,accept,ifNoneMatch,ifModifiedSince);
    }

    public String subscriptionsSubscriptionIdGetGetLastUpdatedTime(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince)
    {
        return delegate.subscriptionsSubscriptionIdGetGetLastUpdatedTime(subscriptionId,accept,ifNoneMatch,ifModifiedSince);
    }
}

