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

package org.wso2.carbon.apimgt.usage.publisher.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.carbon.apimgt.usage.publisher.RequestDataPublisher;

import java.util.Map;

/**
 * Abstract implementation to publish an event
 */
public abstract class AbstractRequestDataPublisher implements RequestDataPublisher {
    private static final Log log = LogFactory.getLog(AbstractRequestDataPublisher.class);
    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final TypeReference<Map<String, String>> mapTypeRef = new TypeReference<Map<String, String>>() {
    };

    final protected void publishData(Map<String, String> eventMap) {
        CounterMetric counterMetric = this.getCounterMetric();
        if (counterMetric == null) {
            log.error("counterMetric cannot be null.");
            return;
        }
        try {
            counterMetric.incrementCount(eventMap);
        } catch (MetricReportingException e) {
            log.error("Error occurred when publishing event.", e);
        }
    }

}
