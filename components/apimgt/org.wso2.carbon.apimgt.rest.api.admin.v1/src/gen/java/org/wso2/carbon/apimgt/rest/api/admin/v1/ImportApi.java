package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.ImportApiServiceImpl;
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
    @Path("/api")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import an API", notes = "This operation can be used to import an API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs")
        })
    }, tags={ "API (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. API Imported Successfully. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden Not Authorized to import. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API to update not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. API to import already exists. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. Error in importing API. ", response = ErrorDTO.class) })
    public Response importApiPost( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "Preserve Original Provider of the API. This is the user choice to keep or replace the API provider. ")  @QueryParam("preserveProvider") Boolean preserveProvider,  @ApiParam(value = "Whether to update the API or not. This is used when updating already existing APIs. ")  @QueryParam("overwrite") Boolean overwrite) throws APIManagementException{
        return delegate.importApiPost(fileInputStream, fileDetail, preserveProvider, overwrite, securityContext);
    }

    @POST
    @Path("/api-product")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import an API Product", notes = "This operation can be used to import an API Product. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products")
        })
    }, tags={ "API Product (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. API Product Imported Successfully. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden Not Authorized to import. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API Product to update not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. API Product to import already exists. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. Error in importing API Product. ", response = ErrorDTO.class) })
    public Response importApiProductPost( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "Preserve Original Provider of the API Product. This is the user choice to keep or replace the API Product provider. ")  @QueryParam("preserveProvider") Boolean preserveProvider,  @ApiParam(value = "Whether to import the dependent APIs or not. ")  @QueryParam("importAPIs") Boolean importAPIs,  @ApiParam(value = "Whether to update the API Product or not. This is used when updating already existing API Products. ")  @QueryParam("overwriteAPIProduct") Boolean overwriteAPIProduct,  @ApiParam(value = "Whether to update the dependent APIs or not. This is used when updating already existing dependent APIs of an API Product. ")  @QueryParam("overwriteAPIs") Boolean overwriteAPIs) throws APIManagementException{
        return delegate.importApiProductPost(fileInputStream, fileDetail, preserveProvider, importAPIs, overwriteAPIProduct, overwriteAPIs, securityContext);
    }

    @POST
    @Path("/applications")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import an Application", notes = "This operation can be used to import an Application. ", response = ApplicationInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_import_export", description = "Import and export applications")
        })
    }, tags={ "Application (Individual)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the updated object information as entity in the body. ", response = ApplicationInfoDTO.class),
        @ApiResponse(code = 207, message = "Multi Status. Partially successful response with skipped APIs information object as entity in the body. ", response = APIInfoListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response importApplicationsPost( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "Preserve Original Creator of the Application ")  @QueryParam("preserveOwner") Boolean preserveOwner,  @ApiParam(value = "Skip importing Subscriptions of the Application ")  @QueryParam("skipSubscriptions") Boolean skipSubscriptions,  @ApiParam(value = "Expected Owner of the Application in the Import Environment ")  @QueryParam("appOwner") String appOwner,  @ApiParam(value = "Skip importing Keys of the Application ")  @QueryParam("skipApplicationKeys") Boolean skipApplicationKeys,  @ApiParam(value = "Update if application exists ")  @QueryParam("update") Boolean update) throws APIManagementException{
        return delegate.importApplicationsPost(fileInputStream, fileDetail, preserveOwner, skipSubscriptions, appOwner, skipApplicationKeys, update, securityContext);
    }
}
