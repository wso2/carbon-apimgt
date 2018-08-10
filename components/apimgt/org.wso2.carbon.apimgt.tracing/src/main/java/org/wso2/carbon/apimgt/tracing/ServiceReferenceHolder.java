package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private APIManagerConfigurationService amConfigService;

    private Tracer tracer;

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public Tracer getTracer() {
        return tracer;
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigService) {
        this.amConfigService = amConfigService;
    }


}
