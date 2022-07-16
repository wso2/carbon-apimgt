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
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

/**
 * Class for getting Log tracer from reading configuration file.
 */
public class LogTelemetry implements APIMOpenTelemetry {

    private static final APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfiguration();
    private static final String NAME = "log";
    private static final Log log = LogFactory.getLog(LogTelemetry.class);
    private SdkTracerProvider sdkTracerProvider;
    private OpenTelemetry openTelemetry;

    @Override
    public void init(String serviceName) {

        boolean logEnabled = Boolean.parseBoolean(configuration.getFirstProperty(TelemetryConstants.LOG_ENABLED));

        if (logEnabled) {

            LogExporter logExporter = LogExporter.create();

            if (log.isDebugEnabled()) {
                log.debug("Log exporter: " + logExporter + " is configured");
            }

            Resource serviceNameResource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName));

            sdkTracerProvider = SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(logExporter).build())
                    .setResource(Resource.getDefault().merge(serviceNameResource))
                    .build();

            openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(sdkTracerProvider)
                    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                    .build();

            if (log.isDebugEnabled()) {
                log.debug("OpenTelemetry instance: " + openTelemetry + " is configured.");
            }
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
