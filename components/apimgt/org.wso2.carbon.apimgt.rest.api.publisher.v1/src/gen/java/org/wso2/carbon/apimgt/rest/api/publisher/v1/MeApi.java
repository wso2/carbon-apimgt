package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.MeApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.MeApiServiceImpl;
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


    @HEAD
    @Path("/roles/{roleId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Validate Whether the Logged-in User has the Given Role", notes = "Using this operation, logged-in user can check whether he has given role. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Roles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested user has the role.", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response validateUserRole(@ApiParam(value = "The Base 64 URL encoded role name with domain. If the given role is in secondary user-store, role ID should be derived as Base64URLEncode({user-store-name}/{role-name}). If the given role is in PRIMARY user-store, role ID can be derived as Base64URLEncode(role-name) ",required=true) @PathParam("roleId") String roleId) throws APIManagementException{
        return delegate.validateUserRole(roleId, securityContext);
    }
}
