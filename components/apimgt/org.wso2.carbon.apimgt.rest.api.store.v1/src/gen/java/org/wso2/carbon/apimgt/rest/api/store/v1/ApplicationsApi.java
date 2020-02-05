package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyRevokeRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyReGenerateResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ApplicationsApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/applications")

@Api(description = "the applications API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApplicationsApi  {

  @Context MessageContext securityContext;

ApplicationsApiService delegate = new ApplicationsApiServiceImpl();


    @POST
    @Path("/{applicationId}/api-keys/{keyType}/generate")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Generate API Key", notes = "Generate a self contained API Key for the application ", response = APIKeyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:api_key", description = "Generate API Keys"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "API Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. apikey generated. ", response = APIKeyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdApiKeysKeyTypeGeneratePost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox). ",required=true, allowableValues="PRODUCTION, SANDBOX") @PathParam("keyType") String keyType, @ApiParam(value = "API Key generation request object " ) APIKeyGenerateRequestDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.applicationsApplicationIdApiKeysKeyTypeGeneratePost(applicationId, keyType, body, ifMatch, securityContext);
    }

    @POST
    @Path("/{applicationId}/api-keys/{keyType}/revoke")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Revoke API Key", notes = "Revoke a self contained API Key for the application ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:api_key", description = "Generate API Keys"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "API Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. apikey revoked successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdApiKeysKeyTypeRevokePost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox). ",required=true, allowableValues="PRODUCTION, SANDBOX") @PathParam("keyType") String keyType, @ApiParam(value = "API Key revoke request object " ) APIKeyRevokeRequestDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.applicationsApplicationIdApiKeysKeyTypeRevokePost(applicationId, keyType, body, ifMatch, securityContext);
    }

    @DELETE
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Remove an application ", notes = "This operation can be used to remove an application specifying its id. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Applications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdDelete(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.applicationsApplicationIdDelete(applicationId, ifMatch, securityContext);
    }

    @POST
    @Path("/{applicationId}/generate-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Generate application keys", notes = "Generate keys (Consumer key/secret) for application ", response = ApplicationKeyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Keys are generated. ", response = ApplicationKeyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdGenerateKeysPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "Application key generation request object " ,required=true) ApplicationKeyGenerateRequestDTO body) throws APIManagementException{
        return delegate.applicationsApplicationIdGenerateKeysPost(applicationId, body, securityContext);
    }

    @GET
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of an application ", notes = "This operation can be used to retrieve details of an individual application specifying the application id in the URI. ", response = ApplicationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Applications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application returned. ", response = ApplicationDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested application does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.applicationsApplicationIdGet(applicationId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{applicationId}/keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve all application keys", notes = "Retrieve keys (Consumer key/secret) of application ", response = ApplicationKeyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Keys are returned. ", response = ApplicationKeyListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdKeysGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId) throws APIManagementException{
        return delegate.applicationsApplicationIdKeysGet(applicationId, securityContext);
    }

    @POST
    @Path("/{applicationId}/keys/{keyType}/clean-up")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Clean up application keys", notes = "Clean up keys after failed key generation of an application ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Clean up is performed ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdKeysKeyTypeCleanUpPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox). ",required=true, allowableValues="PRODUCTION, SANDBOX") @PathParam("keyType") String keyType, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.applicationsApplicationIdKeysKeyTypeCleanUpPost(applicationId, keyType, ifMatch, securityContext);
    }

    @POST
    @Path("/{applicationId}/keys/{keyType}/generate-token")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Generate application token", notes = "Generate an access token for application by client_credentials grant type ", response = ApplicationTokenDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application Tokens",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Token is generated. ", response = ApplicationTokenDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdKeysKeyTypeGenerateTokenPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox). ",required=true, allowableValues="PRODUCTION, SANDBOX") @PathParam("keyType") String keyType, @ApiParam(value = "Application token generation request object " ,required=true) ApplicationTokenGenerateRequestDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.applicationsApplicationIdKeysKeyTypeGenerateTokenPost(applicationId, keyType, body, ifMatch, securityContext);
    }

    @GET
    @Path("/{applicationId}/keys/{keyType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get key details of a given type ", notes = "This operation can be used to retrieve key details of an individual application specifying the key type in the URI. ", response = ApplicationKeyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Keys of given type are returned. ", response = ApplicationKeyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdKeysKeyTypeGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox). ",required=true, allowableValues="PRODUCTION, SANDBOX") @PathParam("keyType") String keyType,  @ApiParam(value = "Application Group Id ")  @QueryParam("groupId") String groupId) throws APIManagementException{
        return delegate.applicationsApplicationIdKeysKeyTypeGet(applicationId, keyType, groupId, securityContext);
    }

    @PUT
    @Path("/{applicationId}/keys/{keyType}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update grant types and callback url of an application ", notes = "This operation can be used to update grant types and callback url of an application. (Consumer Key and Consumer Secret are ignored) Upon succesfull you will retrieve the updated key details as the response. ", response = ApplicationKeyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. Grant types or/and callback url is/are updated. ", response = ApplicationKeyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdKeysKeyTypePut(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox). ",required=true, allowableValues="PRODUCTION, SANDBOX") @PathParam("keyType") String keyType, @ApiParam(value = "Grant types/Callback URL update request object " ,required=true) ApplicationKeyDTO body) throws APIManagementException{
        return delegate.applicationsApplicationIdKeysKeyTypePut(applicationId, keyType, body, securityContext);
    }

    @POST
    @Path("/{applicationId}/keys/{keyType}/regenerate-secret")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Re-generate consumer secret ", notes = "This operation can be used to re generate consumer secret for an application for the give key type ", response = ApplicationKeyReGenerateResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Keys are re generated. ", response = ApplicationKeyReGenerateResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met (Will be supported in future). ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "**Application Key Type** standing for the type of the keys (i.e. Production or Sandbox). ",required=true, allowableValues="PRODUCTION, SANDBOX") @PathParam("keyType") String keyType) throws APIManagementException{
        return delegate.applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(applicationId, keyType, securityContext);
    }

    @POST
    @Path("/{applicationId}/map-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Map application keys", notes = "Map keys (Consumer key/secret) to an application ", response = ApplicationKeyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application Keys",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Keys are mapped. ", response = ApplicationKeyDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdMapKeysPost(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "Application key mapping request object " ,required=true) ApplicationKeyMappingRequestDTO body) throws APIManagementException{
        return delegate.applicationsApplicationIdMapKeysPost(applicationId, body, securityContext);
    }

    @PUT
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an application ", notes = "This operation can be used to update an application. Upon succesfull you will retrieve the updated application as the response. ", response = ApplicationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Applications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application updated. ", response = ApplicationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdPut(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "Application object that needs to be updated " ,required=true) ApplicationDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.applicationsApplicationIdPut(applicationId, body, ifMatch, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search applications ", notes = "This operation can be used to retrieve list of applications that is belonged to the user associated with the provided access token. ", response = ApplicationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Applications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application list returned. ", response = ApplicationListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ErrorDTO.class) })
    public Response applicationsGet( @ApiParam(value = "Application Group Id ")  @QueryParam("groupId") String groupId,  @ApiParam(value = "**Search condition**.  You can search for an application by specifying the name as \"query\" attribute.  Eg. \"app1\" will match an application if the name is exactly \"app1\".  Currently this does not support wildcards. Given name must exactly match the application name. ")  @QueryParam("query") String query,  @ApiParam(value = "", allowableValues="name, throttlingPolicy, status")  @QueryParam("sortBy") String sortBy,  @ApiParam(value = "", allowableValues="asc, desc")  @QueryParam("sortOrder") String sortOrder,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.applicationsGet(groupId, query, sortBy, sortOrder, limit, offset, ifNoneMatch, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new application ", notes = "This operation can be used to create a new application specifying the details of the application in the payload. ", response = ApplicationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_manage", description = "Retrieve, Manage applications"),
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Applications" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = ApplicationDTO.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Application already exists. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response applicationsPost(@ApiParam(value = "Application object that is to be created. " ,required=true) ApplicationDTO body) throws APIManagementException{
        return delegate.applicationsPost(body, securityContext);
    }
}
