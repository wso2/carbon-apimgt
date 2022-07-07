package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.GraphQLQueryComplexityInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.GraphQLSchemaTypeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PostRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ApisApiServiceImpl;
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
@Path("/apis")

@Api(description = "the apis API")




public class ApisApi  {

  @Context MessageContext securityContext;

ApisApiService delegate = new ApisApiServiceImpl();


    @POST
    @Path("/{apiId}/comments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an API Comment", notes = "", response = CommentDTO.class, authorizations = {
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
    public Response addCommentToAPI(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "" ,required=true) PostRequestBodyDTO postRequestBodyDTO,  @ApiParam(value = "ID of the perent comment. ")  @QueryParam("replyTo") String replyTo) throws APIManagementException{
        return delegate.addCommentToAPI(apiId, postRequestBodyDTO, replyTo, securityContext);
    }

    @GET
    @Path("/{apiId}/documents/{documentId}/content")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the Content of an API Document ", notes = "This operation can be used to retrive the content of an API's document.  The document can be of 3 types. In each cases responses are different.  1. **Inline type**:    The content of the document will be retrieved in `text/plain` content type 2. **FILE type**:    The file will be downloaded with the related content type (eg. `application/pdf`) 3. **URL type**:     The client will recieve the URL of the document as the Location header with the response with - `303 See Other`  `X-WSO2-Tenant` header can be used to retrive the content of a document of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's document content, you need to provide Authorization header. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. File or inline content returned. ", response = Void.class),
        @ApiResponse(code = 303, message = "See Other. Source can be retrived from the URL specified at the Location header. ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsDocumentIdContentGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsDocumentIdContentGet(apiId, documentId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/documents/{documentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Document of an API ", notes = "This operation can be used to retrieve a particular document's metadata associated with an API.  `X-WSO2-Tenant` header can be used to retrive a document of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's document, you need to provide Authorization header. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document returned. ", response = DocumentDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsDocumentIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsDocumentIdGet(apiId, documentId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/documents")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a List of Documents of an API ", notes = "This operation can be used to retrive a list of documents belonging to an API by providing the id of the API.  `X-WSO2-Tenant` header can be used to retrive documents of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's documents, you need to provide Authorization header. ", response = DocumentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document list is returned. ", response = DocumentListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsGet(apiId, limit, offset, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of an API ", notes = "Using this operation, you can retrieve complete details of a single API. You need to provide the Id of the API to retrive it.  `X-WSO2-Tenant` header can be used to retrieve an API of a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But if it is provided, it will be validated and checked for permissions of the user, hence you may be able to see APIs which are restricted for special permissions/roles. \\n ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested API is returned ", response = APIDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdGet(apiId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/graphql-policies/complexity")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the Complexity Related Details of an API", notes = "This operation can be used to retrieve complexity related details belonging to an API by providing the API id. ", response = GraphQLQueryComplexityInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "GraphQL Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested complexity details returned. ", response = GraphQLQueryComplexityInfoDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not contain any complexity details. ", response = Void.class) })
    public Response apisApiIdGraphqlPoliciesComplexityGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdGraphqlPoliciesComplexityGet(apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/graphql-policies/complexity/types")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve Types and Fields of a GraphQL Schema", notes = "This operation can be used to retrieve all types and fields of the GraphQL Schema by providing the API id. ", response = GraphQLSchemaTypeListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "GraphQL Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Types and fields returned successfully. ", response = GraphQLSchemaTypeListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Retrieving types and fields failed. ", response = Void.class) })
    public Response apisApiIdGraphqlPoliciesComplexityTypesGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdGraphqlPoliciesComplexityTypesGet(apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/graphql-schema")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get GraphQL Definition ", notes = "You can use this operation to retrieve the GraphQL schema definition of a GraphQL API.   `X-WSO2-Tenant` header can be used to retrieve the swagger definition an API of a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's swagger definition, you need to provide Authorization header. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the API is returned ", response = String.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdGraphqlSchemaGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apisApiIdGraphqlSchemaGet(apiId, ifNoneMatch, xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{apiId}/ratings")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve API Ratings", notes = "This operation can be used to retrieve the list of ratings of an API.  `X-WSO2-Tenant` header can be used to retrieve ratings of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = RatingListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Rating list returned. ", response = RatingListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdRatingsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apisApiIdRatingsGet(apiId, limit, offset, xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{apiId}/sdks/{language}")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Generate a SDK for an API ", notes = "This operation can be used to generate SDKs (System Development Kits), for the APIs available in the API Developer Portal, for a requested development language. ", response = byte[].class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "SDKs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. SDK generated successfully. ", response = byte[].class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response apisApiIdSdksLanguageGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Programming language of the SDK that is required.  Languages supported by default are **Java**, **Javascript**, **Android** and **JMeter**. ",required=true) @PathParam("language") String language,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apisApiIdSdksLanguageGet(apiId, language, xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{apiId}/subscription-policies")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of the Subscription Throttling Policies of an API ", notes = "This operation can be used to retrieve details of the subscription throttling policy of an API by specifying the API Id.  `X-WSO2-Tenant` header can be used to retrive API subscription throttling policies that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = ThrottlingPolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Throttling Policy returned ", response = ThrottlingPolicyDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdSubscriptionPoliciesGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdSubscriptionPoliciesGet(apiId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/swagger")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Swagger Definition ", notes = "You can use this operation to retrieve the swagger definition of an API.   `X-WSO2-Tenant` header can be used to retrieve the swagger definition an API of a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's swagger definition, you need to provide Authorization header. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the API is returned ", response = String.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdSwaggerGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Name of the API gateway environment. ")  @QueryParam("environmentName") String environmentName,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apisApiIdSwaggerGet(apiId, environmentName, ifNoneMatch, xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{apiId}/thumbnail")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Thumbnail Image", notes = "This operation can be used to download a thumbnail image of an API.  `X-WSO2-Tenant` header can be used to retrive a thumbnail of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.  **NOTE:** * This operation does not require an Authorization header by default. But in order to see a restricted API's thumbnail, you need to provide Authorization header. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Thumbnail image returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdThumbnailGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdThumbnailGet(apiId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/topics")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a list of available topics for a given async API ", notes = "This operation will provide a list of topics available for a given Async API, based on the provided API ID. ", response = TopicListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Topics",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Topic list returned. ", response = TopicListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response apisApiIdTopicsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apisApiIdTopicsGet(apiId, xWSO2Tenant, securityContext);
    }

    @DELETE
    @Path("/{apiId}/user-rating")
    
    
    @ApiOperation(value = "Delete User API Rating", notes = "This operation can be used to delete logged in user API rating.  `X-WSO2-Tenant` header can be used to delete the logged in user rating of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class) })
    public Response apisApiIdUserRatingDelete(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdUserRatingDelete(apiId, xWSO2Tenant, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/user-rating")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve API Rating of User", notes = "This operation can be used to get the user rating of an API.  `X-WSO2-Tenant` header can be used to retrieve the logged in user rating of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = RatingDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Rating returned. ", response = RatingDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client already has the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisApiIdUserRatingGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdUserRatingGet(apiId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/user-rating")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add or Update Logged in User's Rating for an API", notes = "This operation can be used to add or update an API rating.  `X-WSO2-Tenant` header can be used to add or update the logged in user rating of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = RatingDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Ratings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the newly created or updated object as entity in the body. ", response = RatingDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response apisApiIdUserRatingPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Rating object that should to be added " ,required=true) RatingDTO ratingDTO,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apisApiIdUserRatingPut(apiId, ratingDTO, xWSO2Tenant, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search APIs ", notes = "This operation provides you a list of available APIs qualifying under a given search condition.  Each retrieved API is represented with a minimal amount of attributes. If you want to get complete details of an API, you need to use **Get details of an API** operation.  This operation supports retrieving APIs of other tenants. The required tenant domain need to be specified as a header `X-WSO2-Tenant`. If not specified super tenant's APIs will be retrieved. If you used an Authorization header, the user's tenant associated with the access token will be used.  **NOTE:** * By default, this operation retrieves Published APIs. In order to retrieve Prototyped APIs, you need to use **query** parameter and specify **status:PROTOTYPED**. * This operation does not require an Authorization header by default. But if it is provided, it will be validated and checked for permissions of the user, hence you may be able to see APIs which are restricted for special permissions/roles. ", response = APIListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying APIs is returned. ", response = APIListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response apisGet( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"<attribute>:\"** modifier.  Eg. \"provider:wso2\" will match an API if the provider of the API is exactly \"wso2\".  Additionally you can use wildcards.  Eg. \"provider:wso2*\" will match an API if the provider of the API starts with \"wso2\".  Supported attribute modifiers are [**version, context, status, description, doc, provider, tag**]  To search by API Properties provide the query in below format.  **property_name:property_value**  Eg. \"environment:test\" where environment is the property name and test is the propert value.  If no advanced attribute modifier has been specified, search will match the given query string against API Name. ")  @QueryParam("query") String query,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisGet(limit, offset, xWSO2Tenant, query, ifNoneMatch, securityContext);
    }

    @DELETE
    @Path("/{apiId}/comments/{commentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an API Comment", notes = "Remove a Comment ", response = Void.class, authorizations = {
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
    public Response deleteComment(@ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId, @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.deleteComment(commentId, apiId, ifMatch, securityContext);
    }

    @PATCH
    @Path("/{apiId}/comments/{commentId}")
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
    public Response editCommentOfAPI(@ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId, @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "" ,required=true) PatchRequestBodyDTO patchRequestBodyDTO) throws APIManagementException{
        return delegate.editCommentOfAPI(commentId, apiId, patchRequestBodyDTO, securityContext);
    }

    @GET
    @Path("/{apiId}/comments")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve API Comments", notes = "Get a list of Comments that are already added to APIs ", response = CommentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comments list is returned. ", response = CommentListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAllCommentsOfAPI(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Whether we need to display commentor details. ", defaultValue="false") @DefaultValue("false") @QueryParam("includeCommenterInfo") Boolean includeCommenterInfo) throws APIManagementException{
        return delegate.getAllCommentsOfAPI(apiId, xWSO2Tenant, limit, offset, includeCommenterInfo, securityContext);
    }

    @GET
    @Path("/{apiId}/comments/{commentId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Details of an API Comment", notes = "Get the individual comment given by a user for a certain API. ", response = CommentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comment returned. ", response = CommentDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getCommentOfAPI(@ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId, @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Whether we need to display commentor details. ", defaultValue="false") @DefaultValue("false") @QueryParam("includeCommenterInfo") Boolean includeCommenterInfo,  @ApiParam(value = "Maximum size of replies array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("replyLimit") Integer replyLimit,  @ApiParam(value = "Starting point within the complete list of replies. ", defaultValue="0") @DefaultValue("0") @QueryParam("replyOffset") Integer replyOffset) throws APIManagementException{
        return delegate.getCommentOfAPI(commentId, apiId, xWSO2Tenant, ifNoneMatch, includeCommenterInfo, replyLimit, replyOffset, securityContext);
    }

    @GET
    @Path("/{apiId}/comments/{commentId}/replies")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get replies of a comment", notes = "Get replies of a comment ", response = CommentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Comments",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Comment returned. ", response = CommentListDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getRepliesOfComment(@ApiParam(value = "Comment Id ",required=true) @PathParam("commentId") String commentId, @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Whether we need to display commentor details. ", defaultValue="false") @DefaultValue("false") @QueryParam("includeCommenterInfo") Boolean includeCommenterInfo) throws APIManagementException{
        return delegate.getRepliesOfComment(commentId, apiId, xWSO2Tenant, limit, offset, ifNoneMatch, includeCommenterInfo, securityContext);
    }

    @GET
    @Path("/{apiId}/wsdl")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get API WSDL definition", notes = "This operation can be used to retrieve the swagger definition of an API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "APIs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested WSDL document of the API is returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getWSDLOfAPI(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Name of the API gateway environment. ")  @QueryParam("environmentName") String environmentName,  @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.getWSDLOfAPI(apiId, environmentName, ifNoneMatch, xWSO2Tenant, securityContext);
    }
}
