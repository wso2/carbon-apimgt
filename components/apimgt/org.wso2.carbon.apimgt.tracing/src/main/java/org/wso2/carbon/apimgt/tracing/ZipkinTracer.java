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
import org.wso2.carbon.apimgt.tracing.OpenTracer;
import org.wso2.carbon.apimgt.tracing.TracingConstants;
import org.wso2.carbon.apimgt.tracing.TracingReporter;
import org.wso2.carbon.apimgt.tracing.TracingServiceImpl;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class ZipkinTracer implements OpenTracer {

    private static final String NAME = "zipkin";
    private static APIManagerConfiguration configuration = new TracingServiceImpl().getConfiguration();

    @Override
    public Tracer getTracer(String serviceName) {
        String hostname = configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_HOST) != null ?
                configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_HOST)
                : TracingConstants.ZIPKIN_DEFAULT_HOST;

        int port = configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_PORT) != null ?
                Integer.parseInt(configuration.getFirstProperty(TracingConstants.ZIPKIN_CONFIG_PORT))
                : TracingConstants.ZIPKIN_DEFAULT_PORT;

        String apiContext = configuration.getFirstProperty(TracingConstants.CONFIG_API_CONTEXT) != null ?
                configuration.getFirstProperty(TracingConstants.CONFIG_API_CONTEXT)
                : TracingConstants.DEFAULT_API_CONTEXT;

        String tracerLogEnabled = configuration.getFirstProperty(TracingConstants.CONFIG_TRACER_LOG_ENABLED) != null ?
                configuration.getFirstProperty(TracingConstants.CONFIG_TRACER_LOG_ENABLED)
                : TracingConstants.DEFAULT_TRACER_LOG_ENABLED;

        OkHttpSender sender = OkHttpSender.create("http://" + hostname + ":" + port + apiContext);
        Tracer tracer = BraveTracer.create(Tracing.newBuilder()
                .localServiceName(serviceName)
                .spanReporter(AsyncReporter.builder(sender).build())
                .propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, TracingConstants.REQUEST_ID))
                .build());

        if (tracerLogEnabled.equals("true")) {
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
