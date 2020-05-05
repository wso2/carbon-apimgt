package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokeAPIKeyDTO;
import org.wso2.carbon.apimgt.internal.service.ApikeyApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ApikeyApiServiceImpl;
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
@Path("/apikey")

@Api(description = "the apikey API")

@Produces({ "application/json" })


public class ApikeyApi  {

  @Context MessageContext securityContext;

ApikeyApiService delegate = new ApikeyApiServiceImpl();


    @POST
    @Path("/revoke")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Revoke given API Key", notes = "Revoke and notify the provided API Key", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Api key revoked successfully. ", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response apikeyRevokePost(@ApiParam(value = "API Key revoke request object " ) RevokeAPIKeyDTO body) throws APIManagementException{
        return delegate.apikeyRevokePost(body, securityContext);
    }
}
