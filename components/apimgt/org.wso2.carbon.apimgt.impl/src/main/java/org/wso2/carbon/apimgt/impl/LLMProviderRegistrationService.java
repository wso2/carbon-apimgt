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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.BuiltInLLMProviderService;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LLMProviderRegistrationService {

    private static final Log log = LogFactory.getLog(LLMProviderRegistrationService.class);
    private static final String METADATA = "metadata";
    private static final String ATTRIBUTE_NAME = "attributeName";
    private static final String ATTRIBUTE_IDENTIFIER = "attributeIdentifier";
    private static final String ANALYTICS_CATEGORY = "analyticsCategory";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

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
            LLMProviderService llmProviderService = llmProviderServiceMap.get(connectorType);
            if (llmProviderService.isDeprecated() && llmProviderConnectorTypes.contains(connectorType)) {
                    LLMProvider retrievedProvider = apiAdmin
                            .getLLMProvider(organization, llmProviderMap.get(connectorType));
                if (retrievedProvider != null) {
                    String configurations = retrievedProvider.getConfigurations();
                    if (configurations != null) {
                        org.json.JSONObject configJson = new org.json.JSONObject(configurations);
                        boolean isDeprecatedInConfig = configJson.optBoolean(
                                APIConstants.AIAPIConstants.LLM_PROVIDER_DEPRECATED, false);
                        if (!isDeprecatedInConfig) {
                            configJson.put(APIConstants.AIAPIConstants.LLM_PROVIDER_DEPRECATED, true);
                            retrievedProvider.setConfigurations(configJson.toString());
                            apiAdmin.updateLLMProvider(organization, retrievedProvider);
                            if (log.isDebugEnabled()) {
                                log.debug("Updated deprecated status for LLM provider: " + connectorType);
                            }
                        }
                    }
                }
            } else if (!llmProviderConnectorTypes.contains(connectorType)) {
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
            } else {
                if (llmProviderService instanceof BuiltInLLMProviderService) {
                    syncBuiltInProviderMetadata(organization, apiAdmin, llmProviderMap.get(connectorType),
                            (BuiltInLLMProviderService) llmProviderService);
                }
            }
        }
    }

    /**
     * Brings the metadata of an already registered built-in LLM provider up to date with the metadata shipped by
     * its provider service. Built-in provider configurations are stored once and cannot be edited through the admin
     * API, so metadata attributes added in later versions (for example prompt-cache token counters) would otherwise
     * never reach existing deployments. Failures are logged and never block startup.
     *
     * @param organization       Organization.
     * @param apiAdmin           APIAdmin instance.
     * @param providerId         UUID of the stored provider.
     * @param llmProviderService Built-in provider service.
     */
    private static void syncBuiltInProviderMetadata(String organization, APIAdmin apiAdmin, String providerId,
                                                    BuiltInLLMProviderService llmProviderService) {

        try {
            LLMProvider storedProvider = apiAdmin.getLLMProvider(organization, providerId);
            LLMProvider shippedProvider = llmProviderService.getLLMProvider();
            if (storedProvider == null || shippedProvider == null) {
                return;
            }
            String mergedConfigurations = mergeBuiltInMetadata(storedProvider.getConfigurations(),
                    shippedProvider.getConfigurations(), llmProviderService.getSupersededMetadataIdentifiers());
            if (mergedConfigurations == null) {
                return;
            }
            if (ApiMgtDAO.getInstance().updateLLMProviderConfigurations(organization, providerId,
                    mergedConfigurations)) {
                new LLMProviderNotificationSender().notify(providerId, storedProvider.getName(),
                        storedProvider.getApiVersion(), mergedConfigurations, organization,
                        org.wso2.carbon.apimgt.impl.APIConstants.EventType.LLM_PROVIDER_UPDATE.name());
                log.info("Updated metadata of built-in LLM provider " + storedProvider.getName() + " "
                        + storedProvider.getApiVersion() + " in organization " + organization);
            }
        } catch (Exception e) {
            log.error("Error while updating metadata of built-in LLM provider " + providerId + " in organization "
                    + organization, e);
        }
    }

    /**
     * Merges shipped built-in metadata into stored provider configurations. Metadata attributes missing from the
     * stored configurations are appended. An existing attribute identifier is replaced only if it still equals a
     * superseded default identifier. An existing attribute's {@code analyticsCategory} tag is always synced to the
     * shipped value -- it is a product-owned classification signal (not admin-editable for built-in providers, see
     * {@link #syncBuiltInProviderMetadata}), so a tag introduced or changed in a later version (for example an
     * already-registered provider's prompt-cache counters gaining {@code "TOKEN_USAGE"}) reaches existing
     * deployments too, not just newly registered ones. Nothing else in the configurations is changed.
     *
     * @param storedConfigurations  Stored configurations JSON.
     * @param shippedConfigurations Configurations JSON shipped by the provider service.
     * @param supersededIdentifiers Attribute name to superseded default identifiers.
     * @return Merged configurations JSON, or null if no change is needed.
     */
    static String mergeBuiltInMetadata(String storedConfigurations, String shippedConfigurations,
                                       Map<String, List<String>> supersededIdentifiers) {

        if (StringUtils.isEmpty(storedConfigurations) || StringUtils.isEmpty(shippedConfigurations)) {
            return null;
        }
        JsonObject stored = JsonParser.parseString(storedConfigurations).getAsJsonObject();
        JsonObject shipped = JsonParser.parseString(shippedConfigurations).getAsJsonObject();
        JsonArray shippedMetadata = shipped.has(METADATA) && shipped.get(METADATA).isJsonArray()
                ? shipped.getAsJsonArray(METADATA) : null;
        if (shippedMetadata == null || shippedMetadata.size() == 0) {
            return null;
        }
        JsonArray storedMetadata;
        if (stored.has(METADATA) && stored.get(METADATA).isJsonArray()) {
            storedMetadata = stored.getAsJsonArray(METADATA);
        } else {
            storedMetadata = new JsonArray();
            stored.add(METADATA, storedMetadata);
        }
        Map<String, JsonObject> storedByName = new HashMap<>();
        for (JsonElement element : storedMetadata) {
            if (element.isJsonObject()) {
                String name = getString(element.getAsJsonObject(), ATTRIBUTE_NAME);
                if (name != null) {
                    storedByName.put(name, element.getAsJsonObject());
                }
            }
        }
        boolean changed = false;
        for (JsonElement element : shippedMetadata) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject shippedEntry = element.getAsJsonObject();
            String name = getString(shippedEntry, ATTRIBUTE_NAME);
            if (name == null) {
                continue;
            }
            JsonObject storedEntry = storedByName.get(name);
            if (storedEntry == null) {
                storedMetadata.add(shippedEntry.deepCopy());
                storedByName.put(name, shippedEntry);
                changed = true;
                continue;
            }
            String storedIdentifier = getString(storedEntry, ATTRIBUTE_IDENTIFIER);
            String shippedIdentifier = getString(shippedEntry, ATTRIBUTE_IDENTIFIER);
            List<String> superseded = supersededIdentifiers != null ? supersededIdentifiers.get(name) : null;
            if (shippedIdentifier != null && !shippedIdentifier.equals(storedIdentifier) && superseded != null
                    && superseded.contains(storedIdentifier)) {
                storedEntry.addProperty(ATTRIBUTE_IDENTIFIER, shippedIdentifier);
                changed = true;
            }
            String storedCategory = getString(storedEntry, ANALYTICS_CATEGORY);
            String shippedCategory = getString(shippedEntry, ANALYTICS_CATEGORY);
            if (!Objects.equals(storedCategory, shippedCategory)) {
                if (shippedCategory == null) {
                    storedEntry.remove(ANALYTICS_CATEGORY);
                } else {
                    storedEntry.addProperty(ANALYTICS_CATEGORY, shippedCategory);
                }
                changed = true;
            }
        }
        return changed ? GSON.toJson(stored) : null;
    }

    private static String getString(JsonObject object, String member) {

        JsonElement value = object.get(member);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
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
