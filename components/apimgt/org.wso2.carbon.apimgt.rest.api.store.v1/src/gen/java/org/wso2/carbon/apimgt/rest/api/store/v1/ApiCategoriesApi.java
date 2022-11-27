package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ApiCategoriesApiServiceImpl;
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


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get All API Categories", notes = "Get all API categories ", response = APICategoryListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "API Categories" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Categories returned ", response = APICategoryListDTO.class) })
    public Response apiCategoriesGet( @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apiCategoriesGet(xWSO2Tenant, securityContext);
    }
}
