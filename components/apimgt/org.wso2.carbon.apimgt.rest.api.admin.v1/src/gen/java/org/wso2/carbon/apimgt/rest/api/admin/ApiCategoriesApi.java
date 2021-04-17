package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.ApiCategoriesApiServiceImpl;
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
@Path("/api-categories")

@Api(description = "the api-categories API")




public class ApiCategoriesApi  {

  @Context MessageContext securityContext;

ApiCategoriesApiService delegate = new ApiCategoriesApiServiceImpl();


    @DELETE
    @Path("/{apiCategoryId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an API Category", notes = "Delete an API Category by API Category Id ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations")
        })
    }, tags={ "API Category (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API Category successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response apiCategoriesApiCategoryIdDelete(@ApiParam(value = "API Category UUID ",required=true) @PathParam("apiCategoryId") String apiCategoryId) throws APIManagementException{
        return delegate.apiCategoriesApiCategoryIdDelete(apiCategoryId, securityContext);
    }

    @PUT
    @Path("/{apiCategoryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an API Category", notes = "Update an API Category by category Id ", response = APICategoryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations")
        })
    }, tags={ "API Category (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Label updated. ", response = APICategoryDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response apiCategoriesApiCategoryIdPut(@ApiParam(value = "API Category UUID ",required=true) @PathParam("apiCategoryId") String apiCategoryId, @ApiParam(value = "API Category object with updated information " ,required=true) APICategoryDTO apICategoryDTO) throws APIManagementException{
        return delegate.apiCategoriesApiCategoryIdPut(apiCategoryId, apICategoryDTO, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all API Categories", notes = "Get all API categories ", response = APICategoryListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations")
        })
    }, tags={ "API Category (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Categories returned ", response = APICategoryListDTO.class) })
    public Response apiCategoriesGet() throws APIManagementException{
        return delegate.apiCategoriesGet(securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add API Category", notes = "Add a new API category ", response = APICategoryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_operations", description = "Manage API categories and Key Managers related operations")
        })
    }, tags={ "API Category (Individual)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. ", response = APICategoryDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response apiCategoriesPost(@ApiParam(value = "API Category object that should to be added " ,required=true) APICategoryDTO apICategoryDTO) throws APIManagementException{
        return delegate.apiCategoriesPost(apICategoryDTO, securityContext);
    }
}
