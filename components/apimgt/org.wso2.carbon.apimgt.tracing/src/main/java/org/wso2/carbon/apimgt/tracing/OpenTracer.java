package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;

import javax.cache.InvalidConfigurationException;

public interface OpenTracer {

    void init() throws InvalidConfigurationException;

    Tracer getTracer(String tracerName, String serviceName);

    String getName();
}
