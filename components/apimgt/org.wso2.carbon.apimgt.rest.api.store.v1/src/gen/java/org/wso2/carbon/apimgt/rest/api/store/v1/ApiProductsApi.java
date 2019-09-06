package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ApiProductsApiServiceImpl;
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
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApiProductsApi  {

  @Context MessageContext securityContext;

ApiProductsApiService delegate = new ApiProductsApiServiceImpl();


    @DELETE
    @Path("/{apiProductId}/comments/{commentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an API Product comment", notes = "Remove a Comment ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdCommentsCommentIdDelete(@ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId, @ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdCommentsCommentIdDelete(commentId, apiProductId, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/comments/{commentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of an API Product comment", notes = "Get the individual comment given by a username for a certain API Product. ", response = CommentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comment returned. ", response = CommentDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested comment does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdCommentsCommentIdGet(@ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId, @ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdCommentsCommentIdGet(commentId, apiProductId, ifNoneMatch, securityContext);
    }

    @PUT
    @Path("/{apiProductId}/comments/{commentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an API Product comment", notes = "Update a certain Comment ", response = CommentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comment updated. ", response = CommentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdCommentsCommentIdPut(@ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId, @ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Comment object that needs to be updated " ,required=true) CommentDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdCommentsCommentIdPut(commentId, apiProductId, body, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/comments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve API Product comments", notes = "Get a list of Comments that are already added to API Products ", response = CommentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comments list is returned. ", response = CommentListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdCommentsGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.apiProductsApiProductIdCommentsGet(apiProductId, limit, offset, securityContext);
    }

    @POST
    @Path("/{apiProductId}/comments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an API Product comment", notes = "", response = CommentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = CommentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdCommentsPost(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Comment object that should to be added " ,required=true) CommentDTO body) throws APIManagementException{
        return delegate.apiProductsApiProductIdCommentsPost(apiProductId, body, securityContext);
    }

    @GET
    @Path("/{apiProductId}/documents/{documentId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the content of an API Product document ", notes = "This operation can be used to retrive the content of an API's document.  The document can be of 3 types. In each cases responses are different.  1. **Inline type**:    The content of the document will be retrieved in `text/plain` content type 2. **FILE type**:    The file will be downloaded with the related content type (eg. `application/pdf`) 3. **URL type**:     The client will recieve the URL of the document as the Location header with the response with - `303 See Other`  `X-WSO2-Tenant` header can be used to retrive the content of a document of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's document content, you need to provide Authorization header. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. File or inline content returned. ", response = Void.class),
        @ApiResponse(code = 303, message = "See Other. Source can be retrived from the URL specified at the Location header. ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Document does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdDocumentsDocumentIdContentGet(apiProductId, documentId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a document of an API Product ", notes = "This operation can be used to retrieve a particular document's metadata associated with an API Product.  `X-WSO2-Tenant` header can be used to retrive a document of an API Product that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's document, you need to provide Authorization header. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document returned. ", response = DocumentDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Document does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdDocumentsDocumentIdGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdDocumentsDocumentIdGet(apiProductId, documentId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a list of documents of an API product ", notes = "This operation can be used to retrive a list of documents belonging to an API Product by providing the id of the API Product.  `X-WSO2-Tenant` header can be used to retrive documents of an API Product that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's documents, you need to provide Authorization header. ", response = DocumentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Product Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document list is returned. ", response = DocumentListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdDocumentsGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdDocumentsGet(apiProductId, limit, offset, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of an API Product ", notes = "Using this operation, you can retrieve complete details of a single API Product. You need to provide the Id of the API Product to retrive it.  `X-WSO2-Tenant` header can be used to retrive an API of a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But if it is provided, it will be validated and checked for permissions of the user, hence you may be able to see APIs which are restricted for special permissions/roles. \\n ", response = APIProductDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested API Product is returned ", response = APIProductDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apiProductsApiProductIdGet(apiProductId, ifNoneMatch, xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{apiProductId}/ratings")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get API Product ratings", notes = "Get the rating of an API Product. ", response = RatingListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Rating returned. ", response = RatingListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested rating does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdRatingsGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.apiProductsApiProductIdRatingsGet(apiProductId, limit, offset, securityContext);
    }

    @GET
    @Path("/{apiProductId}/ratings/{ratingId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a single API Product rating", notes = "Get a specific rating of an API Product. ", response = RatingDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Rating returned. ", response = RatingDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested rating does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdRatingsRatingIdGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Rating Id ",required=true) @PathParam("ratingId") String ratingId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdRatingsRatingIdGet(apiProductId, ratingId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/subscription-policies")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of the subscription throttling policies of an API Product ", notes = "This operation can be used to retrieve details of the subscription throttling policy of an API Product by specifying the API Product Id.  `X-WSO2-Tenant` header can be used to retrive API Product subscription throttling policies that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = ThrottlingPolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Throttling Policy returned ", response = ThrottlingPolicyDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Throttling Policy does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdSubscriptionPoliciesGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdSubscriptionPoliciesGet(apiProductId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiProductId}/swagger")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get swagger definition ", notes = "You can use this operation to retrieve the swagger definition of an API product.   `X-WSO2-Tenant` header can be used to retrive the swagger definition an API product of a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's swagger definition, you need to provide Authorization header. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the API Product is returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdSwaggerGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apiProductsApiProductIdSwaggerGet(apiProductId, ifNoneMatch, xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{apiProductId}/thumbnail")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get thumbnail image", notes = "This operation can be used to download a thumbnail image of an API Product.  `X-WSO2-Tenant` header can be used to retrive a thumbnail of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's thumbnail, you need to provide Authorization header. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Products",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Thumbnail image returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Document does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdThumbnailGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apiProductsApiProductIdThumbnailGet(apiProductId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @PUT
    @Path("/{apiProductId}/user-rating")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add or update logged in user's rating for an API Product", notes = "Adds or updates a rating ", response = RatingDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = RatingDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response apiProductsApiProductIdUserRatingPut(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. ",required=true) @PathParam("apiProductId") String apiProductId, @ApiParam(value = "Rating object that should to be added " ,required=true) RatingDTO body) throws APIManagementException{
        return delegate.apiProductsApiProductIdUserRatingPut(apiProductId, body, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search API products ", notes = "This operation provides you a list of available API products qualifying under a given search condition.  Each retrieved API product is represented with a minimal amount of attributes. If you want to get complete details of an API, you need to use **Get details of an API Product** operation.  This operation supports retriving APIs of other tenants. The required tenant domain need to be specified as a header `X-WSO2-Tenant`. If not specified super tenant's APIs will be retrieved. If you used an Authorization header, the user's tenant ", response = APIProductListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Products" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying API products is returned. ", response = APIProductListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apiProductsGet( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"<attribute>:\"** modifier.  Eg. \"provider:wso2\" will match an API if the provider of the API is exactly \"wso2\".  Additionally you can use wildcards.  Eg. \"provider:wso2*\" will match an API if the provider of the API starts with \"wso2\".  Supported attribute modifiers are [**version, context, status, description, subcontext, doc, provider, tag**]  If no advanced attribute modifier has been specified, search will match the given query string against API Name. ")  @QueryParam("query") String query, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apiProductsGet(limit, offset, xWSO2Tenant, query, ifNoneMatch, securityContext);
    }
}
