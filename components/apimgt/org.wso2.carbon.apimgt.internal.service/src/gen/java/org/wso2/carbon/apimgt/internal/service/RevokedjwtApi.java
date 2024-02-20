package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedEventsDTO;
import org.wso2.carbon.apimgt.internal.service.RevokedjwtApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RevokedjwtApiServiceImpl;
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
@Path("/revokedjwt")

@Api(description = "the revokedjwt API")

@Produces({ "application/json" })


public class RevokedjwtApi  {

  @Context MessageContext securityContext;

RevokedjwtApiService delegate = new RevokedjwtApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "JTIs of revoked jwt tokens and application and subject entity revocation event data", notes = "This will provide access to    - JTIs of directly revoked JWT tokens in database   - consumer application information of revoked JWTs due to application change events   - subject entity information of revoked JWTs due to user change events ", response = RevokedEventsDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An object of revoke JWTs, revoked subject entities, revoked consumer keys", response = RevokedEventsDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response revokedjwtGet() throws APIManagementException{
        return delegate.revokedjwtGet(securityContext);
    }
}
