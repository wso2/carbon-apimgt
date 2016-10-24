package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.SubscriptionsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionList;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Subscription;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/subscriptions")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the subscriptions API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T13:00:17.095+05:30")
public class SubscriptionsApi implements Microservice  {
   private final SubscriptionsApiService delegate = SubscriptionsApiServiceFactory.getSubscriptionsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get subscription list. The API Identifier or Application Identifier the subscriptions of which are to be returned are passed as parameters. ", response = SubscriptionList.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Subscription list returned. ", response = SubscriptionList.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = SubscriptionList.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = SubscriptionList.class) })
    public Response subscriptionsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.  The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**. ",required=true) @QueryParam("apiId") String apiId
,@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @QueryParam("applicationId") String applicationId
,@ApiParam(value = "Application Group Id ") @QueryParam("groupId") String groupId
,@ApiParam(value = "Starting point within the complete list of items qualified.   ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
)
    throws NotFoundException {
        return delegate.subscriptionsGet(apiId,applicationId,groupId,offset,limit,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new subscription ", response = Subscription.class, tags={ "Create", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.  Successful response with the newly created object as entity in the body.  Location header contains URL of newly created entity. ", response = Subscription.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Invalid request or validation error. ", response = Subscription.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.  The entity of the request was in a not supported format. ", response = Subscription.class) })
    public Response subscriptionsPost(@ApiParam(value = "Subscription object that should to be added " ,required=true) Subscription body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
)
    throws NotFoundException {
        return delegate.subscriptionsPost(body,contentType);
    }
    @DELETE
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove subscription ", response = void.class, tags={ "Delete", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.  The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response subscriptionsSubscriptionIdDelete(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
)
    throws NotFoundException {
        return delegate.subscriptionsSubscriptionIdDelete(subscriptionId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get subscription details ", response = Subscription.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Subscription returned ", response = Subscription.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = Subscription.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Requested Subscription does not exist. ", response = Subscription.class) })
    public Response subscriptionsSubscriptionIdGet(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the  formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
)
    throws NotFoundException {
        return delegate.subscriptionsSubscriptionIdGet(subscriptionId,accept,ifNoneMatch,ifModifiedSince);
    }
}
