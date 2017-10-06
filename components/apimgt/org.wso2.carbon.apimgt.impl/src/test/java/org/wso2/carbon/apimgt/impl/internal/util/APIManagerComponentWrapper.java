package org.wso2.carbon.apimgt.impl.internal.util;

import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.registry.api.Registry;

public class APIManagerComponentWrapper extends APIManagerComponent {
    private Registry registry;

    public APIManagerComponentWrapper(Registry registry) {
        this.registry = registry;
    }

    @Override
    protected Registry getRegistry() {
        return this.registry;
    }
}
