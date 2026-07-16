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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.DesignAssistant;
import org.wso2.carbon.apimgt.api.DesignAssistantRequest;
import org.wso2.carbon.apimgt.api.DesignAssistantResponse;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Default {@link DesignAssistant} implementation that integrates with the WSO2 (Choreo) deployed AI service.
 * This preserves the out-of-the-box API Design Assistant behaviour and is used whenever no custom implementation
 * class is configured.
 */
public class DefaultDesignAssistantServiceImpl implements DesignAssistant {

    private static final Log log = LogFactory.getLog(DefaultDesignAssistantServiceImpl.class);
    public static final String TEXT = "text";
    public static final String SESSIONID = "sessionId";

    @Override
    public DesignAssistantResponse generatePayload(DesignAssistantRequest request) throws APIManagementException {
        DesignAssistantConfigurationDTO configDto = getConfiguration();
        if (configDto == null || !(configDto.isKeyProvided() || configDto.isAuthTokenProvided())) {
            return null;
        }

        JSONObject payload = new JSONObject();
        payload.put(SESSIONID, request.getSessionId());

        String aiResponse;
        if (configDto.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), configDto.getTokenEndpoint(),
                    configDto.getKey(), configDto.getGenApiPayloadResource(), payload.toString(), null);
        } else {
            aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), null, configDto.getAccessToken(),
                    configDto.getGenApiPayloadResource(), payload.toString(), null);
        }

        DesignAssistantResponse response = new DesignAssistantResponse();
        response.setPayload(aiResponse);
        return response;
    }

    @Override
    public DesignAssistantResponse chat(DesignAssistantRequest request) throws APIManagementException {
        DesignAssistantConfigurationDTO configDto = getConfiguration();
        if (configDto == null || !(configDto.isKeyProvided() || configDto.isAuthTokenProvided())) {
            return null;
        }

        JSONObject payload = new JSONObject();
        payload.put(TEXT, request.getText());
        payload.put(SESSIONID, request.getSessionId());

        String aiResponse;
        if (configDto.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), configDto.getTokenEndpoint(),
                    configDto.getKey(), configDto.getChatResource(), payload.toString(), null);
        } else {
            aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), null, configDto.getAccessToken(),
                    configDto.getChatResource(), payload.toString(), null);
        }

        DesignAssistantResponse response = new DesignAssistantResponse();
        response.setChatResponse(aiResponse);
        return response;
    }

    private DesignAssistantConfigurationDTO getConfiguration() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
            return null;
        }
        return configuration.getDesignAssistantConfigurationDto();
    }
}
