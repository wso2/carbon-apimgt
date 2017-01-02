package org.wso2.carbon.apimgt.gateway.analytics;
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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.analytics.dto.AnalyticsEventStreamDTO;

/**
 * Default implementation of the EventPublisher
 */
public class EventPublisherImpl implements EventPublisher {

    private static Logger log = LoggerFactory.getLogger(EventPublisherImpl.class);

    /**
     * Initialization of the publisher
     */
    @Override
    public void init() {
        log.debug("Initializing  EventPublisherImpl");

    }

    /**
     * Publish event DTO
     *
     * @param dto AnalyticsEventStreamDTO to be published
     */
    @Override
    public void publishEvent(AnalyticsEventStreamDTO dto) {
        log.debug("publish event...");
        log.info("publishing event.. " + new Gson().toJson(AnalyticsUtil.generateStream(dto)));
    }
}
