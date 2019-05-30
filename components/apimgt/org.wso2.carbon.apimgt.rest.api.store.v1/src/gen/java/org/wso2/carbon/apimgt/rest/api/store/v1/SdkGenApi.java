package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.SdkGenApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SdkGenApiServiceImpl;

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
@Path("/sdk-gen")

@Api(description = "the sdk-gen API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class SdkGenApi  {

  @Context MessageContext securityContext;

SdkGenApiService delegate = new SdkGenApiServiceImpl();


    @GET
    @Path("/languages")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a list of supported SDK languages ", notes = "This operation will provide a list of programming languages that are supported by the swagger codegen library for generating System Development Kits (SDKs) for APIs available in the API Manager Store ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "SDKs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of supported languages for generating SDKs. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The list of languages is not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. Error while retrieving the list. ", response = ErrorDTO.class) })
    public Response sdkGenLanguagesGet() {
        return delegate.sdkGenLanguagesGet(securityContext);
    }
}
