package org.wso2.carbon.apimgt.tracing;

import com.google.common.collect.ImmutableMap;
import io.opentracing.Span;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    public static TracingSpan startSpan(String spanName, TracingSpan parentSpan, TracingTracer tracer) {

        if (parentSpan == null) {
            Span span = tracer.getTracingTracer().buildSpan(spanName).start();
            return new TracingSpan(span);

        } else {
            Span childSpan = tracer.getTracingTracer().buildSpan(spanName).asChildOf(parentSpan.getSpan()).start();
            return new TracingSpan(childSpan);
        }
    }

    public static void setTag(TracingSpan span, String key, String value) {

        span.getSpan().setTag(key, value);
    }

    public static void setLog(TracingSpan span, String key, String value) {

        span.getSpan().log(ImmutableMap.of(key, value));
    }

    public static void finishSpan(TracingSpan span) {

        span.getSpan().finish();
    }

}
