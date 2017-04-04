package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.SubscriptionsApiServiceImpl;

public class SubscriptionsApiServiceFactory {
    private static final SubscriptionsApiService service = new SubscriptionsApiServiceImpl();

    public static SubscriptionsApiService getSubscriptionsApi() {
        return service;
    }
}
