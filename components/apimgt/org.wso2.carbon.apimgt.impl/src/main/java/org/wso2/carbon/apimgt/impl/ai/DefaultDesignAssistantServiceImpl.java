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
import org.wso2.carbon.apimgt.api.AIServiceConfiguration;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.DesignAssistant;
import org.wso2.carbon.apimgt.api.DesignAssistantRequest;
import org.wso2.carbon.apimgt.api.DesignAssistantResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Default {@link DesignAssistant} implementation that integrates with the WSO2 (Choreo) deployed AI service.
 * This preserves the out-of-the-box API Design Assistant behaviour and is used whenever no custom implementation
 * class is configured.
 */
public class DefaultDesignAssistantServiceImpl implements DesignAssistant {

    private static final Log log = LogFactory.getLog(DefaultDesignAssistantServiceImpl.class);
    private static final String TEXT = "text";
    private static final String SESSIONID = "sessionId";

    private AIServiceConfiguration configuration;

    @Override
    public void init(AIServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public DesignAssistantResponse generatePayload(DesignAssistantRequest request) throws APIManagementException {
        AIServiceConfiguration config = getValidatedConfiguration();

        JSONObject payload = new JSONObject();
        payload.put(SESSIONID, request.getSessionId());

        String genApiPayloadResource =
                (String) config.getProperty(APIConstants.AI.DESIGN_ASSISTANT_GEN_API_PAYLOAD_RESOURCE);
        String aiResponse;
        if (config.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), config.getTokenEndpoint(),
                    config.getKey(), genApiPayloadResource, payload.toString(), null);
        } else {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), null, config.getAccessToken(),
                    genApiPayloadResource, payload.toString(), null);
        }

        DesignAssistantResponse response = new DesignAssistantResponse();
        response.setPayload(aiResponse);
        return response;
    }

    @Override
    public DesignAssistantResponse chat(DesignAssistantRequest request) throws APIManagementException {
        AIServiceConfiguration config = getValidatedConfiguration();

        JSONObject payload = new JSONObject();
        payload.put(TEXT, request.getText());
        payload.put(SESSIONID, request.getSessionId());

        String chatResource = (String) config.getProperty(APIConstants.AI.DESIGN_ASSISTANT_CHAT_RESOURCE);
        String aiResponse;
        if (config.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), config.getTokenEndpoint(),
                    config.getKey(), chatResource, payload.toString(), null);
        } else {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), null, config.getAccessToken(),
                    chatResource, payload.toString(), null);
        }

        DesignAssistantResponse response = new DesignAssistantResponse();
        response.setChatResponse(aiResponse);
        return response;
    }

    private AIServiceConfiguration getValidatedConfiguration() throws APIManagementException {
        AIServiceConfiguration config = this.configuration;
        if (config == null || !(config.isKeyProvided() || config.isAuthTokenProvided())) {
            String errorMessage = "API Design Assistant service is not configured properly. Please provide the "
                    + "API key or the access token in the configuration.";
            log.error(errorMessage);
            throw new APIManagementException(errorMessage);
        }
        return config;
    }
}
