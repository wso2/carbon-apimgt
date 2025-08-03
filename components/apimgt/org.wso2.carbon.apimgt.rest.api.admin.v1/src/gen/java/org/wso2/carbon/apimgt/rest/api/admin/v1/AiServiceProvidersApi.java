package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AIServiceProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AIServiceProviderSummaryResponseListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AiServiceProvidersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.AiServiceProvidersApiServiceImpl;
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
@Path("/ai-service-providers")

@Api(description = "the ai-service-providers API")




public class AiServiceProvidersApi  {

  @Context MessageContext securityContext;

AiServiceProvidersApiService delegate = new AiServiceProvidersApiServiceImpl();


    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a AI Service provider", notes = "Add a new AI Service provider ", response = AIServiceProviderResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:llm_provider_manage", description = "Manage LLM Providers")
        })
    }, tags={ "AIServiceProviders",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created AI Service provider as entity in the body. ", response = AIServiceProviderResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response addAIServiceProvider(@Multipart(value = "name")  String name, @Multipart(value = "apiVersion")  String apiVersion, @Multipart(value = "configurations")  String configurations,  @Multipart(value = "apiDefinition") InputStream apiDefinitionInputStream, @Multipart(value = "apiDefinition" ) Attachment apiDefinitionDetail, @Multipart(value = "description", required = false)  String description, @Multipart(value = "multipleModelProviderSupport", required = false)  String multipleModelProviderSupport, @Multipart(value = "modelProviders", required = false)  String modelProviders) throws APIManagementException{
        return delegate.addAIServiceProvider(name, apiVersion, configurations, apiDefinitionInputStream, apiDefinitionDetail, description, multipleModelProviderSupport, modelProviders, securityContext);
    }

    @DELETE
    @Path("/{aiServiceProviderId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a AI Service Provider", notes = "Delete a AI Service Provider by aiServiceProviderId ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:llm_provider_manage", description = "Manage LLM Providers")
        })
    }, tags={ "AIServiceProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. AI Service provider successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deleteAIServiceProvider(@ApiParam(value = "AI Service Provider UUID ",required=true) @PathParam("aiServiceProviderId") String aiServiceProviderId) throws APIManagementException{
        return delegate.deleteAIServiceProvider(aiServiceProviderId, securityContext);
    }

    @GET
    @Path("/{aiServiceProviderId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get AI Service Provider", notes = "Get a AI Service Provider ", response = AIServiceProviderResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "AIServiceProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. OpenAPI specification ", response = AIServiceProviderResponseDTO.class) })
    public Response getAIServiceProvider(@ApiParam(value = "AI Service Provider UUID ",required=true) @PathParam("aiServiceProviderId") String aiServiceProviderId) throws APIManagementException{
        return delegate.getAIServiceProvider(aiServiceProviderId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all AI Service providers", notes = "Get all AI Service providers ", response = AIServiceProviderSummaryResponseListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "AIServiceProviders",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. AI Service providers returned ", response = AIServiceProviderSummaryResponseListDTO.class) })
    public Response getAIServiceProviders() throws APIManagementException{
        return delegate.getAIServiceProviders(securityContext);
    }

    @PUT
    @Path("/{aiServiceProviderId}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an AI Service provider", notes = "Update a AI Service provider by AIServiceProviderId ", response = AIServiceProviderResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:llm_provider_manage", description = "Manage LLM Providers")
        })
    }, tags={ "AIServiceProvider" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. AI Service Provider updated. ", response = AIServiceProviderResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response updateAIServiceProvider(@ApiParam(value = "AI Service Provider UUID ",required=true) @PathParam("aiServiceProviderId") String aiServiceProviderId, @Multipart(value = "name")  String name, @Multipart(value = "apiVersion")  String apiVersion, @Multipart(value = "configurations")  String configurations,  @Multipart(value = "apiDefinition") InputStream apiDefinitionInputStream, @Multipart(value = "apiDefinition" ) Attachment apiDefinitionDetail, @Multipart(value = "description", required = false)  String description, @Multipart(value = "multipleModelProviderSupport", required = false)  String multipleModelProviderSupport, @Multipart(value = "modelProviders", required = false)  String modelProviders) throws APIManagementException{
        return delegate.updateAIServiceProvider(aiServiceProviderId, name, apiVersion, configurations, apiDefinitionInputStream, apiDefinitionDetail, description, multipleModelProviderSupport, modelProviders, securityContext);
    }
}
