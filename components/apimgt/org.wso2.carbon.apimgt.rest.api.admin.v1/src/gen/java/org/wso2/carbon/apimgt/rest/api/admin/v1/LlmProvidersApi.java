package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LLMProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LLMProviderSummaryResponseListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.LlmProvidersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.LlmProvidersApiServiceImpl;
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
@Path("/llm-providers")

@Api(description = "the llm-providers API")




public class LlmProvidersApi  {

  @Context MessageContext securityContext;

LlmProvidersApiService delegate = new LlmProvidersApiServiceImpl();


    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a LLM provider", notes = "Add a new LLM provider ", response = LLMProviderResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "LLMProviders",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created LLM provider as entity in the body. ", response = LLMProviderResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response addLlmProvider(@Multipart(value = "id", required = false)  String id, @Multipart(value = "name", required = false)  String name, @Multipart(value = "version", required = false)  String version, @Multipart(value = "headers", required = false)  List<String> headers, @Multipart(value = "queryParams", required = false)  List<String> queryParams, @Multipart(value = "description", required = false)  String description,  @Multipart(value = "apiDefinition", required = false) InputStream apiDefinitionInputStream, @Multipart(value = "apiDefinition" , required = false) Attachment apiDefinitionDetail, @Multipart(value = "modelPath", required = false)  String modelPath, @Multipart(value = "promptTokensPath", required = false)  String promptTokensPath, @Multipart(value = "completionTokensPath", required = false)  String completionTokensPath, @Multipart(value = "totalTokensPath", required = false)  String totalTokensPath, @Multipart(value = "hasMetadataInPayload", required = false)  String hasMetadataInPayload, @Multipart(value = "payloadHandler", required = false)  String payloadHandler) throws APIManagementException{
        return delegate.addLlmProvider(id, name, version, headers, queryParams, description, apiDefinitionInputStream, apiDefinitionDetail, modelPath, promptTokensPath, completionTokensPath, totalTokensPath, hasMetadataInPayload, payloadHandler, securityContext);
    }

    @DELETE
    @Path("/{llmProviderId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a LLM Provider", notes = "Delete a LLM Provider by llmProviderId ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "LLMProviders",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. LLM provider successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deleteLlmProvider(@ApiParam(value = "LLM Provider UUID ",required=true) @PathParam("llmProviderId") String llmProviderId) throws APIManagementException{
        return delegate.deleteLlmProvider(llmProviderId, securityContext);
    }

    @GET
    @Path("/{llmProviderId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get an OpenAPI specification of a LLM Provider", notes = "Get an OpenAPI specification of a LLM Provider ", response = LLMProviderResponseDTO.class, tags={ "LLMProviders",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. OpenAPI specification ", response = LLMProviderResponseDTO.class) })
    public Response getLlmProvider(@ApiParam(value = "LLM Provider UUID ",required=true) @PathParam("llmProviderId") String llmProviderId) throws APIManagementException{
        return delegate.getLlmProvider(llmProviderId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all LLM providers", notes = "Get all LLM providers ", response = LLMProviderSummaryResponseListDTO.class, tags={ "LLMProviders",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. LLM providers returned ", response = LLMProviderSummaryResponseListDTO.class) })
    public Response getLlmProviders() throws APIManagementException{
        return delegate.getLlmProviders(securityContext);
    }

    @PUT
    @Path("/{llmProviderId}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an LLM provider", notes = "Update a LLM provider by LLMProviderId ", response = LLMProviderResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "LLMProviders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. LLM Provider updated. ", response = LLMProviderResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response updateLlmProvider(@ApiParam(value = "LLM Provider UUID ",required=true) @PathParam("llmProviderId") String llmProviderId, @Multipart(value = "id", required = false)  String id, @Multipart(value = "name", required = false)  String name, @Multipart(value = "version", required = false)  String version, @Multipart(value = "headers", required = false)  List<String> headers, @Multipart(value = "queryParams", required = false)  List<String> queryParams, @Multipart(value = "description", required = false)  String description,  @Multipart(value = "apiDefinition", required = false) InputStream apiDefinitionInputStream, @Multipart(value = "apiDefinition" , required = false) Attachment apiDefinitionDetail, @Multipart(value = "modelPath", required = false)  String modelPath, @Multipart(value = "promptTokensPath", required = false)  String promptTokensPath, @Multipart(value = "completionTokensPath", required = false)  String completionTokensPath, @Multipart(value = "totalTokensPath", required = false)  String totalTokensPath, @Multipart(value = "hasMetadataInPayload", required = false)  String hasMetadataInPayload, @Multipart(value = "payloadHandler", required = false)  String payloadHandler) throws APIManagementException{
        return delegate.updateLlmProvider(llmProviderId, id, name, version, headers, queryParams, description, apiDefinitionInputStream, apiDefinitionDetail, modelPath, promptTokensPath, completionTokensPath, totalTokensPath, hasMetadataInPayload, payloadHandler, securityContext);
    }
}
