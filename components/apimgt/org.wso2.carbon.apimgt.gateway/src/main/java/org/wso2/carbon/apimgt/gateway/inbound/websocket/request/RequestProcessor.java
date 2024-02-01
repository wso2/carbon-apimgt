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
package org.wso2.carbon.apimgt.gateway.inbound.websocket.request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;

/**
 * This class intercepts the inbound websocket execution path of request frames sent from client to server
 * (publish messages).
 */
public class RequestProcessor {

    private static final Log log = LogFactory.getLog(RequestProcessor.class);

    /**
     * Handles requests (publish messages) during inbound websocket execution. For websocket API publish messages,
     * this method performs throttling.
     *
     * @param msgSize               Message size of websocket frame payload
     * @param msgText               The Websocket frame payload text
     * @param inboundMessageContext InboundMessageContext
     * @return InboundProcessorResponseDTO
     */
    public InboundProcessorResponseDTO handleRequest(int msgSize, String msgText,
                                                     InboundMessageContext inboundMessageContext) throws APISecurityException {
        InboundProcessorResponseDTO responseDTO;
        responseDTO = InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext);
        if (!responseDTO.isError()) {
            responseDTO = WebsocketUtil.validateDenyPolicies(inboundMessageContext);
            if (!responseDTO.isError()) {
                if (log.isDebugEnabled()) {
                    log.debug("Perform websocket request throttling for: " + inboundMessageContext.getApiContext());
                }
                responseDTO = InboundWebsocketProcessorUtil.doThrottle(msgSize, null, inboundMessageContext, responseDTO);
            }
        }
        return responseDTO;
    }
}
