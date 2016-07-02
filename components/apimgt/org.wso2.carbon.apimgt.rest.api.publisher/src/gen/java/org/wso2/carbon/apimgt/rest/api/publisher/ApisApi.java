package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ApisApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/apis")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/apis", description = "the apis API")
public class ApisApi  {

   private final ApisApiService delegate = ApisApiServiceFactory.getApisApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all APIs\n", notes = "Get a list of available APIs qualifying under a given search condition.\n", response = APIListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of qualifying APIs is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "**Search condition**.\n\nYou can search in attributes by using an **\"attribute:\"** modifier.\n\nEg. \"provider:wso2\" will match an API if the provider of the API contains \"wso2\".\n\nSupported attribute modifiers are [**version, context, status,\ndescription, subcontext, doc, provider, tag **]\n\nIf no advanced attribute modifier has been specified, search will match the\ngiven query string against API Name.\n") @QueryParam("query") String query,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apisGet(limit,offset,query,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new API", notes = "Create a new API\n", response = APIDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type.\nThe entity of the request was in a not supported format.\n") })

    public Response apisPost(@ApiParam(value = "API object that needs to be added\n" ,required=true ) APIDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.apisPost(body,contentType);
    }
    @POST
    @Path("/change-lifecycle")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Change API Status", notes = "Change the lifecycle of an API\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nLifecycle changed successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apisChangeLifecyclePost(@ApiParam(value = "The action to demote or promote the state of the API.\n\nSupported actions are [ **Publish, Deploy as a Prototype, Demote to Created, Demote to Prototyped, Block, Deprecate, Re-Publish, Retire **]\n",required=true, allowableValues="{values=[Publish, Deploy as a Prototype, Demote to Created, Demote to Prototyped, Block, Deprecate, Re-Publish, Retire]}") @QueryParam("action") String action,
    @ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API I.\nShould be formatted as **provider-name-version**.\n",required=true) @QueryParam("apiId") String apiId,
    @ApiParam(value = "\nYou can specify additional checklist items by using an **\"attribute:\"** modifier.\n\nEg: \"Deprecate Old Versions:true\" will deprecate older versions of a particular API when it is promoted to\nPublished state from Created state. Multiple checklist items can be given in \"attribute1:true, attribute2:false\"\nformat.\n\nSupported checklist items are as follows.\n1. **Deprecate Old Versions**: Setting this to true will deprecate older versions of a particular API when it is promoted to Published state from Created state.\n2. **Require Re-Subscription**: If you set this to true, users need to re subscribe to the API although they may have subscribed to an older version.\n") @QueryParam("lifecycleChecklist") String lifecycleChecklist,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisChangeLifecyclePost(action,apiId,lifecycleChecklist,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/copy-api")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Copy API", notes = "Create a new API by copying an existing API\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created API as entity in the body. Location header contains URL of newly created API.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nAPI to copy does not exist.\n") })

    public Response apisCopyApiPost(@ApiParam(value = "Version of the new API.",required=true) @QueryParam("newVersion") String newVersion,
    @ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API I.\nShould be formatted as **provider-name-version**.\n",required=true) @QueryParam("apiId") String apiId)
    {
    return delegate.apisCopyApiPost(newVersion,apiId);
    }
    @GET
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get API details", notes = "Get details of an API\n", response = APIDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested API is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdGet(apiId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing API", notes = "Update an existing API\n", response = APIDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with updated API object\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apisApiIdPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "API object that needs to be added\n" ,required=true ) APIDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdPut(apiId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete API", notes = "Delete an existing API\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apisApiIdDelete(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdDelete(apiId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get API Documents", notes = "Get a list of documents belonging to an API.\n", response = DocumentListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument list is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdDocumentsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apisApiIdDocumentsGet(apiId,limit,offset,accept,ifNoneMatch);
    }
    @POST
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new document", notes = "Add a new document to an API\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created Document object as entity in the body.\nLocation header contains URL of newly added document.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response apisApiIdDocumentsPost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Document object that needs to be added\n" ,required=true ) DocumentDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.apisApiIdDocumentsPost(apiId,body,contentType);
    }
    @GET
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get an API Document", notes = "Get a particular document associated with an API.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdDocumentsDocumentIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "**Document Identifier**\n",required=true ) @PathParam("documentId") String documentId,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdGet(apiId,documentId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an API Document", notes = "Update document details.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument updated\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apisApiIdDocumentsDocumentIdPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "**Document Identifier**\n",required=true ) @PathParam("documentId") String documentId,
    @ApiParam(value = "Document object that needs to be added\n" ,required=true ) DocumentDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdPut(apiId,documentId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an API Document", notes = "Delete a document of an API\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apisApiIdDocumentsDocumentIdDelete(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "**Document Identifier**\n",required=true ) @PathParam("documentId") String documentId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdDelete(apiId,documentId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get document content", notes = "Downloads a FILE type document/get the inline content or source url of a certain document.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nFile or inline content returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 303, message = "See Other.\nSource can be retrived from the URL specified at the Location header.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdDocumentsDocumentIdContentGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "**Document Identifier**\n",required=true ) @PathParam("documentId") String documentId,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdContentGet(apiId,documentId,accept,ifNoneMatch,ifModifiedSince);
    }
    @POST
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update API document content.", notes = "Upload a file to a document or add inline content to the document.\n\nDocument's source type should be **FILE** in order to upload a file to the document using **file** parameter.\nDocument's source type should be **INLINE** in order to add inline content to the document using **inlineContent** parameter.\n\nOnly one of **file** or **inlineContent** can be specified at one time.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument updated\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apisApiIdDocumentsDocumentIdContentPost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "**Document Identifier**\n",required=true ) @PathParam("documentId") String documentId,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Document to upload") @Multipart(value = "file", required = false) InputStream fileInputStream,
    @ApiParam(value = "Document to upload : details") @Multipart(value = "file" , required = false) Attachment fileDetail,
    @ApiParam(value = "Inline content of the document" )@Multipart(value = "inlineContent", required = false)  String inlineContent,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdContentPost(apiId,documentId,contentType,fileInputStream,fileDetail,inlineContent,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/swagger")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get API Definition", notes = "Get the swagger of an API\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested swagger document of the API is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdSwaggerGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Media types acceptable for the response. Default is JSON.\n"  , defaultValue="JSON")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdSwaggerGet(apiId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}/swagger")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update API Definition", notes = "Update an existing swagger definition of an API\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with updated Swagger definition\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apisApiIdSwaggerPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Swagger definition of the API", required=true )@Multipart(value = "apiDefinition")  String apiDefinition,
    @ApiParam(value = "Media type of the entity in the body. Default is JSON.\n" ,required=true , defaultValue="JSON")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdSwaggerPut(apiId,apiDefinition,contentType,ifMatch,ifUnmodifiedSince);
    }
}

