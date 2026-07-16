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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIChatAssistant;
import org.wso2.carbon.apimgt.api.APIChatRequest;
import org.wso2.carbon.apimgt.api.APIChatResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.ApiChatConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Default {@link APIChatAssistant} implementation that integrates with the WSO2 (Choreo) deployed API Chat agent.
 * This preserves the out-of-the-box API Chat behaviour and is used whenever no custom implementation class is
 * configured.
 */
public class DefaultAPIChatAssistantServiceImpl implements APIChatAssistant {

    private static final Log log = LogFactory.getLog(DefaultAPIChatAssistantServiceImpl.class);

    @Override
    public APIChatResponse prepare(APIChatRequest request) throws APIManagementException {
        ApiChatConfigurationDTO configDto = getConfiguration();
        if (configDto == null || !(configDto.isKeyProvided() || configDto.isAuthTokenProvided())) {
            return null;
        }
        try {
            // Generate the payload for the prepare call
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode openAPIDefinitionJsonNode = objectMapper.readTree(request.getOpenAPIDefinition());
            ObjectNode payload = objectMapper.createObjectNode();
            payload.set(APIConstants.OPEN_API, openAPIDefinitionJsonNode);

            String aiResponse;
            if (configDto.isKeyProvided()) {
                aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), configDto.getTokenEndpoint(),
                        configDto.getKey(), configDto.getPrepareResource(), payload.toString(),
                        request.getApiChatRequestId());
            } else {
                aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), null, configDto.getAccessToken(),
                        configDto.getPrepareResource(), payload.toString(), request.getApiChatRequestId());
            }

            APIChatResponse response = new APIChatResponse();
            response.setPrepareResponse(aiResponse);
            return response;
        } catch (JsonProcessingException e) {
            String error = "Error while parsing OpenAPI definition to JSON for API ID: " + request.getApiId();
            log.error(error, e);
            throw new APIManagementException(error, e);
        }
    }

    @Override
    public APIChatResponse execute(APIChatRequest request) throws APIManagementException {
        ApiChatConfigurationDTO configDto = getConfiguration();
        String aiResponse;
        if (configDto.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), configDto.getTokenEndpoint(),
                    configDto.getKey(), configDto.getExecuteResource(), request.getRequestPayload(),
                    request.getApiChatRequestId());
        } else {
            aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), null, configDto.getAccessToken(),
                    configDto.getExecuteResource(), request.getRequestPayload(), request.getApiChatRequestId());
        }

        APIChatResponse response = new APIChatResponse();
        response.setExecuteResponse(aiResponse);
        return response;
    }

    private ApiChatConfigurationDTO getConfiguration() throws APIManagementException {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration == null) {
            throw new APIManagementException("API Manager configuration is not initialized.");
        }
        return configuration.getApiChatConfigurationDto();
    }
}
