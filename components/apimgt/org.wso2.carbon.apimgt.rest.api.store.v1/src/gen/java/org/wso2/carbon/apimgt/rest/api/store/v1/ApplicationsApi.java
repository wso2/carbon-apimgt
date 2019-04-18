package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeysListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/applications")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/applications", description = "the applications API")
public class ApplicationsApi  {

   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

    @DELETE
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an application", notes = "Delete an application\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted.\nThe request has been accepted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdDelete(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsApplicationIdDelete(applicationId,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/{applicationId}/generate-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Generate application keys", notes = "Generate keys (Consumer key/secret) for application\n", response = ApplicationKeysDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys are generated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdGenerateKeysPost(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Application key generation request object\n" ,required=true ) ApplicationKeyGenerateRequestDTO body)
    {
    return delegate.applicationsApplicationIdGenerateKeysPost(applicationId,body);
    }
    @POST
    @Path("/{applicationId}/generate-token")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Generate application token", notes = "Generate an access token for application by client_credentials grant type\n", response = ApplicationTokenDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nToken is generated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdGenerateTokenPost(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Application token generation request object\n" ,required=true ) ApplicationTokenGenerateRequestDTO body,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsApplicationIdGenerateTokenPost(applicationId,body,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an application", notes = "Get application details\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested application does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response applicationsApplicationIdGet(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.applicationsApplicationIdGet(applicationId,ifNoneMatch,ifModifiedSince);
    }
    @GET
    @Path("/{applicationId}/keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve all application keys", notes = "Retrieve keys (Consumer key/secret) of application\n", response = ApplicationKeysListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys are returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdKeysGet(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId)
    {
    return delegate.applicationsApplicationIdKeysGet(applicationId);
    }
    @GET
    @Path("/{applicationId}/keys/{keyType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve application keys for a provided type", notes = "Retrieve keys (Consumer key/secret) of application by a given type\n", response = ApplicationKeysDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys of given type are returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdKeysKeyTypeGet(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox).\n",required=true, allowableValues="{values=[PRODUCTION, SANDBOX]}" ) @PathParam("keyType")  String keyType)
    {
    return delegate.applicationsApplicationIdKeysKeyTypeGet(applicationId,keyType);
    }
    @PUT
    @Path("/{applicationId}/keys/{keyType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an application key", notes = "Update grant types and callback url (Consumer Key and Consumer Secret are ignored)\n", response = ApplicationKeysDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok.\nGrant types or/and callback url is/are updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdKeysKeyTypePut(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox).\n",required=true, allowableValues="{values=[PRODUCTION, SANDBOX]}" ) @PathParam("keyType")  String keyType,
    @ApiParam(value = "Grant types/Callback URL update request object\n" ,required=true ) ApplicationKeysDTO body)
    {
    return delegate.applicationsApplicationIdKeysKeyTypePut(applicationId,keyType,body);
    }
    @POST
    @Path("/{applicationId}/map-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Map application keys", notes = "Map keys (Consumer key/secret) to an application\n", response = ApplicationKeysDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys are mapped.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdMapKeysPost(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Application key mapping request object\n" ,required=true ) ApplicationKeyMappingRequestDTO body)
    {
    return delegate.applicationsApplicationIdMapKeysPost(applicationId,body);
    }
    @PUT
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an application", notes = "Update application details\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdPut(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Application object that needs to be updated\n" ,required=true ) ApplicationDTO body,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsApplicationIdPut(applicationId,body,ifMatch,ifUnmodifiedSince);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all applications", notes = "Get a list of applications\n", response = ApplicationListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication list returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response applicationsGet(@ApiParam(value = "**Search condition**.\n\nYou can search for an application by specifying the name as \"query\" attribute.\n\nEg.\n\"app1\" will match an application if the name is exactly \"app1\".\n\nCurrently this does not support wildcards. Given name must exactly match the application name.\n") @QueryParam("query")  String query,
    @ApiParam(value = "", allowableValues="{values=[name, throttlingTier, status]}") @QueryParam("sortBy")  String sortBy,
    @ApiParam(value = "", allowableValues="{values=[asc, desc]}") @QueryParam("sortOrder")  String sortOrder,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.applicationsGet(query,sortBy,sortOrder,limit,offset,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new application", notes = "Create a new application.\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted.\nThe request has been accepted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict.\nApplication already exists.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response applicationsPost(@ApiParam(value = "Application object that is to be created.\n" ,required=true ) ApplicationDTO body)
    {
    return delegate.applicationsPost(body);
    }
}

