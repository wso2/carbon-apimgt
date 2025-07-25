package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderEndpointConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderSummaryResponseListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.LlmProvidersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.LlmProvidersApiServiceImpl;
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


    @GET
    @Path("/{llmProviderId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get LLM Provider", notes = "Get a LLM Provider ", response = LLMProviderResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "LLMProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. LLM Provider ", response = LLMProviderResponseDTO.class) })
    public Response getLLMProvider(@ApiParam(value = "LLM Provider ID ",required=true) @PathParam("llmProviderId") String llmProviderId) throws APIManagementException{
        return delegate.getLLMProvider(llmProviderId, securityContext);
    }

    @GET
    @Path("/{llmProviderId}/api-definition")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get LLM Provider's API Definition", notes = "Get LLM Provider's API Definition ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "LLMProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API Definition ", response = String.class) })
    public Response getLLMProviderApiDefinition(@ApiParam(value = "",required=true) @PathParam("llmProviderId") String llmProviderId) throws APIManagementException{
        return delegate.getLLMProviderApiDefinition(llmProviderId, securityContext);
    }

    @GET
    @Path("/{llmProviderId}/endpoint-configuration")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get LLM provider's security configurations", notes = "Get LLM provider's endpoint security configurations ", response = LLMProviderEndpointConfigurationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "LLMProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API Definition ", response = LLMProviderEndpointConfigurationDTO.class) })
    public Response getLLMProviderEndpointConfiguration(@ApiParam(value = "",required=true) @PathParam("llmProviderId") String llmProviderId) throws APIManagementException{
        return delegate.getLLMProviderEndpointConfiguration(llmProviderId, securityContext);
    }

    @GET
    @Path("/{llmProviderId}/models")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get LLM provider's model list", notes = "Get LLM provider's model list ", response = String.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "LLMProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of models ", response = String.class, responseContainer = "List") })
    public Response getLLMProviderModels(@ApiParam(value = "",required=true) @PathParam("llmProviderId") String llmProviderId) throws APIManagementException{
        return delegate.getLLMProviderModels(llmProviderId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all LLM providers", notes = "Get all LLM providers ", response = LLMProviderSummaryResponseListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "LLMProviders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of LLM providers. ", response = LLMProviderSummaryResponseListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getLLMProviders() throws APIManagementException{
        return delegate.getLLMProviders(securityContext);
    }
}
