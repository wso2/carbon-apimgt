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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricReporter;
import org.wso2.am.analytics.publisher.reporter.MetricReporterFactory;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.carbon.apimgt.common.analytics.AnalyticsCommonConfiguration;
import org.wso2.carbon.apimgt.common.analytics.Constants;
import java.util.Map;

/**
 * Analytics event publisher for APIM.
 */
public class AnalyticsDataPublisher {

    private static final Log log = LogFactory.getLog(AnalyticsDataPublisher.class);
    private static AnalyticsDataPublisher instance = new AnalyticsDataPublisher();
    private CounterMetric successMetricReporter;
    private CounterMetric faultyMetricReporter;

    private AnalyticsDataPublisher() {

    }

    public static AnalyticsDataPublisher getInstance() {

        return instance;
    }

    public void initialize(AnalyticsCommonConfiguration commonConfig) {
        Map<String, String> configs = commonConfig.getConfigurations();
        String reporterClass = configs.get("publisher.reporter.class");
        try {
            MetricReporter metricReporter;
            if (reporterClass != null) {
                metricReporter = MetricReporterFactory.getInstance()
                        .createMetricReporter(reporterClass, configs);
            } else {
                metricReporter = MetricReporterFactory.getInstance().createMetricReporter(configs);
            }

            if (!StringUtils.isEmpty(commonConfig.getResponseSchema())) {
                this.successMetricReporter = metricReporter.createCounterMetric(Constants.RESPONSE_METRIC_NAME,
                                MetricSchema.valueOf(commonConfig.getResponseSchema()));
            } else {
                this.successMetricReporter = metricReporter
                        .createCounterMetric(Constants.RESPONSE_METRIC_NAME, MetricSchema.RESPONSE);
            }

            if (!StringUtils.isEmpty(commonConfig.getFaultSchema())) {
                this.faultyMetricReporter = metricReporter.createCounterMetric(Constants.FAULTY_METRIC_NAME,
                                MetricSchema.valueOf(commonConfig.getFaultSchema()));
            } else {
                this.faultyMetricReporter = metricReporter
                        .createCounterMetric(Constants.FAULTY_METRIC_NAME, MetricSchema.ERROR);
            }

            // Illegal Argument is possible as the enum conversion could fail
        } catch (MetricCreationException | IllegalArgumentException e) {
            log.error("Error initializing event publisher.", e);
        }
    }

    public CounterMetric getSuccessMetricReporter() {

        if (this.successMetricReporter == null) {
            throw new RuntimeException("AnalyticsDataPublisher is not initialized.");
        }
        return successMetricReporter;
    }

    public CounterMetric getFaultyMetricReporter() {

        if (this.faultyMetricReporter == null) {
            throw new RuntimeException("AnalyticsDataPublisher is not initialized.");
        }
        return faultyMetricReporter;
    }
}
