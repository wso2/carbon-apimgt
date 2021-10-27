package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantConfigApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.TenantConfigApiServiceImpl;
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
@Path("/tenant-config")

@Api(description = "the tenant-config API")




public class TenantConfigApi  {

  @Context MessageContext securityContext;

TenantConfigApiService delegate = new TenantConfigApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Export a tenant-Config.", notes = "This operation can be used to export a tenant-config.json used in deployment. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Tenant Config",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Tenant config Exported Successfully. ", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response exportTenantConfig() throws APIManagementException{
        return delegate.exportTenantConfig(securityContext);
    }

    @PUT
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a tenant-config.", notes = "This operation can be used to update tenant-config. ", response = Object.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Tenant Config" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Role mapping alias returned ", response = Object.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 413, message = "Payload Too Large. Request entity is larger than limits defined by server.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response updateTenantConfig(@ApiParam(value = "tenant-config" ,required=true) String body) throws APIManagementException{
        return delegate.updateTenantConfig(body, securityContext);
    }
}
