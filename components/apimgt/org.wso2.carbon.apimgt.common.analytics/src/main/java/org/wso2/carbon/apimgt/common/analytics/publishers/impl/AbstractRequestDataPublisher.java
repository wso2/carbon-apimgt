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

package org.wso2.carbon.apimgt.common.analytics.publishers.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;
import org.wso2.carbon.apimgt.common.analytics.publishers.RequestDataPublisher;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Event;

import java.util.Map;

/**
 * Abstract implementation to publish an event.
 */
public abstract class AbstractRequestDataPublisher implements RequestDataPublisher {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    protected static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() {
            };
    private static final Log log = LogFactory.getLog(AbstractRequestDataPublisher.class);

    @Override
    public void publish(Event analyticsEvent) {

        CounterMetric counterMetric = this.getCounterMetric();
        if (counterMetric == null) {
            log.error("counterMetric cannot be null.");
            return;
        }

        Map<String, Object> dataMap = OBJECT_MAPPER.convertValue(analyticsEvent, MAP_TYPE_REFERENCE);
        MetricEventBuilder builder = counterMetric.getEventBuilder();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            try {
                builder.addAttribute(entry.getKey(), entry.getValue());
            } catch (MetricReportingException e) {
                log.error("Error adding data to the event stream.", e);
                return;
            }
        }

        try {
            counterMetric.incrementCount(builder);
        } catch (MetricReportingException e) {
            log.error("Error occurred when publishing event.", e);
        }
    }
}
