package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CurrentAndNewPasswordsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.MeApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.MeApiServiceImpl;
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
@Path("/me")

@Api(description = "the me API")




public class MeApi  {

  @Context MessageContext securityContext;

MeApiService delegate = new MeApiServiceImpl();


    @POST
    @Path("/change-password")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Change the Password of the user", notes = "Using this operation, logged-in user can change their password. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Users" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. User password changed successfully", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response changeUserPassword(@ApiParam(value = "Current and new password of the user " ,required=true) CurrentAndNewPasswordsDTO currentAndNewPasswordsDTO) throws APIManagementException{
        return delegate.changeUserPassword(currentAndNewPasswordsDTO, securityContext);
    }
}
