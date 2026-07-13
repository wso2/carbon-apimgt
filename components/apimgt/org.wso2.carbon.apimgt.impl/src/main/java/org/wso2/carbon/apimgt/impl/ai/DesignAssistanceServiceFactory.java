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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.lang.reflect.InvocationTargetException;

public class DesignAssistanceServiceFactory {
    private static final Log log = LogFactory.getLog(DesignAssistanceServiceFactory.class);
    private static volatile DesignAssistantService designAssistantService;
    private DesignAssistanceServiceFactory() {
    }
    public static DesignAssistantService getDesignAssistantService() throws APIManagementException {
        if (designAssistantService == null) {
            synchronized (DesignAssistanceServiceFactory.class) {
                if (designAssistantService == null) {
                    designAssistantService = initialize();
                }
            }
        }
        return designAssistantService;
    }

    private static DesignAssistantService initialize() throws APIManagementException {
        String implClass = APIConstants.AI.DESIGN_ASSISTANT_DEFAULT_IMPL;
        try {
            APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            if (configuration != null) {
                DesignAssistantConfigurationDTO configDto = configuration.getDesignAssistantConfigurationDto();
                if (configDto != null && StringUtils.isNotBlank(configDto.getImplementationClass())) {
                    implClass = configDto.getImplementationClass();
                }
            }
            Class<?> clazz = Class.forName(implClass);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof DesignAssistantService)) {
                throw new APIManagementException("Configured Design Assistant implementation class '" + implClass
                        + "' does not implement " + DesignAssistantService.class.getName());
            }
            if (log.isDebugEnabled()) {
                log.debug("Initialized Design Assistant service implementation: " + implClass);
            }
            return (DesignAssistantService) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                InvocationTargetException e) {
            throw new APIManagementException("Error while instantiating Design Assistant implementation class: "
                    + implClass, e);
        }
    }
}
