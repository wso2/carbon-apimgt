package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.OrganizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.MeApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.MeApiServiceImpl;
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


    @GET
    @Path("/organization-information")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the Organization information of the user", notes = "Using this operation, logged-in user can get their organization information. ", response = OrganizationInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_settings", description = "Retrieve admin settings")
        })
    }, tags={ "Users" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Key Manager list returned ", response = OrganizationInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response organizationInformation() throws APIManagementException{
        return delegate.organizationInformation(securityContext);
    }
}
