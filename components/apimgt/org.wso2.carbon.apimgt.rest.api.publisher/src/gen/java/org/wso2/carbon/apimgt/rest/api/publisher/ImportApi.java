package org.wso2.carbon.apimgt.rest.api.publisher;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ImportApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.ImportApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1.[\\d]+/import")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/import")
@io.swagger.annotations.Api(description = "the import API")
public class ImportApi implements Microservice  {
   private final ImportApiService delegate = ImportApiServiceFactory.getImportApi();

    @POST
    @OPTIONS
    @Path("/apis")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports API(s).", notes = "This operation can be used to import one or more existing APIs. ", response = APIListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Import Configuration", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = APIListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = APIListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = APIListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = APIListDTO.class) })
    public Response importApisPost(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FileInfo fileDetail
,@ApiParam(value = "If defined, updates the existing provider of each API with the specified provider. This is to cater scenarios where the current API provider does not exist in the environment that the API is imported to. ") @QueryParam("provider") String provider
, @Context Request request)
    throws NotFoundException {
        return delegate.importApisPost(fileInputStream, fileDetail,provider, request);
    }
    @PUT
    @OPTIONS
    @Path("/apis")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports API(s).", notes = "This operation can be used to import one or more existing APIs. ", response = APIListDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:api_update", description = "Update API")
        })
    }, tags={ "Import Configuration", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = APIListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = APIListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = APIListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = APIListDTO.class) })
    public Response importApisPut(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FileInfo fileDetail
,@ApiParam(value = "If defined, updates the existing provider of each API with the specified provider. This is to cater scenarios where the current API provider does not exist in the environment that the API is imported to. ") @QueryParam("provider") String provider
, @Context Request request)
    throws NotFoundException {
        return delegate.importApisPut(fileInputStream, fileDetail,provider, request);
    }
}
