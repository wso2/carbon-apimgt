package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTUserListDTO;
import org.wso2.carbon.apimgt.internal.service.RevokedUsersApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RevokedUsersApiServiceImpl;
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
@Path("/revokedUsers")

@Api(description = "the revokedUsers API")

@Produces({ "application/json" })


public class RevokedUsersApi  {

  @Context MessageContext securityContext;

RevokedUsersApiService delegate = new RevokedUsersApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Users of revoked jwt", notes = "This will provide access to Users of revoked JWTs by internal user events. ", response = RevokedJWTUserListDTO.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of revoke JWTs", response = RevokedJWTUserListDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response revokedUsersGet() throws APIManagementException{
        return delegate.revokedUsersGet(securityContext);
    }
}
