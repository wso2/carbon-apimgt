package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.ResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ResourcesApiServiceImpl;

public class ResourcesApiServiceFactory {
    private static final ResourcesApiService service = new ResourcesApiServiceImpl();

    public static ResourcesApiService getResourcesApi() {
        return service;
    }
}
