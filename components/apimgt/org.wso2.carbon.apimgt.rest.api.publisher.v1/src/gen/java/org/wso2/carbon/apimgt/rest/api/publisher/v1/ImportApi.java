package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ImportApiServiceImpl;

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
@Path("/import")

@Api(description = "the import API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ImportApi  {

  @Context MessageContext securityContext;

ImportApiService delegate = new ImportApiServiceImpl();


    @POST
    @Path("/apis")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Imports API(s).", notes = "This operation can be used to import one or more existing APIs. ", response = APIListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Import Configuration",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = APIListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response importApisPost( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "If defined, updates the existing provider of each API with the specified provider. This is to cater scenarios where the current API provider does not exist in the environment that the API is imported to. ")  @QueryParam("provider") String provider) {
        return delegate.importApisPost(fileInputStream, fileDetail, provider, securityContext);
    }

    @PUT
    @Path("/apis")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Imports API(s).", notes = "This operation can be used to import one or more existing APIs. ", response = APIListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_update", description = "Update API")
        })
    }, tags={ "Import Configuration" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = APIListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response importApisPut( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "If defined, updates the existing provider of each API with the specified provider. This is to cater scenarios where the current API provider does not exist in the environment that the API is imported to. ")  @QueryParam("provider") String provider) {
        return delegate.importApisPut(fileInputStream, fileDetail, provider, securityContext);
    }
}
