package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ApiProductsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductDetailedDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductListDTO;

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
    @ApiParam(value = "Validator for conditional requests; based on ETag (Will be supported in future).\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header (Will be supported in future).\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apiProductsApiProductIdDelete(apiProductId,ifMatch,ifUnmodifiedSince);
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
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource (Will be supported in future).\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource (Will be supported in future).\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.apiProductsApiProductIdGet(apiProductId,accept,ifNoneMatch,ifModifiedSince);
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
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource (Will be supported in future).\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
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

    public Response apiProductsPost(@ApiParam(value = "API object that needs to be added\n" ,required=true ) APIProductDetailedDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.apiProductsPost(body,contentType);
    }
}

