package org.wso2.carbon.apimgt.impl.notification.util;

import org.wso2.carbon.apimgt.impl.notification.NotificationExecutor;
import org.wso2.carbon.registry.core.Registry;

public class NotificationExecutorWrapper extends NotificationExecutor {
    Registry registry;

    public NotificationExecutorWrapper(Registry registry) {
        this.registry = registry;
    }

    @Override
    protected Registry getRegistry(int tenantId) {
        return registry;
    }
}
