package org.wso2.carbon.apimgt.rest.api.analytics.factories;

import org.wso2.carbon.apimgt.rest.api.analytics.ApiApiService;
import org.wso2.carbon.apimgt.rest.api.analytics.impl.ApiApiServiceImpl;

public class ApiApiServiceFactory {
    private static final ApiApiService service = new ApiApiServiceImpl();

    public static ApiApiService getApiApi() {
        return service;
    }
}
