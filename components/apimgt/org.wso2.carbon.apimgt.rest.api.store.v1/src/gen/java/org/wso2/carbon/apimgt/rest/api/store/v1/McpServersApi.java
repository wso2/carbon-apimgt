package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PostRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.McpServersApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.McpServersApiServiceImpl;
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
    @Path("/{mcpServerId}/comments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a MCP Server Comment", notes = "", response = CommentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = CommentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response addCommentToMCPServer(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId, @ApiParam(value = "Comment object that should to be added " ,required=true) PostRequestBodyDTO postRequestBodyDTO,  @ApiParam(value = "ID of the perent comment. ")  @QueryParam("replyTo") String replyTo) throws APIManagementException{
        return delegate.addCommentToMCPServer(mcpServerId, postRequestBodyDTO, replyTo, securityContext);
    }

    @PUT
    @Path("/{mcpServerId}/user-rating")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add or Update Logged in User's Rating for a MCP Server", notes = "This operation can be used to add or update a MCP Server rating.  `X-WSO2-Tenant` header can be used to add or update the logged in user rating of a MCP Server that belongs to a  different tenant domain. If not specified super tenant will be used. If Authorization header is present in  the request, the user's tenant associated with the access token will be used. ", response = RatingDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the newly created or updated object as entity in the body. ", response = RatingDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response addMCPServerRating(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId, @ApiParam(value = "Rating object that should to be added " ,required=true) RatingDTO ratingDTO,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.addMCPServerRating(mcpServerId, ratingDTO, xWSO2Tenant, securityContext);
    }

    @DELETE
    @Path("/{mcpServerId}/comments/{commentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a MCP Server Comment", notes = "Remove a Comment ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 405, message = "MethodNotAllowed. Request method is known by the server but is not supported by the target resource. ", response = Void.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteCommentOfMCPServer(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId, @ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.deleteCommentOfMCPServer(mcpServerId, commentId, ifMatch, securityContext);
    }

    @DELETE
    @Path("/{mcpServerId}/user-rating")
    
    
    @ApiOperation(value = "Delete User MCP Server Rating", notes = "This operation can be used to delete logged in user MCP Server rating.  `X-WSO2-Tenant` header can be used to delete the logged in user rating of a MCP Server that belongs to a  different tenant domain. If not specified super tenant will be used. If Authorization header is present in the  request, the user's tenant associated with the access token will be used. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class) })
    public Response deleteMCPServerRating(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.deleteMCPServerRating(mcpServerId, xWSO2Tenant, ifMatch, securityContext);
    }

    @PATCH
    @Path("/{mcpServerId}/comments/{commentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Edit a comment", notes = "Edit the individual comment ", response = CommentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comment updated. ", response = CommentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response editCommentOfMCPServer(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId, @ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId, @ApiParam(value = "Comment object that should to be updated " ,required=true) PatchRequestBodyDTO patchRequestBodyDTO) throws APIManagementException{
        return delegate.editCommentOfMCPServer(mcpServerId, commentId, patchRequestBodyDTO, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/comments")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve MCP Server Comments", notes = "Get a list of Comments that are already added to MCP Servers ", response = CommentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comments list is returned. ", response = CommentListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAllCommentsOfMCPServer(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Whether we need to display commentor details. ", defaultValue="false") @DefaultValue("false") @QueryParam("includeCommenterInfo") Boolean includeCommenterInfo) throws APIManagementException{
        return delegate.getAllCommentsOfMCPServer(mcpServerId, xWSO2Tenant, limit, offset, includeCommenterInfo, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search MCP Servers ", notes = "This operation provides you a list of available MCP servers qualifying under a given search condition.  Each retrieved MCP Server is represented with a minimal amount of attributes. If you want to get complete  details of a MCP server, you need to use **Get details of a MCP** operation.  This operation supports retrieving MCP servers of other tenants. The required tenant domain need to be  specified as a header `X-WSO2-Tenant`. If not specified super tenant's MCP servers will be retrieved. If you  used an Authorization header, the user's tenant associated with the access token will be used.  **NOTE:** * By default, this operation retrieves Published MCP Server.  * This operation does not require an Authorization header by default. But if it is provided, it will be validated and checked for permissions of the user, hence you may be able to see APIs which are restricted for special permissions/roles. ", response = APIListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying MCP Servers is returned. ", response = APIListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAllMCPServers( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"<attribute>:\"** modifier.  Eg. \"provider:wso2\" will match an MCP Server if the provider of the MCP Server is exactly \"wso2\".  Additionally you can use wildcards.  Eg. \"provider:wso2*\" will match a MCP Server if the provider of the MCP Server starts with \"wso2\".  Supported attribute modifiers are [**version, context, status, description, doc, provider, tag**]  To search by Properties provide the query in below format.  **property_name:property_value**  Eg. \"environment:test\" where environment is the property name and test is the propert value.  If no advanced attribute modifier has been specified, search will match the given query string against API Name. ")  @QueryParam("query") String query,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAllMCPServers(limit, offset, xWSO2Tenant, query, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/comments/{commentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of a MCP Server Comment", notes = "Get the individual comment given by a user for a certain MCP Server. ", response = CommentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comment returned. ", response = CommentDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getCommentOfMCPServer(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId, @ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Whether we need to display commentor details. ", defaultValue="false") @DefaultValue("false") @QueryParam("includeCommenterInfo") Boolean includeCommenterInfo,  @ApiParam(value = "Maximum size of replies array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("replyLimit") Integer replyLimit,  @ApiParam(value = "Starting point within the complete list of replies. ", defaultValue="0") @DefaultValue("0") @QueryParam("replyOffset") Integer replyOffset) throws APIManagementException{
        return delegate.getCommentOfMCPServer(mcpServerId, commentId, xWSO2Tenant, ifNoneMatch, includeCommenterInfo, replyLimit, replyOffset, securityContext);
    }

    @GET
    @Path("/{mcpServerId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of a MCP Server ", notes = "Using this operation, you can retrieve complete details of a single MCP Server. You need to provide the Id of  the MCP Server to retrieve it.  `X-WSO2-Tenant` header can be used to retrieve a MCP Server of a different tenant domain. If not specified  super tenant will be used. If Authorization header is present in the request, the user's tenant associated  with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But if it is provided, it will be  validated and checked for permissions of the user, hence you may be able to see APIs which are restricted for special permissions/roles. \\n ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested API is returned ", response = APIDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServer(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServer(mcpServerId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/documents/{documentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Document of a MCP Server ", notes = "This operation can be used to retrieve a particular document's metadata associated with a MCP Server.  `X-WSO2-Tenant` header can be used to retrieve a document of a MCP Server that belongs to a different tenant  domain. If not specified super tenant will be used. If Authorization header is present in the request, the  user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted MCP  Server's document, you need to provide Authorization header. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document returned. ", response = DocumentDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerDocument(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerDocument(mcpServerId, documentId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/documents/{documentId}/content")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the Content of a MCP Server Document ", notes = "This operation can be used to retrieve the content of a MCP Server's document.  The document can be of 3 types. In each cases responses are different.  1. **Inline type**:    The content of the document will be retrieved in `text/plain` content type 2. **FILE type**:    The file will be downloaded with the related content type (eg. `application/pdf`) 3. **URL type**:     The client will recieve the URL of the document as the Location header with the response with - `303 See Other`  `X-WSO2-Tenant` header can be used to retrieve the content of a document of a MCP Server that belongs to a  different tenant domain. If not specified super tenant will be used. If Authorization header is present in the  request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted MCP  Server's document content, you need to provide Authorization header. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. File or inline content returned. ", response = Void.class),
        @ApiResponse(code = 303, message = "See Other. Source can be retrieved from the URL specified at the Location header. ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerDocumentContent(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerDocumentContent(mcpServerId, documentId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/documents")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a List of Documents of a MCP Server ", notes = "This operation can be used to retrieve a list of documents belonging to a MCP Server by providing the id of the  MCP Server.  `X-WSO2-Tenant` header can be used to retrieve documents of a MCP Server that belongs to a different tenant  domain. If not specified super tenant will be used. If Authorization header is present in the request, the  user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted MCP  Server's documents, you need to provide Authorization header. ", response = DocumentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "MCP Server Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document list is returned. ", response = DocumentListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerDocuments(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerDocuments(mcpServerId, limit, offset, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/user-rating")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve MCP Server Rating of User", notes = "This operation can be used to get the user rating of a MCP Server.  `X-WSO2-Tenant` header can be used to retrieve the logged in user rating of a MCP Server that belongs to a  different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = RatingDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Rating returned. ", response = RatingDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client already has the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerRating(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerRating(mcpServerId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/ratings")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve MCP Server Ratings", notes = "This operation can be used to retrieve the list of ratings of a MCP Server.  `X-WSO2-Tenant` header can be used to retrieve ratings of a MCP Server that belongs to a different tenant  domain. If not specified super tenant will be used. If Authorization header is present in the request, the  user's tenant associated with the access token will be used. ", response = RatingListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Rating list returned. ", response = RatingListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerRatings(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.getMCPServerRatings(mcpServerId, limit, offset, xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/subscription-policies")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of the Subscription Throttling Policies of a MCP Server ", notes = "This operation can be used to retrieve details of the subscription throttling policy of a MCP Server by  specifying the MCP Server Id.  `X-WSO2-Tenant` header can be used to retrieve MCP Server subscription throttling policies that belongs to a  different tenant domain. If not specified super tenant will be used. If Authorization header is present in the  request, the user's tenant associated with the access token will be used. ", response = ThrottlingPolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Throttling Policy returned ", response = ThrottlingPolicyDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerSubscriptionPolicies(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerSubscriptionPolicies(mcpServerId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/thumbnail")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Thumbnail Image", notes = "This operation can be used to download a thumbnail image of a MCP Server.  `X-WSO2-Tenant` header can be used to retrieve a thumbnail of a MCP Server that belongs to a different tenant  domain. If not specified super tenant will be used. If Authorization header is present in the request, the  user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted  MCP Server's thumbnail, you need to provide Authorization header. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "MCP Servers",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Thumbnail image returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getMCPServerThumbnail(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getMCPServerThumbnail(mcpServerId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mcpServerId}/comments/{commentId}/replies")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get replies of a comment", notes = "Get replies of a comment ", response = CommentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Comments" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comment returned. ", response = CommentListDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getRepliesOfCommentOfMCPServer(@ApiParam(value = "**MCP Server ID** consisting of the **UUID** of the MCP Server. ",required=true) @PathParam("mcpServerId") String mcpServerId, @ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Whether we need to display commentor details. ", defaultValue="false") @DefaultValue("false") @QueryParam("includeCommenterInfo") Boolean includeCommenterInfo) throws APIManagementException{
        return delegate.getRepliesOfCommentOfMCPServer(mcpServerId, commentId, xWSO2Tenant, limit, offset, ifNoneMatch, includeCommenterInfo, securityContext);
    }
}
