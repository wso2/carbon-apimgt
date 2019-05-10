package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.ApiProductsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/api-products")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/api-products", description = "the api-products API")
public class ApiProductsApi  {

   private final ApiProductsApiService delegate = ApiProductsApiServiceFactory.getApiProductsApi();

    @GET
    @Path("/{apiProductId}/documents/{documentId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get the content of an API Product document\n", notes = "This operation can be used to retrive the content of an API's document.\n\nThe document can be of 3 types. In each cases responses are different.\n\n1. **Inline type**:\n   The content of the document will be retrieved in `text/plain` content type\n2. **FILE type**:\n   The file will be downloaded with the related content type (eg. `application/pdf`)\n3. **URL type**:\n    The client will recieve the URL of the document as the Location header with the response with - `303 See Other`\n\n`X-WSO2-Tenant` header can be used to retrive the content of a document of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n\n**NOTE:**\n* This operation does not require an Authorization header by default. But in order to see a restricted API's document content, you need to provide Authorization header.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nFile or inline content returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 303, message = "See Other.\nSource can be retrived from the URL specified at the Location header.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Document Identifier\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsDocumentIdContentGet(apiProductId,documentId,ifNoneMatch);
    }
    @GET
    @Path("/{apiProductId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a document of an API Product\n", notes = "This operation can be used to retrieve a particular document's metadata associated with an API Product.\n\n`X-WSO2-Tenant` header can be used to retrive a document of an API Product that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n\n**NOTE:**\n* This operation does not require an Authorization header by default. But in order to see a restricted API's document, you need to provide Authorization header.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdDocumentsDocumentIdGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Document Identifier\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsDocumentIdGet(apiProductId,documentId,xWSO2Tenant,ifNoneMatch);
    }
    @GET
    @Path("/{apiProductId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a list of documents of an API product\n", notes = "This operation can be used to retrive a list of documents belonging to an API Product by providing the id of the API Product.\n\n`X-WSO2-Tenant` header can be used to retrive documents of an API Product that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n\n**NOTE:**\n* This operation does not require an Authorization header by default. But in order to see a restricted API's documents, you need to provide Authorization header.\n", response = DocumentListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument list is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdDocumentsGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsGet(apiProductId,limit,offset,xWSO2Tenant,ifNoneMatch);
    }
    @GET
    @Path("/{apiProductId}/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an API Product\n", notes = "Using this operation, you can retrieve complete details of a single API Product. You need to provide the Id of the API Product to retrive it.\n\n`X-WSO2-Tenant` header can be used to retrive an API of a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n\n**NOTE:**\n* This operation does not require an Authorization header by default. But if it is provided, it will be validated and checked for permissions of the user, hence you may be able to see APIs which are restricted for special permissions/roles. \\n\n", response = APIProductDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested API Product is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant)
    {
    return delegate.apiProductsApiProductIdGet(apiProductId,ifNoneMatch,xWSO2Tenant);
    }
    @GET
    @Path("/{apiProductId}/swagger")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get swagger definition\n", notes = "You can use this operation to retrieve the swagger definition of an API product.\n\n `X-WSO2-Tenant` header can be used to retrive the swagger definition an API product of a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n\n**NOTE:**\n* This operation does not require an Authorization header by default. But in order to see a restricted API's swagger definition, you need to provide Authorization header.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested swagger document of the API Product is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdSwaggerGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant)
    {
    return delegate.apiProductsApiProductIdSwaggerGet(apiProductId,ifNoneMatch,xWSO2Tenant);
    }
    @GET
    @Path("/{apiProductId}/thumbnail")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get thumbnail image", notes = "This operation can be used to download a thumbnail image of an API Product.\n\n`X-WSO2-Tenant` header can be used to retrive a thumbnail of an API that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used.\n\n**NOTE:**\n* This operation does not require an Authorization header by default. But in order to see a restricted API's thumbnail, you need to provide Authorization header.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThumbnail image returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdThumbnailGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdThumbnailGet(apiProductId,xWSO2Tenant,ifNoneMatch);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search API products\n", notes = "This operation provides you a list of available API products qualifying under a given search condition.\n\nEach retrieved API product is represented with a minimal amount of attributes. If you want to get complete details of an API, you need to use **Get details of an API Product** operation.\n\nThis operation supports retriving APIs of other tenants. The required tenant domain need to be specified as a header `X-WSO2-Tenant`. If not specified super tenant's APIs will be retrieved. If you used an Authorization header, the user's tenant \n", response = APIProductListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of qualifying API products is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "**Search condition**.\n\nYou can search in attributes by using an **\"<attribute>:\"** modifier.\n\nEg.\n\"provider:wso2\" will match an API if the provider of the API is exactly \"wso2\".\n\nAdditionally you can use wildcards.\n\nEg.\n\"provider:wso2*\" will match an API if the provider of the API starts with \"wso2\".\n\nSupported attribute modifiers are [**version, context, status,\ndescription, subcontext, doc, provider, tag**]\n\nIf no advanced attribute modifier has been specified, search will match the\ngiven query string against API Name.\n") @QueryParam("query")  String query,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsGet(limit,offset,xWSO2Tenant,query,ifNoneMatch);
    }
}

