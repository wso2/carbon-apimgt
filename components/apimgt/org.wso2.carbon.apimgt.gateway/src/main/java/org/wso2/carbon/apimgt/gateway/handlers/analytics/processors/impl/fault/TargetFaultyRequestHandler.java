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
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Target faulty request data collector
 */
public class TargetFaultyRequestHandler extends AbstractFaultHandler {
    private static final Log log = LogFactory.getLog(TargetFaultyRequestHandler.class);

    public TargetFaultyRequestHandler() {
        this(new FaultyRequestDataPublisher());
    }

    public TargetFaultyRequestHandler(RequestDataPublisher processor) {
        super(FAULT_EVENT_TYPE.TARGET_CONNECTIVITY, processor);
    }

    @Override
    public void handleFault(MessageContext messageContext, FaultyEvent faultyEvent) {
        log.debug("handling target failure analytics events");
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        if (authContext != null) {
            if (APIConstants.END_USER_ANONYMOUS.equalsIgnoreCase(authContext.getUsername())) {
                authContext.setApplicationName(Constants.UNKNOWN_VALUE);
                authContext.setApplicationId(Constants.UNKNOWN_VALUE);
                authContext.setSubscriber(Constants.UNKNOWN_VALUE);
                authContext.setKeyType(Constants.UNKNOWN_VALUE);
            }
        } else {
            log.warn("Ignore API request without authentication context.");
            return;
        }
        String applicationName = authContext.getApplicationName();
        String applicationId = authContext.getApplicationId();
        String applicationOwner = authContext.getSubscriber();
        String keyType = authContext.getKeyType();

        faultyEvent.setApplicationId(applicationId);
        faultyEvent.setApplicationName(applicationName);
        faultyEvent.setKeyType(keyType);
        faultyEvent.setApplicationOwner(applicationOwner);

        this.processRequest(faultyEvent);
    }
}
