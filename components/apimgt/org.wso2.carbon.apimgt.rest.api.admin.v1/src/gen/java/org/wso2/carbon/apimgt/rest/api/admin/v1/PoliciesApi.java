package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.PoliciesApiServiceImpl;
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
@Path("/policies")

@Api(description = "the policies API")




public class PoliciesApi  {

  @Context MessageContext securityContext;

PoliciesApiService delegate = new PoliciesApiServiceImpl();


    @GET
    @Path("/mediation")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Global Mediation Policies ", notes = "This operation provides you a list of all available global mediation policies. ", response = MediationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies")
        })
    }, tags={ "Mediation Policy (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of mediation policies is returned. ", response = MediationListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response policiesMediationGet( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "-Not supported yet-")  @QueryParam("query") String query,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.policiesMediationGet(limit, offset, query, accept, securityContext);
    }

    @DELETE
    @Path("/mediation/{mediationPolicyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Global Mediation Policy", notes = "This operation can be used to delete an existing global mediation policy providing the Id of the mediation policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_create", description = "Create and update mediation policies")
        })
    }, tags={ "Mediation Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Mediation policy successfully deleted. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response policiesMediationMediationPolicyIdDelete(@ApiParam(value = "Mediation policy Id ",required=true) @PathParam("mediationPolicyId") String mediationPolicyId) throws APIManagementException{
        return delegate.policiesMediationMediationPolicyIdDelete(mediationPolicyId, securityContext);
    }

    @GET
    @Path("/mediation/{mediationPolicyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Global Mediation Policy", notes = "This operation can be used to retrieve a particular global mediation policy. ", response = MediationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies")
        })
    }, tags={ "Mediation Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Mediation Policy returned. ", response = MediationDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response policiesMediationMediationPolicyIdGet(@ApiParam(value = "Mediation policy Id ",required=true) @PathParam("mediationPolicyId") String mediationPolicyId,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.policiesMediationMediationPolicyIdGet(mediationPolicyId, accept, securityContext);
    }

    @PUT
    @Path("/mediation/{mediationPolicyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Global Mediation Policy", notes = "This operation can be used to update an existing global mediation policy. ", response = MediationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_create", description = "Create and update mediation policies")
        })
    }, tags={ "Mediation Policy (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated mediation policy object ", response = MediationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response policiesMediationMediationPolicyIdPut(@ApiParam(value = "Mediation policy Id ",required=true) @PathParam("mediationPolicyId") String mediationPolicyId,  @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "Mediation policy object that needs to be updated " ,required=true) MediationDTO mediationDTO) throws APIManagementException{
        return delegate.policiesMediationMediationPolicyIdPut(mediationPolicyId, contentType, mediationDTO, securityContext);
    }

    @POST
    @Path("/mediation")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a Global Mediation Policy", notes = "This operation can be used to add a new global mediation policy. ", response = MediationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_create", description = "Create and update mediation policies")
        })
    }, tags={ "Mediation Policy (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Mediation policy added successfully. ", response = MediationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response policiesMediationPost( @NotNull  @ApiParam(value = "Media type of the entity in the body. Default is application/json. " ,required=true, defaultValue="application/json")@HeaderParam("Content-Type") String contentType, @ApiParam(value = "mediation policy to upload" ,required=true) MediationDTO mediationDTO) throws APIManagementException{
        return delegate.policiesMediationPost(contentType, mediationDTO, securityContext);
    }
}
