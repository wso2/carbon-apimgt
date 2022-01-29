package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SharedOperationPolicyApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.SharedOperationPolicyApiServiceImpl;
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
@Path("/shared-operation-policy")

@Api(description = "the shared-operation-policy API")




public class SharedOperationPolicyApi  {

  @Context MessageContext securityContext;

SharedOperationPolicyApiService delegate = new SharedOperationPolicyApiServiceImpl();


    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new shared operation policy", notes = "This operation can be used to add a new shared operation policy. ", response = OperationPolicyDataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_create", description = "Create mediation policies"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies"),
            @AuthorizationScope(scope = "apim:api_mediation_policy_manage", description = "View, create, update and remove API specific mediation policies")
        })
    }, tags={ "Shared Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Shared operation policy uploaded ", response = OperationPolicyDataDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response addSharedOperationPolicy( @Multipart(value = "sharedPolicySpecFile", required = false) InputStream sharedPolicySpecFileInputStream, @Multipart(value = "sharedPolicySpecFile" , required = false) Attachment sharedPolicySpecFileDetail,  @Multipart(value = "sharedPolicyDefinitionFile", required = false) InputStream sharedPolicyDefinitionFileInputStream, @Multipart(value = "sharedPolicyDefinitionFile" , required = false) Attachment sharedPolicyDefinitionFileDetail) throws APIManagementException{
        return delegate.addSharedOperationPolicy(sharedPolicySpecFileInputStream, sharedPolicySpecFileDetail, sharedPolicyDefinitionFileInputStream, sharedPolicyDefinitionFileDetail, securityContext);
    }

    @DELETE
    @Path("/{operationPolicyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Shared Operation Policy", notes = "This operation can be used to delete an existing shared opreation policy by providing the Id of the policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies"),
            @AuthorizationScope(scope = "apim:api_mediation_policy_manage", description = "View, create, update and remove API specific mediation policies")
        })
    }, tags={ "Shared Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deleteSharedOperationPolicyByPolicyId(@ApiParam(value = "Operation policy Id ",required=true) @PathParam("operationPolicyId") String operationPolicyId) throws APIManagementException{
        return delegate.deleteSharedOperationPolicyByPolicyId(operationPolicyId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all shared operation policies ", notes = "This operation provides you a list of all shared operation policies ", response = OperationPolicyDataListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies"),
            @AuthorizationScope(scope = "apim:api_mediation_policy_manage", description = "View, create, update and remove API specific mediation policies")
        })
    }, tags={ "Shared Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying policies is returned. ", response = OperationPolicyDataListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAllSharedOperationPolicies( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "-Not supported yet-")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.getAllSharedOperationPolicies(limit, offset, query, securityContext);
    }

    @GET
    @Path("/{operationPolicyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the shared operation policy by policy ID", notes = "This operation can be used to retrieve a particular shared operation policy. ", response = OperationPolicyDataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies"),
            @AuthorizationScope(scope = "apim:api_mediation_policy_manage", description = "View, create, update and remove API specific mediation policies")
        })
    }, tags={ "Shared Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Operation policy returned. ", response = OperationPolicyDataDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getSharedOperationPolicyByPolicyId(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Operation policy Id ",required=true) @PathParam("operationPolicyId") String operationPolicyId) throws APIManagementException{
        return delegate.getSharedOperationPolicyByPolicyId(apiId, operationPolicyId, securityContext);
    }

    @GET
    @Path("/{operationPolicyId}/content")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Download an Shared Operation Policy", notes = "This operation can be used to download a particular shared operation policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies"),
            @AuthorizationScope(scope = "apim:api_mediation_policy_manage", description = "View, create, update and remove API specific mediation policies")
        })
    }, tags={ "Shared Operation Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Operation policy returned. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getSharedOperationPolicyContentByPolicyId(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Operation policy Id ",required=true) @PathParam("operationPolicyId") String operationPolicyId) throws APIManagementException{
        return delegate.getSharedOperationPolicyContentByPolicyId(apiId, operationPolicyId, securityContext);
    }
}
