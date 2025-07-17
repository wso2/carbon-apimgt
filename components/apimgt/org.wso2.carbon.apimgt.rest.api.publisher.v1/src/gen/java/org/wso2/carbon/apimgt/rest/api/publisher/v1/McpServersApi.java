package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.McpServersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.McpServersApiServiceImpl;
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
@Path("/mcp-servers")

@Api(description = "the mcp-servers API")




public class McpServersApi  {

  @Context MessageContext securityContext;

McpServersApiService delegate = new McpServersApiServiceImpl();


    @POST
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a New Document to a MCP server", notes = "This operation can be used to add a new documentation to a MCP server. This operation only adds the metadata  of a document. To add the actual content we need to use **Upload the content of an MCP server document ** MCP server once we obtain a document Id by this operation. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created Document object as entity in the body. Location header contains URL of newly added document. ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response addMCPServerDocument(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document object that needs to be added" ,required=true) DocumentDTO documentDTO) throws APIManagementException{
        return delegate.addMCPServerDocument(apiId, documentDTO, securityContext);
    }

    @POST
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Upload the Content of a MCP Server Document", notes = "Thid operation can be used to upload a file or add inline content to a MCP server document.  **IMPORTANT:** * Either **file** or **inlineContent** form data parameters should be specified at one time. * Document's source type should be **FILE** in order to upload a file to the document using **file** parameter. * Document's source type should be **INLINE** in order to add inline content to the document using **inlineContent** parameter. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document updated ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response addMCPServerDocumentContent(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "inlineContent", required = false)  String inlineContent) throws APIManagementException{
        return delegate.addMCPServerDocumentContent(apiId, documentId, ifMatch, fileInputStream, fileDetail, inlineContent, securityContext);
    }

    @POST
    @Path("/change-lifecycle")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Change MCP Server Status", notes = "This operation is used to change the lifecycle of a MCP server. Eg: Publish a MCP server which is in `CREATED`  state. In order to change the lifecycle, we need to provide the lifecycle `action` as a query parameter.  For example, to Publish an MCP server, `action` should be `Publish`. Note that the `Re-publish` action is  available only after calling `Block`.  Some actions supports providing additional paramters which should be provided as `lifecycleChecklist` parameter. Please see parameters table for more information. ", response = WorkflowResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "MCP Server Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle changed successfully. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response changeMCPServerLifecycle( @NotNull @ApiParam(value = "The action to demote or promote the state of the MCP server.  Supported actions are [ **Publish**, **Deploy as a Prototype**, **Demote to Created**, **Block**, **Deprecate**, **Re-Publish**, **Retire** ] ",required=true, allowableValues="Publish, Deploy as a Prototype, Demote to Created, Block, Deprecate, Re-Publish, Retire")  @QueryParam("action") String action,  @NotNull @ApiParam(value = "**API ID** consisting of the **UUID** of the API**. ",required=true)  @QueryParam("apiId") String apiId,  @ApiParam(value = " Supported checklist items are as follows. 1. **Deprecate old versions after publishing the MCP server**: Setting this to true will deprecate older versions of a particular API when it is promoted to Published state from Created state. 2. **Requires re-subscription when publishing the MCP server**: If you set this to true, users need to re  subscribe to the MCP server although they may have subscribed to an older version. You can specify additional checklist items by using an **\"attribute:\"** modifier. Eg: \"Deprecate old versions after publishing the MCP server:true\" will deprecate older versions of a particular MCP server when it is promoted to Published state from Created state. Multiple checklist items can be given in \"attribute1:true, attribute2:false\" format. **Sample CURL :**  curl -k -H \"Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8\" -X POST \"https://localhost:9443/api/am/publisher/v4/apis/change-lifecycle?apiId=890a4f4d-09eb-4877-a323-57f6ce2ed79b&action=Publish&lifecycleChecklist=Deprecate%20old%20versions%20after%20publishing%20the%20API%3Atrue,Requires%20re-subscription%20when%20publishing%20the%20API%3Afalse\" ")  @QueryParam("lifecycleChecklist") String lifecycleChecklist,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.changeMCPServerLifecycle(action, apiId, lifecycleChecklist, ifMatch, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a New MCP Server", notes = "This operation can be used to create a new MCP server specifying the details of the object in the payload. The  new MCP server will be in `CREATED` state.  There is a special capability for a user who has `APIM Admin` permission such that he can create MCP servers on  behalf of other users. For that he can to specify `\"provider\" : \"some_other_user\"` in the payload so that the  creator will be shown as `some_other_user` in the UI. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response createMCPServer(@ApiParam(value = "API object that needs to be added" ,required=true) APIDTO APIDTO,  @ApiParam(value = "Open api version", allowableValues="v2, v3", defaultValue="v3") @DefaultValue("v3") @QueryParam("openAPIVersion") String openAPIVersion) throws APIManagementException{
        return delegate.createMCPServer(APIDTO, openAPIVersion, securityContext);
    }

    @POST
    @Path("/{apiId}/revisions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create API Revision", notes = "Create a new API revision ", response = APIRevisionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "MCP Server Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created APIRevision object as the entity in the body. ", response = APIRevisionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response createMCPServerRevision(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "API object that needs to be added" ) APIRevisionDTO apIRevisionDTO) throws APIManagementException{
        return delegate.createMCPServerRevision(apiId, apIRevisionDTO, securityContext);
    }

    @POST
    @Path("/copy-api")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a New MCP server Version", notes = "This operation can be used to create a new version of an existing MCP server. The new version is specified as  `newVersion` query parameter. New MCP server will be in `CREATED` state. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created MCP server as entity in the body. Location header contains URL of newly created MCP server. ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response createNewMCPServerVersion( @NotNull @Size(max=30) @ApiParam(value = "Version of the new MCP server.",required=true)  @QueryParam("newVersion") String newVersion,  @NotNull @ApiParam(value = "**API ID** consisting of the **UUID** of the API**. ",required=true)  @QueryParam("apiId") String apiId,  @ApiParam(value = "Specifies whether new MCP server should be added as default version.", defaultValue="false") @DefaultValue("false") @QueryParam("defaultVersion") Boolean defaultVersion,  @ApiParam(value = "Version of the Service that will used in creating new version")  @QueryParam("serviceVersion") String serviceVersion) throws APIManagementException{
        return delegate.createNewMCPServerVersion(newVersion, apiId, defaultVersion, serviceVersion, securityContext);
    }

    @DELETE
    @Path("/{apiId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an MCP server API", notes = "This operation can be used to delete an MCP server API by providing the Id of the API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_delete", description = "Delete API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response deleteMCPServer(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.deleteMCPServer(apiId, ifMatch, securityContext);
    }

    @DELETE
    @Path("/{apiId}/documents/{documentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Document of a MCP Server", notes = "This operation can be used to delete a document associated with a MCP server. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response deleteMCPServerDocument(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.deleteMCPServerDocument(apiId, documentId, ifMatch, securityContext);
    }

    @DELETE
    @Path("/{apiId}/revisions/{revisionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete MCP server Revision", notes = "Delete a revision of a MCP server. ", response = APIRevisionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "MCP Server Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of remaining MCP servers revisions are returned. ", response = APIRevisionListDTO.class),
        @ApiResponse(code = 204, message = "No Content. Successfully deleted the revision ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deleteMCPServerRevision(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Revision ID of an API ",required=true) @PathParam("revisionId") String revisionId) throws APIManagementException{
        return delegate.deleteMCPServerRevision(apiId, revisionId, securityContext);
    }

    @POST
    @Path("/{apiId}/deploy-revision")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Deploy Revision", notes = "Deploy a revision ", response = APIRevisionDeploymentDTO.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "MCP Server Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. Successful response with the newly deployed APIRevisionDeployment List object as the entity in the body. ", response = APIRevisionDeploymentDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deployMCPServerRevision(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Revision ID of an API ")  @QueryParam("revisionId") String revisionId, @ApiParam(value = "Deployment object that needs to be added" ) List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO) throws APIManagementException{
        return delegate.deployMCPServerRevision(apiId, revisionId, apIRevisionDeploymentDTO, securityContext);
    }

    @GET
    @Path("/export")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Export a MCP Server", notes = "This operation can be used to export the details of a particular MCP server as a zip file. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Import Export",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Successful. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response exportMCPServer( @ApiParam(value = "UUID of the MCP server")  @QueryParam("apiId") String apiId,  @ApiParam(value = "API Name ")  @QueryParam("name") String name,  @ApiParam(value = "Version of the MCP Server ")  @QueryParam("version") String version,  @ApiParam(value = "Revision number of the API artifact ")  @QueryParam("revisionNumber") String revisionNumber,  @ApiParam(value = "Provider name of the MCP Server ")  @QueryParam("providerName") String providerName,  @ApiParam(value = "Format of output documents. Can be YAML or JSON. ", allowableValues="JSON, YAML")  @QueryParam("format") String format,  @ApiParam(value = "Preserve MCP Server Status during export ")  @QueryParam("preserveStatus") Boolean preserveStatus,  @ApiParam(value = "Export the latest revision of the MCP server ", defaultValue="false") @DefaultValue("false") @QueryParam("latestRevision") Boolean latestRevision,  @ApiParam(value = "Gateway environment of the exported MCP servers ")  @QueryParam("gatewayEnvironment") String gatewayEnvironment,  @ApiParam(value = "Preserve endpoint configuration credentials ")  @QueryParam("preserveCredentials") Boolean preserveCredentials) throws APIManagementException{
        return delegate.exportMCPServer(apiId, name, version, revisionNumber, providerName, format, preserveStatus, latestRevision, gatewayEnvironment, preserveCredentials, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search MCP servers ", notes = "This operation provides you a list of available MCP servers qualifying under a given search condition.  Each retrieved API is represented with a minimal amount of attributes. If you want to get complete details,  you need to use **Get details of an MCP server** operation. ", response = APIListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations"),
            @AuthorizationScope(scope = "apim:api_list_view", description = "View, Retrieve API list")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying APIs is returned. ", response = APIListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAllMCPServers( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"<attribute>:\"** modifier.  Eg. \"provider:wso2\" will match an MCP server if the provider of the API contains \"wso2\". \"provider:\"wso2\"\" will match an API if the provider of the API is exactly \"wso2\". \"status:PUBLISHED\" will match an API if the API is in PUBLISHED state.  Also you can use combined modifiers Eg. name:pizzashack version:v1 will match an API if the name of the API is pizzashack and version is v1.  Supported attribute modifiers are [**version, context, name, status, description, provider, api-category, tags, doc, contexttemplate, lcstate, content, type, label, enablestore, thirdparty**]  If no advanced attribute modifier has been specified,  the API names containing the search term will be returned as a result.  Please note that you need to use encoded URL (URL encoding) if you are using a client which does not  support URL encoding (such as curl) ")  @QueryParam("query") String query,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.getAllMCPServers(limit, offset, xWSO2Tenant, query, ifNoneMatch, accept, securityContext);
    }

    @GET
    @Path("/{apiId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of an MCP Server", notes = "Using this operation, you can retrieve complete details of a single API. You need to provide the Id of the API  to retrieve it. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations"),
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested API is returned ", response = APIDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServer(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServer(apiId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/documents/{documentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Document of a MCP Server", notes = "This operation can be used to retrieve a particular document's metadata associated with a MCP server. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document returned. ", response = DocumentDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerDocument(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerDocument(apiId, documentId, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/documents/{documentId}/content")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the Content of an MCP Server Document", notes = "This operation can be used to retrive the content of an MCP server's document.  The document can be of 3 types. In each cases responses are different.  1. **Inline type**:    The content of the document will be retrieved in `text/plain` content type     _Sample cURL_ : `curl -k -H \"Authorization:Bearer 579f0af4-37be-35c7-81a4-f1f1e9ee7c51\" -F  inlineContent=@\"docs.txt\" -X POST \"https://localhost:9443/api/am/publisher/v4/mcp-servers/995a4972-3178-4b17-a374-756e0e19127c/documents/43c2bcce-60e7-405f-bc36-e39c0c5e189e/content` 2. **FILE type**:    The file will be downloaded with the related content type (eg. `application/pdf`) 3. **URL type**:     The client will recieve the URL of the document as the Location header with the response with - `303 See Other` ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. File or inline content returned. ", response = Void.class),
        @ApiResponse(code = 303, message = "See Other. Source can be retrived from the URL specified at the Location header. ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerDocumentContent(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerDocumentContent(apiId, documentId, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/documents")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a List of Documents of a MCP Server", notes = "This operation can be used to retrive a list of documents belonging to a MCP server by providing the ID of  the MCP server. ", response = DocumentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document list is returned. ", response = DocumentListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerDocuments(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerDocuments(apiId, limit, offset, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/endpoints/{endpointId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a backend details of an MCP server", notes = "This operation can be used to get an endpoint of an API by UUID. ", response = BackendEndpointDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "MCP Server Endpoints",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API Endpoint object is returned. ", response = BackendEndpointDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getMCPServerEndpoint(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**. ",required=true) @PathParam("endpointId") String endpointId) throws APIManagementException{
        return delegate.getMCPServerEndpoint(apiId, endpointId, securityContext);
    }

    @GET
    @Path("/{apiId}/endpoints")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a list of backend details of an MCP server", notes = "This operation can be used to get a list of backend endpoints of a MCP server by the API UUID. ", response = BackendEndpointListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "MCP Server Endpoints",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. A list of BackendEndpoint objects are returned. ", response = BackendEndpointListDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getMCPServerEndpoints(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.getMCPServerEndpoints(apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/lifecycle-history")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Lifecycle State Change History of the MCP Server.", notes = "This operation can be used to retrieve Lifecycle state change history of the MCP server. ", response = LifecycleHistoryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle state change history returned successfully. ", response = LifecycleHistoryDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getMCPServerLifecycleHistory(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerLifecycleHistory(apiId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/lifecycle-state")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Lifecycle State Data of the MCP server.", notes = "This operation can be used to retrieve Lifecycle state data of the MCP server. ", response = LifecycleStateDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle state data returned successfully. ", response = LifecycleStateDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response getMCPServerLifecycleState(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerLifecycleState(apiId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/revisions/{revisionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve revision of a MCP server.", notes = "Retrieve a revision of a MCP server ", response = APIRevisionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "MCP Server Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. A MCP server revision is returned. ", response = APIRevisionDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getMCPServerRevision(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Revision ID of an API ",required=true) @PathParam("revisionId") String revisionId) throws APIManagementException{
        return delegate.getMCPServerRevision(apiId, revisionId, securityContext);
    }

    @GET
    @Path("/{apiId}/revisions")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List Revisions", notes = "List available revisions of a MCP server. ", response = APIRevisionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "MCP Server Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of MCP server revisions are returned. ", response = APIRevisionListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getMCPServerRevisions(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.getMCPServerRevisions(apiId, query, securityContext);
    }

    @GET
    @Path("/{apiId}/subscription-policies")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of the Subscription Throttling Policies of a MCP Server ", notes = "This operation can be used to retrieve details of the subscription throttling policy of a MCP server by  specifying the API Id.  `X-WSO2-Tenant` header can be used to retrive MCP server subscription throttling policies that belongs to a  different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = ThrottlingPolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Throttling Policy returned ", response = ThrottlingPolicyDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerSubscriptionPolicies(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Indicates the quota policy type to be AI API quota or not. ", defaultValue="false") @DefaultValue("false") @QueryParam("isAiApi") Boolean isAiApi,  @ApiParam(value = "Indicates the organization ID ")  @QueryParam("organizationID") String organizationID) throws APIManagementException{
        return delegate.getMCPServerSubscriptionPolicies(apiId, xWSO2Tenant, ifNoneMatch, isAiApi, organizationID, securityContext);
    }

    @GET
    @Path("/{apiId}/swagger")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Swagger Definition", notes = "This operation can be used to retrieve the swagger definition of a MCP server. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the MCP server is returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerSwagger(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerSwagger(apiId, accept, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/import")
    @Consumes({ "multipart/form-data" })
    @Produces({ "text/plain", "application/json" })
    @ApiOperation(value = "Import a MCP server", notes = "This operation can be used to import a MCP server. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Import Export",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. API Imported Successfully. ", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response importMCPServer( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "Preserve Original Provider of the MCP server. This is the user choice to keep or replace the MCP server  provider ")  @QueryParam("preserveProvider") Boolean preserveProvider,  @ApiParam(value = "Once the revision max limit reached, undeploy and delete the earliest revision and create a new revision ")  @QueryParam("rotateRevision") Boolean rotateRevision,  @ApiParam(value = "Whether to update the MCP server or not. This is used when updating already existing MCP servers ")  @QueryParam("overwrite") Boolean overwrite,  @ApiParam(value = "Preserve Portal Configurations. This is used to preserve the portal configurations of the MCP server ")  @QueryParam("preservePortalConfigurations") Boolean preservePortalConfigurations,  @ApiParam(value = "Dry Run. This is used to validate the MCP server without importing it ", defaultValue="false") @DefaultValue("false") @QueryParam("dryRun") Boolean dryRun,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.importMCPServer(fileInputStream, fileDetail, preserveProvider, rotateRevision, overwrite, preservePortalConfigurations, dryRun, accept, securityContext);
    }

    @POST
    @Path("/import-mcp-server")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create an MCP server using the backend API's OpenAPI definition", notes = "This operation can be used to create a MCP server using the backend API's OpenAPI definition. Provide  either `url` or `file` to specify the definition.  Specify additionalProperties with **at least** API's name, version, context and endpointConfig. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response importMCPServerDefinition( @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "url", required = false)  String url, @Multipart(value = "additionalProperties", required = false)  String additionalProperties) throws APIManagementException{
        return delegate.importMCPServerDefinition(fileInputStream, fileDetail, url, additionalProperties, securityContext);
    }

    @POST
    @Path("/{apiId}/restore-revision")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Restore MCP server revision", notes = "Restore a revision to the current MCP server ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "MCP Server Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Restored. Successful response with the newly restored API object as the entity in the body. ", response = APIDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response restoreMCPServerRevision(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Revision ID of an API ")  @QueryParam("revisionId") String revisionId) throws APIManagementException{
        return delegate.restoreMCPServerRevision(apiId, revisionId, securityContext);
    }

    @POST
    @Path("/{apiId}/undeploy-revision")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "UnDeploy Revision", notes = "UnDeploy a revision ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "MCP Server Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = Void.class),
        @ApiResponse(code = 201, message = "Created. Successful response with the newly undeployed APIRevisionDeploymentList object as the entity in the body. ", response = APIRevisionDeploymentDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response undeployMCPServerRevision(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Revision ID of an API ")  @QueryParam("revisionId") String revisionId,  @ApiParam(value = "Revision Number of an API ")  @QueryParam("revisionNumber") String revisionNumber,  @ApiParam(value = "", defaultValue="false") @DefaultValue("false") @QueryParam("allEnvironments") Boolean allEnvironments, @ApiParam(value = "Deployment object that needs to be added" ) List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO) throws APIManagementException{
        return delegate.undeployMCPServerRevision(apiId, revisionId, revisionNumber, allEnvironments, apIRevisionDeploymentDTO, securityContext);
    }

    @PUT
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an API", notes = "This operation can be used to update an existing MCP Server. But the properties `name`, `version`, `context`, `provider`, `state` will not be changed by this operation. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated API object ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response updateMCPServer(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "API object that needs to be added" ,required=true) APIDTO APIDTO,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.updateMCPServer(apiId, APIDTO, ifMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Document of a MCP Server", notes = "This operation can be used to update metadata of an MCP server's document. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document updated ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response updateMCPServerDocument(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId, @ApiParam(value = "Document object that needs to be added" ,required=true) DocumentDTO documentDTO,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.updateMCPServerDocument(apiId, documentId, documentDTO, ifMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/endpoints/{endpointId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a backend details of an MCP server", notes = "This operation can be used to update a API endpoint. ", response = BackendEndpointDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "API Endpoints",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Updated API Endpoint is returned. ", response = BackendEndpointDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response updateMCPServerEndpoint(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**. ",required=true) @PathParam("endpointId") String endpointId, @ApiParam(value = "API Endpoint object with updated details" ) BackendEndpointDTO backendEndpointDTO) throws APIManagementException{
        return delegate.updateMCPServerEndpoint(apiId, endpointId, backendEndpointDTO, securityContext);
    }

    @POST
    @Path("/validate")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Check Given API Context Name already Exists", notes = "Using this operation, you can check a given API context is already used. You need to provide the context name you want to check. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API definition validation information is returned ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response validateMCPServer( @NotNull @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"<attribute>:\"** modifier.  Eg. \"name:wso2\" will match an MCP server if the provider of the API is exactly \"wso2\".  Supported attribute modifiers are [** version, context, name **]  If no advanced attribute modifier has been specified, search will match the given query string against MCP server Name. ",required=true)  @QueryParam("query") String query,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.validateMCPServer(query, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/validate-endpoint")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Check Whether Given Endpoint URL is Valid", notes = "Using this operation, it is possible check whether the given MCP server url is a valid url ", response = ApiEndpointValidationResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API definition validation information is returned ", response = ApiEndpointValidationResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response validateMCPServerEndpoint( @NotNull @ApiParam(value = "API endpoint url",required=true)  @QueryParam("endpointUrl") String endpointUrl,  @ApiParam(value = "API ID consisting of the UUID of the MCP server")  @QueryParam("apiId") String apiId) throws APIManagementException{
        return delegate.validateMCPServerEndpoint(endpointUrl, apiId, securityContext);
    }

    @POST
    @Path("/validate-openapi")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Validate an OpenAPI Definition", notes = "This operation can be used to validate an OpenAPI definition and retrieve a summary. Provide either `url` or `file` to specify the definition. ", response = OpenAPIDefinitionValidationResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API definition validation information is returned ", response = OpenAPIDefinitionValidationResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response validateOpenAPIDefinitionOfMCPServer( @ApiParam(value = "Specify whether to return the full content of the OpenAPI definition in the response. This is only applicable when using url based validation ", defaultValue="false") @DefaultValue("false") @QueryParam("returnContent") Boolean returnContent, @Multipart(value = "url", required = false)  String url,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "inlineAPIDefinition", required = false)  String inlineAPIDefinition) throws APIManagementException{
        return delegate.validateOpenAPIDefinitionOfMCPServer(returnContent, url, fileInputStream, fileDetail, inlineAPIDefinition, securityContext);
    }
}
