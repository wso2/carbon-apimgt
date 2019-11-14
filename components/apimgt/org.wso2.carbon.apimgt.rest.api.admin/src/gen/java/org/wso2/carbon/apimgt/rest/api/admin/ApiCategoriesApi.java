package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.ApiCategoriesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryDTO;

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

