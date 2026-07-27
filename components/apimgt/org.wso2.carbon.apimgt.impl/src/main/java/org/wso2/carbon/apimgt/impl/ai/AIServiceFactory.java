/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.ai;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.AIService;
import org.wso2.carbon.apimgt.api.AIServiceConfiguration;
import org.wso2.carbon.apimgt.api.APIChatAssistant;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.DesignAssistant;
import org.wso2.carbon.apimgt.api.MarketplaceAssistant;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.ApiChatConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Resolves and caches the AI-service extension implementations - the {@link DesignAssistant},
 * {@link MarketplaceAssistant} and {@link APIChatAssistant}.
 * <p>
 * For each service the implementation class is read from the corresponding {@code <AI>} configuration in
 * {@code api-manager.xml} and instantiated reflectively via its public no-argument constructor - the same
 * config-classname + reflection extensibility pattern used elsewhere in the product (e.g. extension listeners, token
 * revocation notifier). When no class is configured the shipped default implementation is used. Once instantiated,
 * the resolved settings are pushed into the instance through {@link AIService#init(AIServiceConfiguration)} so that a
 * custom implementation reads its configuration from the {@code api}-module {@link AIServiceConfiguration} rather than
 * reaching into the {@code impl}-module configuration itself. Each resolved instance is cached, as the configured
 * implementation does not change at runtime.
 */
public class AIServiceFactory {

    private static final Log log = LogFactory.getLog(AIServiceFactory.class);

    private static volatile DesignAssistant designAssistant;
    private static volatile MarketplaceAssistant marketplaceAssistant;
    private static volatile APIChatAssistant apiChatAssistant;

    private AIServiceFactory() {
    }

    /**
     * Returns the configured Design Assistant implementation, initializing it on first access.
     *
     * @return the {@link DesignAssistant} instance
     * @throws APIManagementException if the configured implementation class cannot be instantiated
     */
    public static DesignAssistant getDesignAssistantService() throws APIManagementException {
        if (designAssistant == null) {
            synchronized (AIServiceFactory.class) {
                if (designAssistant == null) {
                    designAssistant = initializeDesignAssistant();
                }
            }
        }
        return designAssistant;
    }

    /**
     * Returns the configured Marketplace Assistant implementation, initializing it on first access.
     *
     * @return the {@link MarketplaceAssistant} instance
     * @throws APIManagementException if the configured implementation class cannot be instantiated
     */
    public static MarketplaceAssistant getMarketplaceAssistantService() throws APIManagementException {
        if (marketplaceAssistant == null) {
            synchronized (AIServiceFactory.class) {
                if (marketplaceAssistant == null) {
                    marketplaceAssistant = initializeMarketplaceAssistant();
                }
            }
        }
        return marketplaceAssistant;
    }

    /**
     * Returns the configured API Chat implementation, initializing it on first access.
     *
     * @return the {@link APIChatAssistant} instance
     * @throws APIManagementException if the configured implementation class cannot be instantiated
     */
    public static APIChatAssistant getAPIChatService() throws APIManagementException {
        if (apiChatAssistant == null) {
            synchronized (AIServiceFactory.class) {
                if (apiChatAssistant == null) {
                    apiChatAssistant = initializeAPIChatAssistant();
                }
            }
        }
        return apiChatAssistant;
    }

    private static DesignAssistant initializeDesignAssistant() throws APIManagementException {
        String implClass = APIConstants.AI.DESIGN_ASSISTANT_DEFAULT_IMPL;
        AIServiceConfiguration configuration = new AIServiceConfiguration();
        DesignAssistantConfigurationDTO configDto = getDesignConfigurationDto();
        if (configDto != null) {
            if (StringUtils.isNotBlank(configDto.getImplementationClass())) {
                implClass = configDto.getImplementationClass();
            }
            populateCommonConfiguration(configuration, configDto.isEnabled(), configDto.getEndpoint(),
                    configDto.getTokenEndpoint(), configDto.getKey(), configDto.getAccessToken(),
                    configDto.isKeyProvided(), configDto.isAuthTokenProvided());
            configuration.addProperty(APIConstants.AI.DESIGN_ASSISTANT_CHAT_RESOURCE, configDto.getChatResource());
            configuration.addProperty(APIConstants.AI.DESIGN_ASSISTANT_GEN_API_PAYLOAD_RESOURCE,
                    configDto.getGenApiPayloadResource());
        }
        return instantiate(implClass, DesignAssistant.class, configuration);
    }

    private static MarketplaceAssistant initializeMarketplaceAssistant() throws APIManagementException {
        String implClass = APIConstants.AI.MARKETPLACE_ASSISTANT_DEFAULT_IMPL;
        AIServiceConfiguration configuration = new AIServiceConfiguration();
        MarketplaceAssistantConfigurationDTO configDto = getMarketplaceConfigurationDto();
        if (configDto != null) {
            if (StringUtils.isNotBlank(configDto.getImplementationClass())) {
                implClass = configDto.getImplementationClass();
            }
            populateCommonConfiguration(configuration, configDto.isEnabled(), configDto.getEndpoint(),
                    configDto.getTokenEndpoint(), configDto.getKey(), configDto.getAccessToken(),
                    configDto.isKeyProvided(), configDto.isAuthTokenProvided());
            configuration.addProperty(APIConstants.AI.MARKETPLACE_ASSISTANT_CHAT_RESOURCE,
                    configDto.getChatResource());
            configuration.addProperty(APIConstants.AI.MARKETPLACE_ASSISTANT_PUBLISH_API_RESOURCE,
                    configDto.getApiPublishResource());
            configuration.addProperty(APIConstants.AI.MARKETPLACE_ASSISTANT_DELETE_API_RESOURCE,
                    configDto.getApiDeleteResource());
            configuration.addProperty(APIConstants.AI.MARKETPLACE_ASSISTANT_API_COUNT_RESOURCE,
                    configDto.getApiCountResource());
        }
        return instantiate(implClass, MarketplaceAssistant.class, configuration);
    }

    private static APIChatAssistant initializeAPIChatAssistant() throws APIManagementException {
        String implClass = APIConstants.AI.API_CHAT_DEFAULT_IMPL;
        AIServiceConfiguration configuration = new AIServiceConfiguration();
        ApiChatConfigurationDTO configDto = getApiChatConfigurationDto();
        if (configDto != null) {
            if (StringUtils.isNotBlank(configDto.getImplementationClass())) {
                implClass = configDto.getImplementationClass();
            }
            populateCommonConfiguration(configuration, configDto.isEnabled(), configDto.getEndpoint(),
                    configDto.getTokenEndpoint(), configDto.getKey(), configDto.getAccessToken(),
                    configDto.isKeyProvided(), configDto.isAuthTokenProvided());
            configuration.addProperty(APIConstants.AI.API_CHAT_PREPARE_RESOURCE, configDto.getPrepareResource());
            configuration.addProperty(APIConstants.AI.API_CHAT_EXECUTE_RESOURCE, configDto.getExecuteResource());
        }
        return instantiate(implClass, APIChatAssistant.class, configuration);
    }

    private static void populateCommonConfiguration(AIServiceConfiguration configuration, boolean enabled,
            String endpoint, String tokenEndpoint, String key, String accessToken, boolean keyProvided,
            boolean authTokenProvided) {
        configuration.setEnabled(enabled);
        configuration.setEndpoint(endpoint);
        configuration.setTokenEndpoint(tokenEndpoint);
        configuration.setKey(key);
        configuration.setAccessToken(accessToken);
        configuration.setKeyProvided(keyProvided);
        configuration.setAuthTokenProvided(authTokenProvided);
    }

    private static DesignAssistantConfigurationDTO getDesignConfigurationDto() {
        APIManagerConfiguration configuration = getApiManagerConfiguration();
        return configuration != null ? configuration.getDesignAssistantConfigurationDto() : null;
    }

    private static MarketplaceAssistantConfigurationDTO getMarketplaceConfigurationDto() {
        APIManagerConfiguration configuration = getApiManagerConfiguration();
        return configuration != null ? configuration.getMarketplaceAssistantConfigurationDto() : null;
    }

    private static ApiChatConfigurationDTO getApiChatConfigurationDto() {
        APIManagerConfiguration configuration = getApiManagerConfiguration();
        return configuration != null ? configuration.getApiChatConfigurationDto() : null;
    }

    private static APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }

    private static <T extends AIService> T instantiate(String implClass, Class<T> spiType,
            AIServiceConfiguration configuration) throws APIManagementException {
        try {
            Class<?> clazz = Class.forName(implClass);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!spiType.isInstance(instance)) {
                throw new APIManagementException("Configured " + spiType.getSimpleName() + " implementation class '"
                        + implClass + "' does not implement " + spiType.getName());
            }
            T service = spiType.cast(instance);
            service.init(configuration);
            if (log.isDebugEnabled()) {
                log.debug("Initialized " + spiType.getSimpleName() + " implementation: " + implClass);
            }
            return service;
        } catch (ReflectiveOperationException e) {
            throw new APIManagementException("Error while instantiating " + spiType.getSimpleName()
                    + " implementation class: " + implClass, e);
        }
    }
}
