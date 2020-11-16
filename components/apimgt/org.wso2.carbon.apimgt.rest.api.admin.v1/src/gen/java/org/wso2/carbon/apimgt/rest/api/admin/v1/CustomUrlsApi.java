package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomUrlInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.CustomUrlsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.CustomUrlsApiServiceImpl;
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
@Path("/custom-urls")

@Api(description = "the custom-urls API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class CustomUrlsApi  {

  @Context MessageContext securityContext;

CustomUrlsApiService delegate = new CustomUrlsApiServiceImpl();


    @GET
    @Path("/{tenantDomain}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Custom URL Info of a Tenant Domain ", notes = "This operation is to get custom-url information of the provided tenant-domain ", response = CustomUrlInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:tenantInfo", description = "Retrieve tenant related information"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Tenants" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Custom url info of the tenant is retrieved. ", response = CustomUrlInfoDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getCustomUrlInfoByTenantDomain(@ApiParam(value = "The tenant domain name. ",required=true) @PathParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.getCustomUrlInfoByTenantDomain(tenantDomain, securityContext);
    }
}
