package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedConditionsDTO;
import org.wso2.carbon.apimgt.internal.service.RevokeConditionsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RevokeConditionsApiServiceImpl;
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
@Path("/revoke-conditions")

@Api(description = "the revoke-conditions API")

@Produces({ "application/json" })


public class RevokeConditionsApi  {

  @Context MessageContext securityContext;

RevokeConditionsApiService delegate = new RevokeConditionsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "get JTIs of revoked jwt tokens, users and consumer keys of revoked jwt tokens", notes = "This will provide access to JTIs of revoked JWT tokens in database. Users of revoked JWTs by internal user events. Consumer keys of revoked JWTs by internal consumer key events. ", response = RevokedConditionsDTO.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An object containing arrays of revoke JWTs, revoked users, revoked consumer keys.", response = RevokedConditionsDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response revokeConditionsGet() throws APIManagementException{
        return delegate.revokeConditionsGet(securityContext);
    }
}
