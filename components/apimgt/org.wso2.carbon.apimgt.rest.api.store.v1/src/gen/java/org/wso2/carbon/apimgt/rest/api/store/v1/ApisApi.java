package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.ApisApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;

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

    @DELETE
    @Path("/{apiId}/comments/{commentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an API comment", notes = "Remove a Comment\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n") })

    public Response apisApiIdCommentsCommentIdDelete(@ApiParam(value = "Comment Id\n",required=true ) @PathParam("commentId")  String commentId,
    @ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdCommentsCommentIdDelete(commentId,apiId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/comments/{commentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an API comment", notes = "Get the individual comment given by a username for a certain API.\n", response = CommentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nComment returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested comment does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdCommentsCommentIdGet(@ApiParam(value = "Comment Id\n",required=true ) @PathParam("commentId")  String commentId,
    @ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdCommentsCommentIdGet(commentId,apiId,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}/comments/{commentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an API comment", notes = "Update a certain Comment\n", response = CommentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nComment updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apisApiIdCommentsCommentIdPut(@ApiParam(value = "Comment Id\n",required=true ) @PathParam("commentId")  String commentId,
    @ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Comment object that needs to be updated\n" ,required=true ) CommentDTO body,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apisApiIdCommentsCommentIdPut(commentId,apiId,body,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/comments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve API comments", notes = "Get a list of Comments that are already added to APIs\n", response = CommentListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nComments list is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported\n") })

    public Response apisApiIdCommentsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset)
    {
    return delegate.apisApiIdCommentsGet(apiId,limit,offset);
    }
    @POST
    @Path("/{apiId}/comments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an API comment", notes = "", response = CommentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response apisApiIdCommentsPost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Comment object that should to be added\n" ,required=true ) CommentDTO body)
    {
    return delegate.apisApiIdCommentsPost(apiId,body);
    }
    @GET
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get the content of an API document", notes = "Downloads a FILE type document/get the inline content or source url of a certain document.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nFile or inline content returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 303, message = "See Other.\nSource can be retrived from the URL specified at the Location header.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdDocumentsDocumentIdContentGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "**Document Identifier**\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdContentGet(apiId,documentId,ifNoneMatch,ifModifiedSince);
    }
    @GET
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a document of an API", notes = "Get a particular document associated with an API.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdDocumentsDocumentIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "**Document Identifier**\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdDocumentsDocumentIdGet(apiId,documentId,ifNoneMatch,ifModifiedSince);
    }
    @GET
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a list of API documents", notes = "Get a list of documents belonging to an API.\n", response = DocumentListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument list is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdDocumentsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apisApiIdDocumentsGet(apiId,limit,offset,ifNoneMatch);
    }
    @GET
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an API", notes = "Get details of an API\n", response = APIDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested API is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdGet(apiId,xWSO2Tenant,ifNoneMatch,ifModifiedSince);
    }
    @GET
    @Path("/{apiId}/ratings")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get API ratings", notes = "Get the rating of an API.\n", response = RatingListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRating returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested rating does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdRatingsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset)
    {
    return delegate.apisApiIdRatingsGet(apiId,limit,offset);
    }
    @GET
    @Path("/{apiId}/ratings/{ratingId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a single API rating", notes = "Get a specific rating of an API.\n", response = RatingDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRating returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested rating does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdRatingsRatingIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Rating Id\n",required=true ) @PathParam("ratingId")  String ratingId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdRatingsRatingIdGet(apiId,ratingId,ifNoneMatch,ifModifiedSince);
    }
    @GET
    @Path("/{apiId}/sdks/{language}")
    @Consumes({ "application/json" })
    @Produces({ "application/zip" })
    @io.swagger.annotations.ApiOperation(value = "Generate a SDK for an API\n", notes = "This operation can be used to generate SDKs (System Development Kits), for the APIs available in the API Store, for a requested development language.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSDK generated successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nRequested SDK Language is not supported.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.\nError while generating SDK.\n") })

    public Response apisApiIdSdksLanguageGet(@ApiParam(value = "ID of the specific API for which the SDK is required.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Programming language of the SDK that is required.\n",required=true ) @PathParam("language")  String language)
    {
    return delegate.apisApiIdSdksLanguageGet(apiId,language);
    }
    @GET
    @Path("/{apiId}/swagger")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get API swagger definition", notes = "Get the swagger of an API\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested swagger document of the API is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdSwaggerGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdSwaggerGet(apiId,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}/user-rating")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add or update logged in user's rating for an API", notes = "Adds or updates a rating\n", response = RatingDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n") })

    public Response apisApiIdUserRatingPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Rating object that should to be added\n" ,required=true ) RatingDTO body)
    {
    return delegate.apisApiIdUserRatingPut(apiId,body);
    }
    @GET
    @Path("/{apiId}/wsdl")
    @Consumes({ "application/json" })
    @Produces({ "application/octet-stream" })
    @io.swagger.annotations.ApiOperation(value = "Get API WSDL definition", notes = "This operation can be used to retrieve the swagger definition of an API.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested WSDL document of the API is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisApiIdWsdlGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("apiId")  String apiId,
    @ApiParam(value = "Gateway Label\n") @QueryParam("label")  String label,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apisApiIdWsdlGet(apiId,label,ifNoneMatch,ifModifiedSince);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search APIs\n", notes = "Get a list of available APIs qualifying under a given search condition.\n", response = APIListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of qualifying APIs is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apisGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Comma seperated store labels\n") @QueryParam("labels")  String labels,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "**Search condition**.\n\nYou can search in attributes by using an **\"attribute:\"** modifier.\n\nEg.\n\"provider:wso2\" will match an API if the provider of the API is exactly \"wso2\".\n\nAdditionally you can use wildcards.\n\nEg.\n\"provider:wso2*\" will match an API if the provider of the API starts with \"wso2\".\n\nSupported attribute modifiers are [**version, context, lifeCycleStatus,\ndescription, subcontext, doc, provider, tag **]\n\nIf no advanced attribute modifier has been specified, search will match the\ngiven query string against API Name.\n") @QueryParam("query")  String query,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apisGet(limit,offset,labels,xWSO2Tenant,query,ifNoneMatch);
    }
}

