/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket;

import io.netty.channel.ChannelHandlerContext;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.FaultCodeClassifier;

import java.util.Map;

/**
 * Classifies faulty codes for WebSocket APIs.
 */
public class WebSocketFaultCodeClassifier extends FaultCodeClassifier {
    private ChannelHandlerContext ctx;

    public WebSocketFaultCodeClassifier(ChannelHandlerContext ctx) {
        super(null); // MessageContext is not used in WebSocket fault code classifying
        this.ctx = ctx;
    }

    @Override
    public boolean isResourceNotFound() {
        Map<String, Object> properties = WebSocketUtils.getApiProperties(ctx);
        if (properties.containsKey(SynapseConstants.ERROR_CODE)) {
            int errorCode = (int) properties.get(SynapseConstants.ERROR_CODE);
            return errorCode == Constants.RESOURCE_NOT_FOUND_ERROR_CODE;
        }
        return false;
    }

    @Override
    public boolean isMethodNotAllowed() {
        return false; // Not applicable
    }

}
