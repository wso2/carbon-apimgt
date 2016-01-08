package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.dto.*;
import org.wso2.carbon.apimgt.rest.api.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.factories.ApisApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.dto.DocumentDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/apis")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/apis", description = "the apis API")
public class ApisApi  {

   private final ApisApiService delegate = ApisApiServiceFactory.getApisApi();

    @GET
    
    
    
    @io.swagger.annotations.ApiOperation(value = "Retrieving APIs\n", notes = "Get a list of available APIs qualifying under a given search condition.\n", response = APIListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. List of qualifying APIs is returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response apisGet(@ApiParam(value = "Maximum size of API array to return.",required=true) @QueryParam("limit") String limit,
    @ApiParam(value = "Starting point of the item list.",required=true) @QueryParam("offset") String offset,
    @ApiParam(value = "**Search condition**.\n\nYou can search in attributes by using an **\"attribute:\"** modifier.\n\nEg. \"provider:wso2\" will match an API if the provider of the API is wso2.\n\nSupported attribute modifiers are [**version, context, status,\ndescription, subcontext, doc, provider, tag **]\n\nIf no advanced attribute modifier has been specified, search will match the\ngiven query string against API Name.\n") @QueryParam("query") String query,
    @ApiParam(value = "List prototype or production APIs.\n") @QueryParam("type") String type,
    @ApiParam(value = "** Sort expression **\n\nA *sort expression* consists of a sequence of names of API \nproperties concatenated by a '+' or '-' (indicating ascending or \ndecending order) separated by a comma. The sequence of names \ncorresponds to a conjunction. \n") @QueryParam("sort") String sort,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apisGet(limit,offset,query,type,sort,accept,ifNoneMatch);
    }
    @POST
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Create a new API", response = APIDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error."),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format.") })

    public Response apisPost(@ApiParam(value = "API object that needs to be added" ,required=true ) APIDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType)
    {
    return delegate.apisPost(body,contentType);
    }
    @POST
    @Path("/change-lifecycle")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Change the lifecycle of an API", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Lifecycle changed successfully."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response apisChangeLifecyclePost(@ApiParam(value = "New lifecycle state of the API." )@FormParam("newState")  String newState,
    @ApiParam(value = "" )@FormParam("publishToGateway")  String publishToGateway,
    @ApiParam(value = "" )@FormParam("resubscription")  String resubscription,
    @ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true) @QueryParam("apiId") String apiId,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisChangeLifecyclePost(newState,publishToGateway,resubscription,apiId,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/copy-api")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Create a new API by copying an existing API", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created API as entity in the body. Location header contains URL of newly created API."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. API to copy does not exist.") })

    public Response apisCopyApiPost(@ApiParam(value = "Version of the new API.") @QueryParam("newVersion") String newVersion,
    @ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true) @QueryParam("apiId") String apiId)
    {
    return delegate.apisCopyApiPost(newVersion,apiId);
    }
    @GET
    @Path("/{apiId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get details of an API", response = APIDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK Requested API is returned"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response apisApiIdGet(@ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdGet(apiId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update an existing API", response = APIDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with updated API object"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response apisApiIdPut(@ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "API object that needs to be added" ,required=true ) APIDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdPut(apiId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{apiId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Delete an existing API", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted."),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response apisApiIdDelete(@ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdDelete(apiId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/documents")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of documents belonging to an API.", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Document list is returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response apisApiIdDocumentsGet(@ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Maximum size of API array to return.",required=true) @QueryParam("limit") String limit,
    @ApiParam(value = "Starting point of the item list.",required=true) @QueryParam("offset") String offset,
    @ApiParam(value = "Search condition.") @QueryParam("query") String query,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apisApiIdDocumentsGet(apiId,limit,offset,query,accept,ifNoneMatch);
    }
    @POST
    @Path("/{apiId}/documents")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new document to an API", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created Document object as entity in the body. Location header contains URL of newly added document."),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format.") })

    public Response apisApiIdDocumentsPost(@ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Document object that needs to be added" ,required=true ) DocumentDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType)
    {
    return delegate.apisApiIdDocumentsPost(apiId,body,contentType);
    }
    @GET
    @Path("/{apiId}/documents/{documentId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a particular document associated with an API.", response = APIDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Document returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Document does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response apisApiIdDocumentsDocumentIdGet(@ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Document Id",required=true ) @PathParam("documentId") String documentId,
    @ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdGet(apiId,documentId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}/documents/{documentId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Update document details.", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Document updated"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response apisApiIdDocumentsDocumentIdPut(@ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Document Id",required=true ) @PathParam("documentId") String documentId,
    @ApiParam(value = "Document object that needs to be added" ,required=true ) DocumentDTO body,
    @ApiParam(value = "Media type of the entity in the request body. Should denote XML or JSON, default is JSON."  )@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdPut(apiId,documentId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{apiId}/documents/{documentId}")
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Delete a document of an API", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met.") })

    public Response apisApiIdDocumentsDocumentIdDelete(@ApiParam(value = "**API ID** consisting of the name of the API, the identifier of the version and of the provider of the API. \nShould be formatted as **name/version/provider**\n",required=true ) @PathParam("apiId") String apiId,
    @ApiParam(value = "Document Id",required=true ) @PathParam("documentId") String documentId,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header."  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdDelete(apiId,documentId,ifMatch,ifUnmodifiedSince);
    }
}

