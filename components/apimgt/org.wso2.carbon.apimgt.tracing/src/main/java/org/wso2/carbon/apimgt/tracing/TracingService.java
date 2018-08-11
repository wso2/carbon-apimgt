package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;

public interface TracingService {

    Tracer getTracer(String serviceName);

}
