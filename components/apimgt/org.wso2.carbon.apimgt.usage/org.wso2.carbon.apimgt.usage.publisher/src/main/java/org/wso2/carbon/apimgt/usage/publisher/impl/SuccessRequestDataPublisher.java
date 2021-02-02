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

import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.carbon.apimgt.usage.publisher.dto.AnalyticsEvent;

import java.util.Map;

/**
 * Success event publisher implementation
 */
public class SuccessRequestDataPublisher extends AbstractRequestDataPublisher {

    @Override
    public void publish(AnalyticsEvent analyticsEvent) {
        Map<String, String> responseEventMap = mapper.convertValue(analyticsEvent, mapTypeRef);
        this.publishData(responseEventMap);
    }

    @Override
    public CounterMetric getCounterMetric() {
        return AnalyticsDataPublisher.getInstance().getSuccessMetricReporter();
    }
}
