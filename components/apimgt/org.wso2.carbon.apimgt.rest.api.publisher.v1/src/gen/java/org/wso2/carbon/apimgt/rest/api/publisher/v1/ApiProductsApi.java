package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductOutdatedStatusDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ApiProductsApiServiceImpl;
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
@Path("/api-products")

@Api(description = "the api-products API")




public class ApiProductsApi  {

  @Context MessageContext securityContext;

ApiProductsApiService delegate = new ApiProductsApiServiceImpl();


    @POST
    @Path("/{apiProductId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a New Document to an API Product", notes = "This operation can be used to add a new documentation to an API Product. This operation only adds the metadata of a document. To add the actual content we need to use **Upload the content of an API Product document ** API once we obtain a document Id by this operation. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created Document object as entity in the body. Location header contains URL of newly added document. ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response addAPIProductDocument(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Document object that needs to be added" ,required=true) DocumentDTO documentDTO) throws APIManagementException{
        return delegate.addAPIProductDocument(apiProductId, documentDTO, securityContext);
    }

    @POST
    @Path("/{apiProductId}/documents/{documentId}/content")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Upload the Content of an API Product Document", notes = "Thid operation can be used to upload a file or add inline content to an API Product document.  **IMPORTANT:** * Either **file** or **inlineContent** form data parameters should be specified at one time. * Document's source type should be **FILE** in order to upload a file to the document using **file** parameter. * Document's source type should be **INLINE** in order to add inline content to the document using **inlineContent** parameter. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document updated ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response addAPIProductDocumentContent(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "inlineContent", required = false)  String inlineContent) throws APIManagementException{
        return delegate.addAPIProductDocumentContent(apiProductId, documentId, ifMatch, fileInputStream, fileDetail, inlineContent, securityContext);
    }

    @POST
    @Path("/change-lifecycle")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Change API Product LC Status", notes = "This operation is used to change the lifecycle of an API Product. Eg: Publish an API Product which is in `CREATED` state. In order to change the lifecycle, we need to provide the lifecycle `action` as a query parameter.  For example, to Publish an API Product, `action` should be `Publish`. Note that the `Re-publish` action is available only after calling `Block`.  Some actions supports providing additional paramters which should be provided as `lifecycleChecklist` parameter. Please see parameters table for more information. ", response = WorkflowResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations")
        })
    }, tags={ "API Product Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle changed successfully. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response changeAPIProductLifecycle( @NotNull @ApiParam(value = "The action to demote or promote the state of the API Product.  Supported actions are [ **Publish**, **Deploy as a Prototype**, **Demote to Created**, **Block**, **Deprecate**, **Re-Publish**, **Retire** ] ",required=true, allowableValues="Publish, Deploy as a Prototype, Demote to Created, Block, Deprecate, Re-Publish, Retire")  @QueryParam("action") String action,  @NotNull @ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. The combination of the provider, name and the version of the API Product is also accepted as a valid API Product ID. Should be formatted as **provider-name-version**. ",required=true)  @QueryParam("apiProductId") String apiProductId,  @ApiParam(value = "Supported checklist items are as follows. 1. **Deprecate old versions after publishing the API**: Setting this to true will deprecate older versions of a particular API Product when it is promoted to Published state from Created state. 2. **Requires re-subscription when publishing the API**: If you set this to true, users need to re subscribe to the API Products although they may have subscribed to an older version. You can specify additional checklist items by using an **\"attribute:\"** modifier. Eg: \"Deprecate old versions after publishing the API:true\" will deprecate older versions of a particular API Product when it is promoted to Published state from Created state. Multiple checklist items can be given in \"attribute1:true, attribute2:false\" format. **Sample CURL :**  curl -k -H \"Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8\" -X POST \"https://localhost:9443/api/am/publisher/v3/api-products/change-lifecycle?apiId=890a4f4d-09eb-4877-a323-57f6ce2ed79b&action=Publish&lifecycleChecklist=Deprecate%20old%20versions%20after%20publishing%20the%20API%3Atrue,Requires%20re-subscription%20when%20publishing%20the%20API%3Afalse\" ")  @QueryParam("lifecycleChecklist") String lifecycleChecklist,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.changeAPIProductLifecycle(action, apiProductId, lifecycleChecklist, ifMatch, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a New API Product", notes = "This operation can be used to create a new API Product specifying the details of the API Product in the payload. ", response = APIProductDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "'Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity.' ", response = APIProductDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response createAPIProduct(@ApiParam(value = "API object that needs to be added" ,required=true) APIProductDTO apIProductDTO) throws APIManagementException{
        return delegate.createAPIProduct(apIProductDTO, securityContext);
    }

    @POST
    @Path("/{apiProductId}/revisions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new API Product revision", notes = "Create a new API Product revision ", response = APIRevisionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created APIRevision object as the entity in the body. ", response = APIRevisionDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response createAPIProductRevision(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "API Product object that needs to be added" ) APIRevisionDTO apIRevisionDTO) throws APIManagementException{
        return delegate.createAPIProductRevision(apiProductId, apIRevisionDTO, securityContext);
    }

    @DELETE
    @Path("/{apiProductId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an API Product", notes = "This operation can be used to delete an existing API Product proving the Id of the API Product. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations")
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response deleteAPIProduct(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.deleteAPIProduct(apiProductId, ifMatch, securityContext);
    }

    @DELETE
    @Path("/{apiProductId}/documents/{documentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Document of an API Product", notes = "This operation can be used to delete a document associated with an API Product. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response deleteAPIProductDocument(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.deleteAPIProductDocument(apiProductId, documentId, ifMatch, securityContext);
    }

    @DELETE
    @Path("/{apiProductId}/lifecycle-state/pending-tasks")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete Pending Lifecycle State Change Tasks", notes = "This operation can be used to remove pending lifecycle state change requests that are in pending state ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle state change pending task removed successfully. ", response = Void.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteAPIProductLifecycleStatePendingTasks(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId) throws APIManagementException{
        return delegate.deleteAPIProductLifecycleStatePendingTasks(apiProductId, securityContext);
    }

    @DELETE
    @Path("/{apiProductId}/revisions/{revisionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete Revision", notes = "Delete a revision of an API Product ", response = APIRevisionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of remaining API revisions are returned. ", response = APIRevisionListDTO.class),
        @ApiResponse(code = 204, message = "No Content. Successfully deleted the revision ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deleteAPIProductRevision(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Revision ID of an API ",required=true) @PathParam("revisionId") String revisionId) throws APIManagementException{
        return delegate.deleteAPIProductRevision(apiProductId, revisionId, securityContext);
    }

    @POST
    @Path("/{apiProductId}/deploy-revision")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Deploy Revision", notes = "Deploy an API Product Revision ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = Void.class),
        @ApiResponse(code = 201, message = "Created. Successful response with the newly deployed APIRevisionDeployment List object as the entity in the body. ", response = APIRevisionDeploymentDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deployAPIProductRevision(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Revision ID of an API ")  @QueryParam("revisionId") String revisionId, @ApiParam(value = "Deployment object that needs to be added" ) List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO) throws APIManagementException{
        return delegate.deployAPIProductRevision(apiProductId, revisionId, apIRevisionDeploymentDTO, securityContext);
    }

    @GET
    @Path("/export")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Export an API Product", notes = "This operation can be used to export the details of a particular API Product as a zip file. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations")
        })
    }, tags={ "Import Export",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Successful. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response exportAPIProduct( @ApiParam(value = "API Product Name ")  @QueryParam("name") String name,  @ApiParam(value = "Version of the API Product ")  @QueryParam("version") String version,  @ApiParam(value = "Provider name of the API Product ")  @QueryParam("providerName") String providerName,  @ApiParam(value = "Revision number of the API Product ")  @QueryParam("revisionNumber") String revisionNumber,  @ApiParam(value = "Format of output documents. Can be YAML or JSON. ", allowableValues="JSON, YAML")  @QueryParam("format") String format,  @ApiParam(value = "Preserve API Product Status on export ")  @QueryParam("preserveStatus") Boolean preserveStatus,  @ApiParam(value = "Export the latest revision of the API Product ", defaultValue="false") @DefaultValue("false") @QueryParam("latestRevision") Boolean latestRevision) throws APIManagementException{
        return delegate.exportAPIProduct(name, version, providerName, revisionNumber, format, preserveStatus, latestRevision, securityContext);
    }

    @GET
    @Path("/{apiProductId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of an API Product", notes = "Using this operation, you can retrieve complete details of a single API Product. You need to provide the Id of the API to retrive it. ", response = APIProductDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations")
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested API Product is returned ", response = APIProductDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAPIProduct(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAPIProduct(apiProductId, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/documents/{documentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Document of an API Product", notes = "This operation can be used to retrieve a particular document's metadata associated with an API. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document returned. ", response = DocumentDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAPIProductDocument(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAPIProductDocument(apiProductId, documentId, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/documents/{documentId}/content")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the Content of an API Product Document", notes = "This operation can be used to retrive the content of an API's document.  The document can be of 3 types. In each cases responses are different.  1. **Inline type**:    The content of the document will be retrieved in `text/plain` content type     _Sample cURL_ : `curl -k -H \"Authorization:Bearer 579f0af4-37be-35c7-81a4-f1f1e9ee7c51\" -F inlineContent=@\"docs.txt\" -X POST \"https://localhost:9443/api/am/publisher/v3/apis/995a4972-3178-4b17-a374-756e0e19127c/documents/43c2bcce-60e7-405f-bc36-e39c0c5e189e/content` 2. **FILE type**:    The file will be downloaded with the related content type (eg. `application/pdf`) 3. **URL type**:     The client will recieve the URL of the document as the Location header with the response with - `303 See Other` ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. File or inline content returned. ", response = Void.class),
        @ApiResponse(code = 303, message = "See Other. Source can be retrived from the URL specified at the Location header. ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAPIProductDocumentContent(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAPIProductDocumentContent(apiProductId, documentId, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/documents")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a List of Documents of an API Product", notes = "This operation can be used to retrive a list of documents belonging to an API Product by providing the ID of the API Product. ", response = DocumentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document list is returned. ", response = DocumentListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAPIProductDocuments(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAPIProductDocuments(apiProductId, limit, offset, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/lifecycle-history")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Lifecycle State Change History of the API Products.", notes = "This operation can be used to retrieve Lifecycle state change history of the API Products. ", response = LifecycleHistoryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle state change history returned successfully. ", response = LifecycleHistoryDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAPIProductLifecycleHistory(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAPIProductLifecycleHistory(apiProductId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/lifecycle-state")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Lifecycle State Data of the API Product.", notes = "This operation can be used to retrieve Lifecycle state data of the API Product. ", response = LifecycleStateDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle state data returned successfully. ", response = LifecycleStateDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAPIProductLifecycleState(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAPIProductLifecycleState(apiProductId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/revisions/{revisionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve Revision", notes = "Retrieve a revision of an API Product (This resource is not supported at the moment) ", response = APIRevisionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. An API revision is returned. ", response = APIRevisionDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getAPIProductRevision(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Revision ID of an API ",required=true) @PathParam("revisionId") String revisionId) throws APIManagementException{
        return delegate.getAPIProductRevision(apiProductId, revisionId, securityContext);
    }

    @GET
    @Path("/{apiProductId}/deployments")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List Deployments", notes = "List available deployed revision deployment details of an API Product ", response = APIRevisionDeploymentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of deployed revision deployment details are returned. ", response = APIRevisionDeploymentListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getAPIProductRevisionDeployments(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId) throws APIManagementException{
        return delegate.getAPIProductRevisionDeployments(apiProductId, securityContext);
    }

    @GET
    @Path("/{apiProductId}/revisions")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List Revisions", notes = "List available revisions of an API Product ", response = APIRevisionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of API Product revisions are returned. ", response = APIRevisionListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getAPIProductRevisions(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.getAPIProductRevisions(apiProductId, query, securityContext);
    }

    @GET
    @Path("/{apiProductId}/swagger")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Swagger Definition", notes = "This operation can be used to retrieve the swagger definition of an API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the API is returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAPIProductSwagger(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAPIProductSwagger(apiProductId, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/thumbnail")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Thumbnail Image", notes = "This operation can be used to download a thumbnail image of an API product. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Thumbnail image returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAPIProductThumbnail(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAPIProductThumbnail(apiProductId, accept, ifNoneMatch, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search API Products ", notes = "This operation provides you a list of available API Products qualifying under a given search condition.  Each retrieved API Product is represented with a minimal amount of attributes. If you want to get complete details of an API Product, you need to use **Get details of an API Product** operation. ", response = APIProductListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying API Products is returned. ", response = APIProductListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAllAPIProducts( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "")  @QueryParam("query") String query,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAllAPIProducts(limit, offset, query, accept, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/is-outdated")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Check Whether API Product is Outdated", notes = "This operation can be used to retrieve the status indicating if an API Product is outdated due to updating of dependent APIs (This resource is not supported at the moment) ", response = APIProductOutdatedStatusDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the API is returned ", response = APIProductOutdatedStatusDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getIsAPIProductOutdated(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getIsAPIProductOutdated(apiProductId, accept, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/import")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import an API Product", notes = "This operation can be used to import an API Product. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations")
        })
    }, tags={ "Import Export",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. API Product Imported Successfully. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response importAPIProduct( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "Preserve Original Provider of the API Product. This is the user choice to keep or replace the API Product provider ")  @QueryParam("preserveProvider") Boolean preserveProvider,  @ApiParam(value = "Once the revision max limit reached, undeploy and delete the earliest revision and create a new revision ")  @QueryParam("rotateRevision") Boolean rotateRevision,  @ApiParam(value = "Whether to import the dependent APIs or not. ")  @QueryParam("importAPIs") Boolean importAPIs,  @ApiParam(value = "Whether to update the API Product or not. This is used when updating already existing API Products. ")  @QueryParam("overwriteAPIProduct") Boolean overwriteAPIProduct,  @ApiParam(value = "Whether to update the dependent APIs or not. This is used when updating already existing dependent APIs of an API Product. ")  @QueryParam("overwriteAPIs") Boolean overwriteAPIs) throws APIManagementException{
        return delegate.importAPIProduct(fileInputStream, fileDetail, preserveProvider, rotateRevision, importAPIs, overwriteAPIProduct, overwriteAPIs, securityContext);
    }

    @POST
    @Path("/{apiProductId}/restore-revision")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Restore Revision", notes = "Restore a revision to the Current API of the API Product ", response = APIProductDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Restored. Successful response with the newly restored API Product object as the entity in the body. ", response = APIProductDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response restoreAPIProductRevision(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Revision ID of an API ")  @QueryParam("revisionId") String revisionId) throws APIManagementException{
        return delegate.restoreAPIProductRevision(apiProductId, revisionId, securityContext);
    }

    @POST
    @Path("/{apiProductId}/undeploy-revision")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "UnDeploy Revision", notes = "UnDeploy an API Product Revision ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = Void.class),
        @ApiResponse(code = 201, message = "Created. Successful response with the newly undeployed APIRevisionDeploymentList object as the entity in the body. ", response = APIRevisionDeploymentDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response undeployAPIProductRevision(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Revision ID of an API ")  @QueryParam("revisionId") String revisionId,  @ApiParam(value = "Revision Number of an API ")  @QueryParam("revisionNumber") String revisionNumber,  @ApiParam(value = "", defaultValue="false") @DefaultValue("false") @QueryParam("allEnvironments") Boolean allEnvironments, @ApiParam(value = "Deployment object that needs to be added" ) List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO) throws APIManagementException{
        return delegate.undeployAPIProductRevision(apiProductId, revisionId, revisionNumber, allEnvironments, apIRevisionDeploymentDTO, securityContext);
    }

    @PUT
    @Path("/{apiProductId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an API Product", notes = "This operation can be used to update an existing API product. But the properties `name`, `provider` and `version` cannot be changed. ", response = APIProductDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated API product object ", response = APIProductDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response updateAPIProduct(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "API object that needs to be added" ,required=true) APIProductDTO apIProductDTO,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.updateAPIProduct(apiProductId, apIProductDTO, ifMatch, securityContext);
    }

    @PUT
    @Path("/{apiProductId}/deployments/{deploymentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update Deployment", notes = "Update deployment devportal visibility ", response = APIRevisionDeploymentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. Successful response with the newly updated APIRevisionDeployment List object as the entity in the body. ", response = APIRevisionDeploymentDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response updateAPIProductDeployment(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Base64 URL encoded value of the name of an environment ",required=true) @PathParam("deploymentId") String deploymentId, @ApiParam(value = "Deployment object that needs to be updated" ) APIRevisionDeploymentDTO apIRevisionDeploymentDTO) throws APIManagementException{
        return delegate.updateAPIProductDeployment(apiProductId, deploymentId, apIRevisionDeploymentDTO, securityContext);
    }

    @PUT
    @Path("/{apiProductId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Document of an API Product", notes = "This operation can be used to update metadata of an API's document. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document updated ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response updateAPIProductDocument(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId, @ApiParam(value = "Document object that needs to be added" ,required=true) DocumentDTO documentDTO,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.updateAPIProductDocument(apiProductId, documentId, documentDTO, ifMatch, securityContext);
    }

    @PUT
    @Path("/{apiProductId}/thumbnail")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Upload a Thumbnail Image", notes = "This operation can be used to upload a thumbnail image of an API Product. The thumbnail to be uploaded should be given as a form data parameter `file`. ", response = FileInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Products" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Image updated ", response = FileInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.", response = ErrorDTO.class) })
    public Response updateAPIProductThumbnail(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended. ",required=true) @PathParam("apiProductId") String apiProductId,  @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.updateAPIProductThumbnail(apiProductId, fileInputStream, fileDetail, ifMatch, securityContext);
    }
}
