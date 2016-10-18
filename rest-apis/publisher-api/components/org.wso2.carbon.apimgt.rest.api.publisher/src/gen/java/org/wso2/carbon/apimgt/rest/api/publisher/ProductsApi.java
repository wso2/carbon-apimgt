package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.ProductsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ProductsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/products")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/products", description = "the products API")
public class ProductsApi  {

   private final ProductsApiService delegate = ProductsApiServiceFactory.getProductsApi();

    @POST
    @Path("/change-product-lifecycle")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Change API Product Status", notes = "This operation is used to change the lifecycle of an API Product. Eg: Publish an API Product which is in `CREATED` state. In order to change the lifecycle, we need to provide the lifecycle `action` as a query parameter.\n\nFor example, to Publish an API Product, `action` should be `Publish`.\n\nSome actions supports providing additional paramters which should be provided as `lifecycleChecklist` parameter. Please see parameters table for more information.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nLifecycle changed successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API Product does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response productsChangeProductLifecyclePost(@ApiParam(value = "The action to demote or promote the state of the API Product.\n\nSupported actions are [ **Publish, Demote to Created, Block, Deprecate, Re-Publish, Retire **]\n",required=true, allowableValues="{values=[Publish, Demote to Created, Block, Deprecate, Re-Publish, Retire]}") @QueryParam("action") String action,
    @ApiParam(value = "**API PRODUCT ID** consisting of the **UUID** of the API Product.\nThe combination of the provider of the API Product, name of the API Product and the version is also accepted as a valid API Product ID.\nShould be formatted as **provider-name-version**.\n",required=true) @QueryParam("productId") String productId,
    @ApiParam(value = "\nYou can specify additional checklist items by using an **\"attribute:\"** modifier.\n\nEg: \"Deprecate Old Versions:true\" will deprecate older versions of a particular API Product when it is promoted to\nPublished state from Created state. Multiple checklist items can be given in \"attribute1:true, attribute2:false\"\nformat.\n\nSupported checklist items are as follows.\n1. **Deprecate Old Versions**: Setting this to true will deprecate older versions of a particular API Product when it is promoted to Published state from Created state.\n2. **Require Re-Subscription**: If you set this to true, users need to re subscribe to the API Product although they may have subscribed to an older version.\n") @QueryParam("lifecycleChecklist") String lifecycleChecklist,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.productsChangeProductLifecyclePost(action,productId,lifecycleChecklist,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/copy-product")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new API Product version", notes = "This operation can be used to create a new version of an existing API Product. The new version is specified as `newVersion` query parameter. New API Product will be in `CREATED` state.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created API Product as entity in the body. Location header contains URL of newly created API Product.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nAPI to copy does not exist.\n") })

    public Response productsCopyProductPost(@ApiParam(value = "Version of the new API Product.",required=true) @QueryParam("newVersion") String newVersion,
    @ApiParam(value = "**API PRODUCT ID** consisting of the **UUID** of the API Product.\nThe combination of the provider of the API Product, name of the API Product and the version is also accepted as a valid API Product ID.\nShould be formatted as **provider-name-version**.\n",required=true) @QueryParam("productId") String productId)
    {
    return delegate.productsCopyProductPost(newVersion,productId);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all API products", notes = "This operation provides you a list of available API products of the user associated with the provided access token, qualifying under a given search condition.\n\nEach retrieved API product is represented with a minimal amount of attributes. If you want to get complete details of an API Product, you need to use **Get details of an API Product** operation.\n", response = APIProductListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of qualifying API Products is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported\n") })

    public Response productsGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "**Search condition**.\n\nYou can search in attributes by using an **\"<attribute>:\"** modifier.\n\nEg.\n\"provider:wso2\" will match an API Product if the provider of the API Product is exactly \"wso2\".\n\nAdditionally you can use wildcards.\n\nEg.\n\"provider:wso2*\" will match an API Product if the provider of the API Product starts with \"wso2\".\n\nSupported attribute modifiers are [**version, context, status,\ndescription, subcontext, doc, provider**]\n\nIf no advanced attribute modifier has been specified, search will match the\ngiven query string against API Product Name.\n") @QueryParam("query") String query,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.productsGet(limit,offset,query,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new API Product", notes = "This operation can be used to create a new API Product specifying the details of the API Product in the payload. The new API Product will be in `CREATED` state.\n", response = APIProductDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type.\nThe entity of the request was in a not supported format.\n") })

    public Response productsPost(@ApiParam(value = "API Product object that needs to be added\n" ,required=true ) APIProductDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType)
    {
    return delegate.productsPost(body,contentType);
    }
    @DELETE
    @Path("/{productId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an API Product", notes = "This operation can be used to delete an existing API Product providing the Id of the API Product.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response productsProductIdDelete(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\nThe combination of the provider of the API Product, name of the API Product and the version is also accepted as a valid API Product ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("productId") String productId,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.productsProductIdDelete(productId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{productId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an API Product", notes = "Using this operation, you can retrieve complete details of a single API Product. You need to provide the Id of the API Product to retrive it.\n", response = APIProductDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested API Product is returned\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API Product does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response productsProductIdGet(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\nThe combination of the provider of the API Product, name of the API Product and the version is also accepted as a valid API Product ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("productId") String productId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource.\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.productsProductIdGet(productId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{productId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an API Product", notes = "This operation can be used to update an existing API Product.\nBut the properties `name`, `version`, `context`, `provider`, `state` will not be changed by this operation.\n", response = APIProductDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nSuccessful response with updated API Product object\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden.\nThe request must be conditional but no condition has been specified.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response productsProductIdPut(@ApiParam(value = "**API Product ID** consisting of the **UUID** of the API Product.\nThe combination of the provider of the API Product, name of the API Product and the version is also accepted as a valid API Product ID.\nShould be formatted as **provider-name-version**.\n",required=true ) @PathParam("productId") String productId,
    @ApiParam(value = "API Product object that needs to be added\n" ,required=true ) APIProductDTO body,
    @ApiParam(value = "Media type of the entity in the body. Default is application/json.\n" ,required=true , defaultValue="application/json")@HeaderParam("Content-Type") String contentType,
    @ApiParam(value = "Validator for conditional requests; based on ETag.\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header.\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.productsProductIdPut(productId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
}

