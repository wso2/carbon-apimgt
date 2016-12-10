package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.ValidateSubscriptionApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ValidateSubscriptionApiServiceImpl;

public class ValidateSubscriptionApiServiceFactory {
    private final static ValidateSubscriptionApiService service = new ValidateSubscriptionApiServiceImpl();

    public static ValidateSubscriptionApiService getValidateSubscriptionApi() {
        return service;
    }
}
