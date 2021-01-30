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

package org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl.fault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultyEvent;
import org.wso2.carbon.apimgt.usage.publisher.dto.enums.FAULT_EVENT_TYPE;
import org.wso2.carbon.apimgt.usage.publisher.impl.FaultyRequestDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.RequestDataPublisher;

/**
 * Auth faulty request data collector
 */
public class AuthFaultyRequestHandler extends AbstractFaultHandler {
    private static final Log log = LogFactory.getLog(AuthFaultyRequestHandler.class);

    public AuthFaultyRequestHandler() {
        this(new FaultyRequestDataPublisher());
    }

    public AuthFaultyRequestHandler(RequestDataPublisher processor) {
        super(FAULT_EVENT_TYPE.AUTH, processor);
    }

    @Override
    public void handleFault(MessageContext messageContext, FaultyEvent faultyEvent) {
        log.debug("handling auth failure analytics events");

        faultyEvent.setApplicationId(Constants.UNKNOWN_VALUE);
        faultyEvent.setApplicationName(Constants.UNKNOWN_VALUE);
        faultyEvent.setKeyType(Constants.UNKNOWN_VALUE);
        faultyEvent.setApplicationOwner(Constants.UNKNOWN_VALUE);

        this.processRequest(faultyEvent);
    }
}
