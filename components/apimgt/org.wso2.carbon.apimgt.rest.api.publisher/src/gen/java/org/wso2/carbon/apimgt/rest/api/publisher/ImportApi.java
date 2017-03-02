package org.wso2.carbon.apimgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ImportApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
        name = "org.wso2.carbon.apimgt.import.export.ImportApi",
        service = Microservice.class,
        immediate = true) @Path("/api/am/publisher/v1/import") @Consumes({ "application/json" }) @Produces({
        "application/json" }) @io.swagger.annotations.Api(description = "the import API") @javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-19T18:57:34.679+05:30") public class ImportApi
        implements Microservice {
    private final ImportApiService delegate = ImportApiServiceFactory.getImportApi();

    @POST
    @Path("/apis")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Imports API(s).", notes = "This operation can be used to import one or more existing APIs. ", response = APIListDTO.class, tags={ "Import Configuration", })
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = APIListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = APIListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = APIListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = APIListDTO.class) })
    public Response importApisPost(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FileInfo fileDetail
            ,@ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType
            ,@ApiParam(value = "If defined, updates the existing provider of each API with the specified provider. This is to cater scenarios where the current API provider does not exist in the environment that the API is imported to. ") @QueryParam("provider") String provider
    )
            throws NotFoundException {
        return delegate.importApisPost(fileInputStream, fileDetail,contentType,provider);
    }

    @PUT @Path("/apis") @Consumes({ "multipart/form-data" }) @Produces({
            "application/json" }) @io.swagger.annotations.ApiOperation(value = "Imports API(s).", notes = "This operation can be used to import one or more existing APIs. ", response = APIListDTO.class, tags = {
            "Import Configuration", }) @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body.", response = APIListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = APIListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = APIListDTO.class),

            @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = APIListDTO.class) }) public Response importApisPut(
            @FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FileInfo fileDetail,
            @ApiParam(value = "Media type of the entity in the body. Default is application/json. ", required = true, defaultValue = "application/json") @HeaderParam("Content-Type") String contentType,
            @ApiParam(value = "If defined, updates the existing provider of each API with the specified provider. This is to cater scenarios where the current API provider does not exist in the environment that the API is imported to. ") @QueryParam("provider") String provider)
            throws NotFoundException {
        return delegate.importApisPut(fileInputStream, fileDetail, contentType, provider);
    }
}
