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
import org.wso2.carbon.apimgt.api.APIChatAssistant;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.ApiChatConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Resolves and caches the {@link APIChatAssistant} implementation to use.
 * <p>
 * The implementation class is read from the {@code <APIChat><ApiChatImplementation>} configuration and instantiated
 * reflectively via its public no-argument constructor - the same extensibility pattern used elsewhere in the product
 * (e.g. token revocation notifier, key validation handlers). When no class is configured,
 * {@link DefaultAPIChatAssistantService} is used. The resolved instance is cached, as the configured implementation does not
 * change at runtime.
 */
public class APIChatAssistantFactory {

    private static final Log log = LogFactory.getLog(APIChatAssistantFactory.class);

    private static volatile APIChatAssistant apiChatAssistant;

    private APIChatAssistantFactory() {
    }

    /**
     * Returns the configured API Chat implementation, initializing it on first access.
     *
     * @return the {@link APIChatAssistant} instance
     * @throws APIManagementException if the configured implementation class cannot be instantiated
     */
    public static APIChatAssistant getAPIChatService() throws APIManagementException {
        if (apiChatAssistant == null) {
            synchronized (APIChatAssistantFactory.class) {
                if (apiChatAssistant == null) {
                    apiChatAssistant = initialize();
                }
            }
        }
        return apiChatAssistant;
    }

    private static APIChatAssistant initialize() throws APIManagementException {
        String implClass = APIConstants.AI.API_CHAT_DEFAULT_IMPL;
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration != null) {
            ApiChatConfigurationDTO configDto = configuration.getApiChatConfigurationDto();
            if (configDto != null && StringUtils.isNotBlank(configDto.getImplementationClass())) {
                implClass = configDto.getImplementationClass();
            }
        }
        try {
            Class<?> clazz = Class.forName(implClass);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof APIChatAssistant)) {
                throw new APIManagementException("Configured API Chat implementation class '" + implClass
                        + "' does not implement " + APIChatAssistant.class.getName());
            }
            if (log.isDebugEnabled()) {
                log.debug("Initialized API Chat implementation: " + implClass);
            }
            return (APIChatAssistant) instance;
        } catch (ReflectiveOperationException e) {
            throw new APIManagementException("Error while instantiating API Chat implementation class: "
                    + implClass, e);
        }
    }
}
