package org.wso2.carbon.apimgt.tracing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.tracing.TracingService;
import org.wso2.carbon.apimgt.tracing.TracingServiceImpl;


/**
 * @scr.component name="org.wso2.carbon.apimgt.tracing.internal.TracingServiceComponent" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
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

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }
}
