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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.jetbrains.annotations.NotNull;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

import java.util.Map;

/**
 * Span utility class
 */

public class TelemetryUtil {

    /**
     * Start the telemetry tracing span
     *
     * @param spanName
     * @param parentSpan
     * @param tracer     io.opentelemetry.api.trace tracer
     * @return a TelemetrySpan object
     */
    public static TelemetrySpan startSpan(String spanName, TelemetrySpan parentSpan, TelemetryTracer tracer) {

        Span childSpan;
        if (parentSpan == null) {
            Span span = tracer.getTelemetryTracingTracer().spanBuilder(spanName).startSpan();
            return new TelemetrySpan(span);
        } else {
            Object sp = parentSpan.getSpan();
            if (sp != null) {
                if (sp instanceof Span) {
                    childSpan = tracer.getTelemetryTracingTracer().spanBuilder(spanName).setParent(Context.current()
                            .with((Span) sp)).startSpan();
                } else {
                    childSpan =
                            tracer.getTelemetryTracingTracer().spanBuilder(spanName)
                                    .setParent((Context) sp).startSpan();
                }
            } else {
                childSpan = tracer.getTelemetryTracingTracer().spanBuilder(spanName).startSpan();
            }
        }
        return new TelemetrySpan(childSpan);
    }

    /**
     * Set tag to the span
     *
     * @param span
     * @param key
     * @param value
     */
    public static void setTag(TelemetrySpan span, String key, String value) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).setAttribute(key, value);
        }
    }

    /**
     * Update operation to the span
     *
     * @param span
     * @param name
     */
    public static void updateOperation(TelemetrySpan span, String name) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).updateName(name);
        }
    }

    public static void setLog(TelemetrySpan span, String key, String value) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).addEvent("event", Attributes.of(AttributeKey.stringKey(key), value));
        }
    }

    /**
     * Finish the span
     *
     * @param span
     */
    public static void finishSpan(TelemetrySpan span) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).end();
        }
    }

    /**
     * Inject tracer specific information to tracerSpecificCarrier
     *
     * @param span
     * @param tracerSpecificCarrier
     */
    public static void inject(TelemetrySpan span, Map<String, String> tracerSpecificCarrier) {

        OpenTelemetry openTelemetry = TelemetryServiceImpl.getInstance().getOpenTelemetry();
        TextMapSetter<Map<String, String>> setter = new TextMapSetter<Map<String, String>>() {
            @Override
            public void set(@NotNull Map<String, String> tracerSpecificCarrier, String key, String value) {

                tracerSpecificCarrier.put(key, value);
            }
        };

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            try (Scope scope = ((Span) sp).makeCurrent()) {
                openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), tracerSpecificCarrier
                        , setter);
            }
        }
    }

    /**
     * Inject tracer specific information to tracerSpecificCarrier
     *
     * @param tracerSpecificCarrier
     */
    public static TelemetrySpan extract(Map<String, String> tracerSpecificCarrier) {

        OpenTelemetry openTelemetry = TelemetryServiceImpl.getInstance().getOpenTelemetry();
        TextMapGetter<Map<String, String>> getter =
                new TextMapGetter<Map<String, String>>() {
                    public String get(@NotNull Map<String, String> tracerSpecificCarrier, String key) {

                        if (tracerSpecificCarrier.containsKey(key)) {
                            return tracerSpecificCarrier.get(key);
                        }
                        return null;
                    }

                    public Iterable<String> keys(Map<String, String> tracerSpecificCarrier) {

                        return tracerSpecificCarrier.keySet();
                    }
                };

        return new TelemetrySpan((Span) openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), tracerSpecificCarrier, getter));
    }

    public static TelemetryTracer getGlobalTracer() {

        return new TelemetryTracer(GlobalOpenTelemetry.getTracer("org.wso2.carbon.apimgt.tracing.telemetry"));
    }

    public static boolean telemetryEnabled() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            boolean remoteTracerEnabled =
                    Boolean.parseBoolean(apiManagerConfiguration
                            .getFirstProperty(TelemetryConstants.REMOTE_TELEMETRY_TRACER_ENABLED));
            boolean logTracerEnabled =
                    Boolean.parseBoolean(apiManagerConfiguration
                            .getFirstProperty(TelemetryConstants.LOG_TELEMETRY_TRACER_ENABLED));
            return remoteTracerEnabled || logTracerEnabled;
        }
        return false;
    }
}
