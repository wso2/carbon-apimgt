package org.wso2.carbon.apimgt.rest.api.store;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.store.factories.ImportApiServiceFactory;

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
    name = "org.wso2.carbon.apimgt.rest.api.store.ImportApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/store/v1.[\\d]+/import")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/import")
@io.swagger.annotations.Api(description = "the import API")
public class ImportApi implements Microservice  {
   private final ImportApiService delegate = ImportApiServiceFactory.getImportApi();

    @OPTIONS
    @POST
    @Path("/applications")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports an Application.", notes = "This operation can be used to import an existing Application. ", response = ApplicationDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ApplicationDTO.class) })
    public Response importApplicationsPost(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FileInfo fileDetail
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.importApplicationsPost(fileInputStream, fileDetail,request);
    }
    @OPTIONS
    @PUT
    @Path("/applications")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports an Updates an Application.", notes = "This operation can be used to import an existing Application. ", response = ApplicationDTO.class, authorizations = {
        @io.swagger.annotations.Authorization(value = "OAuth2Security", scopes = {
            @io.swagger.annotations.AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Application (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ApplicationDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ApplicationDTO.class) })
    public Response importApplicationsPut(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FileInfo fileDetail
 ,@Context Request request)
    throws NotFoundException {
        
        return delegate.importApplicationsPut(fileInputStream, fileDetail,request);
    }
}
