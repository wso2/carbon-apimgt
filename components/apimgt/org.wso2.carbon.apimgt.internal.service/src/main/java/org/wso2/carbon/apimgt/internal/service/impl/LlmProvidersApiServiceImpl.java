package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.LLMProviderListDTO;

import java.util.List;

import java.io.InputStream;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class LlmProvidersApiServiceImpl implements LlmProvidersApiService {

    /**
     * Retrieves LLM Provider configurations.
     *
     * @param messageContext The message context for the request.
     * @return The response containing LLM Provider configurations.
     * @throws APIManagementException If retrieval fails.
     */
    @Override
    public Response getLLMProviders(String name, String apiVersion, String organization,
                                    MessageContext messageContext) throws APIManagementException {

        APIAdmin admin = new APIAdminImpl();
        List<LLMProvider> LLMProviderList = admin.getLLMProviders(organization, name, apiVersion, null);

        List<LLMProviderDTO> llmProviderDtoList = LLMProviderList.stream()
                .map(llmProvider -> {
                    LLMProviderDTO llmProviderDto = new LLMProviderDTO();
                    llmProviderDto.setName(llmProvider.getName());
                    llmProviderDto.setApiVersion(llmProvider.getApiVersion());
                    llmProviderDto.setOrganization(llmProvider.getOrganization());
                    llmProviderDto.setConfigurations(llmProvider.getConfigurations());
                    return llmProviderDto;
                })
                .collect(Collectors.toList());

        LLMProviderListDTO llmProviderListDTO = new LLMProviderListDTO();
        llmProviderListDTO.setApis(llmProviderDtoList);

        return Response.ok().entity(llmProviderListDTO).build();
    }
}
