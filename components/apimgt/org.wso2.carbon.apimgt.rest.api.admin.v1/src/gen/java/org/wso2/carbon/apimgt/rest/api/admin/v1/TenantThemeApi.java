package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantThemeApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.TenantThemeApiServiceImpl;
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
@Path("/tenant-theme")

@Api(description = "the tenant-theme API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class TenantThemeApi  {

  @Context MessageContext securityContext;

TenantThemeApiService delegate = new TenantThemeApiServiceImpl();


    @POST
    @Path("/import")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import a DevPortal Tenant Theme", notes = "This operation can be used to import a DevPortal tenant theme. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:tenant_theme_manage", description = "Manage tenant themes")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok. Theme Imported Successfully. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. Not Authorized to import. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Tenant does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. Error in importing Theme. ", response = ErrorDTO.class) })
    public Response tenantThemeImportPost( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail) throws APIManagementException{
        return delegate.tenantThemeImportPost(fileInputStream, fileDetail, securityContext);
    }
}
