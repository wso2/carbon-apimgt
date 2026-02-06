package org.wso2.carbon.apimgt.notification.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.OpaqueAPIKeyNotifier;
import org.wso2.carbon.apimgt.notification.OpaqueAPIKeyNotifierImpl;

/**
 * This class is used to activate Api key info notification bundle.
 */
@Component(name = "apim.api.key.notifier.component", immediate = true)
public class OpaqueApiKeyNotifierComponent {
    @Activate
    protected void activate() {
        OpaqueAPIKeyNotifier notifier = new OpaqueAPIKeyNotifierImpl();
        ServiceReferenceHolder.getInstance().setOpaqueApiKeyNotifier(notifier);
    }

    @Deactivate
    protected void deactivate() {
        ServiceReferenceHolder.getInstance().setOpaqueApiKeyNotifier(null);
    }
}
