package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.store.v1.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ImportApiServiceImpl;
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
@Path("/import")

@Api(description = "the import API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ImportApi  {

  @Context MessageContext securityContext;

ImportApiService delegate = new ImportApiServiceImpl();


    @POST
    @Path("/applications")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Imports an Application.", notes = "This operation can be used to import an existing Application. ", response = ApplicationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Import and Export Applications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = ApplicationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response importApplicationsPost( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail) throws APIManagementException{
        return delegate.importApplicationsPost(fileInputStream, fileDetail, securityContext);
    }

    @PUT
    @Path("/applications")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Imports an Updates an Application.", notes = "This operation can be used to import an existing Application. ", response = ApplicationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Import and Export Applications" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the updated object as entity in the body. ", response = ApplicationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response importApplicationsPut( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail) throws APIManagementException{
        return delegate.importApplicationsPut(fileInputStream, fileDetail, securityContext);
    }
}
