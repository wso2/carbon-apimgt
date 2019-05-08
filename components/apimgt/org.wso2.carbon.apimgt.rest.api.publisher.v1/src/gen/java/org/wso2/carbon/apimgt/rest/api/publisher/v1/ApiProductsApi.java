package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.factories.ApiProductsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDetailedDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductListDTO;

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

    @DELETE
    @Path("/{apiProductId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an API Product", notes = "This operation can be used to delete an existing API Product proving the Id of the API Product.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apiProductsApiProductIdDelete(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.apiProductsApiProductIdDelete(apiProductId,ifMatch);
    }
    @GET
    @Path("/{apiProductId}/documents/{documentId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get the content of an API Product document", notes = "This operation can be used to retrive the content of an API's document.\n\nThe document can be of 3 types. In each cases responses are different.\n\n1. **Inline type**:\n   The content of the document will be retrieved in `text/plain` content type\n\n   _Sample cURL_ : `curl -k -H \"Authorization:Bearer 579f0af4-37be-35c7-81a4-f1f1e9ee7c51\" -F inlineContent=@\"docs.txt\" -X POST \"https://localhost:9443/api/am/publisher/v0.14/apis/995a4972-3178-4b17-a374-756e0e19127c/documents/43c2bcce-60e7-405f-bc36-e39c0c5e189e/content`\n2. **FILE type**:\n   The file will be downloaded with the related content type (eg. `application/pdf`)\n3. **URL type**:\n    The client will recieve the URL of the document as the Location header with the response with - `303 See Other`\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nFile or inline content returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 303, message = "See Other.\nSource can be retrived from the URL specified at the Location header.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Document Identifier\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsDocumentIdContentGet(apiProductId,documentId,accept,ifNoneMatch);
    }
    @POST
    @Path("/{apiProductId}/documents/{documentId}/content")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Upload the content of an API Product document", notes = "Thid operation can be used to upload a file or add inline content to an API Product document.\n\n**IMPORTANT:**\n* Either **file** or **inlineContent** form data parameters should be specified at one time.\n* Document's source type should be **FILE** in order to upload a file to the document using **file** parameter.\n* Document's source type should be **INLINE** in order to add inline content to the document using **inlineContent** parameter.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument updated\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apiProductsApiProductIdDocumentsDocumentIdContentPost(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Document Identifier\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "Document to upload") @Multipart(value = "file", required = false) InputStream fileInputStream,
    @ApiParam(value = "Document to upload : details") @Multipart(value = "file" , required = false) Attachment fileDetail,
    @ApiParam(value = "Inline content of the document" )@Multipart(value = "inlineContent", required = false)  String inlineContent,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsDocumentIdContentPost(apiProductId,documentId,fileInputStream,fileDetail,inlineContent,ifMatch);
    }
    @DELETE
    @Path("/{apiProductId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a document of an API Product", notes = "This operation can be used to delete a document associated with an API Product.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apiProductsApiProductIdDocumentsDocumentIdDelete(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Document Identifier\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsDocumentIdDelete(apiProductId,documentId,ifMatch);
    }
    @GET
    @Path("/{apiProductId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a document of an API", notes = "This operation can be used to retrieve a particular document's metadata associated with an API.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdDocumentsDocumentIdGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Document Identifier\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsDocumentIdGet(apiProductId,documentId,accept,ifNoneMatch);
    }
    @PUT
    @Path("/{apiProductId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a document of an API Product", notes = "This operation can be used to update metadata of an API's document.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument updated\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apiProductsApiProductIdDocumentsDocumentIdPut(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Document Identifier\n",required=true ) @PathParam("documentId")  String documentId,
    @ApiParam(value = "Document object that needs to be added\n" ,required=true ) DocumentDTO body,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsDocumentIdPut(apiProductId,documentId,body,ifMatch);
    }
    @GET
    @Path("/{apiProductId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a list of documents of an API Product", notes = "This operation can be used to retrive a list of documents belonging to an API Product by providing the id of the API Product.\n", response = DocumentListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nDocument list is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API Product does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdDocumentsGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdDocumentsGet(apiProductId,limit,offset,accept,ifNoneMatch);
    }
    @POST
    @Path("/{apiProductId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new document to an API Product", notes = "This operation can be used to add a new documentation to an API Product. This operation only adds the metadata of a document. To add the actual content we need to use **Upload the content of an API Product document ** API once we obtain a document Id by this operation.\n", response = DocumentDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created Document object as entity in the body.\nLocation header contains URL of newly added document.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type.\nThe entity of the request was in a not supported format.\n\n######################################################\n") })

    public Response apiProductsApiProductIdDocumentsPost(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Document object that needs to be added\n" ,required=true ) DocumentDTO body)
    {
    return delegate.apiProductsApiProductIdDocumentsPost(apiProductId,body);
    }
    @GET
    @Path("/{apiProductId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an API Product", notes = "Using this operation, you can retrieve complete details of a single API Product. You need to provide the Id of the API to retrive it.\n", response = APIProductDetailedDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested API Product is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdGet(apiProductId,accept,ifNoneMatch);
    }
    @PUT
    @Path("/{apiProductId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an API product", notes = "This operation can be used to update an existing API product.\nBut the properties `name`, `provider`\n", response = APIProductDetailedDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with updated API product object\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apiProductsApiProductIdPut(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "API object that needs to be added\n" ,required=true ) APIProductDetailedDTO body,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.apiProductsApiProductIdPut(apiProductId,body,ifMatch);
    }
    @GET
    @Path("/{apiProductId}/swagger")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get swagger definition", notes = "This operation can be used to retrieve the swagger definition of an API.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested swagger document of the API is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdSwaggerGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdSwaggerGet(apiProductId,accept,ifNoneMatch);
    }
    @PUT
    @Path("/{apiProductId}/swagger")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "upload swagger definition", notes = "This operation can be used to create/update the swagger definition of an existing API Product. Swagger definition to be updated is passed as a form data parameter `apiDefinition`.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with updated Swagger definition\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apiProductsApiProductIdSwaggerPut(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Swagger definition of the API product", required=true )@Multipart(value = "apiDefinition")  String apiDefinition,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.apiProductsApiProductIdSwaggerPut(apiProductId,apiDefinition,ifMatch);
    }
    @GET
    @Path("/{apiProductId}/thumbnail")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get thumbnail image", notes = "This operation can be used to download a thumbnail image of an API product.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThumbnail image returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Document does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsApiProductIdThumbnailGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.apiProductsApiProductIdThumbnailGet(apiProductId,accept,ifNoneMatch);
    }
    @POST
    @Path("/{apiProductId}/thumbnail")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Upload a thumbnail image", notes = "This operation can be used to upload a thumbnail image of an API Product. The thumbnail to be uploaded should be given as a form data parameter `file`.\n", response = FileInfoDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nImage updated\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response apiProductsApiProductIdThumbnailPost(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product. Using the **UUID** in the API call is recommended.\n",required=true ) @PathParam("apiProductId") @Encoded String apiProductId,
    @ApiParam(value = "Image to upload") @Multipart(value = "file") InputStream fileInputStream,
    @ApiParam(value = "Image to upload : details") @Multipart(value = "file" ) Attachment fileDetail,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch)
    {
    return delegate.apiProductsApiProductIdThumbnailPost(apiProductId,fileInputStream,fileDetail,ifMatch);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/gzip" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search API Products\n", notes = "This operation provides you a list of available API Products qualifying under a given search condition.\n\nEach retrieved API Product is represented with a minimal amount of attributes. If you want to get complete details of an API Product, you need to use **Get details of an API Product** operation.\n", response = APIProductListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of qualifying API Products is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response apiProductsGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "") @QueryParam("query")  String query,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Defines whether the returned response should contain full details of API\n") @QueryParam("expand")  Boolean expand,
    @ApiParam(value = "Tenant domain, whose API Products should be retrieved. If not specified, the logged in user's tenant domain will\nbe considered for this.\n") @QueryParam("tenantDomain")  String tenantDomain)
    {
    return delegate.apiProductsGet(limit,offset,query,accept,ifNoneMatch,expand,tenantDomain);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new API Product", notes = "This operation can be used to create a new API Product specifying the details of the API Product in the payload. \n", response = APIProductDetailedDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type.\nThe entity of the request was in a not supported format.\n") })

    public Response apiProductsPost(@ApiParam(value = "API object that needs to be added\n" ,required=true ) APIProductDetailedDTO body)
    {
    return delegate.apiProductsPost(body);
    }
}

