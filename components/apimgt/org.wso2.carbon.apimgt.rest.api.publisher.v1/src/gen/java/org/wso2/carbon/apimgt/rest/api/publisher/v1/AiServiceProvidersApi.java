package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderEndpointConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderSummaryResponseListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ModelProviderDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.AiServiceProvidersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.AiServiceProvidersApiServiceImpl;
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


    @GET
    @Path("/{aiServiceProviderId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get AI Service Provider", notes = "Get a AI Service Provider ", response = AIServiceProviderResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "AIServiceProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. AI Service Provider ", response = AIServiceProviderResponseDTO.class) })
    public Response getAIServiceProvider(@ApiParam(value = "AI Service Provider ID ",required=true) @PathParam("aiServiceProviderId") String aiServiceProviderId) throws APIManagementException{
        return delegate.getAIServiceProvider(aiServiceProviderId, securityContext);
    }

    @GET
    @Path("/{aiServiceProviderId}/api-definition")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get AI Service Provider's API Definition", notes = "Get AI Service Provider's API Definition ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "AIServiceProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API Definition ", response = String.class) })
    public Response getAIServiceProviderApiDefinition(@ApiParam(value = "",required=true) @PathParam("aiServiceProviderId") String aiServiceProviderId) throws APIManagementException{
        return delegate.getAIServiceProviderApiDefinition(aiServiceProviderId, securityContext);
    }

    @GET
    @Path("/{aiServiceProviderId}/endpoint-configuration")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get AI Service Provider's security configurations", notes = "Get AI Service Provider's endpoint security configurations ", response = AIServiceProviderEndpointConfigurationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "AIServiceProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API Definition ", response = AIServiceProviderEndpointConfigurationDTO.class) })
    public Response getAIServiceProviderEndpointConfiguration(@ApiParam(value = "",required=true) @PathParam("aiServiceProviderId") String aiServiceProviderId) throws APIManagementException{
        return delegate.getAIServiceProviderEndpointConfiguration(aiServiceProviderId, securityContext);
    }

    @GET
    @Path("/{aiServiceProviderId}/models")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get AI Service Provider's model list", notes = "Get AI Service Provider's model list ", response = ModelProviderDTO.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "AIServiceProvider",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of supported model families grouped by vendor ", response = ModelProviderDTO.class, responseContainer = "List") })
    public Response getAIServiceProviderModels(@ApiParam(value = "",required=true) @PathParam("aiServiceProviderId") String aiServiceProviderId) throws APIManagementException{
        return delegate.getAIServiceProviderModels(aiServiceProviderId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all AI Service providers", notes = "Get all AI Service providers ", response = AIServiceProviderSummaryResponseListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:llm_provider_read", description = "Read LLM Providers")
        })
    }, tags={ "AIServiceProviders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of AI Service providers. ", response = AIServiceProviderSummaryResponseListDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAIServiceProviders() throws APIManagementException{
        return delegate.getAIServiceProviders(securityContext);
    }
}
