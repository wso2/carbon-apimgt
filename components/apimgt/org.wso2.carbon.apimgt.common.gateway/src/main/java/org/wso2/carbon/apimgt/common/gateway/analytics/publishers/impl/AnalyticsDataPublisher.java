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

package org.wso2.carbon.apimgt.common.gateway.analytics.publishers.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
//import org.wso2.am.analytics.publisher.reporter.MetricReporter;
//import org.wso2.am.analytics.publisher.reporter.MetricReporterFactory;
//import org.wso2.am.analytics.publisher.reporter.MetricSchema;
//import org.wso2.carbon.apimgt.common.gateway.analytics.AnalyticsConfigurationHolder;
//import org.wso2.carbon.apimgt.common.gateway.analytics.Constants;
//
//import java.util.Map;

/**
 * Analytics event publisher for APIM
 */
public class AnalyticsDataPublisher {
    private static final Log log = LogFactory.getLog(AnalyticsDataPublisher.class);

    private CounterMetric successMetricReporter;
    private CounterMetric faultyMetricReporter;

    private static AnalyticsDataPublisher instance;

    private AnalyticsDataPublisher() {

    }

    public static AnalyticsDataPublisher getInstance() {
//        if (instance == null) {
//            synchronized (AnalyticsDataPublisher.class) {
//                if (instance == null) {
//                    instance = new AnalyticsDataPublisher();
//                    instance.init();
//                }
//            }
//        }
        return instance;
    }

//    private void init() {
//        Map<String, String> configs = AnalyticsConfigurationHolder.getInstance().getConfigurations();
//        String reporterClass = configs.get("publisher.reporter.class");
//        try {
//            MetricReporter metricReporter;
//            if (reporterClass != null) {
//                metricReporter = MetricReporterFactory.getInstance()
//                        .createMetricReporter(reporterClass, configs);
//            } else {
//                metricReporter = MetricReporterFactory.getInstance().createMetricReporter(configs);
//            }
//            this.successMetricReporter = metricReporter
//                    .createCounterMetric(Constants.RESPONSE_METRIC_NAME, MetricSchema.RESPONSE);
//            this.faultyMetricReporter = metricReporter
//                    .createCounterMetric(Constants.FAULTY_METRIC_NAME, MetricSchema.ERROR);
//        } catch (MetricCreationException e) {
//            log.error("Error initializing event publisher.", e);
//        }
//    }

    public CounterMetric getSuccessMetricReporter() {
        return successMetricReporter;
    }

    public CounterMetric getFaultyMetricReporter() {
        return faultyMetricReporter;
    }
}
