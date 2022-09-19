package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SdkGenApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
@Path("/sdk-gen")

@Api(description = "the sdk-gen API")




public class SdkGenApi  {

  @Context MessageContext securityContext;

SdkGenApiService delegate = new SdkGenApiServiceImpl();


    @GET
    @Path("/languages")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a List of Supported SDK Languages ", notes = "This operation will provide a list of programming languages that are supported by the swagger codegen library for generating System Development Kits (SDKs) for APIs available in the API Manager Developer Portal ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "SDKs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of supported languages for generating SDKs. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response sdkGenLanguagesGet() throws APIManagementException{
        return delegate.sdkGenLanguagesGet(securityContext);
    }
}
