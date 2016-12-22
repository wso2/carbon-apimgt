package org.wso2.carbon.apimgt.gateway.extension;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.GatewayConstants;
import org.wso2.carbon.apimgt.gateway.analytics.AnalyticsUtil;
import org.wso2.carbon.apimgt.gateway.analytics.EventPublisher;
import org.wso2.carbon.apimgt.gateway.analytics.dto.AnalyticsEventStreamDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.handler.MessagingHandler;

/**
 * Gateway handler for publishing events to analyzer
 */
@Component(name = "org.wso2.carbon.apimgt.gateway.extension.StatisticsHandler",
        immediate = true, service = MessagingHandler.class)
public class StatisticsHandler implements MessagingHandler {
    private Logger log = LoggerFactory.getLogger(StatisticsHandler.class);

    @Override
    public boolean validateRequestContinuation(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        return true;
    }

    @Override
    public void invokeAtSourceConnectionInitiation(String s) {

    }

    @Override
    public void invokeAtSourceConnectionTermination(String s) {

    }

    @Override
    public void invokeAtTargetConnectionInitiation(String s) {

    }

    @Override
    public void invokeAtTargetConnectionTermination(String s) {

    }

    @Override
    public void invokeAtSourceRequestReceiving(CarbonMessage carbonMessage) {
        log.debug("invokeAtSourceRequestReceiving");
        log.info("Stat handler invoked");
        boolean enabled = ServiceReferenceHolder.getInstance().getAnalyticsConfiguration().isEnabled();
        if (enabled) {
            AnalyticsEventStreamDTO dto = AnalyticsUtil.processRequestData(carbonMessage);
            carbonMessage.setProperty(GatewayConstants.EVENT_DTO_PROPERTY_NAME, dto);
        }
    }

    @Override
    public void invokeAtSourceRequestSending(CarbonMessage carbonMessage) {
        log.debug("invokeAtSourceRequestSending");
    }

    @Override
    public void invokeAtTargetRequestReceiving(CarbonMessage carbonMessage) {
        log.debug("invokeAtTargetRequestReceiving");
    }

    @Override
    public void invokeAtTargetRequestSending(CarbonMessage carbonMessage) {
        log.debug("invokeAtTargetRequestSending");
    }

    @Override
    public void invokeAtTargetResponseReceiving(CarbonMessage carbonMessage) {
        log.debug("invokeAtTargetResponseReceiving");
    }

    @Override
    public void invokeAtTargetResponseSending(CarbonMessage carbonMessage) {
        log.debug("invokeAtTargetResponseSending");
    }

    @Override
    public void invokeAtSourceResponseReceiving(CarbonMessage carbonMessage) {
        log.debug("invokeAtSourceResponseReceiving");
    }

    @Override
    public void invokeAtSourceResponseSending(CarbonMessage carbonMessage) {
        log.debug("invokeAtSourceResponseSending");
        boolean enabled = ServiceReferenceHolder.getInstance().getAnalyticsConfiguration().isEnabled();
        if (enabled) {
            AnalyticsEventStreamDTO dto = AnalyticsUtil.processResponseData(carbonMessage);
            carbonMessage.setProperty(GatewayConstants.EVENT_DTO_PROPERTY_NAME, dto);

            EventPublisher publisher = ServiceReferenceHolder.getInstance().getPublisher();
            if (publisher == null) {
                String publisherClass = ServiceReferenceHolder.getInstance().getAnalyticsConfiguration()
                        .getEventPublisher();
                try {
                    publisher = (EventPublisher) Class.forName(publisherClass).newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    log.error("error occurred while initializing event publisher", e);
                    log.warn("Skipping event publishing..");
                    return;
                }
                ServiceReferenceHolder.getInstance().setPublisher(publisher);
            }
            publisher.init();
            publisher.publishEvent(dto);
        }
    }

    @Override
    public String handlerName() {
        return "StatisticsHandler";
    }
}
