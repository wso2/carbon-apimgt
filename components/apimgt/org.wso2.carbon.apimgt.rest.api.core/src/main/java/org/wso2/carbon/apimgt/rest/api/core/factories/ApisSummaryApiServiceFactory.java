package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.ApisSummaryApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ApisSummaryApiServiceImpl;

public class ApisSummaryApiServiceFactory {
    private static final ApisSummaryApiService service = new ApisSummaryApiServiceImpl();

    public static ApisSummaryApiService getApisSummaryApi() {
        return service;
    }
}
