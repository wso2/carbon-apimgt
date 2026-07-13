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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.ai.DesignAssistanceServiceFactory;
import org.wso2.carbon.apimgt.impl.ai.DesignAssistantRequest;
import org.wso2.carbon.apimgt.impl.ai.DesignAssistantService;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Response designAssistantApiPayloadGen(DesignAssistantGenAPIPayloadDTO designAssistantGenAPIPayloadDTO,
                                                 MessageContext messageContext) throws APIManagementException {
        try {
            String sessionId = designAssistantGenAPIPayloadDTO.getSessionId();
            if (StringUtils.isEmpty(sessionId)) {
                String errorMessage = "Payload is badly formatted. Expected to have 'sessionId'";
                RestApiUtil.handleBadRequest(errorMessage, log);
                return null;
            }

            DesignAssistantRequest request = new DesignAssistantRequest();
            request.setSessionId(sessionId);

            DesignAssistantService designAssistantService = DesignAssistanceServiceFactory.getDesignAssistantService();
            String response = designAssistantService.generatePayload(request);
            if (response == null) {
                return null;
            }
            DesignAssistantAPIPayloadResponseDTO responseDTO = new DesignAssistantAPIPayloadResponseDTO();
            responseDTO.setGeneratedPayload(response);
            return Response.ok(responseDTO).build();
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
        try {
            String sessionId = designAssistantChatQueryDTO.getSessionId();
            if (StringUtils.isEmpty(sessionId)) {
                String errorMessage = "Payload is badly formatted. Expected to have 'sessionId'";
                RestApiUtil.handleBadRequest(errorMessage, log);
                return null;
            }

            DesignAssistantRequest request = new DesignAssistantRequest();
            request.setSessionId(sessionId);
            request.setText(designAssistantChatQueryDTO.getText());

            DesignAssistantService designAssistantService = DesignAssistanceServiceFactory.getDesignAssistantService();
            String response = designAssistantService.chat(request);
            if (response == null) {
                return null;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            DesignAssistantChatResponseDTO responseDTO = OBJECT_MAPPER.readValue(response,
                    DesignAssistantChatResponseDTO.class);
            return Response.ok(responseDTO).build();
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
