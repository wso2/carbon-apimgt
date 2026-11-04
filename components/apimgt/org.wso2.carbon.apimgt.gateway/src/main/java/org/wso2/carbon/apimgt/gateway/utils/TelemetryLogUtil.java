/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import org.apache.logging.log4j.ThreadContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetrySpan;

/**
 * Sets and clears the OpenTelemetry trace and span IDs that log4j2 reads from the ThreadContext, so that they can be
 * printed in the logs using %X{trace_id} and %X{span_id}.
 * <p>
 * The gateway spans are kept in the Synapse message context, while log4j2 reads from the ThreadContext of the
 * thread that writes the log. As the request and the response of a message are mediated on different threads, the
 * IDs are set on each flow from the spans of the message being mediated, and removed when the flow leaves the
 * gateway handlers so that the thread does not log them for the next message it mediates.
 */
public final class TelemetryLogUtil {

    private TelemetryLogUtil() {
    }

    /**
     * Replaces the trace and span IDs on this thread with the ones of the given span.
     *
     * @param telemetrySpan span whose IDs should be logged
     */
    public static void setTracingIds(TelemetrySpan telemetrySpan) {

        clearTracingIds();
        if (telemetrySpan == null) {
            return;
        }
        Object sp = telemetrySpan.getSpan();
        Span span = null;
        if (sp instanceof Span) {
            span = (Span) sp;
        } else if (sp instanceof Context) {
            span = Span.fromContext((Context) sp);
        }
        if (span == null) {
            return;
        }
        SpanContext spanContext = span.getSpanContext();
        if (spanContext.isValid()) {
            ThreadContext.put(APIMgtGatewayConstants.TRACE_ID_LOG_KEY, spanContext.getTraceId());
            ThreadContext.put(APIMgtGatewayConstants.SPAN_ID_LOG_KEY, spanContext.getSpanId());
        }
    }

    /**
     * Sets the IDs of a span that a handler opens and finishes within its own processing, such as the key validation
     * span, and returns the IDs it replaces. The returned IDs must be passed to {@link #exitSpan} when the span
     * finishes, so that the logs written after it show the span that was active before it was opened.
     *
     * @param telemetrySpan span whose IDs should be logged
     * @return the IDs that were active before the span was opened, or null if there is no span
     */
    public static TracingIds enterSpan(TelemetrySpan telemetrySpan) {

        if (telemetrySpan == null) {
            return null;
        }
        TracingIds previous = new TracingIds(ThreadContext.get(APIMgtGatewayConstants.TRACE_ID_LOG_KEY),
                ThreadContext.get(APIMgtGatewayConstants.SPAN_ID_LOG_KEY));
        setTracingIds(telemetrySpan);
        previous.openedSpanId = ThreadContext.get(APIMgtGatewayConstants.SPAN_ID_LOG_KEY);
        return previous;
    }

    /**
     * Restores the IDs that were active before a span opened with {@link #enterSpan} was opened. Nothing is restored
     * if the IDs were changed or cleared while the span was open, such as when the response was sent from within the
     * handler, so that IDs of a message that has already completed are not put back on the thread.
     *
     * @param previous IDs returned by {@link #enterSpan}, or null if the span was not opened
     */
    public static void exitSpan(TracingIds previous) {

        if (previous == null || previous.openedSpanId == null
                || !previous.openedSpanId.equals(ThreadContext.get(APIMgtGatewayConstants.SPAN_ID_LOG_KEY))) {
            return;
        }
        restore(APIMgtGatewayConstants.TRACE_ID_LOG_KEY, previous.traceId);
        restore(APIMgtGatewayConstants.SPAN_ID_LOG_KEY, previous.spanId);
    }

    /**
     * Removes the trace and span IDs from this thread.
     */
    public static void clearTracingIds() {

        ThreadContext.remove(APIMgtGatewayConstants.TRACE_ID_LOG_KEY);
        ThreadContext.remove(APIMgtGatewayConstants.SPAN_ID_LOG_KEY);
    }

    private static void restore(String key, String value) {

        if (value == null) {
            ThreadContext.remove(key);
        } else {
            ThreadContext.put(key, value);
        }
    }

    /**
     * Trace and span IDs that were active on a thread before a span was opened.
     */
    public static final class TracingIds {

        private final String traceId;
        private final String spanId;
        private String openedSpanId;

        private TracingIds(String traceId, String spanId) {

            this.traceId = traceId;
            this.spanId = spanId;
        }
    }
}
