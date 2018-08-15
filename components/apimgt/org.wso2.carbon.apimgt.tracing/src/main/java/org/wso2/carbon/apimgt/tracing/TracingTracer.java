package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;

public class TracingTracer {

    private Tracer tracer;

    public TracingTracer( Tracer tracer ) {
        this.tracer = tracer;
    }

    public Tracer getTracingTracer(){
        return this.tracer;
    }
}
