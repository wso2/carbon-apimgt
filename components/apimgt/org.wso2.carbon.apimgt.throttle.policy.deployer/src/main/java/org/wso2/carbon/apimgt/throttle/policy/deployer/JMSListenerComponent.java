package org.wso2.carbon.apimgt.throttle.policy.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

@Component(
        name = "org.wso2.apimgt.throttle.policy.deployer",
        immediate = true)
public class JMSListenerComponent {

    private static final Log log = LogFactory.getLog(JMSListenerComponent.class);

    private ServiceRegistration registration;

    @Activate
    protected void activate(ComponentContext context) {

        log.debug("Activating component...");
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        if (configuration == null) {
            log.warn("API Manager Configuration not properly set.");
            return;
        }
        JMSListenerStartupShutdownListener jmsListenerStartupShutdownListener =
                new JMSListenerStartupShutdownListener();
        registration = context.getBundleContext()
                .registerService(ServerStartupObserver.class, jmsListenerStartupShutdownListener, null);
        registration = context.getBundleContext()
                .registerService(ServerShutdownHandler.class, jmsListenerStartupShutdownListener, null);
    }



    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {

        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {

        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Deactivating component");
        }
        if (this.registration != null) {
            this.registration.unregister();
        }
    }



}
