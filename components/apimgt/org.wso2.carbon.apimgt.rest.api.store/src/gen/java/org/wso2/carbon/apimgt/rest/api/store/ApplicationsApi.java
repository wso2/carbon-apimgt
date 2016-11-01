package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.store.ApplicationsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/applications")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the applications API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class ApplicationsApi implements Microservice  {
   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

    @DELETE
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Remove an application ", response = void.class, tags={ "Delete", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = void.class) })
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
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get application details ", response = ApplicationDTO.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Application returned. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested application does not exist. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ApplicationDTO.class) })
    public Response applicationsApplicationIdGet(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
)
    throws NotFoundException {
        return delegate.applicationsApplicationIdGet(applicationId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update application details ", response = ApplicationDTO.class, tags={ "Update", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Application updated. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ApplicationDTO.class) })
    public Response applicationsApplicationIdPut(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId
,@ApiParam(value = "Application object that needs to be updated " ,required=true) ApplicationDTO body
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
    @io.swagger.annotations.ApiOperation(value = "", notes = "Generate keys for application ", response = ApplicationKeyDTO.class, tags={ "Generate Keys", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Keys are generated. ", response = ApplicationKeyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ApplicationKeyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ApplicationKeyDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ApplicationKeyDTO.class) })
    public Response applicationsGenerateKeysPost(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ",required=true) @QueryParam("applicationId") String applicationId
,@ApiParam(value = "Application object the keys of which are to be generated " ,required=true) ApplicationKeyGenerateRequestDTO body
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
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of applications ", response = ApplicationListDTO.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Application list returned. ", response = ApplicationListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = ApplicationListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ApplicationListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ApplicationListDTO.class) })
    public Response applicationsGet(@ApiParam(value = "**Search condition**.  You can search for an application by specifying the name as \"query\" attribute.  Eg. \"app1\" will match an application if the name is exactly \"app1\".  Currently this does not support wildcards. Given name must exactly match the application name. ") @QueryParam("query") String query
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
)
    throws NotFoundException {
        return delegate.applicationsGet(query,limit,offset,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Create a new application. ", response = ApplicationDTO.class, tags={ "Create", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict. Application already exists. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = ApplicationDTO.class) })
    public Response applicationsPost(@ApiParam(value = "Application object that is to be created. " ,required=true) ApplicationDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
)
    throws NotFoundException {
        return delegate.applicationsPost(body,contentType);
    }
}
