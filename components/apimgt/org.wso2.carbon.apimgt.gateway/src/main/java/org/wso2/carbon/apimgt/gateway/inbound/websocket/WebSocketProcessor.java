/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.inbound.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;

/**
 * Interface for processing WebSocket API handshakes, requests, and responses.
 */
public interface WebSocketProcessor {
    /**
     * Handles the WebSocket handshake request.
     *
     * @param req the HTTP handshake request
     * @param ctx the channel handler context
     * @param inboundMessageContext the inbound message context
     * @return the response DTO for the handshake
     */
    InboundProcessorResponseDTO handleHandshake(FullHttpRequest req, ChannelHandlerContext ctx,
            InboundMessageContext inboundMessageContext);

    /**
     * Handles an incoming WebSocket request frame.
     *
     * @param msg the WebSocket frame
     * @param inboundMessageContext the inbound message context
     * @return the response DTO for the request
     * @throws APISecurityException if authentication or authorization fails
     */
    InboundProcessorResponseDTO handleRequest(WebSocketFrame msg, InboundMessageContext inboundMessageContext)
            throws APISecurityException;

    /**
     * Handles an outgoing WebSocket response frame.
     *
     * @param msg the WebSocket frame
     * @param inboundMessageContext the inbound message context
     * @return the response DTO for the response
     * @throws Exception if an error occurs during processing
     */
    InboundProcessorResponseDTO handleResponse(WebSocketFrame msg, InboundMessageContext inboundMessageContext)
            throws Exception;
}