package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.SubscriptionsApiServiceImpl;

public class SubscriptionsApiServiceFactory {
    private static final SubscriptionsApiService service = new SubscriptionsApiServiceImpl();

    public static SubscriptionsApiService getSubscriptionsApi() {
        return service;
    }
}
