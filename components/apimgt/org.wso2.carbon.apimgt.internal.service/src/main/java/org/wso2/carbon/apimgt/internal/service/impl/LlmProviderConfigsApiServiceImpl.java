package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.internal.service.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.internal.service.dto.LLMProviderDTO;
import org.wso2.carbon.apimgt.internal.service.dto.LLMProviderListDTO;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

public class LlmProviderConfigsApiServiceImpl implements LlmProviderConfigsApiService {

    /**
     * Retrieves LLM Provider configurations.
     *
     * @param messageContext The message context for the request.
     * @return The response containing LLM Provider configurations.
     * @throws APIManagementException If retrieval fails.
     */
    @Override
    public Response getLLMProviderConfigs(MessageContext messageContext) throws APIManagementException {

        APIAdmin admin = new APIAdminImpl();
        List<LLMProvider> LLMProviderList = admin.getLLMProviderConfigurations();

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
