package org.wso2.carbon.apimgt.notification.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.APIKeyLastUsedNotifier;
import org.wso2.carbon.apimgt.notification.APIKeyLastUsedNotifierImpl;

/**
 * This class is used to activate Api key last used time notification bundle.
 */
@Component(name = "apim.api.key.last.used.notifier.component", immediate = true)
public class ApiKeyLastUsedNotifierComponent {
    @Activate
    protected void activate() {
        APIKeyLastUsedNotifier notifier = new APIKeyLastUsedNotifierImpl();
        ServiceReferenceHolder.getInstance().setApiKeyLastUsedNotifier(notifier);
    }

    @Deactivate
    protected void deactivate() {
        ServiceReferenceHolder.getInstance().setApiKeyLastUsedNotifier(null);
    }
}
