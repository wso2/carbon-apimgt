package org.wso2.carbon.apimgt.tracing.internal;


import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.tracing.TracingService;
import org.wso2.carbon.apimgt.tracing.TracingServiceImpl;

@Component(name = "org.wso2.carbon.apimgt.tracing.internal.TracingServiceComponent",
        immediate = true)
public class TracingServiceComponent {

    @Activate
    protected void activate(BundleContext bundleContext) {

        bundleContext.registerService(TracingService.class, new TracingServiceImpl(), null);
    }
}
