/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.DesignAssistantApiService;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantAPIPayloadResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantChatQueryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantChatResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantGenAPIPayloadDTO;

import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.io.IOException;

public class DesignAssistantApiServiceImpl implements DesignAssistantApiService {

    private static final Log log = LogFactory.getLog(DesignAssistantApiServiceImpl.class);
    private static DesignAssistantConfigurationDTO configDto;
    public static final String TEXT = "text";
    public static final String SESSIONID = "sessionId";

    @Override
    public Response designAssistantApiPayloadGen(DesignAssistantGenAPIPayloadDTO designAssistantGenAPIPayloadDTO,
                                                 MessageContext messageContext) throws APIManagementException {
        APIManagerConfiguration configuration = ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            configDto = configuration.getDesignAssistantConfigurationDto();
        }
        try {
            if (configDto.isKeyProvided() || configDto.isAuthTokenProvided()) {
                String sessionId = designAssistantGenAPIPayloadDTO.getSessionId();
                boolean isChatQueryEmpty = StringUtils.isEmpty(sessionId);
                if (isChatQueryEmpty) {
                    String errorMessage = "Payload is badly formatted. Expected to have 'sessionId'";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                    return null;
                }

                JSONObject payload = new JSONObject();
                payload.put(SESSIONID, sessionId);

                String response;
                if (configDto.isKeyProvided()) {
                    response = APIUtil.invokeAIService(configDto.getEndpoint(), configDto.getTokenEndpoint(),
                            configDto.getKey(), configDto.getGenApiPayloadResource(), payload.toString(), null);
 
                } else {
                    response = APIUtil.invokeAIService(configDto.getEndpoint(), null,
                            configDto.getAccessToken(), configDto.getGenApiPayloadResource(), payload.toString(), null);

                }

                DesignAssistantAPIPayloadResponseDTO responseDTO = new DesignAssistantAPIPayloadResponseDTO();
                responseDTO.setGeneratedPayload(response);

                return Response.ok(responseDTO).build();
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAIServiceNotAccessible(e)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
            } else if (RestApiUtil.isDueToAIServiceThrottled(e)) {
                return Response.status(Response.Status.TOO_MANY_REQUESTS).entity(e.getMessage()).build();
            } else {
                String errorMessage = "Error encountered while executing the execute statement of API Design " +
                        "Assistant service";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response designAssistantChat(DesignAssistantChatQueryDTO designAssistantChatQueryDTO,
                                                 MessageContext messageContext) throws APIManagementException {
        APIManagerConfiguration configuration = ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            configDto = configuration.getDesignAssistantConfigurationDto();
        }
        try {
            if (configDto.isKeyProvided() || configDto.isAuthTokenProvided()) {
                String sessionId = designAssistantChatQueryDTO.getSessionId();
                boolean isChatQueryEmpty = StringUtils.isEmpty(sessionId);
                if (isChatQueryEmpty) {
                    String errorMessage = "Payload is badly formatted. Expected to have 'sessionId'";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                    return null;
                }

                JSONObject payload = new JSONObject();

                payload.put(TEXT, designAssistantChatQueryDTO.getText());
                payload.put(SESSIONID, sessionId);

                String response;
                if (configDto.isKeyProvided()) {
                    response = APIUtil.invokeAIService(configDto.getEndpoint(), configDto.getTokenEndpoint(),
                            configDto.getKey(), configDto.getChatResource(), payload.toString(), null);
    
                } else {
                    response = APIUtil.invokeAIService(configDto.getEndpoint(), null,
                            configDto.getAccessToken(), configDto.getChatResource(), payload.toString(), null);

                }
                ObjectMapper objectMapper = new ObjectMapper();
                DesignAssistantChatResponseDTO responseDTO = objectMapper.readValue(response, DesignAssistantChatResponseDTO.class);
                return Response.ok(responseDTO).build();
            }
        } catch (APIManagementException | IOException e) {
            if (RestApiUtil.isDueToAIServiceNotAccessible(e)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
            } else if (RestApiUtil.isDueToAIServiceThrottled(e)) {
                return Response.status(Response.Status.TOO_MANY_REQUESTS).entity(e.getMessage()).build();
            } else {
                String errorMessage = "Error encountered while executing the execute statement of API Design " +
                        "Assistant service";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
}
