package org.wso2.carbon.apimgt.rest.api.analytics.factories;

import org.wso2.carbon.apimgt.rest.api.analytics.SubscriptionApiService;
import org.wso2.carbon.apimgt.rest.api.analytics.impl.SubscriptionApiServiceImpl;

public class SubscriptionApiServiceFactory {
    private static final SubscriptionApiService service = new SubscriptionApiServiceImpl();

    public static SubscriptionApiService getSubscriptionApi() {
        return service;
    }
}
