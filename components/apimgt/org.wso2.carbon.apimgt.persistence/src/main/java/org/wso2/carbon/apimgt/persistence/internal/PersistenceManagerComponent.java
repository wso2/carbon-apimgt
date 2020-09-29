package org.wso2.carbon.apimgt.persistence.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

@Component(name = "api.keymgt.component", immediate = true)
public class PersistenceManagerComponent {

    private static Log log = LogFactory.getLog(PersistenceManagerComponent.class);

    private ServiceRegistration serviceRegistration = null;

    @Activate protected void activate(ComponentContext ctxt) {

    }

    @Deactivate protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (log.isDebugEnabled()) {
            log.info("Key Manager User Operation Listener is deactivated.");
        }
    }

    @Reference(name = "registry.service", service = org.wso2.carbon.registry.core.service.RegistryService.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, unbind = "unsetRegistryService") protected void setRegistryService(
                                    RegistryService registryService) {
        PersistenceMgtDataHolder.setRegistryService(registryService);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        PersistenceMgtDataHolder.setRegistryService(null);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is unset in the API KeyMgt bundle.");
        }
    }
}

