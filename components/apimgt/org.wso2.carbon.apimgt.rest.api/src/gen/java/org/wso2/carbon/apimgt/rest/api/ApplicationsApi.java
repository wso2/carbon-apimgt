package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.dto.*;
import org.wso2.carbon.apimgt.rest.api.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationKeyGenerateRequestDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/applications")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/applications", description = "the applications API")
public class ApplicationsApi  {

   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

    @GET
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of applications", response = ApplicationListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Application list returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response applicationsGet(@ApiParam(value = "Subscriber username") @QueryParam("subscriber") String subscriber,
    @ApiParam(value = "Application Group Id") @QueryParam("groupId") String groupId,
    @ApiParam(value = "Maximum size of API array to return.",required=true) @QueryParam("limit") String limit,
    @ApiParam(value = "Starting point of the item list.",required=true) @QueryParam("offset") String offset,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.applicationsGet(subscriber,groupId,limit,offset,accept,ifNoneMatch);
    }
    @POST
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Create a new application", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format.") })

    public Response applicationsPost(@ApiParam(value = "Application object that is to be created" ,required=true ) ApplicationDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType)
    {
    return delegate.applicationsPost(body,contentType);
    }
    @GET
    @Path("/{applicationId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get application details", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Application returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Requested application does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response applicationsApplicationIdGet(@ApiParam(value = "Application Id",required=true ) @PathParam("applicationId") String applicationId,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.applicationsApplicationIdGet(applicationId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{applicationId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update application details", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Application updated."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response applicationsApplicationIdPut(@ApiParam(value = "Application Id",required=true ) @PathParam("applicationId") String applicationId,
    @ApiParam(value = "Application object that needs to be updated" ,required=true ) ApplicationDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsApplicationIdPut(applicationId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{applicationId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove an application", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response applicationsApplicationIdDelete(@ApiParam(value = "Application Id",required=true ) @PathParam("applicationId") String applicationId,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsApplicationIdDelete(applicationId,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/{applicationId}/generate-keys")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Generate keys for application", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Specified Production or Sandbox keys generated."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response applicationsApplicationIdGenerateKeysPost(@ApiParam(value = "Application Id",required=true ) @PathParam("applicationId") String applicationId,
    @ApiParam(value = "Application Key Generation object that includes request parameters" ,required=true ) ApplicationKeyGenerateRequestDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsApplicationIdGenerateKeysPost(applicationId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
}

