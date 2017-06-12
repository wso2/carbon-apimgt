package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ApplicationsApiServiceImpl;

public class ApplicationsApiServiceFactory {
    private static final ApplicationsApiService service = new ApplicationsApiServiceImpl();

    public static ApplicationsApiService getApplicationsApi() {
        return service;
    }
}
