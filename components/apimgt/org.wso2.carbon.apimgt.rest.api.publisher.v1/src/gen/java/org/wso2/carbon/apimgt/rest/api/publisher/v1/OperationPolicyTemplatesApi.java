package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDefinitionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDefinitionsListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.OperationPolicyTemplatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.OperationPolicyTemplatesApiServiceImpl;
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
@Path("/operation-policy-templates")

@Api(description = "the operation-policy-templates API")




public class OperationPolicyTemplatesApi  {

  @Context MessageContext securityContext;

OperationPolicyTemplatesApiService delegate = new OperationPolicyTemplatesApiServiceImpl();


    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new operation policy template", notes = "This operation can be used to add a new operation policy template. ", response = OperationPolicyDefinitionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:mediation_policy_create", description = "Create mediation policies"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies"),
            @AuthorizationScope(scope = "apim:api_mediation_policy_manage", description = "View, create, update and remove API specific mediation policies")
        })
    }, tags={ "API Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Operation policy template uploaded ", response = OperationPolicyDefinitionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response addOperationPolicyTemplate( @Multipart(value = "templateSpecFile", required = false) InputStream templateSpecFileInputStream, @Multipart(value = "templateSpecFile" , required = false) Attachment templateSpecFileDetail,  @Multipart(value = "templateDefinitionFile", required = false) InputStream templateDefinitionFileInputStream, @Multipart(value = "templateDefinitionFile" , required = false) Attachment templateDefinitionFileDetail, @Multipart(value = "templateName", required = false)  String templateName, @Multipart(value = "flow", required = false)  String flow) throws APIManagementException{
        return delegate.addOperationPolicyTemplate(templateSpecFileInputStream, templateSpecFileDetail, templateDefinitionFileInputStream, templateDefinitionFileDetail, templateName, flow, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all operation policy templates ", notes = "This operation provides you a list of all operation policy templates ", response = OperationPolicyDefinitionsListDTO.class, authorizations = {
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
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class) })
    public Response getAllOperationPolicyTemplates( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "-Not supported yet-")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.getAllOperationPolicyTemplates(limit, offset, query, securityContext);
    }
}
