package org.wso2.carbon.apimgt.tracing;

import com.google.common.collect.ImmutableMap;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    public static TracingSpan startSpan(String spanName, TracingSpan parentSpan, TracingTracer tracer) {

        if(parentSpan == null) {
            Span span = tracer.getTracingTracer().buildSpan(spanName).start();
            return new TracingSpan(span);

        } else {
            Object sp = parentSpan.getSpan();
            Span childSpan;
            if (sp instanceof Span) {
                childSpan = tracer.getTracingTracer().buildSpan(spanName).asChildOf((Span) sp).start();

            } else {
                childSpan = tracer.getTracingTracer().buildSpan(spanName).asChildOf((SpanContext) sp).start();

            }
            return new TracingSpan(childSpan);
        }
    }

    public static void setTag(TracingSpan span, String key, String value) {

        Object sp = span.getSpan();
        if(sp instanceof Span) {
            ((Span) sp).setTag(key, value);
        }
    }

    public static void setLog(TracingSpan span, String key, String value) {

        Object sp = span.getSpan();
        if(sp instanceof Span) {
            ((Span) sp).log(ImmutableMap.of(key, value));
        }
    }

    public static void setLog(TracingSpan span, String key) {

        Object sp = span.getSpan();
        if(sp instanceof Span) {
            ((Span) sp).log(key);
        }
    }

    public static void finishSpan(TracingSpan span) {

        Object sp = span.getSpan();
        if(sp instanceof Span) {
            ((Span) sp).finish();
        }
    }

    public static void inject(TracingSpan span, TracingTracer tracer, Map<String, String> tracerSpecificCarrier) {

        Object sp = span.getSpan();
        if(sp instanceof Span) {
            tracer.getTracingTracer().inject(((Span) sp).context(), Format.Builtin.HTTP_HEADERS,
                    new TextMapInjectAdapter(tracerSpecificCarrier));

        } else if(sp instanceof SpanContext) {
            tracer.getTracingTracer().inject((SpanContext) sp, Format.Builtin.HTTP_HEADERS,
                    new TextMapInjectAdapter(tracerSpecificCarrier));
        }
    }

    public static TracingSpan extract(TracingTracer tracer, Map<String, String> headerMap) {

        return new TracingSpan(tracer.getTracingTracer().extract(Format.Builtin.HTTP_HEADERS,
                new TextMapExtractAdapter(headerMap)));
    }

    public static TracingTracer getGlobalTracer() {

        return new TracingTracer(GlobalTracer.get());
    }

    public static void baggageSet(TracingSpan span, String key, String value) {

        Object sp = span.getSpan();
        if(sp instanceof Span) {
            ((Span) sp).setBaggageItem(key, value);
        }
    }

    public static String baggageGet(TracingSpan span, String key) {

        Object sp = span.getSpan();
        if(sp instanceof Span) {
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
}
