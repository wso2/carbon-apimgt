package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import java.util.List;
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
    }, tags={ "API Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created APIRevision object as the entity in the body. ", response = APIRevisionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response createMCPServerRevision(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "API object that needs to be added" ) APIRevisionDTO apIRevisionDTO) throws APIManagementException{
        return delegate.createMCPServerRevision(apiId, apIRevisionDTO, securityContext);
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
    }, tags={ "MCPServer",  })
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
    @Path("/{apiId}/revisions/{revisionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete MCP server Revision", notes = "Delete a revision of a MCP server. ", response = APIRevisionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "API Revisions",  })
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
    }, tags={ "API Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. Successful response with the newly deployed APIRevisionDeployment List object as the entity in the body. ", response = APIRevisionDeploymentDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deployMCPServerRevision(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Revision ID of an API ")  @QueryParam("revisionId") String revisionId, @ApiParam(value = "Deployment object that needs to be added" ) List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO) throws APIManagementException{
        return delegate.deployMCPServerRevision(apiId, revisionId, apIRevisionDeploymentDTO, securityContext);
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
    }, tags={ "APIs",  })
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
    }, tags={ "MCPServer",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested API is returned ", response = APIDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServer(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServer(apiId, xWSO2Tenant, ifNoneMatch, securityContext);
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
    }, tags={ "API Endpoints",  })
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
    }, tags={ "API Endpoints",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. A list of BackendEndpoint objects are returned. ", response = BackendEndpointListDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getMCPServerEndpoints(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.getMCPServerEndpoints(apiId, securityContext);
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
    }, tags={ "API Revisions",  })
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
    }, tags={ "API Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of MCP server revisions are returned. ", response = APIRevisionListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getMCPServerRevisions(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.getMCPServerRevisions(apiId, query, securityContext);
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
    }, tags={ "APIs",  })
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
    }, tags={ "API Revisions",  })
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
    }, tags={ "API Revisions",  })
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
    }, tags={ "APIs",  })
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
    }, tags={ "API Endpoints" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Updated API Endpoint is returned. ", response = BackendEndpointDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response updateMCPServerEndpoint(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "**Endpoint ID** consisting of the **UUID** of the Endpoint**. ",required=true) @PathParam("endpointId") String endpointId, @ApiParam(value = "API Endpoint object with updated details" ) APIEndpointDTO apIEndpointDTO) throws APIManagementException{
        return delegate.updateMCPServerEndpoint(apiId, endpointId, apIEndpointDTO, securityContext);
    }
}
