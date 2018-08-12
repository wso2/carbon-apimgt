package org.wso2.carbon.apimgt.tracing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.tracing.TracingService;
import org.wso2.carbon.apimgt.tracing.TracingServiceImpl;


/**
 * @scr.component name="org.wso2.carbon.apimgt.tracing.internal.TracingServiceComponent" immediate="true"
 */
public class TracingServiceComponent {

    private static final Log log = LogFactory.getLog(TracingServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        try {
            log.info("*******Tracing Component activated**********");
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(TracingService.class, new TracingServiceImpl(), null);

        } catch (Throwable t) {
            log.error("Error occured", t);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        log.info("*******Tracing Component deactivated**********");
    }
}
