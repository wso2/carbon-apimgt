package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.ApplicationsApiServiceImpl;

public class ApplicationsApiServiceFactory {
    private static final ApplicationsApiService service = new ApplicationsApiServiceImpl();

    public static ApplicationsApiService getApplicationsApi() {
        return service;
    }
}
