package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import com.google.gson.Gson;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.LLMProviderAuthenticationConfiguration;
import org.wso2.carbon.apimgt.api.LLMProviderConfiguration;
import org.wso2.carbon.apimgt.api.model.LLMModel;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderEndpointConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;

import javax.ws.rs.core.Response;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;


public class AiServiceProvidersApiServiceImpl implements AiServiceProvidersApiService {
    private static final Log log = LogFactory.getLog(AiServiceProvidersApiServiceImpl.class);

    public Response getAIServiceProvider(String aiServiceProviderId, MessageContext messageContext)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            AIServiceProviderResponseDTO result =
                    LLMProviderMappingUtil.fromProviderToAIServiceProviderResponseDTO(
                            apiAdmin.getLLMProvider(organization,
                                    aiServiceProviderId));
            return Response.ok().entity(result).build();
        } catch (APIManagementException e) {
            log.warn("Error while retrieving LLM Provider");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response getAIServiceProviderApiDefinition(String aiServiceProviderId, MessageContext messageContext)
            throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            LLMProvider provider = apiAdmin
                    .getLLMProvider(organization, aiServiceProviderId);
            String apiDefinition = provider.getApiDefinition();
            return Response.ok().entity(apiDefinition).build();
        } catch (APIManagementException e) {
            log.warn("Error while trying to retrieve LLM Provider's API definition");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response getAIServiceProviderEndpointConfiguration(String aiServiceProviderId, MessageContext messageContext)
            throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LLMProvider provider = apiAdmin.getLLMProvider(organization, aiServiceProviderId);
        LLMProviderConfiguration providerConfiguration = new Gson()
                .fromJson(provider.getConfigurations(), LLMProviderConfiguration.class);
        AIServiceProviderEndpointConfigurationDTO endpointConfigurationDTO =
                new AIServiceProviderEndpointConfigurationDTO();
        if (providerConfiguration.getAuthenticationConfiguration() != null) {
            LLMProviderAuthenticationConfiguration authenticationConfiguration =
                    providerConfiguration.getAuthenticationConfiguration();
            AIServiceProviderEndpointAuthenticationConfigurationDTO
                    aiServiceProviderEndpointAuthenticationConfigurationDTO =
                    new AIServiceProviderEndpointAuthenticationConfigurationDTO().enabled(
                                    authenticationConfiguration.isEnabled()).type(authenticationConfiguration.getType())
                            .parameters(authenticationConfiguration.getParameters());
            endpointConfigurationDTO.setAuthenticationConfiguration(
                    aiServiceProviderEndpointAuthenticationConfigurationDTO);
        }
        return Response.ok().entity(endpointConfigurationDTO).build();
    }

    public Response getAIServiceProviderModels(String aiServiceProviderId, MessageContext messageContext)
            throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            LLMProvider provider = apiAdmin.getLLMProvider(organization, aiServiceProviderId);
            List<LLMModel> modelList = provider.getModelList();
            return Response.ok().entity(LLMProviderMappingUtil.fromLLMModelToModelProviderList(modelList)).build();
        } catch (APIManagementException e) {
            log.warn("Error while trying to retrieve AI Service Provider's models");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response getAIServiceProviders(MessageContext messageContext) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        List<LLMProvider> LLMProviderList = apiAdmin
                .getLLMProviders(organization, null, null, null);
        AIServiceProviderSummaryResponseListDTO providerListDTO =
                LLMProviderMappingUtil.fromProviderSummaryListToAIServiceProviderSummaryResponseListDTO(
                        LLMProviderList);
        return Response.ok().entity(providerListDTO).build();
    }
}
