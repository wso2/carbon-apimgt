/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.inbound.websocket.handshake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.impl.dto.ResourceInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * This class intercepts the inbound websocket handshake process and perform authentication etc.
 */
public class HandshakeProcessor {

    private static final Log log = LogFactory.getLog(HandshakeProcessor.class);

    /**
     * This method process websocket handshake and perform authentication using the inbound message context.
     * For successful authentications, it sets the resource map of the invoking API to the context.
     *
     * @param inboundMessageContext InboundMessageContext
     * @return InboundProcessorResponseDTO with handshake processing response
     */
    public InboundProcessorResponseDTO processHandshake(InboundMessageContext inboundMessageContext) {

        if (log.isDebugEnabled()) {
            log.debug("Processing handshake message for inbound websocket context: "
                    + inboundMessageContext.getApiContext());
        }
        InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();
        boolean isTokenValid;
        try {
            isTokenValid = InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext);
        } catch (APISecurityException e) {
            log.error(e);
            return InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                    WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR,
                    e.getMessage());
        }
        if (isTokenValid) {
            if (log.isDebugEnabled()) {
                log.debug("Handshake authentication success for inbound websocket context: "
                        + inboundMessageContext.getApiContext() + " Setting ResourceInfoDTOs of elected API "
                        + "to inbound message context");
            }
            setResourcesMapToContext(inboundMessageContext);
        } else {
            log.error("Authentication failed for " + inboundMessageContext.getApiContext());
            return InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                    WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR,
                    WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        }
        return inboundProcessorResponseDTO;
    }

    /**
     * Set the resource map with VerbInfoDTOs to the context using URL mappings from the InboundMessageContext.
     *
     * @param inboundMessageContext InboundMessageContext
     */
    private void setResourcesMapToContext(InboundMessageContext inboundMessageContext) {

        List<URLMapping> urlMappings = inboundMessageContext.getElectedAPI().getResources();
        Map<String, ResourceInfoDTO> resourcesMap = inboundMessageContext.getResourcesMap();

        ResourceInfoDTO resourceInfoDTO;
        VerbInfoDTO verbInfoDTO;
        for (URLMapping urlMapping : urlMappings) {
            resourceInfoDTO = resourcesMap.get(urlMapping.getUrlPattern());
            if (resourceInfoDTO == null) {
                resourceInfoDTO = new ResourceInfoDTO();
                resourceInfoDTO.setUrlPattern(urlMapping.getUrlPattern());
                resourceInfoDTO.setHttpVerbs(new LinkedHashSet<>());
                resourcesMap.put(urlMapping.getUrlPattern(), resourceInfoDTO);
            }
            verbInfoDTO = new VerbInfoDTO();
            verbInfoDTO.setHttpVerb(urlMapping.getHttpMethod());
            verbInfoDTO.setAuthType(urlMapping.getAuthScheme());
            verbInfoDTO.setThrottling(urlMapping.getThrottlingPolicy());
            resourceInfoDTO.getHttpVerbs().add(verbInfoDTO);
        }
    }
}
