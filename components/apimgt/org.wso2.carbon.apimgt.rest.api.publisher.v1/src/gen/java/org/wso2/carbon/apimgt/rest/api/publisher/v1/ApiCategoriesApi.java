package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ApiCategoriesApiServiceImpl;
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
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApiCategoriesApi  {

  @Context MessageContext securityContext;

ApiCategoriesApiService delegate = new ApiCategoriesApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all API categories", notes = "Get all API categories ", response = APICategoryListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Category (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Categories returned ", response = APICategoryListDTO.class) })
    public Response apiCategoriesGet() throws APIManagementException{
        return delegate.apiCategoriesGet(securityContext);
    }
}
