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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricReporter;
import org.wso2.am.analytics.publisher.reporter.MetricReporterFactory;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.dto.ResponseEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * event publisher Util to handle even publishing.
 */
public class PublisherUtils {
    private static final Log log = LogFactory.getLog(AnalyticsMetricsHandler.class);
    public static final String AUTH_API_URL = "auth.api.url";
    public static final String TOKEN_API_URL = "token.api.url";
    public static final String CONSUMER_KEY = "consumer.key";
    public static final String CONSUMER_SECRET = "consumer.secret";
    public static final String SAS_TOKEN = "sas.token";
    public static final Map<String, String> configs = new HashMap<String, String>() {{
        put(TOKEN_API_URL, "localhost/token-api");
        put(AUTH_API_URL, "localhost/auth-api");
        put(CONSUMER_KEY, "consumer_key");
        put(CONSUMER_SECRET, "consumer_secret");
        put(SAS_TOKEN, null);
    }};
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
    };

    public static void doPublish(String metricName, ResponseEvent responseEvent) {
        MetricReporter metricReporter;
        try {
            metricReporter = MetricReporterFactory.getInstance().createMetricReporter(null, configs);
            CounterMetric counterMetric = metricReporter.createCounterMetric(metricName, MetricSchema.RESPONSE);
            Map<String, String> responseEventMap = mapper.convertValue(responseEvent, typeRef);
            counterMetric.incrementCount(responseEventMap);
        } catch (MetricCreationException e) {
            log.error("Error initializing event publisher.", e);
        } catch (MetricReportingException e) {
            log.error("Error occurred when publishing event.", e);
        }
    }
}
