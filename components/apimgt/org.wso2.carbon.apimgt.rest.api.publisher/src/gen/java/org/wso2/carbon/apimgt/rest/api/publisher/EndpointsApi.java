package org.wso2.carbon.apimgt.rest.api.publisher;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.EndpointsApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HEAD;
import javax.ws.rs.QueryParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.EndpointsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1.[\\d]+/endpoints")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/endpoints")
@io.swagger.annotations.Api(description = "the endpoints API")
public class EndpointsApi implements Microservice  {
   private final EndpointsApiService delegate = EndpointsApiServiceFactory.getEndpointsApi();

    @DELETE
    @OPTIONS
    @Path("/{endpointId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an endpoint", notes = "This operation can be used to delete an existing Endpoint proving the Id of the Endpoint. ", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Endpoint (individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response endpointsEndpointIdDelete(@ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**. ",required=true) @PathParam("endpointId") String endpointId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.endpointsEndpointIdDelete(endpointId,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @OPTIONS
    @Path("/{endpointId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get specific endpoints", notes = "This operation can be used to retrieve endpoint specific details. ", response = EndPointDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Endpoint (individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Endpoint details returned. ", response = EndPointDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = EndPointDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = EndPointDTO.class) })
    public Response endpointsEndpointIdGet(@ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**. ",required=true) @PathParam("endpointId") String endpointId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.endpointsEndpointIdGet(endpointId,ifMatch,ifUnmodifiedSince, request);
    }
    @PUT
    @OPTIONS
    @Path("/{endpointId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Tier", notes = "This operation can be used to update an existing endpoint. `PUT https://127.0.0.1:9443/api/am/publisher/v1.0/endpoints/api/Low` ", response = EndPointDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Endpoint (individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Subscription updated. ", response = EndPointDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = EndPointDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = EndPointDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = EndPointDTO.class) })
    public Response endpointsEndpointIdPut(@ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**. ",required=true) @PathParam("endpointId") String endpointId
,@ApiParam(value = "Tier object that needs to be modified " ,required=true) EndPointDTO body
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.endpointsEndpointIdPut(endpointId,body,ifMatch,ifUnmodifiedSince, request);
    }
    @GET
    @OPTIONS
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all endpoints", notes = "This operation can be used to retrieve the list of endpoints available. ", response = EndPointListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Endpoint (Collection)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Endpoint list is returned. ", response = EndPointListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = EndPointListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = EndPointListDTO.class) })
    public Response endpointsGet(@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.endpointsGet(ifNoneMatch,ifModifiedSince, request);
    }
    @HEAD
    @OPTIONS
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Check given Endpoint is already exist ", notes = "Using this operation, you can check a given Endpoint name is already used. You need to provide the name you want to check. ", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Endpoint (Collection)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested Endpoint attibute status is returned ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Requested Endpoint attribute does not meet requiremnts. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Endpoint does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = void.class) })
    public Response endpointsHead(@ApiParam(value = "") @QueryParam("name") String name
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
, @Context Request request)
    throws NotFoundException {
        return delegate.endpointsHead(name,ifNoneMatch, request);
    }
    @POST
    @OPTIONS
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new endpoint", notes = "This operation can be used to add a new endpoint. ", response = EndPointDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Endpoint (Collection)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created Document object as entity in the body. Location header contains URL of newly added document. ", response = EndPointDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = EndPointDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = EndPointDTO.class) })
    public Response endpointsPost(@ApiParam(value = "EndPoint object that needs to be added " ,required=true) EndPointDTO body
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.endpointsPost(body,ifNoneMatch,ifModifiedSince, request);
    }
}
