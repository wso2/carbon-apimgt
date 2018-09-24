/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.tracing.jaeger;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.OpenTracer;
import org.wso2.carbon.apimgt.tracing.TracingReporter;
import org.wso2.carbon.apimgt.tracing.TracingServiceImpl;

public class JaegerTracerImpl implements OpenTracer {

    private static final String NAME = "jaeger";
    private APIManagerConfiguration configuration = new TracingServiceImpl().getConfiguration();

    @Override
    public Tracer getTracer(String serviceName) {

        String hostname = configuration.getFirstProperty(Constants.CONFIG_HOST) != null ?
                configuration.getFirstProperty(Constants.CONFIG_HOST) : Constants.DEFAULT_HOST;

        int port = configuration.getFirstProperty(Constants.CONFIG_PORT) != null ?
                Integer.parseInt(configuration.getFirstProperty(Constants.CONFIG_PORT)) : Constants.DEFAULT_PORT;

        String samplerType = configuration.getFirstProperty(Constants.CONFIG_SAMPLER_TYPE) != null ?
                configuration.getFirstProperty(Constants.CONFIG_SAMPLER_TYPE) : Constants.DEFAULT_SAMPLER_TYPE;

        float samplerParam = configuration.getFirstProperty(Constants.CONFIG_SAMPLER_PARAM) != null ?
                Float.parseFloat(configuration.getFirstProperty(Constants.CONFIG_SAMPLER_PARAM))
                : Constants.DEFAULT_SAMPLER_PARAM;

        int reporterFlushInterval = configuration.getFirstProperty(Constants.CONFIG_REPORTER_FLUSH_INTERVAL) != null ?
                Integer.parseInt(configuration.getFirstProperty(Constants.CONFIG_REPORTER_FLUSH_INTERVAL))
                : Constants.DEFAULT_REPORTER_FLUSH_INTERVAL;

        int reporterBufferSize = configuration.getFirstProperty(Constants.CONFIG_REPORTER_BUFFER_SIZE) != null ?
                Integer.parseInt(configuration.getFirstProperty(Constants.CONFIG_REPORTER_BUFFER_SIZE))
                : Constants.DEFAULT_REPORTER_BUFFER_SIZE;

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

        Tracer tracer = new Configuration(serviceName).withSampler(samplerConfig).withReporter(reporterConfig).getTracer();
        Reporter reporter = new TracingReporter(LogFactory.getLog("tracer"), true);
        Tracer tracerR = new TracerR(tracer, reporter, new ThreadLocalScopeManager());
        GlobalTracer.register(tracerR);

        return tracerR;
    }

    @Override
    public String getName() {

        return NAME;
    }
}

