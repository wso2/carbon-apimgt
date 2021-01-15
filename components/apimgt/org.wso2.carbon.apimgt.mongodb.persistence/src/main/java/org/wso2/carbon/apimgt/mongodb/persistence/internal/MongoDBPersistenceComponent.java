package org.wso2.carbon.apimgt.mongodb.persistence.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.mongodb.persistence.MongoDBPersistenceImpl;

@Component(
        name = "org.wso2.carbon.apimgt.mongodb.persistence",
        immediate = true)
public class MongoDBPersistenceComponent {


    private ServiceRegistration serviceRegistration = null;
    private static final Log log = LogFactory.getLog(MongoDBPersistenceImpl.class);

    @Activate
    protected void activate(ComponentContext context) {
        serviceRegistration = context.getBundleContext().registerService(APIPersistence.class.getName(),
                new MongoDBPersistenceImpl(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (log.isDebugEnabled()) {
            log.info("Mongodb service deactivated.");
        }
    }
}
