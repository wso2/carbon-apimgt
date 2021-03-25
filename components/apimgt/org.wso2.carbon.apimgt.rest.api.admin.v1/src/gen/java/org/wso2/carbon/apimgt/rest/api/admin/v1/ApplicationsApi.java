package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.ApplicationsApiServiceImpl;
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
@Path("/applications")

@Api(description = "the applications API")




public class ApplicationsApi  {

  @Context MessageContext securityContext;

ApplicationsApiService delegate = new ApplicationsApiServiceImpl();


    @POST
    @Path("/{applicationId}/change-owner")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Change Application Owner", notes = "This operation is used to change the owner of an Application. In order to change the owner of an application, we need to pass the new application owner as a query parameter ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:app_owner_change", description = "Retrieve and manage applications")
        })
    }, tags={ "Application",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application owner changed successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response applicationsApplicationIdChangeOwnerPost( @NotNull @ApiParam(value = "",required=true)  @QueryParam("owner") String owner, @ApiParam(value = "Application UUID ",required=true) @PathParam("applicationId") String applicationId) throws APIManagementException{
        return delegate.applicationsApplicationIdChangeOwnerPost(owner, applicationId, securityContext);
    }

    @DELETE
    @Path("/{applicationId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an Application ", notes = "This operation can be used to delete an application by specifying its id. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:app_import_export", description = "Import and export applications related operations")
        })
    }, tags={ "Applications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response applicationsApplicationIdDelete(@ApiParam(value = "Application UUID ",required=true) @PathParam("applicationId") String applicationId) throws APIManagementException{
        return delegate.applicationsApplicationIdDelete(applicationId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search Applications ", notes = "This operation can be used to retrieve list of applications owned by the given user, If no user is provided, the applications owned by the user associated with the provided access token will be returned. ", response = ApplicationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:app_owner_change", description = "Retrieve and manage applications"),
            @AuthorizationScope(scope = "apim:app_import_export", description = "Import and export applications related operations")
        })
    }, tags={ "Application (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application list returned. ", response = ApplicationListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response applicationsGet( @ApiParam(value = "username of the application creator ")  @QueryParam("user") String user,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept,  @ApiParam(value = "Application Name ")  @QueryParam("name") String name,  @ApiParam(value = "Tenant domain of the applications to get. This has to be specified only if it is required to get applications of a tenant other than the requester's tenant. So, if not specified, the default will be set as the requester's tenant domain. This cross tenant Application access is allowed only for super tenant admin users **only at a migration process**. ")  @QueryParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.applicationsGet(user, limit, offset, accept, name, tenantDomain, securityContext);
    }
}
