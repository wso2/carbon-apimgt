package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.OperationPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.OperationPoliciesApiServiceImpl;
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
@Path("/operation-policies")

@Api(description = "the operation-policies API")




public class OperationPoliciesApi  {

  @Context MessageContext securityContext;

OperationPoliciesApiService delegate = new OperationPoliciesApiServiceImpl();


    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new common operation policy", notes = "This operation can be used to add a new common operation policy. ", response = OperationPolicyDataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:common_operation_policy_manage", description = "Add, Update and Delete common operation policies")
        })
    }, tags={ "Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Shared operation policy uploaded ", response = OperationPolicyDataDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response addCommonOperationPolicy( @Multipart(value = "policySpecFile", required = false) InputStream policySpecFileInputStream, @Multipart(value = "policySpecFile" , required = false) Attachment policySpecFileDetail,  @Multipart(value = "synapsePolicyDefinitionFile", required = false) InputStream synapsePolicyDefinitionFileInputStream, @Multipart(value = "synapsePolicyDefinitionFile" , required = false) Attachment synapsePolicyDefinitionFileDetail,  @Multipart(value = "ccPolicyDefinitionFile", required = false) InputStream ccPolicyDefinitionFileInputStream, @Multipart(value = "ccPolicyDefinitionFile" , required = false) Attachment ccPolicyDefinitionFileDetail) throws APIManagementException{
        return delegate.addCommonOperationPolicy(policySpecFileInputStream, policySpecFileDetail, synapsePolicyDefinitionFileInputStream, synapsePolicyDefinitionFileDetail, ccPolicyDefinitionFileInputStream, ccPolicyDefinitionFileDetail, securityContext);
    }

    @DELETE
    @Path("/{operationPolicyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a common operation policy", notes = "This operation can be used to delete an existing common opreation policy by providing the Id of the policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:common_operation_policy_manage", description = "Add, Update and Delete common operation policies"),
            @AuthorizationScope(scope = "apim:policies_import_export", description = "Export and import policies related operations")
        })
    }, tags={ "Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteCommonOperationPolicyByPolicyId(@ApiParam(value = "Operation policy Id ",required=true) @PathParam("operationPolicyId") String operationPolicyId) throws APIManagementException{
        return delegate.deleteCommonOperationPolicyByPolicyId(operationPolicyId, securityContext);
    }

    @GET
    @Path("/export")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Export an API Policy by its name and version ", notes = "This operation provides you to export a preferred common API policy ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:policies_import_export", description = "Export and import policies related operations")
        })
    }, tags={ "Import Export",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Successful. ", response = File.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response exportOperationPolicy( @ApiParam(value = "Policy name")  @QueryParam("name") String name,  @ApiParam(value = "Version of the policy")  @QueryParam("version") String version,  @ApiParam(value = "Format of the policy definition file")  @QueryParam("format") String format) throws APIManagementException{
        return delegate.exportOperationPolicy(name, version, format, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all common operation policies to all the APIs ", notes = "This operation provides you a list of all common operation policies that can be used by any API ", response = OperationPolicyDataListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:common_operation_policy_view", description = "View common operation policies"),
            @AuthorizationScope(scope = "apim:common_operation_policy_manage", description = "Add, Update and Delete common operation policies"),
            @AuthorizationScope(scope = "apim:policies_import_export", description = "Export and import policies related operations")
        })
    }, tags={ "Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying policies is returned. ", response = OperationPolicyDataListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAllCommonOperationPolicies( @ApiParam(value = "Maximum size of policy array to return. ")  @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"<attribute>:\"** modifier.  Eg. \"name:addHeader\" will match an API Policy if the provider of the API Policy contains \"addHeader\". \"version:\"v1\"\" will match an API Policy if the provider of the API Policy contains \"v1\".  Also you can use combined modifiers Eg. name:addHeader&version:v1 will match an API Policy if the name of the API Policy is addHeader and version is v1.  Supported attribute modifiers are [**version, name**]  If query attributes are provided, this returns all API policies available under the given limit.  Please note that you need to use encoded URL (URL encoding) if you are using a client which does not support URL encoding (such as curl) ")  @QueryParam("query") String query) throws APIManagementException{
        return delegate.getAllCommonOperationPolicies(limit, offset, query, securityContext);
    }

    @GET
    @Path("/{operationPolicyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the details of a common operation policy by providing policy ID", notes = "This operation can be used to retrieve a particular common operation policy. ", response = OperationPolicyDataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:common_operation_policy_view", description = "View common operation policies"),
            @AuthorizationScope(scope = "apim:common_operation_policy_manage", description = "Add, Update and Delete common operation policies"),
            @AuthorizationScope(scope = "apim:policies_import_export", description = "Export and import policies related operations")
        })
    }, tags={ "Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Operation policy returned. ", response = OperationPolicyDataDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getCommonOperationPolicyByPolicyId(@ApiParam(value = "Operation policy Id ",required=true) @PathParam("operationPolicyId") String operationPolicyId) throws APIManagementException{
        return delegate.getCommonOperationPolicyByPolicyId(operationPolicyId, securityContext);
    }

    @GET
    @Path("/{operationPolicyId}/content")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Download a common operation policy", notes = "This operation can be used to download a selected common operation policy. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:common_operation_policy_view", description = "View common operation policies"),
            @AuthorizationScope(scope = "apim:common_operation_policy_manage", description = "Add, Update and Delete common operation policies")
        })
    }, tags={ "Operation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Operation policy returned. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getCommonOperationPolicyContentByPolicyId(@ApiParam(value = "Operation policy Id ",required=true) @PathParam("operationPolicyId") String operationPolicyId) throws APIManagementException{
        return delegate.getCommonOperationPolicyContentByPolicyId(operationPolicyId, securityContext);
    }

    @POST
    @Path("/import")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import an API Policy", notes = "This operation can be used to import an API Policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:policies_import_export", description = "Export and import policies related operations")
        })
    }, tags={ "Import Export" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. Policy Imported Successfully. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response importOperationPolicy( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail) throws APIManagementException{
        return delegate.importOperationPolicy(fileInputStream, fileDetail, securityContext);
    }
}
