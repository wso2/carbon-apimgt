package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.TenantInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantInfoApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.TenantInfoApiServiceImpl;
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
@Path("/tenant-info")

@Api(description = "the tenant-info API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class TenantInfoApi  {

  @Context MessageContext securityContext;

TenantInfoApiService delegate = new TenantInfoApiServiceImpl();


    @GET
    @Path("/{username}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get tenant id of the user ", notes = "This operation is to get tenant id of the provided user ", response = TenantInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:tenantInfo", description = "Retrieve tenant related information")
        })
    }, tags={ "Tenants" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Tenant id of the user retrieved. ", response = TenantInfoDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested user does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response getTenantInfoByUsername(@ApiParam(value = "The state represents the current state of the tenant  Supported states are [ active, inactive] ",required=true) @PathParam("username") String username) throws APIManagementException{
        return delegate.getTenantInfoByUsername(username, securityContext);
    }
}
