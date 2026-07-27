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
import org.wso2.carbon.apimgt.api.AIServiceEndpointConfiguration;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.ApiChatConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Resolves and caches the single {@link AIService} implementation that serves every AI capability (Design Assistant,
 * Marketplace Assistant, API Chat).
 * <p>
 * The implementation class is read from the {@code <AIServiceImplementation>} configuration and instantiated
 * reflectively via its public no-argument constructor - the same config-classname + reflection extensibility pattern
 * used elsewhere in the product. When no class is configured, {@link DefaultAIServiceImpl} is used. This factory is
 * the single place that reads {@link APIManagerConfiguration}: it builds a neutral {@link AIServiceConfiguration}
 * from the {@code api-manager.xml} settings and pushes it into the instance through
 * {@link AIService#init(AIServiceConfiguration)}, so a custom implementation never reads {@code APIManagerConfiguration}
 * itself. The resolved instance is cached, as the configured implementation does not change at runtime.
 */
public class AIServiceFactory {

    private static final Log log = LogFactory.getLog(AIServiceFactory.class);

    private static volatile AIService aiService;

    private AIServiceFactory() {
    }

    /**
     * Returns the configured AI service implementation, initializing it on first access.
     *
     * @return the {@link AIService} instance
     * @throws APIManagementException if the configured implementation class cannot be instantiated
     */
    public static AIService getAIService() throws APIManagementException {
        if (aiService == null) {
            synchronized (AIServiceFactory.class) {
                if (aiService == null) {
                    aiService = initialize();
                }
            }
        }
        return aiService;
    }

    private static AIService initialize() throws APIManagementException {
        String implClass = APIConstants.AI.AI_SERVICE_DEFAULT_IMPL;
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration != null && StringUtils.isNotBlank(configuration.getAIServiceImplementationClass())) {
            implClass = configuration.getAIServiceImplementationClass();
        }
        try {
            Class<?> clazz = Class.forName(implClass);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof AIService)) {
                throw new APIManagementException("Configured AI service implementation class '" + implClass
                        + "' does not implement " + AIService.class.getName());
            }
            AIService service = (AIService) instance;
            service.init(buildConfiguration(configuration));
            if (log.isDebugEnabled()) {
                log.debug("Initialized AI service implementation: " + implClass);
            }
            return service;
        } catch (ReflectiveOperationException e) {
            throw new APIManagementException("Error while instantiating AI service implementation class: "
                    + implClass, e);
        }
    }

    /**
     * Builds the neutral {@link AIServiceConfiguration} handed to the implementation from the {@code api-manager.xml}
     * settings held in {@link APIManagerConfiguration}.
     *
     * @param configuration the API Manager configuration (may be {@code null} if not yet initialized)
     * @return the populated {@link AIServiceConfiguration} (empty sections when configuration is unavailable)
     */
    private static AIServiceConfiguration buildConfiguration(APIManagerConfiguration configuration) {
        AIServiceConfiguration config = new AIServiceConfiguration();
        if (configuration == null) {
            return config;
        }

        DesignAssistantConfigurationDTO designDto = configuration.getDesignAssistantConfigurationDto();
        if (designDto != null) {
            AIServiceEndpointConfiguration design = config.getDesignAssistant();
            copyCommon(design, designDto.isEnabled(), designDto.getEndpoint(), designDto.getTokenEndpoint(),
                    designDto.getKey(), designDto.getAccessToken(), designDto.isKeyProvided(),
                    designDto.isAuthTokenProvided());
            design.addResource(AIServiceEndpointConfiguration.CHAT_RESOURCE, designDto.getChatResource());
            design.addResource(AIServiceEndpointConfiguration.GEN_API_PAYLOAD_RESOURCE,
                    designDto.getGenApiPayloadResource());
        }

        MarketplaceAssistantConfigurationDTO marketplaceDto = configuration.getMarketplaceAssistantConfigurationDto();
        if (marketplaceDto != null) {
            AIServiceEndpointConfiguration marketplace = config.getMarketplaceAssistant();
            copyCommon(marketplace, marketplaceDto.isEnabled(), marketplaceDto.getEndpoint(),
                    marketplaceDto.getTokenEndpoint(), marketplaceDto.getKey(), marketplaceDto.getAccessToken(),
                    marketplaceDto.isKeyProvided(), marketplaceDto.isAuthTokenProvided());
            marketplace.addResource(AIServiceEndpointConfiguration.CHAT_RESOURCE, marketplaceDto.getChatResource());
            marketplace.addResource(AIServiceEndpointConfiguration.API_PUBLISH_RESOURCE,
                    marketplaceDto.getApiPublishResource());
            marketplace.addResource(AIServiceEndpointConfiguration.API_DELETE_RESOURCE,
                    marketplaceDto.getApiDeleteResource());
            marketplace.addResource(AIServiceEndpointConfiguration.API_COUNT_RESOURCE,
                    marketplaceDto.getApiCountResource());
        }

        ApiChatConfigurationDTO apiChatDto = configuration.getApiChatConfigurationDto();
        if (apiChatDto != null) {
            AIServiceEndpointConfiguration apiChat = config.getApiChat();
            copyCommon(apiChat, apiChatDto.isEnabled(), apiChatDto.getEndpoint(), apiChatDto.getTokenEndpoint(),
                    apiChatDto.getKey(), apiChatDto.getAccessToken(), apiChatDto.isKeyProvided(),
                    apiChatDto.isAuthTokenProvided());
            apiChat.addResource(AIServiceEndpointConfiguration.PREPARE_RESOURCE, apiChatDto.getPrepareResource());
            apiChat.addResource(AIServiceEndpointConfiguration.EXECUTE_RESOURCE, apiChatDto.getExecuteResource());
        }

        return config;
    }

    private static void copyCommon(AIServiceEndpointConfiguration target, boolean enabled, String endpoint,
            String tokenEndpoint, String key, String accessToken, boolean keyProvided, boolean authTokenProvided) {
        target.setEnabled(enabled);
        target.setEndpoint(endpoint);
        target.setTokenEndpoint(tokenEndpoint);
        target.setKey(key);
        target.setAccessToken(accessToken);
        target.setKeyProvided(keyProvided);
        target.setAuthTokenProvided(authTokenProvided);
    }
}
