package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Application;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationKey;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationKeyGenerateRequest;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationList;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/applications")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the applications API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:47:36.442+05:30")
public class ApplicationsApi  {
   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

    @DELETE
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove an application ", response = void.class, tags={ "Delete", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.  The request has not been performed because one of the preconditions is not met. ", response = void.class) })
    public Response applicationsApplicationIdDelete(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
)
    throws NotFoundException {
        return delegate.applicationsApplicationIdDelete(applicationId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get application details ", response = Application.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Application returned. ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested application does not exist. ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported ", response = Application.class) })
    public Response applicationsApplicationIdGet(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the  formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
)
    throws NotFoundException {
        return delegate.applicationsApplicationIdGet(applicationId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update application details ", response = Application.class, tags={ "Update", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Application updated. ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Invalid request or validation error ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  The resource to be updated does not exist. ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.  The request has not been performed because one of the preconditions is not met. ", response = Application.class) })
    public Response applicationsApplicationIdPut(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId
,@ApiParam(value = "Application object that needs to be updated " ,required=true) Application body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
)
    throws NotFoundException {
        return delegate.applicationsApplicationIdPut(applicationId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/generate-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Generate keys for application ", response = ApplicationKey.class, tags={ "Generate Keys", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Keys are generated. ", response = ApplicationKey.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Invalid request or validation error ", response = ApplicationKey.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  The resource to be updated does not exist. ", response = ApplicationKey.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.  The request has not been performed because one of the preconditions is not met. ", response = ApplicationKey.class) })
    public Response applicationsGenerateKeysPost(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @QueryParam("applicationId") String applicationId
,@ApiParam(value = "Application object the keys of which are to be generated " ,required=true) ApplicationKeyGenerateRequest body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
)
    throws NotFoundException {
        return delegate.applicationsGenerateKeysPost(applicationId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of applications ", response = ApplicationList.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Application list returned. ", response = ApplicationList.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = ApplicationList.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Invalid request or validation error. ", response = ApplicationList.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported. ", response = ApplicationList.class) })
    public Response applicationsGet(@ApiParam(value = "Application Group Id ") @QueryParam("groupId") String groupId
,@ApiParam(value = "**Search condition**.  You can search for an application by specifying the name as \"query\" attribute.  Eg. \"app1\" will match an application if the name is exactly \"app1\".  Currently this does not support wildcards. Given name must exactly match the application name. ") @QueryParam("query") String query
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Starting point within the complete list of items qualified.   ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
)
    throws NotFoundException {
        return delegate.applicationsGet(groupId,query,limit,offset,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Create a new application. ", response = Application.class, tags={ "Create", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.  Successful response with the newly created object as entity in the body.  Location header contains URL of newly created entity. ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Invalid request or validation error ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict.  Application already exists. ", response = Application.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.  The entity of the request was in a not supported format. ", response = Application.class) })
    public Response applicationsPost(@ApiParam(value = "Application object that is to be created. " ,required=true) Application body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
)
    throws NotFoundException {
        return delegate.applicationsPost(body,contentType);
    }
}
