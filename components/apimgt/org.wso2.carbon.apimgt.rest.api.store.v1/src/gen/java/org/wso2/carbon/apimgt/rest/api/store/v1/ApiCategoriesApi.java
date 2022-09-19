package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ApiCategoriesApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
@Path("/api-categories")

@Api(description = "the api-categories API")




public class ApiCategoriesApi  {

  @Context MessageContext securityContext;

ApiCategoriesApiService delegate = new ApiCategoriesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get All API Categories", notes = "Get all API categories ", response = APICategoryListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Category (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Categories returned ", response = APICategoryListDTO.class) })
    public Response apiCategoriesGet( @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apiCategoriesGet(xWSO2Tenant, securityContext);
    }
}
