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
import org.wso2.carbon.apimgt.eventing.EventPublisherFactory;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.eventing.hub.internal.ServiceReferenceHolder;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for getting event hub event publisher instances.
 */
public class EventHubEventPublisherFactory implements EventPublisherFactory {
    private static final Log log = LogFactory.getLog(EventHubEventPublisherFactory.class);

    @Override
    public void configure(Map<String, String> configuration) {
        OutputEventAdapterConfiguration adapterConfiguration = new OutputEventAdapterConfiguration();
        adapterConfiguration.setName(EventHubEventPublisherConstants.EVENT_HUB_NOTIFICATION_EVENT_PUBLISHER);
        adapterConfiguration.setType(EventHubEventPublisherConstants.BLOCKING_EVENT_TYPE);
        adapterConfiguration.setMessageFormat(EventHubEventPublisherConstants.BLOCKING_EVENT_FORMAT);
        Map<String, String> adapterParameters = new HashMap<>();
        adapterParameters.put(EventHubEventPublisherConstants.RECEIVER_URL,
                configuration.get(EventHubEventPublisherConstants.RECEIVER_URL));
        adapterParameters.put(EventHubEventPublisherConstants.AUTHENTICATOR_URL,
                configuration.get(EventHubEventPublisherConstants.AUTHENTICATOR_URL));
        adapterParameters.put(EventHubEventPublisherConstants.USERNAME,
                configuration.get(EventHubEventPublisherConstants.USERNAME));
        adapterParameters.put(EventHubEventPublisherConstants.PASSWORD,
                configuration.get(EventHubEventPublisherConstants.PASSWORD));
        adapterParameters.put(EventHubEventPublisherConstants.PROTOCOL,
                configuration.get(EventHubEventPublisherConstants.PROTOCOL));
        adapterParameters.put(EventHubEventPublisherConstants.PUBLISHING_MODE,
                configuration.get(EventHubEventPublisherConstants.PUBLISHING_MODE));
        adapterParameters.put(EventHubEventPublisherConstants.PUBLISHING_TIME_OUT,
                configuration.get(EventHubEventPublisherConstants.PUBLISHING_TIME_OUT));
        adapterConfiguration.setStaticProperties(adapterParameters);
        try {
            ServiceReferenceHolder.getInstance().getOutputEventAdapterService().create(adapterConfiguration);
        } catch (OutputEventAdapterException e) {
            log.warn("Exception occurred while creating WSO2 Event Adapter. Event notification may not work "
                    + "properly", e);
        }
    }

    @Override
    public EventPublisher getEventPublisher(EventPublisherType eventPublisherType) {
        return new EventHubEventPublisher();
    }
}
