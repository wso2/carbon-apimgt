package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LlmProvider;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LLMProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LLMProviderSummaryResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LLMProviderSummaryResponseListDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LLMProviderMappingUtil {

    private static final Log log = LogFactory.getLog(LLMProviderMappingUtil.class);

    private static final String JSON_OBJECT_NAME = "name";
    private static final String JSON_OBJECT_VALUE = "value";


    /**
     * Converts a list of LLMProvider objects to an LLMProviderSummaryResponseListDTO object.
     *
     * @param llmProviderList The list of LLMProvider objects to be converted.
     * @return An LLMProviderSummaryResponseListDTO containing the summary of the LLMProvider objects.
     */
    public static LLMProviderSummaryResponseListDTO fromProviderSummaryListToProviderSummaryListDTO(List<LlmProvider> llmProviderList) {

        LLMProviderSummaryResponseListDTO providerListDTO = new LLMProviderSummaryResponseListDTO();
        if (llmProviderList != null) {
            providerListDTO.setCount(llmProviderList.size());
            providerListDTO.setList(llmProviderList.stream().map(LLMProviderMappingUtil::fromProviderToProviderSummaryDTO).collect(Collectors.toList()));
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
    public static LLMProviderResponseDTO fromProviderToProviderResponseDTO(LlmProvider llmProvider) {

        if (llmProvider == null) {
            return null;
        }
        LLMProviderResponseDTO llmProviderResponseDTO = new LLMProviderResponseDTO();
        llmProviderResponseDTO.setId(llmProvider.getId());
        llmProviderResponseDTO.setName(llmProvider.getName());
        llmProviderResponseDTO.setVersion(llmProvider.getVersion());
        llmProviderResponseDTO.setApiDefinition(llmProvider.getApiDefinition());
        llmProviderResponseDTO.setHeaders(fromMapToProviderParameterDTO(llmProvider.getHeaders()));
        llmProviderResponseDTO.setQueryParams(fromMapToProviderParameterDTO(llmProvider.getQueryParams()));
        llmProviderResponseDTO.setDescription(llmProvider.getDescription());
        llmProviderResponseDTO.setModelPath(llmProvider.getModelPath());
        llmProviderResponseDTO.setPromptTokensPath(llmProvider.getPromptTokensPath());
        llmProviderResponseDTO.setCompletionTokensPath(llmProvider.getCompletionTokensPath());
        llmProviderResponseDTO.setTotalTokensPath(llmProvider.getTotalTokensPath());
        llmProviderResponseDTO.setHasMetadataInPayload(String.valueOf(llmProvider.hasMetadataInPayload()));
        llmProviderResponseDTO.setPayloadHandler(llmProvider.getPayloadHandler());
        return llmProviderResponseDTO;
    }

    /**
     * Converts a list of JSON string representations of parameters to a map of parameter names and values.
     *
     * @param parameterList A list of JSON string representations of parameters.
     * @return A map containing parameter names as keys and parameter values as values.
     * @throws APIManagementException If there is an error parsing the JSON strings or converting parameters.
     */
    public static Map<String, String> fromProviderParameterDTOToMap(List<String> parameterList) throws APIManagementException {

        Map<String, String> parameterMap = new HashMap<>();
        if (parameterList == null) {
            return parameterMap;
        }
        for (String parameter : parameterList) {
            try {
                JsonObject jsonObject = JsonParser.parseString(parameter).getAsJsonObject();
                String name = jsonObject.get(JSON_OBJECT_NAME).getAsString();
                String value = jsonObject.get(JSON_OBJECT_VALUE).getAsString();
                parameterMap.put(name, value);
            } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
                handleException("Error while converting parameter to map: " + parameter, e);
            }
        }
        return parameterMap;
    }

    /**
     * Converts a map of parameter names and values to a list of JSON string representations of parameters.
     *
     * @param parameterMap A map containing parameter names as keys and parameter values as values.
     * @return A list of JSON string representations of the parameters.
     */
    public static List<String> fromMapToProviderParameterDTO(Map<String, String> parameterMap) {

        List<String> parameterList = new ArrayList<>();
        if (parameterMap == null) {
            return parameterList;
        }

        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(JSON_OBJECT_NAME, entry.getKey());
            jsonObject.addProperty(JSON_OBJECT_VALUE, entry.getValue());
            parameterList.add(jsonObject.toString());
        }
        return parameterList;
    }

    /**
     * Converts an LLMProvider object to an LLMProviderSummaryResponseDTO object.
     *
     * @param llmProvider The LLMProvider object to be converted.
     * @return An LLMProviderSummaryResponseDTO containing summary information about the LLMProvider object.
     */
    public static LLMProviderSummaryResponseDTO fromProviderToProviderSummaryDTO(LlmProvider llmProvider) {

        if (llmProvider == null) {
            return null;
        }

        LLMProviderSummaryResponseDTO llmProviderSummaryDTO = new LLMProviderSummaryResponseDTO();
        llmProviderSummaryDTO.setId(llmProvider.getId());
        llmProviderSummaryDTO.setName(llmProvider.getName());
        llmProviderSummaryDTO.setVersion(llmProvider.getVersion());
        llmProviderSummaryDTO.setDescription(llmProvider.getDescription());
        return llmProviderSummaryDTO;
    }

    /**
     * Handles exceptions by logging the error message and throwing an APIManagementException.
     *
     * @param msg The error message to be logged and included in the exception.
     * @param t   The throwable cause of the exception.
     * @throws APIManagementException Thrown with the provided message and cause.
     */
    private static void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

}
