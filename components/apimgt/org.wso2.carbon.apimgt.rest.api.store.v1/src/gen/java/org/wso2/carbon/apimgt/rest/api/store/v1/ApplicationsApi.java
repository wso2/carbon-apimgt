package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyReGenerateResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ScopeListDTO;
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
    @io.swagger.annotations.ApiOperation(value = "Remove an application\n", notes = "This operation can be used to remove an application specifying its id.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted.\nThe request has been accepted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdDelete(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.applicationsApplicationIdDelete(applicationId,ifMatch);
    }
    @POST
    @Path("/{applicationId}/generate-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Generate application keys", notes = "Generate keys (Consumer key/secret) for application\n", response = ApplicationKeyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys are generated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdGenerateKeysPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Application key generation request object\n" ,required=true ) ApplicationKeyGenerateRequestDTO body)
    {
    return delegate.applicationsApplicationIdGenerateKeysPost(applicationId,body);
    }
    @GET
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an application\n", notes = "This operation can be used to retrieve details of an individual application specifying the application id in the URI.\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested application does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response applicationsApplicationIdGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.applicationsApplicationIdGet(applicationId,ifNoneMatch);
    }
    @GET
    @Path("/{applicationId}/keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve all application keys", notes = "Retrieve keys (Consumer key/secret) of application\n", response = ApplicationKeyListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys are returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdKeysGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId)
    {
    return delegate.applicationsApplicationIdKeysGet(applicationId);
    }
    @POST
    @Path("/{applicationId}/keys/{keyType}/generate-token")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Generate application token", notes = "Generate an access token for application by client_credentials grant type\n", response = ApplicationTokenDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nToken is generated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdKeysKeyTypeGenerateTokenPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox).\n",required=true, allowableValues="{values=[PRODUCTION, SANDBOX]}" ) @PathParam("keyType")  String keyType,
    @ApiParam(value = "Application token generation request object\n" ,required=true ) ApplicationTokenGenerateRequestDTO body,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.applicationsApplicationIdKeysKeyTypeGenerateTokenPost(applicationId,keyType,body,ifMatch);
    }
    @GET
    @Path("/{applicationId}/keys/{keyType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get key details of a given type\n", notes = "This operation can be used to retrieve key details of an individual application specifying the key type in the URI.\n", response = ApplicationKeyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys of given type are returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdKeysKeyTypeGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox).\n",required=true, allowableValues="{values=[PRODUCTION, SANDBOX]}" ) @PathParam("keyType")  String keyType,
    @ApiParam(value = "Application Group Id\n") @QueryParam("groupId")  String groupId)
    {
    return delegate.applicationsApplicationIdKeysKeyTypeGet(applicationId,keyType,groupId);
    }
    @PUT
    @Path("/{applicationId}/keys/{keyType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update grant types and callback url of an application\n", notes = "This operation can be used to update grant types and callback url of an application. (Consumer Key and Consumer Secret are ignored) Upon succesfull you will retrieve the updated key details as the response.\n", response = ApplicationKeyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok.\nGrant types or/and callback url is/are updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdKeysKeyTypePut(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox).\n",required=true, allowableValues="{values=[PRODUCTION, SANDBOX]}" ) @PathParam("keyType")  String keyType,
    @ApiParam(value = "Grant types/Callback URL update request object\n" ,required=true ) ApplicationKeyDTO body)
    {
    return delegate.applicationsApplicationIdKeysKeyTypePut(applicationId,keyType,body);
    }
    @POST
    @Path("/{applicationId}/keys/{keyType}/regenerate-secret")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Re-generate consumer secret\n", notes = "This operation can be used to re generate consumer secret for an application for the give key type\n", response = ApplicationKeyReGenerateResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys are re generated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met (Will be supported in future).\n") })

    public Response applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox).\n",required=true, allowableValues="{values=[PRODUCTION, SANDBOX]}" ) @PathParam("keyType")  String keyType)
    {
    return delegate.applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(applicationId,keyType);
    }
    @POST
    @Path("/{applicationId}/map-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Map application keys", notes = "Map keys (Consumer key/secret) to an application\n", response = ApplicationKeyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys are mapped.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdMapKeysPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Application key mapping request object\n" ,required=true ) ApplicationKeyMappingRequestDTO body)
    {
    return delegate.applicationsApplicationIdMapKeysPost(applicationId,body);
    }
    @PUT
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an application\n", notes = "This operation can be used to update an application. Upon succesfull you will retrieve the updated application as the response.\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdPut(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Application object that needs to be updated\n" ,required=true ) ApplicationDTO body,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.applicationsApplicationIdPut(applicationId,body,ifMatch);
    }
    @GET
    @Path("/{applicationId}/scopes")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get scopes of application\n", notes = "Get scopes associated with a particular application based on subscribed APIs\n", response = ScopeListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nScope returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Un authorized.\nThe user is not authorized to view the application .\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested application does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response applicationsApplicationIdScopesGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Filter user by roles.\n") @QueryParam("filterByUserRoles")  Boolean filterByUserRoles,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.applicationsApplicationIdScopesGet(applicationId,filterByUserRoles,ifNoneMatch);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search applications\n", notes = "This operation can be used to retrieve list of applications that is belonged to the user associated with the provided access token.\n", response = ApplicationListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication list returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response applicationsGet(@ApiParam(value = "Application Group Id\n") @QueryParam("groupId")  String groupId,
    @ApiParam(value = "**Search condition**.\n\nYou can search for an application by specifying the name as \"query\" attribute.\n\nEg.\n\"app1\" will match an application if the name is exactly \"app1\".\n\nCurrently this does not support wildcards. Given name must exactly match the application name.\n") @QueryParam("query")  String query,
    @ApiParam(value = "", allowableValues="{values=[name, throttlingPolicy, status]}") @QueryParam("sortBy")  String sortBy,
    @ApiParam(value = "", allowableValues="{values=[asc, desc]}") @QueryParam("sortOrder")  String sortOrder,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.applicationsGet(groupId,query,sortBy,sortOrder,limit,offset,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new application\n", notes = "This operation can be used to create a new application specifying the details of the application in the payload.\n", response = ApplicationDTO.class)
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

