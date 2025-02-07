package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.v1.OrgThemesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.OrgThemesApiServiceImpl;
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
@Path("/org-themes")

@Api(description = "the org-themes API")




public class OrgThemesApi  {

  @Context MessageContext securityContext;

OrgThemesApiService delegate = new OrgThemesApiServiceImpl();


    @DELETE
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an org theme", notes = "Deletes the org theme for the given ID.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tenant_theme_manage", description = "Manage tenant themes")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully deleted", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteOrgTheme(@ApiParam(value = "",required=true) @PathParam("id") String id) throws APIManagementException{
        return delegate.deleteOrgTheme(id, securityContext);
    }

    @GET
    @Path("/{id}/content")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Retrieve org theme as zip", notes = "Returns the org theme as a zip file for the given ID.", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tenant_theme_manage", description = "Manage tenant themes")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns the org theme zip", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getOrgThemeContent(@ApiParam(value = "",required=true) @PathParam("id") String id) throws APIManagementException{
        return delegate.getOrgThemeContent(id, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve UUIDs of org-theme contents", notes = "Returns the UUIDs of org-theme contents and their publish status.", response = ContentPublishStatusResponseDTO.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tenant_theme_manage", description = "Manage tenant themes")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of org themes", response = ContentPublishStatusResponseDTO.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getOrgThemes( @ApiParam(value = "Filter themes based on published status")  @QueryParam("publish") Boolean publish) throws APIManagementException{
        return delegate.getOrgThemes(publish, securityContext);
    }

    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import org theme", notes = "Imports a drafted zip of an org theme to APIM.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tenant_theme_manage", description = "Manage tenant themes")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully imported", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 413, message = "Payload Too Large. Request entity is larger than limits defined by server.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response importOrgTheme( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail) throws APIManagementException{
        return delegate.importOrgTheme(fileInputStream, fileDetail, securityContext);
    }

    @POST
    @Path("/{id}/status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update publish status of an org theme", notes = "Publishes or unpublishes an org theme to the dev portal.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:tenant_theme_manage", description = "Manage tenant themes")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully updated status", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response updateOrgThemeStatus(@ApiParam(value = "",required=true) @PathParam("id") String id, @ApiParam(value = "" ,required=true) ContentPublishStatusDTO contentPublishStatusDTO) throws APIManagementException{
        return delegate.updateOrgThemeStatus(id, contentPublishStatusDTO, securityContext);
    }
}
