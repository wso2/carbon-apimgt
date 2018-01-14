package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ScopeListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.validation.constraints.NotNull;
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
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdDelete(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header (Will be supported in future).\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsApplicationIdDelete(applicationId,ifMatch,ifUnmodifiedSince);
    }

    public String applicationsApplicationIdDeleteGetLastUpdatedTime(String applicationId,String ifMatch,String ifUnmodifiedSince)
    {
        return delegate.applicationsApplicationIdDeleteGetLastUpdatedTime(applicationId,ifMatch,ifUnmodifiedSince);
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
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource (Will be supported in future).\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.applicationsApplicationIdGet(applicationId,accept,ifNoneMatch,ifModifiedSince);
    }

    public String applicationsApplicationIdGetGetLastUpdatedTime(String applicationId,String accept,String ifNoneMatch,String ifModifiedSince)
    {
        return delegate.applicationsApplicationIdGetGetLastUpdatedTime(applicationId,accept,ifNoneMatch,ifModifiedSince);
    }
    @GET
    @Path("/{applicationId}/keys/{keyType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get key details of a given type\n", notes = "This operation can be used to retrieve key details of an individual application specifying the key type in the URI.\n", response = ApplicationKeyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication key details returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested application does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response applicationsApplicationIdKeysKeyTypeGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox).\n",required=true, allowableValues="{values=[PRODUCTION, SANDBOX]}" ) @PathParam("keyType")  String keyType,
    @ApiParam(value = "Application Group Id\n") @QueryParam("groupId")  String groupId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept)
    {
    return delegate.applicationsApplicationIdKeysKeyTypeGet(applicationId,keyType,groupId,accept);
    }

    public String applicationsApplicationIdKeysKeyTypeGetGetLastUpdatedTime(String applicationId,String keyType,String groupId,String accept)
    {
        return delegate.applicationsApplicationIdKeysKeyTypeGetGetLastUpdatedTime(applicationId,keyType,groupId,accept);
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
    @ApiParam(value = "Grant types/Callback URL update request object\n" ,required=true ) @NotNull ApplicationKeyDTO
body)
    {
    return delegate.applicationsApplicationIdKeysKeyTypePut(applicationId,keyType,body);
    }

    public String applicationsApplicationIdKeysKeyTypePutGetLastUpdatedTime(String applicationId,String keyType,ApplicationKeyDTO body)
    {
        return delegate.applicationsApplicationIdKeysKeyTypePutGetLastUpdatedTime(applicationId,keyType,body);
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
    @ApiParam(value = "Application object that needs to be updated\n" ,required=true ) @NotNull ApplicationDTO
body,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header (Will be supported in future).\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsApplicationIdPut(applicationId,body,contentType,ifMatch,ifUnmodifiedSince);
    }

    public String applicationsApplicationIdPutGetLastUpdatedTime(String applicationId,ApplicationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince)
    {
        return delegate.applicationsApplicationIdPutGetLastUpdatedTime(applicationId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/generate-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Generate keys for application\n", notes = "This operation can be used to generate client Id and client secret for an application\n", response = ApplicationKeyDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nKeys are generated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met (Will be supported in future).\n") })

    public Response applicationsGenerateKeysPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true) @QueryParam("applicationId")  String applicationId,
    @ApiParam(value = "Application object the keys of which are to be generated\n" ,required=true ) @NotNull ApplicationKeyGenerateRequestDTO
body,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header (Will be supported in future).\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.applicationsGenerateKeysPost(applicationId,body,contentType,ifMatch,ifUnmodifiedSince);
    }

    public String applicationsGenerateKeysPostGetLastUpdatedTime(String applicationId,ApplicationKeyGenerateRequestDTO body,String contentType,String ifMatch,String ifUnmodifiedSince)
    {
        return delegate.applicationsGenerateKeysPostGetLastUpdatedTime(applicationId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search applications\n", notes = "This operation can be used to retrieve list of applications that is belonged to the user associated with the provided access token.\n", response = ApplicationListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication list returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response applicationsGet(@ApiParam(value = "Application Group Id\n") @QueryParam("groupId")  String groupId,
    @ApiParam(value = "**Search condition**.\n\nYou can search for an application by specifying the name as \"query\" attribute.\n\nEg.\n\"app1\" will match an application if the name is exactly \"app1\".\n\nCurrently this does not support wildcards. Given name must exactly match the application name.\n") @QueryParam("query")  String query,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.applicationsGet(groupId,query,limit,offset,accept,ifNoneMatch);
    }

    public String applicationsGetGetLastUpdatedTime(String groupId,String query,Integer limit,Integer offset,String accept,String ifNoneMatch)
    {
        return delegate.applicationsGetGetLastUpdatedTime(groupId,query,limit,offset,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new application\n", notes = "This operation can be used to create a new application specifying the details of the application in the payload.\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict.\nApplication already exists.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response applicationsPost(@ApiParam(value = "Application object that is to be created.\n" ,required=true ) @NotNull ApplicationDTO
body,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.applicationsPost(body,contentType);
    }

    public String applicationsPostGetLastUpdatedTime(ApplicationDTO body,String contentType)
    {
        return delegate.applicationsPostGetLastUpdatedTime(body,contentType);
    }
    @GET
    @Path("/scopes/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get scopes associated with a particular application based on subscribed APIs\n", notes = "Get scopes associated with a particular application based on subscribed APIs\n", response = ScopeListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nScope returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Un authorized.\nThe user is not authorized to view the application .\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested application does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response applicationsScopesApplicationIdGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Filter user by roles.\n") @QueryParam("filterByUserRoles")  Boolean filterByUserRoles,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource (Will be supported in future).\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.applicationsScopesApplicationIdGet(applicationId,filterByUserRoles,ifNoneMatch,ifModifiedSince);
    }

    public String applicationsScopesApplicationIdGetGetLastUpdatedTime(String applicationId,Boolean filterByUserRoles,String ifNoneMatch,String ifModifiedSince)
    {
        return delegate.applicationsScopesApplicationIdGetGetLastUpdatedTime(applicationId,filterByUserRoles,ifNoneMatch,ifModifiedSince);
    }
}

