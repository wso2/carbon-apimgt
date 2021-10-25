/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.eventing.hub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.eventing.EventPublisher;
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.hub.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * EventHubEventStreamServiceEventPublisher class.
 */
public class EventHubEventStreamServiceEventPublisher implements EventPublisher {
    private static final Log log = LogFactory.getLog(EventHubEventStreamServiceEventPublisher.class);

    @Override
    public void init() {
        log.info("EventHubEventStreamServiceEventPublisher is initialized.");
    }

    @Override
    public void publish(EventPublisherEvent eventPublisherEvent) {
        boolean tenantFlowStarted = false;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            tenantFlowStarted = true;
            ServiceReferenceHolder.getInstance().getEventStreamService().publish(eventPublisherEvent);
        } finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
