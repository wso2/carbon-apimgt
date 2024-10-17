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

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class LlmProvidersApiServiceImpl implements LlmProvidersApiService {

    /**
     * Retrieves LLM provider details for the given ID.
     *
     * @param llmProviderId  LLM provider UUID
     * @param xWSO2Tenant    Tenant identifier
     * @param messageContext Message context of the API call
     * @return Response containing the LLM provider details in DTO format
     * @throws APIManagementException if an error occurs while retrieving the LLM provider
     */
    @Override
    public Response getLLMProviderById(String llmProviderId, String xWSO2Tenant, MessageContext messageContext)
            throws APIManagementException {

        APIAdmin admin = new APIAdminImpl();
        LLMProvider llmProvider = admin.getLLMProvider(xWSO2Tenant, llmProviderId);
        LLMProviderDTO llmProviderDto = convertToDTO(llmProvider, xWSO2Tenant);
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
    public Response getLLMProviders(String xWSO2Tenant, String name, String apiVersion,
                                    MessageContext messageContext) throws APIManagementException {

        APIAdmin admin = new APIAdminImpl();
        List<LLMProvider> llmProviderList = admin.getLLMProviders(xWSO2Tenant, name, apiVersion, null);

        List<LLMProviderDTO> llmProviderListDTO = new ArrayList<>();
        for (LLMProvider provider : llmProviderList) {
            llmProviderListDTO.add(convertToDTO(provider, xWSO2Tenant));
        }
        return Response.ok().entity(new LLMProviderListDTO().apis(llmProviderListDTO)).build();
    }

    /**
     * Helper method to convert LLMProvider to LLMProviderDTO.
     *
     * @param llmProvider The LLMProvider object to convert.
     * @return The corresponding LLMProviderDTO.
     */
    private LLMProviderDTO convertToDTO(LLMProvider llmProvider, String organization) {

        LLMProviderDTO llmProviderDto = new LLMProviderDTO();
        llmProviderDto.setId(llmProvider.getId());
        llmProviderDto.setName(llmProvider.getName());
        llmProviderDto.setOrganization(organization);
        llmProviderDto.setApiVersion(llmProvider.getApiVersion());
        llmProviderDto.setConfigurations(llmProvider.getConfigurations());
        return llmProviderDto;
    }
}
