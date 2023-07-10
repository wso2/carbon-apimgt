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

import brave.Tracing;
import brave.opentracing.BraveTracer;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import io.opentracing.Tracer;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * Class for getting Zipkin tracer from reading configuration file
 * @deprecated
 * <p> Use {@link org.wso2.carbon.apimgt.tracing.telemetry.ZipkinTelemetry} instead</p>
 */

@Deprecated
public class ZipkinTracer implements OpenTracer {

    private static final String NAME = "zipkin";
    private static APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfiguration();

    @Override
    public Tracer getTracer(String serviceName) {

        boolean tracerLogEnabled =
                Boolean.parseBoolean(configuration.getFirstProperty(TracingConstants.CONFIG_TRACER_LOG_ENABLED) != null
                        ? configuration.getFirstProperty(TracingConstants.CONFIG_TRACER_LOG_ENABLED)
                        : TracingConstants.DEFAULT_TRACER_LOG_ENABLED);

        String hostname = configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_HOST) != null
                ? configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_HOST)
                : TracingConstants.ZIPKIN_DEFAULT_HOST;

        int port = configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_PORT) != null
                ? Integer.parseInt(configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_PORT))
                : TracingConstants.ZIPKIN_DEFAULT_PORT;

        // Read the configurable endpoint and format the endpoint based on whether is configurable or not
        String endpoint = configuration
                .getFirstProperty(TracingConstants.ZIPKIN_CONFIG_ENDPOINT_URL) != null
                ? configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_ENDPOINT_URL)
                : "http://" + hostname + ":" + port + TracingConstants.ZIPKIN_API_CONTEXT;

        // Read proxy configurations from the configuration file.
        String proxyHost = configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_PROXY_HOST);
        String proxyPort = configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_PROXY_PORT);
        OkHttpSender sender;
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
            // Configure proxy if the proxy configurations are available.
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
            OkHttpSender.Builder builder = OkHttpSender.newBuilder().endpoint(endpoint);
            builder.clientBuilder().proxy(proxy);
            sender = builder.build();
        } else {
            sender = OkHttpSender.create(endpoint);
        }
        Tracer tracer = BraveTracer.create(Tracing.newBuilder()
                .localServiceName(serviceName)
                .spanReporter(AsyncReporter.builder(sender).build())
                .propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY,
                        TracingConstants.REQUEST_ID))
                .build());

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
