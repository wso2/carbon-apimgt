package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDefinitionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDefinitionsListDTO;
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
    @ApiOperation(value = "Add a new shared operation policy", notes = "This operation can be used to add a new shared operation policy. ", response = OperationPolicyDefinitionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_create", description = "Create mediation policies"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies"),
            @AuthorizationScope(scope = "apim:api_mediation_policy_manage", description = "View, create, update and remove API specific mediation policies")
        })
    }, tags={ "API Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Shared operation policy uploaded ", response = OperationPolicyDefinitionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response addSharedOperationPolicy( @Multipart(value = "sharedPolicySpecFile", required = false) InputStream sharedPolicySpecFileInputStream, @Multipart(value = "sharedPolicySpecFile" , required = false) Attachment sharedPolicySpecFileDetail,  @Multipart(value = "sharedPolicyDefinitionFile", required = false) InputStream sharedPolicyDefinitionFileInputStream, @Multipart(value = "sharedPolicyDefinitionFile" , required = false) Attachment sharedPolicyDefinitionFileDetail) throws APIManagementException{
        return delegate.addSharedOperationPolicy(sharedPolicySpecFileInputStream, sharedPolicySpecFileDetail, sharedPolicyDefinitionFileInputStream, sharedPolicyDefinitionFileDetail, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all shared operation policies ", notes = "This operation provides you a list of all shared operation policies ", response = OperationPolicyDefinitionsListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies"),
            @AuthorizationScope(scope = "apim:api_mediation_policy_manage", description = "View, create, update and remove API specific mediation policies")
        })
    }, tags={ "API Operation Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying policies is returned. ", response = OperationPolicyDefinitionsListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAllSharedOperationPolicies( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "-Not supported yet-")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.getAllSharedOperationPolicies(limit, offset, query, securityContext);
    }
}
