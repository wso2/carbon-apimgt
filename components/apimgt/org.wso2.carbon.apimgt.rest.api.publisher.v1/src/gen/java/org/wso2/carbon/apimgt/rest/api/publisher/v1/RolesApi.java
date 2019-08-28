package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.RolesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.RolesApiServiceImpl;
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
@Path("/roles")

@Api(description = "the roles API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class RolesApi  {

  @Context MessageContext securityContext;

RolesApiService delegate = new RolesApiServiceImpl();


    @HEAD
    @Path("/{roleId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Check given role name is already exist", notes = "Using this operation, user can check a given role name exists or not. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Roles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested role name exists.", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested role name does not exist.", response = Void.class) })
    public Response rolesRoleIdHead(@ApiParam(value = "The Base 64 URL encoded role name with domain. If the given role is in secondary user-store, role ID should be derived as Base64URLEncode({user-store-name}/{role-name}). If the given role is in PRIMARY user-store, role ID can be derived as Base64URLEncode(role-name) ",required=true) @PathParam("roleId") String roleId) throws APIManagementException{
        return delegate.rolesRoleIdHead(roleId, securityContext);
    }
}
