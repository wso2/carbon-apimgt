package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.SdkGenApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.SdkGenApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/sdk-gen")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/sdk-gen", description = "the sdk-gen API")
public class SdkGenApi  {

   private final SdkGenApiService delegate = SdkGenApiServiceFactory.getSdkGenApi();

    @GET
    @Path("/languages")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a list of supported SDK languages\n", notes = "This operation will provide a list of programming languages that are supported by the swagger codegen library for generating System Development Kits (SDKs) for APIs available in the API Manager Store\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of supported languages for generating SDKs.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe list of languages is not found.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.\nError while retrieving the list.\n") })

    public Response sdkGenLanguagesGet()
    {
    return delegate.sdkGenLanguagesGet();
    }
}

