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

package org.wso2.carbon.apimgt.gateway.handlers.analytics.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.AnalyticsUtils;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl.FaultyRequestHandler;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl.SuccessRequestHandler;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl.UnclassifiedRequestHandler;

/**
 * Handle all the request and forward to appropriate sub request handlers
 */
public class GenericRequestHandler implements RequestHandler {
    private static final Log log = LogFactory.getLog(GenericRequestHandler.class);
    private RequestHandler successRequestHandler;
    private RequestHandler faultRequestHandler;
    private RequestHandler unclassifiedRequestHandler;

    public GenericRequestHandler(RequestHandler successRequestHandler, RequestHandler faultRequestHandler,
            RequestHandler unclassifiedRequestHandler) {
        this.successRequestHandler = successRequestHandler;
        this.faultRequestHandler = faultRequestHandler;
        this.unclassifiedRequestHandler = unclassifiedRequestHandler;
    }

    public GenericRequestHandler() {
        this.successRequestHandler = new SuccessRequestHandler();
        this.faultRequestHandler = new FaultyRequestHandler();
        this.unclassifiedRequestHandler = new UnclassifiedRequestHandler();
    }

    public void handleRequest(MessageContext messageContext) {
        if (AnalyticsUtils.isSuccessRequest(messageContext)) {
            handleSuccessRequest(messageContext);
        } else if (AnalyticsUtils.isFaultRequest(messageContext)) {
            handleFaultRequest(messageContext);
        } else {
            handleUnclassifiedRequest(messageContext);
        }
    }

    private void handleSuccessRequest(MessageContext messageContext) {
        successRequestHandler.handleRequest(messageContext);
    }

    private void handleFaultRequest(MessageContext messageContext) {
        faultRequestHandler.handleRequest(messageContext);
    }

    private void handleUnclassifiedRequest(MessageContext messageContext) {
        unclassifiedRequestHandler.handleRequest(messageContext);
    }
}
