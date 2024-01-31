/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.tracing.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

/**
 * Class for getting Zipkin tracer from reading configuration file.
 */
public class ZipkinTelemetry implements APIMOpenTelemetry {

    private static final String NAME = "zipkin";
    private static final Log log = LogFactory.getLog(ZipkinTelemetry.class);
    private static final APIManagerConfiguration configuration =
            ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    private SdkTracerProvider sdkTracerProvider;
    private OpenTelemetry openTelemetry;

    @Override
    public void init(String serviceName) {

        String hostname = configuration.getFirstProperty(TelemetryConstants.ZIPKIN_CONFIG_HOST) != null ?
                configuration.getFirstProperty(TelemetryConstants.ZIPKIN_CONFIG_HOST)
                : TelemetryConstants.ZIPKIN_DEFAULT_HOST;

        int port = configuration.getFirstProperty(TelemetryConstants.ZIPKIN_CONFIG_PORT) != null ?
                Integer.parseInt(configuration.getFirstProperty(TelemetryConstants.ZIPKIN_CONFIG_PORT))
                : TelemetryConstants.ZIPKIN_DEFAULT_PORT;

        ZipkinSpanExporter zipkinExporter = ZipkinSpanExporter.builder()
                .setEndpoint("http://" + hostname + ":" + port + TelemetryConstants.ZIPKIN_API_CONTEXT)
                .build();

        if (log.isDebugEnabled()) {
            log.debug("Zipkin exporter: " + zipkinExporter + " is configured at http://" + hostname + ":" + port);
        }

        sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(zipkinExporter).build())
                .setResource(TelemetryUtil.getTracerProviderResource(serviceName))
                .build();

        openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(B3Propagator.injectingMultiHeaders()))
                .build();

        if (log.isDebugEnabled()) {
            log.debug("OpenTelemetry instance: " + openTelemetry.toString() + " is configured.");
        }
    }

    @Override
    public OpenTelemetry getAPIMOpenTelemetry() {

        return openTelemetry;
    }

    @Override
    public Tracer getTelemetryTracer() {

        return openTelemetry.getTracer(TelemetryConstants.OPENTELEMETRY_INSTRUMENTATION_NAME);
    }

    @Override
    public String getName() {

        return NAME;
    }

    @Override
    public void close() {

        if (sdkTracerProvider != null) {
            sdkTracerProvider.close();
        }
    }
}
