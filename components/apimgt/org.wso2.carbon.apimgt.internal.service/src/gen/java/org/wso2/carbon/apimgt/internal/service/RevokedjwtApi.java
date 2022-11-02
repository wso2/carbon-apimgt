package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTListDTO;
import org.wso2.carbon.apimgt.internal.service.impl.RevokedjwtApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

@Path("/revokedjwt")

@Api(description = "the revokedjwt API")

@Produces({ "application/json" })


public class RevokedjwtApi  {

  @Context MessageContext securityContext;

RevokedjwtApiService delegate = new RevokedjwtApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "JTIs of revoked jwt tokens", notes = "This will provide access to JTIs of revoked JWT tokens in database. ", response = RevokedJWTListDTO.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of revoke JWTs", response = RevokedJWTListDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response revokedjwtGet() throws APIManagementException{
        return delegate.revokedjwtGet(securityContext);
    }
}
