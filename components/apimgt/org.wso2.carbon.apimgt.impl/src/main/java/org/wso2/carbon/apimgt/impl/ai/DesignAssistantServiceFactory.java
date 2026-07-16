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
import org.wso2.carbon.apimgt.api.DesignAssistant;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Resolves and caches the {@link DesignAssistant} implementation to use.
 * <p>
 * The implementation class is read from the {@code <DesignAssistant><DesignAssistanceImplementation>} configuration
 * and instantiated reflectively via its public no-argument constructor. When no class is configured,
 * {@link DefaultDesignAssistantServiceImpl} is used. The resolved instance is cached, as the configured implementation
 * does not change at runtime.
 */
public class DesignAssistantServiceFactory {

    private static final Log log = LogFactory.getLog(DesignAssistantServiceFactory.class);

    private static volatile DesignAssistant designAssistant;

    private DesignAssistantServiceFactory() {
    }

    /**
     * Returns the configured Design Assistant implementation, initializing it on first access.
     *
     * @return the {@link DesignAssistant} instance
     * @throws APIManagementException if the configured implementation class cannot be instantiated
     */
    public static DesignAssistant getDesignAssistantService() throws APIManagementException {
        if (designAssistant == null) {
            synchronized (DesignAssistantServiceFactory.class) {
                if (designAssistant == null) {
                    designAssistant = initialize();
                }
            }
        }
        return designAssistant;
    }

    private static DesignAssistant initialize() throws APIManagementException {
        String implClass = APIConstants.AI.DESIGN_ASSISTANT_DEFAULT_IMPL;
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration != null) {
            DesignAssistantConfigurationDTO configDto = configuration.getDesignAssistantConfigurationDto();
            if (configDto != null && StringUtils.isNotBlank(configDto.getImplementationClass())) {
                implClass = configDto.getImplementationClass();
            }
        }
        try {
            Class<?> clazz = Class.forName(implClass);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof DesignAssistant)) {
                throw new APIManagementException("Configured Design Assistant implementation class '" + implClass
                        + "' does not implement " + DesignAssistant.class.getName());
            }
            if (log.isDebugEnabled()) {
                log.debug("Initialized Design Assistant implementation: " + implClass);
            }
            return (DesignAssistant) instance;
        } catch (ReflectiveOperationException e) {
            throw new APIManagementException("Error while instantiating Design Assistant implementation class: "
                    + implClass, e);
        }
    }
}
