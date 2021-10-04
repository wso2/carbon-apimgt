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

import com.google.common.collect.ImmutableMap;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.util.GlobalTracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

import java.util.Map;

/**
 * Span utility class
 */
public class Util {

    /**
     * Start the tracing span
     *
     * @param spanName
     * @param parentSpan
     * @param tracer     io.opentracing tracer
     * @return a TracingSpan object
     */
    public static TracingSpan startSpan(String spanName, TracingSpan parentSpan, TracingTracer tracer) {

        if (parentSpan == null) {
            Span span = tracer.getTracingTracer().buildSpan(spanName).start();
            return new TracingSpan(span);
        } else {
            Object sp = parentSpan.getSpan();
            Span childSpan;
            if (sp != null) {
                if (sp instanceof Span) {
                    childSpan = tracer.getTracingTracer().buildSpan(spanName).asChildOf((Span) sp).start();
                } else {
                    childSpan = tracer.getTracingTracer().buildSpan(spanName).asChildOf((SpanContext) sp).start();
                }
            } else {
                childSpan = tracer.getTracingTracer().buildSpan(spanName).start();
            }
            return new TracingSpan(childSpan);
        }
    }

    /**
     * Set tag to the span
     *
     * @param span
     * @param key
     * @param value
     */
    public static void setTag(TracingSpan span, String key, String value) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).setTag(key, value);
        }
    }
    /**
     * Update operation to the span
     *
     * @param span
     * @param name
     */
    public static void updateOperation(TracingSpan span,  String name) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).setOperationName(name);
        }
    }

    public static void setLog(TracingSpan span, String key, String value) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).log(ImmutableMap.of(key, value));
        }
    }

    public static void setLog(TracingSpan span, String key) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).log(key);
        }
    }

    /**
     * Finish the span
     *
     * @param span
     */
    public static void finishSpan(TracingSpan span) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).finish();
        }
    }

    /**
     * Inject tracer specific information to tracerSpecificCarrier
     *
     * @param span
     * @param tracer
     * @param tracerSpecificCarrier
     */
    public static void inject(TracingSpan span, TracingTracer tracer, Map<String, String> tracerSpecificCarrier) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            tracer.getTracingTracer().inject(((Span) sp).context(), Format.Builtin.HTTP_HEADERS,
                    new TextMapInjectAdapter(tracerSpecificCarrier));
        } else if (sp instanceof SpanContext) {
            tracer.getTracingTracer().inject((SpanContext) sp, Format.Builtin.HTTP_HEADERS,
                    new TextMapInjectAdapter(tracerSpecificCarrier));
        }
    }

    /**
     * Extract the tracer specific information from headerMap
     *
     * @param tracer
     * @param headerMap
     * @return a TracingSpan object
     */
    public static TracingSpan extract(TracingTracer tracer, Map<String, String> headerMap) {

        return new TracingSpan(tracer.getTracingTracer().extract(Format.Builtin.HTTP_HEADERS,
                new TextMapExtractAdapter(headerMap)));
    }

    public static TracingTracer getGlobalTracer() {

        return new TracingTracer(GlobalTracer.get());
    }

    /**
     * Set the baggage item to the span which can be passed to the distributed systems
     *
     * @param span
     * @param key
     * @param value
     */
    public static void baggageSet(TracingSpan span, String key, String value) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).setBaggageItem(key, value);
        }
    }

    /**
     * Get the baggage item from the span context
     *
     * @param span
     * @param key
     * @return a baggage item String
     */
    public static String baggageGet(TracingSpan span, String key) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            return ((Span) sp).getBaggageItem(key);
        } else if (sp instanceof SpanContext) {
            Iterable<Map.Entry<String, String>> entries = ((SpanContext) sp).baggageItems();
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getKey().equals(key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public static boolean tracingEnabled() {
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            boolean remoteTracerEnabled =
                    Boolean.parseBoolean(apiManagerConfiguration
                            .getFirstProperty(TracingConstants.REMOTE_TRACER_ENABLED));
            boolean logTracerEnabled =
                    Boolean.parseBoolean(apiManagerConfiguration
                            .getFirstProperty(TracingConstants.LOG_TRACER_ENABLED));
            return remoteTracerEnabled || logTracerEnabled;
        }
        return false;
    }
}
