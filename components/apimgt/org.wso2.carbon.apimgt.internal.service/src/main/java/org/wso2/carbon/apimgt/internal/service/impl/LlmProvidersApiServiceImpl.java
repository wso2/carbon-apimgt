/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

    @Override
    public Response getLLMProviderById(String llmProviderId, MessageContext messageContext) throws APIManagementException {
        APIAdmin admin = new APIAdminImpl();
        LLMProvider llmProvider = admin.getLLMProvider(null, llmProviderId);
        LLMProviderDTO llmProviderDto = convertToDTO(llmProvider);
        return Response.ok().entity(llmProviderDto).build();
    }

    /**
     * Retrieves LLM Provider configurations.
     *
     * @param messageContext The message context for the request.
     * @return The response containing LLM Provider configurations.
     * @throws APIManagementException If retrieval fails.
     */
    @Override
    public Response getLLMProviders(String name, String apiVersion, String organization, MessageContext messageContext) throws APIManagementException {
        APIAdmin admin = new APIAdminImpl();
        List<LLMProvider> llmProviderList = admin.getLLMProviders(organization, name, apiVersion, null);

        List<LLMProviderDTO> llmProviderDtoList = llmProviderList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        LLMProviderListDTO llmProviderListDTO = new LLMProviderListDTO();
        llmProviderListDTO.setApis(llmProviderDtoList);

        return Response.ok().entity(llmProviderListDTO).build();
    }

    /**
     * Helper method to convert LLMProvider to LLMProviderDTO.
     *
     * @param llmProvider The LLMProvider object to convert.
     * @return The corresponding LLMProviderDTO.
     */
    private LLMProviderDTO convertToDTO(LLMProvider llmProvider) {
        LLMProviderDTO llmProviderDto = new LLMProviderDTO();
        llmProviderDto.setId(llmProvider.getId());
        llmProviderDto.setName(llmProvider.getName());
        llmProviderDto.setApiVersion(llmProvider.getApiVersion());
        llmProviderDto.setConfigurations(llmProvider.getConfigurations());
        return llmProviderDto;
    }
}
