package org.wso2.carbon.apimgt.rest.api.analytics.factories;

import org.wso2.carbon.apimgt.rest.api.analytics.ApplicationApiService;
import org.wso2.carbon.apimgt.rest.api.analytics.impl.ApplicationApiServiceImpl;

public class ApplicationApiServiceFactory {
    private static final ApplicationApiService service = new ApplicationApiServiceImpl();

    public static ApplicationApiService getApplicationApi() {
        return service;
    }
}
