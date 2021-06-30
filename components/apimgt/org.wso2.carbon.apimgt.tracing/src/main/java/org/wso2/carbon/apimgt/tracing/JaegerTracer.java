/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.tracing;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

/**
 * Class for getting Jaeger tracer from reading configuration file
 * */
public class JaegerTracer implements OpenTracer {

    private static final String NAME = "jaeger";
    private static APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfiguration();

    @Override
    public Tracer getTracer(String serviceName) {
        String hostname = configuration.getFirstProperty(TracingConstants.JAEGER_CONFIG_HOST) != null ?
                configuration.getFirstProperty(TracingConstants.JAEGER_CONFIG_HOST)
                : TracingConstants.JAEGER_DEFAULT_HOST;

        int port = configuration.getFirstProperty(TracingConstants.JAEGER_CONFIG_PORT) != null ?
                Integer.parseInt(configuration.getFirstProperty(TracingConstants.JAEGER_CONFIG_PORT))
                : TracingConstants.JAEGER_DEFAULT_PORT;

        String samplerType = configuration.getFirstProperty(TracingConstants.CONFIG_SAMPLER_TYPE) != null ?
                configuration.getFirstProperty(TracingConstants.CONFIG_SAMPLER_TYPE)
                : TracingConstants.DEFAULT_SAMPLER_TYPE;

        float samplerParam = configuration.getFirstProperty(TracingConstants.CONFIG_SAMPLER_PARAM) != null ?
                Float.parseFloat(configuration.getFirstProperty(TracingConstants.CONFIG_SAMPLER_PARAM))
                : TracingConstants.DEFAULT_SAMPLER_PARAM;

        int reporterFlushInterval =
                configuration.getFirstProperty(TracingConstants.CONFIG_REPORTER_FLUSH_INTERVAL) != null ?
                Integer.parseInt(configuration.getFirstProperty(TracingConstants.CONFIG_REPORTER_FLUSH_INTERVAL))
                : TracingConstants.DEFAULT_REPORTER_FLUSH_INTERVAL;

        int reporterBufferSize = configuration.getFirstProperty(TracingConstants.CONFIG_REPORTER_BUFFER_SIZE) != null ?
                Integer.parseInt(configuration.getFirstProperty(TracingConstants.CONFIG_REPORTER_BUFFER_SIZE))
                : TracingConstants.DEFAULT_REPORTER_BUFFER_SIZE;

        boolean tracerLogEnabled =
                Boolean.parseBoolean(configuration.getFirstProperty(TracingConstants.CONFIG_TRACER_LOG_ENABLED) != null
                        ? configuration.getFirstProperty(TracingConstants.CONFIG_TRACER_LOG_ENABLED)
                        : TracingConstants.DEFAULT_TRACER_LOG_ENABLED);

        Configuration.SamplerConfiguration samplerConfig = new Configuration.SamplerConfiguration()
                .withType(samplerType)
                .withParam(samplerParam);
        Configuration.SenderConfiguration senderConfig = new Configuration.SenderConfiguration()
                .withAgentHost(hostname)
                .withAgentPort(port);
        Configuration.ReporterConfiguration reporterConfig = new Configuration.ReporterConfiguration()
                .withLogSpans(true)
                .withFlushInterval(reporterFlushInterval)
                .withMaxQueueSize(reporterBufferSize)
                .withSender(senderConfig);

        Tracer tracer = new Configuration(serviceName).withSampler(samplerConfig)
                .withReporter(reporterConfig).getTracer();

        if (tracerLogEnabled) {
            Reporter reporter = new TracingReporter(LogFactory.getLog(TracingConstants.TRACER));
            Tracer tracerR = new TracerR(tracer, reporter, new ThreadLocalScopeManager());
            GlobalTracer.register(tracerR);
            return tracerR;
        } else {
            GlobalTracer.register(tracer);
            return tracer;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}

