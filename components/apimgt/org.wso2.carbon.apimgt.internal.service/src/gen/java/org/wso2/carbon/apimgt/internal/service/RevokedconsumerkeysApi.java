package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTConsumerKeyListDTO;
import org.wso2.carbon.apimgt.internal.service.RevokedconsumerkeysApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RevokedconsumerkeysApiServiceImpl;
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
@Path("/revokedconsumerkeys")

@Api(description = "the revokedconsumerkeys API")

@Produces({ "application/json" })


public class RevokedconsumerkeysApi  {

  @Context MessageContext securityContext;

RevokedconsumerkeysApiService delegate = new RevokedconsumerkeysApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Consumer keys of revoked jwt", notes = "This will provide access to Consumer keys of revoked JWT's in database. ", response = RevokedJWTConsumerKeyListDTO.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of revoke JWTs", response = RevokedJWTConsumerKeyListDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response revokedconsumerkeysGet() throws APIManagementException{
        return delegate.revokedconsumerkeysGet(securityContext);
    }
}
