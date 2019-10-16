package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.TenantInfoApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.TenantInfoApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TenantInfoDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/tenant-info")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/tenant-info", description = "the tenant-info API")
public class TenantInfoApi  {

   private final TenantInfoApiService delegate = TenantInfoApiServiceFactory.getTenantInfoApi();

    @GET
    @Path("/{username}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get tenant id of the user\n", notes = "This operation is to get tenant id of the provided user\n", response = TenantInfoDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTenant id of the user retrieved.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested user does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response getTenantInfoByUsername(@ApiParam(value = "The state represents the current state of the tenant\n\nSupported states are [ active, inactive]\n",required=true ) @PathParam("username")  String username)
    {
    return delegate.getTenantInfoByUsername(username);
    }
}

