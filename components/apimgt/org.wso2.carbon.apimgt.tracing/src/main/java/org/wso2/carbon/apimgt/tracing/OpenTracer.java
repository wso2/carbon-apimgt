package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

public interface OpenTracer {

    Tracer getTracer(String tracerName, APIManagerConfiguration configuration, String serviceName);

    String getName();
}
