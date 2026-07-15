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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.MarketplaceAssistant;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Resolves and caches the {@link MarketplaceAssistant} implementation to use.
 * <p>
 * The implementation class is read from the {@code <MarketplaceAssistant><MarketPlaceAssistanceImplementation>}
 * configuration and instantiated reflectively via its public no-argument constructor - the same extensibility
 * pattern used elsewhere in the product (e.g. token revocation notifier, key validation handlers). When no class is
 * configured, {@link DefaultMarketplaceAssistantService} is used. The resolved instance is cached, as the configured
 * implementation does not change at runtime.
 */
public class MarketplaceAssistantServiceFactory {

    private static final Log log = LogFactory.getLog(MarketplaceAssistantServiceFactory.class);

    private static volatile MarketplaceAssistant marketplaceAssistant;

    private MarketplaceAssistantServiceFactory() {
    }

    /**
     * Returns the configured Marketplace Assistant implementation, initializing it on first access.
     *
     * @return the {@link MarketplaceAssistant} instance
     * @throws APIManagementException if the configured implementation class cannot be instantiated
     */
    public static MarketplaceAssistant getMarketplaceAssistantService() throws APIManagementException {
        if (marketplaceAssistant == null) {
            synchronized (MarketplaceAssistantServiceFactory.class) {
                if (marketplaceAssistant == null) {
                    marketplaceAssistant = initialize();
                }
            }
        }
        return marketplaceAssistant;
    }

    private static MarketplaceAssistant initialize() throws APIManagementException {
        String implClass = APIConstants.AI.MARKETPLACE_ASSISTANT_DEFAULT_IMPL;
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration != null) {
            MarketplaceAssistantConfigurationDTO configDto = configuration.getMarketplaceAssistantConfigurationDto();
            if (configDto != null && StringUtils.isNotBlank(configDto.getImplementationClass())) {
                implClass = configDto.getImplementationClass();
            }
        }
        try {
            Class<?> clazz = Class.forName(implClass);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof MarketplaceAssistant)) {
                throw new APIManagementException("Configured Marketplace Assistant implementation class '" + implClass
                        + "' does not implement " + MarketplaceAssistant.class.getName());
            }
            if (log.isDebugEnabled()) {
                log.debug("Initialized Marketplace Assistant implementation: " + implClass);
            }
            return (MarketplaceAssistant) instance;
        } catch (ReflectiveOperationException e) {
            throw new APIManagementException("Error while instantiating Marketplace Assistant implementation class: "
                    + implClass, e);
        }
    }
}
