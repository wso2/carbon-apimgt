package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

public abstract class OpenTracer {

    public abstract Tracer getTracer(String tracerName, APIManagerConfiguration configuration, String serviceName);

    public abstract String getName();

    public Span startSpan(String spanName, Span parentSpan, Tracer tracer) {

        if (parentSpan == null) {
            Span span = tracer.buildSpan(spanName).start();
            return span;

        } else {
            Span childSpan = tracer.buildSpan(spanName).asChildOf(parentSpan).start();
            return childSpan;
        }
    }

    public void finishSpan(Span span) {
        span.finish();
    }

}