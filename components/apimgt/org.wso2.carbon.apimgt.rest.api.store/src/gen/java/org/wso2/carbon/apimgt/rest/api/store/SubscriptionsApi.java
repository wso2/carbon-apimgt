package org.wso2.carbon.apimgt.rest.api.store;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.factories.SubscriptionsApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.store.SubscriptionsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/store/v1/subscriptions")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the subscriptions API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-28T14:58:05.886+05:30")
public class SubscriptionsApi implements Microservice  {
   private final SubscriptionsApiService delegate = SubscriptionsApiServiceFactory.getSubscriptionsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get subscription list. The API Identifier or Application Identifier the subscriptions of which are to be returned are passed as parameters. ", response = SubscriptionListDTO.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Subscription list returned. ", response = SubscriptionListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = SubscriptionListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = SubscriptionListDTO.class) })
    public Response subscriptionsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**. ",required=true) @QueryParam("apiId") String apiId
,@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @QueryParam("applicationId") String applicationId
,@ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for API Minor Version " , defaultValue="1.0")@HeaderParam("Minor-Version") String minorVersion
)
    throws NotFoundException {
        return delegate.subscriptionsGet(apiId,applicationId,offset,limit,accept,ifNoneMatch,minorVersion);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new subscription ", response = SubscriptionDTO.class, tags={ "Create", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = SubscriptionDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = SubscriptionDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = SubscriptionDTO.class) })
    public Response subscriptionsPost(@ApiParam(value = "Subscription object that should to be added " ,required=true) SubscriptionDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Validator for API Minor Version " , defaultValue="1.0")@HeaderParam("Minor-Version") String minorVersion
)
    throws NotFoundException {
        return delegate.subscriptionsPost(body,contentType,minorVersion);
    }
    @DELETE
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove subscription ", response = void.class, tags={ "Delete", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response subscriptionsSubscriptionIdDelete(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
,@ApiParam(value = "Validator for API Minor Version " , defaultValue="1.0")@HeaderParam("Minor-Version") String minorVersion
)
    throws NotFoundException {
        return delegate.subscriptionsSubscriptionIdDelete(subscriptionId,ifMatch,ifUnmodifiedSince,minorVersion);
    }
    @GET
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get subscription details ", response = SubscriptionDTO.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Subscription returned ", response = SubscriptionDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = SubscriptionDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Subscription does not exist. ", response = SubscriptionDTO.class) })
    public Response subscriptionsSubscriptionIdGet(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") String subscriptionId
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
,@ApiParam(value = "Validator for API Minor Version " , defaultValue="1.0")@HeaderParam("Minor-Version") String minorVersion
)
    throws NotFoundException {
        return delegate.subscriptionsSubscriptionIdGet(subscriptionId,accept,ifNoneMatch,ifModifiedSince,minorVersion);
    }
}
