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
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import org.apache.logging.log4j.ThreadContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetrySpan;

/**
 * Tests for {@link TelemetryLogUtil}, which keeps the OpenTelemetry trace and span IDs in the log4j2 ThreadContext.
 */
public class TelemetryLogUtilTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String RESOURCE_SPAN_ID = "00f067aa0ba902b7";
    private static final String HANDLER_SPAN_ID = "53995c3f42cd8ad8";
    private static final String NESTED_SPAN_ID = "a2fb4a1d1a96d312";
    private static final String OTHER_TRACE_ID = "0af7651916cd43dd8448eb211c80319c";
    private static final String OTHER_SPAN_ID = "b7ad6b7169203331";

    @Before
    public void setUp() {

        ThreadContext.clearMap();
    }

    @After
    public void tearDown() {

        ThreadContext.clearMap();
    }

    @Test
    public void testSetTracingIdsFromSpan() {

        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));

        assertTracingIds(TRACE_ID, RESOURCE_SPAN_ID);
    }

    @Test
    public void testSetTracingIdsFromContext() {

        Span span = Span.wrap(spanContext(TRACE_ID, RESOURCE_SPAN_ID));
        TelemetryLogUtil.setTracingIds(new TelemetrySpan(Context.root().with(span)));

        assertTracingIds(TRACE_ID, RESOURCE_SPAN_ID);
    }

    @Test
    public void testSetTracingIdsReplacesExistingIds() {

        TelemetryLogUtil.setTracingIds(spanOf(OTHER_TRACE_ID, OTHER_SPAN_ID));
        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));

        assertTracingIds(TRACE_ID, RESOURCE_SPAN_ID);
    }

    @Test
    public void testSetTracingIdsWithNullSpanClearsExistingIds() {

        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));
        TelemetryLogUtil.setTracingIds(null);

        assertTracingIds(null, null);
    }

    @Test
    public void testSetTracingIdsWithInvalidSpanClearsExistingIds() {

        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));
        TelemetryLogUtil.setTracingIds(new TelemetrySpan(Span.getInvalid()));

        assertTracingIds(null, null);
    }

    @Test
    public void testClearTracingIdsRemovesOnlyTracingIds() {

        ThreadContext.put("Correlation-ID", "correlation-id-1");
        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));

        TelemetryLogUtil.clearTracingIds();

        assertTracingIds(null, null);
        Assert.assertEquals("correlation-id-1", ThreadContext.get("Correlation-ID"));
    }

    @Test
    public void testEnterSpanWithNullSpanLeavesIdsUnchanged() {

        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));

        TelemetryLogUtil.TracingIds previous = TelemetryLogUtil.enterSpan(null);
        TelemetryLogUtil.exitSpan(previous);

        Assert.assertNull(previous);
        assertTracingIds(TRACE_ID, RESOURCE_SPAN_ID);
    }

    @Test
    public void testEnterSpanSetsIdsAndExitSpanRestoresPreviousIds() {

        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));

        TelemetryLogUtil.TracingIds previous = TelemetryLogUtil.enterSpan(spanOf(TRACE_ID, HANDLER_SPAN_ID));
        assertTracingIds(TRACE_ID, HANDLER_SPAN_ID);

        TelemetryLogUtil.exitSpan(previous);
        assertTracingIds(TRACE_ID, RESOURCE_SPAN_ID);
    }

    @Test
    public void testExitSpanRemovesIdsWhenNoneWereSetBeforeEnterSpan() {

        TelemetryLogUtil.TracingIds previous = TelemetryLogUtil.enterSpan(spanOf(TRACE_ID, HANDLER_SPAN_ID));
        TelemetryLogUtil.exitSpan(previous);

        assertTracingIds(null, null);
    }

    @Test
    public void testNestedSpansRestoreIdsInOrder() {

        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));

        TelemetryLogUtil.TracingIds outer = TelemetryLogUtil.enterSpan(spanOf(TRACE_ID, HANDLER_SPAN_ID));
        TelemetryLogUtil.TracingIds inner = TelemetryLogUtil.enterSpan(spanOf(TRACE_ID, NESTED_SPAN_ID));
        assertTracingIds(TRACE_ID, NESTED_SPAN_ID);

        TelemetryLogUtil.exitSpan(inner);
        assertTracingIds(TRACE_ID, HANDLER_SPAN_ID);

        TelemetryLogUtil.exitSpan(outer);
        assertTracingIds(TRACE_ID, RESOURCE_SPAN_ID);
    }

    @Test
    public void testExitSpanDoesNotRestoreIdsClearedWhileSpanWasOpen() {

        // A handler that sends the response itself completes the message, which clears the IDs before its span ends
        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));
        TelemetryLogUtil.TracingIds previous = TelemetryLogUtil.enterSpan(spanOf(TRACE_ID, HANDLER_SPAN_ID));

        TelemetryLogUtil.clearTracingIds();
        TelemetryLogUtil.exitSpan(previous);

        assertTracingIds(null, null);
    }

    @Test
    public void testExitSpanDoesNotOverwriteIdsSetWhileSpanWasOpen() {

        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));
        TelemetryLogUtil.TracingIds previous = TelemetryLogUtil.enterSpan(spanOf(TRACE_ID, HANDLER_SPAN_ID));

        TelemetryLogUtil.setTracingIds(spanOf(OTHER_TRACE_ID, OTHER_SPAN_ID));
        TelemetryLogUtil.exitSpan(previous);

        assertTracingIds(OTHER_TRACE_ID, OTHER_SPAN_ID);
    }

    @Test
    public void testExitSpanWithNullIsNoOp() {

        TelemetryLogUtil.setTracingIds(spanOf(TRACE_ID, RESOURCE_SPAN_ID));

        TelemetryLogUtil.exitSpan(null);

        assertTracingIds(TRACE_ID, RESOURCE_SPAN_ID);
    }

    private static TelemetrySpan spanOf(String traceId, String spanId) {

        return new TelemetrySpan(Span.wrap(spanContext(traceId, spanId)));
    }

    private static SpanContext spanContext(String traceId, String spanId) {

        return SpanContext.create(traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());
    }

    private static void assertTracingIds(String expectedTraceId, String expectedSpanId) {

        Assert.assertEquals(expectedTraceId, ThreadContext.get(APIMgtGatewayConstants.TRACE_ID_LOG_KEY));
        Assert.assertEquals(expectedSpanId, ThreadContext.get(APIMgtGatewayConstants.SPAN_ID_LOG_KEY));
    }
}
