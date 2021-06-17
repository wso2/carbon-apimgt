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
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApplicationsApi  {

  @Context MessageContext securityContext;

ApplicationsApiService delegate = new ApplicationsApiServiceImpl();


    @POST
    @Path("/{applicationId}/change-owner")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Change Application Owner", notes = "This operation is used to change the owner of an Application. In order to change the owner of an application, we need to pass the new application owner as a query parameter ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_owner_change", description = "Retrieve and manage applications"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Application",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application owner changed successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Application does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdChangeOwnerPost( @NotNull @ApiParam(value = "",required=true)  @QueryParam("owner") String owner, @ApiParam(value = "Application UUID ",required=true) @PathParam("applicationId") String applicationId) throws APIManagementException{
        return delegate.applicationsApplicationIdChangeOwnerPost(owner, applicationId, securityContext);
    }

    @DELETE
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Remove an application ", notes = "This operation can be used to remove an application specifying its id. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_import_export", description = "Import and export applications related operations"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Applications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdDelete(@ApiParam(value = "Application UUID ",required=true) @PathParam("applicationId") String applicationId, @ApiParam(value = "Validator for conditional requests; based on ETag (Will be supported in future). " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.applicationsApplicationIdDelete(applicationId, ifMatch, securityContext);
    }

    @GET
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the details of an Application ", notes = "This operation can be used to get the details of an application by specifying its id. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_application_view", description = "View Applications"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Applications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application details returned. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested application does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ErrorDTO.class) })
    public Response applicationsApplicationIdGet(@ApiParam(value = "Application UUID ",required=true) @PathParam("applicationId") String applicationId) throws APIManagementException{
        return delegate.applicationsApplicationIdGet(applicationId, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search applications ", notes = "This operation can be used to retrieve list of applications that is belonged to the given user, If no user is provided then the application for the user associated with the provided access token will be returned. ", response = ApplicationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:app_import_export", description = "Import and export applications related operations"),
            @AuthorizationScope(scope = "apim:app_owner_change", description = "Retrieve and manage applications"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Application (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application list returned. ", response = ApplicationListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ErrorDTO.class) })
    public Response applicationsGet( @ApiParam(value = "username of the application creator ")  @QueryParam("user") String user,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource (Will be supported in future). " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Application Name ")  @QueryParam("name") String name,  @ApiParam(value = "Tenant domain of the applications to get. This has to be specified only if require to get applications of another tenant other than the requester's tenant. So, if not specified, the default will be set as the requester's tenant domain. This cross tenant Application access is allowed only for super tenant admin users only at a migration process. ")  @QueryParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.applicationsGet(user, limit, offset, accept, ifNoneMatch, name, tenantDomain, securityContext);
    }
}
