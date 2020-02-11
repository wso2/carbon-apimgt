package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.CustomUrlApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.CustomUrlApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomUrlInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/custom-url")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/custom-url", description = "the custom-url API")
public class CustomUrlApi  {

   private final CustomUrlApiService delegate = CustomUrlApiServiceFactory.getCustomUrlApi();

    @GET
    @Path("/{tenantDomain}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get custom-url info of a tenant domain\n", notes = "This operation is to get custom-url information of the provided tenant-domain\n", response = CustomUrlInfoDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nCustom url info of the tenant is retrieved.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested user does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response getCustomUrlInfoByTenantDomian(@ApiParam(value = "The tenant domain name.\n",required=true ) @PathParam("tenantDomain")  String tenantDomain)
    {
    return delegate.getCustomUrlInfoByTenantDomain(tenantDomain);
    }
}

