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

package org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.AnalyticsUtils;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.impl.FaultyRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.impl.SuccessRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.impl.UnclassifiedRequestDataCollector;

/**
 * Handle all the request and forward to appropriate sub request handlers
 */
public class GenericRequestDataCollector implements RequestDataCollector {
    private static final Log log = LogFactory.getLog(GenericRequestDataCollector.class);
    private RequestDataCollector successDataCollector;
    private RequestDataCollector faultDataCollector;
    private RequestDataCollector unclassifiedDataCollector;

    public GenericRequestDataCollector(RequestDataCollector successRequestHandler, RequestDataCollector faultRequestHandler,
            RequestDataCollector unclassifiedRequestHandler) {
        this.successDataCollector = successRequestHandler;
        this.faultDataCollector = faultRequestHandler;
        this.unclassifiedDataCollector = unclassifiedRequestHandler;
    }

    public GenericRequestDataCollector() {
        this.successDataCollector = new SuccessRequestDataCollector();
        this.faultDataCollector = new FaultyRequestDataCollector();
        this.unclassifiedDataCollector = new UnclassifiedRequestDataCollector();
    }

    public void collectData(MessageContext messageContext) {
        if (AnalyticsUtils.isSuccessRequest(messageContext)) {
            handleSuccessRequest(messageContext);
        } else if (AnalyticsUtils.isFaultRequest(messageContext)) {
            handleFaultRequest(messageContext);
        } else {
            handleUnclassifiedRequest(messageContext);
        }
    }

    private void handleSuccessRequest(MessageContext messageContext) {
        successDataCollector.collectData(messageContext);
    }

    private void handleFaultRequest(MessageContext messageContext) {
        faultDataCollector.collectData(messageContext);
    }

    private void handleUnclassifiedRequest(MessageContext messageContext) {
        unclassifiedDataCollector.collectData(messageContext);
    }
}
