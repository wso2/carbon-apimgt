package org.wso2.carbon.apimgt.rest.api.store;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.factories.SdkGenApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.store.SdkGenApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/store/v1.[\\d]+/sdk-gen")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/sdk-gen")
@io.swagger.annotations.Api(description = "the sdk-gen API")
public class SdkGenApi implements Microservice  {
   private final SdkGenApiService delegate = SdkGenApiServiceFactory.getSdkGenApi();

    @OPTIONS
    @GET
    @Path("/languages")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get a list of supported SDK languages ", notes = "This operation will provide a list of programming languages that are supported by the swagger codegen library for generating System Development Kits (SDKs) for APIs available in the API Manager Store ", response = void.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "SDK Languages", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. List of supported languages for generating SDKs. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The list of languages is not found. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error. Error while retrieving the list. ", response = void.class) })
    public Response sdkGenLanguagesGet( @Context Request request)
    throws NotFoundException {
        
        return delegate.sdkGenLanguagesGet(request);
    }
}
