package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderSummaryResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderSummaryResponseListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * Converts an LLMProvider object to an LLMProviderSummaryResponseDTO object.
     *
     * @param llmProvider The LLMProvider object to be converted.
     * @return An LLMProviderSummaryResponseDTO containing summary information about the LLMProvider object.
     */
    public static LLMProviderSummaryResponseDTO fromProviderToProviderSummaryDTO(LLMProvider llmProvider) {

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

}
