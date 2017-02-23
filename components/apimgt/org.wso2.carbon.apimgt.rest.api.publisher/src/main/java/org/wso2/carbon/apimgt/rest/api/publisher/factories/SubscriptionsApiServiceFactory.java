package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.SubscriptionsApiServiceImpl;

public class SubscriptionsApiServiceFactory {
    private static final SubscriptionsApiService service = new SubscriptionsApiServiceImpl();

    public static SubscriptionsApiService getSubscriptionsApi() {
        return service;
    }
}
