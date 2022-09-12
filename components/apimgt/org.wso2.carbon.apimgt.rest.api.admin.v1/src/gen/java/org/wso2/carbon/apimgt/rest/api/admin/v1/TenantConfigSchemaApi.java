package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantConfigSchemaApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.TenantConfigSchemaApiServiceImpl;
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
@Path("/tenant-config-schema")

@Api(description = "the tenant-config-schema API")




public class TenantConfigSchemaApi  {

  @Context MessageContext securityContext;

TenantConfigSchemaApiService delegate = new TenantConfigSchemaApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Export a tenant-Config-Schema.", notes = "This operation can be used to export a tenant-config-schema.json used in deployment. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Tenant Config Schema" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Tenant config schema exported successfully. ", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response exportTenantConfigSchema() throws APIManagementException{
        return delegate.exportTenantConfigSchema(securityContext);
    }
}
