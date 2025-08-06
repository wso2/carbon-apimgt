/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.api.model.LLMModel;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderSummaryResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderSummaryResponseListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderSummaryResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderSummaryResponseListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ModelProviderDTO;

public class LLMProviderMappingUtil {

    /**
     * Converts a list of LLMProvider objects to an LLMProviderSummaryResponseListDTO.
     *
     * @param LLMProviderList The list of LLM Providers.
     * @return The converted LLMProviderSummaryResponseListDTO.
     */
    public static LLMProviderSummaryResponseListDTO fromProviderSummaryListToProviderSummaryListDTO(
            List<LLMProvider> LLMProviderList) {

        LLMProviderSummaryResponseListDTO providerListDTO = new LLMProviderSummaryResponseListDTO();
        if (LLMProviderList != null) {
            providerListDTO.setCount(LLMProviderList.size());
            providerListDTO.setList(LLMProviderList.stream()
                    .map(LLMProviderMappingUtil::fromProviderToProviderSummaryDTO).collect(Collectors.toList()));
        } else {
            providerListDTO.setCount(0);
            providerListDTO.setList(new ArrayList<>());
        }
        return providerListDTO;
    }

    /**
     * Converts an LLMProvider object to an LLMProviderResponseDTO object.
     *
     * @param llmProvider The LLMProvider object to be converted.
     * @return An LLMProviderResponseDTO containing detailed information about the LLMProvider object.
     */
    public static LLMProviderResponseDTO fromProviderToProviderResponseDTO(LLMProvider llmProvider) {

        if (llmProvider == null) {
            return null;
        }
        LLMProviderResponseDTO llmProviderResponseDTO = new LLMProviderResponseDTO();
        llmProviderResponseDTO.setId(llmProvider.getId());
        llmProviderResponseDTO.setName(llmProvider.getName());
        llmProviderResponseDTO.setApiVersion(llmProvider.getApiVersion());
        llmProviderResponseDTO.setDescription(llmProvider.getDescription());
        llmProviderResponseDTO.setApiDefinition(llmProvider.getApiDefinition());
        llmProviderResponseDTO.setBuiltInSupport(llmProvider.isBuiltInSupport());
        llmProviderResponseDTO.setConfigurations(llmProvider.getConfigurations());
        return llmProviderResponseDTO;
    }

    /**
     * Converts an LLMProvider object to an LLMProviderResponseDTO object.
     *
     * @param llmProvider The LLMProvider object to be converted.
     * @return An LLMProviderResponseDTO containing detailed information about the LLMProvider object.
     */
    public static AIServiceProviderResponseDTO fromProviderToAIServiceProviderResponseDTO(LLMProvider llmProvider) {

        if (llmProvider == null) {
            return null;
        }
        AIServiceProviderResponseDTO aiServiceProviderResponseDTO = new AIServiceProviderResponseDTO();
        aiServiceProviderResponseDTO.setId(llmProvider.getId());
        aiServiceProviderResponseDTO.setName(llmProvider.getName());
        aiServiceProviderResponseDTO.setApiVersion(llmProvider.getApiVersion());
        aiServiceProviderResponseDTO.setDescription(llmProvider.getDescription());
        aiServiceProviderResponseDTO.setApiDefinition(llmProvider.getApiDefinition());
        aiServiceProviderResponseDTO.setBuiltInSupport(llmProvider.isBuiltInSupport());
        aiServiceProviderResponseDTO.setConfigurations(llmProvider.getConfigurations());
        return aiServiceProviderResponseDTO;
    }

    /**
     * Converts an LLMProvider object to an LLMProviderSummaryResponseDTO object.
     *
     * @param llmProvider The LLMProvider object to be converted.
     * @return An LLMProviderSummaryResponseDTO containing summary information about the LLMProvider object.
     */
    private static LLMProviderSummaryResponseDTO fromProviderToProviderSummaryDTO(LLMProvider llmProvider) {

        if (llmProvider == null) {
            return null;
        }

        LLMProviderSummaryResponseDTO llmProviderSummaryDTO = new LLMProviderSummaryResponseDTO();
        llmProviderSummaryDTO.setId(llmProvider.getId());
        llmProviderSummaryDTO.setName(llmProvider.getName());
        llmProviderSummaryDTO.setApiVersion(llmProvider.getApiVersion());
        llmProviderSummaryDTO.setBuiltInSupport(llmProvider.isBuiltInSupport());
        llmProviderSummaryDTO.setDescription(llmProvider.getDescription());
        return llmProviderSummaryDTO;
    }

    /**
     * Converts an LLMProvider object to an LLMProviderSummaryResponseDTO object.
     *
     * @param llmProvider The LLMProvider object to be converted.
     * @return An LLMProviderSummaryResponseDTO containing summary information about the LLMProvider object.
     */
    private static AIServiceProviderSummaryResponseDTO fromProviderToAIServiceProviderSummaryDTO(LLMProvider llmProvider) {

        if (llmProvider == null) {
            return null;
        }

        AIServiceProviderSummaryResponseDTO aiServiceProviderSummaryResponseDTO = new AIServiceProviderSummaryResponseDTO();
        aiServiceProviderSummaryResponseDTO.setId(llmProvider.getId());
        aiServiceProviderSummaryResponseDTO.setName(llmProvider.getName());
        aiServiceProviderSummaryResponseDTO.setApiVersion(llmProvider.getApiVersion());
        aiServiceProviderSummaryResponseDTO.setBuiltInSupport(llmProvider.isBuiltInSupport());
        aiServiceProviderSummaryResponseDTO.setDescription(llmProvider.getDescription());
        return aiServiceProviderSummaryResponseDTO;
    }

    public static List<ModelProviderDTO> fromLLMModelToModelProviderList(List<LLMModel> modelList) {
        if (modelList == null) {
            return new ArrayList<>();
        }
        return modelList.stream().map(model -> {
            return new ModelProviderDTO().name(model.getModelVendor()).models(model.getValues());
        }).collect(Collectors.toList());
    }

    public static AIServiceProviderSummaryResponseListDTO fromProviderSummaryListToAIServiceProviderSummaryResponseListDTO(
            List<LLMProvider> llmProviderList) {
        AIServiceProviderSummaryResponseListDTO providerListDTO = new AIServiceProviderSummaryResponseListDTO();
        if (llmProviderList != null) {
            providerListDTO.setCount(llmProviderList.size());
            providerListDTO.setList(
                    llmProviderList.stream().map(LLMProviderMappingUtil::fromProviderToAIServiceProviderSummaryDTO)
                            .collect(Collectors.toList()));
        } else {
            providerListDTO.setCount(0);
            providerListDTO.setList(new ArrayList<>());
        }
        return providerListDTO;
    }
}
