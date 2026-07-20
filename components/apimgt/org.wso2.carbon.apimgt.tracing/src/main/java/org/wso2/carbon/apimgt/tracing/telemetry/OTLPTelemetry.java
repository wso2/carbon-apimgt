/*
 * Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

import java.util.Set;

/**
 * Class for getting Otlp tracer from reading configuration file.
 */
public class OTLPTelemetry implements APIMOpenTelemetry {

    private static final String NAME = "otlp";
    private static final Log log = LogFactory.getLog(OTLPTelemetry.class);
    private static final APIManagerConfiguration configuration =
            ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    private SdkTracerProvider sdkTracerProvider;
    private OpenTelemetry openTelemetry;

    @Override
    public void init(String serviceName) {
        String headerKey = null;
        String headerValue = null;

        String otlpProtocol = configuration.getFirstProperty(TelemetryConstants.OTLP_CONFIG_PROTOCOL) != null ?
                configuration.getFirstProperty(TelemetryConstants.OTLP_CONFIG_PROTOCOL) :
                TelemetryConstants.GRPC_PROTOCOL;

        String headerProperty = getHeaderKeyProperty();
        String endPointURL = configuration.getFirstProperty(TelemetryConstants.OTLP_CONFIG_URL) != null ?
                configuration.getFirstProperty(TelemetryConstants.OTLP_CONFIG_URL) : null;
        if (headerProperty != null) {
            headerKey = headerProperty.substring(TelemetryConstants.OPENTELEMETRY_PROPERTIES_PREFIX.length());

            headerValue = configuration.getFirstProperty(headerProperty) != null ?
                    configuration.getFirstProperty(headerProperty) : null;
        }

//        if (StringUtils.isNotEmpty(endPointURL)) {
//            OtlpGrpcSpanExporterBuilder otlpGrpcSpanExporterBuilder = null;
//            if (headerKey != null && headerValue != null) {
//                otlpGrpcSpanExporterBuilder = OtlpGrpcSpanExporter.builder()
//                        .setEndpoint(endPointURL)
//                        .setCompression("gzip")
//                        .addHeader(headerKey, headerValue);
//            } else {
//                otlpGrpcSpanExporterBuilder = OtlpGrpcSpanExporter.builder()
//                        .setEndpoint(endPointURL)
//                        .setCompression("gzip");
//                if (log.isDebugEnabled()) {
//                    log.debug("OTLP exporter: " + otlpGrpcSpanExporterBuilder + " is configured at " + endPointURL +
//                            " without headers.");
//                }
//            }

        boolean useHttp;
        if (TelemetryConstants.HTTP_PROTOCOL.equalsIgnoreCase(otlpProtocol)) {
            useHttp = true;
        } else if (TelemetryConstants.GRPC_PROTOCOL.equalsIgnoreCase(otlpProtocol)) {
            useHttp = false;
        } else {
            log.warn("OTLP protocol '" + otlpProtocol + "' is invalid. Defaulting to gRPC.");
            useHttp = false;
        }

//            if (log.isDebugEnabled()) {
//                log.debug("OTLP exporter: " + otlpGrpcSpanExporterBuilder + " is configured at " + endPointURL);
//            }
        if (StringUtils.isNotEmpty(endPointURL)) {
            SpanExporter spanExporter = buildSpanExporter(useHttp, endPointURL, headerKey, headerValue);

            sdkTracerProvider = SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                    .setResource(TelemetryUtil.getTracerProviderResource(serviceName))
                    .setSampler(getConfiguredSampler())
                    .build();

            openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(sdkTracerProvider).
                setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();

            if (log.isDebugEnabled()) {
                log.debug("OpenTelemetry instance: " + openTelemetry + " is configured.");
            }
        } else {
            log.error("The OTLP endpoint URL is not configured properly. Hence, the OTLP tracer " +
                    "initialization failed.");
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

    /**
     * Builds the OTLP span exporter for the configured protocol (HTTP or gRPC).
     *
     * @param useHttp     whether to use the OTLP/HTTP exporter instead of OTLP/gRPC.
     * @param endPointURL the OTLP endpoint URL.
     * @param headerKey   optional authentication header name (may be {@code null}).
     * @param headerValue optional authentication header value (may be {@code null}).
     * @return the configured {@link SpanExporter}.
     */
    private SpanExporter buildSpanExporter(boolean useHttp, String endPointURL, String headerKey, String headerValue) {

        boolean hasHeader = headerKey != null && headerValue != null;
        SpanExporter spanExporter;
        if (useHttp) {
            OtlpHttpSpanExporterBuilder builder =
                    OtlpHttpSpanExporter.builder().setEndpoint(endPointURL).setCompression("gzip");
            if (hasHeader) {
                builder.addHeader(headerKey, headerValue);
            }
            spanExporter = builder.build();
        } else {
            OtlpGrpcSpanExporterBuilder builder =
                    OtlpGrpcSpanExporter.builder().setEndpoint(endPointURL).setCompression("gzip");
            if (hasHeader) {
                builder.addHeader(headerKey, headerValue);
            }
            spanExporter = builder.build();
        }
        if (log.isDebugEnabled()) {
            log.debug("OTLP " + (useHttp ? "HTTP" : "gRPC") + " exporter is configured at " + endPointURL +
                    (hasHeader ? " with header: " + headerKey : " without headers."));
        }
        return spanExporter;
    }

    /**
     * Return the header key from properties file for specific OTLP based APM.
     *
     * @return Header key.
     */
    public String getHeaderKeyProperty() {

        Set<String> keySet = configuration.getConfigKeySet();
        for (String property : keySet) {
            if (property.startsWith(TelemetryConstants.OPENTELEMETRY_PROPERTIES_PREFIX)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Resolves the trace sampler from the configured "OpenTelemetry.RemoteTracer.SamplerMode" value.
     *
     * @return the configured {@link Sampler}.
     */
    private Sampler getConfiguredSampler() {

        String configuredSampler = configuration.getFirstProperty(TelemetryConstants.OTLP_CONFIG_SAMPLER);
        if (StringUtils.isEmpty(configuredSampler)) {
            return Sampler.parentBased(Sampler.alwaysOn());
        }
        if (StringUtils.equalsIgnoreCase(configuredSampler, TelemetryConstants.SAMPLER_ALWAYS_ON)) {
            return Sampler.alwaysOn();
        } else if (StringUtils.equalsIgnoreCase(configuredSampler, TelemetryConstants.SAMPLER_ALWAYS_OFF)) {
            return Sampler.alwaysOff();
        } else if (StringUtils.equalsIgnoreCase(configuredSampler, TelemetryConstants.SAMPLER_PARENT_BASED)) {
            return Sampler.parentBased(Sampler.alwaysOn());
        }
        log.warn("Unknown OpenTelemetry sampler '" + configuredSampler + "' configured. Falling back to the "
                + "default parent-based (always-on) sampler.");
        return Sampler.parentBased(Sampler.alwaysOn());
    }

}
