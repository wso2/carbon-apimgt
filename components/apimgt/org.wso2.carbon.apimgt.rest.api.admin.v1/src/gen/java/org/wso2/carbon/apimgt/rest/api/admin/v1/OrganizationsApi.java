package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.OrganizationDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.OrganizationListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.OrganizationsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.OrganizationsApiServiceImpl;
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
@Path("/organizations")

@Api(description = "the organizations API")




public class OrganizationsApi  {

  @Context MessageContext securityContext;

OrganizationsApiService delegate = new OrganizationsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all registered Organizations", notes = "Get all Registered Organizations ", response = OrganizationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:organization_read", description = "Read Organizations")
        })
    }, tags={ "Organizations",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Organizations returned ", response = OrganizationListDTO.class) })
    public Response organizationsGet() throws APIManagementException{
        return delegate.organizationsGet(securityContext);
    }

    @DELETE
    @Path("/{organizationId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an Organization", notes = "Delete an organization by organization Id ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:organization_manage", description = "Manage Organizations")
        })
    }, tags={ "Organizations",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Organization successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response organizationsOrganizationIdDelete(@ApiParam(value = "Organization UUID ",required=true) @PathParam("organizationId") String organizationId) throws APIManagementException{
        return delegate.organizationsOrganizationIdDelete(organizationId, securityContext);
    }

    @GET
    @Path("/{organizationId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get an Organization", notes = "Get an organization by organization Id ", response = OrganizationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:organization_read", description = "Read Organizations")
        })
    }, tags={ "Organizations",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Organization. ", response = OrganizationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response organizationsOrganizationIdGet(@ApiParam(value = "Organization UUID ",required=true) @PathParam("organizationId") String organizationId) throws APIManagementException{
        return delegate.organizationsOrganizationIdGet(organizationId, securityContext);
    }

    @PUT
    @Path("/{organizationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an Organization", notes = "Update an organization by organization Id ", response = OrganizationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:organization_manage", description = "Manage Organizations")
        })
    }, tags={ "Organizations",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Organization updated. ", response = OrganizationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response organizationsOrganizationIdPut(@ApiParam(value = "Organization UUID ",required=true) @PathParam("organizationId") String organizationId, @ApiParam(value = "Organization object with updated information " ,required=true) OrganizationDTO organizationDTO) throws APIManagementException{
        return delegate.organizationsOrganizationIdPut(organizationId, organizationDTO, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an Organizations", notes = "Add a new organization ", response = OrganizationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:organization_manage", description = "Manage Organizations")
        })
    }, tags={ "Organizations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created organization as entity in the body. ", response = OrganizationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response organizationsPost(@ApiParam(value = "Organization object that should to be added " ,required=true) OrganizationDTO organizationDTO) throws APIManagementException{
        return delegate.organizationsPost(organizationDTO, securityContext);
    }
}
