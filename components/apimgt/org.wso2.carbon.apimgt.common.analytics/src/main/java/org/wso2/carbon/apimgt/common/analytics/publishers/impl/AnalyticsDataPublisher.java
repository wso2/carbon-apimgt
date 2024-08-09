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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analytics event publisher for APIM.
 */
public class AnalyticsDataPublisher {

    private static final Log log = LogFactory.getLog(AnalyticsDataPublisher.class);
    private static AnalyticsDataPublisher instance = new AnalyticsDataPublisher();

    private List<CounterMetric> successMetricReporters;
    private List<CounterMetric> faultyMetricReporters;

    private AnalyticsDataPublisher() {

    }

    public static AnalyticsDataPublisher getInstance() {

        return instance;
    }

    private List<String> getReportersClassesOrNull(Map<String, String> configs) {
        List<String> reporterClasses = new ArrayList<>();
        List<String> reporterKeys = configs.keySet()
                .stream()
                .filter(s -> s.matches("^publisher\\.reporter[1-9][0-9]*\\.class$"))
                .collect(Collectors.toList());

        for (String key : reporterKeys) {
            String reporterClass = configs.get(key);
            if (reporterClass != null && !reporterClass.equals("")) {
                reporterClasses.add(reporterClass);
            }
        }
        if (reporterClasses.size() > 0) {
            return reporterClasses;
        }
        return null;
    }

    private List<CounterMetric> getSuccessOrFaultyCounterMetrics(List<MetricReporter> metricReporters, String name,
                                                                 MetricSchema schema) {
        List<CounterMetric> counterMetricsList = new ArrayList<>();
        for (MetricReporter metricReporter : metricReporters) {
            String reporterClassName = metricReporter.getClass().toString().replaceAll("[\r\n]", "");
            try {
                CounterMetric counterMetric = metricReporter.createCounterMetric(name, schema);
                if (counterMetric == null) {
                    throw new MetricCreationException("AnalyticsDataPublisher is not initialized.");
                }
                counterMetricsList.add(counterMetric);
            } catch (MetricCreationException | IllegalArgumentException e) {
                log.error("Error initializing event publisher for the Reporter of type " + reporterClassName, e);
            }
        }
        return counterMetricsList;
    }

    public void initialize(AnalyticsCommonConfiguration commonConfig) {
        Map<String, String> configs = commonConfig.getConfigurations();
        String reporterClass = configs.get("publisher.reporter.class");
        String reporterType = configs.get("type");
        List<String> reporterClasses = getReportersClassesOrNull(configs);
        try {
            List<MetricReporter> metricReporters = new ArrayList<>();
            MetricReporter metricReporter;
            if (reporterClass != null) {
                metricReporter = MetricReporterFactory.getInstance()
                        .createMetricReporter(reporterClass, configs);
                metricReporters.add(metricReporter);
            } else if (reporterClasses != null) {
                for (String reporterClassName : reporterClasses) {
                    try {
                        metricReporter = MetricReporterFactory.getInstance()
                                .createMetricReporter(reporterClassName, configs);
                        metricReporters.add(metricReporter);
                    } catch (MetricCreationException e) {
                        log.error("Error while creating reporter " + reporterClassName +
                                " out of multiple metric reporters.", e);
                    }
                }
            } else if (reporterType != null && !reporterType.equals("")) {
                metricReporter = MetricReporterFactory.getInstance().createLogMetricReporter(configs);
                metricReporters.add(metricReporter);
            } else {
                String authEndpoint = configs.get(Constants.AUTH_API_URL);

                if (authEndpoint == null || authEndpoint.isEmpty()) {
                    throw new MetricCreationException("Analytics Config Endpoint is not provided.");
                }

                metricReporter = MetricReporterFactory.getInstance().createMetricReporter(configs);
                metricReporters.add(metricReporter);
            }

            if (!StringUtils.isEmpty(commonConfig.getResponseSchema())) {

                this.successMetricReporters =
                        getSuccessOrFaultyCounterMetrics(metricReporters, Constants.RESPONSE_METRIC_NAME,
                                MetricSchema.valueOf(commonConfig.getResponseSchema()));
            } else {
                this.successMetricReporters =
                        getSuccessOrFaultyCounterMetrics(metricReporters, Constants.RESPONSE_METRIC_NAME,
                                MetricSchema.RESPONSE);
            }

            if (!StringUtils.isEmpty(commonConfig.getFaultSchema())) {
                this.faultyMetricReporters =
                        getSuccessOrFaultyCounterMetrics(metricReporters, Constants.FAULTY_METRIC_NAME,
                                MetricSchema.valueOf(commonConfig.getFaultSchema()));
            } else {
                this.faultyMetricReporters =
                        getSuccessOrFaultyCounterMetrics(metricReporters, Constants.FAULTY_METRIC_NAME,
                                MetricSchema.ERROR);
            }

            // not necessary to handle IllegalArgumentException here
            // since we are handling it in getSuccessOrFaultyCounterMetrics method
        } catch (MetricCreationException e) {
            log.error("Error while creating the metric reporter", e);
        }
    }

    public List<CounterMetric> getSuccessMetricReporters() throws MetricCreationException {

        if (this.successMetricReporters.isEmpty()) {
            throw new MetricCreationException("None of AnalyticsDataPublishers are initialized.");
        }
        return successMetricReporters;
    }

    public List<CounterMetric> getFaultyMetricReporters() throws MetricCreationException {

        if (this.faultyMetricReporters.isEmpty()) {
            throw new MetricCreationException("None of AnalyticsDataPublishers are initialized.");
        }
        return faultyMetricReporters;
    }
}
