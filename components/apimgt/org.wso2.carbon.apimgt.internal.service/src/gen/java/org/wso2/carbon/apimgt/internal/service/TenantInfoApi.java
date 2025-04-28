package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.TenantInfoListDTO;
import org.wso2.carbon.apimgt.internal.service.TenantInfoApiService;
import org.wso2.carbon.apimgt.internal.service.impl.TenantInfoApiServiceImpl;
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




public class TenantInfoApi  {

  @Context MessageContext securityContext;

TenantInfoApiService delegate = new TenantInfoApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve tenant information", notes = "This will provide access to tenant information. ", response = TenantInfoListDTO.class, tags={ "Tenant" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Tenant information received successfully", response = TenantInfoListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response tenantInfoGet( @NotNull  @ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "")  @QueryParam("tenants") String tenants) throws APIManagementException{
        return delegate.tenantInfoGet(xWSO2Tenant, tenants, securityContext);
    }
}
