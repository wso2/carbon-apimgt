package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.BuiltInLlmProviderService;
import org.wso2.carbon.apimgt.api.LlmProviderService;
import org.wso2.carbon.apimgt.api.model.LlmProvider;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LlmProviderRegistrationService {

    private static final Log log = LogFactory.getLog(LlmProviderRegistrationService.class);

    /**
     * Registers default built-in LLM Providers for the organization.
     *
     * @param organization The organization name.
     * @throws APIManagementException If registration or deletion fails.
     */
    public static void registerDefaultLLMProviders(String organization) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        List<LlmProvider> builtInLlmProviders = apiAdmin.getBuiltInLlmProviders(organization);
        Map<String, String> llmProviderMap = mapLlmProviders(builtInLlmProviders);

        Map<String, LlmProviderService> llmProviderServiceMap =
                ServiceReferenceHolder.getInstance().getLlmProviderServiceMap();

        Set<String> llmProviderConnectorTypes = llmProviderMap.keySet();
        Set<String> llmProviderServiceConnectorTypes = llmProviderServiceMap.keySet();

        String apiDefinitionFilePath = CarbonUtils.getCarbonHome()
                + APIConstants.AIAPIConstants.AI_API_DEFINITION_FILE_PATH;
        for (String connectorType : llmProviderConnectorTypes) {
            if (!llmProviderServiceConnectorTypes.contains(connectorType)) {
                LlmProvider provider = apiAdmin.deleteLlmProvider(organization, llmProviderMap.get(connectorType), true);
                if (provider == null) {
                    log.debug("Failed to delete LLM Provider with ID: " + llmProviderMap.get(connectorType) + "in " +
                            "organization " + organization);
                }
            }
        }
        for (String connectorType : llmProviderServiceConnectorTypes) {
            if (!llmProviderConnectorTypes.contains(connectorType)) {
                LlmProviderService llmProviderService = llmProviderServiceMap.get(connectorType);
                if (llmProviderService instanceof BuiltInLlmProviderService) {
                    LlmProvider llmProvider = llmProviderService
                            .registerLlmProvider(organization, apiDefinitionFilePath);
                    if (llmProvider != null) {
                        apiAdmin.addLlmProvider(llmProvider);
                    }
                } else {
                    log.debug("Skipping non-built-in LLM service provider: "
                            + llmProviderService.getClass().getName());
                }
            }
        }
    }

    /**
     * Maps built-in LLM Providers to their connector types and IDs.
     *
     * @param builtInLlmProviders The list of built-in LLM Providers.
     * @return A map of connector types to LLM Provider IDs.
     */
    private static Map<String, String> mapLlmProviders(List<LlmProvider> builtInLlmProviders) {

        Map<String, String> providerMap = new HashMap<>();
        for (LlmProvider provider : builtInLlmProviders) {
            String configurations = provider.getConfigurations();
            org.json.JSONObject configJson = new org.json.JSONObject(configurations);
            String connectorType = configJson.getString(APIConstants.AIAPIConstants.CONNECTOR_TYPE);
            providerMap.put(connectorType, provider.getId());
        }
        return providerMap;
    }
}
