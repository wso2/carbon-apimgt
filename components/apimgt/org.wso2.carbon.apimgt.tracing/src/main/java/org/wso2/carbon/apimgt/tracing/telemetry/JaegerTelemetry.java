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
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

import java.util.concurrent.TimeUnit;

/**
 * Class for getting Jaeger tracer from reading configuration file
 * */
public class JaegerTelemetry implements APIMOpenTelemetry {

    private static final String NAME = "jaeger";
    private static APIManagerConfiguration configuration =
            ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    private SdkTracerProvider sdkTracerProvider;
    private OpenTelemetry openTelemetry;

    @Override
    public void init(String serviceName) {

        String hostname = configuration.getFirstProperty(TelemetryConstants.JAEGER_CONFIG_HOST) != null ?
                configuration.getFirstProperty(TelemetryConstants.JAEGER_CONFIG_HOST)
                : TelemetryConstants.JAEGER_DEFAULT_HOST;

        int port = configuration.getFirstProperty(TelemetryConstants.JAEGER_CONFIG_PORT) != null ?
                Integer.parseInt(configuration.getFirstProperty(TelemetryConstants.JAEGER_CONFIG_PORT))
                : TelemetryConstants.JAEGER_DEFAULT_PORT;

        JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
                .setEndpoint("http://" + hostname + ":" + port)
                .setTimeout(30, TimeUnit.SECONDS)
                .build();

        Resource serviceNameResource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName));

        sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
                .setResource(Resource.getDefault().merge(serviceNameResource))
                .build();

        openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider).
                setPropagators(ContextPropagators.create(JaegerPropagator.getInstance()))
                .build();
    }

    @Override
    public OpenTelemetry getAPIMOpenTelemetry() {

        return openTelemetry;
    }

    @Override
    public Tracer getTelemetryTracer() {

        return openTelemetry.getTracer("org.wso2.carbon.apimgt.tracing.telemetry");
    }

    @Override
    public String getName() {

        return NAME;
    }

    @Override
    public void close() {

        sdkTracerProvider.close();
    }

}
