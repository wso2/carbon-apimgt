package org.wso2.carbon.apimgt.tracing;

public interface TracingService {

    TracingTracer buildTracer(String serviceName);

}
