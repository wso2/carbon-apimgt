package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Span;

public class TracingSpan{

    private Span span;

    public TracingSpan( Span span ) {
        this.span = span;
    }

    public Span getSpan(){
        return this.span;
    }

}
