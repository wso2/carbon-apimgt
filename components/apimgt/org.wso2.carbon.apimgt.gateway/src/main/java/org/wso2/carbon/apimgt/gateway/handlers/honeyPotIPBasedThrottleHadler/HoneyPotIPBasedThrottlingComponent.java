package org.wso2.carbon.apimgt.gateway.handlers.honeyPotIPBasedThrottleHadler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

public class HoneyPotIPBasedThrottlingComponent {
    private static final Log log = LogFactory.getLog(HoneyPotIPBasedThrottlingComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("IPBasedThrottlingComponent activated");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("IPBasedThrottlingComponent deactivated");
        }
    }
}
