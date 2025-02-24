/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.BuiltInLLMProviderService;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LLMProviderRegistrationService {

    private static final Log log = LogFactory.getLog(LLMProviderRegistrationService.class);

    /**
     * Registers default built-in LLM Providers for the organization.
     *
     * @param organization The organization name.
     * @throws APIManagementException If registration or deletion fails.
     */
    public static void registerDefaultLLMProviders(String organization) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        List<LLMProvider> builtInLLMProviders = apiAdmin
                .getLLMProviders(organization, null, null, true);
        Map<String, String> llmProviderMap = mapLLMProviders(builtInLLMProviders);

        Map<String, LLMProviderService> llmProviderServiceMap =
                ServiceReferenceHolder.getInstance().getLLMProviderServiceMap();

        Set<String> llmProviderConnectorTypes = llmProviderMap.keySet();
        Set<String> llmProviderServiceConnectorTypes = llmProviderServiceMap.keySet();
        for (String connectorType : llmProviderConnectorTypes) {
            if (!llmProviderServiceConnectorTypes.contains(connectorType)) {
                LLMProvider retrievedProvider = apiAdmin
                        .getLLMProvider(organization, llmProviderMap.get(connectorType));
                if (retrievedProvider != null) {
                    apiAdmin.deleteLLMProvider(organization, retrievedProvider, true);
                }
            }
        }
        for (String connectorType : llmProviderServiceConnectorTypes) {
            if (!llmProviderConnectorTypes.contains(connectorType)) {
                LLMProviderService llmProviderService = llmProviderServiceMap.get(connectorType);
                if (llmProviderService instanceof BuiltInLLMProviderService) {
                    LLMProvider llmProvider = llmProviderService
                            .getLLMProvider();
                    if (llmProvider != null) {
                        apiAdmin.addLLMProvider(organization, llmProvider);
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
     * @param builtInLLMProviders The list of built-in LLM Providers.
     * @return A map of connector types to LLM Provider IDs.
     */
    private static Map<String, String> mapLLMProviders(List<LLMProvider> builtInLLMProviders) {

        Map<String, String> providerMap = new HashMap<>();
        for (LLMProvider provider : builtInLLMProviders) {
            String configurations = provider.getConfigurations();
            org.json.JSONObject configJson = new org.json.JSONObject(configurations);
            String connectorType = configJson.getString(APIConstants.AIAPIConstants.CONNECTOR_TYPE);
            providerMap.put(connectorType, provider.getId());
        }
        return providerMap;
    }
}
