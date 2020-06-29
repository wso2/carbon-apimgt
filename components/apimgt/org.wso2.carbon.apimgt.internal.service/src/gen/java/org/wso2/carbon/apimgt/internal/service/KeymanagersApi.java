package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.internal.service.KeymanagersApiService;
import org.wso2.carbon.apimgt.internal.service.impl.KeymanagersApiServiceImpl;
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
@Path("/keymanagers")

@Api(description = "the keymanagers API")

@Produces({ "application/json" })


public class KeymanagersApi  {

  @Context MessageContext securityContext;

KeymanagersApiService delegate = new KeymanagersApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "An Array of key managers configured", notes = "this will provide key managers configured ", response = KeyManagerDTO.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of key managers", response = KeyManagerDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response keymanagersGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.keymanagersGet(xWSO2Tenant, securityContext);
    }
}
