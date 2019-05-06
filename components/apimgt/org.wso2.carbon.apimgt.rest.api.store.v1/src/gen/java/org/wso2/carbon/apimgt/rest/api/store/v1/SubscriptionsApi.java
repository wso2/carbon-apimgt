package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.SubscriptionsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;

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
    @io.swagger.annotations.ApiOperation(value = "Get all subscriptions\n", notes = "This operation can be used to retrieve a list of subscriptions of the user associated with the provided access token. This operation is capable of\n\n1. Retrieving applications which are subscibed to a specific API.\n`GET https://localhost:9443/api/am/store/v1.0/subscriptions?apiId=c43a325c-260b-4302-81cb-768eafaa3aed`\n\n2. Retrieving APIs which are subscribed by a specific application.\n`GET https://localhost:9443/api/am/store/v1.0/subscriptions?applicationId=c43a325c-260b-4302-81cb-768eafaa3aed`\n\n**IMPORTANT:**\n* It is mandatory to provide either **apiId** or **applicationId**.\n", response = SubscriptionListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSubscription list returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported\n") })

    public Response subscriptionsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\n",required=true) @QueryParam("apiId")  String apiId,
    @ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true) @QueryParam("applicationId")  String applicationId,
    @ApiParam(value = "Application Group Id\n") @QueryParam("groupId")  String groupId,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.subscriptionsGet(apiId,applicationId,groupId,offset,limit,ifNoneMatch);
    }
    @POST
    @Path("/multiple")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add new subscriptions\n", notes = "This operation can be used to add a new subscriptions providing the ids of the APIs and the applications.\n", response = SubscriptionDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with the newly created objects as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response subscriptionsMultiplePost(@ApiParam(value = "Subscription objects that should to be added\n" ,required=true ) List<SubscriptionDTO> body)
    {
    return delegate.subscriptionsMultiplePost(body);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new subscription\n", notes = "This operation can be used to add a new subscription providing the id of the API and the application.\n", response = SubscriptionDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted.\nThe request has been accepted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response subscriptionsPost(@ApiParam(value = "Subscription object that should to be added\n" ,required=true ) SubscriptionDTO body)
    {
    return delegate.subscriptionsPost(body);
    }
    @DELETE
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Remove a subscription\n", notes = "This operation can be used to remove a subscription.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted.\nThe request has been accepted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response subscriptionsSubscriptionIdDelete(@ApiParam(value = "Subscription Id\n",required=true ) @PathParam("subscriptionId")  String subscriptionId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.subscriptionsSubscriptionIdDelete(subscriptionId,ifMatch);
    }
    @GET
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of a subscription\n", notes = "This operation can be used to get details of a single subscription.\n", response = SubscriptionDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSubscription returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Subscription does not exist.\n") })

    public Response subscriptionsSubscriptionIdGet(@ApiParam(value = "Subscription Id\n",required=true ) @PathParam("subscriptionId")  String subscriptionId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.subscriptionsSubscriptionIdGet(subscriptionId,ifNoneMatch);
    }
}

