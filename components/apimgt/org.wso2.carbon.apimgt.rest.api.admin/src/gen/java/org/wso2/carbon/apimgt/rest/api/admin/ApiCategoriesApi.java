package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.ApiCategoriesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/api-categories")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/api-categories", description = "the api-categories API")
public class ApiCategoriesApi  {

   private final ApiCategoriesApiService delegate = ApiCategoriesApiServiceFactory.getApiCategoriesApi();

    @DELETE
    @Path("/{apiCategoryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an API Category", notes = "Delete an API Category by API Category Id\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nAPI Category successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nAPI Category to be deleted does not exist.\n") })

    public Response apiCategoriesApiCategoryIdDelete(@ApiParam(value = "API Category UUID\n",required=true ) @PathParam("apiCategoryId")  String apiCategoryId,
    @ApiParam(value = "Validator for conditional requests; based on ETag (Will be supported in future).\n"  )@HeaderParam("If-Match") String ifMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header (Will be supported in future).\n"  )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince)
    {
    return delegate.apiCategoriesApiCategoryIdDelete(apiCategoryId,ifMatch,ifUnmodifiedSince);
    }
    @PUT
    @Path("/{apiCategoryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an API Category", notes = "Update an API Category by category Id\n", response = APICategoryDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nLabel updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n") })

    public Response apiCategoriesApiCategoryIdPut(@ApiParam(value = "API Category UUID\n",required=true ) @PathParam("apiCategoryId")  String apiCategoryId,
    @ApiParam(value = "API Category object with updated information\n" ,required=true ) APICategoryDTO body)
    {
    return delegate.apiCategoriesApiCategoryIdPut(apiCategoryId,body);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all API categories", notes = "Get all API categories\n", response = APICategoryListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nCategories returned\n") })

    public Response apiCategoriesGet()
    {
    return delegate.apiCategoriesGet();
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new API Category", notes = "Add a new API Category\n", response = APICategoryDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n") })

    public Response apiCategoriesPost(@ApiParam(value = "API Category object that should to be added\n" ,required=true ) APICategoryDTO body)
    {
    return delegate.apiCategoriesPost(body);
    }
}

