package org.wso2.carbon.apimgt.rest.api.common.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

/**
 * Class used to activate configuration loading
 */

@Component(
        name = "org.wso2.carbon.apimgt.commons",
        immediate = true
)

public class ConfigurationActivator {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationActivator.class);
    /**
     * Get the ConfigProvider service.
     * This is the bind method that gets called for ConfigProvider service registration that satisfy the policy.
     *
     * @param configProvider the ConfigProvider service that is registered as a service.
     */
    @Reference(name = "carbon.config.provider", service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider")
    protected void registerConfigProvider(ConfigProvider configProvider) {
        ServiceReferenceHolder.getInstance().setConfigProvider(configProvider);

    }

    /**
     * This is the unbind method for the above reference that gets called for ConfigProvider instance un-registrations.
     *
     * @param configProvider the ConfigProvider service that get unregistered.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        ServiceReferenceHolder.getInstance().setConfigProvider(null);
    }
}

