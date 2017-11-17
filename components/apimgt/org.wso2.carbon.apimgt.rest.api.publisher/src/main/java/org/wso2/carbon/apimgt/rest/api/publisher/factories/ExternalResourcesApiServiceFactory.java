package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ExternalResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ExternalResourcesApiServiceImpl;

public class ExternalResourcesApiServiceFactory {
    private static final ExternalResourcesApiService service = new ExternalResourcesApiServiceImpl();

    public static ExternalResourcesApiService getExternalResourcesApi() {
        return service;
    }
}
