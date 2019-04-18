package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.EndpointsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.factories.EndpointsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndPointListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/endpoints")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/endpoints", description = "the endpoints API")
public class EndpointsApi  {

   private final EndpointsApiService delegate = EndpointsApiServiceFactory.getEndpointsApi();

    @DELETE
    @Path("/{endpointId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an endpoint", notes = "This operation can be used to delete an existing Endpoint proving the Id of the Endpoint.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response endpointsEndpointIdDelete(@ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**.\n",required=true ) @PathParam("endpointId")  String endpointId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.endpointsEndpointIdDelete(endpointId,ifMatch);
    }
    @GET
    @Path("/{endpointId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get specific endpoints", notes = "This operation can be used to retrieve endpoint specific details.\n", response = EndPointDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nEndpoint details returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n") })

    public Response endpointsEndpointIdGet(@ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**.\n",required=true ) @PathParam("endpointId")  String endpointId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.endpointsEndpointIdGet(endpointId,ifMatch);
    }
    @PUT
    @Path("/{endpointId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a Tier", notes = "This operation can be used to update an existing endpoint.\n`PUT https://127.0.0.1:9443/api/am/publisher/v1.0/endpoints/api/Low`\n", response = EndPointDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSubscription updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response endpointsEndpointIdPut(@ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**.\n",required=true ) @PathParam("endpointId")  String endpointId,
    @ApiParam(value = "Tier object that needs to be modified\n" ,required=true ) EndPointDTO body,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.endpointsEndpointIdPut(endpointId,body,ifMatch);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all endpoints", notes = "This operation can be used to retrieve the list of endpoints available.\n", response = EndPointListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nEndpoint list is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n") })

    public Response endpointsGet(@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.endpointsGet(ifNoneMatch);
    }
    @HEAD
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Check given Endpoint is already exist\n", notes = "Using this operation, you can check a given Endpoint name is already used. You need to provide the name you want to check.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested Endpoint attibute status is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nRequested Endpoint attribute does not meet requiremnts.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Endpoint does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response endpointsHead(@ApiParam(value = "") @QueryParam("name")  String name,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.endpointsHead(name,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new endpoint", notes = "This operation can be used to add a new endpoint.\n", response = EndPointDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created Document object as entity in the body.\nLocation header contains URL of newly added document.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response endpointsPost(@ApiParam(value = "EndPoint object that needs to be added\n" ,required=true ) EndPointDTO body,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.endpointsPost(body,ifNoneMatch);
    }
}

